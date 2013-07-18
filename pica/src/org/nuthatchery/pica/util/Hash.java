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
package org.nuthatchery.pica.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.nuthatchery.pica.errors.ImplementationError;

public class Hash {
	public static MessageDigest getMessageDigest() {
		try {
			return MessageDigest.getInstance("SHA-1");
		}
		catch(NoSuchAlgorithmException e) {
			try {
				return MessageDigest.getInstance("SHA-256");
			}
			catch(NoSuchAlgorithmException e1) {
				try {
					return MessageDigest.getInstance("MD5");
				}
				catch(NoSuchAlgorithmException e2) {
					throw new ImplementationError("No message digest algorithm found", e);
				}
			}
		}
	}


	/**
	 * This method produces a hash of the given byte array.
	 * 
	 * The hashing algorithm is (in order of preference) SHA-256, SHA-1 or MD5.
	 * 
	 * @param bytes
	 *            The bytes to hash; will not be modified by the method
	 * @return A hash of the byte array
	 */
	public static ISignature hashBytes(byte[] bytes) {
		MessageDigest md = getMessageDigest();
		byte[] digest = md.digest(bytes);
		return new Signature(digest);
	}


	/**
	 * This method produces a hash of the char array.
	 * 
	 * Due to character encodings and such, the returned hash is not guaranteed
	 * to be equal to the hash obtained using hashBytes or hashStream on the
	 * same data, although the same algorithm is used.
	 * 
	 * The hashing algorithm is (in order of preference) SHA-1, SHA-256 or MD5.
	 * 
	 * @param chars
	 *            The characters to hash; will not be modified by the method
	 * @return A hash of the char array
	 */
	public static ISignature hashChars(char[] chars) {
		MessageDigest md = getMessageDigest();
		byte[] bytes = new byte[chars.length * 2];
		CharBuffer cBuffer = ByteBuffer.wrap(bytes).asCharBuffer();
		for(char c : chars) {
			cBuffer.put(c);
		}

		byte[] digest = md.digest(bytes);
		return new Signature(digest);
	}


	/**
	 * This method produces a hash of the given input stream.
	 * 
	 * The hashing algorithm is (in order of preference) SHA-1, SHA-256 or MD5.
	 * 
	 * @param stream
	 *            A stream. Must be closed by the caller.
	 * @return A hash of the data in the stream
	 * @throws IOException
	 *             on errors reading from the stream
	 */
	public static ISignature hashStream(InputStream stream) throws IOException {
		MessageDigest md = getMessageDigest();
		byte[] buffer = new byte[8192];
		int bytesRead = stream.read(buffer);
		while(bytesRead >= 0) {
			md.update(buffer, 0, bytesRead);
			bytesRead = stream.read(buffer);
		}
		byte[] digest = md.digest();
		System.out.println("Message digest: " + md.toString());
		return new Signature(digest);
	}

}
