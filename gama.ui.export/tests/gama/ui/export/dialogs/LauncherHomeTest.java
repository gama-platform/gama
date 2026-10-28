package gama.ui.export.dialogs;

import java.lang.reflect.Field;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import gama.export.LauncherConfiguration;
import gama.ui.application.workbench.PerspectiveHelper;
import gama.ui.export.launcher.LauncherHomePerspective;
import gama.ui.export.launcher.LauncherHomeView;
import gama.ui.export.launcher.StartupModelHelper;

/** Standalone SWT checks; use -XstartOnFirstThread on macOS. */
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
				.equals("Run    from model    project/model.gaml"), "Contextual experiment labels");

		final Display display = new Display();
		try {
			checkDialog(false, true);
			checkDialog(true, false);
			checkDialog(true, true);
			checkHomeView(display, false);
			checkHomeView(display, true);
		} finally {
			display.dispose();
		}
		System.out.println("Launcher home routing and export dialog checks passed");
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
			final var labels = java.util.Arrays.stream(content.getChildren())
					.filter(org.eclipse.swt.widgets.Label.class::isInstance)
					.map(org.eclipse.swt.widgets.Label.class::cast).map(org.eclipse.swt.widgets.Label::getText).toList();
			check(labels.containsAll(java.util.List.of("A model", "Description")),
					"Home must display its presentation");
			check(labels.contains("Choose an experiment") != singleExperiment,
					"Only multiple experiments need a choice prompt");
			final var buttons = java.util.Arrays.stream(content.getChildren()).filter(Button.class::isInstance)
					.map(Button.class::cast).map(Button::getText).toList();
			check(buttons.equals(singleExperiment ? java.util.List.of("Run", "Save Simulation Data...")
					: java.util.List.of("First", "Second", "Save Simulation Data...")),
					"Home must expose every exported experiment and artifact saving");
			view.setFocus();
			view.dispose();
		} finally {
			shell.dispose();
			configuration.set(helper, previousConfiguration);
			selections.set(helper, previousSelections);
		}
	}

	private static void checkDialog(final boolean hasDefault, final boolean showHome) throws Exception {
		final var dialog = new ExportModelDialog(new String[] { "First", "Second" });
		try {
			dialog.create();
			final Combo startup = field(dialog, "startupExperiment", Combo.class);
			final Button home = field(dialog, "showHomeButton", Button.class);
			check(home.getSelection() && !home.isEnabled(), "Home is mandatory without a default");
			check(startup.getSelectionIndex() == 0 && startup.isEnabled(), "No default must mean Home-first");
			check(startup.getItemCount() == 3, "The default selector must include None and all experiments");
			field(dialog, "txtOutputPath", Text.class).setText(System.getProperty("java.io.tmpdir"));
			final Button[] experiments = field(dialog, "buttons", Button[].class);
			experiments[1].setSelection(true);
			if (hasDefault) {
				startup.select(1);
				startup.notifyListeners(SWT.Selection, null);
				check(home.isEnabled(), "Home option must be independent when a default is selected");
				dialog.okPressed();
				check(!dialog.getShell().isDisposed(), "An unexported startup experiment must be rejected");
				startup.select(2);
				startup.notifyListeners(SWT.Selection, null);
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
		} finally {
			dialog.close();
		}
	}
}
