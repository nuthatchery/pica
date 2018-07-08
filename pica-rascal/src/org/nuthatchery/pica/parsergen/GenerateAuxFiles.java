/**************************************************************************
 * Copyright (c) 2011-2013 Anya Helene Bagge
 * Copyright (c) 2011-2013 University of Bergen
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. See http://www.gnu.org/licenses/
 *
 *
 * See the file COPYRIGHT for more information.
 *
 * Contributors:
 * * Anya Helene Bagge
 *
 *************************************************************************/
package org.nuthatchery.pica.parsergen;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.IString;
import io.usethesource.vallang.ITuple;
import io.usethesource.vallang.IValue;
import io.usethesource.vallang.io.old.BinaryValueReader;
import io.usethesource.vallang.io.old.BinaryValueWriter;
import org.eclipse.jdt.annotation.Nullable;
import org.nuthatchery.pica.ConsolePicaInfra;
import org.nuthatchery.pica.Pica;
import org.nuthatchery.pica.rascal.EvaluatorFactory;
import org.nuthatchery.pica.rascal.ISearchPathProvider;
import org.nuthatchery.pica.resources.ILanguage;
import org.nuthatchery.pica.resources.managed.IManagedCodeUnit;
import org.nuthatchery.pica.resources.IProjectManager;
import org.nuthatchery.pica.resources.IWorkspaceConfig;
import org.nuthatchery.pica.resources.handles.IFileHandle;
import org.nuthatchery.pica.resources.storage.IStorage;
import org.nuthatchery.pica.terms.TermFactory;
import org.rascalmpl.interpreter.Evaluator;

public class GenerateAuxFiles {
	private static final EvaluatorFactory evaluatorFactory = new EvaluatorFactory(new ISearchPathProvider() {

		@SuppressWarnings("null")
		@Override
		public Collection<ClassLoader> additionalClassLoaders() {
			return Arrays.asList(getClass().getClassLoader());
		}


		@Override
		public void addRascalSearchPaths(Evaluator evaluator) {
			// TODO Auto-generated method stub

		}
	});


	@SuppressWarnings("null")
	public static void main(String[] args) throws IOException {
		if(args.length != 3) {
			System.err.println("Usage: java org.nuthatchery.pica.parsergen.GenerateParser <basename> <input_dir/> <output_dir/>");
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
		GenerateAuxFiles generator = new GenerateAuxFiles(args[0], args[1], args[2]);
		generator.generate();
	}

	private final String moduleName;
	private String outDir;
	private String baseName;


	private String srcDir;


	@SuppressWarnings("null")
	GenerateAuxFiles(String baseName, String srcDir, String outDir) {
		this.baseName = baseName;
		this.outDir = outDir;
		this.srcDir = srcDir;
		this.moduleName = baseName.replace(File.separator, "::");

	}


	@SuppressWarnings("null")
	protected void generate() throws IOException {
		Evaluator eval = evaluatorFactory.makeEvaluator();
		eval.doImport(null, "org::nuthatchery::pica::parsergen::GrammarInfoGenerator");
		IConstructor grammar = loadGrammar(eval);

		ITuple infoTuple = (ITuple) eval.call("grammar2info", grammar);

		saveGrammarInfo(infoTuple.get(0));
		saveAstModule(infoTuple.get(1));
		savePPModule(infoTuple.get(2));
		saveJavaPatterns(infoTuple.get(3));
		saveJavaVisitor(infoTuple.get(4));
		saveJavaPP(infoTuple.get(5));
	}


	private String getFileName(String dir, String name, String suffix) {
		String fileName = dir + File.separator + name + suffix;
		System.err.println("Filename: " + fileName);
		return fileName;
	}


	@SuppressWarnings("null")
	private IConstructor loadGrammar(Evaluator eval) throws IOException {
		ITuple tuple = (ITuple) BinaryValueReader.readValueFromFile(eval.getValueFactory(), eval.getCurrentEnvt().getStore(), new File(getFileName(srcDir, baseName, "Grammar.pbf")));
		return (IConstructor) tuple.get(1);
	}


	private void saveAstModule(IValue str) throws IOException {
		String astModule = "module " + moduleName + "AST\n" + ((IString) str).getValue();

		String outFile = getFileName(outDir, baseName, "AST.rsc");

		try (PrintStream stream = new PrintStream(new FileOutputStream(outFile), false, "UTF-8")) {
			stream.print(astModule);
		}
	}


	private void saveGrammarInfo(IValue info) throws IOException {

		String outFile = getFileName(outDir, baseName, "Info.pbf");
		try (OutputStream stream = new FileOutputStream(outFile)) {
			new BinaryValueWriter().write(info, stream, TermFactory.ts);
		}
	}


	private void saveJavaPatterns(IValue str) throws IOException {
		String clsName = moduleName.substring(moduleName.lastIndexOf("::") + 2);
		String pkgName = moduleName.substring(0, moduleName.lastIndexOf("::")).replace("::", ".");

		String javaStr = "// WARNING: this file is autogenerated! just copy it and adapt to your needs\n"//
				+ "package " + pkgName + ";\n\n"//
				+ "import nuthatch.rascal.pattern.impl.ValuesPatternFactory;\n"//
				+ "import io.usethesource.vallang.IValue;\n"//
				+ "import io.usethesource.vallang.IString;\n"//
				+ "import io.usethesource.vallang.type.Type;\n"//
				+ "import org.nuthatchery.pica.terms.TermFactory;\n"//
				+ "import nuthatch.pattern.Pattern;"//
				+ "public class " + clsName + "Patterns {\n"//
				+ "\tprotected static final ValuesPatternFactory pf = ValuesPatternFactory.getInstance();\n"//
				+ "\n"//
				+ "\t@SafeVarargs public static Pattern<IValue, Type> seq(Pattern<IValue, Type>... args) {\n"//
				+ "\t\treturn pf.cons(TermFactory.Cons_Seq, pf.list(args));\n"//
				+ "\t}\n\n"//
				+ "\tpublic static Pattern<IValue, Type> leaf(Pattern<IValue, Type> s) {\n"//
				+ "\t\treturn pf.cons(TermFactory.Cons_Leaf, s);\n"//
				+ "\t}\n\n"//
				+ "\tpublic static Pattern<IValue, Type> leaf(IString s) {\n"//
				+ "\t\treturn pf.cons(TermFactory.Cons_Leaf, pf.string(s));\n"//
				+ "\t}\n\n"//
				+ "\tpublic static Pattern<IValue, Type> leaf(String s) {\n"//
				+ "\t\treturn pf.cons(TermFactory.Cons_Leaf, pf.string(TermFactory.vf.string(s)));\n"//
				+ "\t}\n\n"//
				+ ((IString) str).getValue()//
				+ "}\n";

		String outFile = getFileName(outDir, baseName, "Patterns.java");
		try (PrintStream stream = new PrintStream(new FileOutputStream(outFile), false, "UTF-8")) {
			stream.print(javaStr);
		}
	}


	private void saveJavaPP(IValue str) throws IOException {
		String clsName = moduleName.substring(moduleName.lastIndexOf("::") + 2);
		String pkgName = moduleName.substring(0, moduleName.lastIndexOf("::")).replace("::", ".");

		String javaStr = "// WARNING: this file is autogenerated! just copy it and adapt to your needs\n"//
				+ "package " + pkgName + ";\n\n"//
				+ "import io.usethesource.vallang.IValue;\n"//
				+ "import io.usethesource.vallang.IString;\n"//
				+ "import io.usethesource.vallang.type.Type;\n"//
				+ "import org.nuthatchery.pica.terms.TermFactory;\n"//
				+ "import nuthatch.library.ActionFactory;\n"//
				+ "import nuthatch.library.FactoryFactory;\n"//
				+ "import nuthatch.rascal.adapter.ValuesCursor;\n"//
				+ "import nuthatch.rascal.adapter.ValuesWalker;\n"//
				+ "import nuthatch.rascal.pattern.impl.ValuesPatternFactory;\n"//
				+ "import nuthatch.rascal.pattern.ValuesBuildContext;\n"//
				+ "import org.nuthatchery.pgf.trees.PPBuilder;\n"//
				+ "import org.nuthatchery.pgf.trees.PrettyPrinter;\n"//
				+ "import org.nuthatchery.pgf.trees.TreeToStream;\n"//
				+ "import org.nuthatchery.pgf.plumbing.ForwardStream;"//
				+ "import org.nuthatchery.pgf.tokens.Token;"//
				+ "import org.nuthatchery.pgf.tokens.DataToken;"//
				+ "import org.nuthatchery.pgf.config.TokenizerConfig;\n"//
				+ "import static org.nuthatchery.pgf.trees.PPUtil.*;\n"//
				+ "import static nuthatch.rascal.pattern.StaticValuesPatternFactory._;\n"//
				+ "import static " + pkgName + "." + clsName + "Patterns.*;\n"//
				+ "\n"//
				+ "public class " + clsName + "PP {\n"//
				+ "\tprotected static final ActionFactory<IValue, Type, ValuesCursor, ValuesWalker> af = FactoryFactory.getActionFactory(IValue.class, Type.class, ValuesCursor.class, ValuesWalker.class);\n" + "\tprotected static final ValuesPatternFactory pf = ValuesPatternFactory.getInstance();\n"//
				+ "\tprotected static final ValuesBuildContext context = new ValuesBuildContext(TermFactory.vf, TermFactory.ts);\n"//
				+ "\n"//
				+ "\tpublic static PrettyPrinter<IValue, Type> makePrinter() {\n"//
				+ "\t\tfinal TokenizerConfig config = (TokenizerConfig) new Object(); /* TODO: fill in config */\n\n"//
				+ "\t\tPPBuilder<IValue, Type> builder = new PPBuilder<>(config, context);\n"//
				+ "\n"//
				+ "\t\tbuilder.addList(leaf(_), PPBuilder.custom(new TreeToStream<IValue>() {\n"//
				+ "\t\t\t@Override\n" + "\t\t\tpublic void printTree(IValue tree, ForwardStream<Token> output) {\n"//
				+ "\t\t\t\tif(tree instanceof IString) {\n"//
				+ "\t\t\t\t\tString s = ((IString) tree).getValue();\n"//
				+ "\t\t\t\t\toutput.put(new DataToken(s, config.getCatForLexical(s)));\n"//
				+ "\t\t\t\t}\n"//
				+ "\t\t\t}\n"//
				+ "\t\t}, 1));\n\n"//
				+ ((IString) str).getValue()//
				+ "\t\treturn builder.compile();\n"//
				+ " \t}\n"//
				+ "}\n";

		String outFile = getFileName(outDir, baseName, "PP.java");
		try (PrintStream stream = new PrintStream(new FileOutputStream(outFile), false, "UTF-8")) {
			stream.print(javaStr);
		}
	}


	private void saveJavaVisitor(IValue str) throws IOException {
		String clsName = moduleName.substring(moduleName.lastIndexOf("::") + 2);
		String pkgName = moduleName.substring(0, moduleName.lastIndexOf("::")).replace("::", ".");

		String javaStr = "// WARNING: this file is autogenerated! just copy it and adapt to your needs\n"//
				+ "package " + pkgName + ";\n\n"//
				+ "import io.usethesource.vallang.IConstructor;\n"//
				+ "public interface " + clsName + "Visitor<R, E> {\n"//
				+ ((IString) str).getValue()//
				+ "}\n";

		String outFile = getFileName(outDir, baseName, "Visitor.java");
		try (PrintStream stream = new PrintStream(new FileOutputStream(outFile), false, "UTF-8")) {
			stream.print(javaStr);
		}
	}


	private void savePPModule(IValue str) throws IOException {
		String ppModule = "// WARNING: this file is autogenerated! just copy it and adapt to your needs\n"//
				+ "module " + moduleName + "PP\n"//
				+ "import org::nuthatchery::pgf::Token;\n"//
				+ "import org::nuthatchery::pgf::Stream;\n"//
				+ "import org::nuthatchery::pgf::AstToStream;\n"//
				+ "import " + moduleName + "AST;\n"//
				+ "public default Stream[Token] pp(AST ast, Stream[Token] stream) {\n"//
				+ "  return stream;\n"//
				+ "}\n"//
				+ ((IString) str).getValue();

		String outFile = getFileName(outDir, baseName, "PP.rsc");

		try (PrintStream stream = new PrintStream(new FileOutputStream(outFile), false, "UTF-8")) {
			stream.print(ppModule);
		}
	}

}
