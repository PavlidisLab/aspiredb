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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;

import ubc.pavlab.aspiredb.shared.CNVValueObject;
import ubc.pavlab.aspiredb.shared.CharacteristicValueObject;
import ubc.pavlab.aspiredb.shared.GenomicRange;
import ubc.pavlab.aspiredb.shared.VariantValueObject;

/**
 * @version $Id: CNV.java,v 1.24 2013/06/21 18:35:45 cmcdonald Exp $
 */
@Entity
@DiscriminatorValue("CNV")
public class CNV extends Variant {

    public static CNVValueObject getCNVValueObject( CNV cnv ) {
        CNVValueObject vo = new CNVValueObject();
        vo.setId( cnv.getId() );
        vo.setPatientId( cnv.getSubject().getPatientId() );
        vo.setSubjectId( cnv.getSubject().getId() );
        vo.setType( cnv.getType().toString() );
        vo.setGenomicRange( new GenomicRange( cnv.getLocation().getChromosome(), cnv.getLocation().getStart(), cnv
                .getLocation().getEnd() ) );
        vo.setCnvLength( cnv.getCnvLength() );

        return vo;
    }

    @Column(name = "CNVTYPE")
    @Enumerated(javax.persistence.EnumType.STRING)
    private CnvType type;

    @Column(name = "COPY_NUMBER")
    private Integer copyNumber;

    @Column(name = "CNV_LENGTH")
    private Integer cnvLength;

    @OneToMany(cascade = javax.persistence.CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "CNV_LOC", joinColumns = { @JoinColumn(name = "VARIANT_FK") }, inverseJoinColumns = { @JoinColumn(name = "LOC_FK") })
    private List<GenomicLocation> targetLocations;

    public CNV() {
    }

    public Integer getCnvLength() {
        return cnvLength;
    }

    public Integer getCopyNumber() {
        return copyNumber;
    }

    public List<GenomicLocation> getTargetLocations() {
        return targetLocations;
    }

    public CnvType getType() {
        return type;
    }

    public void setCnvLength( Integer cnvLength ) {
        this.cnvLength = cnvLength;
    }

    public void setCopyNumber( Integer copyNumber ) {
        this.copyNumber = copyNumber;
    }

    public void setTargetLocations( List<GenomicLocation> targetLocations ) {
        this.targetLocations = targetLocations;
    }

    public void setType( CnvType type ) {
        this.type = type;
    }

    @Override
    public VariantValueObject toValueObject() {
        CNVValueObject vo = new CNVValueObject();
        vo.setId( this.getId() );
        vo.setVariantType( this.getClass().getSimpleName() );
        vo.setPatientId( this.getSubject().getPatientId() );
        vo.setSubjectId( this.getSubject().getId() );
        vo.setUserVariantId( this.getUserVariantId() );

        vo.setGenomicRange( new GenomicRange( this.getLocation().getChromosome(), this.getLocation().getStart(), this
                .getLocation().getEnd() ) );

        vo.setType( this.getType().toString() );
        vo.setDetails( "Type: " + vo.getType() );
        Collection<CharacteristicValueObject> characteristicValueObjects = Characteristic.toValueObjects( this
                .getCharacteristics() );
        Map<String, CharacteristicValueObject> map = new HashMap<String, CharacteristicValueObject>();
        for ( CharacteristicValueObject characteristicValueObject : characteristicValueObjects ) {
            map.put( characteristicValueObject.getKey(), characteristicValueObject );
        }
        vo.setCharacteristics( map );
        vo.setLabels( Label.toValueObjects( this.getLabels() ) );

        return vo;
    }
}
