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
package ubc.pavlab.aspiredb.client.view.filter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer.BorderLayoutData;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import ubc.pavlab.aspiredb.client.ActiveProjectSettings;
import ubc.pavlab.aspiredb.client.HasSubjectFilterHandlers;
import ubc.pavlab.aspiredb.client.aspiredb;
import ubc.pavlab.aspiredb.client.callback.AspireAsyncCallback;
import ubc.pavlab.aspiredb.client.events.QueryUpdateEvent;
import ubc.pavlab.aspiredb.client.events.RemoveMeEvent;
import ubc.pavlab.aspiredb.client.events.SubjectFilterEvent;
import ubc.pavlab.aspiredb.client.handlers.QueryUpdateEventHandler;
import ubc.pavlab.aspiredb.client.handlers.RemoveMeHandler;
import ubc.pavlab.aspiredb.client.handlers.SubjectFilterHandler;
import ubc.pavlab.aspiredb.client.service.*;
import ubc.pavlab.aspiredb.client.view.SaveQueryWindow;
import ubc.pavlab.aspiredb.shared.AspireDbPagingLoadConfig;
import ubc.pavlab.aspiredb.shared.AspireDbPagingLoadConfigBean;
import ubc.pavlab.aspiredb.shared.VariantType;
import ubc.pavlab.aspiredb.shared.query.*;

import java.util.*;

/**
 * Filter Window
 * 
 * @version $Id: FilterWindow.java,v 1.26 2013/07/12 19:38:45 anton Exp $
 */
public class FilterWindow extends Window implements HasSubjectFilterHandlers {

    public void clearFilter() {
        filterContainer.clear();
        savedQueryComboBox.setValue(null);
        aspiredb.EVENT_BUS.fireEvent(new QueryUpdateEvent());
    }

    interface MyUIBinder extends UiBinder<Widget, FilterWindow> {}
    private static MyUIBinder uiBinder = GWT.create( MyUIBinder.class );
   
    private static final String FILTER_PLACEHOLDER = "<Filter>";
    private static final String SUBJECT_FILTER = "Subject Filter";
    private static final String VARIANT_FILTER = "Variant Filter";
    private static final String PHENOTYPE_FILTER = "Phenotype Filter";
	
    private static final QueryServiceAsync queryService = GWT.create( QueryService.class );
    private static final SubjectServiceAsync subjectService = GWT.create( SubjectService.class );
    private static final VariantServiceAsync variantService = GWT.create( VariantService.class );

    @UiField(provided = true)
    BorderLayoutData northData = new BorderLayoutData(40);
    
    @UiField(provided = true)
    BorderLayoutData southData = new BorderLayoutData(40);

    @UiField
    VerticalLayoutContainer filterContainer;    
    
    @UiField(provided = true)
    ComboBox<String> filterTypeComboBox;
    
    @UiField(provided = true)
    ComboBox<QueryValueObject> savedQueryComboBox;

    @UiField
    Label numberOfSubjectsLabel;

    @UiField
    Label numberOfVariantsLabel;

    @UiField
    Button applyButton;

    @UiField
    Button saveQueryButton;

    @UiField
    RadioButton subjectRadioButton;

    @UiField
    RadioButton variantRadioButton;

    // TODO : refactor into separate class
    private int numPreviewQueriesInProgress = 0;
    private boolean runPreviewQuery = false;

	private Collection<Property> subjectProperties;
	private Map<String, Collection<Property>> variantProperties; 
	private Collection<Property> variantLocationProperties;
	
	private ListStore<QueryValueObject> savedQueryListStore;
    
    private static void initializeComboBox(ComboBox comboBox) {
    	comboBox.setEditable(false);
    	comboBox.setForceSelection(true);
    	comboBox.setTriggerAction(TriggerAction.ALL);
    }
    
    public FilterWindow() {
    	ListStore<String> filterTypeListStore = new ListStore<String>(new ModelKeyProvider<String>() {
    		@Override
    		public String getKey(String item) { return item; } 
    	}); 	
    	filterTypeComboBox = new ComboBox<String>(filterTypeListStore, new LabelProvider<String>() {
    		@Override
    		public String getLabel(String item) { return item; }
    	});
    	initializeComboBox(filterTypeComboBox);

    	savedQueryListStore = new ListStore<QueryValueObject>(new ModelKeyProvider<QueryValueObject>() {
    		@Override
    		public String getKey(QueryValueObject query) { return query.getName(); } 
    	}); 	
    	savedQueryComboBox = new ComboBox<QueryValueObject>(savedQueryListStore, new LabelProvider<QueryValueObject>() {
    		@Override
    		public String getLabel(QueryValueObject query) { return query.getName(); }
    	});
    	initializeComboBox(savedQueryComboBox);

		subjectService.suggestProperties(new AsyncCallback<Collection<Property>>() {
            @Override
            public void onFailure(Throwable caught) {
            }

            @Override
            public void onSuccess(Collection<Property> properties) {
            	subjectProperties = properties;
            }
        });
        
		variantProperties = new HashMap<String, Collection<Property>>();
		String[] variantTypeNames = { "CNV", "Indel", "SNV" };
		for (final String variantTypeName: variantTypeNames) {
			variantService.suggestProperties(VariantType.findByName(variantTypeName), new AsyncCallback<Collection<Property>>() {
	            @Override
	            public void onFailure(Throwable caught) {
	            }

	            @Override
	            public void onSuccess(Collection<Property> properties) {
	            	variantProperties.put(variantTypeName, properties);
	            }
			});
		}
		
		variantService.suggestVariantLocationProperties(new AsyncCallback<Collection<Property>>() {
		    @Override
		    public void onFailure(Throwable caught) {
		    }
		
		    @Override
		    public void onSuccess(Collection<Property> properties) {
		    	variantLocationProperties = properties;
		    }
		});

    	setWidget(uiBinder.createAndBindUi(this));
    	
    	this.setHeadingText("Filter");
    	this.setPixelSize(800, 580);

    	filterTypeListStore.add( FILTER_PLACEHOLDER );
    	filterTypeListStore.add( SUBJECT_FILTER );
    	filterTypeListStore.add( VARIANT_FILTER );
    	filterTypeListStore.add( PHENOTYPE_FILTER );
    	filterTypeComboBox.setValue(FILTER_PLACEHOLDER);
    	
		this.addResizeHandler(new ResizeHandler() {
			@Override
			public void onResize(ResizeEvent event) {
				refreshSavedQueryComboBox();
			}
		});

        aspiredb.EVENT_BUS.addHandler( QueryUpdateEvent.TYPE, new QueryUpdateEventHandler() {
            @Override
            public void onQueryUpdate(QueryUpdateEvent event) {
                // update result counts
                if (numPreviewQueriesInProgress > 0) {
                    runPreviewQuery = true; // run after current on completes.
                } else {
                    runPreviewQuery = false;
                    updateResultCounts();
                }
            }
        } );
    }

    private void updateResultCounts() {
        numPreviewQueriesInProgress = 2;
        numberOfSubjectsLabel.setText("...");
        numberOfVariantsLabel.setText("...");

        AspireDbPagingLoadConfig config = new AspireDbPagingLoadConfigBean();
        config.getFilters().add( new ProjectFilterConfig( ActiveProjectSettings.getActiveProjects() ) );
        config.setOffset(0);
        config.setLimit(2000);
        config.getFilters().addAll( getFilterConfigs() );
        queryService.getSubjectCount(config, new AspireAsyncCallback<Integer>(){
            @Override
            public void onSuccess(Integer count) {
                numberOfSubjectsLabel.setText(count.toString());
                numPreviewQueriesInProgress--;
                if (numPreviewQueriesInProgress == 0 && runPreviewQuery) {
                    updateResultCounts();
                    runPreviewQuery = false;
                }
            }
        });

        queryService.getVariantCount(config, new AspireAsyncCallback<Integer>(){
            @Override
            public void onSuccess(Integer count) {
                numberOfVariantsLabel.setText(count.toString());
                numPreviewQueriesInProgress--;
                if (numPreviewQueriesInProgress == 0 && runPreviewQuery) {
                    updateResultCounts();
                    runPreviewQuery = false;
                }
            }
        });
    }

    @UiHandler("applyButton")
    public void onApplyFilterClick(ClickEvent event) {
    	SubjectFilterEvent e = new SubjectFilterEvent( this.getFilterConfigs() );
    	this.fireEvent( e );
    	this.hide();
    }

    @UiHandler("saveQueryButton")
    public void onSaveFilterClick(ClickEvent event) {
        final SaveQueryWindow saveQueryWindow = new SaveQueryWindow() {
        	@Override
        	public void onOkButtonClick(ClickEvent event) {
                super.onOkButtonClick(event);
                
                String queryName = this.getQueryName();

                final QueryValueObject queryValueObject = new QueryValueObject(queryName, getFilterConfigs());
                queryService.saveQuery(queryValueObject, new AsyncCallback<QueryValueObject>(){
                    @Override
                    public void onFailure(Throwable caught) {
                    }

                    @Override
                    public void onSuccess(QueryValueObject result) {
                        refreshSavedQueryComboBox();
                        savedQueryComboBox.setValue(queryValueObject);
                    }
                });
            }
        };

        saveQueryWindow.show();
    }

    @UiHandler("clearButton")
    public void onClearButtonClick(ClickEvent event) {
    	filterContainer.clear();
        this.hide(); // Browser bug workaround.
        this.show(); // Browser bug workaround.
    	savedQueryComboBox.setValue(null);
        aspiredb.EVENT_BUS.fireEvent(new QueryUpdateEvent());
    }
    
    @UiHandler("cancelButton")
    public void onCancelButtonClick(ClickEvent event) {
    	this.hide();
    }
    
    private void refreshSavedQueryComboBox() {
    	savedQueryListStore.clear();
    	
    	AsyncCallback<Collection<QueryValueObject>> callback = new AsyncCallback<Collection<QueryValueObject>>() {
            @Override
            public void onFailure(Throwable caught) {
            }

            @Override
            public void onSuccess(Collection<QueryValueObject> queries) {
                savedQueryListStore.addAll(queries);
            }
        };

        queryService.getSavedQueries(new AspireAsyncCallback<Collection<QueryValueObject>>(callback));
    }

    @UiHandler("filterTypeComboBox")
    public void onFilterTypeSelection(SelectionEvent<String> event) {
    	FilterWidget filterWidget = null;
    	String selectedFilter = event.getSelectedItem();
    	if (selectedFilter.equals(SUBJECT_FILTER)) {
    		filterWidget = new SubjectFilterWidget(subjectProperties);
    	} else if (selectedFilter.equals(VARIANT_FILTER)) {
    		filterWidget =  new VariantFilterWidget(variantProperties, variantLocationProperties);
    	} else if (selectedFilter.equals(PHENOTYPE_FILTER)) {
    		filterWidget = new PhenotypeFilterWidget();
    	}

    	if (filterWidget != null) {
    		savedQueryComboBox.setValue(null);
    		
    		// Using setValue() does not work.
	    	filterTypeComboBox.setText(FILTER_PLACEHOLDER);

            final Window me = this;
	    	filterWidget.addRemoveMeHandler( new RemoveMeHandler() {
				@Override
				public void onRemoveMe( final RemoveMeEvent event ) {
                    // Browser bug workaround.
                    if (filterContainer.getWidgetCount() == 1) {
                        me.hide();
                    }

                    filterContainer.remove(event.getWidget());

                    // Browser bug workaround.
                    if (filterContainer.getWidgetCount() == 0) {
                        me.show();
                    }

                    aspiredb.EVENT_BUS.fireEvent(new QueryUpdateEvent());
                }
	    	});
	    	
	    	filterWidget.addNewRow();
	    	filterContainer.add( filterWidget );
    	}
    }
    
   	@UiHandler("savedQueryComboBox")
    public void onSavedQuerySelection(SelectionEvent<QueryValueObject> event) {
        QueryValueObject selectedQuery = event.getSelectedItem();

        filterContainer.clear();
    	for (RestrictionFilterConfig filterConfig: selectedQuery.getQuery()) {
    		FilterWidget filterWidget = null;
    		
    		if (filterConfig instanceof PhenotypeFilterConfig) {
    			filterWidget = new PhenotypeFilterWidget();
    		} else if (filterConfig instanceof SubjectFilterConfig) {
    			filterWidget = new SubjectFilterWidget(subjectProperties);
    		} else if (filterConfig instanceof VariantFilterConfig) {
    			filterWidget = new VariantFilterWidget(variantProperties, variantLocationProperties);
    		}
    		
    		if (filterWidget != null) {
    	    	filterWidget.addRemoveMeHandler( new RemoveMeHandler() {
    				@Override
    				public void onRemoveMe( RemoveMeEvent event ) {
    					filterContainer.remove( event.getWidget() );
                        aspiredb.EVENT_BUS.fireEvent(new QueryUpdateEvent());
    				}    		
    	    	});
    			filterWidget.setFilterConfig(filterConfig);
    			filterContainer.add(filterWidget);
    		}
    	}

        aspiredb.EVENT_BUS.fireEvent(new QueryUpdateEvent());
    }

    public Set<RestrictionFilterConfig> getFilterConfigs() {
    	Set<RestrictionFilterConfig> filterConfigs = new HashSet<RestrictionFilterConfig>();
    	Iterator<Widget> iterator = filterContainer.iterator();

    	while ( iterator.hasNext() ) {
    		FilterWidget filterWidget = (FilterWidget) iterator.next();  		
    		filterConfigs.add ( filterWidget.getFilterConfig() );
    	}
    	return filterConfigs;
    }

    @Override
    public HandlerRegistration addSubjectFilterHandler( SubjectFilterHandler handler ) {
        return this.addHandler( handler, SubjectFilterEvent.TYPE );
    }
}
