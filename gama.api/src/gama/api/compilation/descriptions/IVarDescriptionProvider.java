/*******************************************************************************************************
 *
 * IVarDescriptionProvider.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.descriptions;

import gama.api.gaml.expressions.IExpression;

/**
 * Interface for objects that can provide variable expressions for attribute access.
 * 
 * <p>
 * This interface defines the contract for objects that can resolve variable names to expressions,
 * enabling attribute access in GAML code. It is implemented by species descriptions, agents, and
 * other contexts where variables can be accessed.
 * </p>
 * 
 * <h2>Purpose</h2>
 * 
 * <p>
 * IVarDescriptionProvider enables:
 * </p>
 * <ul>
 *   <li><strong>Variable Resolution:</strong> Converting variable names to executable expressions</li>
 *   <li><strong>Scoped Access:</strong> Providing variables in the appropriate context (species, agent, etc.)</li>
 *   <li><strong>Field vs. Global Access:</strong> Distinguishing between direct field access and global variables</li>
 *   <li><strong>Compilation Support:</strong> Resolving variables during expression compilation</li>
 * </ul>
 * 
 * <h2>Implementation Hierarchy</h2>
 * 
 * <p>
 * This interface is implemented by:
 * </p>
 * <ul>
 *   <li>{@link IDescription} - All descriptions can provide variable expressions</li>
 *   <li>{@link ISpeciesDescription} - Species provide their attributes</li>
 *   <li>Agent runtime contexts - Agents provide their current variable values</li>
 * </ul>
 * 
 * <h2>Field vs. Global Access</h2>
 * 
 * <p>
 * The {@code asField} parameter controls how variables are accessed:
 * </p>
 * <ul>
 *   <li><strong>As Field ({@code true}):</strong> Direct attribute access (regular VarExpression)</li>
 *   <li><strong>Not As Field ({@code false}):</strong> May return GlobalVarExpression for global variables</li>
 * </ul>
 * 
 * <h3>Example:</h3>
 * <pre>{@code
 * // In species context
 * global {
 *     int world_time <- 0;
 * }
 * 
 * species my_agent {
 *     int energy <- 100;
 *     
 *     action test {
 *         // energy: asField=true returns VarExpression
 *         // world_time: asField=false returns GlobalVarExpression
 *     }
 * }
 * }</pre>
 * 
 * <h2>Usage Example</h2>
 * 
 * <pre>{@code
 * IVarDescriptionProvider provider = ...; // species, agent, etc.
 * 
 * // Check if variable exists
 * if (provider.hasAttribute("energy")) {
 *     // Get expression for accessing the variable
 *     IExpression energyExpr = provider.getVarExpr("energy", true);
 *     
 *     // Use in expression compilation
 *     Object value = energyExpr.value(scope);
 * }
 * 
 * // Access global variable (not as field)
 * IExpression globalExpr = provider.getVarExpr("world_time", false);
 * }</pre>
 * 
 * @author Alexis Drogoul
 * @since GAMA 1.0
 * @version 2025-03
 * 
 * @see IDescription
 * @see ISpeciesDescription
 * @see gama.api.gaml.expressions.IExpression
 */
public interface IVarDescriptionProvider {

	/**
	 * Returns an expression for accessing the variable with the given name.
	 * 
	 * <p>
	 * This method creates an expression that can read (and possibly write) the variable.
	 * The type of expression returned depends on the {@code asField} parameter:
	 * </p>
	 * <ul>
	 *   <li>If {@code asField} is {@code true}: Returns a regular VarExpression for direct attribute access</li>
	 *   <li>If {@code asField} is {@code false}: May return a GlobalVarExpression for global variables</li>
	 * </ul>
	 * 
	 * <h3>Example:</h3>
	 * <pre>{@code
	 * // Get expression for a species attribute
	 * IExpression energyExpr = speciesDesc.getVarExpr("energy", true);
	 * 
	 * // Get expression for a global variable
	 * IExpression globalExpr = speciesDesc.getVarExpr("world_time", false);
	 * }</pre>
	 *
	 * @param name the name of the variable to access
	 * @param asField if true, return a field access expression; if false, may return a global variable expression
	 * @return an expression for accessing the variable, or null if the variable doesn't exist
	 */
	IExpression getVarExpr(final String name, boolean asField);

	/**
	 * Checks if this provider has an attribute with the given name.
	 * 
	 * <p>
	 * This method checks whether a variable with the specified name exists in this context.
	 * It should be called before {@link #getVarExpr(String, boolean)} to avoid null returns.
	 * </p>
	 * 
	 * <h3>Example:</h3>
	 * <pre>{@code
	 * if (provider.hasAttribute("energy")) {
	 *     IExpression expr = provider.getVarExpr("energy", true);
	 *     // expr is guaranteed to be non-null
	 * } else {
	 *     // Handle missing attribute
	 *     System.err.println("Attribute 'energy' not found");
	 * }
	 * }</pre>
	 *
	 * @param name the name of the attribute to check for
	 * @return true if an attribute with this name exists, false otherwise
	 */
	boolean hasAttribute(String name);

}
