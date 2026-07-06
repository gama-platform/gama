package gama.export.ui;

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

public class ExportModelDialog extends TitleAreaDialog {

    private Text txtOutputPath;
    private Button btnMultiExperimentExport;
    private Combo experimentsCombo;

    private String outputPath = "";
    private String selectedExperiment = DEFAULT_EXPERIMENT;
    private boolean multiExperimentExport = false;
    private String[] availableExperiments;

    public static final String DEFAULT_EXPERIMENT = "";


    public ExportModelDialog(String[] experimentNames) {
        super(Display.getDefault().getActiveShell());
        availableExperiments = experimentNames;
        setHelpAvailable(false);
    }

    @Override
    public void create() {
        super.create();
        setTitle("Model Export Configuration");
        setMessage("Please, configure simulation export options.", IMessageProvider.INFORMATION);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(area, SWT.NONE);
        
        GridData gdContainer = new GridData(SWT.FILL, SWT.FILL, true, true);
        container.setLayoutData(gdContainer);
        
        GridLayout layout = new GridLayout(3, false);
        layout.marginWidth = 15;
        layout.marginHeight = 15;
        container.setLayout(layout);

        createOptionSection(container);
        createDropdownSection(container);
        createPathSection(container);

        return area;
    }

    private void createOptionSection(Composite container) {
        CLabel lblOptions = new CLabel(container, SWT.NONE);
        lblOptions.setText("Options :");
        lblOptions.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));

        btnMultiExperimentExport = new Button(container, SWT.CHECK);
        btnMultiExperimentExport.setText("Multi-experiment launcher");
        btnMultiExperimentExport.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));

        btnMultiExperimentExport.addListener(SWT.Selection, event -> {
            // updates the state of the dropdown depending on the "multi-experiment" checkbox
            boolean isChecked = btnMultiExperimentExport.getSelection();
            experimentsCombo.setEnabled(!isChecked);
        });
    }

        private void createDropdownSection(Composite container) {
        CLabel lblCombo = new CLabel(container, SWT.NONE);
        lblCombo.setText("Exported experiment :");
        lblCombo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));

        experimentsCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
        GridData gdCombo = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        experimentsCombo.setLayoutData(gdCombo);

        experimentsCombo.setItems(availableExperiments);
        experimentsCombo.select(0); 

        // Règle l'état de départ : désactivé si btnOption1 n'est pas coché par défaut
        experimentsCombo.setEnabled(!btnMultiExperimentExport.getSelection());
    }


    private void createPathSection(Composite container) {
        CLabel lblPath = new CLabel(container, SWT.NONE);
        lblPath.setText("Export path :");
        lblPath.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));

        txtOutputPath = new Text(container, SWT.BORDER);
        GridData gdText = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        txtOutputPath.setLayoutData(gdText);

        Button btnBrowse = new Button(container, SWT.PUSH);
        btnBrowse.setText("Browse");
        btnBrowse.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        btnBrowse.addListener(SWT.Selection, event -> {
            DirectoryDialog dialog = new DirectoryDialog(getShell());
            dialog.setText("Select the folder where the exported simulation launcher will be saved");
            dialog.setMessage("Select a target path");
            String selectedDirectory = dialog.open();
            if (selectedDirectory != null) {
                txtOutputPath.setText(selectedDirectory);
            }
        });
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    @Override
    protected void okPressed() {
        outputPath = txtOutputPath.getText().trim();
        multiExperimentExport = btnMultiExperimentExport.getSelection();
        selectedExperiment = experimentsCombo.getText();

        if (outputPath.isEmpty()) {
            setMessage("Error : The destination path cannot be empty.", IMessageProvider.ERROR);
            return;
        }

        super.okPressed();
    }

    public String getOutputPath() {
        return outputPath;
    }

    public boolean isMultiExperimentExportSelected() {
        return multiExperimentExport;
    }

    public String getSelectedExperiment() {
        return multiExperimentExport ? ExportModelDialog.DEFAULT_EXPERIMENT : selectedExperiment;
    }
}