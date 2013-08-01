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
package ubc.pavlab.aspiredb.client.service;


import com.google.gwt.user.client.rpc.AsyncCallback;
import ubc.pavlab.aspiredb.shared.ProjectValueObjectOld;
import ubc.pavlab.aspiredb.shared.VariantType;

import java.util.List;

/**
 * TODO Document Me
 * 
 * @author ??
 * @version $Id: ProjectServiceAsync.java,v 1.5 2013/06/11 22:30:37 anton Exp $
 */
public interface ProjectServiceAsync {

    public void getProjects( AsyncCallback<List<ProjectValueObjectOld>> callback );
    
    public void numSubjects( Long projectId, AsyncCallback<Integer> callback );
    
    public void numVariants( Long projectId, AsyncCallback<Integer> callback );
    
    public void processUploadedFile(String projectName, String filename, VariantType v, AsyncCallback<String> callback );
    
    public void processUploadedPhenotypeFile(String projectName, String filename, AsyncCallback<String> callback) ;
    
    public void deleteProject(String projectName, AsyncCallback<String> callback);
    
    public void alterGroupPermissions(String projectName, String groupName, Boolean grant, AsyncCallback<String> callback);
    
    public void createUserAndAssignToGroup(String userName, String password, String groupName, AsyncCallback<String> callback);        
    
}
