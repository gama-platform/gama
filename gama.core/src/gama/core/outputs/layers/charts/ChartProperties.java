/*******************************************************************************************************
 *
 * ChartProperties.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers.charts;

import java.awt.Font;

import gama.annotations.constants.IKeyword;
import gama.api.runtime.scope.IScope;
import gama.api.types.color.GamaColorFactory;
import gama.api.types.color.IColor;
import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.IPoint;
import gama.api.ui.IOutput;
import gama.api.utils.prefs.GamaPreferences;
import gama.gaml.operators.Random;

/**
 * Encapsulates style, font, color, label, and axis configuration properties for GAMA charts. Initialized from
 * GamaPreferences.Displays user preference defaults.
 */
public class ChartProperties {

	/**
	 * The Class FontSpec.
	 */
	public static class FontSpec {

		/** The face. */
		private String face;

		/** The size. */
		private int size;

		/** The style. */
		private int style;

		/**
		 * Instantiates a new font spec.
		 *
		 * @param face
		 *            the face
		 * @param size
		 *            the size
		 * @param style
		 *            the style
		 */
		public FontSpec(final String face, final int size, final int style) {
			this.face = face;
			this.size = size;
			this.style = style;
		}

		/**
		 * Gets the font.
		 *
		 * @return the font
		 */
		public Font getFont() { return new Font(face, style, size); }

		/**
		 * Gets the face.
		 *
		 * @return the face
		 */
		public String getFace() { return face; }

		/**
		 * Sets the face.
		 *
		 * @param face
		 *            the new face
		 */
		public void setFace(final String face) {
			if (face != null) { this.face = face; }
		}

		/**
		 * Gets the size.
		 *
		 * @return the size
		 */
		public int getSize() { return size; }

		/**
		 * Sets the size.
		 *
		 * @param size
		 *            the new size
		 */
		public void setSize(final int size) { this.size = size; }

		/**
		 * Gets the style.
		 *
		 * @return the style
		 */
		public int getStyle() { return style; }

		/**
		 * Sets the style.
		 *
		 * @param style
		 *            the new style
		 */
		public void setStyle(final int style) { this.style = style; }
	}

	/**
	 * The Class AxisRange.
	 */
	public static class AxisRange {

		/** The interval. */
		private double interval;

		/** The min. */
		private double min;

		/** The max. */
		private double max;

		/** The use interval. */
		private boolean useInterval = false;

		/** The use min max. */
		private boolean useMinMax = false;

		/** The bound min. */
		private double boundMin;

		/** The bound max. */
		private double boundMax;

		/** The use min. */
		private boolean useMin = false;

		/** The use max. */
		private boolean useMax = false;

		/**
		 * Gets the interval.
		 *
		 * @return the interval
		 */
		public double getInterval() { return interval; }

		/**
		 * Sets the interval.
		 *
		 * @param val
		 *            the new interval
		 */
		public void setInterval(final double val) {
			this.useInterval = true;
			this.interval = val;
		}

		/**
		 * Checks if is use interval.
		 *
		 * @return true, if is use interval
		 */
		public boolean isUseInterval() { return useInterval; }

		/**
		 * Sets the min max.
		 *
		 * @param min
		 *            the min
		 * @param max
		 *            the max
		 */
		public void setMinMax(final double min, final double max) {
			this.useMinMax = true;
			this.min = min;
			this.max = max;
		}

		/**
		 * Checks if is use min max.
		 *
		 * @return true, if is use min max
		 */
		public boolean isUseMinMax() { return useMinMax; }

		/**
		 * Gets the min.
		 *
		 * @return the min
		 */
		public double getMin() { return min; }

		/**
		 * Gets the max.
		 *
		 * @return the max
		 */
		public double getMax() { return max; }

		/**
		 * Checks if is use min.
		 *
		 * @return true, if is use min
		 */
		public boolean isUseMin() { return useMin; }

		/**
		 * Sets the min.
		 *
		 * @param min
		 *            the new min
		 */
		public void setMin(final double min) {
			this.useMin = true;
			this.boundMin = min;
		}

		/**
		 * Gets the bound min.
		 *
		 * @return the bound min
		 */
		public double getBoundMin() { return boundMin; }

		/**
		 * Checks if is use max.
		 *
		 * @return true, if is use max
		 */
		public boolean isUseMax() { return useMax; }

		/**
		 * Sets the max.
		 *
		 * @param max
		 *            the new max
		 */
		public void setMax(final double max) {
			this.useMax = true;
			this.boundMax = max;
		}

		/**
		 * Gets the bound max.
		 *
		 * @return the bound max
		 */
		public double getBoundMax() { return boundMax; }
	}

	/**
	 * The Class AxisVisibility.
	 */
	public static class AxisVisibility {

		/** The tick value visible. */
		private boolean tickValueVisible = true;

		/** The tick line visible. */
		private boolean tickLineVisible = true;

		/** The logscale. */
		private boolean logscale = false;

		/**
		 * Checks if is tick value visible.
		 *
		 * @return true, if is tick value visible
		 */
		public boolean isTickValueVisible() { return tickValueVisible; }

		/**
		 * Sets the tick value visible.
		 *
		 * @param v
		 *            the new tick value visible
		 */
		public void setTickValueVisible(final boolean v) { this.tickValueVisible = v; }

		/**
		 * Checks if is tick line visible.
		 *
		 * @return true, if is tick line visible
		 */
		public boolean isTickLineVisible() { return tickLineVisible; }

		/**
		 * Sets the tick line visible.
		 *
		 * @param v
		 *            the new tick line visible
		 */
		public void setTickLineVisible(final boolean v) { this.tickLineVisible = v; }

		/**
		 * Checks if is logscale.
		 *
		 * @return true, if is logscale
		 */
		public boolean isLogscale() { return logscale; }

		/**
		 * Sets the logscale.
		 *
		 * @param v
		 *            the new logscale
		 */
		public void setLogscale(final boolean v) { this.logscale = v; }
	}

	/**
	 * The Class ChartLabels.
	 */
	public static class ChartLabels {

		/** The x label. */
		private String xLabel = null;

		/** The y label. */
		private String yLabel = null;

		/** The y 2 label. */
		private String y2Label = null;

		/**
		 * Gets the x label.
		 *
		 * @return the x label
		 */
		public String getXLabel() { return xLabel; }

		/**
		 * Sets the x label.
		 *
		 * @param label
		 *            the new x label
		 */
		public void setXLabel(final String label) { this.xLabel = label; }

		/**
		 * Gets the y label.
		 *
		 * @return the y label
		 */
		public String getYLabel() { return yLabel; }

		/**
		 * Sets the y label.
		 *
		 * @param label
		 *            the new y label
		 */
		public void setYLabel(final String label) { this.yLabel = label; }

		/**
		 * Gets the y 2 label.
		 *
		 * @return the y 2 label
		 */
		public String getY2Label() { return y2Label; }

		/**
		 * Sets the y 2 label.
		 *
		 * @param label
		 *            the new y 2 label
		 */
		public void setY2Label(final String label) { this.y2Label = label; }
	}

	/**
	 * The Class ColorPalette.
	 */
	public static class ColorPalette {

		/** The host display output. */
		private IOutput.Display hostDisplayOutput = null;

		/** The background color. */
		private IColor backgroundColor = null;

		/** The axes color. */
		private IColor axesColor = GamaPreferences.Displays.CHART_GRID_COLOR.getValue();

		/** The label background color. */
		private IColor labelBackgroundColor = null;

		/** The label text color. */
		private IColor labelTextColor = GamaPreferences.Displays.CHART_TEXT_COLOR.getValue();

		/** The text color. */
		private IColor textColor = GamaPreferences.Displays.CHART_TEXT_COLOR.getValue();

		/** The tick color. */
		private IColor tickColor = GamaColorFactory.get(100, 110, 120);

		/**
		 * Sets the host display output.
		 *
		 * @param host
		 *            the new host display output
		 */
		public void setHostDisplayOutput(final IOutput.Display host) { this.hostDisplayOutput = host; }

		/**
		 * Gets the background color.
		 *
		 * @return the background color
		 */
		public IColor getBackgroundColor() {
			if (backgroundColor != null) return backgroundColor;
			if (GamaPreferences.Displays.CHART_MATCH_DISPLAY_BACKGROUND.getValue()) {
				if (hostDisplayOutput != null && hostDisplayOutput.getData() != null) {
					IColor bg = hostDisplayOutput.getData().getBackgroundColor();
					if (bg != null) return bg;
				}
				return GamaPreferences.Displays.CORE_BACKGROUND.getValue();
			}
			return GamaPreferences.Displays.CHART_BACKGROUND_COLOR.getValue();
		}

		/**
		 * Sets the background color.
		 *
		 * @param color
		 *            the new background color
		 */
		public void setBackgroundColor(final IColor color) { this.backgroundColor = color; }

		/**
		 * Gets the axes color.
		 *
		 * @return the axes color
		 */
		public IColor getAxesColor() { return axesColor; }

		/**
		 * Sets the axes color.
		 *
		 * @param color
		 *            the new axes color
		 */
		public void setAxesColor(final IColor color) { this.axesColor = color; }

		/**
		 * Gets the label background color.
		 *
		 * @return the label background color
		 */
		public IColor getLabelBackgroundColor() { return labelBackgroundColor; }

		/**
		 * Sets the label background color.
		 *
		 * @param color
		 *            the new label background color
		 */
		public void setLabelBackgroundColor(final IColor color) { this.labelBackgroundColor = color; }

		/**
		 * Gets the label text color.
		 *
		 * @return the label text color
		 */
		public IColor getLabelTextColor() { return labelTextColor; }

		/**
		 * Sets the label text color.
		 *
		 * @param color
		 *            the new label text color
		 */
		public void setLabelTextColor(final IColor color) { this.labelTextColor = color; }

		/**
		 * Gets the text color.
		 *
		 * @return the text color
		 */
		public IColor getTextColor() { return textColor; }

		/**
		 * Sets the text color.
		 *
		 * @param color
		 *            the new text color
		 */
		public void setTextColor(final IColor color) { this.textColor = color; }

		/**
		 * Gets the tick color.
		 *
		 * @return the tick color
		 */
		public IColor getTickColor() { return tickColor; }

		/**
		 * Sets the tick color.
		 *
		 * @param color
		 *            the new tick color
		 */
		public void setTickColor(final IColor color) { this.tickColor = color; }
	}

	/**
	 * The Class LegendOptions.
	 */
	public static class LegendOptions {

		/** The series label position. */
		private String seriesLabelPosition = IKeyword.DEFAULT;

		/** The legend orientation. */
		private String legendOrientation = "default";

		/** The series label anchor. */
		private IPoint seriesLabelAnchor = GamaPointFactory.create(1, 1);

		/**
		 * Gets the series label position.
		 *
		 * @return the series label position
		 */
		public String getSeriesLabelPosition() { return seriesLabelPosition; }

		/**
		 * Sets the series label position.
		 *
		 * @param pos
		 *            the new series label position
		 */
		public void setSeriesLabelPosition(final String pos) { this.seriesLabelPosition = pos; }

		/**
		 * Gets the legend orientation.
		 *
		 * @return the legend orientation
		 */
		public String getLegendOrientation() { return legendOrientation; }

		/**
		 * Sets the legend orientation.
		 *
		 * @param orient
		 *            the new legend orientation
		 */
		public void setLegendOrientation(final String orient) { this.legendOrientation = orient; }

		/**
		 * Gets the series label anchor.
		 *
		 * @return the series label anchor
		 */
		public IPoint getSeriesLabelAnchor() { return seriesLabelAnchor; }

		/**
		 * Sets the series label anchor.
		 *
		 * @param anchor
		 *            the new series label anchor
		 */
		public void setSeriesLabelAnchor(final IPoint anchor) { this.seriesLabelAnchor = anchor; }
	}

	/**
	 * The Class TickUnits.
	 */
	public static class TickUnits {

		/** The x tick unit. */
		private double xTickUnit = -1;

		/** The y tick unit. */
		private double yTickUnit = -1;

		/** The y 2 tick unit. */
		private double y2TickUnit = -1;

		/**
		 * Gets the x tick unit.
		 *
		 * @return the x tick unit
		 */
		public double getXTickUnit() { return xTickUnit; }

		/**
		 * Sets the x tick unit.
		 *
		 * @param unit
		 *            the new x tick unit
		 */
		public void setXTickUnit(final double unit) { this.xTickUnit = unit; }

		/**
		 * Gets the y tick unit.
		 *
		 * @return the y tick unit
		 */
		public double getYTickUnit() { return yTickUnit; }

		/**
		 * Sets the y tick unit.
		 *
		 * @param unit
		 *            the new y tick unit
		 */
		public void setYTickUnit(final double unit) { this.yTickUnit = unit; }

		/**
		 * Gets the y 2 tick unit.
		 *
		 * @return the y 2 tick unit
		 */
		public double getY2TickUnit() { return y2TickUnit; }

		/**
		 * Sets the y 2 tick unit.
		 *
		 * @param unit
		 *            the new y 2 tick unit
		 */
		public void setY2TickUnit(final double unit) { this.y2TickUnit = unit; }
	}

	/**
	 * The Class DisplayOptions.
	 */
	public static class DisplayOptions {

		/** The reverse axes. */
		private boolean reverseAxes = false;

		/** The use second Y axis. */
		private boolean useSecondYAxis = false;

		/** The title visible. */
		private boolean titleVisible = true;

		/** The grid lines visible. */
		private boolean gridLinesVisible = GamaPreferences.Displays.CHART_GRID_LINES.getValue();

		/** The style. */
		private String style = IKeyword.DEFAULT;

		/** The gap. */
		private double gap = -1;

		/**
		 * Checks if is reverse axes.
		 *
		 * @return true, if is reverse axes
		 */
		public boolean isReverseAxes() { return reverseAxes; }

		/**
		 * Sets the reverse axes.
		 *
		 * @param reverse
		 *            the new reverse axes
		 */
		public void setReverseAxes(final boolean reverse) { this.reverseAxes = reverse; }

		/**
		 * Checks if is use second Y axis.
		 *
		 * @return true, if is use second Y axis
		 */
		public boolean isUseSecondYAxis() { return useSecondYAxis; }

		/**
		 * Sets the use second Y axis.
		 *
		 * @param useSecond
		 *            the new use second Y axis
		 */
		public void setUseSecondYAxis(final boolean useSecond) { this.useSecondYAxis = useSecond; }

		/**
		 * Checks if is title visible.
		 *
		 * @return true, if is title visible
		 */
		public boolean isTitleVisible() { return titleVisible; }

		/**
		 * Sets the title visible.
		 *
		 * @param visible
		 *            the new title visible
		 */
		public void setTitleVisible(final boolean visible) { this.titleVisible = visible; }

		/**
		 * Checks if is grid lines visible.
		 *
		 * @return true, if is grid lines visible
		 */
		public boolean isGridLinesVisible() { return gridLinesVisible; }

		/**
		 * Sets the grid lines visible.
		 *
		 * @param visible
		 *            the new grid lines visible
		 */
		public void setGridLinesVisible(final boolean visible) { this.gridLinesVisible = visible; }

		/**
		 * Gets the style.
		 *
		 * @return the style
		 */
		public String getStyle() { return style; }

		/**
		 * Sets the style.
		 *
		 * @param style
		 *            the new style
		 */
		public void setStyle(final String style) { this.style = style; }

		/**
		 * Gets the gap.
		 *
		 * @return the gap
		 */
		public double getGap() { return gap; }

		/**
		 * Sets the gap.
		 *
		 * @param gap
		 *            the new gap
		 */
		public void setGap(final double gap) { this.gap = gap; }
	}

	/**
	 * Returns default series color based on the selected ColorBrewer palette preference.
	 */
	public static IColor getDefaultSeriesColor(final IScope scope, final int index) {
		String palette = GamaPreferences.Displays.CHART_COLOR_PALETTE.getValue();
		int idx = Math.max(0, index);
		if (GamaPreferences.Displays.CHART_PALETTE_VIVID.equals(palette) && GamaPreferences.VIVID_COLORS.length > 0)
			return GamaPreferences.VIVID_COLORS[idx % GamaPreferences.VIVID_COLORS.length].get();
		if (GamaPreferences.Displays.CHART_PALETTE_TABLEAU.equals(palette) && GamaPreferences.TABLEAU_COLORS.length > 0)
			return GamaPreferences.TABLEAU_COLORS[idx % GamaPreferences.TABLEAU_COLORS.length].get();
		if (GamaPreferences.Displays.CHART_PALETTE_NEON.equals(palette) && GamaPreferences.NEON_COLORS.length > 0)
			return GamaPreferences.NEON_COLORS[idx % GamaPreferences.NEON_COLORS.length].get();
		if (GamaPreferences.Displays.CHART_PALETTE_PASTEL.equals(palette) && GamaPreferences.PASTEL_COLORS.length > 0)
			return GamaPreferences.PASTEL_COLORS[idx % GamaPreferences.PASTEL_COLORS.length].get();
		if (GamaPreferences.Displays.CHART_PALETTE_DIVERGING.equals(palette)
				&& GamaPreferences.DIVERGING_COLORS.length > 0)
			return GamaPreferences.DIVERGING_COLORS[idx % GamaPreferences.DIVERGING_COLORS.length].get();
		if (GamaPreferences.Displays.CHART_PALETTE_BASIC.equals(palette) && GamaPreferences.BASIC_COLORS.length > 0)
			return GamaPreferences.BASIC_COLORS[idx % GamaPreferences.BASIC_COLORS.length].get();
		if (GamaPreferences.Displays.CHART_PALETTE_PIVOT.equals(palette)) {
			IColor c = GamaPreferences.Displays.CHART_PIVOT_COLOR.getValue();
			if (c == null) { c = GamaColorFactory.get(31, 120, 180); }
			IColor[] ramp = { c.darker().darker().darker().darker(), c.darker().darker().darker(), c.darker().darker(),
					c.darker(), c, c.brighter(), c.brighter().brighter(), c.brighter().brighter().brighter(),
					c.brighter().brighter().brighter().brighter() };
			return ramp[idx % ramp.length];
		}
		if (GamaPreferences.Displays.CHART_PALETTE_RANDOM.equals(palette) && scope != null) return GamaColorFactory
				.createWithRGBA(Random.opRnd(scope, 255), Random.opRnd(scope, 255), Random.opRnd(scope, 255), 255);
		if (GamaPreferences.QUALITATIVE_COLORS.length > 0)
			return GamaPreferences.QUALITATIVE_COLORS[idx % GamaPreferences.QUALITATIVE_COLORS.length].get();
		return GamaColorFactory.get("blue");
	}

	/** The tick font spec. */
	// Component Objects initialized from GamaPreferences.Displays
	private final FontSpec tickFontSpec = new FontSpec(Font.SANS_SERIF, 9, Font.PLAIN);

	/** The label font spec. */
	private final FontSpec labelFontSpec = new FontSpec(GamaPreferences.Displays.CHART_LABEL_FONT.getValue(),
			GamaPreferences.Displays.CHART_LABEL_FONT_SIZE.getValue(), Font.PLAIN);

	/** The legend font spec. */
	private final FontSpec legendFontSpec = new FontSpec(Font.SANS_SERIF, 10, Font.PLAIN);

	/** The title font spec. */
	private final FontSpec titleFontSpec = new FontSpec(GamaPreferences.Displays.CHART_TITLE_FONT.getValue(),
			GamaPreferences.Displays.CHART_TITLE_FONT_SIZE.getValue(), Font.BOLD);

	/** The x range. */
	private final AxisRange xRange = new AxisRange();

	/** The y range. */
	private final AxisRange yRange = new AxisRange();

	/** The y 2 range. */
	private final AxisRange y2Range = new AxisRange();

	/** The x axis vis. */
	private final AxisVisibility xAxisVis = new AxisVisibility();

	/** The y axis vis. */
	private final AxisVisibility yAxisVis = new AxisVisibility();

	/** The y 2 axis vis. */
	private final AxisVisibility y2AxisVis = new AxisVisibility();

	/** The labels. */
	private final ChartLabels labels = new ChartLabels();

	/** The palette. */
	private final ColorPalette palette = new ColorPalette();

	/** The legend opts. */
	private final LegendOptions legendOpts = new LegendOptions();

	/** The tick units. */
	private final TickUnits tickUnits = new TickUnits();

	/** The display opts. */
	private final DisplayOptions displayOpts = new DisplayOptions();

	/**
	 * Gets the tick font.
	 *
	 * @return the tick font
	 */
	// Font getters
	public Font getTickFont() { return tickFontSpec.getFont(); }

	/**
	 * Gets the label font.
	 *
	 * @return the label font
	 */
	public Font getLabelFont() { return labelFontSpec.getFont(); }

	/**
	 * Gets the legend font.
	 *
	 * @return the legend font
	 */
	public Font getLegendFont() { return legendFontSpec.getFont(); }

	/**
	 * Gets the title font.
	 *
	 * @return the title font
	 */
	public Font getTitleFont() { return titleFontSpec.getFont(); }

	/**
	 * Gets the tick font spec.
	 *
	 * @return the tick font spec
	 */
	public FontSpec getTickFontSpec() { return tickFontSpec; }

	/**
	 * Gets the label font spec.
	 *
	 * @return the label font spec
	 */
	public FontSpec getLabelFontSpec() { return labelFontSpec; }

	/**
	 * Gets the legend font spec.
	 *
	 * @return the legend font spec
	 */
	public FontSpec getLegendFontSpec() { return legendFontSpec; }

	/**
	 * Gets the title font spec.
	 *
	 * @return the title font spec
	 */
	public FontSpec getTitleFontSpec() { return titleFontSpec; }

	/**
	 * Gets the x range.
	 *
	 * @return the x range
	 */
	public AxisRange getXRange() { return xRange; }

	/**
	 * Gets the y range.
	 *
	 * @return the y range
	 */
	public AxisRange getYRange() { return yRange; }

	/**
	 * Gets the y 2 range.
	 *
	 * @return the y 2 range
	 */
	public AxisRange getY2Range() { return y2Range; }

	/**
	 * Gets the x axis vis.
	 *
	 * @return the x axis vis
	 */
	public AxisVisibility getXAxisVis() { return xAxisVis; }

	/**
	 * Gets the y axis vis.
	 *
	 * @return the y axis vis
	 */
	public AxisVisibility getYAxisVis() { return yAxisVis; }

	/**
	 * Gets the y 2 axis vis.
	 *
	 * @return the y 2 axis vis
	 */
	public AxisVisibility getY2AxisVis() { return y2AxisVis; }

	/**
	 * Gets the labels.
	 *
	 * @return the labels
	 */
	public ChartLabels getLabels() { return labels; }

	/**
	 * Gets the palette.
	 *
	 * @return the palette
	 */
	public ColorPalette getPalette() { return palette; }

	/**
	 * Sets the host display output.
	 *
	 * @param host
	 *            the new host display output
	 */
	public void setHostDisplayOutput(final IOutput.Display host) {
		palette.setHostDisplayOutput(host);
	}

	/**
	 * Gets the legend opts.
	 *
	 * @return the legend opts
	 */
	public LegendOptions getLegendOpts() { return legendOpts; }

	/**
	 * Gets the tick units.
	 *
	 * @return the tick units
	 */
	public TickUnits getTickUnits() { return tickUnits; }

	/**
	 * Gets the display opts.
	 *
	 * @return the display opts
	 */
	public DisplayOptions getDisplayOpts() { return displayOpts; }

	// Delegated property getters/setters

	/**
	 * Gets the x label.
	 *
	 * @return the x label
	 */
	public String getXLabel() { return labels.getXLabel(); }

	/**
	 * Sets the x label.
	 *
	 * @param label
	 *            the new x label
	 */
	public void setXLabel(final String label) {
		labels.setXLabel(label);
	}

	/**
	 * Gets the y label.
	 *
	 * @return the y label
	 */
	public String getYLabel() { return labels.getYLabel(); }

	/**
	 * Sets the y label.
	 *
	 * @param label
	 *            the new y label
	 */
	public void setYLabel(final String label) {
		labels.setYLabel(label);
	}

	/**
	 * Gets the y 2 label.
	 *
	 * @return the y 2 label
	 */
	public String getY2Label() { return labels.getY2Label(); }

	/**
	 * Sets the y 2 label.
	 *
	 * @param label
	 *            the new y 2 label
	 */
	public void setY2Label(final String label) {
		labels.setY2Label(label);
	}

	/**
	 * Checks if is reverse axes.
	 *
	 * @return true, if is reverse axes
	 */
	public boolean isReverseAxes() { return displayOpts.isReverseAxes(); }

	/**
	 * Sets the reverse axes.
	 *
	 * @param reverse
	 *            the new reverse axes
	 */
	public void setReverseAxes(final boolean reverse) {
		displayOpts.setReverseAxes(reverse);
	}

	/**
	 * Checks if is x logscale.
	 *
	 * @return true, if is x logscale
	 */
	public boolean isXLogscale() { return xAxisVis.isLogscale(); }

	/**
	 * Sets the x logscale.
	 *
	 * @param logscale
	 *            the new x logscale
	 */
	public void setXLogscale(final boolean logscale) {
		xAxisVis.setLogscale(logscale);
	}

	/**
	 * Checks if is y logscale.
	 *
	 * @return true, if is y logscale
	 */
	public boolean isYLogscale() { return yAxisVis.isLogscale(); }

	/**
	 * Sets the y logscale.
	 *
	 * @param logscale
	 *            the new y logscale
	 */
	public void setYLogscale(final boolean logscale) {
		yAxisVis.setLogscale(logscale);
	}

	/**
	 * Checks if is y 2 logscale.
	 *
	 * @return true, if is y 2 logscale
	 */
	public boolean isY2Logscale() { return y2AxisVis.isLogscale(); }

	/**
	 * Sets the y 2 logscale.
	 *
	 * @param logscale
	 *            the new y 2 logscale
	 */
	public void setY2Logscale(final boolean logscale) {
		y2AxisVis.setLogscale(logscale);
	}

	/**
	 * Checks if is use second Y axis.
	 *
	 * @return true, if is use second Y axis
	 */
	public boolean isUseSecondYAxis() { return displayOpts.isUseSecondYAxis(); }

	/**
	 * Sets the use second Y axis.
	 *
	 * @param useSecond
	 *            the new use second Y axis
	 */
	public void setUseSecondYAxis(final boolean useSecond) {
		displayOpts.setUseSecondYAxis(useSecond);
	}

	/**
	 * Checks if is title visible.
	 *
	 * @return true, if is title visible
	 */
	public boolean isTitleVisible() { return displayOpts.isTitleVisible(); }

	/**
	 * Sets the title visible.
	 *
	 * @param visible
	 *            the new title visible
	 */
	public void setTitleVisible(final boolean visible) {
		displayOpts.setTitleVisible(visible);
	}

	/**
	 * Checks if is x tick value visible.
	 *
	 * @return true, if is x tick value visible
	 */
	public boolean isXTickValueVisible() { return xAxisVis.isTickValueVisible(); }

	/**
	 * Sets the x tick value visible.
	 *
	 * @param visible
	 *            the new x tick value visible
	 */
	public void setXTickValueVisible(final boolean visible) {
		xAxisVis.setTickValueVisible(visible);
	}

	/**
	 * Checks if is y tick value visible.
	 *
	 * @return true, if is y tick value visible
	 */
	public boolean isYTickValueVisible() { return yAxisVis.isTickValueVisible(); }

	/**
	 * Sets the y tick value visible.
	 *
	 * @param visible
	 *            the new y tick value visible
	 */
	public void setYTickValueVisible(final boolean visible) {
		yAxisVis.setTickValueVisible(visible);
	}

	/**
	 * Checks if is y 2 tick value visible.
	 *
	 * @return true, if is y 2 tick value visible
	 */
	public boolean isY2TickValueVisible() { return y2AxisVis.isTickValueVisible(); }

	/**
	 * Sets the y 2 tick value visible.
	 *
	 * @param visible
	 *            the new y 2 tick value visible
	 */
	public void setY2TickValueVisible(final boolean visible) {
		y2AxisVis.setTickValueVisible(visible);
	}

	/**
	 * Checks if is x tick line visible.
	 *
	 * @return true, if is x tick line visible
	 */
	public boolean isXTickLineVisible() { return xAxisVis.isTickLineVisible(); }

	/**
	 * Sets the x tick line visible.
	 *
	 * @param visible
	 *            the new x tick line visible
	 */
	public void setXTickLineVisible(final boolean visible) {
		xAxisVis.setTickLineVisible(visible);
	}

	/**
	 * Checks if is y tick line visible.
	 *
	 * @return true, if is y tick line visible
	 */
	public boolean isYTickLineVisible() { return yAxisVis.isTickLineVisible(); }

	/**
	 * Sets the y tick line visible.
	 *
	 * @param visible
	 *            the new y tick line visible
	 */
	public void setYTickLineVisible(final boolean visible) {
		yAxisVis.setTickLineVisible(visible);
	}

	/**
	 * Checks if is grid lines visible.
	 *
	 * @return true, if is grid lines visible
	 */
	public boolean isGridLinesVisible() { return displayOpts.isGridLinesVisible(); }

	/**
	 * Sets the grid lines visible.
	 *
	 * @param visible
	 *            the new grid lines visible
	 */
	public void setGridLinesVisible(final boolean visible) {
		displayOpts.setGridLinesVisible(visible);
	}

	/**
	 * Gets the background color.
	 *
	 * @return the background color
	 */
	public IColor getBackgroundColor() { return palette.getBackgroundColor(); }

	/**
	 * Sets the background color.
	 *
	 * @param color
	 *            the new background color
	 */
	public void setBackgroundColor(final IColor color) {
		palette.setBackgroundColor(color);
	}

	/**
	 * Gets the axes color.
	 *
	 * @return the axes color
	 */
	public IColor getAxesColor() { return palette.getAxesColor(); }

	/**
	 * Sets the axes color.
	 *
	 * @param color
	 *            the new axes color
	 */
	public void setAxesColor(final IColor color) {
		palette.setAxesColor(color);
	}

	/**
	 * Gets the label background color.
	 *
	 * @return the label background color
	 */
	public IColor getLabelBackgroundColor() { return palette.getLabelBackgroundColor(); }

	/**
	 * Sets the label background color.
	 *
	 * @param color
	 *            the new label background color
	 */
	public void setLabelBackgroundColor(final IColor color) {
		palette.setLabelBackgroundColor(color);
	}

	/**
	 * Gets the label text color.
	 *
	 * @return the label text color
	 */
	public IColor getLabelTextColor() { return palette.getLabelTextColor(); }

	/**
	 * Sets the label text color.
	 *
	 * @param color
	 *            the new label text color
	 */
	public void setLabelTextColor(final IColor color) {
		palette.setLabelTextColor(color);
	}

	/**
	 * Gets the text color.
	 *
	 * @return the text color
	 */
	public IColor getTextColor() { return palette.getTextColor(); }

	/**
	 * Sets the text color.
	 *
	 * @param color
	 *            the new text color
	 */
	public void setTextColor(final IColor color) {
		palette.setTextColor(color);
	}

	/**
	 * Gets the tick color.
	 *
	 * @return the tick color
	 */
	public IColor getTickColor() { return palette.getTickColor(); }

	/**
	 * Sets the tick color.
	 *
	 * @param color
	 *            the new tick color
	 */
	public void setTickColor(final IColor color) {
		palette.setTickColor(color);
	}

	/**
	 * Gets the tick font face.
	 *
	 * @return the tick font face
	 */
	public String getTickFontFace() { return tickFontSpec.getFace(); }

	/**
	 * Sets the tick font face.
	 *
	 * @param fontFace
	 *            the new tick font face
	 */
	public void setTickFontFace(final String fontFace) {
		tickFontSpec.setFace(fontFace);
	}

	/**
	 * Gets the tick font size.
	 *
	 * @return the tick font size
	 */
	public int getTickFontSize() { return tickFontSpec.getSize(); }

	/**
	 * Sets the tick font size.
	 *
	 * @param fontSize
	 *            the new tick font size
	 */
	public void setTickFontSize(final int fontSize) {
		tickFontSpec.setSize(fontSize);
	}

	/**
	 * Gets the tick font style.
	 *
	 * @return the tick font style
	 */
	public int getTickFontStyle() { return tickFontSpec.getStyle(); }

	/**
	 * Sets the tick font style.
	 *
	 * @param fontStyle
	 *            the new tick font style
	 */
	public void setTickFontStyle(final int fontStyle) {
		tickFontSpec.setStyle(fontStyle);
	}

	/**
	 * Gets the label font face.
	 *
	 * @return the label font face
	 */
	public String getLabelFontFace() { return labelFontSpec.getFace(); }

	/**
	 * Sets the label font face.
	 *
	 * @param fontFace
	 *            the new label font face
	 */
	public void setLabelFontFace(final String fontFace) {
		labelFontSpec.setFace(fontFace);
	}

	/**
	 * Gets the label font size.
	 *
	 * @return the label font size
	 */
	public int getLabelFontSize() { return labelFontSpec.getSize(); }

	/**
	 * Sets the label font size.
	 *
	 * @param fontSize
	 *            the new label font size
	 */
	public void setLabelFontSize(final int fontSize) {
		labelFontSpec.setSize(fontSize);
	}

	/**
	 * Gets the label font style.
	 *
	 * @return the label font style
	 */
	public int getLabelFontStyle() { return labelFontSpec.getStyle(); }

	/**
	 * Sets the label font style.
	 *
	 * @param fontStyle
	 *            the new label font style
	 */
	public void setLabelFontStyle(final int fontStyle) {
		labelFontSpec.setStyle(fontStyle);
	}

	/**
	 * Gets the legend font face.
	 *
	 * @return the legend font face
	 */
	public String getLegendFontFace() { return legendFontSpec.getFace(); }

	/**
	 * Sets the legend font face.
	 *
	 * @param fontFace
	 *            the new legend font face
	 */
	public void setLegendFontFace(final String fontFace) {
		legendFontSpec.setFace(fontFace);
	}

	/**
	 * Gets the legend font size.
	 *
	 * @return the legend font size
	 */
	public int getLegendFontSize() { return legendFontSpec.getSize(); }

	/**
	 * Sets the legend font size.
	 *
	 * @param fontSize
	 *            the new legend font size
	 */
	public void setLegendFontSize(final int fontSize) {
		legendFontSpec.setSize(fontSize);
	}

	/**
	 * Gets the legend font style.
	 *
	 * @return the legend font style
	 */
	public int getLegendFontStyle() { return legendFontSpec.getStyle(); }

	/**
	 * Sets the legend font style.
	 *
	 * @param fontStyle
	 *            the new legend font style
	 */
	public void setLegendFontStyle(final int fontStyle) {
		legendFontSpec.setStyle(fontStyle);
	}

	/**
	 * Gets the title font face.
	 *
	 * @return the title font face
	 */
	public String getTitleFontFace() { return titleFontSpec.getFace(); }

	/**
	 * Sets the title font face.
	 *
	 * @param fontFace
	 *            the new title font face
	 */
	public void setTitleFontFace(final String fontFace) {
		titleFontSpec.setFace(fontFace);
	}

	/**
	 * Gets the title font size.
	 *
	 * @return the title font size
	 */
	public int getTitleFontSize() { return titleFontSpec.getSize(); }

	/**
	 * Sets the title font size.
	 *
	 * @param fontSize
	 *            the new title font size
	 */
	public void setTitleFontSize(final int fontSize) {
		titleFontSpec.setSize(fontSize);
	}

	/**
	 * Gets the title font style.
	 *
	 * @return the title font style
	 */
	public int getTitleFontStyle() { return titleFontSpec.getStyle(); }

	/**
	 * Sets the title font style.
	 *
	 * @param fontStyle
	 *            the new title font style
	 */
	public void setTitleFontStyle(final int fontStyle) {
		titleFontSpec.setStyle(fontStyle);
	}

	/**
	 * Gets the series label position.
	 *
	 * @return the series label position
	 */
	public String getSeriesLabelPosition() { return legendOpts.getSeriesLabelPosition(); }

	/**
	 * Sets the series label position.
	 *
	 * @param pos
	 *            the new series label position
	 */
	public void setSeriesLabelPosition(final String pos) {
		legendOpts.setSeriesLabelPosition(pos);
	}

	/**
	 * Gets the legend orientation.
	 *
	 * @return the legend orientation
	 */
	public String getLegendOrientation() { return legendOpts.getLegendOrientation(); }

	/**
	 * Sets the legend orientation.
	 *
	 * @param orient
	 *            the new legend orientation
	 */
	public void setLegendOrientation(final String orient) {
		legendOpts.setLegendOrientation(orient);
	}

	/**
	 * Gets the series label anchor.
	 *
	 * @return the series label anchor
	 */
	public IPoint getSeriesLabelAnchor() { return legendOpts.getSeriesLabelAnchor(); }

	/**
	 * Sets the series label anchor.
	 *
	 * @param anchor
	 *            the new series label anchor
	 */
	public void setSeriesLabelAnchor(final IPoint anchor) {
		legendOpts.setSeriesLabelAnchor(anchor);
	}

	/**
	 * Gets the style.
	 *
	 * @return the style
	 */
	public String getStyle() { return displayOpts.getStyle(); }

	/**
	 * Sets the style.
	 *
	 * @param style
	 *            the new style
	 */
	public void setStyle(final String style) {
		displayOpts.setStyle(style);
	}

	/**
	 * Gets the gap.
	 *
	 * @return the gap
	 */
	public double getGap() { return displayOpts.getGap(); }

	/**
	 * Sets the gap.
	 *
	 * @param gap
	 *            the new gap
	 */
	public void setGap(final double gap) {
		displayOpts.setGap(gap);
	}

	/**
	 * Gets the x range interval.
	 *
	 * @return the x range interval
	 */
	public double getXRangeInterval() { return xRange.getInterval(); }

	/**
	 * Sets the x range interval.
	 *
	 * @param val
	 *            the new x range interval
	 */
	public void setXRangeInterval(final double val) {
		xRange.setInterval(val);
	}

	/**
	 * Checks if is use X range interval.
	 *
	 * @return true, if is use X range interval
	 */
	public boolean isUseXRangeInterval() { return xRange.isUseInterval(); }

	/**
	 * Sets the X range min max.
	 *
	 * @param min
	 *            the min
	 * @param max
	 *            the max
	 */
	public void setXRangeMinMax(final double min, final double max) {
		xRange.setMinMax(min, max);
	}

	/**
	 * Checks if is use X range min max.
	 *
	 * @return true, if is use X range min max
	 */
	public boolean isUseXRangeMinMax() { return xRange.isUseMinMax(); }

	/**
	 * Gets the x range min.
	 *
	 * @return the x range min
	 */
	public double getXRangeMin() { return xRange.getMin(); }

	/**
	 * Gets the x range max.
	 *
	 * @return the x range max
	 */
	public double getXRangeMax() { return xRange.getMax(); }

	/**
	 * Checks if is use X min.
	 *
	 * @return true, if is use X min
	 */
	public boolean isUseXMin() { return xRange.isUseMin(); }

	/**
	 * Sets the x min.
	 *
	 * @param min
	 *            the new x min
	 */
	public void setXMin(final double min) {
		xRange.setMin(min);
	}

	/**
	 * Gets the x min val.
	 *
	 * @return the x min val
	 */
	public double getXMinVal() { return xRange.getBoundMin(); }

	/**
	 * Checks if is use X max.
	 *
	 * @return true, if is use X max
	 */
	public boolean isUseXMax() { return xRange.isUseMax(); }

	/**
	 * Sets the x max.
	 *
	 * @param max
	 *            the new x max
	 */
	public void setXMax(final double max) {
		xRange.setMax(max);
	}

	/**
	 * Gets the x max val.
	 *
	 * @return the x max val
	 */
	public double getXMaxVal() { return xRange.getBoundMax(); }

	/**
	 * Gets the y range interval.
	 *
	 * @return the y range interval
	 */
	public double getYRangeInterval() { return yRange.getInterval(); }

	/**
	 * Sets the y range interval.
	 *
	 * @param val
	 *            the new y range interval
	 */
	public void setYRangeInterval(final double val) {
		yRange.setInterval(val);
	}

	/**
	 * Checks if is use Y range interval.
	 *
	 * @return true, if is use Y range interval
	 */
	public boolean isUseYRangeInterval() { return yRange.isUseInterval(); }

	/**
	 * Sets the Y range min max.
	 *
	 * @param min
	 *            the min
	 * @param max
	 *            the max
	 */
	public void setYRangeMinMax(final double min, final double max) {
		yRange.setMinMax(min, max);
	}

	/**
	 * Checks if is use Y range min max.
	 *
	 * @return true, if is use Y range min max
	 */
	public boolean isUseYRangeMinMax() { return yRange.isUseMinMax(); }

	/**
	 * Gets the y range min.
	 *
	 * @return the y range min
	 */
	public double getYRangeMin() { return yRange.getMin(); }

	/**
	 * Gets the y range max.
	 *
	 * @return the y range max
	 */
	public double getYRangeMax() { return yRange.getMax(); }

	/**
	 * Checks if is use Y min.
	 *
	 * @return true, if is use Y min
	 */
	public boolean isUseYMin() { return yRange.isUseMin(); }

	/**
	 * Sets the y min.
	 *
	 * @param min
	 *            the new y min
	 */
	public void setYMin(final double min) {
		yRange.setMin(min);
	}

	/**
	 * Gets the y min val.
	 *
	 * @return the y min val
	 */
	public double getYMinVal() { return yRange.getBoundMin(); }

	/**
	 * Checks if is use Y max.
	 *
	 * @return true, if is use Y max
	 */
	public boolean isUseYMax() { return yRange.isUseMax(); }

	/**
	 * Sets the y max.
	 *
	 * @param max
	 *            the new y max
	 */
	public void setYMax(final double max) {
		yRange.setMax(max);
	}

	/**
	 * Gets the y max val.
	 *
	 * @return the y max val
	 */
	public double getYMaxVal() { return yRange.getBoundMax(); }

	/**
	 * Gets the y 2 range interval.
	 *
	 * @return the y 2 range interval
	 */
	public double getY2RangeInterval() { return y2Range.getInterval(); }

	/**
	 * Sets the y 2 range interval.
	 *
	 * @param val
	 *            the new y 2 range interval
	 */
	public void setY2RangeInterval(final double val) {
		y2Range.setInterval(val);
	}

	/**
	 * Checks if is use Y 2 range interval.
	 *
	 * @return true, if is use Y 2 range interval
	 */
	public boolean isUseY2RangeInterval() { return y2Range.isUseInterval(); }

	/**
	 * Sets the Y 2 range min max.
	 *
	 * @param min
	 *            the min
	 * @param max
	 *            the max
	 */
	public void setY2RangeMinMax(final double min, final double max) {
		y2Range.setMinMax(min, max);
	}

	/**
	 * Checks if is use Y 2 range min max.
	 *
	 * @return true, if is use Y 2 range min max
	 */
	public boolean isUseY2RangeMinMax() { return y2Range.isUseMinMax(); }

	/**
	 * Gets the y 2 range min.
	 *
	 * @return the y 2 range min
	 */
	public double getY2RangeMin() { return y2Range.getMin(); }

	/**
	 * Gets the y 2 range max.
	 *
	 * @return the y 2 range max
	 */
	public double getY2RangeMax() { return y2Range.getMax(); }

	/**
	 * Gets the x tick unit.
	 *
	 * @return the x tick unit
	 */
	public double getXTickUnit() { return tickUnits.getXTickUnit(); }

	/**
	 * Sets the x tick unit.
	 *
	 * @param unit
	 *            the new x tick unit
	 */
	public void setXTickUnit(final double unit) {
		tickUnits.setXTickUnit(unit);
	}

	/**
	 * Gets the y tick unit.
	 *
	 * @return the y tick unit
	 */
	public double getYTickUnit() { return tickUnits.getYTickUnit(); }

	/**
	 * Sets the y tick unit.
	 *
	 * @param unit
	 *            the new y tick unit
	 */
	public void setYTickUnit(final double unit) {
		tickUnits.setYTickUnit(unit);
	}

	/**
	 * Gets the y 2 tick unit.
	 *
	 * @return the y 2 tick unit
	 */
	public double getY2TickUnit() { return tickUnits.getY2TickUnit(); }

	/**
	 * Sets the y 2 tick unit.
	 *
	 * @param unit
	 *            the new y 2 tick unit
	 */
	public void setY2TickUnit(final double unit) {
		tickUnits.setY2TickUnit(unit);
	}

}
