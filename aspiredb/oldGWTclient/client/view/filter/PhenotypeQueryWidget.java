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
package ubc.pavlab.aspiredb.client.view.filter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.data.client.loader.RpcProxy;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.Store.StoreSortInfo;
import com.sencha.gxt.data.shared.loader.LoadResultListStoreBinding;
import com.sencha.gxt.data.shared.loader.PagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoader;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import com.sencha.gxt.widget.core.client.form.SimpleComboBox;
import ubc.pavlab.aspiredb.client.ActiveProjectSettings;
import ubc.pavlab.aspiredb.client.AutoSuggestComboBox;
import ubc.pavlab.aspiredb.client.aspiredb;
import ubc.pavlab.aspiredb.client.callback.AspireAsyncCallback;
import ubc.pavlab.aspiredb.client.events.QueryUpdateEvent;
import ubc.pavlab.aspiredb.client.events.RemoveMeEvent;
import ubc.pavlab.aspiredb.client.handlers.RemoveMeHandler;
import ubc.pavlab.aspiredb.client.service.QueryService;
import ubc.pavlab.aspiredb.client.service.QueryServiceAsync;
import ubc.pavlab.aspiredb.shared.query.restriction.PhenotypeRestriction;
import ubc.pavlab.aspiredb.shared.query.restriction.RestrictionExpression;
import ubc.pavlab.aspiredb.shared.suggestions.PhenotypeSuggestion;

import java.util.Comparator;
import java.util.List;

public class PhenotypeQueryWidget extends NonExpandableQueryWidget {

	interface MyUIBinder extends UiBinder<Widget, PhenotypeQueryWidget> {}
	private static MyUIBinder uiBinder = GWT.create( MyUIBinder.class );

    private final QueryServiceAsync queryService = GWT.create( QueryService.class );

	@UiField(provided=true)
	ComboBox<PhenotypeSuggestion> phenotypeComboBox;

	@UiField(provided=true)
	SimpleComboBox<String> value;

	@UiField
	Image removeImage;

	private PagingLoader<PagingLoadConfig, PagingLoadResult<PhenotypeSuggestion>> phenotypeTermloader;

	public PhenotypeQueryWidget () {
		
        RpcProxy<PagingLoadConfig, PagingLoadResult<PhenotypeSuggestion>> ontologySearchProxy =
        		new RpcProxy<PagingLoadConfig, PagingLoadResult<PhenotypeSuggestion>>() {
            @Override
            public void load( PagingLoadConfig loadConfig, AsyncCallback<PagingLoadResult<PhenotypeSuggestion>> callback ) {
                queryService.getPhenotypeSuggestionLoadResult( phenotypeComboBox.getText(), ActiveProjectSettings.getActiveProjects(),
                        new AspireAsyncCallback<PagingLoadResult<PhenotypeSuggestion>>(callback));
            }
        };

		phenotypeTermloader = new PagingLoader<PagingLoadConfig, PagingLoadResult<PhenotypeSuggestion>>( ontologySearchProxy );
		final ListStore<PhenotypeSuggestion> store = new ListStore<PhenotypeSuggestion>(new ModelKeyProvider<PhenotypeSuggestion>() {

            @Override
            public String getKey(PhenotypeSuggestion item) {
                return item.getName();
            }

        });
		store.addSortInfo(new StoreSortInfo<PhenotypeSuggestion>(
				new Comparator<PhenotypeSuggestion>() {
					public int compare(PhenotypeSuggestion suggestion1, PhenotypeSuggestion suggestion2) {
						return suggestion1.getName().toLowerCase().compareTo(suggestion2.getName().toLowerCase());
					}
				}, SortDir.ASC));

		phenotypeTermloader.addLoadHandler(
				new LoadResultListStoreBinding<PagingLoadConfig, PhenotypeSuggestion, PagingLoadResult<PhenotypeSuggestion>> ( store )
		);

		phenotypeComboBox = new AutoSuggestComboBox<PhenotypeSuggestion>(store, new LabelProvider<PhenotypeSuggestion>() {

			@Override
			public String getLabel(PhenotypeSuggestion item) {
//                if (item.isExistInDatabase()) {
//                    return item.getName()+"*";
//                } else {
				    return item.getName();
//                }
			}
		});

        phenotypeComboBox.setQueryDelay( 500 );

		phenotypeComboBox.setLoader( phenotypeTermloader );
		
		phenotypeComboBox.setHideTrigger( true );
		phenotypeComboBox.setForceSelection( true );
//		phenotypeComboBox.setTypeAhead( true );
		phenotypeComboBox.setMinChars( 3 );
		phenotypeComboBox.setTriggerAction(TriggerAction.QUERY);
		
		LabelProvider<String> labelProvider = new LabelProvider<String>() {

			@Override
			public String getLabel(String item) {
			    return decodePhenotypeValue( item.toString() );
			}		
		};
		
		value = new SimpleComboBox<String>( labelProvider );
		value.setTriggerAction(TriggerAction.ALL);
		
		initWidget( uiBinder.createAndBindUi( this ) );

        value.addSelectionHandler(new SelectionHandler<String>() {
            @Override
            public void onSelection(SelectionEvent<String> stringSelectionEvent) {
                String text = stringSelectionEvent.getSelectedItem();
                value.setValue( encodePhenotypeValue(text) );
                aspiredb.EVENT_BUS.fireEvent(new QueryUpdateEvent());
            }
        });
	}

	private String encodePhenotypeValue(String text) {
        if ( text.equals( "Absent" ) ) {
            return "0" ;
        } else if ( text.equals( "Present" ) ) {
            return "1" ;
        } else {
            return text;
        }
    }
    
    private String decodePhenotypeValue(String text) {
        if ( text.equals( "0" ) ) {
            return "Absent";
        } else if ( text.equals( "1" ) ) { 
            return "Present";
        } else {
            return text;
        }
    }

    /**
	 * @param event  
	 */
    @UiHandler("removeImage")
    public void onRemoveButtonClick (ClickEvent event) {
    	this.fireEvent( new RemoveMeEvent( this ) );
        aspiredb.EVENT_BUS.fireEvent(new QueryUpdateEvent());
    }
    
	@UiHandler("phenotypeComboBox")
	public void onSelection ( SelectionEvent <PhenotypeSuggestion> event) {
		value.getStore().clear();
		
		queryService.getValuesForOntologyTerm( event.getSelectedItem().getName(), new AsyncCallback<List<String>>() {

			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub				
			}

			@Override
			public void onSuccess(List<String> result) {
                if (result.isEmpty()) {
                    value.add("0");
                    value.add("1");
                } else {
				    value.add( result );
                }
			}
		});
	}
	
    @Override
    public RestrictionExpression getRestrictionExpression() {
        return new PhenotypeRestriction (
                phenotypeComboBox.getValue().getName(),
                value.getValue(),
                phenotypeComboBox.getValue().getUri() );
    }

	@Override
	public void setRestrictionExpression(RestrictionExpression restriction) {
		if (restriction instanceof PhenotypeRestriction) {
			PhenotypeRestriction phenotypeRestriction = (PhenotypeRestriction)restriction;

			this.phenotypeComboBox.setValue(new PhenotypeSuggestion(phenotypeRestriction.getName(), phenotypeRestriction.getUri()));
			this.value.setValue(phenotypeRestriction.getValue());
		}
	}

	@Override
	public HandlerRegistration addRemoveMeHandler(RemoveMeHandler handler) {
		return this.addHandler( handler, RemoveMeEvent.TYPE );
	}

	@Override
	public void setRemoveButtonVisible(boolean visible) {
		removeImage.setVisible(visible);
	}
}
