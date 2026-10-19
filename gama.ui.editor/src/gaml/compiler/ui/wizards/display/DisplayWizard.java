package gaml.compiler.ui.wizards.display;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.xtext.parser.IParser;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

import gama.ui.editor.internal.EditorActivator;
import gaml.compiler.GamlStandaloneSetup;
import gaml.compiler.ui.wizards.action.ActionBodyContext;
import gaml.compiler.ui.wizards.action.ActionVerifier;
import gaml.compiler.ui.wizards.action.EmbeddedActionBody;
import gaml.compiler.ui.wizards.experiment.ExperimentDraft;
import gaml.compiler.ui.wizards.species.SpeciesDraft.Facet;
import gaml.compiler.ui.wizards.species.SpeciesWizard.FormHost;
import gaml.compiler.ui.wizards.species.WizardWidgets;
import gaml.compiler.ui.wizards.species.BooleanFacetField;
import gaml.compiler.ui.wizards.species.DeclarationWizard;

/**
 * Shared transactional display settings editor with a generated (not runtime) preview. Layer editing is currently
 * not exposed; existing ordered layer drafts and body source are retained unchanged. All source
 * compilation and adapter validation is explicit, on Validate/acceptance only. Adapters own live objects and undo.
 * Settings intentionally exposes only its dedicated controls. Other facets, and nonliteral values behind literal-only
 * checkboxes or the background color chooser, are retained verbatim until explicitly replaced, never evaluated.
 */
public class DisplayWizard extends DeclarationWizard<DisplayDraft> {
	public static final String FIELD = "display.field";
	private final DisplayDraft original;
	private final Function<DisplayDraft, String> validator;
	private final Function<DisplayDraft, ActionBodyContext> context;
	private final Supplier<List<String>> species;
	private final Supplier<List<String>> grids;
	private final Function<String, List<String>> aspects;
	private final List<LayerDraft> layers;
	private List<Facet> facets;
	private FormHost host;
	private Text name;
	private Combo renderer;
	private Button twoD, threeD;
	private Composite threeDFields;
	private Composite settingsBody;
	private org.eclipse.swt.widgets.List rows;
	private Combo addKind;
	private Composite inspector;
	private ScrolledComposite inspectorScroll;
	private StyledText preview;
	private Button duplicate, remove, up, down;
	private DisplayDraft result;
	private boolean ready;
	private boolean refreshing;
	private Set<String> baseline;

	public DisplayWizard(final Shell shell, final DisplayDraft draft, final Function<DisplayDraft, String> validator,
			final Supplier<List<String>> speciesNames, final Supplier<List<String>> gridNames,
			final Function<DisplayDraft, ActionBodyContext> context) {
		this(shell, draft, validator, speciesNames, gridNames, context, null);
	}

	/** Suggestions never restrict expressions. Null suppliers use empty suggestions; null context uses a synthetic model. */
	public DisplayWizard(final Shell shell, final DisplayDraft draft, final Function<DisplayDraft, String> validator,
			final Supplier<List<String>> speciesNames, final Supplier<List<String>> gridNames,
			final Function<DisplayDraft, ActionBodyContext> context, final Function<String, List<String>> aspectsBySpecies) {
		super(shell);
		original = Objects.requireNonNull(draft);
		this.validator = Objects.requireNonNull(validator);
		this.context = context;
		species = speciesNames == null ? List::of : speciesNames;
		grids = gridNames == null ? List::of : gridNames;
		aspects = aspectsBySpecies == null ? s -> List.of() : aspectsBySpecies;
		facets = draft.facets();
		layers = new ArrayList<>(draft.layers());
	}

	private static class StandaloneParser {
		private static final IParser INSTANCE = GamlStandaloneSetup.doSetup().getInstance(IParser.class);
	}

	protected IParser parser() {
		var activator = EditorActivator.getInstance();
		return activator == null ? StandaloneParser.INSTANCE
				: activator.getInjector(EditorActivator.GAML_COMPILER_GAML).getInstance(IParser.class);
	}

	public static ActionBodyContext standalone(final DisplayDraft draft, final IParser parser) {
		return new ActionBodyContext(URI.createURI("synthetic:/display-wizard/.display-wizard.gaml"),
				"model display_wizard\nexperiment e type: gui { output {\n" + DisplaySource.renderDisplay(draft, parser)
						+ "\n} }\n", "");
	}

	@Override
	protected String dialogTitle() { return "Display"; }

	/** Builds the same form in a graphical editor's container. */
	public Composite createForm(final Composite parent, final FormHost formHost) {
		host = Objects.requireNonNull(formHost);
		host.setTitle("Display settings");
		host.setMessage("Configure the display settings. Existing layers are kept unchanged. GAML is verified on Validate and acceptance.");
		Composite form = WizardWidgets.composite(parent, 1);
		TabFolder tabs = new TabFolder(form, SWT.NONE);
		GridData tabData = new GridData(SWT.FILL, SWT.FILL, true, true);
		tabData.widthHint = 760;
		tabData.heightHint = 390;
		tabs.setLayoutData(tabData);
		field(tabs, "Tabs");
		Composite settings = page(tabs, "Settings");
		ScrolledComposite settingsScroll = scroll(settings);
		settingsBody = WizardWidgets.composite(settingsScroll, 1);
		settingsScroll.setContent(settingsBody);
		Composite first = settingsRow(settingsBody, 7, "Settings row 1");
		first.setData("wizard.section", "Identity");
		name = WizardWidgets.textField(first, "Name", original.name(), "Plain display name; quoted labels are supported.",
				FIELD, "Name", 160);
		settingsExpression(first, "title", "", List.of());
		Composite dimensions = WizardWidgets.composite(first, 3);
		dimensions.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		twoD = new Button(dimensions, SWT.RADIO);
		twoD.setText("2D");
		field(twoD, "2D");
		threeD = new Button(dimensions, SWT.RADIO);
		threeD.setText("3D");
		field(threeD, "3D");
		renderer = new Combo(dimensions, SWT.DROP_DOWN);
		renderer.setItems("", "java2D", "opengl", "\"2d\"", "\"3d\"");
		renderer.setText(original.renderer());
		fill(renderer, "Renderer");
		renderer.setToolTipText("Preserves an omitted or custom renderer exactly. Empty follows the live display preference.");
		twoD.setToolTipText("Use the 2D renderer. New displays start in 2D.");
		threeD.setToolTipText("Use the 3D renderer and show its controls.");
		twoD.addListener(SWT.Selection, e -> { if (twoD.getSelection()) selectDimension(false); });
		threeD.addListener(SWT.Selection, e -> { if (threeD.getSelection()) selectDimension(true); });
		settingsFlag(first, "virtual", false);
		Composite second = settingsRow(settingsBody, 8, "Settings row 2");
		settingsExpression(second, "refresh", "true", List.of("true", "false"));
		settingsExpression(second, "autosave", "false", List.of("false", "true"));
		settingsFlag(second, "antialias", gama.api.utils.prefs.GamaPreferences.Displays.CORE_ANTIALIAS.getValue());
		settingsFlag(second, "fullscreen", false);
		Composite third = settingsRow(settingsBody, 4, "Settings row 3");
		settingsBackground(third);
		settingsExpression(third, "toolbar", "true", List.of("true", "false"));
		threeDFields = settingsRow(settingsBody, 1, "3D settings");
		Composite fifth = settingsRow(threeDFields, 6, "Settings row 5");
		settingsFlag(fifth, "light", true);
		settingsFlag(fifth, "axes", true);
		settingsExpression(fifth, "keystone", "", List.of());
		Composite sixth = settingsRow(threeDFields, 6, "Settings row 6");
		List<String> cameras = new ArrayList<>();
		cameras.add("\"default\"");
		for (String preset : gama.api.ui.layers.ICameraDefinition.PRESETS)
			cameras.add("\"" + preset + "\"");
		settingsExpression(sixth, "camera", "\"default\"", cameras);
		settingsExpression(sixth, "z_near", "", List.of());
		settingsExpression(sixth, "z_far", "", List.of());
		updateDimension();
		settingsBody.addListener(SWT.Resize, e -> resizeScroll(settingsScroll, settingsBody));
		settingsScroll.addListener(SWT.Resize, e -> resizeScroll(settingsScroll, settingsBody));
		resizeScroll(settingsScroll, settingsBody);

		WizardWidgets.label(form, "Generated GAML (read-only)", "Source preview only. Runtime rendering is not part of this editor.");
		preview = new StyledText(form, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY);
		preview.setFont(JFaceResources.getTextFont());
		GridData previewData = new GridData(SWT.FILL, SWT.FILL, true, false);
		previewData.heightHint = 120;
		preview.setLayoutData(previewData);
		field(preview, "Preview");
		name.addModifyListener(e -> changed());
		renderer.addModifyListener(e -> { updateDimension(); changed(); });
		ready = true;
		changed();
		return form;
	}

	private Composite settingsRow(final Composite parent, final int columns, final String key) {
		Composite row = WizardWidgets.composite(parent, columns);
		row.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		field(row, key);
		return row;
	}

	private DisplayFacet displayFacet(final String key) {
		return DisplayFacet.of("display").stream().filter(f -> f.name().equals(key)).findFirst()
				.orElseThrow(() -> new IllegalStateException("Display facet not declared: " + key));
	}

	private void setDisplayFacet(final String key, final String value) {
		facets = ExperimentDraft.withFacet(facets, original.facets(), key, value);
		changed();
	}

	private void settingsFlag(final Composite parent, final String key, final boolean initial) {
		DisplayFacet metadata = displayFacet(key);
		String value = DisplayDraft.find(facets, key);
		WizardWidgets.literalBoolean(parent, key, value, initial, () -> metadata.defaultValue("display", facets),
				metadata.documentation(), FIELD, "display:" + key, next -> setDisplayFacet(key, next));
	}

	private void settingsBackground(final Composite parent) {
		DisplayFacet metadata = displayFacet("background");
		WizardWidgets.label(parent, "Background", metadata.documentation());
		Button choose = button(parent, "Choose color...", () -> {
			String chosen = chooseColor(DisplayDraft.find(facets, "background"));
			if (chosen != null) setDisplayFacet("background", chosen);
		});
		field(choose, "display:choose:background");
		choose.setToolTipText("Choose a background color. Existing GAML, including expressions, is preserved until a color is chosen.");
	}

	private void settingsExpression(final Composite parent, final String key, final String initial,
			final List<String> choices) {
		DisplayFacet metadata = displayFacet(key);
		String value = DisplayDraft.find(facets, key);
		String help = metadata.documentation() + " (" + metadata.type() + "). Initial UI value: "
				+ (initial.isEmpty() ? "empty" : initial) + ". Untouched omitted facets remain omitted.";
		Consumer<String> modified = text -> {
			String next = text.isBlank() ? null : text;
			Boolean runtimeDefault = metadata.defaultValue("display", facets);
			if (next != null && runtimeDefault != null && next.trim().equals(Boolean.toString(runtimeDefault))) next = null;
			if ("camera".equals(key) && "\"default\"".equals(next) && DisplayDraft.find(facets, "parent") == null) next = null;
			setDisplayFacet(key, next);
		};
		if ("z_near".equals(key) || "z_far".equals(key)) {
			Text input = WizardWidgets.textField(parent, key.replace('_', '-'), value == null ? initial : value,
					help + " Enter a GAML float expression; empty uses runtime clipping.", FIELD, "display:" + key, 160);
			input.addModifyListener(e -> modified.accept(input.getText()));
		} else {
			Combo input = WizardWidgets.choiceField(parent, key, value == null ? initial : value, choices, true,
					help, FIELD, "display:" + key, 160);
			input.addModifyListener(e -> modified.accept(input.getText()));
		}
	}

	/** null denotes a plugin/custom renderer; omitted types follow the live renderer preference. */
	private static Boolean dimension(final String type) {
		if (type.isBlank()) return !gama.api.utils.prefs.GamaPreferences.Displays.CORE_DISPLAY.getValue();
		return switch (type.trim()) {
			case "java2D", "\"java2D\"", "'java2D'", "\"2d\"", "'2d'" -> false;
			case "opengl", "\"opengl\"", "'opengl'", "\"3d\"", "'3d'" -> true;
			default -> null;
		};
	}

	private void selectDimension(final boolean is3D) {
		Boolean before = dimension(original.renderer());
		renderer.setText(before != null && before == is3D ? original.renderer() : is3D ? "opengl" : "java2D");
	}

	private void updateDimension() {
		Boolean is3D = dimension(renderer.getText());
		twoD.setSelection(Boolean.FALSE.equals(is3D));
		threeD.setSelection(Boolean.TRUE.equals(is3D));
		boolean custom = is3D == null || renderer.getText().isBlank();
		renderer.setVisible(custom);
		((GridData) renderer.getLayoutData()).exclude = !custom;
		boolean visible = Boolean.TRUE.equals(is3D);
		threeDFields.setVisible(visible);
		((GridData) threeDFields.getLayoutData()).exclude = !visible;
		settingsBody.layout(true, true);
		resizeScroll((ScrolledComposite) settingsBody.getParent(), settingsBody);
		host.relayout();
	}

	private void move(final int delta) {
		int index = rows.getSelectionIndex();
		java.util.Collections.swap(layers, index, index + delta);
		refreshRows(index + delta);
		changed();
	}

	private void refreshRows(final int selected) {
		rows.setItems(layers.stream().map(l -> l.kind() + (l.expression().isEmpty() ? "" : "  " + l.expression())).toArray(String[]::new));
		if (selected >= 0) rows.setSelection(selected);
		updateButtons();
	}

	private void updateButtons() {
		int index = rows.getSelectionIndex();
		duplicate.setEnabled(index >= 0); remove.setEnabled(index >= 0);
		up.setEnabled(index > 0); down.setEnabled(index >= 0 && index < layers.size() - 1);
	}

	private void showLayer() {
		refreshing = true;
		try {
			for (var child : inspector.getChildren()) child.dispose();
			updateButtons();
			int selected = rows.getSelectionIndex();
			if (selected < 0) { WizardWidgets.label(inspector, "No layer selected", "Add a layer to begin."); return; }
			var layer = layers.get(selected);
			Label title = new Label(inspector, SWT.NONE);
			title.setText(WizardWidgets.labelText(layer.kind() + " (kind fixed; add a new layer to choose another kind)"));
			if (layer.kind() == LayerKind.RAW) {
				Label note = new Label(inspector, SWT.WRAP);
				note.setText("Source-backed declaration. Charts, drawing code and other unsupported declarations are kept as GAML.");
				note.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
				Control code = createRawEditor(inspector, layer.source(), text -> {
					var current = layers.get(rows.getSelectionIndex());
					replaceSelected(new LayerDraft(current.id(), current.kind(), current.expression(), current.facets(),
							current.textFacets(), text));
				});
				GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
				data.heightHint = 230;
				code.setLayoutData(data);
				field(code, "Raw source");
				return;
			}
			WizardWidgets.label(inspector, switch (layer.kind()) {
				case SPECIES, GRID -> "Species";
				case AGENTS -> "Layer label (GAML, optional)";
				case IMAGE -> "Image expression (GAML)";
				case TEXT -> "Text expression (GAML)";
				default -> throw new IllegalStateException();
			}, "Editable GAML expression; suggestions are not restrictions.");
			Composite expressionParent = inspector;
			if (layer.kind() == LayerKind.IMAGE) {
				expressionParent = WizardWidgets.composite(inspector, 2);
				expressionParent.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			}
			Combo expression = new Combo(expressionParent, SWT.DROP_DOWN);
			expression.setItems((layer.kind() == LayerKind.GRID ? grids.get()
					: layer.kind() == LayerKind.SPECIES ? species.get() : List.<String>of()).toArray(String[]::new));
			expression.setText(layer.expression());
			fill(expression, "Expression");
			if (layer.kind() == LayerKind.IMAGE) button(expressionParent, "Browse...", () -> {
				String file = browseImage();
				if (file != null) expression.setText(ExperimentDraft.quote(file));
			});
			Consumer<List<Facet>> setOuter = values -> {
				var current = layers.get(rows.getSelectionIndex());
				replaceSelected(new LayerDraft(current.id(), current.kind(), current.expression(), values,
						current.textFacets(), current.source()));
			};
			facetFields(inspector, layer.kind().compilerKeyword(),
					layer.kind().expressionFacet() == null ? List.of() : List.of(layer.kind().expressionFacet()),
					layer.facets(), layer.facets(), setOuter,
					n -> "aspect".equals(n) && layer.kind() == LayerKind.SPECIES ? aspects.apply(expression.getText())
							: "value".equals(n) && layer.kind() == LayerKind.AGENTS ? species.get() : List.of(), "layer:");
			if (layer.kind() == LayerKind.TEXT) {
				WizardWidgets.label(inspector, "Text drawing", "Facets of the single draw statement; outer graphics settings are above.");
				facetFields(inspector, "draw", List.of("geometry"), layer.textFacets(), layer.textFacets(), values -> {
					var current = layers.get(rows.getSelectionIndex());
					replaceSelected(new LayerDraft(current.id(), current.kind(), current.expression(), current.facets(),
							values, current.source()));
				}, n -> List.of(), "draw:");
			}
			expression.addModifyListener(e -> {
				var current = layers.get(rows.getSelectionIndex());
				replaceSelected(new LayerDraft(current.id(), current.kind(), expression.getText(), current.facets(),
						current.textFacets(), current.source()));
			});
		} finally {
			refreshing = false;
			inspector.layout(true, true);
			resizeScroll(inspectorScroll, inspector);
		}
	}

	/** Actual embedded Xtext editor in the running UI; plain SWT only when the workbench is unavailable. */
	protected Control createRawEditor(final Composite parent, final String initial, final Consumer<String> modified) {
		var context = new ActionBodyContext(URI.createURI("synthetic:/display-wizard/.layer-wizard.gaml"),
				"model display_wizard\nexperiment e type: gui { output { display d {\n", "\n} } }\n");
		var embedded = createEmbeddedEditor(parent, context, initial);
		if (embedded != null) {
			embedded.control().addListener(SWT.Modify, e -> modified.accept(embedded.text()));
			return embedded.control();
		}
		StyledText text = new StyledText(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		text.setText(initial);
		text.addModifyListener(e -> modified.accept(text.getText()));
		return text;
	}

	/** Factory seam for the real embedded viewer (also used by the native regression harness without a workbench). */
	protected EmbeddedActionBody createEmbeddedEditor(final Composite parent, final ActionBodyContext context, final String initial) {
		return EmbeddedActionBody.create(parent, context, initial);
	}

	/** Selection assists do not constrain, evaluate or replace expressions unless the user confirms a choice. */
	protected String browseImage() {
		FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
		dialog.setText("Choose image");
		dialog.setFilterExtensions(new String[] { "*.png;*.jpg;*.jpeg;*.gif;*.tif;*.tiff;*.bmp", "*" });
		return dialog.open();
	}

	protected String chooseColor(final String current) {
		ColorDialog dialog = new ColorDialog(getShell());
		dialog.setText("Choose color");
		var color = dialog.open();
		return color == null ? null : "rgb(" + color.red + ", " + color.green + ", " + color.blue + ")";
	}

	private void replaceSelected(final LayerDraft next) {
		if (refreshing) return;
		int index = rows.getSelectionIndex();
		layers.set(index, next);
		rows.setItem(index, next.kind() + (next.expression().isEmpty() ? "" : "  " + next.expression()));
		changed();
	}

	/** All declared facets plus unrecognized explicit facets; advanced controls never rewrite hidden values. */
	private void facetFields(final Composite parent, final String keyword, final List<String> excluded,
			final List<Facet> values, final List<Facet> originalValues, final Consumer<List<Facet>> modified,
			final Function<String, List<String>> suggestions, final String keyPrefix) {
		List<Facet> current = new ArrayList<>(values);
		List<DisplayFacet> metadata = new ArrayList<>(DisplayFacet.of(keyword));
		for (var f : values) if (metadata.stream().noneMatch(m -> m.name().equals(f.name())))
			metadata.add(new DisplayFacet(f.name(), "expression", false, false, "Unrecognized facet, kept verbatim."));
		Composite common = WizardWidgets.composite(parent, 2);
		common.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		ExpandableComposite advanced = new ExpandableComposite(parent, SWT.NONE,
				ExpandableComposite.TWISTIE | ExpandableComposite.CLIENT_INDENT);
		advanced.setText("Advanced");
		advanced.setToolTipText("Expand to configure additional facets.");
		advanced.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		field(advanced, keyPrefix + "Advanced");
		WizardWidgets.accessible(advanced, "Advanced");
		Composite other = WizardWidgets.composite(advanced, 2);
		advanced.setClient(other);
		advanced.setExpanded(false);
		Set<String> primary = Set.of("title", "background", "refresh", "visible", "aspect", "value", "elevation", "border",
				"gis", "color", "at", "font");
		for (var facet : metadata) if (!excluded.contains(facet.name())) {
			Composite target = primary.contains(facet.name()) || facet.required() ? common : other;
			String value = DisplayDraft.find(values, facet.name());
			Consumer<String> set = next -> {
				var updated = ExperimentDraft.withFacet("display".equals(keyword) ? facets : current,
						originalValues, facet.name(), next);
				current.clear(); current.addAll(updated);
				modified.accept(List.copyOf(current));
			};
			if (facet.flag()) {
				new BooleanFacetField(target, facet.name(), value,
						() -> facet.defaultValue(keyword, "display".equals(keyword) ? facets : current), facet.required(),
						facet.documentation() + ("display".equals(keyword)
								&& Set.of("antialias", "axes", "draw_env").contains(facet.name())
										? " Default follows the live display preference." : ""),
						FIELD, keyPrefix + facet.name(), set);
			} else {
				WizardWidgets.label(target, facet.name() + (facet.required() ? " *" : ""),
						facet.documentation() + " (" + facet.type() + ")");
				boolean color = facet.type().contains("rgb") || facet.type().contains("color");
				Composite inputParent = target;
				if (color) inputParent = WizardWidgets.composite(target, 2);
				if (color) inputParent.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				Combo input = new Combo(inputParent, SWT.DROP_DOWN);
				input.setItems(suggestions.apply(facet.name()).toArray(String[]::new));
				input.setText(value == null ? "" : value);
				fill(input, keyPrefix + facet.name());
				input.setToolTipText(facet.documentation() + " (" + facet.type() + ")");
				if (color) {
					Button choose = button(inputParent, "Choose color...", () -> {
						String chosen = chooseColor(input.getText());
						if (chosen != null) input.setText(chosen);
					});
					field(choose, keyPrefix + "choose:" + facet.name());
				}
				input.addListener(SWT.MouseDown, e -> {
					String text = input.getText();
					// Refresh model-aware choices without emitting a fake edit.
					String[] choices = suggestions.apply(facet.name()).toArray(String[]::new);
					if (!java.util.Arrays.equals(input.getItems(), choices)) {
						input.setItems(choices); input.setText(text);
					}
				});
				input.addModifyListener(e -> set.accept(input.getText().isBlank() ? null : input.getText()));
			}
		}
		other.setVisible(false);
		advanced.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(final ExpansionEvent event) {
				parent.layout(true, true);
				Composite p = parent;
				while (p != null && !(p instanceof ScrolledComposite)) p = p.getParent();
				if (p instanceof ScrolledComposite scroll) resizeScroll(scroll, (Composite) scroll.getContent());
				host.relayout();
			}
		});
	}

	public DisplayDraft readDraft() {
		DisplayDraft draft = new DisplayDraft(name.getText(), renderer.getText(), facets, layers, original.trailingSource());
		return draft.equals(original) ? original : draft;
	}

	private String localError(final DisplayDraft draft) {
		String error = draft.validationError();
		if (error != null) return error;
		for (var layer : draft.layers()) if (layer.kind() == LayerKind.RAW) {
			try { DisplaySource.parseLayer(layer.source(), layer.id(), parser()); }
			catch (IllegalArgumentException failure) { return "Layer GAML: " + failure.getMessage(); }
		}
		for (var layer : draft.layers()) for (var before : original.layers())
			if (layer.id().equals(before.id()) && layer.kind() != before.kind()) return "Existing layer kinds are fixed.";
		return null;
	}

	private void changed() {
		if (!ready || refreshing) return;
		BooleanFacetField.refreshDefaults(name.getParent().getParent());
		DisplayDraft draft = readDraft();
		String error = localError(draft);
		try {
			preview.setText(DisplaySource.renderDisplay(draft, parser()));
		} catch (IllegalArgumentException failure) {
			error = failure.getMessage();
			preview.setText("Cannot generate GAML: " + error);
		}
		host.setErrorMessage(error);
		host.setComplete(error == null);
	}

	public boolean validateNow() {
		DisplayDraft draft = readDraft();
		String[] error = { localError(draft) };
		if (error[0] == null) BusyIndicator.showWhile(name.getDisplay(), () -> {
			try {
				var currentContext = context(draft);
				if (currentContext == null) { error[0] = "Cannot build the display context."; return; }
				var problems = compile(currentContext);
				Set<String> previous = baseline();
				for (var problem : problems) if (!previous.contains(problem.message())) { error[0] = problem.toString(); break; }
			} catch (RuntimeException | LinkageError failure) { error[0] = "Cannot verify the display: " + failure.getMessage(); }
		});
		if (error[0] == null) error[0] = validator.apply(draft);
		host.setErrorMessage(error[0]);
		return error[0] == null;
	}

	private ActionBodyContext context(final DisplayDraft draft) {
		return context == null ? standalone(draft, parser()) : context.apply(draft);
	}

	protected List<ActionVerifier.Problem> compile(final ActionBodyContext source) { return ActionVerifier.verify(source, ""); }

	private Set<String> baseline() {
		if (baseline != null) return baseline;
		baseline = new HashSet<>();
		if (original.validationError() != null) return baseline;
		var initial = context(original);
		if (initial == null) return baseline;
		for (var problem : compile(initial)) baseline.add(problem.message());
		return baseline;
	}

	public boolean accept() {
		result = null;
		if (readDraft() == original && original.validationError() == null) {
			String error = validator.apply(original);
			host.setErrorMessage(error);
			if (error != null) return false;
			result = original; return true;
		}
		if (!validateNow()) return false;
		result = readDraft();
		return true;
	}

	public DisplayDraft getResult() { return result; }
	@Override protected void clearResult() { result = null; }

	private static Composite page(final TabFolder tabs, final String title) {
		TabItem tab = new TabItem(tabs, SWT.NONE);
		tab.setText(title);
		Composite content = WizardWidgets.composite(tabs, 1);
		tab.setControl(content);
		return content;
	}
	private static ScrolledComposite scroll(final Composite parent) {
		ScrolledComposite scroll = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		scroll.setExpandHorizontal(true); scroll.setExpandVertical(true);
		scroll.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		return scroll;
	}
	private static void resizeScroll(final ScrolledComposite scroll, final Composite content) {
		scroll.setMinSize(content.computeSize(Math.max(300, scroll.getClientArea().width), SWT.DEFAULT));
	}
	private static Button button(final Composite parent, final String label, final Runnable run) {
		Button button = WizardWidgets.button(parent, label, label);
		field(button, label);
		button.addListener(SWT.Selection, e -> run.run());
		return button;
	}
	private static void fill(final Control control, final String label) {
		GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		data.widthHint = 230;
		control.setLayoutData(data);
		field(control, label);
	}
	private static void field(final Control control, final String label) {
		control.setData(FIELD, label);
		WizardWidgets.accessible(control, label);
	}
}
