package gaml.compiler.ui.wizards.species;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * A literal boolean has one checkbox, not separate presence/value controls. Only user edits normalize optional
 * literals equal to the effective runtime default; opening a form preserves even explicit default literals.
 * A null default means inheritance/runtime context is unresolved and requires an expression field.
 * Expression mode never evaluates GAML; returning to checkbox mode explicitly replaces a nonliteral expression.
 * Required facets are never omitted by normalization, even when their literal equals a known default.
 */
public final class BooleanFacetField extends Composite {
	private final String label, fieldProperty, fieldKey, documentation;
	private final Supplier<Boolean> defaults;
	private final boolean required;
	private final Consumer<String> modified;
	private String value;
	private boolean edited;
	private Boolean displayedDefault;

	public BooleanFacetField(final Composite parent, final String label, final String value,
			final Supplier<Boolean> defaults, final boolean required, final String documentation,
			final String fieldProperty, final String fieldKey, final Consumer<String> modified) {
		super(parent, SWT.NONE);
		this.label = label;
		this.value = value;
		this.defaults = defaults;
		this.required = required;
		this.documentation = documentation;
		this.fieldProperty = fieldProperty;
		this.fieldKey = fieldKey;
		this.modified = modified;
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = layout.marginHeight = 0;
		setLayout(layout);
		setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		render(defaults.get() == null || !literal(value));
	}

	public String value() { return value; }
	public boolean edited() { return edited; }

	/** Refreshes context/preference changes without normalizing or emitting an edit. */
	public static void refreshDefaults(final Composite parent) {
		for (Control child : parent.getChildren()) {
			if (child instanceof BooleanFacetField field) {
				Boolean next = field.defaults.get();
				if (!java.util.Objects.equals(next, field.displayedDefault)) {
					field.render(next == null || !literal(field.value));
					field.getParent().layout(true, true);
				}
			} else if (child instanceof Composite composite) refreshDefaults(composite);
		}
	}

	private static boolean literal(final String text) {
		return text == null || "true".equals(text.trim()) || "false".equals(text.trim());
	}

	private void update(final String next) {
		Boolean defaultValue = defaults.get();
		value = next == null || next.isBlank() ? null : !required && defaultValue != null && literal(next)
				&& Boolean.parseBoolean(next.trim()) == defaultValue ? null : next;
		edited = true;
		modified.accept(value);
	}

	private void identify(final Control control, final String suffix) {
		control.setData(fieldProperty, fieldKey + suffix);
		WizardWidgets.accessible(control, label + suffix);
	}

	private void render(final boolean expression) {
		for (Control child : getChildren()) child.dispose();
		Boolean defaultValue = defaults.get();
		displayedDefault = defaultValue;
		String help = documentation + (defaultValue == null
				? " No context-independent literal default is assumed; edit a GAML expression, or clear to omit."
				: " Default: " + defaultValue + (required ? ". Required: always written explicitly."
						: ". Edited literals equal to this default are omitted."))
				+ " Expressions are preserved without evaluation.";
		if (expression) {
			((GridLayout) getLayout()).numColumns = defaultValue == null ? 2 : 3;
			Label caption = new Label(this, SWT.NONE);
			caption.setText(WizardWidgets.labelText(label) + (required ? " *" : ""));
			caption.setToolTipText(help);
			Text input = new Text(this, SWT.BORDER | SWT.SINGLE);
			input.setMessage(label + (required ? " *" : ""));
			input.setText(value == null ? "" : value);
			input.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			input.setToolTipText(label + ": " + help);
			identify(input, "");
			input.addModifyListener(e -> update(input.getText()));
		} else {
			((GridLayout) getLayout()).numColumns = 2;
			Button check = new Button(this, SWT.CHECK);
			check.setText(WizardWidgets.labelText(label) + (required ? " *" : ""));
			check.setSelection(value == null ? Boolean.TRUE.equals(defaultValue) : Boolean.parseBoolean(value.trim()));
			check.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			check.setToolTipText(help);
			identify(check, "");
			check.addListener(SWT.Selection, e -> update(Boolean.toString(check.getSelection())));
		}
		if (defaultValue != null) {
			Button mode = new Button(this, SWT.PUSH);
			mode.setText(expression ? "Checkbox" : "Expression...");
			mode.setToolTipText(expression ? "Replace the expression with a literal boolean (initially the default)."
					: "Edit a GAML boolean expression instead of a literal. Opening this field changes nothing.");
			identify(mode, ":expression");
			mode.addListener(SWT.Selection, e -> {
				if (expression && !literal(value)) update(Boolean.toString(defaults.get()));
				render(!expression);
				getParent().layout(true, true);
				requestLayout();
			});
		}
		layout(true, true);
	}
}
