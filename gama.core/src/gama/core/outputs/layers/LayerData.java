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
	public static final String COORDINATES_DOC = "Explicit XY placement convention: #world uses model units without a threshold at 1; #screen uses fractions for magnitudes below 1 and logical pixels from 1, independently per component. Negative screen positions are measured from the opposite edge. Omit the facet to preserve historical behavior. World Z rules are unchanged; screen frames have no world-depth placement. An omitted size fills the reference extent.";
	public static final String REFERENTIAL_DOC = "The coordinate system of draw contents, independently of layer placement. In #screen, at: and rectangular size: use fractions below 1 and logical pixels from 1; geometric lengths are pixels. In #world, contents use model units. Explicit graphics and overlays inherit coordinates. This facet requires explicit coordinates; screen contents require screen placement.";
	public static final String POSITION_DOC = "Position of the upper-left corner of the layer. With coordinates: #world, XY values are model units. With coordinates: #screen, magnitudes strictly below 1 are viewport fractions and values from 1 are logical pixels; negative values count from the opposite edge. Without coordinates:, the historical hybrid convention is retained: magnitudes up to and including 1 are relative, larger values are model units, and #px performs its historical conversion. World-layer Z behavior is unchanged; screen frames have no world-depth placement.";
	public static final String SIZE_DOC = "Extent of the layer from its origin. With coordinates: #world, XY values are model units. With coordinates: #screen, magnitudes strictly below 1 are viewport fractions and values from 1 are logical pixels: {1,1} is one pixel by one pixel. In either explicit mode, omitting size fills the reference extent. Without coordinates:, the historical hybrid convention is retained: magnitudes up to and including 1 are relative, larger values are model units, and #px performs its historical conversion. The Z component retains the existing scaling convention.";

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

	/** The size is in pixels. */
	boolean positionIsInPixels, sizeIsInPixels;

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
		coordinates = readCoordinates(def, COORDINATES, LayerCoordinates.LEGACY);
		final boolean graphic = def instanceof GraphicLayerStatement;
		contentCoordinates = readCoordinates(def, REFERENTIAL,
				coordinates == LayerCoordinates.LEGACY ? LayerCoordinates.LEGACY
						: graphic ? coordinates : LayerCoordinates.WORLD);
		if (def.getFacet(REFERENTIAL) != null && coordinates == LayerCoordinates.LEGACY) {
			throw GamaRuntimeException.error("referential requires explicit coordinates", null);
		}
		if (contentCoordinates == LayerCoordinates.SCREEN && coordinates == LayerCoordinates.WORLD) {
			throw GamaRuntimeException.error("Screen contents require coordinates: #screen", null);
		}
		final IExpression sizeExp = def.getFacet(SIZE);
		defaultSize = sizeExp == null || def instanceof MeshLayerStatement && sizeExp.getGamlType().isNumber();
		sizeIsInPixels = sizeExp != null && sizeExp.containsPixels();
		size = create(SIZE, sizeExp, POINT, GamaPointFactory.create(1, 1, 1));
		final IExpression posExp = def.getFacet(POSITION);
		positionIsInPixels = posExp != null && posExp.containsPixels();
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
		boolean v = isVisible();
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
		return scope.getClock().getCycle() > 0 && isVisible() != v;
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
		sizeIsInPixels = false;
	}

	@Override
	public void setPosition(final IPoint p) {
		setPosition(p.getX(), p.getY(), p.getZ());
	}

	@Override
	public void setPosition(final double x, final double y, final double z) {
		position = create(POSITION, GamaPointFactory.create(x, y, z));
		positionIsInPixels = false;
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
	public boolean isRelativePosition() { return !positionIsInPixels; }

	@Override
	public boolean isRelativeSize() { return !sizeIsInPixels; }

	@Override
	public Point getSizeInPixels() { return sizeInPixels; }

	@Override
	public Point getPositionInPixels() { return positionInPixels; }

	/**
	 * @param boundingBox
	 * @param g
	 */
	@Override
	public void computePixelsDimensions(final IGraphics g) {
		if (coordinates != LayerCoordinates.LEGACY) {
			computeExplicitDimensions(g);
			return;
		}
		// Voir comment conserver cette information
		final int pixelWidth = g.getDisplayWidth();
		final int pixelHeight = g.getDisplayHeight();
		final double xRatio = g.getxRatioBetweenPixelsAndModelUnits();
		final double yRatio = g.getyRatioBetweenPixelsAndModelUnits();

		IPoint point = getPosition();
		// Computation of x
		final double x = point.getX();

		double relative_x;
		if (!isRelativePosition()) {
			relative_x = xRatio * x;
		} else {
			relative_x = Math.abs(x) <= 1 ? pixelWidth * x : xRatio * x;
		}
		final double absolute_x = Math.signum(x) < 0 ? pixelWidth + relative_x : relative_x;
		// Computation of y

		final double y = point.getY();
		double relative_y;
		if (!isRelativePosition()) {
			relative_y = yRatio * y;
		} else {
			relative_y = Math.abs(y) <= 1 ? pixelHeight * y : yRatio * y;
		}

		// relative_y = Math.abs(y) <= 1 ? pixelHeight * y : yRatio * y;
		final double absolute_y = Math.signum(y) < 0 ? pixelHeight + relative_y : relative_y;

		point = getSize();
		// Computation of width
		final double w = point.getX();
		double absolute_width;
		if (!isRelativeSize()) {
			absolute_width = xRatio * w;
		} else {
			absolute_width = Math.abs(w) <= 1 ? pixelWidth * w : xRatio * w;
		}
		// Computation of height
		final double h = point.getY();
		double absolute_height;
		if (!isRelativeSize()) {
			absolute_height = yRatio * h;
		} else {
			absolute_height = Math.abs(h) <= 1 ? pixelHeight * h : yRatio * h;
		}

		getSizeInPixels().setLocation(absolute_width, absolute_height);
		getPositionInPixels().setLocation(absolute_x, absolute_y);
	}

	/** Resolves the rectangle once for all renderers; world placement never treats small values as fractions. */
	protected void computeExplicitDimensions(final IGraphics g) {
		final IPoint p = getPosition();
		final IPoint s = getSize();
		final double width = g.getViewWidth();
		final double height = g.getViewHeight();
		double x, y, w, h;
		if (coordinates == LayerCoordinates.SCREEN) {
			x = LayerCoordinates.screenPosition(p.getX(), width);
			y = LayerCoordinates.screenPosition(p.getY(), height);
			w = defaultSize ? width : LayerCoordinates.screenLength(s.getX(), width);
			h = defaultSize ? height : LayerCoordinates.screenLength(s.getY(), height);
		} else {
			final double xr = g.getDisplayWidth() / g.getEnvWidth();
			final double yr = g.getDisplayHeight() / g.getEnvHeight();
			x = p.getX() * xr;
			y = p.getY() * yr;
			w = defaultSize ? g.getDisplayWidth() : s.getX() * xr;
			h = defaultSize ? g.getDisplayHeight() : s.getY() * yr;
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
			visible = create(VISIBLE, BOOL, b);
			structuralChangeByUser = true;
		}
	}

	/**
	 * Checks for structurally changed.
	 *
	 * @return true, if successful
	 */
	@Override
	public boolean hasStructurallyChanged() {
		boolean result = transparency.changed() || trace.changed() || refresh.changed() || visible.changed()
				|| structuralChangeByUser;
		structuralChangeByUser = false;
		return result;
	}

}
