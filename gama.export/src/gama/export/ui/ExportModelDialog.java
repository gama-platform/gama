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

public class ExportModelDialog extends TitleAreaDialog {

    private Text txtOutputPath;
    private Text txtOutputFileName;
    private Button[] buttons;
    private Button selectAllExperimentsButton;

    private String outputPath = "";
    private String outputFileName = "";
    private String[] availableExperiments;
    private List<String> selectedExperiments;

    public ExportModelDialog(String[] experimentNames) {
        super(Display.getDefault().getActiveShell());
        availableExperiments = experimentNames;
        buttons = new Button[experimentNames.length];
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
        createPathSection(container);
        createFileNameSection(container);

        return area;
    }

    private void createOptionSection(Composite container) {
        CLabel lblOptions = new CLabel(container, SWT.NONE);
        lblOptions.setText("Options :");
        lblOptions.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));

        selectAllExperimentsButton = new Button(container, SWT.CHECK);
        selectAllExperimentsButton.setText("Select all experiments");
        selectAllExperimentsButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));

        for (int i=0 ; i < availableExperiments.length ; i++)
        {
            final int index = i;
            buttons[i] = new Button(container, SWT.CHECK);
            buttons[i].setText(availableExperiments[i]);
            buttons[i].setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
            buttons[i].addListener(SWT.Selection, event -> {
                if (!buttons[index].getSelection() && selectAllExperimentsButton.getSelection())
                    selectAllExperimentsButton.setSelection(false);
            });
        }

        selectAllExperimentsButton.addListener(SWT.Selection, event -> {
            // Récupère l'état actuel de la case maîtresse (true/false)
            final boolean masterState = selectAllExperimentsButton.getSelection();
            
            // Applique cet état exact à chaque enfant de la liste
            for (Button button : buttons) {
                button.setSelection(masterState);
            }
        });
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

    private void createFileNameSection(Composite container) {
        CLabel lblPath = new CLabel(container, SWT.NONE);
        lblPath.setText("Export file name : ");
        lblPath.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));

        txtOutputFileName = new Text(container, SWT.BORDER);
        txtOutputFileName.setText("launcher.zip");
        GridData gdText = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        txtOutputFileName.setLayoutData(gdText);
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    @Override
    protected void okPressed() {
        outputPath = txtOutputPath.getText().trim();
        outputFileName = txtOutputFileName.getText().trim();
        selectedExperiments = new ArrayList<String>();

        for(int i=0 ; i < availableExperiments.length ; i++)
        {
            if(buttons[i].getSelection())
                selectedExperiments.add(availableExperiments[i]);
        }

        if (outputPath.isEmpty()) {
            setMessage("Error : The destination path cannot be empty.", IMessageProvider.ERROR);
            return;
        }

        if (selectedExperiments.isEmpty())
        {
            setMessage("Error : There must be at least one experiment", IMessageProvider.ERROR);
            return;            
        }

        if (outputFileName.isEmpty()) {
            setMessage("Error : The export file name cannot be empty.", IMessageProvider.ERROR);
            return;
        }

        super.okPressed();
    }

    public String getOutputPath() {
        return outputPath;
    }

    public String getOutputFileName()
    {
        return outputFileName;
    }

    public String[] getSelectedExperiments() {
        return selectedExperiments.toArray(new String[0]);
    }
}