package gama.export.ui;

import java.util.List;
import java.util.ArrayList;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class SaveSimulationsArtifactsDialog extends TitleAreaDialog {

    private Text txtOutputPath;
    private Text txtOutputDirectoryName;
    private Button openFileExplorerButton;

    private String outputPath = "";
    private String outputDirectoryName = "";
    private boolean openFileExplorer = false;
    
    public SaveSimulationsArtifactsDialog() {
        super(Display.getDefault().getActiveShell());
        setHelpAvailable(false);
    }

    @Override
    public void create() {
        super.create();
        setTitle("Save Simulations Artifacts");
        setMessage("Please, set the desired path of the saved data", IMessageProvider.INFORMATION);
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
        
        createPathSection(container);
        createDirectoryNameSection(container);
        createOptionsSection(container);
        return area;
    }

    private void createOptionsSection(Composite container) {
        CLabel lblOptions = new CLabel(container, SWT.NONE);
        lblOptions.setText("Options :");
        lblOptions.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));

        openFileExplorerButton = new Button(container, SWT.CHECK);
        openFileExplorerButton.setText("Open file explorer after saving ?");
        openFileExplorerButton.setSelection(this.openFileExplorer);
        openFileExplorerButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
    }


    private void createPathSection(Composite container) {
        CLabel lblPath = new CLabel(container, SWT.NONE);
        lblPath.setText("Save in directory :");
        lblPath.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));

        txtOutputPath = new Text(container, SWT.BORDER);
        GridData gdText = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        txtOutputPath.setLayoutData(gdText);

        Button btnBrowse = new Button(container, SWT.PUSH);
        btnBrowse.setText("Browse");
        btnBrowse.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        btnBrowse.addListener(SWT.Selection, event -> {
            DirectoryDialog dialog = new DirectoryDialog(getShell());
            dialog.setText("Select the folder where the simulation data will be saved");
            dialog.setMessage("Select a target path");
            String selectedDirectory = dialog.open();
            if (selectedDirectory != null) {
                txtOutputPath.setText(selectedDirectory);
            }
        });
    }

    private void createDirectoryNameSection(Composite container) {
        CLabel lblPath = new CLabel(container, SWT.NONE);
        lblPath.setText("Save as : ");
        lblPath.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));

        txtOutputDirectoryName = new Text(container, SWT.BORDER);
        txtOutputDirectoryName.setText("simulation_artifacts");
        GridData gdText = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        txtOutputDirectoryName.setLayoutData(gdText);
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    @Override
    protected void okPressed() {
        outputPath = txtOutputPath.getText().trim();
        outputDirectoryName = txtOutputDirectoryName.getText().trim();
        openFileExplorer = openFileExplorerButton.getSelection();

        if (outputPath.isEmpty()) {
            setMessage("Error : The destination path cannot be empty.", IMessageProvider.ERROR);
            return;
        }

        if (outputDirectoryName.isEmpty()) {
            setMessage("Error : The export file name cannot be empty.", IMessageProvider.ERROR);
            return;
        }

        super.okPressed();
    }

    public String getOutputPath() {
        return outputPath;
    }

    public String getOutputDirectoryName() {
        return outputDirectoryName;
    }

    public boolean getOpenFileExplorer(){
        return openFileExplorer;
    }
}
