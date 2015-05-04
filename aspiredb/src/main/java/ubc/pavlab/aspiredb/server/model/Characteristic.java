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
import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import ubc.pavlab.aspiredb.shared.CharacteristicValueObject;

/**
 * @author cmcdonald
 * @version $Id: Characteristic.java,v 1.8 2013/06/11 22:55:59 anton Exp $
 */
@Entity
@Table(name = "CHARACTERISTIC")
public class Characteristic {

    public static Collection<CharacteristicValueObject> toValueObjects( Collection<Characteristic> entityCharacteristics ) {
        Collection<CharacteristicValueObject> characteristicValueObjects = new ArrayList<CharacteristicValueObject>();
        for ( Characteristic entityCharacteristic : entityCharacteristics ) {
            characteristicValueObjects.add( entityCharacteristic.toValueObject() );
        }
        return characteristicValueObjects;
    }

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;

    @Column(name = "NAME")
    private String key;

    @Column(name = "VALUE", length = 510)
    private String value;

    public Characteristic() {
    }

    public Characteristic( String k, String v ) {
        this.key = k;
        this.value = v;
    }

    public Long getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public void setId( Long id ) {
        this.id = id;
    }

    public void setKey( String key ) {
        this.key = key;
    }

    public void setValue( String value ) {
        this.value = value;
    }

    public CharacteristicValueObject toValueObject() {
        return new CharacteristicValueObject( id, key, value );
    }
}
