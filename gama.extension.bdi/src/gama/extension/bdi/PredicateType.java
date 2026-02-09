/*******************************************************************************************************
 *
 * PredicateType.java, in gama.extension.bdi, is part of the source code of the GAMA modeling and
 * simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.bdi;

import java.util.Map;

import gama.annotations.doc;
import gama.annotations.type;
import gama.annotations.support.IConcept;
import gama.api.data.objects.IMap;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.GamaType;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;

/**
 * The Class PredicateType.
 */
@SuppressWarnings ("unchecked")
@type (
		name = SimpleBdiArchitecture.PREDICATE,
		id = PredicateType.id,
		wraps = { Predicate.class },
		concept = { IConcept.TYPE, IConcept.BDI })
@doc ("represents a predicate")
public class PredicateType extends GamaType<Predicate> {

	/** The Constant id. */
	public final static int id = IType.AVAILABLE_TYPES + 546654;

	@Override
	public boolean canCastToConst() {
		return true;
	}

	@SuppressWarnings ({ "rawtypes" })
	@Override
	@doc ("cast an object as a predicate")
	public Predicate cast(final IScope scope, final Object obj, final Object val, final boolean copy)
			throws GamaRuntimeException {
		if (obj instanceof Predicate p) return p;
		if (obj instanceof String s) return new Predicate(s);
		if (obj instanceof Map) {
			final Map<String, Object> map = (Map<String, Object>) obj;
			String nm = (String) map.get("name");
			if (nm == null) {
				nm = SimpleBdiArchitecture.PREDICATE;
			}
			final IMap values = (IMap) map.get("values");
			return new Predicate(nm, values);
		}
		return null;
	}

	@Override
	public Predicate getDefault() { return null; }

	@Override
	public Predicate deserializeFromJson(final IScope scope, final IMap<String, Object> map2) {
		// See later how to deal with BDI objects
		return null;
	}

}
