/*******************************************************************************************************
 *
 * BDIPlanType.java, in gama.extension.bdi, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.bdi;

import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.type;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.IMap;
import gama.gaml.types.GamaType;
import gama.gaml.types.IType;

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

	/** The Constant id. */
	public final static int BDIPLANTYPE_ID = IType.AVAILABLE_TYPES + 546655;

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
