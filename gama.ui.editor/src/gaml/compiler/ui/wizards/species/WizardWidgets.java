package gaml.compiler.ui.wizards.species;

import java.util.List;
import java.util.Locale;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/** Layout and accessibility primitives shared by the declaration wizards. */
public final class WizardWidgets {

	private WizardWidgets() {}

	public static Composite composite(final Composite parent, final int columns) {
		Composite result = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(columns, false);
		layout.marginWidth = 6;
		layout.marginHeight = 4;
		layout.horizontalSpacing = 8;
		layout.verticalSpacing = 5;
		result.setLayout(layout);
		return result;
	}

	public static Label label(final Composite parent, final String text, final String description) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(text + ":");
		label.setToolTipText(description);
		return label;
	}

	/** A push button at least as wide as the standard dialog buttons. */
	public static Button button(final Composite parent, final String text, final String description) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(text);
		GridData data = new GridData(SWT.FILL, SWT.CENTER, false, false);
		GC gc = new GC(button);
		gc.setFont(button.getFont());
		int width = Dialog.convertHorizontalDLUsToPixels(gc.getFontMetrics(), IDialogConstants.BUTTON_WIDTH);
		gc.dispose();
		data.widthHint = Math.max(width, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		button.setLayoutData(data);
		button.setToolTipText(description);
		accessible(button, description);
		return button;
	}

	public static void accessible(final Control control, final String label) {
		control.getAccessible().addAccessibleListener(new AccessibleAdapter() {
			@Override
			public void getName(final AccessibleEvent event) { event.result = label; }
		});
	}

	/**
	 * Restricts the items of an editable type combo to the choices starting with its text, keeping the text and the
	 * caret. Callers must ignore the modify events this triggers.
	 */
	public static void filterByPrefix(final Combo combo, final List<String> choices) {
		String input = combo.getText();
		String prefix = input.toLowerCase(Locale.ROOT);
		List<String> matches =
				choices.stream().filter(candidate -> candidate.toLowerCase(Locale.ROOT).startsWith(prefix)).toList();
		combo.setItems(matches.toArray(String[]::new));
		combo.setText(input);
		combo.setSelection(new Point(input.length(), input.length()));
		if (!input.isBlank() && !matches.isEmpty()) combo.setListVisible(true);
	}
}
