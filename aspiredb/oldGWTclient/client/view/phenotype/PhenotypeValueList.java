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

package ubc.pavlab.aspiredb.client.view.phenotype;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import ubc.pavlab.aspiredb.client.HasOntologyTermSelectionHandlers;
import ubc.pavlab.aspiredb.client.events.OntologyTermSelectionEvent;
import ubc.pavlab.aspiredb.client.handlers.OntologyTermSelectionHandler;
import ubc.pavlab.aspiredb.shared.PhenotypeSummaryValueObject;

import java.util.ArrayList;

/**
 * TODO Document Me
 * 
 * @author cmcdonald
 * @version $Id: PhenotypeValueList.java,v 1.3 2013/06/11 22:30:40 anton Exp $
 */
public class PhenotypeValueList extends Composite implements HasOntologyTermSelectionHandlers {

    interface MyUIBinder extends UiBinder<Widget, PhenotypeValueList> {
    }

    private static MyUIBinder uiBinder = GWT.create( MyUIBinder.class );

    @UiField
    VerticalPanel verticalPanel;

    @UiField
    Label title;

    @UiField
    ScrollPanel scrollPanel;

    CellList<PhenotypeBrowserSummary> cellList;

    private static class PhenotypeBrowserSummary {
        private static int nextId = 0;

        private final int id;
        private String name;
        private String uri;
        private String value;
        private Integer count;

        public PhenotypeBrowserSummary( String name, String uri, String value, Integer count ) {
            nextId++;
            this.id = nextId;
            this.name = name;
            this.uri = uri;
            this.value = value;
            this.count = count;
        }
    }

    private static class PhenotypeBrowserSummaryCell extends AbstractCell<PhenotypeBrowserSummary> {
        @Override
        public void render( Context context, PhenotypeBrowserSummary value, SafeHtmlBuilder sb ) {
            if ( value != null ) {
                sb.appendEscaped( "Value: " + value.value + ", Count: " + value.count );
            }
        }
    }

    ProvidesKey<PhenotypeBrowserSummary> keyProvider = new ProvidesKey<PhenotypeBrowserSummary>() {
        public Object getKey( PhenotypeBrowserSummary item ) {
            // Always do a null check.
            return ( item == null ) ? null : item.id;
        }
    };

    public PhenotypeValueList() {
        initWidget( uiBinder.createAndBindUi( this ) );
    }

    public void showSummary( PhenotypeSummaryValueObject pvo ) {

        if ( cellList != null ) scrollPanel.remove( cellList );

        // Create a CellList that uses the cell.
        cellList = new CellList<PhenotypeBrowserSummary>( new PhenotypeBrowserSummaryCell(), keyProvider );
        cellList.setKeyboardSelectionPolicy( KeyboardSelectionPolicy.ENABLED );

        // Add a selection model to handle user selection.
        final SingleSelectionModel<PhenotypeBrowserSummary> selectionModel = new SingleSelectionModel<PhenotypeBrowserSummary>();
        cellList.setSelectionModel( selectionModel );
        selectionModel.addSelectionChangeHandler( new SelectionChangeEvent.Handler() {
            public void onSelectionChange( SelectionChangeEvent event ) {
                PhenotypeBrowserSummary selected = selectionModel.getSelectedObject();
                if ( selected != null ) {
                    // TODO make a new event type for this (currently hitching a ride on OntologyTermSelectionEvent
                    // seems a little unwieldy)
                    fireEvent( new OntologyTermSelectionEvent( null, selected.name, selected.uri, selected.value ) );
                }
            }
        } );

        // Set the total row count. This isn't strictly necessary, but it affects
        // paging calculations, so its good habit to keep the row count up to date.
        cellList.setRowCount( pvo.getInferredValueToSubjectSet().keySet().size(), true );

        ArrayList<PhenotypeBrowserSummary> cellData = new ArrayList<PhenotypeBrowserSummary>();

//        for ( String key : pvo.getValuesAndCounts().keySet() ) {
//            // public PhenotypeBrowserSummary(String name, String uri, String value, Integer count) {
//            PhenotypeBrowserSummary summary = new PhenotypeBrowserSummary( pvo.getName(), pvo.getUri(), key, pvo
//                    .getValuesAndCounts().get( key ) );
//
//            cellData.add( summary );
//
//        }

        String titleText = pvo.getName();

        if ( pvo.getUri() != null ) titleText = titleText + "(" + pvo.getUri() + ")";

        title.setText( titleText );

        // Push the data into the widget.
        cellList.setRowData( 0, cellData );

        scrollPanel.add( cellList );
        scrollPanel.setAlwaysShowScrollBars( true );
        scrollPanel.setHeight( "500px" );

        scrollPanel.setWidth( "300px" );

    }

    public void clearList() {
        title.setText( "" );
        if ( cellList != null ) scrollPanel.remove( cellList );
        cellList = null;

    }

    @Override
    public HandlerRegistration addOntologyTermSelectionHandler( OntologyTermSelectionHandler handler ) {
        return this.addHandler( handler, OntologyTermSelectionEvent.TYPE );
    }

}