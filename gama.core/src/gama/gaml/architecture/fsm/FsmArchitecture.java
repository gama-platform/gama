/*******************************************************************************************************
 *
 * FsmArchitecture.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.architecture.fsm;

import java.util.Map;

import gama.annotations.doc;
import gama.annotations.getter;
import gama.annotations.setter;
import gama.annotations.skill;
import gama.annotations.variable;
import gama.annotations.vars;
import gama.annotations.support.IConcept;
import gama.api.constants.IKeyword;
import gama.api.data.factories.GamaMapFactory;
import gama.api.data.objects.IList;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.statements.IStatement;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.species.ISpecies;
import gama.api.runtime.scope.IScope;
import gama.api.utils.list.GamaListFactory;
import gama.gaml.architecture.reflex.ReflexArchitecture;

/**
 * Written by drogoul Modified on 12 sept. 2010
 *
 * @todo Description
 *
 */
@vars ({ @variable (
		name = IKeyword.STATE,
		type = IType.STRING,
		doc = @doc ("Returns the name of the current state of the agent")),
		@variable (
				name = IKeyword.STATES,
				type = IType.LIST,
				constant = true,
				doc = @doc ("Returns the list of all the states defined in the species")) })
@skill (
		name = IKeyword.FSM,
		concept = { IConcept.BEHAVIOR, IConcept.ARCHITECTURE },
		doc = @doc ("The Finite State Machine architecture allows to program agents using a finite set of states and conditional transitions between them"))
public class FsmArchitecture extends ReflexArchitecture {

	/** The states. */
	protected final Map<String, FsmStateStatement> states = GamaMapFactory.createUnordered();

	/** The initial state. */
	protected FsmStateStatement initialState;

	@Override
	protected void clearBehaviors() {
		super.clearBehaviors();
		states.clear();
	}

	@Override
	public void verifyBehaviors(final ISpecies context) {
		super.verifyBehaviors(context);
		for (final FsmStateStatement s : states.values()) { if (s.isInitial()) { initialState = s; } }
		if (initialState != null) { context.getVar(IKeyword.STATE).setValue(null, initialState.getName()); }
	}

	/**
	 * Gets the state names.
	 *
	 * @param agent
	 *            the agent
	 * @return the state names
	 */
	@getter (
			value = IKeyword.STATES,
			initializer = true)
	public IList<String> getStateNames(final IAgent agent) {
		return GamaListFactory.wrap(Types.STRING, states.keySet());
	}

	/**
	 * Sets the state names.
	 *
	 * @param agent
	 *            the agent
	 * @param list
	 *            the list
	 */
	@setter (IKeyword.STATES)
	public void setStateNames(final IAgent agent, final IList<String> list) {}

	/**
	 * Gets the state name.
	 *
	 * @param agent
	 *            the agent
	 * @return the state name
	 */
	@getter (IKeyword.STATE)
	public String getStateName(final IAgent agent) {
		return (String) agent.getAttribute(IKeyword.CURRENT_STATE);
	}

	/**
	 * Gets the state.
	 *
	 * @param stateName
	 *            the state name
	 * @return the state
	 */
	public FsmStateStatement getState(final String stateName) {
		return states.get(stateName);
	}

	/**
	 * Sets the state name.
	 *
	 * @param agent
	 *            the agent
	 * @param stateName
	 *            the state name
	 */
	@setter (IKeyword.STATE)
	public void setStateName(final IAgent agent, final String stateName) {
		if (stateName != null && states.containsKey(stateName)) { setCurrentState(agent, states.get(stateName)); }
	}

	@Override
	public void addBehavior(final IStatement c) {
		if (c instanceof final FsmStateStatement state) {
			states.put(state.getName(), state);
		} else {
			super.addBehavior(c);
		}
	}

	@Override
	public Object executeOn(final IScope scope) throws GamaRuntimeException {
		super.executeOn(scope);
		return executeCurrentState(scope);
	}

	/**
	 * Execute current state.
	 *
	 * @param scope
	 *            the scope
	 * @return the object
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	protected Object executeCurrentState(final IScope scope) throws GamaRuntimeException {
		final IAgent agent = getCurrentAgent(scope);
		if (scope.interrupted()) return null;
		final FsmStateStatement currentState = getState((String) agent.getAttribute(IKeyword.CURRENT_STATE));
		if (currentState == null) return null;
		return scope.execute(currentState).getValue();
	}

	/**
	 * Sets the current state.
	 *
	 * @param agent
	 *            the agent
	 * @param state
	 *            the state
	 */
	public void setCurrentState(final IAgent agent, final FsmStateStatement state) {
		final FsmStateStatement currentState = getState((String) agent.getAttribute(IKeyword.CURRENT_STATE));
		if (currentState == state) return;
		// if ( currentState != null && currentState.hasExitActions() ) {
		// agent.setAttribute(IKeyword.STATE_TO_EXIT, currentState);
		// }
		agent.setAttribute(IKeyword.ENTER, true);
		agent.setAttribute(IKeyword.CURRENT_STATE, state.getName());
	}

	/***
	 * What happens when the agent dies: calls the exit statement of the current state if it exists (see Issue #2865)
	 */
	@Override
	public boolean abort(final IScope scope) throws GamaRuntimeException {
		final IAgent agent = getCurrentAgent(scope);
		if (scope.interrupted() || agent == null) return true;
		final FsmStateStatement currentState = getState((String) agent.getAttribute(IKeyword.CURRENT_STATE));
		if (currentState == null) return true;
		currentState.haltOn(scope);
		// and we return the regular abort behavior
		return super.abort(scope);
	}
}
