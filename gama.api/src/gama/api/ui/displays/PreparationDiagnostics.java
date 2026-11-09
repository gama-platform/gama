/*******************************************************************************************************
 *
 * PreparationDiagnostics.java, in gama.api, is part of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.ui.displays;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import gama.api.utils.prefs.GamaPreferences;
import gama.dev.DEBUG;

/** Producer-thread timings. Nested aspect/draw times are inclusive and must not be added together. */
public final class PreparationDiagnostics {

	private static final long REPORT_INTERVAL = TimeUnit.SECONDS.toNanos(5);

	private volatile boolean enabled;
	private long intervalStart, preparationStart, preparationNanos, layerDataNanos, aspectNanos, facetNanos, drawNanos;
	private long updates, skipped, aspects, draws, primitives;
	private final AtomicLong waitNanos = new AtomicLong();
	private final AtomicLong waits = new AtomicLong();
	private final Map<String, Long> layers = new LinkedHashMap<>();

	public void begin(final boolean openGL) {
		enabled = GamaPreferences.Displays.DISPLAY_PREPARATION_DIAGNOSTICS.getValue()
				|| openGL && GamaPreferences.Displays.OPENGL_DIAGNOSTICS.getValue();
		if (!enabled) {
			reset();
			waitNanos.set(0);
			waits.set(0);
			return;
		}
		preparationStart = System.nanoTime();
		if (intervalStart == 0) { intervalStart = preparationStart; }
	}

	public long startTimer() { return enabled ? System.nanoTime() : 0; }

	public void layerDataFinished(final long start) {
		if (start != 0) { layerDataNanos += System.nanoTime() - start; }
	}

	public void layerFinished(final String layer, final long start) {
		if (start != 0) { layers.merge(layer, System.nanoTime() - start, Long::sum); }
	}

	public void aspectFinished(final long start) {
		if (start != 0) {
			aspectNanos += System.nanoTime() - start;
			aspects++;
		}
	}

	public void facetsFinished(final long start) {
		if (start != 0) { facetNanos += System.nanoTime() - start; }
	}

	public void drawFinished(final long start) {
		if (start != 0) {
			drawNanos += System.nanoTime() - start;
			draws++;
		}
	}

	public void primitive() {
		if (enabled) { primitives++; }
	}

	public void renderWaitFinished(final long start) {
		if (start != 0 && enabled) {
			waitNanos.addAndGet(System.nanoTime() - start);
			waits.incrementAndGet();
		}
	}

	public void end(final String display, final boolean prepared) {
		if (!enabled) return;
		final long now = System.nanoTime();
		if (prepared) {
			preparationNanos += now - preparationStart;
			updates++;
		} else {
			skipped++;
		}
		if (now - intervalStart < REPORT_INTERVAL) return;
		DEBUG.LOG(String.format(Locale.ROOT,
				"[Display preparation] display=%s updates=%d skipped=%d CPU ms/update: total=%.3f layer-data=%.3f "
						+ "aspects(inclusive)=%.3f facets=%.3f draw-delegates=%.3f; aspects/update=%.1f draws/update=%.1f "
						+ "primitives/update=%.1f; render-wait total ms=%.3f waits=%d; layer total ms=%s",
				display, updates, skipped, perUpdate(preparationNanos), perUpdate(layerDataNanos),
				perUpdate(aspectNanos), perUpdate(facetNanos), perUpdate(drawNanos),
				(double) aspects / Math.max(1, updates), (double) draws / Math.max(1, updates),
				(double) primitives / Math.max(1, updates), waitNanos.getAndSet(0) / 1_000_000d,
				waits.getAndSet(0), layerTimes()));
		reset();
	}

	private double perUpdate(final long nanos) { return nanos / (1_000_000d * Math.max(1, updates)); }

	private String layerTimes() {
		final StringBuilder result = new StringBuilder();
		layers.forEach((name, nanos) -> {
			if (!result.isEmpty()) { result.append(", "); }
			result.append(name).append('=').append(String.format(Locale.ROOT, "%.3f", nanos / 1_000_000d));
		});
		return result.toString();
	}

	private void reset() {
		intervalStart = preparationNanos = layerDataNanos = aspectNanos = facetNanos = drawNanos = 0;
		updates = skipped = aspects = draws = primitives = 0;
		layers.clear();
	}
}
