/*******************************************************************************************************
 *
 * GamaSpeciesType.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.types;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.type;
import gama.annotations.usage;
import gama.annotations.constants.IKeyword;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IPopulationSet;
import gama.api.kernel.species.ISpecies;
import gama.api.runtime.scope.IScope;

/**
 * Meta-type representing species in GAML - the templates for creating agents.
 * <p>
 * Species are the fundamental building blocks of agent-based models in GAMA. A species defines the blueprint for
 * creating agents, specifying their attributes, behaviors, and relationships. The species type allows species
 * themselves to be manipulated as first-class objects in GAML, enabling meta-programming and dynamic model
 * construction.
 * </p>
 * 
 * <h2>Key Features:</h2>
 * <ul>
 * <li>Template for agent creation</li>
 * <li>Defines agent attributes and behaviors</li>
 * <li>Supports inheritance hierarchies</li>
 * <li>Can be treated as container of agents (population)</li>
 * <li>Provides access to population-level operations</li>
 * <li>Drawable for visualizing all agents of a species</li>
 * <li>Dynamic species manipulation</li>
 * </ul>
 * 
 * <h2>Species as Containers:</h2>
 * <p>
 * Species act as containers of their agent instances (population). They provide:
 * <ul>
 * <li>Access to all agents of that species</li>
 * <li>Population-level queries and operations</li>
 * <li>Topology for spatial operations among agents</li>
 * </ul>
 * </p>
 * 
 * <h2>Usage Examples:</h2>
 * 
 * <pre>
 * {@code
 * // Define a species
 * species person {
 *     int age;
 *     string name;
 *     
 *     reflex aging {
 *         age <- age + 1;
 *     }
 * }
 * 
 * // Species as type for variables
 * species my_species <- person;
 * 
 * // Access species from agent
 * species agent_species <- species(self);
 * species str_species <- species("person");
 * 
 * // Species as container (population access)
 * int population_size <- length(person);
 * list<person> all_persons <- person as list;
 * person random_person <- one_of(person);
 * 
 * // Create agents from species
 * create my_species number: 10;
 * 
 * // Species inheritance
 * species child parent: person {
 *     // Inherits age, name, and aging reflex
 *     int grade;
 * }
 * 
 * // Check species type
 * bool is_person <- agent1.species = person;
 * 
 * // Dynamic species reference
 * list<species> all_species <- [person, child, vehicle];
 * loop sp over: all_species {
 *     create sp number: 5;
 * }
 * }
 * </pre>
 * 
 * <h2>Meta-programming:</h2>
 * <p>
 * The species type enables powerful meta-programming capabilities:
 * <ul>
 * <li>Dynamic species selection and instantiation</li>
 * <li>Runtime inspection of species properties</li>
 * <li>Generic algorithms operating on any species</li>
 * <li>Model generation and modification</li>
 * </ul>
 * </p>
 * 
 * @author GAMA Development Team
 * @see GamaContainerType
 * @see gama.api.kernel.species.ISpecies
 * @see gama.api.kernel.agent.IAgent
 * @since GAMA 1.0
 */
@type (
		name = IKeyword.SPECIES,
		id = IType.SPECIES,
		wraps = { ISpecies.class },
		kind = ISymbolKind.REGULAR,
		concept = { IConcept.TYPE, IConcept.SPECIES },
		doc = @doc ("Meta-type of the species present in the GAML language"))
@SuppressWarnings ({ "rawtypes", "unchecked" })
public class GamaSpeciesType extends GamaContainerType<ISpecies> {

	/**
	 * Constructs a new species type.
	 * 
	 * @param typesManager
	 *            the types manager responsible for type resolution and management
	 */
	public GamaSpeciesType(final ITypesManager typesManager) {
		super(typesManager);
	}

	/**
	 * Casts an object to a species.
	 * <p>
	 * This method supports casting from various source types:
	 * <ul>
	 * <li>null - returns null</li>
	 * <li>ISpecies - returns the species itself</li>
	 * <li>IAgent - returns the agent's species</li>
	 * <li>String - looks up and returns the species with that name in the current model</li>
	 * <li>IPopulationSet - returns the species of that population</li>
	 * <li>Other types - returns null</li>
	 * </ul>
	 * </p>
	 * 
	 * @param scope
	 *            the current execution scope
	 * @param obj
	 *            the object to cast to a species
	 * @param param
	 *            optional parameter (not used for species casting)
	 * @param copy
	 *            whether to create a copy (not applicable for species)
	 * @return the species if found, null otherwise
	 * @throws GamaRuntimeException
	 *             if the casting operation encounters an error
	 */
	@Override
	@doc (
			value = "casting of the operand to a species.",
			usages = { @usage ("if the operand is nil, returns nil;"),
					@usage ("if the operand is an agent, returns its species;"),
					@usage ("if the operand is a string, returns the species with this name (nil if not found);"),
					@usage ("otherwise, returns nil") },
			examples = { @example (
					value = "species(self)",
					equals = "the species of the current agent",
					isExecutable = false),
					@example (
							value = "species('node')",
							equals = "node",
							isExecutable = false),
					@example (
							value = "species([1,5,9,3])",
							equals = "nil",
							isExecutable = false),
					@example (
							value = "species(node1)",
							equals = "node",
							isExecutable = false) })
	public ISpecies cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		// TODO Add a more general cast with list of agents to find a common
		// species.
		ISpecies species =
				obj == null ? getDefault() : obj instanceof ISpecies i ? i : obj instanceof IAgent i ? i.getSpecies()
						: obj instanceof String s
								? scope.getModel() != null ? scope.getModel().getSpecies(s) : getDefault()
						: getDefault();
		if (obj instanceof IPopulationSet) { species = ((IPopulationSet) obj).getSpecies(); }
		return species;
	}

	/**
	 * Casts an object to a species with type parameters.
	 * <p>
	 * This variant handles parametric casting where the content type may specify an agent type. If the result is null
	 * but contentType is an agent type, attempts to find a species with that type's name.
	 * </p>
	 * 
	 * @param scope
	 *            the current execution scope
	 * @param obj
	 *            the object to cast to a species
	 * @param param
	 *            optional parameter
	 * @param keyType
	 *            the key type (not used)
	 * @param contentType
	 *            the content type - if agent type, used to find species by name
	 * @param copy
	 *            whether to create a copy
	 * @return the species if found, null otherwise
	 */
	@Override
	public ISpecies cast(final IScope scope, final Object obj, final Object param, final IType keyType,
			final IType contentType, final boolean copy) {

		final ISpecies result = cast(scope, obj, param, copy);
		if (result == null && contentType.isAgentType()) return scope.getModel().getSpecies(contentType.getName());
		return result;
	}

	/**
	 * Returns the default value for species type.
	 * <p>
	 * The default species is null, as there is no meaningful default species.
	 * </p>
	 * 
	 * @return null
	 */
	@Override
	public ISpecies getDefault() { return null; }

	/**
	 * Returns the content type of species.
	 * <p>
	 * Species are containers of agents, so their content type is agent.
	 * </p>
	 * 
	 * @return the agent type
	 */
	@Override
	public IType getContentType() { return Types.get(AGENT); }

	/**
	 * Returns the key type for species containers.
	 * <p>
	 * Species populations are indexed by integer (agent index).
	 * </p>
	 * 
	 * @return the integer type
	 */
	@Override
	public IType getKeyType() { return Types.INT; }

	/**
	 * Indicates whether species can be drawn/visualized.
	 * <p>
	 * Species are drawable - drawing a species displays all its agents.
	 * </p>
	 * 
	 * @return true, species can be visualized
	 */
	@Override
	public boolean isDrawable() { return true; }

	/**
	 * Determines the content type when casting an expression to a species.
	 * <p>
	 * Analyzes the expression type to determine what agent type the species will contain:
	 * <ul>
	 * <li>For agent types: returns the agent type itself</li>
	 * <li>For species: returns the species' content type</li>
	 * <li>For strings: returns generic agent type</li>
	 * <li>Otherwise: returns the expression's type</li>
	 * </ul>
	 * </p>
	 * 
	 * @param exp
	 *            the expression being cast to a species
	 * @return the agent type that the species will contain
	 */
	@Override
	public IType contentsTypeIfCasting(final IExpression exp) {
		final IType itemType = exp.getGamlType();
		if (itemType.isAgentType()) return itemType;
		switch (exp.getGamlType().id()) {
			case SPECIES:
				return itemType.getContentType();
			case IType.STRING:
				return Types.AGENT;
		}
		return exp.getGamlType();
	}

	/**
	 * Indicates whether species can be cast to constant values.
	 * <p>
	 * Species cannot be constant as they represent dynamic populations.
	 * </p>
	 * 
	 * @return false, species are not constant
	 */
	@Override
	public boolean canCastToConst() {
		return false;
	}

}
