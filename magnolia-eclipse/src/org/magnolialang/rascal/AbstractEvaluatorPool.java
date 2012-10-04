package org.magnolialang.rascal;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.imp.pdb.facts.IValue;
import org.magnolialang.terms.TermFactory;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.IRascalMonitor;

public abstract class AbstractEvaluatorPool implements IEvaluatorPool {

	private final List<String>	imports;
	protected final String		jobName;


	public AbstractEvaluatorPool(String jobName, List<String> imports) {
		super();
		this.jobName = jobName;
		this.imports = Collections.unmodifiableList(new ArrayList<String>(imports));

	}


	protected abstract Evaluator getEvaluator();


	@Override
	public synchronized IValue call(IRascalMonitor rm, String funName, IValue... args) {
		return getEvaluator().call(rm, funName, args);
	}


	protected Evaluator makeEvaluator(IRascalMonitor rm) {
		rm.startJob("Loading " + jobName, 10 + imports.size() * 10);
		PrintWriter stderr = new PrintWriter(System.err);
		rm.event(5);
		Evaluator evaluator = RascalInterpreter.getInstance().newEvaluator(stderr, stderr);
		rm.event(5);
		evaluator.getCurrentEnvt().getStore().importStore(TermFactory.ts);
		for(String imp : imports) {
			rm.event("Importing " + imp, 10);
			evaluator.doImport(rm, imp);
		}
		rm.endJob(true);

		return evaluator;
	}
}
