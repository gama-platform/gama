/*******************************************************************************************************
 *
 * ChartDataStatement.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers.charts;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.facet;
import gama.annotations.precompiler.GamlAnnotations.facets;
import gama.annotations.precompiler.GamlAnnotations.inside;
import gama.annotations.precompiler.GamlAnnotations.symbol;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.ISymbolKind;
import gama.core.common.interfaces.IKeyword;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.descriptions.IDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.operators.Cast;
import gama.gaml.statements.AbstractStatement;
import gama.gaml.types.IType;

/**
 * The Class ChartDataStatement.
 */
@symbol (
		name = IKeyword.DATA,
		kind = ISymbolKind.SINGLE_STATEMENT,
		with_sequence = false,
		concept = { IConcept.CHART })
@doc ("This statement allows to describe the values that will be displayed on the chart.")
@inside (
		symbols = IKeyword.CHART,
		kinds = ISymbolKind.SEQUENCE_STATEMENT)
@facets (
		value = { @facet (
				name = IKeyword.VALUE,
				type = { IType.FLOAT, IType.POINT, IType.LIST },
				optional = false,
				doc = @doc ("The value to output on the chart")),
				// @facet(name = IKeyword.NAME, type = IType.ID, optional = true),
				@facet (
						name = IKeyword.LEGEND,
						type = IType.STRING,
						optional = false,
						doc = @doc ("The legend of the chart")),
				@facet (
						name = ChartDataStatement.USE_SECOND_Y_AXIS,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("Use second y axis for this serie")),
				@facet (
						name = ChartDataStatement.YERR_VALUES,
						type = { IType.FLOAT, IType.LIST },
						optional = true,
						doc = @doc ("the Y Error bar values to display. Has to be a List. Each element can be a number or a list with two values (low and high value)")),
				@facet (
						name = ChartDataStatement.XERR_VALUES,
						type = { IType.FLOAT, IType.LIST },
						optional = true,
						doc = @doc ("the X Error bar values to display. Has to be a List. Each element can be a number or a list with two values (low and high value)")),
				@facet (
						name = ChartDataStatement.YMINMAX_VALUES,
						type = IType.LIST,
						optional = true,
						doc = @doc ("the Y MinMax bar values to display (BW charts). Has to be a List. Each element can be a number or a list with two values (low and high value)")),
				@facet (
						name = ChartDataStatement.MARKERSIZE,
						type = IType.FLOAT,
						optional = true,
						doc = @doc ("Size in pixels of the marker")),
				@facet (
						name = IKeyword.COLOR,
						type = { IType.COLOR, IType.LIST },
						optional = true,
						doc = @doc ("color of the serie, for heatmap can be a list to specify [minColor,maxColor] or [minColor,medColor,maxColor]")),
				@facet (
						name = ChartDataStatement.CUMUL_VALUES,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("Force to replace values at each step (false) or accumulate with previous steps (true)")),
				@facet (
						name = ChartDataStatement.LINE_VISIBLE,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("Whether lines are visible or not")),
				@facet (
						name = ChartDataStatement.MARKER,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("marker visible or not")),
				@facet (
						name = ChartDataStatement.MARKERSHAPE,
						type = IType.ID,
						values = { ChartDataStatement.MARKER_EMPTY, ChartDataStatement.MARKER_SQUARE,
								ChartDataStatement.MARKER_CIRCLE, ChartDataStatement.MARKER_UP_TRIANGLE,
								ChartDataStatement.MARKER_DIAMOND, ChartDataStatement.MARKER_HOR_RECTANGLE,
								ChartDataStatement.MARKER_DOWN_TRIANGLE, ChartDataStatement.MARKER_HOR_ELLIPSE,
								ChartDataStatement.MARKER_RIGHT_TRIANGLE, ChartDataStatement.MARKER_VERT_RECTANGLE,
								ChartDataStatement.MARKER_LEFT_TRIANGLE },
						optional = true,
						doc = @doc ("Shape of the marker")),
				@facet (
						name = ChartDataStatement.FILL,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("Marker filled (true) or not (false)")),
				@facet (
						name = ChartDataStatement.THICKNESS,
						type = IType.FLOAT,
						optional = true,
						doc = @doc ("The thickness of the lines to draw")),
				@facet (
						name = IKeyword.STYLE,
						type = IType.ID,
						values = { IKeyword.LINE, /* IKeyword.WHISKER, */ IKeyword.AREA, IKeyword.BAR, IKeyword.DOT,
								IKeyword.STEP, IKeyword.SPLINE, IKeyword.STACK, IKeyword.THREE_D, IKeyword.RING,
								IKeyword.EXPLODED },
						optional = true,
						doc = @doc ("Style for the serie (if not the default one sepecified on chart statement)")) },
		omissible = IKeyword.LEGEND)
public class ChartDataStatement extends AbstractStatement {

	/** The Constant MARKER. */
	public static final String MARKER = "marker";

	/** The Constant USE_SECOND_Y_AXIS. */
	public static final String USE_SECOND_Y_AXIS = "use_second_y_axis";

	/** The Constant MARKERSHAPE. */
	public static final String MARKERSHAPE = "marker_shape";

	/** The Constant MARKERSIZE. */
	public static final String MARKERSIZE = "marker_size";

	/** The Constant FILL. */
	public static final String FILL = "fill";

	/** The Constant LINE_VISIBLE. */
	public static final String LINE_VISIBLE = "line_visible";

	/** The Constant CUMUL_VALUES. */
	public static final String CUMUL_VALUES = "accumulate_values";

	/** The Constant XERR_VALUES. */
	public static final String XERR_VALUES = "x_err_values";

	/** The Constant YERR_VALUES. */
	public static final String YERR_VALUES = "y_err_values";

	/** The Constant YMINMAX_VALUES. */
	public static final String YMINMAX_VALUES = "y_minmax_values";

	/** The Constant MARKER_EMPTY. */
	public static final String MARKER_EMPTY = "marker_empty";

	/** The Constant MARKER_SQUARE. */
	public static final String MARKER_SQUARE = "marker_square";

	/** The Constant MARKER_CIRCLE. */
	public static final String MARKER_CIRCLE = "marker_circle";

	/** The Constant MARKER_UP_TRIANGLE. */
	public static final String MARKER_UP_TRIANGLE = "marker_up_triangle";

	/** The Constant MARKER_DIAMOND. */
	public static final String MARKER_DIAMOND = "marker_diamond";

	/** The Constant MARKER_HOR_RECTANGLE. */
	public static final String MARKER_HOR_RECTANGLE = "marker_hor_rectangle";

	/** The Constant MARKER_DOWN_TRIANGLE. */
	public static final String MARKER_DOWN_TRIANGLE = "marker_down_triangle";

	/** The Constant MARKER_HOR_ELLIPSE. */
	public static final String MARKER_HOR_ELLIPSE = "marker_hor_ellipse";

	/** The Constant MARKER_RIGHT_TRIANGLE. */
	public static final String MARKER_RIGHT_TRIANGLE = "marker_right_triangle";

	/** The Constant MARKER_VERT_RECTANGLE. */
	public static final String MARKER_VERT_RECTANGLE = "marker_vert_rectangle";

	/** The Constant MARKER_LEFT_TRIANGLE. */
	public static final String MARKER_LEFT_TRIANGLE = "marker_left_triangle";

	/** The Constant THICKNESS. */
	public static final String THICKNESS = "thickness";

	/**
	 * Instantiates a new chart data statement.
	 *
	 * @param desc
	 *            the desc
	 */
	public ChartDataStatement(final IDescription desc) {
		super(desc);
	}

	/**
	 * @throws GamaRuntimeException
	 * @param scope
	 */
	public ChartDataSourceUnique createDataSource(final IScope scope, final ChartDataSet graphdataset)
			throws GamaRuntimeException {

		final ChartDataSourceUnique data = new ChartDataSourceUnique();

		// final IExpression string1 = getFacet(IKeyword.TYPE);

		data.setDataset(scope, graphdataset);

		String stval = getLiteral(IKeyword.STYLE);
		if (stval != null) { data.setStyle(scope, stval); }

		boolean boolval = getFacetValue(scope, MARKER, true);
		data.setMarkerBool(scope, boolval);

		boolval = getFacetValue(scope, ChartDataStatement.LINE_VISIBLE, true);
		data.setShowLine(scope, boolval);
		boolval = getFacetValue(scope, ChartDataStatement.FILL, true);
		data.setFillMarker(scope, boolval);
		boolval = getFacetValue(scope, ChartDataStatement.USE_SECOND_Y_AXIS, false);
		data.setUseSecondYAxis(scope, boolval);

		stval = Cast.asString(scope, getFacetValue(scope, IKeyword.LEGEND));

		data.setLegend(scope, stval);

		IExpression expval = getFacet(IKeyword.VALUE).resolveAgainst(scope);
		data.setValueExp(scope, expval);

		expval = getFacet(ChartDataStatement.YERR_VALUES);
		if (expval != null) {
			expval = expval.resolveAgainst(scope);
			data.setYErrValueExp(scope, expval);

		}

		expval = getFacet(ChartDataStatement.XERR_VALUES);
		if (expval != null) {
			expval = expval.resolveAgainst(scope);
			data.setXErrValueExp(scope, expval);

		}

		expval = getFacet(ChartDataStatement.YMINMAX_VALUES);
		if (expval != null) {
			expval = expval.resolveAgainst(scope);
			data.setYMinMaxValueExp(scope, expval);

		}

		expval = getFacet(IKeyword.COLOR);
		if (expval != null) {
			expval = expval.resolveAgainst(scope);
			data.setColorExp(scope, expval);

		}

		expval = getFacet(ChartDataStatement.THICKNESS);
		if (expval != null) {
			expval = expval.resolveAgainst(scope);
			data.setLineThickness(expval);
		}
		final Object forcecumul = getFacetValue(scope, ChartDataStatement.CUMUL_VALUES, null);
		if (forcecumul != null) {
			data.setCumulative(scope, Cast.asBool(scope, forcecumul));
			data.setForceCumulative(scope, true);
		}

		data.createInitialSeries(scope);

		expval = getFacet(ChartDataStatement.MARKERSIZE);
		if (expval != null) {
			data.setUseSize(true);
			expval = expval.resolveAgainst(scope);
			data.setMarkerSize(scope, expval);

		}

		stval = getFacetValue(scope, MARKERSHAPE, null);
		data.setMarkerShape(scope, stval);

		// TODO
		/*
		 * stval = getFacetValue(scope, IKeyword.COLOR, "black"); data.sourceParameters.put(IKeyword.COLOR,stval);
		 *
		 * boolval = getFacetValue(scope, MARKER, true); data.sourceParameters.put(MARKER,boolval);
		 *
		 * boolval = getFacetValue(scope, LINE_VISIBLE, true); data.sourceParameters.put(LINE_VISIBLE,boolval);
		 *
		 * boolval = getFacetValue(scope, FILL, true); data.sourceParameters.put(FILL,boolval);
		 *
		 * stval = getFacetValue(scope, MARKERSHAPE, null); data.sourceParameters.put(MARKERSHAPE,stval);
		 *
		 * stval = getFacetValue(scope, MARKERSHAPE, null); data.sourceParameters.put(MARKERSHAPE,stval);
		 */

		return data;
	}

	/**
	 * Data statements rely on the fact that a variable called "chart_datas" is available in the scope. If not, it will
	 * not do anything. This variable is normally created by the ChartLayerStatement.
	 *
	 * @see gama.gaml.statements.AbstractStatement#privateExecuteIn(gama.core.runtime.IScope)
	 */

	@Override
	protected Object privateExecuteIn(final IScope scope) {
		final ChartDataSet graphdataset = (ChartDataSet) scope.getVarValue(ChartLayerStatement.CHARTDATASET);
		final ChartDataSourceUnique data = createDataSource(scope, graphdataset);
		graphdataset.addDataSource(data);
		return data;
	}

}