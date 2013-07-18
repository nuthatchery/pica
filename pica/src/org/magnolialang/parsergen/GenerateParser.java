package org.magnolialang.parsergen;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IMap;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.imp.pdb.facts.io.BinaryValueWriter;
import org.magnolialang.pica.ConsolePicaInfra;
import org.magnolialang.pica.Pica;
import org.magnolialang.resources.ILanguage;
import org.magnolialang.resources.IManagedPackage;
import org.magnolialang.resources.IResourceManager;
import org.magnolialang.resources.IWorkspaceConfig;
import org.magnolialang.resources.storage.IStorage;
import org.rascalmpl.interpreter.ConsoleRascalMonitor;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.IRascalMonitor;
import org.rascalmpl.interpreter.utils.JavaBridge;
import org.rascalmpl.parser.ParserGenerator;
import org.rascalmpl.parser.gtd.IGTD;
import org.rascalmpl.parser.gtd.result.action.IActionExecutor;
import org.rascalmpl.parser.gtd.result.action.VoidActionExecutor;
import org.rascalmpl.uri.URIUtil;

public class GenerateParser {
	protected final IRascalMonitor rm;
	protected final ParserGenerator parserGenerator;
	public static final String parserPackageName = "org.rascalmpl.java.parser.object";
	protected final Evaluator evaluator = Pica.getEvaluatorFactory().makeEvaluator();
	protected final IValueFactory vf = evaluator.getValueFactory();
	protected final JavaBridge bridge = new JavaBridge(evaluator.getClassLoaders(), vf, evaluator.getConfiguration());
	protected final String grammarModuleName;
	protected final URI fileURI;
	protected final URI moduleURI;
	protected final String grammarName;
	protected final String outputDir;


	public GenerateParser(IRascalMonitor rm, String grammarModuleName, String outputDir) {
		this.grammarModuleName = grammarModuleName;
		this.moduleURI = URIUtil.createRascalModule(grammarModuleName);
		this.fileURI = evaluator.getRascalResolver().resolve(moduleURI);
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


	public IActionExecutor<IConstructor> getActionExecutor() {
		return new VoidActionExecutor<IConstructor>();
	}


	private void generateParser() throws IOException {
		rm.startJob("Generating parser for " + grammarModuleName, 90, 135);

		rm.todo(105);
		// writerMonitor.setMonitor(monitor);

		rm.event("Loading grammar", 5); // 0.5s
		evaluator.doImport(rm, grammarModuleName);

		IMap prodmap = evaluator.getCurrentModuleEnvironment().getSyntaxDefinition();

		rm.event("Importing and normalizing grammar:" + grammarModuleName, 30);
		IConstructor grammar = parserGenerator.getGrammar(rm, grammarModuleName, prodmap);

		rm.event("Generating parser", 100); // 62s,
		// 13msg

		Class<IGTD<IConstructor, IConstructor, ISourceLocation>> parserClass = parserGenerator.getNewParser(rm, fileURI, grammarModuleName, grammar);

		if(parserClass != null) {
			saveParserInfo(grammarName, grammar);
			saveParser(parserClass, grammarName);
		}
		else {
			throw new RuntimeException("Failed to create parser");
		}
		rm.endJob(true);
	}


	private void saveParser(Class<IGTD<IConstructor, IConstructor, ISourceLocation>> cls, String name) throws IOException {
		rm.startJob("Saving parser classes to disk");
		FileOutputStream stream = null;
		try {
			String path = outputDir + File.separator + name + ".jar";
			stream = new FileOutputStream(path);
			parserGenerator.saveToJar(cls, stream);
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


	public static void main(String[] args) throws IOException {
		if(args.length != 2) {
			System.err.println("Usage: java org.magnolialang.parsergen.GenerateParser <input_dir/grammar>.rsc <output_dir/>");
			System.exit(1);
		}

		Pica.set(new ConsolePicaInfra(new IWorkspaceConfig() {

			@Override
			public Collection<String> getActiveNatures() {
				return Collections.EMPTY_LIST;
			}


			@Override
			public ClassLoader getParserClassLoader() {
				// TODO Auto-generated method stub
				return null;
			}


			@Override
			public void initCompiler() {
			}


			@Override
			public IManagedPackage makePackage(IResourceManager manager, IFile resource, IStorage storage, IConstructor id, ILanguage lang) {
				return null;
			}


			@Override
			public String moreRascalClassPath() {
				return null;
			}


			@Override
			public List<URI> moreRascalSearchPath() {
				return null;
			}

		}));

		GenerateParser pgen = new GenerateParser(new ConsoleRascalMonitor(), args[0].replace(File.separator, "::"), args[1]);
		pgen.generateParser();
	}

}