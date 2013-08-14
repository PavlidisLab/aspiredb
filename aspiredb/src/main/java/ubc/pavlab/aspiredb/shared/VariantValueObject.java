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

import org.directwebremoting.annotations.DataTransferObject;
import ubc.pavlab.aspiredb.shared.query.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@DataTransferObject
public class VariantValueObject implements Serializable {

	private static final long serialVersionUID = -5007872164222760764L;

	protected Long id;
	protected String variantType;

    protected Long subjectId;
    protected String details="";
	
	protected String patientId;
	protected Map<String, CharacteristicValueObject> characteristics;

    public void setLabels(Collection<LabelValueObject> labels) {
        this.labels = labels;
    }

    protected Collection<LabelValueObject> labels = new ArrayList<LabelValueObject>();

    public Collection<LabelValueObject> getLabels() {
        return labels;
    }

    protected GenomicRange genomicRange;

	protected String userVariantId;
	protected String description;
	protected String externalId;
		
	public VariantValueObject() {
		super();
	}

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public String getVariantType() {
        return variantType;
    }

    public void setVariantType(String variantType) {
        this.variantType = variantType;
    }

	public String getGenomeCoordinates() {
		return this.genomicRange.toBaseString();
	}

    public Map<String,CharacteristicValueObject> getCharacteristics() {
        return characteristics;
    }

    public void setCharacteristics(Map<String,CharacteristicValueObject> characteristics) {
        this.characteristics = characteristics;
    }

	public GenomicRange getGenomicRange() {
		return genomicRange;
	}

	public void setGenomicRange(GenomicRange genomicRange) {
		this.genomicRange = genomicRange;
	}
	
    public Long getId() {
        return id;
    }

    public void setId( Long id ) {
        this.id = id;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId( String id ) {
        this.patientId = id;
    }

    public String getUserVariantId() {
        return userVariantId;
    }

    public void setUserVariantId( String userVariantId ) {
        this.userVariantId = userVariantId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription( String description ) {
        this.description = description;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId( String externalId ) {
        this.externalId = externalId;
    }

    public String getPropertyStringValue(Property property) {
        if (property instanceof VariantTypeProperty) {
            return this.variantType;
        }
        else if (property instanceof CharacteristicProperty) {
            final CharacteristicValueObject characteristicValueObject = this.getCharacteristics().get(property.getName());
            if (characteristicValueObject == null) return null;
            return characteristicValueObject.getValue();
        }
        return null;
    }
}