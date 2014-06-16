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
package ubc.pavlab.aspiredb.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.directwebremoting.annotations.DataTransferObject;

import ubc.pavlab.aspiredb.shared.query.CharacteristicProperty;
import ubc.pavlab.aspiredb.shared.query.Property;
import ubc.pavlab.aspiredb.shared.query.VariantTypeProperty;

@DataTransferObject(javascript = "VariantValueObject")
public class VariantValueObject implements Serializable {

    private static final long serialVersionUID = -5007872164222760764L;

    protected Long id;
    protected String variantType;

    protected Long subjectId;
    protected String details = "";
    protected SubjectValueObject subject;

    protected String patientId;
    protected Map<String, CharacteristicValueObject> characteristics;

    protected Collection<LabelValueObject> labels = new ArrayList<LabelValueObject>();

    protected GenomicRange genomicRange;

    protected String userVariantId;

    protected String description;

    protected String externalId;

    public VariantValueObject() {
        super();
    }

    public Map<String, CharacteristicValueObject> getCharacteristics() {
        return characteristics;
    }

    public String getDescription() {
        return description;
    }

    public String getDetails() {
        return details;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getGenomeCoordinates() {
        return this.genomicRange.toBaseString();
    }

    public GenomicRange getGenomicRange() {
        return genomicRange;
    }

    public Long getId() {
        return id;
    }

    public Collection<LabelValueObject> getLabels() {
        return labels;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getPropertyStringValue( Property property ) {
        if ( property instanceof VariantTypeProperty ) {
            return this.variantType;
        } else if ( property instanceof CharacteristicProperty ) {
            final CharacteristicValueObject characteristicValueObject = this.getCharacteristics().get(
                    property.getName() );
            if ( characteristicValueObject == null ) {
                return null;
            }
            return characteristicValueObject.getValue();
        }
        return null;
    }

    public SubjectValueObject getSubject() {
        return subject;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public String getUserVariantId() {
        return userVariantId;
    }

    public String getVariantType() {
        return variantType;
    }

    public void setCharacteristics( Map<String, CharacteristicValueObject> characteristics ) {
        this.characteristics = characteristics;
    }

    public void setDescription( String description ) {
        this.description = description;
    }

    public void setDetails( String details ) {
        this.details = details;
    }

    public void setExternalId( String externalId ) {
        this.externalId = externalId;
    }

    public void setGenomicRange( GenomicRange genomicRange ) {
        this.genomicRange = genomicRange;
    }

    public void setId( Long id ) {
        this.id = id;
    }

    public void setLabels( Collection<LabelValueObject> labels ) {
        this.labels = labels;
    }

    public void setPatientId( String id ) {
        this.patientId = id;
    }

    public void setSubject( SubjectValueObject subject ) {
        this.subject = subject;
    }

    public void setSubjectId( Long subjectId ) {
        this.subjectId = subjectId;
    }

    public void setUserVariantId( String userVariantId ) {
        this.userVariantId = userVariantId;
    }

    public void setVariantType( String variantType ) {
        this.variantType = variantType;
    }
}