/*******************************************************************************************************
 *
 * AbstractAgent.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.agent;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.primitives.Ints;

import gama.annotations.action;
import gama.annotations.arg;
import gama.annotations.doc;
import gama.api.GAMA;
import gama.api.compilation.descriptions.IModelDescription;
import gama.api.constants.IKeyword;
import gama.api.data.factories.GamaListFactory;
import gama.api.data.factories.GamaMapFactory;
import gama.api.data.json.IJson;
import gama.api.data.json.IJsonValue;
import gama.api.data.objects.IList;
import gama.api.data.objects.IMap;
import gama.api.data.objects.IPoint;
import gama.api.data.objects.IShape;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.symbols.IVariable;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IMacroAgent;
import gama.api.kernel.agent.IPopulation;
import gama.api.kernel.serialization.ISerialisedAgent;
import gama.api.kernel.serialization.SerialisedAgent;
import gama.api.kernel.simulation.ISimulationAgent;
import gama.api.kernel.species.IModelSpecies;
import gama.api.kernel.species.ISpecies;
import gama.api.kernel.topology.ITopology;
import gama.api.runtime.scope.IScope;
import gama.api.utils.AgentReference;
import gama.api.utils.StringUtils;
import gama.api.utils.files.BufferingUtils;

/**
 *
 * Class AbstractAgent. An abstract class that tries to minimize the number of attributes manipulated by agents. In
 * particular, it declares no Geometry (leaving the programmer the possibility to redeclare getGeometry(), for example
 * in a dynamic fashion), no Population (leaving the programmer the possibility to redeclare getPopulation(), for
 * example in a dynamic fashion, etc.)
 *
 * These agents have no sub-population by default (but subclasses can be declared by implementing IMacroAgent, and the
 * appropriate methods can be redefined). Their name is fixed by construction (but subclasses can always implement a
 * name).
 *
 * From a functional point of view, this class delegates most of its methods to either the geometry (by calling
 * getGeometry()) or the population (by calling getPopulation()).
 *
 * Furthermore, and contrary to GamlAgent, this class does not delegate its step() and init() behaviors to GAML actions
 * (_init_ and _step_).
 *
 * Most of the methods observe a "fail-fast" pattern. That is, if either the population or the geometry of the agent is
 * null, it throws an exception and does not attempt to return guessed values.
 *
 * Abstract methods to override: - getGeometry() - getPopulation()
 *
 * @author drogoul
 * @since 18 mai 2013
 *
 */
public abstract class AbstractAgent implements IAgent {

	/** The index. */
	private final int index;

	/** The dead. */
	protected volatile boolean dead = false;

	/** The dying. */
	protected volatile boolean dying = false;

	/** The attributes. */
	protected IMap<String, Object> attributes;

	/**
	 * Instantiates a new abstract agent.
	 *
	 * @param index
	 *            the index
	 */
	protected AbstractAgent(final int index) {
		this.index = index;
	}

	/**
	 * Gets the agent.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the agent
	 * @date 17 sept. 2023
	 */
	@Override
	public IAgent getAgent() { return this; }

	/**
	 * Sets the agent.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param agent
	 *            the new agent
	 * @date 17 sept. 2023
	 */
	@Override
	public void setAgent(final IAgent agent) {}

	/**
	 * Dispose.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 17 sept. 2023
	 */
	@Override
	public void dispose() {
		if (dead) return;
		dead = true;
		final IPopulation<? extends IAgent> p = getPopulation();
		// if (p != null) { p.removeValue(null, this); }
		if (p != null && !p.isDisposing()) { p.removeValue(null, this); }
		final IShape s = getGeometry();
		if (s != null) { s.dispose(); }
		if (attributes != null) {
			attributes.clear();
			attributes = null;
		}
		BufferingUtils.getInstance().flushSaveFilesOfAgent(this);
		BufferingUtils.getInstance().flushWriteOfAgent(this);
	}

	/**
	 * String value.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @return the string
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 * @date 17 sept. 2023
	 */
	@Override
	public String stringValue(final IScope scope) throws GamaRuntimeException {
		return serializeToGaml(true);
	}

	/**
	 * Copy.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @return the i shape
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 * @date 17 sept. 2023
	 */
	@Override
	public IShape copy(final IScope scope) throws GamaRuntimeException {
		return this;
	}

	/**
	 * Serialize.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param includingBuiltIn
	 *            the including built in
	 * @return the string
	 * @date 17 sept. 2023
	 */
	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		final StringBuilder sb = new StringBuilder(30);
		// AD. See issue #3053
		sb.append(getSpeciesName()).append('(').append(getIndex()).append(')');
		if (dead()) { sb.append(" /* dead */"); }
		return sb.toString();
	}

	@Override
	public String toString() {
		return getName();
	}

	/**
	 * Gets the attributes of the agents
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the attributes
	 * @date 17 sept. 2023
	 */
	@Override
	public IMap<String, Object> getAttributes(final boolean createIfNeeded) {
		if (attributes == null && createIfNeeded) { attributes = GamaMapFactory.create(Types.STRING, Types.NO_TYPE); }
		return attributes;
	}

	/**
	 * Compare to.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param o
	 *            the o
	 * @return the int
	 * @date 17 sept. 2023
	 */
	@Override
	public int compareTo(final IAgent o) {
		return Ints.compare(getIndex(), o.getIndex());
	}

	/**
	 * Inits the.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @return true, if successful
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 * @date 17 sept. 2023
	 */
	@Override
	public boolean init(final IScope scope) throws GamaRuntimeException {
		return getSpecies().getArchitecture().init(scope) ? initSubPopulations(scope) : false;
	}

	/**
	 * Method called repetitively by the simulation engine. Should not be redefined except in rare cases (like special
	 * forms of experiments, which need to define their own sequence)
	 */
	@Override
	public boolean step(final IScope scope) throws GamaRuntimeException {
		boolean result = false;
		try {
			result = preStep(scope) && doStep(scope);
			return result;
		} finally {
			if (result) { postStep(scope); }
		}
	}

	/**
	 * This method contains everything to do *before* the actual step is done (runs of reflexes, etc.). The basis
	 * consists in updating the variables.
	 *
	 * @param scope
	 *            the scope in which the agent is asked to do the preStep()
	 * @return r
	 */
	protected boolean preStep(final IScope scope) {
		return scope.update(this).passed();
	}

	/**
	 * This method contains everything to do *during* during the step of an agent. The basis consists in asking the
	 * architecture to execute on this and, if successful, to step its sub-populations (if any). Only called if the
	 * preStep() method has been successful
	 *
	 * @param scope
	 *            the scope in which the agent is asked to do the step
	 * @return whether or not the step has been successful (i.e. no errors, etc.)
	 */
	protected boolean doStep(final IScope scope) {
		boolean populationStepPassed = scope.execute(getSpecies().getArchitecture(), this, null).passed();
		if (populationStepPassed) {
			
			
			return stepSubPopulations(scope);
	
		
		}
		return false;
	}

	/**
	 * Inits the sub populations.
	 *
	 * @param scope
	 *            the scope
	 * @return true, if successful
	 */
	protected boolean initSubPopulations(final IScope scope) {
		return true;
	}

	/**
	 * Step sub populations.
	 *
	 * @param scope
	 *            the scope
	 * @return true, if successful
	 */
	protected boolean stepSubPopulations(final IScope scope) {
		return true;
	}

	/**
	 * This method contains everything to do *after* the actual step of the agent has been done. Only called if the
	 * doStep() method has been successful.
	 *
	 * @param scope
	 */
	protected void postStep(final IScope scope) {}

	@Override
	public ITopology getTopology() { return getPopulation().getTopology(); }

	@Override
	public void setPeers(final IList<IAgent> peers) {
		// "peers" is read-only attribute
	}

	@SuppressWarnings ("unchecked")
	@Override
	public IList<IAgent> getPeers() throws GamaRuntimeException {
		if (getHost() == null) return GamaListFactory.getEmptyList();
		final IPopulation<? extends IAgent> pop = getHost().getPopulationFor(this.getSpecies());
		if (pop != null) {
			final IScope scope = getScope();
			final IList<IAgent> retVal =
					GamaListFactory.<IAgent> createWithoutCasting(scope.getType(getSpeciesName()), (List<IAgent>) pop);
			retVal.remove(this);
			return retVal;
		}
		return GamaListFactory.getEmptyList();
	}

	@Override
	public String getName() { return getSpeciesName() + getIndex() + (dead() ? " (dead)" : ""); }

	@Override
	public void setName(final String name) {}

	@Override
	public IPoint getLocation(final IScope scope) {
		return getGeometry().getLocation();
	}

	@Override
	public IPoint setLocation(final IScope scope, final IPoint l) {
		return getGeometry(scope).setLocation(l);
	}

	@Override
	public void setGeometry(final IScope scope, final IShape newGeometry) {
		getGeometry(scope).setGeometry(newGeometry);
	}

	@Override
	public boolean dead() {
		return dead;
	}

	@Override
	public IMacroAgent getHost() { return getPopulation().getHost(); }

	@Override
	public void setHost(final IMacroAgent macroAgent) {}

	@Override
	public void schedule(final IScope scope) {
		if (!dead()) { scope.init(this); }
	}

	@Override
	public final int getIndex() { return index; }

	// @Override
	// public final void setIndex(final int index) {
	// this.index = index;
	// }

	@Override
	public String getSpeciesName() { return getSpecies().getName(); }

	@Override
	public ISpecies getSpecies() { return getPopulation().getSpecies(); }

	@Override
	public boolean isInstanceOf(final ISpecies s, final boolean direct) {
		final ISpecies species = getSpecies();
		if (species == s) return true;
		if (!direct) return species.extendsSpecies(s);
		return false;
	}

	@Override
	public Object getDirectVarValue(final IScope scope, final String n) throws GamaRuntimeException {
		final IVariable var = getPopulation().getVar(n);
		if (var != null) return var.value(scope, this);
		final IMacroAgent host = this.getHost();
		if (host != null) {
			final IVariable varOfHost = host.getPopulation().getVar(n);
			if (varOfHost != null) return varOfHost.value(scope, host);
		}
		return null;
	}

	@Override
	public void setDirectVarValue(final IScope scope, final String s, final Object v) throws GamaRuntimeException {
		final IVariable var = getPopulation().getVar(s);
		if (var != null) {
			var.setVal(scope, this, v);
		} else {
			final IAgent host = this.getHost();
			if (host != null) {
				final IVariable varOfHost = host.getPopulation().getVar(s);
				if (varOfHost != null) { varOfHost.setVal(scope, host, v); }
			}
		}
		// TODO: else ? launch an error ?
	}

	/**
	 * Gets the macro agents.
	 *
	 * @return the macro agents
	 */
	protected List<IAgent> getMacroAgents() {
		final List<IAgent> retVal = GamaListFactory.create(Types.AGENT);
		IAgent currentMacro = this.getHost();
		while (currentMacro != null) {
			retVal.add(currentMacro);
			currentMacro = currentMacro.getHost();
		}
		return retVal;
	}

	@Override
	public IModelSpecies getModel() {
		final IMacroAgent a = getHost();
		if (a == null) return GAMA.getModel();
		return a.getModel();
	}

	// @Override
	// public IExperimentAgent getExperiment() {
	// return getHost().getExperiment();
	// }

	/**
	 * Gets the scope.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the scope
	 * @date 17 sept. 2023
	 */
	@Override
	public IScope getScope() {
		final IMacroAgent a = getHost();
		if (a == null) return null;
		return a.getScope();
	}

	@Override
	public boolean isInstanceOf(final String skill, final boolean direct) {
		return getSpecies().implementsSkill(skill);
	}

	/**
	 * Method getPopulationFor()
	 *
	 * @see gama.api.kernel.agent.IAgent#getPopulationFor(gama.api.kernel.species.ISpecies)
	 */
	@Override
	public IPopulation<? extends IAgent> getPopulationFor(final ISpecies microSpecies) {

		IPopulation<? extends IAgent> pop = getPopulationFor(microSpecies.getName());
		if (pop == null) {
			final IModelDescription micro = microSpecies.getDescription().getModelDescription();
			final IModelDescription main = this.getModel().getDescription();
			if (main.getMicroModel(micro.getAlias()) != null && getHost() != null) {
				pop = getHost().getExternMicroPopulationFor(micro.getAlias() + "." + microSpecies.getName());
			}
		}
		return pop;
	}

	/**
	 * Method getPopulationFor()
	 *
	 * @see gama.api.kernel.agent.IAgent#getPopulationFor(java.lang.String)
	 */
	@Override
	public IPopulation<? extends IAgent> getPopulationFor(final String speciesName) {
		final IMacroAgent a = getHost();
		if (a == null) return null;
		return getHost().getPopulationFor(speciesName);
	}

	/**
	 * GAML actions
	 */

	@action (
			name = "debug",
			args = { @arg (
					name = "message",
					type = IType.STRING,
					doc = @doc ("The message to display.")),
					@arg (
							name = "separator",
							type = IType.STRING,
							optional = true,
							doc = @doc ("The string to place between the message and the sender. By default a new line.")),
					@arg (
							name = "end",
							type = IType.STRING,
							optional = true,
							doc = @doc ("The string to append at the end. By default a new line.")),

			})
	public final Object primDebug(final IScope scope) throws GamaRuntimeException {
		final String message = (String) scope.getArg("message", IType.STRING);
		final String end = scope.getTypedArgIfExists("end", IType.STRING, StringUtils.LN);
		final String separator = scope.getTypedArgIfExists("separator", IType.STRING, StringUtils.LN);

		scope.getGui().getConsole().debugConsole(scope.getClock().getCycle(),
				message + separator + "sender: " + GamaMapFactory.createFrom(scope, this) + end, scope.getRoot());
		return message;
	}

	/**
	 * Prim error.
	 *
	 * @param scope
	 *            the scope
	 * @return the object
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@action (
			name = IKeyword.ERROR,
			args = { @arg (
					name = "message",
					type = IType.STRING,
					doc = @doc ("The message to display")) })
	public final Object primError(final IScope scope) throws GamaRuntimeException {
		final String error = (String) scope.getArg("message", IType.STRING);
		scope.getGui().getDialogFactory().error(scope, error);
		return error;
	}

	/**
	 * Prim tell.
	 *
	 * @param scope
	 *            the scope
	 * @return the object
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@action (
			name = "tell",
			args = { @arg (
					name = "msg",
					type = IType.STRING,
					doc = @doc ("The message to display")),
					@arg (
							name = "add_name",
							optional = true,
							type = IType.BOOL,
							doc = @doc ("Should the name of the agent that uses the action be displayed?")) })
	public final Object primTell(final IScope scope) throws GamaRuntimeException {
		boolean addName = !scope.hasArg("add_name") || Cast.asBool(scope, scope.getArg("add_name", IType.BOOL));
		final String s = (addName ? getName() + " says : " : "") + scope.getArg("msg", IType.STRING);
		scope.getGui().getDialogFactory().inform(scope, s);
		return s;
	}

	@Override
	@action (
			name = "die",
			doc = @doc ("Kills the agent and disposes of it. Once dead, the agent cannot behave anymore"))
	public Object primDie(final IScope scope) throws GamaRuntimeException {
		if (!dying) {
			dying = true;
			getSpecies().getArchitecture().abort(scope);
			scope.setDeathStatus();
			dispose();
		}
		return null;
	}

	/**
	 * Gets the geometrical type.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the geometrical type
	 * @date 17 sept. 2023
	 */
	@Override
	public Type getGeometricalType() { return getGeometry().getGeometricalType(); }

	/**
	 * Gets the gaml type.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the gaml type
	 * @date 17 sept. 2023
	 */
	@Override
	public IType<?> getGamlType() { return getScope().getType(getSpeciesName()); }

	/**
	 * Method get()
	 *
	 * @see gama.api.data.objects.IContainer.ToGet#get(gama.api.runtime.scope.IScope, java.lang.Object)
	 */
	@Override
	public Object get(final IScope scope, final String index) throws GamaRuntimeException {
		if (getPopulation().hasVar(index)) return scope.getAgentVarValue(this, index);
		return getAttribute(index);
	}

	/**
	 * Method getFromIndicesList()
	 *
	 * @see gama.api.data.objects.IContainer.ToGet#getFromIndicesList(gama.api.runtime.scope.IScope,
	 *      gama.api.data.objects.IList)
	 */
	@Override
	public Object getFromIndicesList(final IScope scope, final IList<String> indices) throws GamaRuntimeException {
		if (indices == null || indices.isEmpty()) return null;
		return get(scope, indices.firstValue(scope));
	}

	/**
	 * Sets the defining plugin.
	 *
	 * @param plugin
	 *            the new defining plugin
	 */
	public void setDefiningPlugin(final String plugin) {}

	@Override
	public void updateWith(final IScope scope, final ISerialisedAgent sa) {
		// Update attributes
		final Map<String, Object> mapAttr = sa.attributes();
		for (final Entry<String, Object> attr : mapAttr.entrySet()) {
			this.setDirectVarValue(scope, attr.getKey(), attr.getValue());
		}

	}

	@Override
	public ISimulationAgent getSimulation() { return getPopulation().getHost().getSimulation(); }

	@Override
	public IShape translatedTo(final IScope scope, final IPoint absoluteLocation) {
		this.setLocation(absoluteLocation);
		return this;
	}

	@Override
	public IType<?> computeRuntimeType(final IScope scope) {
		return scope.getType(getSpeciesName());
	}

	@Override
	public IJsonValue serializeToJson(final IJson json) {
		AgentReference ar = AgentReference.of(this);
		json.addRef(ar.toString(), () -> SerialisedAgent.of(this, false));
		return json.valueOf(ar);
	}

	@Override
	public IList<IPoint> getPoints() {
		if (getGeometry() == null) return GamaListFactory.getEmptyList();
		return getGeometry().getPoints();
	}

}
