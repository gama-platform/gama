/*******************************************************************************************************
 *
 * LightHighlightingConfiguration.java, in gama.ui.shared.modeling, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gaml.compiler.ui.highlight;

/**
 * Written by drogoul Modified on 16 nov. 2011
 *
 * @todo Description
 *
 */

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.xtext.ui.editor.utils.TextStyle;

import com.google.inject.Singleton;

/**
 * The Class LightHighlightingConfiguration.
 */
@Singleton
public class LightHighlightingConfiguration extends DelegateHighlightingConfiguration {

	/**
	 * Instantiates a new light highlighting configuration.
	 */
	public LightHighlightingConfiguration() {
		super("light");
	}

	@Override
	public TextStyle facetTextStyle() {
		return newStyle(SWT.NORMAL, 176, 58, 132); // Modern magenta / rose
	}

	@Override
	public TextStyle pragmaTextStyle() {
		return newStyle(SWT.ITALIC, 140, 149, 159);
	}

	@Override
	public TextStyle typeTextStyle() {
		return newStyle(SWT.BOLD, 9, 105, 218); // Modern crisp blue
	}

	@Override
	public TextStyle assignTextStyle() {
		return newStyle(SWT.NORMAL, 87, 96, 106);
	}

	@Override
	public TextStyle variableTextStyle() {
		return newStyle(SWT.NORMAL, 31, 35, 40);
	}

	@Override
	public TextStyle fieldTextStyle() {
		return newStyle(SWT.NORMAL, 44, 93, 99); // Teal-slate
	}

	@Override
	public TextStyle operatorTextStyle() {
		return newStyle(SWT.NORMAL, 5, 80, 174); // Royal blue
	}

	@Override
	public TextStyle reservedTextStyle() {
		return newStyle(SWT.ITALIC, 142, 68, 173);
	}

	@Override
	public TextStyle errorTextStyle() {
		return newStyle();
	}

	@Override
	public TextStyle unitTextStyle() {
		return newStyle(SWT.ITALIC, 181, 83, 9);
	}

	@Override
	public TextStyle numberTextStyle() {
		return newStyle(SWT.NORMAL, 181, 83, 9); // Warm amber/rust
	}

	@Override
	public TextStyle keywordTextStyle() {
		return newStyle(SWT.BOLD, 142, 68, 173); // Purple
	}

	@Override
	public TextStyle punctuationTextStyle() {
		return newStyle(SWT.NORMAL, 87, 96, 106);
	}

	@Override
	public TextStyle stringTextStyle() {
		return newStyle(SWT.NORMAL, 17, 99, 41); // Forest green
	}

	@Override
	public TextStyle varDefTextStyle() {
		return newStyle(SWT.NORMAL, 27, 73, 101); // Steel blue
	}

	@Override
	public TextStyle taskTextStyle() {
		return newStyle(SWT.ITALIC | SWT.BOLD, 181, 83, 9);
	}

	@Override
	public TextStyle commentTextStyle() {
		return newStyle(SWT.ITALIC, 110, 119, 129); // Muted slate gray
	}

	@Override
	public TextStyle defaultTextStyle() {
		final var textStyle = new TextStyle();
		textStyle.setColor(new RGB(31, 35, 40));
		return textStyle;
	}

}
