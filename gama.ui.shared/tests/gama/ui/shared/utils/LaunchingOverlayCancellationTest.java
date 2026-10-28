package gama.ui.shared.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/** Standalone cancellation-dispatch checks; no SWT display is needed. */
public final class LaunchingOverlayCancellationTest {

	public static void main(final String[] args) throws Exception {
		final CountDownLatch canceled = new CountDownLatch(1);
		final Thread caller = Thread.currentThread();
		final AtomicBoolean offUIThread = new AtomicBoolean();
		final var normal = new LaunchingOverlay(null, "", "", null, null, () -> {
			offUIThread.set(Thread.currentThread() != caller);
			canceled.countDown();
		});
		final Field visibility = LaunchingOverlay.class.getDeclaredField("showCancelButton");
		visibility.setAccessible(true);
		if (!visibility.getBoolean(normal)) throw new AssertionError("Legacy constructor must retain cancellation");
		final var launcher = new LaunchingOverlay(null, "", "", null, null, () -> {}, false);
		if (visibility.getBoolean(launcher)) throw new AssertionError("Cancellation must be optional");
		final Method cancel = LaunchingOverlay.class.getDeclaredMethod("handleCancel");
		cancel.setAccessible(true);
		cancel.invoke(normal);
		if (!canceled.await(5, TimeUnit.SECONDS)) throw new AssertionError("Cancel action was ignored");
		if (!offUIThread.get()) throw new AssertionError("Cancellation must not block the UI thread");
		System.out.println("Overlay cancellation-dispatch checks passed");
	}
}
