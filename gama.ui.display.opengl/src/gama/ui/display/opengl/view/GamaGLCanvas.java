/*******************************************************************************************************
 *
 * GamaGLCanvas.java, in gama.ui.display.opengl, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.display.opengl.view;

import java.io.PrintStream;
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

import gama.core.runtime.PlatformHelper;
import gama.dev.DEBUG;
import gama.ui.display.opengl.OpenGL;
import gama.ui.display.opengl.camera.IMultiListener;
import gama.ui.display.opengl.renderer.IOpenGLRenderer;
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
	final Control canvas;

	/** The surface. */
	SWTOpenGLDisplaySurface surface;

	/** The drawable. */
	final GLWindow drawable;

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
		super(parent, SWT.NONE);
		parent.addControlListener(new ControlAdapter() {
			@Override
			public void controlMoved(final ControlEvent e) {
				DEBUG.OUT("Setting monitor for GLCanvas " + parent.getMonitor().toString());
				GamaGLCanvas.this.setMonitor(parent.getMonitor());
			}

			@Override
			public void controlResized(final ControlEvent e) {
				DEBUG.OUT("Setting monitor for GLCanvas " + parent.getMonitor().toString());
				GamaGLCanvas.this.setMonitor(parent.getMonitor());
			}
		});
		this.name = name;
		parent.setLayout(new FillLayout());
		this.setLayout(new FillLayout());
		final GLCapabilities cap = defineCapabilities();

		drawable = GLWindow.create(cap);
		drawable.setAutoSwapBufferMode(true);
		canvas = new NewtCanvasSWT(this, SWT.NONE, drawable);
		animator = new GamaGLAnimator(drawable);
		renderer.setCanvas(this);
		addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(final ControlEvent e) {
				/* Detached views have no title! */
				if (PlatformHelper.isMac()) {
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
			animator.stop();
			animator = null;
		}).start());
	}

	/**
	 * Sets the monitor.
	 *
	 * @param monitor
	 *            the new monitor
	 */
	protected void setMonitor(final Monitor monitor) { this.monitor = monitor; }

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
		drawable.setRealized(realized);
	}

	@Override
	public boolean isRealized() { return drawable.isRealized(); }

	@Override
	public int getSurfaceWidth() { return drawable.getSurfaceWidth(); }

	@Override
	public int getSurfaceHeight() { return drawable.getSurfaceHeight(); }

	@Override
	public boolean isGLOriented() { return drawable.isGLOriented(); }

	@Override
	public void swapBuffers() throws GLException {
		drawable.swapBuffers();
	}

	@Override
	public GLCapabilitiesImmutable getChosenGLCapabilities() { return drawable.getChosenGLCapabilities(); }

	@Override
	public GLCapabilitiesImmutable getRequestedGLCapabilities() { return drawable.getRequestedGLCapabilities(); }

	@Override
	public GLProfile getGLProfile() { return drawable.getGLProfile(); }

	@Override
	public NativeSurface getNativeSurface() { return drawable.getNativeSurface(); }

	@Override
	public long getHandle() { return drawable.getHandle(); }

	@Override
	public GLDrawableFactory getFactory() { return drawable.getFactory(); }

	@Override
	public GLDrawable getDelegatedDrawable() { return drawable.getDelegatedDrawable(); }

	@Override
	public GLContext getContext() { return drawable.getContext(); }

	@Override
	public GLContext setContext(final GLContext newCtx, final boolean destroyPrevCtx) {
		return drawable.setContext(newCtx, destroyPrevCtx);
	}

	@Override
	public void addGLEventListener(final GLEventListener listener) {
		drawable.addGLEventListener(listener);
	}

	@Override
	public void addGLEventListener(final int index, final GLEventListener listener) throws IndexOutOfBoundsException {
		drawable.addGLEventListener(index, listener);
	}

	@Override
	public int getGLEventListenerCount() { return drawable.getGLEventListenerCount(); }

	@Override
	public boolean areAllGLEventListenerInitialized() {
		return drawable.areAllGLEventListenerInitialized();
	}

	@Override
	public GLEventListener getGLEventListener(final int index) throws IndexOutOfBoundsException {
		return drawable.getGLEventListener(index);
	}

	@Override
	public boolean getGLEventListenerInitState(final GLEventListener listener) {
		return drawable.getGLEventListenerInitState(listener);
	}

	@Override
	public void setGLEventListenerInitState(final GLEventListener listener, final boolean initialized) {
		drawable.setGLEventListenerInitState(listener, initialized);
	}

	@Override
	public GLEventListener disposeGLEventListener(final GLEventListener listener, final boolean remove) {
		return drawable.disposeGLEventListener(listener, remove);
	}

	@Override
	public GLEventListener removeGLEventListener(final GLEventListener listener) {
		return drawable.removeGLEventListener(listener);
	}

	@Override
	public void setAnimator(final GLAnimatorControl animatorControl) throws GLException {
		drawable.setAnimator(animatorControl);
	}

	@Override
	public GLAnimatorControl getAnimator() { return drawable.getAnimator(); }

	@Override
	public Thread setExclusiveContextThread(final Thread t) throws GLException {
		return drawable.setExclusiveContextThread(t);
	}

	@Override
	public Thread getExclusiveContextThread() { return drawable.getExclusiveContextThread(); }

	@Override
	public boolean invoke(final boolean wait, final GLRunnable glRunnable) throws IllegalStateException {
		return drawable.invoke(wait, glRunnable);
	}

	@Override
	public boolean invoke(final boolean wait, final List<GLRunnable> glRunnables) throws IllegalStateException {
		return drawable.invoke(wait, glRunnables);
	}

	@Override
	public void flushGLRunnables() {
		drawable.flushGLRunnables();
	}

	@Override
	public void destroy() {
		drawable.destroy();
	}

	@Override
	public void display() {
		drawable.display();
	}

	@Override
	public void setAutoSwapBufferMode(final boolean enable) {
		drawable.setAutoSwapBufferMode(enable);
	}

	@Override
	public boolean getAutoSwapBufferMode() { return drawable.getAutoSwapBufferMode(); }

	@Override
	public void setContextCreationFlags(final int flags) {
		drawable.setContextCreationFlags(flags);
	}

	@Override
	public int getContextCreationFlags() { return drawable.getContextCreationFlags(); }

	@Override
	public GLContext createContext(final GLContext shareWith) {
		return drawable.createContext(shareWith);
	}

	@Override
	public GL getGL() { return drawable.getGL(); }

	@Override
	public GL setGL(final GL gl) {
		return drawable.setGL(gl);
	}

	@Override
	public Object getUpstreamWidget() { return drawable.getUpstreamWidget(); }

	@Override
	public RecursiveLock getUpstreamLock() { return drawable.getUpstreamLock(); }

	@Override
	public boolean isThreadGLCapable() { return drawable.isThreadGLCapable(); }

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
		final Window w = drawable;
		setWindowVisible(false);
		w.setFullscreen(true);
		w.setFullscreen(false);
		setWindowVisible(true);
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
			if (isDisposed() || canvas.isDisposed()) return;
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
			if (isDisposed() || canvas.isDisposed()) return;
			drawable.removeKeyListener(camera);
			drawable.removeMouseListener(camera);
		});
	}

	@Override
	public void setUpdateFPSFrames(final int frames, final PrintStream out) {
		animator.setUpdateFPSFrames(frames, out);
	}

	@Override
	public void resetFPSCounter() {
		animator.resetFPSCounter();
	}

	@Override
	public int getUpdateFPSFrames() {
		return animator.getUpdateFPSFrames();

	}

	@Override
	public long getFPSStartTime() { return animator.getFPSStartTime(); }

	@Override
	public long getLastFPSUpdateTime() {
		return animator.getLastFPSUpdateTime();

	}

	@Override
	public long getLastFPSPeriod() { return animator.getLastFPSPeriod(); }

	@Override
	public float getLastFPS() { return animator.getLastFPS(); }

	@Override
	public int getTotalFPSFrames() { return animator.getTotalFPSFrames(); }

	@Override
	public long getTotalFPSDuration() { return animator.getTotalFPSDuration(); }

	@Override
	public float getTotalFPS() { return animator.getTotalFPS(); }

	@Override
	public void setVisible(final boolean v) {
		// DEBUG.OUT("VISIBLE changed through composite : " + v);
		visible = v;
		setWindowVisible(v);
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

}
