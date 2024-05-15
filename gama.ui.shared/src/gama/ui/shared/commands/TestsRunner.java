/*******************************************************************************************************
 *
 * TestsRunner.java, in gama.ui.shared.shared, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.ui.shared.commands;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.Job;

import gama.core.common.interfaces.IGamaView;
import gama.core.common.interfaces.IGui;
import gama.core.common.preferences.GamaPreferences;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.gaml.statements.test.CompoundSummary;
import gama.gaml.statements.test.TestExperimentSummary;
import gama.ui.shared.access.ModelsFinder;
import gama.ui.shared.utils.SwtGui;
import gama.ui.shared.utils.WorkbenchHelper;

/**
 * The Class TestsRunner.
 */
public class TestsRunner {

	/** The last run. */
	public static CompoundSummary<TestExperimentSummary, ?> LAST_RUN;

	/**
	 * Start.
	 */
	public static void start() {
		if (SwtGui.ALL_TESTS_RUNNING)
			return;

		LAST_RUN = new CompoundSummary<>();

		final IGui gui = GAMA.getRegularGui();
		final IScope scope = GAMA.getRuntimeScope();
		IGamaView.Test testView = gui.openTestView(scope, true);
		final List<IFile> testFiles;
		try {
			testFiles = findTestModels();
		} catch (CoreException e) {
			return;
		}
		int size = testFiles.size();
		int[] i = { 1 };

		Job.createSystem("All tests", (m) -> {
			for (final IFile file : testFiles) {
				if (testView != null) {
					testView.displayProgress(i[0]++, size);
				}
				final List<TestExperimentSummary> list = gui.runHeadlessTests(file);
				if (list != null) {
					LAST_RUN.addSummaries(list);
				}
			}
			gui.displayTestsResults(scope, LAST_RUN);
			SwtGui.ALL_TESTS_RUNNING = false;
			gui.endTestDisplay();
		}).schedule();
	}

	/**
	 * Find test models.
	 *
	 * @return the list
	 * @throws CoreException the core exception
	 */
	private static List<IFile> findTestModels() throws CoreException {
		final List<IFile> result = new ArrayList<>();
		final IWorkspaceRoot w = ResourcesPlugin.getWorkspace().getRoot();
		for (final IProject p : w.getProjects()) {
			if (isInteresting(p))
				result.addAll(ModelsFinder.getAllGamaFilesInProject(p));
		}
		return result;

	}

	/**
	 * Checks if is interesting.
	 *
	 * @param p the p
	 * @return true, if is interesting
	 * @throws CoreException the core exception
	 */
	private static boolean isInteresting(final IProject p) throws CoreException {
		if (p == null || !p.exists() || !p.isAccessible())
			return false;
		// If it is contained in one of the built-in tests projects, return true
		if (p.getDescription().hasNature(WorkbenchHelper.TEST_NATURE))
			return true;
		if (GamaPreferences.Runtime.USER_TESTS.getValue()) {
			// If it is not in user defined projects, return false
			if (p.getDescription().hasNature(WorkbenchHelper.BUILTIN_NATURE))
				return false;
			// We try to find in the project a folder called 'tests'
			final IResource r = p.findMember("tests");
			if (r != null && r.exists() && r.isAccessible() && r.getType() == IResource.FOLDER)
				return true;
		}
		return false;
	}

}
