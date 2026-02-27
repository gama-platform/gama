/*******************************************************************************************************
 *
 * GamaKmlExportType.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.file;

import gama.annotations.doc;
import gama.annotations.type;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.GamaType;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.ITypesManager;
import gama.api.runtime.scope.IScope;

/**
 * The Class GamaKmlExportType.
 */
@type (
		name = "kml",
		id = IType.KML,
		wraps = { GamaKmlExport.class },
		kind = ISymbolKind.REGULAR,
		concept = { IConcept.TYPE },
		doc = { @doc (
				value = "Type of variables that enables to store objects and to export them into a KML (Keyhole Markup Language) file") })
public class GamaKmlExportType extends GamaType<GamaKmlExport> {

	/**
	 * @param typesManager
	 * @param varKind
	 * @param id
	 * @param name
	 * @param support
	 */
	public GamaKmlExportType(final ITypesManager typesManager) {
		super(typesManager);
	}

	@Override
	public boolean canCastToConst() {
		return true;
	}

	@Override
	@doc ("Returns a kml exportation object if the argument is alrady of type kml, otherwise nil")
	public GamaKmlExport cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		if (obj instanceof GamaKmlExport) return (GamaKmlExport) obj;
		return null;
	}

	@Override
	public GamaKmlExport getDefault() { return new GamaKmlExport(); }

}
