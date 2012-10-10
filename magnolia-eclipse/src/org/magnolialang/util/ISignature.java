package org.magnolialang.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;

import org.apache.commons.codec.DecoderException;

public interface ISignature {
	public ISignature readFrom(InputStream stream) throws IOException;


	public void writeTo(OutputStream stream) throws IOException;


	void digest(MessageDigest md);


	@Override
	boolean equals(Object o);


	byte[] toBytes();


	String toHexString();


	ISignature valueOf(byte[] bytes);


	ISignature valueOf(String hexString) throws DecoderException;
}
