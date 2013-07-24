package ubc.pavlab.aspiredb.shared;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Table<R,C,V> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1456793494466479362L;
	
	private Map<R, Map<C,V>> map;
	private Set<R> rowKeySet;
	private Set<C> columnKeySet;
	
	public Table() {
	}
	
	private Table(Set<R> rows, Set<C> columns) {
		this.map = new HashMap<R, Map<C,V>>();
		this.rowKeySet = rows;
		this.columnKeySet = columns;
	}
	
	public static <R,C,V> Table<R,C,V> create(Set<R> rows, Set<C> columns) {		
		return new Table<R, C, V>( rows, columns );
	}
	
	public Set<R> rowKeySet() {
		return rowKeySet;
	}
	
	public Set<C> columnKeySet() {
		return columnKeySet;
	}

	public void put(R row, C column, V value) {
		//if ( isOutOfBounds( row, column ) ) throw ...;
		Map<C,V> r = this.map.get( row );
		if (r == null) {
			r = new HashMap<C,V>();
			this.map.put( row, r );
		}
		r.put( column, value );
	}
	
	public V get(R row, C column) {
		//if ( isOutOfBounds( row, column ) ) throw ...;
		Map<C,V> r = this.map.get( row );
		if (r == null) return null;
		return r.get( column );		
	}

}
