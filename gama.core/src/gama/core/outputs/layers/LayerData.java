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
import gama.api.utils.AttributeHolder;
import gama.api.utils.geometry.IEnvelope;

/**
 * Written by drogoul Modified on 16 nov. 2010
 *
 * @todo Description
 *
 */
public class LayerData extends AttributeHolder implements ILayerData {

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
		final IExpression sizeExp = def.getFacet(SIZE);
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
		this.refresh(scope);
		computePixelsDimensions(g);
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
