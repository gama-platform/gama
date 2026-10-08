/*******************************************************************************************************
 *
 * package-info.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

/**
 * The utils package provides utility classes and helper functions used throughout the GAMA platform.
 * 
 * <p>This package contains a wide variety of utility classes for common operations including
 * collections management, file handling, geometry operations, preferences, benchmarking,
 * and more.</p>
 * 
 * <h2>Core Utilities</h2>
 * 
 * <h3>Properties and Preferences:</h3>
 * <ul>
 *   <li>{@link gama.api.utils.GamlProperties} - Properties for GAML models</li>
 *   <li>{@link gama.api.utils.prefs.GamaPreferences} - Platform preferences system</li>
 *   <li>{@link gama.api.utils.prefs.IGamaPreferenceStore} - Preference store interface</li>
 *   <li>{@link gama.api.utils.prefs.GamaPreferenceStore} - Standard preference store</li>
 * </ul>
 * 
 * <h3>Java Utilities:</h3>
 * <ul>
 *   <li>{@link gama.api.utils.JavaUtils} - General Java utility methods</li>
 *   <li>{@link gama.api.utils.MathUtils} - Mathematical utility functions</li>
 * </ul>
 * 
 * <h3>Collections and Data Structures:</h3>
 * <ul>
 *   <li>{@link gama.api.utils.PoolUtils} - Object pooling utilities</li>
 *   <li>{@link gama.api.utils.AttributeHolder} - Holder for named attributes</li>
 * </ul>
 * 
 * <h3>Random Number Generation:</h3>
 * <ul>
 *   <li>{@link gama.api.utils.random.IRandom} - Random number generator interface</li>
 * </ul>
 * 
 * <h3>Benchmarking:</h3>
 * <ul>
 *   <li>{@link gama.api.utils.benchmark.Benchmark} - Benchmarking framework</li>
 *   <li>{@link gama.api.utils.benchmark.IBenchmarkable} - Interface for benchmarkable code</li>
 *   <li>{@link gama.api.utils.benchmark.StopWatch} - Simple stopwatch utility</li>
 * </ul>
 * 
 * <h3>File Handling:</h3>
 * <ul>
 *   <li>{@link gama.api.utils.files.IFileMetadataProvider} - Provider for file metadata</li>
 *   <li>{@link gama.api.utils.files.IGamaFileMetaData} - File metadata interface</li>
 * </ul>
 * 
 * <h3>JSON Support:</h3>
 * <ul>
 *   <li>{@link gama.api.utils.json.IJson} - JSON serialization interface</li>
 * </ul>
 * 
 * <h3>Server:</h3>
 * <ul>
 *   <li>{@link gama.api.utils.server.IGamaServer} - Server interface for remote access</li>
 * </ul>
 * 
 * <h3>Listeners:</h3>
 * <ul>
 *   <li>{@link gama.api.utils.interfaces.ITopLevelAgentChangeListener} - Listener for top-level agent changes</li>
 * </ul>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <h3>Accessing Preferences:</h3>
 * <pre>{@code
 * GamaPreferences.create("my.pref.key", "default value", IType.STRING);
 * String value = GamaPreferences.get("my.pref.key").getValue();
 * }</pre>
 * 
 * <h3>Benchmarking Code:</h3>
 * <pre>{@code
 * Benchmark.beginSection("my_operation");
 * // ... code to benchmark
 * Benchmark.endSection("my_operation");
 * }</pre>
 * 
 * <h3>Using Object Pools:</h3>
 * <pre>{@code
 * Object obj = PoolUtils.getPool().get();
 * try {
 *     // Use object
 * } finally {
 *     PoolUtils.getPool().release(obj);
 * }
 * }</pre>
 * 
 * @author Alexis Drogoul
 * @author The GAMA Development Team
 * @since GAMA 1.0
 * @version 2025-03
 * 
 * @see gama.api.utils.prefs
 * @see gama.api.utils.benchmark
 * @see gama.api.utils.files
 */
package gama.api.utils;
