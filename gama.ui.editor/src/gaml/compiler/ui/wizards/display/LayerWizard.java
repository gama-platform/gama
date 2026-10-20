package gaml.compiler.ui.wizards.display;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.emf.common.util.URI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.xtext.parser.IParser;

import gaml.compiler.GamlStandaloneSetup;
import gaml.compiler.ui.wizards.action.ActionBodyContext;
import gaml.compiler.ui.wizards.action.ActionVerifier;
import gaml.compiler.ui.wizards.action.EmbeddedActionBody;
import gaml.compiler.ui.wizards.experiment.ExperimentDraft;
import gaml.compiler.ui.wizards.species.BooleanFacetField;
import gaml.compiler.ui.wizards.species.DeclarationWizard;
import gaml.compiler.ui.wizards.species.SpeciesDraft.Facet;
import gaml.compiler.ui.wizards.species.SpeciesWizard.FormHost;
import gaml.compiler.ui.wizards.species.WizardWidgets;

/** Kind-locked detached editor. Context is a complete model containing the candidate layer, with an empty suffix. */
public class LayerWizard extends DeclarationWizard<LayerDraft> {
	public static final String FIELD = "layer.field";
	private final LayerDraft original;
	private final Function<LayerDraft, String> validator;
	private final Supplier<List<String>> species, grids;
	private final Function<String, List<String>> aspects;
	private final Function<LayerDraft, ActionBodyContext> context;
	private List<Facet> facets, textFacets;
	private Combo expression;
	private StyledText preview;
	private String body;
	private FormHost host;
	private LayerDraft result;
	private boolean ready;
	private Set<String> baseline;

	public LayerWizard(final Shell shell, final LayerDraft draft, final Function<LayerDraft, String> validator,
			final Supplier<List<String>> speciesNames, final Supplier<List<String>> gridNames,
			final Function<LayerDraft, ActionBodyContext> context, final Function<String, List<String>> aspectsBySpecies) {
		super(shell);
		original = Objects.requireNonNull(draft);
		if (draft.kind() == LayerKind.RAW) throw new IllegalArgumentException("Unsupported layers are source-preserved, not editable here.");
		this.validator = Objects.requireNonNull(validator);
		species = speciesNames == null ? List::of : speciesNames;
		grids = gridNames == null ? List::of : gridNames;
		aspects = aspectsBySpecies == null ? s -> List.of() : aspectsBySpecies;
		this.context = context;
		facets = draft.facets();
		textFacets = draft.textFacets();
		body = draft.body();
	}

	@Override
	protected String dialogTitle() { return original.kind().title() + " Layer Editor"; }

	private static class Parser {
		static final IParser INSTANCE = GamlStandaloneSetup.doSetup().getInstance(IParser.class);
	}

	protected IParser parser() { return Parser.INSTANCE; }

	@Override
	public Composite createForm(final Composite parent, final FormHost host) {
		this.host = Objects.requireNonNull(host);
		host.setTitle(dialogTitle());
		host.setMessage("Configure this layer. Validate checks its display context without applying changes.");
		Composite form = WizardWidgets.composite(parent, 1);
		LayerKind kind = original.kind();
		Composite identity = WizardWidgets.row(form, kind == LayerKind.IMAGE ? 3 : 2, "Identity");
		String label = switch (kind) {
			case SPECIES, GRID -> "Species";
			case IMAGE -> "Image";
			case TEXT -> "Text";
			case MESH -> "Source";
			case ROTATION -> "Angle";
			default -> "Name";
		};
		expression = WizardWidgets.choiceField(identity, label, original.expression(),
				kind == LayerKind.GRID ? grids.get() : kind == LayerKind.SPECIES ? species.get() : List.of(),
				true, "GAML expression; suggestions do not constrain the value.", FIELD, "Expression", 280);
		expression.addModifyListener(e -> changed());
		if (kind == LayerKind.IMAGE) {
			var browse = WizardWidgets.button(identity, "Browse...", "Choose an image file.");
			browse.addListener(SWT.Selection, e -> {
				String file = browseImage();
				if (file != null) expression.setText(ExperimentDraft.quote(file));
			});
		}
		ScrolledComposite scroll = new ScrolledComposite(form, SWT.V_SCROLL | SWT.H_SCROLL);
		scroll.setExpandHorizontal(true);
		scroll.setExpandVertical(true);
		GridData scrolling = new GridData(SWT.FILL, SWT.FILL, true, true);
		scrolling.heightHint = 270;
		scrolling.widthHint = 650;
		scroll.setLayoutData(scrolling);
		Composite fields = WizardWidgets.composite(scroll, 1);
		scroll.setContent(fields);
		facetFields(fields, kind.compilerKeyword(), kind.expressionFacet(), false);
		if (kind == LayerKind.TEXT) facetFields(fields, "draw", "geometry", true);
		if (kind.hasBody()) {
			WizardWidgets.label(fields, "Body", "GAML block contents only. Existing drawing code/comments are preserved.");
			Control editor = createBodyEditor(fields, body, value -> { body = value; changed(); });
			GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
			data.heightHint = 150;
			editor.setLayoutData(data);
			editor.setData(FIELD, "Body");
		}
		scroll.setMinSize(fields.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		scroll.addListener(SWT.Resize, e -> scroll.setMinSize(fields.computeSize(scroll.getClientArea().width, SWT.DEFAULT)));
		WizardWidgets.label(form, "Generated GAML", "Read-only source preview.");
		preview = new StyledText(form, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY);
		preview.setFont(JFaceResources.getTextFont());
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
		data.heightHint = 90;
		preview.setLayoutData(data);
		preview.setData(FIELD, "Preview");
		ready = true;
		changed();
		return form;
	}

	private void facetFields(final Composite parent, final String keyword, final String excluded, final boolean drawing) {
		List<DisplayFacet> metadata = new ArrayList<>(DisplayFacet.of(keyword));
		List<Facet> values = drawing ? textFacets : facets;
		for (Facet value : values) if (metadata.stream().noneMatch(f -> f.name().equals(value.name())))
			metadata.add(new DisplayFacet(value.name(), "expression", false, false, "Existing facet retained verbatim."));
		for (DisplayFacet facet : metadata) if (!facet.name().equals(excluded)) {
			boolean color = facet.type().contains("rgb") || facet.type().contains("color");
			Composite row = WizardWidgets.row(parent, color ? 3 : 2, "Facet");
			String key = (drawing ? "draw:" : "facet:") + facet.name();
			String value = DisplayDraft.find(values, facet.name());
			java.util.function.Consumer<String> modified = next -> {
				List<Facet> current = drawing ? textFacets : facets;
				List<Facet> updated = ExperimentDraft.withFacet(current, values, facet.name(), next);
				if (drawing) textFacets = updated; else facets = updated;
				changed();
			};
			if (facet.flag()) {
				new BooleanFacetField(row, facet.name(), value,
						() -> facet.defaultValue(keyword, drawing ? textFacets : facets), facet.required(),
						facet.documentation(), FIELD, key, modified);
			} else {
				List<String> suggestions = suggestions(facet.name());
				Combo input = WizardWidgets.choiceField(row, facet.name() + (facet.required() ? " *" : ""),
						value == null ? "" : value, suggestions, true,
						facet.documentation() + " (" + facet.type() + ")", FIELD, key, 260);
				input.addListener(SWT.MouseDown, e -> {
					String text = input.getText();
					List<String> choices = suggestions(facet.name());
					if (!List.of(input.getItems()).equals(choices)) {
						// Refresh before restoring the text, without reporting initialization as an edit.
						input.setData("refreshing", Boolean.TRUE);
						input.setItems(choices.toArray(String[]::new));
						input.setText(text);
						input.setData("refreshing", null);
					}
				});
				input.addModifyListener(e -> {
					if (!Boolean.TRUE.equals(input.getData("refreshing")))
						modified.accept(input.getText().isBlank() ? null : input.getText());
				});
				if (color) {
					var choose = WizardWidgets.button(row, "Choose color...", "Choose a literal color; Cancel preserves the expression.");
					choose.addListener(SWT.Selection, e -> {
						String chosen = chooseColor(input.getText());
						if (chosen != null) input.setText(chosen);
					});
				}
			}
		}
	}

	private List<String> suggestions(final String facet) {
		if ("aspect".equals(facet) && original.kind() == LayerKind.SPECIES) return aspects.apply(expression.getText());
		if ("value".equals(facet) && original.kind() == LayerKind.AGENTS) return species.get();
		return List.of();
	}

	protected Control createBodyEditor(final Composite parent, final String initial,
			final java.util.function.Consumer<String> modified) {
		ActionBodyContext codeContext = bodyContext(readDraft());
		var embedded = createEmbeddedEditor(parent, codeContext, initial);
		if (embedded != null) {
			embedded.control().addListener(SWT.Modify, e -> modified.accept(embedded.text()));
			return embedded.control();
		}
		StyledText text = new StyledText(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		text.setFont(JFaceResources.getTextFont());
		text.setText(initial);
		text.addModifyListener(e -> modified.accept(text.getText()));
		return text;
	}

	/** Locate only this layer's editable body in the host model, even when several identical layers exist. */
	private ActionBodyContext bodyContext(final LayerDraft draft) {
		String marker = "/*layer_body_" + java.util.UUID.randomUUID() + "*/";
		LayerDraft marked = new LayerDraft(draft.id(), draft.kind(), draft.expression(), draft.facets(),
				draft.textFacets(), draft.source(), marker);
		ActionBodyContext full = Objects.requireNonNull(context(marked), "Cannot build the layer editor context.");
		String text = full.text("");
		int offset = text.indexOf(marker);
		if (offset < 0 || text.indexOf(marker, offset + marker.length()) >= 0)
			throw new IllegalStateException("The layer context must contain the candidate body exactly once.");
		return new ActionBodyContext(full.uri(), text.substring(0, offset), text.substring(offset + marker.length()));
	}

	protected EmbeddedActionBody createEmbeddedEditor(final Composite parent, final ActionBodyContext context, final String initial) {
		return EmbeddedActionBody.create(parent, context, initial);
	}

	protected String browseImage() {
		FileDialog dialog = new FileDialog(expression.getShell(), SWT.OPEN);
		dialog.setText("Choose image");
		dialog.setFilterExtensions(new String[] { "*.png;*.jpg;*.jpeg;*.gif;*.tif;*.tiff;*.bmp", "*" });
		return dialog.open();
	}

	protected String chooseColor(final String current) {
		var color = new ColorDialog(expression.getShell()).open();
		return color == null ? null : "rgb(" + color.red + ", " + color.green + ", " + color.blue + ")";
	}

	public LayerDraft readDraft() {
		LayerDraft draft = new LayerDraft(original.id(), original.kind(), expression == null ? "" : expression.getText(),
				facets, textFacets, original.source(), body);
		return draft.equals(original) ? original : draft;
	}

	private String localError(final LayerDraft draft) {
		String error = draft.validationError();
		if (error != null) return error;
		try {
			LayerDraft parsed = DisplaySource.parseLayer(DisplaySource.renderLayer(draft, parser()), draft.id(), parser());
			boolean graphics = draft.kind().keyword().equals("graphics") && parsed.kind().keyword().equals("graphics");
			if (parsed.kind() != draft.kind() && !graphics)
				return "The declaration must remain a " + draft.kind().title() + " layer.";
		} catch (IllegalArgumentException failure) {
			return failure.getMessage();
		}
		return null;
	}

	private void changed() {
		if (!ready) return;
		LayerDraft draft = readDraft();
		String error = localError(draft);
		try {
			preview.setText(DisplaySource.renderLayer(draft, parser()));
		} catch (IllegalArgumentException failure) {
			preview.setText("Cannot generate GAML: " + failure.getMessage());
			error = failure.getMessage();
		}
		host.setErrorMessage(error);
		host.setComplete(error == null);
	}

	protected List<ActionVerifier.Problem> compile(final ActionBodyContext context) {
		return ActionVerifier.verify(context, "");
	}

	private ActionBodyContext context(final LayerDraft draft) {
		if (context != null) return context.apply(draft);
		return new ActionBodyContext(URI.createURI("synthetic:/layer-wizard/.layer-wizard.gaml"),
				"model layer_wizard\nexperiment e type: gui { output { display d type: opengl {\n"
						+ DisplaySource.renderLayer(draft, parser()) + "\n} } }\n", "");
	}

	@Override
	public boolean validateNow() {
		LayerDraft draft = readDraft();
		String[] error = { localError(draft) };
		if (error[0] == null) BusyIndicator.showWhile(preview.getDisplay(), () -> {
			try {
				if (baseline == null) {
					baseline = new java.util.HashSet<>();
					if (!original.source().isEmpty() && localError(original) == null)
						for (var problem : compile(Objects.requireNonNull(context(original))))
							baseline.add(problem.message());
				}
				for (var problem : compile(Objects.requireNonNull(context(draft))))
					if (!baseline.contains(problem.message())) { error[0] = problem.toString(); break; }
			} catch (RuntimeException | LinkageError failure) {
				error[0] = "Cannot verify the layer: " + failure.getMessage();
			}
		});
		if (error[0] == null) error[0] = validator.apply(draft);
		host.setErrorMessage(error[0]);
		host.setComplete(localError(draft) == null);
		return error[0] == null;
	}

	@Override
	public boolean accept() {
		result = null;
		if (!isCreationMode() && readDraft() == original && localError(original) == null) {
			String error = validator.apply(original);
			host.setErrorMessage(error);
			if (error != null) return false;
		} else if (!validateNow()) return false;
		result = readDraft();
		return true;
	}

	@Override public LayerDraft getResult() { return result; }
	@Override protected void clearResult() { result = null; }
}
