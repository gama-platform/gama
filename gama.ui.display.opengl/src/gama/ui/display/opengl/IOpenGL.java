package gama.ui.display.opengl;

import java.awt.image.BufferedImage;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.GLProfile;

import gama.api.types.color.IColor;
import gama.api.types.geometry.IPoint;
import gama.api.types.geometry.IShape;
import gama.api.utils.geometry.IEnvelope;
import gama.api.utils.geometry.Rotation3D;
import gama.api.utils.geometry.Scaling3D;
import gama.api.utils.interfaces.IImageProvider;
import gama.core.util.file.GamaGeometryFile;
import gama.ui.display.opengl.renderer.helpers.AbstractRendererHelper.Pass;
import gama.ui.display.opengl.scene.AbstractObject;
import gama.ui.display.opengl.scene.ObjectDrawer;

/**
 * Rendering operations used by the shared scene, camera and display infrastructure.
 * Implementations own their GL context and GPU resources; shared code must not assume a fixed-function pipeline.
 */
public interface IOpenGL {

	int NO_TEXTURE = Integer.MAX_VALUE;

	GLProfile getProfile();

	GL2GL3 getGL();

	void setGL(GL gl);

	void reshape(GL gl, int width, int height);

	void initializeGLStates(IColor background);

	void dispose();

	Pass beginScene();

	ObjectDrawer<?, ?> getDrawerFor(AbstractObject<?, ?> object);

	void beginObject(AbstractObject<?, ?> object, boolean picking);

	void endObject(AbstractObject<?, ?> object, boolean picking);

	int getViewWidth();

	int getViewHeight();

	double getWorldWidth();

	double getWorldHeight();

	double getMaxEnvDim();

	int[] getViewport();

	void lookAt(IPoint position, IPoint target, IPoint up);

	void unproject(double x, double y, double depth, double[] result);

	double[] getPixelWidthAndHeightOfWorld();

	IPoint getRatios();

	void setZIncrement(double increment);

	void suspendZTranslation();

	void resumeZTranslation();

	void rotateModel();

	void setRotationMode(boolean rotating);

	boolean isInRotationMode();

	double sizeOfRotationElements();

	boolean setDisplayLighting(boolean lighted);

	boolean setObjectWireframe(boolean wireframe);

	void pushMatrix();

	void popMatrix();

	void pushIdentity(int mode);

	void push(int mode);

	void pop(int mode);

	void ortho(double left, double right, double bottom, double top, double near, double far);

	void translateBy(double x, double y, double z);

	void translateBy(IPoint point);

	void translateByYNegated(IPoint point);

	void rotateBy(double angle, double x, double y, double z);

	void rotateBy(Rotation3D rotation);

	void scaleBy(double x, double y, double z);

	void scaleBy(Scaling3D scaling);

	void beginDrawing(int style);

	void outputVertex(double x, double y, double z);

	void endDrawing();

	void setCurrentColor(IColor color);

	void setCurrentColor(IColor color, double alpha);

	void setCurrentColor(double red, double green, double blue, double alpha);

	void setCurrentObjectAlpha(double alpha);

	boolean isTextured();

	void deleteVolatileTextures();

	void cacheTexture(IImageProvider image);

	int getTextureId(IImageProvider image, boolean useCache);

	int getTextureId(BufferedImage image);

	void cacheGeometry(GamaGeometryFile geometry);

	IEnvelope getEnvelopeFor(Object object);

	void drawCachedGeometry(GamaGeometryFile geometry, IColor border);

	void drawCachedGeometry(IShape.Type type, IColor border);

	void runWithNames(Runnable drawing);

	/** Whether a cached drawing records GPU commands rather than replaying the drawing each frame. */
	boolean isDrawingStored();

	int compileAsList(Runnable drawing);

	void drawList(int id);

	void deleteList(Integer id);
}
