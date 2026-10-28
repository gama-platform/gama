package gama.ui.export.dialogs;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import gama.api.runtime.SystemInfo;
import gama.export.LauncherConfiguration;

public class ExportModelDialog extends TitleAreaDialog {

    private Text txtOutputPath;
    private Text txtOutputFileName;
    private Button[] buttons;
    private Button includeJdkButton;
    private Button selectAllExperimentsButton;
    private Button oneFileButton;
    private Button showHomeButton;
    private Combo startupExperiment;
    private Text title;
    private Text description;
    private Text image;
    private LauncherConfiguration homeConfiguration;
    private String startupLabel;

    private String outputPath = "";
    private String outputFileName = "";
    private boolean includeJdk = true;
    private boolean oneFile = false;
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

        createExperimentSection(container);
        createOptionsSection(container);
        createHomeSection(container);
        createPathSection(container);
        createFileNameSection(container);

        return area;
    }

    private void createExperimentSection(Composite container) {
        CLabel lblExperiments = new CLabel(container, SWT.NONE);
        lblExperiments.setText("Select the experiments you want to export :");
        lblExperiments.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));

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

    private void createOptionsSection(Composite container) {
        CLabel lblOptions = new CLabel(container, SWT.NONE);
        lblOptions.setText("Options :");
        lblOptions.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));

        includeJdkButton = new Button(container, SWT.CHECK);
        includeJdkButton.setText("Include the JDK");
        includeJdkButton.setSelection(true);
        includeJdkButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));

        if (! SystemInfo.isMac()) {
            oneFileButton = new Button(container, SWT.CHECK);
            oneFileButton.setText("Export as a single file executable");
            oneFileButton.setSelection(false);
            oneFileButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
            oneFileButton.addListener(SWT.Selection, event -> {

                final String currentOutputFileNameValue = txtOutputFileName.getText();

                if(! currentOutputFileNameValue.startsWith("launcher.") && ! currentOutputFileNameValue.equals("launcher"))
                    return;

                if(oneFileButton.getSelection())
                    if(SystemInfo.isWindows())
                        txtOutputFileName.setText("launcher.exe");
                    else
                        txtOutputFileName.setText("launcher");
                else
                    txtOutputFileName.setText("launcher.zip");
            });        
        }
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

    private void createHomeSection(final Composite container) {
        final CLabel heading = new CLabel(container, SWT.NONE);
        heading.setText("Launcher home page:");
        heading.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
        title = textField(container, "Title", SWT.SINGLE);
        description = textField(container, "Description", SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
        ((GridData) description.getLayoutData()).heightHint = 60;
        image = textField(container, "Image (optional)", SWT.SINGLE);
        final Button browse = new Button(container, SWT.PUSH);
        browse.setText("Choose image...");
        browse.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 3, 1));
        browse.addListener(SWT.Selection, event -> {
            final FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
            dialog.setFilterExtensions(new String[] { "*.png;*.jpg;*.jpeg;*.gif;*.bmp", "*.*" });
            final String selected = dialog.open();
            if (selected != null) image.setText(selected);
        });
        showHomeButton = new Button(container, SWT.CHECK);
        showHomeButton.setText("Show the home page before launching an experiment");
        showHomeButton.setSelection(true);
        showHomeButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
        final CLabel label = new CLabel(container, SWT.NONE);
        label.setText("Otherwise launch:");
        startupExperiment = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
        startupExperiment.setItems(availableExperiments);
        startupExperiment.select(0);
        startupExperiment.setEnabled(false);
        startupExperiment.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        showHomeButton.addListener(SWT.Selection,
                event -> startupExperiment.setEnabled(!showHomeButton.getSelection()));
    }

    private Text textField(final Composite parent, final String label, final int style) {
        final CLabel caption = new CLabel(parent, SWT.NONE);
        caption.setText(label);
        final Text field = new Text(parent, SWT.BORDER | style);
        field.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        return field;
    }

    private void createFileNameSection(Composite container) {
        CLabel lblPath = new CLabel(container, SWT.NONE);
        lblPath.setText("Export file name : ");
        lblPath.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));

        txtOutputFileName = new Text(container, SWT.BORDER);
        if(SystemInfo.isMac())
            txtOutputFileName.setText("launcher.app.zip");
        else
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
        includeJdk = includeJdkButton.getSelection();

        if (! SystemInfo.isMac())
            oneFile = oneFileButton.getSelection();

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

        if (!showHomeButton.getSelection() && !selectedExperiments.contains(startupExperiment.getText())) {
            setMessage("The startup experiment must be selected for export.", IMessageProvider.ERROR);
            return;
        }
        if (!image.getText().isBlank()) {
            try {
                final Path path = Path.of(image.getText().trim());
                if (!Files.isRegularFile(path)) throw new IllegalArgumentException("The image does not exist");
                new ImageData(path.toString());
            } catch (final RuntimeException e) {
                setMessage("Choose a readable image file.", IMessageProvider.ERROR);
                return;
            }
        }

        homeConfiguration = new LauncherConfiguration(title.getText().trim(), description.getText(),
                showHomeButton.getSelection(), "", image.getText().trim());
        startupLabel = startupExperiment.getText();
        super.okPressed();
    }

    public String getOutputPath() {
        return outputPath;
    }

    public String getOutputFileName()
    {
        return outputFileName;
    }

    public boolean getIncludeJdk()
    {
        return includeJdk;
    }

    public boolean getOneFile() {
        return oneFile;
    }

    public String[] getSelectedExperiments() {
        return selectedExperiments.toArray(new String[0]);
    }

    public LauncherConfiguration getLauncherConfiguration(final String defaultTitle, final String startupSelection) {
        return new LauncherConfiguration(homeConfiguration.title().isBlank() ? defaultTitle : homeConfiguration.title(),
                homeConfiguration.description(), homeConfiguration.showHome(), startupSelection, homeConfiguration.image());
    }

    public String getStartupExperiment() { return startupLabel; }
}