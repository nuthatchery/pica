package org.magnolialang.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;

public interface ISignature {
	@Override
	boolean equals(Object o);


	public void writeTo(OutputStream stream) throws IOException;


	public ISignature readFrom(InputStream stream) throws IOException;


	ISignature valueOf(byte[] bytes);


	byte[] toBytes();


	void digest(MessageDigest md);
}
