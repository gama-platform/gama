package gama.ui.display.opengl.renderer.helpers;

import gama.api.types.geometry.IPoint;
import gama.ui.display.opengl.renderer.helpers.AbstractRendererHelper.Pass;

/** Keystone controls shared by the camera and the renderer lifecycle. */
public interface IKeystoneHelper {

	int getCornerSelected();

	IPoint getKeystoneCoordinates(int corner);

	void setKeystoneCoordinates(int corner, IPoint point);

	void setCornerSelected(int corner);

	void setCornerHovered(int corner);

	void resetCorner(int corner);

	int cornerSelected(IPoint mouse);

	int cornerHovered(IPoint mouse);

	void startDrawHelper();

	void stopDrawHelper();

	boolean isActive();

	Pass render();

	void reshape(int width, int height);

	void dispose();
}
