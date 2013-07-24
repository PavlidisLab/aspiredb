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
package ubc.pavlab.aspiredb.client.view.phenotype;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import ubc.pavlab.aspiredb.client.ActiveProjectSettings;
import ubc.pavlab.aspiredb.client.aspiredb;
import ubc.pavlab.aspiredb.client.callback.AspireAsyncCallback;
import ubc.pavlab.aspiredb.client.events.LogoutEvent;
import ubc.pavlab.aspiredb.client.handlers.LogoutEventHandler;
import ubc.pavlab.aspiredb.client.service.PhenotypeService;
import ubc.pavlab.aspiredb.client.service.PhenotypeServiceAsync;
import ubc.pavlab.aspiredb.client.service.ProjectService;
import ubc.pavlab.aspiredb.client.service.ProjectServiceAsync;
import ubc.pavlab.aspiredb.client.service.SubjectService;
import ubc.pavlab.aspiredb.client.service.SubjectServiceAsync;
import ubc.pavlab.aspiredb.client.view.TextDataDownloadWindow;
import ubc.pavlab.aspiredb.client.view.phenotype.enrichment.PhenotypeEnrichmentGridWindow;
import ubc.pavlab.aspiredb.shared.PhenotypeSummaryProperties;
import ubc.pavlab.aspiredb.shared.PhenotypeSummaryValueObject;
import ubc.pavlab.aspiredb.shared.PhenotypeValueObject;
import ubc.pavlab.aspiredb.shared.SubjectValueObject;
import ubc.pavlab.aspiredb.shared.query.AspireDbFilterConfig;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.FramedPanel;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.RowExpander;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

/**
 * TODO Document Me
 * 
 * @version $Id: PhenotypeGrid.java,v 1.20 2013/07/11 23:35:55 cmcdonald Exp $
 * @author cmcdonald
 */
public class PhenotypeGrid extends ResizeComposite {

    interface MyUIBinder extends UiBinder<Widget, PhenotypeGrid> {
    }

    private static MyUIBinder uiBinder = GWT.create( MyUIBinder.class );

    private final PhenotypeServiceAsync phenotypeService = GWT.create( PhenotypeService.class );
    private final SubjectServiceAsync subjectService = GWT.create( SubjectService.class );

    private final PhenotypeSummaryProperties phenotypeProperties = GWT.create( PhenotypeSummaryProperties.class );
    public ListStore<PhenotypeSummaryValueObject> phenotypeStore;

    private final ProjectServiceAsync projectService = GWT.create( ProjectService.class );

    private RowExpander<PhenotypeSummaryValueObject> expander = new PhenotypeGridRowExpander();

    @UiField
    FramedPanel main;

    @UiField(provided = true)
    public Grid<PhenotypeSummaryValueObject> phenotypeGrid;

    @UiField
    public ToolBar toolbar;

    @UiField
    TextButton downloadAllButton;

    @UiField
    TextButton showEnrichmentButton;

    private List<AspireDbFilterConfig> subjectFilters = new ArrayList<AspireDbFilterConfig>();

    private List<Long> currentSubjectIds;

    private Integer totalSubjects;

    private ColumnConfig<PhenotypeSummaryValueObject, PhenotypeSummaryValueObject> selectedValuesColumn;

    public PhenotypeGrid() {
        ColumnModel<PhenotypeSummaryValueObject> columnModel = constructColumnModel();

        phenotypeStore = new ListStore<PhenotypeSummaryValueObject>( phenotypeProperties.id() );
        phenotypeGrid = new Grid<PhenotypeSummaryValueObject>( phenotypeStore, columnModel );

        phenotypeGrid.getView().setEmptyText( "No results" );

        expander.initPlugin( phenotypeGrid );

        initWidget( uiBinder.createAndBindUi( this ) );

        initHandlers();
    }

    private ColumnModel<PhenotypeSummaryValueObject> constructColumnModel() {
        ColumnConfig<PhenotypeSummaryValueObject, PhenotypeSummaryValueObject> nameColumn = new ColumnConfig<PhenotypeSummaryValueObject, PhenotypeSummaryValueObject>(
                phenotypeProperties.summary(), 250, "Name" );
        nameColumn.setCell( new NamePhenotypeCell() );

        selectedValuesColumn = new ColumnConfig<PhenotypeSummaryValueObject, PhenotypeSummaryValueObject>(
                phenotypeProperties.summary(), 100, "" );
        selectedValuesColumn.setCell( new SelectedPhenotypeCell() );
        
        selectedValuesColumn.setComparator( new Comparator<PhenotypeSummaryValueObject>() {
            @Override
            public int compare( PhenotypeSummaryValueObject object1, PhenotypeSummaryValueObject object2 ) {
                if (object1.getSelectedPhenotype() !=null && object2.getSelectedPhenotype()!=null){                
                    return object1.getSelectedPhenotype().getDbValue().compareTo( object2.getSelectedPhenotype().getDbValue() );
                }
                return -1;
            }
        }  );
        

        ColumnConfig<PhenotypeSummaryValueObject, PhenotypeSummaryValueObject> valuesSummaryColumn = new ColumnConfig<PhenotypeSummaryValueObject, PhenotypeSummaryValueObject>(
                phenotypeProperties.summary(), 250, "Value (subject count)" );
        valuesSummaryColumn.setCell( new ValueSummaryPhenotypeCell() );
        valuesSummaryColumn.setComparator( new Comparator<PhenotypeSummaryValueObject>() {
            @Override
            public int compare( PhenotypeSummaryValueObject object1, PhenotypeSummaryValueObject object2 ) {
                return ( int ) object1.getPresentRatio() - ( int ) object2.getPresentRatio();
            }
        } );

        // ColumnConfig<PhenotypeSummaryValueObject, Double> ratioColumn =
        // new ColumnConfig<PhenotypeSummaryValueObject, Double>(
        // phenotypeProperties.presentRatio(), 70, "Ratio" );

        List<ColumnConfig<PhenotypeSummaryValueObject, ?>> columns = new ArrayList<ColumnConfig<PhenotypeSummaryValueObject, ?>>();
        columns.add( expander );
        columns.add( nameColumn );
        columns.add( selectedValuesColumn );
        columns.add( valuesSummaryColumn );
        // columns.add( ratioColumn );
        ColumnModel<PhenotypeSummaryValueObject> columnModel = new ColumnModel<PhenotypeSummaryValueObject>( columns );
        return columnModel;
    }

    private void initHandlers() {
        aspiredb.EVENT_BUS.addHandler( LogoutEvent.TYPE, new LogoutEventHandler() {
            @Override
            public void onLogout( LogoutEvent event ) {
                phenotypeStore.clear();
            }
        } );
    }

    public void loadPhenotypeSummaries( List<Long> subjectIds ) {

        currentSubjectIds = subjectIds;
        
        showEnrichmentButton.disable();

        AsyncCallback<List<PhenotypeSummaryValueObject>> callback = new AspireAsyncCallback<List<PhenotypeSummaryValueObject>>() {
            @Override
            public void onSuccess( List<PhenotypeSummaryValueObject> result ) {
                phenotypeStore.replaceAll( result );
                phenotypeGrid.unmask();
            }
        };

        if ( subjectIds.size() > 0 ) {
            AsyncCallback<Integer> updateNumSubjectsCallback = new AsyncCallback<Integer>() {
                @Override
                public void onFailure( Throwable caught ) {
                }

                @Override
                public void onSuccess( Integer result ) {
                    totalSubjects = result;

                    if ( currentSubjectIds.size() < totalSubjects - 1 ) {
                        showEnrichmentButton.enable();
                    } 
                }
            };

            // TODO maybe set this in some global settings somewhere, only do it once when you select project(s)
            projectService.numSubjects( ActiveProjectSettings.getActiveProject(), new AspireAsyncCallback<Integer>(
                    updateNumSubjectsCallback ) );
        }

        phenotypeGrid.mask( "Loading..." );
        subjectService.getPhenotypeSummaries( subjectIds, ActiveProjectSettings.getActiveProjects(), callback );
    }

    public void highlightPhenotypes( final SubjectValueObject subject ) {
        AsyncCallback<Map<String, PhenotypeValueObject>> callback = new AspireAsyncCallback<Map<String, PhenotypeValueObject>>() {
            @Override
            public void onSuccess( Map<String, PhenotypeValueObject> phenotypes ) {
                selectedValuesColumn.setHeader( subject.getPatientId() );
                overlaySelectedSubjectPhenotypes( phenotypes );

                phenotypeGrid.getView().refresh( true );

                phenotypeGrid.unmask();
            }
        };

        phenotypeGrid.mask( "Loading..." );
        phenotypeService.getPhenotypes( subject.getId(), callback );
    }

    private void overlaySelectedSubjectPhenotypes( Map<String, PhenotypeValueObject> selectedSubjectPhenotypes ) {
        for ( PhenotypeSummaryValueObject phenotypeSummary : phenotypeStore.getAll() ) {
            PhenotypeValueObject subjectPhenotype = selectedSubjectPhenotypes.get( phenotypeSummary.getName() );
            phenotypeSummary.setSelectedPhenotype( subjectPhenotype );
        }
    }

    @UiHandler("downloadAllButton")
    public void onDownloadAllClick( SelectEvent event ) {

        AsyncCallback<List<SubjectValueObject>> callback = new AsyncCallback<List<SubjectValueObject>>() {
            @Override
            public void onSuccess( List<SubjectValueObject> result ) {
                TextDataDownloadWindow tddw = new TextDataDownloadWindow();
                tddw.showPhenotypesDownload( result );
                downloadAllButton.enable();
            }

            @Override
            public void onFailure( Throwable caught ) {
            }
        };

        downloadAllButton.disable();
        subjectService.getSubjectsWithPhenotypesBySubjectIds( currentSubjectIds,
                new AspireAsyncCallback<List<SubjectValueObject>>( callback ) );

    }

    @UiHandler("showEnrichmentButton")
    public void onShowEnrichmentClick( SelectEvent event ) {
        final PhenotypeEnrichmentGridWindow phenotypeEnrichmentWindow = new PhenotypeEnrichmentGridWindow();
        phenotypeEnrichmentWindow.showPhenotypeEnrichment( currentSubjectIds );
    }

    @Override
    public void onResize() {
        int width = this.main.getBody().getClientWidth();
        this.phenotypeGrid.setWidth( width );
        this.toolbar.setWidth( width );
    }

    public void setWidth( int width ) {
        this.main.setWidth( width );
        width = this.main.getBody().getClientWidth();
        this.phenotypeGrid.setWidth( width );
    }

    public void setHeight( int height ) {
        this.main.setHeight( height );
        int heightInsidePanel = this.main.getBody().getClientHeight();
        heightInsidePanel -= this.toolbar.getOffsetHeight();
        this.phenotypeGrid.setHeight( heightInsidePanel );
    }

    public void removeHighlight() {
        for ( PhenotypeSummaryValueObject phenotypeSummary : phenotypeStore.getAll() ) {
            phenotypeSummary.setSelectedPhenotype( null );
        }
        selectedValuesColumn.setHeader( "" );
        phenotypeGrid.getView().refresh( true );
    }

    public void setCurrentFilters( List<AspireDbFilterConfig> filterConfigs ) {
        subjectFilters = filterConfigs;
    }
}
