/*******************************************************************************************************
 *
 * GMLFileViewer.java, in gama.ui.viewers, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.viewers.gis;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.style.FeatureTypeStyle;
import org.geotools.api.style.Fill;
import org.geotools.api.style.LineSymbolizer;
import org.geotools.api.style.PointSymbolizer;
import org.geotools.api.style.PolygonSymbolizer;
import org.geotools.api.style.Stroke;
import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.MapContent;
import org.geotools.map.StyleLayer;
import org.geotools.styling.SLD;
import org.geotools.styling.StyleBuilder;
import org.geotools.wfs.GML;
import org.geotools.wfs.GML.Version;

import gama.api.GAMA;
import gama.api.data.objects.IColor;
import gama.core.util.file.GMLInfo;
import gama.ui.shared.controls.FlatButton;
import gama.ui.shared.menus.GamaMenu;
import gama.ui.shared.resources.GamaColors;
import gama.ui.shared.resources.GamaColors.GamaUIColor;
import gama.ui.shared.utils.PreferencesHelper;
import gama.ui.shared.views.toolbar.Selector;
import gama.ui.viewers.gis.geotools.styling.Mode;
import gama.ui.viewers.gis.geotools.styling.SLDs;
import gama.ui.viewers.gis.geotools.styling.Utils;

/**
 * The Class GMLFileViewer.
 */
public class GMLFileViewer extends GISFileViewer {

	/** The mode. */
	Mode mode;

	/** The fts. */
	FeatureTypeStyle fts;

	/** The attributes. */
	Map<String, String> attributes;

	@Override
	public void init(final IEditorSite site, final IEditorInput input) throws PartInitException {
		setSite(site);
		final FileEditorInput fi = (FileEditorInput) input;
		file = fi.getFile();
		final IPath path = fi.getPath();
		final File f = path.makeAbsolute().toFile();
		try {
			pathStr = f.getAbsolutePath();

			// Load GML
			final GML gml = new GML(Version.GML3);
			SimpleFeatureCollection collection = gml.decodeFeatureCollection(new FileInputStream(f));

			content = new MapContent();
			featureSource = DataUtilities.source(collection);
			style = Utils.createStyle2(featureSource);
			layer = new FeatureLayer(featureSource, style);
			mode = determineMode(featureSource.getSchema(), "Polygon");
			final List<FeatureTypeStyle> ftsList = style.featureTypeStyles();
			if (ftsList.size() > 0) {
				fts = ftsList.get(0);
			} else {
				fts = null;
			}
			if (fts != null) {
				this.setFillColor(IColor.toAWTColor(PreferencesHelper.SHAPEFILE_VIEWER_FILL.getValue()), mode, fts);
				this.setStrokeColor(IColor.toAWTColor(PreferencesHelper.SHAPEFILE_VIEWER_LINE_COLOR.getValue()), mode,
						fts);
				((StyleLayer) layer).setStyle(style);
			}

			content.addLayer(layer);

			// Load attributes from metadata
			GMLInfo info = (GMLInfo) GAMA.getMetadataProvider().getMetaData(file, false, true);
			if (info != null) { attributes = info.getAttributes(); }

		} catch (final Exception e) {
			e.printStackTrace();
		}
		this.setPartName(path.lastSegment());
		setInput(input);
	}

	@Override
	protected void displayInfoString() {
		String s = "";
		try {
			s = featureSource.getFeatures().size() + " objects | "
					+ (int) (featureSource.getBounds().getWidth() * (Math.PI / 180) * 6378137) + "m x "
					+ (int) (featureSource.getBounds().getHeight() * (Math.PI / 180) * 6378137) + "m";
		} catch (final IOException e1) {
			e1.printStackTrace();
		}
		final ToolItem item = toolbar.status(s);

		((FlatButton) item.getControl()).setSelectionListener(new Selector() {

			Menu menu;

			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (menu == null) {
					menu = new Menu(toolbar.getShell(), SWT.POP_UP);
					fillMenu();
				}
				final Point point = toolbar.toDisplay(new Point(e.x, e.y + toolbar.getSize().y));
				menu.setLocation(point.x, point.y);
				menu.setVisible(true);
			}

			private void fillMenu() {
				GamaMenu.separate(menu, "Bounds");
				try {
					final ReferencedEnvelope env = featureSource.getBounds();
					MenuItem m2 = new MenuItem(menu, SWT.NONE);
					m2.setEnabled(false);
					m2.setText("     - upper corner : " + env.getUpperCorner().getOrdinate(0) + " "
							+ env.getUpperCorner().getOrdinate(1));
					m2 = new MenuItem(menu, SWT.NONE);
					m2.setEnabled(false);
					m2.setText("     - lower corner : " + env.getLowerCorner().getOrdinate(0) + " "
							+ env.getLowerCorner().getOrdinate(1));

					m2 = new MenuItem(menu, SWT.NONE);
					m2.setEnabled(false);
					// approximation
					m2.setText("     - dimensions : " + (int) (env.getWidth() * (Math.PI / 180) * 6378137) + "m x "
							+ (int) (env.getHeight() * (Math.PI / 180) * 6378137) + "m");
				} catch (final Exception e) {
					e.printStackTrace();
				}
				GamaMenu.separate(menu);
				if (attributes != null && !attributes.isEmpty()) {
					GamaMenu.separate(menu, "Attributes");
					try {
						final List<String> atts = new ArrayList<>(attributes.keySet());
						Collections.sort(atts);
						for (final String att : atts) {
							final MenuItem m2 = new MenuItem(menu, SWT.NONE);
							m2.setEnabled(false);
							m2.setText("       - " + att + " (" + attributes.get(att) + ")");
						}
					} catch (final Exception e) {
						e.printStackTrace();
					}
				}

			}

		});

	}

	@Override
	public void saveAsCSV() {
		// TODO: Implement CSV export for GML if needed, similar to ShapeFileViewer
	}

	@Override
	public String[] getColorLabels() {
		if (mode == Mode.POLYGON || mode == Mode.ALL) return new String[] { "Set line color...", "Set fill color..." };
		return new String[] { "Set line color..." };
	}

	@Override
	public GamaUIColor getColor(final int index) {
		Color c;
		if (index == 0) {
			c = SLD.color(getStroke(mode, fts));
		} else {
			c = SLD.color(getFill(mode, fts));
		}
		return GamaColors.get(c);
	}

	@Override
	public void setColor(final int index, final GamaUIColor gc) {
		final RGB rgb = gc.getRGB();
		final Color c = new Color(rgb.red, rgb.green, rgb.blue);
		if (index == 0) {
			setStrokeColor(c, mode, fts);
		} else {
			setFillColor(c, mode, fts);
		}
		((StyleLayer) layer).setStyle(style);
		pane.redraw();
	}

	/**
	 * Sets the fill color.
	 *
	 * @param color
	 *            the color
	 * @param mode
	 *            the mode
	 * @param fts
	 *            the fts
	 */
	public void setFillColor(final Color color, final Mode mode, final FeatureTypeStyle fts) {
		if (mode == Mode.POLYGON) {
			final PolygonSymbolizer sym = SLD.polySymbolizer(fts);
			final Fill s = new StyleBuilder().createFill(color);
			sym.setFill(s);
		} else if (mode == Mode.POINT || mode == Mode.ALL) { // default to
			// handling as
			// Point
			final PointSymbolizer sym = SLD.pointSymbolizer(fts);
			SLD.setPointColour(sym, color);
		}
	}

	/**
	 * Sets the stroke color.
	 *
	 * @param color
	 *            the color
	 * @param mode
	 *            the mode
	 * @param fts
	 *            the fts
	 */
	public void setStrokeColor(final Color color, final Mode mode, final FeatureTypeStyle fts) {
		switch (mode) {
			case LINE: {
				final LineSymbolizer sym = SLD.lineSymbolizer(fts);
				SLD.setLineColour(sym, color);
				break;
			}
			case POLYGON: {
				final PolygonSymbolizer sym = SLD.polySymbolizer(fts);
				final Stroke s = new StyleBuilder().createStroke(color);
				sym.setStroke(s);
				break;
			}
			case POINT:
			case ALL: {
				// handling as
				// Point
				final PointSymbolizer sym = SLD.pointSymbolizer(fts);
				SLD.setPointColour(sym, color);
				break;
			}
			case null:
			default:
				break;
		}
	}

	/**
	 * Gets the stroke.
	 *
	 * @param mode
	 *            the mode
	 * @param fts
	 *            the fts
	 * @return the stroke
	 */
	public Stroke getStroke(final Mode mode, final FeatureTypeStyle fts) {
		// Stroke stroke = null;
		switch (mode) {
			case LINE: {
				final LineSymbolizer sym = SLD.lineSymbolizer(fts);
				return SLD.stroke(sym);
			}
			case POLYGON: {
				final PolygonSymbolizer sym = SLD.polySymbolizer(fts);
				return SLD.stroke(sym);
			}
			case POINT:
			case ALL: {
				// handling as
				// Point
				final PointSymbolizer sym = SLD.pointSymbolizer(fts);
				return SLD.stroke(sym);
			}
			case null:
			default:
				break;
		}
		return new StyleBuilder().createStroke();
	}

	/**
	 * Gets the fill.
	 *
	 * @param mode
	 *            the mode
	 * @param fts
	 *            the fts
	 * @return the fill
	 */
	public Fill getFill(final Mode mode, final FeatureTypeStyle fts) {
		if (mode == Mode.POLYGON) {
			final PolygonSymbolizer sym = SLD.polySymbolizer(fts);
			return SLD.fill(sym);
		}
		if (mode == Mode.POINT || mode == Mode.ALL) { // default to
			// handling as
			// Point
			final PointSymbolizer sym = SLD.pointSymbolizer(fts);
			return SLD.fill(sym);
		}
		return new StyleBuilder().createFill();
	}

	/**
	 * Determine mode.
	 *
	 * @param schema
	 *            the schema
	 * @param def
	 *            the def
	 * @return the mode
	 */
	public Mode determineMode(final SimpleFeatureType schema, final String def) {
		if (schema == null) return Mode.NONE;
		if (SLDs.isLine(schema)) return Mode.LINE;
		if (SLDs.isPolygon(schema)) return Mode.POLYGON;
		if (SLDs.isPoint(schema)) return Mode.POINT;
		switch (def) {
			case "Polygon":
				return Mode.POLYGON;
			case "Line":
				return Mode.LINE;
			case "Point":
				return Mode.POINT;
			case null:
			default:
				break;
		}
		return Mode.ALL; // we are a generic geometry
	}

}
