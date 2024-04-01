/*******************************************************************************************************
 *
 * DrawStatement.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements.draw;

import static gama.core.common.interfaces.IKeyword.ANCHOR;
import static gama.core.common.interfaces.IKeyword.ASPECT;
import static gama.core.common.interfaces.IKeyword.AT;
import static gama.core.common.interfaces.IKeyword.BORDER;
import static gama.core.common.interfaces.IKeyword.COLOR;
import static gama.core.common.interfaces.IKeyword.DEPTH;
import static gama.core.common.interfaces.IKeyword.DRAW;
import static gama.core.common.interfaces.IKeyword.FONT;
import static gama.core.common.interfaces.IKeyword.PERSPECTIVE;
import static gama.core.common.interfaces.IKeyword.ROTATE;
import static gama.core.common.interfaces.IKeyword.SIZE;
import static gama.core.common.interfaces.IKeyword.TEXTURE;

import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.facet;
import gama.annotations.precompiler.GamlAnnotations.facets;
import gama.annotations.precompiler.GamlAnnotations.inside;
import gama.annotations.precompiler.GamlAnnotations.symbol;
import gama.annotations.precompiler.GamlAnnotations.usage;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.ISymbolKind;
import gama.core.common.interfaces.IDrawDelegate;
import gama.core.common.interfaces.IGraphics;
import gama.core.common.interfaces.IKeyword;
import gama.core.runtime.IScope;
import gama.core.runtime.IScope.IGraphicsScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.dev.DEBUG;
import gama.gaml.compilation.GAML;
import gama.gaml.compilation.IDescriptionValidator;
import gama.gaml.compilation.annotations.validator;
import gama.gaml.descriptions.IDescription;
import gama.gaml.descriptions.IExpressionDescription;
import gama.gaml.descriptions.StatementDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.interfaces.IGamlIssue;
import gama.gaml.statements.AbstractStatementSequence;
import gama.gaml.statements.draw.DrawStatement.DrawValidator;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

// A command that is used to draw shapes, figures, text on the display

/**
 * The Class DrawStatement.
 */
@symbol (
		name = DRAW,
		kind = ISymbolKind.SINGLE_STATEMENT,
		with_sequence = false,
		concept = { IConcept.DISPLAY })
@facets (
		value = {
				// Allows to pass any arbitrary geometry to the drawing command
				@facet (
						name = IKeyword.GEOMETRY,
						type = IType.NONE,
						optional = true,
						doc = @doc ("any type of data (it can be geometry, image, text)")),
				// AD 18/01/13: geometry is now accepting any type of data
				@facet (
						name = TEXTURE,
						type = { IType.NONE },
						optional = true,
						doc = @doc ("the texture(s) that should be applied to the geometry. Either a path to a file or a list of paths")),
				@facet (
						name = IKeyword.WIREFRAME,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("a condition specifying whether to draw the geometry in wireframe or not")),
				@facet (
						name = BORDER,
						type = { IType.COLOR, IType.BOOL },
						optional = true,
						doc = @doc ("if used with a color, represents the color of the geometry border. If set to false, expresses that no border should be drawn. If not set, the borders will be drawn using the color of the geometry.")),
				@facet (
						name = AT,
						type = IType.POINT,
						optional = true,
						doc = @doc ("location where the shape/text/icon is drawn")),
				@facet (
						name = ANCHOR,
						type = IType.POINT,
						optional = true,
						doc = @doc ("Only used when perspective: true in OpenGL. The anchor point of the location with respect to the envelope of the text to draw, can take one of the following values: #center, #top_left, #left_center, #bottom_left, #bottom_center, #bottom_right, #right_center, #top_right, #top_center; or any point between {0,0} (#bottom_left) and {1,1} (#top_right)")),
				@facet (
						name = SIZE,
						type = { IType.FLOAT, IType.POINT },
						optional = true,
						doc = @doc ("Size of the shape/icon/image to draw, expressed as a bounding box (width, height, depth; if expressed as a float, represents the box as a cube). Does not apply to texts: use a font with the required size instead")),
				@facet (
						name = COLOR,
						type = { IType.COLOR, IType.CONTAINER },
						optional = true,
						doc = @doc ("the color to use to display the object. In case of images, will try to colorize it. You can also pass a list of colors : in that case, each color will be matched to its corresponding vertex.")),
				@facet (
						name = ROTATE,
						type = { IType.FLOAT, IType.INT, IType.PAIR },
						index = IType.FLOAT,
						of = IType.POINT,
						optional = true,
						doc = @doc ("orientation of the shape/text/icon; can be either an int/float (angle) or a pair float::point (angle::rotation axis). The rotation axis, when expressed as an angle, is by defaut {0,0,1}")),
				@facet (
						name = FONT,
						type = { IType.FONT, IType.STRING },
						optional = true,
						doc = @doc ("the font used to draw the text, if any. Applying this facet to geometries or images has no effect. You can construct here your font with the operator \"font\". ex : font:font(\"Helvetica\", 20 , #plain)")),
				@facet (
						name = DEPTH,
						type = IType.FLOAT,
						optional = true,
						doc = @doc ("(only if the display type is opengl) Add an artificial depth to the geometry previously defined (a line becomes a plan, a circle becomes a cylinder, a square becomes a cube, a polygon becomes a polyhedron with height equal to the depth value). Note: This only works if the geometry is not a point ")),

				@facet (
						name = "precision",
						type = IType.FLOAT,
						optional = true,
						doc = @doc ("(only if the display type is opengl and only for text drawing) controls the accuracy with which curves are rendered in glyphs. Between 0 and 1, the default is 0.1. "
								+ "Smaller values will output much more faithful curves but can be considerably slower, "
								+ "so it is better if they concern text that does not change and can be drawn inside layers marked as 'refresh: false'")),
				@facet (
						name = DrawStatement.BEGIN_ARROW,
						type = { IType.INT, IType.FLOAT },
						optional = true,
						doc = @doc ("the size of the arrow, located at the beginning of the drawn geometry")),
				@facet (
						name = DrawStatement.END_ARROW,
						type = { IType.INT, IType.FLOAT },
						optional = true,
						doc = @doc ("the size of the arrow, located at the end of the drawn geometry")),
				@facet (
						name = IKeyword.LIGHTED,
						type = IType.BOOL,
						optional = true,
						doc = @doc (
								value = "Whether the object should be lighted or not (only applicable in the context of opengl displays)")),
				@facet (
						name = PERSPECTIVE,
						type = IType.BOOL,
						optional = true,
						doc = @doc (
								value = "Whether to render the text in perspective or facing the user. Default is in perspective.")),
				@facet (
						name = IKeyword.WIDTH,
						type = IType.FLOAT,
						optional = true,
						doc = @doc (
								value = "The line width to use for drawing this object. In OpenGL displays, this attribute is considered as optional and not implemented by all gaphic card vendors. "
										+ "The default value is set by the preference found in Displays>OpenGL Rendering Properties (which, when inspected, also provides the maximal possible value on the local graphics configuration)")), 
				},

		omissible = IKeyword.GEOMETRY)
@inside (
		symbols = { ASPECT },
		kinds = { ISymbolKind.SEQUENCE_STATEMENT, ISymbolKind.LAYER })
@doc (
		value = "`" + DRAW
				+ "` is used in an aspect block to express how agents of the species will be drawn. It is evaluated each time the agent has to be drawn. It can also be used in the graphics block.",
		usages = { @usage (
				value = "Any kind of geometry as any location can be drawn when displaying an agent (independently of his shape)",
				examples = { @example (
						value = "aspect geometryAspect {",
						isExecutable = false),
						@example (
								value = "	draw circle(1.0) empty: !hasFood color: #orange ;",
								isExecutable = false),
						@example (
								value = "}",
								isExecutable = false) }),
				@usage (
						value = "Image or text can also be drawn",
						examples = { @example (
								value = "aspect arrowAspect {",
								isExecutable = false),
								@example (
										value = "	draw \"Current state= \"+state at: location + {-3,1.5} color: #white font: font('Default', 12, #bold) ;",
										isExecutable = false),
								@example (
										value = "	draw file(ant_shape_full) rotate: heading at: location size: 5",
										isExecutable = false),
								@example (
										value = "}",
										isExecutable = false) }),
				@usage (
						value = "Arrows can be drawn with any kind of geometry, using " + DrawStatement.BEGIN_ARROW
								+ " and " + DrawStatement.END_ARROW
								+ " facets, combined with the empty: facet to specify whether it is plain or empty",
						examples = { @example (
								value = "aspect arrowAspect {",
								isExecutable = false),
								@example (
										value = "	draw line([{20, 20}, {40, 40}]) color: #black begin_arrow:5;",
										isExecutable = false),
								@example (
										value = "	draw line([{10, 10},{20, 50}, {40, 70}]) color: #green end_arrow: 2 begin_arrow: 2 empty: true;",
										isExecutable = false),
								@example (
										value = "	draw square(10) at: {80,20} color: #purple begin_arrow: 2 empty: true;",
										isExecutable = false),
								@example (
										value = "}",
										isExecutable = false) }) })
@validator (DrawValidator.class)
public class DrawStatement extends AbstractStatementSequence {

	/**
	 * The Class DrawValidator.
	 */
	public static class DrawValidator implements IDescriptionValidator<StatementDescription> {

		/**
		 * Method validate()
		 *
		 * @see gama.gaml.compilation.IDescriptionValidator#validate(gama.gaml.descriptions.IDescription)
		 */
		@Override
		public void validate(final StatementDescription description) {

			final IExpressionDescription geom = description.getFacet(GEOMETRY);
			if (geom != null) {
				for (final String s : Arrays.asList(TEXT, SHAPE, IMAGE)) {
					final IExpressionDescription other = description.getFacet(s);
					if (other != null) {
						description.warning("'" + s + "' has no effect here", IGamlIssue.CONFLICTING_FACETS, s);
					}
				}
				final IExpression exp = geom.getExpression();
				final IType<?> type = exp == null ? Types.NO_TYPE : exp.getGamlType();
				if (exp == null || !canDraw(exp)) {
					description.error("'draw' cannot draw objects of type " + type, IGamlIssue.WRONG_TYPE, GEOMETRY);
					return;
				}
				if (type.equals(Types.STRING)) {
					final IExpressionDescription rot = description.getFacet(ROTATE);

					if (rot != null) {
						final IExpressionDescription per = description.getFacet(PERSPECTIVE);
						if (per != null && per.isConst() && per.equalsString(FALSE)) {
							description.warning("Rotations cannot be applied when perspective is false",
									IGamlIssue.CONFLICTING_FACETS, ROTATE);
						}
					}
				}
				IDrawDelegate executer = null;
				for (Entry<IType, IDrawDelegate> entry : DELEGATES.entrySet()) {
					if (entry.getKey().isAssignableFrom(type)) {
						executer = entry.getValue();
						break;
					}
				}
				if (executer == null) {
					description.error("'draw' cannot draw objects of type " + type, IGamlIssue.WRONG_TYPE, GEOMETRY);
					return;
				}
				executer.validate(description, exp);
				description.setFacet("delegate", GAML.getExpressionFactory().createConst(executer, Types.NO_TYPE));
			}

		}

		/**
		 * Can draw.
		 *
		 * @param exp
		 *            the exp
		 * @return true, if successful
		 */
		private boolean canDraw(final IExpression exp) {
			IType<?> type = exp.getGamlType();
			if (type.isDrawable()) return true;
			// In case we have a generic file operator, for instance
			type = type.typeIfCasting(exp);
			return type.isDrawable();
		}

	}

	/** The Constant END_ARROW. */
	public static final String END_ARROW = "end_arrow";

	/** The Constant BEGIN_ARROW. */
	public static final String BEGIN_ARROW = "begin_arrow";

	/** The Constant DELEGATES. */
	private static final Map<IType, IDrawDelegate> DELEGATES = new HashMap<>();

	/** The delegate. */
	private IDrawDelegate delegate;

	/**
	 * @param createExecutableExtension
	 */
	public static void addDelegate(final IDrawDelegate delegate) {
		final IType t = delegate.typeDrawn();
		if (t != null) { DELEGATES.put(t, delegate); }
	}

	/** The data. */
	private final WeakHashMap<IGraphics, DrawingData> data = new WeakHashMap<>();

	/**
	 * Instantiates a new draw statement.
	 *
	 * @param desc
	 *            the desc
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public DrawStatement(final IDescription desc) throws GamaRuntimeException {
		super(desc);

	}

	/**
	 * Private execute in.
	 *
	 * @param scope
	 *            the scope
	 * @return the rectangle 2 D
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@Override
	public Rectangle2D privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		if (scope.isGraphics()) return privateExecuteIn((IGraphicsScope) scope);
		return null;
	}

	/**
	 * Private execute in.
	 *
	 * @param scope
	 *            the scope
	 * @return the rectangle 2 D
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	private Rectangle2D privateExecuteIn(final IGraphicsScope scope) throws GamaRuntimeException {
		final IGraphics g = scope.getGraphics();
		if (scope.interrupted() || g == null || scope.getAgent() == null) return null;
		try {
			if (delegate == null) { delegate = (IDrawDelegate) getFacet("delegate").value(scope); }
			DrawingData d = data.get(g);
			if (d == null) {
				d = new DrawingData(this);
				data.put(g, d);
			}
			d.refresh(scope);
			final IExpression item = getFacet(IKeyword.GEOMETRY);
			final Rectangle2D result = delegate.executeOn(scope, d, item, getFacet(BEGIN_ARROW), getFacet(END_ARROW));
			if (result != null) { g.accumulateTemporaryEnvelope(result); }
			return result;
		} catch (final GamaRuntimeException e) {
			throw e;
		} catch (final Throwable e) {
			DEBUG.ERR("Error when drawing in a display : " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void dispose() {
		data.clear();
		super.dispose();
	}

}