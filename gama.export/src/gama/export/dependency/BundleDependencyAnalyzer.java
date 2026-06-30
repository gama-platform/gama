package gama.export.dependency;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BundleDependencyAnalyzer {

    private static String jdepsLocation = Path.of(System.getProperty("java.home"),"bin","jdeps").toString();

    private final Collection<String> exportedRoots;

    public BundleDependencyAnalyzer(Collection<String> exportedRoots) {
        this.exportedRoots = Objects.requireNonNull(exportedRoots);
    }

    public Set<Path> analyze(Set<Path> targetBundles, Path libsRoot) throws IOException {

        List<Path> libraries; // gama.dependencies libs

        // retrive gama.dependencies libs
        try (Stream<Path> stream = Files.walk(libsRoot)) {
            libraries = stream
                    .filter(p -> p.toString().endsWith(".jar"))
                    .toList();
        }

        // mapping libraries with the packages they export
        Map<Path, Set<String>> exportedPackages = new HashMap<>();

        // System.out.println("Scanning exported packages");

        for (Path library : libraries) {
            exportedPackages.put(library, getExportedPackages(library));
        }

        //  scan for used library can begin
        Map<Path, Set<String>> usedLibraries = new HashMap<>();

        // System.out.println("Scanning used packages");

        for (Path bundle : targetBundles) {

            // System.out.println(bundle.getFileName());

            Set<String> dependencies =
                    filterDependencies(getDependencies(bundle));


            for (Map.Entry<Path, Set<String>> entry : exportedPackages.entrySet()) {

                Path library = entry.getKey();
                Set<String> exports = entry.getValue();

                Set<String> intersection = new HashSet<String>(dependencies.size());
                
                for (String dependency : dependencies)
                {
                    for (String exportedPackage : exports)
                    {
                        if (dependency.startsWith(exportedPackage) || exportedPackage.startsWith(dependency))
                        {
                            intersection.add(exportedPackage);
                        }
                    }
                }

                if (!intersection.isEmpty()) {

                    // System.out.println("\t" + library);

                    // intersection.forEach(
                    //         dep -> // System.out.println("\t\t" + dep));

                    usedLibraries
                            .computeIfAbsent(library, k -> new HashSet<>())
                            .addAll(intersection);
                }
            }
        }

        // System.out.println();
        // System.out.println(usedLibraries.size() + " useful libs found");

        // usedLibraries.keySet()
        //         .forEach(System.out::println);

        Set<Path> removable =
                new HashSet<>(libraries);

        removable.removeAll(usedLibraries.keySet());

        // System.out.println();
        // System.out.println("You can remove:");

        // removable.forEach(System.out::println);

        return removable;
    }

    private Set<String> filterDependencies(Set<String> dependencies) {

        Set<String> result = new HashSet<>();

        for (String dependency : dependencies) {
            for (String exportedRoot : exportedRoots) {

                if (dependency.startsWith(exportedRoot)) {
                    result.add(exportedRoot);
                    result.add(dependency);
                    break;
                }
            }
        }

        return result;
    }

    private Set<String> getDependencies(Path jar) {

        Set<String> result = new HashSet<>();

        ProcessBuilder pb = new ProcessBuilder(
                jdepsLocation,
                "--recursive",
                jar.toAbsolutePath().toString());

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
                    "Failed to execute jdeps on " + jar, e);
        }

        return result;
    }

private Set<String> getExportedPackages(Path jar) {

    try (JarFile jarFile = new JarFile(jar.toFile())) {

        return jarFile.stream()
                .map(entry -> entry.getName())
                .filter(name -> name.endsWith(".class"))
                .map(name -> name
                        .replace('/', '.')
                        .substring(0, name.length() - 6))
                .collect(Collectors.toSet());

    } catch (IOException e) {
        throw new RuntimeException(
                "Failed to read classes from " + jar, e);
        }
    }
}