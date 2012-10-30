package org.magnolialang.resources.storage;

public interface IStorableValue {
	byte[] getData();


	byte[] getMetaData();


	void setData(byte[] data);


	void setMetaData(byte[] data);
}
