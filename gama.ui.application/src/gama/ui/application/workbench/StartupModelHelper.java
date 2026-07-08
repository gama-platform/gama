package gama.ui.application.workbench;
    
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.core.runtime.Platform;
import com.google.inject.Injector;

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

    private IModelSpecies model;

    private GamlModelBuilder builder = null;

    private String experiment;

    public StartupModelHelper() {}

    public void initialize()
    {
        experiment = GamaPreferences.Interface.CORE_DEFAULT_EXPERIMENT.getValue();

        if(experiment.contains(StartupModelHelper.SEPARATOR))
            pickExperiment(true);
    }

    public void pickExperiment(boolean startup)
    {
        experiment = GamaPreferences.Interface.CORE_DEFAULT_EXPERIMENT.getValue();

        final String [] experimentNames = experiment.split(StartupModelHelper.SEPARATOR);
        
        final PickExperimentDialog dialog = new PickExperimentDialog(experimentNames);

        final int result = dialog.open();

        if(result == IDialogConstants.OK_ID)
            experiment = dialog.getSelectedExperiment();
        else
        {
            if (startup)
            {
                GAMA.getGui().getDialogFactory().error("The GAMA simulation launcher has to start on an experiment.");
                GAMA.getGui().exit();
            }
        }

    }

    public void pickExperiment()
    {
        pickExperiment(false);
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

    public IModelSpecies getModel()
    {
        if(model == null)
        {
            IGamaFile<?, ?> file = GamaPreferences.Interface.CORE_DEFAULT_MODEL.getValue();
			String filePathStr = file.getPath(null);
			file = new GenericFile(ExportHelper.resolveEmbeddedPath(filePathStr));

            if (file != null && file.exists(null)) {

				while (GAMA.getRegularGui() == null) {
					THREADS.WAIT(100, Thread.currentThread().getName() + ": waiting for the GUI to become available");
				}

				final URI uri = file.getURIRelativeToWorkspace();
				final List<GamlCompilationError> errors = new ArrayList<GamlCompilationError>();

				model = getBuilder().compile(uri,errors);
			}
        }

        return model;
    }

    public void startSimulation()
    {
        GAMA.runGuiExperiment(experiment,getModel());
    }
}