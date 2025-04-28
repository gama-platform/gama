/*******************************************************************************************************
 *
 * GamaMinimapView.java, in gama.ui.editor, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.views;

import org.eclipse.ui.internal.views.minimap.MinimapView;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.MessagePage;
import org.eclipse.ui.part.PageBook;

import gama.core.common.interfaces.IGui;

/**
 * The class GamaMinimapView.
 *
 * @author drogoul
 * @since 14 avr. 2025
 *
 */
public class GamaMinimapView extends MinimapView {

	/** The id. */
	public static String ID = IGui.MINIMAP_VIEW_ID;

	@Override
	protected IPage createDefaultPage(final PageBook book) {
		MessagePage page = (MessagePage) super.createDefaultPage(book);
		page.setMessage("No model is opened in the editor");
		return page;
	}

}
