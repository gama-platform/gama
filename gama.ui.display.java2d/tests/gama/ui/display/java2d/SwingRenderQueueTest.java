package gama.ui.display.java2d;

import java.awt.EventQueue;
import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import gama.api.runtime.GeneralSynchronizer;

/** Standalone headless regression checks for the Java2D render scheduler. */
public final class SwingRenderQueueTest {

	public static void main(final String[] args) throws Exception {
		coalescesRequests();
		retainsRequestsDuringPainting();
		closesPendingRequests();
		closesDuringPainting();
		releasesAfterFailure();
	}

	private static void coalescesRequests() throws Exception {
		final AtomicInteger paints = new AtomicInteger();
		final var first = GeneralSynchronizer.withInitialPermits(0);
		final var second = GeneralSynchronizer.withInitialPermits(0);
		final var queue = new SwingRenderQueue(() -> {
			check(EventQueue.isDispatchThread(), "Painting must run on the EDT");
			check(permits(first) == 0 && permits(second) == 0, "Released before painting");
			paints.incrementAndGet();
		});
		EventQueue.invokeAndWait(() -> {
			for (int i = 0; i < 10_000; i++) {
				queue.request(first);
				queue.request(null);
			}
			queue.request(second);
			check(paints.get() == 0, "Painting must not block a UI caller");
		});
		drain();
		check(paints.get() == 1, "A burst must produce exactly one paint");
		check(permits(first) == 1 && permits(second) == 1, "Each synchronizer must be released once");
		queue.close();
	}

	private static void retainsRequestsDuringPainting() throws Exception {
		final AtomicInteger paints = new AtomicInteger();
		final var first = GeneralSynchronizer.withInitialPermits(0);
		final var next = GeneralSynchronizer.withInitialPermits(0);
		final CountDownLatch painting = new CountDownLatch(1);
		final CountDownLatch finish = new CountDownLatch(1);
		final CountDownLatch secondPaint = new CountDownLatch(1);
		final var queue = new SwingRenderQueue(() -> {
			if (paints.incrementAndGet() == 1) {
				painting.countDown();
				try {
					check(finish.await(5, TimeUnit.SECONDS), "Timed out waiting to finish the paint");
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new AssertionError(e);
				}
			} else {
				check(permits(first) == 1, "The previous frame must already be acknowledged");
				check(permits(next) == 0, "The next frame must not be acknowledged early");
				secondPaint.countDown();
			}
		});
		queue.request(first);
		try {
			check(painting.await(5, TimeUnit.SECONDS), "Painting did not start");
			for (int i = 0; i < 10_000; i++) { queue.request(next); }
			check(permits(first) == 0 && permits(next) == 0, "Acknowledged an unfinished paint");
		} finally {
			finish.countDown();
		}
		check(secondPaint.await(5, TimeUnit.SECONDS), "Lost an update made during painting");
		drain();
		check(paints.get() == 2 && permits(next) == 1, "The second frame must complete exactly once");
		queue.close();
	}

	private static void closesPendingRequests() throws Exception {
		final AtomicInteger paints = new AtomicInteger();
		final var pending = GeneralSynchronizer.withInitialPermits(0);
		final var afterClose = GeneralSynchronizer.withInitialPermits(0);
		final var queue = new SwingRenderQueue(paints::incrementAndGet);
		EventQueue.invokeAndWait(() -> {
			queue.request(pending);
			queue.close();
			queue.close();
			queue.request(afterClose);
		});
		drain();
		check(paints.get() == 0, "A disposed display must not paint");
		check(permits(pending) == 1 && permits(afterClose) == 1, "Closing must unblock pending updates");
	}

	private static void closesDuringPainting() throws Exception {
		final AtomicReference<SwingRenderQueue> queue = new AtomicReference<>();
		final var current = GeneralSynchronizer.withInitialPermits(0);
		final var pending = GeneralSynchronizer.withInitialPermits(0);
		queue.set(new SwingRenderQueue(() -> {
			queue.get().request(pending);
			queue.get().close();
			check(permits(current) == 0, "Closing must not acknowledge an unfinished paint");
			check(permits(pending) == 1, "Closing must cancel the next frame");
		}));
		queue.get().request(current);
		drain();
		drain();
		check(permits(current) == 1 && permits(pending) == 1, "Closing released an update twice");
	}

	private static void releasesAfterFailure() throws Exception {
		final AtomicInteger paints = new AtomicInteger();
		final AtomicReference<Throwable> failure = new AtomicReference<>();
		final AtomicReference<Thread.UncaughtExceptionHandler> previous = new AtomicReference<>();
		final var sync = GeneralSynchronizer.withInitialPermits(0);
		final var expected = new IllegalStateException("Expected paint failure");
		final var queue = new SwingRenderQueue(() -> {
			if (paints.incrementAndGet() == 1) throw expected;
		});
		EventQueue.invokeAndWait(() -> {
			previous.set(Thread.currentThread().getUncaughtExceptionHandler());
			Thread.currentThread().setUncaughtExceptionHandler((thread, error) -> failure.set(error));
		});
		try {
			queue.request(sync);
			drain();
			check(failure.get() == expected, "Rendering failures must not be swallowed");
			check(permits(sync) == 1, "A failed paint must not strand the simulation");
			queue.request(null);
			drain();
			check(paints.get() == 2, "A failed paint must not prevent later rendering");
		} finally {
			queue.close();
			EventQueue.invokeAndWait(() -> Thread.currentThread().setUncaughtExceptionHandler(previous.get()));
		}
	}

	private static int permits(final GeneralSynchronizer synchronizer) {
		try {
			final Field field = GeneralSynchronizer.class.getDeclaredField("semaphore");
			field.setAccessible(true);
			return ((Semaphore) field.get(synchronizer)).availablePermits();
		} catch (ReflectiveOperationException e) {
			throw new AssertionError(e);
		}
	}

	private static void drain() throws Exception {
		EventQueue.invokeAndWait(() -> {});
	}

	private static void check(final boolean condition, final String message) {
		if (!condition) throw new AssertionError(message);
	}
}
