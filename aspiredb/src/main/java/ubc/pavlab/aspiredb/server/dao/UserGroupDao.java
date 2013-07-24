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

import ubc.pavlab.aspiredb.server.model.common.auditAndSecurity.User;
import ubc.pavlab.aspiredb.server.model.common.auditAndSecurity.UserGroup;

import java.util.Collection;




/**
 * TODO Document Me
 * 
 * @author cmcdonald
 * @version $Id: UserGroupDao.java,v 1.2 2013/06/11 22:30:45 anton Exp $
 */
public interface UserGroupDao extends DaoBase<UserGroup> {

    public void addAuthority( UserGroup group, String authority );

    /**
     * 
     */
    public void addToGroup( UserGroup group, User user );

    /**
     * 
     */
    public UserGroup findByUserGroupName( java.lang.String name );

    public Collection<UserGroup> findGroupsForUser( User user );

    public void removeAuthority( UserGroup group, String authority );

}