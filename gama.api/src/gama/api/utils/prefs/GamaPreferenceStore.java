/*******************************************************************************************************
 *
 * GamaPreferenceStore.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.prefs;

import static gama.api.gaml.types.Cast.asBool;
import static gama.api.gaml.types.Cast.asFloat;
import static gama.api.gaml.types.Cast.asInt;
import static gama.api.gaml.types.Cast.asString;
import static gama.api.types.date.GamaDateFactory.createFromISOString;
import static gama.api.types.font.GamaFontFactory.castToFont;
import static gama.api.types.geometry.GamaPointFactory.castToPoint;
import static gama.api.utils.StringUtils.toJavaString;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import gama.api.gaml.types.IType;
import gama.api.runtime.SystemInfo;
import gama.api.types.color.GamaColorFactory;
import gama.api.types.color.IColor;
import gama.api.types.date.GamaDateFactory;
import gama.api.types.date.IDate;
import gama.api.types.file.GenericFile;
import gama.api.types.file.IGamaFile;
import gama.api.types.geometry.IPoint;
import gama.api.utils.StringUtils;
import one.util.streamex.StreamEx;

/**
 * Abstract base implementation of {@link IGamaPreferenceStore} that bridges the GAMA in-memory preference model
 * ({@link Pref}) with an arbitrary persistent backend store of type {@code T}. Concrete subclasses supply the
 * backend-specific I/O operations ({@link #flush()}, {@link #putInStore}, {@link #getInStore}, etc.) while this class
 * handles the common concerns of:
 * <ul>
 * <li>Maintaining an ordered registry of all registered preferences (keyed by name).</li>
 * <li>Resolving preference override precedence: JVM system properties beat persisted store values beat the compiled-in
 * defaults.</li>
 * <li>Serializing typed {@link Pref} values to the backend-neutral string representation required by the store.</li>
 * <li>Exporting the preference set to a GAML model file or a Java {@link java.util.Properties} file.</li>
 * <li>Applying preference overrides loaded from an external properties file.</li>
 * </ul>
 *
 * <p>
 * Override resolution is handled by {@link #isOverriden(String)} which checks two sources in order:
 * <ol>
 * <li>JVM system properties (set via {@code -Dkey=value} on the command line).</li>
 * <li>Values already persisted in the backing store at the time this store was instantiated (captured in
 * {@link #overriddenKeys}).</li>
 * </ol>
 * </p>
 *
 * @param <T>
 *            the type of the backend persistence object (e.g. {@link java.util.prefs.Preferences} or
 *            {@code IEclipsePreferences})
 */
@SuppressWarnings ({ "restriction", "unchecked", "rawtypes" })
public abstract class GamaPreferenceStore<T> implements IGamaPreferenceStore {

	/**
	 * The name of the root node used when creating the JRE {@link java.util.prefs.Preferences} node for GAMA. All
	 * global preferences are stored under this node name.
	 */
	public static final String NODE_NAME = "gama";

	/**
	 * The sentinel string written to the backend store when a font preference has no explicit value (i.e., the system
	 * default font should be used).
	 */
	private static final String DEFAULT_FONT = "Default";

	/**
	 * The backend persistence object supplied by the concrete subclass (e.g. a {@link java.util.prefs.Preferences} node
	 * or an {@code IEclipsePreferences} node).
	 */
	T store;

	/**
	 * An ordered registry mapping preference keys to their corresponding {@link Pref} instances. The insertion order is
	 * preserved so that iteration produces preferences in registration order.
	 */
	Map<String, Pref> map = new LinkedHashMap<>();

	/**
	 * The set of keys that were already present in the backing store at construction time, indicating that those
	 * preferences have previously been persisted and should override the compiled-in default values.
	 */
	Set<String> overriddenKeys = Set.of();

	/**
	 * Constructs a new store wrapping the given backend persistence object. After setting the {@link #store} field the
	 * constructor immediately calls {@link #flush()} to synchronize any in-flight state between the in-memory registry
	 * and the backend.
	 *
	 * @param store
	 *            the backend persistence object; must not be {@code null}
	 */
	GamaPreferenceStore(final T store) {
		this.store = store;
		flush();
	}

	/**
	 * Flushes all pending writes to the backend persistence store, ensuring that the in-memory state and the on-disk
	 * (or registry) state remain synchronized. Concrete subclasses must implement this method using the appropriate
	 * flush mechanism of their backend (e.g. {@link java.util.prefs.Preferences#flush()} or
	 * {@code IEclipsePreferences#flush()}).
	 */
	protected abstract void flush();

	/**
	 * Exports all registered preference key/value pairs into a Java {@link java.util.Properties} file at the given
	 * path. The resulting file is a human-readable {@code key=value} text file that can be shared across GAMA instances
	 * and later restored via {@link #loadFromProperties(String)}. Concrete subclasses must implement this method to
	 * iterate over the keys available in their backend store.
	 *
	 * @param path
	 *            the absolute file-system path at which to write the properties file
	 */
	@Override
	public abstract void saveToProperties(final String path);

	/**
	 * Reads a Java {@link java.util.Properties} file previously created by {@link #saveToProperties(String)} and
	 * restores the preference values it contains into the backing store. After loading, the registered {@link Pref}
	 * instances whose keys appear in the file will reflect the imported values on the next call to
	 * {@link #register(Pref)}. Concrete subclasses must implement this using their backend-specific import mechanism.
	 *
	 * @param path
	 *            the absolute file-system path of the properties file to load
	 */
	@Override
	public abstract void loadFromProperties(final String path);

	/**
	 * Exports all visible GAML-accessible preferences as a GAML model file at the given path. The generated file
	 * contains:
	 * <ul>
	 * <li>A {@code Display __PREFS__} GUI experiment that prints the current value of each preference to the console
	 * using {@code write sample(gama.&lt;key&gt;)}.</li>
	 * <li>A {@code Set __PREFS__} GUI experiment that reassigns each preference to its current value via
	 * {@code gama.&lt;key&gt; &lt;- &lt;value&gt;}.</li>
	 * </ul>
	 * Hidden preferences and preferences not accessible from GAML are excluded. The file is prefixed with a comment
	 * recording the GAMA version and the current date/time.
	 *
	 * @param path
	 *            the absolute file-system path at which to write the GAML file
	 */
	@Override
	public void saveToGAML(final String path) {
		try (var os = new FileWriter(path)) {
			final var entries = StreamEx.ofValues(map).sortedBy(Pref::getName).toList();

			final var read = new StringBuilder(1000);
			final var write = new StringBuilder(1000);
			for (final Pref<?> e : entries) {
				if (e.isHidden() || !e.inGaml()) { continue; }
				read.append(StringUtils.TAB).append("//").append(e.getTitle()).append(StringUtils.LN);
				read.append(StringUtils.TAB).append("write sample(gama.").append(e.getName()).append(");")
						.append(StringUtils.LN).append(StringUtils.LN);
				write.append(StringUtils.TAB).append("//").append(e.getTitle()).append(StringUtils.LN);
				write.append(StringUtils.TAB).append("gama.").append(e.getName()).append(" <- ")
						.append(StringUtils.toGaml(e.getValue(), false)).append(";").append(StringUtils.LN)
						.append(StringUtils.LN);
			}
			os.append("// ").append(SystemInfo.VERSION).append(" __PREFS__ saved on ")
					.append(LocalDateTime.now().toString()).append(StringUtils.LN).append(StringUtils.LN);
			os.append("model preferences").append(StringUtils.LN).append(StringUtils.LN);
			os.append("experiment 'Display __PREFS__' type: gui {").append(StringUtils.LN);
			os.append("init {").append(StringUtils.LN);
			os.append(read);
			os.append("}").append(StringUtils.LN);
			os.append("}").append(StringUtils.LN).append(StringUtils.LN).append(StringUtils.LN);
			os.append("experiment 'Set __PREFS__' type: gui {").append(StringUtils.LN);
			os.append("init {").append(StringUtils.LN);
			os.append(write);
			os.append("}").append(StringUtils.LN);
			os.append("}").append(StringUtils.LN);
			os.flush();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Serializes the current value of the given {@link Pref} into the backing store. The method dispatches on the GAML
	 * type identifier of the preference ({@link gama.api.gaml.types.IType#INT}, {@code FLOAT}, {@code BOOL},
	 * {@code STRING}, {@code FILE}, {@code COLOR}, {@code POINT}, {@code FONT}, {@code DATE}) and converts the typed
	 * Java value to the appropriate representation before delegating to {@link #putInStore(String, Object)}, after
	 * which {@link #flush()} is called to guarantee durability.
	 *
	 * @param gp
	 *            the preference whose value should be persisted; must not be {@code null}
	 */
	@Override
	public void write(final Pref gp) {
		final String key = gp.getKey();
		final Object value = gp.getValue();
		switch (gp.getTypeId()) {
			case IType.INT -> putInStore(key, value == null ? 0 : value.toString());
			case IType.FLOAT -> putInStore(key, value == null ? 0.0 : value.toString());
			case IType.BOOL -> putInStore(key, value == null ? false : value.toString());
			case IType.STRING -> putInStore(key, StringUtils.toJavaString((String) value));
			case IType.FILE -> putInStore(key, value == null ? "" : ((IGamaFile) value).getPath(null));
			case IType.COLOR -> putInStore(key, value == null ? 0 : ((IColor) value).getRGB());
			case IType.POINT -> putInStore(key, value == null ? "{0,0}" : ((IPoint) value).stringValue(null));
			case IType.FONT -> putInStore(key, value == null ? DEFAULT_FONT : value.toString());
			case IType.DATE -> putInStore(key,
					value == null ? StringUtils.toJavaString(GamaDateFactory.EPOCH.toISOString())
							: StringUtils.toJavaString(((IDate) value).toISOString()));
			default -> putInStore(key, (String) value);
		}

		flush();
	}

	/**
	 * Registers a {@link Pref} with this store. During registration the store resolves the override precedence for the
	 * preference key:
	 * <ol>
	 * <li>If the key is overridden (via a JVM system property or a previously persisted value), the preference's
	 * initial value supplier is replaced by one that returns the overriding value, correctly typed according to the
	 * preference's GAML type id.</li>
	 * <li>The preference is added to the internal registry ({@link #map}) under its key.</li>
	 * <li>{@link #flush()} is called to synchronize the backend.</li>
	 * </ol>
	 * If the preference's key is {@code null}, this method returns silently.
	 *
	 * @param gp
	 *            the preference to register; must not be {@code null}
	 */
	@Override
	public void register(final Pref<?> gp) {
		final String key = gp.getKey();
		if (key == null) return;
		if (isOverriden(key)) { gp.init(getValueSupplierFor(gp, getOverridenValue(key))); }
		map.put(key, gp);
		flush();
	}

	/**
	 * Gets the value supplier for.
	 *
	 * @param gp
	 *            the gp
	 * @param val
	 *            the val
	 * @param scope
	 *            the scope
	 * @return the value supplier for
	 */
	private Supplier getValueSupplierFor(final Pref<?> gp, final String val) {
		if (val == null) return gp.getValueProvider();
		return switch (gp.getTypeId()) {
			case IType.POINT -> (Supplier) () -> castToPoint(null, asString(null, val), false);
			case IType.INT -> (Supplier) () -> asInt(null, val);
			case IType.FLOAT -> (Supplier) () -> asFloat(null, val);
			case IType.BOOL -> (Supplier) () -> asBool(null, val);
			case IType.STRING -> (Supplier) () -> toJavaString(asString(null, val));
			case IType.FILE -> (Supplier) () -> new GenericFile(val, false);
			case IType.COLOR -> (Supplier) () -> GamaColorFactory.get(asInt(null, val));
			case IType.FONT -> (Supplier) () -> {
				if (DEFAULT_FONT.equals(val)) return null;
				return castToFont(null, asString(null, val), false);
			};
			case IType.DATE -> (Supplier) () -> createFromISOString(toJavaString(asString(null, val)));
			default -> (Supplier) () -> asString(null, val);
		};
	}

	/**
	 * Returns the overriding string value for the given preference key, following override precedence:
	 * <ol>
	 * <li>JVM system properties ({@code System.getProperty(key)}) take highest priority.</li>
	 * <li>A value previously persisted in the backing store (indicated by {@link #overriddenKeys}) is used as a
	 * fallback.</li>
	 * </ol>
	 * Returns {@code null} if neither source provides a value (which should not happen when called only after
	 * {@link #isOverriden(String)} returns {@code true}).
	 *
	 * @param key
	 *            the preference key to look up; must not be {@code null}
	 * @return the overriding string value, or {@code null} if not overridden
	 */
	protected String getOverridenValue(final String key) {
		if (isOverridenInSystemProperties(key)) return System.getProperty(key);
		if (isOverridenInStore(key)) return getInStore(key, null);
		return null;
	}

	/**
	 * Determines whether the preference with the given key has been overridden — either by a JVM system property or by
	 * a value persisted in the backing store prior to this store being instantiated.
	 *
	 * @param key
	 *            the preference key to test; must not be {@code null}
	 * @return {@code true} if the key has been overridden in any source, {@code false} otherwise
	 */
	protected boolean isOverriden(final String key) {
		return isOverridenInSystemProperties(key) || isOverridenInStore(key);
	}

	/**
	 * Checks if is overriden in system properties.
	 *
	 * @param key
	 *            the key
	 * @return true, if is overriden in system properties
	 */
	protected boolean isOverridenInSystemProperties(final String key) {
		return System.getProperty(key) != null;
	}

	/**
	 * Checks if is overriden in store.
	 *
	 * @param key
	 *            the key
	 * @return true, if is overriden in store
	 */
	protected boolean isOverridenInStore(final String key) {
		return overriddenKeys.contains(key);
	}

	/**
	 * Returns the live key set of the internal preference registry. The returned collection reflects the current state
	 * of the registry and iterates in insertion (registration) order.
	 *
	 * @return a {@link Collection} of all registered preference keys; never {@code null}
	 */
	@Override
	public Collection<String> getKeys() { return map.keySet(); }

	/**
	 * Returns the live values collection of the internal preference registry. The returned collection reflects the
	 * current state of the registry and iterates in insertion (registration) order.
	 *
	 * @return a {@link Collection} of all registered {@link Pref} instances; never {@code null}
	 */
	@Override
	public Collection<Pref> getPreferences() { return map.values(); }

	/**
	 * Looks up a registered preference by its key.
	 *
	 * @param key
	 *            the preference key to look up
	 * @return the {@link Pref} registered under {@code key}, or {@code null} if no such preference exists
	 */
	@Override
	public Pref get(final String key) {
		return map.get(key);
	}

	/**
	 * Applies preference overrides from the properties file at {@code path} and then re-registers all currently
	 * registered preferences, populating {@code modelValues} with their (potentially overridden) values. This method is
	 * called at model load time to allow a GAML model to carry its own preference snapshot.
	 *
	 * @param path
	 *            the absolute file-system path of the properties file to load; the file is imported via
	 *            {@link #loadFromProperties(String)} before preferences are re-registered
	 * @param modelValues
	 *            a mutable map that will be populated with {@code key -> value} entries for every registered preference
	 *            after the overrides have been applied
	 */
	@Override
	public void applyPreferencesFrom(final String path, final Map<String, Object> modelValues) {
		// DEBUG.OUT("Apply preferences from " + path);
		loadFromProperties(path);
		for (final Pref<?> e : getPreferences()) {
			register(e);
			modelValues.put(e.getKey(), e.getValue());
		}
	}

}