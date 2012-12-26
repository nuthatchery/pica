package org.magnolialang.resources.internal.storage;

import org.magnolialang.util.ISignature;
import org.magnolialang.util.Signature;

public abstract class StoreUnit<T> implements IStoreUnit<T> {
	private ISignature signature;


	public StoreUnit() {
		this.signature = null;
	}


	public StoreUnit(ISignature signature) {
		this.signature = signature;
	}


	@Override
	public byte[] getMetaData() {
		return signature.toBytes();
	}


	@Override
	public ISignature getSignature() {
		return signature;
	}


	@Override
	public void setMetaData(byte[] data) {
		signature = new Signature(data);
	}

}
