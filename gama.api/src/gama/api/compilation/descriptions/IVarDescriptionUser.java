/*******************************************************************************************************
 *
 * IVarDescriptionUser.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.descriptions;

import gama.api.utils.collections.ICollector;

/**
 * Interface for objects that use (reference) variables from species descriptions.
 * 
 * <p>
 * This interface defines the contract for objects that reference variables defined in species,
 * enabling dependency tracking and analysis. It is primarily implemented by expressions and
 * descriptions that need to track which species attributes they use.
 * </p>
 * 
 * <h2>Purpose</h2>
 * 
 * <p>
 * IVarDescriptionUser enables:
 * </p>
 * <ul>
 *   <li><strong>Dependency Analysis:</strong> Identifying which variables an expression uses</li>
 *   <li><strong>Optimization:</strong> Determining which attributes need to be computed</li>
 *   <li><strong>Incremental Updates:</strong> Knowing what to recompute when variables change</li>
 *   <li><strong>Validation:</strong> Checking if used variables exist in the appropriate context</li>
 * </ul>
 * 
 * <h2>Implementation Hierarchy</h2>
 * 
 * <p>
 * This interface is implemented by:
 * </p>
 * <ul>
 *   <li>{@link IDescription} - Descriptions can use variables in their facets</li>
 *   <li>{@link gama.api.gaml.expressions.IExpression} - Expressions reference variables</li>
 *   <li>Variable access expressions (VarExpression, GlobalVarExpression)</li>
 * </ul>
 * 
 * <h2>Dependency Collection</h2>
 * 
 * <p>
 * The {@link #collectUsedVarsOf(ISpeciesDescription, ICollector, ICollector)} method performs
 * recursive dependency analysis:
 * </p>
 * <ol>
 *   <li>Identifies variables from the specified species that are used</li>
 *   <li>Recursively analyzes sub-expressions</li>
 *   <li>Avoids re-processing already analyzed elements</li>
 *   <li>Collects all used variable descriptions in the result collector</li>
 * </ol>
 * 
 * <h2>Usage Example</h2>
 * 
 * <pre>{@code
 * IVarDescriptionUser expr = ...; // An expression like "energy + speed"
 * ISpeciesDescription species = ...; // Species defining energy and speed
 * 
 * // Collect all variables from 'species' used in 'expr'
 * ICollector<IVarDescriptionUser> alreadyProcessed = new GamaCollector<>();
 * ICollector<IVariableDescription> usedVars = new GamaCollector<>();
 * 
 * expr.collectUsedVarsOf(species, alreadyProcessed, usedVars);
 * 
 * // usedVars now contains descriptions for 'energy' and 'speed'
 * for (IVariableDescription var : usedVars.items()) {
 *     System.out.println("Expression uses variable: " + var.getName());
 * }
 * }</pre>
 * 
 * <h2>Performance Considerations</h2>
 * 
 * <p>
 * The {@code alreadyProcessed} collector prevents infinite recursion and redundant analysis
 * when expressions have shared sub-expressions or circular references.
 * </p>
 * 
 * @author Alexis Drogoul
 * @since GAMA 1.0
 * @version 2025-03
 * 
 * @see IVarDescriptionProvider
 * @see IVariableDescription
 * @see ISpeciesDescription
 * @see gama.api.utils.collections.ICollector
 */
public interface IVarDescriptionUser {

	/**
	 * Collects all variables from the specified species that are used by this object.
	 * 
	 * <p>
	 * This method performs dependency analysis to identify which attributes defined in the
	 * given species are referenced by this expression or description (and recursively by
	 * any sub-expressions or child descriptions).
	 * </p>
	 * 
	 * <p>
	 * The method ensures that:
	 * </p>
	 * <ul>
	 *   <li>Each IVarDescriptionUser is processed only once (tracked via {@code alreadyProcessed})</li>
	 *   <li>Only variables actually defined in {@code species} are collected</li>
	 *   <li>Sub-expressions and children are recursively analyzed</li>
	 *   <li>The analysis handles complex nested structures</li>
	 * </ul>
	 * 
	 * <h3>Algorithm:</h3>
	 * <ol>
	 *   <li>Check if this object has already been processed (in {@code alreadyProcessed})</li>
	 *   <li>If not, add it to {@code alreadyProcessed}</li>
	 *   <li>For each variable referenced by this object:
	 *     <ul>
	 *       <li>If it's defined in {@code species}, add it to {@code result}</li>
	 *     </ul>
	 *   </li>
	 *   <li>Recursively call this method on sub-expressions/children</li>
	 * </ol>
	 * 
	 * <h3>Example:</h3>
	 * <pre>{@code
	 * // Expression: energy * 2 + speed
	 * IExpression expr = ...;
	 * ISpeciesDescription species = ...; // Defines energy and speed
	 * 
	 * ICollector<IVarDescriptionUser> processed = new GamaCollector<>();
	 * ICollector<IVariableDescription> vars = new GamaCollector<>();
	 * 
	 * expr.collectUsedVarsOf(species, processed, vars);
	 * 
	 * // vars contains: [energy_description, speed_description]
	 * // processed contains: [expr, energy_ref_expr, speed_ref_expr]
	 * }</pre>
	 * 
	 * <p><b>Default Implementation:</b> Does nothing (no variables used).</p>
	 *
	 * @param species the species description whose variables to track
	 * @param alreadyProcessed collector of already analyzed IVarDescriptionUser objects
	 *                         (used to prevent redundant analysis and infinite recursion)
	 * @param result collector that will be filled with variable descriptions from {@code species}
	 *               that are used by this object or its sub-components
	 */
	default void collectUsedVarsOf(final ISpeciesDescription species,
			final ICollector<IVarDescriptionUser> alreadyProcessed, final ICollector<IVariableDescription> result) {}

}
