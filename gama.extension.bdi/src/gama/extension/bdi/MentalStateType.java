/*******************************************************************************************************
 *
 * MentalStateType.java, in gama.extension.bdi, is part of the source code of the GAMA modeling and
 * simulation platform .
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
 * The Class MentalStateType.
 */
@SuppressWarnings ("unchecked")
@type (
		name = "mental_state",
		id = MentalStateType.id,
		wraps = { MentalState.class },
		concept = { IConcept.TYPE, IConcept.BDI })
@doc ("a type representing a mental state")
public class MentalStateType extends GamaType<MentalState> {

	/** The Constant id. */
	public final static int id = IType.AVAILABLE_TYPES + 546658;

	@Override
	public boolean canCastToConst() {
		return true;
	}

	@SuppressWarnings ({ "rawtypes" })
	@Override
	@doc ("cast an object as a mental state if it is an instance o a mental state")
	public MentalState cast(final IScope scope, final Object obj, final Object val, final boolean copy)
			throws GamaRuntimeException {
		if (obj instanceof MentalState) return (MentalState) obj;
		if (obj instanceof String) return new MentalState((String) obj);
		return null;
	}

	@Override
	public MentalState getDefault() { return null; }

	@Override
	public MentalState deserializeFromJson(final IScope scope, final IMap<String, Object> map2) {
		// See later how to deal with BDI objects
		return null;
	}

}
