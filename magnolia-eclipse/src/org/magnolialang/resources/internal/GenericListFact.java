package org.magnolialang.resources.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.magnolialang.resources.IResourceManager;
import org.magnolialang.resources.ISerializer;
import org.magnolialang.util.ISignature;

public class GenericListFact<T> extends Fact<List<T>> {
	private final ISerializer<T>	io;


	public GenericListFact(String name, IResourceManager manager, URI fileStoreURI, ISerializer<T> io) {
		super(name, manager, fileStoreURI);
		this.io = io;
	}


	@Override
	protected IStoreUnit<List<T>> loadHelper() throws IOException {
		return storage.get(factName, new StoreUnit<T>(io));
	}


	@Override
	protected void saveHelper(List<T> val) {
		StoreUnit<T> unit = new StoreUnit<T>(val, signature, io);
		storage.put(factName, unit);
	}


	static class StoreUnit<T> implements IStoreUnit<List<T>> {
		private List<T>					val;
		private ISignature				signature;
		private final ISerializer<T>	io;


		public StoreUnit(ISerializer<T> io) {
			this.val = null;
			this.signature = null;
			this.io = io;
		}


		public StoreUnit(List<T> val, ISignature signature, ISerializer<T> io) {
			this.val = val;
			this.signature = signature;
			this.io = io;
		}


		@Override
		public void writeValue(OutputStream stream) throws IOException {
			int size = val.size();
			stream.write(size & 0xff);
			stream.write((size >>> 8) & 0xff);
			stream.write((size >>> 16) & 0xff);
			stream.write((size >>> 24) & 0xff);
			for(T v : val)
				io.write(v, stream);
			signature.writeTo(stream);
		}


		@Override
		public void readValue(InputStream stream) throws IOException {
			int size = stream.read();
			size = (size << 8) | stream.read();
			size = (size << 8) | stream.read();
			size = (size << 8) | stream.read();
			val = new ArrayList<T>();
			for(int i = 0; i < size; i++)
				val.add(io.read(stream));
			signature = signature.readFrom(stream);
		}


		@Override
		public List<T> getValue() {
			return val;
		}


		@Override
		public ISignature getSignature() {
			return signature;
		}
	}

}
