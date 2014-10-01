/*
 * The rdp project
 * 
 * Copyright (c) 2013 University of British Columbia
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
package ubc.pavlab.aspiredb.server.controller;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import gemma.gsec.authentication.UserManager;

import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import ubc.pavlab.aspiredb.server.BaseSpringContextTest;
import ubc.pavlab.aspiredb.server.security.authentication.UserService;

/**
 * Tortures the signup system by starting many threads and signing up many users, while at the same time creating a lot
 * of expression experiments.
 * 
 * @author Paul
 * @version $Id: SignupControllerTest.java,v 1.13 2013/09/21 01:30:05 paul Exp $
 */
public class SignupControllerTest extends BaseSpringContextTest {

    @Autowired
    private SignupController suc;

    @Autowired
    private UserService userService;

    @Autowired
    private UserManager userManager;

    @Before
    public void setup() {
        suc.setRecaptchaTester( new RecaptchaTester() {
            @Override
            public boolean validateCaptcha( HttpServletRequest request, String recatpchaPvtKey ) {
                return true;
            }
        } );
    }

    @Test
    public void testSignup() throws Exception {

        int numThreads = 10; // too high and we run out of connections, which is not what we're testing.
        final int numsignupsperthread = 20;
        final Random random = new Random();
        final AtomicInteger c = new AtomicInteger( 0 );
        final AtomicBoolean failed = new AtomicBoolean( false );
        Collection<Thread> threads = new HashSet<Thread>();

        for ( int i = 0; i < numThreads; i++ ) {

            Thread k = new Thread( new Runnable() {
                @Override
                public void run() {
                    try {
                        for ( int j = 0; j < numsignupsperthread; j++ ) {
                            MockHttpServletRequest req = null;
                            Thread.sleep( random.nextInt( 50 ) );
                            req = new MockHttpServletRequest( "POST", "/signup.html" );
                            final String uname = RandomStringUtils.randomAlphabetic( 10 );
                            // log.info( "Signingup: " + uname + " (" + c.get() + ")" );

                            String password = RandomStringUtils.randomAlphabetic( 40 );
                            req.addParameter( "password", password );

                            req.addParameter( "passwordConfirm", password );
                            req.addParameter( "username", uname );
                            String email = "foo@" + RandomStringUtils.randomAlphabetic( 10 ) + ".edu";
                            req.addParameter( "email", email );
                            suc.signup( req, new MockHttpServletResponse() );

                            // Cleanup
                            assertNotNull( userManager.findByUserName( uname ) );
                            userManager.deleteUser( uname );
                            // userService.delete( userService.findByUserName( uname ) );

                            c.incrementAndGet();

                        }
                    } catch ( Exception e ) {
                        failed.set( true );
                        log.error( "!!!!!!!!!!!!!!!!!!!!!! FAILED: " + e.getMessage() );
                        log.debug( e, e );
                        throw new RuntimeException( e );
                    }
                    log.debug( "Thread done." );
                }
            } );
            threads.add( k );

            k.start();
        }

        int waits = 0;
        int maxWaits = 30;
        int expectedEventCount = numThreads * numsignupsperthread;
        while ( c.get() < expectedEventCount && !failed.get() ) {
            try {
                Thread.sleep( 8000 );
            } catch ( InterruptedException e ) {
                e.printStackTrace();
            }
            log.info( "Waiting ... C=" + +c.get() );
            if ( ++waits > maxWaits ) {
                for ( Thread t : threads ) {
                    if ( t.isAlive() ) t.interrupt();
                }
                fail( "Multithreaded failure: timed out." );
            }
        }

        log.debug( " &&&&& DONE &&&&&" );

        for ( Thread thread : threads ) {
            if ( thread.isAlive() ) thread.interrupt();
        }

        if ( failed.get() || c.get() != expectedEventCount ) {
            fail( "Multithreaded loading failure: check logs for failure to recover from deadlock?" );
        } else {
            log.info( "TORTURE TEST PASSED!" );
        }

    }
}
