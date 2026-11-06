/*******************************************************************************************************
 *
 * OverlayLayer.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import gama.annotations.constants.IKeyword;
import gama.api.GAMA;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.kernel.agent.IAgent;
import gama.api.types.geometry.IShape;
import gama.api.ui.displays.IDisplaySurface;
import gama.api.ui.displays.IGraphics;
import gama.api.ui.displays.IGraphicsScope;
import gama.api.ui.layers.ILayerStatement;
import gama.api.ui.layers.LayerCoordinates;
import gama.api.ui.layers.IDisplayControlLayer;
import gama.gaml.statements.draw.UiControlStatement;

/**
 * Class OverlayLayer.
 *
 * @author drogoul
 * @since 23 févr. 2016
 *
 */
public class OverlayLayer extends GraphicLayer implements IDisplayControlLayer {

	private record Control(UiControlStatement statement, Rectangle2D bounds, Object value, boolean enabled,
			double min, double max, double step, String group) {}

	private record Interaction(Control control, Object preview) {}

	private volatile List<Control> controls = List.of();
	private final List<Control> drawingControls = new ArrayList<>();
	private final AtomicReference<Interaction> interaction = new AtomicReference<>();
	private volatile IDisplaySurface controlSurface;
	private volatile UiControlStatement hovered;

	public boolean isControlHovered(final UiControlStatement statement) { return hovered == statement; }

	public boolean isControlPressed(final UiControlStatement statement) {
		final Interaction current = interaction.get();
		return current != null && current.control().statement() == statement && hovered == statement;
	}

	private void refreshControls() {
		final IDisplaySurface surface = controlSurface;
		if (surface != null && !surface.isDisposed()) surface.updateDisplay(false);
	}

	public Object registerControl(final UiControlStatement statement, final Rectangle2D localBounds,
			final Object value, final boolean enabled, final double min, final double max, final double step,
			final String group) {
		final Rectangle2D screenBounds = new Rectangle2D.Double(
				localBounds.getX() + getData().getPositionInPixels().getX(),
				localBounds.getY() + getData().getPositionInPixels().getY(),
				localBounds.getWidth(), localBounds.getHeight());
		final Control current = new Control(statement, screenBounds, value, enabled, min, max, step, group);
		drawingControls.add(current);
		final Interaction active = interaction.get();
		if (active == null || active.control().statement() != statement) return value;
		if (sameControl(current, active.control())) return active.preview();
		interaction.compareAndSet(active, null);
		return value;
	}

	/**
	 * Instantiates a new overlay layer.
	 *
	 * @param layer
	 *            the layer
	 */
	public OverlayLayer(final ILayerStatement layer) {
		super(layer);
	}

	@Override
	public boolean isOverlay() {
		return getData().getCoordinates() != LayerCoordinates.WORLD;
	}

	@Override
	protected OverlayLayerData createData() {
		return new OverlayLayerData(definition);
	}

	@Override
	public OverlayLayerData getData() { return (OverlayLayerData) super.getData(); }

	@Override
	public Rectangle2D focusOn(final IShape geometry, final IDisplaySurface s) {
		return null;
	}

	@Override
	public String getType() { return IKeyword.OVERLAY; }

	@Override
	protected void privateDraw(final IGraphicsScope scope, final IGraphics g) throws GamaRuntimeException {
		if (!isOverlay()) {
			super.privateDraw(scope, g);
			return;
		}
		controlSurface = g.getSurface();
		drawingControls.clear();
		final Object previous = scope.getData(UiControlStatement.OVERLAY_SCOPE_KEY);
		scope.setData(UiControlStatement.OVERLAY_SCOPE_KEY, this);
		g.beginOverlay(this);
		boolean completed = false;
		try {
			final IAgent agent = scope.getAgent();
			scope.execute(((OverlayStatement) definition).getAspect(), agent, null);
			completed = true;
		} finally {
			scope.setData(UiControlStatement.OVERLAY_SCOPE_KEY, previous);
			controls = completed ? List.copyOf(drawingControls) : List.of();
			if (hovered != null && controls.stream().noneMatch(c -> c.statement() == hovered && c.enabled())) {
				hovered = null;
			}
			final Interaction active = interaction.get();
			if (active != null && controls.stream().noneMatch(c -> sameControl(c, active.control()))) {
				interaction.compareAndSet(active, null);
			}
			g.endOverlay();
		}
	}

	private Control controlAt(final int x, final int y) {
		if (!getData().isVisible()) return null;
		final List<Control> current = controls;
		for (int i = current.size() - 1; i >= 0; i--) {
			final Control c = current.get(i);
			if (c.enabled() && c.bounds().contains(x, y)) return c;
		}
		return null;
	}

	@Override
	public boolean containsControl(final int x, final int y) { return controlAt(x, y) != null; }

	@Override
	public boolean isControlCaptured() { return interaction.get() != null; }

	@Override
	public boolean controlMouseDown(final int x, final int y, final int button) {
		if (button != 1) return false;
		final Control control = controlAt(x, y);
		if (control == null) return false;
		if (!interaction.compareAndSet(null, new Interaction(control, control.value()))) return true;
		controlMouseMove(x, y);
		if (!control.statement().isSlider()) refreshControls();
		return true;
	}

	@Override
	public boolean controlMouseMove(final int x, final int y) {
		final Control hit = controlAt(x, y);
		final UiControlStatement previousHover = hovered;
		hovered = hit == null ? null : hit.statement();
		final Interaction current = interaction.get();
		if (current == null) {
			if (previousHover != hovered) refreshControls();
			return false;
		}
		final Control active = current.control();
		if (active.statement().isSlider()) {
			final double width = active.bounds().getWidth();
			final double inset = UiControlStatement.getSliderTrackInset(width, active.bounds().getHeight());
			final double fraction = Math.clamp((x - active.bounds().getX() - inset) / (width - 2 * inset), 0, 1);
			double next = active.min() + fraction * (active.max() - active.min());
			if (active.step() > 0 && fraction > 0 && fraction < 1) {
				next -= Math.IEEEremainder(next - active.min(), active.step());
			}
			if (interaction.compareAndSet(current, new Interaction(active, Math.clamp(next, active.min(), active.max())))) {
				refreshControls();
			}
		} else if (previousHover != hovered) refreshControls();
		return true;
	}

	@Override
	public boolean controlMouseUp(final int x, final int y, final int button) {
		if (interaction.get() == null || button != 1) return false;
		controlMouseMove(x, y);
		final Interaction current = interaction.getAndSet(null);
		if (current == null) return true;
		final Control active = current.control();
		final Object next = active.statement().isSlider() ? current.preview() : active.statement().changedValue(active.value());
		final boolean commit = getData().isVisible()
				&& controls.stream().anyMatch(c -> sameControl(c, active))
				&& (active.statement().isSlider() || active.bounds().contains(x, y));
		final IDisplaySurface surface = controlSurface;
		final List<Control> radioGroup = active.statement().isRadio()
				? controls.stream().filter(c -> c.statement().isRadio() && Objects.equals(c.group(), active.group())).toList()
				: List.of();
		if (commit && (!active.statement().isSlider() || !sameValue(next, active.value()))) {
			GAMA.runAndUpdateAll(() -> {
				if (surface == null || controlSurface != surface || surface.isDisposed()) return;
				try (IGraphicsScope callback = surface.getScope().copyForGraphics("for overlay control")) {
					final IAgent experiment = callback.getExperiment();
					if (experiment == null || experiment.dead()) return;
					callback.execute(s -> {
						if (active.statement().isRadio()) {
							final List<Control> changed = new ArrayList<>();
							for (final Control radio : radioGroup) {
								final boolean selected = radio.statement() == active.statement();
								if (!sameValue(radio.statement().getBoundValue(s), selected)) changed.add(radio);
								radio.statement().setBoundValue(s, selected);
							}
							for (final Control radio : changed) {
								radio.statement().notifyChange(s, radio.statement() == active.statement());
							}
						} else {
							active.statement().activate(s, next);
						}
						return null;
					}, experiment, null);
				}
			});
		}
		refreshControls();
		return true;
	}

	@Override
	public void cancelControlInteraction() {
		final Interaction previous = interaction.getAndSet(null);
		final UiControlStatement previousHover = hovered;
		hovered = null;
		if (previous != null || previousHover != null) refreshControls();
	}

	private static boolean sameValue(final Object first, final Object second) {
		return first instanceof Number a && second instanceof Number b
				? Double.compare(a.doubleValue(), b.doubleValue()) == 0 : Objects.equals(first, second);
	}

	private static boolean sameControl(final Control first, final Control second) {
		return first.statement() == second.statement() && first.enabled() && first.bounds().equals(second.bounds())
				&& sameValue(first.value(), second.value()) && first.min() == second.min()
				&& first.max() == second.max() && first.step() == second.step()
				&& Objects.equals(first.group(), second.group());
	}

	@Override
	public void disableOn(final IDisplaySurface surface) {
		interaction.set(null);
		hovered = null;
		controls = List.of();
		controlSurface = null;
		super.disableOn(surface);
	}

	@Override
	public void dispose() {
		interaction.set(null);
		hovered = null;
		controls = List.of();
		drawingControls.clear();
		controlSurface = null;
		super.dispose();
	}

	@Override
	public boolean isProvidingCoordinates() {
		return false; // by default
	}

	@Override
	public boolean stayProportional() {
		return false;
	}

	@Override
	public boolean isProvidingWorldCoordinates() {
		return false; // by default
	}

}
