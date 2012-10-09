package org.magnolialang.resources.internal;

import org.magnolialang.util.ISignature;
import org.magnolialang.util.Pair;

public interface IFact<T> {

	T dispose();


	Pair<T, ISignature> getValue();


	T getValue(ISignature sourceSignature);


	T setValue(T newValue, ISignature newSignature);

}
