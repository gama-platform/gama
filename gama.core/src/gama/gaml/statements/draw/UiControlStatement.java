/*******************************************************************************************************
 *
 * UiControlStatement.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements.draw;

import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.Locale;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import gama.annotations.doc;
import gama.annotations.facet;
import gama.annotations.facets;
import gama.annotations.inside;
import gama.annotations.symbol;
import gama.annotations.support.ISymbolKind;
import gama.api.annotations.validator;
import gama.api.compilation.IInternalFacets;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IDescriptionValidator;
import gama.api.compilation.descriptions.IActionDescription;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IVarExpression;
import gama.api.gaml.statements.AbstractStatementSequence;
import gama.api.gaml.statements.IStatement;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.GamaFileType;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.IExecutable;
import gama.api.runtime.scope.IScope;
import gama.api.types.color.GamaColorFactory;
import gama.api.types.color.AdaptiveColor;
import gama.api.types.color.IColor;
import gama.api.types.file.IGamaFile;
import gama.api.types.font.GamaFontFactory;
import gama.api.types.font.IFont;
import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.IPoint;
import gama.api.types.geometry.IShape;
import gama.api.ui.IStatusMessage;
import gama.api.ui.displays.IGraphics;
import gama.api.ui.layers.LayerCoordinates;
import gama.api.utils.interfaces.IImageProvider;
import gama.api.utils.geometry.Scaling3D;
import gama.core.outputs.layers.OverlayLayer;

@symbol(name = { "ui_button", "ui_switch", "ui_slider", "ui_radio" }, kind = ISymbolKind.SEQUENCE_STATEMENT,
		with_sequence = true)
@inside(symbols = "overlay")
@facets(value = {
		@facet(name = "label", type = IType.STRING, optional = true, doc = @doc("Text shown on the control.")),
		@facet(name = "var", type = IType.NONE, optional = true, excludes = "value", doc = @doc("Writable variable bound to this control.")),
		@facet(name = "group", type = IType.STRING, optional = true, requires = "var", doc = @doc("Required for ui_radio: a non-empty group name. Selection clears the other radio bindings in the same overlay and group, including inactive radios.")),
		@facet(name = "value", type = IType.NONE, optional = true, doc = @doc("Read expression; ui_value is available in the change handler.")),
		@facet(name = "min", type = IType.FLOAT, optional = true, doc = @doc("Slider minimum, default 0.")),
		@facet(name = "max", type = IType.FLOAT, optional = true, doc = @doc("Slider maximum, default 100.")),
		@facet(name = "step", type = IType.FLOAT, optional = true, doc = @doc("Slider increment from min; omitted means continuous.")),
		@facet(name = "at", type = IType.POINT, optional = true, doc = @doc("Overlay-local position; screen fractions below 1, otherwise pixels.")),
		@facet(name = "size", type = IType.POINT, optional = true, doc = @doc("Screen extent, default {160,28}.")),
		@facet(name = "anchor", type = IType.POINT, optional = true, doc = @doc("Anchor of the control rectangle; default #top_left.")),
		@facet(name = "font", type = IType.FONT, optional = true, doc = @doc("Label font.")),
		@facet(name = "color", type = IType.COLOR, optional = true, doc = @doc("Text colour; defaults to #adaptive, choosing black or white per label from the scene pixels beneath the overlay, composited with defined overlay and control backgrounds at their rendered opacity. An explicit colour remains unchanged when inactive.")),
		@facet(name = "accent", type = IType.COLOR, optional = true, doc = @doc("Colour of slider tracks, switch indicators and button interaction highlights. Defaults to GAMA's neutral colour, rgb(102, 114, 126). Cannot be nil.")),
		@facet(name = "background", type = IType.COLOR, optional = true, doc = @doc("Control background colour; defaults to nil (transparent), including while hovered, pressed or checked.")),
		@facet(name = "border", type = IType.COLOR, optional = true, doc = @doc("Border colour; defaults to accent for buttons (momentary or toggle), otherwise nil. Explicit nil disables the border.")),
		@facet(name = "rounded", type = IType.BOOL, optional = true, doc = @doc("Control outline: true (default) uses capsule edges; false uses FlatButton-style corners with an 8-pixel arc. Switch indicators and slider thumbs keep their shape.")),
		@facet(name = "active", type = IType.BOOL, optional = true, doc = @doc("Whether this control accepts input; default true.")),
		@facet(name = "toggle", type = IType.BOOL, optional = true, doc = @doc("Constant button mode: true binds a persistent boolean checked state.")),
		@facet(name = "image", type = IType.NONE, optional = true, doc = @doc("Button image: an image provider, image file or path. Declaring any image facet makes the button image-only: no text, background or border. Images are centered and scaled proportionally to fit size:.")),
		@facet(name = "image_pressed", type = IType.NONE, optional = true, doc = @doc("Button image while pressed inside its bounds; otherwise falls back to hover, checked or default image.")),
		@facet(name = "image_hover", type = IType.NONE, optional = true, doc = @doc("Button image while hovered; if omitted or nil, the checked or default fallback image is multiplied by #lightgray, preserving transparency.")),
		@facet(name = "image_checked", type = IType.NONE, optional = true, doc = @doc("Toggle button image while checked; defaults to image_pressed, then image.")),
		@facet(name = "action", type = IType.ACTION, optional = true, excludes = { "var", "value", "on_change" }, doc = @doc("Momentary button handler, as an action or block.")),
		@facet(name = "on_change", type = IType.ACTION, optional = true, doc = @doc("Switch, radio, toggle button or slider change handler, as an action or block. Radio group bindings are updated before handlers run; ui_value is false for deselected radios and true for the selected radio.")),
}, omissible = "label")
@validator(UiControlStatement.ControlValidator.class)
@doc("Screen-overlay controls. A final block handles activation/change, never drawing. var: binds directly; value: requires a handler. Inline handlers run on the experiment, like event blocks. Pointer activation commits on release.")
public class UiControlStatement extends AbstractStatementSequence {

	public static final String OVERLAY_SCOPE_KEY = "gama.ui.overlay.controls";
	private static final GeometryFactory GEOMETRIES = new GeometryFactory();
	private static final FontRenderContext TEXT_CONTEXT = new FontRenderContext(null, true, true);
	private final AdaptiveColor.Contrast labelContrast = new AdaptiveColor.Contrast();
	private final AdaptiveColor.Contrast valueContrast = new AdaptiveColor.Contrast();
	private final String kind;
	private final boolean toggle;
	private final IExpression binding, value, handler;
	private final IExecutable compiledHandler;
	private final boolean handlerInSimulation;

	public UiControlStatement(final IDescription description) {
		super(description);
		kind = description.getKeyword();
		final IExpression toggleMode = getFacet("toggle");
		toggle = "ui_button".equals(kind) && toggleMode != null && Boolean.TRUE.equals(toggleMode.getConstValue());
		binding = getFacet("var");
		value = getFacet("value");
		handler = getFacet("ui_button".equals(kind) && !toggle ? "action" : "on_change");
		// Action references denote descriptions even though they are not marked constant.
		final IActionDescription action = handler != null && handler.getConstValue() instanceof IActionDescription a ? a : null;
		compiledHandler = action == null ? null : (IExecutable) action.compile();
		handlerInSimulation = action != null && !action.getName().startsWith(IInternalFacets.SYNTHETIC)
				&& action.getTypeContext().isModel();
	}

	public static class ControlValidator implements IDescriptionValidator<IDescription> {
		@Override
		public void validate(final IDescription d) {
			final boolean imageButton = "ui_button".equals(d.getKeyword())
					&& (d.hasFacet("image") || d.hasFacet("image_pressed")
							|| d.hasFacet("image_hover") || d.hasFacet("image_checked"));
			for (final String facet : imageButton ? new String[0] : new String[] { "accent", "background", "border" }) {
				final IExpression expression = d.getFacetExpr(facet);
				if (expression != null && expression.isConst() && expression.getConstValue() == AdaptiveColor.INSTANCE) {
					d.error("#adaptive is only supported as the text color: of controls.");
				}
			}
			final boolean buttonKind = "ui_button".equals(d.getKeyword());
			final boolean radio = "ui_radio".equals(d.getKeyword());
			if (radio) {
				if (!d.hasFacet("group")) d.error("ui_radio requires group:.");
				final IExpression group = d.getFacetExpr("group");
				if (group != null && group.isConst()
						&& (!(group.getConstValue() instanceof String name) || name.isBlank())) {
					d.error("ui_radio group: must be a non-empty string.");
				}
			} else if (d.hasFacet("group")) {
				d.error("group: is only supported by ui_radio.");
			}
			final IExpression toggleMode = d.getFacetExpr("toggle");
			final boolean toggle = buttonKind && toggleMode != null && Boolean.TRUE.equals(toggleMode.getConstValue());
			final boolean button = buttonKind && !toggle;
			if (toggleMode != null && (!buttonKind || !toggleMode.isConst())) {
				d.error("toggle: is a constant boolean facet for ui_button only.");
			}
			for (final String image : new String[] { "image", "image_pressed", "image_hover", "image_checked" }) {
				if (d.hasFacet(image) && !buttonKind) d.error(image + ": is a button facet.");
			}
			if (d.hasFacet("image_checked") && !toggle) d.error("image_checked: requires toggle: true.");
			if (button && !d.hasFacet("action")
					&& (d.hasFacet("var") || d.hasFacet("value") || d.hasFacet("on_change"))) {
				d.error("A momentary button accepts action: or a final block; use toggle: true for a boolean binding.");
			}
			if (!button && d.hasFacet("action")) d.error("Use on_change: or a final block for value controls.");
			if (!"ui_slider".equals(d.getKeyword()) && (d.hasFacet("min") || d.hasFacet("max") || d.hasFacet("step"))) {
				d.error("min:, max: and step: are slider facets.");
			}
			final IExpression callback = d.getFacetExpr(button ? "action" : "on_change");
			if (callback != null && callback.getConstValue() instanceof IActionDescription action
					&& !action.getArgNames().isEmpty()) {
				d.error("UI handler actions must be argument-free.");
			}
			final var source = d.getUnderlyingElement();
			final var blockFeature = source == null ? null : source.eClass().getEStructuralFeature("block");
			final boolean block = d.getOwnChildren().iterator().hasNext()
					|| blockFeature != null && source.eIsSet(blockFeature);
			if (button && !block && !d.hasFacet("action")) {
				d.error("A button needs action: or a final block.");
			}
			if (block && d.hasFacet(button ? "action" : "on_change")) {
				d.error("Use either a handler facet or a final block, not both.");
			}
			if (!button && !d.hasFacet("var") && !d.hasFacet("value")) d.error("A value control needs var: or value:.");
			if (d.hasFacet("value") && !block && !d.hasFacet("on_change")) {
				d.error("value: is read-only; provide a change handler or use var:.");
			}
			if (d.hasFacet("var")) {
				final IExpression e = d.getFacetExpr("var");
				if (!(e instanceof IVarExpression v) || v.isNotModifiable()) d.error("var: must be a writable variable.");
			}
			final IExpression e = d.getFacetExpr("var", "value");
			if (e != null && !button) {
				if (("ui_switch".equals(d.getKeyword()) || radio || toggle) && e.getGamlType().id() != IType.BOOL) {
					d.error("Switches, radios and toggle buttons require a boolean value.");
				}
				if ("ui_slider".equals(d.getKeyword()) && !e.getGamlType().isNumber()) {
					d.error("ui_slider requires a numeric value.");
				}
			}
			for (IDescription parent = d.getEnclosingDescription(); parent != null; parent = parent.getEnclosingDescription()) {
				if (!"overlay".equals(parent.getKeyword())) continue;
				for (final String facet : new String[] { "coordinates", "referential" }) {
					final IExpression mode = parent.getFacetExpr(facet);
					if (mode != null && !"screen".equals(String.valueOf(mode.getConstValue()))) {
						d.error("UI controls require a screen overlay with screen contents.");
					}
				}
				break;
			}
		}
	}

	private Object evaluate(final IScope scope, final String facet, final Object fallback) {
		final IExpression expression = getFacet(facet);
		return expression == null ? fallback : expression.value(scope);
	}

	private double number(final IScope scope, final String facet, final double fallback) {
		return Cast.asFloat(scope, evaluate(scope, facet, fallback));
	}

	@Override
	public Object privateExecuteIn(final IScope scope) {
		if (!(scope.getData(OVERLAY_SCOPE_KEY) instanceof OverlayLayer overlay)) {
			throw GamaRuntimeException.error("UI controls can only be drawn inside a screen overlay", scope);
		}
		final IGraphics g = scope.getGraphics();
		final IPoint at = Types.POINT.cast(scope, evaluate(scope, "at", GamaPointFactory.create()), null, false);
		final IPoint size = Types.POINT.cast(scope, evaluate(scope, "size", GamaPointFactory.create(160, 28)), null, false);
		final IPoint anchor = Types.POINT.cast(scope, evaluate(scope, "anchor", GamaPointFactory.create(0, 1)), null, false);
		final double w = LayerCoordinates.screenLength(size.getX(), g.getContentWidth());
		final double h = LayerCoordinates.screenLength(size.getY(), g.getContentHeight());
		final double x = LayerCoordinates.screenPosition(at.getX(), g.getContentWidth()) - w * anchor.getX();
		final double y = LayerCoordinates.screenPosition(at.getY(), g.getContentHeight()) - h * (1 - anchor.getY());
		if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(w) || !Double.isFinite(h) || w <= 0 || h <= 0) {
			throw GamaRuntimeException.error("UI control position and size must be finite, with a positive size", scope);
		}
		final boolean enabled = Cast.asBool(scope, evaluate(scope, "active", true));
		final Object current = binding != null ? binding.value(scope) : value == null ? null : value.value(scope);
		final boolean integer = binding != null && binding.getGamlType().id() == IType.INT;
		final double min = number(scope, "min", 0), max = number(scope, "max", 100), step = number(scope, "step", integer ? 1 : 0);
		if (isSlider() && (!Double.isFinite(min) || !Double.isFinite(max) || !Double.isFinite(max - min)
				|| max <= min || !Double.isFinite(step) || step < 0 || hasFacet("step") && step == 0
				|| !Double.isFinite(Cast.asFloat(scope, current)))) {
			throw GamaRuntimeException.error("A slider needs finite min < max, a finite value and a positive step if supplied", scope);
		}
		if (isSlider() && integer && (min != Math.rint(min) || max != Math.rint(max) || step != Math.rint(step)
				|| min < Integer.MIN_VALUE || max > Integer.MAX_VALUE)) {
			throw GamaRuntimeException.error("An integer slider needs representable integer bounds and an integer step", scope);
		}
		final Rectangle2D bounds = new Rectangle2D.Double(x, y, w, h);
		final Object groupValue = isRadio() ? evaluate(scope, "group", null) : null;
		if (isRadio() && (!(groupValue instanceof String name) || name.isBlank())) {
			throw GamaRuntimeException.error("ui_radio group: must be a non-empty string", scope);
		}
		final String group = isRadio() ? (String) groupValue : null;
		final Object shown = overlay.registerControl(this, bounds, current, enabled, min, max, step, group);
		if ("ui_button".equals(kind) && (hasFacet("image") || hasFacet("image_pressed")
				|| hasFacet("image_hover") || hasFacet("image_checked"))) {
			final ButtonImage image = buttonImage(scope, overlay, enabled, toggle && Cast.asBool(scope, shown));
			if (image != null) drawImage(scope, g, x, y, w, h, image);
			return null;
		}
		final IColor color = (IColor) evaluate(scope, "color", AdaptiveColor.INSTANCE);
		final IColor accent = (IColor) evaluate(scope, "accent", IStatusMessage.NEUTRAL_COLOR);
		final IColor background = (IColor) evaluate(scope, "background", null);
		final IColor border = (IColor) evaluate(scope, "border", "ui_button".equals(kind) ? accent : null);
		final IFont font = (IFont) evaluate(scope, "font", GamaFontFactory.createFont("Helvetica", 14));
		if (color == null || font == null) {
			throw GamaRuntimeException.error("UI control color and font cannot be nil", scope);
		}
		if (accent == null) {
			throw GamaRuntimeException.error("UI control accent cannot be nil", scope);
		}
		if (accent == AdaptiveColor.INSTANCE || background == AdaptiveColor.INSTANCE || border == AdaptiveColor.INSTANCE) {
			throw GamaRuntimeException.error("#adaptive is only supported as the text color: of controls", scope);
		}
		final boolean hovered = enabled && overlay.isControlHovered(this);
		final boolean pressed = enabled && overlay.isControlPressed(this);
		final boolean checked = ("ui_switch".equals(kind) || isRadio() || toggle) && Cast.asBool(scope, shown);
		final IColor controlAccent = enabled ? accent : accent.withAlpha(accent.alpha() / 255d * .45);
		final double accentOpacity = controlAccent.alpha() / 255d;
		final IColor fill = background == null ? null : checked ? controlAccent.withAlpha(accentOpacity * .18)
				: pressed ? controlAccent.withAlpha(accentOpacity * .22) : hovered
						? background.withAlpha(Math.min(1, background.alpha() / 255d + .12)) : background;
		final boolean rounded = Cast.asBool(scope, evaluate(scope, "rounded", true));
		final double radius = rounded ? Math.min(w, h) / 2 : Math.min(4, Math.min(w, h) / 2);
		final double padding = contentPadding(w, h);
		final AdaptiveColor.Background adaptiveBackground = color == AdaptiveColor.INSTANCE && fill != null
				? new AdaptiveColor.Background(new RoundRectangle2D.Double(x, y, w, h, 2 * radius, 2 * radius), fill, 1)
				: null;
		if (background != null) {
			drawRoundedRectangle(g, x + 1, y + 2, w - 2, h - 1,
					GamaColorFactory.createWithRGBA(20, 33, 54, 35), null, radius);
		}
		drawRoundedRectangle(g, x, y, w, h, fill, null, radius);
		final boolean highlighted = border != null
				&& (hovered || pressed || checked && !"ui_switch".equals(kind) && !isRadio());
		drawRoundedRectangle(g, x, y, w, h, null, highlighted ? controlAccent.withAlpha(accentOpacity * .72) : border,
				radius);
		final String label = Cast.asString(scope, evaluate(scope, "label", ""));
		if (isSlider()) {
			final double fraction = Math.clamp((Cast.asFloat(scope, shown) - min) / (max - min), 0, 1);
			final double thumbRadius = sliderThumbRadius(w, h);
			final double trackInset = getSliderTrackInset(w, h);
			final double trackX = x + trackInset, trackWidth = w - 2 * trackInset;
			final double trackHeight = Math.min(4, h * .1);
			final double trackCenterY = y + h - Math.min(8, h * .15) - thumbRadius;
			drawRoundedRectangle(g, trackX, trackCenterY - trackHeight / 2, trackWidth, trackHeight,
					controlAccent.withAlpha(accentOpacity * .2), null, trackHeight / 2);
			drawRoundedRectangle(g, trackX, trackCenterY - trackHeight / 2, trackWidth * fraction, trackHeight,
					controlAccent, null, trackHeight / 2);
			drawRoundedRectangle(g, trackX + trackWidth * fraction - thumbRadius, trackCenterY - thumbRadius,
					2 * thumbRadius, 2 * thumbRadius,
					GamaColorFactory.createWithRGBA(255, 255, 255, 245), controlAccent, thumbRadius);
			drawText(g, label, x + padding, y + h * .32, color, font, 0, .5, labelContrast, adaptiveBackground);
			final String displayValue = integer ? Integer.toString((int) Math.round(Cast.asFloat(scope, shown)))
					: String.format(Locale.ROOT, "%.2f", Cast.asFloat(scope, shown));
			drawText(g, displayValue, x + w - padding, y + h * .32, color, font, 1, .5, valueContrast, adaptiveBackground);
		} else if (isRadio()) {
			final double diameter = Math.min(w - 2 * padding, Math.min(20, h * .65));
			final double circleX = x + w - diameter - padding, circleY = y + (h - diameter) / 2;
			drawText(g, label, x + padding, y + h / 2, color, font, 0, .5, labelContrast, adaptiveBackground);
			drawRoundedRectangle(g, circleX, circleY, diameter, diameter,
					checked ? controlAccent : controlAccent.withAlpha(accentOpacity * .3), null, diameter / 2);
			final double inset = Math.min(2, diameter * .1);
			final double innerDiameter = diameter - 2 * inset;
			drawRoundedRectangle(g, circleX + inset, circleY + inset, innerDiameter, innerDiameter,
					GamaColorFactory.createWithRGBA(255, 255, 255, 250),
					controlAccent.withAlpha(accentOpacity * .12), innerDiameter / 2);
			if (checked) {
				final double dotDiameter = innerDiameter * .55;
				final double dotInset = (diameter - dotDiameter) / 2;
				drawRoundedRectangle(g, circleX + dotInset, circleY + dotInset, dotDiameter, dotDiameter,
						controlAccent, null, dotDiameter / 2);
			}
		} else if ("ui_switch".equals(kind)) {
			final double trackWidth = Math.min(w - 2 * padding, Math.min(38, h * 1.35));
			final double trackHeight = Math.min(trackWidth, Math.min(20, h * .65));
			final double trackX = x + w - trackWidth - padding, trackY = y + (h - trackHeight) / 2;
			drawText(g, label, x + padding, y + h / 2, color, font, 0, .5, labelContrast, adaptiveBackground);
			drawRoundedRectangle(g, trackX, trackY, trackWidth, trackHeight,
					checked ? controlAccent : controlAccent.withAlpha(accentOpacity * .3), null,
					trackHeight / 2);
			final double knobInset = Math.min(2, trackHeight * .1);
			final double knobSize = trackHeight - 2 * knobInset;
			final double knobX = checked ? trackX + trackWidth - knobSize - knobInset : trackX + knobInset;
			drawRoundedRectangle(g, knobX, trackY + knobInset, knobSize, knobSize,
					GamaColorFactory.createWithRGBA(255, 255, 255, 250),
					controlAccent.withAlpha(accentOpacity * .12), knobSize / 2);
		} else {
			drawText(g, label, x + w / 2, y + h / 2, color, font, .5, .5, labelContrast, adaptiveBackground);
		}
		return null;
	}

	private static double contentPadding(final double width, final double height) {
		return Math.min(width * .2, Math.max(18, height / 2));
	}

	private static double sliderThumbRadius(final double width, final double height) {
		return Math.min(7, Math.min(height * .175, width * .1));
	}

	/** Shared by rendering and pointer-to-value conversion so padded track endpoints remain exact. */
	public static double getSliderTrackInset(final double width, final double height) {
		return contentPadding(width, height) + sliderThumbRadius(width, height);
	}

	private static void drawText(final IGraphics g, final String value, final double x, final double y,
			final IColor color, final IFont font, final double anchorX, final double anchorY,
			final AdaptiveColor.Contrast contrast, final AdaptiveColor.Background background) {
		if (value.isEmpty()) return;
		final TextDrawingAttributes text = new TextDrawingAttributes(null, null, GamaPointFactory.create(x, y), color);
		text.setFont(font);
		text.setAnchor(GamaPointFactory.create(anchorX, anchorY));
		if (color == AdaptiveColor.INSTANCE) {
			text.setLighting(false);
			final var awtFont = font.getAwtFont();
			final double lineHeight = awtFont.getLineMetrics("Ag", TEXT_CONTEXT).getHeight();
			Rectangle2D textBounds = null;
			int lineNumber = 0;
			for (final String line : value.split("\\r?\\n", -1)) {
				final Rectangle2D glyphs = awtFont.createGlyphVector(TEXT_CONTEXT, line).getVisualBounds();
				final double lineY = y + lineHeight * lineNumber++;
				if (glyphs.isEmpty()) continue;
				final Rectangle2D bounds = new Rectangle2D.Double(x - glyphs.getWidth() * anchorX,
						lineY - glyphs.getHeight() * (1 - anchorY), glyphs.getWidth(), glyphs.getHeight());
				if (textBounds == null) {
					textBounds = bounds;
				} else {
					textBounds.add(bounds);
				}
			}
			if (textBounds == null) return;
			text.setAdaptiveText(textBounds, contrast, background);
		}
		g.drawString(value, text);
	}

	private static void drawRoundedRectangle(final IGraphics g, final double x, final double y, final double w,
			final double h, final IColor fill, final IColor border, final double requestedRadius) {
		if ((fill == null && border == null) || w <= 0 || h <= 0) return;
		final ShapeDrawingAttributes attributes = rectangleAttributes(fill, border);
		g.drawShape(roundedRectangle(x, y, w, h, requestedRadius), attributes);
	}

	private static Geometry roundedRectangle(final double x, final double y, final double w, final double h,
			final double requestedRadius) {
		final double radius = Math.min(Math.max(0, requestedRadius), Math.min(w, h) / 2);
		if (radius == 0) {
			return GEOMETRIES.createPolygon(new Coordinate[] {
					new Coordinate(x, y), new Coordinate(x + w, y), new Coordinate(x + w, y + h),
					new Coordinate(x, y + h), new Coordinate(x, y) });
		}
		final Coordinate[] coordinates = new Coordinate[53];
		int index = 0;
		for (int corner = 0; corner < 4; corner++) {
			final double centerX = x + (corner == 0 || corner == 3 ? radius : w - radius);
			final double centerY = y + (corner < 2 ? radius : h - radius);
			final double start = Math.PI + corner * Math.PI / 2;
			for (int segment = 0; segment <= 12; segment++) {
				final double angle = start + segment * Math.PI / 24;
				coordinates[index++] = new Coordinate(centerX + radius * Math.cos(angle),
						centerY + radius * Math.sin(angle));
			}
		}
		coordinates[index] = new Coordinate(coordinates[0]);
		return GEOMETRIES.createPolygon(coordinates);
	}

	private static ShapeDrawingAttributes rectangleAttributes(final IColor fill, final IColor border) {
		final ShapeDrawingAttributes attributes = new ShapeDrawingAttributes(null, fill, border, IShape.Type.POLYGON);
		attributes.setLighting(false);
		attributes.setEmpty(fill == null);
		return attributes;
	}

	private record ButtonImage(IImageProvider provider, boolean tinted) {}

	private static void drawImage(final IScope scope, final IGraphics g, final double x, final double y, final double w,
			final double h, final ButtonImage selected) {
		final IImageProvider image = selected.provider();
		final int width = image.getCols(scope), height = image.getRows(scope);
		if (width <= 0 || height <= 0) {
			throw GamaRuntimeException.error("Button images must have positive dimensions", scope);
		}
		final double scale = Math.min(w / width, h / height);
		final double imageWidth = width * scale, imageHeight = height * scale;
		final IPoint location = g.is2D()
				? GamaPointFactory.create(x + (w - imageWidth) / 2, y + (h - imageHeight) / 2)
				: GamaPointFactory.create(x + w / 2, y + h / 2);
		final AssetDrawingAttributes attributes = new AssetDrawingAttributes(
				Scaling3D.of(imageWidth, imageHeight, 0), null, location,
				selected.tinted() && !g.is2D() ? GamaColorFactory.LIGHT_GRAY : GamaColorFactory.WHITE,
				null, null, null, true, false);
		if (selected.tinted() && g.is2D()) {
			// Java2D image drawing does not multiply pixels by the drawing colour.
			final BufferedImage source = image.getImage(scope, true);
			if (source == null) throw GamaRuntimeException.error("Button image could not be loaded", scope);
			final BufferedImage tinted = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
			final var graphics = tinted.createGraphics();
			try {
				graphics.drawImage(source, 0, 0, null);
			} finally {
				graphics.dispose();
			}
			final IColor tint = GamaColorFactory.LIGHT_GRAY;
			new RescaleOp(new float[] { tint.red() / 255f, tint.green() / 255f, tint.blue() / 255f, 1f },
					new float[4], null).filter(tinted, tinted);
			g.drawImage(tinted, attributes);
		} else {
			g.drawAsset(image, attributes);
		}
	}

	private ButtonImage buttonImage(final IScope scope, final OverlayLayer overlay, final boolean active,
			final boolean checked) {
		Object image = null;
		if (active && overlay.isControlPressed(this)) image = evaluate(scope, "image_pressed", null);
		boolean tinted = false;
		if (image == null && active && overlay.isControlHovered(this)) {
			image = evaluate(scope, "image_hover", null);
			tinted = image == null;
		}
		if (image == null && checked) {
			image = evaluate(scope, "image_checked", null);
			if (image == null) image = evaluate(scope, "image_pressed", null);
		}
		if (image == null) image = evaluate(scope, "image", null);
		if (image == null) return null;
		if (image instanceof String path) {
			final IGamaFile file = GamaFileType.createFile(scope, path, false, null);
			if (!file.exists(scope)) throw GamaRuntimeException.error("Button image file not found: " + path, scope);
			image = file;
		}
		if (!(image instanceof IImageProvider provider)) {
			throw GamaRuntimeException.error("Button images must be image providers, image files or image paths", scope);
		}
		return new ButtonImage(provider, tinted);
	}

	public boolean isSlider() { return "ui_slider".equals(kind); }

	public boolean isRadio() { return "ui_radio".equals(kind); }

	public Object changedValue(final Object current) {
		if (isRadio()) return true;
		return "ui_switch".equals(kind) || toggle ? !((Boolean) current) : current;
	}

	public Object getBoundValue(final IScope scope) { return binding.value(scope); }

	public void setBoundValue(final IScope scope, final Object newValue) {
		if (binding instanceof IVarExpression variable) {
			variable.setVal(scope, variable.getGamlType().cast(scope, newValue, null, false), false);
		}
	}

	public void activate(final IScope scope, final Object newValue) {
		setBoundValue(scope, newValue);
		notifyChange(scope, newValue);
	}

	public void notifyChange(final IScope scope, final Object newValue) {
		enterScope(scope);
		try {
			if (!"ui_button".equals(kind) || toggle) {
				scope.addVarWithValue("ui_value", newValue);
			}
			if (handler != null) {
				final Object action = compiledHandler == null ? handler.value(scope) : compiledHandler;
				if (!(action instanceof IExecutable executable)) {
					throw GamaRuntimeException.error("The UI handler must resolve to an action", scope);
				}
				final var agent = handlerInSimulation ? scope.getRoot().getSimulation() : scope.getExperiment();
				if (agent != null && !agent.dead()) scope.execute(executable, agent, null);
			} else if (commands != null) {
				for (final IStatement statement : commands) {
					if (!scope.execute(statement).passed()) break;
				}
			}
		} finally {
			leaveScope(scope);
		}
	}

	@Override
	public void dispose() {
		if (compiledHandler instanceof IStatement statement) statement.dispose();
		super.dispose();
	}
}
