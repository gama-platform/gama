package gama.ui.application.workbench;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.graphics.Color;

public class PickExperimentDialog extends TitleAreaDialog {

    final public static String DEFAULT_EXPERIMENT = "";

    private Combo experimentsCombo;

    private String selectedExperiment = DEFAULT_EXPERIMENT;
    private String[] availableExperiments;


    public PickExperimentDialog(String[] experimentNames) {
        super(Display.getDefault().getActiveShell());
        availableExperiments = experimentNames;
        setHelpAvailable(false);
    }

    @Override
    public void create() {
        super.create();
        setTitle("Select an experiment to launch");
        // setMessage("", IMessageProvider.INFORMATION);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(area, SWT.NONE);
            
        Color systemBackgroundColor = 
            Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
        
        // 2. L'appliquer de force sur le conteneur principal
        container.setBackground(systemBackgroundColor);
        
        // Si vous utilisez des CLabel ou des composites imbriqués, 
        // forcez-les aussi à hériter du fond :
        container.setBackgroundMode(SWT.INHERIT_FORCE);

        GridData gdContainer = new GridData(SWT.FILL, SWT.FILL, true, true);
        container.setLayoutData(gdContainer);
        
        GridLayout layout = new GridLayout(3, false);
        layout.marginWidth = 15;
        layout.marginHeight = 15;
        container.setLayout(layout);

        createDropdownSection(container);

        return area;
    }

        private void createDropdownSection(Composite container) {
        CLabel lblCombo = new CLabel(container, SWT.NONE);
        lblCombo.setText("Available experiments :");
        lblCombo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));

        experimentsCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
        GridData gdCombo = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        experimentsCombo.setLayoutData(gdCombo);

        experimentsCombo.setItems(availableExperiments);
        experimentsCombo.select(0); 

    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    @Override
    protected void okPressed() {
        selectedExperiment = experimentsCombo.getText();
        super.okPressed();
    }

    public String getSelectedExperiment() {
        return selectedExperiment;
    }
}