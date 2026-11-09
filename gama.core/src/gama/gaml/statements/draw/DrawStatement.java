/*******************************************************************************************************
 *
 * DrawStatement.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements.draw;

import static gama.annotations.constants.IKeyword.ANCHOR;
import static gama.annotations.constants.IKeyword.ASPECT;
import static gama.annotations.constants.IKeyword.AT;
import static gama.annotations.constants.IKeyword.BORDER;
import static gama.annotations.constants.IKeyword.COLOR;
import static gama.annotations.constants.IKeyword.DEPTH;
import static gama.annotations.constants.IKeyword.DRAW;
import static gama.annotations.constants.IKeyword.FONT;
import static gama.annotations.constants.IKeyword.PERSPECTIVE;
import static gama.annotations.constants.IKeyword.ROTATE;
import static gama.annotations.constants.IKeyword.SIZE;
import static gama.annotations.constants.IKeyword.TEXTURE;

import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Map.Entry;

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
import gama.api.additions.delegates.IDrawDelegate;
import gama.api.additions.registries.GamaAdditionRegistry;
import gama.api.annotations.validator;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IDescriptionValidator;
import gama.api.compilation.descriptions.IStatementDescription;
import gama.api.constants.IGamlIssue;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.GAML;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IExpressionDescription;
import gama.api.gaml.statements.AbstractStatementSequence;
import gama.api.gaml.statements.IStatement;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.color.AdaptiveColor;
import gama.api.ui.displays.DrawingData;
import gama.api.ui.displays.IGraphics;
import gama.api.ui.displays.IGraphicsScope;
import gama.api.ui.displays.PreparationDiagnostics;
import gama.dev.DEBUG;
import gama.gaml.statements.draw.DrawStatement.DrawValidator;

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
						doc = @doc ("The color used to display the object. Text accepts #adaptive to choose black or white from the rendered pixels beneath it; this is opt-in, not the default, and adds a scene readback in OpenGL. In case of images, will try to colorize it. You can also pass a list of colors: each color will be matched to its corresponding vertex.")),
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
						doc = @doc ("""
								(only if the display type is opengl and only for text drawing) controls the accuracy with which curves are rendered in glyphs. Between 0 and 1, the default is 0.1. \
								Smaller values will output much more faithful curves but can be considerably slower, \
								so it is better if they concern text that does not change and can be drawn inside layers marked as 'refresh: false'""")),
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
								value = "Whether the object should be lighted or not (only applicable in OpenGL displays). Defaults to false for lines without depth, including their arrowheads, and true for other geometries. An explicit value overrides this default.")),
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
								value = """
										The width in screen pixels (not world units) for line-based geometries and for the borders or wireframes of arbitrary shapes in Java2D and OpenGL displays. It does not change with world zoom. In OpenGL, this value is passed directly to the underlying line width setting, \
										so the effective rendering depends on the capabilities of the current GPU and driver. \
										The default value is set by the preference found in Displays>OpenGL Rendering Properties""")), },

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
public class DrawStatement extends AbstractStatementSequence implements IStatement.Draw {
	/** The Constant INTERNAL_DELEGATE. */
	private static final String INTERNAL_DELEGATE = "delegate";

	/**
	 * The Class DrawValidator.
	 */
	public static class DrawValidator implements IDescriptionValidator<IStatementDescription> {

		/**
		 * Method validate()
		 *
		 * @see gama.api.compilation.descriptions.IDescriptionValidator#validate(gama.api.compilation.descriptions.IDescription)
		 */
		@Override
		public void validate(final IStatementDescription description) {

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
				for (Entry<IType, IDrawDelegate> entry : GamaAdditionRegistry.getDrawDelegates().entrySet()) {
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
				description.setFacet(INTERNAL_DELEGATE,
						GAML.getExpressionFactory().createConst(executer, Types.NO_TYPE));
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

	/** The delegate. */
	private IDrawDelegate delegate;

	private final IExpression[] drawingItems;

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
		drawingItems = new IExpression[] { getFacet(IKeyword.GEOMETRY), getFacet(BEGIN_ARROW), getFacet(END_ARROW) };
	}

	// A draw cannot declare locals; its enclosing aspect already provides the execution frame.
	@Override
	public void enterScope(final IScope scope) {
		if (getClass() != DrawStatement.class) { super.enterScope(scope); }
	}

	@Override
	public void leaveScope(final IScope scope) {
		if (getClass() != DrawStatement.class) { super.leaveScope(scope); }
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
			if (delegate == null) {
				final IDrawDelegate candidate = (IDrawDelegate) getFacet(INTERNAL_DELEGATE).value(scope);
				delegate = candidate.getClass() == ShapeDrawer.class
						? ((ShapeDrawer) candidate).prepare(drawingItems[0], drawingItems[1] != null
								|| drawingItems[2] != null, hasFacet(TEXTURE)) : candidate;
			}
			final DrawingData d = g.getDrawingData(this);
			final PreparationDiagnostics diagnostics = g.getPreparationDiagnostics();
			final long facetsStart = diagnostics == null ? 0 : diagnostics.startTimer();
			d.refresh(scope);
			if (diagnostics != null) { diagnostics.facetsFinished(facetsStart); }
			if (d.color.get() == AdaptiveColor.INSTANCE && !(delegate instanceof TextDrawer)) {
				throw GamaRuntimeException.error("#adaptive is only supported when draw renders text", scope);
			}
			if (d.border.get() == AdaptiveColor.INSTANCE) {
				throw GamaRuntimeException.error("#adaptive is a text foreground, not a border color", scope);
			}
			final long drawStart = diagnostics == null ? 0 : diagnostics.startTimer();
			final Rectangle2D result = delegate.executeOn(scope, d, drawingItems);
			if (diagnostics != null) { diagnostics.drawFinished(drawStart); }
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

}