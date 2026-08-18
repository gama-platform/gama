package gama.export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Comparator;
import java.util.stream.Stream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.*;
// import java.util.zip.ZipArchiveOutputStream;
// import java.util.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import java.net.URL;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.eclipse.core.resources.IProject;

import gama.api.utils.prefs.JREPreferenceStore;
import gama.api.utils.prefs.GamaPreferenceStore;
import gama.api.GAMA;
import gama.api.runtime.IWorkspaceManager;
import gama.api.runtime.SystemInfo;
import gama.export.dependency.BundleDependencyAnalyzer;
import gama.export.ExportActivator;
import gama.export.ZipHelper;
import gama.export.ExportHelper;
import gama.export.ElfPacker;

public class GamaZipBuilder {


    /**
	 * The set of plugins which must always be included in a Gama distribution.
     * gama.dependencies and gama.ui.application are necessary but they
     * are treated separately
	 */
    private static Set<String> necessaryGamaPlugins = new HashSet<String>(Set.of(
        "gama.dependencies",
        "gama.ui.application",
        "gama.ui.shared"
    ));

    // private static Set<String> necessaryGamaPlugins = new HashSet<String>(Set.of(
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
    private Set<String> neededGamaPlugins;

    private Set<Path> neededGamaPluginsPath;

    private static final String embeddedWorkspaceName = ExportHelper.getEmbeddedWorkspaceName();

    private static final Path embeddedJdkPath = Path.of("jdk");

    private static final Path appRootPath = Path.of(ExportActivator.appRootPathStr);
    
    private static final Path pluginsPath = appRootPath.resolve("plugins");

    private static final Path dropinsPath = appRootPath.resolve("dropins");

    private static final Path tmpDirectoryPath = Path.of(System.getProperty("java.io.tmpdir"),"gama.export.tmp");

    private static final Path gamaIniTmpPath = tmpDirectoryPath.resolve("gama.ini.tmp");

    private static final Path gamaPrefsTmpPath = tmpDirectoryPath.resolve("gama.prefs.tmp");

    private static final Path gamaUiApplicationJarTmpPath = tmpDirectoryPath.resolve("gama.ui.application.jar.tmp");

    private static final Path gamaUiApplicationPluginPath = BundleDependencyAnalyzer.getPluginPath("gama.ui.application");
    
    private static final String gamaUiApplicationPluginFileName = gamaUiApplicationPluginPath.getFileName().toString();

    private static final Set<String> shapeFileExtensionsSet = new HashSet<String>(Set.of(".shx",".dbf",".prj",".sbn",".sbx",".xml"));

    private IProject targetProject = null;
    
    private String targetModelRelativePathStr = null;

    private String targetProjectPathStr = null;

    private String targetWorkspacePathStr = null;

    private String targetExperiment = null;

    private boolean zipWithJdk = false;

    private boolean isOneFileExport = false;

    private static Path jdkPath = null; 
    /**
     * Data files referenced by the exported models, as absolute, normalized
     * paths. Those that live outside the exported project are rerouted into the
     * project's <code>include</code> directory and the path is rewritten in
     * every GAML file that references them.
     */
    private Set<String> dataFiles = null;

    private static Set<Path> dontZipPaths = new HashSet<Path>(Set.of(
        Path.of(appRootPath.toString(),"configuration","org.eclipse.equinox.app"),
        Path.of(appRootPath.toString(),"configuration","org.eclipse.equinox.launcher"),
        Path.of(appRootPath.toString(),"configuration","org.eclipse.osgi"),
        Path.of(appRootPath.toString(),"configuration","org.eclipse.core.runtime"),
        Path.of(appRootPath.toString(),"configuration","org.eclipse.e4.ui.css.swt.theme"),
        Path.of(appRootPath.toString(),"configuration",".settings"),
        Path.of(appRootPath.toString(),"jdk"),
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

    public void handleUpdateGamlImports(Path filePath, Path includeDir, Map<Path,String> externalDataFiles, ZipArchiveOutputStream zos) throws IOException, RuntimeException{
        String content = Files.readString(filePath, StandardCharsets.UTF_8);
        final Path gamlParent = filePath.getParent();

        for (final Map.Entry<Path, String> entry : externalDataFiles.entrySet()) {
            final Path sourcePath = entry.getKey();
            final String uniqueName = entry.getValue();
            // Only meaningful when both paths share the same root.
            // if (!gamlParent.getRoot().equals(sourcePath.getRoot())) {
            //     continue;
            // }
            final String originalString =
                    gamlParent.relativize(sourcePath).toString().replace('\\', '/');
            final String newRelativePath = gamlParent
                    .relativize(includeDir.resolve(uniqueName)).toString().replace('\\', '/');
            content = content.replace("\"" + originalString + "\"",
                    "\"" + newRelativePath + "\"");
            content = content.replace("'" + originalString + "'",
                    "'" + newRelativePath + "'");
        }
        zos.write(content.getBytes(StandardCharsets.UTF_8));
    }

    public GamaZipBuilder(final Set<String> plugins, final IProject targetProject,
            final String targetModelRelativePathStr, final String targetExperiment, 
            final Set<String> dataFiles, final boolean zipWithJdk, final boolean isOneFileExport) 
    {
        neededGamaPlugins = plugins;
        neededGamaPlugins.addAll(necessaryGamaPlugins);
        this.zipWithJdk = zipWithJdk;
        
        if(zipWithJdk && GamaZipBuilder.jdkPath == null)
            GamaZipBuilder.jdkPath = Path.of(System.getProperty("java.home"));

        this.targetProject = targetProject;

        this.targetProjectPathStr = targetProject.getLocation().toOSString();
        this.targetWorkspacePathStr = targetProject.getWorkspace().getRoot().getLocation().toOSString();

        this.targetModelRelativePathStr = targetModelRelativePathStr; 
        this.targetExperiment = targetExperiment;
        this.dataFiles = dataFiles;

        this.isOneFileExport = isOneFileExport;

        // expanding necessary plugins based on needed plugins (GamlProperties doesn't expand the dependency tree)
        neededGamaPluginsPath = BundleDependencyAnalyzer.getMinimalGamaPluginSet(neededGamaPlugins);

        System.out.println("Including the following plugins : ");
        neededGamaPlugins.forEach(System.out::println);

        neededGamaPlugins.remove("gama.ui.application");
        neededGamaPluginsPath.remove(gamaUiApplicationPluginPath);  
    }

    private Stream<Path> filter(Stream<Path> stream)
    { 
        return stream
        // filter the non essential GAMA plugins
            .filter(path -> {
                String filename = path.getFileName().toString();
                if((path.startsWith(GamaZipBuilder.pluginsPath) || path.startsWith(GamaZipBuilder.dropinsPath)) 
                    && (filename.startsWith("gama.") || filename.startsWith("gaml."))
                    && (filename.endsWith(".jar") || Files.isDirectory(path))
                    && ! neededGamaPluginsPath.contains(path)
                    // && ! neededGamaPlugins.stream().anyMatch(plugin -> filename.contains(plugin))
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

        final Path zipOutputPath = isOneFileExport ? GamaZipBuilder.tmpDirectoryPath.resolve("archive.zip") : outputPath;

        if(Files.isDirectory(GamaZipBuilder.tmpDirectoryPath))
            deleteDirectory(GamaZipBuilder.tmpDirectoryPath); 

        Files.createDirectories(GamaZipBuilder.tmpDirectoryPath);

        try(ZipArchiveOutputStream zos = new ZipArchiveOutputStream(new FileOutputStream(zipOutputPath.toFile()))){
        
            ///////////////////////////////////////////
            // Zipping GAMA in the desired directory //
            ///////////////////////////////////////////

            // Walk the appRootPath tree stream
            try (Stream<Path> stream = filter(Files.walk(appRootPath))) {
                stream.forEach(sourcePath -> {
                    try {
                        if(! Files.isDirectory(sourcePath))
                        {
                            ZipArchiveEntry entry = new ZipArchiveEntry(appRootPath.relativize(sourcePath).toString());
                            // Replace existing files/directories if needed
                            // Create a new entry inside the ZIP archive
                            if (SystemInfo.isLinux() || SystemInfo.isMac())
                                    ZipHelper.transfertFilePermissions(sourcePath,entry);
                            zos.putArchiveEntry(entry);
                            
                            // Write bytes to the entry
                            Files.copy(sourcePath, zos);
                            zos.closeArchiveEntry();
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

            Files.copy(GamaZipBuilder.pluginsPath.resolve(GamaZipBuilder.gamaUiApplicationPluginFileName),GamaZipBuilder.gamaUiApplicationJarTmpPath);
            ZipHelper.renameEntry(GamaZipBuilder.gamaUiApplicationJarTmpPath,"splash.png","old_splash.png");
            ZipHelper.renameEntry(GamaZipBuilder.gamaUiApplicationJarTmpPath,"splash_simulation_launcher.png","splash.png");

            zos.putArchiveEntry(new ZipArchiveEntry(Path.of("plugins",GamaZipBuilder.gamaUiApplicationPluginFileName).toString()));

            Files.copy(GamaZipBuilder.gamaUiApplicationJarTmpPath, zos);

            zos.closeArchiveEntry();

            /////////////////////////////////////
            // Applying the needed preferences //
            /////////////////////////////////////

            // switching to non global preferences
            Files.copy(appRootPath.resolve("Gama.ini"),GamaZipBuilder.gamaIniTmpPath);
            String gamaIniContent = Files.readString(GamaZipBuilder.gamaIniTmpPath); 

            // use embedded preferences
            if (gamaIniContent.contains("\n-Duse_global_preference_store="))
                gamaIniContent = gamaIniContent.replaceAll("\n-Duse_global_preference_store=.*","\n-Duse_global_preference_store=false");
            else
                gamaIniContent += "\n-Duse_global_preference_store=false\n";

            // trigger simulation only mode
            if (gamaIniContent.contains("\n-Dsimulation_only="))
                gamaIniContent = gamaIniContent.replaceAll("\n-Dsimulation_only=.*","\n-Dsimulation_only=true");
            else
                gamaIniContent += "\n-Dsimulation_only=true\n";
            
            // avoid saving corrupted ui states
            gamaIniContent = gamaIniContent.replace("-vmargs","-persistState\nfalse\n-vmargs");

            // register embedded jdk if needed
            if(this.zipWithJdk)
            {
                String javaBinaryPathStr = null;
                if(SystemInfo.isWindows())
                    javaBinaryPathStr = "./jdk/bin/javaw";
                if(SystemInfo.isLinux())
                    javaBinaryPathStr = "./jdk/bin/java";
                if(SystemInfo.isMac())
                    javaBinaryPathStr = "./jdk/Contents/Home/bin/java/";

                if(gamaIniContent.contains("-vm\n"))
                    gamaIniContent.replaceAll("^-vm\n.*\n","-vm\n" + javaBinaryPathStr + "\n");
                else
                    gamaIniContent = "-vm\n" + javaBinaryPathStr + "\n" + gamaIniContent;
            }

            Files.writeString(GamaZipBuilder.gamaIniTmpPath,gamaIniContent);

            ZipArchiveEntry gamaIniEntry = new ZipArchiveEntry(Path.of("Gama.ini").toString());
            // Replace existing files/directories if needed
            // Create a new entry inside the ZIP archive
            zos.putArchiveEntry(gamaIniEntry);
            
            // Write bytes to the entry
            Files.copy(GamaZipBuilder.gamaIniTmpPath, zos);
            zos.closeArchiveEntry();

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
            store.putInStore("pref_default_model",GamaZipBuilder.embeddedWorkspaceName + targetModelRelativePathStr);
            store.putInStore("pref_default_experiment",targetExperiment);
            store.putInStore("pref_errors_in_editor",false);
            
            store.saveToProperties(GamaZipBuilder.gamaPrefsTmpPath.toString());

            store.putInStore("pref_workspace_path",workspacePathPreferenceOld);
            store.putInStore("pref_workspace_remember",workspaceRememberPreferenceOld);
            store.putInStore("pref_startup_model",startupModelPreferenceOld);
            store.putInStore("pref_default_model",defaultModelPreferenceOld);
            store.putInStore("pref_default_experiment",defaultExperimentPreferenceOld);
            store.putInStore("pref_errors_in_editor",errorsInEditorPreferenceOld);

            ZipArchiveEntry gamaPrefsEntry = new ZipArchiveEntry(Path.of("configuration",".settings","gama.prefs").toString());
            zos.putArchiveEntry(gamaPrefsEntry);
            
            // Write bytes to the entry
            Files.copy(GamaZipBuilder.gamaPrefsTmpPath, zos);
            zos.closeArchiveEntry();

            ////////////////////////////////////
            // Embedding the target workspace //
            ////////////////////////////////////

            Path targetProjectPath = Path.of(targetProjectPathStr);
            // String targetWorkspacePathStr = targetProjectPath.getParent().toString();

            ////////////////////////////////////////////////////////////////////
            // Computing the data files that lie outside the exported project //
            // and must be rerouted into the project's "include" directory.   //
            // Their path is rewritten in every GAML file that references     //
            // them during the project walk below.                            //
            ////////////////////////////////////////////////////////////////////

            final Path includeDir = targetProjectPath.resolve("includes");
            final String projectName = targetProjectPath.getFileName().toString();
            final Map<Path, String> externalDataFiles = new LinkedHashMap<>();
            final Set<String> usedIncludeNames = new HashSet<>();

            if (dataFiles != null) {
                for (final String dataFile : dataFiles) {
                    if (dataFile == null || dataFile.isBlank()) {
                        continue;
                    }
                    final Path resolved = Path.of(dataFile).normalize();
                    // Already inside the exported project: it is embedded by the
                    // project walk below, no rerouting needed.
                    if (resolved.startsWith(targetProjectPath)) {
                        continue;
                    }
                    // Already scheduled for embedding (referenced by several models)
                    if (externalDataFiles.containsKey(resolved)) {
                        continue;
                    }
                    if (!Files.exists(resolved)) {
                        System.err.println("Export: data file not found, skipping: " + resolved);
                        continue;
                    }
                    final String fileName = resolved.getFileName().toString();
                    String uniqueName = fileName;
                    int counter = 1;
                    while (!usedIncludeNames.add(uniqueName)) {
                        final int dot = fileName.lastIndexOf('.');
                        if (dot > 0) {
                            uniqueName = fileName.substring(0, dot) + "_" + counter + fileName.substring(dot);
                        } else {
                            uniqueName = fileName + "_" + counter;
                        }
                        counter++;
                    }
                    externalDataFiles.put(resolved, uniqueName);

                    // .shp files may require additionnal files.
                    if(fileName.endsWith(".shp"))
                    {
                        String filePrefix = fileName.replace(".shp","");
                        String uniquePrefix = uniqueName.replace(".shp","");
                        try (Stream<Path> stream = Files.walk(resolved.getParent())) {
                            stream.forEach(filePath -> { 
                                final String currentFileName = filePath.getFileName().toString();
                                final int lastDotIndex = currentFileName.lastIndexOf('.');

                                if (lastDotIndex < 0)
                                    return;

                                final String currentFilePrefix = currentFileName.substring(0, lastDotIndex);
                                final String currentFileExtension = currentFileName.substring(lastDotIndex);

                                if (
                                    ! externalDataFiles.containsKey(filePath)
                                    && ! Files.isDirectory(filePath) 
                                    && currentFilePrefix.equals(filePrefix)
                                    && shapeFileExtensionsSet.contains(currentFileExtension)
                                )
                                    externalDataFiles.put(filePath, currentFileName.replace(filePrefix,uniquePrefix));
                            });
                            
                        } catch (IOException exception) 
                        {
                            exception.printStackTrace();
                        }
                    }
                }
            }

            externalDataFiles.keySet()
                .forEach(key -> 
                    System.out.println("Found external ressource : " 
                    + key + " mapped to includes/" + externalDataFiles.get(key)));

            try (Stream<Path> stream = Files.walk(targetProjectPath)) {
                stream.forEach(filePath -> {
                    
                    try 
                    {
                        if(! Files.isDirectory(filePath))
                        {
                            ZipArchiveEntry entry = new ZipArchiveEntry(filePath.toString().replace(targetWorkspacePathStr,GamaZipBuilder.embeddedWorkspaceName));

                            if (SystemInfo.isLinux() || SystemInfo.isMac())
                                    ZipHelper.transfertFilePermissions(filePath,entry);

                            zos.putArchiveEntry(entry);
                            
                            final String currentFileName = filePath.getFileName().toString();
                            final boolean isGaml = currentFileName.toLowerCase().endsWith(".gaml");

                            // Rewrite, in every GAML file, the paths of the data
                            // files that have been rerouted into the include dir.
                            if (isGaml && !externalDataFiles.isEmpty()) {
                                handleUpdateGamlImports(filePath,includeDir,externalDataFiles,zos);
                            } else {
                                Files.copy(filePath, zos);
                            }

                            zos.closeArchiveEntry();                        
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

            ////////////////////////////////////////////////////////////
            // Embedding the external data files into the project's   //
            // "include" directory so they travel with the export.    //
            ////////////////////////////////////////////////////////////

            for (final Map.Entry<Path, String> entry : externalDataFiles.entrySet()) {
                final String ZipArchiveEntryPath = Path.of(GamaZipBuilder.embeddedWorkspaceName, projectName, "includes",
                        entry.getValue()).toString();
                zos.putArchiveEntry(new ZipArchiveEntry(ZipArchiveEntryPath));
                Files.copy(entry.getKey(), zos);
                zos.closeArchiveEntry();
            }

            ////////////////////////////////////////
            // Resolving the project linked files //
            ////////////////////////////////////////

            final Map<String,Path> linkedFilesMap = ExportHelper.resolveLinks(targetProjectPath.resolve(".project"),targetProject);
            
            for (String virtualPathStr : linkedFilesMap.keySet())
            {
                // preserve the link virtual path /Embedded_Workspace/projectName/path/to/link
                zos.putArchiveEntry(
                    new ZipArchiveEntry(
                        GamaZipBuilder.embeddedWorkspaceName 
                        + File.separator + projectName 
                        + File.separator + virtualPathStr
                    ));

                // but write the actual content of the file designed by the link
                handleUpdateGamlImports(linkedFilesMap.get(virtualPathStr),includeDir,externalDataFiles,zos);
                // Files.copy(linkedFilesMap.get(virtualPathStr),zos);
                zos.closeArchiveEntry();
            }
            
            // WORKSPACE_IDENTIFIER
            zos.putArchiveEntry(
                new ZipArchiveEntry(GamaZipBuilder.embeddedWorkspaceName + File.separator + IWorkspaceManager.WORKSPACE_IDENTIFIER));

            zos.closeArchiveEntry();

            //WORKSPACE MODEL IDENTIFIER
            zos.putArchiveEntry(
                new ZipArchiveEntry(GamaZipBuilder.embeddedWorkspaceName + File.separator + GAMA.getWorkspaceManager().getModelIdentifier()));

            zos.closeArchiveEntry();

            /////////////////////////
            // Embedding the JDK   //
            /////////////////////////

            if (this.zipWithJdk)
            {
                // Walk the appRootPath tree stream
                try (Stream<Path> stream = Files.walk(jdkPath)) {
                    stream.forEach(sourcePath -> {
                        try {
                            if(! Files.isDirectory(sourcePath))
                            {
                                ZipArchiveEntry entry = new ZipArchiveEntry(GamaZipBuilder.embeddedJdkPath
                                    .resolve(jdkPath.relativize(sourcePath)).toString());
                                // Replace existing files/directories if needed
                                // Create a new entry inside the ZIP archive
                                if (SystemInfo.isLinux() || SystemInfo.isMac())
                                    ZipHelper.transfertFilePermissions(sourcePath,entry);
                                
                                zos.putArchiveEntry(entry);
                                
                                // Write bytes to the entry
                                Files.copy(sourcePath, zos);
                                zos.closeArchiveEntry();
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
            }

        }

        if(isOneFileExport) {
            if(SystemInfo.isLinux()) {
                Bundle bundle = FrameworkUtil.getBundle(this.getClass());

                if (bundle == null)
                    throw new IllegalStateException(
                        "Unable to access gama.export osgi bundle"
                    );

                URL runnerUrl = bundle.getEntry("binaries/runner");
                URL zipExtractorUrl = bundle.getEntry("binaries/zipextractor");

                if (runnerUrl == null)
                    throw new IOException(
                        "unable to find the elf64 runner : binaries/runner"
                    );

                if (zipExtractorUrl == null)
                    throw new IOException(
                        "unable to find the elf64 zip extractor : binaries/zipextractor"
                    );

                Path tmpRunnerPath = GamaZipBuilder.tmpDirectoryPath.resolve("runner");
                Path tmpZipExtractorPath = GamaZipBuilder.tmpDirectoryPath.resolve("zipextractor");

                try (InputStream inputStream = runnerUrl.openStream()) {
                    Files.copy(
                        inputStream,
                        tmpRunnerPath,
                        StandardCopyOption.REPLACE_EXISTING
                    );
                }

                try (InputStream inputStream = zipExtractorUrl.openStream()) {
                    Files.copy(
                        inputStream,
                        tmpZipExtractorPath,
                        StandardCopyOption.REPLACE_EXISTING
                    );
                }
                
                try {
                    ElfPacker.pack(tmpRunnerPath,tmpZipExtractorPath,zipOutputPath,outputPath);
                } catch (Exception e)
                {
                    System.out.println("error: unable to pack the elf64 executable : ");
                    e.printStackTrace();
                }
            }
        }

        // clean tmp directory
        deleteDirectory(GamaZipBuilder.tmpDirectoryPath);
    }
}