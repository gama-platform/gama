package gama.ui.display.java2d;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import gama.api.runtime.GeneralSynchronizer;

/**
 * Coalesces display updates on the EDT and acknowledges them only after the paint attempt finishes.
 */
final class SwingRenderQueue {

	private final Runnable paint;
	private final Set<GeneralSynchronizer> synchronizers = new HashSet<>();
	private boolean pending;
	private boolean closed;

	SwingRenderQueue(final Runnable paint) {
		this.paint = paint;
	}

	synchronized void request(final GeneralSynchronizer synchronizer) {
		if (closed) {
			if (synchronizer != null) { synchronizer.release(); }
			return;
		}
		if (synchronizer != null) { synchronizers.add(synchronizer); }
		if (pending) return;
		pending = true;
		EventQueue.invokeLater(this::render);
	}

	private void render() {
		final ArrayList<GeneralSynchronizer> toRelease;
		synchronized (this) {
			if (closed) return;
			toRelease = new ArrayList<>(synchronizers);
			synchronizers.clear();
			// Requests arriving during this paint belong to the next frame.
			pending = false;
		}
		try {
			paint.run();
		} finally {
			toRelease.forEach(GeneralSynchronizer::release);
		}
	}

	synchronized void close() {
		closed = true;
		synchronizers.forEach(GeneralSynchronizer::release);
		synchronizers.clear();
	}
}
