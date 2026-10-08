/*******************************************************************************************************
 *
 * IGamlAdditions.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.additions;



/**
 * The IGamlAdditions interface defines the contract for plugin classes that contribute extensions to the GAML language.
 * 
 * <p>This interface is the foundation of GAMA's plugin architecture for language extensibility. Plugins implement
 * this interface (typically by extending {@link AbstractGamlAdditions}) to register new GAML language elements
 * such as operators, types, skills, statements, and other constructs.</p>
 * 
 * <h2>Purpose</h2>
 * 
 * <p>Classes implementing this interface serve as:</p>
 * <ul>
 *   <li><strong>Language Extension Points:</strong> Entry points for plugins to contribute GAML additions</li>
 *   <li><strong>Registration Coordinators:</strong> Orchestrate the registration of multiple related additions</li>
 *   <li><strong>Initialization Hooks:</strong> Provide initialization logic specific to the plugin</li>
 * </ul>
 * 
 * <h2>Implementation Pattern</h2>
 * 
 * <p>Typical implementation approach:</p>
 * <ol>
 *   <li>Extend {@link AbstractGamlAdditions} (recommended) or implement this interface directly</li>
 *   <li>Annotate methods and classes with GAML annotations (@operator, @skill, @action, etc.)</li>
 *   <li>Override {@link #initialize()} if custom initialization logic is needed</li>
 *   <li>The platform automatically discovers and loads the additions via Eclipse extension points</li>
 * </ol>
 * 
 * <h2>Usage Example</h2>
 * 
 * <pre>{@code
 * public class MyPluginAdditions extends AbstractGamlAdditions {
 *     
 *     @Override
 *     public void initialize() throws SecurityException, NoSuchMethodException {
 *         // Custom initialization if needed
 *         super.initialize();
 *     }
 *     
 *     @operator(value = "custom_op")
 *     @doc("My custom operator")
 *     public static Object customOperator(IScope scope, Object arg) {
 *         return processArg(arg);
 *     }
 * }
 * }</pre>
 * 
 * <h2>Lifecycle</h2>
 * 
 * <p>Addition classes follow this lifecycle:</p>
 * <ol>
 *   <li>Platform discovers the class via Eclipse extension point</li>
 *   <li>Platform instantiates the class</li>
 *   <li>Platform calls {@link #initialize()}</li>
 *   <li>Annotations are processed and additions are registered</li>
 *   <li>Additions become available in GAML models</li>
 * </ol>
 * 
 * <h2>Thread Safety</h2>
 * 
 * <p>The {@link #initialize()} method is called during single-threaded platform startup,
 * so thread safety is not a concern during initialization.</p>
 * 
 * @author Alexis Drogoul
 * @since GAMA 1.0
 * @version 2025-03
 * 
 * @see AbstractGamlAdditions
 * @see gama.api.additions.GamaBundleLoader
 * @see gama.api.additions.registries.GamaAdditionRegistry
 */

public interface IGamlAdditions {

	/**
	 * Initializes this GAML additions provider.
	 * 
	 * <p>This method is called by the platform during the addition loading phase. Implementations
	 * should perform any custom initialization required by their additions, such as:</p>
	 * <ul>
	 *   <li>Registering types, operators, or other language elements programmatically</li>
	 *   <li>Setting up internal data structures</li>
	 *   <li>Loading configuration or resources</li>
	 *   <li>Initializing delegates or helper objects</li>
	 * </ul>
	 * 
	 * <p>Most implementations using {@link AbstractGamlAdditions} and annotations don't need
	 * to override this method, as annotation processing happens automatically.</p>
	 * 
	 * <p><b>Note:</b> This method is called once during platform startup in a single-threaded context.</p>
	 *
	 * @throws SecurityException if security manager prevents reflection operations needed for registration
	 * @throws NoSuchMethodException if expected methods for registration are not found (typically indicates
	 *         a programming error in the additions class)
	 */
	void initialize() throws SecurityException, NoSuchMethodException;

}
