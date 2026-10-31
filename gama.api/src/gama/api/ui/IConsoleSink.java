/*******************************************************************************************************
 *
 * IConsoleSink.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.ui;

/** Receives structured console events from a console producer or dispatcher. */
@FunctionalInterface
public interface IConsoleSink {

	void accept(ConsoleEvent event);
}
