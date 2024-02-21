/*******************************************************************************************************
 *
 * GraphicsScope.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.runtime;

import gama.core.common.interfaces.IGraphics;
import gama.core.common.util.RandomUtils;
import gama.core.runtime.IScope.IGraphicsScope;

/**
 * The Class GraphicsScope.
 */
public class GraphicsScope extends ExecutionScope implements IGraphicsScope {

	/** The graphics. */
	private IGraphics graphics;

	/** The horizontal pixel context. */
	// private boolean horizontalPixelContext;

	/**
	 * Instantiates a new graphics scope.
	 *
	 * @param scope
	 *            the scope
	 */
	public GraphicsScope(final IScope scope, final String name) {
		super(scope.getRoot(), name);
	}

	@Override
	public RandomUtils getRandom() {
		if (graphics != null) return graphics.getRandom();
		return super.getRandom();
	}

	/**
	 * Method setGraphics()
	 *
	 * @see gama.core.runtime.IScope#setGraphics(gama.core.common.interfaces.IGraphics)
	 */
	@Override
	public void setGraphics(final IGraphics val) { graphics = val; }

	/**
	 * Method getGraphics()
	 *
	 * @see gama.core.runtime.IScope#getGraphics()
	 */
	@Override
	public IGraphics getGraphics() { return graphics; }

	/**
	 * Sets the horizontal pixel context.
	 */
	// @Override
	// public void setHorizontalPixelContext() {
	// horizontalPixelContext = true;
	//
	// }

	/**
	 * Sets the vertical pixel context.
	 */
	// @Override
	// public void setVerticalPixelContext() {
	// horizontalPixelContext = false;
	//
	// }

	/**
	 * Checks if is horizontal pixel context.
	 *
	 * @return true, if is horizontal pixel context //
	 */
	// @Override
	// public boolean isHorizontalPixelContext() { return horizontalPixelContext; }

	@Override
	public IGraphicsScope copy(final String additionalName) {
		return super.copyForGraphics(additionalName);
	}

}
