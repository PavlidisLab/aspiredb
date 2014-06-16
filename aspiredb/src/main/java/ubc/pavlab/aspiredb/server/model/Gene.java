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

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * TODO Document Me
 * 
 * @author ??
 * @version $Id: Gene.java,v 1.4 2013/06/11 22:30:36 anton Exp $
 */
@Entity
@Table(name = "GENE")
public class Gene {

    @Id
    @GeneratedValue
    @Column(name = "GENE_ID")
    private Long id;

    @Column(name = "GENE_NAME")
    private String name;

    @OneToOne
    @JoinColumn(name = "GENOMELOC_ID")
    private GenomicLocation location;

    @ManyToMany
    @JoinTable(name = "GENEGROUP_GENES", joinColumns = { @JoinColumn(name = "GENE_ID", referencedColumnName = "GENE_ID") }, inverseJoinColumns = { @JoinColumn(name = "GENEGROUP_ID", referencedColumnName = "GENEGROUP_ID") })
    private List<GeneGroup> genegroups;

    public Gene() {
    }

    public List<GeneGroup> getGeneGroups() {
        return genegroups;
    }

    public Long getId() {
        return id;
    }

    public GenomicLocation getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }

    public void setId( Long id ) {
        this.id = id;
    }

    public void setLocation( GenomicLocation location ) {
        this.location = location;
    }

    public void setName( String name ) {
        this.name = name;
    }

}
