/*******************************************************************************************************
 *
 * GeoPackageViewer.java, in gama.ui.viewers, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.viewers.gis;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;
import org.geotools.api.data.DataStore;
import org.geotools.api.data.DataStoreFinder;
import org.geotools.api.feature.type.AttributeDescriptor;
import org.geotools.api.feature.type.GeometryType;
import org.geotools.api.style.FeatureTypeStyle;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.MapContent;
import org.geotools.map.StyleLayer;
import org.geotools.referencing.CRS;
import org.geotools.styling.SLD;

import gama.api.GAMA;
import gama.api.types.color.IColor;
import gama.ui.shared.controls.FlatButton;
import gama.ui.shared.menus.GamaMenu;
import gama.ui.shared.resources.GamaColors;
import gama.ui.shared.resources.GamaColors.GamaUIColor;
import gama.ui.shared.utils.PreferencesHelper;
import gama.ui.shared.views.toolbar.Selector;
import gama.ui.viewers.gis.geotools.styling.Mode;
import gama.ui.viewers.gis.geotools.styling.Utils;

/**
 * Displays feature layers from a GeoPackage in the GIS map viewer.
 */
public class GeoPackageViewer extends ShapeFileViewer {

	private DataStore dataStore;
	private String[] layerNames = new String[0];
	private String selectedLayer;
	private FlatButton statusButton;

	@Override
	public void init(final IEditorSite site, final IEditorInput input) throws PartInitException {
		setSite(site);
		final FileEditorInput fileInput = (FileEditorInput) input;
		file = fileInput.getFile();
		final IPath path = fileInput.getPath();
		final File geoPackage = path.makeAbsolute().toFile();
		pathStr = geoPackage.getAbsolutePath();

		try {
			final Map<String, Object> parameters = Map.of("dbtype", "geopkg", "database", geoPackage);
			dataStore = DataStoreFinder.getDataStore(parameters);
			if (dataStore == null) { throw new IOException("No GeoPackage data store is available"); }
			layerNames = dataStore.getTypeNames();
			if (layerNames.length == 0) { throw new IOException("GeoPackage contains no feature layers"); }
			content = new MapContent();
			selectLayer(layerNames[0]);
			setPartName(path.lastSegment());
			setInput(input);
		} catch (final Exception e) {
			if (dataStore != null) {
				dataStore.dispose();
				dataStore = null;
			}
			throw new PartInitException("Unable to open GeoPackage " + path, e);
		}
	}

	private void selectLayer(final String layerName) throws IOException {
		final var nextFeatureSource = dataStore.getFeatureSource(layerName);
		final var nextStyle = Utils.createStyle2(nextFeatureSource);
		final var nextLayer = new FeatureLayer(nextFeatureSource, nextStyle);
		final Mode nextMode = determineMode(nextFeatureSource.getSchema(), "Polygon");
		final List<FeatureTypeStyle> styles = nextStyle.featureTypeStyles();
		final FeatureTypeStyle nextFts = styles.isEmpty() ? null : styles.get(0);
		if (nextFts != null) {
			setFillColor(IColor.toAWTColor(PreferencesHelper.SHAPEFILE_VIEWER_FILL.getValue()), nextMode, nextFts);
			setStrokeColor(IColor.toAWTColor(PreferencesHelper.SHAPEFILE_VIEWER_LINE_COLOR.getValue()), nextMode,
					nextFts);
			((StyleLayer) nextLayer).setStyle(nextStyle);
		}
		if (layer != null) { content.removeLayer(layer); }
		selectedLayer = layerName;
		featureSource = nextFeatureSource;
		style = nextStyle;
		layer = nextLayer;
		mode = nextMode;
		fts = nextFts;
		content.addLayer(layer);
		updateStatus();
		if (pane != null) {
			pane.setMapContent(content);
			pane.reset();
			pane.redraw();
		}
	}

	@Override
	protected void displayInfoString() {
		final ToolItem item = toolbar.status("");
		statusButton = (FlatButton) item.getControl();
		updateStatus();
		statusButton.setSelectionListener(new Selector() {

			Menu menu;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				if (menu == null || menu.isDisposed()) { menu = new Menu(toolbar.getShell(), SWT.POP_UP); }
				for (final MenuItem oldItem : menu.getItems()) { oldItem.dispose(); }
				fillMenu();
				final Point point = toolbar.toDisplay(new Point(event.x, event.y + toolbar.getSize().y));
				menu.setLocation(point.x, point.y);
				menu.setVisible(true);
			}

			private void fillMenu() {
				GamaMenu.separate(menu, "Feature layers");
				for (final String name : layerNames) {
					final MenuItem item = new MenuItem(menu, SWT.RADIO);
					item.setText(name);
					item.setSelection(name.equals(selectedLayer));
					item.addListener(SWT.Selection, event -> {
						if (item.getSelection()) {
							try {
								selectLayer(name);
								item.setSelection(true);
							} catch (final IOException e) {
								GAMA.reportError(GAMA.getRuntimeScope(),
										gama.api.exceptions.GamaRuntimeException.create(e, GAMA.getRuntimeScope()),
										false);
							}
						}
					});
				}
				GamaMenu.separate(menu, "Bounds");
				try {
					final ReferencedEnvelope bounds = featureSource.getBounds();
					addInfo(menu, "upper corner", bounds.getUpperCorner().getOrdinate(0) + " "
							+ bounds.getUpperCorner().getOrdinate(1));
					addInfo(menu, "lower corner", bounds.getLowerCorner().getOrdinate(0) + " "
							+ bounds.getLowerCorner().getOrdinate(1));
					addInfo(menu, "dimensions", bounds.getWidth() + " x " + bounds.getHeight());
					final var crs = featureSource.getSchema().getCoordinateReferenceSystem();
					addInfo(menu, "coordinate reference system", crs == null ? "Unknown" : CRS.toSRS(crs));
				} catch (final IOException e) {
					addInfo(menu, "bounds", "Unavailable");
				}
				GamaMenu.separate(menu);
				GamaMenu.separate(menu, "Attributes");
				for (final AttributeDescriptor descriptor : featureSource.getSchema().getAttributeDescriptors()) {
					final String type = descriptor.getType() instanceof GeometryType ? "geometry"
							: descriptor.getType().getBinding().getSimpleName();
					addInfo(menu, descriptor.getName().getLocalPart(), type);
				}
			}

			private void addInfo(final Menu parent, final String label, final String value) {
				final MenuItem item = new MenuItem(parent, SWT.NONE);
				item.setEnabled(false);
				item.setText("     - " + label + ": " + value);
			}
		});
	}

	private void updateStatus() {
		if (statusButton == null || statusButton.isDisposed() || featureSource == null) return;
		String summary = selectedLayer;
		try {
			summary += " | " + featureSource.getFeatures().size() + " features";
		} catch (final IOException e) {
			summary += " | Unable to read feature count";
		}
		statusButton.setText(summary);
	}

	@Override
	public void dispose() {
		if (dataStore != null) {
			dataStore.dispose();
			dataStore = null;
		}
		super.dispose();
	}

	@Override
	public GamaUIColor getColor(final int index) {
		if (index == 0) return GamaColors.get(SLD.color(getStroke(mode, fts)));
		return GamaColors.get(SLD.color(getFill(mode, fts)));
	}

	@Override
	public void setColor(final int index, final GamaUIColor color) {
		final org.eclipse.swt.graphics.RGB rgb = color.getRGB();
		final Color awtColor = new Color(rgb.red, rgb.green, rgb.blue);
		if (index == 0) {
			setStrokeColor(awtColor, mode, fts);
		} else {
			setFillColor(awtColor, mode, fts);
		}
		((StyleLayer) layer).setStyle(style);
		if (pane != null) { pane.redraw(); }
	}

}
