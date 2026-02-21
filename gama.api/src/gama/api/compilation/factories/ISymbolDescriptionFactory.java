/*******************************************************************************************************
 *
 * ISymbolDescriptionFactory.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.factories;

import java.util.Set;

import org.eclipse.emf.ecore.EObject;

import gama.annotations.support.ISymbolKind;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.ISkillDescription;
import gama.api.compilation.descriptions.ISpeciesDescription;
import gama.api.compilation.prototypes.IArtefactProto;
import gama.api.gaml.symbols.Facets;
import gama.api.kernel.agent.IAgentConstructor;

/**
 * Factory interface for creating semantic descriptions of GAML symbols.
 * 
 * <p>
 * This interface defines the contract for factories that transform syntactic elements
 * (parsed from source code) into semantic descriptions. Symbol description factories are
 * responsible for creating the appropriate {@link IDescription} subtype based on the
 * symbol keyword and kind.
 * </p>
 * 
 * <h2>Purpose</h2>
 * 
 * <p>
 * ISymbolDescriptionFactory enables:
 * </p>
 * <ul>
 *   <li><strong>Syntactic to Semantic Transformation:</strong> Converting AST nodes to descriptions</li>
 *   <li><strong>Built-in Element Creation:</strong> Creating platform-provided species and skills</li>
 *   <li><strong>Extensibility:</strong> Allowing plugins to define new symbol types</li>
 *   <li><strong>Type-Specific Logic:</strong> Specialized creation for species, skills, and statements</li>
 * </ul>
 * 
 * <h2>Factory Hierarchy</h2>
 * 
 * <p>
 * The interface defines specialized sub-interfaces for different symbol categories:
 * </p>
 * <ul>
 *   <li>{@link Species} - Factory for creating species descriptions</li>
 *   <li>{@link Skill} - Factory for creating skill descriptions</li>
 * </ul>
 * 
 * <h2>Symbol Kinds</h2>
 * 
 * <p>
 * Factories are associated with one or more symbol kinds defined in {@link ISymbolKind}:
 * </p>
 * <ul>
 *   <li>{@code SPECIES} - Agent type definitions</li>
 *   <li>{@code SKILL} - Skill modules</li>
 *   <li>{@code ACTION} - Behaviors</li>
 *   <li>{@code VARIABLE} - Attributes</li>
 *   <li>{@code SINGLE_STATEMENT}, {@code SEQUENCE_STATEMENT}, {@code BEHAVIOR} - Various statement types</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * 
 * <h3>Implementing a Description Factory:</h3>
 * <pre>{@code
 * public class SpeciesDescriptionFactory implements ISymbolDescriptionFactory.Species {
 *     
 *     @Override
 *     public IDescription buildDescription(String keyword, Facets facets, EObject element,
 *             Iterable<IDescription> children, IDescription enclosing, IArtefactProto.Symbol proto) {
 *         // Create species description from facets and children
 *         String name = facets.getLabel(IKeyword.NAME);
 *         ISpeciesDescription parent = resolveParent(facets);
 *         
 *         return new SpeciesDescription(name, parent, enclosing, element, facets, children);
 *     }
 *     
 *     @Override
 *     public int[] getKinds() {
 *         return new int[] { ISymbolKind.SPECIES };
 *     }
 *     
 *     @Override
 *     public ISpeciesDescription createBuiltInSpeciesDescription(String name, Class clazz,
 *             ISpeciesDescription macro, ISpeciesDescription parent, IAgentConstructor helper,
 *             Set<String> skills, String plugin) {
 *         // Create platform-provided species (agent, world, etc.)
 *         return new BuiltInSpeciesDescription(name, clazz, parent, helper, skills, plugin);
 *     }
 * }
 * }</pre>
 * 
 * @author Alexis Drogoul
 * @since 11 mai 2010
 * @version 2025-03
 * 
 * @see ISymbolKind
 * @see IDescription
 * @see ISpeciesDescription
 * @see ISkillDescription
 * @see gama.api.compilation.ast.ISyntacticElement
 */
public interface ISymbolDescriptionFactory extends ISymbolKind {

	/**
	 * Specialized factory for creating species descriptions.
	 * 
	 * <p>
	 * Species are agent type definitions in GAML. This interface adds capability
	 * for creating built-in species provided by the platform.
	 * </p>
	 */
	interface Species extends ISymbolDescriptionFactory {

		/**
		 * Creates a built-in species description.
		 * 
		 * <p>
		 * This method creates platform-provided species like "agent", "world", "grid", etc.
		 * These species are defined in Java code rather than GAML source.
		 * </p>
		 * 
		 * <h3>Example:</h3>
		 * <pre>{@code
		 * // Create the base 'agent' species
		 * ISpeciesDescription agentSpecies = factory.createBuiltInSpeciesDescription(
		 *     "agent",
		 *     GamlAgent.class,
		 *     null,  // no macro
		 *     null,  // no parent (root species)
		 *     new AgentConstructor(),
		 *     Set.of("moving", "visible"),
		 *     "gama.core"
		 * );
		 * }</pre>
		 *
		 * @param name the name of the built-in species
		 * @param clazz the Java class implementing the species
		 * @param macro the macro species (for grid species), may be null
		 * @param parent the parent species, may be null for root species
		 * @param helper the agent constructor for creating instances
		 * @param skills the set of skill names the species has
		 * @param plugin the plugin defining this species
		 * @return a new built-in species description
		 */
		ISpeciesDescription createBuiltInSpeciesDescription(String name, Class clazz, ISpeciesDescription macro,
				ISpeciesDescription parent, IAgentConstructor helper, Set<String> skills, String plugin);

	}

	/**
	 * Specialized factory for creating skill descriptions.
	 * 
	 * <p>
	 * Skills are modules that augment species with additional attributes and behaviors.
	 * This interface adds capability for creating built-in skills provided by the platform.
	 * </p>
	 */
	interface Skill extends ISymbolDescriptionFactory {

		/**
		 * Creates a built-in skill description.
		 * 
		 * <p>
		 * This method creates platform-provided skills like "moving", "grid", "3d", etc.
		 * These skills are defined in Java code and registered with the platform.
		 * </p>
		 * 
		 * <h3>Example:</h3>
		 * <pre>{@code
		 * // Create the 'moving' skill
		 * ISkillDescription movingSkill = factory.createBuiltInSkillDescription(
		 *     "moving",
		 *     MovingSkill.class,
		 *     childrenDescriptions,  // actions and variables
		 *     "gama.core"
		 * );
		 * }</pre>
		 *
		 * @param name the name of the skill
		 * @param clazz the Java class implementing the skill
		 * @param children the attributes and actions provided by the skill
		 * @param plugin the plugin defining this skill
		 * @return a new built-in skill description
		 */
		ISkillDescription createBuiltInSkillDescription(String name, Class clazz, Iterable<IDescription> children,
				String plugin);
	}

	/**
	 * Builds a symbol description from syntactic information.
	 * 
	 * <p>
	 * This is the main factory method called during compilation to transform syntactic elements
	 * into semantic descriptions. It creates the appropriate description type based on the
	 * keyword and symbol prototype.
	 * </p>
	 * 
	 * <p>
	 * The method is responsible for:
	 * </p>
	 * <ol>
	 *   <li>Creating the correct description subtype (species, action, statement, etc.)</li>
	 *   <li>Processing facets and validating required parameters</li>
	 *   <li>Establishing parent-child relationships</li>
	 *   <li>Linking to the source EObject for error reporting</li>
	 * </ol>
	 * 
	 * <h3>Example:</h3>
	 * <pre>{@code
	 * // Called during compilation
	 * IDescription desc = factory.buildDescription(
	 *     "species",                    // keyword
	 *     facets,                       // name="my_agent", parent="agent"
	 *     eObject,                      // source code reference
	 *     childDescriptions,            // actions, variables, etc.
	 *     modelDescription,             // enclosing model
	 *     symbolPrototype               // species prototype
	 * );
	 * }</pre>
	 *
	 * @param keyword the GAML keyword (e.g., "species", "action", "if")
	 * @param facets the facets (properties) of the symbol
	 * @param element the source EObject for error reporting and location tracking
	 * @param children the child descriptions (nested elements)
	 * @param enclosing the enclosing description (parent in the hierarchy)
	 * @param proto the symbol prototype containing metadata about this symbol type
	 * @return a new description instance appropriate for this symbol type
	 */
	IDescription buildDescription(String keyword, Facets facets, EObject element, Iterable<IDescription> children,
			IDescription enclosing, IArtefactProto.Symbol proto);

	/**
	 * Returns the symbol kinds that this factory can create.
	 * 
	 * <p>
	 * This method identifies which symbol categories this factory handles. Factories
	 * are registered and retrieved based on these kinds.
	 * </p>
	 * 
	 * <h3>Example:</h3>
	 * <pre>{@code
	 * @Override
	 * public int[] getKinds() {
	 *     return new int[] { 
	 *         ISymbolKind.SPECIES,
	 *         ISymbolKind.GRID
	 *     };
	 * }
	 * }</pre>
	 *
	 * @return an array of symbol kind constants from {@link ISymbolKind}
	 */
	int[] getKinds();

}
