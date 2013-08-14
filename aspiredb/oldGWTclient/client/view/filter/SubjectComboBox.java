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
package ubc.pavlab.aspiredb.client.view.filter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.data.client.loader.RpcProxy;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.loader.PagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoadResultBean;
import com.sencha.gxt.widget.core.client.event.BlurEvent;
import com.sencha.gxt.widget.core.client.event.BlurEvent.BlurHandler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ubc.pavlab.aspiredb.client.ActiveProjectSettings;
import ubc.pavlab.aspiredb.client.service.SubjectService;
import ubc.pavlab.aspiredb.client.service.SubjectServiceAsync;
import ubc.pavlab.aspiredb.shared.query.Property;
import ubc.pavlab.aspiredb.shared.query.PropertyValue;
import ubc.pavlab.aspiredb.shared.suggestions.SuggestionContext;


/**
 * Subject ComboBox
 * 
 * @author frances
 * @version $Id: SubjectComboBox.java,v 1.9 2013/06/28 18:06:00 frances Exp $
 */
public class SubjectComboBox extends SuggestionComboBox<Serializable> {
	private static final int MIN_CHARS = 1;

	private final SubjectServiceAsync subjectService = GWT.create( SubjectService.class );

	public SubjectComboBox(final Property property, boolean shouldValidateOnBlur) {
		super(
			new ModelKeyProvider<Serializable>() {
				@Override
				public String getKey(Serializable object) {
					return object.toString();
				}},
			new LabelProvider<Serializable>() {
				@Override
				public String getLabel(Serializable object) {
					return object.toString();
				}});

		this.setDataProxy(
			new RpcProxy<PagingLoadConfig, PagingLoadResult<Serializable>>() {
		        @Override
		        public void load(PagingLoadConfig loadConfig, final AsyncCallback<PagingLoadResult<Serializable>> callback) {
		    		SuggestionContext suggestionContext = new SuggestionContext();
		    		suggestionContext.setValuePrefix(getText());
		    		suggestionContext.setActiveProjectIds(ActiveProjectSettings.getActiveProjects());
		    		
		    		subjectService.suggestValues(property, suggestionContext, new AsyncCallback<Collection<PropertyValue>>() {
		                @Override
		                public void onFailure(Throwable caught) {
		                }

		                @Override
		                public void onSuccess(Collection<PropertyValue> propertyValues) {
		                	List<Serializable> suggestions = new ArrayList<Serializable>();
		                	
		                	for (PropertyValue propertyValue : propertyValues) {
		                		suggestions.add(propertyValue.getValue());
		                	}
		                	
		            		callback.onSuccess(new PagingLoadResultBean<Serializable>(new ArrayList<Serializable>(suggestions), 0, 0));
		                }
		            });
		        }
			});
		
		this.setForceSelection(true);
		this.setMinChars(MIN_CHARS);
		
		if (shouldValidateOnBlur) {
            this.addBlurHandler( new BlurHandler() {
                @Override
                public void onBlur( final BlurEvent event ) {
                    // Should not use getText() because it will return an empty string.
                    final String currentText = getCurrentText();
    
                    if (!currentText.equals( "" )) {
                        SuggestionContext suggestionContext = new SuggestionContext();
                        suggestionContext.setValuePrefix(currentText);
                        suggestionContext.setActiveProjectIds(ActiveProjectSettings.getActiveProjects());
                    
                        subjectService.suggestValues(property, suggestionContext, new AsyncCallback<Collection<PropertyValue>>() {                
                            @Override
                            public void onFailure(Throwable caught) {
                            }
        
                            @Override
                            public void onSuccess(Collection<PropertyValue> propertyValues) {
                                validateOnBlur(currentText, propertyValues);
                            }
                        });
                    }
                }
            } );
		}
	}
}
