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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "GENOMIC_LOC")
@org.hibernate.annotations.Table(appliesTo = "GENOMIC_LOC", indexes = {
        @org.hibernate.annotations.Index(name = "index_START", columnNames = "START"),
        @org.hibernate.annotations.Index(name = "index_END", columnNames = "END")

})
public class GenomicLocation {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;

    @Column(name = "CHROMOSOME")
    private String chromosome;

    @Column(name = "START")
    private int start;

    @Column(name = "END")
    private int end;

    @Column(name = "ASSEMBLY_VERSION")
    private String assemblyVersion;

    @Column(name = "STRAND")
    private String strand;

    @Column(name = "CHR_BAND")
    private String chrBand;

    public GenomicLocation() {
    }

    public Long getId() {
        return id;
    }

    public void setId( Long id ) {
        this.id = id;
    }

    public String getChromosome() {
        return chromosome;
    }

    public void setChromosome( String chromosome ) {
        this.chromosome = chromosome;
    }

    public int getStart() {
        return start;
    }

    public void setStart( int start ) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd( int end ) {
        this.end = end;
    }

    public String getAssemblyVersion() {
        return assemblyVersion;
    }

    public void setAssemblyVersion( String assemblyVersion ) {
        this.assemblyVersion = assemblyVersion;
    }

    public String getStrand() {
        return strand;
    }

    public void setStrand( String strand ) {
        this.strand = strand;
    }

    public String getChrBand() {
        return chrBand;
    }

    public void setChrBand( String chrBand ) {
        this.chrBand = chrBand;
    }
}
