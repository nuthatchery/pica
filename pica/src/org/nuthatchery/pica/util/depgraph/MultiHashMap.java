/**************************************************************************
 * Copyright (c) 2012 Anya Helene Bagge
 * Copyright (c) 2012 University of Bergen
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. See http://www.gnu.org/licenses/
 * 
 * 
 * See the file COPYRIGHT for more information.
 * 
 * Contributors:
 * * Anya Helene Bagge
 * 
 *************************************************************************/
package org.nuthatchery.pica.util.depgraph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class MultiHashMap<K, V> implements IMultiMap<K, V> {
	private final HashMap<K, Set<? extends V>> map = new HashMap<K, Set<? extends V>>();


	@Override
	public void clear() {
		map.clear();
	}


	@Override
	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}


	@Override
	public MultiHashMap<K, V> copy() {
		MultiHashMap<K, V> multiMap = new MultiHashMap<K, V>();
		for(Entry<K, Set<? extends V>> entry : entrySet()) {
			multiMap.put(entry.getKey(), entry.getValue());
		}
		return multiMap;
	}


	@Override
	public Set<Entry<K, Set<? extends V>>> entrySet() {
		return map.entrySet();
	}


	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj == null) {
			return false;
		}
		if(!(obj instanceof IMultiMap)) {
			return false;
		}
		if(!keySet().equals(((IMultiMap<?, ?>) obj).keySet())) {
			return false;
		}
		@SuppressWarnings("unchecked")
		IMultiMap<K, ?> other = (IMultiMap<K, ?>) obj;
		for(K key : keySet()) {
			if(!map.get(key).equals(other.get(key))) {
				return false;
			}
		}
		return true;
	}


	@Override
	public Set<V> get(K key) {
		return (Set<V>) map.get(key);
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (map == null ? 0 : map.hashCode());
		return result;
	}


	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}


	@Override
	public boolean isEmpty(K key) {
		if(map.containsKey(key)) {
			return map.get(key).isEmpty();
		}
		else {
			return true;
		}
	}


	@Override
	public Set<K> keySet() {
		return map.keySet();
	}


	@Override
	public int numKeys() {
		return map.size();
	}


	@Override
	public void put(K key) {
		Set<V> c = (Set<V>) map.get(key);
		if(c == null) {
			c = new HashSet<V>();
			map.put(key, c);
		}
	}


	@Override
	public void put(K arg0, Set<? extends V> val) {
		Set<V> c = (Set<V>) map.get(arg0);
		if(c == null) {
			c = new HashSet<V>();
			map.put(arg0, c);
		}
		c.addAll(val);
	}


	@Override
	public void put(K key, V val) {
		Set<V> c = (Set<V>) map.get(key);
		if(c == null) {
			c = new HashSet<V>();
			map.put(key, c);
		}
		c.add(val);
	}


	@Override
	public void putAll(IMultiMap<? extends K, ? extends V> arg0) {
		for(Entry<? extends K, ?> entry : arg0.entrySet()) {
			put(entry.getKey(), (Set<V>) entry.getValue());
		}
	}


	@Override
	public void putAll(Map<? extends K, ? extends V> arg0) {
		for(Entry<? extends K, ? extends V> entry : arg0.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}


	@Override
	public Set<V> remove(K key) {
		return (Set<V>) map.remove(key);
	}


	@Override
	public boolean remove(K key, V value) {
		Set<V> c = (Set<V>) map.get(key);
		if(c == null) {
			return false;
		}
		return c.remove(value);
	}
}
