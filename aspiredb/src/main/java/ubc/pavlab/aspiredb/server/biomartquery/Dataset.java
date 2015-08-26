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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * An XML dataset element which contains a list of filters and attributes.
 * 
 * @author ??
 * @version $Id: Dataset.java,v 1.4 2013/06/11 22:30:47 anton Exp $
 */
@XmlRootElement(name = "Dataset")
public class Dataset {

    @XmlAttribute
    public String name;

    // (optional): the config of the mart (if applicable)
    @XmlAttribute
    public String config;

    @XmlElement
    public List<Filter> Filter = new ArrayList<Filter>();

    @XmlElement
    public List<Attribute> Attribute = new ArrayList<Attribute>();

    public Dataset() {
    }

    public Dataset( String name ) {
        this.name = name;
    }

}
