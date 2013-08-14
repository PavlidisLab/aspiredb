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
package ubc.pavlab.aspiredb.client.view.phenotype.enrichment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ubc.pavlab.aspiredb.client.ActiveProjectSettings;
import ubc.pavlab.aspiredb.client.callback.AspireAsyncCallback;
import ubc.pavlab.aspiredb.client.service.PhenotypeService;
import ubc.pavlab.aspiredb.client.service.PhenotypeServiceAsync;
import ubc.pavlab.aspiredb.client.view.TextDataDownloadWindow;
import ubc.pavlab.aspiredb.shared.PhenotypeEnrichmentValueObject;
import ubc.pavlab.aspiredb.shared.PhenotypeEnrichmentValueObjectProperties;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.Store.StoreSortInfo;
import com.sencha.gxt.widget.core.client.FramedPanel;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

/**
 * Phenotype enrichment grid
 * 
 * @version $Id: PhenotypeEnrichmentGrid.java,v 1.2 2013/07/11 18:44:29 cmcdonald Exp $
 */
public class PhenotypeEnrichmentGrid extends Composite implements RequiresResize {

    interface MyUIBinder extends UiBinder<Widget, PhenotypeEnrichmentGrid> {
    }

    private static MyUIBinder uiBinder = GWT.create( MyUIBinder.class );

    @UiField
    FramedPanel main;

    @UiField(provided = true)
    Grid<PhenotypeEnrichmentValueObject> phenotypeEnrichmentGrid;

    @UiField
    ToolBar toolbar;

    @UiField
    TextButton downloadAllButton;

    ListStore<PhenotypeEnrichmentValueObject> phenStore;

    private final PhenotypeEnrichmentValueObjectProperties phenProperties = GWT
            .create( PhenotypeEnrichmentValueObjectProperties.class );

    private final PhenotypeServiceAsync phenotypeService = GWT.create( PhenotypeService.class );

    public PhenotypeEnrichmentGrid() {
        ColumnModel<PhenotypeEnrichmentValueObject> cmGene = constructPhenotypeEnrichmentColumnModel();
        phenStore = new ListStore<PhenotypeEnrichmentValueObject>( phenProperties.id() );
        phenStore.addSortInfo( new StoreSortInfo<PhenotypeEnrichmentValueObject>( phenProperties.pValueString(),
                String.CASE_INSENSITIVE_ORDER, SortDir.ASC ) );

        phenotypeEnrichmentGrid = new Grid<PhenotypeEnrichmentValueObject>( phenStore, cmGene );
        phenotypeEnrichmentGrid.getView().setAutoFill( true );

        initWidget( uiBinder.createAndBindUi( this ) );

    }

    private ColumnModel<PhenotypeEnrichmentValueObject> constructPhenotypeEnrichmentColumnModel() {

        // TODO the columns/names will obviously change

        ColumnConfig<PhenotypeEnrichmentValueObject, String> nameColumn = new ColumnConfig<PhenotypeEnrichmentValueObject, String>(
                phenProperties.name(), 70, "Name" );
        nameColumn.setToolTip( SafeHtmlUtils.fromString( "The name of the phenotype" ));

        ColumnConfig<PhenotypeEnrichmentValueObject, String> inGroupColumn = new ColumnConfig<PhenotypeEnrichmentValueObject, String>(
                phenProperties.inGroupTotalString(), 70, "In Group Present" );
        inGroupColumn.setToolTip( SafeHtmlUtils.fromString( "Number of subjects in filtered group that have phenotype value 'Present' / total" ) );

        ColumnConfig<PhenotypeEnrichmentValueObject, String> outGroupColumn = new ColumnConfig<PhenotypeEnrichmentValueObject, String>(
                phenProperties.outGroupTotalString(), 70, "Out Group Present" );

        outGroupColumn.setToolTip( SafeHtmlUtils.fromString( "Number of subjects outside filtered group that have phenotype value 'Present' / total" ) );
        
        ColumnConfig<PhenotypeEnrichmentValueObject, String> pValueColumn = new ColumnConfig<PhenotypeEnrichmentValueObject, String>(
                phenProperties.pValueString(), 70, "P-value" );
        
        pValueColumn.setToolTip(SafeHtmlUtils.fromString("Cumulative Probability: P(X >= Number of subjects in filtered group that have phenotype value 'Present') ") );

        ColumnConfig<PhenotypeEnrichmentValueObject, String> pValueCorrectedColumn = new ColumnConfig<PhenotypeEnrichmentValueObject, String>(
                phenProperties.pValueCorrectedString(), 70, "Corrected P-value" );

        pValueCorrectedColumn.setToolTip(SafeHtmlUtils.fromString("Corrected P-value (Benjamini and Hochberg method)") );
        
        List<ColumnConfig<PhenotypeEnrichmentValueObject, ?>> columns = new ArrayList<ColumnConfig<PhenotypeEnrichmentValueObject, ?>>();
        columns.add( nameColumn );
        columns.add( inGroupColumn );
        columns.add( outGroupColumn );
        columns.add( pValueColumn );
        columns.add( pValueCorrectedColumn );

        ColumnModel<PhenotypeEnrichmentValueObject> cm = new ColumnModel<PhenotypeEnrichmentValueObject>( columns );
        return cm;
    }

    public void loadPhenotypeEnrichment( Collection<Long> ids ) {

        AsyncCallback<List<PhenotypeEnrichmentValueObject>> callback = new AsyncCallback<List<PhenotypeEnrichmentValueObject>>() {
            @Override
            public void onFailure( Throwable caught ) {
                MessageBox messageBox = new MessageBox( "Error", "An error occured: " + caught.getMessage() );
                messageBox.show();
            }

            @Override
            public void onSuccess( List<PhenotypeEnrichmentValueObject> result ) {
                downloadAllButton.setEnabled( result.size() >= 1 );
                phenStore.replaceAll( result );
                phenotypeEnrichmentGrid.unmask();
            }
        };

        downloadAllButton.disable();
        phenotypeEnrichmentGrid.mask( "Analyzing..." );
        phenotypeService.getPhenotypeEnrichmentValueObjects( ActiveProjectSettings.getActiveProjects(), ids,
                new AspireAsyncCallback<List<PhenotypeEnrichmentValueObject>>( callback ) );

    }

    @UiHandler("downloadAllButton")
    void onDownloadAllButtonClick( SelectEvent event ) {

        List<PhenotypeEnrichmentValueObject> pvos = phenStore.getAll();

        if ( pvos.size() > 0 ) {
            TextDataDownloadWindow tddw = new TextDataDownloadWindow();
            tddw.showPhenotypeEnrichmentDownload( pvos );
        }

    }

    @Override
    public void onResize() {
        int height = this.main.getBody().getClientHeight();
        int width = this.main.getBody().getClientWidth();
        this.setHeight( height );
        this.setWidth( width );
    }

    public void setHeight( int height ) {
        this.main.setHeight( height );
        height = this.main.getBody().getClientHeight() - 60;
        this.phenotypeEnrichmentGrid.setHeight( height );
    }

    public void setWidth( int width ) {
        this.toolbar.setWidth( width );
        this.phenotypeEnrichmentGrid.setWidth( width );
    }

    public void setHeadingText( String title ) {
        this.main.setHeadingText( title );
    }

    public void addWidgetToToolbar( Widget widget ) {
        this.toolbar.add( widget );
    }
}