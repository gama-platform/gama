/*******************************************************************************************************
 *
 * GamaSourceViewer.java, in gama.ui.editor, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ui.editor;

import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.xtext.ui.editor.XtextSourceViewer;

/**
 * The class GamaSourceViewer.
 *
 * @author drogoul
 * @since 12 août 2016
 *
 */
public class GamaSourceViewer extends XtextSourceViewer {

	/** The is overview visible. */
	private boolean isOverviewVisible;

	/**
	 * @param parent
	 * @param ruler
	 * @param overviewRuler
	 * @param showsAnnotationOverview
	 * @param styles
	 */
	public GamaSourceViewer(final Composite parent, final IVerticalRuler ruler, final IOverviewRuler overviewRuler,
			final boolean showsAnnotationOverview, final int styles) {
		super(parent, ruler, overviewRuler, showsAnnotationOverview, styles);
		isOverviewVisible = showsAnnotationOverview && overviewRuler != null;
	}

	@Override
	public void showAnnotationsOverview(final boolean show) {
		super.showAnnotationsOverview(show);
		isOverviewVisible = show;
	}

	/**
	 * Checks if is overview visible.
	 *
	 * @return true, if is overview visible
	 */
	public boolean isOverviewVisible() { return isOverviewVisible; }

	/**
	 * @param string
	 * @return
	 */
	public int find(final String string) {
		return super.findAndSelect(0, string, true, true, false, false);
	}

}
