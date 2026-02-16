/*******************************************************************************************************
 *
 * NormType.java, in gama.extension.bdi, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.bdi;

import gama.annotations.doc;
import gama.annotations.type;
import gama.annotations.support.IConcept;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.GamaType;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.ITypesManager;
import gama.api.runtime.scope.IScope;
import gama.api.types.map.IMap;

/**
 * The Class NormType.
 */
@type (
		name = "Norm",
		id = NormType.id,
		wraps = { Norm.class },
		concept = { IConcept.TYPE, IConcept.BDI })
@doc ("represents a norm")
public class NormType extends GamaType<Norm> {

	/**
	 * @param typesManager
	 * @param varKind
	 * @param id
	 * @param name
	 * @param support
	 */
	public NormType(final ITypesManager typesManager) {
		super(typesManager);
	}

	/** The Constant id. */
	public final static int id = IType.BEGINNING_OF_CUSTOM_TYPES + 546660;

	@Override
	public boolean canCastToConst() {
		return true;
	}

	@Override
	@doc ("cast an object into a norm, if it is an instance of a norm")
	public Norm cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		if (obj instanceof Norm) return (Norm) obj;
		return null;
	}

	@Override
	public Norm getDefault() { return null; }

	@Override
	public Norm deserializeFromJson(final IScope scope, final IMap<String, Object> map2) {
		// See later how to deal with BDI objects
		return null;
	}

}
