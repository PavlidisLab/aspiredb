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

import org.directwebremoting.annotations.DataTransferObject;

import ubc.pavlab.aspiredb.shared.VariantType;

/**
 * @deprecated Use VariantTypeProperty instead. See Bug 4173.
 * @author: anton date: 07/05/13
 */
@Deprecated
@DataTransferObject(javascript = "VariantTypeRestriction")
public class VariantTypeRestriction implements RestrictionExpression {

    private static final long serialVersionUID = 1L;

    private String type;

    public VariantTypeRestriction() {
    }

    public VariantTypeRestriction( VariantType type ) {
        this.type = type.name();
    }

    public String getType() {
        return type;
    }

    public void setType( String type ) {
        this.type = type;
    }
}
