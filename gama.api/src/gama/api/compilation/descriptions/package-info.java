/*******************************************************************************************************
 *
 * package-info.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

/**
 * The descriptions package provides semantic descriptions of GAML model elements.
 * 
 * <p>This package contains interfaces and implementations for describing the semantic structure
 * of GAML models. Descriptions are the compiled, validated representations of model elements
 * that can be used to generate executable code.</p>
 * 
 * <h2>Description Hierarchy</h2>
 * 
 * <h3>Base Descriptions:</h3>
 * <ul>
 *   <li>{@link gama.api.compilation.descriptions.IDescription} - Base interface for all descriptions</li>
 *   <li>{@link gama.api.compilation.descriptions.IModelDescription} - Complete model specification</li>
 *   <li>{@link gama.api.compilation.descriptions.ISpeciesDescription} - Agent type descriptions</li>
 * </ul>
 * 
 * <h3>Member Descriptions:</h3>
 * <ul>
 *   <li>{@link gama.api.compilation.descriptions.IVariableDescription} - Attribute descriptions</li>
 *   <li>{@link gama.api.compilation.descriptions.IActionDescription} - Behavior descriptions</li>
 *   <li>{@link gama.api.compilation.descriptions.IStatementDescription} - Statement descriptions</li>
 * </ul>
 * 
 * <h3>Specialized Descriptions:</h3>
 * <ul>
 *   <li>{@link gama.api.compilation.descriptions.IExperimentDescription} - Experiment specifications</li>
 *   <li>{@link gama.api.compilation.descriptions.ISkillDescription} - Skill descriptions</li>
 * </ul>
 * 
 * <h2>Features</h2>
 * 
 * <p>Descriptions provide:</p>
 * <ul>
 *   <li>Type information and validation</li>
 *   <li>Facet values (parameters, constraints)</li>
 *   <li>Child element access</li>
 *   <li>Parent and context navigation</li>
 *   <li>Compilation and code generation support</li>
 *   <li>Documentation metadata</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * 
 * <pre>{@code
 * IModelDescription model = ...;
 * 
 * // Access species
 * ISpeciesDescription species = model.getSpeciesDescription("my_species");
 * 
 * // Access attributes
 * IVariableDescription var = species.getAttribute("my_attribute");
 * IType<?> type = var.getType();
 * 
 * // Access actions
 * IActionDescription action = species.getAction("my_action");
 * }</pre>
 * 
 * @author Alexis Drogoul
 * @since GAMA 1.0
 * @version 2025-03
 * 
 * @see gama.api.compilation.descriptions.IDescription
 * @see gama.api.compilation.descriptions.IModelDescription
 */
package gama.api.compilation.descriptions;
