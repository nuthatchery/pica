package org.magnolialang.resources.internal;

import org.magnolialang.util.ISignature;
import org.magnolialang.util.Signature;

public abstract class StoreUnit<T> implements IStoreUnit<T> {
	private ISignature	signature;


	public StoreUnit(ISignature signature) {
		this.signature = signature;
	}


	public StoreUnit() {
		this.signature = null;
	}


	@Override
	public byte[] getMetaData() {
		return signature.toBytes();
	}


	@Override
	public void setMetaData(byte[] data) {
		signature = new Signature(data);
	}


	@Override
	public ISignature getSignature() {
		return signature;
	}

}
