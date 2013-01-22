package org.magnolialang.resources.storage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class InMemoryStorage implements IStorage {

	private final Map<String, IStorableValue> store = new HashMap<String, IStorableValue>();


	@Override
	public void declare(String key) {

	}


	@Override
	public <T extends IStorableValue> T get(String key, T storable) throws IOException {
		IStorableValue v = store.get(key);
		if(v == null)
			return null;
		storable.setData(v.getData());
		storable.setMetaData(v.getMetaData());
		return storable;
	}


	/**
	 * We assume the byte[] fields in 'value' will not change.
	 * Otherwise would have to copy them.
	 */
	@Override
	public void put(String key, IStorableValue value) {
		IStorableValue v = new StorableValue(value.getData(), value.getMetaData());
		store.put(key, v);
	}


	/** Does nothing, as this storage is not persistent. */
	@Override
	public void save() throws IOException {
	}


	@Override
	public IStorage subStorage(String name) {
		// TODO Auto-generated method stub
		return null;
	}


	private static final class StorableValue implements IStorableValue {
		private byte[] data;

		private byte[] metaData;


		public StorableValue(byte[] data, byte[] metaData) {
			this.data = data;
			this.metaData = metaData;
		}


		@Override
		public byte[] getData() {
			return data;
		}


		@Override
		public byte[] getMetaData() {
			return metaData;
		}


		@Override
		public void setData(byte[] data) {
			this.data = data;
		}


		@Override
		public void setMetaData(byte[] metaData) {
			this.metaData = metaData;
		}

	}

}
