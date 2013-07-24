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
package ubc.pavlab.aspiredb.server.biomartquery;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * TODO Document Me
 * 
 * @author ??
 * @version $Id: Query.java,v 1.4 2013/02/05 23:39:41 frances Exp $
 */
@XmlRootElement(name = "Query")
public class Query {

    // name of the client making the call
    @XmlAttribute
    public String client = "ASPIREdb";

    // processor name (e.g. TSV, JSON)
    @XmlAttribute
    public String processor = "TSV";

    // the number of rows to return (-1 for no limit)
    @XmlAttribute
    public String limit = "-1";

    // if set to 1 then first row of results will be column headers
    @XmlAttribute
    public String header = "0";

    @XmlAttribute
    public String uniqueRows = "1";

    @XmlElement
    public Dataset Dataset;

}
