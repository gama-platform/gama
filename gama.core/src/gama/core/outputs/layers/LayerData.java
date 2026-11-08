/*******************************************************************************************************
 *
 * LayerData.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers;

import static gama.annotations.constants.IKeyword.FADING;
import static gama.annotations.constants.IKeyword.POSITION;
import static gama.annotations.constants.IKeyword.REFRESH;
import static gama.annotations.constants.IKeyword.ROTATE;
import static gama.annotations.constants.IKeyword.SELECTABLE;
import static gama.annotations.constants.IKeyword.SIZE;
import static gama.annotations.constants.IKeyword.TRACE;
import static gama.annotations.constants.IKeyword.TRANSPARENCY;
import static gama.annotations.constants.IKeyword.VISIBLE;
import static gama.api.gaml.types.Types.BOOL;
import static gama.api.gaml.types.Types.FLOAT;
import static gama.api.gaml.types.Types.INT;
import static gama.api.gaml.types.Types.POINT;

import java.awt.Point;
import java.util.concurrent.atomic.AtomicBoolean;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.Cast;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.IPoint;
import gama.api.ui.displays.IGraphics;
import gama.api.ui.layers.ILayerData;
import gama.api.ui.layers.ILayerStatement;
import gama.api.ui.layers.LayerCoordinates;
import gama.api.utils.AttributeHolder;
import gama.api.utils.geometry.IEnvelope;

/**
 * Written by drogoul Modified on 16 nov. 2010
 *
 * @todo Description
 *
 */
public class LayerData extends AttributeHolder implements ILayerData {

	public static final String COORDINATES = "coordinates";
	public static final String REFERENTIAL = "referential";
	public static final String COORDINATES_DOC = "XY placement convention: #world uses model units without a threshold at 1; #screen uses fractions for magnitudes below 1 and logical pixels from 1, independently per component. Overlays and charts default to #screen; other layers default to #world. Negative screen positions are measured from the opposite edge. World Z rules are unchanged; screen frames have no world-depth placement. An omitted size fills the reference extent.";
	public static final String REFERENTIAL_DOC = "The coordinate system of draw contents, independently of layer placement. In #screen, at: and rectangular size: use the layer's dimensions as their reference, fractions below 1 and logical pixels from 1; geometric lengths are pixels and #px is 1. In #world, contents use model units. A screen overlay fits world contents uniformly into its frame from the upper-left corner; screen graphics scale them independently along X and Y. Neither clips overflowing drawings. Font sizes keep their nominal typographic meaning in both modes. Graphics and overlays inherit coordinates, including its default; screen contents require screen placement.";
	public static final String POSITION_DOC = "Position of the upper-left corner of the layer. In #world, XY values are model units, including values below 1 and negative positions. In #screen, magnitudes strictly below 1 are viewport fractions and values from 1 are logical pixels; negative values count from the opposite edge. Overlays and charts default to #screen; other layers default to #world. World-layer Z behavior is unchanged; screen frames have no world-depth placement.";
	public static final String SIZE_DOC = "Extent of the layer from its origin. In #world, XY values are model units. In #screen, magnitudes strictly below 1 are viewport fractions and values from 1 are logical pixels: {1,1} is one pixel by one pixel. Omitting size fills the reference extent. Overlays and charts default to #screen; other layers default to #world. The Z component retains the existing scaling convention.";
	public static final String REFRESH_DOC = "In OpenGL displays, refresh: false retains the layer's drawings instead of rebuilding them each step. The drawings are built when the layer first becomes visible and rebuilt once each time it is shown after being hidden. Explicit display redraws can also rebuild them. Java2D continues to redraw layers.";

	private final LayerCoordinates coordinates;
	private final LayerCoordinates contentCoordinates;
	private boolean defaultSize;

	private static LayerCoordinates readCoordinates(final ILayerStatement def, final String facet,
			final LayerCoordinates fallback) {
		final IExpression expression = def.getFacet(facet);
		if (expression == null) return fallback;
		if (!expression.isConst()) throw GamaRuntimeException.error(facet + " must be a constant #screen or #world", null);
		return switch (String.valueOf(expression.getConstValue())) {
			case "screen" -> LayerCoordinates.SCREEN;
			case "world" -> LayerCoordinates.WORLD;
			default -> throw GamaRuntimeException.error(facet + " must be #screen or #world", null);
		};
	}

	@Override
	public LayerCoordinates getCoordinates() { return coordinates; }

	@Override
	public LayerCoordinates getContentCoordinates() { return contentCoordinates; }

	static {
		// DEBUG.OFF();
	}

	/** The position in pixels. */
	protected final Point positionInPixels = new Point();

	/** The size in pixels. */
	protected final Point sizeInPixels = new Point();

	/** The visible region. */
	IEnvelope visibleRegion;

	/** The rotation. */
	final Attribute<Double> rotation;

	/** The size. */
	Attribute<IPoint> size;

	/** The position. */
	Attribute<IPoint> position;

	/** The refresh. */
	final Attribute<Boolean> refresh;

	/** The fading. */
	final Attribute<Boolean> fading;

	/** The trace. */
	final Attribute<Integer> trace;

	/** The selectable. */
	Attribute<Boolean> selectable;

	/** The transparency. */
	Attribute<Double> transparency;

	/** The visible. */
	Attribute<Boolean> visible;

	private final AtomicBoolean hiddenSinceLastVisibleUpdate = new AtomicBoolean();

	/** The structural change by user. */
	volatile boolean structuralChangeByUser;

	/**
	 * Instantiates a new layer data.
	 *
	 * @param def
	 *            the def
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public LayerData(final ILayerStatement def) throws GamaRuntimeException {
		super(def);
		coordinates = readCoordinates(def, COORDINATES, LayerCoordinates.defaultFor(def.getKeyword()));
		final boolean graphic = def instanceof GraphicLayerStatement;
		contentCoordinates = readCoordinates(def, REFERENTIAL,
				graphic ? coordinates : LayerCoordinates.WORLD);
		if (contentCoordinates == LayerCoordinates.SCREEN && coordinates == LayerCoordinates.WORLD) {
			throw GamaRuntimeException.error("Screen contents require coordinates: #screen", null);
		}
		final IExpression sizeExp = def.getFacet(SIZE);
		defaultSize = sizeExp == null || def instanceof MeshLayerStatement && sizeExp.getGamlType().isNumber();
		size = create(SIZE, sizeExp, POINT, GamaPointFactory.create(1, 1, 1));
		final IExpression posExp = def.getFacet(POSITION);
		position = create(POSITION, posExp, POINT, GamaPointFactory.create());
		refresh = create(REFRESH, def.getRefreshFacet(), BOOL, true);
		fading = create(FADING, BOOL, false);
		visible = create(VISIBLE, BOOL, true);
		trace = create(TRACE, (scope, exp) -> exp.getGamlType() == BOOL && Cast.asBool(scope, exp.value(scope))
				? Integer.MAX_VALUE : Cast.asInt(scope, exp.value(scope)), INT, 0);
		selectable = create(SELECTABLE, BOOL, true);
		transparency = create(TRANSPARENCY,
				(scope, exp) -> Math.min(Math.max(Cast.asFloat(scope, exp.value(scope)), 0d), 1d), FLOAT, 0d);
		rotation = create(ROTATE, FLOAT, 0d);

	}

	@Override
	public boolean compute(final IScope scope, final IGraphics g) throws GamaRuntimeException {
		final LayerCoordinates previous = g.setCoordinateSystem(coordinates);
		try {
			this.refresh(scope);
			if (coordinates == LayerCoordinates.SCREEN && getRotation() != 0d) {
				throw GamaRuntimeException.error("Rotating screen layers is not supported; rotate individual drawings instead", scope);
			}
			computePixelsDimensions(g);
		} finally {
			g.setCoordinateSystem(previous);
		}
		if (!isVisible()) {
			hiddenSinceLastVisibleUpdate.set(true);
			return false;
		}
		return hiddenSinceLastVisibleUpdate.getAndSet(false);
	}

	@Override
	public void setTransparency(final double f) {
		transparency = create(TRANSPARENCY, Math.min(Math.max(f, 0d), 1d));
		structuralChangeByUser = true;
	}

	@Override
	public void setSize(final IPoint p) {
		setSize(p.getX(), p.getY(), p.getZ());
	}

	@Override
	public void setSize(final double width, final double height, final double depth) {
		defaultSize = false;
		size = create(SIZE, GamaPointFactory.create(width, height, depth));
	}

	@Override
	public void setPosition(final IPoint p) {
		setPosition(p.getX(), p.getY(), p.getZ());
	}

	@Override
	public void setPosition(final double x, final double y, final double z) {
		position = create(POSITION, GamaPointFactory.create(x, y, z));
	}

	@Override
	public final Double getTransparency(final IScope scope) {
		return Cast.asFloat(scope, transparency.value(scope));
	}

	@Override
	public IPoint getPosition() {
		// DEBUG.OUT("Position.z = " + position.get().z);
		return position.get();
	}

	@Override
	public IPoint getSize() { return size.get(); }

	@Override
	public Boolean getRefresh() { return refresh.get(); }

	@Override
	public void setSelectable(final Boolean b) { selectable = create(SELECTABLE, b); }

	/**
	 * Method getTrace()
	 *
	 * @see gama.api.ui.layers.ILayerData#getTrace()
	 */
	@Override
	public Integer getTrace() { return trace.get(); }

	@Override
	public Double getRotation() { return rotation.get(); }

	/**
	 * Method getFading()
	 *
	 * @see gama.api.ui.layers.ILayerData#getFading()
	 */
	@Override
	public Boolean getFading() { return fading.get(); }

	@Override
	public Boolean isSelectable() { return selectable.get(); }

	@Override
	public Point getSizeInPixels() { return sizeInPixels; }

	@Override
	public Point getPositionInPixels() { return positionInPixels; }

	/** Resolves the rectangle once per layer update for all renderers. */
	@Override
	public void computePixelsDimensions(final IGraphics g) {
		final IPoint p = getPosition();
		final IPoint s = getSize();
		double x, y, w, h;
		if (coordinates == LayerCoordinates.SCREEN) {
			final double width = g.getViewWidth();
			final double height = g.getViewHeight();
			x = LayerCoordinates.screenPosition(p.getX(), width);
			y = LayerCoordinates.screenPosition(p.getY(), height);
			w = defaultSize ? width : LayerCoordinates.screenLength(s.getX(), width);
			h = defaultSize ? height : LayerCoordinates.screenLength(s.getY(), height);
		} else {
			final double width = g.getDisplayWidth();
			final double height = g.getDisplayHeight();
			final double xr = width / g.getEnvWidth();
			final double yr = height / g.getEnvHeight();
			x = p.getX() * xr;
			y = p.getY() * yr;
			w = defaultSize ? width : s.getX() * xr;
			h = defaultSize ? height : s.getY() * yr;
		}
		if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(w) || !Double.isFinite(h)
				|| w < 0 || h < 0) {
			throw GamaRuntimeException.error("Layer position and size must be finite, and size must be non-negative", null);
		}
		getPositionInPixels().setLocation(x, y);
		getSizeInPixels().setLocation(w, h);
	}

	@Override
	public void setVisibleRegion(final IEnvelope e) { visibleRegion = e; }

	@Override
	public IEnvelope getVisibleRegion() { return visibleRegion; }

	/**
	 * Checks if is visible.
	 *
	 * @return
	 */
	@Override
	public Boolean isVisible() {
		return visible.get();

	}

	/**
	 * Sets the visible.
	 *
	 * @param b
	 *            the new visible
	 */
	@Override
	public void setVisible(final Boolean b) {
		// TODO AD We should maybe force it to a constant ?
		if (isVisible() != b) {
			if (!b) { hiddenSinceLastVisibleUpdate.set(true); }
			visible = create(VISIBLE, BOOL, b);
		}
	}

	/**
	 * Checks for structurally changed.
	 *
	 * @return true, if successful
	 */
	@Override
	public boolean hasStructurallyChanged() {
		boolean result = transparency.changed() || trace.changed() || refresh.changed()
				|| structuralChangeByUser;
		structuralChangeByUser = false;
		return result;
	}

}
