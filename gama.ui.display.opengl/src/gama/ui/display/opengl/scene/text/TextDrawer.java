/*******************************************************************************************************
 *
 * TextDrawer.java, in gama.ui.display.opengl, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.display.opengl.scene.text;

import static com.jogamp.common.nio.Buffers.newDirectDoubleBuffer;
import static com.jogamp.opengl.GL.GL_LINE_LOOP;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL2.GL_QUAD_STRIP;
import static com.jogamp.opengl.GL2GL3.GL_DOUBLE;
import static com.jogamp.opengl.fixedfunc.GLPointerFunc.GL_NORMAL_ARRAY;
import static com.jogamp.opengl.fixedfunc.GLPointerFunc.GL_TEXTURE_COORD_ARRAY;
import static com.jogamp.opengl.fixedfunc.GLPointerFunc.GL_VERTEX_ARRAY;
import static com.jogamp.opengl.glu.GLU.GLU_TESS_BEGIN;
import static com.jogamp.opengl.glu.GLU.GLU_TESS_COMBINE;
import static com.jogamp.opengl.glu.GLU.GLU_TESS_EDGE_FLAG;
import static com.jogamp.opengl.glu.GLU.GLU_TESS_END;
import static com.jogamp.opengl.glu.GLU.GLU_TESS_ERROR;
import static com.jogamp.opengl.glu.GLU.GLU_TESS_VERTEX;
import static com.jogamp.opengl.glu.GLU.GLU_TESS_WINDING_NONZERO;
import static com.jogamp.opengl.glu.GLU.GLU_TESS_WINDING_ODD;
import static com.jogamp.opengl.glu.GLU.GLU_TESS_WINDING_RULE;
import static com.jogamp.opengl.glu.GLU.gluTessBeginContour;
import static com.jogamp.opengl.glu.GLU.gluTessBeginPolygon;
import static com.jogamp.opengl.glu.GLU.gluTessCallback;
import static com.jogamp.opengl.glu.GLU.gluTessEndContour;
import static com.jogamp.opengl.glu.GLU.gluTessEndPolygon;
import static com.jogamp.opengl.glu.GLU.gluTessNormal;
import static com.jogamp.opengl.glu.GLU.gluTessProperty;
import static com.jogamp.opengl.glu.GLU.gluTessVertex;
import static java.awt.geom.PathIterator.SEG_CLOSE;
import static java.awt.geom.PathIterator.SEG_LINETO;
import static java.awt.geom.PathIterator.SEG_MOVETO;
import static java.awt.geom.PathIterator.WIND_EVEN_ODD;

import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.nio.DoubleBuffer;

import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUtessellator;
import com.jogamp.opengl.util.gl2.GLUT;

import gama.api.types.color.IColor;
import gama.api.types.font.IFont;
import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.IPoint;
import gama.api.ui.layers.IDrawingAttributes;
import gama.api.utils.geometry.AxisAngle;
import gama.api.utils.geometry.GamaCoordinateSequenceFactory;
import gama.api.utils.geometry.ICoordinates;
import gama.ui.display.opengl.ITesselator;
import gama.ui.display.opengl.OpenGL;
import gama.ui.display.opengl.scene.ObjectDrawer;

/**
 *
 * The class StringDrawer.
 *
 * @author drogoul
 * @since 4 mai 2013
 *
 */

public class TextDrawer extends ObjectDrawer<StringObject> implements ITesselator {

	/** The temp. */
	// Utilities
	ICoordinates temp = GamaCoordinateSequenceFactory.ofLength(4);

	/** The normal. */
	IPoint normal = GamaPointFactory.create();

	/** The tobj. */
	final GLUtessellator tobj = GLU.gluNewTess();

	/** The previous Y. */
	double previousX = Double.MIN_VALUE, previousY = Double.MIN_VALUE;

	/**
	 * Position of the placeholder normal written for the first vertex of the current contour in sideNormalBuffer.
	 * Used by endContour to overwrite the placeholder with the correct closing-edge normal.
	 */
	int contourStartNormalPos = -1;

	/** X/Y of the first vertex of the current contour, used to compute the closing-edge normal in endContour. */
	double contourStartX = Double.MIN_VALUE, contourStartY = Double.MIN_VALUE;

	/** The current index. */
	int currentIndex = -1;

	/** The Constant BUFFER_SIZE. */
	// Constants
	private static final int BUFFER_SIZE = 1000000;

	/** The Constant AT. */
	private final static AffineTransform AT = AffineTransform.getScaleInstance(1.0, -1.0);

	/** The Constant context. */
	private static final FontRenderContext context = new FontRenderContext(new AffineTransform(), true, true);

	/** The face vertex buffer. */
	// Buffers
	DoubleBuffer faceVertexBuffer = newDirectDoubleBuffer(BUFFER_SIZE);

	/** The face texture buffer. */
	DoubleBuffer faceTextureBuffer = newDirectDoubleBuffer(BUFFER_SIZE * 2 / 3);

	/** The indices. */
	int[] indices = new int[1000]; // Indices of the "move_to" or "close"

	/** The side normal buffer. */
	private DoubleBuffer sideNormalBuffer = newDirectDoubleBuffer(BUFFER_SIZE);

	/** The side quads buffer. */
	private DoubleBuffer sideQuadsBuffer = newDirectDoubleBuffer(BUFFER_SIZE); // Contains the sides

	/** The border. */
	// Properties
	IColor border;

	/** The depth. */
	double width, height, depth;

	/**
	 * Instantiates a new text drawer.
	 *
	 * @param gl
	 *            the gl
	 */
	public TextDrawer(final OpenGL gl) {
		super(gl);
		gluTessCallback(tobj, GLU_TESS_BEGIN, this);
		gluTessCallback(tobj, GLU_TESS_END, this);
		gluTessCallback(tobj, GLU_TESS_ERROR, this);
		gluTessCallback(tobj, GLU_TESS_VERTEX, this);
		gluTessCallback(tobj, GLU_TESS_COMBINE, this);
		gluTessCallback(tobj, GLU_TESS_EDGE_FLAG, this);
	}

	@Override
	protected void _draw(final StringObject s) {
		IDrawingAttributes attributes = s.getAttributes();
		if (!attributes.isPerspective()) {
			drawBitmap(s.getObject(), attributes);
		} else {
			Font font = attributes.getFont().getAwtFont();
			Shape shape = font.createGlyphVector(context, s.getObject()).getOutline();
			final Rectangle2D bounds = shape.getBounds2D();
			this.depth = attributes.getDepth();
			this.border = attributes.getBorder();
			this.width = bounds.getWidth();
			this.height = bounds.getHeight();
			faceVertexBuffer.clear();
			faceTextureBuffer.clear();
			sideNormalBuffer.clear();
			sideQuadsBuffer.clear();
			currentIndex = -1;
			// Reset the previous-vertex state so that the first contour of this string
			// does not inherit stale coordinates from the previous drawn string.
			previousX = Double.MIN_VALUE;
			previousY = Double.MIN_VALUE;
			contourStartX = Double.MIN_VALUE;
			contourStartY = Double.MIN_VALUE;
			contourStartNormalPos = -1;
			process(shape.getPathIterator(AT, attributes.getPrecision()));
			// process() already flips all buffers internally
			drawText(attributes, bounds.getY());
		}

	}

	/**
	 * Draw bitmap.
	 *
	 * @param object
	 *            the object
	 * @param attributes
	 *            the attributes
	 */
	private void drawBitmap(final String object, final IDrawingAttributes attributes) {
		int fontToUse = GLUT.BITMAP_HELVETICA_18;
		final IFont f = attributes.getFont();
		if (f != null) {
			if (f.getSize() < 10) {
				fontToUse = GLUT.BITMAP_HELVETICA_10;
			} else if (f.getSize() < 16) { fontToUse = GLUT.BITMAP_HELVETICA_12; }
		}
		gl.pushMatrix();
		final AxisAngle rotation = attributes.getRotation();
		final IPoint p = attributes.getLocation();

		if (rotation != null) {
			gl.translateBy(p.getX(), p.getY(), p.getZ());
			final IPoint axis = rotation.getAxis();
			// AD Change to a negative rotation to fix Issue #1514
			gl.rotateBy(-rotation.getAngle(), axis.getX(), axis.getY(), axis.getZ());
			// Voids the location so as to make only one translation
			p.setLocation(0, 0, 0);
		}

		gl.rasterText(object, fontToUse, p.getX(), p.getY(), p.getZ());
		gl.popMatrix();
	}

	/**
	 * Process.
	 *
	 * @param pi
	 *            the pi
	 */
	void process(final PathIterator pi) {
		boolean wireframe = gl.isWireframe();
		if (!wireframe) {
			gluTessProperty(tobj, GLU_TESS_WINDING_RULE,
					pi.getWindingRule() == WIND_EVEN_ODD ? GLU_TESS_WINDING_ODD : GLU_TESS_WINDING_NONZERO);
			gluTessNormal(tobj, 0, 0, -1);
			gluTessBeginPolygon(tobj, (double[]) null);
		}
		double x0 = 0, y0 = 0;
		while (!pi.isDone()) {
			final var coords = new double[6];
			switch (pi.currentSegment(coords)) {
				case SEG_MOVETO:
					// We begin a new contour within the global polygon
					// If we are solid, we pass the information to the tesselation algorithm
					if (!wireframe) {
						gluTessBeginContour(tobj);
						gluTessVertex(tobj, coords, 0, coords);
					}
					x0 = coords[0];
					y0 = coords[1];
					beginNewContour();
					addContourVertex0(x0, y0);
					break;
				case SEG_LINETO:
					// If we are solid, we pass the coordinates to the tesselation algorithm
					if (!wireframe) { gluTessVertex(tobj, coords, 0, coords); }
					// We also pass the coordinates to the side buffer, which will decide whether to create depth
					// level coordinates and compute the normal vector associated with this vertex
					addContourVertex0(coords[0], coords[1]);
					break;
				case SEG_CLOSE:
					if (!wireframe) { gluTessEndContour(tobj); }
					// We close the contour by adding it explicitly to the sideBuffer in order to close the loop
					addContourVertex0(x0, y0);
					endContour();
			}
			pi.next();
		}
		if (!wireframe) { gluTessEndPolygon(tobj); }
		sideQuadsBuffer.flip();
		sideNormalBuffer.flip();
		faceVertexBuffer.flip();
		if (faceTextureBuffer != null) { faceTextureBuffer.flip(); }
	}

	/**
	 * Draw text.
	 *
	 * @param attributes
	 *            the attributes
	 * @param y
	 *            the y
	 */
	void drawText(final IDrawingAttributes attributes, final double y) {

		final IPoint p = attributes.getLocation();

		IColor previous = null;
		gl.pushMatrix();
		try {
			IPoint anchor = attributes.getAnchor();
			applyRotation(attributes, p);
			final float scale = 1f / (float) gl.getRenderer().getAbsoluteRatioBetweenPixelsAndModelsUnits();
			gl.translateBy(p.getX() - width * scale * anchor.getX(), p.getY() + y * scale * anchor.getY(),
					p.getZ() + gl.getCurrentZTranslation());
			gl.scaleBy(scale, scale, scale);
			if (!gl.isWireframe()) {
				previous = drawFacesAndBorder(previous);
			} else {
				previous = drawWireframe(previous);
			}
		} finally {
			if (previous != null) { gl.setCurrentColor(previous); }
			gl.popMatrix();
		}
	}

	/**
	 * Draw wireframe.
	 *
	 * @param previous
	 *            the previous
	 * @return the color
	 */
	private IColor drawWireframe(IColor previous) {
		// Wireframe case
		if (border != null) {
			previous = gl.getCurrentColor();
			gl.setCurrentColor(border);
		}
		if (depth == 0) {
			// We use the quads side buffer to render only the top (lines)
			drawBorder();
		} else {
			drawSide();
		}
		return previous;
	}

	/**
	 * Draw faces and border.
	 *
	 * @param previous
	 *            the previous
	 * @return the color
	 */
	private IColor drawFacesAndBorder(IColor previous) {
		// Solid case
		drawFace(depth == 0);
		if (depth > 0) {
			gl.translateBy(0, 0, depth);
			drawFace(true);
			gl.translateBy(0, 0, -depth);
			drawSide();
		}
		if (border != null) {
			previous = gl.getCurrentColor();
			gl.setCurrentColor(border);
			gl.translateBy(0, 0, depth + 5 * gl.getCurrentZIncrement());
			drawBorder();
			gl.translateBy(0, 0, -depth - 5 * gl.getCurrentZIncrement());
		}
		return previous;
	}

	/**
	 * Apply rotation.
	 *
	 * @param attributes
	 *            the attributes
	 * @param p
	 *            the p
	 */
	private void applyRotation(final IDrawingAttributes attributes, final IPoint p) {
		final AxisAngle rotation = attributes.getRotation();
		if (rotation != null) {
			gl.translateBy(p.getX(), p.getY(), p.getZ());
			final IPoint axis = rotation.getAxis();
			// AD Change to a negative rotation to fix Issue #1514
			gl.rotateBy(-rotation.getAngle(), axis.getX(), axis.getY(), axis.getZ());
			// Voids the location so as to make only one translation
			p.setLocation(0, 0, 0);
		}
	}

	/**
	 * End contour. Records the end position and retroactively fixes the placeholder normal written for the first vertex
	 * by copying the closing-edge normal (which was just written for the SEG_CLOSE vertex) to the placeholder position.
	 * This ensures the first quad face has the correct normal instead of the [0,0,1] placeholder.
	 */
	public void endContour() {
		indices[++currentIndex] = sideQuadsBuffer.position();
		// If we have depth and a valid placeholder to update, copy the closing-edge normal (the last
		// normal written by addContourVertex0 for the SEG_CLOSE vertex) to the placeholder position.
		if (depth > 0 && contourStartNormalPos >= 0) {
			final int currentPos = sideNormalBuffer.position();
			final int lastNormalPos = currentPos - 6; // each vertex writes 6 doubles of normals
			// Only copy if there is at least one edge normal (i.e. the last normal is not the placeholder itself)
			if (lastNormalPos > contourStartNormalPos) {
				final int savedPos = currentPos;
				// Read the closing-edge normal (written for the SEG_CLOSE vertex)
				sideNormalBuffer.position(lastNormalPos);
				final double nx = sideNormalBuffer.get();
				final double ny = sideNormalBuffer.get();
				final double nz = sideNormalBuffer.get();
				// Overwrite the placeholder at the start of the contour
				sideNormalBuffer.position(contourStartNormalPos);
				sideNormalBuffer.put(new double[] { nx, ny, nz, nx, ny, nz });
				// Restore position
				sideNormalBuffer.position(savedPos);
			}
			contourStartNormalPos = -1;
		}
	}

	/**
	 * Begin new contour.
	 */
	public void beginNewContour() {
		indices[++currentIndex] = sideQuadsBuffer.position();
		// Each new contour must not inherit the last vertex of the previous contour;
		// reset so that addContourVertex0 knows there is no "previous" vertex yet.
		previousX = Double.MIN_VALUE;
		previousY = Double.MIN_VALUE;
		contourStartX = Double.MIN_VALUE;
		contourStartY = Double.MIN_VALUE;
		contourStartNormalPos = -1;
	}

	/**
	 * Add a point at altitude zero: creates automatically a point at altitude "depth" if depth > 0 in the quads
	 *
	 * @param x
	 *            and y represent the new vertex to be added
	 *
	 */
	public void addContourVertex0(final double x, final double y) {
		sideQuadsBuffer.put(x).put(y).put(0);
		if (depth > 0) {
			// If depth > 0, then we will build side faces, and we need to calculate their normal.
			// We always emit a normal entry so that sideNormalBuffer stays parallel to sideQuadsBuffer
			// (both 2 entries per geometric point).
			if (previousX > Double.MIN_VALUE) {
				temp.setTo(previousX, previousY, 0, previousX, previousY, depth, x, y, 0, previousX, previousY, 0);
				temp.getNormal(true, 1, normal);
				// We add two normal vectors as the vertex buffer will be filled by 2 coordinates
				sideNormalBuffer.put(new double[] { normal.getX(), normal.getY(), normal.getZ(), normal.getX(),
						normal.getY(), normal.getZ() });
			} else {
				// First vertex of a new contour: write a placeholder normal [0,0,1].
				// We record the buffer position so endContour() can retroactively overwrite it with the
				// correct closing-edge normal (from last vertex back to this first vertex).
				contourStartNormalPos = sideNormalBuffer.position();
				contourStartX = x;
				contourStartY = y;
				sideNormalBuffer.put(new double[] { 0, 0, 1, 0, 0, 1 });
			}
			// And we store the upper face
			sideQuadsBuffer.put(x).put(y).put(depth);
		}
		previousX = x;
		previousY = y;
	}

	@Override
	public void dispose() {
		faceVertexBuffer = faceTextureBuffer = sideNormalBuffer = sideQuadsBuffer = null;
	}

	/**
	 * Draw side.
	 */
	public void drawSide() {
		if (sideQuadsBuffer.limit() == 0) return;
		// AD - See issue #3125
		if (gl.isRenderingKeystone()) {
			drawSideFallback(gl);
			return;
		}
		var ogl = gl.getGL();
		gl.enable(GL_VERTEX_ARRAY);
		gl.enable(GL_NORMAL_ARRAY);
		ogl.glNormalPointer(GL_DOUBLE, 0, sideNormalBuffer);
		ogl.glVertexPointer(3, GL_DOUBLE, 0, sideQuadsBuffer);
		for (var i = 0; i < currentIndex; i++) {
			ogl.getGL().glDrawArrays(GL_QUAD_STRIP, indices[i] / 3, (indices[i + 1] - indices[i]) / 3);
		}
		gl.disable(GL_NORMAL_ARRAY);
		gl.disable(GL_VERTEX_ARRAY);
	}

	/**
	 * Draw border.
	 */
	public void drawBorder() {
		if (sideQuadsBuffer.limit() == 0) return;
		if (gl.isRenderingKeystone()) {
			drawBorderFallback();
			return;
		}
		gl.enable(GL_VERTEX_ARRAY);
		var olg = gl.getGL();
		// To draw the border, we provide a stride of 0 (= 3*BYTES_PER_DOUBLE) if depth = 0, as the bottom
		// coordinates are contiguous, or 6*BYTES_PER_DOUBLE to take the upper coordinates into account
		olg.glVertexPointer(3, GL_DOUBLE, depth == 0 ? 0 : 6 * Double.SIZE / Byte.SIZE, sideQuadsBuffer);
		// We use the sides buffer to draw only the top contours. Depending on whether or not there is a
		// depth, we rely on the indices of the different contours as either every 3 ordinates (if depth ==
		// 0), or every 6 ordinates to account for the added 'z = depth' coordinates.
		var stride = depth == 0 ? 3 : 6;
		for (var i = 0; i < currentIndex; i++) {
			olg.glDrawArrays(GL_LINE_LOOP, indices[i] / stride, (indices[i + 1] - indices[i]) / stride);
		}
		gl.disable(GL_VERTEX_ARRAY);
	}

	/**
	 * Draw face.
	 *
	 * @param up
	 *            the up
	 */
	void drawFace(final boolean up) {
		if (faceVertexBuffer.limit() == 0) return;
		if (gl.isRenderingKeystone()) {
			drawFaceFallback(up);
			return;
		}
		gl.enable(GL_VERTEX_ARRAY);
		gl.outputNormal(0, 0, up ? 1 : -1);
		if (gl.isTextured()) {
			gl.enable(GL_TEXTURE_COORD_ARRAY);
			gl.getGL().glTexCoordPointer(2, GL_DOUBLE, 0, faceTextureBuffer);
		}
		gl.getGL().glVertexPointer(3, GL_DOUBLE, 0, faceVertexBuffer);
		gl.getGL().glDrawArrays(GL_TRIANGLES, 0, faceVertexBuffer.limit() / 3);
		if (gl.isTextured()) { gl.disable(GL_TEXTURE_COORD_ARRAY); }
		gl.disable(GL_VERTEX_ARRAY);
	}

	@Override
	public void drawVertex(final int i, final double x, final double y, final double z) {
		if (gl.isTextured()) { faceTextureBuffer.put(x / width).put(y / height); }
		faceVertexBuffer.put(x).put(y).put(z);
	}

	@Override
	public void combine(final double[] coords, final Object[] data, final float[] weight, final Object[] outData) {
		outData[0] = data[0];
	}

	/**
	 * Fallback methods: use direct draw to OpenGL.
	 */

	public void drawFaceFallback(final boolean up) {
		if (faceVertexBuffer.limit() == 0) return;
		gl.beginDrawing(GL_TRIANGLES);
		gl.outputNormal(0, 0, up ? 1 : -1);
		for (var i = 0; i < faceVertexBuffer.limit(); i += 3) {
			if (gl.isTextured()) {
				gl.outputTexCoord(faceTextureBuffer.get(2 * i / 3), faceTextureBuffer.get(2 * i / 3 + 1));
			}
			gl.getGL().glVertex3d(faceVertexBuffer.get(i), faceVertexBuffer.get(i + 1), faceVertexBuffer.get(i + 2));
		}
		gl.endDrawing();
	}

	/**
	 * Draw side fallback.
	 *
	 * @param openGL
	 *            the open GL
	 */
	public void drawSideFallback(final OpenGL openGL) {
		if (sideQuadsBuffer.limit() == 0) return;
		var i = -1;
		while (i < currentIndex) {
			var begin = indices[++i];
			var end = indices[++i];
			openGL.beginDrawing(GL_QUAD_STRIP);
			for (var index = begin; index < end; index += 3) {
				openGL.outputNormal(sideNormalBuffer.get(index), sideNormalBuffer.get(index + 1),
						sideNormalBuffer.get(index + 2));
				openGL.outputVertex(sideQuadsBuffer.get(index), sideQuadsBuffer.get(index + 1),
						sideQuadsBuffer.get(index + 2));
			}
			openGL.endDrawing();
		}

	}

	/**
	 * Draw border fallback.
	 */
	public void drawBorderFallback() {
		if (sideQuadsBuffer.limit() == 0) return;
		var stride = depth == 0 ? 3 : 6;
		var i = -1;
		while (i < currentIndex) {
			var begin = indices[++i];
			var end = indices[++i];
			gl.beginDrawing(GL_LINE_LOOP);
			for (var index = begin; index < end; index += stride) {
				gl.outputVertex(sideQuadsBuffer.get(index), sideQuadsBuffer.get(index + 1),
						sideQuadsBuffer.get(index + 2));
			}
			gl.endDrawing();
		}

	}

}