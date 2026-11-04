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
		value = "With explicit `coordinates: #screen`, an overlay uses view-relative positions below 1 in magnitude and logical pixels from 1. Its contents default to screen coordinates, where #px is 1, at: and rectangular size: use the overlay as their reference, and geometric lengths are pixels. `content_coordinates: #world` fits model-unit contents proportionally into the frame, without clipping. Explicit `coordinates: #world` instead creates a camera-attached world layer whose contents default to model units. "
				+ "When coordinates is omitted, the following historical rules are preserved: "
				+ "`overlay` defines a rectangular layer drawn on top of the other layers of a display, at a fixed place in the view: it is not affected by the camera, the zoom or the rotation of the display, and is placed and sized identically in 2D (java2D) and 3D (opengl) displays. "
				+ "Each coordinate of the `position` and `size` facets is interpreted independently: when expressed in pixels (using `#px`), it represents a number of pixels of the view; otherwise, a value in [0,1] (in [-1,1] for the position) represents a fraction of the view and a larger value represents model units (considering that the model occupies the entire view). Negative positions are counted from the right (resp. bottom) border of the view. "
				+ "The statements in the block draw the contents of the overlay in a coordinate system whose origin is its upper-left corner, in which `#px` represents exactly one pixel of the view and model units are scaled as in the view, whatever the size of the overlay. It is therefore recommended to express the locations and sizes of the contents in pixels.",
		usages = { @usage (
				value = "For instance, a legend in the upper-left corner of the view (only one overlay can be defined per display):",
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
						value = "Other placements: a semi-transparent box in the lower-right corner, or a band covering the bottom of the view:",
						examples = {
							@example (
									value = "overlay position: {-160#px, -60#px} size: {150#px, 50#px} background: #gray transparency: 0.5 { ... } // bottom-right corner",
									isExecutable = false),
							@example (
									value = "overlay position: {0, 0.9} size: {1, 0.1} { ... } // band covering the bottom 10% of the view",
									isExecutable = false) }) })
@facets (
		value = { @facet (
				name = LayerData.COORDINATES, type = IType.STRING,
				optional = true, doc = @doc (LayerData.COORDINATES_DOC)),
				@facet (
						name = LayerData.CONTENT_COORDINATES, type = IType.STRING,
						optional = true, doc = @doc (LayerData.CONTENT_COORDINATES_DOC)),
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
						doc = @doc (LayerData.POSITION_DOC + " Overlays without explicit coordinates use the visible view as the reference for their historical hybrid placement, with per-component #px detection and negative positions counted from the opposite edge.")),
				@facet (
						name = IKeyword.SIZE,
						type = IType.POINT,
						optional = true,
						doc = @doc (LayerData.SIZE_DOC + " Overlays without explicit coordinates retain their full-view default {1,1}, per-component #px detection and content scale independent of frame size. Explicit screen overlays default to pixel contents; world contents are fitted proportionally into the frame. No implicit clipping is applied.")),
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
