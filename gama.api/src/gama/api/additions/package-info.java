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
 * The additions package provides the framework for extending GAMA with new GAML language constructs,
 * operators, types, skills, and other platform capabilities.
 * 
 * <p>This package implements GAMA's extensibility mechanism, allowing plugins and extensions to
 * contribute new functionality to the GAML modeling language and the GAMA platform without modifying
 * the core system.</p>
 * 
 * <h2>Core Concepts</h2>
 * 
 * <h3>GAML Additions:</h3>
 * <p>Extensions can add new GAML language elements including:</p>
 * <ul>
 *   <li><strong>Operators:</strong> New functions and operators for expressions</li>
 *   <li><strong>Types:</strong> Custom data types with associated operations</li>
 *   <li><strong>Skills:</strong> Reusable agent behaviors and capabilities</li>
 *   <li><strong>Statements:</strong> New control structures and actions</li>
 *   <li><strong>Variables:</strong> Special variable types with custom behavior</li>
 *   <li><strong>Symbols:</strong> Custom modeling constructs and keywords</li>
 * </ul>
 * 
 * <h3>Extension Mechanism:</h3>
 * <p>The package uses a registry-based architecture where extensions:</p>
 * <ol>
 *   <li>Declare their additions using Java annotations</li>
 *   <li>Extend base classes like {@link gama.api.additions.AbstractGamlAdditions}</li>
 *   <li>Register their contributions at bundle initialization</li>
 *   <li>Are discovered and loaded by {@link gama.api.additions.GamaBundleLoader}</li>
 * </ol>
 * 
 * <h2>Key Classes</h2>
 * 
 * <ul>
 *   <li>{@link gama.api.additions.AbstractGamlAdditions} - Base class for implementing GAML additions</li>
 *   <li>{@link gama.api.additions.IGamlAdditions} - Interface for GAML addition providers</li>
 *   <li>{@link gama.api.additions.GamaBundleLoader} - Service for loading and managing plugin contributions</li>
 *   <li>{@link gama.api.additions.GamaClassLoader} - Custom class loader for plugin classes</li>
 *   <li>{@link gama.api.additions.GamaHelper} - Helper class for accessing addition metadata</li>
 *   <li>{@link gama.api.additions.IConstantAcceptor} - Interface for accepting constant contributions</li>
 *   <li>{@link gama.api.additions.IGamaGetter} - Interface for typed value getters</li>
 * </ul>
 * 
 * <h2>Registries</h2>
 * 
 * <p>The {@code registries} sub-package contains specialized registries for different types of additions:</p>
 * <ul>
 *   <li>{@link gama.api.additions.registries.GamaAdditionRegistry} - Main registry for all additions</li>
 *   <li>{@link gama.api.additions.registries.GamaSkillRegistry} - Registry for agent skills</li>
 *   <li>{@link gama.api.additions.registries.ArtefactRegistry} - Registry for action/operator prototypes</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * 
 * <h3>Creating a GAML Addition:</h3>
 * <pre>{@code
 * public class MyGamlAdditions extends AbstractGamlAdditions {
 *     
 *     // Register an operator
 *     @operator(value = "my_operator", can_be_const = true)
 *     @doc("Description of my operator")
 *     public static Object myOperator(IScope scope, Object arg) {
 *         // Implementation
 *         return result;
 *     }
 *     
 *     // Register a skill
 *     @skill(name = "my_skill")
 *     @doc("Description of my skill")
 *     public static class MySkill extends Skill {
 *         // Skill implementation
 *     }
 * }
 * }</pre>
 * 
 * <h3>Registering the Addition:</h3>
 * <pre>{@code
 * // In bundle activator
 * public void start(BundleContext context) {
 *     GamaBundleLoader.preBuildContributions();
 *     // Additions are automatically discovered via annotations
 * }
 * }</pre>
 * 
 * <h2>Delegates</h2>
 * 
 * <p>The {@code delegates} sub-package provides specialized functionality for handling different
 * aspects of GAML additions, such as type conversion, operator dispatch, and skill management.</p>
 * 
 * <h2>Thread Safety</h2>
 * 
 * <p>The addition registration system is designed to be called during bundle initialization,
 * which occurs before concurrent access to the platform. Once registered, additions are
 * read-only and can be safely accessed from multiple threads.</p>
 * 
 * <h2>Design Patterns</h2>
 * 
 * <ul>
 *   <li><strong>Registry Pattern:</strong> Centralized registration and lookup of additions</li>
 *   <li><strong>Factory Pattern:</strong> Creating instances of contributed types</li>
 *   <li><strong>Plugin Architecture:</strong> Modular extension mechanism</li>
 *   <li><strong>Annotation-based Configuration:</strong> Declarative addition metadata</li>
 * </ul>
 * 
 * @author Alexis Drogoul
 * @author The GAMA Development Team
 * @since GAMA 1.0
 * @version 2025-03
 * 
 * @see gama.api.additions.AbstractGamlAdditions
 * @see gama.api.additions.GamaBundleLoader
 * @see gama.api.additions.registries
 */
package gama.api.additions;
