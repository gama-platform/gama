/*******************************************************************************************************
 *
 * GraphicsScope.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.runtime;

import gama.core.common.interfaces.IGraphics;
import gama.core.common.preferences.GamaPreferences;
import gama.core.common.util.RandomUtils;
import gama.core.runtime.IScope.IGraphicsScope;

/**
 * The Class GraphicsScope.
 */
public class GraphicsScope extends ExecutionScope implements IGraphicsScope {

	/** The graphics. */
	private IGraphics graphics;

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

	@Override
	public IGraphicsScope copy(final String additionalName) {
		return super.copyForGraphics(additionalName);
	}

	@Override
	public boolean reportErrors() {
		return super.reportErrors() && GamaPreferences.Runtime.ERRORS_IN_DISPLAYS.getValue();
	}

}
