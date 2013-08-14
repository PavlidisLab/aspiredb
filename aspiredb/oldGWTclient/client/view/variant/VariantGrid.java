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
package ubc.pavlab.aspiredb.client.view.variant;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.client.loader.RpcProxy;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.SortInfo;
import com.sencha.gxt.data.shared.SortInfoBean;
import com.sencha.gxt.data.shared.Store.StoreSortInfo;
import com.sencha.gxt.data.shared.loader.*;
import com.sencha.gxt.data.shared.loader.BeforeLoadEvent.BeforeLoadHandler;
import com.sencha.gxt.widget.core.client.FramedPanel;
import com.sencha.gxt.widget.core.client.TabPanel;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.event.RowClickEvent;
import com.sencha.gxt.widget.core.client.event.RowClickEvent.RowClickHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.info.Info;
import com.sencha.gxt.widget.core.client.menu.CheckMenuItem;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

import java.util.*;

import ubc.pavlab.aspiredb.client.ActiveProjectSettings;
import ubc.pavlab.aspiredb.client.HasVariantSelectionHandlers;
import ubc.pavlab.aspiredb.client.callback.AspireAsyncCallback;
import ubc.pavlab.aspiredb.client.events.GenomeRegionSelectionEvent;
import ubc.pavlab.aspiredb.client.events.VariantFilterEvent;
import ubc.pavlab.aspiredb.client.events.VariantSelectionEvent;
import ubc.pavlab.aspiredb.client.handlers.GenomeRegionSelectionHandler;
import ubc.pavlab.aspiredb.client.handlers.VariantSelectionHandler;
import ubc.pavlab.aspiredb.client.service.*;
import ubc.pavlab.aspiredb.client.view.CreateLabelWindow;
import ubc.pavlab.aspiredb.client.view.LabelCell;
import ubc.pavlab.aspiredb.client.view.TextDataDownloadWindow;
import ubc.pavlab.aspiredb.client.view.common.PagingGridPanel;
import ubc.pavlab.aspiredb.client.view.filter.VariantPropertyValueComboBox;
import ubc.pavlab.aspiredb.client.view.gene.GeneGridWindow;
import ubc.pavlab.aspiredb.client.view.subject.LabelControlWindow;
import ubc.pavlab.aspiredb.client.view.variant.ideogram.IdeogramPanel;
import ubc.pavlab.aspiredb.shared.*;
import ubc.pavlab.aspiredb.shared.query.*;
import ubc.pavlab.aspiredb.shared.suggestions.SuggestionContext;

/**
 * TODO Document Me
 * 
 * @author azoubare
 * @version $Id: VariantGrid.java,v 1.58 2013/07/12 21:28:03 anton Exp $
 */
public class VariantGrid extends ResizeComposite implements HasVariantSelectionHandlers {

    private static final String SELECT_ALL_TEXT = "Select all";
    private static final String DESELECT_ALL_TEXT = "Deselect";
    private static final String SELECT_ALL_TOOLTIP = "Select all rows";
    private static final String DESELECT_ALL_TOOLTIP = "Deselect all rows";

    interface MyUIBinder extends UiBinder<Widget, VariantGrid> {}
    private static MyUIBinder uiBinder = GWT.create( MyUIBinder.class );

    private boolean selectAllButtonState = false;

    // Created inside async callback so can't use ui binder.
    public PagingGridPanel<VariantValueObject> variantGrid;
    @UiField public IdeogramPanel ideogram;

    @UiField FramedPanel main;

    @UiField public TabPanel tabPanel;

    @UiField public ToolBar toolbar;

    @UiField MenuItem showRangeInUCSCButton;
    @UiField MenuItem showGenesButton;
    @UiField MenuItem makeLabelButton;
    @UiField MenuItem labelSettingsButton;
    @UiField MenuItem showColourLegend;
    @UiField TextButton selectAllButton;
    @UiField TextButton downloadAllButton;
    @UiField TextButton ideogramSettingsMenu;
    @UiField MenuItem chooseDisplayProperty;
    @UiField MenuItem zoomNormalButton;
    @UiField MenuItem zoom2XButton;
    @UiField MenuItem zoom4XButton;

    private final QueryServiceAsync queryService = GWT.create( QueryService.class );
    private final VariantServiceAsync variantService = GWT.create( VariantService.class );
    private final UCSCConnectorAsync ucscConnector = GWT.create( UCSCConnector.class );

    private final VariantValueObjectProperties variantProperties = GWT.create( VariantValueObjectProperties.class );
    public ListStore<VariantValueObject> variantStore = new ListStore<VariantValueObject>( variantProperties.id() );

    // TODO: Refactor me!
    private final Set<Long> visibleLabels = new HashSet<Long>();

    private Collection<Property> downloadProperties;
    
    private PagingLoader<AspireDbPagingLoadConfig, PagingLoadResult<VariantValueObject>> variantLoader;

    public VariantGrid() {
        initWidget(uiBinder.createAndBindUi(this));

        updateSelectAllButtonState( selectAllButtonState );

        // Initial State
        makeLabelButton.disable();
        showRangeInUCSCButton.disable();
        showGenesButton.disable();
        selectAllButton.disable();

        tabPanel.addSelectionHandler(new SelectionHandler<Widget>() {
            @Override
            public void onSelection(SelectionEvent<Widget> event) {
                if (event.getSelectedItem() instanceof IdeogramPanel) {
                    ideogramSettingsMenu.enable();
                    selectAllButton.disable();
                } else {
                    ideogramSettingsMenu.disable();
                    selectAllButton.enable();
                }
            }
        });

        ideogram.addGenomeRegionSelectionHandler(new GenomeRegionSelectionHandler() {
            @Override
            public void onGenomeRangeSelection(GenomeRegionSelectionEvent event) {
                //variantGrid.grid.getSelectionModel().setSelection(new ArrayList<VariantValueObject>());

                if (event.getRange() == null) {
                    showRangeInUCSCButton.disable();
                    showGenesButton.disable();
                    makeLabelButton.disable();
                } else {
                    showRangeInUCSCButton.enable();
                    showGenesButton.enable();
                    makeLabelButton.enable();
                }
            }
        });

        variantService.suggestProperties(new AsyncCallback<Collection<Property>>() {
            @Override
            public void onFailure(Throwable caught) {
            }

            @Override
            public void onSuccess(Collection<Property> properties) {
                fillPropertyIdeogramDisplayOptions(properties, chooseDisplayProperty);
                ColumnModel<VariantValueObject> variantColumnModel = constructVariantColumnModel(properties);

                variantGrid = new PagingGridPanel<VariantValueObject>( variantStore, variantColumnModel );

                variantGrid.grid.getSelectionModel().addSelectionChangedHandler(new SelectionChangedEvent.SelectionChangedHandler<VariantValueObject>() {
                    @Override
                    public void onSelectionChanged(SelectionChangedEvent<VariantValueObject> event) {
                        if ( event.getSelection().isEmpty() ) {
                            makeLabelButton.disable();
                            showRangeInUCSCButton.disable();
                            showGenesButton.disable();
                        } else {
                            makeLabelButton.enable();
                            if (areOnSameChromosome(event.getSelection())) {
                                showRangeInUCSCButton.enable();
                            }
                            showGenesButton.enable();
                        }
                    }
                });

                tabPanel.add(variantGrid, "Table View");

                initVariantLoader();
                variantGrid.setLoader(variantLoader);
                variantGrid.groupBy(variantColumnModel.getColumn(0));
                variantGrid.view.setAutoFill(true);

                variantGrid.addRowClickHandler( new RowClickHandler() {
                    @Override
                    public void onRowClick( RowClickEvent event ) {
                        List<VariantValueObject> variants = variantGrid.grid.getSelectionModel().getSelectedItems();
                        selectAllButtonState = variants.size() > 0;
                        updateSelectAllButtonState( selectAllButtonState );

                        //this.fireEvent( new VariantSelectionEvent( getVariantByRowIndex( event.getRowIndex() ) ) );
                        // We show UCSC button only if variants in the grid are on the same chromosome.
                        if (areOnSameChromosome(variants)) {
                            showRangeInUCSCButton.enable();
                        } else {
                            showRangeInUCSCButton.disable();
                        }
                    }
                } );

                downloadProperties = properties;
            }
        });
    }

    private void updateSelectAllButtonState( boolean state ) {
        if ( state ) {
            selectAllButton.setText( DESELECT_ALL_TEXT );
            selectAllButton.setToolTip( DESELECT_ALL_TOOLTIP );
        } else {
            selectAllButton.setText( SELECT_ALL_TEXT );
            selectAllButton.setToolTip( SELECT_ALL_TOOLTIP );
        }
    }


    @UiHandler("showColourLegend")
    public void onShowColourLegend(SelectionEvent<Item> event) {
        this.ideogram.showColourLegend();
    }

    @UiHandler("zoomNormalButton")
    public void onZoomNormalClick(SelectionEvent<Item> event) {
        this.ideogram.zoom(1);
    }

    @UiHandler("zoom2XButton")
    public void onZoom2XClick(SelectionEvent<Item> event) {
        this.ideogram.zoom(2);
    }

    @UiHandler("zoom4XButton")
    public void onZoom4XClick(SelectionEvent<Item> event) {
        this.ideogram.zoom(4);
    }

    @UiHandler("showGenesButton")
    public void onShowGenesClick(SelectionEvent<Item> event) {
        final Collection<VariantValueObject> variants = getSelection();

        if(variants.isEmpty()) {
            MessageBox alertBox = new MessageBox("No Variants Selected", "Please select variants to use this function");
            alertBox.show();
            return;
        }

        final GeneGridWindow geneWindow = new GeneGridWindow();
        geneWindow.showGenes( variants );
        geneWindow.show();
    }

    @UiHandler("selectAllButton")
    public void onSelectAllClick(SelectEvent event) {
        selectAllButtonState = !selectAllButtonState;
        updateSelectAllButtonState( selectAllButtonState );

        if ( selectAllButtonState ) {
            main.mask("Selecting...");
            variantGrid.grid.disableEvents();
            Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                @Override
                public void execute() {
                    variantGrid.view.expandAllGroups();
    
                    class SelectChunk implements Scheduler.ScheduledCommand  {
                        int start;
                        int end;
                        int last;
                        public SelectChunk(int start, int end, int last) {
                            this.start = start;
                            this.end = end;
                            this.last = last;
                        }
    
                        @Override
                        public void execute() {
                            variantGrid.grid.getSelectionModel().select(start, end, true);
                            if (end == last) {
                                main.unmask();
                                variantGrid.grid.enableEvents();
                            } else {
                                int end = Math.min(this.end + 50, this.last);
                                Scheduler.get().scheduleDeferred( new SelectChunk(this.end, end, last));
                            }
                        }
                    }
    
                    final int lastIndex = variantStore.size() - 1;
                    final int pageSize = Math.min(10, lastIndex);
    
                    Scheduler.get().scheduleDeferred( new SelectChunk(0, pageSize, lastIndex)  );
                }
            });
        } else {
            variantGrid.grid.getSelectionModel().deselectAll();
            ideogram.deselectAll();
        }
    }

    @UiHandler("downloadAllButton")
    public void onDownloadAllClick(SelectEvent event) {
        List<VariantValueObject> variantList = variantStore.getAll();

        TextDataDownloadWindow tddw = new TextDataDownloadWindow();
        tddw.showVariantsDownload(variantList, downloadProperties);        
    }

    @UiHandler("makeLabelButton")
    public void onApplyLabelClick(SelectionEvent<Item> event) {
        final Collection<VariantValueObject> variants = getSelection(); //variantGrid.grid.getSelectionModel().getSelection();

        if(variants.isEmpty()) {
            MessageBox alertBox = new MessageBox("No Variants Selected", "Please select variants to use this function" );
            alertBox.show();
            return;
        }

		VariantPropertyValueComboBox variantPropertyValueComboBox = new VariantPropertyValueComboBox(new VariantLabelProperty(), false);
		variantPropertyValueComboBox.setForceSelection(false);
		CreateLabelWindow labelWindow = new CreateLabelWindow(variantPropertyValueComboBox) {
            @Override
            public void onOkButtonClick(ClickEvent event) {
            	super.onOkButtonClick(event);

                if(variants.isEmpty()) {
                    MessageBox alertBox = new MessageBox("No Variants Selected", "Please select variants to use this function" );
                    alertBox.show();
                    return;
                }

                Collection<Long> ids = new ArrayList<Long>();
                for (VariantValueObject variant: variants) {
                    ids.add(variant.getId());
                }
                
                AsyncCallback<LabelValueObject> callback = new AsyncCallback<LabelValueObject>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        MessageBox alertBox = new MessageBox("Server error while saving label." );
                        alertBox.show();
                        variantGrid.unmask();
                    }

                    @Override
                    public void onSuccess(LabelValueObject label) {
                        visibleLabels.add( label.getId() );
                        variantGrid.grid.getLoader().setReuseLoadConfig(true);
                        variantGrid.grid.getLoader().load();
                        variantGrid.grid.getLoader().setReuseLoadConfig(false);
                        variantGrid.unmask();
                    }
                };
                variantGrid.mask("Labeling...");
                variantService.addLabel(ids, this.getLabel(), new AspireAsyncCallback<LabelValueObject>(callback));
            }
        };
        labelWindow.show();
    }

    private void fillPropertyIdeogramDisplayOptions(Collection<Property> properties, MenuItem button) {
        Menu menu = new Menu();
        button.setSubMenu(menu);

        final CheckMenuItem variantTypeMenuItem = new CheckMenuItem("Variant Type");
        variantTypeMenuItem.setGroup("overlays");
        variantTypeMenuItem.setChecked(true);
        variantTypeMenuItem.addSelectionHandler(new SelectionHandler<Item>() {
            @Override
            public void onSelection(SelectionEvent<Item> event) {
                ideogram.setDisplayedProperty(new VariantTypeProperty());
                ideogram.redraw();
            }
        });
        menu.add(variantTypeMenuItem);

        for (final Property property : properties) {
            if ( property instanceof LabelProperty
                 || property instanceof IndelLengthProperty
                 || property instanceof CnvLengthProperty) {
                continue;
            }
            final CheckMenuItem menuItem = new CheckMenuItem(property.getDisplayName());
            menuItem.setGroup("overlays");
            menuItem.addSelectionHandler(new SelectionHandler<Item>() {
                @Override
                public void onSelection(SelectionEvent<Item> event) {
                    ideogram.setDisplayedProperty( property );
                    ideogram.redraw();
                    ideogram.redraw();
                }
            });
            menu.add(menuItem);
        }
    }

    private ColumnModel<VariantValueObject> constructVariantColumnModel(Collection<Property> properties) {
        ColumnConfig<VariantValueObject, String> patientColumn = new ColumnConfig<VariantValueObject, String>(
                variantProperties.patientId(), 70, "Subject Id" );
        ColumnConfig<VariantValueObject, String> variantTypeColumn = new ColumnConfig<VariantValueObject, String>(
                variantProperties.variantType(), 50, "Type" );

        ColumnConfig<VariantValueObject, GenomicRange> genomicRangeColumn = new ColumnConfig<VariantValueObject, GenomicRange>( variantProperties.genomicRange(),
                50, "Genome Coordinates" );
        genomicRangeColumn.setCell(new AbstractCell<GenomicRange>() {
            @Override
            public void render(Context context, GenomicRange value, SafeHtmlBuilder sb) {
                sb.appendEscaped(value.toBaseString());
            }
        });
        //ColumnConfig<VariantValueObject, Integer> sizeColumn = new ColumnConfig<VariantValueObject, Integer>( variantProperties.length(),
        //        40, "Size" );
        ColumnConfig<VariantValueObject, Collection<LabelValueObject>> labelsColumn =
                new ColumnConfig<VariantValueObject, Collection<LabelValueObject>>( variantProperties.labels(), 70, "Labels" );
        labelsColumn.setSortable(false);
        labelsColumn.setCell(new LabelCell(visibleLabels));

        List<ColumnConfig<VariantValueObject, ?>> columns = new ArrayList<ColumnConfig<VariantValueObject, ?>>();
        columns.add( patientColumn );
        columns.add( variantTypeColumn );
        columns.add( genomicRangeColumn );
        //columns.add( sizeColumn );
        columns.add( labelsColumn );

        for (Property property : properties) {
            if (property instanceof LabelProperty) {
                continue;
            }
            ColumnConfig<VariantValueObject, String> propertyColumn =
                    constructPropertyColumn(property);
            columns.add(propertyColumn);
/*
            else if (property instanceof CharacteristicProperty) {
                ColumnConfig<VariantValueObject, String> propertyColumn =
                        constructPropertyColumn(property);
                columns.add(propertyColumn);
            }
*/
        }

        ColumnModel<VariantValueObject> columnModel = new ColumnModel<VariantValueObject>(columns);
        return columnModel;
    }

    private class PropertyValueProvider implements ValueProvider<VariantValueObject, String> {
        private final Property property;

        public PropertyValueProvider(Property property) {
            this.property = property;
        }

        private String emptyIfNull(Integer value) {
            return value == null ? "" : String.valueOf(value);
        }

        private String emptyIfNull(String value) {
            return value == null ? "" : value;
        }

        @Override
        public String getValue(VariantValueObject variant) {
            CharacteristicValueObject vo = variant.getCharacteristics().get(property.getName());
            if (vo != null) return vo.getValue();

            if (variant instanceof CNVValueObject) {
                CNVValueObject cnv = (CNVValueObject) variant;
                if (property instanceof CopyNumberProperty) {
                    return emptyIfNull( cnv.getCopyNumber() );
                } else if (property instanceof CNVTypeProperty) {
                    return emptyIfNull( cnv.getType() );
                }
            } else if (variant instanceof IndelValueObject) {
                IndelValueObject indel = (IndelValueObject) variant;
                if (property instanceof IndelLengthProperty) {
                    return emptyIfNull(indel.getLength());
                }
            } else if (variant instanceof SNVValueObject) {
                SNVValueObject snv = (SNVValueObject) variant;
                if (property instanceof DbSnpIdProperty) {
                    return emptyIfNull( snv.getDbSNPID() );
                } else if (property instanceof ReferenceBaseProperty) {
                    return emptyIfNull( snv.getReferenceBase() );
                } else if (property instanceof ObservedBaseProperty) {
                    return emptyIfNull( snv.getObservedBase() );
                }
            } else if (variant instanceof InversionValueObject) {
            }

            return "";
        }

        @Override
        public void setValue(VariantValueObject object, String value) {
        }

        @Override
        public String getPath() {
            return property.getDisplayName();
        }
    }

    private ColumnConfig<VariantValueObject, String> constructPropertyColumn(Property property) {
        ColumnConfig<VariantValueObject, String> propertyColumn =
                new ColumnConfig<VariantValueObject, String>(new PropertyValueProvider(property), 60, property.getDisplayName());
        propertyColumn.setHidden(true);
        return propertyColumn;
    }

    private void initVariantLoader() {

        RpcProxy<AspireDbPagingLoadConfig, PagingLoadResult<VariantValueObject>> allCNVproxy = new RpcProxy<AspireDbPagingLoadConfig, PagingLoadResult<VariantValueObject>>() {
            @Override
            public void load( AspireDbPagingLoadConfig loadConfig, AsyncCallback<PagingLoadResult<VariantValueObject>> callback ) {
                queryService.queryVariants(loadConfig, new AspireAsyncCallback<PagingLoadResult<VariantValueObject>>(callback));
            }
        };

        variantLoader = new PagingLoader<AspireDbPagingLoadConfig, PagingLoadResult<VariantValueObject>>( allCNVproxy ) {
                    @Override
                    protected AspireDbPagingLoadConfig newLoadConfig() {
                        AspireDbPagingLoadConfig config = new AspireDbPagingLoadConfigBean();
                        config.getFilters().add( new ProjectFilterConfig( ActiveProjectSettings.getActiveProjects() ) );
                        config.setLimit( variantLoader.getLimit() );
                        config.setOffset( 0 );
                        return config; 
                    }
        };

        variantLoader.addLoadHandler( new LoadResultListStoreBinding<AspireDbPagingLoadConfig, VariantValueObject, PagingLoadResult<VariantValueObject>>(
                variantStore ) );

        variantLoader.setRemoteSort( false );
        
        variantLoader.addBeforeLoadHandler( new BeforeLoadHandler<AspireDbPagingLoadConfig>() {
            @Override
            public void onBeforeLoad( BeforeLoadEvent<AspireDbPagingLoadConfig> event ) {
                //variantGrid.mask( "Loading..." );
                main.mask("Loading...");
            }
        } );

        variantLoader.addLoadHandler(new LoadHandler<AspireDbPagingLoadConfig, PagingLoadResult<VariantValueObject>>() {
            @Override
            public void onLoad(LoadEvent<AspireDbPagingLoadConfig, PagingLoadResult<VariantValueObject>> event) {
                //variantGrid.view.collapseAllGroups();

                List<VariantValueObject> variants = event.getLoadResult().getData();
                ideogram.setVariants(variants);
                ideogram.redraw();

                main.unmask();
            }
        });

        // default sort
        // TODO: should it be on the backend?
        variantLoader.addSortInfo( new SortInfoBean( "id", SortDir.ASC ) );
    }    
    
    // Check if all variants are on the same chromosome.
    private boolean areOnSameChromosome( List<VariantValueObject> variants ) {
		if ( variants.isEmpty() ) return false;
		
        String chromosome = variants.get( 0 ).getGenomicRange().getChromosome();
        
        for ( VariantValueObject variant : variants ) {
        	if ( ! variant.getGenomicRange().getChromosome().equals( chromosome )) {
        		return false;
        	}
        }
		
		return true;
	}                	
        
    /**
     * 
     * @param customTrackData
     * @return
     */
    private FormPanel constructFormForPOST( String customTrackData ) {
    	// Form fields we're POST'ing. Example.        
    	// 	name="clade"
    	//    	mammal
    	//  name="org"
    	//     	Human
    	//  name="db"
    	//		hg19
    	//  name="hgct_customText"
    	//    	browser position chr22:20100000-20100900
    	//    	track name=coords description="Chromosome coordinates list" visibility=2
    	// 		chr22   20100000 20100100
        FormPanel form = new FormPanel("_blank");
        form.setEncoding(FormPanel.ENCODING_MULTIPART);
        form.setMethod(FormPanel.METHOD_POST);

        VerticalPanel holder = new VerticalPanel();

        Hidden clade = new Hidden("clade", "mammal");
        Hidden org = new Hidden("org", "Human");
        Hidden db = new Hidden("db", "hg19");
        Hidden track = new Hidden("hgct_customText", customTrackData);
        
        holder.add( clade );
        holder.add( org );
        holder.add( db );
        holder.add( track );
        
        form.add( holder );        
        form.setAction("http://genome.ucsc.edu/cgi-bin/hgCustom");

        return form;
    }

    private List<VariantValueObject> getSelection() {
        if (this.tabPanel.getActiveWidget().getClass() == IdeogramPanel.class ) {
            GenomicRange range = ideogram.getSelection();
            if (range == null) return new ArrayList<VariantValueObject>();
            List<VariantValueObject> variants = variantStore.getAll();
            List<VariantValueObject> variantsInsideRange = new ArrayList<VariantValueObject>();
            for (VariantValueObject variant : variants) {
                if (variant.getGenomicRange().isWithin( range )) {
                    variantsInsideRange.add(variant);
                }
            }
            return variantsInsideRange;
        } else {
            List<VariantValueObject> variants = variantGrid.grid.getSelectionModel().getSelection();
            return variants;
        }
    }

    private GenomicRange getSpanningGenomicRange (List<VariantValueObject> variants) {
        if ( !areOnSameChromosome( variants ) ) return null;

        int start = Integer.MAX_VALUE;
        int end = Integer.MIN_VALUE;
        String chromosome = variants.get( 0 ).getGenomicRange().getChromosome();
        for ( VariantValueObject variant : variants ) {
            if (variant.getGenomicRange().getChromosome().equals(chromosome)) {
                if (variant.getGenomicRange().getBaseStart() < start)
                    start = variant.getGenomicRange().getBaseStart();
                if (variant.getGenomicRange().getBaseEnd() > end)
                    end = variant.getGenomicRange().getBaseEnd();
            }
        }
        return new GenomicRange(chromosome, start, end);
    }

    private GenomicRange getSelectedRange() {
        if (this.tabPanel.getActiveWidget().getClass() == IdeogramPanel.class ) {
            return ideogram.getSelection();
        } else {
            List<VariantValueObject> variants = variantGrid.grid.getSelectionModel().getSelection();
            return getSpanningGenomicRange(variants);
        }
    }

    @UiHandler("showRangeInUCSCButton")
    void onShowAllVariantsInUCSCButtonClick(SelectionEvent<Item> event) {

        GenomicRange range = getSelectedRange();
        if (range == null) {
            Info.display( "View All in UCSC", "No results to show" );
            return;
        }

        AsyncCallback<String> constructPOSTRequestCallback = new AsyncCallback<String>() {

			@Override
			public void onFailure( Throwable caught ) {
				// TODO Auto-generated method stub				
			}

			@Override
			public void onSuccess( String trackData ) {
				FormPanel form = constructFormForPOST( trackData );
		        form.submit();
			}
        };

        ucscConnector.constructCustomTracksFile( range, ActiveProjectSettings.getActiveProjects(),
                GWT.getModuleBaseURL(),
                constructPOSTRequestCallback );
    }
        
    public void applyFilter( VariantFilterEvent event ) {
    	AspireDbPagingLoadConfig loadConfig = new AspireDbPagingLoadConfigBean();
    	loadConfig.setOffset( 0 );
    	loadConfig.setLimit( 2000 );
    	
    	Collection<StoreSortInfo<VariantValueObject>> sortInfos = variantStore.getSortInfo();
    	List<SortInfo> convertedSortInfos = new ArrayList<SortInfo>();
    	
    	for ( StoreSortInfo<VariantValueObject> sort : sortInfos ) {
    		//TODO: finish me
    	}
    	
    	loadConfig.setSortInfo( convertedSortInfos );

    	// Add project filter first.
    	loadConfig.getFilters().add( new ProjectFilterConfig( ActiveProjectSettings.getActiveProjects() ) );
    	// Then the rest of the filters.
    	loadConfig.getFilters().addAll( event.getFilterConfigs() );
    	       
        variantLoader.load( loadConfig );        
    }    
    
    public VariantValueObject getVariantByRowIndex(int index) {
        return variantStore.get( index );        
    }

    @Override
    public HandlerRegistration addVariantSelectionHandler( VariantSelectionHandler handler ) {
        return this.addHandler( handler, VariantSelectionEvent.TYPE );
    }
            
    @Override
    public void onResize() {
    	int width = this.main.getBody().getClientWidth();
    	this.variantGrid.setWidth( width );
    	this.toolbar.setWidth( width );    	
    }

    // TODO: figure out proper way to manage/calculate size
    public void setHeight (int height) {
		this.main.setHeight( height );
		int heightInsidePanel = this.main.getBody().getClientHeight();
		heightInsidePanel -= this.toolbar.getOffsetHeight();
		this.tabPanel.setHeight( heightInsidePanel );

		heightInsidePanel -= 27; // size of tabs

        this.ideogram.setHeight( heightInsidePanel );
		this.variantGrid.grid.setHeight( heightInsidePanel );
    }

    // TODO: figure out proper way to manage/calculate size
    public void setWidth(int width) {
    	this.main.setWidth( width );
    	width = this.main.getBody().getClientWidth();
    	this.variantGrid.setWidth( width );
        this.ideogram.setWidth( width );
	}

    @UiHandler("labelSettingsButton")
    public void onLabelSettingsClick(SelectionEvent<Item> event) {
        final LabelControlWindow w = new LabelControlWindow(visibleLabels, variantGrid.view, false);
        w.show();

        AsyncCallback<List<LabelValueObject>> updateSubjectLabelsCallback = new AsyncCallback<List<LabelValueObject>>() {
            @Override
            public void onFailure(Throwable caught) {
            }

            @Override
            public void onSuccess(List<LabelValueObject> labels) {
                w.initializeStore(labels);
            }
        };

        variantService.suggestLabels(new SuggestionContext(), new AspireAsyncCallback<List<LabelValueObject>>(updateSubjectLabelsCallback));
    }

    public void mask(String message) {
        main.mask(message);
    }

    public void unmask() {
        main.unmask();
    }
}