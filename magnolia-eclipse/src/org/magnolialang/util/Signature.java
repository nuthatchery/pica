/**************************************************************************
 * Copyright (c) 2012 Anya Helene Bagge
 * Copyright (c) 2012 University of Bergen
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. See http://www.gnu.org/licenses/
 * 
 * 
 * See the file COPYRIGHT for more information.
 * 
 * Contributors:
 * * Anya Helene Bagge
 * 
 *************************************************************************/
package org.magnolialang.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.Arrays;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

public class Signature implements ISignature {

	private final byte[] signature;


	public Signature(byte[] signature) {
		this.signature = Arrays.copyOf(signature, signature.length);
	}


	public Signature(String hexString) throws DecoderException {
		this.signature = Hex.decodeHex(hexString.toCharArray());
	}


	/**
	 * @param foo
	 */
	private Signature(InputStream stream, int length) throws IOException {
		signature = new byte[length];
		int read = stream.read(signature);
		if(read != length) {
			throw new IOException("Short read: expected " + length + " bytes, got " + read);
		}
	}


	@Override
	public void digest(MessageDigest md) {
		md.update(signature);
	}


	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj == null) {
			return false;
		}
		byte[] bytes;
		if(obj instanceof Signature) {
			bytes = ((Signature) obj).signature;
		}
		else if(obj instanceof ISignature) {
			bytes = ((ISignature) obj).toBytes();
		}
		else {
			return false;
		}
		if(!Arrays.equals(signature, bytes)) {
			return false;
		}
		return true;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(signature);
		return result;
	}


	@Override
	public ISignature readFrom(InputStream stream) throws IOException {
		return new Signature(stream, signature.length);
	}


	@Override
	public byte[] toBytes() {
		return Arrays.copyOf(signature, signature.length);
	}


	@Override
	public String toHexString() {
		return String.valueOf(Hex.encodeHex(signature));
	}


	@Override
	public ISignature valueOf(byte[] bytes) {
		return new Signature(bytes);
	}


	@Override
	public ISignature valueOf(String hexString) throws DecoderException {
		return new Signature(hexString);
	}


	@Override
	public void writeTo(OutputStream stream) throws IOException {
		stream.write(signature);
	}

}
