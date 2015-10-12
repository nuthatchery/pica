package org.nuthatchery.pica.tasks;

import java.util.Arrays;

/**
 * A task identifier, identifying some job to be done.
 *
 * A task id has
 * <ul>
 * <li>a name, which should be unique,</li>
 * <li>a description, which may be displayed to the user (defaults to the name)</li>
 * <li>an optional list of arguments that are used to get better estimates of
 * the time it takes to complete a task</li>
 * <li>a scale factor, used to estimate timings for tasks that scale linearly
 * with the size of the input</li>
 *
 * The task monitor can estimate the time to complete a task based on previous
 * history, recorded in the TaskEstimator, which records an overall average for
 * a given task, as well as estimates based on the supplied arguments (for
 * simplicity, only the hashCode of the arguments is stored and taken into
 * consideration).
 *
 * Typically, for a task with a known number of inputs N, the scale can be set
 * to N (or some number computed from N).
 *
 * @author anya
 *
 */
public class TaskId {

	private final String name;
	private final String desc;
	private final int[] ids;
	private final double scale;


	public TaskId(String name, Object... arguments) {
		this(name, name, 1.0, arguments);
	}


	public TaskId(String name, String desc, double scale, Object... arguments) {
		this.ids = new int[arguments.length];
		for(int i = 0; i < arguments.length; i++) {
			this.ids[i] = arguments[i].hashCode();
		}
		this.scale = scale;
		this.name = name;
		this.desc = name;
	}


	public TaskId(String name, String desc, Object... arguments) {
		this(name, desc, 1.0, arguments);
	}


	public String getDescription() {
		return desc;
	}


	public String getName() {
		return name;
	}


	public double getScale() {
		return scale;
	}
}
