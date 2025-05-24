/*******************************************************************************************************
 *
 * OSMFileViewer.java, in gama.ui.viewers, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.viewers.gis;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;
import org.geotools.data.DataUtilities;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.Style;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import gama.core.metamodel.shape.IShape;
import gama.core.runtime.GAMA;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.file.GamaOsmFile;
import gama.ui.shared.controls.FlatButton;
import gama.ui.shared.menus.GamaMenu;
import gama.ui.shared.resources.GamaColors;
import gama.ui.shared.resources.GamaColors.GamaUIColor;
import gama.ui.shared.views.toolbar.GamaToolbarFactory;
import gama.ui.shared.views.toolbar.Selector;
import gama.ui.viewers.gis.geotools.styling.Utils;

/**
 * The Class OSMFileViewer.
 */
public class OSMFileViewer extends GISFileViewer {

    /** The attributes. */
    Map<String, String> attributes;

    /** The osmfile. */
    GamaOsmFile osmfile;

    /** The map layer table. */
    MapLayerComposite mapLayerTable;

    @Override
    public void createPartControl(final Composite composite) {
	final Composite parent = GamaToolbarFactory.createToolbars(this, composite);
	final SashForm sashForm = new SashForm(parent, SWT.HORIZONTAL | SWT.NULL);
	displayInfoString();
	mapLayerTable = new MapLayerComposite(sashForm, SWT.BORDER);
	pane = new SwtMapPane(sashForm, SWT.BORDER | SWT.NO_BACKGROUND, new StreamingRenderer(), content);
	pane.setBackground(GamaColors.system(SWT.COLOR_WHITE));
	mapLayerTable.setMapPane(pane);
	sashForm.setWeights(1, 4);
	pane.redraw();

    }

    @Override
    public void init(final IEditorSite site, final IEditorInput input) throws PartInitException {
	setSite(site);
	final FileEditorInput fi = (FileEditorInput) input;
	file = fi.getFile();
	final IPath path = fi.getPath();

	final File f = path.makeAbsolute().toFile();

	try {
	    pathStr = f.getAbsolutePath();
	    osmfile = new GamaOsmFile(null, pathStr);
	    attributes = osmfile.getOSMAttributes(GAMA.getRuntimeScope());
	    final SimpleFeatureType TYPE = DataUtilities.createType("geometries", "geom:LineString");

	    final ArrayList<SimpleFeature> list = new ArrayList<>();

	    for (final IShape shape : osmfile.iterable(null)) {
		list.add(SimpleFeatureBuilder.build(TYPE, new Object[] { shape.getInnerGeometry() }, null));
	    }
	    final SimpleFeatureCollection collection = new ListFeatureCollection(TYPE, list);
	    featureSource = DataUtilities.source(collection);
	    content = new MapContent();
	    style = Utils.createStyle2(featureSource);
	    layer = new FeatureLayer(featureSource, style);
	    final List<String> layers = new ArrayList<>(osmfile.getLayers().keySet());
	    Collections.sort(layers);
	    Collections.reverse(layers);
	    for (final String val : layers) {
		final boolean isPoint = val.endsWith("(point)");
		final boolean isLine = val.endsWith("(line)");
		final SimpleFeatureType TYPET = isPoint ? DataUtilities.createType(val, "geom:Point")
			: isLine ? DataUtilities.createType(val, "geom:LineString")
				: DataUtilities.createType(val, "geom:Polygon");

		final ArrayList<SimpleFeature> listT = new ArrayList<>();

		for (final IShape shape : osmfile.getLayers().get(val)) {
		    listT.add(SimpleFeatureBuilder.build(TYPET, new Object[] { shape.getInnerGeometry() }, null));
		}
		final SimpleFeatureCollection collectionT = new ListFeatureCollection(TYPET, listT);
		final SimpleFeatureSource featureSourceT = DataUtilities.source(collectionT);

		final Style styleT = Utils.createStyle2(featureSourceT);
		final FeatureLayer layerT = new FeatureLayer(featureSourceT, styleT);
		content.addLayer(layerT);

	    }
	} catch (final SchemaException | GamaRuntimeException e) {
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
		GamaMenu.separate(menu, "Attributes");
		try {
		    final List<String> atts = new ArrayList<>(attributes.keySet());
		    Collections.sort(atts);
		    String currentType = "";

		    for (final String att : atts) {
			final String[] attP = att.split(";");
			if (!currentType.equals(attP[0])) {
			    currentType = attP[0];
			    final MenuItem m3 = new MenuItem(menu, SWT.NONE);
			    m3.setEnabled(false);
			    m3.setText("  * " + currentType);
			}
			final MenuItem m2 = new MenuItem(menu, SWT.NONE);
			m2.setEnabled(false);
			m2.setText("       - " + attP[1] + " (" + attributes.get(att) + ")");
		    }
		} catch (final Exception e) {
		    e.printStackTrace();
		}

	    }

	});

    }

    @Override
    public void saveAsCSV() {

	final Layer layer = mapLayerTable.getMapLayerTableViewer().getSelectedMapLayer();
	if (layer == null) {
	    return;
	}
	final HashSet<String> atts = new HashSet<>();

	final String layerName = layer.getFeatureSource().getName().toString();
	for (final String at : attributes.keySet()) {
	    final String[] dec = at.split(";");

	    if (layerName.equals(dec[0])) {
		atts.add(dec[1]);

	    }
	}
	final List<IShape> geoms = osmfile.getLayers().get(layerName);
	final List<String> attsOrd = new ArrayList<>(atts);
	Collections.sort(attsOrd);
	saveAsCSV(attsOrd, geoms, layerName);
    }

    @Override
    public String[] getColorLabels() {
	return new String[] {};
    }

    @Override
    public GamaUIColor getColor(final int index) {
	return null;
    }

    @Override
    public void setColor(final int index, final GamaUIColor c) {
    }

}
