/*******************************************************************************************************
 *
 * GamaServerGUIHandler.java, in gama.headless, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.headless.server;

import static gama.api.utils.server.ISocketCommand.LOAD;
import static gama.api.utils.server.ISocketCommand.PLAY;
import static gama.api.utils.server.ISocketCommand.STOP;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.kernel.simulation.IExperimentAgent;
import gama.api.runtime.scope.IScope;
import gama.api.ui.IConsoleListener;
import gama.api.ui.IDialogFactory;
import gama.api.ui.IStatusDisplayer;
import gama.api.ui.NullGuiHandler;
import gama.api.utils.server.CommandExecutor;
import gama.api.utils.server.GamaServerConsoleListener;
import gama.api.utils.server.GamaServerMessager;
import gama.api.utils.server.GamaServerStatusDisplayer;
import gama.api.utils.server.ISocketCommand;
import gama.api.utils.server.MessageType;
import gama.dev.DEBUG;
import gama.headless.listener.LoadCommand;
import gama.headless.listener.PlayCommand;
import gama.headless.listener.StopCommand;

/**
 * Implements the behaviours to trigger when GUI events happen in a simulation run in GamaServer
 *
 */
public class GamaServerGUIHandler extends NullGuiHandler {

	/** The status. */
	IStatusDisplayer status;

	/** The dialog factory. */
	IDialogFactory dialogFactory = new IDialogFactory() {
		@Override
		public void inform(final IScope scope, final String message) {
			DEBUG.OUT(message);
			if (!dialogMessager.canSendMessage(scope.getExperiment())) return;
			dialogMessager.sendMessage(scope.getExperiment(), message, MessageType.SimulationDialog);
		}

		@Override
		public void error(final IScope scope, final String message) {
			DEBUG.OUT(message);
			if (!dialogMessager.canSendMessage(scope.getExperiment())) return;
			dialogMessager.sendMessage(scope.getExperiment(), message, MessageType.SimulationErrorDialog);
		}
	};

	/** The dialog messager. */
	GamaServerMessager dialogMessager = new GamaServerMessager() {

		@Override
		public boolean canSendMessage(final IExperimentAgent exp) {
			if (exp == null) return false;
			var scope = exp.getScope();
			return scope != null && scope.getServerConfiguration().hasDialog();
		}

	};

	/**
	 * Can send runtime errors.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @return true, if successful
	 * @date 14 août 2023
	 */
	private boolean canSendRuntimeErrors(final IScope scope) {
		if (scope != null && scope.getExperiment() != null && scope.getExperiment().getScope() != null)
			return scope.getExperiment().getScope().getServerConfiguration().hasRuntime();
		return true;
	}

	@Override
	public void runtimeError(final IScope scope, final GamaRuntimeException g) {
		DEBUG.OUT(g);
		// removed to fix #3758
		// if (!canSendDialogMessages(scope)) return;
		if (!canSendRuntimeErrors(scope)) return;
		dialogMessager.sendMessage(scope.getExperiment(), g, MessageType.SimulationError);
	}

	@Override
	public IStatusDisplayer getStatus() {
		if (status == null) { status = new GamaServerStatusDisplayer(); }
		return status;
	}

	@Override
	public IConsoleListener getConsole() {
		if (console == null) { console = new GamaServerConsoleListener(); }
		return console;

	}

	@Override
	public Map<String, ISocketCommand> getServerCommands() {
		final Map<String, ISocketCommand> cmds = new HashMap<>(CommandExecutor.getDefaultCommands());
		// We replace some commands by specialized commands
		cmds.put(LOAD, new LoadCommand());
		cmds.put(PLAY, new PlayCommand());
		cmds.put(STOP, new StopCommand());
		return Collections.unmodifiableMap(cmds);
	}

}
