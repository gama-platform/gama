/*******************************************************************************************************
 *
 * ChartJFreeChartOutput.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers.charts;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTitleAnnotation;
import org.jfree.chart.block.BlockBorder;
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
import org.jfree.data.general.Dataset;

import gama.core.common.interfaces.IKeyword;
import gama.core.outputs.display.AbstractDisplayGraphics;
import gama.core.runtime.IScope;
import gama.core.util.GamaColor;
import gama.gaml.expressions.IExpression;
import gama.gaml.operators.Cast;
import gama.gaml.operators.Colors;

/**
 * The Class ChartJFreeChartOutput.
 */
public class ChartJFreeChartOutput extends ChartOutput implements ChartProgressListener {

	/** The lock. */
	Object lock = new Object();

	/** The Constant defaultmarkers. */
	public static final Shape[] defaultmarkers =
			org.jfree.chart.plot.DefaultDrawingSupplier.createStandardSeriesShapes();

	/** The old anti alias. */
	boolean oldAntiAlias;

	/** The info. */
	final public ChartRenderingInfo info;

	/** The jfreedataset. */
	final List<Dataset> jfreedataset = new ArrayList<>();

	/** The chart. */
	JFreeChart chart = null;

	/** The area. */
	final Rectangle2D area = new Rectangle2D.Double();

	/** The cache. */
	BufferedImage frontImage, backImage;

	/** The defaultrenderer. */
	AbstractRenderer defaultrenderer;

	/** The Id position. */
	final HashMap<String, Integer> idPosition = new HashMap<>();

	/** The renderer set. */
	final HashMap<String, AbstractRenderer> rendererSet = new HashMap<>();

	/** The nbseries. */
	int nbseries = 0;

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
		info = new ChartRenderingInfo();
	}

	/**
	 * Creates the chart output.
	 *
	 * @param scope
	 *            the scope
	 * @param name
	 *            the name
	 * @param typeexp
	 *            the typeexp
	 * @return the chart J free chart output
	 */
	public static ChartJFreeChartOutput createChartOutput(final IScope scope, final String name,
			final IExpression typeexp) {

		final IExpression string1 = typeexp;
		if (string1 != null) {
			final String t = Cast.asString(scope, string1.value(scope));
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
	public BufferedImage getImage(final int sizeX, final int sizeY, final boolean antiAlias) {
		adjustImage(sizeX, sizeY, antiAlias);

		final Graphics2D g2D = backImage.createGraphics();
		try {

			synchronized (lock) {
				chart.draw(g2D, area, info);
			}
		} catch (IndexOutOfBoundsException | IllegalArgumentException | NullPointerException e) {
			// Do nothing. See #1605
			// e.printStackTrace();
			// Should we force redrawing in case of error ? See #3442
		} finally {
			g2D.dispose();
		}
		return frontImage;

	}

	/**
	 * Chart progress.
	 *
	 * @param event
	 *            the event
	 */
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

	@Override
	public void initChart(final IScope scope, final String chartname) {
		super.initChart(scope, chartname);

		initRenderer(scope);
		final Plot plot = chart.getPlot();
		chart.addProgressListener(this);
		chart.setBorderVisible(false);
		plot.setOutlineVisible(false);
		chart.setTitle(this.getName());
		chart.getTitle().setVisible(true);
		chart.getTitle().setFont(getTitleFont());
		if (!this.getTitleVisible(scope)) { chart.getTitle().setVisible(false); }
		if (textColor != null) { chart.getTitle().setPaint(textColor); }

		if (backgroundColor == null) {
			plot.setBackgroundPaint(null);
			chart.setBackgroundPaint(null);
			chart.setBorderPaint(null);
			if (chart.getLegend() != null) { chart.getLegend().setBackgroundPaint(null); }
		} else {
			final Color bg = backgroundColor;
			chart.setBackgroundPaint(bg);
			plot.setBackgroundPaint(bg);
			chart.setBorderPaint(bg);
			if (chart.getLegend() != null) { chart.getLegend().setBackgroundPaint(bg); }
		}
		if (chart.getLegend() != null) {
			LegendTitle legend = chart.getLegend(); // Get the existing legend
			legend.setItemFont(getLegendFont());
			legend.setFrame(BlockBorder.NONE);
			legend.setPosition(RectangleEdge.BOTTOM);
			// legend.setPadding(5, 5, 5, 5);
			// Set legend position
			switch (series_label_position) {
				case IKeyword.LEFT:
					legend.setPosition(RectangleEdge.LEFT);
					break;
				case IKeyword.RIGHT:
					legend.setPosition(RectangleEdge.RIGHT);
					break;
				case IKeyword.TOP:
					legend.setPosition(RectangleEdge.TOP);
					break;
				case "none":
					legend.setVisible(false);
					break;
				case "onchart":
					if (plot instanceof XYPlot p) {
						// Place the legend inside the chart area at the corner specified by the anchor
						double x = series_label_anchor.x / 2 + 0.25; // Normalize to [0.25, 0.75]
						double y = series_label_anchor.y / 2 + 0.25;
						XYTitleAnnotation ta = new XYTitleAnnotation(x, y, legend, RectangleAnchor.CENTER);
						ta.setMaxWidth(0.5); // Legend will take up to 50% of the chart width by default
						ta.setMaxHeight(0.5);
						legend.setHorizontalAlignment(HorizontalAlignment.CENTER);
						legend.setVerticalAlignment(VerticalAlignment.CENTER);
						// Legend with 50% transparency by default
						legend.setBackgroundPaint(Colors.rgb(scope, GamaColor.get(backgroundColor), 0.5));
						p.addAnnotation(ta);
						// Remove the default legend
						chart.removeLegend();
					}
			}

			// Set legend text color
			if (textColor != null) { legend.setItemPaint(textColor); }

		}

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
	Font getLabelFont() { return new Font(labelFontFace, labelFontStyle, labelFontSize); }

	/**
	 * Gets the tick font.
	 *
	 * @return the tick font
	 */
	Font getTickFont() { return new Font(tickFontFace, tickFontStyle, tickFontSize); }

	/**
	 * Gets the legend font.
	 *
	 * @return the legend font
	 */
	Font getLegendFont() { return new Font(legendFontFace, legendFontStyle, legendFontSize); }

	/**
	 * Gets the title font.
	 *
	 * @return the title font
	 */
	Font getTitleFont() { return new Font(titleFontFace, titleFontStyle, titleFontSize); }

	@Override
	public JFreeChart getJFChart() { return chart; }

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
