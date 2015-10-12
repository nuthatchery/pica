package org.nuthatchery.pica.tasks.rascal;

import java.util.function.Consumer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.nuthatchery.pica.tasks.ITaskMonitor;
import org.nuthatchery.pica.tasks.TaskAbortedException;
import org.nuthatchery.pica.tasks.TaskCanceledException;
import org.nuthatchery.pica.tasks.TaskEstimator;
import org.nuthatchery.pica.tasks.TaskEstimator.TaskTimer;
import org.nuthatchery.pica.tasks.TaskId;
import org.nuthatchery.pica.tasks.ITaskMonitor;
import org.rascalmpl.debug.IRascalMonitor;
import org.rascalmpl.eclipse.nature.RascalMonitor;

public class RascalTaskMonitor implements ITaskMonitor {

	public static IRascalMonitor makeRascalMonitor(ITaskMonitor monitor, int workToDo) {
		if(monitor instanceof RascalTaskMonitor) {
			return ((RascalTaskMonitor) monitor).makeRascalMonitor(workToDo);
		}
		else
			throw new UnsupportedOperationException("Only works for instances of EclipseTaskMonitor");
	}

	private final SubMonitor monitor;
	private boolean aborted = false;
	private boolean autoCancel = false;


	private final RascalTaskMonitor root;


	public RascalTaskMonitor(SubMonitor pm) {
		this.monitor = pm;
		this.root = null;
	}


	private RascalTaskMonitor(SubMonitor pm, RascalTaskMonitor root) {
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
		// TODO Auto-generated method stub

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
		monitor.done();
	}


	@Override
	public void done(int work) {
		monitor.worked(work);
	}


	@Override
	public void doTask(TaskId taskId, Consumer<ITaskMonitor> task) throws TaskCanceledException, TaskAbortedException {
		autoCheck();

		int estimate = TaskEstimator.estimate(taskId);
		ITaskMonitor subMonitor = subMonitor(estimate);
		TaskTimer timer = TaskEstimator.measureTask(taskId);
		task.accept(subMonitor);
		timer.done(); // no timing is done if an exception is thrown
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


	public IRascalMonitor makeRascalMonitor(int workToDo) {
		return new RascalMonitor(monitor.newChild(workToDo), null);
	}


	@Override
	public void setWorkTodo(int work) {
		autoCheck();

		monitor.setWorkRemaining(work);
	}


	@Override
	public ITaskMonitor subMonitor() {
		autoCheck();

		int todo = Math.max(1, getWorkTodo() / 100);
		return new RascalTaskMonitor(monitor.newChild(todo), this);
	}


	@Override
	public ITaskMonitor subMonitor(int progress) {
		autoCheck();
		return new RascalTaskMonitor(monitor.newChild(progress), this);
	}


	@Override
	public ITaskMonitor subMonitor(String taskName, int progress) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void subTask(String string) {
		// TODO Auto-generated method stub

	}


	@Override
	public void subTask(TaskId taskId) {
		// TODO Auto-generated method stub

	}

}
