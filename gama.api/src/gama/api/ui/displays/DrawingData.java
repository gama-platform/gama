/*******************************************************************************************************
 *
 * DrawingData.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.ui.displays;

import gama.api.constants.IKeyword;
import gama.api.data.factories.GamaPairFactory;
import gama.api.data.objects.IColor;
import gama.api.data.objects.IFont;
import gama.api.data.objects.IList;
import gama.api.data.objects.IPair;
import gama.api.data.objects.IPoint;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.constants.GamlCoreConstants;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.statements.IStatement;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.utils.AttributeHolder;
import gama.api.utils.color.GamaColorFactory;
import gama.api.utils.geometry.AxisAngle;
import gama.api.utils.geometry.GamaPointFactory;
import gama.api.utils.geometry.Rotation3D;
import gama.api.utils.list.GamaListFactory;
import gama.api.utils.prefs.GamaPreferences;

/**
 * Class DrawingData. This class contains a number of attributes to help draw geometries, pictures, files and text.
 * These attributes are supplied either by the draw statement or by the layer
 *
 * @author drogoul
 * @since 28 janv. 2016
 *
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class DrawingData extends AttributeHolder {

	/** The Constant DEFAULT_BORDER_COLOR. */
	static final IColor DEFAULT_BORDER_COLOR = GamaColorFactory.BLACK;

	/** The size. */
	public final Attribute<IPoint> size;

	/** The depth. */
	public final Attribute<Double> depth;

	/** The rotation. */
	public final Attribute<AxisAngle> rotation;

	/** The location. */
	public final Attribute<IPoint> location;

	/** The anchor. */
	public final Attribute<IPoint> anchor;

	/** The empty. */
	public final Attribute<Boolean> empty;

	/** The color. */
	public final Attribute<IColor> border, color;

	/** The font. */
	public final Attribute<IFont> font;

	/** The texture. */
	public final Attribute<IList> texture;

	/** The material. */
	// final Attribute<GamaMaterial> material;

	/** The perspective. */
	public final Attribute<Boolean> perspective;

	/** The line width. */
	public final Attribute<Double> lineWidth;

	/** The lighting. */
	public final Attribute<Boolean> lighting;

	/** The precision. */
	public final Attribute<Double> precision;

	/**
	 * Instantiates a new drawing data.
	 *
	 * @param symbol
	 *            the symbol
	 */
	public DrawingData(final IStatement.Draw symbol) {
		super(symbol);
		this.size = create(IKeyword.SIZE, this::castSize, Types.POINT, null);
		this.lighting = create(IKeyword.LIGHTED, Types.BOOL, true);
		this.depth = create(IKeyword.DEPTH, Types.FLOAT, null);
		this.precision = create("precision", Types.FLOAT, 0.01);
		this.rotation = create(IKeyword.ROTATE, this::castRotation, Types.NO_TYPE, null);
		this.anchor = create(IKeyword.ANCHOR, this::castAnchor, Types.POINT, GamlCoreConstants.bottom_left);
		this.location = create(IKeyword.AT, Types.POINT, null);
		this.empty = create(IKeyword.WIREFRAME, Types.BOOL, false);
		this.border = create(IKeyword.BORDER, this::castBorder, Types.COLOR, null);
		this.color = create(IKeyword.COLOR, this::castColor, Types.COLOR, null);
		this.font = create(IKeyword.FONT, Types.FONT, GamaPreferences.Displays.DEFAULT_DISPLAY_FONT.getValue());
		this.texture = create(IKeyword.TEXTURE, this::castTexture, Types.LIST, null);
		// this.material = create(IKeyword.MATERIAL, Types.MATERIAL, null);
		this.perspective = create(IKeyword.PERSPECTIVE, Types.BOOL, true);
		this.lineWidth = create(IKeyword.WIDTH, Types.FLOAT, GamaPreferences.Displays.CORE_LINE_WIDTH.getValue());
	}

	/**
	 * Cast size.
	 *
	 * @param scope
	 *            the scope
	 * @param exp
	 *            the exp
	 * @return the gama point
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	private IPoint castSize(final IScope scope, final IExpression exp) throws GamaRuntimeException {
		if (exp.getGamlType().isNumber()) {
			final double val = Cast.asFloat(scope, exp.value(scope));
			// We do not consider the z ordinate -- see Issue #1539
			return GamaPointFactory.create(val, val, 0);
		}
		return (IPoint) exp.value(scope);
	}

	/**
	 * Cast rotation.
	 *
	 * @param scope
	 *            the scope
	 * @param exp
	 *            the exp
	 * @return the axis angle
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	private AxisAngle castRotation(final IScope scope, final IExpression exp) throws GamaRuntimeException {
		if (exp.getGamlType().getGamlType() == Types.PAIR) {
			final IPair currentRotation = GamaPairFactory.toPair(scope, exp.value(scope), true);
			return new AxisAngle(GamaPointFactory.toPoint(scope, currentRotation.getValue()),
					Cast.asFloat(scope, currentRotation.getKey()));
		}
		return new AxisAngle(Rotation3D.PLUS_K, Cast.asFloat(scope, exp.value(scope)));
	}

	/**
	 * Cast anchor.
	 *
	 * @param scope
	 *            the scope
	 * @param exp
	 *            the exp
	 * @return the gama point
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	private IPoint castAnchor(final IScope scope, final IExpression exp) throws GamaRuntimeException {
		final IPoint p = GamaPointFactory.toPoint(scope, exp.value(scope));
		p.setX(Math.min(1d, Math.max(p.getX(), 0d)));
		p.setY(Math.min(1d, Math.max(p.getY(), 0d)));
		return p;
	}

	/**
	 * Cast texture.
	 *
	 * @param scope
	 *            the scope
	 * @param exp
	 *            the exp
	 * @return the i list
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	private IList castTexture(final IScope scope, final IExpression exp) throws GamaRuntimeException {
		if (exp.getGamlType().getGamlType() == Types.LIST)
			return GamaListFactory.toList(scope, exp.value(scope), Types.STRING, false);
		return GamaListFactory.wrap(Types.NO_TYPE, exp.value(scope));
	}

	/**
	 * Cast color.
	 *
	 * @param scope
	 *            the scope
	 * @param exp
	 *            the exp
	 * @return the gama color
	 */
	private IColor castColor(final IScope scope, final IExpression exp) {
		return switch (exp.getGamlType().id()) {
			case IType.COLOR -> GamaColorFactory.createFrom(scope, exp.value(scope));
			default -> null;
		};
	}

	/**
	 * Cast border.
	 *
	 * @param scope
	 *            the scope
	 * @param exp
	 *            the exp
	 * @return the gama color
	 */
	private IColor castBorder(final IScope scope, final IExpression exp) {
		if (exp.getGamlType() != Types.BOOL) return (IColor) exp.value(scope);
		final boolean hasBorder = Cast.asBool(scope, exp.value(scope));
		if (hasBorder) return DEFAULT_BORDER_COLOR;
		return null;
	}

	/**
	 * Gets the location.
	 *
	 * @return the location
	 */
	public IPoint getLocation() { return location.get() == null ? null : location.get(); }

	/**
	 * Gets the anchor.
	 *
	 * @return the anchor
	 */
	public IPoint getAnchor() { return anchor.get() == null ? null : anchor.get(); }

}
