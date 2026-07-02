package gama.export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.HashSet;
import java.util.Comparator;
import java.util.stream.Stream;
import java.nio.file.*;
import java.util.zip.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.prefs.Preferences;

import gama.api.utils.prefs.JREPreferenceStore;
import gama.api.utils.prefs.GamaPreferenceStore;
import gama.export.dependency.BundleDependencyAnalyzer;
import gama.export.ExportActivator;
import gama.export.ZipHelper;

public class GamaZipBuilder {

    private static Set<String> necessaryGamaModules = new HashSet<String>(Set.of(
        "gama.annotations",
        "gama.api",
        "gama.core",
        "gama.dev",
        "gama.export",
        "gama.dependencies",
        "gama.extension.batch",
        "gama.extension.image",
        "gama.extension.maths",
        "gama.extension.network",
        "gama.extension.serialize",
        "gama.extension.sound",
        "gama.extension.stats",
        "gama.headless",
        "gama.processor",
        "gama.ui.application",
        "gama.ui.display.java2d",
        "gama.ui.experiment",
        "gama.ui.navigator",
        "gama.ui.shared",
        "gama.ui.viewers",
        "gama.workspace",
        "gaml.compiler"
    ));

    // depends on the operating system
    private Set<String> neededGamaModules;

    private static final String embeddedWorkspaceName = "Embedded_Workspace";

    private static final Path appRootPath = Path.of(ExportActivator.appRootPathStr);
    
    private static final Path pluginsPath = Path.of(appRootPath.toString(),"plugins");

    private static final Path tmpDirectoryPath = Path.of(System.getProperty("java.io.tmpdir"),"gama.export.tmp");

    private static final Path gamaIniTmpPath = tmpDirectoryPath.resolve("gama.ini.tmp");

    private static final Path gamaPrefsTmpPath = tmpDirectoryPath.resolve("gama.prefs.tmp");

    private static final Path gamaDependenciesTmpPath = tmpDirectoryPath.resolve("gama.dependencies.tmp");
    
    private static String gamaDependenciesModuleFileName = null;

    private String targetWorkspacePathStr = null;
    
    private String targetModelPathStr = null;

    private String targetExperiment = null;

    private static Set<Path> dontZipPaths = new HashSet<Path>(Set.of(
        Path.of(appRootPath.toString(),"configuration","org.eclipse.equinox.app"),
        Path.of(appRootPath.toString(),"configuration","org.eclipse.equinox.launcher"),
        Path.of(appRootPath.toString(),"configuration","org.eclipse.osgi"),
        Path.of(appRootPath.toString(),"configuration","org.eclipse.core.runtime"),
        Path.of(appRootPath.toString(),"configuration","org.eclipse.e4.ui.css.swt.theme"),
        Path.of(appRootPath.toString(),"configuration",".settings"),
        Path.of(appRootPath.toString(),"Gama.ini")
    ));

    public static void deleteDirectory(Path pathToBeDeleted) {
        if (Files.isDirectory(pathToBeDeleted))
            try (Stream<Path> walk = Files.walk(pathToBeDeleted)) {
                walk.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            System.err.printf("Failed to delete %s%n", path);
                        }
                    });
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public GamaZipBuilder(Set<String> modules, String targetWorkspacePathStr, String targetModelPathStr, String targetExperiment,  final boolean shrinkGamaDependencies) 
    {
        this(modules,targetWorkspacePathStr,targetModelPathStr,targetExperiment);

        if(shrinkGamaDependencies)
            GamaZipBuilder.necessaryGamaModules.remove("gama.dependencies");
    }

    public GamaZipBuilder(Set<String> modules, String targetWorkspacePathStr, String targetModelPathStr, String targetExperiment) 
    {
        neededGamaModules = modules;
        this.targetWorkspacePathStr = targetWorkspacePathStr; 
        this.targetModelPathStr = targetModelPathStr;
        this.targetExperiment = targetExperiment;
    }

    private Stream<Path> filter(Stream<Path> stream)
    { 
        return stream
        // filter the non essential GAMA modules
            .filter(path -> {
                String filename = path.getFileName().toString();
                if
                (
                    path.startsWith(GamaZipBuilder.pluginsPath)
                    &&
                    filename.startsWith("gama.") && (filename.endsWith(".jar") || Files.isDirectory(path))
                    &&
                    (
                        ! necessaryGamaModules.stream()
                        .anyMatch(module -> filename.contains(module))
                        && 
                        ! neededGamaModules.stream()
                            .anyMatch(module -> filename.contains(module))
                    )
                ) 
                {
                    if (Files.isDirectory(path))
                    {
                        GamaZipBuilder.dontZipPaths.add(path);
                    }
                    else
                        if(filename.startsWith("gama.dependencies"))
                            GamaZipBuilder.gamaDependenciesModuleFileName = filename;
                    
                    return false;
                }

                return true;
            })
            // filter other non essential files  
            .filter(path -> ! dontZipPaths.stream()
                .anyMatch(dontZipPath -> path.startsWith(dontZipPath)));
    }

    public void zip(String outputPathStr) throws IOException
    {
        final Path outputPath = Path.of(outputPathStr);

        if(Files.isDirectory(GamaZipBuilder.tmpDirectoryPath))
            deleteDirectory(GamaZipBuilder.tmpDirectoryPath); 

        Files.createDirectories(GamaZipBuilder.tmpDirectoryPath);

        try(ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outputPath.toFile()))){
        
            ///////////////////////////////////////////
            // Zipping GAMA in the desired directory //
            ///////////////////////////////////////////

            // Walk the appRootPath tree stream
            try (Stream<Path> stream = filter(Files.walk(appRootPath))) {
                stream.forEach(sourcePath -> {
                    try {
                        if(! Files.isDirectory(sourcePath))
                        {
                            ZipEntry entry = new ZipEntry(appRootPath.relativize(sourcePath).toString());
                            // Replace existing files/directories if needed
                            // Create a new entry inside the ZIP archive
                            zos.putNextEntry(entry);
                            
                            // Write bytes to the entry
                            Files.copy(sourcePath, zos);
                            zos.closeEntry();
                        }
                        
                        // Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to copy: " + sourcePath, e);
                    }
                });
            } catch (RuntimeException e) {
                // Unwrap IOException from the stream loop
                if (e.getCause() instanceof IOException) {
                    throw (IOException) e.getCause();
                }
                throw e;
            }

            ////////////////////////////////////////
            // Filtering the desired dependencies //
            // in gama.dependencies               //
            ////////////////////////////////////////
            if (! GamaZipBuilder.necessaryGamaModules.contains("gama.dependencies"))
            {
                final Path gamaDependenciesJarTmpPath = Path.of(GamaZipBuilder.tmpDirectoryPath.toString(),GamaZipBuilder.gamaDependenciesModuleFileName);

                // Unzip dependencies in tmp folder
                ZipHelper.unzip(Path.of(GamaZipBuilder.pluginsPath.toString(),GamaZipBuilder.gamaDependenciesModuleFileName), GamaZipBuilder.gamaDependenciesTmpPath);

                // Read ande parse the META-INF/MANIFEST.MF file, to retrieve the exported packages

                final String manifest = Files.readString(Path.of(GamaZipBuilder.gamaDependenciesTmpPath.toString(),"META-INF","MANIFEST.MF"));
                Matcher matcher = Pattern.compile("(Export-Package[\s]*:[\s]*)([^:]*)\n([^\n:]*:)").matcher(manifest);

                if(! matcher.find())
                    System.err.println("an error occured while parsing manifest of gama.dependencies");
                
                final Set<String> exportedPackages = Set.of(matcher.group(2).replaceAll("[\n\s\r]","").split(","));

                // Analyze dependencies
                final BundleDependencyAnalyzer analyzer =
                    new BundleDependencyAnalyzer(exportedPackages);

                Set<Path> targetBundles = new HashSet<Path>();

                try (Stream<Path> stream = Files.walk(pluginsPath)) {
                    stream.forEach(pluginPath -> {
                        String pluginPathString = pluginPath.toString();
                        if( (GamaZipBuilder.necessaryGamaModules.stream().anyMatch( module -> 
                                pluginPathString.startsWith(pluginsPath.resolve(module).toString())) 
                            || 
                            this.neededGamaModules.stream().anyMatch( module -> 
                                    pluginPathString.startsWith(pluginsPath.resolve(module).toString())))
                            && !Files.isDirectory(pluginPath) && pluginPathString.endsWith(".jar")
                        )
                            targetBundles.add(pluginPath);
                    });
                }

                final Set<Path> removableDependencies = analyzer.analyze(targetBundles,GamaZipBuilder.gamaDependenciesTmpPath);
                
                // Remove non essential dependencies

                for(Path dependency : removableDependencies)
                {
                    Files.delete(dependency);
                }

                // Zip dependencies back
                ZipHelper.zip(GamaZipBuilder.gamaDependenciesTmpPath,gamaDependenciesJarTmpPath);         

                // Delete tmp folder
                deleteDirectory(GamaZipBuilder.gamaDependenciesTmpPath);

                // Add dependencies to final zip

                ZipEntry entry = new ZipEntry(Path.of("plugins",GamaZipBuilder.gamaDependenciesModuleFileName).toString());
                // Replace existing files/directories if needed
                // Create a new entry inside the ZIP archive
                zos.putNextEntry(entry);

                // Write bytes to the entry
                Files.copy(gamaDependenciesJarTmpPath, zos);
                zos.closeEntry();            
            }
            /////////////////////////////////////
            // Applying the needed preferences //
            // to start a simulation           //
            /////////////////////////////////////

            // switching to non global preferences
            Files.copy(appRootPath.resolve("Gama.ini"),GamaZipBuilder.gamaIniTmpPath);
            String gamaIniContent = Files.readString(GamaZipBuilder.gamaIniTmpPath); 

            if (gamaIniContent.contains("\n-Duse_global_preference_store="))
                gamaIniContent = gamaIniContent.replaceAll("\n-Duse_global_preference_store=.*","\n-Duse_global_preference_store=false");
            else
                gamaIniContent += "\n-Duse_global_preference_store=false\n";

            if (gamaIniContent.contains("\n-Dsimulation_only="))
                gamaIniContent = gamaIniContent.replaceAll("\n-Dsimulation_only=.*","\n-Dsimulation_only=true");
            else
                gamaIniContent += "\n-Dsimulation_only=true\n";


            Files.writeString(GamaZipBuilder.gamaIniTmpPath,gamaIniContent);

            ZipEntry gamaIniEntry = new ZipEntry(Path.of("Gama.ini").toString());
            // Replace existing files/directories if needed
            // Create a new entry inside the ZIP archive
            zos.putNextEntry(gamaIniEntry);
            
            // Write bytes to the entry
            Files.copy(GamaZipBuilder.gamaIniTmpPath, zos);
            zos.closeEntry();

            // creating / updating preferences
            JREPreferenceStore store = new JREPreferenceStore(Preferences.userRoot().node(GamaPreferenceStore.NODE_NAME));

            //pref error display
            // show errors in editor
            store.putInStore("pref_workspace_path",GamaZipBuilder.embeddedWorkspaceName);
            store.putInStore("pref_workspace_remember",true);
            store.putInStore("pref_startup_model",true);
            store.putInStore("pref_default_model",targetModelPathStr.replace(targetWorkspacePathStr,GamaZipBuilder.embeddedWorkspaceName));
            store.putInStore("pref_default_experiment",targetExperiment);
            
            store.saveToProperties(GamaZipBuilder.gamaPrefsTmpPath.toString());

            ZipEntry gamaPrefsEntry = new ZipEntry(Path.of("configuration",".settings","gama.prefs").toString());
            zos.putNextEntry(gamaPrefsEntry);
            
            // Write bytes to the entry
            Files.copy(GamaZipBuilder.gamaPrefsTmpPath, zos);
            zos.closeEntry();

            ////////////////////////////////////
            // Embedding the target workspace //
            ////////////////////////////////////

            try (Stream<Path> stream = Files.walk(Path.of(targetWorkspacePathStr))) {
                stream.forEach(filePath -> {
                    
                    try 
                    {
                        if(! Files.isDirectory(filePath))
                        {
                            zos.putNextEntry(
                                new ZipEntry(filePath.toString().replace(targetWorkspacePathStr,GamaZipBuilder.embeddedWorkspaceName)));
                            
                            Files.copy(filePath, zos);

                            zos.closeEntry();                        
                        }
                    } 
                    catch (IOException e)
                    {
                        throw new RuntimeException("Failed to copy: " + filePath, e);
                    }
                });
                
            } catch (RuntimeException e) {
                // Unwrap IOException from the stream loop
                if (e.getCause() instanceof IOException) {
                    throw (IOException) e.getCause();
                }
                throw e;
            }


            deleteDirectory(GamaZipBuilder.tmpDirectoryPath);
        }
    }



    
}