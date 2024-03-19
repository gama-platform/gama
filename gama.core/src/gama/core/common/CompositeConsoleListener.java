/*******************************************************************************************************
 *
 * CompositeConsoleListener.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform (v.2024-06).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.core.common;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import gama.core.common.interfaces.IConsoleListener;
import gama.core.kernel.experiment.ITopLevelAgent;
import gama.core.util.GamaColor;

/**
 * The listener interface for receiving console messages and dispatching them to console listeners
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @see CompositeConsoleEvent
 * @date 2 nov. 2023
 */
public class CompositeConsoleListener implements IConsoleListener {

	/** The consoles. */
	List<IConsoleListener> consoles = new CopyOnWriteArrayList<>();

	@Override
	public void addConsoleListener(final IConsoleListener console) {
		if (console == null || consoles.contains(console)) return;
		consoles.add(console);
	}

	@Override
	public void removeConsoleListener(final IConsoleListener console) {
		consoles.remove(console);
	}

	@Override
	public void informConsole(final String s, final ITopLevelAgent root, final GamaColor color) {
		for (IConsoleListener console : consoles) { console.informConsole(s, root, color); }
	}

	/**
	 * Show console view.
	 *
	 * @param agent
	 *            the agent
	 */
	@Override
	public void toggleConsoleViews(final ITopLevelAgent agent, final boolean show) {
		for (IConsoleListener console : consoles) { console.toggleConsoleViews(agent, show); }
	}

	/**
	 * Erase console.
	 *
	 * @param setToNull
	 *            the set to null
	 */
	@Override
	public void eraseConsole(final boolean setToNull) {
		for (IConsoleListener console : consoles) { console.eraseConsole(setToNull); }
	}

}
