package org.nuthatchery.pica.errors;

public class CancelledException extends RuntimeException {
	private static final long serialVersionUID = -7697859171024079501L;


	public CancelledException(String message) {
		super(message);
	}


	public CancelledException(String message, Throwable cause) {
		super(message, cause);
	}
}
