/*******************************************************************************************************
 *
 * GamaGLCanvas.java, in gama.ui.display.opengl4, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.display.opengl4.view;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Monitor;

import com.jogamp.common.util.locks.RecursiveLock;
import com.jogamp.nativewindow.NativeSurface;
import com.jogamp.newt.Window;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.newt.swt.NewtCanvasSWT;
import com.jogamp.opengl.FPSCounter;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GLAnimatorControl;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLCapabilitiesImmutable;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLDrawable;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.GLRunnable;

import gama.api.runtime.SystemInfo;
import gama.dev.DEBUG;
import gama.ui.display.opengl4.OpenGL;
import gama.ui.display.opengl4.camera.IMultiListener;
import gama.ui.display.opengl4.renderer.IOpenGLRenderer;
import gama.ui.shared.bindings.IDelegateEventsToParent;
import gama.ui.shared.utils.WorkbenchHelper;

/**
 * The Class GamaGLCanvas.
 */
public class GamaGLCanvas extends Composite implements GLAutoDrawable, IDelegateEventsToParent, FPSCounter {

	static {
		DEBUG.OFF();
	}

	/** The canvas. */
	Control canvas;

	/** The drawable. */
	GLWindow drawable;

	/** The fps delegate. */
	GamaGLAnimator animator;

	/** The detached. */
	protected boolean detached = false;

	/** The name. */
	final String name;

	/** The visible. */
	volatile boolean visible;

	/** The monitor. */
	private Monitor monitor;

	/** The renderer owning this canvas. */
	private final IOpenGLRenderer renderer;

	/** GLEventListeners registered before the native peer exists. */
	private final List<GLEventListener> pendingGlListeners = new ArrayList<>();

	/** Camera listeners registered before the native peer exists. */
	private final List<IMultiListener> pendingCameraListeners = new ArrayList<>();

	/** Indicates that the native peer has just been created and not yet shown once. */
	private boolean nativePeerJustCreated;

	/**
	 * Instantiates a new gama GL canvas.
	 *
	 * @param parent
	 *            the parent
	 * @param renderer
	 *            the renderer
	 * @param name
	 *            for debug purposes
	 */
	public GamaGLCanvas(final Composite parent, final IOpenGLRenderer renderer, final String name) {
		this(parent, renderer, name, true);
	}

	/**
	 * Instantiates a new gama GL canvas with a specified initial visibility.
	 *
	 * @param parent
	 *            the parent
	 * @param renderer
	 *            the renderer
	 * @param name
	 *            for debug purposes
	 * @param initiallyVisible
	 *            whether both the SWT host control and the native GL window should start visible
	 */
	public GamaGLCanvas(final Composite parent, final IOpenGLRenderer renderer, final String name,
			final boolean initiallyVisible) {
		super(parent, SWT.NONE);
		this.renderer = renderer;
		visible = initiallyVisible;
		super.setVisible(initiallyVisible);
		setBackground(parent.getBackground());
		parent.addControlListener(new ControlAdapter() {
			@Override
			public void controlMoved(final ControlEvent e) {
				DEBUG.OUT("Setting monitor for GLCanvas " + parent.getMonitor().toString());
				GamaGLCanvas.this.setMonitor(parent.getMonitor());
				GamaGLCanvas.this.fixSurfaceScaleOnWindows();
			}

			@Override
			public void controlResized(final ControlEvent e) {
				DEBUG.OUT("Setting monitor for GLCanvas " + parent.getMonitor().toString());
				GamaGLCanvas.this.setMonitor(parent.getMonitor());
				GamaGLCanvas.this.fixSurfaceScaleOnWindows();
			}
		});
		this.name = name;
		parent.setLayout(new FillLayout());
		this.setLayout(new FillLayout());
		renderer.setCanvas(this);
		if (initiallyVisible) { ensureNativePeer(); }
		addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(final ControlEvent e) {
				/* Detached views have no title! */
				if (SystemInfo.isMac() || SystemInfo.isWindows()) {
					final var isDetached = parent.getShell().getText().length() == 0;
					if (isDetached) {
						if (!detached) {
							// DEBUG.OUT("Reparenting because of detached");
							reparentWindow();
							detached = true;
						}

					} else if (detached) {
						// DEBUG.OUT("Reparenting because of attached");
						reparentWindow();
						detached = false;
					}
				}
			}
		});
		addDisposeListener(e -> new Thread(() -> {
			if (animator != null) { animator.stop(); }
			animator = null;
		}).start());
	}

	/**
	 * Creates the heavyweight NEWT/SWT native peer the first time it is needed.
	 */
	private void ensureNativePeer() {
		if (drawable != null || isDisposed()) return;
		final GLCapabilities cap = defineCapabilities();
		drawable = GLWindow.create(cap);
		drawable.setAutoSwapBufferMode(true);
		canvas = new NewtCanvasSWT(this, SWT.NONE, drawable);
		canvas.setBackground(getBackground());
		canvas.setVisible(visible);
		for (final GLEventListener listener : pendingGlListeners) {
			drawable.addGLEventListener(listener);
		}
		for (final IMultiListener listener : pendingCameraListeners) {
			drawable.addKeyListener(listener);
			drawable.addMouseListener(listener);
		}
		animator = new GamaGLAnimator(drawable);
		drawable.setVisible(visible);
		nativePeerJustCreated = true;
		layout(true, true);
	}

	/**
	 * Returns whether the native peer was created since the last call, then clears the flag.
	 *
	 * @return {@code true} if the native peer has just been created, {@code false} otherwise
	 */
	public boolean consumeNativePeerJustCreated() {
		final boolean result = nativePeerJustCreated;
		nativePeerJustCreated = false;
		return result;
	}

	/**
	 * Sets the monitor.
	 *
	 * @param monitor
	 *            the new monitor
	 */
	protected void setMonitor(final Monitor monitor) { this.monitor = monitor; }

	/**
	 * Corrects the NEWT window pixel scale on Windows when DPI zoom is not 100%. JOGL 2.6.0's
	 * NewtCanvasSWT.updatePosSizeCheck() uses integer division to compute pixelScale (e.g. 500/400=1 at 125% zoom),
	 * causing the embedded NEWT window to be sized and positioned incorrectly. Calling setSurfaceScale() with the true
	 * fractional scale overrides the wrong value and triggers the correct Win32 SetWindowPos.
	 */
	private void fixSurfaceScaleOnWindows() {
		if (!SystemInfo.isWindows() || monitor == null) return;
		final int zoom = monitor.getZoom();
		if (zoom == 100) return;
		final float scale = zoom / 100f;
		WorkbenchHelper.asyncRun(() -> {
			if (drawable != null && drawable.isNativeValid()) {
				drawable.setSurfaceScale(new float[] { scale, scale });
			}
		});
	}

	@Override
	public Monitor getMonitor() { return monitor; }

	@Override
	protected void checkWidget() {}

	/**
	 * Define capabilities.
	 *
	 * @return the GL capabilities
	 * @throws GLException
	 *             the GL exception
	 */
	private GLCapabilities defineCapabilities() throws GLException {
		final GLCapabilities cap = new GLCapabilities(OpenGL.PROFILE);
		cap.setDepthBits(24);
		cap.setDoubleBuffered(true);
		cap.setHardwareAccelerated(true);
		cap.setSampleBuffers(true);
		cap.setAlphaBits(8);
		cap.setNumSamples(8);
		return cap;
	}

	@Override
	public void setRealized(final boolean realized) {
		ensureNativePeer();
		drawable.setRealized(realized);
	}

	@Override
	public boolean isRealized() { return drawable != null && drawable.isRealized(); }

	@Override
	public int getSurfaceWidth() { return drawable == null ? 0 : drawable.getSurfaceWidth(); }

	@Override
	public int getSurfaceHeight() { return drawable == null ? 0 : drawable.getSurfaceHeight(); }

	@Override
	public boolean isGLOriented() { return drawable != null && drawable.isGLOriented(); }

	@Override
	public void swapBuffers() throws GLException {
		if (drawable == null) return;
		drawable.swapBuffers();
	}

	@Override
	public GLCapabilitiesImmutable getChosenGLCapabilities() {
		return drawable == null ? null : drawable.getChosenGLCapabilities();
	}

	@Override
	public GLCapabilitiesImmutable getRequestedGLCapabilities() {
		return drawable == null ? null : drawable.getRequestedGLCapabilities();
	}

	@Override
	public GLProfile getGLProfile() { return drawable == null ? null : drawable.getGLProfile(); }

	@Override
	public NativeSurface getNativeSurface() { return drawable == null ? null : drawable.getNativeSurface(); }

	@Override
	public long getHandle() { return drawable == null ? 0L : drawable.getHandle(); }

	@Override
	public GLDrawableFactory getFactory() { return drawable == null ? null : drawable.getFactory(); }

	@Override
	public GLDrawable getDelegatedDrawable() { return drawable == null ? null : drawable.getDelegatedDrawable(); }

	@Override
	public GLContext getContext() { return drawable == null ? null : drawable.getContext(); }

	@Override
	public GLContext setContext(final GLContext newCtx, final boolean destroyPrevCtx) {
		ensureNativePeer();
		return drawable.setContext(newCtx, destroyPrevCtx);
	}

	@Override
	public void addGLEventListener(final GLEventListener listener) {
		if (drawable == null) {
			pendingGlListeners.add(listener);
		} else {
			drawable.addGLEventListener(listener);
		}
	}

	@Override
	public void addGLEventListener(final int index, final GLEventListener listener) throws IndexOutOfBoundsException {
		if (drawable == null) {
			pendingGlListeners.add(index, listener);
		} else {
			drawable.addGLEventListener(index, listener);
		}
	}

	@Override
	public int getGLEventListenerCount() { return drawable == null ? pendingGlListeners.size() : drawable.getGLEventListenerCount(); }

	@Override
	public boolean areAllGLEventListenerInitialized() { return drawable == null || drawable.areAllGLEventListenerInitialized(); }

	@Override
	public GLEventListener getGLEventListener(final int index) throws IndexOutOfBoundsException {
		return drawable == null ? pendingGlListeners.get(index) : drawable.getGLEventListener(index);
	}

	@Override
	public boolean getGLEventListenerInitState(final GLEventListener listener) {
		return drawable != null && drawable.getGLEventListenerInitState(listener);
	}

	@Override
	public void setGLEventListenerInitState(final GLEventListener listener, final boolean initialized) {
		if (drawable != null) { drawable.setGLEventListenerInitState(listener, initialized); }
	}

	@Override
	public GLEventListener disposeGLEventListener(final GLEventListener listener, final boolean remove) {
		if (drawable == null) {
			if (remove) { pendingGlListeners.remove(listener); }
			return listener;
		}
		return drawable.disposeGLEventListener(listener, remove);
	}

	@Override
	public GLEventListener removeGLEventListener(final GLEventListener listener) {
		if (drawable == null) {
			pendingGlListeners.remove(listener);
			return listener;
		}
		return drawable.removeGLEventListener(listener);
	}

	@Override
	public void setAnimator(final GLAnimatorControl animatorControl) throws GLException {
		ensureNativePeer();
		drawable.setAnimator(animatorControl);
	}

	@Override
	public GLAnimatorControl getAnimator() { return drawable.getAnimator(); }

	@Override
	public Thread setExclusiveContextThread(final Thread t) throws GLException {
		ensureNativePeer();
		return drawable.setExclusiveContextThread(t);
	}

	@Override
	public Thread getExclusiveContextThread() { return drawable == null ? null : drawable.getExclusiveContextThread(); }

	@Override
	public boolean invoke(final boolean wait, final GLRunnable glRunnable) throws IllegalStateException {
		return drawable != null && drawable.invoke(wait, glRunnable);
	}

	@Override
	public boolean invoke(final boolean wait, final List<GLRunnable> glRunnables) throws IllegalStateException {
		return drawable != null && drawable.invoke(wait, glRunnables);
	}

	@Override
	public void flushGLRunnables() { if (drawable != null) { drawable.flushGLRunnables(); } }

	@Override
	public void destroy() { if (drawable != null) { drawable.destroy(); } }

	@Override
	public void display() { if (drawable != null) { drawable.display(); } }

	@Override
	public void setAutoSwapBufferMode(final boolean enable) {
		ensureNativePeer();
		drawable.setAutoSwapBufferMode(enable);
	}

	@Override
	public boolean getAutoSwapBufferMode() { return drawable != null && drawable.getAutoSwapBufferMode(); }

	@Override
	public void setContextCreationFlags(final int flags) {
		ensureNativePeer();
		drawable.setContextCreationFlags(flags);
	}

	@Override
	public int getContextCreationFlags() { return drawable == null ? 0 : drawable.getContextCreationFlags(); }

	@Override
	public GLContext createContext(final GLContext shareWith) {
		ensureNativePeer();
		return drawable.createContext(shareWith);
	}

	@Override
	public GL getGL() { return drawable == null ? null : drawable.getGL(); }

	@Override
	public GL setGL(final GL gl) {
		ensureNativePeer();
		return drawable.setGL(gl);
	}

	@Override
	public Object getUpstreamWidget() { return drawable == null ? null : drawable.getUpstreamWidget(); }

	@Override
	public RecursiveLock getUpstreamLock() { return drawable == null ? null : drawable.getUpstreamLock(); }

	@Override
	public boolean isThreadGLCapable() { return drawable != null && drawable.isThreadGLCapable(); }

	/**
	 * Gets the NEWT window.
	 *
	 * @return the NEWT window
	 */
	public Window getNEWTWindow() { return drawable; }

	/**
	 * Reparent window.
	 */
	public void reparentWindow() {
		DEBUG.OUT("Entering making GLWindow " + name + " reparent ");
		if (!visible) return;
		final Window w = drawable;
		setWindowVisible(false);
		w.setFullscreen(true);
		w.setFullscreen(false);
		setWindowVisible(visible);
		fixSurfaceScaleOnWindows();
	}

	/**
	 * Sets the window visible.
	 *
	 * @param b
	 *            the new window visible
	 */
	public boolean setWindowVisible(final boolean b) {
		// DEBUG.OUT("Entering making GLWindow " + name + " visible " + b);
		final Window w = drawable;
		if (!w.isNativeValid()) return false;
		// DEBUG.OUT("Make GLWindow " + name + " visible: " + b);
		w.setVisible(b);
		// DEBUG.OUT("Make GLWindow " + name + " visible " + b + " succeeded");
		// surface.synchronizer.signalSurfaceIsRealized();
		return true;
	}

	@Override
	public boolean setFocus() {
		ensureNativePeer();
		if (canvas == null) return false;
		return canvas.setFocus();
	}

	/**
	 * Adds the camera listeners.
	 *
	 * @param camera
	 *            the camera
	 */
	public void addCameraListeners(final IMultiListener camera) {
		WorkbenchHelper.asyncRun(() -> {
			if (isDisposed()) return;
			if (drawable == null || canvas == null || canvas.isDisposed()) {
				if (!pendingCameraListeners.contains(camera)) { pendingCameraListeners.add(camera); }
				return;
			}
			drawable.addKeyListener(camera);
			drawable.addMouseListener(camera);
		});
	}

	/**
	 * Removes the camera listeners.
	 *
	 * @param camera
	 *            the camera
	 */
	public void removeCameraListeners(final IMultiListener camera) {
		WorkbenchHelper.asyncRun(() -> {
			if (isDisposed()) return;
			pendingCameraListeners.remove(camera);
			if (drawable == null || canvas == null || canvas.isDisposed()) return;
			drawable.removeKeyListener(camera);
			drawable.removeMouseListener(camera);
		});
	}

	@Override
	public void setUpdateFPSFrames(final int frames, final PrintStream out) {
		if (animator != null) { animator.setUpdateFPSFrames(frames, out); }
	}

	@Override
	public void resetFPSCounter() {
		if (animator != null) { animator.resetFPSCounter(); }
	}

	@Override
	public int getUpdateFPSFrames() { return animator == null ? 0 : animator.getUpdateFPSFrames(); }

	@Override
	public long getFPSStartTime() { return animator == null ? 0L : animator.getFPSStartTime(); }

	@Override
	public long getLastFPSUpdateTime() { return animator == null ? 0L : animator.getLastFPSUpdateTime(); }

	@Override
	public long getLastFPSPeriod() { return animator == null ? 0L : animator.getLastFPSPeriod(); }

	@Override
	public float getLastFPS() { return animator == null ? 0f : animator.getLastFPS(); }

	@Override
	public int getTotalFPSFrames() { return animator == null ? 0 : animator.getTotalFPSFrames(); }

	@Override
	public long getTotalFPSDuration() { return animator == null ? 0L : animator.getTotalFPSDuration(); }

	@Override
	public float getTotalFPS() { return animator == null ? 0f : animator.getTotalFPS(); }

	@Override
	public void setVisible(final boolean v) {
		// DEBUG.OUT("VISIBLE changed through composite : " + v);
		visible = v;
		if (v) { ensureNativePeer(); }
		if (canvas != null && !canvas.isDisposed()) { canvas.setVisible(v); }
		if (drawable != null) { setWindowVisible(v); }
		if (!isDisposed()) { super.setVisible(v); }
	}

	/**
	 * Gets the visible status.
	 *
	 * @return the visible status
	 */
	public boolean getVisibleStatus() { return visible; }

	/**
	 * Update visible status.
	 *
	 * @param v
	 *            the v
	 */
	public void updateVisibleStatus(final boolean v) {
		// DEBUG.OUT("VISIBLE changed through display : " + v);
		visible = v;
	}

	/**
	 * Starts the animator if needed and ensures it is running.
	 */
	public void startAnimator() {
		ensureNativePeer();
		if (animator == null) return;
		if (!animator.isStarted()) { animator.start(); }
		animator.resume();
	}

	/**
	 * Pauses the animator if it exists.
	 */
	public void pauseAnimator() {
		if (animator != null) { animator.pause(); }
	}

	/**
	 * Resumes the animator if it exists.
	 */
	public void resumeAnimator() {
		if (animator != null) { animator.resume(); }
	}

}
