package org.nuthatchery.pica.parsergen;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.jdt.annotation.Nullable;
import org.nuthatchery.pica.ConsolePicaInfra;
import org.nuthatchery.pica.Pica;
import org.nuthatchery.pica.rascal.EvaluatorFactory;
import org.nuthatchery.pica.rascal.ISearchPathProvider;
import org.nuthatchery.pica.resources.ILanguage;
import org.nuthatchery.pica.resources.IProjectManager;
import org.nuthatchery.pica.resources.IWorkspaceConfig;
import org.nuthatchery.pica.resources.handles.IFileHandle;
import org.nuthatchery.pica.resources.managed.IManagedCodeUnit;
import org.nuthatchery.pica.resources.storage.IStorage;
import org.nuthatchery.pica.terms.TermFactory;
import org.rascalmpl.debug.IRascalMonitor;
import org.rascalmpl.interpreter.ConsoleRascalMonitor;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.parser.ParserGenerator;
import org.rascalmpl.parser.gtd.IGTD;
import org.rascalmpl.parser.gtd.result.action.IActionExecutor;
import org.rascalmpl.parser.gtd.result.action.VoidActionExecutor;
import org.rascalmpl.uri.URIUtil;
import org.rascalmpl.values.uptr.ITree;

import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.IMap;
import io.usethesource.vallang.ISourceLocation;
import io.usethesource.vallang.IValue;
import io.usethesource.vallang.IValueFactory;
import io.usethesource.vallang.io.old.BinaryValueWriter;

public class GenerateParser {
	private static final EvaluatorFactory evaluatorFactory = new EvaluatorFactory(new ISearchPathProvider() {

		@Override
		public Collection<ClassLoader> additionalClassLoaders() {
			return Arrays.asList(getClass().getClassLoader());
		}


		@Override
		public void addRascalSearchPaths(Evaluator evaluator) {
			// TODO Auto-generated method stub

		}
	});
	public static final String parserPackageName = "org.rascalmpl.java.parser.object";


	public static void main(String[] args) throws IOException {
		if(args.length != 2) {
			System.err.println("Usage: java org.nuthatchery.pica.parsergen.GenerateParser <input_dir/grammar>.rsc <output_dir/>");
			System.exit(1);
		}

		Pica.set(new ConsolePicaInfra(new IWorkspaceConfig() {
			@Override
			public Collection<String> getActiveNatures() {
				return Collections.emptyList();
			}


			@Override
			public void initCompiler() {
			}


			@Override
			public IManagedCodeUnit makePackage(IProjectManager manager, IFileHandle res, @Nullable IStorage storage, Object id, ILanguage lang) {
				throw new UnsupportedOperationException();
			}

		}));

		GenerateParser pgen = new GenerateParser(new ConsoleRascalMonitor(), args[0].replace(File.separator, "::"), args[1]);
		pgen.generateParser();
	}

	protected final IRascalMonitor rm;
	protected final ParserGenerator parserGenerator;
	protected final Evaluator evaluator = evaluatorFactory.makeEvaluator();
	protected final IValueFactory vf = evaluator.getValueFactory();
	// protected final JavaBridge bridge = new JavaBridge(evaluator.getClassLoaders(), vf, evaluator.getConfiguration());
	protected final String grammarModuleName;
	protected final URI fileURI;
	protected final URI moduleURI;
	protected final String grammarName;


	protected final String outputDir;


	public GenerateParser(IRascalMonitor rm, String grammarModuleName, String outputDir) {
		this.grammarModuleName = grammarModuleName;
		this.moduleURI = URIUtil.createRascalModule(grammarModuleName);
		this.fileURI = evaluator.getRascalResolver().resolveModule(grammarModuleName).getURI();
		this.rm = rm;
		this.outputDir = outputDir;
		rm.startJob("Loading parser generator", 100);
		this.parserGenerator = new ParserGenerator(rm, evaluator.getStdErr(), evaluator.getClassLoaders(), evaluator.getValueFactory(), evaluator.getConfiguration());
		rm.endJob(true);
		if(grammarModuleName.contains("::")) {
			this.grammarName = grammarModuleName.substring(grammarModuleName.lastIndexOf(':') + 1, grammarModuleName.length());
		}
		else {
			this.grammarName = grammarModuleName;
		}

	}


	private void generateParser() throws IOException {
		rm.startJob("Generating parser for " + grammarModuleName, 90, 135);

		rm.todo(105);
		// writerMonitor.setMonitor(monitor);

		rm.event("Loading grammar", 5);// 0.5s
		evaluator.doImport(rm, grammarModuleName);

		IMap prodmap = evaluator.getCurrentModuleEnvironment().getSyntaxDefinition();

		rm.event("Importing and normalizing grammar:" + grammarModuleName, 30);
		// TODO: or getGrammarFromModules()?
		IConstructor grammar = parserGenerator.getExpandedGrammar(rm, grammarModuleName, prodmap);

		rm.event("Generating parser", 100);// 62s,
		// 13msg

		Class<IGTD<IConstructor, ITree, ISourceLocation>> parserClass = parserGenerator.getNewParser(rm, TermFactory.vf.sourceLocation(fileURI), grammarModuleName, grammar);

		if(parserClass != null) {
			saveParserInfo(grammarName, grammar);
			saveParser(parserClass, grammarName);
		}
		else {
			throw new RuntimeException("Failed to create parser");
		}
		rm.endJob(true);
	}


	public IActionExecutor<ITree> getActionExecutor() {
		return new VoidActionExecutor<ITree>();
	}


	private void saveParser(Class<IGTD<IConstructor, ITree, ISourceLocation>> cls, String name) throws IOException {
		rm.startJob("Saving parser classes to disk");
		FileOutputStream stream = null;
		try {
			String path = outputDir + File.separator + name + ".jar";
			stream = new FileOutputStream(path);
			throw new UnsupportedOperationException("parserGenerator.saveToJar(cls, stream)");
		}
		finally {
			if(stream != null) {
				stream.close();
			}
			rm.endJob(true);
		}
	}


	private void saveParserInfo(String name, IConstructor grammar) throws IOException {
		rm.startJob("Saving parser information");
		try {
			String fileName = outputDir + File.separator + name + "Grammar.pbf";
			IValue value = vf.tuple(vf.sourceLocation(moduleURI), grammar);
			BinaryValueWriter.writeValueToFile(value, new File(fileName), evaluator.getCurrentEnvt().getStore());
		}
		finally {
			rm.endJob(true);
		}
	}

}
