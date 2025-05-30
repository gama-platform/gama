/*******************************************************************************************************
 *
 * SearchItem.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.toolbar;

import org.eclipse.swt.widgets.ToolItem;

import gama.ui.shared.access.GamlAccessContents2;
import gama.ui.shared.views.toolbar.GamaToolbarSimple;

/**
 * The Class SearchItem.
 */
public class SearchItem extends GlobalToolbarItem {

	/**
	 * Instantiates a new search item.
	 *
	 * @param toolbar
	 *            the toolbar
	 */
	SearchItem(final GamaToolbarSimple toolbar) {
		super(toolbar);

	}

	@Override
	public ToolItem createItem(final GamaToolbarSimple toolbar) {
		return toolbar.button("editor/command.find", null, "Search GAML reference", e -> {
			final GamlAccessContents2 quickAccessDialog = new GamlAccessContents2();
			quickAccessDialog.open();
		});
	}

	@Override
	public void update() {}

	@Override
	public void reinit() {}

}
