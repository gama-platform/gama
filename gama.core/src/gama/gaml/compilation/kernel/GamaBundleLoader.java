/*******************************************************************************************************
 *
 * GamaBundleLoader.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.1.9.3).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.compilation.kernel;

import static gama.dev.DEBUG.TIMER;
import static gama.dev.DEBUG.TIMER_WITH_EXCEPTIONS;
import static org.apache.commons.lang3.SystemUtils.JAVA_VM_NAME;
import static org.apache.commons.lang3.SystemUtils.JAVA_VM_VENDOR;
import static org.apache.commons.lang3.SystemUtils.JAVA_VM_VERSION;
import static org.apache.commons.lang3.SystemUtils.OS_ARCH;
import static org.apache.commons.lang3.SystemUtils.OS_NAME;
import static org.apache.commons.lang3.SystemUtils.OS_VERSION;

import java.awt.Toolkit;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.WrappedException;
import org.osgi.framework.Bundle;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import gama.core.common.interfaces.ICreateDelegate;
import gama.core.common.interfaces.IDrawDelegate;
import gama.core.common.interfaces.IEventLayerDelegate;
import gama.core.common.interfaces.ISaveDelegate;
import gama.core.outputs.layers.EventLayerStatement;
import gama.core.runtime.GAMA;
import gama.dev.DEBUG;
import gama.gaml.compilation.GAML;
import gama.gaml.compilation.IGamlAdditions;
import gama.gaml.constants.IConstantsSupplier;
import gama.gaml.statements.CreateStatement;
import gama.gaml.statements.SaveStatement;
import gama.gaml.statements.draw.DrawStatement;
import gama.gaml.types.Types;
import one.util.streamex.StreamEx;

/**
 * The class GamaBundleLoader.
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
	 * Error.
	 *
	 * @param e
	 *            the e
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
	public volatile static boolean LOADED = false;

	/** The errored. */
	public volatile static boolean ERRORED = false;

	/** The core plugin. */
	public static final Bundle CORE_PLUGIN = Platform.getBundle("gama.core");

	/** The core models. */
	public static final Bundle CORE_MODELS = Platform.getBundle("gama.library");

	/** The core tests. */
	public static final String CORE_TESTS = "tests";

	/** The current plugin name. */
	public static String CURRENT_PLUGIN_NAME = CORE_PLUGIN.getSymbolicName();

	/** The Constant ADDITIONS_PACKAGE_BASE. */
	public static final String ADDITIONS_PACKAGE_BASE = "gaml.additions";

	/** The Constant ADDITIONS_CLASS_NAME. */
	public static final String ADDITIONS_CLASS_NAME = "GamlAdditions";

	/** The grammar extension deprecated. */
	public static final String GRAMMAR_EXTENSION_DEPRECATED = "gaml.grammar.addition";

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

	/** The event layer extension. */
	public static final String EVENT_LAYER_EXTENSION = "gama.event_layer";

	/** The models extension. */
	public static final String MODELS_EXTENSION = "gama.models";

	/** The regular models layout. */
	public static final String REGULAR_MODELS_LAYOUT = "models";

	/** The regular tests layout. */
	public static final String REGULAR_TESTS_LAYOUT = "tests";

	/** The generated tests layout. */
	public static final String GENERATED_TESTS_LAYOUT = "gaml/tests";

	/** The content extension. */
	public static final String CONTENT_EXTENSION = "org.eclipse.core.contenttype.contentTypes";

	/** The gama plugins. */
	private static final List<Bundle> GAMA_PLUGINS = new ArrayList<>();

	/** The Constant GAMA_PLUGINS_NAMES. */
	private static final Set<String> GAMA_PLUGINS_NAMES = new LinkedHashSet<>();

	/** The Constant GAMA_PLUGINS_NAMES. */
	private static final Set<String> GAMA_DISPLAY_PLUGINS_NAMES = new LinkedHashSet<>();

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
	 * Pre build contributions.
	 *
	 * @throws Exception
	 *             the exception
	 */
	public static void preBuildContributions() throws Exception {

		DEBUG.BANNER("GAMA", "version " + GAMA.VERSION_NUMBER, "loading on", OS_NAME + " " + OS_VERSION + ", processor "
				+ OS_ARCH + ", JDK " + JAVA_VM_NAME + " " + JAVA_VM_VENDOR + " version " + JAVA_VM_VERSION);

		TIMER("GAML", "Plugins with language additions", "loaded in", () -> {
			final IExtensionRegistry registry = Platform.getExtensionRegistry();
			// We retrieve the elements declared as extensions to the GAML language,
			// either with the new or the deprecated extension, and add their contributor plugin to GAMA_PLUGINS
			try {
				StreamEx.of(registry.getExtensionPoint(GRAMMAR_EXTENSION).getExtensions())
						.append(StreamEx.of(registry.getExtensionPoint(GRAMMAR_EXTENSION_DEPRECATED).getExtensions()))
						.map(e -> Platform.getBundle(e.getContributor().getName()))
						.sorted(Comparator.comparing(Bundle::getSymbolicName)).into(GAMA_PLUGINS);
			} catch (final InvalidRegistryObjectException e) {
				ERROR("Error in retrieving GAMA plugins. One is invalid. ", e);
			}
			// We remove the core plugin, in order to build it first (important)
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
				preBuild(CORE_PLUGIN);
			} catch (final Exception e2) {
				ERROR("Error in loading core GAML language definition. ", e2);
				// We exit in case the core cannot be built, as there is no point in continuing past this point
				System.exit(0);
				return;
			}
			// We then build the other extensions to the language
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
			TIMER_WITH_EXCEPTIONS("GAMA", "Loading extensions to 'create'", "done in",
					() -> { loadCreateExt(registry); });
			TIMER_WITH_EXCEPTIONS("GAMA", "Loading extensions to 'save'", "done in", () -> { loadSaveExt(registry); });
			TIMER_WITH_EXCEPTIONS("GAMA", "Loading extensions to 'draw'", "done in", () -> { loadDrawExt(registry); });
			TIMER_WITH_EXCEPTIONS("GAMA", "Loading extensions to 'event'", "done in",
					() -> { loadEventExt(registry); });
			TIMER_WITH_EXCEPTIONS("GAMA", "Gathering built-in models", "done in", () -> { loadModels(registry); });
			loadContentExtensions(registry);

			// CRUCIAL INITIALIZATIONS
			LOADED = true;
			// We init the meta-model of GAMA (i.e. abstract agent, model, experiment species)
			GamaMetaModel.INSTANCE.build();
			// We init the type hierarchy, the units and the agent representing the GAMA platform
			Types.init();
			TIMER_WITH_EXCEPTIONS("GAMA", "Loading constants", "done in", () -> { loadConstants(registry); });
			GamaMetaModel.getPlatformSpeciesDescription().validate();
		});

	}

	/**
	 * Load create extensions.
	 *
	 * @param registry
	 *            the registry
	 * @throws InvalidRegistryObjectException
	 *             the invalid registry object exception
	 */
	private static void loadCreateExt(final IExtensionRegistry registry) throws InvalidRegistryObjectException {
		// We gather all the extensions to the `create` statement and add them
		// as delegates to CreateStatement. If an exception occurs, we discard it
		for (final IConfigurationElement e : registry.getConfigurationElementsFor(CREATE_EXTENSION)) {
			ICreateDelegate cd = null;
			try {
				// TODO Add the defining plug-in
				cd = (ICreateDelegate) e.createExecutableExtension("class");
				if (cd != null) { CreateStatement.addDelegate(cd); }
			} catch (final Exception e1) {
				ERROR("Error in loading CreateStatement delegate from "
						+ e.getDeclaringExtension().getContributor().getName(), e1);
				// We do not systematically exit in case of additional plugins failing to load, so as to
				// give the platform a chance to execute even in case of errors (to save files, to
				// remove offending plugins, etc.)
				continue;
			}
		}
	}

	/**
	 * Load save extensions.
	 *
	 * @param registry
	 *            the registry
	 * @throws InvalidRegistryObjectException
	 *             the invalid registry object exception
	 */
	private static void loadSaveExt(final IExtensionRegistry registry) throws InvalidRegistryObjectException {
		// We gather all the extensions to the `save` statement and add them
		// as delegates to SaveStatement. If an exception occurs, we discard it
		for (final IConfigurationElement e : registry.getConfigurationElementsFor(SAVE_EXTENSION)) {
			ISaveDelegate sd = null;
			try {
				// TODO Add the defining plug-in
				sd = (ISaveDelegate) e.createExecutableExtension("class");
				if (sd != null) { SaveStatement.addDelegate(sd); }
			} catch (final Exception e1) {
				ERROR("Error in loading SaveStatement delegate from "
						+ e.getDeclaringExtension().getContributor().getName(), e1);
				// We do not systematically exit in case of additional plugins failing to load, so as to
				// give the platform a chance to execute even in case of errors (to save files, to
				// remove offending plugins, etc.)
				continue;
			}
		}
	}

	/**
	 * Load draw extensions.
	 *
	 * @param registry
	 *            the registry
	 * @throws InvalidRegistryObjectException
	 *             the invalid registry object exception
	 */
	private static void loadDrawExt(final IExtensionRegistry registry) throws InvalidRegistryObjectException {
		// We gather all the extensions to the `draw` statement and add them
		// as delegates to DrawStatement. If an exception occurs, we discard it
		for (final IConfigurationElement e : registry.getConfigurationElementsFor(DRAW_EXTENSION)) {
			IDrawDelegate sd = null;
			try {
				// TODO Add the defining plug-in
				sd = (IDrawDelegate) e.createExecutableExtension("class");
				if (sd != null) { DrawStatement.addDelegate(sd); }
			} catch (final Exception e1) {
				ERROR("Error in loading DrawStatement delegate from "
						+ e.getDeclaringExtension().getContributor().getName(), e1);
				// We do not systematically exit in case of additional plugins failing to load, so as to
				// give the platform a chance to execute even in case of errors (to save files, to
				// remove offending plugins, etc.)
				continue;
			}
		}
	}

	/**
	 * Load event layer extensions.
	 *
	 * @param registry
	 *            the registry
	 * @throws InvalidRegistryObjectException
	 *             the invalid registry object exception
	 */
	private static void loadEventExt(final IExtensionRegistry registry) throws InvalidRegistryObjectException {
		// We gather all the extensions to the `event` statement and add them
		// as delegates to EventLayerStatement
		for (final IConfigurationElement e : registry.getConfigurationElementsFor(EVENT_LAYER_EXTENSION)) {
			try {
				// TODO Add the defining plug-in
				EventLayerStatement.addDelegate((IEventLayerDelegate) e.createExecutableExtension("class"));
			} catch (final Exception e1) {
				ERROR("Error in loading EventLayerStatement delegate : "
						+ e.getDeclaringExtension().getContributor().getName(), e1);
				// We do not systematically exit in case of additional plugins failing to load, so as to
				// give the platform a chance to execute even in case of errors (to save files, to
				// remove
				// offending plugins, etc.)
				continue;
			}
		}
	}

	/**
	 * Load models.
	 *
	 * @param registry
	 *            the registry
	 * @throws InvalidRegistryObjectException
	 *             the invalid registry object exception
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
			MODEL_PLUGINS.put(Platform.getBundle(e.getContributor().getName()), e.getAttribute("name"));
		}
	}

	/**
	 * Load content extensions.
	 *
	 * @param registry
	 *            the registry
	 * @throws InvalidRegistryObjectException
	 *             the invalid registry object exception
	 */
	private static void loadContentExtensions(final IExtensionRegistry registry) throws InvalidRegistryObjectException {
		// We gather all the content types extensions defined in GAMA plugins
		// (not in the other ones)
		final IExtensionPoint contentType = registry.getExtensionPoint(CONTENT_EXTENSION);
		final Set<IExtension> contentExtensions = new HashSet<>();
		contentExtensions.addAll(Arrays.asList(contentType.getExtensions()));
		for (final IExtension ext : contentExtensions) {
			final IConfigurationElement[] configs = ext.getConfigurationElements();
			for (final IConfigurationElement config : configs) {
				final String s = config.getAttribute("file-extensions");
				if (s != null) { HANDLED_FILE_EXTENSIONS.addAll(Arrays.asList(s.split(","))); }
			}
		}
	}

	/**
	 * Load constants.
	 *
	 * @param registry
	 *            the registry
	 * @throws InvalidRegistryObjectException
	 *             the invalid registry object exception
	 */
	private static void loadConstants(final IExtensionRegistry registry) throws InvalidRegistryObjectException {
		// We gather all the extensions to the constants and add them
		// as delegates to GAML. If an exception occurs, we discard it
		for (final IConfigurationElement e : registry.getConfigurationElementsFor(CONSTANTS_EXTENSION)) {
			IConstantsSupplier sd = null;
			try {
				sd = (IConstantsSupplier) e.createExecutableExtension("class");
				if (sd != null) { sd.supplyConstantsTo(GAML.getConstantAcceptor()); }
			} catch (final Exception e1) {
				ERROR("Error in loading constants from " + e.getDeclaringExtension().getContributor().getName(), e1);
				// We do not systematically exit in case of additional plugins failing to load, so as to
				// give the platform a chance to execute even in case of errors (to save files, to
				// remove offending plugins, etc.)
				continue;
			}
		}
	}

	/**
	 * Pre build.
	 *
	 * @param bundle
	 *            the bundle
	 * @throws Exception
	 *             the exception
	 */
	@SuppressWarnings ("unchecked")
	public static void preBuild(final Bundle bundle) throws Exception {
		TIMER_WITH_EXCEPTIONS("GAMA", "Plugin " + bundle.getSymbolicName(), "loaded in", () -> {
			String shortcut = bundle.getSymbolicName();
			GAMA_PLUGINS_NAMES.add(shortcut);
			shortcut = shortcut.substring(shortcut.lastIndexOf('.') + 1);
			GamaClassLoader.getInstance().addBundle(bundle);
			Class<IGamlAdditions> clazz = null;
			String classPath = ADDITIONS_PACKAGE_BASE + "." + shortcut + "." + ADDITIONS_CLASS_NAME;
			String error = ">> Impossible to load additions from " + bundle + " because " + classPath + " cannot be ";
			try {
				clazz = (Class<IGamlAdditions>) bundle.loadClass(classPath);
				clazz.getConstructor().newInstance().initialize();
			} catch (final ClassNotFoundException e) {
				DEBUG.LOG(error + "found.");
				throw new WrappedException(error + "found.", e);
			} catch (final SecurityException | NoSuchMethodException e) {
				DEBUG.LOG(error + "initialized.");
				throw new WrappedException(error + "initialized.", e);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				DEBUG.LOG(error + "instantiated.");
				throw new WrappedException(error + "instantiated.", e);
			} catch (Exception e) {
				DEBUG.LOG(e.getMessage());
				throw new WrappedException(error + "run.", e);
			}
		});
	}

	/**
	 * The list of GAMA_PLUGINS declaring models, together with the inner path to the folder containing model projects
	 *
	 * @return
	 */
	public static Multimap<Bundle, String> getPluginsWithModels() { return MODEL_PLUGINS; }

	/**
	 * Gets the plugins with tests.
	 *
	 * @return the plugins with tests
	 */
	public static Multimap<Bundle, String> getPluginsWithTests() { return TEST_PLUGINS; }

	/**
	 * Gaml plugin exists.
	 *
	 * @param s
	 *            the s
	 * @return true, if successful
	 */
	public static boolean gamlPluginExists(final String s) {
		return GAMA_PLUGINS_NAMES.contains(s);
	}

	/**
	 * Checks if is display plugin.
	 *
	 * @param s
	 *            the s
	 * @return true, if is display plugin
	 */
	public static boolean isDisplayPlugin(final String s) {
		return GAMA_DISPLAY_PLUGINS_NAMES.contains(s) || GAMA_CORE_DISPLAY_PLUGINS.contains(s);
	}

	/**
	 * Adds the display plugin.
	 *
	 * @param plugin
	 *            the plugin
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