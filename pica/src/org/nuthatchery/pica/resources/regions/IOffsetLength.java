package org.nuthatchery.pica.resources.regions;

public interface IOffsetLength {
	/**
	 * @return Length + offset
	 */
	default long getEnd() {
		return getOffset() + getLength();
	}


	/**
	 * @return The length
	 */
	long getLength();


	/**
	 * @return The offset
	 */
	long getOffset();


	IOffsetLength make(long offset, long length);
}
