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

import ubc.pavlab.aspiredb.shared.query.CNVTypeProperty;
import ubc.pavlab.aspiredb.shared.query.CnvLengthProperty;
import ubc.pavlab.aspiredb.shared.query.CopyNumberProperty;
import ubc.pavlab.aspiredb.shared.query.Property;

/**
 * Representation of a CNV
 * 
 * @version $Id: CNVValueObject.java,v 1.29 2013/07/10 20:16:10 anton Exp $
 */
@DataTransferObject(javascript = "CNVValueObject")
public class CNVValueObject extends VariantValueObject {

    private static final long serialVersionUID = 9123410174130882307L;

    private Integer copyNumber;

    private Integer cnvLength;

    protected String type;

    public String getType() {
        return type;
    }

    public void setType( String type ) {
        this.type = type;
    }

    public CNVValueObject() {
    }

    public Integer getCopyNumber() {
        return copyNumber;
    }

    public void setCopyNumber( Integer copyNumber ) {
        this.copyNumber = copyNumber;
    }

    public Integer getCnvLength() {
        return cnvLength;
    }

    public void setCnvLength( Integer cnvLength ) {
        this.cnvLength = cnvLength;
    }

    @Override
    public String getPropertyStringValue( Property property ) {
        if ( property instanceof CopyNumberProperty ) {
            if ( copyNumber == null ) {
                return null;
            } else {
                return copyNumber.toString();
            }
        } else if ( property instanceof CNVTypeProperty ) {
            return this.getType();
        } else if ( property instanceof CnvLengthProperty ) {
            if ( cnvLength == null ) {
                return null;
            } else {
                return cnvLength.toString();
            }
        }

        return super.getPropertyStringValue( property );
    }

    @Override
    public String toString() {
        return patientId + ": Type: " + type;
    }
}