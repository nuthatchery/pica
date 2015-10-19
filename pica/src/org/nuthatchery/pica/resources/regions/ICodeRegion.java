package org.nuthatchery.pica.resources.regions;

public interface ICodeRegion<Base> extends IOffsetLength {

	/**
	 * The base typically identifies a file, uri or text.
	 *
	 * @return The base this region is relative to
	 */
	Base getBase();


	@Override
	ICodeRegion<Base> make(long offset, long length);

}
