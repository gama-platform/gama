/*******************************************************************************************************
 *
 * NullDisplaySurface.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2024-06).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.display;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Semaphore;

import org.locationtech.jts.geom.Envelope;

import gama.core.common.interfaces.IDisplaySurface;
import gama.core.common.interfaces.IGraphics;
import gama.core.common.interfaces.ILayer;
import gama.core.common.interfaces.ILayerManager;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.shape.IShape;
import gama.core.outputs.LayeredDisplayData;
import gama.core.outputs.LayeredDisplayData.Changes;
import gama.core.outputs.LayeredDisplayOutput;
import gama.core.outputs.layers.IEventLayerListener;
import gama.core.runtime.IScope.IGraphicsScope;
import gama.core.util.IList;

/**
 * Class NullDisplaySurface.
 *
 * @author drogoul
 * @since 26 mars 2014
 *
 */
public class NullDisplaySurface implements IDisplaySurface {

	/**
	 * Method getImage()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#getImage()
	 */
	@Override
	public BufferedImage getImage(final int w, final int h) {
		return null;
	}

	@Override
	public IGraphicsScope getScope() { return null; }

	/**
	 * Method dispose()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#dispose()
	 */
	@Override
	public void dispose() {}

	/**
	 * Method updateDisplay()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#updateDisplay()
	 */
	@Override
	public void updateDisplay(final boolean force, final Semaphore synchronizer) {}

	/**
	 * Method zoomIn()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#zoomIn()
	 */
	@Override
	public void zoomIn() {}

	/**
	 * Method zoomOut()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#zoomOut()
	 */
	@Override
	public void zoomOut() {}

	/**
	 * Method zoomFit()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#zoomFit()
	 */
	@Override
	public void zoomFit() {}

	/**
	 * Method zoomFit()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#toggleLock()
	 */
	@Override
	public void toggleLock() {}

	/**
	 * Method getManager()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#getManager()
	 */
	@Override
	public ILayerManager getManager() { return null; }

	/**
	 * Method focusOn()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#focusOn(gama.core.metamodel.shape.IShape)
	 */
	@Override
	public void focusOn(final IShape geometry) {}

	/**
	 * Method getWidth()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#getWidth()
	 */
	@Override
	public int getWidth() { return 0; }

	/**
	 * Method getHeight()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#getHeight()
	 */
	@Override
	public int getHeight() { return 0; }

	/**
	 * Method initialize()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#initialize(double, double,
	 *      gama.core.outputs.LayeredDisplayOutput)
	 */
	@Override
	public void outputReloaded() {}

	/**
	 * Method addMouseListener()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#addMouseListener(java.awt.event.MouseListener)
	 */
	@Override
	public void addListener(final IEventLayerListener e) {}

	/**
	 * Method removeMouseListener()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#removeMouseListener(java.awt.event.MouseListener)
	 */
	@Override
	public void removeListener(final IEventLayerListener e) {}

	/**
	 * Method getEnvWidth()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#getEnvWidth()
	 */
	@Override
	public double getEnvWidth() { return 0; }

	/**
	 * Method getEnvHeight()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#getEnvHeight()
	 */
	@Override
	public double getEnvHeight() { return 0; }

	/**
	 * Method getDisplayWidth()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#getDisplayWidth()
	 */
	@Override
	public double getDisplayWidth() { return 0; }

	/**
	 * Method getDisplayHeight()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#getDisplayHeight()
	 */
	@Override
	public double getDisplayHeight() { return 0; }

	/**
	 * Method selectAgent()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#selectAgent(int, int)
	 */
	@Override
	public IList<IAgent> selectAgent(final int x, final int y) {
		return null;
	}

	/**
	 * Method getZoomLevel()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#getZoomLevel()
	 */
	@Override
	public double getZoomLevel() { return 0; }

	/**
	 * Method setSize()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#setSize(int, int)
	 */
	@Override
	public void setSize(final int x, final int y) {}

	/**
	 * Method getOutput()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#getOutput()
	 */
	@Override
	public LayeredDisplayOutput getOutput() { return null; }

	/**
	 * Method waitForUpdateAndRun()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#waitForUpdateAndRun(java.lang.Runnable)
	 */
	@Override
	public void runAndUpdate(final Runnable r) {}

	/**
	 * Method getData()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#getData()
	 */
	@Override
	public LayeredDisplayData getData() { return null; }

	/**
	 * Method setSWTMenuManager()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#setSWTMenuManager(java.lang.Object)
	 */
	// @Override
	// public void setSWTMenuManager(final Object displaySurfaceMenu) {
	// }

	/**
	 * Method layersChanged()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#layersChanged()
	 */
	@Override
	public void layersChanged() {}

	/**
	 * Method changed()
	 *
	 * @see gama.core.outputs.LayeredDisplayData.DisplayDataListener#changed(gama.core.outputs.LayeredDisplayData.Changes,
	 *      boolean)
	 */
	@Override
	public void changed(final Changes property, final Object value) {}

	@Override
	public Collection<IEventLayerListener> getLayerListeners() { return Collections.EMPTY_LIST; }

	/**
	 * Method getVisibleRegionForLayer()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#getVisibleRegionForLayer(gama.core.common.interfaces.ILayer)
	 */
	@Override
	public Envelope getVisibleRegionForLayer(final ILayer currentLayer) {
		return null;
	}

	/**
	 * Method getFPS()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#getFPS()
	 */
	@Override
	public int getFPS() { return 0; }
	//
	// @Override
	// public boolean isRealized() {
	// return true;
	// }

	/**
	 * Method isRendered()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#isRendered()
	 */
	// @Override
	// public boolean isRendered() { return true; }

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#isDisposed()
	 */
	@Override
	public boolean isDisposed() { return false; }

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#getModelCoordinatesInfo()
	 */
	@Override
	public void getModelCoordinatesInfo(final StringBuilder sb) {}

	@Override
	public void dispatchKeyEvent(final char character) {}

	@Override
	public void dispatchSpecialKeyEvent(final int e) {}

	@Override
	public void dispatchMouseEvent(final int swtEventType, final int x, final int y) {}

	@Override
	public void setMousePosition(final int x, final int y) {}

	@Override
	public void selectAgentsAroundMouse() {}

	@Override
	public void draggedTo(final int x, final int y) {}

	@Override
	public void setMenuManager(final Object displaySurfaceMenu) {}

	@Override
	public boolean isVisible() { return true; }

	@Override
	public IGraphics getIGraphics() { return null; }

	@Override
	public Rectangle getBoundsForRobotSnapshot() { return new Rectangle(); }

	@Override
	public boolean shouldWaitToBecomeRendered() {
		return false;
	}

}
