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

import ubc.pavlab.aspiredb.server.model.common.auditAndSecurity.Securable;
import ubc.pavlab.aspiredb.shared.ProjectValueObject;



import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO Document Me
 * 
 * @author ??
 * @version $Id: Project.java,v 1.10 2013/06/11 22:55:58 anton Exp $
 */
@Entity
@Table(name = "PROJECT")
public class Project implements Securable {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "SUBJECT_PROJECTS", joinColumns = { @JoinColumn(name = "PROJECT_ID", referencedColumnName = "ID") }, inverseJoinColumns = { @JoinColumn(name = "SUBJECT_ID", referencedColumnName = "ID") })
    private List<Subject> subjects = new ArrayList<Subject>();

    @Column(name = "NAME", unique = true)
    private String name;

    @Column(name = "DESCRIPTION")
    private String description;

    public Project() {
    }

    public void setId( Long id ) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription( String description ) {
        this.description = description;
    }

    public List<Subject> getSubjects() {
        return subjects;
    }

    public static ProjectValueObject convertToValueObject( Project project ) {
        ProjectValueObject valueObject = new ProjectValueObject();
        valueObject.setId( project.getId() );
        valueObject.setName( project.getName() );
        valueObject.setDescription( project.getDescription() );
        return valueObject;
    }

}
