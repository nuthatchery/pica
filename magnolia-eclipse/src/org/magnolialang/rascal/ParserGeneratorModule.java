package org.magnolialang.rascal;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.imp.pdb.facts.*;
import org.magnolialang.Config;
import org.magnolialang.eclipse.MagnoliaPlugin;
import org.rascalmpl.eclipse.nature.RascalMonitor;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.asserts.ImplementationError;
import org.rascalmpl.interpreter.utils.JavaBridge;
import org.rascalmpl.parser.gtd.IGTD;
import org.rascalmpl.parser.gtd.result.action.IActionExecutor;
import org.rascalmpl.parser.gtd.result.action.VoidActionExecutor;

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
	private IConstructor grammar = null;
	// private ISet prodSet = null;
	private static final String packageName = "org.rascalmpl.java.parser.object";
	private boolean generatorLoaded = false;
	private boolean moduleImported = false;

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
		if(parser == null)
			throw new ImplementationError("Failed to create parser");
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

	public IConstructor getGrammar() {
		return grammar;
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
		return new VoidActionExecutor();// evaluator, (IParserInfo) parser);
	}

	private long getGrammarLastModified() {
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
				&& (lastModified == 0 || getGrammarLastModified() > lastModified)) {
			parser = null;
			grammar = null;
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
			rm.startJob("Loading parser for " + name, 0, 100);
			try {
				String parserName = moduleName.replaceAll("::", ".");
				String normName = parserName.replaceAll("\\.", "_");
				List<Job> jobs = new ArrayList<Job>();

				// try to load from disk
				if(uri == null) {
					grammar = loadParserInfo(normName);
					if(grammar != null) {
						jobs = runJobs(jobs, IGrammarListener.REQUIRE_GRAMMAR);

						loadParser(packageName, normName,
								getGrammarLastModified());
						if(parser != null) {
							// evaluator.getHeap().storeObjectParser(moduleName,
							// productions, parser);
						}
						else {
							uri = null;
						}
					}
				}

				// regenerate or use rascal's cache
				if(parser == null)
					jobs = generateParser(jobs, normName);
				rm.todo(3);

				jobs = runJobs(jobs, IGrammarListener.REQUIRE_PARSER);

				rm.event("Waiting for subtasks", 3);
				for(Job j : jobs)
					j.join();

				return Status.OK_STATUS;
			}
			catch(Exception e) {
				except = e;
				return Status.CANCEL_STATUS;
			}
			finally {
				rm.endJob(true);

			}
		}

		private List<Job> generateParser(List<Job> jobs, String normName)
				throws IOException {
			rm.startJob("Generating parser for " + name, 90, 118);

			if(!generatorLoaded) {
				rm.startJob("Loading parser generator", 30, 140);
				loadGenerator();
				rm.endJob(true);
			}
			rm.todo(85);
			// writerMonitor.setMonitor(monitor);

			rm.event("Loading grammar", 5); // 0.5s
			if(!moduleImported) {
				evaluator.doImport(rm, moduleName);
				uri = evaluator.getHeap().getModuleURI(moduleName);
				// TODO: this should really be done *before* doImport(),
				// in
				// case the grammar is changed between import and
				// getLastModified()
				lastModified = getGrammarLastModified();
				moduleImported = true;
			}
			else {
				lastModified = getGrammarLastModified();
				evaluator.reloadModules(rm, Collections.singleton(moduleName),
						evaluator.getHeap().getModuleURI(moduleName));
			}

			IMap prodmap = evaluator.getCurrentModuleEnvironment()
					.getSyntaxDefinition();

			// see if Rascal has a cached parser for this set of
			// productions
			// TODO: any point in this in the presence of multiple
			// evaluators?
			// yes -- Rascal keeps track of productions and can tell if
			// they
			// have changed
			// no need to duplicate that work here
			rm.event("Checking for cached parser", 1); // 0s
			// parser = evaluator.getHeap().getObjectParser(moduleName,
			// productions);

			if(parser == null) {

				rm.event("Importing and normalizing grammar", 5); // 5s
				grammar = getGrammar(moduleName, prodmap);

				// rm.event("Getting grammar productions", 4); // 4s
				/*
				 * prodSet = (ISet) RascalInterpreter.getInstance().call(
				 * "astProductions", "import lang::rascal::syntax::ASTGen;",
				 * grammar);
				 */
				jobs = runJobs(jobs, IGrammarListener.REQUIRE_GRAMMAR);

				rm.event("Generating java source code for parser", 62); // 62s,
				// 13msg
				IString classString = (IString) evaluator.call(rm,
						"generateObjectParser", vf.string(packageName),
						vf.string(normName), grammar);

				rm.event("compiling generated java code", 3); // 3s
				parser = bridge.compileJava(uri, packageName + "." + normName,
						classString.getValue());

				if(parser != null) {
					saveParserInfo(normName);
					saveParser(parser, packageName, normName);
				}
			}
			else
				rm.todo(0);
			// if(parser != null)
			// evaluator.getHeap().storeObjectParser(moduleName, productions,
			// parser);
			rm.endJob(true);
			return jobs;

		}

		private void saveParserInfo(String normName) {
			rm.startJob("Saving parser information");
			try {
				String fileName = normName + ".pbf";
				IValue value = vf.tuple(vf.sourceLocation(uri), grammar);
				Config.saveData(fileName, value, evaluator.getCurrentEnvt()
						.getStore());
			}
			catch(IOException e) {
				MagnoliaPlugin.getInstance().logException(
						"Failed to save parser info", e);
			}
			finally {
				rm.endJob(true);
			}
		}

		private IConstructor loadParserInfo(String normName) {
			try {
				rm.startJob("Loading stored parser information");
				IValue value = Config.loadData(normName + ".pbf", vf, evaluator
						.getCurrentEnvt().getStore());
				if(value != null && value instanceof ITuple) {
					ITuple tup = (ITuple) value;
					uri = ((ISourceLocation) tup.get(0)).getURI();
					IConstructor grammar = ((IConstructor) tup.get(1));
					return grammar;
				}
			}
			catch(IOException e) {
			}
			catch(Exception e) {
				MagnoliaPlugin.getInstance().logException(
						"Error loading parser info (probably just stale data)",
						e);
			}
			finally {
				rm.endJob(true);
			}

			return null;
		}

		private void saveParser(Class<?> cls, String packageName, String clsName) {
			rm.startJob("Saving parser classes to disk");
			try {
				IPath path = MagnoliaPlugin.getInstance().getStateLocation();
				path = path.append(clsName + ".jar");
				bridge.saveToJar(cls, path.toOSString());
			}
			catch(IOException e) {
				MagnoliaPlugin.getInstance().logException(
						"Failed to save parser", e);
			}
			finally {
				rm.endJob(true);
			}
		}

		@SuppressWarnings("unchecked")
		private void loadParser(String packageName, String clsName,
				long ifNewerThan) {
			String jarFileName = clsName + ".jar";
			IPath path = MagnoliaPlugin.getInstance().getStateLocation();
			long modTime = new java.io.File(path.append(jarFileName)
					.toOSString()).lastModified();
			if(modTime >= ifNewerThan) {
				rm.startJob("Loading parser classes from disk");
				try {
					List<ClassLoader> loaders = evaluator.getClassLoaders();
					for(ClassLoader l : loaders) {
						try {
							URLClassLoader loader = new URLClassLoader(
									new URL[] { new URL("file://"
											+ path.append(jarFileName)
													.toString()) }, l);
							parser = (Class<IGTD>) loader.loadClass(packageName
									+ "." + clsName);
							lastModified = modTime;
							break;
						}
						catch(ClassCastException e) {
							// e.printStackTrace();
						}
						catch(NoClassDefFoundError e) {
							// e.printStackTrace();
						}
						catch(ClassNotFoundException e) {
							// e.printStackTrace();
						}
						catch(MalformedURLException e) {
						}
					}
				}
				finally {
					rm.endJob(true);
				}
			}
		}

		private List<Job> runJobs(List<Job> jobs, int required) {
			for(IGrammarListener l : RascalParser.getGrammarListeners(name,
					required)) {
				Job j = l.getJob(name, moduleName, uri, grammar, parser,
						evaluator.getStdErr());
				if(j != null) {
					j.schedule();
					jobs.add(j);
				}
			}
			return jobs;
		}

		private void loadGenerator() {
			evaluator.doImport(rm, "lang::rascal::grammar::ParserGenerator");
			evaluator
					.doImport(rm, "lang::rascal::grammar::definition::Modules");
			// evaluator.doImport(rm, "lang::rascal::syntax::Definition");
			// evaluator.doImport(rm, "lang::rascal::syntax::Assimilator");
			generatorLoaded = true;
		}

		public IConstructor getGrammar(String main, IMap definition) {
			return (IConstructor) evaluator.call(rm, "modules2grammar",
					vf.string(main), definition);
		}

	}
}