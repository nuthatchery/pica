package org.magnolialang.rascal;

import org.eclipse.imp.pdb.facts.IValue;
import org.rascalmpl.interpreter.IRascalMonitor;

public interface IEvaluatorPool {

	/**
	 * Call the given Rascal function.
	 * 
	 * @param rm
	 * @param funName
	 * @param args
	 * @return Result of the function call
	 */
	IValue call(IRascalMonitor rm, String funName, IValue... args);


	/**
	 * Ensures that evaluator is fully loaded when method returns.
	 * 
	 * May take a long time to complete.
	 */
	void ensureInit();


	/**
	 * Reload all evaluators.
	 * 
	 * May complete asynchroniously.
	 */
	void reload();


	/**
	 * Call the given Rascal function, with no/default monitor
	 * 
	 * @param funName
	 * @param args
	 * @return Result of the function call
	 */
	IValue call(String string, IValue... args);

}
