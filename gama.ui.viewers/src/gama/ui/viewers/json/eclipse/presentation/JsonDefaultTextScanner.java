/*******************************************************************************************************
 *
 * JsonDefaultTextScanner.java, in gama.ui.viewers, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.viewers.json.eclipse.presentation;

import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.WhitespaceRule;

import gama.ui.viewers.json.eclipse.ColorManager;

/**
 * The Class JsonDefaultTextScanner.
 */
public class JsonDefaultTextScanner extends RuleBasedScanner {

	/**
	 * Instantiates a new highspeed JSON default text scanner.
	 *
	 * @param manager
	 *            the manager
	 */
	public JsonDefaultTextScanner(final ColorManager manager) {
		IRule[] rules = new IRule[1];
		rules[0] = new WhitespaceRule(new JsonWhitespaceDetector());

		setRules(rules);
	}
}
