/*******************************************************************************************************
 *
 * LightHelper.java, in gama.ui.display.opengl4, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.display.opengl4.renderer.helpers;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.gl2.GLUT;

import gama.api.types.color.GamaColorFactory;
import gama.api.types.color.IColor;
import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.IPoint;
import gama.api.ui.layers.ILightDefinition;
import gama.ui.display.opengl4.OpenGL;
import gama.ui.display.opengl4.renderer.IOpenGLRenderer;

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
		IColor c = !light.isActive() ? GamaColorFactory.BLACK : light.getIntensity();
		final float[] array = { c.red() / 255.0f, c.green() / 255.0f, c.blue() / 255.0f, 1.0f };
		// removed glLightfv
	}

	@Override
	public void initialize() {
		// ambient
		setAmbientLight(data.getLights().get(ILightDefinition.ambient));
		// set material properties which will be assigned by glColor
		// getGL().glColorMaterial(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_AMBIENT_AND_DIFFUSE);
		// removed glLightModelf
		// getGL().glEnable(GLLightingFunc.GL_COLOR_MATERIAL);
	}

	/**
	 * Update diffuse light value.
	 *
	 * @param openGL
	 *            the open GL
	 */
	public void updateDiffuseLightValue(final OpenGL openGL) {
		setAmbientLight(data.getLights().get(ILightDefinition.ambient));
		final GL4 gl = getGL();
		final double size = getMaxEnvDim() / 20;
		final double worldWidth = getRenderer().getEnvWidth();
		final double worldHeight = getRenderer().getEnvHeight();
		getData().getLights().forEach((name, light) -> {
			if (ILightDefinition.ambient.equals(name)) return;
			// GL_LIGHT0..GL_LIGHT7 and glEnable/glDisable for individual lights are removed in GL4 core profile.
			// Light properties must be passed to shaders via uniforms instead.
			if (light.isActive()) {
				String type = light.getType();
				IColor c = light.getIntensity();
				// Note: glLightfv/glLightf calls removed (fixed-function lighting not available in GL4 core).
				float[] lightPosition;
				if (ILightDefinition.direction.equals(type)) {
					IPoint p = light.getDirection();
					lightPosition = new float[] { -(float) p.getX(), (float) p.getY(), -(float) p.getZ(), 0 };
				} else {
					IPoint p = light.getLocation();
					lightPosition = new float[] { (float) p.getX(), -(float) p.getY(), (float) p.getZ(), 1 };
				}
				if (light.isDrawing()) {
					// disable the lighting during the time the light is drawn
					final boolean previous = openGL.setObjectLighting(false);
					drawLight(openGL, size, worldWidth, worldHeight, light, lightPosition);
					openGL.setObjectLighting(previous);
				}
			}
		});

	}

	/** The Constant UP_VECTOR. */
	private final static IPoint UP_VECTOR_PLUS_Y = GamaPointFactory.createImmutable(0, 1, 0);

	/** The Constant UP_VECTOR_MINUS_Z. */
	private final static IPoint UP_VECTOR_MINUS_Z = GamaPointFactory.createImmutable(0, 0, -1);

	/** The Constant UP_VECTOR_PLUS_Z. */
	private final static IPoint UP_VECTOR_PLUS_Z = GamaPointFactory.createImmutable(0, 0, 1);

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
		final IColor currentColor = openGL.swapCurrentColor(light.getIntensity());
		// change the current color to the light color (the
		// representation of the color will have the same color as
		// the light in itself)
		final GLUT glut = new GLUT();
		IPoint dir = light.getDirection().normalized();
		final String type = light.getType();
		if (ILightDefinition.point.equals(type)) {
			openGL.pushMatrix();
			openGL.translateBy(pos[0], pos[1], pos[2]);
			glut.glutSolidSphere(size, 16, 16);
			openGL.popMatrix();
		} else if (ILightDefinition.spot.equals(type)) {
			openGL.pushMatrix();
			// Desired direction. It seems that y and z need to be negated, but not x. Not completely sure why.
			dir = GamaPointFactory.create(dir.getX(), -dir.getY(), -dir.getZ());
			double coneRadius = Math.sin(Math.toRadians(light.getAngle())) * size;
			openGL.translateBy(pos[0], pos[1], pos[2]);
			if (dir.norm() > 1e-6) {
				IPoint rotationAxis = UP_VECTOR_PLUS_Z.crossProductWith(dir);
				double rotationAngle = Math.acos(dir.dotProductWith(UP_VECTOR_PLUS_Z));
				if (rotationAxis.getX() != 0 || rotationAxis.getY() != 0) {
					openGL.rotateBy(-Math.toDegrees(rotationAngle), rotationAxis.getX(), rotationAxis.getY(),
							rotationAxis.getZ());
				}
			}
			glut.glutSolidCone(coneRadius, size, 16, 16);
			openGL.popMatrix();
		} else {
			// draw direction light : a line and an sphere at the end of the line.
			final int maxI = 3;
			final int maxJ = 3;
			for (int i = 0; i < maxI; i++) {
				for (int j = 0; j < maxJ; j++) {
					final double[] beginPoint = { i * worldWidth / maxI, -j * worldHeight / maxJ, size * 10 };
					final double[] endPoint = { i * worldWidth / maxI + dir.getX() * size * 3,
							-(j * worldHeight / maxJ) - dir.getY() * size * 3, size * 10 + dir.getZ() * size * 3 };
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
