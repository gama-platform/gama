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

import gama.api.utils.prefs.GamaPreferences;
import gama.api.utils.prefs.IPreferenceChangeListener.IPreferenceAfterChangeListener;

/**
 * The class GamlReconciler.
 *
 * @author drogoul
 * @since 7 mai 2016
 *
 */
public class GamlReconciler extends XtextReconciler {

	private final IPreferenceAfterChangeListener<Integer> preferenceListener =
			delay -> setDelay(GamaPreferences.Modeling.EDITOR_RECOMPILE_DELAY.getValue());

	/**
	 * @param strategy
	 */
	@Inject
	public GamlReconciler(final XtextDocumentReconcileStrategy strategy) {
		super(strategy);
		GamaPreferences.Modeling.EDITOR_RECOMPILE_DELAY.addChangeListener(preferenceListener);
		setDelay(GamaPreferences.Modeling.EDITOR_RECOMPILE_DELAY.getValue());
	}

	@Override
	public void uninstall() {
		GamaPreferences.Modeling.EDITOR_RECOMPILE_DELAY.removeChangeListener(preferenceListener);
		super.uninstall();
	}

}
