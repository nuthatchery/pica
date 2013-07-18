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
 * it under the terms of the GNU General Public License as published by
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
package org.nuthatchery.pica.util;

/**
 * A simple Pair class
 * 
 * @param <T1>
 *            Type of first element
 * @param <T2>
 *            Type of second element
 */
public class Pair<T1, T2> {

	private final T1 first;
	private final T2 second;


	public Pair(T1 first, T2 second) {
		this.first = first;
		this.second = second;
	}


	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj == null) {
			return false;
		}
		if(getClass() != obj.getClass()) {
			return false;
		}
		Pair other = (Pair) obj;
		if(first == null) {
			if(other.first != null) {
				return false;
			}
		}
		else if(!first.equals(other.first)) {
			return false;
		}
		if(second == null) {
			if(other.second != null) {
				return false;
			}
		}
		else if(!second.equals(other.second)) {
			return false;
		}
		return true;
	}


	/**
	 * @return First element of pair
	 */
	public T1 getFirst() {
		return first;
	}


	/**
	 * @return Second element of pair
	 */
	public T2 getSecond() {
		return second;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (first == null ? 0 : first.hashCode());
		result = prime * result + (second == null ? 0 : second.hashCode());
		return result;
	}

}
