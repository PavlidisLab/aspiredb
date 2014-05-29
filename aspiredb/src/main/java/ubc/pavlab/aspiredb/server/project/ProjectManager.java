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

package ubc.pavlab.aspiredb.server.project;

import java.util.List;

import org.springframework.security.access.annotation.Secured;

import ubc.pavlab.aspiredb.server.model.Project;
import ubc.pavlab.aspiredb.shared.PhenotypeValueObject;
import ubc.pavlab.aspiredb.shared.VariantValueObject;

/**
 * @author cmcdonald
 */
public interface ProjectManager {

    @Secured("GROUP_ADMIN")
    public void alterGroupWritePermissions( String projectName, String groupName, boolean grant );

    @Secured("GROUP_ADMIN")
    public Project createProject( String projectName , String description) throws Exception;

    @Secured("GROUP_ADMIN")
    public void deleteProject( String projectName ) throws Exception;

    @Secured({ "GROUP_ADMIN" })
    public void addSubjectPhenotypesToProject( String projectName, boolean createproject,
            List<PhenotypeValueObject> voList ) throws Exception;

    @Secured({ "GROUP_ADMIN" })
    public void addSubjectPhenotypesToSpecialProject( String projectName, boolean deleteProject,
            List<PhenotypeValueObject> voList ) throws Exception;

    @Secured({ "GROUP_ADMIN" })
    public void addSubjectVariantsToProject( String projectName, boolean createproject, List<VariantValueObject> voList )
            throws Exception;

    @Secured({ "GROUP_ADMIN" })
    public void addSubjectVariantsToSpecialProject( String projectName, boolean deleteProject,
            List<VariantValueObject> voList, boolean existingProject ) throws Exception;

    @Secured({ "GROUP_ADMIN" })
    public void addSubjectVariantsToProjectForceCreate( String projectName, List<VariantValueObject> voList )
            throws Exception;

    @Secured({ "GROUP_ADMIN" })
    public List<String> getVariantUploadWarnings( String projectName, List<VariantValueObject> valueObjects );

    @Secured({ "GROUP_ADMIN" })
    public String createUserAndAssignToGroup( String userName, String password, String groupName );

    @Secured({ "GROUP_ADMIN" })
    public void populateProjectToProjectOverlap( String projectName, String specialProjectName ) throws Exception;

}