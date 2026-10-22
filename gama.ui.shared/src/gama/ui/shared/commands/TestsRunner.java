/*******************************************************************************************************
 *
 * TestsRunner.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.Job;

import gama.api.GAMA;
import gama.api.runtime.scope.IScope;
import gama.api.kernel.species.IModelSpecies;
import gama.api.ui.IGui;
import gama.api.utils.prefs.GamaPreferences;
import gama.api.utils.tests.CompoundSummary;
import gama.api.utils.tests.TestExperimentSummary;
import gama.api.utils.tests.IsolatedTestRunner;
import gama.api.utils.tests.TestExperimentRunner;
import gama.dev.DEBUG;
import gama.ui.shared.access.ModelsFinder;
import gama.ui.shared.utils.SwtGui;
import gama.workspace.manager.ProjectCategory;

/**
 * The Class TestsRunner.
 */
public class TestsRunner {

	/** The last run. */
	public static CompoundSummary<TestExperimentSummary, ?> LAST_RUN;

	private static final AtomicBoolean RUNNING = new AtomicBoolean();

	public static void start(final IModelSpecies model, final String experiment) {
		if (!RUNNING.compareAndSet(false, true)) return;
		final IGui gui = GAMA.getRegularGui();
		final IScope scope = GAMA.getRuntimeScope();
		gui.openTestView(scope, false);
		Job.createSystem("Test " + experiment, m -> {
			try {
				gui.displayTestCounts(0, TestExperimentRunner.countTests(model, experiment));
				final var result = TestExperimentRunner.run(model, experiment, gui);
				gui.displayTestsResults(scope, result);
			} catch (final RuntimeException e) {
				DEBUG.ERR("Unable to start test experiment " + experiment, e);
				gui.getDialogFactory().error("Unable to start test experiment: " + e.getMessage());
			} finally {
				try {
					gui.endTestDisplay();
				} finally {
					RUNNING.set(false);
				}
			}
		}).schedule();
	}

	/**
	 * Start.
	 */
	public static void start() {
		if (!RUNNING.compareAndSet(false, true)) return;

		LAST_RUN = new CompoundSummary<>();

		final IGui gui = GAMA.getRegularGui();
		final IScope scope = GAMA.getRuntimeScope();
		gui.openTestView(scope, true);
		final List<IFile> testFiles;
		try {
			testFiles = findTestModels();
		} catch (CoreException e) {
			DEBUG.ERR("Unable to find test models", e);
			SwtGui.ALL_TESTS_RUNNING = false;
			RUNNING.set(false);
			gui.endTestDisplay();
			return;
		}
		int size = testFiles.size();
		int[] i = { 1 };

		Job.createSystem("All tests", m -> {
			try (var session = IsolatedTestRunner.openSession()) {
				int totalTests = 0;
				for (final IFile file : testFiles) {
					if (m.isCanceled() || gui.isTestStopRequested()) return;
					gui.displayRunningTest(scope, "Discovering tests in " + file.getProjectRelativePath());
					totalTests = Math.addExact(totalTests, gui.getModelsManager().countHeadlessTests(file));
				}
				gui.displayTestCounts(0, totalTests);
				for (final IFile file : testFiles) {
					if (m.isCanceled() || gui.isTestStopRequested() || Thread.currentThread().isInterrupted()) break;
					gui.displayTestsProgress(scope, i[0]++, size);
					gui.displayRunningTest(scope, file.getProjectRelativePath().toString());
					final List<TestExperimentSummary> list = gui.getModelsManager().runHeadlessTests(file);
					if (list != null) { LAST_RUN.addSummaries(list); }
				}
			} catch (final RuntimeException e) {
				DEBUG.ERR("Unable to discover or run tests", e);
				gui.getDialogFactory().error("Unable to discover or run tests: " + e.getMessage());
			} finally {
				try {
					gui.displayTestsResults(scope, LAST_RUN);
				} finally {
					SwtGui.ALL_TESTS_RUNNING = false;
					try {
						gui.endTestDisplay();
					} finally {
						RUNNING.set(false);
					}
				}
			}
		}).schedule();
	}

	/**
	 * Find test models.
	 *
	 * @return the list
	 * @throws CoreException
	 *             the core exception
	 */
	private static List<IFile> findTestModels() throws CoreException {
		final List<IFile> result = new ArrayList<>();
		final IWorkspaceRoot w = GAMA.getWorkspaceManager().getRoot();
		for (final IProject p : w.getProjects()) {
			if (isInteresting(p)) { result.addAll(ModelsFinder.getAllGamaFilesInProject(p)); }
		}
		return result;

	}

	/**
	 * Checks if is interesting.
	 *
	 * @param p
	 *            the p
	 * @return true, if is interesting
	 * @throws CoreException
	 *             the core exception
	 */
	private static boolean isInteresting(final IProject p) throws CoreException {
		if (p == null || !p.exists() || !p.isAccessible()) return false;
		final ProjectCategory category = ProjectCategory.of(p);
		// If it is a test project (of a plugin or located in the Tests directory of the workspace), return true
		if (category == ProjectCategory.TESTS) return true;
		if (GamaPreferences.Runtime.USER_TESTS.getValue()) {
			// If it is not in user defined projects, return false
			if (category != ProjectCategory.USER) return false;
			// We try to find in the project a folder called 'tests'
			final IResource r = p.findMember("tests");
			if (r != null && r.exists() && r.isAccessible() && r.getType() == IResource.FOLDER) return true;
		}
		return false;
	}

}
