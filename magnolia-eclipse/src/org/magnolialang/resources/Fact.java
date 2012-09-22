package org.magnolialang.resources;

import java.lang.ref.SoftReference;

import org.magnolialang.util.ISignature;
import org.magnolialang.util.Pair;

public class Fact<T> {
	private ISignature			signature	= null;
	private SoftReference<T>	value		= null;
	private final String		fileStorePath;


	public Fact() {
		this.fileStorePath = null;
	}


	public Fact(String fileStorePath) {
		this.fileStorePath = fileStorePath;
	}


	public T dispose() {
		T result = value.get();
		value.clear();
		return result;
	}


	public Pair<T, ISignature> getValue() {
		T t = value.get();
		return new Pair<T, ISignature>(t, signature);
	}


	public T getValue(ISignature sourceSignature) {
		if(value == null || signature == null)
			return null;
		else if(signature.equals(sourceSignature))
			return value.get();
		else
			return null;
	}


	public T setValue(T newValue, ISignature newSignature) {
		T old = value == null ? null : value.get();
		value = new SoftReference<T>(newValue);
		signature = newSignature;
		return old;
	}
}
