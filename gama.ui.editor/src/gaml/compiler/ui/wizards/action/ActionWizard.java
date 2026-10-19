package gaml.compiler.ui.wizards.action;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
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

/**
 * Detached action settings: name, return type, virtual, arguments and code. Code is edited in an embedded,
 * syntax-highlighted GAML editor and is enabled only for non-virtual actions; its text is kept across Virtual toggles.
 * Compilation runs only on Validate and OK, never on keystrokes. Editor adapters own the undoable apply.
 */
public class ActionWizard extends TitleAreaDialog {

	public static final int VALIDATE_ID = IDialogConstants.CLIENT_ID + 1;

	/** The code area. The default implementation embeds the GAML editor, falling back to plain text without UI. */
	protected interface BodyEditor {
		Control control();

		String text();

		void setEnabled(boolean enabled);
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

	/**
	 * @param draft
	 *            initial values; returned unchanged (same instance) when the form is not modified
	 * @param validator
	 *            adapter validation run on Validate/OK after local and contextual checks; returns an error or null
	 * @param typeNames
	 *            proposed types for the return type ({@code action} is always first) and for arguments
	 * @param bodyContext
	 *            contextual source for code verification, called only on Validate/OK for valid non-virtual drafts;
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
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		getShell().setText("Action");
		Composite form = createForm(area, new FormHost() {
			@Override
			public void setTitle(final String title) { ActionWizard.this.setTitle(title); }

			@Override
			public void setMessage(final String message) { ActionWizard.this.setMessage(message); }

			@Override
			public void setErrorMessage(final String error) { ActionWizard.this.setErrorMessage(error); }

			@Override
			public void setComplete(final boolean complete) {
				Button ok = getButton(IDialogConstants.OK_ID);
				if (ok != null) ok.setEnabled(complete);
				Button validate = getButton(VALIDATE_ID);
				if (validate != null) validate.setEnabled(complete);
			}

			@Override
			public void relayout() { getShell().pack(); }
		});
		form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		return area;
	}

	/** Builds the form in another container; use {@link #accept()} instead of OK. */
	public Composite createForm(final Composite parent, final FormHost formHost) {
		host = Objects.requireNonNull(formHost);
		host.setTitle("Action settings");
		host.setMessage("Define the action signature and its code. Code is verified on Validate and OK.");
		Composite form = composite(parent, 1);
		form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite header = composite(form, 5);
		header.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		label(header, "Name", "Name of the action, used to call it with do or as a function.");
		name = new Text(header, SWT.BORDER | SWT.SINGLE);
		name.setText(original.name());
		field(name, "Name", 160);
		label(header, "Returns", "Type of the returned value. 'action' means that the action returns nothing.");
		returnType = new Combo(header, SWT.DROP_DOWN);
		List<String> returns = new ArrayList<>();
		returns.add(ActionDraft.NO_RETURN);
		for (String type : typeNames) if (!returns.contains(type)) returns.add(type);
		if (!returns.contains(original.returnType())) returns.add(original.returnType());
		returnType.setItems(returns.toArray(String[]::new));
		returnType.setText(original.returnType());
		field(returnType, "Returns", 130);
		virtual = new Button(header, SWT.CHECK);
		virtual.setText(gaml.compiler.ui.wizards.species.WizardWidgets.labelText("Virtual"));
		virtual.setSelection(original.virtual());
		virtual.setToolTipText("A virtual action has no code: it must be redefined by the children of its owner. Default: false.");
		virtual.setData("action.field", "Virtual");
		accessible(virtual, "Virtual");

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

	@Override
	protected void createButtonsForButtonBar(final Composite parent) {
		createButton(parent, VALIDATE_ID, "Validate", false)
				.setToolTipText("Compile the action in its context and report errors.");
		super.createButtonsForButtonBar(parent);
		changed();
	}

	@Override
	protected void buttonPressed(final int buttonId) {
		if (buttonId == VALIDATE_ID) {
			if (validateNow()) host.setMessage("No errors found.");
		} else super.buttonPressed(buttonId);
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
		if (readDraft() == original && original.validationError() == null) {
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
	protected void okPressed() {
		if (accept()) super.okPressed();
	}

	@Override
	protected void cancelPressed() {
		result = null;
		super.cancelPressed();
	}

	/** The accepted draft (the initial instance when unchanged), or null after Cancel or before OK. */
	public ActionDraft getResult() { return result; }

	private Composite composite(final Composite parent, final int columns) {
		Composite result = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(columns, false);
		layout.marginWidth = 6;
		layout.marginHeight = 4;
		layout.horizontalSpacing = 8;
		layout.verticalSpacing = 5;
		result.setLayout(layout);
		return result;
	}

	private void label(final Composite parent, final String text, final String description) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(gaml.compiler.ui.wizards.species.WizardWidgets.labelText(text) + ":");
		label.setToolTipText(description);
	}

	private void field(final Control control, final String label, final int width) {
		GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		data.widthHint = width;
		data.minimumWidth = 70;
		control.setLayoutData(data);
		control.setData("action.field", label);
		accessible(control, label);
	}

	private Button button(final Composite parent, final String text, final String description) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(gaml.compiler.ui.wizards.species.WizardWidgets.labelText(text));
		button.setToolTipText(description);
		button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		accessible(button, description);
		return button;
	}

	private void accessible(final Control control, final String label) {
		control.getAccessible().addAccessibleListener(new AccessibleAdapter() {
			@Override
			public void getName(final AccessibleEvent event) { event.result = label; }
		});
	}
}
