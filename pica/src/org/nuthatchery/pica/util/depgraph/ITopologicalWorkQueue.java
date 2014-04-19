package org.nuthatchery.pica.util.depgraph;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.nuthatchery.pica.errors.CancelledException;

/**
 * Work queue suitable for concurrent processing of work units with
 * dependencies.
 * 
 * A call to {@link #next()} or {@link #waitForNext()} will give a work unit for
 * which all the dependencies have already been processed. Completion of a unit
 * is indicated by calling {@link #done(T)}.
 * 
 * @param <T>
 */
@NonNullByDefault
public interface ITopologicalWorkQueue<T> {

	/**
	 * Indicate that processing is complete for the given work unit.
	 * 
	 * @param work
	 *            The completed work.
	 */
	void done(T work);


	/**
	 * @return True if there are no more work units to be processed.
	 */
	boolean isAllDone();


	/**
	 * Try to obtain another work unit.
	 * 
	 * IMPORTANT: If the return value is non-null, the caller *must*
	 * subsequently call {@link #done(T)} on the object, or the queue will
	 * stall.
	 * 
	 * @return The next work unit in the queue, or null if no work is available
	 *         at this time.
	 */
	@Nullable
	T next();


	/**
	 * Obtain another work unit.
	 * 
	 * Will wait, if necessary.
	 * 
	 * IMPORTANT: If the return value is non-null, the caller *must*
	 * subsequently call {@link #done(T)} on the object, or the queue will
	 * stall.
	 * 
	 * @return The next work unit in the queue, or null if no work is available.
	 * @throws CancelledException
	 *             If the thread was interrupted while waiting.
	 */
	@Nullable
	T waitForNext() throws CancelledException;

}
