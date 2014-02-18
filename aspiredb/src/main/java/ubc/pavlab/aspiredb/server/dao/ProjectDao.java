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

package ubc.pavlab.aspiredb.server.dao;

import org.springframework.security.access.annotation.Secured;
import ubc.pavlab.aspiredb.server.model.Project;
import ubc.pavlab.aspiredb.server.model.Subject;

import java.util.Collection;

/**
 * TODO Document Me
 * 
 * @author cmcdonald
 * @version $Id: ProjectDao.java,v 1.13 2013/06/11 22:30:43 anton Exp $
 */
public interface ProjectDao extends SecurableDaoBase<Project> {
    
    @Secured({"GROUP_USER" ,"AFTER_ACL_READ"})
    public Project findByProjectName( String projectName);    
   
    @Secured({"GROUP_USER"})
    public Integer getVariantCountForProjects(Collection<Long> projectIds);
    
    @Secured({"GROUP_USER"})
    public Integer getSubjectCountForProjects(Collection<Long> projectIds);
    
    @Secured({"GROUP_USER","AFTER_ACL_READ"})
    public Collection<Project> getOverlapProjects(Collection<Long> projectIds);
    
    @Secured({"GROUP_USER"})
    public Collection<Project> getSpecialOverlapProjects();
    
    @Secured({"GROUP_USER"})
    public String getOverlapProjectVariantSupportCharacteristicKey(Long projectId);
    
    
    
    
    

}