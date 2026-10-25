/*******************************************************************************************************
 *
 * RepairMarkerResolutionGenerator.java, in gama.ui.devtools, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.devtools.config;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator2;
import org.eclipse.ui.views.markers.WorkbenchMarkerResolution;

/**
 * Provides the "Repair GAMA plugin configuration" quick fix on the configuration markers.
 */
public class RepairMarkerResolutionGenerator implements IMarkerResolutionGenerator2 {

	@Override
	public boolean hasResolutions(final IMarker marker) {
		ConfigurationIssue issue = PluginConfigurationChecker.fromMarker(marker);
		return issue != null && issue.kind().fixable;
	}

	@Override
	public IMarkerResolution[] getResolutions(final IMarker marker) {
		return hasResolutions(marker) ? new IMarkerResolution[] { new RepairResolution(marker) }
				: new IMarkerResolution[0];
	}

	/**
	 * Repairs the issue (and, when several markers are selected, all the similar ones).
	 */
	static class RepairResolution extends WorkbenchMarkerResolution {

		private final IMarker original;

		RepairResolution(final IMarker marker) {
			this.original = marker;
		}

		@Override
		public String getLabel() { return "Repair GAMA plugin configuration"; }

		@Override
		public String getDescription() {
			return "Automatically fixes the configuration of the plugin so that the GAML annotation processor runs correctly";
		}

		@Override
		public Image getImage() { return null; }

		@Override
		public IMarker[] findOtherMarkers(final IMarker[] markers) {
			List<IMarker> result = new ArrayList<>();
			for (IMarker m : markers) {
				if (m != original && PluginConfigurationChecker.fromMarker(m) != null
						&& PluginConfigurationChecker.fromMarker(m).kind().fixable) {
					result.add(m);
				}
			}
			return result.toArray(IMarker[]::new);
		}

		@Override
		public void run(final IMarker marker) {
			run(new IMarker[] { marker }, null);
		}

		@Override
		public void run(final IMarker[] markers, final IProgressMonitor unused) {
			List<ConfigurationIssue> issues = new ArrayList<>();
			for (IMarker m : markers) {
				ConfigurationIssue i = PluginConfigurationChecker.fromMarker(m);
				if (i != null) { issues.add(i); }
			}
			WorkspaceJob job = new WorkspaceJob("Repairing GAMA plugins configuration") {
				@Override
				public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
					PluginConfigurationChecker.repair(issues, monitor);
					List<IProject> projects = issues.stream().map(ConfigurationIssue::project).distinct().toList();
					PluginConfigurationChecker.updateMarkers(projects, false,
							PluginConfigurationChecker.check(projects, monitor).stream()
									.filter(i -> projects.contains(i.project())).toList());
					return Status.OK_STATUS;
				}
			};
			job.setRule(org.eclipse.core.resources.ResourcesPlugin.getWorkspace().getRoot());
			job.schedule();
		}

	}

}
