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

import gemma.gsec.model.Securable;
import gemma.gsec.model.SecuredNotChild;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import ubc.pavlab.aspiredb.server.ValueObjectConvertible;
import ubc.pavlab.aspiredb.shared.VariantValueObject;

/**
 * Variant class TODO add platform
 * 
 * @author ?
 * @version $Id: Variant.java,v 1.19 2013/06/11 22:55:59 anton Exp $
 */
@Entity
@Table(name = "VARIANT")
@BatchSize(size = 10)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "discriminator", discriminatorType = DiscriminatorType.STRING)
public abstract class Variant implements SecuredNotChild, ValueObjectConvertible<VariantValueObject> {

    @Transient
    Securable securityOwner;

    @OneToMany(cascade = javax.persistence.CascadeType.ALL, orphanRemoval = true)
    @JoinTable(name = "VARIANT_CHARACTERISTIC", joinColumns = { @JoinColumn(name = "VARIANT_FK") }, inverseJoinColumns = { @JoinColumn(name = "CHARACTERISTIC_FK") })
    protected List<Characteristic> characteristics;

    @ManyToMany
    @JoinTable(name = "VARIANT_LABEL", joinColumns = { @JoinColumn(name = "VARIANT_FK", referencedColumnName = "ID") }, inverseJoinColumns = { @JoinColumn(name = "LABEL_FK", referencedColumnName = "ID") })
    protected Set<Label> labels = new HashSet<Label>();

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "EXTERNALID")
    private String externalId;

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @Cascade(CascadeType.SAVE_UPDATE)
    @JoinColumn(name = "GENOMELOC_ID")
    private GenomicLocation location;

    @ManyToOne
    @JoinColumn(name = "PATIENT_ID")
    private Subject subject;

    @Column(name = "USERVARIANTID")
    private String userVariantId;

    // TODO: has to be unique
    public void addLabel( Label label ) {
        this.labels.add( label );
    }

    @Override
    public boolean equals( Object obj ) {
        if ( this == obj ) {
            return true;
        }
        if ( obj == null ) {
            return false;
        }
        if ( getClass() != obj.getClass() ) {
            return false;
        }
        Variant other = ( Variant ) obj;
        if ( id == null ) {
            if ( other.id != null ) {
                return false;
            }
        } else if ( !id.equals( other.id ) ) {
            return false;
        }
        return true;
    }

    public List<Characteristic> getCharacteristics() {
        return characteristics;
    }

    public String getDescription() {
        return description;
    }

    public String getExternalId() {
        return externalId;
    }

    @Override
    public Long getId() {
        return id;
    }

    public Collection<Label> getLabels() {
        return labels;
    }

    public GenomicLocation getLocation() {
        return location;
    }

    public Subject getSubject() {
        return subject;
    }

    public String getUserVariantId() {
        return userVariantId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( id == null ) ? 0 : id.hashCode() );
        return result;
    }

    public void removeLabel( Label label ) {
        this.labels.remove( label );
    }

    public void setCharacteristics( List<Characteristic> characteristics ) {
        this.characteristics = characteristics;
    }

    public void setDescription( String description ) {
        this.description = description;
    }

    public void setExternalId( String externalId ) {
        this.externalId = externalId;
    }

    public void setId( Long id ) {
        this.id = id;
    }

    public void setLabels( Set<Label> labels ) {
        this.labels = labels;
    }

    public void setLocation( GenomicLocation location ) {
        this.location = location;
    }

    public void setSubject( Subject subject ) {
        this.subject = subject;
    }

    public void setUserVariantId( String userVariantId ) {
        this.userVariantId = userVariantId;
    }

    @Override
    public String toString() {
        return "Variant [id=" + id + ", subject=" + subject + ", location=" + location + "]";
    }

    // @Override
    // public Securable getSecurityOwner() {
    // return securityOwner;
    // }

    public void setSecurityOwner( Securable securityOwner ) {
        this.securityOwner = securityOwner;
    }
}
