package gama.gaml.factories;

import org.eclipse.emf.ecore.EObject;

import gama.annotations.precompiler.ISymbolKind;
import gama.gaml.descriptions.DataTypeDescription;
import gama.gaml.descriptions.IDescription;
import gama.gaml.descriptions.SymbolProto;
import gama.gaml.statements.Facets;

public class DataTypeFactory extends SymbolFactory {

	@Override
	protected DataTypeDescription buildDescription(final String keyword, final Facets facets, final EObject element,
			final Iterable<IDescription> children, final IDescription dd, final SymbolProto proto) {	
		return new DataTypeDescription(keyword, null, dd, null, children, element, facets, null);
	}
	
	
	
	public DataTypeDescription createBuiltInDataDescription(final String name, final Class clazz,
			final DataTypeDescription superDesc, final DataTypeDescription parent) {
		DescriptionFactory.addNewTypeName(name, ISymbolKind.DATA);
		return new DataTypeDescription(name, clazz, superDesc, parent, null, null, null, null);
	}
	
	
	
	
	
	
}