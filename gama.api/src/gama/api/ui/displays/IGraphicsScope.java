/**
 *
 */
package gama.api.ui.displays;

import gama.api.runtime.scope.IScope;

/**
 * The IGraphicsScope interface extends IScope for contexts related to graphical display. It provides additional
 * capabilities for working with graphical representations of the simulation.
 */
public interface IGraphicsScope extends IScope {
	/**
	 * Creates a copy of this graphical scope with an additional name identifier.
	 *
	 * @param additionalName
	 *            Name to append to the current scope name
	 * @return A new IGraphicsScope instance that shares context with this scope
	 */
	@Override
	IGraphicsScope copy(String additionalName);

	/**
	 * Sets the graphics context for rendering in this scope.
	 *
	 * @param val
	 *            The graphics object to use for rendering
	 */
	void setGraphics(IGraphics val);

	/**
	 * Indicates that this scope supports graphical operations.
	 *
	 * @return Always returns true for IGraphicsScope implementations
	 */
	@Override
	default boolean isGraphics() { return true; }
}