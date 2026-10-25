/*******************************************************************************************************
 *
 * CleanAndRebuildJob.java, in gama.ui.devtools, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.devtools.jobs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.core.IJavaModelMarker;

import gama.ui.devtools.config.ConfigurationIssue;
import gama.ui.devtools.config.PluginConfigurationChecker;
import gama.ui.devtools.preferences.DevToolsPreferences;
import gama.ui.devtools.util.GamaProjects;
import gama.ui.devtools.util.Notifier;

/**
 * Resets the GAML annotation processing of a set of GAMA plugins and rebuilds them, without any dialog. This
 * replaces the usual manual recipe (deleting the 'gaml' folder, closing/reopening the projects, disabling and
 * re-enabling annotation processing, cleaning and rebuilding):
 * <ol>
 * <li>optionally repairs the configuration of the plugins (see {@link PluginConfigurationChecker});</li>
 * <li>disables annotation processing on the plugins;</li>
 * <li>deletes their 'gaml' folders;</li>
 * <li>re-enables annotation processing, which resets the generated source folder managers of JDT APT;</li>
 * <li>cleans and fully rebuilds the plugins (or the whole workspace, except Maven-only projects like
 * gama.parent);</li>
 * <li>verifies that every plugin got its {@code GamlAdditions} and reports the result in a notification.</li>
 * </ol>
 */
public class CleanAndRebuildJob extends WorkspaceJob {

	private final List<IProject> targets;

	/**
	 * @param targets
	 *            the GAMA plugins to reset
	 */
	public CleanAndRebuildJob(final List<IProject> targets) {
		super("Clean and rebuild GAMA plugins");
		this.targets = targets;
		setRule(ResourcesPlugin.getWorkspace().getRoot());
		setUser(true);
	}

	@Override
	public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
		long start = System.currentTimeMillis();
		SubMonitor sub = SubMonitor.convert(monitor, getName(), 100);
		try {
			if (DevToolsPreferences.is(DevToolsPreferences.REPAIR_BEFORE_CLEAN)) {
				sub.subTask("Repairing configuration");
				List<ConfigurationIssue> issues = PluginConfigurationChecker.check(targets, sub.split(3));
				PluginConfigurationChecker.repair(issues, sub.split(2));
			}
			sub.setWorkRemaining(95);
			sub.subTask("Resetting annotation processing");
			for (IProject p : targets) { AptConfig.setEnabled(GamaProjects.java(p), false); }
			SubMonitor del = sub.split(5).setWorkRemaining(targets.size());
			for (IProject p : targets) {
				IFolder gaml = p.getFolder(GamaProjects.GEN_SRC_DIR);
				if (gaml.exists()) { gaml.delete(IResource.FORCE, del.split(1)); }
			}
			for (IProject p : targets) { AptConfig.setEnabled(GamaProjects.java(p), true); }

			IBuildConfiguration[] configs = buildConfigurations();
			sub.subTask("Cleaning");
			ResourcesPlugin.getWorkspace().build(configs, IncrementalProjectBuilder.CLEAN_BUILD, true, sub.split(10));
			sub.subTask("Building");
			ResourcesPlugin.getWorkspace().build(configs, IncrementalProjectBuilder.FULL_BUILD, true, sub.split(70));

			List<String> missing = new ArrayList<>();
			for (IProject p : targets) {
				if (!p.getFile(GamaProjects.generatedAdditionsPath(p)).exists()) { missing.add(p.getName()); }
			}
			int errors = countErrors(configs);
			if (DevToolsPreferences.is(DevToolsPreferences.CHECK_AFTER_REBUILD)) {
				PluginConfigurationChecker.updateMarkers(targets, true,
						PluginConfigurationChecker.check(targets, sub.split(10)));
			}
			report(missing, errors, (System.currentTimeMillis() - start) / 1000);
			return Status.OK_STATUS;
		} catch (OperationCanceledException e) {
			// Make sure annotation processing is not left disabled
			for (IProject p : targets) { AptConfig.setEnabled(GamaProjects.java(p), true); }
			return Status.CANCEL_STATUS;
		}
	}

	private IBuildConfiguration[] buildConfigurations() throws CoreException {
		List<IBuildConfiguration> configs = new ArrayList<>();
		List<IProject> projects = DevToolsPreferences.is(DevToolsPreferences.REBUILD_WORKSPACE)
				? List.of(ResourcesPlugin.getWorkspace().getRoot().getProjects()) : targets;
		for (IProject p : projects) {
			if (p.isOpen() && !GamaProjects.isMavenOnly(p)) { configs.add(p.getActiveBuildConfig()); }
		}
		return configs.toArray(IBuildConfiguration[]::new);
	}

	private static int countErrors(final IBuildConfiguration[] configs) throws CoreException {
		int errors = 0;
		for (IBuildConfiguration c : configs) {
			for (IMarker m : c.getProject().findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true,
					IResource.DEPTH_INFINITE)) {
				if (m.getAttribute(IMarker.SEVERITY, -1) == IMarker.SEVERITY_ERROR) { errors++; }
			}
		}
		return errors;
	}

	private void report(final List<String> missing, final int errors, final long seconds) {
		StringBuilder sb = new StringBuilder();
		sb.append(targets.size()).append(" GAMA plugin(s) reset and rebuilt in ").append(seconds).append("s.");
		if (missing.isEmpty()) {
			sb.append("\nAll GAML additions have been generated.");
		} else {
			sb.append("\nGAML additions missing for: ").append(String.join(", ", missing))
					.append(". Use 'Check GAMA Plugins Configuration'.");
		}
		if (errors > 0) { sb.append("\n").append(errors).append(" Java compilation error(s) remain."); }
		Notifier.show(missing.isEmpty() && errors == 0 ? "GAMA build succeeded" : "GAMA build finished with problems",
				sb.toString());
	}

}
