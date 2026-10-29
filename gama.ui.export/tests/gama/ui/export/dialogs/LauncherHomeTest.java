package gama.ui.export.dialogs;

import java.lang.reflect.Field;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import gama.export.LauncherConfiguration;
import gama.export.ExportModels;
import gama.ui.application.workbench.PerspectiveHelper;
import gama.ui.export.launcher.LauncherHomePerspective;
import gama.ui.export.launcher.LauncherHomeView;
import gama.ui.export.launcher.StartupModelHelper;
import gama.ui.shared.controls.FlatButton;
import gama.ui.shared.menus.ExperimentMenu;
import gama.ui.shared.utils.LaunchPresentation;

/** SWT checks; Home and menu checks also run in an initialized workbench. */
public final class LauncherHomeTest {

	private static void check(final boolean condition, final String message) {
		if (!condition) throw new AssertionError(message);
	}

	private static <T> T field(final ExportModelDialog dialog, final String name, final Class<T> type)
			throws Exception {
		final Field field = ExportModelDialog.class.getDeclaredField(name);
		field.setAccessible(true);
		return type.cast(field.get(dialog));
	}

	public static void main(final String[] args) throws Exception {
		check(PerspectiveHelper.getHomePerspectiveId().equals(PerspectiveHelper.PERSPECTIVE_MODELING_ID),
				"Normal GAMA must retain its modeling home");
		try {
			PerspectiveHelper.setHomePerspectiveId(LauncherHomePerspective.ID);
			check(PerspectiveHelper.isModelingPerspective(), "Launcher home must act as the idle perspective");
			check(PerspectiveHelper.isHomePerspective(LauncherHomePerspective.ID), "Home recognition");
			check(!PerspectiveHelper.isSimulationPerspective(),
					"Home must not be deleted with simulation perspectives");
		} finally {
			PerspectiveHelper.setHomePerspectiveId(PerspectiveHelper.PERSPECTIVE_MODELING_ID);
		}
		check(StartupModelHelper.getInstance().label("Run@project/model.gaml")
				.equals("Experiment Run"), "Experiment labels omit internal model paths");
		check(ExportModels.leadingDescription("/**\n* Name: Model\n* Description: First line\n* Second line\n"
				+ "* Author: Someone\n* Tags: example\n*/\nmodel test\n// Not header")
				.equals("First line\nSecond line"), "Prefill only leading description, not header metadata");
		check(ExportModels.leadingDescription("// First line\n// Second line\nmodel test")
				.equals("First line\nSecond line"), "Line-comment descriptions");
		check(ExportModels.leadingDescription("model test\n/* not documentation */").isEmpty(),
				"Do not use comments from the model body");

		final Display display = new Display();
		try {
			checkDialog(false, true);
			checkDialog(true, false);
			checkDialog(true, true);
			checkScrollableExperiments();
			checkPackageDialog();
			checkDependenciesDialog();
			checkDependencyVisibility();
		} finally {
			display.dispose();
		}
		System.out.println("Launcher home routing and export dialog checks passed");
	}

	public static void runInWorkbench() throws Exception {
		checkDialog(false, true);
		checkDialog(true, false);
		checkDialog(true, true);
		checkScrollableExperiments();
		checkPackageDialog();
		checkDependenciesDialog();
		checkDependencyVisibility();
		checkHomeView(Display.getCurrent(), false);
		checkHomeView(Display.getCurrent(), true);
		checkSharedMenu(Display.getCurrent());
	}

	private static void checkDependenciesDialog() throws Exception {
		final var file = java.nio.file.Path.of("installed-extension.jar");
		final var dependencies = java.util.List.of(
				new gama.export.SimulationPackageExporter.Dependency("example.extension", "1.0.0", file, true, false, "Automatically embedded"),
				new gama.export.SimulationPackageExporter.Dependency("gama.core", "1.0.0", null, false, true, "Platform requirement"),
				new gama.export.SimulationPackageExporter.Dependency("example.native", "1.0.0", file, false, false, "Native code is host-only"));
		final var dialog = new PackageDependenciesDialog(Display.getCurrent().getActiveShell(), dependencies);
		try {
			dialog.create();
			final Field tableField = PackageDependenciesDialog.class.getDeclaredField("table");
			tableField.setAccessible(true);
			final Table table = (Table) tableField.get(dialog);
			check(table.getItemCount() == 3 && table.getItem(0).getChecked()
					&& table.getItem(0).getText(2).equals("Package (automatic)")
					&& table.getItem(1).getText(2).equals("Standard GAMA")
					&& table.getItem(2).getText(2).equals("Unsupported extra"), "Distinguish standard, packaged and unsupported extra dependencies");
			final var host = table.getItem(1);
			check(host.getGrayed(), "Host-only requirements must not look selectable");
			host.setChecked(true);
			final var event = new org.eclipse.swt.widgets.Event();
			event.item = host;
			event.detail = SWT.CHECK;
			table.notifyListeners(SWT.Selection, event);
			check(!host.getChecked(), "Do not allow embedding platform or unsupported plugins");
			table.getItem(0).setChecked(false);
			event.item = table.getItem(0);
			table.notifyListeners(SWT.Selection, event);
			check(table.getItem(0).getChecked(), "Required non-standard Java plugins remain included");
			final var wizard = new ExportModelDialog(new String[] { "Run" }, "Model", "");
			wizard.setDependencies(dependencies);
			check(wizard.getEmbeddedPlugins().equals(java.util.List.of(file)), "Wizard defaults to automatic extra plugins only");
		} finally {
			dialog.close();
		}
	}

	private static void checkPackageDialog() throws Exception {
		final var dialog = new ExportModelDialog(new String[] { "Run" }, "Model.gaml", "Description");
		final String identifier = dialog.getPackageId();
		try {
			dialog.create();
			check(dialog.getFormat() == ExportModelDialog.Format.PACKAGE
					&& !field(dialog, "applicationOptions", Composite.class).getVisible(), "Default to packages and hide application options");
			check(field(dialog, "destination", Text.class).getText().endsWith("Model.gamp"), "Use the package suffix");
			check(field(dialog, "pluginPaths", Text.class) == null, "No dependency controls when no extras are required");
			check(java.util.Arrays.stream(field(dialog, "packageOptions", Composite.class).getChildren())
					.noneMatch(control -> control instanceof Label label
							&& (label.getText().equals("Package identifier") || label.getText().equals("Package version"))),
					"Technical package metadata is not exposed as form fields");
			field(dialog, "title", Text.class).setText("Preserve my title");
			field(dialog, "experiments", Table.class).setSelection(0);
			dialog.buttonPressed(org.eclipse.jface.dialogs.IDialogConstants.CLIENT_ID);
			check(dialog.getFormat() == ExportModelDialog.Format.STANDALONE
					&& field(dialog, "applicationOptions", Composite.class).getVisible()
					&& !field(dialog, "destination", Text.class).getText().endsWith(".gamp"),
					"Application button must reveal the legacy options and change the destination suffix");
			dialog.buttonPressed(org.eclipse.jface.dialogs.IDialogConstants.CLIENT_ID);
			check(dialog.getFormat() == ExportModelDialog.Format.PACKAGE
					&& field(dialog, "destination", Text.class).getText().endsWith(".gamp")
					&& field(dialog, "title", Text.class).getText().equals("Preserve my title")
					&& dialog.getPackageId().equals(identifier)
					&& field(dialog, "experiments", Table.class).getSelectionCount() == 1,
					"Switching formats must preserve presentation, package metadata and experiment selection");
			field(dialog, "destination", Text.class).setText(java.nio.file.Path.of(System.getProperty("java.io.tmpdir"),
					"package-dialog-" + java.util.UUID.randomUUID() + ".gamp").toString());
			dialog.okPressed();
			check(dialog.getShell() == null || dialog.getShell().isDisposed(), "Package OK must close the dialog");
			check(dialog.getPackageId().equals(identifier) && identifier.matches("simulation\\.[a-f0-9-]+")
					&& dialog.getPackageVersion().equals("1.0.0")
					&& dialog.getEmbeddedPlugins().isEmpty(), "Capture package settings before widget disposal");
			check(!dialog.getIncludeJdk() && !dialog.getOneFile(), "Never embed an application runtime in a package");
		} finally {
			dialog.close();
		}
	}

	private static void checkDependencyVisibility() throws Exception {
		final var standard = new gama.export.SimulationPackageExporter.Dependency("gama.core", "1.0", null, false, true, "Standard");
		for (final boolean extra : new boolean[] { false, true }) {
			final var dialog = new ExportModelDialog(new String[] { "Run" }, "Model", "");
			final var plugin = new gama.export.SimulationPackageExporter.Dependency("example.extra", "1.0",
					java.nio.file.Path.of("extra.jar"), true, false, "Automatic");
			dialog.setDependencies(extra ? java.util.List.of(standard, plugin) : java.util.List.of(standard));
			try {
				dialog.create();
				final Text summary = field(dialog, "pluginPaths", Text.class);
				check(extra == (summary != null), "Show dependency controls only when non-standard plugins are needed");
				if (extra) check(summary.getText().contains("1 additional plugins included automatically")
						&& !summary.getText().contains("standard GAMA"), "Only summarize extra plugins");
			} finally { dialog.close(); }
		}
		final var failed = new ExportModelDialog(new String[] { "Run" }, "Model", "");
		failed.setDependencyError("Discovery failed");
		try {
			failed.create();
			check(java.util.Arrays.stream(field(failed, "packageOptions", Composite.class).getChildren())
					.anyMatch(control -> control instanceof Label label && label.getText().contains("Discovery failed")),
					"Dependency errors must remain visible even without discovered extra plugins");
		} finally { failed.close(); }
	}

	private static void checkHomeView(final Display display, final boolean singleExperiment) throws Exception {
		final var helper = StartupModelHelper.getInstance();
		final Field configuration = StartupModelHelper.class.getDeclaredField("configuration");
		final Field selections = StartupModelHelper.class.getDeclaredField("selections");
		configuration.setAccessible(true);
		selections.setAccessible(true);
		final Object previousConfiguration = configuration.get(helper);
		final Object previousSelections = selections.get(helper);
		final Shell shell = new Shell(display);
		try {
			configuration.set(helper, new LauncherConfiguration("A model", "Description", true, "First", ""));
			selections.set(helper, singleExperiment ? java.util.List.of("First") : java.util.List.of("First", "Second"));
			final String previousPerspective = PerspectiveHelper.currentPerspectiveId;
			try {
				PerspectiveHelper.currentPerspectiveId = PerspectiveHelper.PERSPECTIVE_SIMULATION_ID;
				try {
					helper.launch("First");
					throw new AssertionError("Choosing another experiment outside Home must be rejected");
				} catch (final IllegalStateException expected) {}
			} finally {
				PerspectiveHelper.currentPerspectiveId = previousPerspective;
			}
			final var view = new LauncherHomeView();
			view.createPartControl(shell);
			final var scroll = (org.eclipse.swt.custom.ScrolledComposite) shell.getChildren()[0];
			final var content = (org.eclipse.swt.widgets.Composite) scroll.getContent();
			final var center = (Composite) content.getChildren()[0];
			final var presentation = (LaunchPresentation) center.getChildren()[0];
			final var labels = java.util.Arrays.stream(presentation.getChildren())
					.filter(org.eclipse.swt.widgets.Label.class::isInstance)
					.map(org.eclipse.swt.widgets.Label.class::cast).map(org.eclipse.swt.widgets.Label::getText).toList();
			check(labels.containsAll(java.util.List.of("A model", "Description")),
					"Home must display its presentation");
			check(java.util.Arrays.stream(presentation.getChildren()).filter(FlatButton.class::isInstance).count()
					== (singleExperiment ? 0 : 1), "Only multiple experiments need the shared selector");
			final var play = java.util.Arrays.stream(presentation.getChildren()).filter(ToolBar.class::isInstance)
					.map(ToolBar.class::cast).findFirst().orElseThrow();
			check(play.getItemCount() == 1 && play.getItem(0).getImage() != null,
					"Home must have one play button, not one button per experiment");
			final Label title = (Label) presentation.getChildren()[0];
			check(title.getFont().getFontData()[0].getHeight() == 20
					&& (title.getStyle() & SWT.CENTER) != 0, "Home title must match the centered overlay heading");
			final var buttons = java.util.Arrays.stream(center.getChildren()).filter(Button.class::isInstance)
					.map(Button.class::cast).map(Button::getText).toList();
			check(buttons.equals(java.util.List.of("Save Simulation Data...")), "Retain artifact saving");
			view.setFocus();
			view.dispose();
		} finally {
			shell.dispose();
			configuration.set(helper, previousConfiguration);
			selections.set(helper, previousSelections);
		}
	}

	private static void checkDialog(final boolean hasDefault, final boolean showHome) throws Exception {
		final var dialog = new ExportModelDialog(new String[] { "First", "Second" }, "Model.gaml", "Model description");
		try {
			dialog.create();
			dialog.buttonPressed(org.eclipse.jface.dialogs.IDialogConstants.CLIENT_ID);
			check(dialog.getFormat() == ExportModelDialog.Format.STANDALONE, "Reach application export through the wizard button");
			check(field(dialog, "title", Text.class).getText().equals("Model.gaml"), "Prefill the model filename");
			check(field(dialog, "description", Text.class).getText().equals("Model description"),
					"Prefill the leading model documentation");
			final Table experiments = field(dialog, "experiments", Table.class);
			final Button home = field(dialog, "showHomeButton", Button.class);
			check(home.getSelection() && !home.isEnabled(), "Home is mandatory without a default");
			check(experiments.getItemCount() == 2 && experiments.getSelectionCount() == 2,
					"Initially export all runnable experiments");
			final String destination = java.nio.file.Path.of(System.getProperty("java.io.tmpdir"), "test-launcher.zip")
					.toString();
			field(dialog, "destination", Text.class).setText(destination);
			experiments.setSelection(1);
			if (hasDefault) {
				final var firstCell = (Composite) experiments.getChildren()[0];
				final Button first = (Button) firstCell.getChildren()[0];
				first.setSelection(true);
				first.notifyListeners(SWT.Selection, null);
				check(home.isEnabled(), "Home option must be independent when a default is selected");
				check(experiments.isSelected(0), "Choosing a default must include it in the export");
				experiments.setSelection(1);
				experiments.notifyListeners(SWT.Selection, null);
				check(home.getSelection() && !home.isEnabled(),
						"Deselecting the default restores mandatory Home");
				final var secondCell = (Composite) experiments.getChildren()[1];
				final Button second = (Button) secondCell.getChildren()[0];
				second.setSelection(true);
				second.notifyListeners(SWT.Selection, null);
				check(!first.getSelection() && second.getSelection(), "Only one experiment can be the default");
				home.setSelection(showHome);
			}
			field(dialog, "title", Text.class).setText("Custom title");
			field(dialog, "description", Text.class).setText("Description");
			dialog.okPressed();
			check(dialog.getShell() == null || dialog.getShell().isDisposed(), "OK must close the dialog");
			final var configuration = dialog.getLauncherConfiguration("Default", hasDefault ? "Second" : "");
			check(configuration.title().equals("Custom title") && configuration.description().equals("Description"),
					"Presentation settings must survive SWT disposal");
			check(configuration.showHome() == showHome
					&& configuration.startupExperiment().equals(hasDefault ? "Second" : ""),
					"Startup mode must survive SWT disposal");
			check(dialog.getStartupExperiment().equals(hasDefault ? "Second" : ""),
					"Default experiment must survive SWT disposal");
			check(dialog.getSelectedExperiments().length == 1
					&& dialog.getSelectedExperiments()[0].equals("Second"), "Export only the selected rows");
			check(dialog.getIncludeJdk(), "Preserve application exporter options after switching from a package");
			check(java.nio.file.Path.of(dialog.getOutputPath(), dialog.getOutputFileName()).toString()
					.equals(destination), "Destination combines filename and path");
		} finally {
			dialog.close();
		}
	}

	private static void checkScrollableExperiments() throws Exception {
		final String[] names = java.util.stream.IntStream.range(0, 100).mapToObj(i -> "Experiment " + i)
				.toArray(String[]::new);
		final var dialog = new ExportModelDialog(names);
		try {
			dialog.create();
			final Table table = field(dialog, "experiments", Table.class);
			check((table.getStyle() & SWT.MULTI) != 0 && table.getVerticalBar() != null,
					"Experiments must form a real scrollable multi-selection list");
			check(((org.eclipse.swt.layout.GridData) table.getLayoutData()).heightHint == 170,
					"The list height must not grow with the number of experiments");
			table.deselectAll();
			findButton(dialog.getShell(), "Select all").notifyListeners(SWT.Selection, null);
			check(table.getSelectionCount() == 100, "Select all must select every experiment");
		} finally {
			dialog.close();
		}
	}

	private static Button findButton(final Composite parent, final String text) {
		for (final Control child : parent.getChildren()) {
			if (child instanceof Button button && button.getText().equals(text)) return button;
			if (child instanceof Composite composite) {
				final Button found = findButton(composite, text);
				if (found != null) return found;
			}
		}
		return null;
	}

	private static void checkSharedMenu(final Display display) {
		final Shell shell = new Shell(display);
		try {
			final String[] selected = { "" };
			final Button owner = new Button(shell, SWT.PUSH);
			final var menu = ExperimentMenu.create(owner, java.util.List.of(
					new ExperimentMenu.Entry("full-name", "Display label", "batch")), name -> selected[0] = name);
			menu.getItem(0).notifyListeners(SWT.Selection, null);
			check(selected[0].equals("full-name"), "The shared menu must return the name, not the label");
			owner.dispose();
			check(menu.isDisposed(), "The shared experiment menu must follow its owner's lifetime");
		} finally {
			shell.dispose();
		}
	}
}
