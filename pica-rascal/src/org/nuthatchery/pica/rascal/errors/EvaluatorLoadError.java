package org.nuthatchery.pica.rascal.errors;

public class EvaluatorLoadError extends RuntimeException {

	private static final long serialVersionUID = 6612738563281840041L;


	public EvaluatorLoadError(String message) {
		super(message);
	}


	public EvaluatorLoadError(String message, Throwable cause) {
		super(message, cause);
	}
}
