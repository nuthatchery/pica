package org.nuthatchery.pica.resources.regions;

public class OffsetLength implements IOffsetLength {
	private final long length;
	private final long offset;


	public OffsetLength(long offset, long length) {
		super();
		this.length = length;
		this.offset = offset;
	}


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
		OffsetLength other = (OffsetLength) obj;
		if(length != other.length) {
			return false;
		}
		if(offset != other.offset) {
			return false;
		}
		return true;
	}


	@Override
	public long getLength() {
		return length;
	}


	@Override
	public long getOffset() {
		return offset;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (length ^ length >>> 32);
		result = prime * result + (int) (offset ^ offset >>> 32);
		return result;
	}


	@Override
	public IOffsetLength make(long offset, long length) {
		return new OffsetLength(offset, length);
	}


	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("+").append(offset).append("+").append(length);
		return builder.toString();
	}

}
