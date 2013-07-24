/*
 * The aspiredb project
 * 
 * Copyright (c) 2013 University of British Columbia
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
package ubc.pavlab.aspiredb.client.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sencha.gxt.widget.core.client.Window;
import ubc.pavlab.aspiredb.client.service.ProjectService;
import ubc.pavlab.aspiredb.client.service.ProjectServiceAsync;

public class AdminToolsWindow extends Window {

    private final ProjectServiceAsync projectService = GWT.create( ProjectService.class );

    public TextBox createUserName = new TextBox();
    public TextBox createUserPassword = new TextBox();
    public TextBox createGroupName = new TextBox();
    public Button okCreateButton;

    public TextBox alterProjectName = new TextBox();
    public TextBox alterGroupName = new TextBox();
    public Button okAlterButton;

    public TextBox deleteProjectName = new TextBox();
    public Button okDeleteButton;

    Button cancelButton;

    VerticalPanel panel;

    public AdminToolsWindow() {

        panel = new VerticalPanel();

        panel.add( new Label( " CREATE USER AND ADD TO GROUP " ) );

        panel.add( new Label( "Username" ) );

        panel.add( createUserName );

        panel.add( new Label( "Password" ) );

        panel.add( createUserPassword );

        panel.add( new Label( "Group Name" ) );

        panel.add( createGroupName );

        okCreateButton = new Button( "Create User In Group", new ClickHandler() {
            public void onClick( ClickEvent event ) {

                projectService.createUserAndAssignToGroup( createUserName.getText(), createUserPassword.getText(),
                        createGroupName.getText(), new AsyncCallback<String>() {
                            @Override
                            public void onFailure( Throwable e ) {

                            }

                            @Override
                            public void onSuccess( String result ) {
                                com.google.gwt.user.client.Window.alert( result );
                            }
                        } );
            }
        } );

        panel.add( okCreateButton );

        panel.add( new Label( " GRANT USERGROUP PERMISSIONS " ) );

        panel.add( new Label( "Group Name" ) );

        panel.add( alterGroupName );

        panel.add( new Label( "Project Name" ) );

        panel.add( alterProjectName );

        okAlterButton = new Button( "Grant User Group Permissions", new ClickHandler() {
            public void onClick( ClickEvent event ) {

                projectService.alterGroupPermissions( alterProjectName.getText(), alterGroupName.getText(), true,
                        new AsyncCallback<String>() {
                            @Override
                            public void onFailure( Throwable e ) {

                            }

                            @Override
                            public void onSuccess( String result ) {
                                com.google.gwt.user.client.Window.alert( result );
                            }
                        } );
            }
        } );

        panel.add( okAlterButton );

        panel.add( new Label( " DELETE PROJECT " ) );

        panel.add( new Label( "Project Name" ) );

        panel.add( deleteProjectName );

        okDeleteButton = new Button( "Delete Project", new ClickHandler() {
            public void onClick( ClickEvent event ) {

                projectService.deleteProject( deleteProjectName.getText(), new AsyncCallback<String>() {
                    @Override
                    public void onFailure( Throwable e ) {

                    }

                    @Override
                    public void onSuccess( String result ) {
                        com.google.gwt.user.client.Window.alert( result );
                    }
                } );
            }
        } );

        panel.add( okDeleteButton );

        this.add( panel );

    }

}
