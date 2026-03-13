/*******************************************************************************************************
 *
 * IDescriptionFactory.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.descriptions;

import java.util.Set;

import gama.api.compilation.ast.ISyntacticElement;
import gama.api.compilation.factories.ISymbolDescriptionFactory;
import gama.api.gaml.symbols.Facets;
import gama.api.kernel.agent.IAgentConstructor;

/**
 * Factory interface for creating semantic descriptions of GAML model elements.
 *
 * <p>
 * This interface provides factory methods for constructing {@link IDescription} instances that represent the semantic
 * structure of GAML models. It serves as the central creation point for all types of descriptions during model
 * compilation, from high-level model and species descriptions to low-level statement and variable descriptions.
 * </p>
 *
 * <h2>Purpose</h2>
 *
 * <p>
 * The description factory enables:
 * </p>
 * <ul>
 * <li><strong>Uniform Creation:</strong> Consistent API for creating different description types</li>
 * <li><strong>Built-in Definitions:</strong> Creation of platform-provided species, skills, and experiments</li>
 * <li><strong>Model Compilation:</strong> Converting syntactic elements to semantic descriptions</li>
 * <li><strong>Programmatic Definition:</strong> Creating descriptions directly from code</li>
 * <li><strong>Extension Integration:</strong> Allowing plugins to define new GAML elements</li>
 * </ul>
 *
 * <h2>Description Types</h2>
 *
 * <p>
 * The factory can create various types of descriptions:
 * </p>
 *
 * <h3>High-level Descriptions:</h3>
 * <ul>
 * <li><strong>Model Descriptions:</strong> Complete model specifications ({@link IModelDescription})</li>
 * <li><strong>Species Descriptions:</strong> Agent type definitions ({@link ISpeciesDescription})</li>
 * <li><strong>Experiment Descriptions:</strong> Experiment specifications ({@link IExperimentDescription})</li>
 * <li><strong>Skill Descriptions:</strong> Skill module definitions ({@link ISkillDescription})</li>
 * </ul>
 *
 * <h3>Member Descriptions:</h3>
 * <ul>
 * <li><strong>Variable Descriptions:</strong> Attribute declarations ({@link IVariableDescription})</li>
 * <li><strong>Action Descriptions:</strong> Behavior definitions ({@link IActionDescription})</li>
 * <li><strong>Statement Descriptions:</strong> Executable statements ({@link IStatementDescription})</li>
 * </ul>
 *
 * <h2>Creation Patterns</h2>
 *
 * <h3>From Syntactic Elements:</h3>
 *
 * <pre>{@code
 * IDescriptionFactory factory = ...;
 * ISyntacticElement source = ...; // Parsed from GAML source
 * IDescription parent = ...;
 *
 * // Create description from syntax tree
 * IDescription desc = factory.create(source, parent, Collections.emptyList());
 * }</pre>
 *
 * <h3>Built-in Species:</h3>
 *
 * <pre>{@code
 * // Create a built-in species (defined by the platform)
 * ISpeciesDescription species = factory.createBuiltInSpeciesDescription("agent", IAgent.class, null, // no macro
 * 		null, // no parent
 * 		agentConstructor, skills, "gama.core");
 * }</pre>
 *
 * <h3>Programmatic Creation:</h3>
 *
 * <pre>{@code
 * // Create description directly with keyword and facets
 * IDescription desc = factory.create("action", parentDesc, children, "name", "my_action", "type", "void");
 * }</pre>
 *
 * <h2>Built-in Elements</h2>
 *
 * <p>
 * The factory provides specialized methods for creating built-in platform elements:
 * </p>
 * <ul>
 * <li>{@code createRootModelDescription} - Create the root model of a simulation</li>
 * <li>{@code createBuiltInSpeciesDescription} - Define platform species (agent, world, etc.)</li>
 * <li>{@code createBuiltInExperimentDescription} - Define experiment types (gui, batch, etc.)</li>
 * <li>{@code createBuiltInSkillDescription} - Define skills (moving, grid, etc.)</li>
 * <li>{@code createPlatformSpeciesDescription} - Define platform-specific species</li>
 * </ul>
 *
 * <h2>Method Variants</h2>
 *
 * <p>
 * The factory provides multiple overloaded {@code create} methods with different parameter combinations:
 * </p>
 * <ul>
 * <li><strong>From ISyntacticElement:</strong> Standard compilation path from parsed source</li>
 * <li><strong>With keyword and facets:</strong> Direct programmatic creation</li>
 * <li><strong>With children:</strong> Create complex hierarchical structures</li>
 * <li><strong>With factory parameter:</strong> Delegate to specific symbol factories</li>
 * </ul>
 *
 * <h2>Implementation Notes</h2>
 *
 * <p>
 * Implementations should:
 * </p>
 * <ul>
 * <li>Validate parameters and provide helpful error messages</li>
 * <li>Initialize descriptions with proper parent-child relationships</li>
 * <li>Handle null parameters gracefully where appropriate</li>
 * <li>Support both compiled (from source) and programmatic descriptions</li>
 * <li>Maintain consistency between built-in and user-defined elements</li>
 * <li>Provide proper type information and validation context</li>
 * </ul>
 *
 * <h2>Thread Safety</h2>
 *
 * <p>
 * Factory implementations are typically not thread-safe. Description creation should be performed sequentially during
 * the compilation phase. However, once created, descriptions are generally immutable and can be shared across threads.
 * </p>
 *
 * @author Alexis Drogoul
 * @author The GAMA Development Team
 * @since GAMA 1.0
 * @version 2025-03
 *
 * @see IDescription
 * @see IModelDescription
 * @see ISpeciesDescription
 * @see IExperimentDescription
 * @see gama.api.compilation.ast.ISyntacticElement
 */
public interface IDescriptionFactory {

	/**
	 * Creates the.
	 *
	 * @param source
	 *            the source
	 * @param superDesc
	 *            the super desc
	 * @param cp
	 *            the cp
	 * @return the i description
	 */
	IDescription create(final ISyntacticElement source, final IDescription superDesc, final Iterable<IDescription> cp);

	/**
	 * Creates a new Description object.
	 *
	 * @param name
	 *            the name
	 * @param clazz
	 *            the clazz
	 * @param macro
	 *            the macro
	 * @param parent
	 *            the parent
	 * @param helper
	 *            the helper
	 * @param skills
	 *            the skills
	 * @param plugin
	 *            the plugin
	 * @return the model description
	 */
	IModelDescription createRootModelDescription(final String name, final Class clazz, final ISpeciesDescription macro,
			final ISpeciesDescription parent, final IAgentConstructor helper, final Set<String> skills,
			final String plugin);

	/**
	 * Creates a new Description object.
	 *
	 * @param name
	 *            the name
	 * @param clazz
	 *            the clazz
	 * @param superDesc
	 *            the super desc
	 * @param parent
	 *            the parent
	 * @param helper
	 *            the helper
	 * @param skills
	 *            the skills
	 * @param plugin
	 *            the plugin
	 * @return the species description
	 */
	IExperimentDescription createBuiltInExperimentDescription(final String name, final Class clazz,
			final ISpeciesDescription macro, final ISpeciesDescription parent, final IAgentConstructor helper,
			final Set<String> skills, final String plugin);

	/**
	 * Creates a new Description object.
	 *
	 * @param name
	 *            the name
	 * @param clazz
	 *            the clazz
	 * @param macro
	 *            the macro
	 * @param parent
	 *            the parent
	 * @param helper
	 *            the helper
	 * @param allSkills
	 *            the all skills
	 * @param plugin
	 *            the plugin
	 * @return the species description
	 */
	ISpeciesDescription.Platform createPlatformSpeciesDescription(final String name, final Class clazz,
			final ISpeciesDescription macro, final ISpeciesDescription parent, final IAgentConstructor helper,
			final Set<String> allSkills, final String plugin);

	/**
	 * Creates a new Description object.
	 *
	 * @param name
	 *            the name
	 * @param clazz
	 *            the clazz
	 * @param macro
	 *            the macro
	 * @param parent
	 *            the parent
	 * @param helper
	 *            the helper
	 * @param skills
	 *            the skills
	 * @param plugin
	 *            the plugin
	 * @return the i skill description
	 */
	ISkillDescription createBuiltInSkillDescription(final String name, final Class clazz,
			final Iterable<IDescription> children, final String plugin);

	/**
	 * Creates a new Description object.
	 *
	 * @param name
	 *            the name
	 * @param clazz
	 *            the clazz
	 * @param superDesc
	 *            the super desc
	 * @param parent
	 *            the parent
	 * @param helper
	 *            the helper
	 * @param skills
	 *            the skills
	 * @param plugin
	 *            the plugin
	 * @return the species description
	 */
	ISpeciesDescription createBuiltInSpeciesDescription(final String name, final Class clazz,
			final ISpeciesDescription macro, final ISpeciesDescription parent, final IAgentConstructor helper,
			final Set<String> skills, final String plugin);

	/**
	 * Creates the.
	 *
	 * @param keyword
	 *            the keyword
	 * @param facets
	 *            the facets
	 * @return the i description
	 */
	IDescription create(final String keyword, final String... facets);

	/**
	 * Creates the.
	 *
	 * @param keyword
	 *            the keyword
	 * @param superDescription
	 *            the super description
	 * @param facets
	 *            the facets
	 * @return the i description
	 */
	IDescription create(final String keyword, final IDescription superDescription, final String... facets);

	/**
	 * Creates the.
	 *
	 * @param keyword
	 *            the keyword
	 * @param superDesc
	 *            the super desc
	 * @param children
	 *            the children
	 * @param facets
	 *            the facets
	 * @return the i description
	 */
	IDescription create(final String keyword, final IDescription superDesc, final Iterable<IDescription> children,
			final String... facets);

	/**
	 * Creates the.
	 *
	 * @param keyword
	 *            the keyword
	 * @param superDesc
	 *            the super desc
	 * @param children
	 *            the children
	 * @return the i description
	 */
	IDescription create(final String keyword, final IDescription superDesc, final Iterable<IDescription> children);

	/**
	 * Creates the.
	 *
	 * @param keyword
	 *            the keyword
	 * @param superDesc
	 *            the super desc
	 * @param children
	 *            the children
	 * @param facets
	 *            the facets
	 * @return the i description
	 */
	IDescription create(final String keyword, final IDescription superDesc, final Iterable<IDescription> children,
			final Facets facets);

	/**
	 * Creates the.
	 *
	 * @param factory
	 *            the factory
	 * @param keyword
	 *            the keyword
	 * @param superDesc
	 *            the super desc
	 * @param children
	 *            the children
	 * @param facets
	 *            the facets
	 * @return the i description
	 */
	IDescription create(final ISymbolDescriptionFactory factory, final String keyword, final IDescription superDesc,
			final Iterable<IDescription> children, final Facets facets);

	/**
	 * @param object
	 * @param class1
	 * @param speciesDescription
	 * @param cURRENT_PLUGIN_NAME
	 * @return
	 */
	IClassDescription createBuiltInClassDescription(String plugin);

}
