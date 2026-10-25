/*******************************************************************************************************
 *
 * NewGamlTestModelWizard.java, in gama.ui.devtools, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.devtools.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.ide.IDE;

import gama.ui.devtools.templates.CodeTemplates;
import gama.ui.devtools.util.GamaProjects;
import gama.ui.devtools.util.ResourceUtils;

/**
 * Creates a GAML test model in the {@code models/Tests} folder of a GAMA plugin.
 */
public class NewGamlTestModelWizard extends Wizard implements INewWizard {

	/** The selection. */
	private IStructuredSelection selection;

	/** The page. */
	private Page page;

	@Override
	public void init(final IWorkbench workbench, final IStructuredSelection sel) {
		this.selection = sel;
		setWindowTitle("New GAML Test Model");
		setNeedsProgressMonitor(true);
	}

	@Override
	public void addPages() {
		page = new Page();
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		IProject project = page.project();
		String name = CodeTemplates.gamlIdentifier(page.name.getText().strip());
		String feature = page.feature.getText().strip();
		IFile[] created = new IFile[1];
		try {
			getContainer().run(true, false, new WorkspaceModifyOperation() {
				@Override
				protected void execute(final IProgressMonitor monitor) throws CoreException {
					created[0] = ResourceUtils.write(project, "models/Tests/" + name + ".gaml", CodeTemplates.gamlTestModel(name,
							feature.isEmpty() ? name : feature), monitor);
				}
			});
			IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), created[0]);
		} catch (InvocationTargetException e) {
			page.setErrorMessage("Error: " + e.getCause());
			ResourceUtils.log(e.getCause());
			return false;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return false;
		} catch (PartInitException e) {
			ResourceUtils.log(e);
		}
		return true;
	}

	/** The single page. */
	private class Page extends WizardPage {

		/** Widgets. */
		Combo projects;
		Text name, feature;
		private List<IProject> gamaProjects;

		Page() {
			super("model");
			setTitle("New GAML Test Model");
			setDescription("Creates a model with a 'test' experiment in models/Tests, run by the GAMA test runner.");
		}

		IProject project() {
			int i = projects.getSelectionIndex();
			return i < 0 ? null : gamaProjects.get(i);
		}

		String path() {
			return "models/Tests/" + CodeTemplates.gamlIdentifier(name.getText().strip()) + ".gaml";
		}

		@Override
		public void createControl(final Composite parent) {
			Composite c = new Composite(parent, SWT.NONE);
			c.setLayout(new GridLayout(2, false));
			new Label(c, SWT.NONE).setText("GAMA plugin:");
			projects = new Combo(c, SWT.READ_ONLY);
			projects.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			gamaProjects = GamaProjects.all();
			gamaProjects.forEach(p -> projects.add(p.getName()));
			int i = gamaProjects.indexOf(GamaProjects.firstProject(selection));
			if (i >= 0) { projects.select(i); }
			new Label(c, SWT.NONE).setText("Model name:");
			name = new Text(c, SWT.BORDER);
			name.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			name.setText("MyTests");
			new Label(c, SWT.NONE).setText("Tested feature:");
			feature = new Text(c, SWT.BORDER);
			feature.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			feature.setMessage("e.g. the name of an operator");
			projects.addListener(SWT.Selection, e -> validate());
			name.addListener(SWT.Modify, e -> validate());
			setControl(c);
			validate();
		}

		private void validate() {
			setErrorMessage(null);
			setPageComplete(false);
			IProject p = project();
			if (p == null) {
				setErrorMessage("Choose a GAMA plugin");
			} else if (name.getText().isBlank()) {
				setErrorMessage("Enter the name of the model");
			} else if (p.getFile(path()).exists()) {
				setErrorMessage(path() + " already exists");
			} else {
				setPageComplete(true);
			}
		}
	}

}
