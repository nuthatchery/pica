package org.magnolialang.rascal;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IMap;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.ITuple;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.magnolialang.eclipse.MagnoliaPlugin;
import org.magnolialang.infra.Infra;
import org.rascalmpl.eclipse.nature.RascalMonitor;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.asserts.ImplementationError;
import org.rascalmpl.interpreter.utils.JavaBridge;
import org.rascalmpl.parser.gtd.IGTD;

@edu.umd.cs.findbugs.annotations.SuppressWarnings("IS2_INCONSISTENT_SYNC")
// TODO: maybe move this whole thing to a service thread, to avoid synchronization
public class EclipseParserGeneratorModule extends AbstractParserGeneratorModule {
	private final GeneratorJob job;

	protected long lastModified = 0;
	protected Throwable except = null;
	// private ISet prodSet = null;
	protected boolean generatorLoaded = false;
	protected boolean moduleImported = false;

	protected final URI fileURI;
	protected final Evaluator evaluator = Infra.getEvaluatorFactory().makeEvaluator();

	protected final IValueFactory vf = evaluator.getValueFactory();

	protected final JavaBridge bridge = new JavaBridge(evaluator.getClassLoaders(), vf);


	public EclipseParserGeneratorModule(String moduleName) {
		super(moduleName);
		this.fileURI = evaluator.getRascalResolver().resolve(moduleURI);
		this.job = new GeneratorJob("Generating " + name + " parser");
		evaluator.doImport(null, "Grammar");
		System.out.println("Grammar.rsc loaded");
	}


	@Override
	public synchronized void clearParserFiles() {
		String parserName = moduleName.replaceAll("::", ".");
		String normName = parserName.replaceAll("\\.", "_");
		Infra.get().removeDataFile(normName + ".pbf");
		Infra.get().removeDataFile(normName + ".jar");

		parserClass = null;
		grammar = null;
	}


	@Override
	public synchronized IGTD<IConstructor, IConstructor, ISourceLocation> getParser() {
		checkForUpdate();
		if(parserClass == null) {
			runGenerator();
		}
		if(parserClass == null) {
			throw new ImplementationError("Failed to create parser");
		}
		try {
			// should we return a new instance every time?
			return parserClass.newInstance();
		}
		catch(InstantiationException e) {
			parserClass = null;
			throw new ImplementationError(e.getMessage(), e);
		}
		catch(IllegalAccessException e) {
			parserClass = null;
			throw new ImplementationError(e.getMessage(), e);
		}
		catch(NoClassDefFoundError e) {
			parserClass = null;
			throw new ImplementationError(e.getMessage(), e);
		}

	}


	private void checkForUpdate() {
		if(parserClass != null && (lastModified == 0 || getGrammarLastModified() > lastModified)) {
			parserClass = null;
			grammar = null;
		}
	}


	@edu.umd.cs.findbugs.annotations.SuppressWarnings("RCN_REDUNDANT_NULLCHECK_OF_NULL_VALUE")
	private void runGenerator() {
		except = null;
		job.schedule(); // job may change except to non-null
		try {
			job.join();
		}
		catch(InterruptedException e) {
			e.printStackTrace();
		}
		if(except != null) {
			throw new ImplementationError(except.getMessage(), except);
		}
		if(job.getResult() == Status.CANCEL_STATUS) {
			throw new ImplementationError("Parser generator for " + name + " cancelled");
		}
	}


	protected long getGrammarLastModified() {
		long lm = 0;
		if(grammar != null) {
			try {
				lm = Infra.getResolverRegistry().lastModified(fileURI);
			}
			catch(IOException e) {
				lm = 0;
			}
		}
		return lm;
	}


	private class GeneratorJob extends Job {

		private RascalMonitor rm;


		public GeneratorJob(String jobname) {
			super(jobname);
		}


		@Override
		public boolean belongsTo(Object obj) {
			return obj == MagnoliaPlugin.JOB_FAMILY_MAGNOLIA;
		}


		public IConstructor getGrammar(String main, IMap definition) {
			return (IConstructor) evaluator.call(rm, "modules2grammar", vf.string(main), definition);
		}


		private List<Job> generateParser(List<Job> jobs, String normName) throws IOException {
			rm.startJob("Generating parser for " + name, 90, 118);

			if(!generatorLoaded) {
				rm.startJob("Loading parser generator", 30, 140);
				loadGenerator();
				rm.endJob(true);
			}
			rm.todo(85);
			// writerMonitor.setMonitor(monitor);

			rm.event("Loading grammar", 5); // 0.5s
			if(moduleImported) {
				lastModified = getGrammarLastModified();
				evaluator.reloadModules(rm, Collections.singleton(moduleName), evaluator.getHeap().getModuleURI(moduleName));
			}
			else {
				evaluator.doImport(rm, moduleName);
				// TODO: this should really be done *before* doImport(),
				// in
				// case the grammar is changed between import and
				// getLastModified()
				lastModified = getGrammarLastModified();
				moduleImported = true;
			}

			IMap prodmap = evaluator.getCurrentModuleEnvironment().getSyntaxDefinition();

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

			if(parserClass == null) {

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
				IString classString = (IString) evaluator.call(rm, "generateObjectParser", vf.string(parserPackageName), vf.string(normName), grammar);

				rm.event("compiling generated java code", 3); // 3s
				parserClass = bridge.compileJava(moduleURI, parserPackageName + "." + normName, classString.getValue());

				if(parserClass != null) {
					saveParserInfo(normName);
					saveParser(parserClass, normName);
				}
			}
			else {
				rm.todo(0);
			}
			// if(parser != null)
			// evaluator.getHeap().storeObjectParser(moduleName, productions,
			// parser);
			rm.endJob(true);
			return jobs;

		}


		private void loadGenerator() {
			evaluator.doImport(rm, "lang::rascal::grammar::ParserGenerator");
			evaluator.doImport(rm, "lang::rascal::grammar::definition::Modules");
			// evaluator.doImport(rm, "lang::rascal::syntax::Definition");
			// evaluator.doImport(rm, "lang::rascal::syntax::Assimilator");
			generatorLoaded = true;
		}


		private IConstructor loadParserInfo(String normName, long ifNewerThan) {
			try {
				rm.startJob("Loading stored parser information");
				long modTime = Infra.getDataFile(normName + ".pbf").lastModified();
				if(modTime == 0 || modTime < ifNewerThan) {
					return null;
				}
				IValue value = Infra.get().loadData(normName + ".pbf", vf, evaluator.getCurrentEnvt().getStore());
				if(value instanceof ITuple) {
					ITuple tup = (ITuple) value;
					return (IConstructor) tup.get(1);
				}
			}
			catch(IOException e) { // NOPMD by anya on 1/5/12 4:24 AM
				// ignore -- we can always just generate a new parser anyway
			}
			catch(Exception e) {
				// TODO: maybe ignore this as well?
				MagnoliaPlugin.getInstance().logException("Error loading parser info (probably just stale data)", e);
			}
			finally {
				rm.endJob(true);
			}

			return null;
		}


		private List<Job> runJobs(List<Job> jobs, int required) {
			for(IGrammarListener l : RascalParser.getGrammarListeners(required)) {
				Job j = l.getJob(name, moduleName, fileURI, grammar, parserClass, evaluator.getStdErr());
				if(j != null) {
					j.schedule();
					jobs.add(j);
				}
			}
			return jobs;
		}


		private void saveParser(Class<?> cls, String clsName) {
			rm.startJob("Saving parser classes to disk");
			try {
				IPath path = MagnoliaPlugin.getInstance().getStateLocation();
				path = path.append(clsName + ".jar");
				bridge.saveToJar(cls, path.toOSString());
			}
			catch(IOException e) {
				MagnoliaPlugin.getInstance().logException("Failed to save parser", e);
			}
			finally {
				rm.endJob(true);
			}
		}


		private void saveParserInfo(String normName) {
			rm.startJob("Saving parser information");
			try {
				String fileName = normName + ".pbf";
				IValue value = vf.tuple(vf.sourceLocation(moduleURI), grammar);
				Infra.get().saveData(fileName, value, evaluator.getCurrentEnvt().getStore());
			}
			catch(IOException e) {
				MagnoliaPlugin.getInstance().logException("Failed to save parser info", e);
			}
			finally {
				rm.endJob(true);
			}
		}


		/**
		 * Load a parser from disk.
		 * 
		 * This may fail for any number of reasons, in which case we can just
		 * ignore the error and generate a new parser.
		 * 
		 * @param packageName
		 * @param clsName
		 * @param ifNewerThan
		 */
		protected void loadParser(String packageName, String clsName, long ifNewerThan) {
			final String jarFileName = clsName + ".jar";
			final IPath path = MagnoliaPlugin.getInstance().getStateLocation();
			long modTime = new java.io.File(path.append(jarFileName).toOSString()).lastModified();
			if(modTime >= ifNewerThan) {
				rm.startJob("Loading parser classes from disk");
				try {
					List<ClassLoader> loaders = evaluator.getClassLoaders();
					for(final ClassLoader l : loaders) {
						try {
							URLClassLoader loader = AccessController.doPrivileged(new PrivilegedAction<URLClassLoader>() { // NOPMD by anya on 1/5/12 4:28 AM
								@Override
								public URLClassLoader run() {
									try {
										return new URLClassLoader(new URL[] { new URL("file://" + path.append(jarFileName).toString()) }, l);
									}
									catch(MalformedURLException e) {
										return null;
									}
								}
							});
							parserClass = (Class<IGTD<IConstructor, IConstructor, ISourceLocation>>) loader.loadClass(packageName + "." + clsName);
							lastModified = modTime;
							break;
						}
						catch(ClassCastException e) { // NOPMD by anya on 1/5/12 4:28 AM
							// e.printStackTrace();
						}
						catch(NoClassDefFoundError e) { // NOPMD by anya on 1/5/12 4:28 AM
							// e.printStackTrace();
						}
						catch(ClassNotFoundException e) { // NOPMD by anya on 1/5/12 4:28 AM
							// e.printStackTrace();
						}
					}
				}
				finally {
					rm.endJob(true);
				}
			}
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
				if(grammar == null) {
					grammar = loadParserInfo(normName, getGrammarLastModified());
					if(grammar != null) {
						jobs = runJobs(jobs, IGrammarListener.REQUIRE_GRAMMAR);

						loadParser(parserPackageName, normName, getGrammarLastModified());
						if(parserClass == null) {
							grammar = null;
						}
					}
				}

				// regenerate or use rascal's cache
				if(parserClass == null) {
					jobs = generateParser(jobs, normName);
				}
				rm.todo(3);

				jobs = runJobs(jobs, IGrammarListener.REQUIRE_PARSER);

				rm.event("Waiting for subtasks", 3);
				for(Job j : jobs) {
					j.join();
				}

				return Status.OK_STATUS;
			}
			catch(RuntimeException e) {
				except = e;
				return Status.CANCEL_STATUS;
			}
			catch(Exception e) {
				except = e;
				return Status.CANCEL_STATUS;
			}
			finally {
				rm.endJob(true);

			}
		}

	}
}