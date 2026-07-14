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
// import java.util.regex.Pattern;
// import java.util.regex.Matcher;
import java.util.prefs.Preferences;

import gama.api.utils.prefs.JREPreferenceStore;
import gama.api.utils.prefs.GamaPreferenceStore;
import gama.export.dependency.BundleDependencyAnalyzer;
import gama.export.ExportActivator;
import gama.export.ZipHelper;

public class GamaZipBuilder {


    /**
	 * The set of plugins which must always be included in a Gama distribution.
     * gama.dependencies and gama.ui.application are necessary but they
     * are treated separately
	 */
    private static Set<String> necessaryGamaModules = new HashSet<String>(Set.of(
        "gama.dependencies",
        "gama.extension.stats",
        "gama.headless",
        "gama.ui.application",
        "gama.ui.navigator",
        "gama.ui.shared",
        "gama.ui.viewers"
    ));

    // private static Set<String> necessaryGamaModules = new HashSet<String>(Set.of(
    //     "gama.annotations",
    //     "gama.api",
    //     "gama.core",
    //     "gama.dev",
    //     "gama.export",
    //     "gama.dependencies",
    //     "gama.extension.maths",
    //     "gama.extension.serialize",
    //     "gama.extension.stats",
    //     "gama.headless",
    //     "gama.processor",
    //     "gama.ui.experiment",
    //     "gama.ui.navigator",
    //     "gama.ui.shared",
    //     "gama.ui.viewers",
    //     "gama.workspace",
    //     "gaml.compiler"
    // ));

    // depends on the operating system
    private Set<String> neededGamaModules;

    private Set<Path> neededGamaModulesPath;

    private static final String embeddedWorkspaceName = "Embedded_Workspace";

    private static final Path appRootPath = Path.of(ExportActivator.appRootPathStr);
    
    private static final Path pluginsPath = appRootPath.resolve("plugins");

    private static final Path dropinsPath = appRootPath.resolve("dropins");

    private static final Path tmpDirectoryPath = Path.of(System.getProperty("java.io.tmpdir"),"gama.export.tmp");

    private static final Path gamaIniTmpPath = tmpDirectoryPath.resolve("gama.ini.tmp");

    private static final Path gamaPrefsTmpPath = tmpDirectoryPath.resolve("gama.prefs.tmp");

    private static final Path gamaUiApplicationJarTmpPath = tmpDirectoryPath.resolve("gama.ui.application.jar.tmp");

    private static final Path gamaDependenciesTmpPath = tmpDirectoryPath.resolve("gama.dependencies.tmp");

    private static final Path gamaDependenciesModulePath = BundleDependencyAnalyzer.getPluginPath("gama.dependencies");

    private static final Path gamaUiApplicationModulePath = BundleDependencyAnalyzer.getPluginPath("gama.ui.application");
    
    private static final String gamaDependenciesModuleFileName = gamaDependenciesModulePath.getFileName().toString();

    private static final String gamaUiApplicationModuleFileName = gamaUiApplicationModulePath.getFileName().toString();

    private String targetWorkspacePathStr = null;

    private boolean shrinkGamaDependencies;
    
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
        neededGamaModules = modules;
        neededGamaModules.addAll(necessaryGamaModules);

        this.targetWorkspacePathStr = Path.of(targetWorkspacePathStr).toString(); 
        this.targetModelPathStr = targetModelPathStr;
        this.targetExperiment = targetExperiment;

        // expanding necessary modules based on needed modules (GamlProperties doesn't expand the dependency tree)
        neededGamaModulesPath = BundleDependencyAnalyzer.getMinimalGamaModuleSet(neededGamaModules);

        System.out.println("Including the following plugins : ");
        neededGamaModules.forEach(System.out::println);

        if(shrinkGamaDependencies)
        {
            neededGamaModules.remove("gama.dependencies");
            neededGamaModulesPath.remove(gamaDependenciesModulePath);
        }

        neededGamaModules.remove("gama.ui.application");
        neededGamaModulesPath.remove(gamaUiApplicationModulePath);
        
        this.shrinkGamaDependencies = shrinkGamaDependencies;
    }

    public GamaZipBuilder(Set<String> modules, String targetWorkspacePathStr, String targetModelPathStr, String targetExperiment) 
    {
        this(modules,targetWorkspacePathStr,targetModelPathStr,targetExperiment,false);
    }

    private Stream<Path> filter(Stream<Path> stream)
    { 
        return stream
        // filter the non essential GAMA modules
            .filter(path -> {
                String filename = path.getFileName().toString();
                if((path.startsWith(GamaZipBuilder.pluginsPath) || path.startsWith(GamaZipBuilder.dropinsPath)) 
                    && (filename.startsWith("gama.") || filename.startsWith("gaml."))
                    && (filename.endsWith(".jar") || Files.isDirectory(path))
                    && ! neededGamaModulesPath.contains(path)
                    // && ! neededGamaModules.stream().anyMatch(module -> filename.contains(module))
                ) 
                {
                    if (Files.isDirectory(path))
                        GamaZipBuilder.dontZipPaths.add(path);

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

            ///////////////////////////////////////
            // Adding gama.ui.application to     //
            // the zip file and changing the     //
            // splash screen                     //
            ///////////////////////////////////////

            Files.copy(GamaZipBuilder.pluginsPath.resolve(GamaZipBuilder.gamaUiApplicationModuleFileName),GamaZipBuilder.gamaUiApplicationJarTmpPath);
            ZipHelper.renameEntry(GamaZipBuilder.gamaUiApplicationJarTmpPath,"splash.png","old_splash.png");
            ZipHelper.renameEntry(GamaZipBuilder.gamaUiApplicationJarTmpPath,"splash_simulation_launcher.png","splash.png");

            zos.putNextEntry(new ZipEntry(Path.of("plugins",GamaZipBuilder.gamaUiApplicationModuleFileName).toString()));

            Files.copy(GamaZipBuilder.gamaUiApplicationJarTmpPath, zos);

            zos.closeEntry();

            ////////////////////////////////////////
            // Filtering the desired dependencies //
            // in gama.dependencies               //
            ////////////////////////////////////////
            if (shrinkGamaDependencies)
            {
                final Path gamaDependenciesJarTmpPath = Path.of(GamaZipBuilder.tmpDirectoryPath.toString(),GamaZipBuilder.gamaDependenciesModuleFileName);

                // Unzip dependencies in tmp folder
                ZipHelper.unzip(GamaZipBuilder.gamaDependenciesModulePath, GamaZipBuilder.gamaDependenciesTmpPath);

                // Read ande parse the META-INF/MANIFEST.MF file, to retrieve the exported packages
                final Set<String> exportedPackages = BundleDependencyAnalyzer.getManifestAttribute(GamaZipBuilder.gamaDependenciesModulePath,"Export-Package");

                // Analyze dependencies
                final BundleDependencyAnalyzer analyzer =
                    new BundleDependencyAnalyzer(exportedPackages);

                neededGamaModulesPath.add(gamaUiApplicationModulePath);
                final Set<Path> removableDependencies = analyzer.analyze(neededGamaModulesPath,GamaZipBuilder.gamaDependenciesTmpPath);
                
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

            String workspacePathPreferenceOld = store.getInStore("pref_workspace_path","");
            String workspaceRememberPreferenceOld = store.getInStore("pref_workspace_remember","false");
            String startupModelPreferenceOld = store.getInStore("pref_startup_model","false");
            String defaultModelPreferenceOld = store.getInStore("pref_default_model","Enter Path");
            String defaultExperimentPreferenceOld = store.getInStore("pref_default_experiment","");
            String errorsInEditorPreferenceOld = store.getInStore("pref_errors_in_editor","true");

            //pref error display
            // show errors in editor
            store.putInStore("pref_workspace_path",GamaZipBuilder.embeddedWorkspaceName);
            store.putInStore("pref_workspace_remember",true);
            store.putInStore("pref_startup_model",true);
            store.putInStore("pref_default_model",targetModelPathStr.replace(targetWorkspacePathStr,GamaZipBuilder.embeddedWorkspaceName));
            store.putInStore("pref_default_experiment",targetExperiment);
            store.putInStore("pref_errors_in_editor",false);
            
            store.saveToProperties(GamaZipBuilder.gamaPrefsTmpPath.toString());

            store.putInStore("pref_workspace_path",workspacePathPreferenceOld);
            store.putInStore("pref_workspace_remember",workspaceRememberPreferenceOld);
            store.putInStore("pref_startup_model",startupModelPreferenceOld);
            store.putInStore("pref_default_model",defaultModelPreferenceOld);
            store.putInStore("pref_default_experiment",defaultExperimentPreferenceOld);
            store.putInStore("pref_errors_in_editor",errorsInEditorPreferenceOld);

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