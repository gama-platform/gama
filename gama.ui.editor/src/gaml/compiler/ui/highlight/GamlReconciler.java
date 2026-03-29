/*******************************************************************************************************
 *
 * GamlReconciler.java, in gama.ui.shared.modeling, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ui.highlight;

import org.eclipse.xtext.ui.editor.reconciler.XtextDocumentReconcileStrategy;
import org.eclipse.xtext.ui.editor.reconciler.XtextReconciler;

import com.google.inject.Inject;

import gaml.compiler.ui.editor.GamlEditor;

/**
 * The class GamlReconciler.
 *
 * @author drogoul
 * @since 7 mai 2016
 *
 */
public class GamlReconciler extends XtextReconciler {

	/**
	 * @param strategy
	 */
	@Inject
	public GamlReconciler(final XtextDocumentReconcileStrategy strategy) {
		super(strategy);
		setDelay(GamlEditor.SCHEDULE_DELAY);
	}

}
