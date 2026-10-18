package gaml.compiler.ui.wizards.behavior;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import gaml.compiler.ui.wizards.action.ActionBodyContext;
import gaml.compiler.ui.wizards.action.ActionVerifier;
import gaml.compiler.ui.wizards.action.EmbeddedActionBody;
import gaml.compiler.ui.wizards.species.SpeciesDraft;
import gaml.compiler.ui.wizards.species.SpeciesWizard.FormHost;
import gaml.compiler.ui.wizards.species.WizardWidgets;

/**
 * Detached behaviour settings (reflex, state, task, plan, perceive, rule): kind, name, the kind's facets from the
 * compiler declarations (check boxes for literal booleans, expressions otherwise, other facets kept verbatim) and the
 * code, edited in an embedded GAML editor below a live declaration label. Compilation runs only on Validate and OK.
 * Editor adapters own the undoable apply.
 */
public class BehaviorWizard extends TitleAreaDialog {

	public static final int VALIDATE_ID = IDialogConstants.CLIENT_ID + 1;

	/** Data key identifying the form's controls (for tests and accessibility tools). */
	public static final String FIELD = "behavior.field";

	/** The code area. The default implementation embeds the GAML editor, falling back to plain text without UI. */
	protected interface BodyEditor {
		Control control();

		String text();

		void setEnabled(boolean enabled);
	}

	private final BehaviorDraft original;
	private final List<BehaviorKind> kinds;
	private final Function<BehaviorDraft, String> validator;
	private final Function<BehaviorDraft, ActionBodyContext> bodyContext;
	/** Current facets in source order; values are raw GAML. */
	private final List<SpeciesDraft.Facet> facets = new ArrayList<>();
	private final Map<String, Control> facetControls = new LinkedHashMap<>();
	private BehaviorKind kind;
	private FormHost host;
	private Combo kindCombo;
	private Text name;
	private Composite facetArea;
	private BodyEditor body;
	private Label bodyLabel;
	private Label signature;
	private Label closing;
	private BehaviorDraft result;
	private Set<String> baseline;
	private boolean loading;
	private boolean ready;

	/** Edition (or creation) of a behaviour whose kind is fixed. */
	public BehaviorWizard(final Shell shell, final BehaviorDraft draft, final Function<BehaviorDraft, String> validator,
			final Function<BehaviorDraft, ActionBodyContext> bodyContext) {
		this(shell, draft, Set.of(draft.kind()), validator, bodyContext);
	}

	/**
	 * @param draft
	 *            initial values; returned unchanged (same instance) when the form is not modified
	 * @param selectableKinds
	 *            kinds offered by the Kind selector (creation); a single kind fixes it. Facets are kept when the kind
	 *            changes (those unknown to the new kind are listed as other facets)
	 * @param validator
	 *            adapter validation run on Validate/OK after local and contextual checks; returns an error or null
	 * @param bodyContext
	 *            contextual source of the draft's code (for kinds without code: the whole rendered declaration as
	 *            prefix), called only on Validate/OK for locally valid drafts; null uses a minimal synthetic model
	 */
	public BehaviorWizard(final Shell shell, final BehaviorDraft draft, final Set<BehaviorKind> selectableKinds,
			final Function<BehaviorDraft, String> validator, final Function<BehaviorDraft, ActionBodyContext> bodyContext) {
		super(shell);
		original = Objects.requireNonNull(draft);
		List<BehaviorKind> offered = new ArrayList<>();
		for (BehaviorKind k : BehaviorKind.values()) if (selectableKinds.contains(k) || k == draft.kind()) offered.add(k);
		kinds = List.copyOf(offered);
		this.validator = Objects.requireNonNull(validator);
		this.bodyContext = bodyContext;
		kind = draft.kind();
		facets.addAll(draft.facets());
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	/** A minimal synthetic model placing the behaviour in {@code global}. */
	public static ActionBodyContext standalone(final BehaviorDraft draft) {
		URI uri = URI.createURI("synthetic:/behavior-wizard/.behavior-wizard.gaml");
		String model = "model behavior_wizard\n\nglobal {\n\t";
		if (!draft.kind().hasBody()) return new ActionBodyContext(uri, model + draft.header() + ";\n}\n", "");
		return new ActionBodyContext(uri, model + draft.header() + " {", "}\n}\n");
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		getShell().setText("Behavior");
		Composite form = createForm(area, new FormHost() {
			@Override
			public void setTitle(final String title) { BehaviorWizard.this.setTitle(title); }

			@Override
			public void setMessage(final String message) { BehaviorWizard.this.setMessage(message); }

			@Override
			public void setErrorMessage(final String error) { BehaviorWizard.this.setErrorMessage(error); }

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
		host.setTitle("Behavior settings");
		host.setMessage("Define the behavior, its facets and its code. Code is verified on Validate and OK.");
		Composite form = WizardWidgets.composite(parent, 1);
		form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite header = WizardWidgets.composite(form, 4);
		header.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		WizardWidgets.label(header, "Kind", "Kind of behavior: reflex, state (fsm), task (weighted tasks), plan, perceive"
				+ " or rule (BDI).");
		kindCombo = new Combo(header, SWT.READ_ONLY | SWT.DROP_DOWN);
		kindCombo.setItems(kinds.stream().map(BehaviorKind::keyword).toArray(String[]::new));
		kindCombo.select(kinds.indexOf(kind));
		kindCombo.setEnabled(kinds.size() > 1);
		field(kindCombo, "Kind", 110);
		WizardWidgets.label(header, "Name", "Name of the behavior; optional for reflexes, init, abort, plans, perceives and rules.");
		name = new Text(header, SWT.BORDER | SWT.SINGLE);
		name.setText(original.name());
		field(name, "Name", 180);

		Label facetTitle = new Label(form, SWT.NONE);
		facetTitle.setText("Facets:");
		facetTitle.setToolTipText("Facets declared by the compiler for this kind of behavior.");
		facetArea = WizardWidgets.composite(form, 2);
		facetArea.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		facetArea.setData(FIELD, "Facets");

		bodyLabel = new Label(form, SWT.NONE);
		bodyLabel.setText("Code:");
		signature = new Label(form, SWT.WRAP);
		signature.setFont(JFaceResources.getTextFont());
		signature.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		signature.setToolTipText("Declaration of the behavior, updated from the fields above.");
		signature.setData(FIELD, "Signature");
		WizardWidgets.accessible(signature, "Signature");
		body = createBodyEditor(form, original.body());
		GridData bodyData = new GridData(SWT.FILL, SWT.FILL, true, true);
		bodyData.heightHint = 220;
		bodyData.widthHint = 560;
		body.control().setLayoutData(bodyData);
		body.control().setData(FIELD, "Code");
		WizardWidgets.accessible(body.control(), "Code");
		closing = new Label(form, SWT.NONE);
		closing.setFont(JFaceResources.getTextFont());
		closing.setText("}");
		closing.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		closing.setData(FIELD, "Closing");

		name.addModifyListener(e -> changed());
		kindCombo.addListener(SWT.Selection, e -> {
			kind = kinds.get(kindCombo.getSelectionIndex());
			renderFacets();
			changed();
		});
		ready = true;
		renderFacets();
		changed();
		return form;
	}

	/** Rebuilds the facet fields of the current kind, then the facets it does not declare (kept verbatim). */
	private void renderFacets() {
		loading = true;
		try {
			for (Control child : facetArea.getChildren()) child.dispose();
			facetControls.clear();
			List<BehaviorFacet> declared = BehaviorFacet.of(kind);
			Set<String> known = new HashSet<>();
			for (BehaviorFacet facet : declared) {
				known.add(facet.name());
				String value = value(facet.name());
				boolean literal = value == null || "true".equals(value.trim()) || "false".equals(value.trim());
				if (facet.bool() && literal) {
					Button check = new Button(facetArea, SWT.CHECK);
					check.setText(facet.name());
					check.setSelection(value != null && "true".equals(value.trim()));
					check.setToolTipText(facet.documentation());
					check.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
					check.setData(FIELD, "facet:" + facet.name());
					WizardWidgets.accessible(check, facet.name());
					check.addListener(SWT.Selection, e -> {
						String before = originalValue(facet.name());
						// Unchecking keeps an explicit 'false' that was in the source; otherwise the facet is removed.
						set(facet.name(), check.getSelection() ? "true" : "false".equals(before) ? before : null);
					});
					facetControls.put(facet.name(), check);
				} else expression(facet.name() + (facet.required() ? " *" : ""), facet.name(),
						facet.documentation() + (facet.type().isEmpty() ? "" : " (" + facet.type() + ")"), value);
			}
			for (var facet : List.copyOf(facets)) if (!known.contains(facet.name()))
				expression(facet.name() + " (other)", facet.name(),
						"Facet not declared for " + kind.keyword() + "; kept as written. Clear it to remove it.",
						facet.value());
		} finally {
			loading = false;
		}
		facetArea.getParent().layout(true, true);
	}

	private void expression(final String label, final String facet, final String description, final String value) {
		WizardWidgets.label(facetArea, label, description);
		Text text = new Text(facetArea, SWT.BORDER | SWT.SINGLE);
		text.setText(value == null ? "" : value);
		text.setToolTipText(description);
		GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		data.widthHint = 260;
		text.setLayoutData(data);
		text.setData(FIELD, "facet:" + facet);
		WizardWidgets.accessible(text, facet);
		text.addModifyListener(e -> set(facet, text.getText().isBlank() ? null : text.getText()));
		facetControls.put(facet, text);
	}

	private String value(final String facet) {
		for (var f : facets) if (f.name().equals(facet)) return f.value();
		return null;
	}

	private String originalValue(final String facet) {
		String value = original.facet(facet);
		return value == null ? null : value.trim();
	}

	/** Sets a facet in place, appends it, or removes it (null); an unchanged original value keeps its instance. */
	private void set(final String facet, final String value) {
		if (loading) return;
		String originalText = original.facet(facet);
		String effective = value != null && originalText != null && value.trim().equals(originalText.trim())
				? originalText : value;
		int index = -1;
		for (int i = 0; i < facets.size(); i++) if (facets.get(i).name().equals(facet)) index = i;
		if (effective == null) {
			if (index >= 0) facets.remove(index);
		} else if (index >= 0) facets.set(index, new SpeciesDraft.Facet(facet, effective));
		else {
			// A facet restored to its original value goes back to its original position.
			int position = facets.size();
			if (originalText != null) {
				List<String> originalOrder = original.facets().stream().map(SpeciesDraft.Facet::name).toList();
				int rank = originalOrder.indexOf(facet);
				position = 0;
				for (var f : facets) if (originalOrder.indexOf(f.name()) >= 0 && originalOrder.indexOf(f.name()) < rank)
					position = facets.indexOf(f) + 1;
			}
			facets.add(position, new SpeciesDraft.Facet(facet, effective));
		}
		changed();
	}

	/**
	 * Creates the code editor. Embeds the GAML editor (syntax highlighting, no toolbar, no markers, no automatic
	 * compilation) when the GAML UI is running; otherwise uses a plain text area.
	 */
	protected BodyEditor createBodyEditor(final Composite parent, final String initial) {
		try {
			BehaviorDraft draft = new BehaviorDraft(BehaviorKind.REFLEX, "", List.of(), "");
			EmbeddedActionBody embedded = EmbeddedActionBody.create(parent, standalone(draft), initial);
			if (embedded != null) return new BodyEditor() {
				@Override
				public Control control() { return embedded.control(); }

				@Override
				public String text() { return embedded.text(); }

				@Override
				public void setEnabled(final boolean enabled) { embedded.setEnabled(enabled); }
			};
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
				.setToolTipText("Compile the behavior in its context and report errors.");
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
	public BehaviorDraft readDraft() {
		BehaviorDraft draft = new BehaviorDraft(kind, name.getText().trim(), facets, body.text());
		return draft.equals(original) ? original : draft;
	}

	private void changed() {
		if (!ready) return;
		boolean code = kind.hasBody();
		body.setEnabled(code);
		bodyLabel.setText(code ? "Code:" : "Code (not available: a " + kind.keyword() + " has no code):");
		BehaviorDraft current = readDraft();
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

	/** The declaration shown above the code area: the header followed by {@code " {"}, or {@code ";"} without code. */
	protected String signature(final BehaviorDraft draft) {
		return draft.header() + (draft.kind().hasBody() ? " {" : ";");
	}

	/** Local checks, contextual compilation, then the adapter validator. */
	public boolean validateNow() {
		BehaviorDraft draft = readDraft();
		String[] error = { draft.validationError() };
		if (error[0] == null) BusyIndicator.showWhile(name.getDisplay(), () -> error[0] = verify(draft));
		if (error[0] == null) error[0] = validator.apply(draft);
		host.setErrorMessage(error[0]);
		host.setComplete(draft.validationError() == null);
		return error[0] == null;
	}

	private String code(final BehaviorDraft draft) {
		return draft.kind().hasBody() ? draft.body() : "";
	}

	private String verify(final BehaviorDraft draft) {
		ActionBodyContext context;
		try {
			context = context(draft);
		} catch (RuntimeException failure) {
			return "Cannot build the behavior context: " + failure.getMessage();
		}
		if (context == null) return "Cannot build the behavior context.";
		List<ActionVerifier.Problem> problems;
		try {
			problems = compile(context, code(draft));
		} catch (RuntimeException | LinkageError failure) {
			return "Cannot verify the behavior: " + failure.getMessage();
		}
		for (var problem : problems) if (problem.inBody()) return problem.toString();
		Set<String> existing = baseline();
		for (var problem : problems) if (!existing.contains(problem.message())) return problem.toString();
		return null;
	}

	private ActionBodyContext context(final BehaviorDraft draft) {
		return bodyContext == null ? standalone(draft) : bodyContext.apply(draft);
	}

	/** Compiles {@code context.text(body)}; overridable to substitute the compiler. */
	protected List<ActionVerifier.Problem> compile(final ActionBodyContext context, final String code) {
		return ActionVerifier.verify(context, code);
	}

	/** Errors outside the code that already exist for the initial draft are not attributed to this edit. */
	private Set<String> baseline() {
		if (baseline != null) return baseline;
		baseline = new HashSet<>();
		try {
			if (original.validationError() == null) {
				ActionBodyContext context = context(original);
				if (context != null) for (var problem : compile(context, code(original)))
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
		if (readDraft() == original && original.validationError() == null) {
			// Unchanged: no recompilation; the adapter still checks staleness/conflicts.
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
	public BehaviorDraft getResult() { return result; }

	private void field(final Control control, final String label, final int width) {
		GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		data.widthHint = width;
		data.minimumWidth = 70;
		control.setLayoutData(data);
		control.setData(FIELD, label);
		WizardWidgets.accessible(control, label);
	}
}
