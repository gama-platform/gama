/*******************************************************************************************************
 *
 * GamaStringType.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.types;

import gama.annotations.doc;
import gama.annotations.type;
import gama.annotations.constants.IKeyword;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.runtime.scope.IScope;
import gama.api.types.misc.IValue;
import gama.api.utils.interfaces.INamed;

/**
 * Represents the GAML string type.
 * <p>
 * Strings are ordered lists of characters and the universal conversion type in GAML.
 * Almost any object can be converted to a string representation. The type supports
 * intelligent casting:
 * <ul>
 * <li>null → null</li>
 * <li>String → itself</li>
 * <li>IValue → stringValue()</li>
 * <li>INamed → getName()</li>
 * <li>Other → toString()</li>
 * </ul>
 * String values can be cast to constants and are drawable.
 * </p>
 * 
 * @author drogoul
 * @since GAMA 1.0
 */
@SuppressWarnings ("unchecked")
@type (
		name = IKeyword.STRING,
		id = IType.STRING,
		wraps = { String.class },
		kind = ISymbolKind.REGULAR,
		concept = { IConcept.TYPE, IConcept.STRING },
		doc = @doc ("Strings are ordered list of characters"))
public class GamaStringType extends GamaType<String> {

	/**
	 * Constructs a new GamaStringType.
	 * 
	 * @param typesManager the types manager for type resolution
	 */
	public GamaStringType(final ITypesManager typesManager) {
		super(typesManager);
	}

	@Override
	@doc ("Transforms the parameter into a string")
	public String cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		return staticCast(scope, obj, copy);
	}

	/**
	 * Performs static casting to string for various object types.
	 * <p>
	 * This method provides intelligent string conversion for different types:
	 * <ul>
	 * <li>null returns null</li>
	 * <li>Strings are returned unchanged</li>
	 * <li>IValue objects use their stringValue() method</li>
	 * <li>INamed objects return their name</li>
	 * <li>All other objects use toString()</li>
	 * </ul>
	 * </p>
	 *
	 * @param scope the execution scope
	 * @param obj the object to cast to string
	 * @param copy whether to copy the result (not applicable for strings)
	 * @return the string representation, or null if obj is null
	 * @throws GamaRuntimeException if casting fails
	 */
	public static String staticCast(final IScope scope, final Object obj, final boolean copy)
			throws GamaRuntimeException {
		return switch (obj) {
			case null -> null;
			case String s -> s;
			case IValue i -> i.stringValue(scope);
			case INamed n -> n.getName();
			default -> obj.toString();
		};
	}

	/**
	 * Returns the default value for the string type.
	 * 
	 * @return null, the default string value
	 */
	@Override
	public String getDefault() { return null; }

	/**
	 * Indicates whether string values can be cast to constants.
	 * 
	 * @return true, as strings can be compile-time constants
	 */
	@Override
	public boolean canCastToConst() {
		return true;
	}

	/**
	 * Indicates whether string values can be drawn/visualized.
	 * 
	 * @return true, as strings can be displayed as text
	 */
	@Override
	public boolean isDrawable() { return true; }

}
