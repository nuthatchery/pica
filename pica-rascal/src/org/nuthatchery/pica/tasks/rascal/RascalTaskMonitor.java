package org.nuthatchery.pica.tasks.rascal;

import java.util.function.Consumer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.nuthatchery.pica.errors.ImplementationError;
import org.nuthatchery.pica.tasks.ITaskMonitor;
import org.nuthatchery.pica.tasks.NullTaskMonitor;
import org.nuthatchery.pica.tasks.TaskAbortedException;
import org.nuthatchery.pica.tasks.TaskCanceledException;
import org.nuthatchery.pica.tasks.TaskEstimator;
import org.nuthatchery.pica.tasks.TaskEstimator.TaskTimer;
import org.nuthatchery.pica.tasks.TaskId;
import org.rascalmpl.debug.IRascalMonitor;
import org.rascalmpl.interpreter.ConsoleRascalMonitor;
import org.rascalmpl.interpreter.NullRascalMonitor;

import io.usethesource.vallang.ISourceLocation;

public class RascalTaskMonitor implements ITaskMonitor {

	public static IRascalMonitor makeRascalMonitor(ITaskMonitor monitor, int workToDo) {
		if(monitor instanceof RascalTaskMonitor) {
			return ((RascalTaskMonitor) monitor).makeRascalMonitor(workToDo);
		}
//		else if(monitor instanceof EclipseTaskMonitor) {
//			IProgressMonitor em = EclipseTaskMonitor.makeProgressMonitor(monitor, workToDo);
//			return new RascalMonitor(em, new WarningsToMarkers());
//		}
		else if(monitor instanceof NullTaskMonitor) {
			return new NullRascalMonitor();
		}
		throw new UnsupportedOperationException("Only works for instances of RascalTaskMonitor");
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
		if(root == null) {
			aborted = true;
		}
		else {
			root.abort();
		}
	}


	@Override
	public boolean autoCancel(boolean flag) {
		boolean prev = autoCancel;
		autoCancel = flag;
		return prev;
	}


	public void autoCheck() throws TaskCanceledException, TaskAbortedException {
		if(isAborted()) {
			throw new TaskAbortedException();
		}
		else if(autoCancel && isCanceled()) {
			throw new TaskCanceledException();
		}
	}


	@Override
	public void begin(TaskId taskId) {
		// TODO Auto-generated method stub

	}


	@Override
	public void cancel() {
		monitor.setCanceled(true);
		if(root != null) {
			root.cancel();
		}
	}


	@Override
	public void check() throws TaskCanceledException, TaskAbortedException {
		if(isAborted()) {
			throw new TaskAbortedException();
		}
		else if(isCanceled()) {
			throw new TaskCanceledException();
		}
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
		return new IRascalMonitor() {
			class SubRascalMonitor {
				private final SubRascalMonitor parent;
				private final SubMonitor monitor;
				private int workActuallyDone;
				private int workRemaining;
				private int nextWorkUnit;


				SubRascalMonitor(SubMonitor monitor, String name, int workShare, int totalWork) {
					this.monitor = SubMonitor.convert(monitor, workShare);
					monitor.beginTask(name, totalWork);
					this.workRemaining = totalWork;
					this.parent = null;
				}


				SubRascalMonitor(SubRascalMonitor parent, String name, int workShare, int totalWork) {
					this.monitor = parent.monitor.newChild(workShare);
					monitor.beginTask(name, totalWork);
					this.workRemaining = totalWork;
					this.parent = parent;
					parent.nextWorkUnit = workShare;
				}


				SubRascalMonitor endJob() {
					monitor.done();
					workActuallyDone += nextWorkUnit;
					nextWorkUnit = 0;

					if(parent != null) {
						parent.workActuallyDone += parent.nextWorkUnit;
						if(parent.workRemaining != 0)
							parent.workRemaining -= parent.nextWorkUnit;
						parent.nextWorkUnit = 0;
					}
					return parent;
				}


				void event(int inc) {
					monitor.worked(nextWorkUnit);
					workActuallyDone += nextWorkUnit;
					if(workRemaining == 0)
						monitor.setWorkRemaining(200);
					else
						workRemaining -= nextWorkUnit;
					nextWorkUnit = inc;
				}


				public int getWorkDone() {
					return workActuallyDone;
				}


				void setName(String name) {
					monitor.subTask(name);
				}


				SubRascalMonitor startJob(String name, int workShare, int totalWork) {
					return new SubRascalMonitor(this, name, workShare, totalWork);
				}


				void todo(int work) {
					workRemaining = work;
					monitor.setWorkRemaining(work);
				}
			}
			private SubRascalMonitor subMon = null;
			private final IProgressMonitor monitor = RascalTaskMonitor.this.monitor.newChild(workToDo);


			private String topName;
//		  private final IWarningHandler handler = new WarningsToPrintWriter(new PrintWriter(System.err));


			private long nextPoll = 0;


			private boolean previousResult;


			@Override
			public int endJob(boolean succeeded) {
				if(subMon == null) {
					throw new UnsupportedOperationException("endJob without startJob");
				}
				int worked = subMon.getWorkDone();
				subMon = subMon.endJob();
				monitor.setTaskName(topName);
				return worked;
			}


			@Override
			public void event(int inc) {
				if(subMon != null)
					subMon.event(inc);
				else
					throw new ImplementationError("event() called before startJob()");
			}


			@Override
			public void event(String name) {
				event(name, 1);
			}


			@Override
			public void event(String name, int inc) {
				if(subMon != null) {
					event(inc);
					subMon.setName(name);
				}
				else {
					throw new ImplementationError("event() called before startJob()");
				}
			}


			@Override
			public boolean isCanceled() {
				if(System.currentTimeMillis() < nextPoll) {
					return previousResult;
				}
				nextPoll = System.currentTimeMillis() + 100;
				previousResult = monitor.isCanceled();
				return previousResult;
			}

			@Override
			public void startJob(String name) {
				startJob(name, 10, 0);
			}
			@Override
			public void startJob(String name, int totalWork) {
				startJob(name, totalWork, totalWork);
			}


			@Override
			public void startJob(String name, int workShare, int totalWork) {
				if(topName == null) {
					topName = name;
				}
				monitor.setTaskName(name);

				if(subMon == null)
					subMon = new SubRascalMonitor(SubMonitor.convert(monitor), name, workShare, totalWork);
				else
					subMon = subMon.startJob(name, workShare, totalWork);
			}


			@Override
			public void todo(int workRemaining) {
				if(subMon != null)
					subMon.todo(workRemaining);
				else
					throw new ImplementationError("event() called before startJob()");
			}


			@Override
			public void warning(String msg, ISourceLocation src) {
//		    handler.warning(msg, src);
			}

		};

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
//		endSubTask();
		autoCheck();
		SubMonitor newChild = monitor.newChild(progress);
		newChild.setTaskName(taskName);

		return new RascalTaskMonitor(newChild, this);
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
