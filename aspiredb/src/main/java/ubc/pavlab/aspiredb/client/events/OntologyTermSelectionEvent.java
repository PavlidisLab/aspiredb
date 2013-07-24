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

package ubc.pavlab.aspiredb.client.events;

import com.google.gwt.event.shared.GwtEvent;
import ubc.pavlab.aspiredb.client.handlers.OntologyTermSelectionHandler;
import ubc.pavlab.aspiredb.shared.PhenotypeSummaryValueObject;

/**
 * Event fired when an ontology term is selected in the phenotype tree (also using as an event when a value is clicked in the PhenotypeValueList(should
 * probably put that in a different event))
 * 
 * @author cmcdonald
 * @version $Id: OntologyTermSelectionEvent.java,v 1.5 2013/06/11 22:30:49 anton Exp $
 */
public class OntologyTermSelectionEvent extends GwtEvent<OntologyTermSelectionHandler> {

    public static Type<OntologyTermSelectionHandler> TYPE = new Type<OntologyTermSelectionHandler>();

    private PhenotypeSummaryValueObject phenotypeBrowserValueObject;
    
    private String name;
    
    private String uri;
    
    private String valueToSearch;
    
    public OntologyTermSelectionEvent (PhenotypeSummaryValueObject pvo, String name, String uri, String valueToSearch ) {
        this.setPhenotypeBrowserValueObject( pvo );
        this.setValueToSearch( valueToSearch );
        this.setName( name );
        this.setUri( uri );
    }
        
    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<OntologyTermSelectionHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch( OntologyTermSelectionHandler handler ) {
        handler.onOntologyTermSelection( this );
    }

    public PhenotypeSummaryValueObject getPhenotypeBrowserValueObject() {
        return phenotypeBrowserValueObject;
    }

    public void setPhenotypeBrowserValueObject( PhenotypeSummaryValueObject phenotypeBrowserValueObject ) {
        this.phenotypeBrowserValueObject = phenotypeBrowserValueObject;
    }

    public String getValueToSearch() {
        return valueToSearch;
    }

    public void setValueToSearch( String valueToSearch ) {
        this.valueToSearch = valueToSearch;
    }

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public String getUri() {
        return uri;
    }

    public void setUri( String uri ) {
        this.uri = uri;
    }

    


   


}