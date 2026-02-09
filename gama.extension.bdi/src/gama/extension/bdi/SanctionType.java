/*******************************************************************************************************
 *
 * SanctionType.java, in gama.extension.bdi, is part of the source code of the GAMA modeling and simulation
 * platform .
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
import gama.api.runtime.scope.IScope;

/**
 * The Class SanctionType.
 */
@type (
		name = "Sanction",
		id = SanctionType.id,
		wraps = { Sanction.class },
		concept = { IConcept.TYPE, IConcept.BDI })
@doc ("represents a sanction")
public class SanctionType extends GamaType<Sanction> {

	/** The Constant id. */
	//
	public final static int id = IType.AVAILABLE_TYPES + 546661;

	@Override
	public boolean canCastToConst() {
		return true;
	}

	@Override
	@doc ("cast an object as a sanction, if it is an instance of a sanction")
	public Sanction cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		if (obj instanceof Sanction) return (Sanction) obj;
		return null;
	}

	@Override
	public Sanction getDefault() { return null; }

	@Override
	public Sanction deserializeFromJson(final IScope scope, final IMap<String, Object> map2) {
		// See later how to deal with BDI objects
		return null;
	}

}
