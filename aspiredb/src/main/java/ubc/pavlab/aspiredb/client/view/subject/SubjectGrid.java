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
package ubc.pavlab.aspiredb.client.view.subject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.data.client.loader.RpcProxy;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.loader.*;
import com.sencha.gxt.data.shared.loader.BeforeLoadEvent.BeforeLoadHandler;
import com.sencha.gxt.widget.core.client.FramedPanel;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.event.RefreshEvent.HasRefreshHandlers;
import com.sencha.gxt.widget.core.client.event.RefreshEvent.RefreshHandler;
import com.sencha.gxt.widget.core.client.event.RowClickEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.info.Info;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

import java.util.*;

import ubc.pavlab.aspiredb.client.ActiveProjectSettings;
import ubc.pavlab.aspiredb.client.HasSubjectFilterHandlers;
import ubc.pavlab.aspiredb.client.HasSubjectSelectionHandlers;
import ubc.pavlab.aspiredb.client.aspiredb;
import ubc.pavlab.aspiredb.client.callback.AspireAsyncCallback;
import ubc.pavlab.aspiredb.client.events.LogoutEvent;
import ubc.pavlab.aspiredb.client.events.SelectSubjectEvent;
import ubc.pavlab.aspiredb.client.events.SubjectFilterEvent;
import ubc.pavlab.aspiredb.client.handlers.LogoutEventHandler;
import ubc.pavlab.aspiredb.client.handlers.SelectSubjectHandler;
import ubc.pavlab.aspiredb.client.handlers.SubjectFilterHandler;
import ubc.pavlab.aspiredb.client.service.QueryService;
import ubc.pavlab.aspiredb.client.service.QueryServiceAsync;
import ubc.pavlab.aspiredb.client.service.SubjectService;
import ubc.pavlab.aspiredb.client.service.SubjectServiceAsync;
import ubc.pavlab.aspiredb.client.view.CreateLabelWindow;
import ubc.pavlab.aspiredb.client.view.LabelCell;
import ubc.pavlab.aspiredb.client.view.TextDataDownloadWindow;
import ubc.pavlab.aspiredb.client.view.common.PagingGridPanel;
import ubc.pavlab.aspiredb.client.view.filter.SubjectComboBox;
import ubc.pavlab.aspiredb.shared.*;
import ubc.pavlab.aspiredb.shared.query.ProjectFilterConfig;
import ubc.pavlab.aspiredb.shared.query.SubjectLabelProperty;
import ubc.pavlab.aspiredb.shared.suggestions.SuggestionContext;

/**
 * TODO Document Me
 * 
 * @author azoubare
 * @version $Id: SubjectGrid.java,v 1.32 2013/07/12 21:28:03 anton Exp $
 */
public class SubjectGrid extends Composite implements HasSubjectSelectionHandlers, HasRefreshHandlers, RequiresResize, HasSubjectFilterHandlers {

    private static final int MAX_SUBJECTS_SHOWN = 2000;
    
    private static final String SELECT_ALL_TEXT = "Select all";
    private static final String DESELECT_ALL_TEXT = "Deselect";
    private static final String SELECT_ALL_TOOLTIP = "Select all rows";
    private static final String DESELECT_ALL_TOOLTIP = "Deselect all rows";

    // UIBinder boilerplate.
    interface MyUIBinder extends UiBinder<Widget, SubjectGrid> {
    }

    private static MyUIBinder uiBinder = GWT.create( MyUIBinder.class );

    private final SubjectValueObjectProperties subjectProperties = GWT.create( SubjectValueObjectProperties.class );
    public ListStore<SubjectValueObject> subjectStore = new ListStore<SubjectValueObject>( subjectProperties.id() );

    public PagingLoader<AspireDbPagingLoadConfig, PagingLoadResult<SubjectValueObject>> remoteLoader;

    private final QueryServiceAsync queryService = GWT.create( QueryService.class );
    private final SubjectServiceAsync subjectService = GWT.create( SubjectService.class );

    @UiField
    FramedPanel main;
    @UiField
    public ToolBar toolbar;
    @UiField
    MenuItem makeLabelButton;
    @UiField
    TextButton selectAllButton;
    @UiField
    MenuItem labelSettingsButton;
    @UiField
    TextButton downloadAllButton;

    private boolean selectAllButtonState = false;

    private final Set<Long> visibleLabels = new HashSet<Long>();

    @UiField(provided = true)
    public PagingGridPanel<SubjectValueObject> subjectGrid;

    public SubjectGrid() {
        subjectGrid = new PagingGridPanel<SubjectValueObject>( subjectStore, constructColumnModel() );

        initWidget( uiBinder.createAndBindUi( this ) );

        updateSelectAllButtonState(selectAllButtonState);
        
        initRemoteLoader();
        subjectGrid.setLoader( remoteLoader );

        initHandlers();

        // Initial state
        makeLabelButton.disable();

        subjectGrid.grid.getSelectionModel().addSelectionChangedHandler(new SelectionChangedEvent.SelectionChangedHandler<SubjectValueObject>() {
            @Override
            public void onSelectionChanged(SelectionChangedEvent<SubjectValueObject> event) {
                if ( event.getSelection().isEmpty() ) {
                    makeLabelButton.disable();
                } else {
                    makeLabelButton.enable();
                }
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
    
    @UiHandler("subjectGrid")
    public void onRowClick( RowClickEvent event ) {
        selectAllButtonState = selectionSize() > 0;
        updateSelectAllButtonState( selectAllButtonState );

        int rowIndex = event.getRowIndex();
        Long subjectId = getSubjectByRowIndex( rowIndex ).getId();
        String externalSubjectId = getSubjectByRowIndex( rowIndex ).getPatientId();

        this.fireEvent( new SelectSubjectEvent( subjectId, externalSubjectId ) );
    }

    private boolean isSelectionEmpty() {
        return selectionSize() == 0;
    }

    private int selectionSize() {
        return subjectGrid.grid.getSelectionModel().getSelectedItems().size();
    }

    private void initRemoteLoader() {
        DataProxy<AspireDbPagingLoadConfig, PagingLoadResult<SubjectValueObject>> remoteProxy = new RpcProxy<AspireDbPagingLoadConfig, PagingLoadResult<SubjectValueObject>>() {
            @Override
            public void load( AspireDbPagingLoadConfig loadConfig,
                    AsyncCallback<PagingLoadResult<SubjectValueObject>> callback ) {
                queryService.querySubjects( loadConfig, new AspireAsyncCallback<PagingLoadResult<SubjectValueObject>>(
                        callback ) );
            }
        };

        remoteLoader = new PagingLoader<AspireDbPagingLoadConfig, PagingLoadResult<SubjectValueObject>>( remoteProxy ) {
            @Override
            protected AspireDbPagingLoadConfig newLoadConfig() {
                AspireDbPagingLoadConfig config = new AspireDbPagingLoadConfigBean();
                config.setLimit( remoteLoader.getLimit() );
                config.setOffset( 0 );

                return config;
            }
        };

        // Get results into a grid
        remoteLoader
                .addLoadHandler( new LoadResultListStoreBinding<AspireDbPagingLoadConfig, SubjectValueObject, PagingLoadResult<SubjectValueObject>>(
                        subjectStore ) );

        // Mask the grid
        remoteLoader.addBeforeLoadHandler( new BeforeLoadHandler<AspireDbPagingLoadConfig>() {
            @Override
            public void onBeforeLoad( BeforeLoadEvent<AspireDbPagingLoadConfig> event ) {
                subjectGrid.mask( "Loading..." );
            }
        } );

        // Unmask the grid
        remoteLoader.addLoadHandler( new LoadHandler<AspireDbPagingLoadConfig, PagingLoadResult<SubjectValueObject>>() {
            @Override
            public void onLoad( LoadEvent<AspireDbPagingLoadConfig, PagingLoadResult<SubjectValueObject>> event ) {
                subjectGrid.unmask();
            }
        } );
    }

    private ColumnModel<SubjectValueObject> constructColumnModel() {
        ColumnConfig<SubjectValueObject, String> patientColumn = new ColumnConfig<SubjectValueObject, String>(
                subjectProperties.patientId(), 200, "Subject Id" );
        ColumnConfig<SubjectValueObject, Collection<LabelValueObject>> labelsColumn = new ColumnConfig<SubjectValueObject, Collection<LabelValueObject>>(
                subjectProperties.labels(), 100, "Labels" );
        labelsColumn.setSortable(false);
        labelsColumn.setCell( new LabelCell(visibleLabels) );
        List<ColumnConfig<SubjectValueObject, ?>> columns = new ArrayList<ColumnConfig<SubjectValueObject, ?>>();
        columns.add( patientColumn );
        columns.add( labelsColumn );

        ColumnModel<SubjectValueObject> cm = new ColumnModel<SubjectValueObject>( columns );
        return cm;
    }

    public SubjectValueObject getSubjectByRowIndex( int rowIndex ) {
        return subjectStore.get( rowIndex );
    }

    private void initHandlers() {
        aspiredb.EVENT_BUS.addHandler( LogoutEvent.TYPE, new LogoutEventHandler() {
            @Override
            public void onLogout( LogoutEvent event ) {
                subjectStore.clear();
            }
        } );
        aspiredb.EVENT_BUS.addHandler(SubjectFilterEvent.TYPE, new SubjectFilterHandler() {
            @Override
            public void onFilter(SubjectFilterEvent event) {
                applyFilter(event);
            }
        });
    }

    public void applyFilter( SubjectFilterEvent event ) {
        AspireDbPagingLoadConfig loadConfig = new AspireDbPagingLoadConfigBean();
        loadConfig.getFilters().add( new ProjectFilterConfig( ActiveProjectSettings.getActiveProjects() ) );

        loadConfig.setOffset( 0 );
        loadConfig.setLimit( MAX_SUBJECTS_SHOWN );

        loadConfig.getFilters().addAll( event.getFilterConfigs() );

        remoteLoader.load( loadConfig );
    }

    @Override
    public HandlerRegistration addSelectSubjectHandler( SelectSubjectHandler handler ) {
        return this.addHandler(handler, SelectSubjectEvent.TYPE);
    }

    @Override
    public HandlerRegistration addRefreshHandler( RefreshHandler handler ) {
        return this.subjectGrid.addRefreshHandler( handler );
    }

    @Override
    public void onResize() {
        int width = this.main.getBody().getClientWidth();
        int height = this.main.getBody().getClientHeight() - this.toolbar.getOffsetHeight( false );
        this.subjectGrid.setHeight( height );
        this.subjectGrid.setWidth( width );
        this.toolbar.setWidth(width);
    }

    /**
     * @param event
     */
    @UiHandler("selectAllButton")
    public void onSelectAllClick( SelectEvent event ) {
        selectAllButtonState = !selectAllButtonState;
        updateSelectAllButtonState(selectAllButtonState);

        if ( selectAllButtonState ) {
                subjectGrid.grid.getSelectionModel().selectAll();
        } else {
            // subjectGrid.grid.mask("Deselecting...");
            subjectGrid.grid.getSelectionModel().deselectAll();
            this.fireEvent( new SelectSubjectEvent( null, null ) );
            // subjectGrid.grid.unmask();
        }
    }

    /**
     * @param event
     */
    @UiHandler("downloadAllButton")
    public void onDownloadAllClick( SelectEvent event ) {
        List<SubjectValueObject> subjectList = subjectStore.getAll();

        // pass in properties here
        TextDataDownloadWindow tddw = new TextDataDownloadWindow();
        tddw.showSubjectDownload( subjectList );

    }

    /**
     * @param event
     */
    @UiHandler("makeLabelButton")
    public void onMakeLabelClick( SelectionEvent<Item> event ) {
        SubjectComboBox subjectComboBox = new SubjectComboBox( new SubjectLabelProperty(), false );
        subjectComboBox.setForceSelection( false );

        CreateLabelWindow labelWindow = new CreateLabelWindow( subjectComboBox ) {
            @Override
            public void onOkButtonClick( ClickEvent clickEvent ) {
                super.onOkButtonClick( clickEvent );

                Collection<SubjectValueObject> subjects = subjectGrid.grid.getSelectionModel().getSelection();

                if ( subjects.isEmpty() ) {
                    MessageBox alertBox = new MessageBox( "No Subjects Selected",
                            "Please select subjects to use this function" );
                    alertBox.show();
                    return;
                }

                Collection<Long> ids = new ArrayList<Long>();
                for ( SubjectValueObject subject : subjects ) {
                    ids.add( subject.getId() );
                }

                AsyncCallback<LabelValueObject> callback = new AsyncCallback<LabelValueObject>() {
                    @Override
                    public void onFailure( Throwable caught ) {
                        Info.display( "Error", caught.getMessage() );
                        subjectGrid.unmask();
                    }

                    @Override
                    public void onSuccess( LabelValueObject label ) {
                        visibleLabels.add( label.getId() );
                        subjectGrid.grid.getLoader().setReuseLoadConfig( true );
                        subjectGrid.grid.getLoader().load();
                        subjectGrid.grid.getLoader().setReuseLoadConfig( false );
                        subjectGrid.unmask();
                    }
                };

                subjectGrid.mask("Labeling...");
                subjectService.addLabel( ids, this.getLabel(), new AspireAsyncCallback<LabelValueObject>( callback ) );
            }
        };
        labelWindow.show();
    }

    /**
     * @param event
     */
    @UiHandler("labelSettingsButton")
    public void onLabelSettingsClick( SelectionEvent<Item> event ) {
        final LabelControlWindow w = new LabelControlWindow( visibleLabels, subjectGrid.view, true );
        w.show();

        AsyncCallback<List<LabelValueObject>> updateSubjectLabelsCallback = new AsyncCallback<List<LabelValueObject>>() {
            @Override
            public void onFailure( Throwable caught ) {
            }

            @Override
            public void onSuccess( List<LabelValueObject> labels ) {
                w.initializeStore( labels );
            }
        };

        subjectService.suggestLabels( new SuggestionContext(), new AspireAsyncCallback<List<LabelValueObject>>(
                updateSubjectLabelsCallback ) );
    }

    public void setHeight( int height ) {
        this.main.setHeight( height );
        int heightInsidePanel = this.main.getBody().getClientHeight();
        heightInsidePanel -= this.toolbar.getOffsetHeight();
        this.subjectGrid.grid.setHeight( heightInsidePanel );
    }
    
    @Override
    public HandlerRegistration addSubjectFilterHandler( SubjectFilterHandler handler ) {
        return this.addHandler( handler, SubjectFilterEvent.TYPE );
    }
}