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

/**
 * 
 * 
 * @version $Id: VariantType.java,v 1.2 2013/05/29 20:02:37 cmcdonald Exp $
 */

@DataTransferObject(type="enum", javascript = "VariantType")
public enum VariantType {
    //DGV and DECIPHER probably shouldn't be here,but using anyway just for the parsers
    CNV, SNV, INDEL, INVERSION, TRANSLOCATION, DECIPHER, DGV;        

    public static VariantType findByName( String name ) {
        
        if (name==null) return null;
        
        for ( VariantType type : VariantType.values() ) {
            if ( name.toLowerCase().equals( type.name().toLowerCase() ) ) return type;
        }

        return null;
    }
}