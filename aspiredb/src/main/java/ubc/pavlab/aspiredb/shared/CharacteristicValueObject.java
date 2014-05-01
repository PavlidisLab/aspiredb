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
package ubc.pavlab.aspiredb.shared;

import org.directwebremoting.annotations.DataTransferObject;

/**
 * author: anton date: 11/04/13
 */
@DataTransferObject(javascript = "CharacteristicValueObject")
public class CharacteristicValueObject implements Displayable {

    private Long id;
    private String key;
    private String value;

    public CharacteristicValueObject( Long id, String key, String value ) {
        this.id = id;
        this.key = key;
        this.value = value;
    }

    public CharacteristicValueObject() {
    }

    public Long getId() {
        return id;
    }

    public void setId( Long id ) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey( String key ) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue( String value ) {
        this.value = value;
    }

    @Override
    public String toString() {
        return key + "=" + value;
    }

    @Override
    public String getLabel() {
        return value;
    }

    @Override
    public String getHtmlLabel() {
        return value;
    }

    @Override
    public String getTooltip() {
        return value;
    }
}
