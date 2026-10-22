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
import gaml.compiler.ui.wizards.species.DeclarationWizard;
import gaml.compiler.ui.wizards.species.WizardWidgets;

/**
 * Detached behaviour settings (reflex, state, task, plan, perceive, rule): kind, name, the kind's facets from the
 * compiler declarations (check boxes for literal booleans, expressions otherwise, other facets kept verbatim) and the
 * code, edited in an embedded GAML editor below a live declaration label. Compilation runs only on Validate and acceptance.
 * Editor adapters own the undoable apply. Lifecycle kinds share the Reflex Editor; other kinds use a dedicated,
 * kind-locked editor (State Editor, Rule Editor, etc.) for diagram adapters.
 */
public class BehaviorWizard extends DeclarationWizard<BehaviorDraft> {

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

	/** Edition (or creation), allowing only lossless lifecycle conversions; other kinds are fixed. */
	public BehaviorWizard(final Shell shell, final BehaviorDraft draft, final Function<BehaviorDraft, String> validator,
			final Function<BehaviorDraft, ActionBodyContext> bodyContext) {
		this(shell, draft, draft.kind().convertibleKinds(), validator, bodyContext);
	}

	/**
	 * @param draft
	 *            initial values; returned unchanged (same instance) when the form is not modified
	 * @param selectableKinds
	 *            kinds offered by the Kind selector, restricted to the draft's convertible kinds; a single kind fixes
	 *            it. Non-lifecycle kinds are always locked. Facets are kept when a lifecycle kind changes.
	 * @param validator
	 *            adapter validation run on Validate/acceptance after local and contextual checks; returns an error or null
	 * @param bodyContext
	 *            contextual source of the draft's code (for kinds without code: the whole rendered declaration as
	 *            prefix), called only on Validate/acceptance for locally valid drafts; null uses a minimal synthetic model
	 */
	public BehaviorWizard(final Shell shell, final BehaviorDraft draft, final Set<BehaviorKind> selectableKinds,
			final Function<BehaviorDraft, String> validator, final Function<BehaviorDraft, ActionBodyContext> bodyContext) {
		super(shell);
		original = Objects.requireNonNull(draft);
		List<BehaviorKind> offered = new ArrayList<>();
		for (BehaviorKind k : BehaviorKind.values())
			if (k == draft.kind() || selectableKinds.contains(k) && draft.kind().convertibleKinds().contains(k))
				offered.add(k);
		kinds = List.copyOf(offered);
		this.validator = Objects.requireNonNull(validator);
		this.bodyContext = bodyContext;
		kind = draft.kind();
		facets.addAll(draft.facets());
	}

	/** A minimal synthetic model placing the behaviour in {@code global}. */
	public static ActionBodyContext standalone(final BehaviorDraft draft) {
		URI uri = URI.createURI("synthetic:/behavior-wizard/.behavior-wizard.gaml");
		String model = "model behavior_wizard\n\nglobal {\n\t";
		if (!draft.kind().hasBody()) return new ActionBodyContext(uri, model + draft.header() + ";\n}\n", "");
		return new ActionBodyContext(uri, model + draft.header() + " {", "}\n}\n");
	}

	@Override
	protected String dialogTitle() { return editorTitle(); }

	/** Builds the form in another container; use {@link #accept()} instead of the dialog's primary button. */
	public Composite createForm(final Composite parent, final FormHost formHost) {
		host = Objects.requireNonNull(formHost);
		host.setTitle(editorTitle());
		host.setMessage("Define the " + (BehaviorKind.LIFECYCLE.contains(kind) ? "reflex, init or abort" : kind.keyword())
				+ ", its facets and its code. Code is verified on Validate and acceptance.");
		Composite form = WizardWidgets.composite(parent, 1);
		form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		boolean lifecycle = BehaviorKind.LIFECYCLE.contains(kind);
		Composite header = WizardWidgets.row(form, lifecycle ? 6 : 4, "Identity");
		name = WizardWidgets.textField(header, "Name", original.name(),
				"Name of the behavior; optional for reflexes, init, abort, plans, perceives and rules.", FIELD, "Name", 180);
		kindCombo = WizardWidgets.choiceField(header, "Kind", kind.keyword(),
				kinds.stream().map(BehaviorKind::keyword).toList(), false, lifecycle
						? "Lifecycle declaration: reflex, init or abort." : "This editor only edits " + kind.keyword() + " declarations.",
				FIELD, "Kind", 110);
		kindCombo.setEnabled(kinds.size() > 1);
		if (lifecycle) {
			String help = "Boolean GAML condition. Default: true (unconditional). An edited true or empty value omits the facet.";
			Text when = WizardWidgets.textField(header, "When", value("when") == null ? "true" : value("when"),
					help, FIELD, "facet:when", 260);
			when.addModifyListener(e -> {
				String text = when.getText();
				set("when", text.isBlank() || "true".equals(text.trim()) ? null : text);
			});
		}

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

	private String editorTitle() {
		if (BehaviorKind.LIFECYCLE.contains(original.kind())) return "Reflex Editor";
		String keyword = original.kind().keyword();
		return Character.toUpperCase(keyword.charAt(0)) + keyword.substring(1) + " Editor";
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
				if (BehaviorKind.LIFECYCLE.contains(kind) && "when".equals(facet.name())) continue;
				String value = value(facet.name());
				if (facet.bool()) {
					var field = new gaml.compiler.ui.wizards.species.BooleanFacetField(facetArea, facet.name(), value,
							() -> facet.defaultValue(kind), facet.required(), facet.documentation()
									+ ("finished_when".equals(facet.name())
											? " Omitting this condition is distinct from supplying a literal condition." : ""), FIELD,
							"facet:" + facet.name(), next -> set(facet.name(), next));
					facetControls.put(facet.name(), field);
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
		Text text = WizardWidgets.textField(facetArea, label, value == null ? "" : value, description,
				FIELD, "facet:" + facet, 260);
		text.addModifyListener(e -> set(facet, text.getText().isBlank() ? null : text.getText()));
		facetControls.put(facet, text);
	}

	private String value(final String facet) {
		for (var f : facets) if (f.name().equals(facet)) return f.value();
		return null;
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
	protected void clearResult() {
		result = null;
	}

	/** The accepted draft (the initial instance when unchanged), or null after Cancel or before OK. */
	public BehaviorDraft getResult() { return result; }

}
