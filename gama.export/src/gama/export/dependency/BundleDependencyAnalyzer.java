package gama.export.dependency;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Collections;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import gama.export.ExportHelper;
import gama.export.ZipHelper;


public class BundleDependencyAnalyzer {

    private static String jdepsLocation = Path.of(System.getProperty("java.home"),"bin","jdeps").toString();

    /**
     * get the set of dependencies classes of a jar
     * 
     * @param jarPath
     *              Path of the target jar
     * 
     * @return Set<String> of the dependencies
     */
    private static Set<String> getDependencies(Path jarPath) {

        Set<String> result = new HashSet<>();

        ProcessBuilder pb = new ProcessBuilder(
                jdepsLocation,
                "--recursive",
                jarPath.toAbsolutePath().toString());

        try {

            Process process = pb.start();

            try (BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(process.getInputStream()))) {

                String line;

                while ((line = reader.readLine()) != null) {
                    int arrow = line.indexOf("->");

                    if (arrow < 0) {
                        continue;
                    }

                    String dependency =
                            line.substring(arrow + 2).trim();


                    int firstSpace = dependency.indexOf(' ');

                    if (firstSpace > 0) {
                        dependency =
                                dependency.substring(0, firstSpace);
                    }

                    if (!dependency.isBlank()) {
                        result.add(dependency);
                    }
                }
            }

            process.waitFor();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to execute jdeps on " + jarPath, e);
        }

        return result;
    }

    /**
     * Get the set of classes inside a jar.
     * Assume each class is declared in its own file.
     * 
     * @param jarPath
     *              Path of the target jar
     * 
     * @return Set<String> of the classes
     */
    private static Set<String> getClasses(Path jarPath) {

        try (JarFile jarFile = new JarFile(jarPath.toFile())) {

            return jarFile.stream()
                    .map(entry -> entry.getName())
                    .filter(name -> name.endsWith(".class"))
                    .map(name -> name
                            .replace('/', '.')
                            .substring(0, name.length() - 6))
                    .collect(Collectors.toSet());

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to read classes from " + jarPath, e);
            }
        }

    /**
     * Get the MANIFEST attribute of a jar 
     * 
     * @param jarPath
     *              Path of the target jar
     * 
     * @param attribute
     *                name of the attribute
     * 
     * @return Set<String> of values in the target attribute of target MANIFEST.MF
     */
    public static Set<String> getManifestAttribute(Path jarPath, String attribute)
    {
        try {
            Manifest manifest = null; 

            if(Files.isDirectory(jarPath))
            {
                InputStream is = new FileInputStream(jarPath.resolve("META-INF").resolve("MANIFEST.MF").toString());
                manifest = new Manifest(is);
            } else {
                JarFile jar = new JarFile(jarPath.toString());
                manifest = jar.getManifest();
            }

            Attributes attribs = manifest.getMainAttributes();
        
            return parseOsgiHeader(attribs.getValue(attribute));

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.<String>emptySet();
        }
    }

    /**
     * Parse a MANIFEST.MF OSGI bundle header content
     * 
     * @param headerValue
     *                  the String content of the header to parse
     * 
     * @return Set<String> of values in the target header
     */
    private static Set<String> parseOsgiHeader(String headerValue) {
        Set<String> elements = new HashSet<>();
        if (headerValue == null || headerValue.trim().isEmpty()) {
            return elements;
        }

        // Regex split on commas that are NOT inside double quotes
        String[] parts = headerValue.split(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)");

        for (String part : parts) {
            // Strip out any trailing semicolon directives/parameters (e.g., ;version="1.0.0")
            String cleanIdentifier = part.split(";")[0].trim();
            if (!cleanIdentifier.isEmpty()) {
                elements.add(cleanIdentifier);
            }
        }
        return elements;
    }

    /**
     * Get the Path of a plugin from its name
     * 
     * @param name
     *              name of the target plugin
     * 
     * @return Path of the target plugin
     */
    public static Path getPluginPath(String name)
    {
        Bundle bundle = Platform.getBundle(name);

        if (bundle == null)
            return null;
        else
            return Path.of(ExportHelper.resolveEmbeddedPath(bundle.getLocation().replaceAll(".*file:","")));
    }

    /**
     * Resolve all the required GAMA plugins recursively,
     * from a subset of known required GAMA plugins.
     * The dependencies relations are based on MANFIEST.MF.
     * 
     * @param plugins
     *              needed GAMA plugins names (Sring)
     * 
     * @return Set<Path> of all the required GAMA plugins
     */
    public static Set<Path> getMinimalGamaPluginSet(Set<String> plugins)
    {
        Set<Path> necessaryGamaPlugins = new HashSet<Path>();
        Set<String> alreadySeenPlugins = new HashSet<String>();

        Set<String> pluginsToCheck = new HashSet<String>(plugins);
        
        while(! pluginsToCheck.isEmpty())
        {
            Set<String> foundPlugins = new HashSet<String>();

            for (String plugin : pluginsToCheck)
            {
                alreadySeenPlugins.add(plugin);
                plugins.add(plugin);
                Path pluginPath = getPluginPath(plugin);
                
                if (pluginPath == null)
                    continue;

                necessaryGamaPlugins.add(pluginPath);

                // System.out.println(pluginPath);

                Set<String> neededPlugins = getManifestAttribute(pluginPath,"Require-Bundle");
                neededPlugins.addAll(getManifestAttribute(pluginPath,"Import-Package"));

                for (String neededPlugin : neededPlugins)
                {
                    if ((neededPlugin.startsWith("gama.") || neededPlugin.startsWith("gaml.")) 
                        && ! pluginsToCheck.contains(neededPlugin) 
                        && ! alreadySeenPlugins.contains(neededPlugin)) {
                        
                        foundPlugins.add(neededPlugin);
                    }
                }
            }

            pluginsToCheck = foundPlugins;
        }

        return necessaryGamaPlugins;
    }
}