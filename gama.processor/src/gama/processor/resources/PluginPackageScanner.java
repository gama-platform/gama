/*******************************************************************************************************
 *
 * PluginPackageScanner.java, in gama.processor, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.processor.resources;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A utility class that scans Eclipse plugins in a workspace and lists all Java packages found in their source
 * directories. This is useful for documentation generation, dependency analysis, and understanding the structure of
 * GAMA plugins.
 *
 * <h2>Features</h2>
 * <ul>
 * <li>Scans all plugins in an Eclipse workspace</li>
 * <li>Identifies standard source directories (src, src-gen, etc.)</li>
 * <li>Extracts package names from directory structure</li>
 * <li>Provides per-plugin and global package lists</li>
 * <li>Supports filtering and sorting options</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 *
 * <pre>
 * // Create scanner for workspace
 * PluginPackageScanner scanner = new PluginPackageScanner("/path/to/workspace");
 *
 * // Scan all plugins
 * scanner.scanWorkspace();
 *
 * // Get all packages from all plugins
 * Set&lt;String&gt; allPackages = scanner.getAllPackages();
 *
 * // Get packages for a specific plugin
 * Set&lt;String&gt; corePackages = scanner.getPackagesForPlugin("gama.core");
 *
 * // Get plugins containing a specific package
 * List&lt;String&gt; plugins = scanner.getPluginsContainingPackage("gama.core.kernel");
 *
 * // Print summary
 * scanner.printSummary();
 * </pre>
 *
 * @author drogoul
 * @since GAMA 2025-03
 */
public class PluginPackageScanner {

	// ================================================================================================
	// CONSTANTS
	// ================================================================================================

	/** Standard source directory names in Eclipse plugins */
	private static final String[] SOURCE_DIRECTORIES = { "src", "src-gen", "gaml", "models" };

	/** Plugin identifier patterns (folders containing these files are considered plugins) */
	private static final String[] PLUGIN_MARKERS = { "META-INF/MANIFEST.MF", "pom.xml", "build.properties" };

	// ================================================================================================
	// FIELDS
	// ================================================================================================

	/** The workspace root directory */
	private final File workspaceRoot;

	/** Map of plugin name to set of packages found in that plugin */
	private final Map<String, Set<String>> pluginPackages;

	/** Set of all packages across all plugins */
	private final Set<String> allPackages;

	/** List of all plugin names found in the workspace */
	private final List<String> pluginNames;

	/** Whether the workspace has been scanned */
	private boolean scanned;

	// ================================================================================================
	// CONSTRUCTORS
	// ================================================================================================

	/**
	 * Creates a new plugin package scanner for the specified workspace.
	 *
	 * @param workspacePath
	 *            the absolute path to the Eclipse workspace root
	 * @throws IllegalArgumentException
	 *             if the workspace path is null or doesn't exist
	 */
	public PluginPackageScanner(final String workspacePath) {
		if (workspacePath == null || workspacePath.isEmpty())
			throw new IllegalArgumentException("Workspace path cannot be null or empty");

		this.workspaceRoot = new File(workspacePath);
		if (!this.workspaceRoot.exists() || !this.workspaceRoot.isDirectory())
			throw new IllegalArgumentException("Workspace path must be an existing directory: " + workspacePath);

		this.pluginPackages = new TreeMap<>(); // TreeMap for sorted output
		this.allPackages = new TreeSet<>(); // TreeSet for sorted output
		this.pluginNames = new ArrayList<>();
		this.scanned = false;
	}

	/**
	 * Creates a new plugin package scanner for the specified workspace.
	 *
	 * @param workspaceRoot
	 *            the workspace root directory
	 * @throws IllegalArgumentException
	 *             if the workspace root is null or doesn't exist
	 */
	public PluginPackageScanner(final File workspaceRoot) {
		this(workspaceRoot != null ? workspaceRoot.getAbsolutePath() : null);
	}

	// ================================================================================================
	// SCANNING METHODS
	// ================================================================================================

	/**
	 * Scans the entire workspace for plugins and their packages. This method must be called before accessing package
	 * data.
	 *
	 * @return this scanner instance for method chaining
	 * @throws IOException
	 *             if an I/O error occurs during scanning
	 */
	public PluginPackageScanner scanWorkspace() {
		pluginPackages.clear();
		allPackages.clear();
		pluginNames.clear();

		File[] files = workspaceRoot.listFiles();

		for (File file : files) {
			if (file.isDirectory() && isPlugin(file)) {
				String pluginName = file.getName();
				pluginNames.add(pluginName);
				Set<String> packages = scanPlugin(file);
				if (!packages.isEmpty()) {
					pluginPackages.put(pluginName, packages);
					allPackages.addAll(packages);
				}
			}
		}

		scanned = true;
		return this;
	}

	/**
	 * Scans a specific plugin for packages.
	 *
	 * @param pluginName
	 *            the name of the plugin to scan
	 * @return the set of packages found in the plugin
	 * @throws IOException
	 *             if an I/O error occurs during scanning
	 * @throws IllegalArgumentException
	 *             if the plugin doesn't exist
	 */
	public Set<String> scanSpecificPlugin(final String pluginName) throws IOException {
		File pluginDir = new File(workspaceRoot, pluginName);
		if (!pluginDir.exists() || !pluginDir.isDirectory())
			throw new IllegalArgumentException("Plugin not found: " + pluginName);

		Set<String> packages = scanPlugin(pluginDir);
		if (!packages.isEmpty()) {
			pluginPackages.put(pluginName, packages);
			allPackages.addAll(packages);
			if (!pluginNames.contains(pluginName)) { pluginNames.add(pluginName); }
		}
		return packages;
	}

	/**
	 * Scans a single plugin directory for Java packages.
	 *
	 * @param pluginDir
	 *            the plugin directory
	 * @return the set of packages found
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	private Set<String> scanPlugin(final File pluginDir) {
		Set<String> packages = new HashSet<>();

		// Check each standard source directory
		for (String srcDirName : SOURCE_DIRECTORIES) {
			File srcDir = new File(pluginDir, srcDirName);
			if (srcDir.exists() && srcDir.isDirectory()) { packages.addAll(extractPackages(srcDir.toPath())); }
		}

		return packages;
	}

	/**
	 * Extracts package names from a source directory by walking the directory tree.
	 *
	 * @param sourceRoot
	 *            the source root directory
	 * @return the set of package names found
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	private Set<String> extractPackages(final Path sourceRoot) {
		Set<String> packages = new HashSet<>();

		try (Stream<Path> paths = Files.walk(sourceRoot)) {
			paths.filter(Files::isDirectory).filter(path -> !path.equals(sourceRoot)).forEach(path -> {
				// Check if this directory contains Java files
				try {
					boolean hasJavaFiles =
							Files.list(path).anyMatch(p -> p.toString().endsWith(".java") && Files.isRegularFile(p));
					if (hasJavaFiles) {
						// Convert path to package name
						Path relativePath = sourceRoot.relativize(path);
						String packageName = relativePath.toString().replace(File.separatorChar, '.');
						packages.add(packageName);
					}
				} catch (IOException e) {}
			});
		} catch (IOException e1) {}

		return packages;
	}

	/**
	 * Determines if a directory is an Eclipse plugin by checking for marker files.
	 *
	 * @param directory
	 *            the directory to check
	 * @return true if the directory appears to be a plugin
	 */
	private boolean isPlugin(final File directory) {
		for (String marker : PLUGIN_MARKERS) {
			File markerFile = new File(directory, marker);
			if (markerFile.exists()) return true;
		}
		return false;
	}

	// ================================================================================================
	// QUERY METHODS
	// ================================================================================================

	/**
	 * Gets all packages found across all scanned plugins.
	 *
	 * @return an immutable set of all package names
	 * @throws IllegalStateException
	 *             if the workspace hasn't been scanned yet
	 */
	public Set<String> getAllPackages() {
		ensureScanned();
		return Collections.unmodifiableSet(allPackages);
	}

	/**
	 * Gets packages found in a specific plugin.
	 *
	 * @param pluginName
	 *            the name of the plugin
	 * @return an immutable set of package names, or empty set if plugin not found
	 * @throws IllegalStateException
	 *             if the workspace hasn't been scanned yet
	 */
	public Set<String> getPackagesForPlugin(final String pluginName) {
		ensureScanned();
		Set<String> packages = pluginPackages.get(pluginName);
		return packages != null ? Collections.unmodifiableSet(packages) : Collections.emptySet();
	}

	/**
	 * Gets all plugins that contain the specified package.
	 *
	 * @param packageName
	 *            the package name to search for
	 * @return a list of plugin names containing the package
	 * @throws IllegalStateException
	 *             if the workspace hasn't been scanned yet
	 */
	public List<String> getPluginsContainingPackage(final String packageName) {
		ensureScanned();
		return pluginPackages.entrySet().stream().filter(entry -> entry.getValue().contains(packageName))
				.map(Map.Entry::getKey).collect(Collectors.toList());
	}

	/**
	 * Gets all plugin names found in the workspace.
	 *
	 * @return an immutable list of plugin names
	 * @throws IllegalStateException
	 *             if the workspace hasn't been scanned yet
	 */
	public List<String> getAllPluginNames() {
		ensureScanned();
		return Collections.unmodifiableList(pluginNames);
	}

	/**
	 * Gets the complete mapping of plugins to their packages.
	 *
	 * @return an immutable map of plugin names to sets of package names
	 * @throws IllegalStateException
	 *             if the workspace hasn't been scanned yet
	 */
	public Map<String, Set<String>> getPluginPackageMap() {
		ensureScanned();
		Map<String, Set<String>> result = new HashMap<>();
		pluginPackages.forEach((key, value) -> result.put(key, Collections.unmodifiableSet(value)));
		return Collections.unmodifiableMap(result);
	}

	/**
	 * Gets packages that match a specific prefix.
	 *
	 * @param prefix
	 *            the package prefix (e.g., "gama.core")
	 * @return a set of packages starting with the prefix
	 * @throws IllegalStateException
	 *             if the workspace hasn't been scanned yet
	 */
	public Set<String> getPackagesWithPrefix(final String prefix) {
		ensureScanned();
		return allPackages.stream().filter(pkg -> pkg.startsWith(prefix)).collect(Collectors.toSet());
	}

	/**
	 * Checks if a specific package exists in any plugin.
	 *
	 * @param packageName
	 *            the package name to check
	 * @return true if the package exists
	 * @throws IllegalStateException
	 *             if the workspace hasn't been scanned yet
	 */
	public boolean packageExists(final String packageName) {
		ensureScanned();
		return allPackages.contains(packageName);
	}

	// ================================================================================================
	// STATISTICS METHODS
	// ================================================================================================

	/**
	 * Gets the total number of unique packages found.
	 *
	 * @return the package count
	 * @throws IllegalStateException
	 *             if the workspace hasn't been scanned yet
	 */
	public int getPackageCount() {
		ensureScanned();
		return allPackages.size();
	}

	/**
	 * Gets the total number of plugins found.
	 *
	 * @return the plugin count
	 * @throws IllegalStateException
	 *             if the workspace hasn't been scanned yet
	 */
	public int getPluginCount() {
		ensureScanned();
		return pluginNames.size();
	}

	/**
	 * Gets the number of plugins that contain at least one package.
	 *
	 * @return the count of plugins with packages
	 * @throws IllegalStateException
	 *             if the workspace hasn't been scanned yet
	 */
	public int getPluginsWithPackagesCount() {
		ensureScanned();
		return pluginPackages.size();
	}

	// ================================================================================================
	// OUTPUT METHODS
	// ================================================================================================

	/**
	 * Prints a summary of the scan results to standard output.
	 *
	 * @throws IllegalStateException
	 *             if the workspace hasn't been scanned yet
	 */
	public void printSummary() {
		ensureScanned();
		System.out.println("======================================");
		System.out.println("Plugin Package Scanner Summary");
		System.out.println("======================================");
		System.out.println("Workspace: " + workspaceRoot.getAbsolutePath());
		System.out.println("Total plugins found: " + pluginNames.size());
		System.out.println("Plugins with packages: " + pluginPackages.size());
		System.out.println("Total unique packages: " + allPackages.size());
		System.out.println("======================================");
	}

	/**
	 * Prints detailed information about all plugins and their packages.
	 *
	 * @throws IllegalStateException
	 *             if the workspace hasn't been scanned yet
	 */
	public void printDetails() {
		ensureScanned();
		printSummary();
		System.out.println("\nPlugin Details:");
		System.out.println("======================================");

		for (Map.Entry<String, Set<String>> entry : pluginPackages.entrySet()) {
			System.out.println("\n" + entry.getKey() + " (" + entry.getValue().size() + " packages):");
			for (String pkg : entry.getValue()) { System.out.println("  - " + pkg); }
		}
	}

	/**
	 * Prints all unique packages found across all plugins.
	 *
	 * @throws IllegalStateException
	 *             if the workspace hasn't been scanned yet
	 */
	public void printAllPackages() {
		ensureScanned();
		System.out.println("All Packages (" + allPackages.size() + "):");
		System.out.println("======================================");
		for (String pkg : allPackages) { System.out.println(pkg); }
	}

	// ================================================================================================
	// UTILITY METHODS
	// ================================================================================================

	/**
	 * Ensures the workspace has been scanned before accessing data.
	 *
	 * @throws IllegalStateException
	 *             if not scanned
	 */
	private void ensureScanned() {
		if (!scanned) throw new IllegalStateException("Workspace has not been scanned. Call scanWorkspace() first.");
	}

	/**
	 * Checks if the workspace has been scanned.
	 *
	 * @return true if scanned
	 */
	public boolean isScanned() { return scanned; }

	// ================================================================================================
	// MAIN METHOD (for testing)
	// ================================================================================================

	/**
	 * Main method to generate gama.api packages list file.
	 * 
	 * <p>
	 * Scans the workspace, extracts all packages from the gama.api plugin that start with "gama.api", and writes them
	 * to a text file in the gama.processor.resources package. The output file contains only package names, one per
	 * line, with no comments.
	 * </p>
	 *
	 * @param args
	 *            command line arguments (optional: workspace path as first argument)
	 */
	public static void main(final String[] args) {
		try {
			// Determine workspace path
			String workspacePath = args.length > 0 ? args[0] : "/Users/drogoul/Git/gama";
			
			// Create scanner and scan workspace
			PluginPackageScanner scanner = new PluginPackageScanner(workspacePath);
			scanner.scanWorkspace();
			
			// Get packages for gama.api and filter to only include those starting with "gama.api"
			Set<String> apiPackages = scanner.getPackagesForPlugin("gama.api").stream()
					.filter(pkg -> pkg.startsWith("gama.api"))
					.collect(java.util.stream.Collectors.toSet());
			
			// Determine output file path
			File processorSourceDir = new File("src/gama/processor/resources");
			if (!processorSourceDir.exists()) {
				processorSourceDir.mkdirs();
			}
			
			File outputFile = new File(processorSourceDir, "gama-api-packages.txt");
			
			// Write packages to file - no comments, just package names
			try (java.io.PrintWriter writer = new java.io.PrintWriter(
					new java.io.FileWriter(outputFile))) {
				
				// Write packages in sorted order, one per line, no comments
				apiPackages.stream()
						.sorted()
						.forEach(writer::println);
				
				writer.flush();
			}
			
			// Print success message
			System.out.println("✅ Successfully wrote " + apiPackages.size() + 
					" packages to: " + outputFile.getAbsolutePath());
			System.out.println();
			System.out.println("Preview (first 10 packages):");
			apiPackages.stream()
					.sorted()
					.limit(10)
					.forEach(pkg -> System.out.println("  - " + pkg));
			
			if (apiPackages.size() > 10) {
				System.out.println("  ... and " + (apiPackages.size() - 10) + " more");
			}
			
		} catch (IOException e) {
			System.err.println("❌ Error writing packages file: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		} catch (Exception e) {
			System.err.println("❌ Error: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}
}
