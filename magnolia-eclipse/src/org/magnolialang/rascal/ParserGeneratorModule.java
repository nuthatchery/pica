package org.magnolialang.rascal;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.magnolialang.Config;
import org.magnolialang.util.ITeeReceiver;
import org.magnolialang.util.TeePrintWriter;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.asserts.ImplementationError;
import org.rascalmpl.interpreter.utils.JavaBridge;
import org.rascalmpl.parser.IParserInfo;
import org.rascalmpl.parser.RascalActionExecutor;
import org.rascalmpl.parser.gtd.IGTD;
import org.rascalmpl.parser.gtd.result.action.IActionExecutor;

class ParserGeneratorModule {
	private final PrintWriterMonitor writerMonitor = new PrintWriterMonitor(
			null, 1);
	private final TeePrintWriter stderr = new TeePrintWriter(new PrintWriter(
			System.err), writerMonitor);
	private final Evaluator evaluator = StandAloneInvoker.getInterpreter()
			.newEvaluator(stderr, stderr);
	private final IValueFactory vf = evaluator.getValueFactory();
	private final JavaBridge bridge = new JavaBridge(
			evaluator.getClassLoaders(), vf);
	private final String moduleName;
	private final String name;
	private final GeneratorJob job;
	private URI uri;
	private Class<IGTD> parser = null;
	private long lastModified = 0;
	private Throwable except = null;
	private ISet prodSet = null;
	private static final String packageName = "org.rascalmpl.java.parser.object";
	private boolean generatorLoaded = false;

	ParserGeneratorModule(String moduleName) {
		this.moduleName = moduleName;
		if(moduleName.contains("::"))
			this.name = moduleName.substring(moduleName.lastIndexOf(":") + 1,
					moduleName.length());
		else
			this.name = moduleName;
		this.job = new GeneratorJob("Generating " + name + " parser");
	}

	synchronized IGTD getParser() {
		checkForUpdate();
		if(parser == null) {
			runGenerator();
		}

		try {
			// should we return a new instance every time?
			return parser.newInstance();
		}
		catch(InstantiationException e) {
			throw new ImplementationError(e.getMessage(), e);
		}
		catch(IllegalAccessException e) {
			throw new ImplementationError(e.getMessage(), e);
		}

	}

	public URI getURI() {
		return uri;
	}

	public ISet getProductions() {
		return prodSet;
	}

	public String getName() {
		return name;
	}

	public String getModuleName() {
		return moduleName;
	}

	private void runGenerator() {
		except = null;
		job.schedule();
		try {
			job.join();
		}
		catch(InterruptedException e) {
			e.printStackTrace();
		}
		if(except != null)
			throw new ImplementationError(except.getMessage(), except);
		if(job.getResult() == Status.CANCEL_STATUS)
			throw new ImplementationError("Parser generator for " + name
					+ " cancelled");
	}

	public IActionExecutor getActionExecutor(IGTD parser) {
		return new RascalActionExecutor(evaluator, (IParserInfo) parser);
	}

	private long getLastModified() {
		long lm = 0;
		if(uri != null) {
			try {
				lm = Config.getResolverRegistry().lastModified(uri);
			}
			catch(IOException e) {
				lm = 0;
			}
		}
		return lm;
	}

	private void checkForUpdate() {
		if(parser != null
				&& (lastModified == 0 || getLastModified() > lastModified)) {
			parser = null;
			prodSet = null;
		}
	}

	private class GeneratorJob extends Job {

		public GeneratorJob(String jobname) {
			super(jobname);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			if(generatorLoaded) {
				monitor.beginTask("Generating parser for " + name, 99);
				worked(monitor, 0, "Loading grammar"); // 0.5s
			}
			else {
				monitor.beginTask("Generating parser for " + name, 114);
				worked(monitor, 0, "Loading parser generator"); // 15s
				loadGenerator();
				worked(monitor, 15, "Loading grammar"); // 0.5s
			}
			writerMonitor.setMonitor(monitor);

			try {
				if(uri == null) {
					evaluator.doImport(moduleName);
					uri = evaluator.getHeap().getModuleURI(moduleName);
					// TODO: this should really be done *before* doImport(), in
					// case the grammar is changed between import and
					// getLastModified()
					lastModified = getLastModified();
				}
				else {
					lastModified = getLastModified();
					evaluator.reloadModules(Collections.singleton(moduleName),
							evaluator.getHeap().getModuleURI(moduleName));
				}

				ISet productions = evaluator.getCurrentModuleEnvironment()
						.getProductions();

				// see if Rascal has a cached parser for this set of productions
				// TODO: any point in this in the presence of multiple
				// evaluators?
				// yes -- Rascal keeps track of productions and can tell if they
				// have changed
				// no need to duplicate that work here
				worked(monitor, 1, "Checking for cached parser"); // 0s
				parser = evaluator.getHeap().getObjectParser(moduleName,
						productions);

				if(parser == null) {
					ArrayList<Job> jobs = new ArrayList<Job>();
					String parserName = moduleName.replaceAll("::", ".");
					String normName = parserName.replaceAll("\\.", "_");

					worked(monitor, 1, "Importing and normalizing grammar"); // 5s
					IConstructor grammar = getGrammar(productions);

					worked(monitor, 5, "Getting grammar productions"); // 4s
					prodSet = (ISet) RascalInterpreter.getInstance().call(
							"astProductions",
							"import lang::rascal::syntax::ASTGen;", grammar);

					jobs = runJobs(jobs, IGrammarListener.REQUIRE_GRAMMAR);

					worked(monitor, 4, "Generating java source code for parser"); // 62s,
																					// 13msg
					writerMonitor.workPerLine = 5;
					IString classString = (IString) evaluator.call(
							"generateObjectParser", vf.string(packageName),
							vf.string(normName), grammar);
					writerMonitor.workPerLine = 1;

					worked(monitor, 35, "compiling generated java code"); // 3s
					parser = bridge.compileJava(vf.sourceLocation(uri),
							packageName + "." + normName,
							classString.getValue());

					evaluator.getHeap().storeObjectParser(moduleName,
							productions, parser);

					jobs = runJobs(jobs, IGrammarListener.REQUIRE_PARSER);

					worked(monitor, 3, "Waiting for subtasks");
					for(Job j : jobs)
						j.join();
					worked(monitor, 1, "Done");
					System.err.printf("Total time %dms\n",
							System.currentTimeMillis() - currentTaskTime);
				}
				else
					monitor.worked(98);

				return Status.OK_STATUS;
			}
			catch(Exception e) {
				except = e;
				return Status.CANCEL_STATUS;
			}
			finally {
				writerMonitor.setMonitor(null);
				monitor.done();
			}
		}

		private ArrayList<Job> runJobs(ArrayList<Job> jobs, int required) {
			for(IGrammarListener l : RascalParser.getGrammarListeners(name,
					required)) {
				Job j = l
						.getJob(name, moduleName, uri, prodSet, parser, stderr);
				if(j != null) {
					j.schedule();
					jobs.add(j);
				}
			}
			return jobs;
		}

		private void loadGenerator() {
			evaluator.doImport("lang::rascal::syntax::Generator");
			evaluator.doImport("lang::rascal::syntax::Normalization");
			evaluator.doImport("lang::rascal::syntax::Definition");
			evaluator.doImport("lang::rascal::syntax::Assimilator");
			generatorLoaded = true;
		}

		public IConstructor getGrammar(ISet imports) {
			return (IConstructor) evaluator.call("imports2grammar", imports);
		}

	}

	private String currentTask = null;
	private long currentTaskTime = 0L;
	private int currentWorked = 0;

	public void worked(IProgressMonitor monitor, int amount, String nextTask) {
		if(currentTask != null && currentTaskTime != 0L) {
			System.err.printf("Worked %3d. Task completed in %dms: %s\n",
					currentWorked + amount, System.currentTimeMillis()
							- currentTaskTime, currentTask);
		}
		monitor.worked(amount);
		if(monitor.isCanceled())
			throw new OperationCanceledException();
		monitor.subTask(nextTask);
		currentTask = nextTask;
		currentTaskTime = System.currentTimeMillis();
		currentWorked += amount;
	}

	private class PrintWriterMonitor implements ITeeReceiver {
		IProgressMonitor monitor = null;
		int workPerLine = 0;

		PrintWriterMonitor(IProgressMonitor monitor, int workPerLine) {
			this.monitor = monitor;
			this.workPerLine = workPerLine;
		}

		@Override
		public void receive(String s) {
			synchronized(this) {
				if(monitor != null) {
					String[] lines = s.split("\n");
					for(String line : lines) {
						worked(monitor, workPerLine, line);
					}
				}
			}
		}

		public void setMonitor(IProgressMonitor m) {
			synchronized(this) {
				monitor = m;
			}
		}
	}
}