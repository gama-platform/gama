/*******************************************************************************************************
 *
 * GamaDateType.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.types;

import gama.annotations.doc;
import gama.annotations.type;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.data.factories.GamaDateFactory;
import gama.api.data.objects.IDate;
import gama.api.data.objects.IMap;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.runtime.scope.IScope;

/**
 * Written by Patrick Tallandier
 *
 * @todo Description
 *
 */
@SuppressWarnings ("unchecked")
@type (
		name = "date",
		id = IType.DATE,
		wraps = { IDate.class },
		kind = ISymbolKind.Variable.NUMBER,
		concept = { IConcept.TYPE, IConcept.DATE, IConcept.TIME },
		doc = { @doc ("GAML objects that represent a date") })
public class GamaDateType extends GamaType<IDate> {

	/**
	 * @param typesManager
	 * @param varKind
	 * @param id
	 * @param name
	 * @param support
	 */
	public GamaDateType(final ITypesManager typesManager) {
		super(typesManager);
	}

	@doc ("Cast the argument into a date. If the argument is a date already, returns it, otherwise: if it is a container, casts its contents to integer numbers and tries to build a date from it (following the order 'year, month, day, hour, minute, second'); if it is a string, tries to decode it into a date using the format described in the preferences; otherwise cast the argument into a float number and interprets it as the number of milliseconds since the start of the simulation")
	@Override
	public IDate cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		return GamaDateFactory.toDate(scope, obj, param, copy);
	}

	@Override
	public IDate getDefault() { return null; }

	@Override
	public IType<?> getContentType() { return Types.get(FLOAT); }

	@Override
	public boolean canCastToConst() {
		return false;
	}

	@Override
	public boolean isCompoundType() { return true; }

	@Override
	public IDate deserializeFromJson(final IScope scope, final IMap<String, Object> map2) {
		return GamaDateFactory.createFromISOString(Cast.asString(scope, map2.get("iso")));
	}

}
