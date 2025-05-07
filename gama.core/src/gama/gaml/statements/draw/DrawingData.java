/*******************************************************************************************************
 *
 * DrawingData.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements.draw;

import java.awt.Color;

import gama.core.common.geometry.AxisAngle;
import gama.core.common.geometry.Rotation3D;
import gama.core.common.interfaces.IKeyword;
import gama.core.common.preferences.GamaPreferences;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaColor;
import gama.core.util.GamaFont;
import gama.core.util.GamaListFactory;
import gama.core.util.GamaPair;
import gama.core.util.IList;
import gama.gaml.constants.GamlCoreConstants;
import gama.gaml.expressions.IExpression;
import gama.gaml.operators.Cast;
import gama.gaml.types.GamaListType;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

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
	static final GamaColor DEFAULT_BORDER_COLOR = GamaColor.get(Color.BLACK);

	/** The size. */
	public final Attribute<GamaPoint> size;

	/** The depth. */
	public final Attribute<Double> depth;

	/** The rotation. */
	public final Attribute<AxisAngle> rotation;

	/** The location. */
	public final Attribute<GamaPoint> location;

	/** The anchor. */
	public final Attribute<GamaPoint> anchor;

	/** The empty. */
	public final Attribute<Boolean> empty;

	/** The color. */
	public final Attribute<GamaColor> border, color;

	/** The font. */
	public final Attribute<GamaFont> font;

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
	public DrawingData(final DrawStatement symbol) {
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
	private GamaPoint castSize(final IScope scope, final IExpression exp) throws GamaRuntimeException {
		if (exp.getGamlType().isNumber()) {
			final double val = Cast.asFloat(scope, exp.value(scope));
			// We do not consider the z ordinate -- see Issue #1539
			return new GamaPoint(val, val, 0);
		}
		return (GamaPoint) exp.value(scope);
	}

	/**
	 * Const cast size.
	 *
	 * @param exp
	 *            the exp
	 * @return the gama point
	 */
	private GamaPoint constCastSize(final IExpression exp) {
		if (exp.getGamlType().isNumber()) {
			final double val = Cast.asFloat(null, exp.getConstValue());
			// We do not consider the z ordinate -- see Issue #1539
			return new GamaPoint(val, val, 0);
		}
		return (GamaPoint) exp.getConstValue();
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
			final GamaPair currentRotation = Cast.asPair(scope, exp.value(scope), true);
			return new AxisAngle(Cast.asPoint(scope, currentRotation.value), Cast.asFloat(scope, currentRotation.key));
		}
		return new AxisAngle(Rotation3D.PLUS_K, Cast.asFloat(scope, exp.value(scope)));
	}

	/**
	 * Const cast rotation.
	 *
	 * @param exp
	 *            the exp
	 * @return the axis angle
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	private AxisAngle constCastRotation(final IExpression exp) throws GamaRuntimeException {
		if (exp.getGamlType().getGamlType() == Types.PAIR) {
			final GamaPair currentRotation = Cast.asPair(null, exp.getConstValue(), true);
			return new AxisAngle(Cast.asPoint(null, currentRotation.value), Cast.asFloat(null, currentRotation.key));
		}
		return new AxisAngle(Rotation3D.PLUS_K, Cast.asFloat(null, exp.getConstValue()));
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
	private GamaPoint castAnchor(final IScope scope, final IExpression exp) throws GamaRuntimeException {
		final GamaPoint p = Cast.asPoint(scope, exp.value(scope));
		p.x = Math.min(1d, Math.max(p.x, 0d));
		p.y = Math.min(1d, Math.max(p.y, 0d));
		return p;
	}

	/**
	 * Const cast anchor.
	 *
	 * @param exp
	 *            the exp
	 * @return the gama point
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	private GamaPoint constCastAnchor(final IExpression exp) throws GamaRuntimeException {
		final GamaPoint p = Cast.asPoint(null, exp.getConstValue());
		p.x = Math.min(1d, Math.max(p.x, 0d));
		p.y = Math.min(1d, Math.max(p.y, 0d));
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
			return GamaListType.staticCast(scope, exp.value(scope), Types.STRING, false);
		return GamaListFactory.wrap(Types.NO_TYPE, exp.value(scope));
	}

	/**
	 * Const cast texture.
	 *
	 * @param exp
	 *            the exp
	 * @return the i list
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	private IList constCastTexture(final IExpression exp) throws GamaRuntimeException {
		if (exp.getGamlType().getGamlType() == Types.LIST)
			return GamaListType.staticCast(null, exp.getConstValue(), Types.STRING, false);
		return GamaListFactory.wrap(Types.NO_TYPE, exp.getConstValue());
	}

	/**
	 * Const cast color.
	 *
	 * @param e
	 *            the e
	 * @return the gama color
	 */
	private GamaColor constCastColor(final IExpression e) {
		return switch (e.getGamlType().id()) {
			case IType.COLOR -> Cast.asColor(null, e.getConstValue());
			default -> null;
		};
	}

	/**
	 * Const cast border.
	 *
	 * @param exp
	 *            the exp
	 * @return the gama color
	 */
	private GamaColor constCastBorder(final IExpression exp) {
		if (exp.getGamlType() != Types.BOOL) return (GamaColor) exp.getConstValue();
		final boolean hasBorder = (boolean) exp.getConstValue();
		if (hasBorder) return DEFAULT_BORDER_COLOR;
		return null;
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
	private GamaColor castColor(final IScope scope, final IExpression exp) {
		return switch (exp.getGamlType().id()) {
			case IType.COLOR -> Cast.asColor(scope, exp.value(scope));
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
	private GamaColor castBorder(final IScope scope, final IExpression exp) {
		if (exp.getGamlType() != Types.BOOL) return (GamaColor) exp.value(scope);
		final boolean hasBorder = Cast.asBool(scope, exp.value(scope));
		if (hasBorder) return DEFAULT_BORDER_COLOR;
		return null;
	}

	/**
	 * Gets the location.
	 *
	 * @return the location
	 */
	public GamaPoint getLocation() { return location.get() == null ? null : location.get(); }

	/**
	 * Gets the anchor.
	 *
	 * @return the anchor
	 */
	public GamaPoint getAnchor() { return anchor.get() == null ? null : anchor.get(); }

}
