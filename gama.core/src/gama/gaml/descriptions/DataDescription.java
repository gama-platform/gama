package gama.gaml.descriptions;

import org.eclipse.emf.ecore.EObject;

import gama.core.common.interfaces.IKeyword;
import gama.core.kernel.experiment.IExperimentAgent;
import gama.gaml.compilation.kernel.GamaMetaModel;
import gama.gaml.statements.Facets;


public class DataDescription extends TypeDescription {

	
	public DataDescription(String keyword, Class clazz, IDescription macroDesc, TypeDescription parent,
			Iterable<? extends IDescription> cp, EObject source, Facets facets, final String plugin) {
		super(keyword, clazz, macroDesc, parent, cp, source, facets, plugin);
	}

	@Override
	public boolean isBuiltIn() {
		return false;
	}

	@Override
	public String getTitle() {
		return "data_type " + getName();
	}


	@Override
	public Class<? extends IExperimentAgent> getJavaBase() {
		String type = getLitteral(IKeyword.TYPE);
		return GamaMetaModel.INSTANCE.getJavaBaseFor(type); 
		//TODO: maybe have to add declared variables inside?
	}
}