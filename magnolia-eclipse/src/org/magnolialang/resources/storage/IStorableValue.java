package org.magnolialang.resources.storage;

public interface IStorableValue {
	void setData(byte[] data);


	byte[] getData();


	byte[] getMetaData();


	void setMetaData(byte[] data);
}
