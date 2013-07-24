package ubc.pavlab.aspiredb.server.util;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.search.Attribute;
import net.sf.ehcache.search.Result;
import net.sf.ehcache.search.Results;
import net.sf.ehcache.search.expression.Criteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.HashSet;

/**
 * Implement a searchable cache by using Ehcache. 
 * 
 * @author frances
 * @version $Id: SearchableEhcache.java,v 1.2 2013/06/11 22:30:47 anton Exp $
 */
@Component
public abstract class SearchableEhcache<T> {
	@Autowired
	@Qualifier("ehcache")
	private CacheManager cacheManager;
	
	private Ehcache cache;

	public abstract Object getKey(T object);
	public abstract String getCacheName();
	
	@SuppressWarnings("unused")
	@PostConstruct
	private void initialize() {
		this.cache = this.cacheManager.getCache(getCacheName());
	}

	public Collection<T> fetchByCriteria(Criteria criteria) {
		net.sf.ehcache.search.Query query = this.cache.createQuery();
		query.includeValues();
		query.addCriteria(criteria);
	
		Results results = query.execute();
		
		Collection<T> genes = new HashSet<T>(results.size());
	
		for (Result result : results.all()) {
			genes.add((T)result.getValue());
		}
	
		return genes;
	}

	public boolean hasExpired() {
		this.cache.evictExpiredElements();
		
		return (this.cache.getSize() <= 0);
	}

	public void putAll(Collection<T> objects) {
		for (T object: objects) {
			this.cache.putIfAbsent(new Element(getKey(object), object));			
		}
	}

	public boolean isKeyInCache(Object key) {
		return this.cache.isKeyInCache(key);
	}
	
	public Attribute<Object> getSearchAttribute(String attributeName) {
		return this.cache.getSearchAttribute(attributeName);
	}
}
