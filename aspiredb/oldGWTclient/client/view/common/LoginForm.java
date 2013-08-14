/*
 * The aspiredb project
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
package ubc.pavlab.aspiredb.client.view.common;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.http.client.*;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.PasswordField;
import com.sencha.gxt.widget.core.client.form.TextField;
import ubc.pavlab.aspiredb.client.aspiredb;
import ubc.pavlab.aspiredb.client.events.LoginEvent;
import ubc.pavlab.aspiredb.client.service.LoginStatusService;
import ubc.pavlab.aspiredb.client.service.LoginStatusServiceAsync;
import ubc.pavlab.aspiredb.client.util.GemmaURLUtils;

/**
 * Login form
 * 
 * @author mly
 * @version $Id: LoginForm.java,v 1.4 2013/07/05 00:02:36 frances Exp $
 */
public class LoginForm extends PopupPanel {

    // private static Log log = LogFactory.getLog( LoginForm.class );

    private static LoginFormUiBinder uiBinder = GWT.create( LoginFormUiBinder.class );

    interface LoginFormUiBinder extends UiBinder<Widget, LoginForm> {
    }

    public LoginForm() {
        setWidget( uiBinder.createAndBindUi( this ) );
    }
    
    @UiField HTML message;
    
    // text fields
    @UiField TextField username;
    @UiField PasswordField password;

    // buttons
    @UiField TextButton help;
    @UiField TextButton login;
    @UiField TextButton clear;

    /**
     * @param event  
     */
    @UiHandler("help")
    void onHelpButtonClick( SelectEvent event ) {
        Window.open( GemmaURLUtils.getHelpPageURL( ) , "_blank", "" );
    }

    /**
     * @param event  
     */
    @UiHandler("login")
    void onLoginButtonClick( SelectEvent event ) {
        login();
    }

    /**
     * @param event  
     */
    @UiHandler("clear")
    void onCancelButtonClick( SelectEvent event ) {
        username.clear();
        password.clear();
        message.setVisible( false );
    }

    @UiHandler(value={"password", "username"})
    void onEnter( KeyDownEvent event ) {
        loginFormEnterPressed( event );
    }

    private final LoginStatusServiceAsync loginStatusService = GWT.create( LoginStatusService.class );

    private void loginFormEnterPressed( KeyDownEvent event ) {
        if ( event.getNativeKeyCode() == KeyCodes.KEY_ENTER ) {
            login();           
        }
    }

    private void login() {
        RequestCallback loginCallback = new RequestCallback() {

            @Override
            public void onError( Request request, Throwable exception ) {
                // Change this to let user know something is wrong
                System.out.println( "ERROR" );
                Window.alert( "Error" ); 
            }

            /*
             * FIXME What is the relationship between this and the similar method in AspireDbPanel.java?
             * 
             * (non-Javadoc)
             * 
             * @see com.google.gwt.http.client.RequestCallback#onResponseReceived(com.google.gwt.http.client.Request,
             * com.google.gwt.http.client.Response)
             */
            @Override
            public void onResponseReceived( Request request, Response response ) {

                // check if already logged in
                loginStatusService.isLoggedIn( new AsyncCallback<Boolean>() {
                    @Override
                    public void onFailure( Throwable e ) {
                        // does this mean they are not logged in?
                        // log.info( "Not logged in: " + e.getMessage() );
                    }

                    @Override
                    public void onSuccess( Boolean result ) {
                        username.clear();
                        password.clear();
                        
                        if ( result ) {
                            aspiredb.EVENT_BUS.fireEvent( new LoginEvent() );
                            message.setVisible( false );
                        } else {
                            message.setVisible( true );
                        }
                    }
                } );
            }
        };

        RequestBuilder rb = new RequestBuilder( RequestBuilder.POST, GWT.getModuleName() + "/j_spring_security_check" );

        rb.setHeader( "Content-Type", "application/x-www-form-urlencoded" );

        rb.setRequestData( "j_username="
                + URL.encode( username.getText() + "&j_password=" + URL.encode( password.getText() ) ) );

        rb.setCallback( loginCallback );

        try {
            rb.send();
        } catch ( RequestException re ) {
            System.out.println( "exception" );
        }

    }
}
