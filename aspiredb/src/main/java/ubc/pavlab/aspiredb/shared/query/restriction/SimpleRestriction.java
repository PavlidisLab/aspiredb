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
package ubc.pavlab.aspiredb.shared.query.restriction;

import ubc.pavlab.aspiredb.shared.GwtSerializable;
import ubc.pavlab.aspiredb.shared.query.Operator;
import ubc.pavlab.aspiredb.shared.query.Property;

import java.io.Serializable;

/**
 * author: anton
 * date: 07/05/13
 */
public class SimpleRestriction implements RestrictionExpression {
    protected Property property;
    protected Operator operator;
    protected Serializable value;

    public SimpleRestriction() {
    }

    public SimpleRestriction(Property property, Operator operator, Serializable value) {
        this.property = property;
        this.operator = operator;
        this.value = value;
    }

    public Serializable getValue() {
        return value;
    }

    public Property getProperty() {
        return property;
    }

    public Operator getOperator() {
        return operator;
    }
}
