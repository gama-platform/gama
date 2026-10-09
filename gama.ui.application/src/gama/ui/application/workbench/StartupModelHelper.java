package gama.ui.application.workbench;
    
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import java.util.HashSet;
import java.nio.file.StandardCopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.File;
import java.io.IOException;
import java.lang.Iterable;
import java.time.Instant;

import org.eclipse.emf.common.util.URI;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.core.runtime.Platform;
import com.google.inject.Injector;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;

import gama.api.GAMA;
import gama.api.compilation.GamlCompilationError;
import gaml.compiler.validation.GamlModelBuilder;
import gaml.compiler.GamlStandaloneSetup;
import gama.api.kernel.species.IModelSpecies;
import gama.dev.THREADS;
import org.eclipse.emf.common.util.URI;
import gama.api.utils.prefs.GamaPreferences;
import gama.ui.application.workbench.PickExperimentDialog;
import gama.api.types.file.IGamaFile;
import gama.api.types.file.GenericFile;
import gama.export.ExportHelper;



public class StartupModelHelper
{
    private static StartupModelHelper instance = null;

    private static String SEPARATOR = "#";

	private final static String CONTEXTUAL_SEPARATOR = "    from model    ";

    private IModelSpecies model;

    private Path projectPath;

    private GamlModelBuilder builder = null;

    private String specificModelPathStr = null;

    private String experiment;

    private long lastSimulationArtifactsUpdateMs;

    public StartupModelHelper() {
        lastSimulationArtifactsUpdateMs = Instant.now().toEpochMilli();
    }

    public Object initialize()
    {
        //////////////////////////////////////////////////
        // Resolving projects in the embedded workspace //
        //////////////////////////////////////////////////

        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkspaceRoot root = workspace.getRoot();

        try (Stream<Path> stream = 
            Files.walk(Path.of(ExportHelper.toAbsoluteFromEmbeddedWorkspacePath("")),1))
        {
            stream.forEach(filePath -> {
                try {
                    Path projectMetadataFilePath = filePath.resolve(".project");

                    if(! Files.exists(projectMetadataFilePath) || Files.isDirectory(projectMetadataFilePath))
                        return;
                    
                    IPath projectFilePath = 
                        new org.eclipse.core.runtime.Path(projectMetadataFilePath.toString());

                    IProjectDescription description = workspace.loadProjectDescription(projectFilePath);

                    IProject project = root.getProject(description.getName());

                    if (!project.exists()) {
                        project.create(description, null);
                    }
                    if (!project.isOpen()) {
                        project.open(null);
                    }

                } catch (CoreException ce) {
                    ce.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        experiment = GamaPreferences.Interface.CORE_DEFAULT_EXPERIMENT.getValue();
        boolean experimentHasBeenPicked = true;

        if(experiment.contains(SEPARATOR) || experiment.contains("@"))
            experimentHasBeenPicked = pickExperiment();

        if (!experimentHasBeenPicked)
        {
            GAMA.getGui().getDialogFactory().error("The GAMA simulation launcher has to start on an experiment.");
            return IApplication.EXIT_OK;
        }

        return null;
    }

    public boolean pickExperiment()
    {
        experiment = GamaPreferences.Interface.CORE_DEFAULT_EXPERIMENT.getValue();
    
        final String [] experimentArray = experiment.split(StartupModelHelper.SEPARATOR);
        String[] experimentNames;

        if (experimentArray.length > 0 && experimentArray[0].contains("@"))
            experimentNames = Arrays.stream(experimentArray).map(experimentName -> {
                int lastIndex = experimentName.lastIndexOf("@");
                
                if (lastIndex == -1)
                    return experimentName;

                return experimentName.substring(0,lastIndex) + CONTEXTUAL_SEPARATOR + experimentName.substring(lastIndex + 1);
            }).toArray(String[]::new);
        else
            experimentNames = experimentArray;
            

        final PickExperimentDialog dialog = new PickExperimentDialog(experimentNames);

        final int result = dialog.open();

        if(result == IDialogConstants.OK_ID)
        {
            experiment = dialog.getSelectedExperiment();

            int lastIndex = experiment.lastIndexOf(CONTEXTUAL_SEPARATOR);

            if (lastIndex != -1)
            {
                String newSpecificModelPathStr = experiment.substring(lastIndex + CONTEXTUAL_SEPARATOR.length());
                experiment = experiment.substring(0,lastIndex);
                if (specificModelPathStr == null || ! specificModelPathStr.equals(newSpecificModelPathStr))
                {
                    specificModelPathStr = newSpecificModelPathStr;
                    model = null;
                }
            }
            return true;
        }
        return false;
    }

    public static StartupModelHelper getInstance()
    {
        if (instance == null)
            instance = new StartupModelHelper();
            
        return instance;
    }

    public String getExperiment()
    {
        return experiment;
    }

    public GamlModelBuilder getBuilder()
    {
        if(builder == null)
        {
			// final boolean isEditorLoaded =  
			// 	Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().containsKey("gaml");
			final boolean isEditorLoaded = Platform.getBundle("gama.ui.editor") != null;

			// create a new GamlModelBuilder only if gama.ui.editor is not loaded
			if(isEditorLoaded) {
				builder = GamlModelBuilder.getInstance();
			}
			else {
				Injector injector = GamlStandaloneSetup.doSetup();
				builder = new GamlModelBuilder(injector);
			}
        }

        return builder;
    }

    public boolean saveSimulationArtifacts(Path targetSavePath) throws IOException {

        Stream<Path> stream = Files.walk(getProjectPath());
        boolean somethingHasBeenSaved = false;
                
        for (Path path : (Iterable<Path>) stream::iterator) {

                if(path.toFile().lastModified() <= lastSimulationArtifactsUpdateMs)
                    continue;
                
                Path targetPath = targetSavePath.resolve(getProjectPath().relativize(path));

                if (targetPath.getParent() != null)
                       Files.createDirectories(targetPath.getParent());

                if(! Files.isDirectory(targetPath))
                {
                    Files.copy(path,targetPath,StandardCopyOption.REPLACE_EXISTING);
                    somethingHasBeenSaved = true;
                }
        }
        
        lastSimulationArtifactsUpdateMs = Instant.now().toEpochMilli();
        return somethingHasBeenSaved;   
    }

    public boolean areThereAnyArtifactsToSave() throws IOException {
        Stream<Path> stream = Files.walk(getProjectPath());

        for (Path path : (Iterable<Path>) stream::iterator) {

                if(path.toFile().lastModified() > lastSimulationArtifactsUpdateMs)
                    return true;
        }
        
        return false;   
    }

    public IModelSpecies getModel()
    {
        if(model == null)
        {
            String filePathStr;
            
            if (specificModelPathStr == null) {
                filePathStr = GamaPreferences.Interface.CORE_DEFAULT_MODEL.getValue()
                        .getPath(null);
            } else {
                filePathStr = specificModelPathStr;
            }

            
            if(filePathStr.startsWith("/"))
                filePathStr = filePathStr.substring(1);


				while (GAMA.getRegularGui() == null) {
					THREADS.WAIT(100, Thread.currentThread().getName() + ": waiting for the GUI to become available");
				}

				// final URI uri = file.getURIRelativeToWorkspace();
				final List<GamlCompilationError> errors = new ArrayList<GamlCompilationError>();

				model = getBuilder().compile(
                    URI.createPlatformResourceURI(
						filePathStr,true),
                        errors
                );
        }

        return model;
    }

    public Path getProjectPath()
    {
        if(projectPath == null)
		    projectPath = Path.of(getModel().getProjectPath());
        
        return projectPath;
    }

    public void startSimulation()
    {
        GAMA.runGuiExperiment(experiment,getModel());
    }
}