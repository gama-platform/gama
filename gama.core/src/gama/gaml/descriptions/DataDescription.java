package gama.gaml.descriptions;

import org.eclipse.emf.ecore.EObject;

import gama.core.util.GamaData;
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
	public DataDescription getParent() {
		return (DataDescription) super.getParent();
	}

	@Override
	public Class getJavaBase() {
		return GamaData.class; 
//		throw new NotImplementedException("DataDescription.getJavaBase() has not been implemented yet.");
	}
}