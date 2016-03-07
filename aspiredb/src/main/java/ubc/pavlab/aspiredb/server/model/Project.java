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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import ubc.pavlab.aspiredb.shared.ProjectValueObject;

/**
 * TODO Document Me
 * 
 * @author ??
 * @version $Id: Project.java,v 1.10 2013/06/11 22:55:58 anton Exp $
 */
@Entity
@Table(name = "PROJECT")
public class Project implements SecuredNotChild {

    public static ProjectValueObject convertToValueObject( Project project ) {
        ProjectValueObject valueObject = new ProjectValueObject();
        valueObject.setId( project.getId() );
        valueObject.setName( project.getName() );
        valueObject.setDescription( project.getDescription() );
        valueObject.setSpecial( project.getSpecialData() );
        return valueObject;
    }

    @Column(name = "DESCRIPTION")
    private String description;

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;

    @Column(name = "NAME", unique = true)
    private String name;

    // e.g. DECIPHER or DGV data
    @Column(name = "SPECIAL_DATA")
    private Boolean specialData;

    @OneToMany(cascade = { CascadeType.ALL }, mappedBy = "project", orphanRemoval = true)
    private List<Subject> subjects = new ArrayList<Subject>();

    // currently just for DGV data, the string referring to what characteristic will define the variants 'support', for
    // DGV 'pubmedid'
    @Column(name = "SPECIAL_DATA_SUPPORT_CHARACTERISTIC_KEY")
    private String variantSupportCharacteristicKey;

    public Project() {
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
        Project other = ( Project ) obj;
        if ( id == null ) {
            if ( other.id != null ) {
                return false;
            }
        } else if ( !id.equals( other.id ) ) {
            return false;
        }
        return true;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Boolean getSpecialData() {
        return specialData;
    }

    public List<Subject> getSubjects() {
        return subjects;
    }

    public String getVariantSupportCharacteristicKey() {
        return variantSupportCharacteristicKey;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( id == null ) ? 0 : id.hashCode() );
        return result;
    }

    public void setDescription( String description ) {
        this.description = description;
    }

    public void setId( Long id ) {
        this.id = id;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public void setSpecialData( Boolean specialData ) {
        this.specialData = specialData;
    }

    public void setVariantSupportCharacteristicKey( String variantSupportCharacteristicKey ) {
        this.variantSupportCharacteristicKey = variantSupportCharacteristicKey;
    }

    @Override
    public String toString() {
        return "Project [id=" + id + ", name=" + name + "]";
    }

}
