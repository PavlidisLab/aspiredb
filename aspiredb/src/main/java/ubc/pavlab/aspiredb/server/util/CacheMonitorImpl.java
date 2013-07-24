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

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Statistics;
import net.sf.ehcache.config.CacheConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Get statistics about and manage caches.
 * 
 * @author paul
 * @version $Id: CacheMonitorImpl.java,v 1.3 2013/06/12 17:00:22 anton Exp $
 */
@Component
public class CacheMonitorImpl implements CacheMonitor {

    private static Log log = LogFactory.getLog( CacheMonitorImpl.class );

    @Autowired
    @Qualifier("ehcache")
    private CacheManager cacheManager;

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.util.monitor.CacheMonitor#clearAllCaches()
     */
    @Override
    public void clearAllCaches() {
        log.info( "Clearing all caches" );
        cacheManager.clearAll();
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.util.monitor.CacheMonitor#clearCache(java.lang.String)
     */
    @Override
    public void clearCache( String cacheName ) {
        Cache cache = this.cacheManager.getCache( cacheName );
        if ( cache != null ) {
            cache.removeAll();
            log.info( "Cleared cache: " + cache.getName() );
        } else {
            throw new IllegalArgumentException( "No cache found with name=" + cacheName );
        }
    }

    @Override
    public void disableStatistics() {
        log.info( "Disabling statistics" );
        setStatisticsEnabled( false );
    }

    @Override
    public void enableStatistics() {
        log.info( "Enabling statistics" );
        setStatisticsEnabled( true );

    }

    /*
     * (non-Javadoc)
     * 
     * Doesn't do much yet
     */
    @Override
    public String getStats() {

        StringBuilder buf = new StringBuilder();
        String[] cacheNames = cacheManager.getCacheNames();
        Arrays.sort( cacheNames );

        buf.append( cacheNames.length + " caches; only non-empty caches listed below." );
        
        int count = 0;
        for ( String rawCacheName : cacheNames ) {
            Cache cache = cacheManager.getCache( rawCacheName );
            Statistics statistics = cache.getStatistics();

            long objectCount = statistics.getObjectCount();

            if ( objectCount == 0 ) {
                continue;
            }

            // a little shorter...
            String cacheName = rawCacheName;

            buf.append("\n"+ cacheName );
            long hits = statistics.getCacheHits();
            long misses = statistics.getCacheMisses();
            long inMemoryHits = statistics.getInMemoryHits();
            long inMemoryMisses = statistics.getInMemoryMisses();

            long onDiskHits = statistics.getOnDiskHits();
            long evictions = statistics.getEvictionCount();

            buf.append(  hits+"\t"  );
            buf.append(  misses+"\t"  );
            buf.append( objectCount+"\t");
            buf.append( inMemoryHits+"\t"  );
            buf.append( inMemoryMisses+"\t" );
            buf.append( onDiskHits+"\t" );
            buf.append( evictions+"\t");

            CacheConfiguration cacheConfiguration = cache.getCacheConfiguration();
            boolean eternal = cacheConfiguration.isEternal();
        }  
        return buf.toString();

    }

    
  

    private void setStatisticsEnabled( boolean b ) {
        String[] cacheNames = cacheManager.getCacheNames();

        for ( String rawCacheName : cacheNames ) {
            Cache cache = cacheManager.getCache( rawCacheName );
            cache.setSampledStatisticsEnabled( b );
        }
    }

}
