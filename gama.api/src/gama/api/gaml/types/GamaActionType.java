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
 * Represents the GAML type for actions and aspects of species.
 * <p>
 * This type wraps {@link IStatementDescription} objects that represent executable behaviors (actions) or
 * visual representations (aspects) defined in agent species. Action types cannot be cast to constants
 * and are drawable in the UI.
 * </p>
 * 
 * @author GAMA Development Team
 * @see IDescription
 * @see IStatementDescription
 * @since GAMA 1.0
 */
@type (
		name = IKeyword.ACTION,
		id = IType.ACTION,
		wraps = { IStatementDescription.class },
		kind = ISymbolKind.REGULAR,
		doc = { @doc ("The type of the variables that denote an action or an aspect of a species") },
		concept = { IConcept.TYPE, IConcept.ACTION, IConcept.SPECIES })
public class GamaActionType extends GamaType<IDescription> {

	/**
	 * Constructs a new GamaActionType.
	 * 
	 * @param typesManager the types manager responsible for type resolution and management
	 */
	public GamaActionType(final ITypesManager typesManager) {
		super(typesManager);
	}

	/**
	 * Indicates whether values of this type can be cast to constants.
	 * <p>
	 * Action types cannot be cast to constants as they represent dynamic behavioral constructs.
	 * </p>
	 * 
	 * @return false, as actions cannot be constants
	 */
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

	/**
	 * Returns the default value for this type.
	 * 
	 * @return null, as action types have no meaningful default value
	 */
	@Override
	public IDescription getDefault() {

		return null;
	}

	/**
	 * Indicates whether values of this type can be drawn/visualized.
	 * 
	 * @return true, as actions can be visualized in the UI
	 */
	@Override
	public boolean isDrawable() { return true; }

	/**
	 * Deserializes an action description from a JSON map.
	 * <p>
	 * Extracts the "name" field from the JSON map and casts it to an action description.
	 * </p>
	 * 
	 * @param scope the execution scope
	 * @param map2 the JSON map containing the serialized action data
	 * @return the deserialized action description, or null if not found
	 */
	@Override
	public IDescription deserializeFromJson(final IScope scope, final IMap<String, Object> map2) {
		return cast(scope, map2.get("name"), null, false);
	}

}
