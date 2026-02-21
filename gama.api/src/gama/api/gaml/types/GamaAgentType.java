/*******************************************************************************************************
 *
 * GamaAgentType.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.types;

import gama.annotations.support.ISymbolKind;
import gama.api.compilation.descriptions.ISpeciesDescription;
import gama.api.compilation.documentation.GamlRegularDocumentation;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.species.ISpecies;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.IPoint;

/**
 * Represents the GAML type for agents of a specific species.
 * <p>
 * This type is used to represent agents belonging to a particular species in the GAMA simulation.
 * It provides type-safe casting operations for agent instances, supports species membership checks,
 * and handles agent lookup by ID or position. Each agent type is parameterized by the species it represents.
 * </p>
 * <p>
 * Agent types are drawable, cannot be cast to constants, and support dynamic species-based type checking.
 * </p>
 * 
 * @param <T> the specific agent class this type represents
 * 
 * @author drogoul
 * @since GAMA 1.0
 * @see IAgent
 * @see ISpecies
 * @see ISpeciesDescription
 */
public class GamaAgentType<T extends IAgent> extends GamaType<T> {

	/** The species description associated with this agent type. */
	ISpeciesDescription species;

	/**
	 * Constructs a new GamaAgentType from a species description.
	 * <p>
	 * This constructor automatically derives the type name and Java base class from the species description.
	 * </p>
	 *
	 * @param typesManager the types manager for type resolution
	 * @param species the species description defining the agent type
	 * @param id the unique identifier for this type
	 */
	@SuppressWarnings ("unchecked")
	public GamaAgentType(final ITypesManager typesManager, final ISpeciesDescription species, final int id) {
		this(typesManager, species, species.getName(), (Class<T>) species.getJavaBase(), id);
	}

	/**
	 * Constructs a new GamaAgentType with full parameters.
	 * <p>
	 * This is the primary constructor that initializes all aspects of the agent type including
	 * its name, Java support class, and species association.
	 * </p>
	 *
	 * @param typesManager the types manager for type resolution and management
	 * @param species the species description defining the agent type
	 * @param name the name of this type
	 * @param support the Java class that supports this type
	 * @param id the unique identifier for this type
	 */
	public GamaAgentType(final ITypesManager typesManager, final ISpeciesDescription species, final String name,
			final Class<T> support, final int id) {
		super(typesManager);
		this.species = species;
		this.id = id;
		this.name = name;
		this.varKind = ISymbolKind.Variable.REGULAR;
		this.support = support;
	}

	/**
	 * Performs type-specific initialization.
	 * <p>
	 * Agent types do not require additional initialization beyond the constructor.
	 * </p>
	 */
	@Override
	protected void init() {}

	/**
	 * Checks if this type is assignable from another type.
	 * <p>
	 * This method extends the default assignability check to handle cross-model species references,
	 * addressing issue #1999 where species from imported models may have different type managers
	 * but represent the same species.
	 * </p>
	 * 
	 * @param t the type to check for assignability
	 * @return true if t can be assigned to this type, false otherwise
	 */
	@Override
	public boolean isAssignableFrom(final IType<?> t) {
		final boolean assignable = super.isAssignableFrom(t);
		// Hack to circumvent issue #1999. Should be better handled by
		// letting type managers of comodels inherit from the type managers
		// of imported models.
		if (!assignable && t.isAgentType() && t.getSpecies() == getSpecies()) return true;
		return assignable;
	}

	/**
	 * Gets the plugin that defines this agent type.
	 * 
	 * @return the name of the plugin defining the associated species
	 */
	@Override
	public String getDefiningPlugin() { return getSpecies().getDefiningPlugin(); }

	/**
	 * Casts an object to this agent type.
	 * <p>
	 * The casting behavior depends on the input type:
	 * <ul>
	 * <li>If obj is null or scope/model is null, returns null</li>
	 * <li>If obj is an IAgent, checks species membership and returns it if valid, null otherwise</li>
	 * <li>If obj is an Integer, treats it as an agent ID and retrieves the agent from the population</li>
	 * <li>If obj is an IPoint, retrieves the agent at that location from the population</li>
	 * </ul>
	 * </p>
	 *
	 * @param scope the execution scope
	 * @param obj the object to cast
	 * @param param optional parameter (can be a specific species to use for lookup)
	 * @param copy whether to copy the result (not applicable for agents)
	 * @return the agent instance, or null if casting fails
	 * @throws GamaRuntimeException if an error occurs during casting
	 */
	@SuppressWarnings ("unchecked")
	@Override
	public T cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		if (obj == null || scope == null || scope.getModel() == null) return null;
		final ISpecies species = param instanceof ISpecies sp ? sp : scope.getModel().getSpecies(getName());
		if (species == null) return (T) Types.AGENT.cast(scope, obj, param, copy);
		if (obj instanceof IAgent ia) return ia.isInstanceOf(species, false) ? (T) ia : null;
		final IAgent agent = scope.getAgent();
		if (agent != null) {
			if (obj instanceof Integer i) return (T) agent.getPopulationFor(species).getAgent(i);
			if (obj instanceof IPoint p) return (T) agent.getPopulationFor(species).getAgent(scope, p);
		}
		return null;
	}

	/**
	 * Returns the default value for this type.
	 * 
	 * @return null, as agents have no default value
	 */
	@Override
	public T getDefault() { return null; }

	/**
	 * Indicates whether this is an agent type.
	 * 
	 * @return true, as this is an agent type
	 */
	@Override
	public boolean isAgentType() { return true; }

	/**
	 * Gets the name of the species this type represents.
	 * 
	 * @return the species name
	 */
	@Override
	public String getSpeciesName() { return name; }

	/**
	 * Gets the species description associated with this type.
	 * 
	 * @return the species description
	 */
	@Override
	public ISpeciesDescription getSpecies() { return species; }

	/**
	 * Indicates whether values of this type can be cast to constants.
	 * 
	 * @return false, as agent instances cannot be constants
	 */
	@Override
	public boolean canCastToConst() {
		return false;
	}

	/**
	 * Checks whether a given object can be of this type.
	 * <p>
	 * In addition to the standard type check, this method verifies species membership for agent objects.
	 * </p>
	 * 
	 * @param scope the execution scope
	 * @param obj the object to check
	 * @return true if obj can be of this type, false otherwise
	 */
	@Override
	public boolean canBeTypeOf(final IScope scope, final Object obj) {
		final boolean b = super.canBeTypeOf(scope, obj);
		if (b) return true;
		if (obj instanceof IAgent) {
			final ISpecies s = scope.getModel().getSpecies(getSpeciesName());
			return ((IAgent) obj).isInstanceOf(s, false);
		}
		return false;
	}

	/**
	 * Gets the documentation for this type.
	 * <p>
	 * Generates documentation describing the species and its attributes.
	 * </p>
	 * 
	 * @return the documentation for this agent type
	 */
	@Override
	public IGamlDocumentation getDocumentation() {
		IGamlDocumentation result =
				new GamlRegularDocumentation("Represents instances of species " + getSpecies().getName());
		getSpecies().documentAttributes(result);
		return result;
	}

	/**
	 * Gets the key type for this type (when treated as a container).
	 * 
	 * @return the STRING type, as agents can be indexed by attribute names
	 */
	@Override
	public IType<String> getKeyType() { return Types.STRING; }

	/**
	 * Indicates whether this type has a fixed length.
	 * 
	 * @return false, as agent populations can vary in size
	 */
	@Override
	public boolean isFixedLength() { return false; }

	/**
	 * Indicates whether values of this type can be drawn/visualized.
	 * 
	 * @return true, as agents can be visualized
	 */
	@Override
	public boolean isDrawable() { return true; }

}
