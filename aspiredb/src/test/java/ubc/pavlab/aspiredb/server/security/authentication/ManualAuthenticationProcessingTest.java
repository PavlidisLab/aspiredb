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

import static org.junit.Assert.fail;
import gemma.gsec.authentication.ManualAuthenticationService;
import gemma.gsec.authentication.UserDetailsImpl;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import ubc.pavlab.aspiredb.server.BaseSpringContextTest;

/**
 * @author pavlidis
 * @version $Id: ManualAuthenticationProcessingTest.java,v 1.2 2012/10/09 18:29:54 cmcdonald Exp $
 */
public class ManualAuthenticationProcessingTest extends BaseSpringContextTest {

    private String pwd;

    private String username;

    @Autowired
    ManualAuthenticationService manualAuthenticationService;

    @Autowired
    UserManager userManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Before
    public void before() {

        pwd = randomName();
        username = randomName();

        try {
            userManager.loadUserByUsername( username );
        } catch ( UsernameNotFoundException e ) {

            String encodedPassword = passwordEncoder.encodePassword( pwd, username );
            UserDetailsImpl u = new UserDetailsImpl( encodedPassword, username, true, null, null, null, new Date() );

            userManager.createUser( u );
        }
    }

    @Test
    public void testAttemptAuthentication() throws Exception {
        try {
            manualAuthenticationService.attemptAuthentication( username, pwd );
        } catch ( BadCredentialsException expected ) {
            fail( "Should not have gotten a BadCredentialsException" );
        }

    }

    @Test
    public void testAttemptAuthenticationNonexistentUser() throws Exception {
        try {
            manualAuthenticationService.attemptAuthentication( "I don't exist", "wrong" );
            fail( "Should have gotten a BadCredentialsException" );
        } catch ( BadCredentialsException expected ) {
            // expected.
        }
    }

    @Test
    public void testAttemptAuthenticationWrongPassword() throws Exception {
        try {
            manualAuthenticationService.attemptAuthentication( username, "wrong" );
            fail( "Should have gotten a BadCredentialsException" );
        } catch ( BadCredentialsException expected ) {
            // expected.
        }

    }

}
