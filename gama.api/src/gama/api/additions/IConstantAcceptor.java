/*******************************************************************************************************
 *
 * IConstantAcceptor.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.additions;

/**
 * The IConstantAcceptor interface defines a contract for objects that accept and register GAML constant declarations.
 * 
 * <p>This interface is used during the GAML language initialization phase to register predefined constants
 * that will be available throughout all GAML models. Constants include units (e.g., #km, #h), mathematical
 * constants (e.g., #pi), colors, and other immutable values.</p>
 * 
 * <h2>Purpose</h2>
 * 
 * <p>Implementors of this interface serve as:</p>
 * <ul>
 *   <li><strong>Constant Registries:</strong> Collect and store constant definitions</li>
 *   <li><strong>Validation Gates:</strong> Validate constant names and values before registration</li>
 *   <li><strong>Namespace Managers:</strong> Handle constant naming including aliases</li>
 * </ul>
 * 
 * <h2>Constant Categories</h2>
 * 
 * <p>Common types of constants registered include:</p>
 * <ul>
 *   <li><strong>Units:</strong> Distance (#m, #km), time (#s, #h, #day), mass, etc.</li>
 *   <li><strong>Mathematical:</strong> #pi, #e, #infinity, etc.</li>
 *   <li><strong>Colors:</strong> #red, #blue, #green, etc.</li>
 *   <li><strong>Special Values:</strong> #true, #false, #nil, etc.</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * 
 * <pre>{@code
 * IConstantAcceptor acceptor = ...;
 * 
 * // Register a distance unit
 * acceptor.accept("km", 1000.0, "Kilometer", null, false, "kilometer");
 * 
 * // Register a time unit
 * acceptor.accept("day", 86400.0, "Day in seconds", null, true, "d");
 * 
 * // Register a mathematical constant
 * acceptor.accept("pi", Math.PI, "Pi constant", null, false, "π");
 * }</pre>
 * 
 * @author Alexis Drogoul
 * @since GAMA 1.0
 * @version 2025-03
 * 
 * @see gama.api.gaml.GAML
 */
public interface IConstantAcceptor {

	/**
	 * Adds a constant to this acceptor and returns true if it has been added or false if any problem has prevented the
	 * constant from being added.
	 * 
	 * <p>This method registers a constant value that will be accessible in GAML models using the #name syntax.
	 * The constant can have multiple names (aliases) for user convenience.</p>
	 * 
	 * <p><b>Example Usage in GAML:</b></p>
	 * <pre>{@code
	 * // Using a registered constant
	 * float distance <- 5 #km;  // Uses the "km" constant
	 * float time <- 2 #h;       // Uses the "h" (hour) constant
	 * }</pre>
	 *
	 * @param name the primary name of the constant (cannot be null). Will be accessible as #name in GAML
	 * @param value the value of the constant. Can be any Java object that GAML can handle
	 * @param doc the documentation string attached to this constant (should not be null). 
	 *            Displayed in IDE tooltips and documentation
	 * @param deprec optional deprecation message if the constant is deprecated (can be null).
	 *               If not null, using this constant will trigger a warning
	 * @param isTime whether this constant is a unit related to time concepts (like #month, #day, #h).
	 *               Affects how the constant is interpreted in temporal operations
	 * @param names additional names (aliases) under which this constant can be accessed in GAML.
	 *              For example, "kilometer" as an alias for "km"
	 * @return true if the constant was successfully registered, false if registration failed
	 *         (typically due to name conflicts or validation errors)
	 */
	@SuppressWarnings ("rawtypes")
	boolean accept(final String name, final Object value, final String doc, final String deprec, final boolean isTime,
			final String... names);

}
