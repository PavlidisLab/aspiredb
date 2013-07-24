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
package ubc.pavlab.aspiredb.client.view;

import java.util.ArrayList;
import java.util.List;

import ubc.pavlab.aspiredb.client.ActiveProjectSettings;
import ubc.pavlab.aspiredb.client.aspiredb;
import ubc.pavlab.aspiredb.client.callback.AspireAsyncCallback;
import ubc.pavlab.aspiredb.client.events.LogoutEvent;
import ubc.pavlab.aspiredb.client.events.SelectSubjectEvent;
import ubc.pavlab.aspiredb.client.events.SubjectFilterEvent;
import ubc.pavlab.aspiredb.client.events.VariantFilterEvent;
import ubc.pavlab.aspiredb.client.handlers.LogoutEventHandler;
import ubc.pavlab.aspiredb.client.handlers.SubjectFilterHandler;
import ubc.pavlab.aspiredb.client.service.SubjectService;
import ubc.pavlab.aspiredb.client.service.SubjectServiceAsync;
import ubc.pavlab.aspiredb.client.view.filter.FilterWindow;
import ubc.pavlab.aspiredb.client.view.phenotype.PhenotypeGrid;
import ubc.pavlab.aspiredb.client.view.subject.SubjectGrid;
import ubc.pavlab.aspiredb.client.view.variant.VariantGrid;
import ubc.pavlab.aspiredb.shared.AspireDbPagingLoadConfig;
import ubc.pavlab.aspiredb.shared.SubjectValueObject;
import ubc.pavlab.aspiredb.shared.VariantValueObject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.data.shared.loader.LoadEvent;
import com.sencha.gxt.data.shared.loader.LoadHandler;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.widget.core.client.box.MessageBox;

/**
 * Main panel contains - subjects grid - variants grid - phenotypes grid
 * 
 * @author Paul
 * @version $Id: MainPanel.java,v 1.9 2013/07/04 22:45:08 frances Exp $
 */
public class MainPanel extends Composite implements RequiresResize {

    // UiBinder boilerplate.
    interface MyUIBinder extends UiBinder<Widget, MainPanel> {
    }

    private static MyUIBinder uiBinder = GWT.create( MyUIBinder.class );

    @UiField
    public SubjectGrid subjectGrid;

    @UiField
    public VariantGrid variantGrid;

    @UiField
    PhenotypeGrid phenotypeGrid;

    public FilterWindow filterWindow;

    // Back-end services
    private final SubjectServiceAsync subjectService = GWT.create( SubjectService.class );

    public MainPanel() {
        initWidget( uiBinder.createAndBindUi( this ) );
        setupLogoutHandler();
        
        SubjectFilterHandler subjectFilterHandler = new SubjectFilterHandler() {

            @Override
            public void onFilter( SubjectFilterEvent event ) {
                subjectGrid.applyFilter( event );
                variantGrid.applyFilter( new VariantFilterEvent( event.getFilterConfigs() ) );
                phenotypeGrid.setCurrentFilters( event.getFilterConfigs() );
            }
        };

        filterWindow = new FilterWindow();
        filterWindow.addSubjectFilterHandler( subjectFilterHandler );

        subjectGrid.remoteLoader
                .addLoadHandler( new LoadHandler<AspireDbPagingLoadConfig, PagingLoadResult<SubjectValueObject>>() {
                    /**
                     * Once subjects are loaded, use their ids to fetch their phenotypes.
                     */
                    @Override
                    public void onLoad( LoadEvent<AspireDbPagingLoadConfig, PagingLoadResult<SubjectValueObject>> event ) {
                        List<SubjectValueObject> subjects = event.getLoadResult().getData();
                        List<Long> subjectIds = new ArrayList<Long>();
                        for ( SubjectValueObject sub : subjects ) {
                            subjectIds.add( sub.getId() );
                        }
                        phenotypeGrid.loadPhenotypeSummaries( subjectIds );
                    }
                } );
        
        subjectGrid.addSubjectFilterHandler( subjectFilterHandler );

        // quickly hide panel by double clicking on the sliders
        if ( this.getWidget() instanceof SplitLayoutPanel ) {
            SplitLayoutPanel panel = ( SplitLayoutPanel ) this.getWidget();
            panel.setWidgetToggleDisplayAllowed( subjectGrid, true );
            panel.setWidgetToggleDisplayAllowed( phenotypeGrid, true );
        }
    }

    @UiHandler("subjectGrid")
    void onSelect( SelectSubjectEvent event ) {
        final Long subjectId = event.subjectId;
        if ( subjectId == null ) {
            variantGrid.mask( "Refreshing..." );
            Scheduler.get().scheduleDeferred( new Scheduler.ScheduledCommand() {
                @Override
                public void execute() {
                    variantGrid.ideogram.removeHighlight();
                    variantGrid.unmask();
                }
            } );

            Scheduler.get().scheduleDeferred( new Scheduler.ScheduledCommand() {
                @Override
                public void execute() {
                    phenotypeGrid.removeHighlight();
                }
            } );

        } else {
            variantGrid.mask( "Refreshing..." );
            Scheduler.get().scheduleDeferred( new Scheduler.ScheduledCommand() {
                @Override
                public void execute() {
                    variantGrid.ideogram.highlightSubject( subjectId, variantGrid.variantStore.getAll() );
                    variantGrid.unmask();
                }
            } );

            // Show(highlight) subject details (phenotype and variants)
            // TODO doing the iterator.next() thing to fix a bug, once project handling comes into more focus this
            // should change
            subjectService.getSubject( ActiveProjectSettings.getActiveProjects().iterator().next(), subjectId,
                    new AspireAsyncCallback<SubjectValueObject>( showSubjectDetails ) );
        }
    }

    private AsyncCallback<SubjectValueObject> showSubjectDetails = new AsyncCallback<SubjectValueObject>() {

        @Override
        public void onFailure( Throwable caught ) {
            MessageBox alertBox = new MessageBox( "Server Error", "There was an error fetching subject's details." );
            alertBox.show();
        }

        @Override
        public void onSuccess( SubjectValueObject subject ) {
            if ( subject == null ) {
                phenotypeGrid.removeHighlight();
                variantGrid.ideogram.removeHighlight();
            } else {
                phenotypeGrid.highlightPhenotypes( subject );

                String subjectId = subject.getPatientId();
                for ( VariantValueObject variant : variantGrid.variantStore.getAll() ) {
                    if ( variant.getPatientId().equals( subjectId ) ) {
                        int rowIndex = variantGrid.variantStore.indexOf( variant );
                        variantGrid.variantGrid.view.focusRow( rowIndex );
                        break;
                    }
                }
            }
        }
    };

    private void setupLogoutHandler() {
        aspiredb.EVENT_BUS.addHandler( LogoutEvent.TYPE, new LogoutEventHandler() {
            @Override
            public void onLogout( LogoutEvent event ) {
                clear();
            }
        } );
    }

    public void initialize() {
        subjectGrid.applyFilter( new SubjectFilterEvent() );
        variantGrid.applyFilter( new VariantFilterEvent() );
        this.resizeMe();
    }

    public void resizeMe() {
        final MainPanel me = this;

        Scheduler.get().scheduleDeferred( new Scheduler.ScheduledCommand() {

            @Override
            public void execute() {
                me.onResize();
            }
        } );
    }

    private void clear() {
        subjectGrid.subjectStore.clear();
        phenotypeGrid.phenotypeStore.clear();
        filterWindow.hide();
    }

    @Override
    public void onResize() {
        int height = this.getElement().getOffsetHeight();
        this.subjectGrid.setHeight( height );
        this.phenotypeGrid.setHeight( height );
        this.variantGrid.setHeight( height );

        this.subjectGrid.toolbar.setWidth( this.subjectGrid.subjectGrid.getOffsetWidth() );
        this.phenotypeGrid.toolbar.setWidth( this.phenotypeGrid.phenotypeGrid.getOffsetWidth() );
        this.variantGrid.toolbar.setWidth( this.variantGrid.tabPanel.getOffsetWidth() );
        this.variantGrid.variantGrid.setWidth( this.variantGrid.tabPanel.getOffsetWidth() );
    }

    public void showFilterWindow() {
        filterWindow.show();
    }
}
