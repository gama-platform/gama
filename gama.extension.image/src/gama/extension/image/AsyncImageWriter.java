/*******************************************************************************************************
 *
 * AsyncImageWriter.java, in gama.extension.image, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.image;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import gama.api.GAMA;
import gama.api.exceptions.GamaRuntimeException;
import gama.dev.DEBUG;

/**
 * Writes copied images on a single background thread while preserving submission order.
 */
final class AsyncImageWriter {

	private static final int QUEUE_CAPACITY = 2;

	private static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(0, 1, 1, TimeUnit.SECONDS,
			new ArrayBlockingQueue<>(QUEUE_CAPACITY), runnable -> {
				final Thread thread = new Thread(runnable, "GAMA image writer");
				thread.setDaemon(false);
				return thread;
			},
			(task, executor) -> {
				try {
					executor.getQueue().put(task);
				} catch (final InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new RejectedExecutionException("Interrupted while waiting for the image writer queue", e);
				}
			});

	static {
		EXECUTOR.allowCoreThreadTimeOut(true);
	}

	private AsyncImageWriter() {}

	static synchronized void enqueue(final GamaImage image, final String format, final File file) throws IOException {
		final GamaImage copy = copy(image);
		try {
			EXECUTOR.execute(() -> write(copy, format, file));
		} catch (final RejectedExecutionException e) {
			copy.flush();
			throw new IOException("Unable to queue asynchronous image save for " + file, e);
		}
	}

	private static GamaImage copy(final GamaImage image) {
		final boolean alpha = image.getColorModel().hasAlpha();
		final GamaImage copy = GamaImage.ofDimensions(image.getWidth(), image.getHeight(), alpha);
		final int width = image.getWidth();
		final int height = image.getHeight();
		final int[] pixels = image.getRGB(0, 0, width, height, null, 0, width);
		copy.setRGB(0, 0, width, height, pixels, 0, width);
		return copy;
	}

	private static void write(final GamaImage image, final String format, final File file) {
		try {
			ImageHelper.writeImage(image, format, file);
		} catch (final IOException | RuntimeException e) {
			reportFailure(file, e);
		} finally {
			image.flush();
		}
	}

	private static void reportFailure(final File file, final Exception cause) {
		final var scope = GAMA.getRuntimeScope();
		final GamaRuntimeException error = GamaRuntimeException.create(cause, scope);
		error.addContext("Unable to asynchronously save image to " + file);
		if (!GAMA.reportError(scope, error, false)) { DEBUG.ERR(error.getMessage()); }
	}

}
