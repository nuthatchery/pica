package org.nuthatchery.pica.resources.regions;

public interface IRelativeCodeRegion<BaseRegion extends ICodeRegion<?>> extends ICodeRegion<BaseRegion> {
	@Override
	IRelativeCodeRegion<BaseRegion> make(long offset, long length);


	/**
	 * Resolve this region so that it is relative to the base's point of
	 * reference.
	 *
	 * E.g., if this is a region within a region within a file, make it into a
	 * region directly within the file, adjusting offset as necessary.
	 *
	 * @return A new region, at the same level as the base
	 */
	BaseRegion resolve();
}
