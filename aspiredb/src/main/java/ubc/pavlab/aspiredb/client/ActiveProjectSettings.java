
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

package ubc.pavlab.aspiredb.client;


import com.google.gwt.storage.client.Storage;
import com.google.gwt.storage.client.StorageMap;
import com.google.gwt.user.client.Window;

import java.util.ArrayList;
import java.util.Collection;
/**
 * Use HTML5 localStorage to store app settings, currently only active project
 * TODO make this more robust in terms of error handling, unsupported browsers etc.
 * 
 * @author cmcdonald
 * @version $Id: ActiveProjectSettings.java,v 1.5 2013/01/25 02:59:18 anton Exp $
 */
public class ActiveProjectSettings { 
    
    public static String ACTIVE_PROJECTS = "ACTIVE_PROJECTS";
    
    //TODO there was discussion of setting multiple active projects, change this method/add new one for a collection of projects
    //store active projects as a string of comma separated ids or something
    
    //TODO if multiple users are using the same machine these storage params may clash, make the keys user specific
    
    
    
    public static void setActiveProject(String projectId){
        
        Storage storage= Storage.getLocalStorageIfSupported();
        
        if (storage==null){
          //TODO use a cookie or something
            Window.alert( "You browser is incompatible. Please use Internet Explorer 8+, Firefox, Opera, Chrome, or Safari" );
        }else{       
            storage.setItem( ACTIVE_PROJECTS, projectId );
        }
        
    }
    
    public static void setActiveProject(Long projectId){
        
        setActiveProject(projectId.toString());
        
    }
    
    public static Long getActiveProject(){
        
        Storage storage= Storage.getLocalStorageIfSupported();
        
        return Long.parseLong( storage.getItem( ACTIVE_PROJECTS ) );
        
    }
    
    public static boolean hasActiveProject(){
        
        return containsValue(ACTIVE_PROJECTS);
        
        
    }
    
    private static boolean containsValue(String key){
        
        Storage storage = Storage.getLocalStorageIfSupported();
        if (storage != null) {
          StorageMap storageMap = new StorageMap(storage);
          if (storageMap.containsValue(key)){
              return true;
          }
          
        }
        
        return false;       
        
        
    }
    
    public static Collection<Long> getActiveProjects(){
        
        Storage storage= Storage.getLocalStorageIfSupported();
        
        Collection<Long> activeProjects = new ArrayList<Long>();
        
        //TODO storage.getItem( ACTIVE_PROJECTS ) could potentially be a string representation of of a bunch of projectids(e.g. comma separated) handle converting this to a collection
        activeProjects.add( Long.parseLong( storage.getItem( ACTIVE_PROJECTS ) ) );
        
        return activeProjects;
        
    }
    
    
    
    
    
    
}