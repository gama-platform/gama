/*******************************************************************************************************
 *
 * UpdateJavaHeadersHandler.java, in gama.ui.devtools, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.devtools.handlers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import gama.ui.devtools.preferences.JavaHeaderSettings;
import gama.ui.devtools.util.GamaProjects;
import gama.ui.devtools.util.JavaHeaderText;

/** Explicit, previewable and undoable header maintenance, independent of building. */
public class UpdateJavaHeadersHandler extends AbstractHandler {

	@Override
	public Object execute(final ExecutionEvent event) {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		if (window == null) { return null; }
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		List<IProject> selected = GamaProjects.fromSelection(selection);
		HeaderRefactoring refactoring = new HeaderRefactoring();
		RefactoringWizard wizard = new RefactoringWizard(refactoring,
				RefactoringWizard.DIALOG_BASED_USER_INTERFACE | RefactoringWizard.PREVIEW_EXPAND_FIRST_NODE) {
			@Override
			protected void addUserInputPages() {
				addPage(new HeaderPage(refactoring, selected));
			}

			@Override
			public boolean performFinish() {
				boolean finished = super.performFinish();
				if (finished) { JavaHeaderSettings.save(refactoring.version, refactoring.year); }
				return finished;
			}
		};
		wizard.setDefaultPageTitle("Update Java Headers");
		wizard.setForcePreviewReview(true);
		try {
			new RefactoringWizardOpenOperation(wizard).run(window.getShell(), "Update Java Headers");
		} catch (InterruptedException e) {
			return null;
		}
		return null;
	}

	private static final class HeaderPage extends UserInputWizardPage {

		private final HeaderRefactoring refactoring;
		private final List<IProject> selected;

		HeaderPage(final HeaderRefactoring refactoring, final List<IProject> selected) {
			super("headerSettings");
			this.refactoring = refactoring;
			this.selected = selected;
			setDescription("Update existing GAMA headers and add missing ones. Existing author and license notices "
					+ "are preserved. Review the changes on the next page.");
		}

		@Override
		public void createControl(final Composite parent) {
			Composite content = new Composite(parent, SWT.NONE);
			content.setLayout(new GridLayout(2, false));
			new Label(content, SWT.NONE).setText("GAMA version:");
			Text version = new Text(content, SWT.BORDER);
			version.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			version.setText(refactoring.version);
			new Label(content, SWT.NONE).setText("Copyright end year:");
			Text year = new Text(content, SWT.BORDER);
			year.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			year.setText(refactoring.year);
			new Label(content, SWT.NONE).setText("Scope:");
			Combo scope = new Combo(content, SWT.READ_ONLY);
			scope.setItems("All open GAMA plugins", "Selected GAMA plugins (" + selected.size() + ")");
			scope.select(0);
			Runnable validate = () -> {
				refactoring.version = version.getText().strip();
				refactoring.year = year.getText().strip();
				refactoring.selected = scope.getSelectionIndex() == 1 ? selected : null;
				String error = JavaHeaderText.validate(refactoring.version, refactoring.year);
				if (error == null && refactoring.selected != null && selected.isEmpty()) {
					error = "Select GAMA projects in the workspace, or use all open GAMA plugins.";
				}
				setErrorMessage(error);
				setPageComplete(error == null);
			};
			version.addListener(SWT.Modify, e -> validate.run());
			year.addListener(SWT.Modify, e -> validate.run());
			scope.addListener(SWT.Selection, e -> validate.run());
			setControl(content);
			validate.run();
		}
	}

	private static final class HeaderRefactoring extends Refactoring {

		String version = JavaHeaderSettings.version();
		String year = JavaHeaderSettings.year();
		List<IProject> selected;
		CompositeChange changes;

		@Override
		public String getName() {
			return "Update GAMA Java Headers";
		}

		@Override
		public RefactoringStatus checkInitialConditions(final IProgressMonitor monitor) {
			return new RefactoringStatus();
		}

		@Override
		public RefactoringStatus checkFinalConditions(final IProgressMonitor monitor) throws CoreException {
			String error = JavaHeaderText.validate(version, year);
			if (error != null) { return RefactoringStatus.createFatalErrorStatus(error); }
			if (changes != null) { changes.dispose(); }
			changes = new CompositeChange(getName());
			List<IProject> projects = selected == null ? GamaProjects.all() : selected;
			SubMonitor sub = SubMonitor.convert(monitor, "Checking Java headers", projects.size());
			Set<IFile> seen = new HashSet<>();
			RefactoringStatus status = new RefactoringStatus();
			for (IProject project : projects) {
				sub.split(1);
				for (IPackageFragmentRoot root : GamaProjects.java(project).getPackageFragmentRoots()) {
					sub.checkCanceled();
					if (root.getKind() != IPackageFragmentRoot.K_SOURCE || root.getResource() == null
							|| !root.getResource().getProject().equals(project)
							|| root.getResource().isDerived(org.eclipse.core.resources.IResource.CHECK_ANCESTORS)
							|| project.getFullPath().append(GamaProjects.GEN_SRC_DIR).isPrefixOf(root.getPath())) {
						continue;
					}
					for (IJavaElement element : root.getChildren()) {
						if (!(element instanceof IPackageFragment fragment)) { continue; }
						for (var unit : fragment.getCompilationUnits()) {
							sub.checkCanceled();
							if (unit.getResource() instanceof IFile file && seen.add(file)
									&& !project.getFullPath().append(GamaProjects.GEN_SRC_DIR).isPrefixOf(file.getFullPath())
									&& !file.isDerived(org.eclipse.core.resources.IResource.CHECK_ANCESTORS)) {
								addChange(file, sub, status);
							}
						}
					}
				}
			}
			if (changes.getChildren().length == 0 && !status.hasFatalError()) {
				status.addFatalError("No headers need updating in the chosen GAMA plugins.");
			}
			return status;
		}

		private void addChange(final IFile file, final IProgressMonitor monitor, final RefactoringStatus status)
				throws CoreException {
			TextFileChange change = new TextFileChange(file.getFullPath().toPortableString(), file);
			change.setTextType("java");
			change.setSaveMode(TextFileChange.KEEP_SAVE_STATE);
			String before = change.getCurrentContent(monitor);
			String after;
			try {
				after = JavaHeaderText.update(before, file.getName(), file.getProject().getName(), version, year);
			} catch (IllegalArgumentException e) {
				status.addFatalError(file.getFullPath() + ": " + e.getMessage());
				change.dispose();
				return;
			}
			if (before.equals(after)) {
				change.dispose();
				return;
			}
			int start = 0;
			int endBefore = before.length();
			int endAfter = after.length();
			while (start < endBefore && start < endAfter && before.charAt(start) == after.charAt(start)) { start++; }
			while (endBefore > start && endAfter > start
					&& before.charAt(endBefore - 1) == after.charAt(endAfter - 1)) {
				endBefore--;
				endAfter--;
			}
			change.setEdit(new ReplaceEdit(start, endBefore - start, after.substring(start, endAfter)));
			changes.add(change);
		}

		@Override
		public Change createChange(final IProgressMonitor monitor) {
			return changes;
		}
	}
}
