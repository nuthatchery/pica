package org.magnolialang.resources;

import java.lang.ref.SoftReference;
import java.util.Arrays;

import org.magnolialang.util.Pair;

public class Fact<T> {
	private byte[]				signature	= null;
	private SoftReference<T>	value		= null;
	private final String		fileStorePath;


	public Fact() {
		this.fileStorePath = null;
	}


	public Fact(String fileStorePath) {
		this.fileStorePath = fileStorePath;
	}


	public T getValue(byte[] signature) {
		if(value == null)
			return null;
		if(this.signature == signature || Arrays.equals(this.signature, signature))
			return value.get();
		else
			return null;
	}


	public T setValue(T newValue, byte[] newSignature) {
		T old = value == null ? null : value.get();
		value = new SoftReference<T>(newValue);
		signature = newSignature;
		return old;
	}


	public Pair<T, byte[]> getValue() {
		T t = value.get();
		return new Pair<T, byte[]>(t, signature);
	}


	public T dispose() {
		T result = value.get();
		value.clear();
		return result;
	}
}
