/*******************************************************************************************************
 *
 * TextStatement.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.kernel.experiment;

import static gama.core.common.interfaces.IKeyword.FONT;

import java.awt.Color;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.facet;
import gama.annotations.precompiler.GamlAnnotations.facets;
import gama.annotations.precompiler.GamlAnnotations.inside;
import gama.annotations.precompiler.GamlAnnotations.symbol;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.ISymbolKind;
import gama.core.common.interfaces.IKeyword;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaColor;
import gama.core.util.GamaFont;
import gama.gaml.descriptions.IDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.operators.Cast;
import gama.gaml.statements.AbstractStatement;
import gama.gaml.types.GamaFontType;
import gama.gaml.types.IType;

/**
 * Written by drogoul Modified on 6 févr. 2010
 *
 * @todo Description
 *
 */

@symbol (
		name = IKeyword.TEXT,
		kind = ISymbolKind.SINGLE_STATEMENT,
		with_sequence = false,
		concept = { IConcept.GUI, IConcept.PARAMETER, IConcept.TEXT })
@inside (
		kinds = { ISymbolKind.EXPERIMENT })
@facets (
		value = { @facet (
				name = IKeyword.COLOR,
				type = IType.COLOR,
				optional = true,
				doc = @doc ("The color with wich the text will be displayed")),
				@facet (
						name = IKeyword.BACKGROUND,
						type = IType.COLOR,
						optional = true,
						doc = @doc ("The color of the background of the text")),
				@facet (
						name = FONT,
						type = { IType.FONT, IType.STRING },
						optional = true,
						doc = @doc ("The font used to draw the text, which can be built with the operator \"font\". ex : font:font(\"Helvetica\", 20 , #bold).")),
				@facet (
						name = IKeyword.CATEGORY,
						type = IType.LABEL,
						optional = true,
						doc = @doc ("A category label, used to group parameters in the interface")),
				@facet (
						name = IKeyword.FORMAT,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("Whether or not to interpret the text as formatted using XML / hyperlinks.")),
				@facet (
						name = IKeyword.MESSAGE,
						type = IType.NONE,
						optional = false,
						doc = @doc ("""
								The text to display.
								If `format` is false or not specified, the font, color, and background are controlled by the respective facets.
								If `format` is true, the text will instead be interpreted as a text with a few XML tags and hyperlinks that will be automatically detected. When configured to use formatting XML, it requires any ampersand (&) characters in the text to be replaced by the entity &amp;. The following tags can be used:\r
								\r
								p - for defining paragraphs. The following attributes are allowed:\r
									vspace - if set to 'false', no vertical space will be added (default is 'true')\r
								li - for defining list items. The following attributes are allowed:\r
									vspace - the same as with the p tag\r
									indent - the number of pixels to indent the text in the list item\r
									bindent - the number of pixels to indent the bullet itself\r
								Text in paragraphs and list items will be wrapped according to the width of the control. The following tags can appear as children of either p or li elements:\r
								\r
								a - to render a hyperlink. Element accepts attribute 'href'. The element also accepts 'nowrap' attribute (default is false). When set to 'true', the hyperlink will not be wrapped. Hyperlinks automatically created when 'http://' is encountered in text are not wrapped.\r
								br - forced line break (no attributes).\r
								\r
								""")), },
		omissible = IKeyword.MESSAGE)
@doc (
		value = "The statement makes an experiment display text in the parameters view.")
public class TextStatement extends AbstractStatement implements IExperimentDisplayable {

	/**
	 * Instantiates a new text statement.
	 *
	 * @param desc
	 *            the desc
	 */
	public TextStatement(final IDescription desc) {
		super(desc);
		format = getFacet(IKeyword.FORMAT);
		message = getFacet(IKeyword.MESSAGE);
		color = getFacet(IKeyword.COLOR);
		category = getFacet(IKeyword.CATEGORY);
		font = getFacet(IKeyword.FONT);
		background = getFacet(IKeyword.BACKGROUND);
	}

	/** The message. */
	final IExpression message;

	/** The color. */
	final IExpression color, category, font, background, format;

	@Override
	public Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		return null;
	}

	/**
	 * Gets the text.
	 *
	 * @param scope
	 *            the scope
	 * @return the text
	 */
	public String getText(final IScope scope) {
		return Cast.asString(scope, message.value(scope));
	}

	/**
	 * Checks if is xml.
	 *
	 * @param scope
	 *            the scope
	 * @return true, if is xml
	 */
	public boolean isXML(final IScope scope) {
		if (format == null) return false;
		return Cast.asBool(scope, format.value(scope));
	}

	/**
	 * Gets the font.
	 *
	 * @param scope
	 *            the scope
	 * @return the font
	 */
	public GamaFont getFont(final IScope scope) {
		if (font == null) return null;
		return GamaFontType.staticCast(scope, font.value(scope), false);
	}

	@Override
	public GamaColor getColor(final IScope scope) {
		GamaColor rgb = null;
		if (color != null) { rgb = Cast.asColor(scope, color.value(scope)); }
		return rgb;
	}

	/**
	 * Gets the background.
	 *
	 * @param scope
	 *            the scope
	 * @return the background
	 */
	public Color getBackground(final IScope scope) {
		GamaColor rgb = null;
		if (background != null) { rgb = Cast.asColor(scope, background.value(scope)); }
		return rgb;
	}

	@Override
	public String getTitle() { return ""; }

	@Override
	public String getUnitLabel(final IScope scope) {
		return "";
	}

	@Override
	public void setUnitLabel(final String label) {}

	@Override
	public boolean isDefinedInExperiment() { return true; }

	@Override
	public String getCategory() {
		if (category == null) return IExperimentDisplayable.super.getCategory();
		return category.literalValue();
	}

}
