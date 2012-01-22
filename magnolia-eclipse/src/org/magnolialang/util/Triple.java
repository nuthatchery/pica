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

	public final T1	first;
	public final T2	second;
	public final T3	third;


	public Triple(T1 first, T2 second, T3 third) {
		this.first = first;
		this.second = second;
		this.third = third;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result + ((second == null) ? 0 : second.hashCode());
		result = prime * result + ((third == null) ? 0 : third.hashCode());
		return result;
	}


	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		Triple other = (Triple) obj;
		if(first == null) {
			if(other.first != null)
				return false;
		}
		else if(!first.equals(other.first))
			return false;
		if(second == null) {
			if(other.second != null)
				return false;
		}
		else if(!second.equals(other.second))
			return false;
		if(third == null) {
			if(other.third != null)
				return false;
		}
		else if(!third.equals(other.third))
			return false;
		return true;
	}

}
