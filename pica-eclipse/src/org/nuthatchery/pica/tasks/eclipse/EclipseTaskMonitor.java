package org.nuthatchery.pica.tasks.eclipse;

import java.util.function.Consumer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.nuthatchery.pica.tasks.ITaskMonitor;
import org.nuthatchery.pica.tasks.TaskAbortedException;
import org.nuthatchery.pica.tasks.TaskCanceledException;
import org.nuthatchery.pica.tasks.TaskEstimator;
import org.nuthatchery.pica.tasks.TaskEstimator.TaskTimer;
import org.nuthatchery.pica.tasks.TaskId;

public class EclipseTaskMonitor implements ITaskMonitor {

	public static IProgressMonitor makeProgressMonitor(ITaskMonitor monitor, int workToDo) {
		if(monitor instanceof EclipseTaskMonitor) {
			return ((EclipseTaskMonitor) monitor).makeProgressMonitor(workToDo);
		}
		else
			throw new UnsupportedOperationException("Only works for instances of EclipseTaskMonitor");
	}

	private final SubMonitor monitor;
	private boolean aborted = false;
	private boolean autoCancel = false;


	private final EclipseTaskMonitor root;
	private TaskId currentSubTask;
	private TaskTimer currentMeasure;


	public EclipseTaskMonitor(SubMonitor pm) {
		this.monitor = pm;
		this.root = null;
	}


	private EclipseTaskMonitor(SubMonitor pm, EclipseTaskMonitor root) {
		this.monitor = pm;
		this.root = root;
	}


	@Override
	public void abort() {
		monitor.setCanceled(true);
		if(root == null)
			aborted = true;
		else
			root.abort();
	}


	@Override
	public boolean autoCancel(boolean flag) {
		boolean prev = autoCancel;
		autoCancel = flag;
		return prev;
	}


	public void autoCheck() throws TaskCanceledException, TaskAbortedException {
		if(isAborted())
			throw new TaskAbortedException();
		else if(autoCancel && isCanceled())
			throw new TaskCanceledException();
	}


	@Override
	public void begin(TaskId taskId) {

	}


	@Override
	public void cancel() {
		monitor.setCanceled(true);
		if(root != null)
			root.cancel();
	}


	@Override
	public void check() throws TaskCanceledException, TaskAbortedException {
		if(isAborted())
			throw new TaskAbortedException();
		else if(isCanceled())
			throw new TaskCanceledException();
	}


	@Override
	public void done() {
		endSubTask();
		monitor.done();
	}


	@Override
	public void done(int work) {
		monitor.worked(work);
	}


	@Override
	public void doTask(TaskId taskId, Consumer<ITaskMonitor> task) throws TaskCanceledException, TaskAbortedException {
		endSubTask();
		autoCheck();

		int estimate = TaskEstimator.estimate(taskId);
		ITaskMonitor subMonitor = subMonitor(estimate);
		TaskTimer timer = TaskEstimator.measureTask(taskId);
		task.accept(subMonitor);
		timer.done(); // no timing is done if an exception is thrown
	}


	private void endSubTask() {
		if(currentMeasure != null) {
			currentMeasure.done();
			currentMeasure = null;
		}
		currentSubTask = null;
	}


	@Override
	public int getWorkDone() {
		autoCheck();
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public int getWorkTodo() {
		autoCheck();
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public boolean isAborted() {
		autoCheck();
		return aborted;
	}


	@Override
	public boolean isCanceled() {
		autoCheck();
		return monitor.isCanceled();
	}


	public IProgressMonitor makeProgressMonitor(int workToDo) {
		return monitor.newChild(workToDo);
	}


	@Override
	public void setWorkTodo(int work) {
		autoCheck();

		monitor.setWorkRemaining(work);
	}


	@Override
	public ITaskMonitor subMonitor() {
		endSubTask();
		autoCheck();

		int todo = Math.max(1, getWorkTodo() / 100);
		return new EclipseTaskMonitor(monitor.newChild(todo), this);
	}


	@Override
	public ITaskMonitor subMonitor(int progress) {
		endSubTask();
		autoCheck();
		return new EclipseTaskMonitor(monitor.newChild(progress), this);
	}


	@Override
	public ITaskMonitor subMonitor(String taskName, int progress) {
		endSubTask();
		autoCheck();
		SubMonitor newChild = monitor.newChild(progress);
		newChild.setTaskName(taskName);

		return new EclipseTaskMonitor(newChild);
	}


	@Override
	public void subTask(String string) {
		// TODO Auto-generated method stub

	}


	@Override
	public void subTask(TaskId taskId) {
		endSubTask();
		autoCheck();
		currentSubTask = taskId;
		currentMeasure = TaskEstimator.measureTask(taskId);
		monitor.subTask(taskId.getDescription());
	}

}
