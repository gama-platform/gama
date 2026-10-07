/*******************************************************************************************************
 *
 * ChartJFreeChartOutput.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers.charts;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTitleAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.PieSectionEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.event.ChartProgressEvent;
import org.jfree.chart.event.ChartProgressListener;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.HorizontalAlignment;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.VerticalAlignment;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.xy.XYDataset;

import gama.annotations.constants.IKeyword;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.Cast;
import gama.api.runtime.scope.IScope;
import gama.api.types.color.IColor;
import gama.api.ui.displays.IDisplaySurface;
import gama.core.outputs.display.AbstractDisplayGraphics;
import gama.gaml.operators.Colors;

/**
 * Base JFreeChart implementation of ChartOutput.
 */
public class ChartJFreeChartOutput extends ChartOutput implements ChartProgressListener {

	/** The lock. */
	protected final Object lock = new Object();

	/** The Constant defaultmarkers. */
	public static final Shape[] defaultmarkers =
			org.jfree.chart.plot.DefaultDrawingSupplier.createStandardSeriesShapes();

	/** The old anti alias. */
	protected boolean oldAntiAlias;

	/** The info. */
	public final ChartRenderingInfo info = new ChartRenderingInfo();

	/** The jfreedataset. */
	protected final List<Dataset> jfreedataset = new ArrayList<>();

	/** The chart. */
	protected JFreeChart chart = null;

	/** The area. */
	protected final Rectangle2D area = new Rectangle2D.Double();

	/** The back image. */
	protected BufferedImage frontImage, backImage;

	/** The defaultrenderer. */
	protected AbstractRenderer defaultrenderer;

	/** The id position. */
	protected final HashMap<String, Integer> idPosition = new HashMap<>();

	/** The renderer set. */
	protected final HashMap<String, AbstractRenderer> rendererSet = new HashMap<>();

	/** The nbseries. */
	protected int nbseries = 0;

	/** The Constant STROKE_CACHE. */
	private static final java.util.concurrent.ConcurrentHashMap<Float, java.awt.BasicStroke> STROKE_CACHE =
			new java.util.concurrent.ConcurrentHashMap<>();

	/**
	 * Gets the stroke.
	 *
	 * @param thickness
	 *            the thickness
	 * @return the stroke
	 */
	public static java.awt.BasicStroke getStroke(final float thickness) {
		return STROKE_CACHE.computeIfAbsent(thickness,
				t -> new java.awt.BasicStroke(t, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
	}

	/**
	 * Instantiates a new chart J free chart output.
	 *
	 * @param scope
	 *            the scope
	 * @param name
	 *            the name
	 * @param typeexp
	 *            the typeexp
	 */
	public ChartJFreeChartOutput(final IScope scope, final String name, final IExpression typeexp) {
		super(scope, name, typeexp);
	}

	/**
	 * Factory method to instantiate specific JFreeChart chart output based on type.
	 */
	public static ChartJFreeChartOutput createChartOutput(final IScope scope, final String name,
			final IExpression typeexp) {
		if (typeexp != null) {
			final String t = Cast.asString(scope, typeexp.value(scope));
			return switch (t) {
				case IKeyword.HISTOGRAM -> new ChartJFreeChartOutputHistogram(scope, name, typeexp);
				case IKeyword.PIE -> new ChartJFreeChartOutputPie(scope, name, typeexp);
				case IKeyword.RADAR -> new ChartJFreeChartOutputRadar(scope, name, typeexp);
				case IKeyword.HEATMAP -> new ChartJFreeChartOutputHeatmap(scope, name, typeexp);
				case IKeyword.BOX_WHISKER -> new ChartJFreeChartOutputBoxAndWhiskerCategory(scope, name, typeexp);
				default -> new ChartJFreeChartOutputScatter(scope, name, typeexp);
			};
		}
		return new ChartJFreeChartOutputScatter(scope, name, typeexp);
	}

	@Override
	public Object getNativeChart() { return chart; }

	@Override
	public BufferedImage getImage(final int sizeX, final int sizeY, final boolean antiAlias) {
		if (chart == null) return null;
		adjustImage(sizeX, sizeY, antiAlias);

		final Graphics2D g2D = backImage.createGraphics();
		try {
			if (antiAlias) {
				g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				g2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
				g2D.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
			}
			synchronized (lock) {
				chart.draw(g2D, area, null);
			}
		} catch (IndexOutOfBoundsException | IllegalArgumentException | NullPointerException e) {
			// Ignore transient render errors during dataset updates
		} finally {
			g2D.dispose();
		}
		return frontImage;
	}

	@Override
	public void chartProgress(final ChartProgressEvent event) {
		if (event.getType() == ChartProgressEvent.DRAWING_FINISHED) {
			synchronized (lock) {
				BufferedImage bi = backImage;
				backImage = frontImage;
				frontImage = bi;
			}
		}
	}

	/**
	 * Adjust image.
	 *
	 * @param sizeX
	 *            the size X
	 * @param sizeY
	 *            the size Y
	 * @param antiAlias
	 *            the anti alias
	 */
	private void adjustImage(final int sizeX, final int sizeY, final boolean antiAlias) {
		if (antiAlias != oldAntiAlias) {
			oldAntiAlias = antiAlias;
			chart.setAntiAlias(antiAlias);
			chart.setTextAntiAlias(antiAlias);
		}
		if ((int) area.getWidth() != sizeX || (int) area.getHeight() != sizeY) {
			area.setRect(0, 0, sizeX, sizeY);
			frontImage = AbstractDisplayGraphics.createCompatibleImage(sizeX, sizeY);
			backImage = AbstractDisplayGraphics.createCompatibleImage(sizeX, sizeY);
		}
	}

	@Override
	public void setBackgroundColorValue(final IScope scope, final IColor color) {
		super.setBackgroundColorValue(scope, color);
		if (chart != null) { configureChartBackgrounds(); }
	}

	@Override
	public void updateOutput(final IScope scope) {
		if (chart != null) {
			chart.setNotify(false);
			configureChartBackgrounds();
		}
		try {
			super.updateOutput(scope);
			updateLegendVisibility(scope);
		} finally {
			if (chart != null) { chart.setNotify(true); }
		}
	}

	@Override
	public void step(final IScope scope) {
		synchronized (lock) {
			super.step(scope);
		}
	}

	/**
	 * Inits the renderer.
	 *
	 * @param scope
	 *            the scope
	 */
	protected void initRenderer(final IScope scope) {}

	/**
	 * Configure chart title.
	 */
	private void configureChartTitle() {
		chart.setTitle(this.getName());
		if (chart.getTitle() != null) {
			chart.getTitle().setVisible(properties.isTitleVisible());
			chart.getTitle().setFont(properties.getTitleFont());
			if (properties.getTextColor() != null) {
				chart.getTitle().setPaint(IColor.toAWTColor(properties.getTextColor()));
			}
		}
	}

	/**
	 * Configure chart backgrounds.
	 */
	private void configureChartBackgrounds() {
		Plot plot = chart.getPlot();
		if (properties.getBackgroundColor() == null) {
			plot.setBackgroundPaint(null);
			chart.setBackgroundPaint(null);
			chart.setBorderPaint(null);
			if (chart.getLegend() != null) { chart.getLegend().setBackgroundPaint(null); }
		} else {
			final Color bg = IColor.toAWTColor(properties.getBackgroundColor());
			chart.setBackgroundPaint(bg);
			plot.setBackgroundPaint(bg);
			chart.setBorderPaint(bg);
			if (chart.getLegend() != null) { chart.getLegend().setBackgroundPaint(bg); }
		}
	}

	/**
	 * Configure chart legend.
	 *
	 * @param scope
	 *            the scope
	 */
	private void configureChartLegend(final IScope scope) {
		if (chart.getLegend() == null) return;
		LegendTitle legend = chart.getLegend();
		legend.setItemFont(properties.getLegendFont());
		legend.setFrame(BlockBorder.NONE);
		legend.setPosition(RectangleEdge.BOTTOM);

		configureLegendPosition(legend, chart.getPlot(), scope);
		configureLegendOrientation(legend);

		if (properties.getTextColor() != null) { legend.setItemPaint(IColor.toAWTColor(properties.getTextColor())); }
	}

	@Override
	public void initChart(final IScope scope, final String chartname) {
		super.initChart(scope, chartname);
		if (chart == null) return;

		initRenderer(scope);
		chart.addProgressListener(this);
		chart.setBorderVisible(false);
		chart.getPlot().setOutlineVisible(false);

		configureChartTitle();
		configureChartBackgrounds();
		configureChartLegend(scope);
	}

	/**
	 * Configure legend position.
	 *
	 * @param legend
	 *            the legend
	 * @param plot
	 *            the plot
	 * @param scope
	 *            the scope
	 */
	protected void configureLegendPosition(final LegendTitle legend, final Plot plot, final IScope scope) {
		switch (properties.getSeriesLabelPosition()) {
			case IKeyword.LEFT -> legend.setPosition(RectangleEdge.LEFT);
			case IKeyword.RIGHT -> legend.setPosition(RectangleEdge.RIGHT);
			case IKeyword.TOP -> legend.setPosition(RectangleEdge.TOP);
			case "none" -> legend.setVisible(false);
			case "onchart" -> {
				if (plot instanceof XYPlot p) {
					double x = properties.getSeriesLabelAnchor().getX() / 2 + 0.25;
					double y = properties.getSeriesLabelAnchor().getY() / 2 + 0.25;
					XYTitleAnnotation ta = new XYTitleAnnotation(x, y, legend, RectangleAnchor.CENTER);
					ta.setMaxWidth(0.5);
					ta.setMaxHeight(0.5);
					legend.setHorizontalAlignment(HorizontalAlignment.CENTER);
					legend.setVerticalAlignment(VerticalAlignment.CENTER);
					legend.setBackgroundPaint(
							IColor.toAWTColor(Colors.rgb(scope, properties.getBackgroundColor(), 0.5)));
					p.addAnnotation(ta);
					chart.removeLegend();
				}
			}
			default -> {
			}
		}
	}

	/**
	 * Configure legend orientation.
	 *
	 * @param legend
	 *            the legend
	 */
	protected void configureLegendOrientation(final LegendTitle legend) {
		if (legend == null) return;
		if ("vertical".equalsIgnoreCase(properties.getLegendOrientation())) {
			legend.getItemContainer().setArrangement(new org.jfree.chart.block.ColumnArrangement());
		} else if ("horizontal".equalsIgnoreCase(properties.getLegendOrientation())) {
			legend.getItemContainer().setArrangement(new org.jfree.chart.block.FlowArrangement());
		}
	}

	@Override
	public void setLegendOrientation(final IScope scope, final String orient) {
		super.setLegendOrientation(scope, orient);
		if (chart != null && chart.getLegend() != null) { configureLegendOrientation(chart.getLegend()); }
	}

	/**
	 * Update legend visibility.
	 *
	 * @param scope
	 *            the scope
	 */
	private void updateLegendVisibility(final IScope scope) {
		if (chart == null || chartdataset == null || chart.getLegend() == null) return;

		final String position = properties.getSeriesLabelPosition();
		if ("none".equals(position) || "xaxis".equals(position) || "yaxis".equals(position)) {
			chart.getLegend().setVisible(false);
			return;
		}

		for (final String serieid : chartdataset.getDataSeriesIds(scope)) {
			final ChartDataSeries serie = chartdataset.getDataSeries(scope, serieid);
			if (serie != null) {
				final Comparable label = serie.getSerieLegend(scope);
				if (label != null && StringUtils.isNotBlank(label.toString())) {
					chart.getLegend().setVisible(true);
					return;
				}
			}
		}
		chart.getLegend().setVisible(false);
	}

	/**
	 * Gets the or create renderer.
	 *
	 * @param scope
	 *            the scope
	 * @param serieid
	 *            the serieid
	 * @return the or create renderer
	 */
	AbstractRenderer getOrCreateRenderer(final IScope scope, final String serieid) {
		if (rendererSet.containsKey(serieid)) return rendererSet.get(serieid);
		final AbstractRenderer newrenderer = createRenderer(scope, serieid);
		rendererSet.put(serieid, newrenderer);
		return newrenderer;
	}

	/**
	 * Creates the renderer.
	 *
	 * @param scope
	 *            the scope
	 * @param serieid
	 *            the serieid
	 * @return the abstract renderer
	 */
	protected AbstractRenderer createRenderer(final IScope scope, final String serieid) {
		return new XYErrorRenderer();
	}

	/**
	 * Gets the label font.
	 *
	 * @return the label font
	 */
	Font getLabelFont() { return properties.getLabelFont(); }

	/**
	 * Gets the tick font.
	 *
	 * @return the tick font
	 */
	Font getTickFont() { return properties.getTickFont(); }

	/**
	 * Gets the legend font.
	 *
	 * @return the legend font
	 */
	Font getLegendFont() { return properties.getLegendFont(); }

	/**
	 * Gets the title font.
	 *
	 * @return the title font
	 */
	Font getTitleFont() { return properties.getTitleFont(); }

	/**
	 * Apply X single bounds.
	 *
	 * @param scope
	 *            the scope
	 * @param axis
	 *            the axis
	 */
	protected void applyXSingleBounds(final IScope scope, final NumberAxis axis) {
		if (axis == null) return;
		axis.setAutoRange(true);
		double autoMin = axis.getRange().getLowerBound();
		double autoMax = axis.getRange().getUpperBound();
		double newMin = properties.isUseXMin() ? properties.getXMinVal() : autoMin;
		double newMax = properties.isUseXMax() ? properties.getXMaxVal() : autoMax;
		if (newMax > newMin) { axis.setRange(newMin, newMax); }
	}

	/**
	 * Apply Y single bounds.
	 *
	 * @param scope
	 *            the scope
	 * @param axis
	 *            the axis
	 */
	protected void applyYSingleBounds(final IScope scope, final NumberAxis axis) {
		if (axis == null) return;
		axis.setAutoRange(true);
		double autoMin = axis.getRange().getLowerBound();
		double autoMax = axis.getRange().getUpperBound();
		double newMin = properties.isUseYMin() ? properties.getYMinVal() : autoMin;
		double newMax = properties.isUseYMax() ? properties.getYMaxVal() : autoMax;
		if (newMax > newMin) { axis.setRange(newMin, newMax); }
	}

	@Override
	public void getModelCoordinatesInfo(final int xOnScreen, final int yOnScreen, final IDisplaySurface g,
			final Point positionInPixels, final StringBuilder sb) {
		if (info == null || info.getEntityCollection() == null) return;
		final int x = xOnScreen - positionInPixels.x;
		final int y = yOnScreen - positionInPixels.y;
		final ChartEntity entity = info.getEntityCollection().getEntity(x, y);
		switch (entity) {
			case XYItemEntity xy -> {
				final XYDataset data = xy.getDataset();
				final int index = xy.getItem();
				final int series = xy.getSeriesIndex();
				final double xx = data.getXValue(series, index);
				final double yy = data.getYValue(series, index);
				final XYPlot plot = (XYPlot) getJFChart().getPlot();
				final ValueAxis xAxis = plot.getDomainAxis(series);
				final ValueAxis yAxis = plot.getRangeAxis(series);
				final boolean xInt = xx % 1 == 0;
				final boolean yInt = yy % 1 == 0;
				String xTitle = xAxis != null ? xAxis.getLabel() : "X";
				if (StringUtils.isBlank(xTitle)) { xTitle = "X"; }
				String yTitle = yAxis != null ? yAxis.getLabel() : "Y";
				if (StringUtils.isBlank(yTitle)) { yTitle = "Y"; }
				sb.append(xTitle).append(" ").append(xInt ? (int) xx : String.format("%.2f", xx));
				sb.append(" | ").append(yTitle).append(" ").append(yInt ? (int) yy : String.format("%.2f", yy));
			}
			case PieSectionEntity ps -> {
				final String title = ps.getSectionKey().toString();
				final PieDataset<?> data = ps.getDataset();
				final int index = ps.getSectionIndex();
				final double xx = data.getValue(index).doubleValue();
				final boolean xInt = xx % 1 == 0;
				sb.append(title).append(" ").append(xInt ? (int) xx : String.format("%.2f", xx));
			}
			case CategoryItemEntity ci -> {
				final Comparable<?> columnKey = ci.getColumnKey();
				final String title = columnKey.toString();
				final CategoryDataset data = ci.getDataset();
				final Comparable<?> rowKey = ci.getRowKey();
				final double xx = data.getValue(rowKey, columnKey).doubleValue();
				final boolean xInt = xx % 1 == 0;
				sb.append(title).append(" ").append(xInt ? (int) xx : String.format("%.2f", xx));
			}
			case null, default -> {
			}
		}
	}

	@Override
	public void dispose(final IScope scope) {
		if (frontImage != null) { frontImage.flush(); }
		if (backImage != null) { backImage.flush(); }
		backImage = null;
		frontImage = null;
		clearDataSet(scope);
		jfreedataset.clear();
		chart = null;
	}

}
