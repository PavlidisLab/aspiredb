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

import ubc.pavlab.aspiredb.server.util.GenomeBin;

@Entity
@Table(name = "GENOMIC_LOC")
@org.hibernate.annotations.Table(appliesTo = "GENOMIC_LOC", indexes = {
        @org.hibernate.annotations.Index(name = "index_CHROMOSOME", columnNames = "CHROMOSOME"),
        @org.hibernate.annotations.Index(name = "index_BIN", columnNames = "BIN")

})
public class GenomicLocation {

    @Column(name = "ASSEMBLY_VERSION")
    private String assemblyVersion;

    @Column(name = "BIN")
    private Integer bin;

    @Column(name = "CHR_BAND")
    private String chrBand;

    @Column(name = "CHROMOSOME")
    private String chromosome;

    @Column(name = "END")
    private int end;

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;

    @Column(name = "START")
    private int start;

    @Column(name = "STRAND")
    private String strand;

    public GenomicLocation( String chromosome, Integer start, Integer end ) {
        this();
        assert end >= start;
        this.chromosome = chromosome;
        this.start = start;
        this.end = end;
        this.bin = GenomeBin.binFromRange( start, end );
    }

    private GenomicLocation() {
    }

    public String getAssemblyVersion() {
        return assemblyVersion;
    }

    public Integer getBin() {
        return bin;
    }

    public String getChrBand() {
        return chrBand;
    }

    public String getChromosome() {
        return chromosome;
    }

    public int getEnd() {
        return end;
    }

    public Long getId() {
        return id;
    }

    public int getStart() {
        return start;
    }

    public String getStrand() {
        return strand;
    }

    public void setAssemblyVersion( String assemblyVersion ) {
        this.assemblyVersion = assemblyVersion;
    }

    public void setBin( Integer genomeBin ) {
        this.bin = genomeBin;
    }

    public void setChrBand( String chrBand ) {
        this.chrBand = chrBand;
    }

    public void setChromosome( String chromosome ) {
        this.chromosome = chromosome;
    }

    public void setEnd( int end ) {
        this.end = end;
    }

    public void setId( Long id ) {
        this.id = id;
    }

    public void setStart( int start ) {
        this.start = start;
    }

    public void setStrand( String strand ) {
        this.strand = strand;
    }

    @Override
    public String toString() {
        return "GenomicLocation [chromosome=" + chromosome + ", start=" + start + ", end=" + end + ", bin=" + bin + "]";
    }
}
