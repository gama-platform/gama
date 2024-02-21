/*******************************************************************************************************
 *
 * IAgentMenuFactory.java, in gama.ui.shared.shared, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.ui.shared.interfaces;

import java.util.Collection;

import org.eclipse.swt.widgets.Menu;

import gama.core.metamodel.agent.IAgent;
import gama.ui.shared.menus.MenuAction;

/**
 * A factory for creating IAgentMenu objects.
 */
public interface IAgentMenuFactory {

	/**
	 * Fill population sub menu.
	 *
	 * @param menu the menu
	 * @param species the species
	 * @param actions the actions
	 */
	void fillPopulationSubMenu(final Menu menu, final Collection<? extends IAgent> species,
			final MenuAction... actions);
}