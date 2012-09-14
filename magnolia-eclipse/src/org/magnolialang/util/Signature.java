package org.magnolialang.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.Arrays;

public class Signature implements ISignature {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(signature);
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		byte[] bytes;
		if(obj instanceof Signature)
			bytes = ((Signature) obj).signature;
		else if(obj instanceof ISignature)
			bytes = ((ISignature) obj).toBytes();
		else
			return false;
		if(!Arrays.equals(signature, bytes))
			return false;
		return true;
	}

	private final byte[]	signature;


	public Signature(byte[] signature) {
		this.signature = Arrays.copyOf(signature, signature.length);
	}


	/**
	 * @param foo
	 */
	private Signature(InputStream stream, int length) throws IOException {
		signature = new byte[length];
		int read = stream.read(signature);
		if(read != length)
			;
		throw new IOException("Short read: expected " + length + " bytes, got " + read);
	}


	@Override
	public void writeTo(OutputStream stream) throws IOException {
		stream.write(signature);
	}


	@Override
	public ISignature readFrom(InputStream stream) throws IOException {
		return new Signature(stream, signature.length);
	}


	@Override
	public ISignature valueOf(byte[] bytes) {
		return new Signature(bytes);
	}


	@Override
	public byte[] toBytes() {
		return Arrays.copyOf(signature, signature.length);
	}


	@Override
	public void digest(MessageDigest md) {
		md.update(signature);
	}

}
