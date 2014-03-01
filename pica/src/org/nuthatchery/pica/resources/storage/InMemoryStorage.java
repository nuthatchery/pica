/**************************************************************************
 * Copyright (c) 2012-2013 Anya Helene Bagge
 * Copyright (c) 2012-2013 Tero Hasu
 * Copyright (c) 2012-2013 University of Bergen
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. See http://www.gnu.org/licenses/
 * 
 * 
 * See the file COPYRIGHT for more information.
 * 
 * Contributors:
 * * Anya Helene Bagge
 * * Tero Hasu
 * 
 *************************************************************************/
package org.nuthatchery.pica.resources.storage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

public class InMemoryStorage implements IStorage {

	private final Map<String, IStorableValue> store = new HashMap<String, IStorableValue>();


	@Override
	public void declare(String key) {

	}


	@Override
	@Nullable
	public <T extends IStorableValue> T get(String key, T storable) throws IOException {
		IStorableValue v = store.get(key);
		if(v == null)
			return null;
		byte[] data = v.getData();
		if(data != null) {
			storable.setData(data);
			storable.setMetaData(v.getMetaData());
			return storable;
		}
		else
			return null;
	}


	/**
	 * We assume the byte[] fields in 'value' will not change.
	 * Otherwise we would have to copy them.
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
	@Nullable
	public IStorage subStorage(String name) {
		// TODO Auto-generated method stub
		return null;
	}


	private static final class StorableValue implements IStorableValue {
		@Nullable
		private byte[] data;

		private byte[] metaData;


		public StorableValue(@Nullable byte[] data, byte[] metaData) {
			this.data = data;
			this.metaData = metaData;
		}


		@Override
		@Nullable
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
