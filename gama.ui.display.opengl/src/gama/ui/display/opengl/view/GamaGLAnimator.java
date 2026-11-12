/*******************************************************************************************************
 *
 * GamaGLAnimator.java, in gama.ui.display.opengl, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.display.opengl.view;

import java.io.PrintStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import com.jogamp.opengl.FPSCounter;
import com.jogamp.opengl.GLAnimatorControl;
import com.jogamp.opengl.GLAutoDrawable;

import gama.api.utils.prefs.GamaPreferences;
import gama.api.utils.prefs.IPreferenceChangeListener.IPreferenceAfterChangeListener;
import gama.dev.DEBUG;
import gama.ui.shared.utils.WorkbenchHelper;

/**
 * Single Thread Animator (with target FPS)
 *
 * @author Alexis Drogoul, loosely adapted from (aqd@5star.com.tw)
 */
public class GamaGLAnimator implements Runnable, GLAnimatorControl, GLAnimatorControl.UncaughtExceptionHandler {

	/** The fps changed. */
	IPreferenceAfterChangeListener<Integer> fpsChanged = newValue -> targetFPS = newValue;

	private final IPreferenceAfterChangeListener<Boolean> capChanged = newValue -> capFPS = newValue;

	/** The cap FPS. */
	protected volatile boolean capFPS = GamaPreferences.Displays.OPENGL_CAP_FPS.getValue();

	/** The target FPS. */
	protected volatile int targetFPS = GamaPreferences.Displays.OPENGL_FPS.getValue();

	/** The animator thread. */
	protected final Thread animatorThread;

	/** Platform info for diagnostics. */
	private static final String OS = System.getProperty("os.name").toLowerCase();

	/** The paused. */
	volatile boolean paused = false;

	/** The native drawable; unrealized windows must wait rather than spin or count empty frames. */
	private final GLAutoDrawable window;
	/** The stop requested. */
	protected volatile boolean stopRequested = false;

	/** The fps update frames interval. */
	private int fpsUpdateFramesInterval = 50;
	/** The fps total duration. */
	private long fpsStartTime, fpsLastUpdateTime, fpsLastPeriod, fpsTotalDuration;

	/** The fps total frames. */
	private int fpsTotalFrames;
	/** The fps total. */
	private float fpsLast, fpsTotal;

	@Override
	public void resetFPSCounter() {
		fpsStartTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime()); // overwrite startTime to real init one
		fpsLastUpdateTime = fpsStartTime;
		fpsLastPeriod = 0;
		fpsTotalFrames = 0;
		fpsLast = 0f;
		fpsTotal = 0f;
		fpsLastPeriod = 0;
		fpsTotalDuration = 0;
	}

	@Override
	public int getUpdateFPSFrames() { return fpsUpdateFramesInterval; }

	@Override
	public long getFPSStartTime() { return fpsStartTime; }

	@Override
	public long getLastFPSUpdateTime() { return fpsLastUpdateTime; }

	@Override
	public long getLastFPSPeriod() { return fpsLastPeriod; }

	@Override
	public float getLastFPS() { return fpsLast; }

	@Override
	public int getTotalFPSFrames() { return fpsTotalFrames; }

	@Override
	public long getTotalFPSDuration() { return fpsTotalDuration; }

	@Override
	public float getTotalFPS() { return fpsTotal; }

	@Override
	public void setUpdateFPSFrames(final int frames, final PrintStream out) {
		fpsUpdateFramesInterval = frames;
	}

	/**
	 * Instantiates a new single thread GL animator.
	 *
	 * @param window
	 *            the canvas
	 */
	public GamaGLAnimator(final GLAutoDrawable window) {
		this.window = window;
		window.setAnimator(this);
		this.animatorThread = Thread.ofPlatform().name("Animator thread").unstarted(this);
		// if (DEBUG.IS_ON()) {
		// DEBUG.OUT("[GamaGLAnimator] Created animatorThread: " + animatorThread + " on OS: " + OS);
		// }
		GamaPreferences.Displays.OPENGL_FPS.onChange(fpsChanged);
		GamaPreferences.Displays.OPENGL_CAP_FPS.onChange(capChanged);
		setUpdateFPSFrames(FPSCounter.DEFAULT_FRAMES_PER_INTERVAL, null);
	}

	@Override
	public boolean isStarted() { return animatorThread.isAlive(); }

	@Override
	public Thread getThread() { return animatorThread; }

	@Override
	public boolean start() {
		// if (DEBUG.IS_ON()) { DEBUG.OUT("[GamaGLAnimator] start() called. Thread state: " +
		// animatorThread.getState()); }
		this.stopRequested = false;
		if (!animatorThread.isAlive()) {
			resetFPSCounter();
			animatorThread.start();
			// if (DEBUG.IS_ON()) { DEBUG.OUT("[GamaGLAnimator] animatorThread started."); }
		}
		// else if (DEBUG.IS_ON()) { DEBUG.OUT("[GamaGLAnimator] animatorThread already alive."); }
		return true;
	}

	@Override
	public boolean stop() {
		// if (DEBUG.IS_ON()) { DEBUG.OUT("[GamaGLAnimator] stop() called. Thread state: " + animatorThread.getState());
		// }
		this.stopRequested = true;
		LockSupport.unpark(animatorThread);
		if (!animatorThread.isAlive()) { removePreferenceListeners(); }
		if (WorkbenchHelper.isDisplayThread()) return true;
		try {
			if (animatorThread.isAlive()) {
				animatorThread.join(2000);
				// if (DEBUG.IS_ON()) { DEBUG.OUT("[GamaGLAnimator] animatorThread joined."); }
			}
			// else if (DEBUG.IS_ON()) { DEBUG.OUT("[GamaGLAnimator] animatorThread not alive at stop."); }
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		return true;
	}

	private void removePreferenceListeners() {
		GamaPreferences.Displays.OPENGL_FPS.removeChangeListener(fpsChanged);
		GamaPreferences.Displays.OPENGL_CAP_FPS.removeChangeListener(capChanged);
	}

	@Override
	public boolean isAnimating() { return isStarted() && !paused && !stopRequested; }

	@Override
	public boolean isPaused() { return paused; }

	@Override
	public boolean pause() {
		paused = true;
		// if (DEBUG.IS_ON()) { DEBUG.OUT("[GamaGLAnimator] pause() called. Thread state: " +
		// animatorThread.getState()); }
		return true;
	}

	@Override
	public boolean resume() {
		paused = false;
		LockSupport.unpark(animatorThread);
		// if (DEBUG.IS_ON()) {
		// DEBUG.OUT("[GamaGLAnimator] resume() called. Thread state: " + animatorThread.getState());
		// }
		return true;
	}

	@Override
	public void add(final GLAutoDrawable drawable) {}

	@Override
	public void remove(final GLAutoDrawable drawable) {}

	@Override
	public void run() {
		// while (!window.isRealized()) {}
		// if (DEBUG.IS_ON()) {
		// DEBUG.OUT("[GamaGLAnimator] run() started on thread: " + Thread.currentThread() + " (OS: " + OS + ")");
		// }
		try {
			while (!stopRequested) {
				final long frameStart = System.nanoTime();
				try {
					if (paused || !window.isRealized()) {
						LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(50));
						continue;
					}
					window.display();
					tickFPS();
					if (capFPS && targetFPS > 0) {
						final long remaining = TimeUnit.SECONDS.toNanos(1) / targetFPS
								- (System.nanoTime() - frameStart);
						if (remaining > 0) { LockSupport.parkNanos(remaining); }
					}
				} catch (final RuntimeException ex) {
					uncaughtException(this, null, ex);
				}
			}
		} finally {
			removePreferenceListeners();
		}
		// if (DEBUG.IS_ON()) { DEBUG.OUT("[GamaGLAnimator] run() exiting. Thread: " + Thread.currentThread()); }
	}

	@Override
	public UncaughtExceptionHandler getUncaughtExceptionHandler() { return this; }

	@Override
	public void setUncaughtExceptionHandler(final UncaughtExceptionHandler handler) {}

	@Override
	public void uncaughtException(final GLAnimatorControl animator, final GLAutoDrawable drawable,
			final Throwable cause) {
		DEBUG.ERR("Uncaught exception in animator & canvas:" + cause.getMessage());
		cause.printStackTrace();

	}

	/**
	 * Increases total frame count and updates values if feature is enabled and update interval is reached.<br>
	 *
	 * Shall be called by actual FPSCounter implementing renderer, after display a new frame.
	 *
	 */
	public final void tickFPS() {
		fpsTotalFrames++;
		if (fpsUpdateFramesInterval > 0 && fpsTotalFrames % fpsUpdateFramesInterval == 0) {
			final long now = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
			fpsLastPeriod = now - fpsLastUpdateTime;
			fpsLastPeriod = Math.max(fpsLastPeriod, 1); // div 0
			fpsLast = fpsUpdateFramesInterval * 1000f / fpsLastPeriod;
			fpsTotalDuration = now - fpsStartTime;
			fpsTotalDuration = Math.max(fpsTotalDuration, 1); // div 0
			fpsTotal = fpsTotalFrames * 1000f / fpsTotalDuration;
			fpsLastUpdateTime = now;
			// if (DEBUG.IS_ON()) {
			// // StringBuilder sb = new StringBuilder();
			// String fpsLastS = String.valueOf(fpsLast);
			// fpsLastS = fpsLastS.substring(0, fpsLastS.indexOf('.') + 2);
			// }
		}
	}

}