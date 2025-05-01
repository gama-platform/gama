/*******************************************************************************************************
 *
 * Expander.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.views.toolbar;

import org.eclipse.swt.SWT;

import gama.ui.shared.resources.IGamaIcons;

/**
 * Class FontSizer.
 *
 * @author drogoul
 * @since 9 févr. 2015
 *
 */
public class Expander {

	/**
	 * @param tb
	 */
	public static void install(final IToolbarDecoratedView.Expandable view, final GamaToolbar2 tb) {

		tb.button(IGamaIcons.TREE_EXPAND, "Expand all items", "Expand all items", e -> view.expandAll(), SWT.RIGHT);
		tb.button(IGamaIcons.TREE_COLLAPSE, "Collapse all items", "Collapse all items", e -> view.collapseAll(),
				SWT.RIGHT);

		tb.sep(16, SWT.RIGHT);

	}

}
