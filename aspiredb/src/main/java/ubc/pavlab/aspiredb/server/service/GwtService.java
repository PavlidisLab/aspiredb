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
package ubc.pavlab.aspiredb.server.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ubc.pavlab.aspiredb.server.exceptions.NotLoggedInException;
import ubc.pavlab.aspiredb.server.security.SecurityServiceImpl;

/**
 * used by server-side implementations of gwt client services
 * contains common methods such as throwing checked exceptions that client code can recognize
 * 
 * @author cmcdonald
 * @version $Id: GwtService.java,v 1.6 2013/06/11 22:30:48 anton Exp $
 */
public abstract class GwtService { 
    
    private static Logger log = LoggerFactory.getLogger( GwtService.class );
   
    /**
     * took off Secured("GROUP_USER") annotations from server-side implementations of gwt client services 
     * because we needed to propagate a client side based exception to the client and have the throws declaration on the service methods.
     *  With Spring security Secured("GROUP_USER") annotations on the methods Spring was hijacking it and throwing AccessDeniedExceptions which resulted
     *  in a generic StatusCodeException on the client side. StatusCodeException is usually used for RuntimeExceptions 
     * 
     * @throws NotLoggedInException
     */
    public void throwGwtExceptionIfNotLoggedIn() throws NotLoggedInException{
        
        if (!SecurityServiceImpl.isUserLoggedIn()) {
          //just testing out the logging, make this more descriptive later
            log.info( "User not logged in, throwing NotLoggedInException" );
            throw new NotLoggedInException();
        }
        
    }
    //TODO change to different exception, or just consolidate both into a UserCredentialException or something
    public void throwGwtExceptionIfNotAdmin() throws NotLoggedInException{
        
        if (!SecurityServiceImpl.isUserAdmin()) {
          //just testing out the logging, make this more descriptive later
            log.info( "User not admin, throwing NotLoggedInException" );
            throw new NotLoggedInException();
        }
        
    }
    
    
    
    
}