package gama.export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.StandardOpenOption;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
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
import java.util.regex.Pattern;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.eclipse.core.resources.IProject;

import gama.api.utils.prefs.JREPreferenceStore;
import gama.api.utils.prefs.GamaPreferenceStore;
import gama.api.GAMA;
import gama.api.runtime.IWorkspaceManager;
import gama.api.runtime.SystemInfo;
import gama.export.dependency.BundleDependencyAnalyzer;
import gama.workspace.manager.WorkspacePaths;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import gama.export.ZipHelper;
import gama.export.ExportHelper;
import gama.export.ElfPacker;
import gama.export.ArchiveBuilder;

public class GamaZipBuilder {

    private LauncherConfiguration launcherConfiguration = LauncherConfiguration.defaults();

    public void setLauncherConfiguration(final LauncherConfiguration configuration) {
        launcherConfiguration = java.util.Objects.requireNonNull(configuration);
    }


    /**
	 * The set of plugins which must always be included in a Gama distribution.
     * gama.dependencies and gama.ui.application are necessary but they
     * are treated separately
	 */
    private static Set<String> necessaryGamaPlugins = new HashSet<String>(Set.of(
        "gama.dependencies",
        "gama.ui.application",
        "gama.ui.shared",
        "gama.ui.experiment",
        "gama.ui.export",
        "gama.extension.maths",
        "gama.extension.batch"
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

    private static final Path appRootPath = WorkspacePaths.applicationDirectory();

    private static final Path eclipsePath = WorkspacePaths.installationDirectory();

    private static final String embeddedWorkspacePathStr = appRootPath.relativize(eclipsePath.resolve(embeddedWorkspaceName)).toString();
    
    private static final Path pluginsPath = eclipsePath.resolve("plugins");

    private static final Path dropinsPath = eclipsePath.resolve("dropins");

    private static final Path gamaUiApplicationPluginPath = BundleDependencyAnalyzer.getPluginPath("gama.ui.application");
    
    private static final String gamaUiApplicationPluginFileName = gamaUiApplicationPluginPath.getFileName().toString();

    private static final Set<String> shapeFileExtensionsSet = new HashSet<String>(Set.of(".shx",".dbf",".prj",".sbn",".sbx",".xml"));

    private List<IProject> targetProjects = null;
    
    private String targetModelRelativePathStr = null;

    private Path targetWorkspacePath = null;
    
    private String targetExperiment = null;

    private boolean zipWithJdk = false;

    private boolean isOneFileExport = false;

    private static Path jdkPath = null; 

    private static Path embeddedJdkPath = Path.of("jdk");

    private IProgressMonitor monitor = new NullProgressMonitor();

    private void incrementProgress() {
        checkCanceled();
        monitor.worked(1);
    }

    private void checkCanceled() {
        if (monitor.isCanceled()) throw new OperationCanceledException();
    }

    /**
     * Data files referenced by the playable models, as absolute, normalized
     * paths. Those that live outside the exported project are rerouted into the
     * project's <code>include</code> directory and the path is rewritten in
     * every GAML file that references them.
     */
    private Map<IProject,Set<String>> dataFiles = null;

    private Set<Path> dontZipPaths = new HashSet<Path>(Set.of(
        Path.of(eclipsePath.toString(),"configuration","org.eclipse.equinox.app"),
        Path.of(eclipsePath.toString(),"configuration","org.eclipse.equinox.launcher"),
        Path.of(eclipsePath.toString(),"configuration","org.eclipse.osgi"),
        Path.of(eclipsePath.toString(),"configuration","org.eclipse.core.runtime"),
        Path.of(eclipsePath.toString(),"configuration","org.eclipse.e4.ui.css.swt.theme"),
        Path.of(eclipsePath.toString(),"configuration",".settings"),
        Path.of(eclipsePath.toString(),"jdk"),
        Path.of(eclipsePath.toString(),"Gama.ini")
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

    public void addEntryAndUpdateGamlImports(Path filePath, Path includeDir, Map<Path,String> externalDataFiles, String entryName, ArchiveBuilder archive) throws IOException, RuntimeException{

        String content = Files.readString(filePath, StandardCharsets.UTF_8);
        final Path archiveModelPath = Path.of(entryName.replace('\\', '/'));
        final Path archiveIncludeDir = Path.of(embeddedWorkspacePathStr,
                includeDir.getParent().getFileName().toString(), "includes");
        final Map<Path, Path> relocated = new LinkedHashMap<>();
        externalDataFiles.forEach((source, name) -> relocated.put(source, archiveIncludeDir.resolve(name)));
        archive.addEntryFromString(GamlExportPaths.rewrite(content, filePath, archiveModelPath, relocated), entryName);
    }

    public GamaZipBuilder(
        final Set<String> plugins,
        final List<IProject> targetProjects,
        final String targetModelRelativePathStr,
        final String targetExperiment,
        final Map<IProject,Set<String>> dataFiles,
        final boolean zipWithJdk,
        final boolean isOneFileExport)
    {
        neededGamaPlugins = plugins;
        neededGamaPlugins.addAll(necessaryGamaPlugins);
        this.zipWithJdk = zipWithJdk;
        
        if(zipWithJdk && GamaZipBuilder.jdkPath == null)
        {
            String javaHome = System.getProperty("java.home");

            if (SystemInfo.isWindows())
            {
                int index = 0;
                while(javaHome.charAt(index) == '\\' || javaHome.charAt(index) == '/')
                    index++;
                
                if (index >= 0)
                    GamaZipBuilder.jdkPath = Path.of(javaHome.substring(index));
            }
            else 
                GamaZipBuilder.jdkPath = Path.of(javaHome);

            if(SystemInfo.isMac())
            {
                GamaZipBuilder.jdkPath = GamaZipBuilder.jdkPath.getParent().getParent();
                GamaZipBuilder.embeddedJdkPath = Path.of("./Contents/MacOS").resolve(GamaZipBuilder.embeddedJdkPath);
            }

        }

        this.targetProjects = targetProjects;

        this.targetWorkspacePath = Path.of(
            targetProjects.get(0).getWorkspace().getRoot().getLocation().toOSString());

        this.targetModelRelativePathStr = targetModelRelativePathStr; 
        this.targetExperiment = targetExperiment;
        this.dataFiles = dataFiles;

        this.isOneFileExport = isOneFileExport;

        if(isOneFileExport && SystemInfo.isWindows())
            dontZipPaths.add(eclipsePath.resolve("Gamac.exe"));

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
                        dontZipPaths.add(path);

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
        zip(outputPathStr, new NullProgressMonitor());
    }

    public void zip(String outputPathStr, IProgressMonitor monitor) throws IOException
    {
        this.monitor = monitor;
        monitor.beginTask("Exporting the project", IProgressMonitor.UNKNOWN);
        try {
            buildArchive(outputPathStr);
        } finally {
            monitor.done();
        }
    }

    private void buildArchive(String outputPathStr) throws IOException {
        checkCanceled();
        final Path outputPath = Path.of(outputPathStr).toAbsolutePath().normalize();
        if (outputPath.startsWith(appRootPath.toAbsolutePath().normalize())
                || targetProjects.stream().map(project -> Path.of(project.getLocation().toOSString())
                        .toAbsolutePath().normalize()).anyMatch(outputPath::startsWith)) {
            throw new IOException("The export destination cannot be inside the installation or an exported project");
        }
        if (outputPath.getParent() == null) throw new IOException("The export destination must be a file path");
        Files.createDirectories(outputPath.getParent());
        final Path realOutputPath = outputPath.getParent().toRealPath().resolve(outputPath.getFileName());
        final Path normalizedAppRootPath = appRootPath.toRealPath();
        boolean destinationInProject = false;
        for (IProject project : targetProjects) {
            if (realOutputPath.startsWith(Path.of(project.getLocation().toOSString()).toRealPath())) {
                destinationInProject = true;
                break;
            }
        }
        if (realOutputPath.startsWith(normalizedAppRootPath) || destinationInProject) {
            throw new IOException("The export destination cannot be inside the installation or an exported project");
        }
        final Path tmpDirectoryPath = Files.createTempDirectory("gama.export.tmp-");
        Path stagingPath = null;
        boolean completed = false;
        try {
        stagingPath = Files.createTempFile(outputPath.getParent(), ".gama-export-", ".tmp");
        final Path outputStagingPath = stagingPath;
        final Path gamaIniTmpPath = tmpDirectoryPath.resolve("gama.ini.tmp");
        final Path gamaPrefsTmpPath = tmpDirectoryPath.resolve("gama.prefs.tmp");
        final Path gamaUiApplicationJarTmpPath = tmpDirectoryPath.resolve("gama.ui.application.jar.tmp");
        final Path zipOutputPath = isOneFileExport ? tmpDirectoryPath.resolve("archive.zip") : outputStagingPath;

        try(ArchiveBuilder archive = new ArchiveBuilder(zipOutputPath,isOneFileExport)){

            dontZipPaths.add(eclipsePath.resolve(LauncherConfiguration.FILE));
            dontZipPaths.add(eclipsePath.resolve(LauncherConfiguration.IMAGE));
        
            ///////////////////////////////////////////
            // Zipping GAMA in the desired directory //
            ///////////////////////////////////////////

            // Walk the appRootPath tree stream
            try (Stream<Path> stream = filter(Files.walk(appRootPath))
                    .filter(path -> !path.toAbsolutePath().normalize().startsWith(tmpDirectoryPath))) {
                stream.forEach(sourcePath -> {
                    try {
                        archive.addEntry(sourcePath,appRootPath.relativize(sourcePath).toString());

                        incrementProgress();

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

            Files.copy(GamaZipBuilder.pluginsPath.resolve(GamaZipBuilder.gamaUiApplicationPluginFileName),gamaUiApplicationJarTmpPath);
            final Bundle uiBundle = org.eclipse.core.runtime.Platform.getBundle("gama.ui.export");
            if (uiBundle == null) throw new IOException("The playable model UI bundle is not installed");
            final URL splash = uiBundle.getEntry("splash_simulation_launcher.png");
            if (splash == null) throw new IOException("The playable model splash is missing");
            try (var jar = FileSystems.newFileSystem(gamaUiApplicationJarTmpPath);
                    var input = splash.openStream()) {
                Files.move(jar.getPath("/splash.png"), jar.getPath("/old_splash.png"),
                        StandardCopyOption.REPLACE_EXISTING);
                Files.copy(input, jar.getPath("/splash.png"), StandardCopyOption.REPLACE_EXISTING);
            }

            archive.addEntry(
                gamaUiApplicationJarTmpPath,
                appRootPath.relativize(
                    pluginsPath.resolve(GamaZipBuilder.gamaUiApplicationPluginFileName)
                ).toString()
            );

            /////////////////////////////////////
            // Applying the needed preferences //
            /////////////////////////////////////

            // switching to non global preferences
            Files.copy(eclipsePath.resolve("Gama.ini"),gamaIniTmpPath);
            String gamaIniContent = Files.readString(gamaIniTmpPath);
            // Select the launcher application without changing the normal product.
            gamaIniContent = gamaIniContent.replaceAll("(?m)^-application\\r?\\n[^\\r\\n]*(?:\\r?\\n|$)", "");
            gamaIniContent = "-application\ngama.ui.export.SimulationApplication\n" + gamaIniContent;

            // use embedded preferences
            if (gamaIniContent.contains("\n-Duse_global_preference_store="))
                gamaIniContent = gamaIniContent.replaceAll("\n-Duse_global_preference_store=.*","\n-Duse_global_preference_store=false");
            else
                gamaIniContent += "\n-Duse_global_preference_store=false\n";

            // trigger exported runner mode
            if (gamaIniContent.contains("\n-Dexported_runner="))
                gamaIniContent = gamaIniContent.replaceAll("\n-Dexported_runner=.*","\n-Dexported_runner=true");
            else
                gamaIniContent += "\n-Dexported_runner=true\n";
            
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
                    javaBinaryPathStr = "./jdk/Contents/Home/bin/java";

                if(Pattern.compile("(?m)^-vm\\r?\\n").matcher(gamaIniContent).find())
                    gamaIniContent = gamaIniContent.replaceAll("(?m)^-vm\\r?\\n[^\\r\\n]*(?:\\r?\\n|$)",
                            "-vm\n" + javaBinaryPathStr + "\n");
                else
                    gamaIniContent = "-vm\n" + javaBinaryPathStr + "\n" + gamaIniContent;
            }

            Files.writeString(gamaIniTmpPath,gamaIniContent);

            // Replace existing files/directories if needed
            // Create a new entry inside the ZIP archive
            archive.addEntry(gamaIniTmpPath,appRootPath.relativize(eclipsePath.resolve("Gama.ini")).toString());

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
            store.putInStore("pref_default_model",targetModelRelativePathStr);
            store.putInStore("pref_default_experiment",targetExperiment);
            store.putInStore("pref_errors_in_editor",false);
            
            store.saveToProperties(gamaPrefsTmpPath.toString());

            store.putInStore("pref_workspace_path",workspacePathPreferenceOld);
            store.putInStore("pref_workspace_remember",workspaceRememberPreferenceOld);
            store.putInStore("pref_startup_model",startupModelPreferenceOld);
            store.putInStore("pref_default_model",defaultModelPreferenceOld);
            store.putInStore("pref_default_experiment",defaultExperimentPreferenceOld);
            store.putInStore("pref_errors_in_editor",errorsInEditorPreferenceOld);

            archive.addEntry(
                gamaPrefsTmpPath,
                appRootPath.relativize(
                    eclipsePath
                        .resolve("configuration")
                        .resolve(".settings")
                        .resolve("gama.prefs")
                ).toString()
            );

            ////////////////////////////////////
            // Embedding the target projects  //
            ////////////////////////////////////

            archive.addEntryFromString(launcherConfiguration.serialize(),
                    appRootPath.relativize(eclipsePath.resolve(LauncherConfiguration.FILE)).toString());
            if (!launcherConfiguration.image().isBlank()) {
                archive.addEntry(Path.of(launcherConfiguration.image()),
                        appRootPath.relativize(eclipsePath.resolve(LauncherConfiguration.IMAGE)).toString());
            }

            ////////////////////////////////////////////////////////////////////
            // Computing the data files that lie outside the exported project //
            // and must be rerouted into the project's "include" directory.   //
            // Their path is rewritten in every GAML file that references     //
            // them during the project walk below.                            //
            ////////////////////////////////////////////////////////////////////
            for (final IProject project : this.targetProjects) {
                checkCanceled();
                Path projectPath = Path.of(project.getLocation().toOSString());
                final Path includeDir = projectPath.resolve("includes");
                final String projectName = projectPath.getFileName().toString();
                final Map<Path, String> externalDataFiles = new LinkedHashMap<>();
                final Set<String> usedIncludeNames = new HashSet<>();
                if (Files.isDirectory(includeDir)) {
                    try (Stream<Path> stream = Files.list(includeDir)) {
                        stream.map(path -> path.getFileName().toString()).forEach(usedIncludeNames::add);
                    }
                }

                if (! dataFiles.isEmpty() && dataFiles.keySet().contains(project)) {
                    for (final String dataFile : dataFiles.get(project)) {

                        if (dataFile == null || dataFile.isBlank()) {
                            continue;
                        }
                        final Path resolved = Path.of(dataFile).normalize();
                        // Already inside the exported project: it is embedded by the
                        // project walk below, no rerouting needed.
                        if (resolved.startsWith(projectPath)) {
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
                        final List<Path> shapeCompanions = new ArrayList<>();
                        if (fileName.endsWith(".shp")) {
                            final String filePrefix = fileName.replace(".shp", "");
                            try (Stream<Path> stream = Files.walk(resolved.getParent())) {
                                stream.forEach(filePath -> {
                                    checkCanceled();
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
                                        shapeCompanions.add(filePath);
                                });
                            } catch (IOException exception) {
                                exception.printStackTrace();
                            }
                        }

                        String uniqueName = fileName;
                        int counter = 1;
                        while (true) {
                            final String uniquePrefix = uniqueName.replace(".shp", "");
                            final Set<String> candidateNames = new HashSet<>();
                            boolean hasCollision = !candidateNames.add(uniqueName)
                                    || usedIncludeNames.contains(uniqueName);
                            for (final Path companion : shapeCompanions) {
                                final String companionName = companion.getFileName().toString()
                                        .replace(fileName.replace(".shp", ""), uniquePrefix);
                                if (!candidateNames.add(companionName) || usedIncludeNames.contains(companionName)) {
                                    hasCollision = true;
                                }
                            }
                            if (!hasCollision) {
                                usedIncludeNames.addAll(candidateNames);
                                break;
                            }
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
                        if (fileName.endsWith(".shp")) {
                            final String uniquePrefix = uniqueName.replace(".shp", "");
                            for (final Path companion : shapeCompanions) {
                                final String companionName = companion.getFileName().toString()
                                        .replace(fileName.replace(".shp", ""), uniquePrefix);
                                externalDataFiles.put(companion, companionName);
                            }
                        }
                    }
                }

                externalDataFiles.keySet()
                    .forEach(key -> 
                        System.out.println("Found external ressource : " 
                        + key + " mapped to includes/" + externalDataFiles.get(key)));

                //////////////////////////////////
                // Actual embedding of projects //
                //////////////////////////////////

                try (Stream<Path> stream = Files.walk(projectPath)
                        .filter(path -> !path.toAbsolutePath().normalize().startsWith(tmpDirectoryPath))) {
                    stream.forEach(filePath -> {
                        
                        try 
                        {
                            if(! Files.isDirectory(filePath) 
                               && ! filePath.equals(projectPath.resolve(".project"))
                            )
                            {
                                final String currentFileName = filePath.getFileName().toString();
                                final String lowerFileName = currentFileName.toLowerCase();
                                final boolean isGaml = lowerFileName.endsWith(".gaml")
                                        || lowerFileName.endsWith(".experiment");
                                String entryName;

                                entryName = filePath.toString().replace(
                                    projectPath.getParent().toString(),
                                    GamaZipBuilder.embeddedWorkspacePathStr);

                                // Rewrite, in every GAML file, the paths of the data
                                // files that have been rerouted into the include dir.
                                if (isGaml && !externalDataFiles.isEmpty())
                                    addEntryAndUpdateGamlImports(filePath,includeDir,externalDataFiles,entryName,archive);
                                else
                                    archive.addEntry(filePath,entryName);
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
                    final String zipArchiveEntryPathStr = Path.of(GamaZipBuilder.embeddedWorkspacePathStr, projectName, "includes",
                            entry.getValue()).toString();

                    archive.addEntry(entry.getKey(),zipArchiveEntryPathStr);
                }

                ////////////////////////////////////////
                // Resolving the project linked files //
                ////////////////////////////////////////

                final Map<String,Path> linkedFilesMap = ExportHelper.resolveLinks(projectPath.resolve(".project"),project);
                
                for (String virtualPathStr : linkedFilesMap.keySet())
                {
                    // preserve the link virtual path /Embedded_Workspace/projectName/path/to/link
                    Path entryPath = Path.of(GamaZipBuilder.embeddedWorkspacePathStr, projectName, virtualPathStr);
                    Path linkedPath = linkedFilesMap.get(virtualPathStr);
                    if (Files.isDirectory(linkedPath)) {
                        try (Stream<Path> linkedFiles = Files.walk(linkedPath)) {
                            for (Path linkedFile : (Iterable<Path>) linkedFiles::iterator) {
                                if (Files.isRegularFile(linkedFile)) {
                                    Path linkedEntry = entryPath.resolve(linkedPath.relativize(linkedFile));
                                    addLinkedFile(linkedFile, includeDir, externalDataFiles,
                                            linkedEntry.toString(), archive);
                                }
                            }
                        }
                    } else if (Files.isRegularFile(linkedPath)) {
                        addLinkedFile(linkedPath, includeDir, externalDataFiles, entryPath.toString(), archive);
                    }
                }

                ///////////////////////////////////////////////////////
                // adding the corrected .project file to the archive //
                ///////////////////////////////////////////////////////

                String projectMetadataFileContent = Files.readString(projectPath.resolve(".project"), StandardCharsets.UTF_8);
                String projectMetadataFileEntryName;

                projectMetadataFileEntryName = embeddedWorkspacePathStr 
                            + File.separator 
                            + projectName 
                            + File.separator 
                            + ".project";
                                
                archive.addEntryFromString(
                    projectMetadataFileContent.replaceAll("(?s)<linkedResources>.*</linkedResources>",""),
                    projectMetadataFileEntryName
                );
            }
            
            // WORKSPACE_IDENTIFIER
            archive.addEntryFromString("",GamaZipBuilder.embeddedWorkspacePathStr + File.separator + IWorkspaceManager.WORKSPACE_IDENTIFIER);

            //WORKSPACE MODEL IDENTIFIER
            archive.addEntryFromString("",GamaZipBuilder.embeddedWorkspacePathStr + File.separator + GAMA.getWorkspaceManager().getModelIdentifier());

            /////////////////////////
            // Embedding the JDK   //
            /////////////////////////
       
            if (this.zipWithJdk)
            {
                monitor.subTask("Exporting the JDK");
                // walk the jdk files tree stream
                try (Stream<Path> stream = Files.walk(jdkPath)) {
                    stream.forEach(sourcePath -> {
                        try {
                            if(! Files.isDirectory(sourcePath))
                            {
                                archive.addEntry(sourcePath,GamaZipBuilder.embeddedJdkPath
                                    .resolve(jdkPath.relativize(sourcePath)).toString()
                                );
                                incrementProgress();
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

            if(SystemInfo.isMac())
                archive.addEntryFromString(
                    """
                    MacOS Gatekeeper may prevent you from executing this application.\n
                    To bypass it, open a terminal and type 'xattr -cr ' then enter this 
                    application's path or drag and drop it's icon in the terminal. Press 
                    enter and start the application.
                    """,
                    "./troubleshoot.txt");
        }


        if(isOneFileExport) {

            checkCanceled();
            monitor.subTask("Packing the final executable");

            Bundle bundle = null;

            if(SystemInfo.isLinux() || SystemInfo.isWindows()) {
                bundle = FrameworkUtil.getBundle(this.getClass());
                if (bundle == null)
                    throw new IllegalStateException(
                        "Unable to access gama.export osgi bundle"
                    );
            }
            
            if(SystemInfo.isLinux()) {
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

                Path tmpRunnerPath = tmpDirectoryPath.resolve("runner");
                Path tmpZipExtractorPath = tmpDirectoryPath.resolve("zipextractor");

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
                    ElfPacker.pack(tmpRunnerPath,tmpZipExtractorPath,zipOutputPath,outputStagingPath);
                } catch (Exception e) {
                    throw new IOException("Unable to pack the Linux executable", e);
                }
            }

            if(SystemInfo.isWindows()) {
                URL sevenZStubUrl = bundle.getEntry("binaries/7zS2.sfx");
                
                if (sevenZStubUrl == null)
                    throw new IOException(
                        "unable to find the 7zS2.sfx stub for building Windows x64 PE file : binaries/7zS2.sfx"
                    );

                Path tmpSevenZStubPath = tmpDirectoryPath.resolve("7zS2.sfx");

                try (InputStream inputStream = sevenZStubUrl.openStream()) {
                    Files.copy(
                        inputStream,
                        tmpSevenZStubPath,
                        StandardCopyOption.REPLACE_EXISTING
                    );
                }

                try (OutputStream out = Files.newOutputStream(
                        outputStagingPath,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.WRITE
                )) {
                        Files.copy(tmpSevenZStubPath, out);
                        Files.copy(zipOutputPath, out);
                }
            }

        }
        checkCanceled();
        Files.move(outputStagingPath, outputPath, StandardCopyOption.REPLACE_EXISTING);
        completed = true;
        } finally {
            deleteDirectory(tmpDirectoryPath);
            if (!completed && stagingPath != null) {
                try {
                    Files.deleteIfExists(stagingPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void addLinkedFile(Path filePath, Path includeDir, Map<Path, String> externalDataFiles,
            String entryName, ArchiveBuilder archive) throws IOException {
        checkCanceled();
        String fileName = filePath.getFileName().toString().toLowerCase();
        if (fileName.endsWith(".gaml") || fileName.endsWith(".experiment")) {
            addEntryAndUpdateGamlImports(filePath, includeDir, externalDataFiles, entryName, archive);
        } else {
            archive.addEntry(filePath, entryName);
        }
    }
}