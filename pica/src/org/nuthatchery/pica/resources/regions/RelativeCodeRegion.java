package org.nuthatchery.pica.resources.regions;

public class RelativeCodeRegion<BaseRegion extends ICodeRegion<?>> extends CodeRegion<BaseRegion> implements IRelativeCodeRegion<BaseRegion> {

	public RelativeCodeRegion(BaseRegion base, long offset, long length) {
		super(base, offset, length);
		if(offset + length > base.getLength()) {
			throw new IllegalArgumentException("Region is outside parent region");
		}
	}


	@Override
	public IRelativeCodeRegion<BaseRegion> make(long offset, long length) {
		return new RelativeCodeRegion<>(getBase(), offset, length);
	}


	@SuppressWarnings("unchecked")
	@Override
	public BaseRegion resolve() {
		long baseOffset = getBase().getOffset();
		return (BaseRegion) getBase().make(baseOffset + getOffset(), getLength());
	}
}
