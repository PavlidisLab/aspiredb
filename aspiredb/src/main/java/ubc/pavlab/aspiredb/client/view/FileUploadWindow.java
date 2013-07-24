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
import com.google.gwt.user.client.ui.*;
import com.sencha.gxt.widget.core.client.Window;
import ubc.pavlab.aspiredb.client.service.ProjectService;
import ubc.pavlab.aspiredb.client.service.ProjectServiceAsync;
import ubc.pavlab.aspiredb.shared.VariantType;

public class FileUploadWindow extends Window {

    private final ProjectServiceAsync projectService = GWT.create( ProjectService.class );

    ListBox fileTypeBox;
    public TextBox projectName;
    public Button okButton;
    Button cancelButton;

    VerticalPanel panel;

    public FileUploadWindow( String fileName ) {

        panel = new VerticalPanel();

        fileTypeBox = new ListBox();

        fileTypeBox.addItem( "CNV" );
        fileTypeBox.addItem( "SNV" );
        fileTypeBox.addItem( "INDEL" );
        fileTypeBox.addItem( "INVERSION" );
        fileTypeBox.addItem( "Phenotype" );

        panel.add( fileTypeBox );

        panel.add( new Label( " Project Name " ) );

        projectName = new TextBox();

        panel.add( projectName );

        final String fname = fileName;

        okButton = new Button( "Upload to Project", new ClickHandler() {
            public void onClick( ClickEvent event ) {

                final String pname = projectName.getText();

                if ( fileTypeBox.getValue( fileTypeBox.getSelectedIndex() ).equals( "Phenotype" ) ) {

                    projectService.processUploadedPhenotypeFile( pname, fname, new AsyncCallback<String>() {
                        @Override
                        public void onFailure( Throwable e ) {
                            com.google.gwt.user.client.Window.alert( e.getMessage() );

                        }

                        @Override
                        public void onSuccess( String result ) {
                            com.google.gwt.user.client.Window.alert( result );
                        }
                    } );

                } else {

                    final VariantType v = VariantType.valueOf( fileTypeBox.getValue( fileTypeBox.getSelectedIndex() ) );

                    projectService.processUploadedFile( pname, fname, v, new AsyncCallback<String>() {
                        @Override
                        public void onFailure( Throwable e ) {
                            com.google.gwt.user.client.Window.alert( "FAILURE" + e.getMessage() );

                        }

                        @Override
                        public void onSuccess( String result ) {
                            com.google.gwt.user.client.Window.alert( "SUCCESS" + result );
                        }
                    } );

                }
            }
        } );

        panel.add( okButton );

        this.add( panel );

    }

}
