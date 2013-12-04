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
package ubc.pavlab.aspiredb.shared.query.restriction;

import org.directwebremoting.annotations.DataTransferObject;

import ubc.pavlab.aspiredb.server.util.PhenotypeUtil;

@DataTransferObject(javascript = "PhenotypeRestriction")
public class PhenotypeRestriction implements RestrictionExpression {
    private static final long serialVersionUID = -4503829624276270831L;

    private String name;
    private String value;
    private String uri;

    public PhenotypeRestriction() {
    }

    public PhenotypeRestriction( String name, String value ) {
        this.name = name;
        this.value = value;
    }

    public PhenotypeRestriction( String name, String value, String uri ) {
        this.name = name;
        this.value = value;
        this.uri = uri;
    }

    public String getUri() {
        return uri;
    }

    public void setUri( String uri ) {
        this.uri = uri;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public void setValue( String value ) {
        this.value = value;
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }

    public boolean isOntologyTerm() {
        return this.uri != null;
    }

    public boolean isAbsent() {
        return this.value.equals( PhenotypeUtil.VALUE_ABSENT ); // FIXME! We should probably have special phenotype type
                                                                // for absent/present.
    }

    public boolean isPresent() {
        return this.value.equals( PhenotypeUtil.VALUE_PRESENT ); // FIXME!
    }
}
