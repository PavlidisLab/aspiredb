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
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "discriminator", discriminatorType = DiscriminatorType.STRING)
public abstract class Variant implements SecuredNotChild, ValueObjectConvertible<VariantValueObject> {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "PATIENT_ID")
    private Subject subject;

    @OneToOne(fetch = FetchType.EAGER)
    @Cascade(CascadeType.SAVE_UPDATE)
    @JoinColumn(name = "GENOMELOC_ID")
    private GenomicLocation location;

    @OneToMany(cascade = javax.persistence.CascadeType.ALL)
    @JoinTable(name = "VARIANT_CHARACTERISTIC", joinColumns = { @JoinColumn(name = "VARIANT_FK") }, inverseJoinColumns = { @JoinColumn(name = "CHARACTERISTIC_FK") })
    protected List<Characteristic> characteristics;

    @ManyToMany
    @JoinTable(name = "VARIANT_LABEL", joinColumns = { @JoinColumn(name = "VARIANT_FK", referencedColumnName = "ID") }, inverseJoinColumns = { @JoinColumn(name = "LABEL_FK", referencedColumnName = "ID") })
    protected Set<Label> labels = new HashSet<Label>();

    @Column(name = "USERVARIANTID")
    private String userVariantId;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "EXTERNALID")
    private String externalId;

    @Override
    public Long getId() {
        return id;
    }

    public void setSubject( Subject subject ) {
        this.subject = subject;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setId( Long id ) {
        this.id = id;
    }

    public GenomicLocation getLocation() {
        return location;
    }

    public void setLocation( GenomicLocation location ) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription( String description ) {
        this.description = description;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId( String externalId ) {
        this.externalId = externalId;
    }

    public List<Characteristic> getCharacteristics() {
        return characteristics;
    }

    public void setCharacteristics( List<Characteristic> characteristics ) {
        this.characteristics = characteristics;
    }

    public Collection<Label> getLabels() {
        return labels;
    }

    public void setLabels( Set<Label> labels ) {
        this.labels = labels;
    }

    public String getUserVariantId() {
        return userVariantId;
    }

    public void setUserVariantId( String userVariantId ) {
        this.userVariantId = userVariantId;
    }

    // TODO: has to be unique
    public void addLabel( Label label ) {
        this.labels.add( label );
    }

    public void removeLabel( Label label ) {
        this.labels.remove( label );
    }
}
