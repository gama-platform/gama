/*******************************************************************************************************
 *
 * OverlayStatement.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gama.annotations.doc;
import gama.annotations.facet;
import gama.annotations.facets;
import gama.annotations.inside;
import gama.annotations.symbol;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.compilation.descriptions.IDescription;
import gama.api.constants.IKeyword;
import gama.api.data.factories.GamaColorFactory;
import gama.api.data.factories.GamaListFactory;
import gama.api.data.objects.IColor;
import gama.api.data.objects.IList;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;
import gama.api.ui.IOutput;

/**
 * The Class OverlayStatement.
 */
@symbol (
		name = IKeyword.OVERLAY,
		kind = ISymbolKind.LAYER,
		with_sequence = true,
		unique_in_context = true,
		concept = { IConcept.DISPLAY })
@inside (
		symbols = IKeyword.DISPLAY)
@facets (
		value = { @facet (
				name = IKeyword.ROUNDED,
				type = IType.BOOL,
				optional = true,
				doc = @doc ("Whether or not the rectangular shape of the overlay should be rounded. True by default")),
				@facet (
						name = IKeyword.BORDER,
						type = IType.COLOR,
						optional = true,
						doc = @doc ("Color to apply to the border of the rectangular shape of the overlay. Nil by default")),
				@facet (
						name = IKeyword.POSITION,
						type = IType.POINT,
						optional = true,
						doc = @doc ("position of the upper-left corner of the layer. Note that if coordinates are in [0,1[, the position is relative to the size of the environment (e.g. {0.5,0.5} refers to the middle of the display) whereas it is absolute when coordinates are greater than 1 for x and y. The z-ordinate can only be defined between 0 and 1. The position can only be a 3D point {0.5, 0.5, 0.5}, the last coordinate specifying the elevation of the layer. In case of negative value OpenGl will position the layer out of the environment.")),
				@facet (
						name = IKeyword.SIZE,
						type = IType.POINT,
						optional = true,
						doc = @doc ("extent of the layer in the view from its position. Coordinates in [0,1[ are treated as percentages of the total surface of the view, while coordinates > 1 are treated as absolute sizes in model units (i.e. considering the model occupies the entire view). Unlike  'position', no elevation can be provided with the z coordinate ")),
				@facet (
						name = IKeyword.TRANSPARENCY,
						type = IType.FLOAT,
						optional = true,
						doc = @doc ("the transparency rate of the overlay (between 0 -- opaque and 1 -- fully transparent) when it is displayed inside the view. The bottom overlay will remain at 0.75")),
				@facet (
						name = IKeyword.VISIBLE,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("Defines whether this layer is visible or not")),
				@facet (
						name = IKeyword.BACKGROUND,
						type = IType.COLOR,
						optional = true,
						doc = @doc ("the background color of the overlay displayed inside the view (the bottom overlay remains black)")),
				@facet (
						name = IKeyword.COLOR,
						type = { IType.LIST, IType.COLOR },
						of = IType.COLOR,
						optional = true,
						doc = @doc ("the color(s) used to display the expressions given in the 'left', 'center' and 'right' facets")) })
public class OverlayStatement extends GraphicLayerStatement {

	/** The color. */
	final IExpression color;

	/** The center value. */
	String leftValue, rightValue, centerValue;

	/** The constant colors. */
	List<int[]> constantColors;

	/**
	 * Instantiates a new overlay statement.
	 *
	 * @param desc
	 *            the desc
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public OverlayStatement(final IDescription desc) throws GamaRuntimeException {
		super(desc);
		color = getFacet(IKeyword.COLOR);

		if (color != null && color.isConst()) { constantColors = computeColors(null); }
	}

	/**
	 * Compute colors.
	 *
	 * @param scope
	 *            the scope
	 * @return the list
	 */
	private List<int[]> computeColors(final IScope scope) {
		if (constantColors != null) return constantColors;
		if (color == null) return null;
		if (color.getGamlType().id() != IType.LIST) {
			final int[] rgb = computeColor(scope, color.value(scope));
			return Arrays.asList(rgb, rgb, rgb);
		}
		final IList<?> list = GamaListFactory.toList(scope, color.value(scope));
		final List<int[]> result = new ArrayList<>();
		int i = 0;
		for (final Object o : list) {
			final int[] rgb = computeColor(scope, o);
			result.add(rgb);
			if (++i > 2) { break; }
		}
		return result;
	}

	/**
	 * Compute color.
	 *
	 * @param scope
	 *            the scope
	 * @param color
	 *            the color
	 * @return the int[]
	 */
	private static int[] computeColor(final IScope scope, final Object color) {
		final IColor c = GamaColorFactory.createFrom(scope, color);
		return new int[] { c.red(), c.green(), c.blue() };
	}

	@Override
	public LayerType getType(final IOutput output) {
		return LayerType.OVERLAY;
	}

	@Override
	public boolean isToCreate() { return !aspect.isEmpty(); }

}
