package org.nuthatchery.pica.tasks;

import java.util.function.Consumer;

/**
 * @author anya
 *
 */
public interface ITaskMonitor {

	/**
	 * Force the tasks monitored by this monitor to abort.
	 *
	 * This method returns immediately. The tasks should stop (by throwing a
	 * TaskAbortedException) on their next interaction with the monitor, but
	 * there is no guarantee that they'll actually be stopped.
	 *
	 */
	void abort();


	/**
	 * Set this monitor's auto cancellation flag. If set to true, any call to
	 * the monitor will result in a TaskCanceledException if cancellation has
	 * been requested.
	 *
	 * New submonitors will not inherit this flag, it must be set anew for each
	 * task.
	 *
	 * @param flag
	 *            True if exceptions should be thrown automatically on
	 *            cancellation, false otherwise
	 * @return Previous status of the flag
	 *
	 */
	boolean autoCancel(boolean flag);


	/**
	 * Indicate the start of this task.
	 *
	 * Should only be called once
	 *
	 * @param taskId
	 */
	void begin(TaskId taskId);


	/**
	 * Ask the tasks monitored by this monitor to stop.
	 *
	 * This method returns immediately. The tasks should stop next time they
	 * check the monitor for cancellation; though there is no guarantee that
	 * tasks respond to cancellation in a timely manner.
	 */
	void cancel();


	/**
	 * Throw an exception of cancellation or abortion has been requested.
	 *
	 * @throws TaskCanceledException
	 * @throws TaskAbortedException
	 */
	void check() throws TaskCanceledException, TaskAbortedException;


	/**
	 * Indicate that all work has been done.
	 */
	void done();


	/**
	 * Update the count of how much work has been done.
	 *
	 * @param work
	 */
	void done(int work);


	/**
	 * Perform a subtask.
	 *
	 * The given <code>task</code> is called with a submonitor for monitoring
	 * it's progress.
	 *
	 * @param progress
	 *            The amount of progress the subtask will contribute to this
	 *            monitor when completed
	 * @param task
	 *            The code to run
	 * @throws TaskCanceledException
	 *             if the task was canceled prematurely
	 * @throws TaskAbortedException
	 *             if the task was aborted prematurely
	 */
	default void doTask(int progress, Consumer<ITaskMonitor> task) throws TaskCanceledException, TaskAbortedException {
		task.accept(subMonitor(progress));
	}


	/**
	 * Perform a subtask.
	 *
	 * The given <code>task</code> is called with a submonitor for monitoring
	 * its progress.
	 *
	 * @param taskId
	 *            A task id, used to keep track of the average time to complete
	 *            the task. This will be used to estimate the progress
	 *            contribution.
	 *
	 * @param task
	 *            The code to run
	 * @throws TaskCanceledException
	 *             if the task was canceled prematurely
	 * @throws TaskAbortedException
	 *             if the task was aborted prematurely
	 */
	void doTask(TaskId taskId, Consumer<ITaskMonitor> task) throws TaskCanceledException, TaskAbortedException;


	int getWorkDone();


	int getWorkTodo();


	/**
	 * Check if a forced cancellation has been requested for the tasks monitored
	 * by this monitor.
	 *
	 * If result is true, the current task should be aborted as quickly as
	 * possible, with only minimal effort to ensure consistent state.
	 *
	 * @return True if forced cancellation has been requested
	 */
	boolean isAborted();


	/**
	 * Check if cancellation has been requested for the tasks monitored by this
	 * monitor.
	 *
	 * If result is true, cleanup should be done to restore a consistent state,
	 * and then a TaskCanceledException should be thrown to inform the caller of
	 * the cancellation. It may make sense to complete the current task, rather
	 * than cancel it, if little work remains. In this case, an exception should
	 * still *not* be thrown, the parent task is assumed to check cancellation
	 * on its own.
	 *
	 * @return True if cancellation has been requested.
	 */
	boolean isCanceled();


	/**
	 * Update the estimate of the amount of work left to do.
	 *
	 *
	 * @param work
	 */
	void setWorkTodo(int work);


	/**
	 * Obtain a new monitor for monitoring a subtask for which we have no amount
	 * of work estimate.
	 *
	 * Progress-to-be-done will be estimated as 1% of remaining work.
	 *
	 * @return A new monitor
	 */
	ITaskMonitor subMonitor();


	/**
	 * Obtain a new monitor for monitoring a subtask.
	 *
	 *
	 * @param progress
	 *            The amount of progress the subtask will contribute to this
	 *            monitor when completed
	 * @return A new monitor
	 */
	ITaskMonitor subMonitor(int progress);


	/**
	 * Obtain a new monitor for monitoring a subtask.
	 *
	 * @param taskName
	 *
	 *
	 * @param progress
	 *            The amount of progress the subtask will contribute to this
	 *            monitor when completed
	 * @return A new monitor
	 */
	ITaskMonitor subMonitor(String taskName, int progress);


	void subTask(String string);


	/**
	 * Indicate the start of a subtask.
	 *
	 * The subtask will be considered active until this method is called again,
	 * {@link #doTask(int, Consumer)} or {@link #doTask(TaskId, Consumer)} is
	 * called, a new submonitor is obtained, or {@link #done()} is called.
	 *
	 * The subtask's name will possibly contribute to information displayed to
	 * the user.
	 *
	 * @param taskId
	 *            Id/name of the sub task
	 */
	void subTask(TaskId taskId);
}
