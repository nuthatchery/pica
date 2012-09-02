package org.magnolialang.resources.internal;

import static org.magnolialang.terms.TermFactory.vf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IValue;
import org.magnolialang.compiler.ICompiler;
import org.magnolialang.errors.ErrorMarkers;
import org.magnolialang.errors.ImplementationError;
import org.magnolialang.resources.ILanguage;
import org.magnolialang.resources.IManagedPackage;
import org.magnolialang.resources.IManagedResource;
import org.magnolialang.resources.IResourceManager;
import org.magnolialang.terms.TermFactory;
import org.magnolialang.terms.TermImploder;
import org.rascalmpl.interpreter.IRascalMonitor;
import org.rascalmpl.parser.gtd.exception.ParseError;

public class MagnoliaPackage extends ManagedFile implements IManagedPackage {
	private final IConstructor	id;
	private IConstructor		defInfo		= null;
	private IConstructor		tree		= null;
	private final long			modStamp	= 0L;
	private final ILanguage		lang;
	private final List<IMarker>	markers		= new ArrayList<IMarker>();
	private final ICompiler		compiler;


	MagnoliaPackage(IResourceManager owner, IResource file, IConstructor id, ILanguage lang) {
		super(owner, file);
		this.id = id;
		this.lang = lang;
		this.compiler = lang.getCompiler(owner);
	}


	@Override
	public ILanguage getLanguage() {
		return lang;
	}


	@Override
	public Collection<IManagedResource> getChildren(IRascalMonitor rm) {
		if(defInfo == null)
			loadInfo(rm);
		IConstructor value = (IConstructor) compiler.getEvaluator().call(rm, "getPkgContents", defInfo, this);

		// TODO Auto-generated method stub
		return Collections.EMPTY_SET;
	}


	@Override
	public Collection<IManagedPackage> getDepends(IRascalMonitor rm) {
		if(defInfo == null)
			loadInfo(rm);
		Set<IManagedPackage> depends = new HashSet<IManagedPackage>();
		for(IValue d : (ISet) defInfo.get("depends")) {
			IConstructor dep = (IConstructor) d;

			depends.add(manager.findPackage(lang, dep));
		}

		return depends;
	}


	@Override
	public Collection<IManagedPackage> getTransitiveDepends(IRascalMonitor rm) {
		Set<IManagedPackage> depends = new HashSet<IManagedPackage>();
		List<IManagedPackage> todo = new ArrayList<IManagedPackage>(getDepends(rm));
		while(!todo.isEmpty()) {
			IManagedPackage pkg = todo.remove(0);
			depends.add(pkg);
			for(IManagedPackage p : pkg.getDepends(rm)) {
				if(p != this && !depends.contains(p))
					todo.add(p);
			}
		}
		return depends;
	}


	@Override
	public IConstructor getId() {
		return id;
	}


	@Override
	public String getName() {
		return lang.getNameString(id);
	}


	private void loadTree(IRascalMonitor rm) {
		try {
			tree = null;
			clearMarkers(ErrorMarkers.PARSE_ERROR);
			IConstructor pt = lang.getParser().parseModule(getURI(), getContentsCharArray());
			tree = (IConstructor) compiler.getEvaluator().call(rm, "desugarTree", TermImploder.implodeTree(pt));
		}
		catch(ParseError e) {
			ISourceLocation location = vf.sourceLocation(e.getLocation(), e.getOffset(), e.getLength(), e.getBeginLine(), e.getEndLine(), e.getBeginColumn(), e.getEndColumn());
			addMarker(e.getMessage(), location, ErrorMarkers.PARSE_ERROR, ErrorMarkers.SEVERITY_ERROR_NUMBER);
		}
		catch(Exception e) {
			e.printStackTrace();
			addMarker(e.getMessage(), null, ErrorMarkers.PARSE_ERROR, ErrorMarkers.SEVERITY_ERROR_NUMBER);
		}

		if(tree == null) {
			tree = TermFactory.cons("MagnoliaTree", TermFactory.cons("PackageHead", id, TermFactory.seq()), TermFactory.seq());
		}

		assert tree != null;
	}


	private void loadInfo(IRascalMonitor monitor) {
		if(tree == null)
			loadTree(monitor);

		IConstructor result = (IConstructor) compiler.getEvaluator().call(monitor, "getPkgInfo", this, manager);
		defInfo = (IConstructor) result.get("val");

		assert defInfo != null;
	}


	public void clearMarkers(String markerType) {
		IFile file = (IFile) resource;
		try {
			file.deleteMarkers(markerType, true, IResource.DEPTH_INFINITE);
		}
		catch(CoreException e) {
		}
	}


	@Override
	public synchronized void addMarker(String message, ISourceLocation loc, final String markerType, final int severity) {
		IFile file = (IFile) resource;

		try {
			int start = 0;
			int end = 1;
			if(loc != null && loc.hasOffsetLength()) {
				start = loc.getOffset();
				end = loc.getOffset() + loc.getLength();

				for(final IMarker m : file.findMarkers(markerType, false, IResource.DEPTH_INFINITE))
					if(m.getAttribute(IMarker.CHAR_START, -1) == start && m.getAttribute(IMarker.CHAR_END, -1) == end && m.getAttribute(IMarker.MESSAGE, "").equals(message))
						return;
			}
			else {
				for(final IMarker m : file.findMarkers(markerType, false, IResource.DEPTH_INFINITE))
					if(m.getAttribute(IMarker.CHAR_START, -1) == -1 && m.getAttribute(IMarker.CHAR_END, -1) == -1 && m.getAttribute(IMarker.MESSAGE, "").equals(message))
						return;
			}
			IMarker marker = file.createMarker(markerType);
			//if(loc.hasOffsetLength()) {
			marker.setAttribute(IMarker.CHAR_START, start);
			marker.setAttribute(IMarker.CHAR_END, end);
			//}
			marker.setAttribute(IMarker.MESSAGE, message);
			if(loc != null && loc.hasLineColumn()) {
				marker.setAttribute(IMarker.LOCATION, "Line " + loc.getBeginLine() + ", column " + loc.getBeginColumn());
			}
			else {
				marker.setAttribute(IMarker.LOCATION, "File " + file.getFullPath());
			}
			marker.setAttribute(IMarker.SEVERITY, severity);
			marker.setAttribute(IMarker.TRANSIENT, true);
			markers.add(marker);
		}
		catch(final CoreException e) {
		}

		throw new ImplementationError("Can't find file for error message: " + message);

	}


	@Override
	public IConstructor getAST(IRascalMonitor rm) {
		if(tree == null)
			loadTree(rm);
		return tree;
	}


	@Override
	public IConstructor getDefInfo(IRascalMonitor rm) {
		if(defInfo == null)
			loadInfo(rm);
		return defInfo;
	}


	@Override
	public void onResourceChanged() {
		defInfo = null;
		tree = null;
	}


	@Override
	public boolean isCodeUnit() {
		return true;
	}


	@Override
	public boolean isContainer() {
		return true;
	}

}
