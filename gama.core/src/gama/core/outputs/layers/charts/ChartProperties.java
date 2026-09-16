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

/**
 * Encapsulates style, font, color, label, and axis configuration properties for GAMA charts.
 */
public class ChartProperties {

	// Labels
	private String xLabel = null;
	private String yLabel = null;
	private String y2Label = null;

	// Reverse axis & log scale flags
	private boolean reverseAxes = false;
	private boolean xLogscale = false;
	private boolean yLogscale = false;
	private boolean y2Logscale = false;
	private boolean useSecondYAxis = false;

	// Visibility flags
	private boolean titleVisible = true;
	private boolean xTickValueVisible = true;
	private boolean yTickValueVisible = true;
	private boolean y2TickValueVisible = true;
	private boolean xTickLineVisible = true;
	private boolean yTickLineVisible = true;
	private boolean gridLinesVisible = true;

	// Colors
	private IColor backgroundColor = GamaColorFactory.WHITE;
	private IColor axesColor = GamaColorFactory.BLACK;
	private IColor labelBackgroundColor = null;
	private IColor labelTextColor = null;
	private IColor textColor = null;
	private IColor tickColor = null;

	// Fonts
	private String tickFontFace = Font.SANS_SERIF;
	private int tickFontSize = 10;
	private int tickFontStyle = Font.PLAIN;

	private String labelFontFace = Font.SANS_SERIF;
	private int labelFontSize = 12;
	private int labelFontStyle = Font.BOLD;

	private String legendFontFace = Font.SANS_SERIF;
	private int legendFontSize = 10;
	private int legendFontStyle = Font.ITALIC;

	private String titleFontFace = Font.SERIF;
	private int titleFontSize = 14;
	private int titleFontStyle = Font.BOLD;

	// Layout and legend
	private String seriesLabelPosition = IKeyword.DEFAULT;
	private String legendOrientation = "default";
	private IPoint seriesLabelAnchor = GamaPointFactory.create(1, 1);
	private String style = IKeyword.DEFAULT;
	private double gap = -1;

	// X Axis Ranges
	private double xRangeInterval;
	private double xRangeMin;
	private double xRangeMax;
	private boolean useXRangeInterval = false;
	private boolean useXRangeMinMax = false;
	private double xMinVal;
	private double xMaxVal;
	private boolean useXMin = false;
	private boolean useXMax = false;

	// Y Axis Ranges
	private double yRangeInterval;
	private double yRangeMin;
	private double yRangeMax;
	private boolean useYRangeInterval = false;
	private boolean useYRangeMinMax = false;
	private double yMinVal;
	private double yMaxVal;
	private boolean useYMin = false;
	private boolean useYMax = false;

	// Y2 Axis Ranges
	private double y2RangeInterval;
	private double y2RangeMin;
	private double y2RangeMax;
	private boolean useY2RangeInterval = false;
	private boolean useY2RangeMinMax = false;

	// Tick units
	private double xTickUnit = -1;
	private double yTickUnit = -1;
	private double y2TickUnit = -1;

	/**
	 * Gets font for ticks.
	 */
	public Font getTickFont() {
		return new Font(tickFontFace, tickFontStyle, tickFontSize);
	}

	/**
	 * Gets font for axis labels.
	 */
	public Font getLabelFont() {
		return new Font(labelFontFace, labelFontStyle, labelFontSize);
	}

	/**
	 * Gets font for legend.
	 */
	public Font getLegendFont() {
		return new Font(legendFontFace, legendFontStyle, legendFontSize);
	}

	/**
	 * Gets font for title.
	 */
	public Font getTitleFont() {
		return new Font(titleFontFace, titleFontStyle, titleFontSize);
	}

	// Getters and Setters

	public String getXLabel() { return xLabel; }
	public void setXLabel(final String label) { this.xLabel = label; }

	public String getYLabel() { return yLabel; }
	public void setYLabel(final String label) { this.yLabel = label; }

	public String getY2Label() { return y2Label; }
	public void setY2Label(final String label) { this.y2Label = label; }

	public boolean isReverseAxes() { return reverseAxes; }
	public void setReverseAxes(final boolean reverse) { this.reverseAxes = reverse; }

	public boolean isXLogscale() { return xLogscale; }
	public void setXLogscale(final boolean logscale) { this.xLogscale = logscale; }

	public boolean isYLogscale() { return yLogscale; }
	public void setYLogscale(final boolean logscale) { this.yLogscale = logscale; }

	public boolean isY2Logscale() { return y2Logscale; }
	public void setY2Logscale(final boolean logscale) { this.y2Logscale = logscale; }

	public boolean isUseSecondYAxis() { return useSecondYAxis; }
	public void setUseSecondYAxis(final boolean useSecond) { this.useSecondYAxis = useSecond; }

	public boolean isTitleVisible() { return titleVisible; }
	public void setTitleVisible(final boolean visible) { this.titleVisible = visible; }

	public boolean isXTickValueVisible() { return xTickValueVisible; }
	public void setXTickValueVisible(final boolean visible) { this.xTickValueVisible = visible; }

	public boolean isYTickValueVisible() { return yTickValueVisible; }
	public void setYTickValueVisible(final boolean visible) { this.yTickValueVisible = visible; }

	public boolean isY2TickValueVisible() { return y2TickValueVisible; }
	public void setY2TickValueVisible(final boolean visible) { this.y2TickValueVisible = visible; }

	public boolean isXTickLineVisible() { return xTickLineVisible; }
	public void setXTickLineVisible(final boolean visible) { this.xTickLineVisible = visible; }

	public boolean isYTickLineVisible() { return yTickLineVisible; }
	public void setYTickLineVisible(final boolean visible) { this.yTickLineVisible = visible; }

	public boolean isGridLinesVisible() { return gridLinesVisible; }
	public void setGridLinesVisible(final boolean visible) { this.gridLinesVisible = visible; }

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

	public String getTickFontFace() { return tickFontFace; }
	public void setTickFontFace(final String fontFace) { if (fontFace != null) this.tickFontFace = fontFace; }

	public int getTickFontSize() { return tickFontSize; }
	public void setTickFontSize(final int fontSize) { this.tickFontSize = fontSize; }

	public int getTickFontStyle() { return tickFontStyle; }
	public void setTickFontStyle(final int fontStyle) { this.tickFontStyle = fontStyle; }

	public String getLabelFontFace() { return labelFontFace; }
	public void setLabelFontFace(final String fontFace) { if (fontFace != null) this.labelFontFace = fontFace; }

	public int getLabelFontSize() { return labelFontSize; }
	public void setLabelFontSize(final int fontSize) { this.labelFontSize = fontSize; }

	public int getLabelFontStyle() { return labelFontStyle; }
	public void setLabelFontStyle(final int fontStyle) { this.labelFontStyle = fontStyle; }

	public String getLegendFontFace() { return legendFontFace; }
	public void setLegendFontFace(final String fontFace) { if (fontFace != null) this.legendFontFace = fontFace; }

	public int getLegendFontSize() { return legendFontSize; }
	public void setLegendFontSize(final int fontSize) { this.legendFontSize = fontSize; }

	public int getLegendFontStyle() { return legendFontStyle; }
	public void setLegendFontStyle(final int fontStyle) { this.legendFontStyle = fontStyle; }

	public String getTitleFontFace() { return titleFontFace; }
	public void setTitleFontFace(final String fontFace) { if (fontFace != null) this.titleFontFace = fontFace; }

	public int getTitleFontSize() { return titleFontSize; }
	public void setTitleFontSize(final int fontSize) { this.titleFontSize = fontSize; }

	public int getTitleFontStyle() { return titleFontStyle; }
	public void setTitleFontStyle(final int fontStyle) { this.titleFontStyle = fontStyle; }

	public String getSeriesLabelPosition() { return seriesLabelPosition; }
	public void setSeriesLabelPosition(final String pos) { this.seriesLabelPosition = pos; }

	public String getLegendOrientation() { return legendOrientation; }
	public void setLegendOrientation(final String orient) { this.legendOrientation = orient; }

	public IPoint getSeriesLabelAnchor() { return seriesLabelAnchor; }
	public void setSeriesLabelAnchor(final IPoint anchor) { this.seriesLabelAnchor = anchor; }

	public String getStyle() { return style; }
	public void setStyle(final String style) { this.style = style; }

	public double getGap() { return gap; }
	public void setGap(final double gap) { this.gap = gap; }

	public double getXRangeInterval() { return xRangeInterval; }
	public void setXRangeInterval(final double val) { this.useXRangeInterval = true; this.xRangeInterval = val; }
	public boolean isUseXRangeInterval() { return useXRangeInterval; }

	public void setXRangeMinMax(final double min, final double max) {
		this.useXRangeMinMax = true;
		this.xRangeMin = min;
		this.xRangeMax = max;
	}
	public boolean isUseXRangeMinMax() { return useXRangeMinMax; }
	public double getXRangeMin() { return xRangeMin; }
	public double getXRangeMax() { return xRangeMax; }

	public boolean isUseXMin() { return useXMin; }
	public void setXMin(final double min) { this.useXMin = true; this.xMinVal = min; }
	public double getXMinVal() { return xMinVal; }

	public boolean isUseXMax() { return useXMax; }
	public void setXMax(final double max) { this.useXMax = true; this.xMaxVal = max; }
	public double getXMaxVal() { return xMaxVal; }

	public double getYRangeInterval() { return yRangeInterval; }
	public void setYRangeInterval(final double val) { this.useYRangeInterval = true; this.yRangeInterval = val; }
	public boolean isUseYRangeInterval() { return useYRangeInterval; }

	public void setYRangeMinMax(final double min, final double max) {
		this.useYRangeMinMax = true;
		this.yRangeMin = min;
		this.yRangeMax = max;
	}
	public boolean isUseYRangeMinMax() { return useYRangeMinMax; }
	public double getYRangeMin() { return yRangeMin; }
	public double getYRangeMax() { return yRangeMax; }

	public boolean isUseYMin() { return useYMin; }
	public void setYMin(final double min) { this.useYMin = true; this.yMinVal = min; }
	public double getYMinVal() { return yMinVal; }

	public boolean isUseYMax() { return useYMax; }
	public void setYMax(final double max) { this.useYMax = true; this.yMaxVal = max; }
	public double getYMaxVal() { return yMaxVal; }

	public double getY2RangeInterval() { return y2RangeInterval; }
	public void setY2RangeInterval(final double val) { this.useY2RangeInterval = true; this.y2RangeInterval = val; }
	public boolean isUseY2RangeInterval() { return useY2RangeInterval; }

	public void setY2RangeMinMax(final double min, final double max) {
		this.useY2RangeMinMax = true;
		this.y2RangeMin = min;
		this.y2RangeMax = max;
	}
	public boolean isUseY2RangeMinMax() { return useY2RangeMinMax; }
	public double getY2RangeMin() { return y2RangeMin; }
	public double getY2RangeMax() { return y2RangeMax; }

	public double getXTickUnit() { return xTickUnit; }
	public void setXTickUnit(final double unit) { this.xTickUnit = unit; }

	public double getYTickUnit() { return yTickUnit; }
	public void setYTickUnit(final double unit) { this.yTickUnit = unit; }

	public double getY2TickUnit() { return y2TickUnit; }
	public void setY2TickUnit(final double unit) { this.y2TickUnit = unit; }

}
