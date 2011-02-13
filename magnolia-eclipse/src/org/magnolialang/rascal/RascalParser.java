package org.magnolialang.rascal;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.imp.pdb.facts.ISet;
import org.magnolialang.Config;
import org.magnolialang.util.ITeeReceiver;
import org.magnolialang.util.TeePrintWriter;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.asserts.ImplementationError;
import org.rascalmpl.parser.IParserInfo;
import org.rascalmpl.parser.ParserGenerator;
import org.rascalmpl.parser.RascalActionExecutor;
import org.rascalmpl.parser.gtd.IGTD;
import org.rascalmpl.parser.gtd.result.action.IActionExecutor;

public class RascalParser {
	private static final Map<String, ParserModule> modules = new HashMap<String, ParserModule>();

	/**
	 * Return a parser for the given grammar. The grammar module must be in the
	 * Rascal interpreter's search path.
	 * 
	 * This method *may* take a long time, if the parser needs to be generated.
	 * 
	 * @param moduleName
	 *            Rascal module name for the grammar
	 * @return A new parser instance
	 */
	public static IGTD getParser(String moduleName) {
		return getParserModule(moduleName).getParser();
	}

	public static IActionExecutor getActionExecutor(String moduleName,
			IGTD parser) {
		return getParserModule(moduleName).getActionExecutor(parser);
	}

	private static synchronized ParserModule getParserModule(String moduleName) {
		ParserModule mod = modules.get(moduleName);
		if(mod == null) {
			mod = new ParserModule(moduleName);
			modules.put(moduleName, mod);
		}
		return mod;
	}

}

class ParserModule {
	private static ParserGenerator parserGenerator;
	private final Evaluator evaluator = StandAloneInvoker.getInterpreter()
			.newEvaluator();
	private final PrintWriterMonitor writerMonitor = new PrintWriterMonitor(
			null, 1);
	private final TeePrintWriter stderr = new TeePrintWriter(
			evaluator.getStdErr(), writerMonitor);
	private final String moduleName;
	private final String name;
	private final GeneratorJob job;
	private URI uri;
	private Class<IGTD> parser = null;
	private long lastModified = 0;
	private Throwable except = null;

	ParserModule(String moduleName) {
		this.moduleName = moduleName;
		if(moduleName.contains("::"))
			this.name = moduleName.substring(moduleName.lastIndexOf(":") + 1,
					moduleName.length());
		else
			this.name = moduleName;
		this.job = new GeneratorJob("Generating " + name + " parser");
	}

	public synchronized IGTD getParser() {
		System.out.println("getParser start for " + moduleName);
		checkForUpdate();
		if(parser == null) {
			runGenerator();
		}

		try {
			System.out.println("getParser end for " + moduleName);
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

	private ParserGenerator getParserGenerator() {
		if(parserGenerator == null) {
			long currentTimeMillis = System.currentTimeMillis();
			parserGenerator = new ParserGenerator(stderr,
					evaluator.getClassLoaders(), evaluator.getValueFactory());
			System.out.printf("loaded parser generator in %d millis\n",
					System.currentTimeMillis() - currentTimeMillis);
		}
		return parserGenerator;
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
		}
	}

	private class GeneratorJob extends Job {

		public GeneratorJob(String jobname) {
			super(jobname);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			monitor.beginTask("Generating parser for " + name, 16);
			writerMonitor.setMonitor(monitor);
			try {
				if(uri == null) {
					monitor.subTask("Loading " + moduleName);
					evaluator.doImport(moduleName);
					uri = evaluator.getHeap().getModuleURI(moduleName);
					// TODO: this should really be done *before* doImport(), in
					// case the grammar is changed between import and
					// getLastModified()
					lastModified = getLastModified();
				}
				else {
					monitor.subTask("Reloading " + moduleName);
					lastModified = getLastModified();
					evaluator.reloadModules(Collections.singleton(moduleName),
							evaluator.getHeap().getModuleURI(moduleName));
				}
				monitor.worked(1);
				if(monitor.isCanceled())
					return Status.CANCEL_STATUS;
				ISet productions = evaluator.getCurrentModuleEnvironment()
						.getProductions();

				// see if Rascal has a cached parser for this set of productions
				// TODO: any point in this in the presence of multiple
				// evaluators?
				// yes -- Rascal keeps track of productions and can tell if they
				// have changed
				// no need to duplicate that work here
				monitor.subTask("Checking for cached parser");
				parser = evaluator.getHeap().getObjectParser(moduleName,
						productions);
				monitor.worked(1);

				if(monitor.isCanceled())
					return Status.CANCEL_STATUS;

				if(parser == null) {
					String parserName = moduleName.replaceAll("::", ".");
					monitor.subTask("Generating parser " + parserName);

					ParserGenerator pg = getParserGenerator();
					long currentTimeMillis = System.currentTimeMillis();
					parser = pg.getParser(evaluator.getValueFactory()
							.sourceLocation(uri), parserName, productions);
					System.out.printf("generated parser for %s in %d millis\n",
							moduleName, System.currentTimeMillis()
									- currentTimeMillis);
					evaluator.getHeap().storeObjectParser(moduleName,
							productions, parser);
					monitor.worked(1);
				}
				else
					monitor.worked(14);

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
						monitor.subTask(line);
						if(workPerLine > 0)
							monitor.worked(workPerLine);
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
