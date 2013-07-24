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

package ubc.pavlab.aspiredb.client.view.fileuploader;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.*;
import ubc.pavlab.aspiredb.client.fileuploader.ProgressController;
import ubc.pavlab.aspiredb.client.fileuploader.state.UploadProgressState;
import ubc.pavlab.aspiredb.client.view.FileUploadWindow;
import ubc.pavlab.aspiredb.shared.fileuploader.FileDto;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

public final class FileList extends Composite {

    private FlexTable filesTable;

    public FileList() {

        filesTable = new FlexTable();
        filesTable.getRowFormatter().addStyleName( 0, "FileListHead" );

        Panel filesPanel = new VerticalPanel();
        filesPanel.setStyleName( "FileList" );
        filesPanel.add( filesTable );

        this.initWidget( filesPanel );

        UploadProgressState.instance.addPropertyChangeListener( "page", new PageListener() );
        UploadProgressState.instance.addPropertyChangeListener( "files", new FilesListener() );
    }

    @Override
    protected void onLoad() {
        ProgressController.instance.countFiles();
        ProgressController.instance.findFiles( UploadProgressState.instance.getPage(),
                UploadProgressState.instance.getPageSize() );
    }

    private final class FilesListener implements PropertyChangeListener {

        @Override
        public void propertyChange( final PropertyChangeEvent event ) {

            List<FileDto> files = ( List<FileDto> ) event.getNewValue();

            filesTable.clear( true );

            filesTable.setText( 0, 0, "File Name" );
            filesTable.setText( 0, 1, "Upload Date" );

            for ( int i = 0; i < files.size(); i++ ) {
                FileDto file = files.get( i );
                final String fileName = file.getFilename();

                Label anchor = new Label( fileName );

                // Anchor anchor = new Anchor(fileName, "download?file=" + fileName);

                int row = i + 1;
                filesTable.setWidget( row, 0, anchor );
                filesTable.setText( row, 1, DateTimeFormat.getFormat( DateTimeFormat.PredefinedFormat.DATE_TIME_SHORT )
                        .format( file.getDateUploaded() ) );

                Button b = new Button( "Process file", new ClickHandler() {
                    public void onClick( ClickEvent event ) {
                        final FileUploadWindow fileUploadWindow = new FileUploadWindow( fileName );

                        fileUploadWindow.show();
                    }
                } );

                filesTable.setWidget( row, 2, b );
            }
        }
    }

    private static final class PageListener implements PropertyChangeListener {

        @Override
        public void propertyChange( final PropertyChangeEvent event ) {
            ProgressController.instance.findFiles( UploadProgressState.instance.getPage(),
                    UploadProgressState.instance.getPageSize() );
        }
    }
}