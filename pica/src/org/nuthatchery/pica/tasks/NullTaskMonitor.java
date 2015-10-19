package org.nuthatchery.pica.tasks;

import java.util.function.Consumer;

public class NullTaskMonitor implements ITaskMonitor {

	@Override
	public void abort() {
		// TODO Auto-generated method stub

	}


	@Override
	public boolean autoCancel(boolean flag) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public void begin(TaskId taskId) {
		// TODO Auto-generated method stub

	}


	@Override
	public void cancel() {
		// TODO Auto-generated method stub

	}


	@Override
	public void check() throws TaskCanceledException, TaskAbortedException {
		// TODO Auto-generated method stub

	}


	@Override
	public void done() {
		// TODO Auto-generated method stub

	}


	@Override
	public void done(int work) {
		// TODO Auto-generated method stub

	}


	@Override
	public void doTask(TaskId taskId, Consumer<ITaskMonitor> task) throws TaskCanceledException, TaskAbortedException {
		// TODO Auto-generated method stub

	}


	@Override
	public int getWorkDone() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public int getWorkTodo() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public boolean isAborted() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean isCanceled() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public void setWorkTodo(int work) {
		// TODO Auto-generated method stub

	}


	@Override
	public ITaskMonitor subMonitor() {
		return this;
	}


	@Override
	public ITaskMonitor subMonitor(int progress) {
		return this;
	}


	@Override
	public ITaskMonitor subMonitor(String taskName, int progress) {
		return this;
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
