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
package org.magnolialang.util;

/**
 * A simple Triple class
 * 
 * @param <T1>
 *            Type of first element
 * @param <T2>
 *            Type of second element
 * @param <T3>
 *            Type of third element
 */
public class Triple<T1, T2, T3> {

	public final T1 first;
	public final T2 second;
	public final T3 third;


	public Triple(T1 first, T2 second, T3 third) {
		this.first = first;
		this.second = second;
		this.third = third;
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
		Triple other = (Triple) obj;
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
		if(third == null) {
			if(other.third != null) {
				return false;
			}
		}
		else if(!third.equals(other.third)) {
			return false;
		}
		return true;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (first == null ? 0 : first.hashCode());
		result = prime * result + (second == null ? 0 : second.hashCode());
		result = prime * result + (third == null ? 0 : third.hashCode());
		return result;
	}

}
