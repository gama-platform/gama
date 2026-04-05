/**
 * Created by drogoul, 24 mars 2026
 *
 */
package gaml.compiler.ui.editor;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.SurroundWithBracketsStrategy;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.xtext.ui.editor.XtextSourceViewerConfiguration;

import gama.api.utils.prefs.GamaPreferences;

/**
 * The Class GamaSourceViewerConfiguration.
 */
public class GamaSourceViewerConfiguration extends XtextSourceViewerConfiguration {

	@Override
	public ITextHover getTextHover(final ISourceViewer sourceViewer, final String contentType) {
		return super.getTextHover(sourceViewer, contentType);
	}

	// See issue #391 : automatically surrounds the selected words with a
	// pair of "brackets"
	@Override
	public IAutoEditStrategy[] getAutoEditStrategies(final ISourceViewer sourceViewer, final String contentType) {
		IAutoEditStrategy[] strategies = super.getAutoEditStrategies(sourceViewer, contentType);
		if (!GamaPreferences.Modeling.CORE_SURROUND_SELECTED.getValue()) return strategies;
		for (IAutoEditStrategy strategy : strategies) {
			if (strategy instanceof SurroundWithBracketsStrategy) return strategies;
		}
		return ArrayUtils.insert(0, strategies, new SurroundWithBracketsStrategy(sourceViewer));
	}

}