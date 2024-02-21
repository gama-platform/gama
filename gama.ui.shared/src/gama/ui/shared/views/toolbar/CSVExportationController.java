/*******************************************************************************************************
 *
 * CSVExportationController.java, in gama.ui.shared.shared, is part of the source code of the GAMA modeling and
 * simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.views.toolbar;

import org.eclipse.swt.SWT;

import gama.ui.shared.resources.IGamaIcons;

/**
 * Class ZoomController.
 *
 * @author drogoul
 * @since 9 févr. 2015
 *
 */
public class CSVExportationController {

	/** The view. */
	private final IToolbarDecoratedView.CSVExportable view;

	/**
	 * @param view
	 */
	public CSVExportationController(final IToolbarDecoratedView.CSVExportable view) {
		this.view = view;
	}

	/**
	 * @param tb
	 */
	public void install(final GamaToolbar2 tb) {
		tb.button(IGamaIcons.DISPLAY_TOOLBAR_CSVEXPORT, "CSV Export", "CSV Export", e -> view.saveAsCSV(), SWT.RIGHT);

	}

}
