package gama.ui.export.dialogs;

import java.nio.file.Path;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import gama.api.gaml.types.IType;
import gama.api.utils.prefs.GamaPreferences;
import gama.api.utils.prefs.Pref;

public final class ExportSuccessDialog extends Dialog {

	private static final Pref<Boolean> SHOW_CONFIRMATION = GamaPreferences.create(
			"pref_show_model_export_confirmation", "Show model export confirmation", true, IType.BOOL, false).hidden();

	private final Path output;
	private final boolean playable;
	private Button doNotShowAgain;

	private ExportSuccessDialog(final Shell parent, final Path output, final boolean playable) {
		super(parent);
		this.output = output;
		this.playable = playable;
	}

	public static boolean show(final Shell parent, final Path output, final boolean playable) {
		return SHOW_CONFIRMATION.getValue()
				&& new ExportSuccessDialog(parent, output, playable).open() == IDialogConstants.CLIENT_ID;
	}

	@Override
	protected void configureShell(final Shell shell) {
		super.configureShell(shell);
		shell.setText("Export successful");
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite area = (Composite) super.createDialogArea(parent);
		final Label message = new Label(area, SWT.WRAP);
		message.setText((playable ? "Playable model exported to " : "Standalone application exported to ")
				+ output.toAbsolutePath());
		final GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		data.widthHint = convertWidthInCharsToPixels(65);
		message.setLayoutData(data);
		return area;
	}

	@Override
	protected void createButtonsForButtonBar(final Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "Close", true);
		createButton(parent, IDialogConstants.CLIENT_ID, "Open directory", false);
		((GridLayout) parent.getLayout()).numColumns++;
		doNotShowAgain = new Button(parent, SWT.CHECK);
		doNotShowAgain.setText("Do not show again");
		doNotShowAgain.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
	}

	@Override
	protected void buttonPressed(final int buttonId) {
		if (buttonId == IDialogConstants.CLIENT_ID) {
			setReturnCode(buttonId);
			close();
		} else {
			super.buttonPressed(buttonId);
		}
	}

	@Override
	public boolean close() {
		if (doNotShowAgain != null && !doNotShowAgain.isDisposed() && doNotShowAgain.getSelection()) {
			SHOW_CONFIRMATION.set(false);
			SHOW_CONFIRMATION.save();
		}
		return super.close();
	}
}
