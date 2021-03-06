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
package ubc.pavlab.aspiredb.server.model;

import org.directwebremoting.annotations.DataTransferObject;

/**
 * TODO Document Me. This might need to be changed to VariantType and include CNVs, SNVs etc.
 * 
 * @author ??
 * @version $Id: CnvType.java,v 1.6 2013/06/11 22:55:58 anton Exp $
 */
@DataTransferObject(type = "enum")
public enum CnvType {
    LOSS, GAIN, GAINLOSS, UNKNOWN
}
