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

import java.awt.geom.Rectangle2D;
import java.util.List;

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
import gama.api.types.color.IColor;
import gama.api.types.file.IGamaFile;
import gama.api.types.font.GamaFontFactory;
import gama.api.types.font.IFont;
import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.IPoint;
import gama.api.types.geometry.IShape;
import gama.api.ui.displays.IGraphics;
import gama.api.ui.layers.LayerCoordinates;
import gama.api.utils.interfaces.IImageProvider;
import gama.core.outputs.layers.OverlayLayer;

@symbol(name = { "ui_button", "ui_switch", "ui_slider" }, kind = ISymbolKind.SEQUENCE_STATEMENT,
		with_sequence = true)
@inside(symbols = "overlay")
@facets(value = {
		@facet(name = "label", type = IType.STRING, optional = true, doc = @doc("Text shown on the control.")),
		@facet(name = "var", type = IType.NONE, optional = true, doc = @doc("Writable variable bound to this control.")),
		@facet(name = "value", type = IType.NONE, optional = true, doc = @doc("Read expression; ui_value is available in the change handler.")),
		@facet(name = "min", type = IType.FLOAT, optional = true, doc = @doc("Slider minimum, default 0.")),
		@facet(name = "max", type = IType.FLOAT, optional = true, doc = @doc("Slider maximum, default 100.")),
		@facet(name = "step", type = IType.FLOAT, optional = true, doc = @doc("Slider increment from min; omitted means continuous.")),
		@facet(name = "at", type = IType.POINT, optional = true, doc = @doc("Overlay-local position; screen fractions below 1, otherwise pixels.")),
		@facet(name = "size", type = IType.POINT, optional = true, doc = @doc("Screen extent, default {160,28}.")),
		@facet(name = "anchor", type = IType.POINT, optional = true, doc = @doc("Anchor of the control rectangle; default #top_left.")),
		@facet(name = "font", type = IType.FONT, optional = true, doc = @doc("Label font.")),
		@facet(name = "color", type = IType.COLOR, optional = true, doc = @doc("Text colour.")),
		@facet(name = "background", type = IType.COLOR, optional = true, doc = @doc("Control background colour, or nil for transparent; default white.")),
		@facet(name = "border", type = IType.COLOR, optional = true, doc = @doc("Border colour, or nil for no border (default).")),
		@facet(name = "active", type = IType.BOOL, optional = true, doc = @doc("Whether this control accepts input; default true.")),
		@facet(name = "toggle", type = IType.BOOL, optional = true, doc = @doc("Constant button mode: true binds a persistent boolean checked state.")),
		@facet(name = "image", type = IType.NONE, optional = true, doc = @doc("Button image: an image provider, image file or path. Stretched to the control bounds.")),
		@facet(name = "image_pressed", type = IType.NONE, optional = true, doc = @doc("Button image while pressed inside its bounds; otherwise falls back to hover, checked or default image.")),
		@facet(name = "image_hover", type = IType.NONE, optional = true, doc = @doc("Button image while hovered; otherwise falls back to checked or default image.")),
		@facet(name = "image_checked", type = IType.NONE, optional = true, doc = @doc("Toggle button image while checked; defaults to image_pressed, then image.")),
		@facet(name = "action", type = IType.ACTION, optional = true, doc = @doc("Momentary button handler, as an action or block.")),
		@facet(name = "on_change", type = IType.ACTION, optional = true, doc = @doc("Switch, toggle button or slider change handler, as an action or block."))
}, omissible = "label")
@validator(UiControlStatement.ControlValidator.class)
@doc("Screen-overlay controls. A final block handles activation/change, never drawing. var: binds directly; value: requires a handler. Inline handlers run on the experiment, like event blocks. Pointer activation commits on release.")
public class UiControlStatement extends AbstractStatementSequence {

	public static final String OVERLAY_SCOPE_KEY = "gama.ui.overlay.controls";
	private static final GeometryFactory GEOMETRIES = new GeometryFactory();
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
			final boolean buttonKind = "ui_button".equals(d.getKeyword());
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
			if (d.hasFacet("var") && d.hasFacet("value")) d.error("Use either var: or value:, not both.");
			if (button && (d.hasFacet("var") || d.hasFacet("value") || d.hasFacet("on_change"))) {
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
			final boolean block = d.getOwnChildren().iterator().hasNext();
			if (button && !block && !d.hasFacet("action")) d.error("A button needs action: or a final block.");
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
				if (("ui_switch".equals(d.getKeyword()) || toggle) && e.getGamlType().id() != IType.BOOL) {
					d.error("Switches and toggle buttons require a boolean value.");
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
		final Object shown = overlay.registerControl(this, bounds, current, enabled, min, max, step);
		final IColor color = (IColor) evaluate(scope, "color", enabled ? GamaColorFactory.BLACK : GamaColorFactory.GRAY);
		final IColor background = (IColor) evaluate(scope, "background", GamaColorFactory.WHITE);
		final IColor border = (IColor) evaluate(scope, "border", null);
		final IFont font = (IFont) evaluate(scope, "font", GamaFontFactory.createFont("Helvetica", 14));
		if (color == null || font == null) {
			throw GamaRuntimeException.error("UI control color and font cannot be nil", scope);
		}
		drawRectangle(g, x, y, w, h, background, null);
		if ("ui_button".equals(kind)) {
			final IImageProvider image = buttonImage(scope, overlay, enabled, toggle && Cast.asBool(scope, shown));
			if (image != null) drawImage(g, x, y, w, h, image);
			else if ((toggle && Cast.asBool(scope, shown)) || (enabled && overlay.isControlPressed(this))) {
				drawRectangle(g, x + w * .08, y + h * .12, w * .84, h * .76, null, GamaColorFactory.GRAY);
			}
		}
		drawRectangle(g, x, y, w, h, null, border);
		final String label = Cast.asString(scope, evaluate(scope, "label", ""));
		if (isSlider()) {
			final double fraction = Math.clamp((Cast.asFloat(scope, shown) - min) / (max - min), 0, 1);
			drawRectangle(g, x + w * .05, y + h * .8, w * .9, Math.max(1, h * .08), GamaColorFactory.GRAY, null);
			drawRectangle(g, x + w * .05 + w * .9 * fraction - 3, y + h * .68, 6, h * .3, GamaColorFactory.GRAY, null);
		}
		final TextDrawingAttributes text = new TextDrawingAttributes(null, null,
				GamaPointFactory.create(x + w / 2, y + h * (isSlider() ? .35 : .5)), color);
		text.setFont(font);
		text.setAnchor(GamaPointFactory.create(.5, .5));
		g.drawString("ui_switch".equals(kind) ? label + (Cast.asBool(scope, shown) ? " [on]" : " [off]") : label, text);
		return null;
	}

	private static void drawRectangle(final IGraphics g, final double x, final double y, final double w,
			final double h, final IColor fill, final IColor border) {
		if (fill == null && border == null) return;
		final ShapeDrawingAttributes attributes = rectangleAttributes(fill, border);
		g.drawShape(rectangle(x, y, w, h), attributes);
	}

	private static Geometry rectangle(final double x, final double y, final double w, final double h) {
		return GEOMETRIES.createPolygon(new Coordinate[] {
				new Coordinate(x, y), new Coordinate(x + w, y), new Coordinate(x + w, y + h),
				new Coordinate(x, y + h), new Coordinate(x, y) });
	}

	private static ShapeDrawingAttributes rectangleAttributes(final IColor fill, final IColor border) {
		final ShapeDrawingAttributes attributes = new ShapeDrawingAttributes(null, fill, border, IShape.Type.POLYGON);
		attributes.setLighting(false);
		attributes.setEmpty(fill == null);
		return attributes;
	}

	private static void drawImage(final IGraphics g, final double x, final double y, final double w,
			final double h, final IImageProvider image) {
		final ShapeDrawingAttributes attributes = rectangleAttributes(GamaColorFactory.WHITE, null);
		attributes.setTextures(List.of(image));
		g.drawShape(rectangle(x, y, w, h), attributes);
	}

	private IImageProvider buttonImage(final IScope scope, final OverlayLayer overlay, final boolean active,
			final boolean checked) {
		Object image = null;
		if (active && overlay.isControlPressed(this)) image = evaluate(scope, "image_pressed", null);
		if (image == null && active && overlay.isControlHovered(this)) image = evaluate(scope, "image_hover", null);
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
		return provider;
	}

	public boolean isSlider() { return "ui_slider".equals(kind); }

	public Object changedValue(final Object current) {
		return "ui_switch".equals(kind) || toggle ? !((Boolean) current) : current;
	}

	public void activate(final IScope scope, final Object newValue) {
		enterScope(scope);
		try {
			if (!"ui_button".equals(kind) || toggle) {
				scope.addVarWithValue("ui_value", newValue);
				if (binding instanceof IVarExpression variable) {
					variable.setVal(scope, variable.getGamlType().cast(scope, newValue, null, false), false);
				}
			}
			if (handler != null) {
				final Object action = compiledHandler == null ? handler.value(scope) : compiledHandler;
				if (!(action instanceof IExecutable executable)) {
					throw GamaRuntimeException.error("The UI handler must resolve to an action", scope);
				}
				final var agent = handlerInSimulation ? scope.getSimulation() : scope.getExperiment();
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
