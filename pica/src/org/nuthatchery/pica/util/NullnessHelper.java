package org.nuthatchery.pica.util;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class NullnessHelper {
	private NullnessHelper() {
	}


	/**
	 * Helps nullness analysis by asserting that an expression is known to be
	 * non-null.
	 * 
	 * For use when a method is guaranteed to return non-null, but it has not be
	 * annotated as @NonNull.
	 * 
	 * 
	 * @param value
	 *            A value known not to be null
	 * @return The same value, annotated @NonNull
	 */
	@NonNull
	public static <T> T assertNonNull(@Nullable T value) {
		assert value != null;
		return value;
	}


	/**
	 * Check a value that is assumed to be non-null, and throw a NPE if it
	 * isn't.
	 * 
	 * @param value
	 *            A value assumed to be non-null
	 * @return The same value, annotated @NonNull
	 * @throws NullPointerException
	 *             if the value turns out to be null
	 */
	@NonNull
	@SuppressFBWarnings("NP_PARAMETER_MUST_BE_NONNULL_BUT_MARKED_AS_NULLABLE")
	public static <T> T checkNonNull(@Nullable T value) throws NullPointerException {
		if(value == null) {
			throw new NullPointerException();
		}
		return value;
	}

}
