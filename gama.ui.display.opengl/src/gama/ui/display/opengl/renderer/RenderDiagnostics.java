package gama.ui.display.opengl.renderer;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import gama.api.utils.prefs.GamaPreferences;
import gama.dev.DEBUG;

/** Per-renderer, GL-thread diagnostics. Times measure CPU submission, not GPU completion. */
public final class RenderDiagnostics {

	private static final long REPORT_INTERVAL = TimeUnit.SECONDS.toNanos(5);

	private boolean enabled;
	private long intervalStart, frameStart, frameNanos, sceneNanos, pickingNanos;
	private long frames, pickingPasses, genericDraws, meshDraws, genericBytes, meshBytes;
	private long polygonHits, polygonMisses;
	private long objectReplays;
	private long mapBatchCalls, mapBatchDrawings;
	private long instancedCalls, instances;

	public void instancedDraw(final int count) {
		if (enabled) {
			instancedCalls++;
			instances += count;
		}
	}

	public void objectReplay() {
		if (enabled) { objectReplays++; }
	}

	public void mapBatch(final int drawings) {
		if (enabled) {
			mapBatchCalls++;
			mapBatchDrawings += drawings;
		}
	}

	public void beginFrame() {
		enabled = GamaPreferences.Displays.OPENGL_DIAGNOSTICS.getValue();
		if (!enabled) {
			intervalStart = 0;
			return;
		}
		frameStart = System.nanoTime();
		if (intervalStart == 0) { intervalStart = frameStart; }
	}

	public long startTimer() { return enabled ? System.nanoTime() : 0; }

	public void sceneFinished(final long start) {
		if (enabled) { sceneNanos += System.nanoTime() - start; }
	}

	public void pickingFinished(final long start) {
		if (enabled) {
			pickingNanos += System.nanoTime() - start;
			pickingPasses++;
		}
	}

	public void genericDraw(final long bytes) {
		if (enabled) {
			genericDraws++;
			genericBytes += bytes;
		}
	}

	public void genericUpload(final long bytes) {
		if (enabled) { genericBytes += bytes; }
	}

	public void meshDraw() {
		if (enabled) { meshDraws++; }
	}

	public void meshUpload(final long bytes) {
		if (enabled) { meshBytes += bytes; }
	}

	public void polygonCacheLookup(final boolean hit) {
		if (enabled) {
			if (hit) { polygonHits++; } else { polygonMisses++; }
		}
	}

	public void endFrame(final String display, final String backend) {
		if (!enabled) {
			reset();
			return;
		}
		final long now = System.nanoTime();
		frameNanos += now - frameStart;
		frames++;
		if (now - intervalStart < REPORT_INTERVAL) return;
		final boolean gl4 = backend.startsWith("gama.ui.display.opengl4.");
		final String detail = gl4
				? String.format(Locale.ROOT, "GL4 tracked calls/frame: generic=%.1f mesh=%.1f; "
						+ "buffer MiB/frame: generic=%.3f mesh=%.3f; polygon cache: hits=%d misses=%d; "
						+ "retained objects/frame=%.1f; map multi-draw calls/frame=%.1f drawings/frame=%.1f; "
						+ "instanced calls/frame=%.1f instances/frame=%.1f",
						(double) genericDraws / frames,
						(double) meshDraws / frames, genericBytes / (1024d * 1024d * frames),
						meshBytes / (1024d * 1024d * frames), polygonHits, polygonMisses,
						(double) objectReplays / frames, (double) mapBatchCalls / frames,
						(double) mapBatchDrawings / frames, (double) instancedCalls / frames,
						(double) instances / frames)
				: String.format(Locale.ROOT, "legacy polygon cache: hits=%d misses=%d", polygonHits, polygonMisses);
		DEBUG.LOG(String.format(Locale.ROOT,
				"[OpenGL diagnostics] display=%s backend=%s frames=%d CPU ms/frame: render=%.3f scene=%.3f "
						+ "picking total=%.3f passes=%d; %s",
				display, backend, frames, millisPerFrame(frameNanos), millisPerFrame(sceneNanos),
				pickingNanos / 1_000_000d, pickingPasses, detail));
		reset();
	}

	private double millisPerFrame(final long nanos) { return nanos / (1_000_000d * frames); }

	private void reset() {
		intervalStart = frameNanos = sceneNanos = pickingNanos = 0;
		frames = pickingPasses = genericDraws = meshDraws = genericBytes = meshBytes = 0;
		polygonHits = polygonMisses = 0;
		objectReplays = 0;
		mapBatchCalls = mapBatchDrawings = 0;
		instancedCalls = instances = 0;
	}
}
