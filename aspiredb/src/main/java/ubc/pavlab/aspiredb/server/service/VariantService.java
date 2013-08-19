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
package ubc.pavlab.aspiredb.server.service;

import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import ubc.pavlab.aspiredb.server.exceptions.BioMartServiceException;
import ubc.pavlab.aspiredb.server.exceptions.NeurocartaServiceException;
import ubc.pavlab.aspiredb.server.exceptions.NotLoggedInException;
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
 * We are using spring4gwt to call spring beans from client code. One thing that's a bit ugly is that services exposed
 * in this way have to implement gwt's RemoteService (it's just a marking interface), in other words GWT and Server side
 * are not fully isolated. That's not a big deal, it's just not ideal.
 * 
 * @author azoubare
 * @version $Id: VariantService.java,v 1.19 2013/06/13 19:57:16 anton Exp $
 */
public interface VariantService {

    public VariantValueObject getVariant(Long variantId)
            throws NotLoggedInException;

    public Collection<Property> suggestVariantLocationProperties()
            throws NotLoggedInException;

    public Collection<PropertyValue> suggestVariantLocationValues(Property property, SuggestionContext suggestionContext)
            throws NotLoggedInException, BioMartServiceException, NeurocartaServiceException, BioMartServiceException, NeurocartaServiceException;

    public Collection<Property> suggestProperties(VariantType variantType)
            throws NotLoggedInException;

    public Collection<Property> suggestProperties()
            throws NotLoggedInException, NotLoggedInException;

    public Collection<PropertyValue> suggestValues(Property property, SuggestionContext suggestionContext)
            throws NotLoggedInException;

    @Deprecated
    public Collection<String> suggestCharacteristicPropertyValues(CharacteristicProperty property)
            throws NotLoggedInException;

    @Deprecated
    public List<LabelValueObject> suggestLabels(SuggestionContext suggestionContext)
            throws NotLoggedInException;

    public LabelValueObject addLabel(Long variantId, LabelValueObject label)
            throws NotLoggedInException;

    public LabelValueObject addLabel(Collection<Long> variantIds, LabelValueObject label)
            throws NotLoggedInException;

    public void removeLabel(Long variantId, LabelValueObject label)
            throws NotLoggedInException;

    public void removeLabel(Collection<Long> variantIds, LabelValueObject label)
            throws NotLoggedInException;

    //    public PagingLoadResult<VariantValueObject> getVariants( AspireDbPagingLoadConfig config )
    //    throws NotLoggedInException, ExternalDependencyException;

}
