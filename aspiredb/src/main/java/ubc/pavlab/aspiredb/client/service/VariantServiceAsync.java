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
package ubc.pavlab.aspiredb.client.service;

import com.google.gwt.user.client.rpc.AsyncCallback;
import ubc.pavlab.aspiredb.client.exceptions.BioMartServiceException;
import ubc.pavlab.aspiredb.client.exceptions.NeurocartaServiceException;
import ubc.pavlab.aspiredb.shared.LabelValueObject;
import ubc.pavlab.aspiredb.shared.VariantType;
import ubc.pavlab.aspiredb.shared.VariantValueObject;
import ubc.pavlab.aspiredb.shared.query.CharacteristicProperty;
import ubc.pavlab.aspiredb.shared.query.Property;
import ubc.pavlab.aspiredb.shared.query.PropertyValue;
import ubc.pavlab.aspiredb.shared.suggestions.SuggestionContext;

import java.util.Collection;
import java.util.List;

/**
 * TODO Document Me
 * 
 * @author ??
 * @version $Id: VariantServiceAsync.java,v 1.19 2013/06/27 19:49:54 frances Exp $
 */
public interface VariantServiceAsync {

    public void getVariant(Long variantId, AsyncCallback<VariantValueObject> callback);

    void addLabel(Long id, LabelValueObject label, AsyncCallback<LabelValueObject> async);
    void addLabel(Collection<Long> id, LabelValueObject label, AsyncCallback<LabelValueObject> async);
    void suggestLabels(SuggestionContext suggestionContext, AsyncCallback<List<LabelValueObject>> async);

    void suggestCharacteristicPropertyValues(CharacteristicProperty property, AsyncCallback<Collection<String>> async);
    void suggestProperties(VariantType variantType, AsyncCallback<Collection<Property>> async);

    void suggestValues(Property property, SuggestionContext suggestionContext, AsyncCallback<Collection<PropertyValue>> async);

    void suggestVariantLocationProperties(AsyncCallback<Collection<Property>> async)
            ;

    void suggestVariantLocationValues(Property property, SuggestionContext suggestionContext, AsyncCallback<Collection<PropertyValue>> async)
            ;

    void suggestProperties(AsyncCallback<Collection<Property>> async)
            ;

    void removeLabel(Long variantId, LabelValueObject label, AsyncCallback<Void> async)
            ;

    void removeLabel(Collection<Long> variantIds, LabelValueObject label, AsyncCallback<Void> async)
            ;

    //public void getVariants(AspireDbPagingLoadConfig config, AsyncCallback<PagingLoadResult<VariantValueObject>> callback );
}
