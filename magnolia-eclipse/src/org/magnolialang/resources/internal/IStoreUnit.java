package org.magnolialang.resources.internal;

import org.magnolialang.resources.storage.IStorableValue;
import org.magnolialang.util.ISignature;

public interface IStoreUnit<T> extends IStorableValue {
	ISignature getSignature();


	T getValue();
}
