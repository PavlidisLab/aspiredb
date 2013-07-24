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
package ubc.pavlab.aspiredb.client.view.gene;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ubc.pavlab.aspiredb.client.callback.AspireAsyncCallback;
import ubc.pavlab.aspiredb.client.service.GeneService;
import ubc.pavlab.aspiredb.client.service.GeneServiceAsync;
import ubc.pavlab.aspiredb.client.util.GemmaURLUtils;
import ubc.pavlab.aspiredb.client.view.TextDataDownloadWindow;
import ubc.pavlab.aspiredb.shared.GeneValueObject;
import ubc.pavlab.aspiredb.shared.GeneValueObjectProperties;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.Store.StoreSortInfo;
import com.sencha.gxt.widget.core.client.FramedPanel;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.filters.GridFilters;
import com.sencha.gxt.widget.core.client.grid.filters.ListFilter;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent.SelectionChangedHandler;
import com.sencha.gxt.widget.core.client.toolbar.SeparatorToolItem;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

/**
 * Gene grid
 * 
 * @version $Id: GeneGrid.java,v 1.20 2013/07/15 17:45:29 anton Exp $
 */
public class GeneGrid extends Composite implements RequiresResize {

    // UIBinder boilerplate.
    interface MyUIBinder extends UiBinder<Widget, GeneGrid> {}
    private static MyUIBinder uiBinder = GWT.create( MyUIBinder.class );

//    @UiField
//    FlowPanel main;

    @UiField(provided=true)
    Grid<GeneValueObject> geneGrid;

    @UiField
    ToolBar toolbar;
    
    @UiField
    TextButton viewGeneNetworkInGemmaButton;
    
    @UiField
    TextButton viewEvidenceButton;
    
    
    //ugly but "BEFORE ISMB"
    @UiField
    SeparatorToolItem viewEvidenceSeparator;
    
    
    @UiField
    TextButton downloadAllButton;

    ListStore<GeneValueObject> geneStore;

    private final GeneValueObjectProperties geneProperties = GWT.create( GeneValueObjectProperties.class );

    // Back-end services.
    private final GeneServiceAsync geneService = GWT.create( GeneService.class );
    
    public GeneGrid() {        
        ColumnModel<GeneValueObject> cmGene = constructGeneColumnModel();
        geneStore = new ListStore<GeneValueObject>( geneProperties.key() );
        geneStore.addSortInfo(new StoreSortInfo<GeneValueObject>(geneProperties.symbol(), String.CASE_INSENSITIVE_ORDER, SortDir.ASC));

        geneGrid = new Grid<GeneValueObject>( geneStore, cmGene );
        geneGrid.getView().setAutoFill( true );
        
        ListStore<String> typeStore = new ListStore<String>(new ModelKeyProvider<String>() {
            @Override
            public String getKey(String item) {
              return item;
            }
        });
           
        //FIXME: Populate this from actual grid data
        typeStore.add("protein_coding");
        typeStore.add("rRNA");
        typeStore.add("miRNA");
        typeStore.add("pseudogene");
        typeStore.add("snoRNA");
       
        ListFilter<GeneValueObject, String> listFilter = new ListFilter<GeneValueObject, String>(geneProperties.geneBioType(), typeStore);
        GridFilters<GeneValueObject> filters = new GridFilters<GeneValueObject>();
        filters.initPlugin( geneGrid );
        filters.setLocal( true );
        filters.addFilter( listFilter ); 

        initWidget(uiBinder.createAndBindUi(this));
        
    }
    
    private ColumnModel<GeneValueObject> constructGeneColumnModel() {
        ColumnConfig<GeneValueObject, String> symbolColumn = new ColumnConfig<GeneValueObject, String>(
                geneProperties.symbol(), 70, "Gene symbol" );

        ColumnConfig<GeneValueObject, String> typeColumn = new ColumnConfig<GeneValueObject, String>(
                geneProperties.geneBioType(), 70, "Type" );

        ColumnConfig<GeneValueObject, String> nameColumn = new ColumnConfig<GeneValueObject, String>(
                geneProperties.name(), 300, "Gene name" );

        ColumnConfig<GeneValueObject, GeneValueObject> gemmaColumn = new ColumnConfig<GeneValueObject, GeneValueObject>(
                new IdentityValueProvider<GeneValueObject>(),
                70, "View in Gemma" );

        GemmaButtonCell gemmaButton = new GemmaButtonCell();

//        gemmaButton.setIcon( AspireImageResources.INSTANCE.gemmaLogo() );
//
//        gemmaButton.addSelectHandler( new SelectHandler() {
//            @Override
//            public void onSelect( SelectEvent event ) {
//                Context context = event.getContext();
//                int row = context.getIndex();
//                GeneValueObject gene = geneStore.get( row );
//                String ucscURL = GemmaURLUtils.makeGeneUrl(gene.getSymbol());
//                Window.open( ucscURL, "_blank", "" );
//            }
//        } );
        gemmaColumn.setCell( gemmaButton );

        List<ColumnConfig<GeneValueObject, ?>> columns = new ArrayList<ColumnConfig<GeneValueObject, ?>>();
        columns.add( symbolColumn );
        columns.add( typeColumn );        
        columns.add( nameColumn );
        columns.add( gemmaColumn );

        ColumnModel<GeneValueObject> cm = new ColumnModel<GeneValueObject>( columns );
        return cm;
    }
    
    public void loadGenesForVariants( Collection<Long> ids ) {
        AsyncCallback<List<GeneValueObject>> callback = new AsyncCallback<List<GeneValueObject>>() {
            @Override
            public void onFailure( Throwable caught ) {
                MessageBox messageBox = new MessageBox( "Error", "An error occured: " + caught.getMessage() );
                messageBox.show();
            }

            @Override
            public void onSuccess( List<GeneValueObject> result ) {
                downloadAllButton.setEnabled( result.size() >= 1 );
                viewGeneNetworkInGemmaButton.setEnabled( false );

                geneStore.replaceAll( result );
                geneGrid.unmask();
            }
        };

        geneGrid.mask( "Searching..." );
        geneService.getGenesInsideVariants(ids, new AspireAsyncCallback<List<GeneValueObject>>(callback));
        
        geneGrid.getSelectionModel().addSelectionChangedHandler( new SelectionChangedHandler<GeneValueObject>() {
            @Override
            public void onSelectionChanged( SelectionChangedEvent<GeneValueObject> event ) {
                viewGeneNetworkInGemmaButton.setEnabled( event.getSelection().size() > 0 );
            }
        } );
    }
    
    public void loadGenesWithNeurocartaPhenotype( String phenotypeUri ) {
        AsyncCallback<Collection<GeneValueObject>> callback = new AsyncCallback<Collection<GeneValueObject>>() {
            @Override
            public void onFailure( Throwable caught ) {
                geneGrid.unmask();
            }

            @Override
            public void onSuccess( Collection<GeneValueObject> result ) {
                geneStore.replaceAll( new ArrayList<GeneValueObject>(result) );
                geneGrid.unmask();
            }
        };

        geneGrid.mask( "Searching..." );
        geneService.findGenesWithNeurocartaPhenotype( phenotypeUri,
                new AspireAsyncCallback<Collection<GeneValueObject>>(callback));
        
        geneGrid.getSelectionModel().addSelectionChangedHandler(new SelectionChangedHandler<GeneValueObject>() {
            @Override
            public void onSelectionChanged(SelectionChangedEvent<GeneValueObject> event) {
                viewGeneNetworkInGemmaButton.setEnabled(event.getSelection().size() > 0);
            }
        });
    }

    @UiHandler("viewGeneNetworkInGemmaButton")
    void onViewGeneNetworkInGemmaButtonClick( SelectEvent event ) {
        List<GeneValueObject> gvos = geneGrid.getSelectionModel().getSelectedItems();
        
        if (gvos.size()>0){
            Window.open(GemmaURLUtils.makeViewGeneNetworkInGemmaURL(gvos) , "_blank", "" );
        }
    }
    
    @UiHandler("downloadAllButton")
    void onDownloadAllButtonClick( SelectEvent event ) {
        List<GeneValueObject> gvos = geneStore.getAll();
     
        if (gvos.size()>0){
            TextDataDownloadWindow tddw = new TextDataDownloadWindow();
            tddw.showGenesDownload( gvos );
        }
    }

    @Override
    public void onResize() {
		int height = this.getOffsetHeight();// getBody().getClientHeight();
		int width = this.getOffsetWidth();  //.main.getBody().getClientWidth();
		this.setHeight( height - 35);
		this.setWidth( width );
    }

	public void setHeight( int height ) {
//		this.main.setHeight( height );
//		height = this.main.getBody().getClientHeight();
        this.geneGrid.setHeight( height );
	}

	public void setWidth(int width) {
		this.toolbar.setWidth(width);
        this.geneGrid.setWidth( width );        
	}

	public void addWidgetToToolbar(Widget widget) {
		this.toolbar.add(widget);
	}
}