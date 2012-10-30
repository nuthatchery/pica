package org.magnolialang.resources.internal.storage;

import org.magnolialang.resources.storage.IStorableValue;
import org.magnolialang.util.ISignature;

public interface IStoreUnit<T> extends IStorableValue {
	ISignature getSignature();


	T getValue();
}
