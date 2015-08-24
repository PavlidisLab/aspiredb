/*
 * The ASPIREdb project
 * 
 * Copyright (c) 2006 University of British Columbia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package ubc.pavlab.aspiredb.server.controller;

import gemma.gsec.authentication.LoginDetailsValueObject;
import gemma.gsec.authentication.UserDetailsImpl;
import gemma.gsec.authentication.UserManager;
import gemma.gsec.util.JSONUtil;
import gemma.gsec.util.SecurityUtil;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.tanesha.recaptcha.ReCaptchaImpl;
import net.tanesha.recaptcha.ReCaptchaResponse;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import ubc.pavlab.aspiredb.server.security.authentication.UserService;
import ubc.pavlab.aspiredb.server.util.ConfigUtils;

/**
 * Controller to signup new users. See also the {@see UserListController}.
 * 
 * @author pavlidis
 * @author keshav
 * @version $Id: SignupController.java,v 1.32 2014/06/19 21:42:36 ptan Exp $
 */
@Controller
public class SignupController extends BaseController {

    public static final int MIN_PASSWORD_LENGTH = 6;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @Autowired
    private UserManager userManager;

    private RecaptchaTester recaptchaTester = new DefaultRecaptchaTester();

    @RequestMapping(value = "/ajaxLoginCheck.html")
    public void ajaxLoginCheck( HttpServletRequest request, HttpServletResponse response ) throws Exception {

        JSONUtil jsonUtil = new JSONUtil( request, response );

        String jsonText = "{\"success\":false}";
        String userName = null;

        try {

            if ( userManager.loggedIn() ) {
                userName = userManager.getCurrentUsername();
                log.info( userName + " has logged in." );
                jsonText = "{\"success\":true,\"user\":\"" + userName + "\",\"isAdmin\":\""
                        + SecurityUtil.isUserAdmin() + "\"}";
            } else {
                jsonText = "{\"success\":false}";
            }
        } catch ( Exception e ) {

            log.error( e, e );
            jsonText = jsonUtil.getJSONErrorMessage( e );
            log.info( jsonText );
        } finally {
            jsonUtil.writeToResponse( jsonText );
        }

    }

    /**
     * Entry point for updates.
     * 
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping("/editUser.html")
    public void editUser( HttpServletRequest request, HttpServletResponse response ) throws Exception {

        String password = request.getParameter( "password" );
        String passwordConfirm = request.getParameter( "passwordConfirm" );
        String oldPassword = request.getParameter( "oldPassword" );

        String jsonText = null;
        JSONUtil jsonUtil = new JSONUtil( request, response );

        try {
            /*
             * Pulling username out of security context to ensure users are logged in and can only update themselves.
             */
            String username = SecurityContextHolder.getContext().getAuthentication().getName();

            if ( !SecurityContextHolder.getContext().getAuthentication().isAuthenticated() ) {
                throw new RuntimeException( "You must be logged in to edit your profile." );
            }

            userManager.reauthenticate( username, oldPassword );

            if ( password.length() >= MIN_PASSWORD_LENGTH ) {
                if ( !StringUtils.equals( password, passwordConfirm ) ) {
                    throw new RuntimeException( "Passwords do not match." );
                }
                String encryptedPassword = passwordEncoder.encode( password );
                userManager.changePassword( oldPassword, encryptedPassword );
            } else {
                throw new RuntimeException( "Password must be at least " + MIN_PASSWORD_LENGTH
                        + " characters in length." );
            }

            log.info( "user: (" + username + ") changed password" );
            saveMessage( request, "Changes saved." );
            jsonText = "{\"success\":true, \"message\":\"Changes saved.\"}";

        } catch ( Exception e ) {
            log.error( e.getLocalizedMessage(), e );
            // jsonText = jsonUtil.getJSONErrorMessage( e );
            jsonText = "{\"success\":false, \"message\":\"" + e.getLocalizedMessage() + "\"}";
            log.info( jsonText );
        } finally {
            jsonUtil.writeToResponse( jsonText );
        }
    }

    /**
     * Resets the password to a random alphanumeric (of length MIN_PASSWORD_LENGTH).
     * 
     * @param request
     * @param response
     */
    @RequestMapping("/resetPassword.html")
    public void resetPassword( HttpServletRequest request, HttpServletResponse response ) {
        if ( log.isDebugEnabled() ) {
            log.debug( "entering 'resetPassword' method..." );
        }

        String email = request.getParameter( "resetPasswordEmail" );
        String username = request.getParameter( "resetPasswordId" );

        JSONUtil jsonUtil = new JSONUtil( request, response );
        String txt = null;
        String jsonText = null;

        /* look up the user's information and reset password. */
        try {

            /* make sure the email and username has been sent */
            if ( StringUtils.isEmpty( email ) || StringUtils.isEmpty( username ) ) {
                txt = "Email or username not specified.  These are required fields.";
                log.warn( txt );
                throw new RuntimeException( txt );
            }

            /* Change the password. */
            String pwd = RandomStringUtils.randomAlphanumeric( MIN_PASSWORD_LENGTH ).toLowerCase();

            String token = userManager.changePasswordForUser( email, username, passwordEncoder.encode( pwd ) );

            String message = sendResetConfirmationEmail( request, token, username, pwd, email );

            jsonText = "{\"success\":true,\"message\":\"" + message + "\"}";

        } catch ( Exception e ) {
            log.error( e, e );
            // jsonText = jsonUtil.getJSONErrorMessage( e );
            jsonText = "{\"success\":false,\"message\":\"" + e.getLocalizedMessage() + "\"}";
        } finally {
            try {
                jsonUtil.writeToResponse( jsonText );
            } catch ( IOException e ) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Send an email to request signup confirmation.
     * 
     * @param request
     * @param u
     */
    private String sendResetConfirmationEmail( HttpServletRequest request, String token, String username,
            String password, String email ) {

        String message = "";

        // Send an account information e-mail
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom( ConfigUtils.getAdminEmailAddress() );
        mailMessage.setSubject( getText( "signup.email.subject", request.getLocale() ) );
        try {
            Map<String, Object> model = new HashMap<>();
            model.put( "username", username );
            model.put( "password", password );

            model.put( "confirmLink", ConfigUtils.getBaseUrl() + "confirmRegistration.html?key=" + token + "&username="
                    + username );
            model.put( "message", getText( "login.passwordReset.emailMessage", request.getLocale() ) );

            String templateName = "passwordReset.vm";
            sendEmail( username, email, getText( "login.passwordReset.emailSubject", request.getLocale() ),
                    templateName, model );
            message = getText( "login.passwordReset", new Object[] { username, email }, request.getLocale() );
            saveMessage( request, message );

        } catch ( Exception e ) {
            message = "Couldn't send password change confirmation email to " + email;
            throw new RuntimeException( message, e );
        }

        return message;

    }

    /**
     * This is hit when a user clicks on the confirmation link they received by email.
     * 
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping("/confirmRegistration.html")
    public void confirmRegistration( HttpServletRequest request, HttpServletResponse response ) throws Exception {
        String username = request.getParameter( "username" );
        String key = request.getParameter( "key" );

        if ( StringUtils.isBlank( username ) || StringUtils.isBlank( key ) ) {
            throw new IllegalArgumentException(
                    "The confirmation url was not valid; it must contain the key and username" );
        }

        boolean ok = userManager.validateSignupToken( username, key );

        if ( ok ) {
            super.saveMessage( request, "Your account is now enabled. Log in to continue" );
            response.sendRedirect( response.encodeRedirectURL( ConfigUtils.getBaseUrl() + "home.html" ) );
        } else {
            super.saveMessage( request, "Sorry, your registration could not be validated. Please register again." );
            response.sendRedirect( response.encodeRedirectURL( ConfigUtils.getBaseUrl() + "home.html" ) );
        }

    }

    /**
     * AJAX DWR
     * 
     * @return loginDetails
     */
    public LoginDetailsValueObject loginCheck() {

        LoginDetailsValueObject ldvo = new LoginDetailsValueObject();

        if ( userManager.loggedIn() ) {
            ldvo.setUserName( userManager.getCurrentUsername() );
            ldvo.setLoggedIn( true );
        } else {
            ldvo.setLoggedIn( false );
        }

        return ldvo;

    }

    /**
     * @param userManager the userManager to set
     */
    public void setUserManager( UserManager userManager ) {
        this.userManager = userManager;
    }

    /**
     * Used when a user signs themselves up.
     * 
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "/signup.html", method = RequestMethod.POST)
    public void signup( HttpServletRequest request, HttpServletResponse response ) throws Exception {

        JSONUtil jsonUtil = new JSONUtil( request, response );
        String jsonText = null;

        String password = request.getParameter( "password" );

        String cPass = request.getParameter( "passwordConfirm" );

        String recatpchaPvtKey = ConfigUtils.getString( "aspiredb.recaptcha.privateKey" );

        if ( StringUtils.isNotBlank( recatpchaPvtKey ) ) {

            boolean valid = recaptchaTester.validateCaptcha( request, recatpchaPvtKey );

            if ( !valid ) {
                jsonText = "{\"success\":false,\"message\":\"Captcha was not entered correctly.\"}";
                jsonUtil.writeToResponse( jsonText );
                return;
            }

        } else {
            log.warn( "No recaptcha private key is configured, skipping validation" );
        }

        if ( password.length() < MIN_PASSWORD_LENGTH ) {
            jsonText = "{\"success\":false,\"message\":\"Password must be at least " + MIN_PASSWORD_LENGTH
                    + " characters in length\"}";
            jsonUtil.writeToResponse( jsonText );
            return;
        }

        if ( !password.equals( cPass ) ) {
            jsonText = "{\"success\":false,\"message\":\"Passwords don't match\"}";
            jsonUtil.writeToResponse( jsonText );
            return;
        }

        String username = request.getParameter( "username" );

        String encodedPassword = passwordEncoder.encode( password );

        String email = request.getParameter( "email" );

        /*
         * Validate that it is a valid email....this regex adapted from extjs; a word possibly containing '-', '+' or
         * '.', following by '@', followed by up to 5 chunks separated by '.', finally a 2-4 letter alphabetic suffix.
         */
        if ( !email.matches( "^(\\w+)([-+.][\\w]+)*@(\\w[-\\w]*\\.){1,5}([A-Za-z]){2,4}$" ) ) {
            jsonText = "{\"success\":false,\"message\":\"Email was not valid or didn't match\"}";
            jsonUtil.writeToResponse( jsonText );
            return;
        }

        String key = userManager.generateSignupToken( username );

        Date now = new Date();

        boolean enabled = false;
        UserDetailsImpl u = new UserDetailsImpl( encodedPassword, username, enabled, null, email, key, now );

        try {

            userManager.createUser( u );

            String msg = sendSignupConfirmationEmail( request, u );

            jsonText = "{\"success\":true,\"message\":\"" + msg + "\"}";
        } catch ( Exception e ) {
            /*
             * Most common cause: user exists already.
             */
            log.error( e, e );
            String errMsg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            jsonText = "{\"success\":false, \"message\":\"" + errMsg + "\"}";
            log.info( jsonText );
        } finally {
            jsonUtil.writeToResponse( jsonText );
        }
    }

    @RequestMapping(value = "/signup.html", method = RequestMethod.GET)
    public String signupForm() {
        return "register";
    }

    /**
     * Send an email to request signup confirmation.
     * 
     * @param request
     * @param u
     */
    private String sendSignupConfirmationEmail( HttpServletRequest request, UserDetailsImpl u ) {

        String msg = "";

        try {
            Map<String, Object> model = new HashMap<String, Object>();
            model.put( "username", u.getUsername() );
            model.put( "siteurl", ConfigUtils.getBaseUrl() );
            model.put( "confirmLink", ConfigUtils.getBaseUrl() + "confirmRegistration.html?key=" + u.getSignupToken()
                    + "&username=" + u.getUsername() );

            String templateName = "accountCreated.vm";
            sendEmail( u.getUsername(), u.getEmail(), getText( "signup.email.subject", request.getLocale() ),
                    templateName, model );

            // See if this comes from AjaxRegister.js, if it does don't save confirmation message
            String ajaxRegisterTrue = request.getParameter( "ajaxRegisterTrue" );

            if ( ajaxRegisterTrue == null || !ajaxRegisterTrue.equals( "true" ) ) {
                String defaultMsg = "A confirmation email was sent. Please check your mail and click the link it contains";
                msg = getText( "signup.email.sent", new Object[] { u.getEmail() }, request.getLocale() );
                if ( msg == null ) {
                    msg = defaultMsg;
                }
                this.saveMessage( request, "signup.email.sent", u.getEmail(), defaultMsg );
            }

        } catch ( Exception e ) {
            msg = "Couldn't send email to " + u.getEmail() + ". " + e.getMessage();
            log.error( msg );
        }

        return msg;

    }

    public void setRecaptchaTester( RecaptchaTester recaptchaTester ) {
        this.recaptchaTester = recaptchaTester;

    }
}

interface RecaptchaTester {
    public boolean validateCaptcha( HttpServletRequest request, String recatpchaPvtKey );
}

class DefaultRecaptchaTester implements RecaptchaTester {
    /**
     * @param request
     * @param recatpchaPvtKey
     * @return
     */
    @Override
    public boolean validateCaptcha( HttpServletRequest request, String recatpchaPvtKey ) {
        String rcChallenge = request.getParameter( "recaptcha_challenge_field" );
        String rcResponse = request.getParameter( "recaptcha_response_field" );

        String remoteAddr = request.getRemoteAddr();
        ReCaptchaImpl reCaptcha = new ReCaptchaImpl();
        reCaptcha.setPrivateKey( recatpchaPvtKey );
        ReCaptchaResponse reCaptchaResponse = reCaptcha.checkAnswer( remoteAddr, rcChallenge, rcResponse );
        return reCaptchaResponse.isValid();
    }
}