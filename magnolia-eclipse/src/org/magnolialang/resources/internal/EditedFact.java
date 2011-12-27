package org.magnolialang.resources.internal;

import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.parser.IModelListener;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.pdb.facts.IValue;
import org.magnolialang.eclipse.editor.MagnoliaParseController;
import org.rascalmpl.tasks.IFact;
import org.rascalmpl.tasks.facts.AbstractFact;

public class EditedFact extends AbstractFact<IValue> implements IModelListener {

	public EditedFact(Object key, MagnoliaParseController ctrl) {
		super(key, ctrl.getPath().toString(), null);
		ctrl.getEditor().addModelListener(this);
	}


	@Override
	public IValue getValue() {
		throw new UnsupportedOperationException("updateFrom");
	}


	@Override
	public boolean setValue(IValue val) {
		throw new UnsupportedOperationException("updateFrom");
	}


	@Override
	public synchronized void setDepends(Collection<IFact<IValue>> deps) {
		throw new UnsupportedOperationException("updateFrom");
	}


	@Override
	public boolean updateFrom(IFact<IValue> fact) {
		throw new UnsupportedOperationException("updateFrom");
	}


	@Override
	public AnalysisRequired getAnalysisRequired() {
		return IModelListener.AnalysisRequired.SYNTACTIC_ANALYSIS;
	}


	@Override
	public void update(IParseController parseController, IProgressMonitor monitor) {
		notifyChanged();
	}


	@Override
	public void changed(IFact<?> f, Change c, Object moreInfo) {
	}

}
