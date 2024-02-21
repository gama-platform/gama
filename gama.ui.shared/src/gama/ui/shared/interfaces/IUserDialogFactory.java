/*******************************************************************************************************
 *
 * IUserDialogFactory.java, in gama.ui.shared.shared, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.ui.shared.interfaces;

import gama.core.runtime.IScope;
import gama.gaml.architecture.user.UserPanelStatement;

/**
 * A factory for creating IUserDialog objects.
 */
public interface IUserDialogFactory {

	/**
	 * Open user dialog.
	 *
	 * @param scope the scope
	 * @param panel the panel
	 */
	void openUserDialog(IScope scope, UserPanelStatement panel);

	/**
	 * Close user dialog.
	 */
	void closeUserDialog();
}