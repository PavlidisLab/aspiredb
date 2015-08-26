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

import java.util.Collection;

/**
 * DAO operations for creating, updating, loading, and removing entities from the database.
 * 
 * @author ??
 * @version $Id: DaoBase.java,v 1.5 2012/09/13 22:30:49 cmcdonald Exp $
 * @param <T>
 */
public interface DaoBase<T> {

    public Collection<? extends T> create( Collection<? extends T> entities );

    public T create( T entity );

    public long getCountAll();

    public Collection<? extends T> load( Collection<Long> ids );

    public T load( Long id );

    public Collection<? extends T> loadAll();

    public void remove( Collection<? extends T> entities );

    public void remove( Long id );

    public void remove( T entity );

    public void update( Collection<? extends T> entities );

    public void update( T entity );

}
