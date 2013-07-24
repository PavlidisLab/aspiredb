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

import java.util.HashMap;
import java.util.Map;

/**
 * @author ?
 * 
 */
public class PhenotypeValueObject implements Comparable<PhenotypeValueObject>, GwtSerializable {

    private static final long serialVersionUID = 1418662231622360485L;
    private Long id;
    private String uri;
    private String name;

    private String dbValue;
    private String inferredValue;

    private String valueType;
    private Long subjectId;
    private String externalSubjectId;

    private boolean isOntologyTerm;

    private Map<String, PhenotypeValueObject> descendantPhenotypes = new HashMap<String, PhenotypeValueObject>();

    public PhenotypeValueObject() {
    }

    public PhenotypeValueObject(PhenotypeValueObject phenotype, String uri, String name, String inferredValue) {
        this.subjectId = phenotype.getSubjectId();
        this.inferredValue = inferredValue;
        this.name = name;
        this.uri = uri;
    }

    public PhenotypeValueObject(Long subjectId, String uri, String name, String inferredValue) {
        this.subjectId = subjectId;
        this.inferredValue = inferredValue;
        this.name = name;
        this.uri = uri;
    }

    public PhenotypeValueObject(Long subjectId, String uri, String name) {
        this.subjectId = subjectId;
        this.name = name;
        this.uri = uri;
    }

    public Map<String, PhenotypeValueObject> getDescendantPhenotypes() {
        return descendantPhenotypes;
    }

    public boolean isOntologyTerm() {
        return isOntologyTerm;
    }

    public void setOntologyTerm(boolean ontologyTerm) {
        isOntologyTerm = ontologyTerm;
    }

    public void setDescendantPhenotypes(Map<String, PhenotypeValueObject> descendantPhenotypes) {
        this.descendantPhenotypes = descendantPhenotypes;
    }

    public String getInferredValue() {
        return inferredValue;
    }

    public void setInferredValue(String inferredValue) {
        this.inferredValue = inferredValue;
    }

    public void setId( Long id ) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public String getUri() {
        return uri;
    }

    public void setUri( String uri ) {
        this.uri = uri;
    }

    public String getDbValue() {
        return dbValue;
    }

    public void setDbValue(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType( String valueType ) {
        this.valueType = valueType;
    }

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    @Override
    public boolean equals (Object o) {
    	return (o != null && ((PhenotypeValueObject) o).getName().equals(this.getName()));
    }

    public String getExternalSubjectId() {
        return externalSubjectId;
    }

    public void setExternalSubjectId(String externalSubjectId) {
        this.externalSubjectId = externalSubjectId;
    }

    @Override
    public int hashCode() {
    	return this.getName().hashCode();
    }

	@Override
	public int compareTo(PhenotypeValueObject o) {
		return this.getName().compareTo(o.getName());
	}

    public void addChild(PhenotypeValueObject child) {
        this.getDescendantPhenotypes().put(child.getName(), child);
    }

    public void addChildIfAbsent(PhenotypeValueObject child) {
        if (!this.getDescendantPhenotypes().containsKey(child.getName())) {
            addChild( child );
        }
    }
}
