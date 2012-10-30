package org.magnolialang.resources.internal.facts;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.magnolialang.resources.ISerializer;
import org.magnolialang.resources.internal.storage.IStoreUnit;
import org.magnolialang.resources.internal.storage.StoreUnit;
import org.magnolialang.resources.storage.IStorage;
import org.magnolialang.util.ISignature;

public class GenericListFact<T> extends Fact<List<T>> {
	private final ISerializer<T>	io;


	public GenericListFact(String name, IStorage storage, ISerializer<T> io) {
		super(name, storage);
		this.io = io;
	}


	@Override
	protected IStoreUnit<List<T>> loadHelper() throws IOException {
		return storage.get(factName, new GenericListStoreUnit<T>(io));
	}


	@Override
	protected void saveHelper(List<T> val) {
		GenericListStoreUnit<T> unit = new GenericListStoreUnit<T>(val, signature, io);
		storage.put(factName, unit);
	}


	static class GenericListStoreUnit<T> extends StoreUnit<List<T>> {
		private final List<T>			val;
		private final ISerializer<T>	io;


		public GenericListStoreUnit(ISerializer<T> io) {
			super();
			this.val = null;
			this.io = io;
		}


		public GenericListStoreUnit(List<T> val, ISignature signature, ISerializer<T> io) {
			super(signature);
			this.val = val;
			this.io = io;
		}


		@Override
		public byte[] getData() {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			int size = val.size();
			stream.write(size & 0xff);
			stream.write(size >>> 8 & 0xff);
			stream.write(size >>> 16 & 0xff);
			stream.write(size >>> 24 & 0xff);
			for(T v : val)
				try {
					io.write(v, stream);
				}
			catch(IOException e) {
				e.printStackTrace();
			}
			return stream.toByteArray();
		}


		@Override
		public List<T> getValue() {
			return val;
		}


		@Override
		public void setData(byte[] bytes) {
			ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
			int size = stream.read();
			size = size << 8 | stream.read();
			size = size << 8 | stream.read();
			size = size << 8 | stream.read();
			/*val = new ArrayList<T>();
			for(int i = 0; i < size; i++)
				try {
					val.add(io.read(stream));
				}
				catch(IOException e) {
					e.printStackTrace();
				}
			 */
		}
	}

}