/*******************************************************************************************************
 *
 * ProcessorWarningResolutionGenerator.java, in gama.ui.devtools, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.devtools.quickfix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator2;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.views.markers.WorkbenchMarkerResolution;

import gama.ui.devtools.util.ResourceUtils;

/**
 * Quick fixes for the warnings of the GAML annotation processor ("documentation missing for ...", "operator '...' is
 * not tested"). They are available in the Problems view (where they can be applied to many markers at once) and in
 * the Java editor.
 */
public class ProcessorWarningResolutionGenerator implements IMarkerResolutionGenerator2 {

	@Override
	public boolean hasResolutions(final IMarker marker) {
		return ProcessorWarning.of(marker) != null;
	}

	@Override
	public IMarkerResolution[] getResolutions(final IMarker marker) {
		ProcessorWarning w = ProcessorWarning.of(marker);
		if (w == null) { return new IMarkerResolution[0]; }
		List<IMarkerResolution> result = new ArrayList<>();
		for (JavaSourceFixer.Fix fix : JavaSourceFixer.Fix.values()) {
			if (fix.target == w.kind()) { result.add(new SourceResolution(marker, fix)); }
		}
		if (w.kind() == ProcessorWarning.Kind.UNTESTED) { result.add(new StubResolution(marker)); }
		return result.toArray(IMarkerResolution[]::new);
	}

	/** Returns the markers that designate the same kind of processor warning. */
	static IMarker[] similar(final IMarker original, final IMarker[] markers, final ProcessorWarning.Kind kind) {
		return Arrays.stream(markers).filter(m -> m != original && ProcessorWarning.of(m) != null
				&& ProcessorWarning.of(m).kind() == kind).toArray(IMarker[]::new);
	}

	/** Modifies the Java source. */
	static class SourceResolution extends WorkbenchMarkerResolution {

		private final IMarker original;

		private final JavaSourceFixer.Fix fix;

		SourceResolution(final IMarker marker, final JavaSourceFixer.Fix fix) {
			this.original = marker;
			this.fix = fix;
		}

		@Override
		public String getLabel() { return fix.label; }

		@Override
		public String getDescription() { return fix.description; }

		@Override
		public Image getImage() { return null; }

		@Override
		public IMarker[] findOtherMarkers(final IMarker[] markers) {
			return similar(original, markers, fix.target);
		}

		@Override
		public void run(final IMarker marker) {
			run(new IMarker[] { marker }, null);
		}

		@Override
		public void run(final IMarker[] markers, final IProgressMonitor monitor) {
			try {
				JavaSourceFixer.apply(fix, List.of(markers), monitor);
			} catch (CoreException e) {
				ResourceUtils.log(e);
			}
		}

	}

	/** Generates GAML test stubs in the {@code models/Tests} folder of the plugin. */
	static class StubResolution extends WorkbenchMarkerResolution {

		private final IMarker original;

		StubResolution(final IMarker marker) {
			this.original = marker;
		}

		@Override
		public String getLabel() { return "Generate a GAML test stub in " + TestStubGenerator.PATH; }

		@Override
		public String getDescription() {
			return "Appends a test stub for the operator to the 'Untested Operators' test model of the plugin";
		}

		@Override
		public Image getImage() { return null; }

		@Override
		public IMarker[] findOtherMarkers(final IMarker[] markers) {
			return similar(original, markers, ProcessorWarning.Kind.UNTESTED);
		}

		@Override
		public void run(final IMarker marker) {
			run(new IMarker[] { marker }, null);
		}

		@Override
		public void run(final IMarker[] markers, final IProgressMonitor monitor) {
			Set<IProject> projects = new LinkedHashSet<>();
			for (IMarker m : markers) { projects.add(m.getResource().getProject()); }
			try {
				List<IFile> files = TestStubGenerator.generate(new ArrayList<>(projects), List.of(markers), monitor);
				if (files.size() == 1) {
					PlatformUI.getWorkbench().getDisplay().asyncExec(() -> {
						try {
							IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
									files.get(0));
						} catch (Exception e) {
							ResourceUtils.log(e);
						}
					});
				}
			} catch (CoreException e) {
				ResourceUtils.log(e);
			}
		}

	}

}
