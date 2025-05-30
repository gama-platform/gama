/*******************************************************************************************************
 *
 * ShapeFileViewer.java, in gama.ui.viewers, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.viewers.gis;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
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
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.MapContent;
import org.geotools.map.StyleLayer;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.SLD;
import org.geotools.styling.Stroke;
import org.geotools.styling.StyleBuilder;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.PropertyDescriptor;

import gama.core.metamodel.topology.projection.ProjectionFactory;
import gama.core.runtime.GAMA;
import gama.core.util.file.GamaShapeFile;
import gama.core.util.file.GamaShapeFile.ShapeInfo;
import gama.dev.DEBUG;
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
 * The Class ShapeFileViewer.
 */
public class ShapeFileViewer extends GISFileViewer {

    /** The mode. */
    Mode mode;

    /** The fts. */
    FeatureTypeStyle fts;

    @Override
    public void init(final IEditorSite site, final IEditorInput input) throws PartInitException {
	setSite(site);
	final FileEditorInput fi = (FileEditorInput) input;
	file = fi.getFile();
	final IPath path = fi.getPath();
	final File f = path.makeAbsolute().toFile();
	try {
	    pathStr = f.getAbsolutePath();
	    final ShapefileDataStore store = new ShapefileDataStore(f.toURI().toURL());
	    store.setCharset(Charset.forName("UTF8"));
	    content = new MapContent();
	    featureSource = store.getFeatureSource();
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
		this.setFillColor(PreferencesHelper.SHAPEFILE_VIEWER_FILL.getValue(), mode, fts);
		this.setStrokeColor(PreferencesHelper.SHAPEFILE_VIEWER_LINE_COLOR.getValue(), mode, fts);
		((StyleLayer) layer).setStyle(style);
	    }
	    content.addLayer(layer);
	} catch (final IOException e) {
	    DEBUG.ERR("Unable to view file " + path);
	}
	this.setPartName(path.lastSegment());
	setInput(input);
    }

    @Override
    protected void displayInfoString() {
	String s;
	final GamaShapeFile.ShapeInfo info = (ShapeInfo) GAMA.getGui().getMetaDataProvider().getMetaData(file, false,
		true);
	if (info == null) {
	    s = "Error in reading file information";
	} else {
	    s = info.getSuffix();
	    if (info.getCRS() == null) {
		noCRS = true;
	    } else {
	    }
	}

	final ToolItem item = toolbar.status(s);
	if (info != null) {
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
			ReferencedEnvelope env = featureSource.getBounds();
			MenuItem m2 = new MenuItem(menu, SWT.NONE);
			m2.setEnabled(false);
			m2.setText("     - upper corner : " + env.getUpperCorner().getOrdinate(0) + " "
				+ env.getUpperCorner().getOrdinate(1));
			m2 = new MenuItem(menu, SWT.NONE);
			m2.setEnabled(false);
			m2.setText("     - lower corner : " + env.getLowerCorner().getOrdinate(0) + " "
				+ env.getLowerCorner().getOrdinate(1));
			if (!noCRS) {
			    env = env.transform(new ProjectionFactory().getTargetCRS(GAMA.getRuntimeScope()), true);
			}
			m2 = new MenuItem(menu, SWT.NONE);
			m2.setEnabled(false);
			m2.setText(
				"     - dimensions : " + (int) env.getWidth() + "m x " + (int) env.getHeight() + "m");
		    } catch (final Exception e) {
			e.printStackTrace();
		    }
		    GamaMenu.separate(menu);
		    GamaMenu.separate(menu, "Attributes");
		    try {
			for (final Map.Entry<String, String> entry : info.getAttributes().entrySet()) {
			    final MenuItem m2 = new MenuItem(menu, SWT.NONE);
			    m2.setEnabled(false);
			    m2.setText("     - " + entry.getKey() + " (" + entry.getValue() + ")");
			}
			// java.util.List<AttributeDescriptor> att_list =
			// store.getSchema().getAttributeDescriptors();
		    } catch (final Exception e) {
			e.printStackTrace();
		    }

		}

	    });
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
     * Determine mode.
     *
     * @param schema
     *            the schema
     * @param def
     *            the def
     * @return the mode
     */
    public Mode determineMode(final SimpleFeatureType schema, final String def) {
	if (schema == null) {
	    return Mode.NONE;
	}
	if (SLDs.isLine(schema)) {
	    return Mode.LINE;
	}
	if (SLDs.isPolygon(schema)) {
	    return Mode.POLYGON;
	}
	if (SLDs.isPoint(schema)) {
	    return Mode.POINT;
	}
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

    /**
     * Method getColorLabels()
     *
     * @see gama.ui.shared.views.toolbar.IToolbarDecoratedView.Colorizable#getColorLabels()
     */
    @Override
    public String[] getColorLabels() {
	if (mode == Mode.POLYGON || mode == Mode.ALL) {
	    return new String[] { "Set line color...", "Set fill color..." };
	}
	return new String[] { "Set line color..." };
    }

    /**
     * Method getColor()
     *
     * @see gama.ui.shared.views.toolbar.IToolbarDecoratedView.Colorizable#getColor(int)
     */
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

    /**
     * Method setColor()
     *
     * @see gama.ui.shared.views.toolbar.IToolbarDecoratedView.Colorizable#setColor(int,
     *      gama.ui.shared.resources.GamaColors.GamaUIColor)
     */
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
    }

    @Override
    public void saveAsCSV() {
	final List<String> attributes = new ArrayList<>();
	for (final PropertyDescriptor v : layer.getFeatureSource().getSchema().getDescriptors()) {
	    attributes.add(v.getName().toString());
	}
	saveAsCSV(attributes, null, null);
    }

}
