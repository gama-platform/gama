/*******************************************************************************************************
 *
 * GamaColorType.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.types;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.type;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.ISymbolKind;
import gama.core.common.interfaces.IKeyword;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaColor;
import gama.core.util.GamaColorFactory;
import gama.core.util.IContainer;
import gama.core.util.map.IMap;
import gama.gaml.operators.Cast;

/**
 * Written by drogoul Modified on 1 ao�t 2010
 *
 * @todo Description
 *
 */
@type (
		name = IKeyword.RGB,
		id = IType.COLOR,
		wraps = { GamaColor.class, Color.class },
		kind = ISymbolKind.Variable.REGULAR,
		concept = { IConcept.TYPE, IConcept.COLOR },
		doc = @doc ("The type rgb represents colors in GAML, with their three red, green, blue components and, optionally, a fourth alpha component "))
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaColorType extends GamaType<GamaColor> {

	@Override
	@doc ("Transforms the parameter into a rgb color. A second parameter can be used to express the transparency of the color, either an int (between 0 and 255) or a float (between 0 and 1)")
	public GamaColor cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		return staticCast(scope, obj, param, copy);
	}

	/**
	 * Static cast.
	 *
	 * @param scope
	 *            the scope
	 * @param obj
	 *            the obj
	 * @param param
	 *            the param
	 * @param copy
	 *            the copy
	 * @return the gama color
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public static GamaColor staticCast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		// param can contain the alpha value
		switch (obj) {
			case null -> {
				return null;
			}
			case GamaColor col -> {
				if (param instanceof Integer a) return GamaColorFactory.get(col.getRed(), col.getGreen(), col.getBlue(), a);
				if (param instanceof Double a)
					return GamaColorFactory.getWithDoubleAlpha(col.getRed(), col.getGreen(), col.getBlue(), a);
				return (GamaColor) obj;
			}
			case List l -> {
				final int size = l.size();
				return switch (size) {
					case 0 -> GamaColorFactory.BLACK;
					case 1, 2 -> staticCast(scope, ((List) obj).get(0), param, copy);
					case 3 -> GamaColorFactory.get(Cast.asInt(scope, l.get(0)), Cast.asInt(scope, l.get(1)),
							Cast.asInt(scope, l.get(2)), 255);
					default -> GamaColorFactory.get(Cast.asInt(scope, l.get(0)), Cast.asInt(scope, l.get(1)),
							Cast.asInt(scope, l.get(2)), Cast.asInt(scope, l.get(3)));
				};
			}
			case Map m -> {
				return GamaColorFactory.get(Cast.asInt(scope, m.get("red")),
						Cast.asInt(scope, m.get("green")), Cast.asInt(scope, m.get("blue")), Cast.asInt(scope, m.get("alpha")));
			}
			default -> {
			}
		}
		if (obj instanceof IContainer)
			return staticCast(scope, ((IContainer) obj).listValue(scope, Types.NO_TYPE, false), param, copy);
		if (obj instanceof String) {
			final String s = ((String) obj).toLowerCase();
			GamaColor c = GamaColorFactory.colors.get(s);
			if (c == null) {
				try {
					c = GamaColorFactory.get(Color.decode(s));
				} catch (final NumberFormatException e) {
					c = null;
					if (s != null && s.contains("rgb")) {
						String sClean = s.replace(" ", "").replace("rgb", "").replace("(", "").replace(")", "");
						String[] sval = sClean.split(",");
						if (sval.length >= 3) {
							Integer r = Integer.valueOf(sval[0]);
							Integer g = Integer.valueOf(sval[1]);
							Integer b = Integer.valueOf(sval[2]);
							Integer alpha = sval.length == 4 ? Integer.valueOf(sval[3]) : null;
							if (r != null && b != null && g != null) {
								c = GamaColorFactory.get(r, g, b, alpha == null ? 255 : alpha);
							}
						}
					}
					if (c == null) throw GamaRuntimeException.error("'" + s + "' is not a valid color name", scope);
				}
				GamaColorFactory.colors.put(s, c);
			}
			switch (param) {
				case null -> {
					return c;
				}
				case Integer i -> {
					return GamaColorFactory.get(c, i);
				}
				case Double d -> {
					return GamaColorFactory.get(c, d);
				}
				default -> {
				}
			}
		}
		if (obj instanceof Boolean cond)
			return cond ? GamaColorFactory.get(Color.black) : GamaColorFactory.get(Color.white);
		final int i = Cast.asInt(scope, obj);
		if (param instanceof Integer in) return GamaColorFactory.create(i, in);
		if (param instanceof Double d) return GamaColorFactory.create(i, Double.valueOf(d * 255).intValue());
		return GamaColorFactory.get(i);
	}

	@Override
	public GamaColor getDefault() {
		return null; // new GamaColor(Color.black);
	}

	@Override
	public IType getContentType() { return Types.get(INT); }

	@Override
	public IType getKeyType() { return Types.get(INT); }

	@Override
	public boolean canCastToConst() {
		return true;
	}

	@Override
	public boolean isCompoundType() { return true; }

	@Override
	public GamaColor deserializeFromJson(final IScope scope, final IMap<String, Object> map2) {
		return cast(scope, map2, null, false);
	}

}
