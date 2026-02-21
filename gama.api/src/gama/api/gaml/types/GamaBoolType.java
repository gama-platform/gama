/*******************************************************************************************************
 *
 * GamaBoolType.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.types;

import java.io.File;

import gama.annotations.doc;
import gama.annotations.type;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.kernel.agent.IAgent;
import gama.api.runtime.scope.IScope;
import gama.api.types.file.IGamaFile;
import gama.api.types.misc.IContainer;

/**
 * Represents the GAML boolean type.
 * <p>
 * This type wraps Java boolean and Boolean values, representing logical true/false values in GAML.
 * The type provides intelligent casting from various types:
 * <ul>
 * <li>null → false</li>
 * <li>Boolean → itself</li>
 * <li>IAgent → false if dead, true if alive</li>
 * <li>IGamaFile → true if exists, false otherwise</li>
 * <li>IContainer → false if empty, true otherwise</li>
 * <li>File → true if exists, false otherwise</li>
 * <li>Integer → false if 0, true otherwise</li>
 * <li>Double → false if 0.0, true otherwise</li>
 * <li>String → true if equals "true", false otherwise</li>
 * <li>Other → false</li>
 * </ul>
 * </p>
 * 
 * @author drogoul
 * @since GAMA 1.0
 */
@SuppressWarnings ("unchecked")
@type (
		name = IKeyword.BOOL,
		id = IType.BOOL,
		wraps = { Boolean.class, boolean.class },
		kind = ISymbolKind.Variable.REGULAR,
		doc = { @doc ("Represents boolean values, either true or false") },
		concept = { IConcept.TYPE, IConcept.LOGICAL, IConcept.CONDITION })
public class GamaBoolType extends GamaType<Boolean> {

	/**
	 * Constructs a new GamaBoolType.
	 * 
	 * @param typesManager the types manager for type resolution
	 */
	public GamaBoolType(final ITypesManager typesManager) {
		super(typesManager);
	}

	@Override
	@doc ("Casts parameter into a bool. false if the parameter is nil, equal to zero, empty or dead, depending on its type")
	public Boolean cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		return staticCast(scope, obj, param, copy);
	}

	/**
	 * Performs static casting to boolean for various object types.
	 * <p>
	 * This method implements the core casting logic used by both instance and static contexts.
	 * It uses pattern matching to handle different input types intelligently:
	 * <ul>
	 * <li>null returns false</li>
	 * <li>Boolean values are returned as-is</li>
	 * <li>Agents return false if dead, true if alive</li>
	 * <li>Files return true if they exist</li>
	 * <li>Containers return false if empty</li>
	 * <li>Numbers return false if zero, true otherwise</li>
	 * <li>Strings return true only if they equal "true"</li>
	 * <li>All other types return false</li>
	 * </ul>
	 * </p>
	 *
	 * @param scope the execution scope (may be null for some casting operations)
	 * @param obj the object to cast to boolean
	 * @param param optional casting parameter (currently unused)
	 * @param copy whether to copy the result (not applicable for booleans)
	 * @return the boolean value resulting from the cast
	 */
	@SuppressWarnings ("rawtypes")
	public static Boolean staticCast(final IScope scope, final Object obj, final Object param, final boolean copy) {
		return switch (obj) {
			case null -> false;
			case Boolean b -> b;
			case IAgent a -> !a.dead();
			case IGamaFile f -> f.exists(scope);
			case IContainer c -> !c.isEmpty(scope);
			case File f -> f.exists();
			case Integer i -> i != 0;
			case Double d -> d != 0d;
			case String s -> "true".equals(s);
			default -> false;
		};
	}

	/**
	 * Returns the default value for the boolean type.
	 * 
	 * @return false, the default boolean value
	 */
	@Override
	public Boolean getDefault() { return false; }

	/**
	 * Indicates whether boolean values can be cast to constants.
	 * 
	 * @return true, as boolean values can be compile-time constants
	 */
	@Override
	public boolean canCastToConst() {
		return true;
	}

}
