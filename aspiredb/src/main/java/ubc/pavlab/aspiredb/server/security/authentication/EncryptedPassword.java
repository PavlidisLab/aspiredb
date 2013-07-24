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

import org.springframework.security.authentication.encoding.ShaPasswordEncoder;

/**
 * Use to generate encrypted passwords given password and salt, for use in initializing test databases.
 * 
 * @author pavlidis
 * @version $Id: EncryptedPassword.java,v 1.1 2012/10/09 18:23:27 cmcdonald Exp $
 */
public class EncryptedPassword {

    /**
     * @param args. First argument = password, Second argument = salt.
     */
    public static void main( String[] args ) {
        ShaPasswordEncoder encoder = new ShaPasswordEncoder();
        System.out.println( "Password='" + args[0] + "' Encrypted=" + encoder.encodePassword( args[0], args[1] ) );

    }

}
