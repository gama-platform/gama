package gama.export;
    
import java.util.stream.StreamSupport;
import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.Bundle;
import com.google.inject.Injector;
import org.eclipse.jface.dialogs.IDialogConstants;

import gama.api.GAMA;
import gama.api.kernel.species.IModelSpecies;
import gama.api.compilation.GamlCompilationError;
import gama.api.additions.GamaBundleLoader;
import gaml.compiler.GamlStandaloneSetup;
import gaml.compiler.validation.GamlModelBuilder;
import gama.dev.THREADS;
import gama.export.ui.SelectExperimentDialog;
import gama.export.ExportHelper;

public class StartupModelManager()
{
    private static StartupModelManager instance = null;

    private String experiment;

    private GamlModelBuilder builder = null;

    private IModelSpecies model = null; 

    public StartupModelManager()
    {
        experiment = GamaPreferences.Interface.CORE_DEFAULT_EXPERIMENT.getValue();

        final boolean isEditorLoaded = Platform.getBundle("gama.ui.editor") != null;

        // create a new GamlModelBuilder only if gama.ui.editor is not loaded
        if(isEditorLoaded) {
            builder = GamlModelBuilder.getInstance();
        }
        else {
            Injector injector = GamlStandaloneSetup.doSetup();
            builder = new GamlModelBuilder(injector);
        }

        // opening the selected file
        IGamaFile<?, ?> file = GamaPreferences.Interface.CORE_DEFAULT_MODEL.getValue();
        String filePathStr = file.getPath(null);
        file = new GenericFile(ExportHelper.resolveEmbeddedPath(filePathStr));

        if (file != null && file.exists(null)) {

            while (GAMA.getRegularGui() == null) {
                THREADS.WAIT(100, Thread.currentThread().getName() + ": waiting for the GUI to become available");
            }

            final URI uri = file.getURIRelativeToWorkspace();
            final List<GamlCompilationError> errors = new ArrayList<GamlCompilationError>();

            model = builder.compile(uri,errors);

            if(experiment == "")
            {
                final String[] experimentNames = StreamSupport.stream(model.getExperiments().spliterator(),false)
                	.map(exp -> exp.getDescription().getName())
                	.toArray(String[]::new);
                
                final SelectExperimentDialog dialog = new SelectExperimentDialog(experimentNames);

                final int result = dialog.open();

                if (result != IDialogConstants.OK_ID || dialog.getSelectedExperiment() == SelectExperimentDialog.DEFAULT_EXPERIMENT)
                {
                	GAMA.getGui().getDialogFactory().error("The GAMA simulation launcher has to start on an experiment.");
                	GAMA.getGui().exit();
                }

                experiment = dialog.getSelectedExperiment();                
            }
        }
    }

    public StartupModelManager getInstance()
    {
        if (instance == null)
            instance = new StartupModelManager();
            
        return instance;
    }

    public GamlModelBuilder getBuilder()
    {
        return builder;
    }

    public IModelSpecies getModel()
    {
        return model;
    }

    public String getExperiment()
    {
        return experiment;
    }

    public void startSimulation()
    {
        GAMA.runGuiExperiment(experiment,model);
    }
}