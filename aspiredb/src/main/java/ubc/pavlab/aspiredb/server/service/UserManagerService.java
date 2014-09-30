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

import ubc.pavlab.aspiredb.server.model.common.auditAndSecurity.User;
import ubc.pavlab.aspiredb.server.model.common.auditAndSecurity.UserGroup;
import ubc.pavlab.aspiredb.shared.suggestions.SuggestionContext;

/**
 * @author Gaya
 */
public interface UserManagerService {

    public String createUserGroup( String groupName );

    public Collection<String> loadUserEditableGroups();

    public UserGroup findGroupByName( String name );

    public User getCurrentUser();

    public String getCurrentUsername();

    public List<String> findGroupMemebers( String groupName );

    public String deleteGroup( String groupName );

    public String addUserToGroup( String groupName, String userName );

    public Collection<User> suggestGroupMemebers( SuggestionContext suggestionContext, String groupName );

    public String deleteUserFromGroup( String groupName, String userName );

}