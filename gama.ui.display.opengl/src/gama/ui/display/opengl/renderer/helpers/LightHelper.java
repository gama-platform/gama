/*******************************************************************************************************
 *
 * LightHelper.java, in gama.ui.display.opengl, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.display.opengl.renderer.helpers;

import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_LIGHT0;

import java.awt.Color;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2ES1;
import com.jogamp.opengl.fixedfunc.GLLightingFunc;
import com.jogamp.opengl.util.gl2.GLUT;

import gama.core.metamodel.shape.GamaPoint;
import gama.core.outputs.layers.properties.ILightDefinition;
import gama.ui.display.opengl.OpenGL;
import gama.ui.display.opengl.renderer.IOpenGLRenderer;

/**
 * The Class LightHelper.
 */
public class LightHelper extends AbstractRendererHelper {

	/**
	 * Instantiates a new light helper.
	 *
	 * @param renderer
	 *            the renderer
	 */
	public LightHelper(final IOpenGLRenderer renderer) {
		super(renderer);
	}

	/**
	 * Sets the ambiant light.
	 *
	 * @param gl
	 *            the gl
	 * @param intensity
	 *            the ambient light value
	 */
	public void setAmbientLight(final ILightDefinition light) {
		Color c = !light.isActive() ? Color.black : light.getIntensity();
		final float[] array = { c.getRed() / 255.0f, c.getGreen() / 255.0f, c.getBlue() / 255.0f, 1.0f };
		getGL().glLightModelfv(GL2ES1.GL_LIGHT_MODEL_AMBIENT, array, 0);
	}

	@Override
	public void initialize() {
		// ambient
		setAmbientLight(data.getLights().get(ILightDefinition.ambient));
		// set material properties which will be assigned by glColor
		getGL().glColorMaterial(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_AMBIENT_AND_DIFFUSE);
		getGL().glLightModelf(GL2ES1.GL_LIGHT_MODEL_TWO_SIDE, GL.GL_TRUE);
		getGL().glEnable(GLLightingFunc.GL_COLOR_MATERIAL);
	}

	/**
	 * Update diffuse light value.
	 *
	 * @param openGL
	 *            the open GL
	 */
	public void updateDiffuseLightValue(final OpenGL openGL) {
		setAmbientLight(data.getLights().get(ILightDefinition.ambient));
		final GL2 gl = getGL();
		final double size = getMaxEnvDim() / 20;
		final double worldWidth = getRenderer().getEnvWidth();
		final double worldHeight = getRenderer().getEnvHeight();
		getData().getLights().forEach((name, light) -> {
			if (ILightDefinition.ambient.equals(name)) return;
			int id = GL_LIGHT0 + light.getId();
			if (light.isActive()) {
				String type = light.getType();
				Color c = light.getIntensity();
				gl.glEnable(id);
				final float[] color =
						{ c.getRed() / 255.0f, c.getGreen() / 255.0f, c.getBlue() / 255.0f, c.getAlpha() / 255.0f };
				openGL.getGL().glLightfv(id, GLLightingFunc.GL_DIFFUSE, color, 0);
				float[] lightPosition;
				if (ILightDefinition.direction.equals(type)) {
					GamaPoint p = light.getDirection();
					lightPosition = new float[] { -(float) p.getX(), (float) p.getY(), -(float) p.getZ(), 0 };
				} else {
					GamaPoint p = light.getLocation();
					lightPosition = new float[] { (float) p.getX(), -(float) p.getY(), (float) p.getZ(), 1 };
				}
				gl.glLightfv(id, GLLightingFunc.GL_POSITION, lightPosition, 0);
				// Get and set the attenuation (if it is not a direction light)
				if (!ILightDefinition.direction.equals(type)) {
					final double ca = light.getConstantAttenuation();
					final double l = light.getLinearAttenuation();
					final double q = light.getQuadraticAttenuation();
					gl.glLightf(id, GLLightingFunc.GL_CONSTANT_ATTENUATION, (float) ca);
					gl.glLightf(id, GLLightingFunc.GL_LINEAR_ATTENUATION, (float) l);
					gl.glLightf(id, GLLightingFunc.GL_QUADRATIC_ATTENUATION, (float) q);
				}
				// Get and set spot properties (if the light is a spot light)
				if (ILightDefinition.spot.equals(type)) {
					GamaPoint p = light.getDirection();
					float[] spotLight = { (float) p.x, -(float) p.y, (float) p.z, 0 };
					gl.glLightfv(id, GLLightingFunc.GL_SPOT_DIRECTION, spotLight, 0);
					final double spotAngle = light.getAngle();
					gl.glLightf(id, GLLightingFunc.GL_SPOT_CUTOFF, (float) spotAngle);
				}
				if (light.isDrawing()) {
					// disable the lighting during the time the light is drawn
					final boolean previous = openGL.setObjectLighting(false);
					drawLight(openGL, size, worldWidth, worldHeight, light, lightPosition);
					openGL.setObjectLighting(previous);
				}
			} else {
				gl.glDisable(id);
			}
		});

	}

	/** The Constant UP_VECTOR. */
	private final static GamaPoint UP_VECTOR_PLUS_Y = new GamaPoint.Immutable(0, 1, 0);

	/** The Constant UP_VECTOR_MINUS_Z. */
	private final static GamaPoint UP_VECTOR_MINUS_Z = new GamaPoint.Immutable(0, 0, -1);

	/** The Constant UP_VECTOR_PLUS_Z. */
	private final static GamaPoint UP_VECTOR_PLUS_Z = new GamaPoint.Immutable(0, 0, 1);

	/** The Constant arrow. */
	// private final static GamaShape SPOT = ((GamaShape) buildCone3D(2, 1, GamaPoint.Immutable.NULL_POINT))
	// .withRotation(new AxisAngle(UP_VECTOR_PLUS_Y, 90));

	/**
	 * Draw light.
	 *
	 * @param openGL
	 *            the open GL
	 * @param size
	 *            the size
	 * @param worldWidth
	 *            the world width
	 * @param worldHeight
	 *            the world height
	 * @param light
	 *            the light properties
	 * @param pos
	 *            the light position
	 */
	private void drawLight(final OpenGL openGL, final double size, final double worldWidth, final double worldHeight,
			final ILightDefinition light, final float[] pos) {

		// save the current color to re-set it at the end of this
		// part
		final Color currentColor = openGL.swapCurrentColor(light.getIntensity());
		// change the current color to the light color (the
		// representation of the color will have the same color as
		// the light in itself)
		final GLUT glut = new GLUT();
		GamaPoint dir = light.getDirection().normalized();
		final String type = light.getType();
		if (ILightDefinition.point.equals(type)) {
			openGL.pushMatrix();
			openGL.translateBy(pos[0], pos[1], pos[2]);
			glut.glutSolidSphere(size, 16, 16);
			openGL.popMatrix();
		} else if (ILightDefinition.spot.equals(type)) {
			openGL.pushMatrix();
			openGL.translateBy(pos[0], pos[1], pos[2] - size);
			GamaPoint up = UP_VECTOR_PLUS_Y;
			if (Math.abs(dir.x) < 0.0000000001 && Math.abs(dir.z) < 0.00000000001) { // If x and z are really small
				up = dir.y > 0 ? UP_VECTOR_MINUS_Z : UP_VECTOR_PLUS_Z;
			}
			GamaPoint axis = up.crossProductWith(dir).normalized();
			double angle = Math.acos(up.dotProductWith(dir));
			openGL.rotateBy((dir.x < 0 ? 90 : 270) - Math.toDegrees(angle), axis.x, axis.y, axis.z);
			openGL.rotateBy(90, up.x, up.y, up.z);

			openGL.getGlut().glutSolidCone(size / 2, size, 16, 16);
			// openGL.getGeometryDrawer().drawGeometry(SPOT.getInnerGeometry(), null, size, IShape.Type.CONE);
			// openGL.beginRasterTextMode();
			// openGL.rasterText("Dir " + dir + " Axis " + axis, GLUT.BITMAP_TIMES_ROMAN_24, 0, 0, 0);
			openGL.popMatrix();
		} else

		{
			// draw direction light : a line and an sphere at the end of the line.
			final int maxI = 3;
			final int maxJ = 3;
			for (int i = 0; i < maxI; i++) {
				for (int j = 0; j < maxJ; j++) {
					final double[] beginPoint = { i * worldWidth / maxI, -j * worldHeight / maxJ, size * 10 };
					final double[] endPoint = { i * worldWidth / maxI + dir.x * size * 3,
							-(j * worldHeight / maxJ) - dir.y * size * 3, size * 10 + dir.z * size * 3 };
					// draw the lines
					openGL.beginDrawing(GL.GL_LINES);
					openGL.drawVertex(0, beginPoint[0], beginPoint[1], beginPoint[2]);
					openGL.drawVertex(0, endPoint[0], endPoint[1], endPoint[2]);
					openGL.endDrawing();
					// draw the small sphere
					openGL.pushMatrix();
					openGL.translateBy(endPoint[0], endPoint[1], endPoint[2]);
					glut.glutSolidSphere(size / 5, 16, 16);
					openGL.popMatrix();
				}
			}
		}
		openGL.setCurrentColor(currentColor);

	}

	/**
	 * Draw.
	 */
	public void draw() {
		if (isActive()) {
			final OpenGL openGL = getOpenGL();
			openGL.pushMatrix();
			updateDiffuseLightValue(openGL);
			openGL.popMatrix();
		}
	}

	/**
	 * Checks if is active.
	 *
	 * @return true, if is active
	 */
	public boolean isActive() { return getData().isLightOn(); }

}
