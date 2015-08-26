package ubc.pavlab.aspiredb.server.util;

import java.util.Collection;
import java.util.HashSet;

import javax.annotation.PostConstruct;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.search.Attribute;
import net.sf.ehcache.search.Result;
import net.sf.ehcache.search.Results;
import net.sf.ehcache.search.expression.Criteria;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Implement a searchable cache by using Ehcache.
 * 
 * @author frances
 * @version $Id: SearchableEhcache.java,v 1.2 2013/06/11 22:30:47 anton Exp $
 */
@Component
public abstract class SearchableEhcache<T> {
    @Autowired
    private CacheManager cacheManager;

    private Ehcache cache;

    public Collection<T> fetchByCriteria( Criteria criteria ) {
        net.sf.ehcache.search.Query query = this.cache.createQuery();
        // query.includeValues();
        query.includeKeys();
        query.addCriteria( criteria );

        Results results = query.execute();

        Collection<T> genes = new HashSet<T>( results.size() );

        for ( Result result : results.all() ) {
            genes.add( ( T ) cache.get( result.getKey() ).getObjectValue() );
            // genes.add( ( T ) result.getValue() );
        }

        return genes;
    }

    public abstract String getCacheName();

    public abstract Object getKey( T object );

    public Attribute<Object> getSearchAttribute( String attributeName ) {
        return this.cache.getSearchAttribute( attributeName );
    }

    /**
     * Evicts expired elements in the cache. Warning: Introduces a performance hit (~8-10ms per call).
     * 
     * @return
     */
    public boolean hasExpired() {
        // Causes all elements stored in the Cache to be synchronously checked for expiry (every 5 minutues), and if
        // expired, evicted.
        this.cache.evictExpiredElements();
        // Gets the size of the cache.This number is the actual number of elements, including expired elements that have
        // NOT BEEN REMOVED.
        return ( this.cache.getSize() <= 0 );
    }

    public boolean isKeyInCache( Object key ) {
        return this.cache.isKeyInCache( key );
    }

    public void putAll( Collection<T> objects ) {
        for ( T object : objects ) {
            this.cache.putIfAbsent( new Element( getKey( object ), object ) );
        }
    }

    @PostConstruct
    private void initialize() {
        this.cache = this.cacheManager.getCache( getCacheName() );
    }
}
