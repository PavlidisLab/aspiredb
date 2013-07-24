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

import ubc.pavlab.aspiredb.shared.GwtSerializable;

import java.io.Serializable;

/**
 * author: anton
 * date: 13/05/13
 */
public class PropertyValue<T extends GwtSerializable> implements Serializable {
    private static final long serialVersionUID = 6092519749465884432L;

    private T value;

    public PropertyValue() {}

    public PropertyValue(T value) {
        this.value = value;
    }

    public String toString() {
        return  value.toString();
    }

    public T getValue() {
        return this.value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
