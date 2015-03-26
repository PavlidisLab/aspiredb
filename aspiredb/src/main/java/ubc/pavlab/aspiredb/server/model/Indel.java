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

import ubc.pavlab.aspiredb.shared.CharacteristicValueObject;
import ubc.pavlab.aspiredb.shared.GenomicRange;
import ubc.pavlab.aspiredb.shared.IndelValueObject;
import ubc.pavlab.aspiredb.shared.VariantValueObject;

/**
 * @version $Id: Indel.java,v 1.6 2013/06/11 22:55:58 anton Exp $
 */
@Entity
@DiscriminatorValue("INDEL")
public class Indel extends Variant {

    @Column(name = "INDEL_LENGTH")
    private Integer indelLength;

    public Indel() {
    }

    public Integer getIndelLength() {
        return indelLength;
    }

    public void setIndelLength( Integer indelLength ) {
        this.indelLength = indelLength;
    }

    @Override
    public VariantValueObject toValueObject() {
        IndelValueObject vo = new IndelValueObject();
        vo.setId( this.getId() );
        vo.setVariantType( this.getClass().getSimpleName() );
        vo.setPatientId( this.getSubject().getPatientId() );
        vo.setSubjectId( this.getSubject().getId() );
        vo.setUserVariantId( this.getUserVariantId() );
        vo.setIndelLength( this.getIndelLength() );

        GenomicRange genomicRange = new GenomicRange( this.getLocation().getChromosome(),
                this.getLocation().getStart(), this.getLocation().getEnd() );
        vo.setGenomicRange( genomicRange );
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
