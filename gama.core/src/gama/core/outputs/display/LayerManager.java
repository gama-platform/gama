/*******************************************************************************************************
 *
 * LayerManager.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.display;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gama.api.GAMA;
import gama.api.additions.delegates.IEventLayerDelegate;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.types.geometry.IShape;
import gama.api.ui.IOutput;
import gama.api.ui.displays.IDisplaySurface;
import gama.api.ui.displays.IGraphics;
import gama.api.ui.displays.IGraphicsScope;
import gama.api.ui.layers.ILayer;
import gama.api.ui.layers.ILayerManager;
import gama.api.ui.layers.ILayerStatement;
import gama.core.outputs.layers.AgentLayer;
import gama.core.outputs.layers.EventLayer;
import gama.core.outputs.layers.GisLayer;
import gama.core.outputs.layers.GraphicLayer;
import gama.core.outputs.layers.GridLayer;
import gama.core.outputs.layers.HexagonalGridLayer;
import gama.core.outputs.layers.ImageLayer;
import gama.core.outputs.layers.MeshLayer;
import gama.core.outputs.layers.OverlayLayer;
import gama.core.outputs.layers.SpeciesLayer;
import gama.core.outputs.layers.charts.ChartLayer;

/**
 * Written by drogoul Modified on 23 janv. 2011
 *
 * @todo Description
 *
 */
public class LayerManager implements ILayerManager {

	/**
	 * Creates the layer.
	 *
	 * @param output
	 *            the output
	 * @param layer
	 *            the layer
	 * @return the i layer
	 */
	public static ILayer createLayer(final IOutput.Display output, final ILayerStatement layer) {
		return switch (layer.getType(output)) {
			case GRID -> new GridLayer(layer);
			case AGENTS -> new AgentLayer(layer);
			case GRID_AGENTS -> new HexagonalGridLayer(layer);
			case SPECIES -> new SpeciesLayer(layer);
			case IMAGE -> new ImageLayer(output.getScope(), layer);
			case GIS -> new GisLayer(layer);
			case CHART -> new ChartLayer(layer);
			case EVENT -> new EventLayer(layer);
			case GRAPHICS -> new GraphicLayer(layer);
			case OVERLAY -> new OverlayLayer(layer);
			case MESH -> new MeshLayer(layer);
			default -> null;
		};
	}

	/** The enabled layers. */
	private final ILayer[] layers;

	/** The event layers. */
	private final Map<String, ILayerStatement.Event> eventLayers = new HashMap<>();

	/** The surface. */
	final IDisplaySurface surface;

	/** The count. */
	private int count = 0;

	/**
	 * Instantiates a new layer manager.
	 *
	 * @param surface
	 *            the surface
	 * @param output
	 *            the output
	 */
	public LayerManager(final IDisplaySurface surface, final IOutput.Display output) {
		this.surface = surface;
		OverlayLayer overlay = null;
		final List<ILayer> layers = new ArrayList<>();
		for (final ILayerStatement layer : output.getLayers()) {
			if (layer instanceof ILayerStatement.Event el) { eventLayers.put(el.getTrigger(), el); }
			if (layer.isToCreate()) {
				final ILayer result = createLayer(output, layer);
				if (result instanceof OverlayLayer) {
					overlay = (OverlayLayer) result;
				} else if (result != null) {
					layers.add(result);
					addItem(result);
				}
			}
		}
		if (overlay != null) { layers.add(overlay); }
		this.layers = layers.toArray(new ILayer[layers.size()]);
	}

	@Override
	public void dispose() {
		for (final ILayer d : layers) { d.dispose(); }
	}

	@Override
	public List<ILayer> getLayersIntersecting(final int x, final int y) {
		final List<ILayer> result = new ArrayList<>();
		for (final ILayer layer : layers) { if (layer.containsScreenPoint(x, y)) { result.add(layer); } }
		return result;
	}

	/**
	 * Method focusOn()
	 *
	 * @see gama.api.ui.layers.ILayerManager#focusOn(gama.api.types.geometry.IShape)
	 */
	@Override
	public Rectangle2D focusOn(final IShape geometry, final IDisplaySurface s) {
		if (geometry == null) return null;
		Rectangle2D result = null;
		for (final ILayer display : layers) {
			final Rectangle2D r = display.focusOn(geometry, s);
			if (r != null) {
				if (result == null) {
					result = new Rectangle2D.Double(r.getX(), r.getY(), r.getWidth(), r.getHeight());
				} else {
					result.add(r);
				}
			}
		}
		return result;
	}

	@Override
	public void drawLayersOn(final IGraphics g) {
		if (g == null) return;
		// if ((g == null) || g == null || g.cannotDraw()) return;
		final IGraphicsScope scope = surface.getScope();
		// If the experiment is already closed
		if (scope == null || scope.interrupted()) return;
		scope.setGraphics(g);
		// boolean changed = false;
		// First we compute all the data and verify if anything is changed
		// for (final ILayer dis : layers) {
		// if (scope.interrupted()) return;
		// changed |= dis.getData().compute(scope, g);
		// }
		// if (changed) { forceRedrawingLayers(); }
		if (g.beginDrawingLayers()) {
			try {
				// We stop separating in two phases: updating of the data and then drawing, as it generates artefacts
				// like the ones described in #3446
				for (final ILayer dis : layers) {
					if (scope.interrupted()) return;

					dis.draw(scope, g);
				}
			} catch (final Exception e) {
				GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.create(e, scope), false);
			} finally {
				g.endDrawingLayers();
			}
		}
	}

	@Override
	public List<ILayer> getItems() { return Arrays.asList(layers); }

	/**
	 * Gets the item display name.
	 *
	 * @param obj
	 *            the obj
	 * @param previousName
	 *            the previous name
	 * @return the item display name
	 */
	@Override
	public String getItemDisplayName(final ILayer obj, final String previousName) {
		return obj.getMenuName();
	}

	/**
	 * Adds the item.
	 *
	 * @param obj
	 *            the obj
	 * @return true, if successful
	 */
	@Override
	public boolean addItem(final ILayer obj) {
		obj.getDefinition().setOrder(count++);
		obj.firstLaunchOn(surface);
		return true;
	}

	/**
	 * Allows the layers to do some cleansing when the output of the display changes
	 *
	 * @see gama.api.ui.layers.ILayerManager#outputChanged()
	 */
	@Override
	public void outputChanged() {
		for (final ILayer i : layers) { i.reloadOn(surface); }
	}

	@Override
	public boolean stayProportional() {
		for (final ILayer i : layers) { if (i.stayProportional()) return true; }
		return false;
	}

	@Override
	public void forceRedrawingLayers() {
		for (final ILayer l : layers) { l.forceRedrawingOnce(); }
		surface.layersChanged();
	}

	@Override
	public boolean isProvidingCoordinates() {
		for (final ILayer i : layers) { if (i.getData().isVisible() && i.isProvidingCoordinates()) return true; }
		return false;
	}

	@Override
	public boolean isProvidingWorldCoordinates() {
		for (final ILayer i : layers) { if (i.getData().isVisible() && i.isProvidingWorldCoordinates()) return true; }
		return false;
	}

	@Override
	public boolean hasMouseMenuEventLayer() {
		return eventLayers.containsKey(IEventLayerDelegate.MOUSE_MENU);
	}

	@Override
	public boolean hasEscEventLayer() {
		return eventLayers.containsKey(IEventLayerDelegate.KEY_ESC);
	}

	/**
	 * Checks for arrow event layer.
	 *
	 * @return true, if successful
	 */
	@Override
	public boolean hasArrowEventLayer() {
		return eventLayers.containsKey(IEventLayerDelegate.ARROW_DOWN)
				|| eventLayers.containsKey(IEventLayerDelegate.ARROW_UP)
				|| eventLayers.containsKey(IEventLayerDelegate.ARROW_RIGHT)
				|| eventLayers.containsKey(IEventLayerDelegate.ARROW_LEFT);
	}

	@Override
	public boolean isItemVisible(final ILayer obj) {
		return obj.getData().isVisible();
	}

	@Override
	public boolean hasStructurallyChanged() {
		for (final ILayer i : layers) { if (i.getData().hasStructurallyChanged()) return true; }
		return false;
	}

	@Override
	public ILayer.Chart getOnlyChart() {
		ChartLayer result = null;
		for (final ILayer i : layers) {
			if (i instanceof ChartLayer cl) {
				if (result != null) return null; // two chart layers
				result = cl;
			} else if (!(i instanceof EventLayer)) return null;
		}
		return result;
	}

}
