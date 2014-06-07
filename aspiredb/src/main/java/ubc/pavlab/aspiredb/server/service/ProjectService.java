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
package ubc.pavlab.aspiredb.server.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.Reader;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import ubc.pavlab.aspiredb.server.exceptions.NotLoggedInException;
import ubc.pavlab.aspiredb.server.model.Project;
import ubc.pavlab.aspiredb.server.model.common.auditAndSecurity.User;
import ubc.pavlab.aspiredb.server.model.common.auditAndSecurity.UserGroup;
import ubc.pavlab.aspiredb.shared.ProjectValueObject;
import ubc.pavlab.aspiredb.shared.VariantType;
import ubc.pavlab.aspiredb.shared.VariantValueObject;

/**
 * TODO Document Me
 * 
 * @author ??
 * @version $Id: ProjectService.java,v 1.4 2013/06/11 22:30:38 anton Exp $
 */

public interface ProjectService {

    public List<ProjectValueObject> getProjects() throws NotLoggedInException;
    
    public List<ProjectValueObject> getOverlapProjects( Collection<Long> projectIds );

    public ProjectValueObject getDgvProject();

    public ProjectValueObject getDecipherProject();
    
    public User getCurrentUserName();
    
    public Collection<String> getProjectUserNames(String projectName);
    
    public Collection<User> getProjectUsers( String projectName );
    
    public Collection<String> projectReadableBy( Project project );
    
    public Map<String, String> getProjectUserGroups( String projectName );
    
    public Integer numSubjects( Collection<Long> projectIds ) throws NotLoggedInException;

    public Integer numVariants( Collection<Long> projectIds ) throws NotLoggedInException;

    public String processUploadedFile( String projectName, String filename, VariantType v ) throws NotLoggedInException;

    public String processUploadedPhenotypeFile( String projectName, String filename ) throws NotLoggedInException;

    public String deleteProject( String projectName ) throws NotLoggedInException;

    public String alterGroupPermissions( String projectName, String groupName, Boolean grant )
            throws NotLoggedInException;

    public String createUserAndAssignToGroup( String userName, String password, String groupName )
            throws NotLoggedInException;
    
    public String createUserProject(String projectName, String projectDescription) throws NotLoggedInException;
    
    public ProjectValueObject findUserProject(String projectName) throws NotLoggedInException;

    public String addSubjectVariantsToExistingProject( String fileContent, boolean createProject, String projectName, String variantType );
    
    public String addSubjectPhenotypeToExistingProject( String fileContent, boolean createProject, String projectName, String variantType );
}
