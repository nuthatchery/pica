package org.nuthatchery.pica.resources.regions;

public class CodeRegion<Base> extends OffsetLength implements ICodeRegion<Base> {

	private final Base base;


	public CodeRegion(Base base, long offset, long length) {
		super(offset, length);
		this.base = base;
	}


	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(!super.equals(obj)) {
			return false;
		}
		if(getClass() != obj.getClass()) {
			return false;
		}
		CodeRegion<?> other = (CodeRegion<?>) obj;
		if(base == null) {
			if(other.base != null) {
				return false;
			}
		}
		else if(!base.equals(other.base)) {
			return false;
		}
		return true;
	}


	@Override
	public Base getBase() {
		return base;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((base == null) ? 0 : base.hashCode());
		return result;
	}


	@Override
	public ICodeRegion<Base> make(long offset, long length) {
		return new CodeRegion<Base>(base, offset, length);
	}


	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(base).append(":").append(super.toString());
		return builder.toString();
	}
}
