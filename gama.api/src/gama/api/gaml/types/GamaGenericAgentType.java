/*******************************************************************************************************
 *
 * GamaGenericAgentType.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
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
import gama.api.compilation.descriptions.ISpeciesDescription;
import gama.api.compilation.documentation.GamlConstantDocumentation;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.kernel.GamaMetaModel;
import gama.api.kernel.agent.IAgent;
import gama.api.runtime.scope.IScope;

/**
 * The generic agent type - base type for all agents in GAML models.
 * <p>
 * GamaGenericAgentType represents the fundamental "agent" type in GAMA, serving as the base type for all agents in a
 * model. While specific species define specialized agent types, the generic agent type provides a common interface for
 * operations that work with any agent regardless of species. This is the default type when an agent is referenced
 * without species-specific typing.
 * </p>
 * 
 * <h2>Key Characteristics:</h2>
 * <ul>
 * <li>Base type for all agent types in the type hierarchy</li>
 * <li>Provides common agent interface and operations</li>
 * <li>Every species type is a subtype of agent</li>
 * <li>Allows generic agent manipulation across species</li>
 * <li>Default type for unspecified agent references</li>
 * </ul>
 * 
 * <h2>Type Hierarchy:</h2>
 * 
 * <pre>
 * agent (GamaGenericAgentType)
 *   ├─ world (model/experiment agent)
 *   ├─ species1 (user-defined species)
 *   ├─ species2 (user-defined species)
 *   │   └─ subspecies (inherited species)
 *   └─ grid (grid agents)
 * </pre>
 * 
 * <h2>Common Agent Features:</h2>
 * <p>
 * All agents, regardless of species, have base attributes and capabilities:
 * <ul>
 * <li><b>name</b> - unique identifier string</li>
 * <li><b>location</b> - spatial position (point)</li>
 * <li><b>shape</b> - geometric representation (geometry)</li>
 * <li><b>host</b> - containing agent (for micro-models)</li>
 * <li>Life-cycle management (creation, death)</li>
 * <li>Basic behaviors and reflexes</li>
 * </ul>
 * </p>
 * 
 * <h2>Usage Examples:</h2>
 * 
 * <pre>
 * {@code
 * // Generic agent variable (can hold any species)
 * agent any_agent <- one_of(person);
 * any_agent <- one_of(vehicle);  // Can be reassigned to different species
 * 
 * // Generic agent operations
 * action interact_with(agent other) {
 *     // Works with any agent type
 *     float dist <- distance_to(other);
 *     write "Distance to " + other.name + ": " + dist;
 * }
 * 
 * // Casting to agent
 * agent ag <- agent(self);
 * 
 * // Collections of mixed species
 * list<agent> all_entities <- (person as list) + (vehicle as list);
 * 
 * // Generic agent queries
 * agent nearest <- agent closest_to(self);
 * list<agent> nearby <- agents at_distance(50.0);
 * 
 * // Check if something is an agent
 * bool is_agent <- my_value is agent;
 * }
 * </pre>
 * 
 * <h2>Relationship with Species Types:</h2>
 * <p>
 * While species define specific agent types, the generic agent type provides:
 * <ul>
 * <li>Common interface for all agent operations</li>
 * <li>Type compatibility across different species</li>
 * <li>Generic algorithms that work with any agent</li>
 * <li>Polymorphic behavior in collections and parameters</li>
 * </ul>
 * </p>
 * 
 * <h2>Type Safety:</h2>
 * <p>
 * The agent type balances flexibility with type safety:
 * <ul>
 * <li>Generic enough to work with any species</li>
 * <li>Provides access to common agent attributes and methods</li>
 * <li>Species-specific attributes require type casting or species-typed variables</li>
 * </ul>
 * </p>
 * 
 * @author GAMA Development Team
 * @see GamaAgentType
 * @see gama.api.kernel.agent.IAgent
 * @since GAMA 1.0
 */
@type (
		name = IKeyword.AGENT,
		id = IType.AGENT,
		wraps = { IAgent.class },
		kind = ISymbolKind.REGULAR,
		concept = { IConcept.TYPE, IConcept.SPECIES },
		doc = @doc ("The basic and default type of agents in GAML"))
public class GamaGenericAgentType extends GamaAgentType<IAgent> {

	/**
	 * Constructs a new generic agent type.
	 * 
	 * @param typesManager
	 *            the types manager responsible for type resolution and management
	 */
	public GamaGenericAgentType(final ITypesManager typesManager) {
		super(typesManager, null, IKeyword.AGENT, IAgent.class, IType.AGENT);
	}

	/**
	 * Casts an object to an agent with type parameters.
	 * <p>
	 * Delegates to the simple cast method as generic agent type doesn't use key/content type parameters.
	 * </p>
	 * 
	 * @param scope
	 *            the current execution scope
	 * @param obj
	 *            the object to cast to an agent
	 * @param param
	 *            optional parameter
	 * @param keyType
	 *            the key type (not used for agents)
	 * @param contentsType
	 *            the content type (not used for agents)
	 * @param copy
	 *            whether to create a copy
	 * @return the agent if obj is an agent, null otherwise
	 * @throws GamaRuntimeException
	 *             if the casting operation encounters an error
	 */
	@Override
	public IAgent cast(final IScope scope, final Object obj, final Object param, final IType<?> keyType,
			final IType<?> contentsType, final boolean copy) throws GamaRuntimeException {
		return cast(scope, obj, param, copy);
	}

	/**
	 * Returns the species description for the generic agent type.
	 * <p>
	 * Retrieves the built-in "agent" species description from the meta-model.
	 * </p>
	 * 
	 * @return the agent species description
	 */
	@Override
	public ISpeciesDescription getSpecies() {
		if (species == null) { species = GamaMetaModel.getSpeciesDescription(IKeyword.AGENT); }
		return species;
	}

	/**
	 * Casts an object to an agent.
	 * <p>
	 * Only accepts actual IAgent instances; returns null for all other types. This is stricter than species-specific
	 * agent types which may perform conversions.
	 * </p>
	 * 
	 * @param scope
	 *            the current execution scope
	 * @param obj
	 *            the object to cast to an agent
	 * @param param
	 *            optional parameter (not used)
	 * @param copy
	 *            whether to create a copy (agents are not copied)
	 * @return the agent if obj is an IAgent, null otherwise
	 * @throws GamaRuntimeException
	 *             if the casting operation encounters an error
	 */
	@Override
	@doc ("Returns an agent if the argument is already an agent, otherwise returns null")
	public IAgent cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		if (obj == null) return getDefault();
		if (obj instanceof IAgent a) return a;
		return getDefault();
	}

	/**
	 * Returns the documentation for the generic agent type.
	 * 
	 * @return documentation describing the generic agent type
	 */
	@Override
	public IGamlDocumentation getDocumentation() {
		return new GamlConstantDocumentation("Generic type of all agents in a model");
	}

	/**
	 * Checks if this type is a supertype of another type.
	 * <p>
	 * The generic agent type is a supertype of all GamaAgentType subclasses except itself.
	 * </p>
	 * 
	 * @param type
	 *            the type to check
	 * @return true if type is a GamaAgentType and not this type
	 */
	@Override
	public boolean isSuperTypeOf(final IType<?> type) {
		return type != this && type instanceof GamaAgentType;
	}

}
