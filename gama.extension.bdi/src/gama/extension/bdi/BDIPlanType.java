/*******************************************************************************************************
 *
 * BDIPlanType.java, in gama.extension.bdi, is part of the source code of the GAMA modeling and simulation platform .
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
import gama.api.data.objects.IMap;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.GamaType;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.ITypesManager;
import gama.api.runtime.scope.IScope;

/**
 * The Class BDIPlanType.
 */
@type (
		name = "BDIPlan",
		id = BDIPlanType.BDIPLANTYPE_ID,
		wraps = { BDIPlan.class },
		concept = { IConcept.TYPE, IConcept.BDI })
@doc ("a type representing a plan for the BDI engine")
// @converter (GamaBDIPlanConverter.class)
public class BDIPlanType extends GamaType<BDIPlan> {

	/**
	 * @param typesManager
	 * @param varKind
	 * @param id
	 * @param name
	 * @param support
	 */
	public BDIPlanType(final ITypesManager typesManager) {
		super(typesManager);
	}

	/** The Constant id. */
	public final static int BDIPLANTYPE_ID = IType.BEGINNING_OF_CUSTOM_TYPES + 546655;

	@Override
	public boolean canCastToConst() {
		return true;
	}

	@Override
	@doc ("cast an object into a BDIPlan if it is an instance of a BDIPlan")
	public BDIPlan cast(final IScope scope, final Object obj, final Object val, final boolean copy)
			throws GamaRuntimeException {
		if (obj instanceof BDIPlan plan) return plan;
		return null;
	}

	@Override
	public BDIPlan getDefault() { return null; }

	@Override
	public BDIPlan deserializeFromJson(final IScope scope, final IMap<String, Object> map2) {
		// See later how to deal with BDI objects
		return null;
	}

}
