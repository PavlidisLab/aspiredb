/*
 * The aspiredb project
 * 
 * Copyright (c) 2013 University of British Columbia
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

package ubc.pavlab.aspiredb.server.util;

/**
 * Get statistics about and manage caches.
 * 
 * @author paul
 * @version $Id: CacheMonitor.java,v 1.1 2013/02/04 18:45:06 cmcdonald Exp $
 */
public interface CacheMonitor {

    /**
     * Remove all items from all caches.
     * 
     * @param cacheName
     */
    public abstract void clearAllCaches();

    /**
     * Remove all items from the cache given
     * 
     * @param cacheName
     */
    public abstract void clearCache( String cacheName );

    public void disableStatistics();

    public void enableStatistics();

    /**
     * @return
     */
    public abstract String getStats();

}