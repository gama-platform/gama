/*******************************************************************************************************
 *
 * WriteStatement.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.kernel.experiment;

import static gama.core.common.interfaces.IKeyword.FONT;

import java.awt.Color;

import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.ISymbolKind;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.facet;
import gama.annotations.precompiler.GamlAnnotations.facets;
import gama.annotations.precompiler.GamlAnnotations.inside;
import gama.annotations.precompiler.GamlAnnotations.symbol;
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
						doc = @doc ("the font used to draw the text, which can be built with the operator \"font\". ex : font:font(\"Helvetica\", 20 , #bold)")),
				@facet (
						name = IKeyword.CATEGORY,
						type = IType.LABEL,
						optional = true,
						doc = @doc ("a category label, used to group parameters in the interface")),
				@facet (
						name = IKeyword.MESSAGE,
						type = IType.NONE,
						optional = false,
						doc = @doc ("the text to display.")), },
		omissible = IKeyword.MESSAGE)
@doc (
		value = "The statement makes an experiment display text in the parameters view.")
public class TextStatement extends AbstractStatement implements IExperimentDisplayable {

	public TextStatement(final IDescription desc) {
		super(desc);
		message = getFacet(IKeyword.MESSAGE);
		color = getFacet(IKeyword.COLOR);
		category = getFacet(IKeyword.CATEGORY);
		font = getFacet(IKeyword.FONT);
		background = getFacet(IKeyword.BACKGROUND);
	}

	/** The message. */
	final IExpression message;

	/** The color. */
	final IExpression color, category, font, background;

	@Override
	public Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		return null;
	}

	public String getText(final IScope scope) {
		return Cast.asString(scope, message.value(scope));
	}

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
