package org.magnolialang.rascal;

import java.util.List;

import org.magnolialang.errors.RascalErrors;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.NullRascalMonitor;
import org.rascalmpl.interpreter.control_exceptions.Throw;

/**
 * A version of the EvaluatorPool that does not depend on Eclipse.
 * 
 * TODO: use threads to load stuff asynchroniously
 */

public class ConsoleEvaluatorPool extends AbstractEvaluatorPool {

	private Evaluator evaluator = null;
	private boolean initialized = false;


	/**
	 * Don't call this constructor directly, use
	 * {@link org.magnolialang.infra.IInfra#makeEvaluatorPool(String, List)}
	 * 
	 * @param jobName
	 * @param imports
	 */
	public ConsoleEvaluatorPool(String jobName, List<String> imports) {
		super(jobName, imports);
	}


	/**
	 * Ensures that evaluator is fully loaded when method returns
	 */
	@Override
	public synchronized void ensureInit() {
		if(!initialized || evaluator == null) {
			waitForInit();
		}
	}


	@Override
	public synchronized void reload() {
		initialized = false;
		load();
	}


	private void load() {
		if(initialized) {
			return;
		}
		long time = System.currentTimeMillis();
		try {
			evaluator = makeEvaluator(new NullRascalMonitor());
		}
		catch(Throw t) {
			throw RascalErrors.decodeRascalError(t);
		}
		initialized = true;
		System.err.println(jobName + ": " + (System.currentTimeMillis() - time) + " ms");
		return;
	}


	/**
	 * @return an Evaluator with all the compiler code loaded
	 */
	@Override
	protected synchronized Evaluator getEvaluator() {
		if(!initialized || evaluator == null) {
			waitForInit();
		}
		return evaluator;
	}


	protected void waitForInit() {
	}
}
