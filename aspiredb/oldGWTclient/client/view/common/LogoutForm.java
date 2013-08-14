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
package ubc.pavlab.aspiredb.client.view.common;

import ubc.pavlab.aspiredb.client.aspiredb;
import ubc.pavlab.aspiredb.client.events.LoginEvent;
import ubc.pavlab.aspiredb.client.handlers.LoginEventHandler;
import ubc.pavlab.aspiredb.client.service.LoginStatusService;
import ubc.pavlab.aspiredb.client.service.LoginStatusServiceAsync;
import ubc.pavlab.aspiredb.client.util.AuthenticationUtils;
import ubc.pavlab.aspiredb.client.view.AdminToolsWindow;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.event.SelectEvent;

/**
 * Logout form
 * 
 * @author mly
 * @version $Id: LogoutForm.java,v 1.7 2013/07/19 20:37:57 cmcdonald Exp $
 */
public class LogoutForm extends Composite {

    private static LogoutFormUiBinder uiBinder = GWT.create( LogoutFormUiBinder.class );

    interface LogoutFormUiBinder extends UiBinder<Widget, LogoutForm> {
    }

    private final LoginStatusServiceAsync loginStatusService = GWT.create( LoginStatusService.class );
   
    AsyncCallback<String> logCachesCallback = new AsyncCallback<String>() {

        @Override
        public void onFailure( Throwable caught ) {

        }

        @Override
        public void onSuccess( String logs ) {

            Window.alert( logs );

        }
    };
    
    public LogoutForm() {
        initWidget( uiBinder.createAndBindUi( this ) );
        
        aspiredb.EVENT_BUS.addHandler( LoginEvent.TYPE, new LoginEventHandler() {
            @Override
            public void onLogin( LoginEvent event ) {
                
                loginStatusService.getCurrentUsername( new AsyncCallback<String>() {
                    @Override
                    public void onFailure( Throwable e ) {
                        
                    }

                    @Override
                    public void onSuccess( String username ) {
                        message.setText( "You are logged in as " + username );
                    }
                } );
                
                /*
                loginStatusService.isUserAdministrator( new AsyncCallback<Boolean>() {
                    @Override
                    public void onFailure( Throwable e ) {
                        
                    }

                    @Override
                    public void onSuccess( Boolean result ) {
                        if ( result ) {
                            adminToolsButton.setVisible( true );
                        }else{
                            adminToolsButton.setVisible( false );
                        }

                    }
                } );
                */
                
            }
        } );
    }

    @UiField
    Label message;
    
    @UiField
    TextButton logout;
    
    @UiField
    TextButton adminToolsButton;

    @UiHandler("logout")
    void onButtonClick( SelectEvent event ) {

        AuthenticationUtils.logout();
    }
    
    @UiHandler("adminToolsButton")
    void onAdminToolsButtonClick( SelectEvent event ) {
        
        final AdminToolsWindow adminToolsWindow = new AdminToolsWindow();
        
        adminToolsWindow.show();
        
        
        
    }
}
