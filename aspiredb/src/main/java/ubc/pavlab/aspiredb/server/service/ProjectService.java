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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import ubc.pavlab.aspiredb.server.exceptions.NotLoggedInException;
import ubc.pavlab.aspiredb.server.fileupload.PhenotypeUploadServiceResult;
import ubc.pavlab.aspiredb.server.fileupload.VariantUploadServiceResult;
import ubc.pavlab.aspiredb.server.model.Project;
import ubc.pavlab.aspiredb.server.model.Subject;
import ubc.pavlab.aspiredb.server.model.common.auditAndSecurity.User;
import ubc.pavlab.aspiredb.server.model.common.auditAndSecurity.UserGroup;
import ubc.pavlab.aspiredb.shared.ProjectValueObject;
import ubc.pavlab.aspiredb.shared.suggestions.SuggestionContext;

/**
 * Services for project related tasks such as adding, deleting projects, altering user permissions, adding subjects,
 * variants and phenotypes.
 * 
 * @version $Id: ProjectService.java,v 1.4 2013/06/11 22:30:38 anton Exp $
 */

public interface ProjectService {

    public List<ProjectValueObject> getProjects() throws NotLoggedInException;

    public List<ProjectValueObject> getOverlapProjects( Collection<Long> projectIds );

    public ProjectValueObject getDgvProject();

    public ProjectValueObject getDecipherProject();

    public User getCurrentUserName();

    public Collection<String> getProjectUserNames( String projectName );

    public Collection<User> getProjectUsers( String projectName );

    public Collection<String> projectReadableBy( Project project );

    public Map<String, String> getProjectUserGroups( String projectName );

    public Integer numSubjects( Collection<Long> projectIds ) throws NotLoggedInException;

    public Integer numVariants( Collection<Long> projectIds ) throws NotLoggedInException;

    public String deleteProject( String projectName ) throws NotLoggedInException;

    public String alterGroupPermissions( String projectName, String groupName, Boolean grant )
            throws NotLoggedInException;

    public String createUserAndAssignToGroup( String userName, String password, String groupName )
            throws NotLoggedInException;

    public String createUserProject( String projectName, String projectDescription ) throws NotLoggedInException;

    public Collection<Subject> getSubjects( String projectName ) throws Exception;

    public UserGroup findGroupByName( String name );

    public Collection<String> suggestUsers( SuggestionContext suggestionContext ) throws NotLoggedInException;

    public void deleteUser( String userName );

    public boolean isUser( String userName ) throws NotLoggedInException;

    public VariantUploadServiceResult addSubjectVariantsToProject( String filename, boolean createProject,
            String projectName, String variantType ) throws Exception;

    public PhenotypeUploadServiceResult addSubjectPhenotypeToProject( String filename, boolean createProject,
            String projectName ) throws Exception;

    public String addSubjectVariantsPhenotypeToProject( String variantFilename, String phenotypeFilename,
            boolean createProject, String projectName, String variantType );

    public String addSubjectVariantsPhenotypeToProject( String variantFilename, String phenotypeFilename,
            boolean createProject, String projectName, String variantType, boolean dryRun );

    public VariantUploadServiceResult addSubjectVariantsToProject( String filepath, boolean createProject,
            String projectName, String variantType, boolean dryRun ) throws Exception;

    public PhenotypeUploadServiceResult addSubjectPhenotypeToProject( String filepath, boolean createProject,
            String projectName, boolean dryRun ) throws Exception;

}