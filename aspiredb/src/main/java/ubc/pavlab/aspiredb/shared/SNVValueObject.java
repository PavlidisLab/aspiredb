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

import ubc.pavlab.aspiredb.shared.query.DbSnpIdProperty;
import ubc.pavlab.aspiredb.shared.query.ObservedBaseProperty;
import ubc.pavlab.aspiredb.shared.query.Property;
import ubc.pavlab.aspiredb.shared.query.ReferenceBaseProperty;

/**
 * 
 * 
 * @version $Id: SNVValueObject.java,v 1.4 2013/07/04 17:26:00 anton Exp $
 */
public class SNVValueObject extends VariantValueObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4244741255893027344L;

	private String referenceBase;
	
    private String observedBase;
    
    private String dbSNPID;

    public SNVValueObject() {
    }

	public String getReferenceBase() {
		return referenceBase;
	}

	public void setReferenceBase(String referenceBase) {
		this.referenceBase = referenceBase;
	}

	public String getObservedBase() {
		return observedBase;
	}

	public void setObservedBase(String observedBase) {
		this.observedBase = observedBase;
	}

	public String getDbSNPID() {
		return dbSNPID;
	}

	public void setDbSNPID(String dbSNPID) {
		this.dbSNPID = dbSNPID;
	}

    @Override
    public String getPropertyStringValue(Property property) {
        if (property instanceof DbSnpIdProperty) {
            return this.getDbSNPID();
        } else if (property instanceof ObservedBaseProperty) {
            return this.getObservedBase();
        } else if (property instanceof ReferenceBaseProperty) {
            return this.getReferenceBase();
        }
        return super.getPropertyStringValue(property);
    }
}