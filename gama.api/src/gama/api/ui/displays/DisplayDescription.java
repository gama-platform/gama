/*******************************************************************************************************
 *
 * DisplayDescription.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.ui.displays;

import gama.api.additions.GamlAddition;
import gama.api.ui.IOutput;

/**
 * A metadata and factory wrapper for display implementations in GAMA.
 * 
 * <p>This class extends {@link GamlAddition} to provide a registry entry for display types.
 * It wraps an {@link IDisplayCreator} delegate that handles the actual creation of display
 * surfaces, while also maintaining metadata about the display type (name, plugin, support class).</p>
 * 
 * <h2>Main Responsibilities:</h2>
 * <ul>
 *   <li>Store display type metadata (name, plugin, support class)</li>
 *   <li>Delegate display surface creation to the wrapped creator</li>
 *   <li>Provide descriptive title for the display type</li>
 *   <li>Enable registration and discovery of display types</li>
 * </ul>
 * 
 * <h2>Display Registration:</h2>
 * <p>Display types are registered in GAMA's addition registry, allowing them to be
 * discovered and used by the platform. Each display type is identified by:</p>
 * <ul>
 *   <li><strong>Name:</strong> Unique identifier (e.g., "java2D", "opengl")</li>
 *   <li><strong>Plugin:</strong> Plugin providing the implementation</li>
 *   <li><strong>Support Class:</strong> The display surface class type</li>
 * </ul>
 * 
 * <h2>Delegation Pattern:</h2>
 * <p>This class acts as a lightweight wrapper around the actual display creator,
 * allowing metadata and creation logic to be separated. The delegate handles the
 * complex task of creating and configuring display surfaces.</p>
 * 
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * IDisplayCreator openglCreator = new OpenGLDisplayCreator();
 * DisplayDescription desc = new DisplayDescription(
 *     openglCreator, 
 *     OpenGLDisplaySurface.class, 
 *     "opengl", 
 *     "gama.ui.display.opengl"
 * );
 * // Registered in GamaAdditionRegistry for discovery
 * }</pre>
 *
 * @author The GAMA Development Team
 * @since GAMA 1.0
 */
public class DisplayDescription extends GamlAddition implements IDisplayCreator {

	/**
	 * The delegate creator that handles actual display surface creation.
	 * 
	 * <p>May be null if this is a placeholder description without creation capability.</p>
	 */
	private final IDisplayCreator delegate;

	/**
	 * The Eclipse view ID that this display type requires, or an empty string to use the
	 * platform's built-in routing in {@code LayeredDisplayOutput.getViewId()}.
	 */
	private final String viewId;

	/** Whether this display type renders in 3D. */
	private final boolean is3D;

	/**
	 * Creates a new display description.
	 * 
	 * <p>This constructor initializes the description with metadata about the display
	 * type and optionally a delegate creator for actually creating display surfaces.</p>
	 *
	 * @param original the delegate creator that will handle surface creation, or null
	 *                 if this is just a metadata descriptor
	 * @param support the class of the display surface implementation
	 * @param name the unique identifier for this display type (e.g., "java2D", "opengl")
	 * @param plugin the plugin that provides this display implementation
	 */
	public DisplayDescription(final IDisplayCreator original, final Class<? extends IDisplaySurface> support,
			final String name, final String plugin) {
		this(original, support, name, plugin, "", false);
	}

	/**
	 * Creates a new display description with an explicit Eclipse view ID.
	 *
	 * @param original  the delegate creator that will handle surface creation, or null
	 * @param support   the class of the display surface implementation
	 * @param name      the unique identifier for this display type (e.g., "java2D", "opengl")
	 * @param plugin    the plugin that provides this display implementation
	 * @param viewId    the Eclipse view ID used to open the concrete view, or empty string to
	 *                  use the platform's built-in routing
	 */
	public DisplayDescription(final IDisplayCreator original, final Class<? extends IDisplaySurface> support,
			final String name, final String plugin, final String viewId) {
		this(original, support, name, plugin, viewId, false);
	}

	/**
	 * Creates a new display description with an explicit Eclipse view ID and 3D flag.
	 *
	 * @param original  the delegate creator that will handle surface creation, or null
	 * @param support   the class of the display surface implementation
	 * @param name      the unique identifier for this display type (e.g., "java2D", "opengl")
	 * @param plugin    the plugin that provides this display implementation
	 * @param viewId    the Eclipse view ID used to open the concrete view, or empty string to
	 *                  use the platform's built-in routing
	 * @param is3D      whether this display type renders in 3D
	 */
	public DisplayDescription(final IDisplayCreator original, final Class<? extends IDisplaySurface> support,
			final String name, final String plugin, final String viewId, final boolean is3D) {
		super(name, support, plugin);
		this.delegate = original;
		this.viewId = viewId != null ? viewId : "";
		this.is3D = is3D;
	}

	/**
	 * Creates a display surface for the specified output.
	 * 
	 * <p>This method delegates to the wrapped creator if one is available. If no
	 * delegate is present, returns a NULL display surface.</p>
	 * 
	 * <p>The uiComponent parameter can be used to provide platform-specific UI
	 * components (e.g., SWT Composite, Swing Panel) that the display surface
	 * should embed itself in.</p>
	 *
	 * @param output the display output that needs a surface
	 * @param uiComponent the UI component to embed the surface in, or null for standalone
	 * @return a new display surface instance, or IDisplaySurface.NULL if creation fails
	 */
	@Override
	public IDisplaySurface create(final IOutput.Display output, final Object uiComponent) {
		if (delegate != null) return delegate.create(output, uiComponent);
		return IDisplaySurface.NULL;
	}

	/**
	 * Returns the Eclipse view ID that this display type requires.
	 *
	 * <p>When non-empty, {@code LayeredDisplayOutput.getViewId()} uses this value instead of
	 * falling through to the built-in type checks (isWeb, isOpenGL2, is3D, etc.).
	 * An empty string means "use the platform's built-in routing".</p>
	 *
	 * @return the Eclipse view ID, or an empty string if not applicable
	 */
	public String getViewId() { return viewId; }

	/**
	 * Returns whether this display type is a 3D display.
	 *
	 * @return true if this display type renders in 3D
	 */
	public boolean is3D() {
		return is3D;
	}

	/**
	 * Gets a descriptive title for this display type.
	 * 
	 * <p>The title indicates what kind of display this is and is used in UI
	 * elements like tooltips, documentation, and selection dialogs.</p>
	 *
	 * @return a human-readable title describing this display type
	 */
	@Override
	public String getTitle() { return "Display supported by " + getName() + ""; }

}