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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

/**
 * TODO Document Me. Users should be able to create gene groups and add genes to them. A gene might be in more than one
 * group.
 * 
 * @author ??
 * @version $Id: GeneGroup.java,v 1.4 2013/06/11 22:30:36 anton Exp $
 */
@Entity
@Table(name = "GENE_GROUP")
public class GeneGroup {

    @Id
    @GeneratedValue
    @Column(name = "GENEGROUP_ID")
    private Long id;

    @ManyToMany(mappedBy = "genegroups")
    private List<Gene> genes = new ArrayList<Gene>();

    public GeneGroup() {
    }

    public List<Gene> getGenes() {
        return genes;
    }

    public Long getId() {
        return id;
    }

    public void setGenes( List<Gene> genes ) {
        this.genes = genes;
    }

    public void setId( Long id ) {
        this.id = id;
    }

}
