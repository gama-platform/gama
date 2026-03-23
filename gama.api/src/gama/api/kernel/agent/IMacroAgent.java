/*******************************************************************************************************
 *
 * IMacroAgent.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.agent;

import gama.annotations.doc;
import gama.annotations.getter;
import gama.annotations.setter;
import gama.annotations.variable;
import gama.annotations.vars;
import gama.annotations.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.IType;
import gama.api.kernel.species.ISpecies;
import gama.api.runtime.scope.IScope;
import gama.api.types.list.IList;
import gama.api.types.misc.IContainer;

/**
 * The Interface IMacroAgent.
 * 
 * <p>
 * Represents an agent that can host and manage populations of other agents (micro-agents) in GAMA. A macro-agent acts
 * as a container for micro-species, allowing for hierarchical multi-level modeling where agents can contain other
 * agents.
 * </p>
 * 
 * <h3>Core Concepts</h3>
 * <ul>
 * <li><b>Host-Member Relationship:</b> Macro-agents host populations of micro-agents (members)</li>
 * <li><b>Multi-Level Modeling:</b> Enables nested agent structures and hierarchical simulations</li>
 * <li><b>Dynamic Populations:</b> Can capture, release, and migrate agents between populations</li>
 * <li><b>Species Management:</b> Manages multiple micro-species simultaneously</li>
 * </ul>
 * 
 * <h3>Key Operations</h3>
 * <ul>
 * <li><b>Capture:</b> Take external agents and make them members of the macro-agent</li>
 * <li><b>Release:</b> Free micro-agents back to their parent population</li>
 * <li><b>Migration:</b> Move agents between different micro-species within the same host</li>
 * </ul>
 * 
 * <h3>Usage in GAML</h3>
 * 
 * <h4>1. Basic Multi-Level Model</h4>
 * 
 * <pre>
 * <code>
 * species city {
 *     species building {
 *         species room {
 *             // Three-level hierarchy: city -> building -> room
 *         }
 *     }
 * }
 * 
 * global {
 *     init {
 *         create city number: 3 {
 *             create building number: 10;
 *         }
 *     }
 * }
 * </code>
 * </pre>
 * 
 * <h4>2. Capturing Agents</h4>
 * 
 * <pre>
 * <code>
 * species building {
 *     species tenant;
 *     
 *     reflex capture_nearby_people {
 *         list&lt;person&gt; nearby_people <- person at_distance 10.0;
 *         capture nearby_people as: tenant;
 *         // People are now members of this building
 *     }
 * }
 * 
 * species person skills: [moving] {
 *     // Can be captured by buildings
 * }
 * </code>
 * </pre>
 * 
 * <h4>3. Releasing Agents</h4>
 * 
 * <pre>
 * <code>
 * species building {
 *     species tenant;
 *     
 *     reflex release_tenants when: flip(0.1) {
 *         release list(tenant);
 *         // Tenants become regular person agents again
 *     }
 * }
 * </code>
 * </pre>
 * 
 * <h4>4. Accessing Members</h4>
 * 
 * <pre>
 * <code>
 * species company {
 *     species employee;
 *     species manager;
 *     
 *     reflex manage {
 *         // Access all direct members
 *         ask members {
 *             do work;
 *         }
 *         
 *         // Access specific micro-population
 *         ask employee {
 *             salary <- salary * 1.1;
 *         }
 *     }
 * }
 * </code>
 * </pre>
 * 
 * <h4>5. Migrating Between Micro-Species</h4>
 * 
 * <pre>
 * <code>
 * species organization {
 *     species employee;
 *     species manager;
 *     
 *     reflex promote {
 *         list&lt;employee&gt; candidates <- employee where (each.experience > 5);
 *         if !empty(candidates) {
 *             release candidates as: manager in: self;
 *             // Employees become managers within the same organization
 *         }
 *     }
 * }
 * </code>
 * </pre>
 * 
 * <h3>Implementation Notes</h3>
 * <p>
 * When implementing IMacroAgent:
 * </p>
 * <ul>
 * <li>Micro-species must be declared as nested species in GAML</li>
 * <li>Captured agents change their species and host reference</li>
 * <li>Released agents return to their original parent population</li>
 * <li>The 'members' attribute provides access to all direct micro-agents</li>
 * <li>The 'agents' attribute includes all direct and indirect micro-agents</li>
 * </ul>
 * 
 * @see IAgent
 * @see IPopulation
 * @see gama.api.kernel.species.ISpecies
 * @author drogoul
 * @since GAMA 1.0
 */
@vars ({ @variable (
		name = IKeyword.MEMBERS,
		// Changed from IType.LIST; see issue #3264
		type = IType.CONTAINER,
		of = IType.AGENT,
		doc = { @doc ("Returns the list of agents for the population(s) of which the receiver agent is a direct host") }),
		@variable (
				name = IKeyword.AGENTS,
				type = IType.LIST,
				of = IType.AGENT,
				doc = { @doc ("Returns the list of agents for the population(s) of which the receiver agent is a direct or undirect host") }) })
public interface IMacroAgent extends IAgent {

	/**
	 * Verifies if this agent can capture other agent as the specified micro-species.
	 *
	 * An agent A can capture another agent B as newSpecies if the following conditions are correct: 1. other is not
	 * this agent; 2. other is not "world" agent; 3. newSpecies is a (direct) micro-species of A's species; 4.
	 * newSpecies is a direct sub-species of B's species.
	 *
	 * @param other
	 * @return true if this agent can capture other agent false otherwise
	 */
	boolean canCapture(IAgent other, ISpecies newSpecies);

	/**
	 * Capture micro agent.
	 *
	 * @param scope
	 *            the scope
	 * @param microSpecies
	 *            the micro species
	 * @param microAgent
	 *            the micro agent
	 * @return the i agent
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	IAgent captureMicroAgent(IScope scope, final ISpecies microSpecies, final IAgent microAgent)
			throws GamaRuntimeException;

	/**
	 * Captures some agents as micro-agents with the specified micro-species as their new species.
	 *
	 * @param microSpecies
	 *            the species that the captured agents will become, this must be a micro-species of this agent's
	 *            species.
	 * @param microAgents
	 * @return
	 * @throws GamaRuntimeException
	 */
	IList<IAgent> captureMicroAgents(IScope scope, final ISpecies microSpecies, final IList<IAgent> microAgents)
			throws GamaRuntimeException;

	/**
	 * Returns all the agents which consider this agent as direct host.
	 *
	 * @return
	 */
	@getter (IKeyword.MEMBERS)
	IContainer<?, IAgent> getMembers(IScope scope);

	/**
	 * Returns the population of the specified (direct) micro-species.
	 *
	 * @param microSpecies
	 * @return
	 */
	IPopulation<? extends IAgent> getMicroPopulation(ISpecies microSpecies);

	/**
	 * Returns the population of the specified (direct) micro-species.
	 *
	 * @param microSpeciesName
	 * @return
	 */
	IPopulation<? extends IAgent> getMicroPopulation(String microSpeciesName);

	/**
	 * Returns a list of populations of (direct) micro-species.
	 *
	 * @return
	 */
	IPopulation<? extends IAgent>[] getMicroPopulations();

	/**
	 * Verifies if this agent contains micro-agents or not.
	 *
	 * @return true if this agent contains micro-agent(s) false otherwise
	 */
	boolean hasMembers();

	/**
	 * Gets the members size.
	 *
	 * @param scope
	 *            the scope
	 * @return the members size
	 */
	int getMembersSize(final IScope scope);

	/**
	 * Initialize micro population.
	 *
	 * @param scope
	 *            the scope
	 * @param name
	 *            the name
	 */
	void initializeMicroPopulation(IScope scope, String name);

	/**
	 * Initialize Populations to manage micro-agents.
	 */
	// public abstract void initializeMicroPopulations(IScope scope);

	/**
	 * Migrates some micro-agents from one micro-species to another micro-species of this agent's species.
	 *
	 * @param microAgent
	 * @param newMicroSpecies
	 * @return
	 */
	IList<IAgent> migrateMicroAgents(IScope scope, final IList<IAgent> microAgents, final ISpecies newMicroSpecies);

	/**
	 * Migrates some micro-agents from one micro-species to another micro-species of this agent's species.
	 *
	 * @param microAgent
	 * @param newMicroSpecies
	 * @return
	 */
	IList<IAgent> migrateMicroAgents(IScope scope, final ISpecies oldMicroSpecies, final ISpecies newMicroSpecies);

	/**
	 * Releases some micro-agents of this agent.
	 *
	 * @param microAgents
	 * @return
	 * @throws GamaRuntimeException
	 */
	IList<IAgent> releaseMicroAgents(IScope scope, final IList<IAgent> microAgents) throws GamaRuntimeException;

	/**
	 * Sets the members.
	 *
	 * @param members
	 *            the new members
	 */
	@setter (IKeyword.MEMBERS)
	void setMembers(IList<IAgent> members);

	/**
	 * Sets the agents.
	 *
	 * @param agents
	 *            the new agents
	 */
	@setter (IKeyword.AGENTS)
	void setAgents(IList<IAgent> agents);

	/**
	 * Returns all the agents which consider this agent as direct or in-direct host.
	 *
	 * @return
	 */
	@getter (IKeyword.AGENTS)
	IList<IAgent> getAgents(IScope scope);

	/**
	 * Adds the extern micro population.
	 *
	 * @param expName
	 *            the exp name
	 * @param pop
	 *            the pop
	 */
	void addExternMicroPopulation(final String expName, final IPopulation<? extends IAgent> pop);

	/**
	 * Gets the extern micro population for.
	 *
	 * @param expName
	 *            the exp name
	 * @return the extern micro population for
	 */
	IPopulation<? extends IAgent> getExternMicroPopulationFor(final String expName);

}