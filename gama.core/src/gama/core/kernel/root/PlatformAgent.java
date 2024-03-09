/*******************************************************************************************************
 *
 * PlatformAgent.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.1.9.3).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.kernel.root;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.core.resources.ResourcesPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import gama.annotations.precompiler.GamlAnnotations.action;
import gama.annotations.precompiler.GamlAnnotations.arg;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.getter;
import gama.annotations.precompiler.GamlAnnotations.species;
import gama.annotations.precompiler.GamlAnnotations.variable;
import gama.annotations.precompiler.GamlAnnotations.vars;
import gama.core.common.interfaces.IKeyword;
import gama.core.common.preferences.GamaPreferences;
import gama.core.common.preferences.Pref;
import gama.core.common.util.RandomUtils;
import gama.core.kernel.experiment.IExperimentAgent;
import gama.core.kernel.experiment.ITopLevelAgent;
import gama.core.kernel.simulation.SimulationAgent;
import gama.core.kernel.simulation.SimulationClock;
import gama.core.metamodel.agent.GamlAgent;
import gama.core.metamodel.population.GamaPopulation;
import gama.core.metamodel.population.IPopulation;
import gama.core.metamodel.topology.ITopology;
import gama.core.metamodel.topology.continuous.AmorphousTopology;
import gama.core.outputs.IOutputManager;
import gama.core.runtime.ExecutionScope;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.MemoryUtils;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.runtime.server.GamaGuiWebSocketServer;
import gama.core.runtime.server.GamaServerMessage;
import gama.core.runtime.server.GamaWebSocketServer;
import gama.core.util.GamaColor;
import gama.core.util.GamaMapFactory;
import gama.core.util.IList;
import gama.core.util.file.json.Json;
import gama.dev.DEBUG;
import gama.gaml.compilation.kernel.GamaMetaModel;
import gama.gaml.expressions.IExpression;
import gama.gaml.operators.Containers;
import gama.gaml.species.ISpecies;
import gama.gaml.statements.IExecutable;
import gama.gaml.types.IType;
import gama.gaml.types.Types;
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
						value = "Returns information about GAMA, in a format suitable to be pasted into issues")),
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
				type = IType.INT,
				constant = false,
				doc = @doc (
						value = "Returns the free memory available to GAMA in bytes")),
		@variable (
				name = "max_memory",
				type = IType.INT,
				constant = false,
				doc = @doc (
						value = "Returns the maximum amount of memory available to GAMA in bytes")),
		@variable (
				name = "workspace",
				type = IType.FILE,
				constant = true,
				doc = @doc (
						value = "A folder representing the workspace of GAMA. Can be used to list all the projects and files present in the platform",
						see = { "workspace_path" })), })
public class PlatformAgent extends GamlAgent implements ITopLevelAgent, IExpression {

	static {
		// DEBUG.ON();
	}

	/** The Constant WORKSPACE_PATH. */
	public static final String WORKSPACE_PATH = "workspace_path";

	/** The Constant MACHINE_TIME. */
	public static final String MACHINE_TIME = "machine_time";

	/** The polling. */
	private final Timer polling = new Timer();

	/** The prefs to restore. */
	Map<String, Object> prefsToRestore = GamaMapFactory.create();

	/** The basic scope. */
	final IScope basicScope;

	/** The current task. */
	private TimerTask currentTask;

	/** The my server. */
	private GamaWebSocketServer myServer;

	/** The json encoder. */
	private final Json jsonEncoder = Json.getNew();

	/**
	 * Instantiates a new platform agent.
	 */
	public PlatformAgent() {
		this(new GamaPopulation<>(null,
				GamaMetaModel.INSTANCE.getAbstractModelSpecies().getMicroSpecies(IKeyword.PLATFORM)), 0);
	}

	/**
	 * Instantiates a new platform agent.
	 *
	 * @param pop
	 *            the pop
	 * @param index
	 *            the index
	 */
	public PlatformAgent(final IPopulation<PlatformAgent> pop, final int index) {
		super(pop, index);
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

		if (!GAMA.isInHeadLessMode() && GamaPreferences.Runtime.CORE_SERVER_MODE.getValue()) {
			final int port = GamaPreferences.Runtime.CORE_SERVER_PORT.getValue();
			final int ping = GamaPreferences.Runtime.CORE_SERVER_PING.getValue();
			myServer = GamaGuiWebSocketServer.StartForGUI(port, ping);
		}
		GamaPreferences.Runtime.CORE_SERVER_MODE.onChange(newValue -> {
			if (myServer != null) {
				try {
					myServer.stop();
					myServer = null;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (newValue) {
				final int port = GamaPreferences.Runtime.CORE_SERVER_PORT.getValue();
				final int ping = GamaPreferences.Runtime.CORE_SERVER_PING.getValue();
				myServer = GamaGuiWebSocketServer.StartForGUI(port, ping);
			}
		});
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
	public ISpecies getSpecies() { return getPopulation().getSpecies(); }

	@Override
	public SimulationClock getClock() { return new SimulationClock(getScope()); }

	@Override
	public IScope getScope() { return basicScope; }

	@Override
	public GamaColor getColor() { return GamaColor.get(102, 114, 126); }

	@Override
	public RandomUtils getRandomGenerator() { return new RandomUtils(); }

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
	public SimulationAgent getSimulation() { return GAMA.getSimulation(); }

	@Override
	public IExperimentAgent getExperiment() {
		if (GAMA.getExperiment() != null) return GAMA.getExperiment().getAgent();
		return null;
	}

	/**
	 * Gets the workspace path.
	 *
	 * @return the workspace path
	 */
	@getter (
			value = WORKSPACE_PATH,
			initializer = true)
	public String getWorkspacePath() {
		return ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
		// Patrick : previous version: does not work well on windows (/C:/....)
		// final URL url = Platform.getInstanceLocation().getURL();
		// return url.getPath();
	}

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
		return StreamEx.of(bc.getBundles()).map(Bundle::getSymbolicName).toCollection(Containers.listOf(Types.STRING));
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
	public long getAvailableMemory() {
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
	public long getMaxMemory() { return Runtime.getRuntime().maxMemory(); }

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
	public Doc getDocumentation() {
		return new ConstantDoc("The unique instance of the platform species. Used to access GAMA platform properties.");
	}

	/**
	 * Gets the defining plugin.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the defining plugin
	 * @date 3 nov. 2023
	 */
	@Override
	public String getDefiningPlugin() { return "gama.core"; }

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
	public void savePrefToRestore(final String key, final Object value) {
		// DEBUG.OUT("Save preference to restor" + key + " with value " + value);
		// In order to not restore a previous value if it has already been set
		prefsToRestore.putIfAbsent(key, value);
	}

	/**
	 * Restore prefs.
	 */
	public void restorePrefs() {
		// DEBUG.OUT("Restoring preferences" + prefsToRestore);
		prefsToRestore.forEach((key, value) -> {
			Pref<?> p = GamaPreferences.get(key);
			if (p != null) { p.setValue(basicScope, value); }
		});

	}

	@Override
	public String getFamilyName() { return IKeyword.PLATFORM; }

	@Override
	public boolean isPlatform() { return true; }

	/**
	 * Gets the server.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the server
	 * @date 3 nov. 2023
	 */
	public GamaWebSocketServer getServer() { return myServer; }

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
					optional = false))
	public Object sendMessageThroughServer(final IScope scope) {
		Object message = scope.getArg(IKeyword.MESSAGE);
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
		try {
			var socket = scope.getServerConfiguration().socket();
			// try to get the socket in platformAgent if the request is too soon before agent.schedule()
			if (socket == null && myServer != null) { socket = myServer.obtainGuiServerConfiguration().socket(); }
			if (socket == null) {
				DEBUG.OUT("No socket found, maybe the client is already disconnected. Unable to send message: "
						+ message);
				return;
			}
			socket.send(jsonEncoder.valueOf(new GamaServerMessage(GamaServerMessage.Type.SimulationOutput, message,
					scope.getServerConfiguration().expId())).toString());
		} catch (Exception ex) {
			ex.printStackTrace();
			DEBUG.OUT("Unable to send message:" + message);
			DEBUG.OUT(ex.toString());
		}
	}

}
