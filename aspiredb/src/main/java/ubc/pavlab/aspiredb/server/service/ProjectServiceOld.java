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

import ubc.pavlab.aspiredb.server.exceptions.NotLoggedInException;
import ubc.pavlab.aspiredb.shared.ProjectValueObjectOld;
import ubc.pavlab.aspiredb.shared.VariantType;

import java.util.List;

/**
 * TODO Document Me
 * 
 * @author ??
 * @version $Id: ProjectService.java,v 1.4 2013/06/11 22:30:38 anton Exp $
 */

public interface ProjectServiceOld  {        
    
    public List<ProjectValueObjectOld> getProjects() throws NotLoggedInException;
    
    public Integer numSubjects(Long projectId) throws NotLoggedInException;
    
    public Integer numVariants(Long projectId) throws NotLoggedInException;
    
    public String processUploadedFile(String projectName, String filename, VariantType v) throws NotLoggedInException;
    
    public String processUploadedPhenotypeFile(String projectName, String filename) throws NotLoggedInException;
    
    public String deleteProject(String projectName) throws NotLoggedInException;
    
    public String alterGroupPermissions(String projectName, String groupName, Boolean grant) throws NotLoggedInException;
    
    public String createUserAndAssignToGroup(String userName, String password, String groupName) throws NotLoggedInException;
}
