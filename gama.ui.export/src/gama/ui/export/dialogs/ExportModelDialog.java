package gama.ui.export.dialogs;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;

import gama.api.runtime.SystemInfo;
import gama.export.LauncherConfiguration;
import gama.export.SimulationPackageExporter.Dependency;

public class ExportModelDialog extends TitleAreaDialog {

	public enum Format { STANDALONE, PACKAGE }

	private final String[] availableExperiments;
	private final String defaultTitle;
	private final String defaultDescription;
	private final Format format;
	private Path destinationDirectory = Path.of(System.getProperty("user.home"));
	private Button[] experiments;
	private Combo defaultChoice;
	private Button includeJdkButton;
	private Button oneFileButton;
	private Button showHomeButton;
	private Text title;
	private Text description;
	private String imagePath = "";
	private Image imagePreview;
	private Text pluginPaths;
	private int defaultExperiment = -1;
	private LauncherConfiguration homeConfiguration;
	private Path output;
	private boolean includeJdk = true;
	private boolean oneFile;
	private List<String> selectedExperiments = List.of();
	private String startupLabel = "";
	private final String exportedPackageId = "simulation." + java.util.UUID.randomUUID();
	private List<Dependency> dependencies = List.of();
	private String dependencyError;

	public ExportModelDialog(final String[] experimentNames) {
		this(experimentNames, "", "");
	}

	public ExportModelDialog(final String[] experimentNames, final String defaultTitle, final String defaultDescription) {
		this(experimentNames, defaultTitle, defaultDescription, Format.PACKAGE);
	}

	public ExportModelDialog(final String[] experimentNames, final String defaultTitle, final String defaultDescription,
			final Format format) {
		super(Display.getDefault().getActiveShell());
		availableExperiments = experimentNames.clone();
		this.defaultTitle = defaultTitle;
		this.defaultDescription = defaultDescription;
		this.format = format;
		setHelpAvailable(false);
	}

	@Override
	public void create() {
		super.create();
		updateHeading();
	}

	private void updateHeading() {
		setTitle(format == Format.PACKAGE ? "Export playable model" : "Export standalone application");
		setMessage(availableExperiments.length == 1 ? "Export the experiment shown below."
				: "Check experiments to export and optionally choose one to run by default.",
				IMessageProvider.INFORMATION);
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite area = (Composite) super.createDialogArea(parent);
		final Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		container.setBackgroundMode(SWT.INHERIT_FORCE);
		final GridLayout layout = new GridLayout(3, false);
		layout.marginWidth = 18;
		layout.marginHeight = 12;
		layout.verticalSpacing = 10;
		container.setLayout(layout);

		title = textField(container, "Title", SWT.SINGLE);
		title.setText(defaultTitle);
		createImageSection(container);
		description = textField(container, "Description", SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		((GridData) description.getLayoutData()).heightHint = description.getLineHeight() * 4;
		description.setText(defaultDescription);
		createExperimentSection(container);
		createOptionsSection(container);
		final Composite formatOptions = new Composite(container, SWT.NONE);
		final GridData formatData = new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1);
		formatOptions.setLayoutData(formatData);
		final GridLayout formatLayout = new GridLayout(2, false);
		formatLayout.marginWidth = formatLayout.marginHeight = 0;
		formatOptions.setLayout(formatLayout);
		if (format == Format.PACKAGE) {
			createPackageSection(formatOptions);
		} else {
			createApplicationOptions(formatOptions);
		}
		formatData.exclude = formatOptions.getChildren().length == 0;
		formatOptions.setVisible(!formatData.exclude);
		return area;
	}

	private void createPackageSection(final Composite container) {
		final List<Dependency> extraDependencies = dependencies.stream().filter(dependency -> !dependency.standard()).toList();
		if (!extraDependencies.isEmpty()) createDependencySection(container, extraDependencies);
		if (dependencyError != null) {
			final Label explanation = new Label(container, SWT.WRAP);
			explanation.setText("Model dependencies could not be resolved: " + dependencyError
					+ "\nUse the standalone application export instead.");
			final GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
			data.widthHint = 500;
			explanation.setLayoutData(data);
		}
	}

	private void createDependencySection(final Composite container, final List<Dependency> extraDependencies) {
		new Label(container, SWT.NONE).setText("Plugin dependencies");
		final Composite row = new Composite(container, SWT.NONE);
		row.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		final GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = layout.marginHeight = 0;
		row.setLayout(layout);
		pluginPaths = new Text(row, SWT.BORDER | SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL);
		pluginPaths.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		((GridData) pluginPaths.getLayoutData()).heightHint = 45;
		pluginPaths.setToolTipText("Required non-standard Java plugins are included automatically; review exact host requirements and unsupported extras.");
		updateDependencies();
		final Composite actions = new Composite(row, SWT.NONE);
		actions.setLayout(new GridLayout());
		final Button choose = new Button(actions, SWT.PUSH);
		choose.setText("Review dependencies...");
		choose.setEnabled(dependencyError == null);
		choose.addListener(SWT.Selection, event -> {
			new PackageDependenciesDialog(getShell(), extraDependencies).open();
		});
	}

	private Text textField(final Composite parent, final String caption, final int style) {
		final boolean multiline = (style & SWT.MULTI) != 0;
		final Label label = new Label(parent, SWT.NONE);
		label.setText(caption);
		final GridData labelData = new GridData(SWT.LEFT, multiline ? SWT.TOP : SWT.CENTER, false, false);
		labelData.verticalIndent = multiline ? 3 : 0;
		label.setLayoutData(labelData);
		final Text field = new Text(parent, SWT.BORDER | style);
		final GridData fieldData = new GridData(SWT.FILL, multiline ? SWT.TOP : SWT.CENTER, true, false);
		fieldData.widthHint = convertWidthInCharsToPixels(38);
		field.setLayoutData(fieldData);
		return field;
	}

	private void createImageSection(final Composite container) {
		final Button browse = new Button(container, SWT.PUSH);
		browse.setText("Image (optional)");
		final GridData data = new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 2);
		data.widthHint = data.heightHint = 104;
		browse.setLayoutData(data);
		browse.setToolTipText("Choose an image for the Home screen");
		browse.addDisposeListener(event -> {
			if (imagePreview != null) imagePreview.dispose();
		});
		final Menu menu = new Menu(browse);
		browse.setMenu(menu);
		final MenuItem remove = new MenuItem(menu, SWT.PUSH);
		remove.setText("Remove image");
		remove.setEnabled(false);
		remove.addListener(SWT.Selection, event -> {
			browse.setImage(null);
			if (imagePreview != null) imagePreview.dispose();
			imagePreview = null;
			imagePath = "";
			browse.setText("Image (optional)");
			browse.setToolTipText("Choose an image for the Home screen");
			remove.setEnabled(false);
		});
		browse.addListener(SWT.Selection, event -> {
			final FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
			dialog.setFilterExtensions(new String[] { "*.png;*.jpg;*.jpeg;*.gif;*.bmp", "*.*" });
			final String selected = dialog.open();
			if (selected == null) return;
			try {
				final ImageData original = new ImageData(selected);
				final double scale = 80d / Math.max(original.width, original.height);
				final Image preview = new Image(browse.getDisplay(), original.scaledTo(
						Math.max(1, (int) Math.round(original.width * scale)),
						Math.max(1, (int) Math.round(original.height * scale))));
				browse.setImage(preview);
				browse.setText("");
				browse.setToolTipText(selected + "\nClick to change; right-click to remove.");
				if (imagePreview != null) imagePreview.dispose();
				imagePreview = preview;
				imagePath = selected;
				remove.setEnabled(true);
				setErrorMessage(null);
			} catch (final IllegalArgumentException | org.eclipse.swt.SWTException e) {
				setErrorMessage("Unable to load image: " + e.getMessage());
			}
		});
	}

	private void createExperimentSection(final Composite container) {
		final Label label = new Label(container, SWT.NONE);
		label.setText(availableExperiments.length == 1 ? "Experiment" : "Experiments");
		label.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		if (availableExperiments.length == 1) {
			final Label experiment = new Label(container, SWT.NONE);
			experiment.setText(availableExperiments[0]);
			experiment.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			defaultExperiment = 0;
			return;
		}
		final Composite row = new Composite(container, SWT.NONE);
		row.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
		final GridLayout layout = new GridLayout(3, true);
		layout.marginWidth = layout.marginHeight = 0;
		row.setLayout(layout);
		experiments = new Button[availableExperiments.length];
		final int rows = (availableExperiments.length + 2) / 3;
		for (int column = 0; column < 3; column++) {
			final Composite choices = new Composite(row, SWT.NONE);
			choices.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			final GridLayout columnLayout = new GridLayout();
			columnLayout.marginWidth = columnLayout.marginHeight = 0;
			choices.setLayout(columnLayout);
			for (int index = column * rows; index < Math.min((column + 1) * rows, experiments.length); index++) {
				final Button experiment = new Button(choices, SWT.CHECK | SWT.WRAP);
				experiment.setText(availableExperiments[index]);
				final GridData data = new GridData(SWT.FILL, SWT.TOP, true, false);
				data.widthHint = convertWidthInCharsToPixels(18);
				experiment.setLayoutData(data);
				experiment.addListener(SWT.Selection, event -> updateExperimentSelection());
				experiments[index] = experiment;
			}
		}
	}

	private int[] selectedExperimentIndices() {
		return availableExperiments.length == 1 ? new int[] { 0 }
				: java.util.stream.IntStream.range(0, experiments.length)
						.filter(index -> experiments[index].getSelection()).toArray();
	}

	private void updateExperimentSelection() {
		final int[] selected = selectedExperimentIndices();
		if (selected.length == 1) {
			defaultExperiment = selected[0];
		} else if (defaultExperiment >= 0 && !experiments[defaultExperiment].getSelection()) {
			defaultExperiment = -1;
		}
		if (defaultChoice != null) {
			defaultChoice.removeAll();
			defaultChoice.add("None - show selector");
			int choice = 0;
			for (int index = 0; index < selected.length; index++) {
				defaultChoice.add(availableExperiments[selected[index]]);
				if (selected[index] == defaultExperiment) choice = index + 1;
			}
			defaultChoice.select(choice);
			defaultChoice.setEnabled(selected.length > 1);
		}
		updateDefault();
		final Button export = getButton(IDialogConstants.OK_ID);
		if (export != null) export.setEnabled(selected.length > 0);
	}

	private void updateDefault() {
		showHomeButton.setEnabled(defaultExperiment >= 0);
		if (defaultExperiment < 0) showHomeButton.setSelection(true);
	}

	private void createOptionsSection(final Composite container) {
		new Label(container, SWT.NONE).setText("Default experiment");
		final Composite row = new Composite(container, SWT.NONE);
		row.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		final GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = layout.marginHeight = 0;
		row.setLayout(layout);
		defaultChoice = new Combo(row, SWT.DROP_DOWN | SWT.READ_ONLY);
		final GridData defaultData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		defaultData.widthHint = convertWidthInCharsToPixels(24);
		defaultChoice.setLayoutData(defaultData);
		defaultChoice.addListener(SWT.Selection, event -> {
			final int choice = defaultChoice.getSelectionIndex();
			defaultExperiment = choice <= 0 ? -1 : selectedExperimentIndices()[choice - 1];
			updateDefault();
		});
		showHomeButton = new Button(row, SWT.CHECK);
		showHomeButton.setText("Show selector at startup");
		showHomeButton.setToolTipText("Required when no default experiment is selected");
		updateExperimentSelection();
	}

	private void createApplicationOptions(final Composite container) {
		final Label explanation = new Label(container, SWT.WRAP);
		explanation.setText("Export a complete application instead of a playable model. Application signing and notarization "
				+ "remain your responsibility when distributing it.");
		final GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		data.widthHint = 500;
		explanation.setLayoutData(data);
		includeJdkButton = new Button(container, SWT.CHECK);
		includeJdkButton.setText("Include JDK");
		includeJdkButton.setSelection(true);
		if (!SystemInfo.isMac()) {
			oneFileButton = new Button(container, SWT.CHECK);
			oneFileButton.setText("Export as a single-file executable");
			oneFileButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		}
	}

	@Override
	protected void createButtonsForButtonBar(final Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "Choose destination...", true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		updateExperimentSelection();
	}

	private String exportSuffix() {
		if (format == Format.PACKAGE) return ".gamx";
		if (oneFileButton != null && oneFileButton.getSelection()) return SystemInfo.isWindows() ? ".exe" : "";
		return SystemInfo.isMac() ? ".app.zip" : ".zip";
	}

	private String suggestedFileName() {
		final String filename = title.getText().trim()
				.replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]", "_").replaceFirst("[. ]+$", "")
				.replaceFirst("(?i)(?:\\.gaml|\\.gamx|\\.app\\.zip|\\.zip|\\.exe)+$", "");
		return (filename.isBlank() ? "launcher" : filename) + exportSuffix();
	}

	@Override
	protected boolean isResizable() { return true; }

	@Override
	protected void okPressed() {
		try {
			if (format == Format.PACKAGE) {
				if (dependencyError != null) throw new IllegalArgumentException("Model dependencies could not be resolved: " + dependencyError);
				for (final Dependency dependency : dependencies) {
					if (!dependency.standard() && !dependency.embeddable())
						throw new IllegalArgumentException("Cannot include in the playable model " + dependency.id() + ": " + dependency.reason());
				}
			}
			selectedExperiments = Arrays.stream(selectedExperimentIndices())
					.mapToObj(index -> availableExperiments[index]).toList();
			if (selectedExperiments.isEmpty()) throw new IllegalArgumentException("Select at least one experiment.");
			if (defaultExperiment >= 0 && Arrays.stream(selectedExperimentIndices()).noneMatch(index -> index == defaultExperiment))
				throw new IllegalArgumentException("The default experiment must be selected for export.");
			if (!imagePath.isBlank()) {
				final Path path = Path.of(imagePath);
				if (!Files.isRegularFile(path)) throw new IllegalArgumentException("Choose a readable image file.");
				new ImageData(path.toString());
			}
		} catch (final IllegalArgumentException | org.eclipse.swt.SWTException e) {
			setErrorMessage(e.getMessage());
			return;
		}
		final FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
		dialog.setText(format == Format.PACKAGE ? "Export playable model" : "Export standalone application");
		final String suffix = exportSuffix();
		String filenameSuggestion = suggestedFileName();
		if (!suffix.isEmpty()) {
			// Cocoa supplies the final extension itself and only supports single-part filters.
			final String filterSuffix = SystemInfo.isMac() ? suffix.substring(suffix.lastIndexOf('.')) : suffix;
			dialog.setFilterExtensions(new String[] { "*" + filterSuffix });
			if (SystemInfo.isMac())
				filenameSuggestion = filenameSuggestion.substring(0, filenameSuggestion.length() - filterSuffix.length());
		}
		dialog.setFilterPath(destinationDirectory.toString());
		dialog.setFileName(filenameSuggestion);
		dialog.setOverwrite(true);
		final String selected = dialog.open();
		if (selected == null) return;
		try {
			final Path chosen = Path.of(selected).toAbsolutePath().normalize();
			if (chosen.getFileName() == null || Files.isDirectory(chosen))
				throw new IllegalArgumentException("The destination must be a file, not a directory.");
			destinationDirectory = chosen.getParent();
			final String filename = chosen.getFileName().toString();
			output = suffix.isEmpty() || filename.toLowerCase(java.util.Locale.ROOT).endsWith(suffix)
					? chosen : chosen.resolveSibling(filename + suffix);
			if (!output.equals(chosen) && Files.exists(output)
					&& !org.eclipse.jface.dialogs.MessageDialog.openQuestion(getShell(), "Replace existing export?",
							"The file already exists:\n" + output + "\n\nReplace it?")) return;
		} catch (final IllegalArgumentException e) {
			setErrorMessage("Invalid export destination: " + e.getMessage());
			return;
		}
		includeJdk = format == Format.STANDALONE && includeJdkButton.getSelection();
		oneFile = format == Format.STANDALONE && oneFileButton != null && oneFileButton.getSelection();
		startupLabel = defaultExperiment < 0 ? "" : availableExperiments[defaultExperiment];
		homeConfiguration = new LauncherConfiguration(title.getText().trim(), description.getText(),
				showHomeButton.getSelection(), startupLabel, imagePath);
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

	public String getPackageId() { return exportedPackageId; }

	public String getPackageVersion() { return "1.0.0"; }

	public List<Path> getEmbeddedPlugins() {
		return dependencies.stream().filter(Dependency::automatic).map(Dependency::file).toList();
	}

	public Format getFormat() { return format; }

	public void setDependencies(final List<Dependency> dependencies) {
		this.dependencies = List.copyOf(dependencies);
	}

	public void setDependencyError(final String error) { dependencyError = error; }

	private void updateDependencies() {
		final List<Path> embeddedPlugins = getEmbeddedPlugins();
		final long unsupported = dependencies.stream().filter(dependency -> !dependency.standard() && !dependency.embeddable()).count();
		pluginPaths.setText(embeddedPlugins.size() + " additional plugins included automatically"
				+ (unsupported == 0 ? "" : "; " + unsupported + " unsupported (export blocked)")
				+ "\n" + String.join(", ", dependencies.stream()
						.filter(dependency -> dependency.embeddable() && embeddedPlugins.contains(dependency.file()))
						.map(Dependency::id).toList()));
	}
}
