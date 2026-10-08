/*******************************************************************************************************
 *
 * GraphicsScope.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.runtime.scope;

import gama.api.ui.displays.IGraphics;
import gama.api.ui.displays.IGraphicsScope;
import gama.api.utils.prefs.GamaPreferences;
import gama.api.utils.random.IRandom;

/**
 * Specialized scope implementation for graphical operations and display rendering.
 * 
 * <p>
 * GraphicsScope extends {@link ExecutionScope} to provide support for graphical operations. It is used when executing
 * display statements, drawing commands, and other visualization-related operations. The scope maintains a reference to
 * an {@link IGraphics} object that provides the actual drawing capabilities.
 * </p>
 * 
 * <h2>Key Features</h2>
 * <ul>
 * <li><b>Graphics Context:</b> Access to IGraphics for drawing operations</li>
 * <li><b>Graphics-Specific Random:</b> Uses the graphics object's random generator for deterministic rendering</li>
 * <li><b>Error Handling:</b> Configurable error reporting based on display preferences</li>
 * <li><b>Scope Copying:</b> Creates new GraphicsScope instances when copied</li>
 * </ul>
 * 
 * <h2>When to Use GraphicsScope</h2>
 * 
 * <p>
 * GraphicsScope should be used when:
 * </p>
 * <ul>
 * <li>Executing display statements</li>
 * <li>Rendering visual layers</li>
 * <li>Drawing shapes, agents, or other graphical elements</li>
 * <li>Evaluating expressions that need graphics context (e.g., drawing parameters)</li>
 * </ul>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <h3>Creating a GraphicsScope</h3>
 * <pre>{@code
 * // From an existing scope
 * IScope scope = agent.getScope();
 * IGraphicsScope graphicsScope = new GraphicsScope(scope, "DisplayLayer");
 * 
 * // Set the graphics context
 * IGraphics graphics = ...; // Provided by display
 * graphicsScope.setGraphics(graphics);
 * 
 * // Now use for drawing operations
 * }</pre>
 * 
 * <h3>Using Graphics Context</h3>
 * <pre>{@code
 * public void draw(IGraphicsScope scope) {
 *     IGraphics graphics = scope.getGraphics();
 *     
 *     // Draw a rectangle
 *     graphics.drawRect(10, 10, 100, 50);
 *     
 *     // Draw text
 *     graphics.drawString("Hello World", 10, 70);
 *     
 *     // Access graphics-specific random
 *     IRandom random = scope.getRandom();
 *     double x = random.next() * 100;
 * }
 * }</pre>
 * 
 * <h3>In Display Layer Implementation</h3>
 * <pre>{@code
 * public class CustomLayer implements ILayer {
 *     
 *     @Override
 *     public void draw(IGraphicsScope scope) {
 *         IGraphics g = scope.getGraphics();
 *         
 *         // Get agents to draw
 *         IList<IAgent> agents = scope.getSimulation().getAgents();
 *         
 *         // Draw each agent
 *         for (IAgent agent : agents) {
 *             scope.push(agent);
 *             try {
 *                 GamaPoint location = agent.getLocation();
 *                 GamaColor color = (GamaColor) agent.getAttribute("color");
 *                 
 *                 g.setColor(color);
 *                 g.fillCircle(location.x, location.y, 5);
 *             } finally {
 *                 scope.pop(agent);
 *             }
 *         }
 *     }
 * }
 * }</pre>
 * 
 * <h3>Creating Nested Graphics Scopes</h3>
 * <pre>{@code
 * // Original graphics scope
 * IGraphicsScope mainScope = new GraphicsScope(scope, "MainDisplay");
 * mainScope.setGraphics(graphics);
 * 
 * // Create a copy for a sub-layer
 * IGraphicsScope subScope = mainScope.copy("SubLayer");
 * 
 * // Both share the same graphics context
 * assert subScope.getGraphics() == mainScope.getGraphics();
 * 
 * // But have independent scope state
 * subScope.setVarValue("layer_opacity", 0.5);
 * // Doesn't affect mainScope
 * }</pre>
 * 
 * <h3>Using Graphics Random Generator</h3>
 * <pre>{@code
 * public void drawWithRandomness(IGraphicsScope scope) {
 *     // Graphics scope uses graphics-specific random for deterministic rendering
 *     IRandom random = scope.getRandom();
 *     
 *     // This ensures the same random sequence is used for drawing
 *     // even if simulation random state changes
 *     for (int i = 0; i < 100; i++) {
 *         double x = random.next() * 800;
 *         double y = random.next() * 600;
 *         scope.getGraphics().drawPoint(x, y);
 *     }
 * }
 * }</pre>
 * 
 * <h3>Error Handling in Graphics Context</h3>
 * <pre>{@code
 * public void safeDrawing(IGraphicsScope scope) {
 *     // Error reporting respects display preferences
 *     boolean errorsEnabled = scope.reportErrors();
 *     
 *     try {
 *         // Drawing operation that might fail
 *         drawComplexShape(scope);
 *     } catch (Exception e) {
 *         if (errorsEnabled) {
 *             // Error will be reported to user
 *             scope.getGui().error("Drawing failed: " + e.getMessage());
 *         }
 *         // Otherwise, error is silently ignored
 *     }
 * }
 * }</pre>
 * 
 * <h2>Graphics vs Regular Scope</h2>
 * 
 * <table border="1">
 * <tr>
 * <th>Feature</th>
 * <th>GraphicsScope</th>
 * <th>ExecutionScope</th>
 * </tr>
 * <tr>
 * <td>Graphics Context</td>
 * <td>Available via getGraphics()</td>
 * <td>Not available</td>
 * </tr>
 * <tr>
 * <td>Random Generator</td>
 * <td>From graphics context</td>
 * <td>From simulation/agent</td>
 * </tr>
 * <tr>
 * <td>Error Reporting</td>
 * <td>Based on display preferences</td>
 * <td>Standard error reporting</td>
 * </tr>
 * <tr>
 * <td>isGraphics()</td>
 * <td>Returns true</td>
 * <td>Returns false</td>
 * </tr>
 * <tr>
 * <td>copy()</td>
 * <td>Returns GraphicsScope</td>
 * <td>Returns ExecutionScope</td>
 * </tr>
 * </table>
 * 
 * <h2>Integration with Display Framework</h2>
 * 
 * <p>
 * GraphicsScope is typically created and managed by the display framework:
 * </p>
 * 
 * <pre>{@code
 * // In display implementation
 * public class Display {
 *     private IGraphicsScope scope;
 *     
 *     public void render() {
 *         // Create graphics scope for this rendering cycle
 *         scope = new GraphicsScope(simulation.getScope(), "Display:" + getName());
 *         scope.setGraphics(createGraphics());
 *         
 *         // Render all layers
 *         for (ILayer layer : layers) {
 *             layer.draw(scope);
 *         }
 *     }
 * }
 * }</pre>
 * 
 * @see ExecutionScope
 * @see IGraphicsScope
 * @see IGraphics
 * @see IScope#copyForGraphics(String)
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
	public IRandom getRandom() {
		if (graphics != null) return graphics.getRandom();
		return super.getRandom();
	}

	/**
	 * Method setGraphics()
	 *
	 * @see gama.api.runtime.scope.IScope#setGraphics(gama.api.ui.displays.IGraphics)
	 */
	@Override
	public void setGraphics(final IGraphics val) { graphics = val; }

	/**
	 * Method getGraphics()
	 *
	 * @see gama.api.runtime.scope.IScope#getGraphics()
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
