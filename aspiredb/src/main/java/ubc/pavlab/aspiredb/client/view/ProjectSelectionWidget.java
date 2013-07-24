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

package ubc.pavlab.aspiredb.client.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import ubc.pavlab.aspiredb.client.ActiveProjectSettings;
import ubc.pavlab.aspiredb.client.HasProjectSelectionHandlers;
import ubc.pavlab.aspiredb.client.aspiredb;
import ubc.pavlab.aspiredb.client.callback.AspireAsyncCallback;
import ubc.pavlab.aspiredb.client.events.ProjectSelectionEvent;
import ubc.pavlab.aspiredb.client.handlers.ProjectSelectionHandler;
import ubc.pavlab.aspiredb.client.service.ProjectService;
import ubc.pavlab.aspiredb.client.service.ProjectServiceAsync;
import ubc.pavlab.aspiredb.shared.ProjectValueObject;

import java.util.List;

public class ProjectSelectionWidget extends Composite implements HasProjectSelectionHandlers {

    interface MyUIBinder extends UiBinder<Widget, ProjectSelectionWidget> {
    }

    private static MyUIBinder uiBinder = GWT.create( MyUIBinder.class );

    @UiField
    ListBox listBox;

    private final ProjectServiceAsync projectService = GWT.create( ProjectService.class );

    public ProjectSelectionWidget() {
        initWidget( uiBinder.createAndBindUi( this ) );
    }

    AsyncCallback<List<ProjectValueObject>> getProjectsCallback = new AsyncCallback<List<ProjectValueObject>>() {

        @Override
        public void onFailure( Throwable caught ) {

        }

        @Override
        public void onSuccess( List<ProjectValueObject> projects ) {

            listBox.clear();

            for ( ProjectValueObject pvo : projects ) {
                listBox.addItem( pvo.getName(), pvo.getId().toString() );
                listBox.getItemCount();
            }

            //this stuff doesn't quite work right yet for remembering stuff on subsequent logins
            if ( projects.size() > 0 ) {

                if ( !ActiveProjectSettings.hasActiveProject() ) {

                    ActiveProjectSettings.setActiveProject( projects.get( 0 ).getId() );
                    selectActiveProjectInListBox(projects.get( 0 ).getId());
                    
                    fireEvent( new ProjectSelectionEvent( projects.get( 0 ).getId() ) );
                    
                } else {
                    boolean hasThisProject = selectActiveProjectInListBox(ActiveProjectSettings.getActiveProject() );
                    
                    if (hasThisProject){
                        fireEvent( new ProjectSelectionEvent( ActiveProjectSettings.getActiveProject() ) );
                    }else{
                        ActiveProjectSettings.setActiveProject( projects.get( 0 ).getId() );
                    }
                }

            }

        }
    };

    public void getProjects() {
        projectService.getProjects( new AspireAsyncCallback<List<ProjectValueObject>>( getProjectsCallback ) );

    }

    @UiHandler("listBox")
    void onListBoxSelection( ChangeEvent event ) {

        String selectedProjectId = listBox.getValue( listBox.getSelectedIndex() );

        ActiveProjectSettings.setActiveProject( selectedProjectId );
       
        aspiredb.EVENT_BUS.fireEvent(new ProjectSelectionEvent( Long.parseLong( selectedProjectId ) ) );

        fireEvent( new ProjectSelectionEvent( Long.parseLong( selectedProjectId ) ) );

    }

    @Override
    public HandlerRegistration addProjectSelectionHandler( ProjectSelectionHandler handler ) {
        return this.addHandler( handler, ProjectSelectionEvent.TYPE );
    }
    
    private boolean selectActiveProjectInListBox(Long projectId){
        
        for (int i = 0 ; i<listBox.getItemCount();i++){
            
            String value = listBox.getValue( i );
            
            if (projectId.equals( Long.parseLong( value ))){
                
                listBox.setSelectedIndex( i );
                return true;
                
            }
            
        }
        return false;
        
    }

}