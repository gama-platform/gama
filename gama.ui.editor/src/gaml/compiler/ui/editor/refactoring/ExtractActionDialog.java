/*******************************************************************************************************
 *
 * ExtractActionDialog.java, in gama.ui.editor, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ui.editor.refactoring;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog for extracting a block of selected code into a new GAML action. Asks the user for an action
 * name and an optional return type. The action name must be a valid GAML identifier.
 */
public class ExtractActionDialog extends TitleAreaDialog {

	/** Common return types offered in the combo. */
	private static final String[] RETURN_TYPES =
			{ "action", "bool", "int", "float", "string", "list", "map", "point", "geometry", "agent" };

	/** Input field for the action name. */
	private Text nameField;

	/** Combo offering common return types (editable, so the user can type any type). */
	private Combo returnTypeCombo;

	/** The action name entered by the user. */
	private String actionName = "";

	/** The return type chosen / entered by the user. */
	private String returnType = "action";

	/**
	 * Creates the dialog.
	 *
	 * @param parentShell
	 *            the parent shell
	 */
	public ExtractActionDialog(final Shell parentShell) {
		super(parentShell);
		setHelpAvailable(false);
	}

	@Override
	public void create() {
		super.create();
		setTitle("Extract Action");
		setMessage("Enter a name for the new action.", IMessageProvider.INFORMATION);
		getButton(OK).setEnabled(false);
		nameField.setFocus();
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite area = (Composite) super.createDialogArea(parent);
		final Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		final GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 10;
		layout.marginWidth = 10;
		layout.verticalSpacing = 8;
		container.setLayout(layout);

		// Action name row
		final Label nameLabel = new Label(container, SWT.NONE);
		nameLabel.setText("Action name:");
		nameField = new Text(container, SWT.BORDER);
		nameField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		nameField.addModifyListener(e -> validate());

		// Return type row
		final Label returnTypeLabel = new Label(container, SWT.NONE);
		returnTypeLabel.setText("Return type:");
		returnTypeCombo = new Combo(container, SWT.DROP_DOWN);
		returnTypeCombo.setItems(RETURN_TYPES);
		returnTypeCombo.select(0); // default: "action" (void)
		returnTypeCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		return area;
	}

	/**
	 * Validates the action name and updates the error message / OK button state.
	 */
	private void validate() {
		final String name = nameField.getText().trim();
		if (name.isEmpty()) {
			setErrorMessage("Action name cannot be empty.");
			getButton(OK).setEnabled(false);
		} else if (!name.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
			setErrorMessage(
					"Action name must be a valid GAML identifier (letters, digits and underscores, starting with a letter or underscore).");
			getButton(OK).setEnabled(false);
		} else {
			setErrorMessage(null);
			getButton(OK).setEnabled(true);
		}
	}

	@Override
	protected void okPressed() {
		actionName = nameField.getText().trim();
		returnType = returnTypeCombo.getText().trim();
		if (returnType.isEmpty()) { returnType = "action"; }
		super.okPressed();
	}

	@Override
	protected void configureShell(final Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Extract Action");
	}

	/**
	 * Returns the action name entered by the user.
	 *
	 * @return the action name
	 */
	public String getActionName() { return actionName; }

	/**
	 * Returns the return type chosen by the user.
	 *
	 * @return the return type (e.g. "action" for void, "int", "float", etc.)
	 */
	public String getReturnType() { return returnType; }
}
