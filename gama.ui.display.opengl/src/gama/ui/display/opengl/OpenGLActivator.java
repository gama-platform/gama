/*******************************************************************************************************
 *
 * OpenGLActivator.java, in gama.ui.display.opengl, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.display.opengl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.FutureTask;

import org.eclipse.core.runtime.FileLocator;
import org.osgi.framework.BundleContext;

import com.jogamp.common.util.JarUtil;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLOffscreenAutoDrawable;
import com.jogamp.opengl.GLProfile;

import gama.api.runtime.SystemInfo;
import gama.api.utils.prefs.GamaPreferences;
import gama.api.ui.displays.GraphicsInitializerRegistry;
import gama.dependencies.GamaBundleActivator;
import gama.dev.BANNER_CATEGORY;
import gama.dev.DEBUG;
import gama.ui.shared.utils.GraphicsInitialization;

/**
 * The Class OpenGLActivator.
 */
public class OpenGLActivator extends GamaBundleActivator {

	private static final FutureTask<Void> CAPABILITIES = new FutureTask<>(OpenGLActivator::gatherOpenGLProperties, null);

	/**
	 * Start.
	 *
	 * @param context
	 *            the context
	 * @throws Exception
	 *             the exception
	 */
	@Override
	public void initialize(final BundleContext context) {
		GraphicsInitializerRegistry.register(() -> {
			JarUtil.setResolver(url -> {
				try {
					final URL resolved = FileLocator.resolve(url);
					return new URI(resolved.getProtocol(), resolved.getPath(), null).toURL();
				} catch (final IOException | URISyntaxException e) {
					DEBUG.ERR("Unable to resolve OpenGL native library URL: " + url, e);
					return url;
				}
			});
			// Complete before Java2D creates a view: later JOGL initialization can deadlock with AWT.
			GLProfile.initSingleton();
		});
	}

	public static void initializeCapabilities() {
		GraphicsInitialization.initialize();
		GraphicsInitialization.await(CAPABILITIES);
	}

	/**
	 * Initialize GL preferences.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 21 déc. 2023
	 */
	private static void gatherOpenGLProperties() {
		GL gl = null;
		GLOffscreenAutoDrawable offscreen = null;
		String property = "Loading";
		BANNER_CATEGORY prefix = BANNER_CATEGORY.OpenGL;
		try {
			// GLCapabilities cap = new GLCapabilities(OpenGL.PROFILE);
			// Following problems on macOS with glLineWidth capabilities, stick for the moment to GL2.1
			GLCapabilities cap = new GLCapabilities(GLProfile.getMaxFixedFunc(true));
			cap.setDepthBits(24);
			cap.setDoubleBuffered(true);
			cap.setHardwareAccelerated(true);
			cap.setSampleBuffers(true);
			cap.setAlphaBits(8);
			cap.setNumSamples(8);
			offscreen =
					GLDrawableFactory.getFactory(OpenGL.PROFILE).createOffscreenAutoDrawable(null, cap, null, 10, 10);
			offscreen.display();
			GLContext gc = offscreen.getContext();
			gc.makeCurrent();
			gl = gc.getGL();
			DEBUG.BANNER(prefix, "Profile initialized", "version", gc.getGLVersionNumber().toString());
			float fresult[] = { 0.0f, 0.0f };
			int iresult[] = { 0 };

			property = "Line width range";
			gl.glGetFloatv(GL.GL_SMOOTH_LINE_WIDTH_RANGE, fresult, 0);
			DEBUG.BANNER(prefix, property, "between",
					String.valueOf(fresult[0]) + " and " + String.valueOf(fresult[1]));
			GamaPreferences.Displays.CORE_LINE_WIDTH.between(fresult[0], fresult[1]);
			property = "Line width granularity";
			gl.glGetFloatv(GL2GL3.GL_SMOOTH_LINE_WIDTH_GRANULARITY, fresult, 0);
			DEBUG.BANNER(prefix, property, "value", String.valueOf(fresult[0]));
			// GamaPreferences.Displays.CORE_LINE_WIDTH.step(fresult[0]);
			property = "Point size range";
			gl.glGetFloatv(GL.GL_POINT_SIZE, fresult, 0);
			DEBUG.BANNER(prefix, property, "between",
					String.valueOf(fresult[0]) + " and " + String.valueOf(fresult[1]));
			property = "Max anisotropy level";
			gl.glGetFloatv(GL.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, fresult, 0);
			OpenGL.ANISOTROPIC_LEVEL = fresult[0];
			DEBUG.BANNER(prefix, property, "value", String.valueOf(OpenGL.ANISOTROPIC_LEVEL));
			property = "Non-power of 2 texture size";
			DEBUG.BANNER(prefix, property, "supported", String.valueOf(gl.isNPOTTextureAvailable()));
			property = "Framebuffer objects";
			DEBUG.BANNER(prefix, property, "supported", String.valueOf(gl.hasFullFBOSupport()));
			property = "Shading language";
			DEBUG.BANNER(prefix, property, "supported", String.valueOf(gl.hasGLSL()));
			property = "Max texture size";
			gl.glGetIntegerv(GL.GL_MAX_TEXTURE_SIZE, iresult, 0);
			DEBUG.BANNER(prefix, property, "value", String.valueOf(iresult[0]) + "x" + String.valueOf(iresult[0]));
			property = "Graphics card in use";
			String graphicsCard = gl.glGetString(GL.GL_RENDERER) + " by " + gl.glGetString(GL.GL_VENDOR)
					+ " (GL version: " + gl.glGetString(GL.GL_VERSION) + ")";
			SystemInfo.setGraphicsCard(graphicsCard);
			DEBUG.BANNER(prefix, property, "is", graphicsCard);
		} catch (Exception e) {
			DEBUG.ERR("Unable to inspect OpenGL property: " + property, e);
			throw new IllegalStateException("Unable to inspect OpenGL capabilities", e);
		} finally {
			if (gl != null) { gl.getContext().destroy(); }
			if (offscreen != null) { offscreen.destroy(); }
		}

	}

}
