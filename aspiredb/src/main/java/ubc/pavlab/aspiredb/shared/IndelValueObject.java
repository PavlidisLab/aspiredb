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

import ubc.pavlab.aspiredb.shared.query.IndelLengthProperty;
import ubc.pavlab.aspiredb.shared.query.Property;

/**
 * @version $Id: IndelValueObject.java,v 1.2 2013/07/04 17:26:00 anton Exp $
 */
@DataTransferObject(javascript = "IndelValueObject")
public class IndelValueObject extends VariantValueObject {

    /**
	 * 
	 */
    private static final long serialVersionUID = 6947488320581484129L;

    private int indelLength;

    public IndelValueObject() {

    }

    public int getIndelLength() {
        return indelLength;
    }

    @Override
    public String getPropertyStringValue( Property property ) {
        if ( property instanceof IndelLengthProperty ) {
            return String.valueOf( this.getIndelLength() );
        }
        return super.getPropertyStringValue( property );
    }

    public void setIndelLength( int length ) {
        this.indelLength = length;
    }
}