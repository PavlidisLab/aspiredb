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

package ubc.pavlab.aspiredb.server.model.common.auditAndSecurity;

import org.apache.commons.beanutils.PropertyUtils;

/**
 * TODO Document Me
 * 
 * @author cmcdonald
 * @version $Id: UserExistsException.java,v 1.2 2013/06/11 22:56:00 anton Exp $
 */
public class UserExistsException extends java.lang.Exception {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -6191531408977402526L;

    /**
     * Finds the root cause of the parent exception by traveling up the exception tree
     */
    private static Throwable findRootCause( Throwable th ) {
        if ( th != null ) {
            // Lets reflectively get any JMX or EJB exception causes.
            try {
                Throwable targetException = null;
                // java.lang.reflect.InvocationTargetException
                // or javax.management.ReflectionException
                String exceptionProperty = "targetException";
                if ( PropertyUtils.isReadable( th, exceptionProperty ) ) {
                    targetException = ( Throwable ) PropertyUtils.getProperty( th, exceptionProperty );
                } else {
                    exceptionProperty = "causedByException";
                    // javax.ejb.EJBException
                    if ( PropertyUtils.isReadable( th, exceptionProperty ) ) {
                        targetException = ( Throwable ) PropertyUtils.getProperty( th, exceptionProperty );
                    }
                }
                if ( targetException != null ) {
                    th = targetException;
                }
            } catch ( Exception ex ) {
                // just print the exception and continue
                ex.printStackTrace();
            }

            if ( th.getCause() != null ) {
                th = th.getCause();
                th = findRootCause( th );
            }
        }
        return th;
    }

    /**
     * The default constructor.
     */
    public UserExistsException() {
    }

    /**
     * Constructs a new instance of UserExistsException
     * 
     * @param message the throwable message.
     */
    public UserExistsException( String message ) {
        super( message );
    }

    /**
     * Constructs a new instance of UserExistsException
     * 
     * @param message the throwable message.
     * @param throwable the parent of this Throwable.
     */
    public UserExistsException( String message, Throwable throwable ) {
        super( message, findRootCause( throwable ) );
    }

    /**
     * Constructs a new instance of UserExistsException
     * 
     * @param throwable the parent Throwable
     */
    public UserExistsException( Throwable throwable ) {
        super( findRootCause( throwable ) );
    }
}
