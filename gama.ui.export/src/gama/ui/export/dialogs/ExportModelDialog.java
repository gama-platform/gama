package gama.ui.export.dialogs;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import gama.api.runtime.SystemInfo;
import gama.export.LauncherConfiguration;

public class ExportModelDialog extends TitleAreaDialog {

	private final String[] availableExperiments;
	private final String defaultTitle;
	private final String defaultDescription;
	private final List<Button> defaultButtons = new ArrayList<>();
	private Text destination;
	private Table experiments;
	private Button includeJdkButton;
	private Button oneFileButton;
	private Button showHomeButton;
	private Text title;
	private Text description;
	private Text image;
	private int defaultExperiment = -1;
	private LauncherConfiguration homeConfiguration;
	private Path output;
	private boolean includeJdk = true;
	private boolean oneFile;
	private List<String> selectedExperiments = List.of();
	private String startupLabel = "";

	public ExportModelDialog(final String[] experimentNames) {
		this(experimentNames, "", "");
	}

	public ExportModelDialog(final String[] experimentNames, final String defaultTitle, final String defaultDescription) {
		super(Display.getDefault().getActiveShell());
		availableExperiments = experimentNames.clone();
		this.defaultTitle = defaultTitle;
		this.defaultDescription = defaultDescription;
		setHelpAvailable(false);
	}

	@Override
	public void create() {
		super.create();
		setTitle("Export standalone model");
		setMessage("Select experiments to export and optionally choose one to run by default.",
				IMessageProvider.INFORMATION);
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite area = (Composite) super.createDialogArea(parent);
		final Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		final GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 18;
		layout.marginHeight = 12;
		layout.verticalSpacing = 10;
		container.setLayout(layout);

		title = textField(container, "Title", SWT.SINGLE);
		title.setText(defaultTitle);
		description = textField(container, "Description", SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		((GridData) description.getLayoutData()).heightHint = 70;
		description.setText(defaultDescription);
		createImageSection(container);
		createExperimentSection(container);
		createOptionsSection(container);
		createDestinationSection(container);
		return area;
	}

	private Text textField(final Composite parent, final String caption, final int style) {
		final Label label = new Label(parent, SWT.NONE);
		label.setText(caption);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		final Text field = new Text(parent, SWT.BORDER | style);
		field.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		return field;
	}

	private void createImageSection(final Composite container) {
		final Label label = new Label(container, SWT.NONE);
		label.setText("Image (optional)");
		final Composite row = new Composite(container, SWT.NONE);
		row.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		final GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = layout.marginHeight = 0;
		row.setLayout(layout);
		image = new Text(row, SWT.BORDER);
		image.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		final Button browse = new Button(row, SWT.PUSH);
		browse.setText("Browse...");
		browse.addListener(SWT.Selection, event -> {
			final FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
			dialog.setFilterExtensions(new String[] { "*.png;*.jpg;*.jpeg;*.gif;*.bmp", "*.*" });
			final String selected = dialog.open();
			if (selected != null) image.setText(selected);
		});
	}

	private void createExperimentSection(final Composite container) {
		final Label label = new Label(container, SWT.NONE);
		label.setText("Experiments");
		label.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		final Composite row = new Composite(container, SWT.NONE);
		row.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		final GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = layout.marginHeight = 0;
		row.setLayout(layout);
		experiments = new Table(row, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.V_SCROLL);
		experiments.setHeaderVisible(true);
		final GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = 170;
		data.widthHint = 420;
		experiments.setLayoutData(data);
		final TableColumn name = new TableColumn(experiments, SWT.LEFT);
		name.setText("Experiment");
		final TableColumn startup = new TableColumn(experiments, SWT.CENTER);
		startup.setText("Default");
		startup.setWidth(75);
		for (final String experiment : availableExperiments) {
			final int index = defaultButtons.size();
			final TableItem item = new TableItem(experiments, SWT.NONE);
			item.setText(experiment);
			final Composite cell = new Composite(experiments, SWT.NO_RADIO_GROUP);
			final GridLayout cellLayout = new GridLayout();
			cellLayout.marginWidth = cellLayout.marginHeight = 0;
			cell.setLayout(cellLayout);
			final Button radio = new Button(cell, SWT.RADIO);
			radio.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
			radio.setToolTipText("Run " + experiment + " by default");
			defaultButtons.add(radio);
			final TableEditor editor = new TableEditor(experiments);
			editor.grabHorizontal = editor.grabVertical = true;
			editor.setEditor(cell, item, 1);
			experiments.addDisposeListener(event -> editor.dispose());
			radio.addListener(SWT.Selection, event -> {
				defaultExperiment = radio.getSelection() ? index : -1;
				if (defaultExperiment >= 0) experiments.select(index);
				updateDefault();
			});
		}
		experiments.addListener(SWT.Resize, event ->
				name.setWidth(Math.max(180, experiments.getClientArea().width - startup.getWidth())));
		experiments.addListener(SWT.Selection, event -> {
			if (defaultExperiment >= 0 && !experiments.isSelected(defaultExperiment)) defaultExperiment = -1;
			updateDefault();
		});
		experiments.selectAll();
		final Composite actions = new Composite(row, SWT.NONE);
		actions.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		final GridLayout actionsLayout = new GridLayout();
		actionsLayout.marginWidth = actionsLayout.marginHeight = 0;
		actions.setLayout(actionsLayout);
		final Button selectAll = new Button(actions, SWT.PUSH);
		selectAll.setText("Select all");
		selectAll.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		selectAll.addListener(SWT.Selection, event -> experiments.selectAll());
		final Button clearDefault = new Button(actions, SWT.PUSH);
		clearDefault.setText("No default");
		clearDefault.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		clearDefault.addListener(SWT.Selection, event -> {
			defaultExperiment = -1;
			updateDefault();
		});
	}

	private void updateDefault() {
		for (int index = 0; index < defaultButtons.size(); index++)
			defaultButtons.get(index).setSelection(index == defaultExperiment);
		showHomeButton.setEnabled(defaultExperiment >= 0);
		if (defaultExperiment < 0) showHomeButton.setSelection(true);
	}

	private void createOptionsSection(final Composite container) {
		new Label(container, SWT.NONE).setText("Options");
		final Composite row = new Composite(container, SWT.NONE);
		row.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		final GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = layout.marginHeight = 0;
		row.setLayout(layout);
		includeJdkButton = new Button(row, SWT.CHECK);
		includeJdkButton.setText("Include JDK");
		includeJdkButton.setSelection(true);
		showHomeButton = new Button(row, SWT.CHECK);
		showHomeButton.setText("Show Home at startup");
		showHomeButton.setToolTipText("Required when no default experiment is selected");
		updateDefault();
		if (!SystemInfo.isMac()) {
			oneFileButton = new Button(row, SWT.CHECK);
			oneFileButton.setText("Export as a single-file executable");
			oneFileButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			oneFileButton.addListener(SWT.Selection, event -> {
				final String value = destination.getText();
				final String extension = SystemInfo.isWindows() ? ".exe" : "";
				if (oneFileButton.getSelection() && value.endsWith(".zip"))
					destination.setText(value.substring(0, value.length() - 4) + extension);
				else if (!oneFileButton.getSelection() && !value.endsWith(".zip"))
					destination.setText((!extension.isEmpty() && value.endsWith(extension)
							? value.substring(0, value.length() - extension.length()) : value) + ".zip");
			});
		}
	}

	private void createDestinationSection(final Composite container) {
		new Label(container, SWT.NONE).setText("Save as");
		final Composite row = new Composite(container, SWT.NONE);
		row.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		final GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = layout.marginHeight = 0;
		row.setLayout(layout);
		destination = new Text(row, SWT.BORDER);
		destination.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		final String filename = defaultTitle.isBlank() ? "launcher"
				: defaultTitle.replaceFirst("\\.[^.]+$", "").replaceAll("[\\\\/:*?\"<>|]", "_");
		destination.setText(Path.of(System.getProperty("user.home"),
				filename + (SystemInfo.isMac() ? ".app.zip" : ".zip")).toString());
		final Button browse = new Button(row, SWT.PUSH);
		browse.setText("Browse...");
		browse.addListener(SWT.Selection, event -> {
			final FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
			dialog.setText("Export standalone model");
			dialog.setOverwrite(true);
			try {
				final Path path = Path.of(destination.getText()).toAbsolutePath();
				if (path.getParent() != null) dialog.setFilterPath(path.getParent().toString());
				if (path.getFileName() != null) dialog.setFileName(path.getFileName().toString());
			} catch (final InvalidPathException e) {
				setErrorMessage("The destination is not a valid file path.");
				return;
			}
			final String selected = dialog.open();
			if (selected != null) destination.setText(selected);
		});
	}

	@Override
	protected boolean isResizable() { return true; }

	@Override
	protected void okPressed() {
		try {
			if (destination.getText().isBlank()) throw new IllegalArgumentException("Choose a destination file.");
			output = Path.of(destination.getText().trim()).toAbsolutePath().normalize();
			if (output.getFileName() == null || Files.isDirectory(output))
				throw new IllegalArgumentException("The destination must be a file, not a directory.");
			selectedExperiments = Arrays.stream(experiments.getSelectionIndices())
					.mapToObj(index -> availableExperiments[index]).toList();
			if (selectedExperiments.isEmpty()) throw new IllegalArgumentException("Select at least one experiment.");
			if (defaultExperiment >= 0 && !experiments.isSelected(defaultExperiment))
				throw new IllegalArgumentException("The default experiment must be selected for export.");
			if (!image.getText().isBlank()) {
				final Path path = Path.of(image.getText().trim());
				if (!Files.isRegularFile(path)) throw new IllegalArgumentException("Choose a readable image file.");
				new ImageData(path.toString());
			}
		} catch (final IllegalArgumentException | org.eclipse.swt.SWTException e) {
			setErrorMessage(e.getMessage());
			return;
		}
		includeJdk = includeJdkButton.getSelection();
		oneFile = oneFileButton != null && oneFileButton.getSelection();
		startupLabel = defaultExperiment < 0 ? "" : availableExperiments[defaultExperiment];
		homeConfiguration = new LauncherConfiguration(title.getText().trim(), description.getText(),
				showHomeButton.getSelection(), startupLabel, image.getText().trim());
		super.okPressed();
	}

	public String getOutputPath() { return output.getParent().toString(); }

	public String getOutputFileName() { return output.getFileName().toString(); }

	public boolean getIncludeJdk() { return includeJdk; }

	public boolean getOneFile() { return oneFile; }

	public String[] getSelectedExperiments() { return selectedExperiments.toArray(String[]::new); }

	public LauncherConfiguration getLauncherConfiguration(final String fallbackTitle, final String startupSelection) {
		return new LauncherConfiguration(homeConfiguration.title().isBlank() ? fallbackTitle : homeConfiguration.title(),
				homeConfiguration.description(), homeConfiguration.showHome(), startupSelection, homeConfiguration.image());
	}

	public String getStartupExperiment() { return startupLabel; }
}
