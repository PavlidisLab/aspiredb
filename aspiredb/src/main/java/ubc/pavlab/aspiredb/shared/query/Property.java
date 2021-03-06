/*
 * The aspiredb project
 * 
 * Copyright (c) 2013 University of British Columbia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package ubc.pavlab.aspiredb.shared.query;

import java.io.Serializable;
import java.util.Collection;

import org.directwebremoting.annotations.DataTransferObject;

/**
 * Class is concrete because I couldn't get DWR to see it when it is abstract.
 * 
 * @author: anton date: 07/05/13
 */
@DataTransferObject(javascript = "Property")
public class Property<D extends DataType> implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 4034975395962572782L;
    protected String name;
    protected String displayName;
    protected D dataType;
    protected String exampleValues;
    protected Collection<Operator> operators;
    protected boolean supportsSuggestions = false;
    protected boolean isCharacteristic = false;

    public Property() {
    }

    public D getDataType() {
        return dataType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getExampleValues() {
        return exampleValues;
    }

    public String getName() {
        return name;
    }

    public Collection<Operator> getOperators() {
        return operators;
    }

    public boolean isCharacteristic() {
        return isCharacteristic;
    }

    public boolean isSupportsSuggestions() {
        return supportsSuggestions;
    }

    public void setCharacteristic( boolean isCharacteristic ) {
        this.isCharacteristic = isCharacteristic;
    }

    public void setDataType( D dataType ) {
        this.dataType = dataType;
    }

    public void setDisplayName( String displayName ) {
        this.displayName = displayName;
    }

    public void setExampleValues( String exampleValues ) {
        this.exampleValues = exampleValues;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public void setOperators( Collection<Operator> operators ) {
        this.operators = operators;
    }

    public void setSupportsSuggestions( boolean supportsSuggestions ) {
        this.supportsSuggestions = supportsSuggestions;
    }
}
