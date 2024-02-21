/*******************************************************************************************************
 *
 * GamlHighlightingConfiguration.java, in gama.ui.shared.modeling, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gaml.compiler.ui.highlight;

import org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightingConfiguration;
import org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightingConfigurationAcceptor;

import com.google.inject.Singleton;

import gama.ui.application.workbench.ThemeHelper;

/**
 * The Class GamlHighlightingConfiguration.
 */
@Singleton
public class GamlHighlightingConfiguration implements IHighlightingConfiguration {

	/** The delegate. */
	private DelegateHighlightingConfiguration delegate;
	
	/** The light. */
	final DelegateHighlightingConfiguration light = new LightHighlightingConfiguration();
	
	/** The dark. */
	final DelegateHighlightingConfiguration dark = new DarkHighlightingConfiguration();

	/**
	 * Instantiates a new gaml highlighting configuration.
	 */
	public GamlHighlightingConfiguration() {
		delegate = ThemeHelper.isDark() ? dark : light;
	}

	/**
	 * Change to.
	 *
	 * @param toLight the to light
	 */
	public void changeTo(final boolean toLight) {
		// delegate.saveCurrentPreferences();
		delegate = toLight ? light : dark;
		// delegate.restoreCurrentPreferences();
	}

	@Override
	public void configure(final IHighlightingConfigurationAcceptor acceptor) {
		delegate.configure(acceptor);
	}

}
