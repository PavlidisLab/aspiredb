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
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.text.shared.AbstractSafeHtmlRenderer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.data.client.loader.RpcProxy;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.loader.PagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoadResultBean;
import com.sencha.gxt.widget.core.client.event.BlurEvent;
import com.sencha.gxt.widget.core.client.event.BlurEvent.BlurHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ubc.pavlab.aspiredb.client.ActiveProjectSettings;
import ubc.pavlab.aspiredb.client.service.VariantService;
import ubc.pavlab.aspiredb.client.service.VariantServiceAsync;
import ubc.pavlab.aspiredb.client.view.suggestBox.Displayable;
import ubc.pavlab.aspiredb.shared.GwtSerializable;
import ubc.pavlab.aspiredb.shared.query.Property;
import ubc.pavlab.aspiredb.shared.query.PropertyValue;
import ubc.pavlab.aspiredb.shared.suggestions.SuggestionContext;


/**
 * Variant Location Property Value ComboBox
 * 
 * @author frances
 * @version $Id: VariantLocationPropertyValueComboBox.java,v 1.5 2013/06/28 18:06:00 frances Exp $
 */
public class VariantLocationPropertyValueComboBox extends SuggestionComboBox<GwtSerializable> {
	private static final int MIN_CHARS = 1;

	private final VariantServiceAsync variantService = GWT.create( VariantService.class );

	public VariantLocationPropertyValueComboBox(final Property property, boolean shouldValidateOnBlur) {
		super(
			new ModelKeyProvider<GwtSerializable>() {
				@Override
				public String getKey(GwtSerializable object) {
					return object.toString();
				}},
			new LabelProvider<GwtSerializable>() {
				@Override
				public String getLabel(GwtSerializable object) {
					return object instanceof Displayable
							? ((Displayable)object).getLabel()
							: object.toString();
				}}, 
			new AbstractSafeHtmlRenderer<GwtSerializable>() {
				public SafeHtml render(final GwtSerializable object) {
					return new SafeHtml() {
						private static final long serialVersionUID = 5590416939844198514L;
						@Override
						public String asString() {
							return object instanceof Displayable
									? ((Displayable)object).getHtmlLabel()
									: object.toString();
						}
					};
				}
			});

		this.setDataProxy(
			new RpcProxy<PagingLoadConfig, PagingLoadResult<GwtSerializable>>() {
		        @Override
		        public void load(PagingLoadConfig loadConfig, final AsyncCallback<PagingLoadResult<GwtSerializable>> callback) {
		    		SuggestionContext suggestionContext = new SuggestionContext();
		    		suggestionContext.setValuePrefix(getText());
		    		suggestionContext.setActiveProjectIds(ActiveProjectSettings.getActiveProjects());
		    		
					variantService.suggestVariantLocationValues(property, suggestionContext, new AsyncCallback<Collection<PropertyValue>>() {
					    @Override
					    public void onFailure(Throwable caught) {
					    }

					    @Override
					    public void onSuccess(Collection<PropertyValue> propertyValues) {
					    	List<GwtSerializable> suggestions = new ArrayList<GwtSerializable>();
					    	
					    	for (PropertyValue propertyValue : propertyValues) {
					    		suggestions.add(propertyValue.getValue());
					    	}
					    	
							callback.onSuccess(new PagingLoadResultBean<GwtSerializable>(new ArrayList<GwtSerializable>(suggestions), 0, 0));
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
                        
                        variantService.suggestVariantLocationValues(property, suggestionContext, new AsyncCallback<Collection<PropertyValue>>() {
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
