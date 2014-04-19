package org.nuthatchery.pica.util.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.nuthatchery.pica.errors.CancelledException;

@NonNullByDefault
public class FuturesQueue<V> implements CompletionService<V> {
	int outstandingTasks = 0;
	final ExecutorCompletionService<V> ecs;


	public FuturesQueue(Executor exec) {
		ecs = new ExecutorCompletionService<V>(exec);
	}


	@Override
	public boolean equals(@Nullable Object obj) {
		return ecs.equals(obj);
	}


	@Override
	public int hashCode() {
		return ecs.hashCode();
	}


	@Override
	@Nullable
	public Future<V> poll() {
		synchronized(this) {
			Future<V> future = ecs.poll();
			if(future != null) {
				outstandingTasks--;
			}
			return future;
		}
	}


	/**
	 * Not implemented.
	 * 
	 * @see java.util.concurrent.CompletionService#poll(long,
	 *      java.util.concurrent.TimeUnit)
	 */
	@Override
	@Nullable
	public Future<V> poll(long timeout, @Nullable TimeUnit unit) {
		throw new UnsupportedOperationException(); // we can't do this safely
	}


	@Override
	@Nullable
	public Future<V> submit(Callable<V> task) {
		synchronized(this) {
			outstandingTasks++;
			return ecs.submit(task);
		}
	}


	@Override
	@Nullable
	public Future<V> submit(Runnable task, V result) {
		synchronized(this) {
			outstandingTasks++;
			return ecs.submit(task, result);
		}
	}


	/**
	 * Get the next completed task, waiting for outstanding tasks if necessary.
	 * 
	 * @return The next completed task, or null if no tasks are outstanding or
	 *         finished
	 * @see CompletionService#take()
	 */
	@Override
	@Nullable
	public Future<V> take() {
		synchronized(this) {
			int i = --outstandingTasks;
			if(i < 0) {
				outstandingTasks++;
				return null;
			}
		}
		try {
			Future<V> take = ecs.take();
			return take;
		}
		catch(InterruptedException e) {
			throw new CancelledException("Interrupted while waiting", e);
		}
	}


	@SuppressWarnings("null")
	@Override
	public String toString() {
		return ecs.toString();
	}
}
