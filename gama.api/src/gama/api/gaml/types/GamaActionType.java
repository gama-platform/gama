/*******************************************************************************************************
 *
 * GamaActionType.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
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
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IStatementDescription;
import gama.api.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.runtime.scope.IScope;
import gama.api.types.map.IMap;

/**
 * The Class GamaActionType.
 */
@type (
		name = IKeyword.ACTION,
		id = IType.ACTION,
		wraps = { IStatementDescription.class },
		kind = ISymbolKind.Variable.REGULAR,
		doc = { @doc ("The type of the variables that denote an action or an aspect of a species") },
		concept = { IConcept.TYPE, IConcept.ACTION, IConcept.SPECIES })
public class GamaActionType extends GamaType<IDescription> {

	/**
	 * @param typesManager
	 * @param varKind
	 * @param id
	 * @param name
	 * @param support
	 */
	public GamaActionType(final ITypesManager typesManager) {
		super(typesManager);
	}

	@Override
	public boolean canCastToConst() {
		return false;
	}

	@Override
	@doc ("Converts the argument into the reference to an action. If it is a string, its name is looked up in the current agent species")
	public IDescription cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		if (obj == null) return null;
		if (obj instanceof IDescription) return (IDescription) obj;
		if (obj instanceof String name) {
			final IDescription action = scope.getAgent().getSpecies().getDescription().getAction(name);
			if (action != null) return action;
			return scope.getAgent().getSpecies().getDescription().getAspect(name);
		}
		return null;
	}

	@Override
	public IDescription getDefault() {

		return null;
	}

	@Override
	public boolean isDrawable() { return true; }

	@Override
	public IDescription deserializeFromJson(final IScope scope, final IMap<String, Object> map2) {
		return cast(scope, map2.get("name"), null, false);
	}

}
