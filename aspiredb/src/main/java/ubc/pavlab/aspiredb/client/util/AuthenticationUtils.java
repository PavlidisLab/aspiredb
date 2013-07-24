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

package ubc.pavlab.aspiredb.client.util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.*;
import com.google.gwt.user.client.Window;
import ubc.pavlab.aspiredb.client.aspiredb;
import ubc.pavlab.aspiredb.client.events.LogoutEvent;


public class AuthenticationUtils {
    
    public static void logout(){
        
        RequestCallback logoutCallback = new RequestCallback() {

            @Override
            public void onError( Request request, Throwable exception ) {

            }

            @Override
            public void onResponseReceived( Request request, Response response ) {

                aspiredb.EVENT_BUS.fireEvent( new LogoutEvent() );

            }
        };

        RequestBuilder rb = new RequestBuilder( RequestBuilder.POST, GWT.getModuleName() + "/j_spring_security_logout" );

        rb.setHeader( "Content-Type", "application/x-www-form-urlencoded" );

        rb.setCallback( logoutCallback );

        try {
            rb.send();
        } catch ( RequestException re ) {
            Window.alert( "Logout Failed" );
        }
        
    }
    
    
    
}