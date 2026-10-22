package gaml.compiler.ui.wizards.species;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
import org.eclipse.swt.widgets.Text;

/** Layout and accessibility primitives shared by the declaration wizards. */
public final class WizardWidgets {

	private WizardWidgets() {}

	/** Common row roles let hosts and regression tests identify arrangement without relying on individual fields. */
	public static Composite row(final Composite parent, final int columns, final String role) {
		Composite row = composite(parent, columns);
		row.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		row.setData("wizard.section", role);
		return row;
	}

	public static void field(final Control control, final String property, final String key, final int width) {
		GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		data.widthHint = width;
		data.minimumWidth = 70;
		control.setLayoutData(data);
		control.setData(property, key);
		accessible(control, labelText(key));
	}

	/** No listeners are attached during initialization, so displaying a default never writes a facet. */
	public static Text textField(final Composite parent, final String caption, final String value,
			final String help, final String property, final String key, final int width) {
		label(parent, caption, help);
		Text text = new Text(parent, SWT.BORDER | SWT.SINGLE);
		text.setText(value);
		text.setToolTipText(help);
		field(text, property, key, width);
		return text;
	}

	/** Includes loaded values not in the suggestions; editable choices also allow imported/plugin identifiers. */
	public static Combo choiceField(final Composite parent, final String caption, final String value,
			final List<String> choices, final boolean editable, final String help,
			final String property, final String key, final int width) {
		label(parent, caption, help);
		Combo combo = new Combo(parent, SWT.DROP_DOWN | (editable ? SWT.NONE : SWT.READ_ONLY));
		var items = new java.util.ArrayList<>(choices);
		if (!items.contains(value)) items.add(value);
		combo.setItems(items.toArray(String[]::new));
		combo.select(items.indexOf(value));
		combo.setToolTipText(help);
		field(combo, property, key, width);
		return combo;
	}

	public static Button checkbox(final Composite parent, final String caption, final boolean selected,
			final String help, final String property, final String key) {
		Button check = new Button(parent, SWT.CHECK);
		check.setText(labelText(caption));
		check.setSelection(selected);
		check.setToolTipText(help);
		check.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		check.setData(property, key);
		accessible(check, labelText(caption));
		return check;
	}

	/** Checkbox-only facet, retaining raw expressions until an explicit edit and reading defaults live on each edit. */
	public static Button literalBoolean(final Composite parent, final String caption, final String value,
			final boolean initial, final Supplier<Boolean> defaults, final String documentation,
			final String property, final String key, final Consumer<String> modified) {
		Boolean defaultValue = defaults.get();
		String help = documentation + " Initial UI value: " + initial + ". Effective omission default: "
				+ (defaultValue == null ? "inherited/runtime context" : defaultValue)
				+ (value != null && !"true".equals(value.trim()) && !"false".equals(value.trim())
						? ". Existing expression: " + value : "")
				+ ". Existing expressions are kept until this checkbox is edited; edits replace them with literals.";
		Button check = checkbox(parent, caption, value == null ? initial
				: "true".equals(value.trim()) || (!"false".equals(value.trim()) && initial), help, property, key);
		check.addListener(SWT.Selection, e -> {
			Boolean currentDefault = defaults.get();
			modified.accept(currentDefault != null && check.getSelection() == currentDefault
					? null : Boolean.toString(check.getSelection()));
		});
		return check;
	}

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
		label.setText(labelText(text) + ":");
		label.setToolTipText(description);
		return label;
	}

	/** A push button at least as wide as the standard dialog buttons. */
	public static Button button(final Composite parent, final String text, final String description) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(labelText(text));
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

	/** Sentence-case visible wizard labels without changing facet keys or GAML identifiers. */
	public static String labelText(final String text) {
		if (text == null || text.isEmpty()) return text;
		String words = text.replace('_', ' ');
		int first = 0;
		while (first < words.length() && !Character.isLetterOrDigit(words.charAt(first))) first++;
		if (first == words.length()) return words;
		return words.substring(0, first) + Character.toUpperCase(words.charAt(first)) + words.substring(first + 1);
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
