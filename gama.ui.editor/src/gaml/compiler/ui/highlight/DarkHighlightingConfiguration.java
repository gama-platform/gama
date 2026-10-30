/*******************************************************************************************************
 *
 * DarkHighlightingConfiguration.java, in gama.ui.editor, is part of the source code of the GAMA modeling and
 * simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ui.highlight;

import com.google.inject.Singleton;

/** Selects the dark half of the current syntax configuration. */
@Singleton
public class DarkHighlightingConfiguration extends DelegateHighlightingConfiguration {

	public DarkHighlightingConfiguration() {
		super("dark");
	}
}
