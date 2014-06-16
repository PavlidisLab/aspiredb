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
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import ubc.pavlab.aspiredb.shared.CharacteristicValueObject;
import ubc.pavlab.aspiredb.shared.GenomicRange;
import ubc.pavlab.aspiredb.shared.VariantValueObject;

/**
 * @version $Id: Translocation.java,v 1.4 2013/06/11 22:30:37 anton Exp $
 */
@Entity
@DiscriminatorValue("TRANSLOCATION")
public class Translocation extends Variant {

    @Column(name = "TRANSLOCATIONTYPE")
    @Enumerated(javax.persistence.EnumType.STRING)
    private TranslocationType type;

    @OneToOne
    @Cascade(CascadeType.SAVE_UPDATE)
    @JoinColumn(name = "TRANS_TARGETLOC_ID")
    private GenomicLocation targetLocation;

    public Translocation() {
    }

    public GenomicLocation getTargetLocation() {
        return targetLocation;
    }

    public TranslocationType getType() {
        return type;
    }

    public void setTargetLocation( GenomicLocation targetLocation ) {
        this.targetLocation = targetLocation;
    }

    public void setType( TranslocationType type ) {
        this.type = type;
    }

    @Override
    public VariantValueObject toValueObject() {
        VariantValueObject vo = new VariantValueObject();
        vo.setId( this.getId() );
        vo.setVariantType( this.getClass().getSimpleName() );
        vo.setPatientId( this.getSubject().getPatientId() );
        vo.setSubjectId( this.getSubject().getId() );
        vo.setUserVariantId( this.getUserVariantId() );

        GenomicRange genomicRange = new GenomicRange( this.getLocation().getChromosome(),
                this.getLocation().getStart(), this.getLocation().getEnd() );
        vo.setGenomicRange( genomicRange );
        Collection<CharacteristicValueObject> characteristicValueObjects = Characteristic
                .toValueObjects( this.characteristics );
        Map<String, CharacteristicValueObject> map = new HashMap<String, CharacteristicValueObject>();
        for ( CharacteristicValueObject characteristicValueObject : characteristicValueObjects ) {
            map.put( characteristicValueObject.getKey(), characteristicValueObject );
        }
        vo.setCharacteristics( map );
        vo.setLabels( Label.toValueObjects( this.labels ) );
        return vo;
    }

}
