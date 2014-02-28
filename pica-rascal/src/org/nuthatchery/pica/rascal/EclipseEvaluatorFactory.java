package org.nuthatchery.pica.rascal;

import java.util.List;

public class EclipseEvaluatorFactory extends EvaluatorFactory {

	public EclipseEvaluatorFactory(ISearchPathProvider provider) {
		super(provider);
	}


	@Override
	protected IEvaluatorPool makeEvaluatorPool(String name, List<String> imports) {
		return new EclipseEvaluatorPool(this, name, imports);
	}

}
