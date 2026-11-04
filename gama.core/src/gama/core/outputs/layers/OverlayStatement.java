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
import gama.annotations.example;
import gama.annotations.facet;
import gama.annotations.facets;
import gama.annotations.inside;
import gama.annotations.symbol;
import gama.annotations.usage;
import gama.annotations.constants.IKeyword;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.compilation.descriptions.IDescription;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;
import gama.api.types.color.GamaColorFactory;
import gama.api.types.color.IColor;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.ui.IOutput;
import gama.api.annotations.validator;

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
@doc (
		value = "An overlay defaults to `coordinates: #screen`: view-relative positions below 1 in magnitude and logical pixels from 1. It is unaffected by camera movement. Its contents also default to screen coordinates, where #px is 1, at: and rectangular size: use the overlay as their reference, and geometric lengths are pixels. Omitted size fills the viewport; {1,1} is one pixel by one pixel. Negative positions count from the opposite edge. `referential: #world` fits model-unit contents proportionally into the frame, without clipping. `coordinates: #world` instead creates a camera-attached layer whose contents default to model units. Only #screen and #world are supported; there is no historical hybrid mode.",
		usages = { @usage (
				value = "An explicit screen HUD: fractional viewport placement, pixel height and local pixel drawings. Only one overlay can be defined per display:",
				examples = {
						@example (
								value = "overlay coordinates: #screen position: {0.02, 0.02} size: {0.96, 80} background: #white {",
								isExecutable = false),
						@example (
								value = "    draw \"Cycle: \" + cycle at: {12, 26} color: #black font: font(\"Helvetica\", 14);",
								isExecutable = false),
						@example (
								value = "    draw circle(6) at: {12, 54} color: #red;",
								isExecutable = false),
						@example (
								value = "}",
								isExecutable = false) }),
				@usage (
						value = "A fixed screen mini-map containing model-unit drawings. Agent locations remain world coordinates; the overlay fits the environment uniformly into its frame:",
						examples = {
								@example (
										value = "overlay coordinates: #screen referential: #world position: {-216, 16} size: {200, 150} {",
										isExecutable = false),
								@example (
										value = "    draw world.shape color: #lightgray;",
										isExecutable = false),
								@example (
										value = "}",
										isExecutable = false) }),
				@usage (
				value = "The default screen mode also accepts #px, whose value is 1 in this context:",
				examples = {
							@example (
									value = "overlay position: {10#px, 10#px} size: {200#px, 100#px} background: #white border: #black rounded: true {",
									isExecutable = false),
							@example (
									value = "    draw \"Cycle: \" + cycle at: {20#px, 30#px} color: #black font: font(\"Helvetica\", 14, #bold);",
									isExecutable = false),
							@example (
									value = "    draw square(10#px) at: {20#px, 60#px} color: #red;",
									isExecutable = false),
							@example (
									value = "}",
									isExecutable = false) }),
				@usage (
						value = "Default screen placements: a semi-transparent box in the lower-right corner, or a band covering the bottom of the view:",
						examples = {
							@example (
									value = "overlay position: {-160#px, -60#px} size: {150#px, 50#px} background: #gray transparency: 0.5 { ... } // bottom-right corner",
									isExecutable = false),
							@example (
									value = "overlay position: {0, 0.9} size: {#display_width, 0.1} { ... } // band covering the bottom 10% of the view",
									isExecutable = false) }) })
@facets (
		value = { @facet (
				name = LayerData.COORDINATES, type = IType.STRING,
				optional = true, doc = @doc (LayerData.COORDINATES_DOC)),
				@facet (
						name = LayerData.REFERENTIAL, type = IType.STRING,
						optional = true, doc = @doc (LayerData.REFERENTIAL_DOC)),
				@facet (
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
						doc = @doc (LayerData.POSITION_DOC)),
				@facet (
						name = IKeyword.SIZE,
						type = IType.POINT,
						optional = true,
						doc = @doc (LayerData.SIZE_DOC + " Screen overlays default to pixel contents; world contents are fitted proportionally into the frame. No implicit clipping is applied.")),
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
@validator (AbstractLayerStatement.CoordinatesValidator.class)
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
		final IList<?> list = GamaListFactory.castToList(scope, color.value(scope));
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
		final IColor c = GamaColorFactory.castToColor(scope, color);
		return new int[] { c.red(), c.green(), c.blue() };
	}

	@Override
	public LayerType getType(final IOutput output) {
		return LayerType.OVERLAY;
	}

	@Override
	public boolean isToCreate() { return !aspect.isEmpty(); }

}
