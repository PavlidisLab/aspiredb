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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.BatchSize;
import org.springframework.security.access.annotation.Secured;

import ubc.pavlab.aspiredb.server.util.PhenotypeUtil;
import ubc.pavlab.aspiredb.shared.PhenotypeValueObject;
import ubc.pavlab.aspiredb.shared.SubjectValueObject;

@Entity
@BatchSize(size = 50)
@Table(name = "SUBJECT")
public class Subject implements Serializable, SecuredNotChild {

    private static final long serialVersionUID = -7549951725408353980L;

    public static SubjectValueObject convertToValueObject( Subject subject ) {
        SubjectValueObject valueObject = new SubjectValueObject();
        valueObject.setId( subject.getId() );
        valueObject.setPatientId( subject.getPatientId() );
        valueObject.setLabels( Label.toValueObjects( subject.getLabels() ) );
        return valueObject;
    }

    public static Collection<Subject> emptyCollection() {
        return new ArrayList<Subject>();
    }

    @Transient
    Securable securityOwner;

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;

    @Column(name = "PATIENT_ID")
    private String patientId;

    @ManyToMany
    @JoinTable(name = "SUBJECT_PROJECTS", joinColumns = { @JoinColumn(name = "SUBJECT_ID", referencedColumnName = "ID") }, inverseJoinColumns = { @JoinColumn(name = "PROJECT_ID", referencedColumnName = "ID") })
    private List<Project> projects = new ArrayList<Project>();

    @OneToMany(cascade = { CascadeType.ALL }, mappedBy = "subject", orphanRemoval = true)
    private Collection<Phenotype> phenotypes = new HashSet<Phenotype>();

    @OneToMany(cascade = { CascadeType.ALL }, orphanRemoval = true)
    @JoinTable(name = "SUBJECT_VARIANT", joinColumns = { @JoinColumn(name = "SUBJECT_ID", referencedColumnName = "ID") }, inverseJoinColumns = { @JoinColumn(name = "VARIANT_ID", referencedColumnName = "ID") })
    private List<Variant> variants;

    @ManyToMany
    @JoinTable(name = "SUBJECT_LABEL", joinColumns = { @JoinColumn(name = "SUBJECT_FK", referencedColumnName = "ID") }, inverseJoinColumns = { @JoinColumn(name = "LABEL_FK", referencedColumnName = "ID") })
    private Collection<Label> labels = new HashSet<Label>();

    public Subject() {
        variants = new ArrayList<Variant>();
    }

    public void addLabel( Label label ) {
        this.labels.add( label );
    }

    public void addPhenotype( Phenotype p ) {
        p.setSubject( this );
        phenotypes.add( p );
    }

    public void addVariant( Variant variant ) {
        variant.setSubject( this );
        variants.add( variant );
    }

    public SubjectValueObject convertToValueObject() {
        SubjectValueObject valueObject = new SubjectValueObject();
        valueObject.setId( this.getId() );
        valueObject.setPatientId( this.getPatientId() );
        valueObject.setLabels( Label.toValueObjects( this.getLabels() ) );

        // Map<String, PhenotypeValueObject> map = new HashMap<String, PhenotypeValueObject>();
        //
        // for (Phenotype phenotype : phenotypes) {
        // PhenotypeValueObject phenotypeValueObject = phenotype.convertToValueObject();
        // map.put(phenotypeValueObject.getName(), phenotypeValueObject);
        // }
        //
        // valueObject.setPhenotypes( map );
        return valueObject;
    }

    public SubjectValueObject convertToValueObjectWithPhenotypes() {
        SubjectValueObject valueObject = new SubjectValueObject();
        valueObject.setId( this.getId() );
        valueObject.setPatientId( this.getPatientId() );
        valueObject.setLabels( Label.toValueObjects( this.getLabels() ) );

        Map<String, PhenotypeValueObject> map = new HashMap<String, PhenotypeValueObject>();
        for ( Phenotype phenotype : phenotypes ) {
            PhenotypeValueObject phenotypeValueObject = phenotype.convertToValueObject();
            map.put( phenotypeValueObject.getName(), phenotypeValueObject );
        }

        valueObject.setPhenotypes( map );
        return valueObject;
    }

    @Override
    public boolean equals( Object object ) {

        if ( this == object ) {
            return true;
        }
        if ( !( object instanceof Subject ) ) {
            return false;
        }
        final Subject that = ( Subject ) object;
        if ( this.id == null || that.getId() == null || !this.id.equals( that.getId() ) ) {
            return false;
        }
        return true;

    }

    @Override
    public Long getId() {
        return id;
    }

    @Secured({ "GROUP_USER", "AFTER_ACL_READ" })
    public Collection<Label> getLabels() {
        return labels;
    }

    public String getPatientId() {
        return patientId;
    }

    public Collection<Phenotype> getPhenotypes() {
        return phenotypes;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public List<Variant> getVariants() {
        return variants;
    }

    @Override
    public int hashCode() {
        int hashCode = 0;
        hashCode = 29 * hashCode + ( id == null ? 0 : id.hashCode() );

        return hashCode;
    }

    public boolean hasPhenotype( String phenotype ) {

        boolean isUri = PhenotypeUtil.isUri( phenotype );

        for ( Phenotype p : phenotypes ) {

            // uri takes precedence
            if ( isUri ) {
                if ( phenotype.trim().equalsIgnoreCase( p.getUri() ) ) {
                    return true;
                }
            } else {
                if ( phenotype.trim().equalsIgnoreCase( p.getName().trim() ) ) {
                    return true;
                }
            }

        }

        return false;
    }

    public void removeLabel( Label label ) {
        this.labels.remove( label );
    }

    public void setId( Long id ) {
        this.id = id;
    }

    public void setLabels( Collection<Label> labels ) {
        this.labels = labels;
    }

    public void setPatientId( String patientId ) {
        this.patientId = patientId;
    }

    public void setPhenotypes( Collection<Phenotype> phenotypes ) {
        this.phenotypes = phenotypes;
    }

    public void setProjects( List<Project> projects ) {
        this.projects = projects;
    }

    public void setVariants( List<Variant> variants ) {
        this.variants = variants;
    }

    @Override
    public String toString() {
        return "id=" + id + " patientId=" + patientId;
    }

    // @Override
    // public Securable getSecurityOwner() {
    // return securityOwner;
    // }

    public void setSecurityOwner( Securable securityOwner ) {
        this.securityOwner = securityOwner;
    }
}
