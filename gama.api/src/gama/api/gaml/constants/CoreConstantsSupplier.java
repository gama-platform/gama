/*******************************************************************************************************
 *
 * CoreConstantsSupplier.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.constants;

import gama.annotations.constants.ColorCSS;
import gama.api.additions.IConstantAcceptor;
import gama.api.additions.delegates.IConstantsSupplier;
import gama.api.additions.delegates.IEventLayerDelegate;
import gama.api.additions.registries.GamaAdditionRegistry;
import gama.api.constants.IKeyword;
import gama.api.types.color.GamaColorFactory;
import gama.api.types.color.IColor;
import gama.api.ui.layers.ICameraDefinition;
import gama.api.ui.layers.ILightDefinition;

/**
 * Supplier class responsible for providing all core GAML constants to the GAMA platform. This class implements the
 * {@link IConstantsSupplier} interface and serves as the central registry for built-in constants used throughout the
 * GAMA modeling and simulation platform.
 * 
 * <p>
 * The CoreConstantsSupplier aggregates and supplies constants from multiple sources:
 * </p>
 * <ul>
 * <li>Camera and light definition constants from {@link ICameraDefinition} and {@link ILightDefinition}</li>
 * <li>Unit constants (time, distance, weight, etc.) from {@link GamlCoreUnits}</li>
 * <li>Mathematical, algorithmic, and graphic constants from {@link GamlCoreConstants}</li>
 * <li>CSS color constants from {@link ColorCSS}, converting color names to {@link IColor} objects</li>
 * <li>Event layer constants (mouse, keyboard events) from registered {@link IEventLayerDelegate} instances</li>
 * </ul>
 * 
 * <p>
 * This class uses reflection-based browsing via the {@link IConstantsSupplier#browse(Class, IConstantAcceptor)}
 * method to discover constants annotated with {@code @constant} in the specified classes and delegates.
 * </p>
 * 
 * <h3>Usage Example:</h3>
 * 
 * <pre>
 * // The supplier is automatically invoked during GAMA initialization
 * CoreConstantsSupplier supplier = new CoreConstantsSupplier();
 * IConstantAcceptor acceptor = ...; // Provided by the platform
 * supplier.supplyConstantsTo(acceptor);
 * 
 * // Constants become available in GAML models:
 * // model example
 * // global {
 * //   float distance <- 100 #km;  // Uses unit constant from GamlCoreUnits
 * //   color myColor <- #red;      // Uses CSS color constant
 * //   float pi_value <- #pi;      // Uses mathematical constant from GamlCoreConstants
 * // }
 * </pre>
 * 
 * <h3>Color Constants:</h3>
 * <p>
 * All CSS named colors (e.g., "red", "blue", "azure") are automatically registered with their RGB values and made
 * available as constants in GAML. Each color constant is documented with its RGBA components.
 * </p>
 * 
 * <h3>Event Constants:</h3>
 * <p>
 * Event-related constants (such as mouse button identifiers, keyboard keys) are dynamically discovered from all
 * registered {@link IEventLayerDelegate} instances in the {@link GamaAdditionRegistry}.
 * </p>
 * 
 * @author GAMA Development Team
 * @see IConstantsSupplier
 * @see GamlCoreUnits
 * @see GamlCoreConstants
 * @see IConstantAcceptor
 * @since GAMA 1.0
 */
public class CoreConstantsSupplier implements IConstantsSupplier {

	/**
	 * Supplies all core GAML constants to the provided acceptor. This method is called during GAMA platform
	 * initialization to register all built-in constants that will be available in GAML models.
	 * 
	 * <p>
	 * The method performs the following operations in sequence:
	 * </p>
	 * <ol>
	 * <li>Browses and registers constants from {@link ICameraDefinition} (camera-related constants)</li>
	 * <li>Browses and registers constants from {@link ILightDefinition} (lighting-related constants)</li>
	 * <li>Browses and registers all unit constants from {@link GamlCoreUnits} (time, distance, weight, volume,
	 * surface units)</li>
	 * <li>Browses and registers core constants from {@link GamlCoreConstants} (mathematical, algorithmic, graphic
	 * constants)</li>
	 * <li>Registers the "default" keyword constant for cameras and lights</li>
	 * <li>Iterates through all CSS color names from {@link ColorCSS} and creates corresponding {@link IColor}
	 * constants with RGB documentation</li>
	 * <li>Browses and registers event-related constants from {@link IEventLayerDelegate} base class</li>
	 * <li>Iterates through all registered event layer delegates and browses their constants (mouse, keyboard events,
	 * etc.)</li>
	 * </ol>
	 * 
	 * <p>
	 * Each constant is registered with its name, value, documentation, and metadata, making it accessible in GAML
	 * models with the '#' prefix (e.g., {@code #pi}, {@code #red}, {@code #km}).
	 * </p>
	 * 
	 * @param acceptor
	 *            the constant acceptor that will receive and register all constants. Must not be null.
	 * @see IConstantAcceptor#accept(String, Object, String, String, boolean)
	 * @see IConstantsSupplier#browse(Class, IConstantAcceptor)
	 */
	@Override
	public void supplyConstantsTo(final IConstantAcceptor acceptor) {

		browse(ICameraDefinition.class, acceptor);
		browse(ILightDefinition.class, acceptor);
		browse(GamlCoreUnits.class, acceptor);
		browse(GamlCoreConstants.class, acceptor);

		acceptor.accept(IKeyword.DEFAULT, IKeyword.DEFAULT, "Default value for cameras and lights", null, false);

		// We build constants based on the colors declared in GamaColor / ColorCSS
		for (int i = 0; i < ColorCSS.array.length; i += 2) {
			String name = (String) ColorCSS.array[i];
			IColor c = GamaColorFactory.get(name);
			final String doc =
					"CSS color with rgb (" + c.red() + ", " + c.green() + ", " + c.blue() + "," + c.alpha() + ")";
			acceptor.accept(name, c, doc, null, false);
		}

		// We browse all the constants declared in the event layer delegates and add them (like mouse or keyboard
		// events)
		browse(IEventLayerDelegate.class, acceptor);
		for (final IEventLayerDelegate delegate : GamaAdditionRegistry.getEventLayerDelegates()) {
			browse(delegate.getClass(), acceptor);
		}

	}

}
