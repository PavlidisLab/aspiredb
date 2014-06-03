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

import gemma.gsec.model.Securable;

import java.util.Collection;

import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;

/**
 * TODO Document Me
 * 
 * @author ??
 * @version $Id: SecurableDaoBase.java,v 1.5 2013/06/11 22:30:45 anton Exp $
 */
public interface SecurableDaoBase<T extends Securable> {

    @Secured({ "GROUP_USER" })
    public Collection<T> create( Collection<T> entities );

    @Secured({ "GROUP_USER" })
    public T create( T entity );

    @Secured({ "GROUP_USER", "AFTER_ACL_COLLECTION_READ" })
    public Collection<T> load( Collection<Long> ids );

    @Secured({ "GROUP_USER", "AFTER_ACL_READ" })
    public T load( Long id );

    @Secured({ "GROUP_USER", "AFTER_ACL_COLLECTION_READ" })
    public Collection<T> loadAll();

    @Secured({ "GROUP_USER", "ACL_SECURABLE_EDIT" })
    public void remove( Collection<T> entities );

    @Secured({ "GROUP_USER", "ACL_SECURABLE_EDIT" })
    public void remove( T entity );

    @Secured({ "GROUP_USER", "ACL_SECURABLE_COLLECTION_EDIT" })
    public void update( Collection<T> entities );

    @Secured({ "GROUP_USER", "ACL_SECURABLE_EDIT" })
    public void update( T entity );

    @Transactional(readOnly = true)
    long getCountAll();
}
