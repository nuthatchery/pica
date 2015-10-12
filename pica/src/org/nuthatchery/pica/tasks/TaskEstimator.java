package org.nuthatchery.pica.tasks;

import java.util.HashMap;
import java.util.Map;

public class TaskEstimator {
	static class EstimateEntry {
		private String taskId;
		private long totalTime;
		private int numTimes;


		EstimateEntry(String taskId) {
			this.taskId = taskId;
			this.totalTime = 0L;
			this.numTimes = 0;
		}
	}


	public static class TaskTimer {
		private long t;
		private EstimateEntry entry;


		public TaskTimer(EstimateEntry entry) {
			this.t = System.currentTimeMillis();
			this.entry = entry;
		}


		public void done() {
			if(t != -1) {
				long dt = System.currentTimeMillis() - t;

				synchronized(estimates) {
					numTimes++;
					totalTime += dt;
					entry.numTimes++;
					entry.totalTime += dt;
					t = -1;
				}
			}
			else {
				throw new IllegalStateException("done() called more than once");
			}
		}
	}

	private static long totalTime;


	private static int numTimes;


	private static final Map<String, EstimateEntry> estimates = new HashMap<>();


	public static int estimate(TaskId taskId) {
		String name = taskId.getName();

		synchronized(estimates) {
			EstimateEntry est = estimates.get(name);
			if(est != null && est.numTimes != 0) {
				return (int) (est.totalTime / est.numTimes);
			}
			else if(numTimes != 0) {
				return (int) (totalTime / numTimes);
			}
			else {
				return 100;
			}
		}
	}


	/**
	 * Measure the time taken to complete a given time.
	 *
	 * The measurement is completed by calling done() on the returned TaskTimer
	 * after the task has been completed.
	 *
	 * @param taskId
	 * @return
	 */
	public static TaskTimer measureTask(TaskId taskId) {
		String name = taskId.getName();

		synchronized(estimates) {
			EstimateEntry est = estimates.get(name);
			if(est == null) {
				est = new EstimateEntry(name);
				estimates.put(name, est);
			}

			return new TaskTimer(est);
		}
	}
}
