package org.magnolialang.util.syntax.generators;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

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
import org.magnolialang.infra.Infra;
import org.magnolialang.rascal.IGrammarListener;
import org.magnolialang.rascal.RascalInterpreter;
import org.magnolialang.terms.TermFactory;
import org.rascalmpl.parser.gtd.IGTD;

public class GrammarInfoGenerator implements IGrammarListener {

	@Override
	public int getRequires() {
		return IGrammarListener.REQUIRE_GRAMMAR;
	}


	@Override
	public Job getJob(final String name, final String moduleName, final URI uri, final IConstructor grammar, Class<IGTD<IConstructor, IConstructor, ISourceLocation>> parser, PrintWriter out) {
		try {
			long lastMod = Infra.getResolverRegistry().lastModified(uri);
			URI infoFile = new URI(uri.getScheme(), uri.getAuthority(), getFileName(uri, "Info.pbf"), null);

			URI astFile = new URI(uri.getScheme(), uri.getAuthority(), getFileName(uri, "AST.rsc"), null);

			URI ppFile = new URI(uri.getScheme(), uri.getAuthority(), getFileName(uri, "PP.rsc"), null);

			if(Infra.getResolverRegistry().lastModified(infoFile) >= lastMod && Infra.getResolverRegistry().lastModified(astFile) >= lastMod
					&& Infra.getResolverRegistry().lastModified(ppFile) >= lastMod)
				return null;
		}
		catch(IOException e1) { // NOPMD by anya on 1/5/12 5:41 AM
		}
		catch(URISyntaxException e) { // NOPMD by anya on 1/5/12 5:41 AM
		}

		return new GeneratorJob("Generating Grammar info", grammar, uri, moduleName, name);

	}


	protected static String getFileName(URI uri, String suffix) {
		return uri.getPath().substring(0, uri.getPath().lastIndexOf('.')) + suffix;
	}


	private static final class GeneratorJob extends Job {
		private final IConstructor	grammar;
		private final URI			uri;
		private final String		moduleName;
		private final String		name;


		GeneratorJob(String name, IConstructor grammar, URI uri, String moduleName, String name2) {
			super(name);
			this.grammar = grammar;
			this.uri = uri;
			this.moduleName = moduleName;
			this.name = name2;
		}


		@Override
		protected IStatus run(IProgressMonitor monitor) {
			monitor.beginTask(name, IProgressMonitor.UNKNOWN);

			ITuple infoTuple = (ITuple) RascalInterpreter.getInstance().call("grammar2info", "import org::magnolialang::util::syntax::generators::GrammarInfoGenerator;", grammar);

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


		@Override
		public boolean belongsTo(Object obj) {
			return obj == MagnoliaPlugin.JOB_FAMILY_MAGNOLIA;
		}


		private void saveGrammarInfo(IValue info) {
			String outFile = getFileName(uri, "Info.pbf");

			try {
				OutputStream stream = new FileOutputStream(outFile);
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


		private void saveAstModule(IValue str) {
			String astModule = "module " + moduleName + "AST\n" + ((IString) str).getValue();

			String outFile = getFileName(uri, "AST.rsc");

			try {
				PrintStream stream = new PrintStream(outFile, "UTF-8");
				stream.print(astModule);
				stream.close();
			}
			catch(FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch(UnsupportedEncodingException e) {
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

			String outFile = getFileName(uri, "PP.rsc");

			try {
				PrintStream stream = new PrintStream(outFile, "UTF-8");
				stream.print(ppModule);
				stream.close();
			}
			catch(FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch(UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
