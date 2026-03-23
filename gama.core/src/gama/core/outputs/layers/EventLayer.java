/*******************************************************************************************************
 *
 * EventLayer.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers;

import gama.api.GAMA;
import gama.api.additions.delegates.IEventLayerDelegate;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.Cast;
import gama.api.kernel.agent.IAgent;
import gama.api.runtime.IExecutable;
import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.IPoint;
import gama.api.ui.displays.IDisplaySurface;
import gama.api.ui.displays.IGraphics;
import gama.api.ui.displays.IGraphicsScope;
import gama.api.ui.layers.IEventLayerListener;
import gama.api.ui.layers.ILayerStatement;
import gama.dev.DEBUG;

/**
 * Written by marilleau
 */

public class EventLayer extends AbstractLayer implements IEventLayerListener {

	static {
		DEBUG.OFF();
	}

	/** The execution scope. */
	IGraphicsScope executionScope;

	/** The listened event. */
	private int listenedEvent;

	/** The surface. */
	private IDisplaySurface surface;

	/** The event. */
	private String event;

	/**
	 * Instantiates a new event layer.
	 *
	 * @param layer
	 *            the layer
	 */
	public EventLayer(final ILayerStatement layer) {
		super(layer);
	}

	@Override
	public void enableOn(final IDisplaySurface surface) {
		surface.addListener(this);
	}

	@Override
	public void disableOn(final IDisplaySurface surface) {
		super.disableOn(surface);
		surface.removeListener(this);
	}

	/**
	 * Gets the listening event.
	 *
	 * @param eventTypeName
	 *            the event type name
	 * @return the listening event
	 */
	private int getListeningEvent(final String eventTypeName) {
		return switch (eventTypeName) {
			case IEventLayerDelegate.MOUSE_DOWN -> MOUSE_PRESS;
			case IEventLayerDelegate.MOUSE_UP -> MOUSE_RELEASED;
			case IEventLayerDelegate.MOUSE_CLICKED -> MOUSE_CLICKED;
			case IEventLayerDelegate.MOUSE_MOVED -> MOUSE_MOVED;
			case IEventLayerDelegate.MOUSE_ENTERED -> MOUSE_ENTERED;
			case IEventLayerDelegate.MOUSE_EXITED -> MOUSE_EXITED;
			case IEventLayerDelegate.MOUSE_MENU -> MOUSE_MENU;
			case IEventLayerDelegate.MOUSE_DRAGGED -> MOUSE_DRAGGED;
			case IEventLayerDelegate.ARROW_DOWN -> ARROW_DOWN;
			case IEventLayerDelegate.ARROW_UP -> ARROW_UP;
			case IEventLayerDelegate.ARROW_LEFT -> ARROW_LEFT;
			case IEventLayerDelegate.ARROW_RIGHT -> ARROW_RIGHT;
			case IEventLayerDelegate.KEY_ESC -> KEY_ESC;
			case IEventLayerDelegate.KEY_PAGE_DOWN -> KEY_PAGE_DOWN;
			case IEventLayerDelegate.KEY_PAGE_UP -> KEY_PAGE_UP;
			case IEventLayerDelegate.KEY_ENTER -> KEY_RETURN;
			case IEventLayerDelegate.KEY_TAB -> KEY_TAB;
			case IEventLayerDelegate.SHIFT_MODIFIER -> KEY_SHIFT;
			case IEventLayerDelegate.CMD_MODIFIER -> KEY_CMD;
			case IEventLayerDelegate.CONTROL_MODIFIER -> KEY_CTRL;
			case IEventLayerDelegate.ALT_MODIFIER -> KEY_ALT;
			default -> KEY_PRESSED;
		};

	}

	@Override
	public void firstLaunchOn(final IDisplaySurface surface) {
		super.firstLaunchOn(surface);
		this.surface = surface;
		final IExpression eventType = definition.getFacet(ILayerStatement.Event.TRIGGER);
		if (executionScope != null) { GAMA.releaseScope(executionScope); }
		executionScope = surface.getScope().copy("of event layer");

		// Evaluated in the display surface scope to gather variables defined in
		// there
		event = Cast.asString(surface.getScope(), eventType.value(surface.getScope()));
		listenedEvent = getListeningEvent(event);
		surface.addListener(this);
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	@Override
	public String getType() { return "Event layer"; }

	// We explicitly translate by the origin of the surface
	@Override
	public IPoint getModelCoordinatesFrom(final int xOnScreen, final int yOnScreen, final IDisplaySurface g) {
		if (xOnScreen == -1 && yOnScreen == -1) return GamaPointFactory.create();
		return g.getModelCoordinates();
	}

	// AD: Fix for Issue #1511
	@Override
	public boolean containsScreenPoint(final int x, final int y) {
		return false;
	}

	@Override
	public void mouseClicked(final int x, final int y, final int button) {
		if (MOUSE_CLICKED == listenedEvent && button == 1) { executeEvent(x, y); }
	}

	@Override
	public void mouseDown(final int x, final int y, final int button) {
		if (MOUSE_PRESS == listenedEvent && button == 1) { executeEvent(x, y); }
	}

	@Override
	public void mouseUp(final int x, final int y, final int button) {
		if (MOUSE_RELEASED == listenedEvent && button == 1) { executeEvent(x, y); }
	}

	@Override
	public void mouseMove(final int x, final int y) {
		if (MOUSE_MOVED == listenedEvent) { executeEvent(x, y); }
	}

	@Override
	public void mouseDrag(final int x, final int y, final int button) {
		if (MOUSE_DRAGGED == listenedEvent && button == 1) { executeEvent(x, y); }
	}

	@Override
	public void mouseEnter(final int x, final int y) {
		if (MOUSE_ENTERED == listenedEvent) { executeEvent(x, y); }
	}

	@Override
	public void mouseExit(final int x, final int y) {
		if (MOUSE_EXITED == listenedEvent) { executeEvent(x, y); }
	}

	@Override
	public void mouseMenu(final int x, final int y) {
		if (MOUSE_MENU == listenedEvent) { executeEvent(x, y); }
	}

	/**
	 * Execute event.
	 *
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 */
	private void executeEvent(final int x, final int y) {
		final IAgent agent = ((ILayerStatement.Event) definition).getExecuter(executionScope);
		if (agent == null) return;
		final IExecutable executer = ((ILayerStatement.Event) definition).getExecutable(executionScope);
		if (executer == null) return;
		final IPoint pp = getModelCoordinatesFrom(x, y, surface);
		if (pp == null) return;
		// DEBUG.OUT("Coordinates in env (before test)" + pp);
		// if (pp.getX() < 0 || pp.getY() < 0 || pp.getX() >= surface.getEnvWidth()
		// || pp.getY() >= surface.getEnvHeight()) {
		// if (MOUSE_EXITED != listenedEvent && MOUSE_ENTERED != listenedEvent) return;
		// }
		// DEBUG.OUT("Coordinates in env (after test)" + pp);
		GAMA.runAndUpdateAll(() -> executionScope.execute(executer, agent, null));

	}

	@Override
	public void keyPressed(final String c) {
		if (c.equals(event)) { executeEvent(-1, -1); }
	}

	@Override
	protected void privateDraw(final IGraphicsScope scope, final IGraphics g) throws GamaRuntimeException {}

	@Override
	public void draw(final IGraphicsScope scope, final IGraphics g) throws GamaRuntimeException {}

	@Override
	public Boolean isControllable() { return false; }

	/**
	 * Gets the event.
	 *
	 * @return the event
	 */
	public String getEvent() { return event; }

	@Override
	public void specialKeyPressed(final int keyCode) {
		if (keyCode == listenedEvent) { executeEvent(-1, -1); }
	}
}
