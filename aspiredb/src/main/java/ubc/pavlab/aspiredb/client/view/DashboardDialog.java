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
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.event.SelectEvent;

import ubc.pavlab.aspiredb.client.ActiveProjectSettings;
import ubc.pavlab.aspiredb.client.aspiredb;
import ubc.pavlab.aspiredb.client.callback.AspireAsyncCallback;
import ubc.pavlab.aspiredb.client.events.LoginEvent;
import ubc.pavlab.aspiredb.client.events.ProjectSelectionEvent;
import ubc.pavlab.aspiredb.client.handlers.LoginEventHandler;
import ubc.pavlab.aspiredb.client.service.LoginStatusService;
import ubc.pavlab.aspiredb.client.service.LoginStatusServiceAsync;
import ubc.pavlab.aspiredb.client.service.ProjectServiceOld;
import ubc.pavlab.aspiredb.client.service.ProjectServiceAsync;
import ubc.pavlab.aspiredb.client.view.fileuploader.UploadProgressView;

/**
 * Dashboard Dialog
 * 
 * @author frances
 * @version $Id: DashboardDialog.java,v 1.5 2013/07/19 20:37:56 cmcdonald Exp $
 */
public class DashboardDialog extends Dialog {

    private static final int HEIGHT_WITH_FILE_UPLOADER = 450;
    private static final int HEIGHT_WITHOUT_FILE_UPLOADER = 240;
    
    @UiField(provided = true)
    Label numberOfSubjects;

    @UiField(provided = true)
    Label numberOfVariants;

    @UiField
    ProjectSelectionWidget projectSelection;

    @UiField
    TextButton showFileUpload;

    interface MyUIBinder extends UiBinder<Widget, DashboardDialog> {}
    private static MyUIBinder uiBinder = GWT.create( MyUIBinder.class );

    @UiField
    FlowPanel flowPanel;

    UploadProgressView uploadProgress;

    private final LoginStatusServiceAsync loginStatusService = GWT.create( LoginStatusService.class );

    private final ProjectServiceAsync projectService = GWT.create( ProjectServiceOld.class );

    public DashboardDialog() {
        setModal( true );
        setClosable( false );
        setPredefinedButtons( PredefinedButton.OK, PredefinedButton.CANCEL );
        setHideOnButtonClick( true );

        aspiredb.EVENT_BUS.addHandler( LoginEvent.TYPE, new LoginEventHandler() {
            @Override
            public void onLogin( LoginEvent event ) {
                projectSelection.getProjects();
/*
                loginStatusService.isUserAdministrator( new AsyncCallback<Boolean>() {
                    @Override
                    public void onFailure( Throwable e ) {
                    }

                    @Override
                    public void onSuccess( Boolean result ) {
                        if ( result ) {
                            showFileUpload.setVisible( true );
                        }
                    }
                } );
                */
            }
            
        } );

        numberOfSubjects = new Label();
        numberOfVariants = new Label();

        setWidget( uiBinder.createAndBindUi( this ) );

        this.setHeadingText( "Dashboard" );
        this.setResizable( false );        
    }

    /**
     * @param event
     */
    @UiHandler("projectSelection")
    public void onSelect( ProjectSelectionEvent event ) {

        AsyncCallback<Integer> updateNumSubjectsCallback = new AsyncCallback<Integer>() {
            @Override
            public void onFailure( Throwable caught ) {
            }

            @Override
            public void onSuccess( Integer result ) {
                numberOfSubjects.setText( result.toString() );
            }
        };

        AsyncCallback<Integer> updateNumVariantsCallback = new AsyncCallback<Integer>() {
            @Override
            public void onFailure( Throwable caught ) {
            }

            @Override
            public void onSuccess( Integer result ) {
                numberOfVariants.setText( result.toString() );
            }
        };

        projectService.numSubjects( ActiveProjectSettings.getActiveProject(), new AspireAsyncCallback<Integer>(
                updateNumSubjectsCallback ) );
        projectService.numVariants( ActiveProjectSettings.getActiveProject(), new AspireAsyncCallback<Integer>(
                updateNumVariantsCallback ) );
    }

    /**
     * @param event
     */
    @UiHandler("showFileUpload")
    public void onShowFileUploadClick( SelectEvent event ) {
        if ( uploadProgress == null ) {
            uploadProgress = new UploadProgressView();
        }

        if ( flowPanel.getWidgetCount() == 0 ) {
            this.setHeight( String.valueOf( HEIGHT_WITH_FILE_UPLOADER ) );            
            this.setMinHeight( HEIGHT_WITH_FILE_UPLOADER );        
            flowPanel.add( uploadProgress );
            showFileUpload.setText( "Hide file uploader" );
        } else {
            this.setHeight( String.valueOf( HEIGHT_WITHOUT_FILE_UPLOADER ) );
            this.setMinHeight( HEIGHT_WITHOUT_FILE_UPLOADER );        
            flowPanel.remove( uploadProgress );
            showFileUpload.setText( "Show file uploader" );
        }
    }

    public void setCancelButtonVisible( boolean visible ) {
        getButtonById( PredefinedButton.CANCEL.name() ).setVisible( visible );
    }

    public HandlerRegistration addOkButtonSelectHandler( SelectEvent.SelectHandler handler ) {
        return getButtonById( PredefinedButton.OK.name() ).addSelectHandler( handler );
    }

    public HandlerRegistration addCancelButtonSelectHandler( SelectEvent.SelectHandler handler ) {
        return getButtonById( PredefinedButton.CANCEL.name() ).addSelectHandler( handler );
    }
}