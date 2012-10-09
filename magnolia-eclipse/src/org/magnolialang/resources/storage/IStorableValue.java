package org.magnolialang.resources.storage;


public interface IStorableValue {
	void setData(byte[] data);


	byte[] getData();


	String getMetaData();


	void setMetaData(String data);
}
