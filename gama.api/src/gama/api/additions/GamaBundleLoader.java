/*******************************************************************************************************
 *
 * GamaBundleLoader.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.additions;

import static gama.dev.DEBUG.TIMER;
import static gama.dev.DEBUG.TIMER_WITH_EXCEPTIONS;

import java.awt.Toolkit;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import gama.api.GAMA;
import gama.api.additions.delegates.IConstantsSupplier;
import gama.api.additions.delegates.ICreateDelegate;
import gama.api.additions.delegates.IDrawDelegate;
import gama.api.additions.delegates.IEventLayerDelegate;
import gama.api.additions.delegates.ISaveDelegate;
import gama.api.additions.registries.GamaAdditionRegistry;
import gama.api.constants.IKeyword;
import gama.api.gaml.GAML;
import gama.api.gaml.types.Types;
import gama.api.kernel.GamaMetaModel;
import gama.api.runtime.SystemInfo;
import gama.api.utils.files.IGamaFileMetaData;
import gama.dev.BANNER_CATEGORY;
import gama.dev.DEBUG;

/**
 * The GamaBundleLoader class is responsible for loading and initializing all GAMA plugins and their extensions at
 * platform startup. This is the central registry and orchestrator for the GAMA plugin architecture.
 *
 * <p>
 * The loader performs the following critical operations in sequence:
 * <ul>
 * <li>Discovers all Eclipse bundles that extend the GAML language via extension points</li>
 * <li>Loads language additions from each plugin (operators, statements, types, etc.)</li>
 * <li>Registers extension delegates for key GAML statements (create, save, draw, event)</li>
 * <li>Collects and indexes model libraries and test suites from plugins</li>
 * <li>Initializes the GAMA metamodel and type hierarchy</li>
 * <li>Registers content types and file extensions handled by the platform</li>
 * </ul>
 *
 * <p>
 * The loading process is fault-tolerant: core plugins (gama.api and gama.core) must load successfully or the platform
 * exits, but failures in additional plugins are logged and the platform continues loading. This allows the user to
 * potentially fix issues or remove problematic plugins even when errors occur.
 *
 * <p>
 * Key extension points processed by this loader:
 * <ul>
 * <li><b>gaml.extension</b>: Main extension point for GAML language additions</li>
 * <li><b>gama.create</b>: Extensions to the 'create' statement</li>
 * <li><b>gama.save</b>: Extensions to the 'save' statement</li>
 * <li><b>gama.draw</b>: Extensions to the 'draw' statement</li>
 * <li><b>gama.event_layer</b>: Extensions for event layer handling</li>
 * <li><b>gama.metadata</b>: File metadata providers for different content types</li>
 * <li><b>gama.models</b>: Model library declarations</li>
 * <li><b>gama.constants</b>: Constant value suppliers</li>
 * </ul>
 *
 * <p>
 * Thread Safety: This class is designed to be called once during platform initialization and is not thread-safe. The
 * ERRORED flag is marked volatile for visibility across threads.
 *
 * @author drogoul
 * @since 24 janv. 2012
 *
 */
public class GamaBundleLoader {

	static {
		DEBUG.OFF();
		Toolkit.getDefaultToolkit();
	}

	/**
	 * Logs a critical error that occurred during plugin loading and sets the ERRORED flag. This method is used
	 * throughout the loading process to capture and report failures while ensuring the error state is tracked.
	 *
	 * <p>
	 * When called, this method:
	 * <ul>
	 * <li>Outputs the generic error message about GAML initialization failure</li>
	 * <li>Sets the volatile ERRORED flag to true for global visibility</li>
	 * <li>Logs the specific error message and exception for debugging</li>
	 * </ul>
	 *
	 * @param message
	 *            a descriptive error message explaining what failed
	 * @param e
	 *            the exception that caused the error
	 */
	public static void ERROR(final String message, final Exception e) {
		DEBUG.ERR(ERROR_MESSAGE);
		ERRORED = true;
		DEBUG.ERR(message, e);
	}

	/** The Constant LINE. */
	public static final String LINE =
			"\n\n****************************************************************************************************\n\n";

	/** The Constant ERROR_MESSAGE. */
	public static final String ERROR_MESSAGE = LINE
			+ "The initialization of GAML artifacts went wrong. If you use the developer version, please clean and recompile all plugins. \nOtherwise report an issue at https://github.com/gama-platform/gama/issues"
			+ LINE;

	/** The loaded. */

	/** The errored. */
	public volatile static boolean ERRORED = false;

	/** The Constant API_PLUGIN. */
	public static final Bundle API_PLUGIN = Platform.getBundle("gama.api");

	/** The core plugin. */
	public static final Bundle CORE_PLUGIN = Platform.getBundle("gama.core");

	/** The core models. */
	public static final Bundle CORE_MODELS = Platform.getBundle("gama.library");

	/** The core tests. */
	public static final String CORE_TESTS = "tests";

	/** The current plugin name. */
	public static String CURRENT_PLUGIN_NAME = API_PLUGIN.getSymbolicName();

	/** The Constant ADDITIONS_PACKAGE_BASE. */
	public static final String ADDITIONS_PACKAGE_BASE = "gaml.additions";

	/** The Constant ADDITIONS_CLASS_NAME. */
	public static final String ADDITIONS_CLASS_NAME = "GamlAdditions";

	/** The grammar extension. */
	public static final String GRAMMAR_EXTENSION = "gaml.extension";

	/** The create extension. */
	public static final String CREATE_EXTENSION = "gama.create";

	/** The save extension. */
	public static final String SAVE_EXTENSION = "gama.save";

	/** The draw extension. */
	public static final String DRAW_EXTENSION = "gama.draw";

	/** The draw extension. */
	public static final String CONSTANTS_EXTENSION = "gama.constants";

	/** The content extension. */
	public static final String CONTENT_EXTENSION = "org.eclipse.core.contenttype.contentTypes";

	/** The event layer extension. */
	public static final String EVENT_LAYER_EXTENSION = "gama.event_layer";

	/** The models extension. */
	public static final String MODELS_EXTENSION = "gama.models";

	/** The Constant METADATA_EXTENSION. */
	public static final String METADATA_EXTENSION = "gama.metadata";

	/** The regular models layout. */
	public static final String REGULAR_MODELS_LAYOUT = "models";

	/** The regular tests layout. */
	public static final String REGULAR_TESTS_LAYOUT = "tests";

	/** The generated tests layout. */
	public static final String GENERATED_TESTS_LAYOUT = "gaml/tests";

	/** The gama plugins. */
	private static final List<Bundle> GAMA_PLUGINS = new ArrayList<>(50);

	/** The Constant GAMA_PLUGINS_NAMES. */
	private static final Set<String> GAMA_PLUGINS_NAMES = new LinkedHashSet<>(50);

	/** The Constant GAMA_PLUGINS_NAMES. */
	private static final Set<String> GAMA_DISPLAY_PLUGINS_NAMES = new LinkedHashSet<>(10);

	/** The Constant GAMA_CORE_DISPLAY_PLUGINS. */
	private static final Set<String> GAMA_CORE_DISPLAY_PLUGINS =
			Set.of("gama.ui.display.java2d", "gama.ui.display.opengl");

	/** The Constant GAMA_DIAGRAM_EDITOR_PLUGIN. */
	private static final String GAMA_DIAGRAM_EDITOR_PLUGIN = "gama.ui.diagram";

	/** The model plugins. */
	private static final Multimap<Bundle, String> MODEL_PLUGINS = ArrayListMultimap.create();

	/** The test plugins. */
	private static final Multimap<Bundle, String> TEST_PLUGINS = ArrayListMultimap.create();

	/** The handled file extensions. */
	public static final Set<String> HANDLED_FILE_EXTENSIONS = new LinkedHashSet<>();

	/**
	 * Builds all plugin contributions to the GAMA platform. This is the main entry point for the plugin loading process
	 * and should be called once during platform initialization.
	 *
	 * <p>
	 * This method orchestrates the complete plugin loading sequence in the following order:
	 * <ol>
	 * <li><b>Discovery</b>: Identifies all Eclipse bundles that declare GAML language extensions</li>
	 * <li><b>Core Loading</b>: Loads gama.api and gama.core plugins first (mandatory - exits on failure)</li>
	 * <li><b>Extension Loading</b>: Loads all other GAML extension plugins (fault-tolerant)</li>
	 * <li><b>Delegate Registration</b>: Registers statement extensions (create, save, draw, event, metadata)</li>
	 * <li><b>Model Gathering</b>: Indexes model libraries and test suites from all plugins</li>
	 * <li><b>Content Types</b>: Registers file extensions and content types</li>
	 * <li><b>Metamodel Initialization</b>: Builds the GAMA metamodel (species hierarchy)</li>
	 * <li><b>Type System</b>: Initializes the type hierarchy and units</li>
	 * <li><b>Constants</b>: Loads constant suppliers from all plugins</li>
	 * </ol>
	 *
	 * <p>
	 * In headless mode, display plugins (gama.ui.display.java2d, gama.ui.display.opengl) are automatically excluded
	 * from loading to prevent UI dependencies.
	 *
	 * <p>
	 * Error Handling: If core plugins fail to load, the platform exits immediately. For other plugins, errors are
	 * logged and loading continues to allow potential recovery or plugin removal.
	 *
	 * @throws RuntimeException
	 *             if a critical error occurs during plugin loading
	 */
	public static void buildContributions() {

		DEBUG.BANNER(BANNER_CATEGORY.GAMA, "version " + SystemInfo.VERSION_NUMBER, "loading on",
				SystemInfo.OS_NAME + " " + SystemInfo.OS_VERSION + ", processor " + SystemInfo.OS_ARCH + ", JDK "
						+ SystemInfo.JAVA_VM_NAME + " " + SystemInfo.JAVA_VM_VENDOR + " version "
						+ SystemInfo.JAVA_VM_VERSION);

		TIMER(BANNER_CATEGORY.GAML, "Plugins with language additions", "loaded in", () -> {
			final IExtensionRegistry registry = Platform.getExtensionRegistry();
			// We retrieve the elements declared as extensions to the GAML language,
			// either with the new or the deprecated extension, and add their contributor plugin to GAMA_PLUGINS
			try {
				// Use a LinkedHashSet to maintain order and avoid duplicates automatically
				final Set<Bundle> bundleSet = new LinkedHashSet<>();
				for (final IExtension ext : registry.getExtensionPoint(GRAMMAR_EXTENSION).getExtensions()) {
					bundleSet.add(Platform.getBundle(ext.getContributor().getName()));
				}
				GAMA_PLUGINS.addAll(bundleSet);
			} catch (final InvalidRegistryObjectException e) {
				ERROR("Error in retrieving GAMA plugins. One is invalid. ", e);
			}
			// We remove the api and core plugins, in order to build them first (important)
			GAMA_PLUGINS.remove(API_PLUGIN);
			GAMA_PLUGINS.remove(CORE_PLUGIN);

			/**
			 * AD : Bug fix regarding the fact that SimpleConfigurator can provide ui components in headless mode
			 */
			if (GAMA.isInHeadLessMode()) {
				GAMA_PLUGINS.removeIf(b -> GAMA_CORE_DISPLAY_PLUGINS.contains(b.getSymbolicName()));
			}
			/**
			 * End bug fix
			 */

			try {
				preBuild(API_PLUGIN);
				preBuild(CORE_PLUGIN);
			} catch (final Exception e2) {
				ERROR("Error in loading core GAML language definition. ", e2);
				// We exit in case the core cannot be built, as there is no point in continuing past this point
				System.exit(0);
				return;
			}
			// We then build the other extensions to the language
			CompletableFuture.runAsync(() -> {
				for (final Bundle addition : GAMA_PLUGINS) {
					CURRENT_PLUGIN_NAME = addition.getSymbolicName();
					try {
						preBuild(addition);
					} catch (final Exception e1) {
						ERROR("Error in loading plugin " + CURRENT_PLUGIN_NAME + ". ", e1);
						// We do not systematically exit in case of additional plugins failing to load, so as to
						// give the platform a chance to execute even in case of errors (to save files, to
						// remove offending plugins, etc.)
						continue;
					}
				}
				CURRENT_PLUGIN_NAME = null;
				TIMER_WITH_EXCEPTIONS(BANNER_CATEGORY.GAML, "Loading extensions to 'create'", "completed in", () -> {
					loadCreateExt(registry);
				});
				TIMER_WITH_EXCEPTIONS(BANNER_CATEGORY.GAML, "Loading extensions to 'save'", "completed in", () -> {
					loadSaveExt(registry);
				});
				TIMER_WITH_EXCEPTIONS(BANNER_CATEGORY.GAML, "Loading extensions to 'draw'", "completed in", () -> {
					loadDrawExt(registry);
				});
				TIMER_WITH_EXCEPTIONS(BANNER_CATEGORY.GAML, "Loading extensions to 'event'", "completed in", () -> {
					loadEventExt(registry);
				});
				TIMER_WITH_EXCEPTIONS(BANNER_CATEGORY.GAML, "Loading extensions to 'metadata'", "completed in", () -> {
					loadMetadataExt(registry);
				});
				TIMER_WITH_EXCEPTIONS(BANNER_CATEGORY.GAML, "Gathering built-in models", "completed in",
						() -> { loadModels(registry); });
				TIMER_WITH_EXCEPTIONS(BANNER_CATEGORY.GAMA, "Loading content extensions", "completed in", () -> {
					loadContentExtensions(registry);
				});

				// CRUCIAL INITIALIZATIONS
				// We init the meta-model of GAMA (i.e. abstract agent, model, experiment species)
				TIMER_WITH_EXCEPTIONS(BANNER_CATEGORY.GAML, "Building metamodel", "completed in",
						() -> { GamaMetaModel.build(); });

				// We init the type hierarchy, the units and the agent representing the GAMA platform
				Types.init();
				TIMER_WITH_EXCEPTIONS(BANNER_CATEGORY.GAML, "Loading constants", "completed in",
						() -> { loadConstants(registry); });
				GamaMetaModel.getSpeciesDescription(IKeyword.PLATFORM).validate();
			}, r -> Thread.ofVirtual().start(r));

		});

	}

	/**
	 * Generic method to load delegate extensions with consistent error handling. This method provides a unified way to
	 * load and register various types of statement delegates (create, save, draw, event) from Eclipse extension points.
	 *
	 * <p>
	 * The method iterates through all configuration elements registered for the specified extension point, creates
	 * executable instances of the delegate classes, and passes them to the provided consumer for registration.
	 *
	 * <p>
	 * Error Handling: If a delegate fails to load, an error is logged but processing continues for remaining delegates.
	 * This fault-tolerant approach ensures that one problematic plugin doesn't prevent other plugins from loading
	 * successfully.
	 *
	 * @param <T>
	 *            the delegate type (e.g., ICreateDelegate, ISaveDelegate, IDrawDelegate)
	 * @param registry
	 *            the Eclipse extension registry containing plugin contributions
	 * @param extensionPoint
	 *            the extension point name to query (e.g., "gama.create", "gama.save")
	 * @param delegateType
	 *            a human-readable name for the delegate type, used in error messages (e.g., "CreateStatement",
	 *            "SaveStatement")
	 * @param delegateConsumer
	 *            a consumer function that processes and registers the loaded delegate instance
	 */
	@SuppressWarnings ("unchecked")
	private static <T> void loadDelegateExtension(final IExtensionRegistry registry, final String extensionPoint,
			final String delegateType, final java.util.function.Consumer<T> delegateConsumer) {
		for (final IConfigurationElement e : registry.getConfigurationElementsFor(extensionPoint)) {
			try {
				final T delegate = (T) e.createExecutableExtension("class");
				if (delegate != null) { delegateConsumer.accept(delegate); }
			} catch (final Exception e1) {
				ERROR("Error in loading " + delegateType + " delegate from "
						+ e.getDeclaringExtension().getContributor().getName(), e1);
				// We do not systematically exit in case of additional plugins failing to load, so as to
				// give the platform a chance to execute even in case of errors (to save files, to
				// remove offending plugins, etc.)
			}
		}
	}

	/**
	 * Loads extensions to the 'create' statement from registered plugins. This method discovers and registers all
	 * {@link ICreateDelegate} implementations that extend the behavior of GAML's create statement.
	 *
	 * <p>
	 * The create statement is used to instantiate agents in GAMA simulations. Plugins can extend this statement to
	 * support creating agents from various data sources (files, databases, web services, etc.).
	 *
	 * @param registry
	 *            the Eclipse extension registry containing plugin contributions
	 * @throws InvalidRegistryObjectException
	 *             if the extension registry contains invalid objects
	 */
	private static void loadCreateExt(final IExtensionRegistry registry) throws InvalidRegistryObjectException {
		loadDelegateExtension(registry, CREATE_EXTENSION, "CreateStatement",
				(final ICreateDelegate cd) -> GamaAdditionRegistry.addDelegate(cd));
	}

	/**
	 * Loads extensions to the 'save' statement from registered plugins. This method discovers and registers all
	 * {@link ISaveDelegate} implementations that extend the behavior of GAML's save statement.
	 *
	 * <p>
	 * The save statement is used to persist data in GAMA simulations. Plugins can extend this statement to support
	 * saving data to various formats and destinations (CSV, JSON, databases, remote services, etc.).
	 *
	 * @param registry
	 *            the Eclipse extension registry containing plugin contributions
	 * @throws InvalidRegistryObjectException
	 *             if the extension registry contains invalid objects
	 */
	private static void loadSaveExt(final IExtensionRegistry registry) throws InvalidRegistryObjectException {
		loadDelegateExtension(registry, SAVE_EXTENSION, "SaveStatement",
				(final ISaveDelegate sd) -> GamaAdditionRegistry.addDelegate(sd));
	}

	/**
	 * Loads extensions to the 'draw' statement from registered plugins. This method discovers and registers all
	 * {@link IDrawDelegate} implementations that extend the behavior of GAML's draw statement.
	 *
	 * <p>
	 * The draw statement is used to render visual elements in GAMA simulations. Plugins can extend this statement to
	 * support drawing custom geometries, images, charts, and other visual representations.
	 *
	 * @param registry
	 *            the Eclipse extension registry containing plugin contributions
	 * @throws InvalidRegistryObjectException
	 *             if the extension registry contains invalid objects
	 */
	private static void loadDrawExt(final IExtensionRegistry registry) throws InvalidRegistryObjectException {
		loadDelegateExtension(registry, DRAW_EXTENSION, "DrawStatement",
				(final IDrawDelegate dd) -> GamaAdditionRegistry.addDelegate(dd));
	}

	/**
	 * Loads file metadata extensions from registered plugins. This method discovers and registers metadata providers
	 * for different file content types.
	 *
	 * <p>
	 * Metadata providers supply information about files (e.g., dimensions, CRS, attributes) without fully loading them.
	 * This is used for file previews, validation, and efficient resource management.
	 *
	 * <p>
	 * Each metadata extension associates a content type (e.g., "shapefile", "image", "csv") with a metadata class that
	 * implements {@link IGamaFileMetaData}.
	 *
	 * @param registry
	 *            the Eclipse extension registry containing plugin contributions
	 */
	private static void loadMetadataExt(final IExtensionRegistry registry) {
		// We gather all the extensions to the `metadata` statement and add them
		// as delegates to MetadataStatement
		for (final IConfigurationElement e : registry.getConfigurationElementsFor(METADATA_EXTENSION)) {
			try {
				GAMA.getMetadataProvider().registerMetadataClass(e.getAttribute("content_type"),
						(Class<? extends IGamaFileMetaData>) GamaClassLoader.getInstance()
								.loadClass(e.getAttribute("class")));
				// TODO Add the defining plug-in
			} catch (final Exception e1) {
				ERROR("Error in loading MetadataStatement delegate : "
						+ e.getDeclaringExtension().getContributor().getName(), e1);
				// We do not systematically exit in case of additional plugins failing to load, so as to
				// give the platform a chance to execute even in case of errors (to save files, to
				// remove offending plugins, etc.)
			}
		}
	}

	/**
	 * Loads extensions to event layer statement from registered plugins. This method discovers and registers all
	 * {@link IEventLayerDelegate} implementations that extend event handling in displays.
	 *
	 * <p>
	 * Event layer delegates allow plugins to customize how user interactions (mouse, keyboard) are processed in
	 * graphical displays. This enables custom interaction modes, tools, and display behaviors.
	 *
	 * @param registry
	 *            the Eclipse extension registry containing plugin contributions
	 * @throws InvalidRegistryObjectException
	 *             if the extension registry contains invalid objects
	 */
	private static void loadEventExt(final IExtensionRegistry registry) throws InvalidRegistryObjectException {
		loadDelegateExtension(registry, EVENT_LAYER_EXTENSION, "EventLayerStatement",
				(final IEventLayerDelegate ed) -> GamaAdditionRegistry.addDelegate(ed));
	}

	/**
	 * Discovers and indexes model libraries and test suites from all registered plugins. This method scans plugins for
	 * standard directory layouts containing GAML models and tests.
	 *
	 * <p>
	 * The method recognizes three standard layouts:
	 * <ul>
	 * <li><b>models/</b>: Standard location for model library projects</li>
	 * <li><b>tests/</b>: Standard location for test files</li>
	 * <li><b>gaml/tests/</b>: Alternative location for generated test files</li>
	 * </ul>
	 *
	 * <p>
	 * Plugins can also explicitly declare non-standard model locations using the "gama.models" extension point. The
	 * core library bundle (gama.library) is always included as a model provider.
	 *
	 * @param registry
	 *            the Eclipse extension registry containing plugin contributions
	 * @throws InvalidRegistryObjectException
	 *             if the extension registry contains invalid objects
	 */
	private static void loadModels(final IExtensionRegistry registry) throws InvalidRegistryObjectException {
		MODEL_PLUGINS.put(CORE_MODELS, REGULAR_MODELS_LAYOUT);
		GAMA_PLUGINS.add(CORE_PLUGIN); // We add it back to gather tests
		GAMA_PLUGINS.forEach(bundle -> {
			if (bundle.getEntry(REGULAR_MODELS_LAYOUT) != null) { MODEL_PLUGINS.put(bundle, REGULAR_MODELS_LAYOUT); }
			if (bundle.getEntry(REGULAR_TESTS_LAYOUT) != null) { TEST_PLUGINS.put(bundle, REGULAR_TESTS_LAYOUT); }
			if (bundle.getEntry(GENERATED_TESTS_LAYOUT) != null) { TEST_PLUGINS.put(bundle, GENERATED_TESTS_LAYOUT); }
		});
		// We gather all the GAMA_PLUGINS that explicitly declare models using
		// the non-default scheme (plugin > models ...).
		for (final IConfigurationElement e : registry.getConfigurationElementsFor(MODELS_EXTENSION)) {
			MODEL_PLUGINS.put(Platform.getBundle(e.getContributor().getName()), e.getAttribute(IKeyword.NAME));
		}
	}

	/**
	 * Loads and registers content type extensions and file extension mappings from all GAMA plugins. This method scans
	 * the Eclipse content type registry for extensions declared by GAMA plugins and extracts the file extensions they
	 * handle.
	 *
	 * <p>
	 * The collected file extensions are stored in {@link #HANDLED_FILE_EXTENSIONS} and are used throughout the platform
	 * to identify files that GAMA can process (e.g., .gaml, .shp, .csv, etc.).
	 *
	 * <p>
	 * Content types in Eclipse define the nature of files and can be associated with editors, validators, and other
	 * tools. This method ensures GAMA is aware of all file types its plugins can handle.
	 *
	 * @param registry
	 *            the Eclipse extension registry containing plugin contributions
	 * @throws InvalidRegistryObjectException
	 *             if the extension registry contains invalid objects
	 */
	private static void loadContentExtensions(final IExtensionRegistry registry) throws InvalidRegistryObjectException {
		// We gather all the content types extensions defined in GAMA plugins
		// (not in the other ones)
		final IExtensionPoint contentType = registry.getExtensionPoint(CONTENT_EXTENSION);
		final IExtension[] extensions = contentType.getExtensions();
		final Set<IExtension> contentExtensions = new HashSet<>(extensions.length);
		for (final IExtension ext : extensions) {
			contentExtensions.add(ext);
			final IConfigurationElement[] configs = ext.getConfigurationElements();
			for (final IConfigurationElement config : configs) {
				final String s = config.getAttribute("file-extensions");
				if (s != null) {
					final String[] fileExts = s.split(",");
					Collections.addAll(HANDLED_FILE_EXTENSIONS, fileExts);
				}
			}
		}
	}

	/**
	 * Loads constant suppliers from registered plugins. This method discovers and invokes all
	 * {@link IConstantsSupplier} implementations that contribute constants to the GAML language.
	 *
	 * <p>
	 * Constants suppliers can add predefined values (mathematical constants, colors, units, etc.) to the GAML language
	 * that are available globally in models. Each supplier's constants are registered with the GAML constant acceptor.
	 *
	 * <p>
	 * Error Handling: If a constants supplier fails to load, an error is logged but processing continues to ensure
	 * other plugins can contribute their constants.
	 *
	 * @param registry
	 *            the Eclipse extension registry containing plugin contributions
	 * @throws InvalidRegistryObjectException
	 *             if the extension registry contains invalid objects
	 */
	private static void loadConstants(final IExtensionRegistry registry) throws InvalidRegistryObjectException {
		// We gather all the extensions to the constants and add them
		// as delegates to GAML. If an exception occurs, we discard it
		IConstantAcceptor constantsAcceptor = GAML.getConstantAcceptor();
		for (final IConfigurationElement e : registry.getConfigurationElementsFor(CONSTANTS_EXTENSION)) {
			IConstantsSupplier sd = null;
			try {
				sd = (IConstantsSupplier) e.createExecutableExtension("class");
				if (sd != null) { sd.supplyConstantsTo(constantsAcceptor); }
			} catch (final Exception e1) {
				ERROR("Error in loading constants from " + e.getDeclaringExtension().getContributor().getName(), e1);
				// We do not systematically exit in case of additional plugins failing to load, so as to
				// give the platform a chance to execute even in case of errors (to save files, to
				// remove offending plugins, etc.)
			}
		}
	}

	/**
	 * Loads and initializes language additions from a specific plugin bundle. This method is called for each plugin
	 * that declares GAML language extensions.
	 *
	 * <p>
	 * The method performs the following steps:
	 * <ol>
	 * <li>Registers the bundle's symbolic name in {@link #GAMA_PLUGINS_NAMES}</li>
	 * <li>Adds the bundle to the GAMA class loader for runtime class resolution</li>
	 * <li>Attempts to load the plugin's GamlAdditions class following the naming convention:
	 * {@code gaml.additions.<shortname>.GamlAdditions}</li>
	 * <li>Instantiates and initializes the GamlAdditions class to register operators, statements, types, etc.</li>
	 * </ol>
	 *
	 * <p>
	 * The shortname is derived from the bundle's symbolic name by taking the part after the last dot. For example,
	 * "gama.extension.maths" becomes "maths", and the loader looks for "gaml.additions.maths.GamlAdditions".
	 *
	 * <p>
	 * Error Handling: Any exception during loading is wrapped in a RuntimeException with a descriptive message
	 * indicating the specific failure point (class not found, initialization failed, instantiation failed, or runtime
	 * error).
	 *
	 * @param bundle
	 *            the Eclipse bundle containing GAML language additions to load
	 * @throws Exception
	 *             if the additions class cannot be found, initialized, instantiated, or executed
	 */
	@SuppressWarnings ("unchecked")
	public static void preBuild(final Bundle bundle) throws Exception {
		TIMER_WITH_EXCEPTIONS(BANNER_CATEGORY.GAML, "Extensions in " + bundle.getSymbolicName(), "loaded in", () -> {
			final String symbolicName = bundle.getSymbolicName();
			GAMA_PLUGINS_NAMES.add(symbolicName);
			final String shortcut = symbolicName.substring(symbolicName.lastIndexOf('.') + 1);
			GamaClassLoader.getInstance().addBundle(bundle);
			final String classPath = ADDITIONS_PACKAGE_BASE + "." + shortcut + "." + ADDITIONS_CLASS_NAME;
			Class<IGamlAdditions> clazz = null;
			try {
				clazz = (Class<IGamlAdditions>) bundle.loadClass(classPath);
				clazz.getConstructor().newInstance().initialize();
			} catch (final ClassNotFoundException e) {
				final String error = ">> Impossible to load additions from " + bundle + " because " + classPath
						+ " cannot be found.";
				DEBUG.LOG(error);
				throw new RuntimeException(error, e);
			} catch (final SecurityException | NoSuchMethodException e) {
				final String error = ">> Impossible to load additions from " + bundle + " because " + classPath
						+ " cannot be initialized.";
				DEBUG.LOG(error);
				throw new RuntimeException(error, e);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				final String error = ">> Impossible to load additions from " + bundle + " because " + classPath
						+ " cannot be instantiated.";
				DEBUG.LOG(error);
				throw new RuntimeException(error, e);
			} catch (Throwable e) {
				DEBUG.LOG(e.getMessage());
				final String error =
						">> Impossible to load additions from " + bundle + " because " + classPath + " cannot be run.";
				throw new RuntimeException(error, e);
			}
		});
	}

	/**
	 * Returns a multimap of all GAMA plugins that contain model libraries, along with the inner path to the folder
	 * containing model projects. This is used by the platform to discover and load built-in models.
	 *
	 * <p>
	 * The returned multimap maps Bundle objects to path strings (e.g., "models", "examples"). A single bundle may have
	 * multiple entries if it contains models in multiple locations.
	 *
	 * @return a multimap where keys are bundles containing models and values are the relative paths to model folders
	 *         within those bundles
	 */
	public static Multimap<Bundle, String> getPluginsWithModels() { return MODEL_PLUGINS; }

	/**
	 * Returns a multimap of all GAMA plugins that contain test suites, along with the inner path to the folder
	 * containing test files. This is used by the platform to discover and execute built-in tests.
	 *
	 * <p>
	 * The returned multimap maps Bundle objects to path strings (e.g., "tests", "gaml/tests"). A single bundle may have
	 * multiple entries if it contains tests in multiple locations.
	 *
	 * @return a multimap where keys are bundles containing tests and values are the relative paths to test folders
	 *         within those bundles
	 */
	public static Multimap<Bundle, String> getPluginsWithTests() { return TEST_PLUGINS; }

	/**
	 * Checks whether a GAML plugin with the specified symbolic name has been successfully loaded during platform
	 * initialization.
	 *
	 * @param s
	 *            the symbolic name of the bundle to check (e.g., "gama.extension.maths")
	 * @return true if a plugin with the given symbolic name was loaded, false otherwise
	 */
	public static boolean gamlPluginExists(final String s) {
		return GAMA_PLUGINS_NAMES.contains(s);
	}

	/**
	 * Checks whether the specified plugin is a display plugin. Display plugins are responsible for rendering graphical
	 * outputs in GAMA simulations.
	 *
	 * <p>
	 * This includes both core display plugins (gama.ui.display.java2d, gama.ui.display.opengl) and any additional
	 * display plugins registered through {@link #addDisplayPlugin(String)}.
	 *
	 * @param s
	 *            the symbolic name of the plugin to check
	 * @return true if the plugin is a display plugin, false otherwise
	 */
	public static boolean isDisplayPlugin(final String s) {
		return GAMA_DISPLAY_PLUGINS_NAMES.contains(s) || GAMA_CORE_DISPLAY_PLUGINS.contains(s);
	}

	/**
	 * Registers a plugin as a display plugin. Display plugins are responsible for rendering graphical outputs. This
	 * method allows additional display plugins beyond the core ones (java2d, opengl) to be registered at runtime.
	 *
	 * @param plugin
	 *            the symbolic name of the display plugin to register
	 */
	public static void addDisplayPlugin(final String plugin) {
		GAMA_DISPLAY_PLUGINS_NAMES.add(plugin);
	}

	/**
	 * Checks if is diagram editor loaded.
	 *
	 * @return true, if is diagram editor loaded
	 */
	public static boolean isDiagramEditorLoaded() { return Platform.getBundle(GAMA_DIAGRAM_EDITOR_PLUGIN) != null; }

}