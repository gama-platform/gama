/*******************************************************************************************************
 *
 * PlatformAgent.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel;

import java.util.Collection;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import gama.annotations.action;
import gama.annotations.arg;
import gama.annotations.doc;
import gama.annotations.getter;
import gama.annotations.species;
import gama.annotations.variable;
import gama.annotations.vars;
import gama.api.GAMA;
import gama.api.compilation.documentation.GamlConstantDocumentation;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.constants.IKeyword;
import gama.api.data.factories.GamaColorFactory;
import gama.api.data.factories.GamaListFactory;
import gama.api.data.factories.GamaMapFactory;
import gama.api.data.factories.GamaPointFactory;
import gama.api.data.factories.GamaShapeFactory;
import gama.api.data.json.IJson;
import gama.api.data.json.IJsonValue;
import gama.api.data.objects.IColor;
import gama.api.data.objects.IContainer;
import gama.api.data.objects.IList;
import gama.api.data.objects.IMap;
import gama.api.data.objects.IPoint;
import gama.api.data.objects.IShape;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.symbols.IVariable;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IMacroAgent;
import gama.api.kernel.agent.IPopulation;
import gama.api.kernel.serialization.ISerialisedAgent;
import gama.api.kernel.serialization.SerialisedAgent;
import gama.api.kernel.simulation.IClock;
import gama.api.kernel.simulation.IExperimentAgent;
import gama.api.kernel.simulation.ISimulationAgent;
import gama.api.kernel.simulation.ITopLevelAgent;
import gama.api.kernel.species.IModelSpecies;
import gama.api.kernel.species.ISpecies;
import gama.api.kernel.topology.AmorphousTopology;
import gama.api.kernel.topology.ITopology;
import gama.api.runtime.IExecutable;
import gama.api.runtime.SystemInfo;
import gama.api.runtime.scope.ExecutionScope;
import gama.api.runtime.scope.IScope;
import gama.api.ui.IOutputManager;
import gama.api.utils.AgentReference;
import gama.api.utils.MemoryUtils;
import gama.api.utils.prefs.GamaPreferences;
import gama.api.utils.prefs.Pref;
import gama.api.utils.random.IRandom;
import gama.api.utils.server.GamaServerMessage;
import gama.api.utils.server.MessageType;
import gama.dev.DEBUG;
import one.util.streamex.StreamEx;

/**
 * The Class PlatformAgent.
 */

/**
 * The Class PlatformAgent.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 10 sept. 2023
 */
@species (
		name = IKeyword.PLATFORM,
		internal = true,
		doc = { @doc ("The species of the unique platform agent, called 'gama'") })
@vars ({ @variable (
		name = PlatformAgent.MACHINE_TIME,
		type = IType.FLOAT,
		doc = @doc (
				value = "Returns the current system time in milliseconds (i.e. number of milliseconds since UNIX epoch day)",
				comment = "The return value is a float number")),
		@variable (
				name = PlatformAgent.WORKSPACE_PATH,
				type = IType.STRING,
				constant = true,
				doc = @doc (
						value = "Contains the absolute path to the workspace of GAMA. Can be used to list all the projects and files present in the platform",
						comment = "Always terminated with a trailing separator",
						see = { "workspace" })),
		@variable (
				name = "info",
				type = IType.STRING,
				constant = true,
				doc = @doc (
						value = "Returns information about GAMA, in a format suitable to be pasted into GitHub issues")),
		@variable (
				name = "platform",
				type = IType.STRING,
				constant = true,
				doc = @doc (
						value = "Returns the platform on which GAMA is currently executing.")),
		@variable (
				name = "version",
				type = IType.STRING,
				constant = true,
				doc = @doc (
						value = "Returns the version of the current GAMA installation")),
		@variable (
				name = "plugins",
				type = IType.LIST,
				of = IType.STRING,
				constant = true,
				doc = @doc (
						value = "Lists all the plugins present in this installation of GAMA")),
		@variable (
				name = "free_memory",
				type = IType.FLOAT,
				constant = false,
				doc = @doc (
						value = "A float number that represents the free memory available to GAMA in bytes")),
		@variable (
				name = "max_memory",
				type = IType.FLOAT,
				constant = false,
				doc = @doc (
						value = "A float number that represents the maximum amount of memory available to GAMA in bytes")),
		@variable (
				name = "workspace",
				type = IType.FILE,
				constant = true,
				doc = @doc (
						value = "A folder representing the workspace of GAMA. Can be used to list all the projects and files present in the platform",
						see = { "workspace_path" })), })
public class PlatformAgent implements ITopLevelAgent.Platform {

	static {
		// DEBUG.OFF();
	}

	/** The polling. */
	private final Timer polling = new Timer();

	/** The prefs to restore. */
	final java.util.Map<String, Object> prefsToRestore;

	/** The basic scope. */
	final IScope basicScope;

	/** The current task. */
	private TimerTask currentTask;

	/**
	 * Instantiates a new platform agent.
	 */
	public PlatformAgent() {
		prefsToRestore = new HashMap<>();
		basicScope = new ExecutionScope(this, "Gama platform scope");
		if (GamaPreferences.Runtime.CORE_MEMORY_POLLING.getValue()) { startPollingMemory(); }
		GamaPreferences.Runtime.CORE_MEMORY_POLLING.onChange(newValue -> {
			if (newValue) {
				startPollingMemory();
			} else {
				stopPollingMemory();
			}
		});
		GamaPreferences.Runtime.CORE_MEMORY_FREQUENCY.onChange(newValue -> {
			stopPollingMemory();
			startPollingMemory();
		});

	}

	/**
	 * @param p
	 * @param i
	 */
	public PlatformAgent(final IPopulation p, final int i) {
		this();
	}

	/**
	 * Start polling memory.
	 */
	private void startPollingMemory() {
		if (currentTask == null) {
			currentTask = new TimerTask() {
				@Override
				public void run() {
					if (MemoryUtils.memoryIsLow()) {
						final IExperimentAgent agent = getExperiment();
						if (agent != null) {
							final long mb = (long) (MemoryUtils.availableMemory() / 1000000d);
							final GamaRuntimeException e = GamaRuntimeException.warning("Memory is low (" + mb
									+ " megabytes). You should close the experiment, exit GAMA and give it more memory",
									agent.getScope());
							GAMA.reportError(basicScope, e, false);
						}
					}
				}
			};
		}
		polling.scheduleAtFixedRate(currentTask, 0,
				(long) 1000 * GamaPreferences.Runtime.CORE_MEMORY_FREQUENCY.getValue());
	}

	/**
	 * Stop polling memory.
	 */
	private void stopPollingMemory() {
		if (currentTask != null) {
			currentTask.cancel();
			currentTask = null;
		}
	}

	@Override
	public Object primDie(final IScope scope) {
		stopPollingMemory();
		polling.cancel();
		GAMA.closeAllExperiments(false, true);
		scope.getGui().exit();
		return null;
	}

	@Override
	public boolean isContextIndependant() { return false; }

	@Override
	public ITopology getTopology() { return new AmorphousTopology(); }

	@Override
	public String getName() { return "gama"; }

	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		return "gama";
	}

	@Override
	public ISpecies getSpecies() { return GamaMetaModel.getSpecies(IKeyword.PLATFORM); }

	@Override
	public IClock getClock() { return IClock.NULL_CLOCK; }

	@Override
	public IScope getScope() { return basicScope; }

	@Override
	public IColor getColor() { return GamaColorFactory.get(102, 114, 126); }

	@Override
	public IRandom getRandomGenerator() { return GAMA.getCurrentRandom(); }

	@Override
	public IOutputManager getOutputManager() {
		if (getExperiment() != null) return getExperiment().getOutputManager();
		return null;
	}

	@Override
	public void postEndAction(final IExecutable executable) {}

	@Override
	public void postDisposeAction(final IExecutable executable) {}

	@Override
	public void postOneShotAction(final IExecutable executable) {}

	@Override
	public void executeAction(final IExecutable executable) {}

	@Override
	public boolean isOnUserHold() { return false; }

	@Override
	public void setOnUserHold(final boolean state) {}

	@Override
	public ISimulationAgent getSimulation() { return GAMA.getSimulation(); }

	@Override
	public IExperimentAgent getExperiment() { return GAMA.getExperimentAgent(); }

	/**
	 * Gets the workspace path.
	 *
	 * @return the workspace path
	 */
	@getter (
			value = WORKSPACE_PATH,
			initializer = true)
	public String getWorkspacePath() { return GAMA.getWorkspaceManager().getRoot().getLocation().toOSString(); }

	/**
	 * Gets the plugins list.
	 *
	 * @return the plugins list
	 */
	@SuppressWarnings ("unchecked")
	@getter (
			value = "plugins",
			initializer = true)
	public IList<String> getPluginsList() {
		final BundleContext bc = FrameworkUtil.getBundle(getClass()).getBundleContext();
		return StreamEx.of(bc.getBundles()).map(Bundle::getSymbolicName)
				.toCollection(GamaListFactory.getSupplier(Types.STRING));
	}

	/**
	 * Gets the version.
	 *
	 * @return the version
	 */
	@getter (
			value = "info",
			initializer = true)
	public String getInfo() { return SystemInfo.getSystemInfo(); }

	/**
	 * Gets the platform.
	 *
	 * @return the platform
	 */
	@getter (
			value = "platform",
			initializer = true)
	public String getPlatform() { return SystemInfo.OS_NAME; }

	/**
	 * Gets the version.
	 *
	 * @return the version
	 */
	@getter (
			value = "version",
			initializer = true)
	public String getVersion() {
		final BundleContext bc = FrameworkUtil.getBundle(getClass()).getBundleContext();
		return bc.getBundle().getVersion().toString();
	}

	/**
	 * Gets the available memory.
	 *
	 * @return the available memory
	 */
	@getter (
			value = "free_memory",
			initializer = true)
	public double getAvailableMemory() {
		final long allocatedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		return Runtime.getRuntime().maxMemory() - allocatedMemory;
	}

	/**
	 * Gets the max memory.
	 *
	 * @return the max memory
	 */
	@getter (
			value = "max_memory",
			initializer = true)
	public double getMaxMemory() { return Runtime.getRuntime().maxMemory(); }

	/**
	 * Gets the machine time.
	 *
	 * @return the machine time
	 */
	@getter (PlatformAgent.MACHINE_TIME)
	public Double getMachineTime() { return (double) System.currentTimeMillis(); }

	/**
	 * Gets the title.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the title
	 * @date 3 nov. 2023
	 */
	@Override
	public String getTitle() { return "gama platform agent"; }

	/**
	 * Gets the documentation.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the documentation
	 * @date 3 nov. 2023
	 */
	@Override
	public IGamlDocumentation getDocumentation() {
		return new GamlConstantDocumentation(
				"The unique instance of the platform species. Used to access GAMA platform properties.");
	}

	/**
	 * Gets the defining plugin.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the defining plugin
	 * @date 3 nov. 2023
	 */
	@Override
	public String getDefiningPlugin() { return "gama.api"; }

	@Override
	public Object value(final IScope scope) throws GamaRuntimeException {
		return this;
	}

	@Override
	public boolean isConst() { return false; }

	@Override
	public String literalValue() {
		return IKeyword.GAMA;
	}

	@Override
	public IExpression resolveAgainst(final IScope scope) {
		return this;
	}

	@Override
	public boolean shouldBeParenthesized() {
		return false;
	}

	@Override
	public IType<?> getGamlType() { return Types.get(IKeyword.PLATFORM); }

	/**
	 * Save pref to restore.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	@Override
	public void savePrefToRestore(final String key, final Object value) {
		// DEBUG.OUT("Save preference to restor" + key + " with value " + value);
		// In order to not restore a previous value if it has already been set
		prefsToRestore.putIfAbsent(key, value);
	}

	/**
	 * Restore prefs.
	 */
	@Override
	public void restorePrefs() {
		// DEBUG.OUT("Restoring preferences" + prefsToRestore);
		prefsToRestore.forEach((key, value) -> {
			Pref<?> p = GAMA.getPreferenceStore().get(key);
			if (p != null) { p.setValue(basicScope, value); }
		});

	}

	@Override
	public String getFamilyName() { return IKeyword.PLATFORM; }

	@Override
	public boolean isPlatform() { return true; }

	/**
	 * Send message through server.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @return the object
	 * @date 3 nov. 2023
	 */
	@action (
			name = "send",
			args = @arg (
					name = IKeyword.MESSAGE,
					optional = false,
					doc = @doc (
							value = "The message to send")))
	public Object sendMessageThroughServer(final IScope scope) {
		Object message = scope.getArg(IKeyword.MESSAGE, IType.NONE);
		sendMessage(scope, message);
		return message;
	}

	/**
	 * Send message.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @param message
	 *            the message
	 * @date 3 nov. 2023
	 */
	public void sendMessage(final IScope scope, final Object message) {
		sendMessage(scope, message, MessageType.SimulationOutput);
	}

	/**
	 * Send message.
	 *
	 * @param scope
	 *            the scope
	 * @param message
	 *            the message
	 * @param type
	 *            the type
	 */
	public void sendMessage(final IScope scope, final Object message, final MessageType type) {
		try {
			var socket = scope.getServerConfiguration().socket();
			// try to get the socket in platformAgent if the request is too soon before agent.schedule()
			if (socket == null && GAMA.getServer() != null) {
				socket = GAMA.getServer().obtainGuiServerConfiguration().socket();
			}
			if (socket == null) {
				DEBUG.OUT("No socket found, maybe the client is already disconnected. Unable to send message: "
						+ message);
				return;
			}
			socket.send(GAMA.getJsonEncoder()
					.valueOf(new GamaServerMessage(type, message, scope.getServerConfiguration().expId())).toString());
		} catch (Exception ex) {
			ex.printStackTrace();
			DEBUG.OUT("Unable to send message:" + message);
			DEBUG.OUT(ex.toString());
		}
	}

	@Override
	public boolean canCapture(final IAgent other, final ISpecies newSpecies) {
		return false;
	}

	@Override
	public IAgent captureMicroAgent(final IScope scope, final ISpecies microSpecies, final IAgent microAgent)
			throws GamaRuntimeException {
		return microAgent;
	}

	@Override
	public IList<IAgent> captureMicroAgents(final IScope scope, final ISpecies microSpecies,
			final IList<IAgent> microAgents) throws GamaRuntimeException {
		return microAgents;
	}

	@Override
	public IContainer<?, IAgent> getMembers(final IScope scope) {
		return GamaListFactory.createWithoutCasting(Types.AGENT, GAMA.getExperimentAgent());
	}

	@Override
	public IPopulation<? extends IAgent> getMicroPopulation(final ISpecies microSpecies) {
		return null;
	}

	@Override
	public IPopulation<? extends IAgent> getMicroPopulation(final String microSpeciesName) {
		return null;
	}

	@Override
	public IPopulation<? extends IAgent>[] getMicroPopulations() {
		IExperimentAgent agent = GAMA.getExperimentAgent();
		if (agent != null) return new IPopulation[] { agent.getPopulation() };
		return new IPopulation[0];
	}

	@Override
	public boolean hasMembers() {
		return GAMA.getExperimentAgent() != null;
	}

	@Override
	public int getMembersSize(final IScope scope) {
		return GAMA.getExperimentAgent() != null ? 1 : 0;
	}

	@Override
	public void initializeMicroPopulation(final IScope scope, final String name) {}

	@Override
	public IList<IAgent> migrateMicroAgents(final IScope scope, final IList<IAgent> microAgents,
			final ISpecies newMicroSpecies) {
		return microAgents;
	}

	@Override
	public IList<IAgent> migrateMicroAgents(final IScope scope, final ISpecies oldMicroSpecies,
			final ISpecies newMicroSpecies) {
		return GamaListFactory.getEmptyList();
	}

	@Override
	public IList<IAgent> releaseMicroAgents(final IScope scope, final IList<IAgent> microAgents)
			throws GamaRuntimeException {
		return microAgents;
	}

	@Override
	public void setMembers(final IList<IAgent> members) {}

	@Override
	public void setAgents(final IList<IAgent> agents) {}

	@Override
	public IList<IAgent> getAgents(final IScope scope) {
		IExperimentAgent agent = GAMA.getExperimentAgent();
		if (agent == null) return GamaListFactory.getEmptyList();
		IList<IAgent> list = GamaListFactory.createWithoutCasting(Types.AGENT, GAMA.getExperimentAgent());
		list.addAll(agent.getAgents(scope));
		return list;
	}

	@Override
	public void addExternMicroPopulation(final String expName, final IPopulation<? extends IAgent> pop) {}

	@Override
	public IPopulation<? extends IAgent> getExternMicroPopulationFor(final String expName) {
		return null;
	}

	@Override
	public void setPeers(final IList<IAgent> peers) {}

	@Override
	public IList<IAgent> getPeers() throws GamaRuntimeException { return GamaListFactory.getEmptyList(); }

	@Override
	public void setName(final String name) {}

	@Override
	public IPoint getLocation(final IScope scope) {
		return GamaPointFactory.getNullPoint();
	}

	@Override
	public IPoint setLocation(final IScope scope, final IPoint l) {
		return GamaPointFactory.getNullPoint();
	}

	@Override
	public IShape getGeometry(final IScope scope) {
		return GamaShapeFactory.getNullShape();
	}

	@Override
	public void setGeometry(final IScope scope, final IShape newGeometry) {}

	@Override
	public boolean dead() {
		return false;
	}

	@Override
	public IMacroAgent getHost() { return null; }

	@Override
	public void setHost(final IMacroAgent macroAgent) {}

	@Override
	public void schedule(final IScope scope) {}

	@Override
	public int getIndex() { return 0; }

	@Override
	public String getSpeciesName() { return IKeyword.PLATFORM; }

	@Override
	public IPopulation<? extends IAgent> getPopulation() { return null; }

	@Override
	public boolean isInstanceOf(final ISpecies s, final boolean direct) {
		return s == getSpecies();
	}

	@Override
	public Object getDirectVarValue(final IScope scope, final String s) throws GamaRuntimeException {
		final IVariable var = getSpecies().getVar(s);
		if (var != null) return var.value(scope, this);
		Collection<String> keys = GAMA.getPreferenceStore().getKeys();
		if (keys.contains(s)) return GAMA.getPreferenceStore().get(s).getValue();
		return null;

	}

	@Override
	public void setDirectVarValue(final IScope scope, final String s, final Object v) throws GamaRuntimeException {
		final IVariable var = getSpecies().getVar(s);
		if (var != null) { var.setVal(scope, this, v); }
	}

	@Override
	public IModelSpecies getModel() { return GAMA.getModel(); }

	@Override
	public boolean isInstanceOf(final String skill, final boolean direct) {
		return false;
	}

	@Override
	public IPopulation<? extends IAgent> getPopulationFor(final ISpecies microSpecies) {
		return null;
	}

	@Override
	public IPopulation<? extends IAgent> getPopulationFor(final String speciesName) {
		return null;
	}

	@Override
	public void updateWith(final IScope s, final ISerialisedAgent sa) {

	}

	@Override
	public IShape copy(final IScope scope) {
		return this;
	}

	@Override
	public IAgent getAgent() { return this; }

	@Override
	public Type getGeometricalType() { return Type.NULL; }

	@Override
	public void setAgent(final IAgent agent) {}

	@Override
	public IList<IPoint> getPoints() { return GamaListFactory.getEmptyList(); }

	@Override
	public IShape translatedTo(final IScope scope, final IPoint absoluteLocation) {
		return this;
	}

	@Override
	public String stringValue(final IScope scope) {
		return "platform";
	}

	@Override
	public IJsonValue serializeToJson(final IJson json) {
		AgentReference ar = AgentReference.of(this);
		json.addRef(ar.toString(), () -> SerialisedAgent.of(this, false));
		return json.valueOf(ar);
	}

	/** The attributes. */
	protected IMap<String, Object> attributes;

	@Override
	public IMap<String, Object> getAttributes(final boolean createIfNeeded) {
		if (attributes == null && createIfNeeded) { attributes = GamaMapFactory.create(Types.STRING, Types.NO_TYPE); }
		return attributes;
	}

	@Override
	public int compareTo(final IAgent o) {
		return 0;
	}

	@Override
	public boolean init(final IScope scope) throws GamaRuntimeException {
		return false;
	}

	@Override
	public boolean step(final IScope scope) throws GamaRuntimeException {
		return false;
	}

	@Override
	public Object get(final IScope scope, final String index) throws GamaRuntimeException {
		if (getSpecies().hasVar(index)) return scope.getAgentVarValue(this, index);
		return getAttribute(index);
	}

	@Override
	public Object getFromIndicesList(final IScope scope, final IList<String> indices) throws GamaRuntimeException {
		if (indices == null || indices.isEmpty()) return null;
		return get(scope, indices.firstValue(scope));
	}

}
