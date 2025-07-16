package gama.gaml.factories;

import org.eclipse.emf.ecore.EObject;

import gama.annotations.precompiler.GamlAnnotations.factory;
import gama.annotations.precompiler.ISymbolKind;
import gama.gaml.descriptions.DataDescription;
import gama.gaml.descriptions.IDescription;
import gama.gaml.descriptions.SymbolProto;
import gama.gaml.statements.Facets;
import gama.gaml.types.IType;

@factory(handles = { ISymbolKind.DATA })
public class DataFactory extends SymbolFactory {

	@Override
	protected DataDescription buildDescription(final String keyword, final Facets facets, final EObject element,
			final Iterable<IDescription> children, final IDescription dd, final SymbolProto proto) {
		return new DataDescription(keyword, null, dd, null, children, element, facets, null);
	}
	
	
	
	public DataDescription createBuiltInDataDescription(final String name, final Class clazz,
			final DataDescription superDesc, final DataDescription parent) {
		DescriptionFactory.addNewTypeName(name, IType.COMPOSED);
		return new DataDescription(name, clazz, superDesc, parent, null, null, null, null);
	}
	
	
	
	
	
	
}