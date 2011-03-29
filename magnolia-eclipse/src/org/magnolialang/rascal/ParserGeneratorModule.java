package org.magnolialang.rascal;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.magnolialang.Config;
import org.rascalmpl.eclipse.nature.RascalMonitor;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.asserts.ImplementationError;
import org.rascalmpl.interpreter.utils.JavaBridge;
import org.rascalmpl.parser.IParserInfo;
import org.rascalmpl.parser.RascalActionExecutor;
import org.rascalmpl.parser.gtd.IGTD;
import org.rascalmpl.parser.gtd.result.action.IActionExecutor;

class ParserGeneratorModule {
	private final Evaluator evaluator = StandAloneInvoker.getInterpreter()
			.newEvaluator();
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

		private RascalMonitor rm;

		public GeneratorJob(String jobname) {
			super(jobname);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			rm = new RascalMonitor(monitor);
			rm.startJob("Generating parser for " + name, 0, 118);
			if(!generatorLoaded) {
				rm.startJob("Loading parser generator", 30, 140);
				loadGenerator();
				rm.endJob(true);
			}
			rm.todo(85);
			// writerMonitor.setMonitor(monitor);

			try {
				rm.event("Loading grammar", 5); // 0.5s
				if(uri == null) {
					evaluator.doImport(rm, moduleName);
					uri = evaluator.getHeap().getModuleURI(moduleName);
					// TODO: this should really be done *before* doImport(), in
					// case the grammar is changed between import and
					// getLastModified()
					lastModified = getLastModified();
				}
				else {
					lastModified = getLastModified();
					evaluator.reloadModules(rm, Collections
							.singleton(moduleName), evaluator.getHeap()
							.getModuleURI(moduleName));
				}

				ISet productions = evaluator.getCurrentModuleEnvironment()
						.getProductions();

				// see if Rascal has a cached parser for this set of productions
				// TODO: any point in this in the presence of multiple
				// evaluators?
				// yes -- Rascal keeps track of productions and can tell if they
				// have changed
				// no need to duplicate that work here
				rm.event("Checking for cached parser", 1); // 0s
				parser = evaluator.getHeap().getObjectParser(moduleName,
						productions);

				if(parser == null) {
					ArrayList<Job> jobs = new ArrayList<Job>();
					String parserName = moduleName.replaceAll("::", ".");
					String normName = parserName.replaceAll("\\.", "_");

					rm.event("Importing and normalizing grammar", 5); // 5s
					IConstructor grammar = getGrammar(productions);

					rm.event("Getting grammar productions", 4); // 4s
					prodSet = (ISet) RascalInterpreter.getInstance().call(
							"astProductions",
							"import lang::rascal::syntax::ASTGen;", grammar);

					jobs = runJobs(jobs, IGrammarListener.REQUIRE_GRAMMAR);

					rm.event("Generating java source code for parser", 62); // 62s,
					// 13msg
					IString classString = (IString) evaluator.call(rm,
							"generateObjectParser", vf.string(packageName),
							vf.string(normName), grammar);

					rm.event("compiling generated java code", 3); // 3s
					parser = bridge.compileJava(uri, packageName + "."
							+ normName, classString.getValue());

					evaluator.getHeap().storeObjectParser(moduleName,
							productions, parser);

					jobs = runJobs(jobs, IGrammarListener.REQUIRE_PARSER);

					rm.event("Waiting for subtasks", 3);
					for(Job j : jobs)
						j.join();
					rm.endJob(true);
				}
				else
					rm.endJob(true);

				return Status.OK_STATUS;
			}
			catch(Exception e) {
				except = e;
				rm.endJob(false);
				return Status.CANCEL_STATUS;
			}
		}

		private ArrayList<Job> runJobs(ArrayList<Job> jobs, int required) {
			for(IGrammarListener l : RascalParser.getGrammarListeners(name,
					required)) {
				Job j = l.getJob(name, moduleName, uri, prodSet, parser,
						evaluator.getStdErr());
				if(j != null) {
					j.schedule();
					jobs.add(j);
				}
			}
			return jobs;
		}

		private void loadGenerator() {
			evaluator.doImport(rm, "lang::rascal::syntax::Generator");
			evaluator.doImport(rm, "lang::rascal::syntax::Normalization");
			evaluator.doImport(rm, "lang::rascal::syntax::Definition");
			evaluator.doImport(rm, "lang::rascal::syntax::Assimilator");
			generatorLoaded = true;
		}

		public IConstructor getGrammar(ISet imports) {
			return (IConstructor) evaluator
					.call(rm, "imports2grammar", imports);
		}

	}
}