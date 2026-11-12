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

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import gama.api.GAMA;
import gama.api.additions.delegates.IEventLayerDelegate;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.Cast;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.simulation.IExperimentAgent;
import gama.api.kernel.simulation.ISimulationAgent;
import gama.api.runtime.IExecutable;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.IPoint;
import gama.api.ui.displays.IDisplaySurface;
import gama.api.ui.displays.IGraphics;
import gama.api.ui.displays.IGraphicsScope;
import gama.api.ui.layers.IEventLayerListener;
import gama.api.ui.layers.ILayerStatement;
import gama.api.ui.layers.KeyCombination;
import gama.api.ui.layers.UserInputEvent;
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

	private KeyCombination combination;

	/** Latest queued legacy motion per simulation; discrete events form ordering barriers. */
	private static final WeakHashMap<ISimulationAgent, WeakReference<PendingMotion>> pendingMotion =
			new WeakHashMap<>();

	private static final ScheduledExecutorService motionScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
		final Thread thread = new Thread(r, "Display motion scheduler");
		thread.setDaemon(true);
		return thread;
	});

	private static final class PendingMotion {

		final EventLayer layer;
		final IGraphicsScope source;
		final int kind;
		UserInputEvent input;
		ScheduledFuture<?> timer;
		boolean submitted;

		PendingMotion(final EventLayer layer, final IGraphicsScope source, final int kind, final UserInputEvent input) {
			this.layer = layer;
			this.source = source;
			this.kind = kind;
			this.input = input;
		}
	}

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
		executionScope = surface.getScope().copyForGraphics("of event layer");

		// Evaluated in the display surface scope to gather variables defined in
		// there
		event = Cast.asString(surface.getScope(), eventType.value(surface.getScope()));
		combination = KeyCombination.isCombination(event) ? KeyCombination.parse(event) : null;
		listenedEvent = getListeningEvent(event);
		surface.addListener(this);
	}

	@Override
	public void dispose() {
		super.dispose();
		if (executionScope != null) {
			GAMA.releaseScope(executionScope);
			executionScope = null;
		}
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
		if (MOUSE_MOVED == listenedEvent) { executeEvent(x, y, MOUSE_MOVED); }
	}

	@Override
	public void mouseDrag(final int x, final int y, final int button) {
		if (MOUSE_DRAGGED == listenedEvent && button == 1) { executeEvent(x, y, MOUSE_DRAGGED); }
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
		executeEvent(x, y, 0);
	}

	private void executeEvent(final int x, final int y, final int motionKind) {
		UserInputEvent input = null;
		if (x != -1 || y != -1) {
			final IPoint location = getModelCoordinatesFrom(x, y, surface);
			if (location == null) return;
			input = new UserInputEvent("", "", "",
					GamaPointFactory.create(location.getX(), location.getY(), location.getZ()),
					GamaPointFactory.create(x, y));
		}
		executeEvent(input, motionKind);
	}

	private void executeEvent(final UserInputEvent input) {
		executeEvent(input, 0);
	}

	private void executeEvent(final UserInputEvent input, final int motionKind) {
		final IGraphicsScope source = executionScope;
		if (source == null || source.isClosed()) return;
		final IAgent owner = ((ILayerStatement.Event) definition).getExecuter(source);
		final ISimulationAgent simulation = owner instanceof ISimulationAgent sim ? sim : null;
		final IExperimentAgent experiment = owner instanceof IExperimentAgent exp ? exp : null;
		if (simulation != null && !simulation.isInOwnThread()) {
			synchronized (pendingMotion) {
				final WeakReference<PendingMotion> reference = pendingMotion.get(simulation);
				final PendingMotion previous = reference == null ? null : reference.get();
				if (motionKind != 0 && previous != null && previous.layer == this && previous.source == source
						&& previous.kind == motionKind) {
					previous.input = input;
					return;
				}
				pendingMotion.remove(simulation);
				if (previous != null) { submitMotion(simulation, previous); }
				if (motionKind == 0) {
					simulation.executeAction(ignored -> {
						runEvent(source, simulation, input);
						return null;
					});
				} else {
					final PendingMotion motion = new PendingMotion(this, source, motionKind, input);
					pendingMotion.put(simulation, new WeakReference<>(motion));
					motion.timer = motionScheduler.schedule(() -> {
						synchronized (pendingMotion) { submitMotion(simulation, motion); }
					}, 33, TimeUnit.MILLISECONDS);
				}
			}
		} else {
			if (simulation != null) {
				synchronized (pendingMotion) { pendingMotion.remove(simulation); }
				runEvent(source, simulation, input);
			} else if (experiment != null) {
				experiment.executeAction(ignored -> {
					runEvent(source, null, input);
					return null;
				});
			}
		}
	}

	/** Called under pendingMotion's lock, so barriers are posted after the motion they follow. */
	private static void submitMotion(final ISimulationAgent simulation, final PendingMotion motion) {
		if (motion.submitted) return;
		motion.submitted = true;
		if (motion.timer != null) { motion.timer.cancel(false); }
		simulation.executeAction(ignored -> {
			final UserInputEvent input;
			synchronized (pendingMotion) {
				final WeakReference<PendingMotion> current = pendingMotion.get(simulation);
				if (current != null && current.get() == motion) { pendingMotion.remove(simulation); }
				input = motion.input;
			}
			motion.layer.runEvent(motion.source, simulation, input);
			return null;
		});
	}

	private void runEvent(final IGraphicsScope source, final ISimulationAgent simulation, final UserInputEvent input) {
		if (source.isClosed() || simulation != null && (simulation.dead() || source.getSimulation() != simulation))
			return;
		try (IGraphicsScope scope = source.copyForGraphics("for user input")) {
			if (input != null) { scope.setData(UserInputEvent.SCOPE_KEY, input); }
			final IAgent agent = ((ILayerStatement.Event) definition).getExecuter(scope);
			if (agent == null || agent.dead()) return;
			final IExecutable executer = ((ILayerStatement.Event) definition).getExecutable(scope);
			if (executer != null) { GAMA.runAndUpdateAll(() -> scope.execute(executer, agent, null)); }
		}
	}

	@Override
	public void keyPressed(final String c) {
		if (c.equals(event)) { executeEvent(-1, -1); }
	}

	@Override
	public void keyCombinationPressed(final KeyCombination pressed) {
		if (pressed.equals(combination)) { executeEvent(-1, -1); }
	}

	@Override
	public void userKeyPressed(final String key) {
		if (IEventLayerDelegate.KEY_LISTENER.equals(event)) { executeEvent(UserInputEvent.keyboard(key)); }
	}

	@Override
	public boolean listensToUserMouseEvents() { return IEventLayerDelegate.MOUSE_LISTENER.equals(event); }

	@Override
	public void userMouseEvent(final UserInputEvent input, final int x, final int y) {
		if (listensToUserMouseEvents()) { executeEvent(input); }
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
