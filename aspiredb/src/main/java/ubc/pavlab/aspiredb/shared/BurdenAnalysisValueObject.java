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

import org.directwebremoting.annotations.DataTransferObject;

/**
 * Elements are: name, group1, group2, pValue, qValue. e.g. [ { name : 'Group size', group1 : 200, group2 : 400, pValue
 * : 0.004, qValue : 0.403, }, { name : 'Total CNV length', group1 : 200, group2 : 400, pValue : 0.004, qValue : 0.403,
 * }, ... ]
 * 
 * @version $Id: $
 */

@DataTransferObject
public class BurdenAnalysisValueObject implements Serializable {

    private static final long serialVersionUID = 6470399811395300902L;

    private Long id;

    private String name;
    private Double group1;
    private Double group2;
    private Double pValue;
    private Double qValue;

    public BurdenAnalysisValueObject() {
    }

    public BurdenAnalysisValueObject( String name, Double group1, Double group2, Double pValue, Double qValue ) {
        this.name = name;
        this.group1 = group1;
        this.group2 = group2;
        this.pValue = pValue;
        this.qValue = qValue;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public Double getGroup1() {
        return group1;
    }

    public void setGroup1( Double group1 ) {
        this.group1 = group1;
    }

    public Double getGroup2() {
        return group2;
    }

    public void setGroup2( Double group2 ) {
        this.group2 = group2;
    }

    public Double getpValue() {
        return pValue;
    }

    public void setpValue( Double pValue ) {
        this.pValue = pValue;
    }

    public Double getqValue() {
        return qValue;
    }

    public void setqValue( Double qValue ) {
        this.qValue = qValue;
    }

    public void setId( Long id ) {
        this.id = id;
    }

}
