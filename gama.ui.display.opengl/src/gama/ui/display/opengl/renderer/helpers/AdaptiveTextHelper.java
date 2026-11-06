/*******************************************************************************************************
 *
 * AdaptiveTextHelper.java, in gama.ui.display.opengl, is part of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.display.opengl.renderer.helpers;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.ByteBuffer;
import java.util.List;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2GL3;

import gama.api.types.color.AdaptiveColor;
import gama.gaml.statements.draw.TextDrawingAttributes;
import gama.ui.display.opengl.renderer.IOpenGLRenderer;

/** Captures the rendered scene once per adaptive overlay, shared by both OpenGL backends. */
public final class AdaptiveTextHelper {
	private static final int MAX_SAMPLE_SIZE = 256;
	private final IOpenGLRenderer renderer;
	private final Target resolved = new Target();
	private final Target sampled = new Target();
	private BufferedImage scene;
	private ByteBuffer pixels;

	public AdaptiveTextHelper(final IOpenGLRenderer renderer) {
		this.renderer = renderer;
	}

	public void resolve(final List<TextDrawingAttributes> texts, final double offsetX, final double offsetY,
			final AdaptiveColor.Background background, final double controlOpacity) {
		if (texts.isEmpty()) return;
		final Rectangle2D view = new Rectangle2D.Double(
				-offsetX, -offsetY, renderer.getViewWidth(), renderer.getViewHeight());
		Rectangle2D area = null;
		for (final TextDrawingAttributes text : texts) {
			final Rectangle2D visible = text.getAdaptiveTextBounds().createIntersection(view);
			if (visible.isEmpty()) continue;
			if (area == null) {
				area = visible;
			} else {
				area.add(visible);
			}
		}
		if (area == null) {
			for (final TextDrawingAttributes text : texts) { text.resolveAdaptiveText(null, view, background, controlOpacity); }
			return;
		}
		final Rectangle2D captured = capture(new Rectangle2D.Double(
				area.getX() + offsetX, area.getY() + offsetY, area.getWidth(), area.getHeight()));
		captured.setRect(captured.getX() - offsetX, captured.getY() - offsetY, captured.getWidth(), captured.getHeight());
		for (final TextDrawingAttributes text : texts) { text.resolveAdaptiveText(scene, captured, background, controlOpacity); }
	}

	private Rectangle2D capture(final Rectangle2D area) {
		final GL2GL3 gl = renderer.getOpenGLHelper().getGL();
		final int[] viewport = new int[4];
		gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);
		if (viewport[2] <= 0 || viewport[3] <= 0) {
			throw new IllegalStateException("Adaptive text requires a non-empty OpenGL viewport");
		}
		final double scaleX = (double) viewport[2] / renderer.getViewWidth();
		final double scaleY = (double) viewport[3] / renderer.getViewHeight();
		final int left = viewport[0] + Math.clamp((int) Math.floor(area.getMinX() * scaleX), 0, viewport[2]);
		final int right = viewport[0] + Math.clamp((int) Math.ceil(area.getMaxX() * scaleX), 0, viewport[2]);
		final int bottom = viewport[1] + viewport[3]
				- Math.clamp((int) Math.ceil(area.getMaxY() * scaleY), 0, viewport[3]);
		final int top = viewport[1] + viewport[3]
				- Math.clamp((int) Math.floor(area.getMinY() * scaleY), 0, viewport[3]);
		final int width = right - left, height = top - bottom;
		final double reduction = Math.min(1, (double) MAX_SAMPLE_SIZE / Math.max(width, height));
		final int sampleWidth = Math.max(1, (int) Math.ceil(width * reduction));
		final int sampleHeight = Math.max(1, (int) Math.ceil(height * reduction));
		final int[] saved = new int[10];
		final int[] properties = { GL.GL_READ_FRAMEBUFFER_BINDING, GL.GL_DRAW_FRAMEBUFFER_BINDING,
				GL2GL3.GL_READ_BUFFER, GL2GL3.GL_DRAW_BUFFER, GL.GL_RENDERBUFFER_BINDING,
				GL.GL_PACK_ALIGNMENT, GL2GL3.GL_PACK_ROW_LENGTH, GL2GL3.GL_PACK_SKIP_ROWS,
				GL2GL3.GL_PACK_SKIP_PIXELS, GL2GL3.GL_PIXEL_PACK_BUFFER_BINDING };
		for (int i = 0; i < properties.length; i++) { gl.glGetIntegerv(properties[i], saved, i); }
		final boolean scissor = gl.glIsEnabled(GL.GL_SCISSOR_TEST);
		final int[] sourceReadBuffer = new int[1];
		boolean sourceBound = false;
		try {
			gl.glBindFramebuffer(GL.GL_READ_FRAMEBUFFER, saved[1]);
			gl.glGetIntegerv(GL2GL3.GL_READ_BUFFER, sourceReadBuffer, 0);
			sourceBound = true;
			gl.glReadBuffer(saved[3]);
			gl.glDisable(GL.GL_SCISSOR_TEST);
			// A multisampled source must first be resolved at its native size, before downsampling.
			resolved.resize(gl, width, height);
			gl.glBindFramebuffer(GL.GL_DRAW_FRAMEBUFFER, resolved.framebuffer[0]);
			gl.glBlitFramebuffer(left, bottom, right, top, 0, 0, width, height, GL.GL_COLOR_BUFFER_BIT, GL.GL_NEAREST);
			sampled.resize(gl, sampleWidth, sampleHeight);
			gl.glBindFramebuffer(GL.GL_READ_FRAMEBUFFER, resolved.framebuffer[0]);
			gl.glReadBuffer(GL.GL_COLOR_ATTACHMENT0);
			gl.glBindFramebuffer(GL.GL_DRAW_FRAMEBUFFER, sampled.framebuffer[0]);
			gl.glBlitFramebuffer(0, 0, width, height, 0, 0, sampleWidth, sampleHeight,
					GL.GL_COLOR_BUFFER_BIT, GL.GL_LINEAR);
			gl.glBindFramebuffer(GL.GL_READ_FRAMEBUFFER, sampled.framebuffer[0]);
			gl.glReadBuffer(GL.GL_COLOR_ATTACHMENT0);
			gl.glBindBuffer(GL2GL3.GL_PIXEL_PACK_BUFFER, 0);
			gl.glPixelStorei(GL.GL_PACK_ALIGNMENT, 1);
			gl.glPixelStorei(GL2GL3.GL_PACK_ROW_LENGTH, 0);
			gl.glPixelStorei(GL2GL3.GL_PACK_SKIP_ROWS, 0);
			gl.glPixelStorei(GL2GL3.GL_PACK_SKIP_PIXELS, 0);
			final int bytes = sampleWidth * sampleHeight * 4;
			if (pixels == null || pixels.capacity() != bytes) { pixels = Buffers.newDirectByteBuffer(bytes); }
			pixels.clear();
			gl.glReadPixels(0, 0, sampleWidth, sampleHeight, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, pixels);
			if (scene == null || scene.getWidth() != sampleWidth || scene.getHeight() != sampleHeight) {
				if (scene != null) { scene.flush(); }
				scene = new BufferedImage(sampleWidth, sampleHeight, BufferedImage.TYPE_INT_RGB);
			}
			final int[] rgb = ((DataBufferInt) scene.getRaster().getDataBuffer()).getData();
			for (int y = 0; y < sampleHeight; y++) {
				for (int x = 0; x < sampleWidth; x++) {
					final int index = (y * sampleWidth + x) * 4;
					rgb[(sampleHeight - 1 - y) * sampleWidth + x] = (pixels.get(index) & 255) << 16
							| (pixels.get(index + 1) & 255) << 8 | pixels.get(index + 2) & 255;
				}
			}
		} finally {
			if (sourceBound) {
				gl.glBindFramebuffer(GL.GL_READ_FRAMEBUFFER, saved[1]);
				gl.glReadBuffer(sourceReadBuffer[0]);
			}
			gl.glBindFramebuffer(GL.GL_READ_FRAMEBUFFER, saved[0]);
			gl.glReadBuffer(saved[2]);
			gl.glBindFramebuffer(GL.GL_DRAW_FRAMEBUFFER, saved[1]);
			gl.glDrawBuffer(saved[3]);
			gl.glBindRenderbuffer(GL.GL_RENDERBUFFER, saved[4]);
			gl.glPixelStorei(GL.GL_PACK_ALIGNMENT, saved[5]);
			gl.glPixelStorei(GL2GL3.GL_PACK_ROW_LENGTH, saved[6]);
			gl.glPixelStorei(GL2GL3.GL_PACK_SKIP_ROWS, saved[7]);
			gl.glPixelStorei(GL2GL3.GL_PACK_SKIP_PIXELS, saved[8]);
			gl.glBindBuffer(GL2GL3.GL_PIXEL_PACK_BUFFER, saved[9]);
			if (scissor) { gl.glEnable(GL.GL_SCISSOR_TEST); }
		}
		return new Rectangle2D.Double((left - viewport[0]) / scaleX,
				(viewport[1] + viewport[3] - top) / scaleY, width / scaleX, height / scaleY);
	}

	public void dispose() {
		final GL2GL3 gl = renderer.getOpenGLHelper().getGL();
		resolved.dispose(gl);
		sampled.dispose(gl);
		if (scene != null) { scene.flush(); }
		scene = null;
		pixels = null;
	}

	private static final class Target {
		private final int[] framebuffer = new int[1], renderbuffer = new int[1];
		private int width, height;

		void resize(final GL2GL3 gl, final int w, final int h) {
			if (width == w && height == h) return;
			if (framebuffer[0] == 0) {
				gl.glGenFramebuffers(1, framebuffer, 0);
				gl.glGenRenderbuffers(1, renderbuffer, 0);
			}
			gl.glBindFramebuffer(GL.GL_DRAW_FRAMEBUFFER, framebuffer[0]);
			gl.glBindRenderbuffer(GL.GL_RENDERBUFFER, renderbuffer[0]);
			gl.glRenderbufferStorage(GL.GL_RENDERBUFFER, GL.GL_RGBA8, w, h);
			gl.glFramebufferRenderbuffer(GL.GL_DRAW_FRAMEBUFFER, GL.GL_COLOR_ATTACHMENT0,
					GL.GL_RENDERBUFFER, renderbuffer[0]);
			gl.glDrawBuffer(GL.GL_COLOR_ATTACHMENT0);
			if (gl.glCheckFramebufferStatus(GL.GL_DRAW_FRAMEBUFFER) != GL.GL_FRAMEBUFFER_COMPLETE) {
				throw new IllegalStateException("Cannot allocate the scene capture for adaptive text");
			}
			width = w;
			height = h;
		}

		void dispose(final GL2GL3 gl) {
			if (framebuffer[0] != 0) { gl.glDeleteFramebuffers(1, framebuffer, 0); }
			if (renderbuffer[0] != 0) { gl.glDeleteRenderbuffers(1, renderbuffer, 0); }
			framebuffer[0] = renderbuffer[0] = width = height = 0;
		}
	}
}
