/*
 * The aspiredb project
 * 
 * Copyright (c) 2012 University of British Columbia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package ubc.pavlab.aspiredb.server.security.authentication;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.AnonymousAuthenticationProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Process authentication requests that come from outside a web context. This is used for command line interfaces, for
 * example. modified from Gemma
 * 
 * @author keshav
 */
@Component
public class ManualAuthenticationServiceImpl implements ApplicationContextAware, InitializingBean,
        ManualAuthenticationService {
    private static Log log = LogFactory.getLog( ManualAuthenticationServiceImpl.class.getName() );

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private MessageSource messageSource;

    /**
     * We need to do this because normally the anonymous provider has
     */
    @Override
    public void afterPropertiesSet() {

        AnonymousAuthenticationProvider aap = new AnonymousAuthenticationProvider();
        aap.setKey( "" );
        aap.setMessageSource( messageSource );

        ( ( ProviderManager ) this.authenticationManager ).getProviders().add( aap );

    }

    @Override
    public Authentication attemptAuthentication( String username, String password ) throws AuthenticationException {

        if ( username == null ) {
            username = "";
        }

        if ( password == null ) {
            password = "";
        }

        // now ready to log the user in
        Authentication authRequest = new UsernamePasswordAuthenticationToken( username, password );
        return this.authenticationManager.authenticate( authRequest );
    }

    @Override
    public void setApplicationContext( ApplicationContext applicationContext ) throws BeansException {
        this.context = applicationContext;

    }

    @Override
    public boolean validateRequest( String username, String password ) {

        Authentication authResult = null;

        try {
            authResult = attemptAuthentication( username, password );
            SecurityContextHolder.getContext().setAuthentication( authResult );
        } catch ( AuthenticationException failed ) {
            // Authentication failed
            log.info( "**  Authentication failed for user " + username + ": " + failed.getMessage() + "  **" );
            log.debug( failed );
            unsuccessfulAuthentication( failed );
            return false;
        }

        log.debug( "Updated SecurityContextHolder to contain the following Authentication: '" + authResult + "'" );
        successfulAuthentication( authResult );
        return true;
    }

    /**
     * @param authResult
     * @throws IOException
     */
    protected void successfulAuthentication( Authentication authResult ) {
        if ( log.isDebugEnabled() ) {
            log.debug( "Authentication success: " + authResult.toString() );
        }

        // Fire event
        assert context != null;

        if ( this.context != null ) {
            context.publishEvent( new InteractiveAuthenticationSuccessEvent( authResult, this.getClass() ) );
        } else {
            log.fatal( "No context in which to place the authentication object" );
        }
    }

    /**
     * @param failed
     * @throws IOException
     */
    protected void unsuccessfulAuthentication( AuthenticationException failed ) {
        log.debug( "Updated SecurityContextHolder to contain null Authentication" );
        log.debug( "Authentication request failed: " + failed.toString() );

    }

}
