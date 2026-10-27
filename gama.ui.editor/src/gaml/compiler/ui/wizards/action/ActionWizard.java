package gaml.compiler.ui.wizards.action;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import gaml.compiler.ui.wizards.species.SpeciesWizard.FormHost;
import gaml.compiler.ui.wizards.species.DeclarationWizard;
import gaml.compiler.ui.wizards.species.WizardWidgets;

/**
 * Detached action settings: name, return type, virtual, arguments and code. Code is edited in an embedded,
 * syntax-highlighted GAML editor and is enabled only for non-virtual actions; its text is kept across Virtual toggles.
 * Compilation runs only on Validate and acceptance, never on keystrokes. Editor adapters own the undoable apply.
 * Text-editor creation can opt into selection extraction before opening: Include selection appears beside Returns,
 * defaults to checked and retains independent extracted/separate code buffers when toggled.
 */
public class ActionWizard extends DeclarationWizard<ActionDraft> {

	/** The code area. The default implementation embeds the GAML editor, falling back to plain text without UI. */
	protected interface BodyEditor {
		Control control();

		String text();

		void setEnabled(boolean enabled);

		default void setText(final String value) {
			if (control() instanceof StyledText text) text.setText(value);
			else throw new IllegalStateException("The action body editor does not support replacing its text.");
		}
	}

	private final ActionDraft original;
	private final Function<ActionDraft, String> validator;
	private final List<String> typeNames;
	private final Function<ActionDraft, ActionBodyContext> bodyContext;
	private final List<ActionDraft.Argument> arguments = new ArrayList<>();
	private FormHost host;
	private Text name;
	private Combo returnType;
	private Button virtual;
	private Table table;
	private Text argumentName;
	private Combo argumentType;
	private Text argumentDefault;
	private Button remove;
	private Button up;
	private Button down;
	private BodyEditor body;
	private Label bodyLabel;
	private Label signature;
	private Label closing;
	private ActionDraft result;
	private Set<String> baseline;
	private boolean loading;
	private boolean ready;
	private boolean selectionAvailable;
	private Button includeSelection;
	private String selectionBody, separateBody = "";

	/**
	 * @param draft
	 *            initial values; returned unchanged (same instance) when the form is not modified
	 * @param validator
	 *            adapter validation run on Validate/acceptance after local and contextual checks; returns an error or null
	 * @param typeNames
	 *            proposed types for the return type ({@code action} is always first) and for arguments
	 * @param bodyContext
	 *            contextual source for code verification, called only on Validate/acceptance for valid non-virtual drafts;
	 *            null uses a minimal synthetic model
	 */
	public ActionWizard(final Shell shell, final ActionDraft draft, final Function<ActionDraft, String> validator,
			final List<String> typeNames, final Function<ActionDraft, ActionBodyContext> bodyContext) {
		super(shell);
		original = Objects.requireNonNull(draft);
		this.validator = Objects.requireNonNull(validator);
		this.typeNames = List.copyOf(typeNames);
		this.bodyContext = bodyContext;
		arguments.addAll(draft.arguments());
		selectionBody = draft.body();
	}

	/** Enable only for creation from a text selection; the supplied initial draft contains the selected body. */
	public final void setSelectionExtractionAvailable(final boolean available) {
		if (host != null) throw new IllegalStateException("Configure selection extraction before creating the form.");
		selectionAvailable = available;
	}

	public final boolean isSelectionIncluded() {
		return selectionAvailable && (includeSelection == null || includeSelection.getSelection());
	}

	@Override
	protected String dialogTitle() { return "Action"; }

	/** Builds the form in another container; use {@link #accept()} instead of the dialog's primary button. */
	public Composite createForm(final Composite parent, final FormHost formHost) {
		host = Objects.requireNonNull(formHost);
		host.setTitle("Action settings");
		host.setMessage("Define the action signature and its code. Code is verified on Validate and acceptance.");
		Composite form = composite(parent, 1);
		form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite header = WizardWidgets.row(form, selectionAvailable ? 8 : 6, "Identity");
		name = WizardWidgets.textField(header, "Name", original.name(),
				"Name of the action, used to call it with do or as a function.", "action.field", "Name", 160);
		List<String> returns = new ArrayList<>();
		returns.add(ActionDraft.NO_RETURN);
		for (String type : typeNames) if (!returns.contains(type)) returns.add(type);
		if (!returns.contains(original.returnType())) returns.add(original.returnType());
		returnType = WizardWidgets.choiceField(header, "Returns", original.returnType(), returns, true,
				"Type of the returned value. 'action' means that the action returns nothing.", "action.field", "Returns", 130);
		if (selectionAvailable) {
			includeSelection = WizardWidgets.checkbox(header, "Include selection", true,
					"Move the selected statements into this action and replace them with a call. "
							+ "Uncheck to leave the selection untouched and create a separate action. "
							+ "Required arguments are passed from caller variables with matching names; defaulted arguments are omitted. "
							+ "A typed action's result is not assigned by the generated do call.",
					"action.field", "Include selection");
		}
		virtual = WizardWidgets.checkbox(header, "Virtual", original.virtual(),
				"A virtual action has no code: it must be redefined by the children of its owner. Default: false.",
				"action.field", "Virtual");

		createArguments(form);

		bodyLabel = new Label(form, SWT.NONE);
		bodyLabel.setText("Code:");
		bodyLabel.setToolTipText("GAML statements executed by the action. Disabled for virtual actions.");
		signature = new Label(form, SWT.WRAP);
		signature.setFont(JFaceResources.getTextFont());
		signature.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		signature.setToolTipText("Declaration of the action, updated from the fields above.");
		signature.setData("action.field", "Signature");
		accessible(signature, "Signature");
		body = createBodyEditor(form, original.body());
		GridData bodyData = new GridData(SWT.FILL, SWT.FILL, true, true);
		bodyData.heightHint = 220;
		bodyData.widthHint = 560;
		body.control().setLayoutData(bodyData);
		body.control().setData("action.field", "Code");
		accessible(body.control(), "Code");
		closing = new Label(form, SWT.NONE);
		closing.setFont(JFaceResources.getTextFont());
		closing.setText("}");
		closing.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		closing.setData("action.field", "Closing");

		name.addModifyListener(e -> changed());
		returnType.addModifyListener(e -> changed());
		virtual.addListener(SWT.Selection, e -> changed());
		if (includeSelection != null) includeSelection.addListener(SWT.Selection, e -> {
			if (includeSelection.getSelection()) {
				separateBody = body.text();
				body.setText(selectionBody);
			} else {
				selectionBody = body.text();
				body.setText(separateBody);
			}
			baseline = null;
			changed();
		});
		ready = true;
		renderArguments(arguments.isEmpty() ? -1 : 0);
		changed();
		return form;
	}

	private void createArguments(final Composite form) {
		Label title = new Label(form, SWT.NONE);
		title.setText("Arguments:");
		title.setToolTipText("Arguments passed to the action, in declaration order.");
		Composite body = composite(form, 2);
		body.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		table = new Table(body, SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL);
		table.setData("action.arguments", true);
		table.setHeaderVisible(true);
		table.setToolTipText("Arguments in declaration order: Name identifies the argument, Type defines its values"
				+ " and Default provides an optional default value.");
		accessible(table, "Arguments");
		GridData tableData = new GridData(SWT.FILL, SWT.FILL, false, true);
		tableData.widthHint = 300;
		tableData.heightHint = 110;
		table.setLayoutData(tableData);
		for (String column : List.of("Name", "Type", "Default")) {
			TableColumn c = new TableColumn(table, SWT.NONE);
			c.setText(gaml.compiler.ui.wizards.species.WizardWidgets.labelText(column));
			c.setWidth("Default".equals(column) ? 100 : column.equals("Name") ? 110 : 80);
		}
		table.addListener(SWT.Selection, e -> loadSelection());
		Composite detail = composite(body, 2);
		detail.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		Composite buttons = composite(detail, 4);
		((GridLayout) buttons.getLayout()).marginWidth = 0;
		buttons.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		Button add = button(buttons, "Add", "Add an argument");
		remove = button(buttons, "Remove", "Remove the selected argument");
		up = button(buttons, "Up", "Move the selected argument up");
		down = button(buttons, "Down", "Move the selected argument down");
		label(detail, "Name", "Argument name.");
		argumentName = new Text(detail, SWT.BORDER | SWT.SINGLE);
		field(argumentName, "Argument name", 150);
		label(detail, "Type", "Argument type; choose or type any GAML type, e.g. list<point>.");
		argumentType = new Combo(detail, SWT.DROP_DOWN);
		argumentType.setItems(typeNames.toArray(String[]::new));
		field(argumentType, "Argument type", 150);
		label(detail, "Default", "Optional GAML expression used when the caller omits this argument.");
		argumentDefault = new Text(detail, SWT.BORDER | SWT.SINGLE);
		field(argumentDefault, "Argument default", 150);
		add.addListener(SWT.Selection, e -> {
			int n = arguments.size() + 1;
			Set<String> used = new HashSet<>(arguments.stream().map(ActionDraft.Argument::name).toList());
			while (used.contains("arg" + n)) n++;
			arguments.add(new ActionDraft.Argument("arg" + n, "int", ""));
			renderArguments(arguments.size() - 1);
			argumentName.setFocus();
			argumentName.selectAll();
			changed();
		});
		remove.addListener(SWT.Selection, e -> {
			int index = table.getSelectionIndex();
			if (index < 0) return;
			arguments.remove(index);
			renderArguments(Math.min(index, arguments.size() - 1));
			changed();
		});
		up.addListener(SWT.Selection, e -> move(-1));
		down.addListener(SWT.Selection, e -> move(1));
		argumentName.addModifyListener(e -> editSelection());
		argumentType.addModifyListener(e -> editSelection());
		argumentDefault.addModifyListener(e -> editSelection());
	}

	private void move(final int delta) {
		int index = table.getSelectionIndex();
		int target = index + delta;
		if (index < 0 || target < 0 || target >= arguments.size()) return;
		arguments.add(target, arguments.remove(index));
		renderArguments(target);
		changed();
	}

	private void renderArguments(final int selection) {
		loading = true;
		try {
			table.removeAll();
			for (var argument : arguments) {
				TableItem item = new TableItem(table, SWT.NONE);
				item.setText(new String[] { argument.name(), argument.type(), argument.defaultValue() });
			}
			if (selection >= 0 && selection < arguments.size()) table.setSelection(selection);
		} finally {
			loading = false;
		}
		loadSelection();
	}

	private void loadSelection() {
		int index = table.getSelectionIndex();
		boolean selected = index >= 0;
		loading = true;
		try {
			var argument = selected ? arguments.get(index) : new ActionDraft.Argument("", "", "");
			argumentName.setText(argument.name());
			argumentType.setText(argument.type());
			argumentDefault.setText(argument.defaultValue());
		} finally {
			loading = false;
		}
		for (Control control : List.of(argumentName, argumentType, argumentDefault, remove)) control.setEnabled(selected);
		up.setEnabled(index > 0);
		down.setEnabled(selected && index < arguments.size() - 1);
	}

	private void editSelection() {
		if (loading) return;
		int index = table.getSelectionIndex();
		if (index < 0) return;
		var argument = new ActionDraft.Argument(argumentName.getText().trim(), argumentType.getText(),
				argumentDefault.getText());
		arguments.set(index, argument);
		table.getItem(index).setText(new String[] { argument.name(), argument.type(), argument.defaultValue() });
		changed();
	}

	/**
	 * Creates the code editor. Embeds the GAML editor (syntax highlighting, no toolbar, no markers, no automatic
	 * compilation) when the GAML UI is running; otherwise uses a plain text area.
	 */
	protected BodyEditor createBodyEditor(final Composite parent, final String initial) {
		try {
			BodyEditor embedded = EmbeddedActionBody.create(parent, ActionBodyContext.standalone(original), initial);
			if (embedded != null) return embedded;
		} catch (RuntimeException | LinkageError error) {
			// The GAML UI injector is unavailable (e.g. outside the workbench): fall back to plain text.
		}
		StyledText text = new StyledText(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		text.setText(initial);
		return new BodyEditor() {
			@Override
			public Control control() { return text; }

			@Override
			public String text() { return text.getText(); }

			@Override
			public void setEnabled(final boolean enabled) {
				text.setEditable(enabled);
				text.setEnabled(enabled);
			}
		};
	}

	/** Current form values; the initial instance when nothing changed. */
	public ActionDraft readDraft() {
		ActionDraft draft = new ActionDraft(name.getText().trim(), returnType.getText().trim(), virtual.getSelection(),
				arguments, body.text());
		return draft.equals(original) ? original : draft;
	}

	private void changed() {
		if (!ready) return;
		body.setEnabled(!virtual.getSelection());
		bodyLabel.setText(virtual.getSelection() ? "Code (inactive: virtual action):" : "Code:");
		ActionDraft current = readDraft();
		String opening = signature(current);
		boolean block = opening.endsWith("{");
		signature.setText(opening);
		closing.setVisible(block);
		((GridData) closing.getLayoutData()).exclude = !block;
		bodyLabel.getParent().layout(new Control[] { bodyLabel, signature, closing });
		String error = current.validationError();
		host.setErrorMessage(error);
		host.setComplete(error == null);
	}

	/**
	 * The declaration shown above the code area: the header followed by {@code " {"} when the action has a block
	 * (the code area being its content), or by {@code " virtual: true;"} for a bodyless virtual action. Adapters whose
	 * source keeps the block of virtual actions may override it.
	 */
	protected String signature(final ActionDraft draft) {
		return draft.virtual() ? draft.header() + " virtual: true;" : draft.header() + " {";
	}

	/** Local checks, contextual compilation of non-virtual code, then the adapter validator. */
	public boolean validateNow() {
		ActionDraft draft = readDraft();
		String[] error = { draft.validationError() };
		if (error[0] == null && !draft.virtual()) BusyIndicator.showWhile(name.getDisplay(), () -> error[0] = verify(draft));
		if (error[0] == null) error[0] = validator.apply(draft);
		host.setErrorMessage(error[0]);
		host.setComplete(draft.validationError() == null);
		return error[0] == null;
	}

	private String verify(final ActionDraft draft) {
		ActionBodyContext context;
		try {
			context = context(draft);
		} catch (RuntimeException failure) {
			return "Cannot build the action context: " + failure.getMessage();
		}
		if (context == null) return "Cannot build the action context.";
		List<ActionVerifier.Problem> problems;
		try {
			problems = compile(context, draft.body());
		} catch (RuntimeException | LinkageError failure) {
			return "Cannot verify the code: " + failure.getMessage();
		}
		for (var problem : problems) if (problem.inBody()) return problem.toString();
		Set<String> existing = baseline();
		for (var problem : problems) if (!existing.contains(problem.message())) return problem.toString();
		return null;
	}

	private ActionBodyContext context(final ActionDraft draft) {
		return bodyContext == null ? ActionBodyContext.standalone(draft) : bodyContext.apply(draft);
	}

	/** Compiles {@code context.text(body)}; overridable to substitute the compiler. */
	protected List<ActionVerifier.Problem> compile(final ActionBodyContext context, final String body) {
		return ActionVerifier.verify(context, body);
	}

	/** Errors outside the code that already exist for the initial draft are not attributed to this edit. */
	private Set<String> baseline() {
		if (baseline != null) return baseline;
		baseline = new HashSet<>();
		try {
			ActionDraft initial = original.virtual() || original.validationError() != null ? null : original;
			if (initial != null) {
				ActionBodyContext context = context(initial);
				if (context != null) for (var problem : compile(context, initial.body()))
					if (!problem.inBody()) baseline.add(problem.message());
			}
		} catch (RuntimeException | LinkageError ignored) {
			// no baseline: every context error is reported
		}
		return baseline;
	}

	/** Validates (locally, contextually and with the adapter); on success stores the result. */
	public boolean accept() {
		result = null;
		if (!isCreationMode() && readDraft() == original && original.validationError() == null) {
			// Unchanged: no recompilation of pre-existing code; the adapter still checks staleness/conflicts.
			String error = validator.apply(original);
			host.setErrorMessage(error);
			if (error != null) return false;
			result = original;
			return true;
		}
		if (!validateNow()) return false;
		result = readDraft();
		return true;
	}

	@Override
	protected void clearResult() {
		result = null;
	}

	/** The accepted draft (the initial instance when unchanged), or null after Cancel or before OK. */
	public ActionDraft getResult() { return result; }

	private Composite composite(final Composite parent, final int columns) {
		return WizardWidgets.composite(parent, columns);
	}

	private void label(final Composite parent, final String text, final String description) {
		WizardWidgets.label(parent, text, description);
	}

	private void field(final Control control, final String label, final int width) {
		WizardWidgets.field(control, "action.field", label, width);
	}

	private Button button(final Composite parent, final String text, final String description) {
		return WizardWidgets.button(parent, text, description);
	}

	private void accessible(final Control control, final String label) {
		WizardWidgets.accessible(control, label);
	}
}
