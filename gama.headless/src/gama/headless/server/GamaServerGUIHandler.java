/*******************************************************************************************************
 *
 * GamaServerGUIHandler.java, in gama.headless, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.headless.server;

import static gama.core.runtime.server.ISocketCommand.LOAD;
import static gama.core.runtime.server.ISocketCommand.PLAY;
import static gama.core.runtime.server.ISocketCommand.STOP;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import gama.core.common.interfaces.IConsoleListener;
import gama.core.common.interfaces.IStatusDisplayer;
import gama.core.kernel.experiment.IExperimentAgent;
import gama.core.runtime.IScope;
import gama.core.runtime.NullGuiHandler;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.runtime.server.GamaServerConsoleListener;
import gama.core.runtime.server.GamaServerMessager;
import gama.core.runtime.server.GamaServerStatusDisplayer;
import gama.core.runtime.server.ISocketCommand;
import gama.core.runtime.server.MessageType;
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

	/** The dialog messager. */
	GamaServerMessager dialogMessager = new GamaServerMessager() {

		@Override
		public boolean canSendMessage(final IExperimentAgent exp) {
			if (exp == null) return false;
			var scope = exp.getScope();
			return scope != null && scope.getServerConfiguration().dialog();
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
			return scope.getExperiment().getScope().getServerConfiguration().runtime();
		return true;
	}

	@Override
	public void openMessageDialog(final IScope scope, final String message) {
		DEBUG.OUT(message);
		if (!dialogMessager.canSendMessage(scope.getExperiment())) return;
		dialogMessager.sendMessage(scope.getExperiment(), message, MessageType.SimulationDialog);
	}

	@Override
	public void openErrorDialog(final IScope scope, final String error) {
		DEBUG.OUT(error);
		if (!dialogMessager.canSendMessage(scope.getExperiment())) return;
		dialogMessager.sendMessage(scope.getExperiment(), error, MessageType.SimulationErrorDialog);
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
		final Map<String, ISocketCommand> cmds = new HashMap<>(super.getServerCommands());
		// We replace some commands by specialized commands
		cmds.put(LOAD, new LoadCommand());
		cmds.put(PLAY, new PlayCommand());
		cmds.put(STOP, new StopCommand());
		return Collections.unmodifiableMap(cmds);
	}

}
