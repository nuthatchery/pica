package org.nuthatchery.pica.rascal;

import java.util.List;

public class EclipseEvaluatorFactory extends EvaluatorFactory {

	public EclipseEvaluatorFactory(ISearchPathProvider provider) {
		super(provider);
	}


	@Override
	protected IEvaluatorPool makeEvaluatorPool(String name, List<String> imports, int minEvaluators) {
		EclipseEvaluatorPool pool = new EclipseEvaluatorPool(this, name, imports, minEvaluators);
		pool.initialize();
		return pool;
	}

}
