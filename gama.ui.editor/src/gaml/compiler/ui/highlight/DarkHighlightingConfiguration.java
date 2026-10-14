/*******************************************************************************************************
 *
 * DarkHighlightingConfiguration.java, in gama.ui.shared.modeling, is part of the source code of the
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
 * The Class DarkHighlightingConfiguration.
 */
@Singleton
public class DarkHighlightingConfiguration extends DelegateHighlightingConfiguration {

	/**
	 * Instantiates a new dark highlighting configuration.
	 */
	public DarkHighlightingConfiguration() {
		super("dark");
	}

	@Override
	public TextStyle facetTextStyle() {
		return newStyle(SWT.NORMAL, 224, 108, 117); // Soft coral / rose
	}

	@Override
	public TextStyle pragmaTextStyle() {
		return newStyle(SWT.ITALIC, 139, 148, 158);
	}

	@Override
	public TextStyle typeTextStyle() {
		return newStyle(SWT.BOLD, 78, 201, 176); // Clean teal/cyan
	}

	@Override
	public TextStyle assignTextStyle() {
		return newStyle(SWT.NORMAL, 160, 166, 173);
	}

	@Override
	public TextStyle variableTextStyle() {
		return newStyle(SWT.NORMAL, 142, 166, 219); // Soft periwinkle, distinct from default text
	}

	@Override
	public TextStyle fieldTextStyle() {
		return newStyle(SWT.NORMAL, 94, 198, 163); // Mint
	}

	@Override
	public TextStyle operatorTextStyle() {
		return newStyle(SWT.NORMAL, 97, 175, 239); // Sky blue
	}

	@Override
	public TextStyle reservedTextStyle() {
		return newStyle(SWT.ITALIC, 198, 120, 221); // Soft purple
	}

	@Override
	public TextStyle errorTextStyle() {
		return defaultTextStyle().copy();
	}

	@Override
	public TextStyle unitTextStyle() {
		return newStyle(SWT.ITALIC, 86, 182, 194); // Cyan, distinct from numeric literals
	}

	@Override
	public TextStyle numberTextStyle() {
		return newStyle(SWT.NORMAL, 209, 154, 102); // Warm amber
	}

	@Override
	public TextStyle keywordTextStyle() {
		return newStyle(SWT.BOLD, 198, 120, 221); // Soft purple
	}

	@Override
	public TextStyle punctuationTextStyle() {
		return newStyle(SWT.NORMAL, 160, 166, 173);
	}

	@Override
	public TextStyle stringTextStyle() {
		return newStyle(SWT.NORMAL, 152, 195, 121); // Sage green
	}

	@Override
	public TextStyle varDefTextStyle() {
		return newStyle(SWT.NORMAL, 229, 192, 123); // Soft gold
	}

	@Override
	public TextStyle taskTextStyle() {
		return newStyle(SWT.ITALIC | SWT.BOLD, 229, 192, 123);
	}

	@Override
	public TextStyle commentTextStyle() {
		return newStyle(SWT.ITALIC, 127, 140, 141); // Muted slate gray
	}

	@Override
	public TextStyle defaultTextStyle() {
		final var textStyle = new TextStyle();
		textStyle.setColor(new RGB(171, 178, 191));
		return textStyle;
	}

}
