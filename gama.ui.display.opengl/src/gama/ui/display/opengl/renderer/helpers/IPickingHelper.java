package gama.ui.display.opengl.renderer.helpers;

import gama.api.ui.layers.IDrawingAttributes;

/** Selection lifecycle independent of GL2 selection buffers or GL4 colour picking. */
public interface IPickingHelper {

	void setPicking(boolean picking);

	boolean isPicking();

	boolean isBeginningPicking();

	void beginPicking();

	/** Reapplies a cursor selection volume after replacing the world projection; colour picking needs none. */
	default void applyPickTransform() {}

	void endPicking();

	void tryPick(IDrawingAttributes attributes);
}
