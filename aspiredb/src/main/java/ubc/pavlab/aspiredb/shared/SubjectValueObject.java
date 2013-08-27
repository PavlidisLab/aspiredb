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

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ?
 * @version $Id: SubjectValueObject.java,v 1.4 2013/05/14 20:29:21 anton Exp $
 */
@DataTransferObject(javascript = "SubjectValueObject")
public class SubjectValueObject implements Serializable {

    private static final long serialVersionUID = 6701485127497073745L;

    private Long id;
    private String patientId;
    private Map<String, PhenotypeValueObject> phenotypes;
    private Collection<LabelValueObject> labels;

    public Collection<LabelValueObject> getLabels() {
        return labels;
    }

    public void setLabels(Collection<LabelValueObject> labels) {
        this.labels = labels;
    }

    private Integer variants = 0;

    public SubjectValueObject() {
    	this.phenotypes = new HashMap<String, PhenotypeValueObject>();
    }

    public void setId( Long id ) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setPatientId( String patientId ) {
        this.patientId = patientId;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getGender() {
        return getPhenotypeValue("Gender");
    }

    public void setPhenotypes( Map<String, PhenotypeValueObject> phenotypes ) {
        this.phenotypes = phenotypes;
    }

    public PhenotypeValueObject getPhenotype( String name ) {
    	return this.phenotypes.get(name);
    }
    public String getPhenotypeValue( String name ) {    	
    	PhenotypeValueObject p = this.phenotypes.get(name);
    	if ( p == null ) return null;
    	return p.getDbValue();
    }
    
    public Map<String, PhenotypeValueObject> getPhenotypes() {
        return phenotypes;
    }

    public void setVariants( Integer variants ) {
        this.variants = variants;
    }

    public Integer getVariants() {
        return variants;
    }

    @Override
    public String toString() {
        return phenotypes.toString();
    }

    public Integer getNumPhenotypes() {
        return phenotypes.size();
    }
    
    @Override
    public boolean equals (Object o) {
    	return ((SubjectValueObject) o).getId().equals( this.getId() );
    }

    @Override
    public int hashCode() {
    	return this.getId().hashCode();
    }
    
}
