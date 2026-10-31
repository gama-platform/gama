/*******************************************************************************************************
 *
 * CompositeConsoleListener.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.workspace.console;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import gama.api.kernel.simulation.ITopLevelAgent;
import gama.api.types.color.IColor;
import gama.api.ui.ConsoleEvent;
import gama.api.ui.IConsoleSink;
import gama.api.ui.IConsoleListener;

/**
 * The listener interface for receiving console messages and dispatching them to console listeners
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @see ConsoleEvent
 * @date 2 nov. 2023
 */
public class CompositeConsoleListener implements IConsoleListener {

	private final List<IConsoleSink> sinks = new CopyOnWriteArrayList<>();

	@Override
	public void addConsoleListener(final IConsoleListener console) {
		addConsoleSink(console);
	}

	@Override
	public void addConsoleSink(final IConsoleSink console) {
		if (console == null || console == this || sinks.contains(console)) return;
		sinks.add(console);
	}

	@Override
	public void removeConsoleListener(final IConsoleListener console) {
		removeConsoleSink(console);
	}

	@Override
	public void removeConsoleSink(final IConsoleSink console) {
		sinks.remove(console);
	}

	@Override
	public void informConsole(final String s, final ITopLevelAgent root, final IColor color) {
		accept(ConsoleEvent.info(s, root, color));
	}

	@Override
	public void debugConsole(final int cycle, final String s, final ITopLevelAgent root, final IColor color) {
		accept(ConsoleEvent.debug(cycle, s, root, color));
	}

	@Override
	public void accept(final ConsoleEvent event) {
		for (IConsoleSink sink : sinks) { sink.accept(event); }
	}

	/**
	 * Show console view.
	 *
	 * @param agent
	 *            the agent
	 */
	@Override
	public void toggleConsoleViews(final ITopLevelAgent agent, final boolean show) {
		for (IConsoleSink sink : sinks) {
			if (sink instanceof IConsoleListener listener) { listener.toggleConsoleViews(agent, show); }
		}
	}

	/**
	 * Erase console.
	 *
	 * @param setToNull
	 *            the set to null
	 */
	@Override
	public void eraseConsole(final boolean setToNull) {
		for (IConsoleSink sink : sinks) {
			if (sink instanceof IConsoleListener listener) { listener.eraseConsole(setToNull); }
		}
	}

}
