/*******************************************************************************************************
 *
 * MinimalAgent.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.metamodel.agent;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import com.google.common.primitives.Ints;

import gama.annotations.precompiler.GamlAnnotations.action;
import gama.annotations.precompiler.GamlAnnotations.arg;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.species;
import gama.core.common.geometry.Envelope3D;
import gama.core.common.interfaces.IKeyword;
import gama.core.kernel.model.IModel;
import gama.core.metamodel.population.IPopulation;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.metamodel.shape.GamaShapeFactory;
import gama.core.metamodel.shape.IShape;
import gama.core.metamodel.topology.ITopology;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.core.util.GamaMapFactory;
import gama.core.util.IList;
import gama.core.util.IMap;
import gama.core.util.graph.GamaGraph;
import gama.gaml.descriptions.ModelDescription;
import gama.gaml.operators.Cast;
import gama.gaml.operators.Strings;
import gama.gaml.species.GamlSpecies;
import gama.gaml.species.ISpecies;
import gama.gaml.types.GamaGeometryType;
import gama.gaml.types.IType;
import gama.gaml.types.Types;
import gama.gaml.variables.IVariable;

/**
 * The Class MinimalAgent.
 */
@species (
		name = IKeyword.AGENT,
		doc = @doc ("The species parent of all agent species"))
/**
 * A concrete implementation of AbstractAgent that declares its own population, geometry and name. Base of most of the
 * concrete subclasses of GAMA agents
 *
 * @author drogoul
 *
 */
public class MinimalAgent implements IAgent, Comparable<IAgent> {

	/** The population that this agent belongs to. */
	protected final IPopulation<? extends IAgent> population;

	/** The name. */
	protected String name;

	/** The geometry. */
	protected final IShape geometry;

	/** The hash code. */
	public final int hashCode;

	/** The index. */
	protected final int index;

	/** The dead. */
	protected volatile boolean dead = false;

	/** The dying. */
	protected volatile boolean dying = false;

	/** The attributes. */
	protected IMap<String, Object> attributes;

	/**
	 * @param s
	 *            the population used to prototype the agent.
	 */
	public MinimalAgent(final IPopulation<? extends IAgent> s, final int index) {
		this(s, index, GamaShapeFactory.create());
	}

	/**
	 * Instantiates a new minimal agent.
	 *
	 * @param population
	 *            the population that this agent belongs to.
	 * @param index
	 *            the index
	 * @param geometry
	 *            the geometry
	 */
	public MinimalAgent(final IPopulation<? extends IAgent> population, final int index, final IShape geometry) {
		this(population, index, Objects.hash(population, index), geometry);
	}

	/**
	 * Instantiates a new minimal agent with a given hashcode.
	 *
	 * @param population
	 *            the population that this agent belongs to.
	 * @param index
	 *            the index
	 * @param hashcode
	 *            the hashcode
	 * @param geometry
	 *            the geometry
	 */
	private MinimalAgent(final IPopulation<? extends IAgent> population, final int index, final int hashcode,
			final IShape geometry) {
		this.index = index;
		this.population = population;
		this.hashCode = hashcode;
		this.geometry = geometry;
		geometry.setAgent(this);
	}

	@Override
	public IPopulation<? extends IAgent> getPopulation() { return population; }

	@Override
	public IShape getGeometry(final IScope scope) {
		return geometry;
	}

	@Override
	public void setGeometry(final IScope scope, final IShape newGeometry) {
		// Addition to address Issue 817: if the new geometry is exactly the one
		// possessed by the agent, no need to change anything.
		if (newGeometry == geometry || newGeometry == null || newGeometry.getInnerGeometry() == null || dead()
				|| this.getSpecies().isGrid() && ((GamlSpecies) this.getSpecies()).belongsToAMicroModel())
			return;

		final ITopology topology = getTopology();
		if (topology == null) return;
		final GamaPoint newGeomLocation = newGeometry.getLocation().copy(scope);

		// if the old geometry is "shared" with another agent, we create a new
		// one. otherwise, we copy it directly.
		final IAgent other = newGeometry.getAgent();
		IShape newLocalGeom;
		if (other == null) {
			newLocalGeom = newGeometry;
		} else {
			// If the agent is different, we do not copy the attributes present in the shape passed as argument (see
			// Issue #2053).
			newLocalGeom = GamaShapeFactory.createFrom(newGeometry.getInnerGeometry().copy());
			newLocalGeom.copyShapeAttributesFrom(newGeometry);
		}
		// topology.normalizeLocation(newGeomLocation, false);

		if (!newGeomLocation.equals(newLocalGeom.getLocation())) { newLocalGeom.setLocation(newGeomLocation); }

		newLocalGeom.setAgent(this);
		final Envelope3D previous = Envelope3D.of(geometry);
		geometry.setGeometry(newLocalGeom);

		topology.updateAgent(previous, this);

		// update micro-agents' locations accordingly

		// TODO DOES NOT WORK FOR THE MOMENT
		// for ( final IPopulation pop : getMicroPopulations() ) {
		// pop.hostChangesShape();
		// }

		notifyVarValueChange(IKeyword.SHAPE, newLocalGeom);
	}

	@Override
	public String getName() {
		if (name == null) { name = getSpeciesName() + getIndex(); }
		if (dead()) return name + " (dead)";
		return name;
	}

	@Override
	public void setName(final String name) {
		this.name = name;
		notifyVarValueChange(IKeyword.NAME, name);
	}

	@SuppressWarnings ("rawtypes")
	@Override
	public/* synchronized */GamaPoint setLocation(final IScope scope, final GamaPoint point) {
		if (point == null || dead() || this.getSpecies().isGrid()) return getLocation();
		GamaPoint newLocation = point.copy(scope);
		final ITopology topology = getTopology();
		if (topology == null) return getLocation();
		newLocation = topology.normalizeLocation(scope, newLocation, false);

		if (geometry == null || geometry.getInnerGeometry() == null) {
			setGeometry(GamaGeometryType.createPoint(newLocation));
		} else {
			final GamaPoint previousPoint = geometry.getLocation();
			if (newLocation.equals(previousPoint)) return newLocation;
			final Envelope3D previous = geometry.getEnvelope();
			geometry.setLocation(newLocation);
			topology.updateAgent(previous, this);

			// update micro-agents' locations accordingly
			// for ( final IPopulation pop : getMicroPopulations() ) {
			// // FIXME DOES NOT WORK FOR THE MOMENT
			// pop.hostChangesShape();
			// }
		}
		final GamaGraph graph = (GamaGraph) getAttribute("attached_graph");
		if (graph != null) {
			final Set edgesToModify = graph.edgesOf(this);
			for (final Object obj : edgesToModify) {
				if (obj instanceof IAgent) {
					final IShape ext1 = (IShape) graph.getEdgeSource(obj);
					final IShape ext2 = (IShape) graph.getEdgeTarget(obj);
					((IAgent) obj).setGeometry(GamaGeometryType.buildLine(ext1.getLocation(), ext2.getLocation()));
				}
			}

		}
		notifyVarValueChange(IKeyword.LOCATION, newLocation);
		return newLocation;

	}

	@Override
	public GamaPoint getLocation(final IScope scope) {
		if (geometry == null || geometry.getInnerGeometry() == null) {
			final ITopology t = getTopology();
			final GamaPoint randomLocation = t == null ? null : t.getRandomLocation(scope);
			if (randomLocation == null) return null;
			setGeometry(GamaGeometryType.createPoint(randomLocation));
			return randomLocation;
		}
		return geometry.getLocation();
	}

	@Override
	public boolean isInstanceOf(final ISpecies s, final boolean direct) {
		if (IKeyword.AGENT.equals(s.getName())) return true;
		final ISpecies species = getSpecies();
		if (species == s) return true;
		if (!direct) return species.extendsSpecies(s);
		return false;
	}

	/**
	 * During the call to init, the agent will search for the action named _init_ and execute it. Its default
	 * implementation is provided in this class as well (equivalent to a super.init())
	 *
	 * @see GamlAgent#_init_()
	 * @see gama.core.common.interfaces.IStepable#step(gama.core.runtime.IScope)
	 * @warning This method should NOT be overriden (except for some rare occasions like in SimulationAgent). Always
	 *          override _init_(IScope) instead.
	 */
	@Override
	public boolean init(final IScope scope) {
		if (!getPopulation().isInitOverriden()) {
			_init_(scope);
		} else {
			scope.execute(getSpecies().getAction(ISpecies.initActionName), this, null);
		}
		return !scope.interrupted();
	}

	/**
	 * During the call to doStep(), the agent will search for the action named _step_ and execute it. Its default
	 * implementation is provided in this class as well (equivalent to a super.doStep());
	 *
	 * @see GamlAgent#_step_()
	 * @see gama.core.common.interfaces.IStepable#step(gama.core.runtime.IScope)
	 * @warning This method should NOT be overridden (except for some rare occasions like in SimulationAgent). Always
	 *          override _step_(IScope) instead.
	 */
	public boolean doStep(final IScope scope) {
		if (!getPopulation().isStepOverriden()) {
			_step_(scope);
			return !scope.interrupted();
		}
		return scope.execute(getSpecies().getAction(ISpecies.stepActionName), this, null).passed();
	}

	/**
	 * The default init of agents consists in calling the super implementation of init() in order to realize the default
	 * init sequence
	 *
	 * @param scope
	 * @return
	 */
	@action (
			name = ISpecies.initActionName)
	public Object _init_(final IScope scope) {
		return getSpecies().getArchitecture().init(scope) ? initSubPopulations(scope) : false;
	}

	/**
	 * The default step of agents consists in calling the super implementation of step() in order to realize the default
	 * step sequence
	 *
	 * TODO verify this sequence as _step_() is NEVER called (only from GAML if done explicitly)
	 *
	 * @param scope
	 * @return
	 */
	@action (
			name = ISpecies.stepActionName)
	public Object _step_(final IScope scope) {
		return scope.execute(getSpecies().getArchitecture(), this, null).passed() ? stepSubPopulations(scope) : false;
	}

	@Override
	public final int hashCode() {
		return hashCode;
	}

	@Override
	public void updateWith(final IScope scope, final ISerialisedAgent sa) {
		// Update attributes
		final Map<String, Object> mapAttr = sa.attributes();
		for (final Entry<String, Object> attr : mapAttr.entrySet()) {
			this.setDirectVarValue(scope, attr.getKey(), attr.getValue());
		}
	}

	/**
	 * Method get()
	 *
	 * @see gama.core.util.IContainer.Addressable#get(gama.core.runtime.IScope, java.lang.Object)
	 */
	@Override
	public Object get(final IScope scope, final String index) throws GamaRuntimeException {
		if (getPopulation().hasVar(index)) return scope.getAgentVarValue(this, index);
		return getAttribute(index);
	}

	/**
	 * Method getFromIndicesList()
	 *
	 * @see gama.core.util.IContainer.Addressable#getFromIndicesList(gama.core.runtime.IScope, gama.core.util.IList)
	 */
	@Override
	public Object getFromIndicesList(final IScope scope, final IList<String> indices) throws GamaRuntimeException {
		if (indices == null || indices.isEmpty()) return null;
		return get(scope, indices.firstValue(scope));
	}

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
	 * Gets the geometrical type.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the geometrical type
	 * @date 17 sept. 2023
	 */
	@Override
	public Type getGeometricalType() { return getGeometry().getGeometricalType(); }

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
		final String end = scope.getTypedArgIfExists("end", IType.STRING, Strings.LN);
		final String separator = scope.getTypedArgIfExists("separator", IType.STRING, Strings.LN);

		scope.getGui().getConsole().debugConsole(scope.getClock().getCycle(),
				message + separator + "sender: " + Cast.asMap(scope, this, false) + end, scope.getRoot());
		return message;
	}

	@Override
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
		scope.getGui().openMessageDialog(scope, s);
		return s;
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
		scope.getGui().openErrorDialog(scope, error);
		return error;
	}

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
	 * @see gama.core.metamodel.agent.IAgent#getPopulationFor(gama.gaml.species.ISpecies)
	 */
	@Override
	public IPopulation<? extends IAgent> getPopulationFor(final ISpecies microSpecies) {
		IPopulation<? extends IAgent> pop = getPopulationFor(microSpecies.getName());
		if (pop == null) {
			final ModelDescription micro = microSpecies.getDescription().getModelDescription();
			final ModelDescription main = this.getModel().getDescription();
			if (main.getMicroModel(micro.getAlias()) != null && getHost() != null) {
				pop = getHost().getExternMicroPopulationFor(micro.getAlias() + "." + microSpecies.getName());
			}
		}
		return pop;
	}

	/**
	 * Method getPopulationFor()
	 *
	 * @see gama.core.metamodel.agent.IAgent#getPopulationFor(java.lang.String)
	 */
	@Override
	public IPopulation<? extends IAgent> getPopulationFor(final String speciesName) {
		final IMacroAgent a = getHost();
		if (a == null) return null;
		return getHost().getPopulationFor(speciesName);
	}

	@Override
	public ISpecies getSpecies() { return getPopulation().getSpecies(); }

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
	}

	@Override
	public List<IAgent> getMacroAgents() {
		final List<IAgent> retVal = GamaListFactory.create(Types.AGENT);
		IAgent currentMacro = this.getHost();
		while (currentMacro != null) {
			retVal.add(currentMacro);
			currentMacro = currentMacro.getHost();
		}
		return retVal;
	}

	@Override
	public IModel getModel() {
		final IMacroAgent a = getHost();
		if (a == null) return GAMA.getModel();
		return a.getModel();
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

	@SuppressWarnings ("unchecked")
	@Override
	public IList<IAgent> getPeers() throws GamaRuntimeException {
		if (getHost() == null) return GamaListFactory.EMPTY_LIST;
		final IPopulation<? extends IAgent> pop = getHost().getPopulationFor(this.getSpecies());
		if (pop != null) {
			final IScope scope = getScope();
			final IList<IAgent> retVal =
					GamaListFactory.<IAgent> createWithoutCasting(scope.getType(getSpeciesName()), (List<IAgent>) pop);
			retVal.remove(this);
			return retVal;
		}
		return GamaListFactory.EMPTY_LIST;
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
	 * Inits the sub populations.
	 *
	 * @param scope
	 *            the scope
	 * @return true, if successful
	 */
	protected boolean initSubPopulations(final IScope scope) {
		return true;
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
		GAMA.getBufferingController().flushSaveFilesOfAgent(this);
		GAMA.getBufferingController().flushWriteOfAgent(this);
	}

}
