package gama.ui.application.workbench;
    
import java.util.stream.StreamSupport;

import org.eclipse.jface.dialogs.IDialogConstants;

import gama.api.GAMA;
import gama.api.compilation.GamlCompilationError;
import gama.api.utils.prefs.GamaPreferences;
import gama.ui.application.workbench.PickExperimentDialog;

public class StartupModelHelper
{
    private static StartupModelHelper instance = null;

    private static String SEPARATOR = "#";

    private String experiment;

    public StartupModelHelper() {}

    public void initialize()
    {
        experiment = GamaPreferences.Interface.CORE_DEFAULT_EXPERIMENT.getValue();

        if(experiment.contains(StartupModelHelper.SEPARATOR))
        {
            final String [] experimentNames = experiment.split(StartupModelHelper.SEPARATOR);
            
            final PickExperimentDialog dialog = new PickExperimentDialog(experimentNames);

            final int result = dialog.open();

            if (result != IDialogConstants.OK_ID || dialog.getSelectedExperiment() == PickExperimentDialog.DEFAULT_EXPERIMENT)
            {
                GAMA.getGui().getDialogFactory().error("The GAMA simulation launcher has to start on an experiment.");
                GAMA.getGui().exit();
            }

            experiment = dialog.getSelectedExperiment();                
        }
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
}