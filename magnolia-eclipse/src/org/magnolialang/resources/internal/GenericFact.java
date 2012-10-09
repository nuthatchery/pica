package org.magnolialang.resources.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.magnolialang.resources.IResourceManager;
import org.magnolialang.resources.ISerializer;
import org.magnolialang.util.ISignature;

public class GenericFact<T> extends Fact<T> {

	private final ISerializer<T>	io;


	public GenericFact(String name, ISerializer<T> io) {
		super(name);
		this.io = io;
	}


	public GenericFact(String name, IResourceManager manager, URI fileStoreURI, ISerializer<T> io) {
		super(name, manager, fileStoreURI);
		this.io = io;
	}


	@Override
	protected IStoreUnit<T> loadHelper() throws IOException {
		return storage.get(factName, new StoreUnit<T>(io));
	}


	@Override
	protected void saveHelper(T val) {
		StoreUnit<T> unit = new StoreUnit<T>(val, signature, io);
		storage.put(factName, unit);
	}


	static class StoreUnit<T> implements IStoreUnit<T> {
		private final ISerializer<T>	io;
		private T						val;
		private ISignature				signature;


		public StoreUnit(ISerializer<T> io) {
			this.val = null;
			this.signature = null;
			this.io = io;
		}


		public StoreUnit(T val, ISignature signature, ISerializer<T> io) {
			this.val = val;
			this.signature = signature;
			this.io = io;
		}


		@Override
		public void writeValue(OutputStream stream) throws IOException {
			io.write(val, stream);
			signature.writeTo(stream);
		}


		@Override
		public void readValue(InputStream stream) throws IOException {
			val = io.read(stream);
			signature = signature.readFrom(stream);
		}


		@Override
		public T getValue() {
			return val;
		}


		@Override
		public ISignature getSignature() {
			return signature;
		}
	}

}
