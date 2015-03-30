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
import gemma.gsec.model.SecuredChild;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.BatchSize;

import ubc.pavlab.aspiredb.shared.PhenotypeValueObject;

/**
 * TODO Document Me
 * 
 * @author ??
 * @version $Id: Phenotype.java,v 1.15 2013/06/11 22:55:58 anton Exp $
 */
@Entity
@BatchSize(size = 50)
@Table(name = "PHENOTYPE")
public class Phenotype implements SecuredChild {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;

    @Column(name = "URI")
    private String uri;

    @Column(name = "VALUE")
    private String value;

    @Column(name = "TYPE")
    private String type;

    @Column(name = "VALUE_TYPE")
    private String valueType;

    @Column(name = "NAME")
    private String name;

    @ManyToOne
    @JoinColumn(name = "SUBJECT_FK")
    private Subject subject;

    public Phenotype() {
    }

    public PhenotypeValueObject convertToValueObject() {
        PhenotypeValueObject valueObject = new PhenotypeValueObject();
        valueObject.setId( this.getId() );
        valueObject.setSubjectId( subject.getId() );
        valueObject.setExternalSubjectId( subject.getPatientId() );
        valueObject.setUri( this.getUri() );
        valueObject.setName( this.getName() );
        valueObject.setValueType( this.getValueType() );
        valueObject.setDbValue( this.getValue() );
        return valueObject;
    }

    @Override
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Subject getSubject() {
        return subject;
    }

    public String getType() {
        return type;
    }

    public String getUri() {
        return uri;
    }

    public String getValue() {
        return value;
    }

    public String getValueType() {
        return valueType;
    }

    public void setId( Long id ) {
        this.id = id;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public void setSubject( Subject subject ) {
        this.subject = subject;
    }

    public void setType( String type ) {
        this.type = type;
    }

    public void setUri( String uri ) {
        this.uri = uri;
    }

    public void setValue( String value ) {
        this.value = value;
    }

    public void setValueType( String valueType ) {
        this.valueType = valueType;
    }

    @Override
    public Securable getSecurityOwner() {
        return null;
    }
}
