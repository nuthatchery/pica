package org.magnolialang.util.syntax.generators;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.ITuple;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.io.BinaryValueWriter;
import org.magnolialang.eclipse.MagnoliaPlugin;
import org.magnolialang.errors.ImplementationError;
import org.magnolialang.infra.Infra;
import org.magnolialang.rascal.IGrammarListener;
import org.magnolialang.terms.TermFactory;
import org.rascalmpl.parser.gtd.IGTD;
import org.rascalmpl.uri.URIUtil;

public class GrammarInfoGenerator implements IGrammarListener {

	protected static URI getFileName(URI uri, String suffix) {
		uri = org.eclipse.core.runtime.URIUtil.removeFileExtension(uri);
		try {
			uri = URIUtil.changePath(uri, uri.getPath() + suffix);
			System.out.println("getFileName: " + uri);
			return uri;
		}
		catch(URISyntaxException e) {
			throw new ImplementationError("Unexpected error", e);
		}
	}


	@Override
	public Job getJob(final String name, final String moduleName, URI uri, final IConstructor grammar, Class<IGTD<IConstructor, IConstructor, ISourceLocation>> parser, PrintWriter out) {
		try {
			long lastMod = 0;
			if(uri != null) {
				Infra.getResolverRegistry().lastModified(uri);
			}
			else {
				uri = URIUtil.createFile(Infra.getDataFile(moduleName + ".mg").getAbsolutePath());
			}
			URI infoFile = getFileName(uri, "Info.pbf");

			URI astFile = getFileName(uri, "AST.rsc");

			URI ppFile = getFileName(uri, "PP.rsc");

			if(lastMod != 0 && Infra.getResolverRegistry().lastModified(infoFile) >= lastMod && Infra.getResolverRegistry().lastModified(astFile) >= lastMod && Infra.getResolverRegistry().lastModified(ppFile) >= lastMod) {
				return null;
			}
		}
		catch(IOException e1) { // NOPMD by anya on 1/5/12 5:41 AM
		}
		catch(URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return new GeneratorJob("Generating Grammar info", grammar, uri, moduleName, name);

	}


	@Override
	public int getRequires() {
		return IGrammarListener.REQUIRE_GRAMMAR;
	}


	private static final class GeneratorJob extends Job {
		private final IConstructor grammar;
		private final URI uri;
		private final String moduleName;
		private final String name;


		GeneratorJob(String name, IConstructor grammar, URI uri, String moduleName, String name2) {
			super(name);
			this.grammar = grammar;
			this.uri = uri;
			this.moduleName = moduleName;
			this.name = name2;
		}


		@Override
		public boolean belongsTo(Object obj) {
			return obj == MagnoliaPlugin.JOB_FAMILY_MAGNOLIA;
		}


		private void saveAstModule(IValue str) {
			String astModule = "module " + moduleName + "AST\n" + ((IString) str).getValue();

			try {
				URI outFile = getFileName(uri, "AST.rsc");
				PrintStream stream = new PrintStream(Infra.getResolverRegistry().getOutputStream(outFile, false), false, "UTF-8");
				try {
					stream.print(astModule);
				}
				finally {
					stream.close();
				}
			}
			catch(FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch(UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch(IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


		private void saveGrammarInfo(IValue info) {

			try {
				URI outFile = getFileName(uri, "Info.pbf");
				OutputStream stream = Infra.getResolverRegistry().getOutputStream(outFile, false);
				try {
					new BinaryValueWriter().write(info, stream, TermFactory.ts);
				}
				finally {
					stream.close();
				}
			}
			catch(FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch(IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


		private void savePPModule(IValue str) {
			String ppModule = "// WARNING: this file is autogenerated! just copy it and adapt to your needs\n" //
					+ "module " + moduleName + "PP\n" //
					+ "import org::magnolialang::pgf::Token;\n" //
					+ "import org::magnolialang::pgf::Stream;\n" //
					+ "import org::magnolialang::pgf::AstToStream;\n" //
					+ "import " + moduleName + "AST;\n" //
					+ "public default Stream[Token] pp(AST ast, Stream[Token] stream) {\n" //
					+ "  return stream;\n" //
					+ "}\n" //
					+ ((IString) str).getValue();

			try {
				URI outFile = getFileName(uri, "PP.rsc");
				PrintStream stream = new PrintStream(Infra.getResolverRegistry().getOutputStream(outFile, false), false, "UTF-8");
				try {
					stream.print(ppModule);
				}
				finally {
					stream.close();
				}
			}
			catch(FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch(UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch(IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


		@Override
		protected IStatus run(IProgressMonitor monitor) {
			monitor.beginTask(name, IProgressMonitor.UNKNOWN);

			ITuple infoTuple = (ITuple) Infra.get().getEvaluatorPool("Grammar Info Generator", Arrays.asList("org::magnolialang::util::syntax::generators::GrammarInfoGenerator")).call("grammar2info", grammar);

			try {
				saveGrammarInfo(infoTuple.get(0));
				saveAstModule(infoTuple.get(1));
				savePPModule(infoTuple.get(2));
			}
			finally {
				monitor.done();
			}
			return Status.OK_STATUS;
		}
	}

}
