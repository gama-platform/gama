package gaml.compiler.ui.wizards.species;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * Shared shell for detached declaration forms. Adapters explicitly select creation mode and own the undoable apply.
 * Validation reports feedback without accepting a result; Cancel discards only detached values.
 */
public abstract class DeclarationWizard<D> extends TitleAreaDialog {

	public static final int VALIDATE_ID = IDialogConstants.CLIENT_ID + 1;

	private boolean creation;
	private boolean complete;

	protected DeclarationWizard(final Shell shell) {
		super(shell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	/** Must be selected before creating the dialog; never infer creation from a draft's name. */
	public final void setCreationMode(final boolean create) {
		if (getShell() != null) throw new IllegalStateException("Select creation mode before creating the dialog.");
		creation = create;
	}

	public final boolean isCreationMode() { return creation; }

	protected abstract String dialogTitle();

	public abstract Composite createForm(Composite parent, SpeciesWizard.FormHost host);

	public abstract boolean validateNow();

	public abstract boolean accept();

	public abstract D getResult();

	protected abstract void clearResult();

	private SpeciesWizard.FormHost dialogHost() {
		return new SpeciesWizard.FormHost() {
			@Override public void setTitle(final String title) { DeclarationWizard.this.setTitle(title); }
			@Override public void setMessage(final String message) { showFormMessage(message); }
			@Override public void setErrorMessage(final String error) { DeclarationWizard.this.setErrorMessage(error); }
			@Override public void setComplete(final boolean value) {
				complete = value;
				updateButtons();
			}
			@Override public void relayout() { getShell().pack(); }
		};
	}

	protected void showFormMessage(final String message) { setMessage(message); }

	protected void showValidationSuccess() { showFormMessage("No errors found."); }

	@Override
	protected Control createDialogArea(final Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		getShell().setText(dialogTitle());
		Composite form = createForm(area, dialogHost());
		form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		return area;
	}

	@Override
	protected void createButtonsForButtonBar(final Composite parent) {
		createButton(parent, VALIDATE_ID, "Validate", false)
				.setToolTipText("Validate this declaration in its editor context without applying changes.");
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		createButton(parent, IDialogConstants.OK_ID, creation ? "Create" : "Apply", true);
		updateButtons();
	}

	private void updateButtons() {
		for (int id : new int[] { VALIDATE_ID, IDialogConstants.OK_ID }) {
			var button = getButton(id);
			if (button != null) button.setEnabled(complete);
		}
	}

	@Override
	protected void buttonPressed(final int id) {
		if (id == VALIDATE_ID) {
			if (validateNow()) showValidationSuccess();
		} else super.buttonPressed(id);
	}

	@Override
	protected void okPressed() {
		if (accept()) super.okPressed();
	}

	@Override
	protected void cancelPressed() {
		clearResult();
		super.cancelPressed();
	}
}
