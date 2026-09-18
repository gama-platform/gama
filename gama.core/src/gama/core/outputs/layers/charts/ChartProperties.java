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
import gama.api.types.color.GamaColorFactory;
import gama.api.types.color.IColor;
import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.IPoint;
import gama.api.runtime.scope.IScope;
import gama.api.utils.prefs.GamaPreferences;
import gama.gaml.operators.Random;

/**
 * Encapsulates style, font, color, label, and axis configuration properties for GAMA charts.
 * Initialized from GamaPreferences.Displays user preference defaults.
 */
public class ChartProperties {

	public static class FontSpec {
		private String face;
		private int size;
		private int style;

		public FontSpec(final String face, final int size, final int style) {
			this.face = face;
			this.size = size;
			this.style = style;
		}

		public Font getFont() { return new Font(face, style, size); }
		public String getFace() { return face; }
		public void setFace(final String face) { if (face != null) this.face = face; }
		public int getSize() { return size; }
		public void setSize(final int size) { this.size = size; }
		public int getStyle() { return style; }
		public void setStyle(final int style) { this.style = style; }
	}

	public static class AxisRange {
		private double interval;
		private double min;
		private double max;
		private boolean useInterval = false;
		private boolean useMinMax = false;
		private double boundMin;
		private double boundMax;
		private boolean useMin = false;
		private boolean useMax = false;

		public double getInterval() { return interval; }
		public void setInterval(final double val) { this.useInterval = true; this.interval = val; }
		public boolean isUseInterval() { return useInterval; }

		public void setMinMax(final double min, final double max) {
			this.useMinMax = true;
			this.min = min;
			this.max = max;
		}
		public boolean isUseMinMax() { return useMinMax; }
		public double getMin() { return min; }
		public double getMax() { return max; }

		public boolean isUseMin() { return useMin; }
		public void setMin(final double min) { this.useMin = true; this.boundMin = min; }
		public double getBoundMin() { return boundMin; }

		public boolean isUseMax() { return useMax; }
		public void setMax(final double max) { this.useMax = true; this.boundMax = max; }
		public double getBoundMax() { return boundMax; }
	}

	public static class AxisVisibility {
		private boolean tickValueVisible = true;
		private boolean tickLineVisible = true;
		private boolean logscale = false;

		public boolean isTickValueVisible() { return tickValueVisible; }
		public void setTickValueVisible(final boolean v) { this.tickValueVisible = v; }
		public boolean isTickLineVisible() { return tickLineVisible; }
		public void setTickLineVisible(final boolean v) { this.tickLineVisible = v; }
		public boolean isLogscale() { return logscale; }
		public void setLogscale(final boolean v) { this.logscale = v; }
	}

	public static class ChartLabels {
		private String xLabel = null;
		private String yLabel = null;
		private String y2Label = null;

		public String getXLabel() { return xLabel; }
		public void setXLabel(final String label) { this.xLabel = label; }
		public String getYLabel() { return yLabel; }
		public void setYLabel(final String label) { this.yLabel = label; }
		public String getY2Label() { return y2Label; }
		public void setY2Label(final String label) { this.y2Label = label; }
	}

	public static class ColorPalette {
		private IColor backgroundColor = GamaPreferences.Displays.CHART_BACKGROUND_COLOR.getValue();
		private IColor axesColor = GamaPreferences.Displays.CHART_GRID_COLOR.getValue();
		private IColor labelBackgroundColor = null;
		private IColor labelTextColor = GamaPreferences.Displays.CHART_TEXT_COLOR.getValue();
		private IColor textColor = GamaPreferences.Displays.CHART_TEXT_COLOR.getValue();
		private IColor tickColor = GamaColorFactory.get(100, 110, 120);

		public IColor getBackgroundColor() { return backgroundColor; }
		public void setBackgroundColor(final IColor color) { this.backgroundColor = color; }
		public IColor getAxesColor() { return axesColor; }
		public void setAxesColor(final IColor color) { this.axesColor = color; }
		public IColor getLabelBackgroundColor() { return labelBackgroundColor; }
		public void setLabelBackgroundColor(final IColor color) { this.labelBackgroundColor = color; }
		public IColor getLabelTextColor() { return labelTextColor; }
		public void setLabelTextColor(final IColor color) { this.labelTextColor = color; }
		public IColor getTextColor() { return textColor; }
		public void setTextColor(final IColor color) { this.textColor = color; }
		public IColor getTickColor() { return tickColor; }
		public void setTickColor(final IColor color) { this.tickColor = color; }
	}

	public static class LegendOptions {
		private String seriesLabelPosition = IKeyword.DEFAULT;
		private String legendOrientation = "default";
		private IPoint seriesLabelAnchor = GamaPointFactory.create(1, 1);

		public String getSeriesLabelPosition() { return seriesLabelPosition; }
		public void setSeriesLabelPosition(final String pos) { this.seriesLabelPosition = pos; }
		public String getLegendOrientation() { return legendOrientation; }
		public void setLegendOrientation(final String orient) { this.legendOrientation = orient; }
		public IPoint getSeriesLabelAnchor() { return seriesLabelAnchor; }
		public void setSeriesLabelAnchor(final IPoint anchor) { this.seriesLabelAnchor = anchor; }
	}

	public static class TickUnits {
		private double xTickUnit = -1;
		private double yTickUnit = -1;
		private double y2TickUnit = -1;

		public double getXTickUnit() { return xTickUnit; }
		public void setXTickUnit(final double unit) { this.xTickUnit = unit; }
		public double getYTickUnit() { return yTickUnit; }
		public void setYTickUnit(final double unit) { this.yTickUnit = unit; }
		public double getY2TickUnit() { return y2TickUnit; }
		public void setY2TickUnit(final double unit) { this.y2TickUnit = unit; }
	}

	public static class DisplayOptions {
		private boolean reverseAxes = false;
		private boolean useSecondYAxis = false;
		private boolean titleVisible = true;
		private boolean gridLinesVisible = GamaPreferences.Displays.CHART_GRID_LINES.getValue();
		private String style = IKeyword.DEFAULT;
		private double gap = -1;

		public boolean isReverseAxes() { return reverseAxes; }
		public void setReverseAxes(final boolean reverse) { this.reverseAxes = reverse; }
		public boolean isUseSecondYAxis() { return useSecondYAxis; }
		public void setUseSecondYAxis(final boolean useSecond) { this.useSecondYAxis = useSecond; }
		public boolean isTitleVisible() { return titleVisible; }
		public void setTitleVisible(final boolean visible) { this.titleVisible = visible; }
		public boolean isGridLinesVisible() { return gridLinesVisible; }
		public void setGridLinesVisible(final boolean visible) { this.gridLinesVisible = visible; }
		public String getStyle() { return style; }
		public void setStyle(final String style) { this.style = style; }
		public double getGap() { return gap; }
		public void setGap(final double gap) { this.gap = gap; }
	}

	/**
	 * Returns default series color based on the selected ColorBrewer palette preference.
	 */
	public static IColor getDefaultSeriesColor(final IScope scope, final int index) {
		String palette = GamaPreferences.Displays.CHART_COLOR_PALETTE.getValue();
		int idx = Math.max(0, index);
		if (GamaPreferences.Displays.CHART_PALETTE_DIVERGING.equals(palette) && GamaPreferences.DIVERGING_COLORS.length > 0) {
			return GamaPreferences.DIVERGING_COLORS[idx % GamaPreferences.DIVERGING_COLORS.length].get();
		}
		if (GamaPreferences.Displays.CHART_PALETTE_BASIC.equals(palette) && GamaPreferences.BASIC_COLORS.length > 0) {
			return GamaPreferences.BASIC_COLORS[idx % GamaPreferences.BASIC_COLORS.length].get();
		}
		if (GamaPreferences.Displays.CHART_PALETTE_PIVOT.equals(palette)) {
			IColor c = GamaPreferences.Displays.CHART_PIVOT_COLOR.getValue();
			if (c == null) { c = GamaColorFactory.get(31, 120, 180); }
			IColor[] ramp = new IColor[] {
				c.darker().darker().darker().darker(),
				c.darker().darker().darker(),
				c.darker().darker(),
				c.darker(),
				c,
				c.brighter(),
				c.brighter().brighter(),
				c.brighter().brighter().brighter(),
				c.brighter().brighter().brighter().brighter()
			};
			return ramp[idx % ramp.length];
		}
		if (GamaPreferences.Displays.CHART_PALETTE_RANDOM.equals(palette) && scope != null) {
			return GamaColorFactory.createWithRGBA(Random.opRnd(scope, 255), Random.opRnd(scope, 255), Random.opRnd(scope, 255), 255);
		}
		if (GamaPreferences.QUALITATIVE_COLORS.length > 0) {
			return GamaPreferences.QUALITATIVE_COLORS[idx % GamaPreferences.QUALITATIVE_COLORS.length].get();
		}
		return GamaColorFactory.get("blue");
	}

	// Component Objects initialized from GamaPreferences.Displays
	private final FontSpec tickFontSpec = new FontSpec(Font.SANS_SERIF, 9, Font.PLAIN);
	private final FontSpec labelFontSpec = new FontSpec(GamaPreferences.Displays.CHART_LABEL_FONT.getValue(), GamaPreferences.Displays.CHART_LABEL_FONT_SIZE.getValue(), Font.PLAIN);
	private final FontSpec legendFontSpec = new FontSpec(Font.SANS_SERIF, 10, Font.PLAIN);
	private final FontSpec titleFontSpec = new FontSpec(GamaPreferences.Displays.CHART_TITLE_FONT.getValue(), GamaPreferences.Displays.CHART_TITLE_FONT_SIZE.getValue(), Font.BOLD);

	private final AxisRange xRange = new AxisRange();
	private final AxisRange yRange = new AxisRange();
	private final AxisRange y2Range = new AxisRange();

	private final AxisVisibility xAxisVis = new AxisVisibility();
	private final AxisVisibility yAxisVis = new AxisVisibility();
	private final AxisVisibility y2AxisVis = new AxisVisibility();

	private final ChartLabels labels = new ChartLabels();
	private final ColorPalette palette = new ColorPalette();
	private final LegendOptions legendOpts = new LegendOptions();
	private final TickUnits tickUnits = new TickUnits();
	private final DisplayOptions displayOpts = new DisplayOptions();

	// Font getters
	public Font getTickFont() { return tickFontSpec.getFont(); }
	public Font getLabelFont() { return labelFontSpec.getFont(); }
	public Font getLegendFont() { return legendFontSpec.getFont(); }
	public Font getTitleFont() { return titleFontSpec.getFont(); }

	public FontSpec getTickFontSpec() { return tickFontSpec; }
	public FontSpec getLabelFontSpec() { return labelFontSpec; }
	public FontSpec getLegendFontSpec() { return legendFontSpec; }
	public FontSpec getTitleFontSpec() { return titleFontSpec; }

	public AxisRange getXRange() { return xRange; }
	public AxisRange getYRange() { return yRange; }
	public AxisRange getY2Range() { return y2Range; }

	public AxisVisibility getXAxisVis() { return xAxisVis; }
	public AxisVisibility getYAxisVis() { return yAxisVis; }
	public AxisVisibility getY2AxisVis() { return y2AxisVis; }

	public ChartLabels getLabels() { return labels; }
	public ColorPalette getPalette() { return palette; }
	public LegendOptions getLegendOpts() { return legendOpts; }
	public TickUnits getTickUnits() { return tickUnits; }
	public DisplayOptions getDisplayOpts() { return displayOpts; }

	// Delegated property getters/setters

	public String getXLabel() { return labels.getXLabel(); }
	public void setXLabel(final String label) { labels.setXLabel(label); }

	public String getYLabel() { return labels.getYLabel(); }
	public void setYLabel(final String label) { labels.setYLabel(label); }

	public String getY2Label() { return labels.getY2Label(); }
	public void setY2Label(final String label) { labels.setY2Label(label); }

	public boolean isReverseAxes() { return displayOpts.isReverseAxes(); }
	public void setReverseAxes(final boolean reverse) { displayOpts.setReverseAxes(reverse); }

	public boolean isXLogscale() { return xAxisVis.isLogscale(); }
	public void setXLogscale(final boolean logscale) { xAxisVis.setLogscale(logscale); }

	public boolean isYLogscale() { return yAxisVis.isLogscale(); }
	public void setYLogscale(final boolean logscale) { yAxisVis.setLogscale(logscale); }

	public boolean isY2Logscale() { return y2AxisVis.isLogscale(); }
	public void setY2Logscale(final boolean logscale) { y2AxisVis.setLogscale(logscale); }

	public boolean isUseSecondYAxis() { return displayOpts.isUseSecondYAxis(); }
	public void setUseSecondYAxis(final boolean useSecond) { displayOpts.setUseSecondYAxis(useSecond); }

	public boolean isTitleVisible() { return displayOpts.isTitleVisible(); }
	public void setTitleVisible(final boolean visible) { displayOpts.setTitleVisible(visible); }

	public boolean isXTickValueVisible() { return xAxisVis.isTickValueVisible(); }
	public void setXTickValueVisible(final boolean visible) { xAxisVis.setTickValueVisible(visible); }

	public boolean isYTickValueVisible() { return yAxisVis.isTickValueVisible(); }
	public void setYTickValueVisible(final boolean visible) { yAxisVis.setTickValueVisible(visible); }

	public boolean isY2TickValueVisible() { return y2AxisVis.isTickValueVisible(); }
	public void setY2TickValueVisible(final boolean visible) { y2AxisVis.setTickValueVisible(visible); }

	public boolean isXTickLineVisible() { return xAxisVis.isTickLineVisible(); }
	public void setXTickLineVisible(final boolean visible) { xAxisVis.setTickLineVisible(visible); }

	public boolean isYTickLineVisible() { return yAxisVis.isTickLineVisible(); }
	public void setYTickLineVisible(final boolean visible) { yAxisVis.setTickLineVisible(visible); }

	public boolean isGridLinesVisible() { return displayOpts.isGridLinesVisible(); }
	public void setGridLinesVisible(final boolean visible) { displayOpts.setGridLinesVisible(visible); }

	public IColor getBackgroundColor() { return palette.getBackgroundColor(); }
	public void setBackgroundColor(final IColor color) { palette.setBackgroundColor(color); }

	public IColor getAxesColor() { return palette.getAxesColor(); }
	public void setAxesColor(final IColor color) { palette.setAxesColor(color); }

	public IColor getLabelBackgroundColor() { return palette.getLabelBackgroundColor(); }
	public void setLabelBackgroundColor(final IColor color) { palette.setLabelBackgroundColor(color); }

	public IColor getLabelTextColor() { return palette.getLabelTextColor(); }
	public void setLabelTextColor(final IColor color) { palette.setLabelTextColor(color); }

	public IColor getTextColor() { return palette.getTextColor(); }
	public void setTextColor(final IColor color) { palette.setTextColor(color); }

	public IColor getTickColor() { return palette.getTickColor(); }
	public void setTickColor(final IColor color) { palette.setTickColor(color); }

	public String getTickFontFace() { return tickFontSpec.getFace(); }
	public void setTickFontFace(final String fontFace) { tickFontSpec.setFace(fontFace); }

	public int getTickFontSize() { return tickFontSpec.getSize(); }
	public void setTickFontSize(final int fontSize) { tickFontSpec.setSize(fontSize); }

	public int getTickFontStyle() { return tickFontSpec.getStyle(); }
	public void setTickFontStyle(final int fontStyle) { tickFontSpec.setStyle(fontStyle); }

	public String getLabelFontFace() { return labelFontSpec.getFace(); }
	public void setLabelFontFace(final String fontFace) { labelFontSpec.setFace(fontFace); }

	public int getLabelFontSize() { return labelFontSpec.getSize(); }
	public void setLabelFontSize(final int fontSize) { labelFontSpec.setSize(fontSize); }

	public int getLabelFontStyle() { return labelFontSpec.getStyle(); }
	public void setLabelFontStyle(final int fontStyle) { labelFontSpec.setStyle(fontStyle); }

	public String getLegendFontFace() { return legendFontSpec.getFace(); }
	public void setLegendFontFace(final String fontFace) { legendFontSpec.setFace(fontFace); }

	public int getLegendFontSize() { return legendFontSpec.getSize(); }
	public void setLegendFontSize(final int fontSize) { legendFontSpec.setSize(fontSize); }

	public int getLegendFontStyle() { return legendFontSpec.getStyle(); }
	public void setLegendFontStyle(final int fontStyle) { legendFontSpec.setStyle(fontStyle); }

	public String getTitleFontFace() { return titleFontSpec.getFace(); }
	public void setTitleFontFace(final String fontFace) { titleFontSpec.setFace(fontFace); }

	public int getTitleFontSize() { return titleFontSpec.getSize(); }
	public void setTitleFontSize(final int fontSize) { titleFontSpec.setSize(fontSize); }

	public int getTitleFontStyle() { return titleFontSpec.getStyle(); }
	public void setTitleFontStyle(final int fontStyle) { titleFontSpec.setStyle(fontStyle); }

	public String getSeriesLabelPosition() { return legendOpts.getSeriesLabelPosition(); }
	public void setSeriesLabelPosition(final String pos) { legendOpts.setSeriesLabelPosition(pos); }

	public String getLegendOrientation() { return legendOpts.getLegendOrientation(); }
	public void setLegendOrientation(final String orient) { legendOpts.setLegendOrientation(orient); }

	public IPoint getSeriesLabelAnchor() { return legendOpts.getSeriesLabelAnchor(); }
	public void setSeriesLabelAnchor(final IPoint anchor) { legendOpts.setSeriesLabelAnchor(anchor); }

	public String getStyle() { return displayOpts.getStyle(); }
	public void setStyle(final String style) { displayOpts.setStyle(style); }

	public double getGap() { return displayOpts.getGap(); }
	public void setGap(final double gap) { displayOpts.setGap(gap); }

	public double getXRangeInterval() { return xRange.getInterval(); }
	public void setXRangeInterval(final double val) { xRange.setInterval(val); }
	public boolean isUseXRangeInterval() { return xRange.isUseInterval(); }

	public void setXRangeMinMax(final double min, final double max) { xRange.setMinMax(min, max); }
	public boolean isUseXRangeMinMax() { return xRange.isUseMinMax(); }
	public double getXRangeMin() { return xRange.getMin(); }
	public double getXRangeMax() { return xRange.getMax(); }

	public boolean isUseXMin() { return xRange.isUseMin(); }
	public void setXMin(final double min) { xRange.setMin(min); }
	public double getXMinVal() { return xRange.getBoundMin(); }

	public boolean isUseXMax() { return xRange.isUseMax(); }
	public void setXMax(final double max) { xRange.setMax(max); }
	public double getXMaxVal() { return xRange.getBoundMax(); }

	public double getYRangeInterval() { return yRange.getInterval(); }
	public void setYRangeInterval(final double val) { yRange.setInterval(val); }
	public boolean isUseYRangeInterval() { return yRange.isUseInterval(); }

	public void setYRangeMinMax(final double min, final double max) { yRange.setMinMax(min, max); }
	public boolean isUseYRangeMinMax() { return yRange.isUseMinMax(); }
	public double getYRangeMin() { return yRange.getMin(); }
	public double getYRangeMax() { return yRange.getMax(); }

	public boolean isUseYMin() { return yRange.isUseMin(); }
	public void setYMin(final double min) { yRange.setMin(min); }
	public double getYMinVal() { return yRange.getBoundMin(); }

	public boolean isUseYMax() { return yRange.isUseMax(); }
	public void setYMax(final double max) { yRange.setMax(max); }
	public double getYMaxVal() { return yRange.getBoundMax(); }

	public double getY2RangeInterval() { return y2Range.getInterval(); }
	public void setY2RangeInterval(final double val) { y2Range.setInterval(val); }
	public boolean isUseY2RangeInterval() { return y2Range.isUseInterval(); }

	public void setY2RangeMinMax(final double min, final double max) { y2Range.setMinMax(min, max); }
	public boolean isUseY2RangeMinMax() { return y2Range.isUseMinMax(); }
	public double getY2RangeMin() { return y2Range.getMin(); }
	public double getY2RangeMax() { return y2Range.getMax(); }

	public double getXTickUnit() { return tickUnits.getXTickUnit(); }
	public void setXTickUnit(final double unit) { tickUnits.setXTickUnit(unit); }

	public double getYTickUnit() { return tickUnits.getYTickUnit(); }
	public void setYTickUnit(final double unit) { tickUnits.setYTickUnit(unit); }

	public double getY2TickUnit() { return tickUnits.getY2TickUnit(); }
	public void setY2TickUnit(final double unit) { tickUnits.setY2TickUnit(unit); }

}
