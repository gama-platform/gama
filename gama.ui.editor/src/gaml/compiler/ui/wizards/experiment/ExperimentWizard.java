package gaml.compiler.ui.wizards.experiment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import gaml.compiler.ui.wizards.action.ActionBodyContext;
import gaml.compiler.ui.wizards.action.ActionVerifier;
import gaml.compiler.ui.wizards.action.EmbeddedActionBody;
import gaml.compiler.ui.wizards.species.AttributeTable;
import gaml.compiler.ui.wizards.species.SpeciesDraft.Facet;
import gaml.compiler.ui.wizards.species.SpeciesWizard.FormHost;
import gaml.compiler.ui.wizards.species.DeclarationWizard;
import gaml.compiler.ui.wizards.species.WizardWidgets;

/**
 * Detached experiment settings for gui, batch and test experiments: name and type, the header facets declared by the
 * compiler (check boxes for literal booleans, expressions otherwise) with a section specific to the type, attributes
 * (shared {@link AttributeTable}), parameters, monitors and the init code (embedded GAML editor below a live
 * declaration label). Changing the type reorganises the form but never discards values: settings irrelevant to the
 * new type are kept and reported as a warning. Compilation runs only on Validate and acceptance; adapters own the undoable
 * apply and preserve the statements the draft does not represent.
 */
public class ExperimentWizard extends DeclarationWizard<ExperimentDraft> {

	/** Data key identifying the form's controls (for tests and accessibility tools). */
	public static final String FIELD = "experiment.field";

	/** The init code area. The default implementation embeds the GAML editor, falling back to plain text. */
	protected interface BodyEditor {
		Control control();

		String text();
	}

	private final ExperimentDraft original;
	private final Function<ExperimentDraft, String> validator;
	private final Function<ExperimentDraft, ActionBodyContext> bodyContext;
	private final List<String> typeNames;
	private final Supplier<List<String>> variableNames;
	private List<Facet> facets;
	private final List<ExperimentDraft.Parameter> parameters = new ArrayList<>();
	private final List<ExperimentDraft.Monitor> monitors = new ArrayList<>();
	private String type;
	private FormHost host;
	private Text name;
	private Combo typeCombo;
	private final Supplier<List<String>> experimentNames;
	private Composite commonArea;
	private Composite capabilitiesArea;
	private Group typeArea;
	private Composite otherArea;
	private TabFolder tabs;
	private Composite attributesPage;
	private Composite parametersPage;
	private Composite monitorsPage;
	private Composite initPage;
	private AttributeTable attributes;
	private Table parameterTable;
	private Composite parameterDetail;
	private Button removeParameter;
	private Table monitorTable;
	private Composite monitorDetail;
	private Button removeMonitor;
	private Label signature;
	private BodyEditor init;
	private ExperimentDraft result;
	private Set<String> baseline;
	private boolean loading;
	private boolean ready;

	/**
	 * Edition or creation with adapter validation only (no contextual compilation in the dialog).
	 *
	 * @param draft
	 *            initial values; returned unchanged (same instance) when the form is not modified
	 * @param validator
	 *            run on Validate/acceptance after the local checks; returns an error or null
	 * @param typeNames
	 *            types offered for attributes
	 * @param variableNames
	 *            variables offered for parameters (the experiment's attributes are added)
	 */
	public ExperimentWizard(final Shell shell, final ExperimentDraft draft,
			final Function<ExperimentDraft, String> validator, final List<String> typeNames,
			final Supplier<List<String>> variableNames) {
		this(shell, draft, validator, typeNames, variableNames, null);
	}

	/**
	 * @param bodyContext
	 *            the source containing the rendered experiment, split around the init code (when the draft has no
	 *            init code: the whole source as prefix); compiled on Validate/acceptance for locally valid drafts. Errors
	 *            outside the code already present for the initial draft are ignored. Null skips this step.
	 */
	public ExperimentWizard(final Shell shell, final ExperimentDraft draft,
			final Function<ExperimentDraft, String> validator, final List<String> typeNames,
			final Supplier<List<String>> variableNames, final Function<ExperimentDraft, ActionBodyContext> bodyContext) {
		this(shell, draft, validator, typeNames, variableNames, bodyContext, List::of);
	}

	/**
	 * As above, with existing experiment names offered as parents (the adapter excludes self/descendants).
	 * The header is Name, Title, Type, Parent, Virtual. Parent and Control are editable lists, retaining loaded values
	 * not supplied by the adapter or architecture registry. Older constructors offer no parent candidates.
	 */
	public ExperimentWizard(final Shell shell, final ExperimentDraft draft,
			final Function<ExperimentDraft, String> validator, final List<String> typeNames,
			final Supplier<List<String>> variableNames, final Function<ExperimentDraft, ActionBodyContext> bodyContext,
			final Supplier<List<String>> experimentNames) {
		super(shell);
		original = Objects.requireNonNull(draft);
		this.validator = Objects.requireNonNull(validator);
		this.typeNames = List.copyOf(typeNames);
		this.variableNames = Objects.requireNonNull(variableNames);
		this.bodyContext = bodyContext;
		this.experimentNames = Objects.requireNonNull(experimentNames);
		type = draft.type();
		facets = draft.facets();
		parameters.addAll(draft.parameters());
		monitors.addAll(draft.monitors());
	}

	/** A minimal synthetic model containing only the experiment (a model without global attributes). */
	public static ActionBodyContext standalone(final ExperimentDraft draft) {
		URI uri = URI.createURI("synthetic:/experiment-wizard/.experiment-wizard.gaml");
		String rendered = render(draft, "\u0000");
		int marker = rendered.indexOf('\u0000');
		String model = "model experiment_wizard\n\nglobal {\n}\n\n";
		return new ActionBodyContext(uri, model + rendered.substring(0, marker), rendered.substring(marker + 1));
	}

	/** Renders a complete experiment declaration; {@code initCode} replaces the init code (when non-null). */
	public static String render(final ExperimentDraft draft, final String initCode) {
		StringBuilder text = new StringBuilder(draft.header()).append(" {\n");
		for (var variable : draft.variables()) {
			text.append('\t').append(variable.type()).append(' ').append(variable.name());
			if (!variable.init().isBlank()) text.append(" <- ").append(variable.init().trim());
			if (!variable.update().isBlank()) text.append(" update: ").append(variable.update().trim());
			if (!variable.function().isBlank()) text.append(" -> ").append(variable.function().trim());
			if (!variable.min().isBlank()) text.append(" min: ").append(variable.min().trim());
			if (!variable.max().isBlank()) text.append(" max: ").append(variable.max().trim());
			text.append(";\n");
		}
		for (var parameter : draft.parameters()) text.append('\t').append(parameter(parameter)).append('\n');
		String code = initCode == null ? draft.init() : initCode;
		if (initCode != null || !draft.init().isBlank()) text.append("\tinit {").append(code).append("}\n");
		if (!draft.monitors().isEmpty()) {
			text.append("\toutput {\n");
			for (var monitor : draft.monitors()) text.append("\t\t").append(monitor(monitor)).append('\n');
			text.append("\t}\n");
		}
		return text.append("}\n").toString();
	}

	/** {@code parameter "title" var: variable facets...;} */
	public static String parameter(final ExperimentDraft.Parameter parameter) {
		StringBuilder text = new StringBuilder("parameter ").append(ExperimentDraft.quote(parameter.title()))
				.append(" var: ").append(parameter.variable().trim());
		for (var facet : parameter.facets()) text.append(' ').append(facet.name()).append(": ").append(facet.value().trim());
		return text.append(';').toString();
	}

	/** {@code monitor "title" value: expression facets...;} */
	public static String monitor(final ExperimentDraft.Monitor monitor) {
		StringBuilder text = new StringBuilder("monitor ").append(ExperimentDraft.quote(monitor.title()))
				.append(" value: ").append(monitor.value().trim());
		for (var facet : monitor.facets()) text.append(' ').append(facet.name()).append(": ").append(facet.value().trim());
		return text.append(';').toString();
	}

	@Override
	protected String dialogTitle() { return "Experiment"; }

	@Override
	protected void showFormMessage(final String message) {
		setMessage(message, message != null && message.startsWith(WARNING) ? IMessageProvider.WARNING : IMessageProvider.NONE);
	}

	@Override
	protected void showValidationSuccess() {
		if (readDraft().inactiveFacets().isEmpty()) super.showValidationSuccess();
	}

	private static final String WARNING = "Kept but not used";
	private static final String INFO = "Define the experiment. Code is verified on Validate and acceptance.";

	/** Builds the form in another container; use {@link #accept()} instead of the dialog's primary button. */
	public Composite createForm(final Composite parent, final FormHost formHost) {
		host = Objects.requireNonNull(formHost);
		host.setTitle("Experiment settings");
		host.setMessage(INFO);
		Composite form = WizardWidgets.composite(parent, 1);
		form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite header = WizardWidgets.row(form, 10, "Identity");
		header.setData(FIELD, "Header");
		name = WizardWidgets.textField(header, "Name", original.name(),
				"Name of the experiment, shown in the interface (any text without quotes).", FIELD, "Name", 160);
		facetControl(header, ExperimentFacet.DECLARED.stream().filter(f -> f.name().equals("title")).findFirst().orElseThrow(), 1);
		List<String> types = new ArrayList<>(ExperimentDraft.TYPES);
		if (!types.contains(type)) types.add(type);
		typeCombo = WizardWidgets.choiceField(header, "Type", type, types, false,
				"gui: interactive experiment with displays; batch: exploration of the parameters; test: runs tests.",
				FIELD, "Type", 90);
		facetControl(header, ExperimentFacet.DECLARED.stream().filter(f -> f.name().equals("parent")).findFirst().orElseThrow(), 1);
		facetControl(header, ExperimentFacet.DECLARED.stream().filter(f -> f.name().equals("virtual")).findFirst().orElseThrow(), 1);

		capabilitiesArea = WizardWidgets.row(form, 2, "Capabilities");
		commonArea = WizardWidgets.composite(form, 6);
		commonArea.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		commonArea.setData(FIELD, "Common");
		typeArea = new Group(form, SWT.NONE);
		typeArea.setLayout(new org.eclipse.swt.layout.GridLayout(4, false));
		typeArea.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		typeArea.setData(FIELD, "TypeSection");
		otherArea = WizardWidgets.composite(form, 2);
		otherArea.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		otherArea.setData(FIELD, "Other");

		tabs = new TabFolder(form, SWT.TOP);
		GridData tabData = new GridData(SWT.FILL, SWT.FILL, true, true);
		tabData.heightHint = 300;
		tabData.widthHint = 640;
		tabs.setLayoutData(tabData);
		tabs.setData(FIELD, "Tabs");
		attributesPage = WizardWidgets.composite(tabs, 1);
		attributes = new AttributeTable(attributesPage, original.variables(), () -> typeNames,
				() -> name.getText().trim(), this::changed);
		attributes.control().setData(FIELD, "Attributes");
		parametersPage = createParameters(tabs);
		monitorsPage = createMonitors(tabs);
		initPage = WizardWidgets.composite(tabs, 1);
		signature = new Label(initPage, SWT.WRAP);
		signature.setFont(JFaceResources.getTextFont());
		signature.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		signature.setToolTipText("Declaration of the experiment, updated from the fields above.");
		signature.setData(FIELD, "Signature");
		WizardWidgets.accessible(signature, "Signature");
		init = createBodyEditor(initPage, original.init());
		init.control().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		init.control().setData(FIELD, "Init");
		WizardWidgets.accessible(init.control(), "Init");
		Label closing = new Label(initPage, SWT.NONE);
		closing.setFont(JFaceResources.getTextFont());
		closing.setText("\t}");
		closing.setData(FIELD, "Closing");

		name.addModifyListener(e -> changed());
		typeCombo.addListener(SWT.Selection, e -> {
			type = typeCombo.getItem(typeCombo.getSelectionIndex());
			reorganize();
			changed();
		});
		ready = true;
		reorganize();
		changed();
		return form;
	}

	/** Lays out the type-specific facets, the remaining facets and the tabs for the current type. */
	private void reorganize() {
		renderFacets();
		// Pages are detached first: on some platforms disposing an item disposes its control.
		for (TabItem item : tabs.getItems()) {
			item.setControl(null);
			item.dispose();
		}
		switch (type) {
			case ExperimentDraft.BATCH -> {
				tab("Parameters (explored)", parametersPage);
				tab("Attributes", attributesPage);
				tab("Init", initPage);
				tab("Monitors", monitorsPage);
			}
			case ExperimentDraft.TEST -> {
				tab("Attributes", attributesPage);
				tab("Init", initPage);
				tab("Parameters", parametersPage);
				tab("Monitors", monitorsPage);
			}
			default -> {
				tab("Attributes", attributesPage);
				tab("Parameters", parametersPage);
				tab("Monitors", monitorsPage);
				tab("Init", initPage);
			}
		}
		tabs.setSelection(0);
		showParameter();
		tabs.getParent().layout(true, true);
	}

	private void tab(final String title, final Composite page) {
		TabItem item = new TabItem(tabs, SWT.NONE);
		item.setText(title);
		item.setControl(page);
		item.setData(FIELD, title);
	}

	private void renderFacets() {
		loading = true;
		try {
			for (Composite area : List.of(capabilitiesArea, commonArea, typeArea, otherArea))
				for (Control child : area.getChildren()) child.dispose();
			Set<String> shown = new HashSet<>(List.of("title", "virtual", "parent", "control", "skills", "schedules", "parallel"));
			facetControl(capabilitiesArea, ExperimentFacet.DECLARED.stream().filter(f -> f.name().equals("control")).findFirst().orElseThrow(), 1);
			for (String key : List.of("autorun", "benchmark", "record")) {
				ExperimentFacet facet = ExperimentFacet.DECLARED.stream().filter(f -> f.name().equals(key)).findFirst().orElseThrow();
				shown.add(key);
				facetControl(commonArea, facet, 1);
			}
			for (ExperimentFacet facet : ExperimentFacet.of(ExperimentFacet.COMMON)) {
				if (shown.contains(facet.name())) continue;
				shown.add(facet.name());
				facetControl(commonArea, facet, 1);
			}
			typeArea.setText(switch (type) {
				case ExperimentDraft.BATCH -> "Batch exploration";
				case ExperimentDraft.TEST -> "Test experiment";
				default -> "Interactive (gui) experiment";
			});
			List<ExperimentFacet> specific = ExperimentFacet.of(type);
			for (ExperimentFacet facet : specific) {
				if (shown.contains(facet.name())) continue;
				shown.add(facet.name());
				facetControl(typeArea, facet, 1);
			}
			Label note = new Label(typeArea, SWT.WRAP);
			note.setText(switch (type) {
				case ExperimentDraft.BATCH -> "The exploration method and its facets are kept as written in the source.";
				case ExperimentDraft.TEST -> "Test experiments run the 'test' blocks they contain (kept as written).";
				default -> "Displays and other outputs are kept as written.";
			});
			note.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
			for (Facet facet : List.copyOf(facets)) if (!shown.contains(facet.name())) {
				var flag = ExperimentFacet.DECLARED.stream().filter(f -> f.name().equals(facet.name()) && f.flag())
						.findFirst();
				if (flag.isPresent()) {
					facetControl(otherArea, flag.get(), 1);
					continue;
				}
				String section = ExperimentFacet.section(facet.name());
				String label = facet.name() + (section == null ? " (other)" : " (" + section + " only)");
				String description = section == null ? "Facet not declared for experiments; kept as written. Clear it to remove it."
						: "Only used by " + section + " experiments; kept as written. Clear it to remove it.";
				expression(otherArea, label, facet.name(), description, facet.value(), 1);
			}
			((GridData) otherArea.getLayoutData()).exclude = otherArea.getChildren().length == 0;
		} finally {
			loading = false;
		}
		commonArea.getParent().layout(true, true);
	}

	private void facetControl(final Composite area, final ExperimentFacet facet, final int span) {
		String value = value(facet.name());
		if ("parent".equals(facet.name()) || "control".equals(facet.name())) {
			java.util.TreeSet<String> choices = new java.util.TreeSet<>("parent".equals(facet.name())
					? experimentNames.get() : possibleControlNames());
			choices.add("");
			if (value != null) choices.add(value);
			String help = facet.documentation() + ("parent".equals(facet.name())
					? ". Choose an existing experiment, or enter an imported experiment name."
					: ". Choose a registered control architecture.") + " Clear to omit the facet.";
			Combo choice = WizardWidgets.choiceField(area, facet.name(), value == null ? "" : value,
					List.copyOf(choices), true, help, FIELD, "facet:" + facet.name(), 160);
			choice.addModifyListener(e -> set(facet.name(), choice.getText().isBlank() ? null : choice.getText()));
		} else if (facet.flag()) {
			literalCheckbox(area, facet.name(), value, () -> facet.defaultValue(facets),
					facet.documentation() + ("autorun".equals(facet.name()) ? " Default follows the live auto-run preference." : ""),
					"facet:" + facet.name(), next -> set(facet.name(), next));
		} else expression(area, facet.name(), facet.name(), facet.documentation() + " (" + facet.type() + ")"
				+ ("record".equals(facet.name()) ? ". Initial UI expression: false; untouched omitted record remains omitted." : ""),
				value == null && "record".equals(facet.name()) ? "false" : value, span);
	}

	/** The same registered control architectures offered by the species form. */
	protected List<String> possibleControlNames() {
		return gama.api.additions.registries.GamaSkillRegistry.INSTANCE.getArchitectureNames().stream().sorted().toList();
	}

	/** Literal-only UI preserves existing expressions until the user explicitly replaces them by a checkbox edit. */
	private void literalCheckbox(final Composite parent, final String label, final String value,
			final java.util.function.Supplier<Boolean> defaults, final String description, final String key,
			final Consumer<String> setter) {
		Boolean defaultValue = defaults.get();
		WizardWidgets.literalBoolean(parent, label, value, Boolean.TRUE.equals(defaultValue), defaults,
				description, FIELD, key, next -> { if (!loading) setter.accept(next); });
	}

	private void expression(final Composite area, final String label, final String facet, final String description,
			final String value, final int span) {
		Text text = WizardWidgets.textField(area, label, value == null ? "" : value, description,
				FIELD, "facet:" + facet, 160);
		((GridData) text.getLayoutData()).horizontalSpan = span;
		text.addModifyListener(e -> set(facet, text.getText().isBlank() ? null : text.getText()));
	}

	private String value(final String facet) {
		for (var f : facets) if (f.name().equals(facet)) return f.value();
		return null;
	}

	private void set(final String facet, final String value) {
		if (loading) return;
		facets = ExperimentDraft.withFacet(facets, original.facets(), facet, value);
		gaml.compiler.ui.wizards.species.BooleanFacetField.refreshDefaults(commonArea.getParent());
		changed();
	}

	// ---- Parameters

	private Composite createParameters(final Composite parent) {
		Composite page = WizardWidgets.composite(parent, 2);
		parameterTable = table(page, "Parameters", "Title", "Variable", "Facets");
		Composite buttons = WizardWidgets.composite(page, 1);
		buttons.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		Button add = WizardWidgets.button(buttons, "Add", "Add a parameter");
		add.setData(FIELD, "AddParameter");
		removeParameter = WizardWidgets.button(buttons, "Remove", "Remove the selected parameter");
		removeParameter.setData(FIELD, "RemoveParameter");
		parameterDetail = WizardWidgets.composite(page, 4);
		parameterDetail.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
		parameterDetail.setData(FIELD, "ParameterDetail");
		add.addListener(SWT.Selection, e -> {
			List<String> variables = variables();
			parameters.add(new ExperimentDraft.Parameter("Parameter " + (parameters.size() + 1),
					variables.isEmpty() ? "" : variables.get(0), List.of()));
			fill(parameterTable, parameters.size(), this::parameterRow);
			parameterTable.setSelection(parameters.size() - 1);
			showParameter();
			changed();
		});
		removeParameter.addListener(SWT.Selection, e -> {
			int index = parameterTable.getSelectionIndex();
			if (index < 0) return;
			parameters.remove(index);
			fill(parameterTable, parameters.size(), this::parameterRow);
			if (!parameters.isEmpty()) parameterTable.setSelection(Math.min(index, parameters.size() - 1));
			showParameter();
			changed();
		});
		parameterTable.addListener(SWT.Selection, e -> showParameter());
		fill(parameterTable, parameters.size(), this::parameterRow);
		if (!parameters.isEmpty()) parameterTable.setSelection(0);
		return page;
	}

	private String[] parameterRow(final int index) {
		var parameter = parameters.get(index);
		return new String[] { parameter.title(), parameter.variable(), summary(parameter.facets()) };
	}

	/** The variables offered for parameters: the adapter's, then the experiment's attributes. */
	private List<String> variables() {
		Set<String> result = new LinkedHashSet<>(variableNames.get());
		for (var variable : attributes.values()) result.add(variable.name());
		return List.copyOf(result);
	}

	private void showParameter() {
		for (Control child : parameterDetail.getChildren()) child.dispose();
		int index = parameterTable.getSelectionIndex();
		removeParameter.setEnabled(index >= 0);
		if (index >= 0) {
			loading = true;
			try {
				var parameter = parameters.get(index);
				Consumer<ExperimentDraft.Parameter> update = changed -> {
					parameters.set(index, changed);
					row(parameterTable, index, parameterRow(index));
					changed();
				};
				Text title = detailText(parameterDetail, "Title", "Label shown in the interface (plain text).",
						parameter.title());
				title.addModifyListener(e -> {
					if (!loading) update.accept(new ExperimentDraft.Parameter(title.getText(),
							parameters.get(index).variable(), parameters.get(index).facets()));
				});
				WizardWidgets.label(parameterDetail, "Variable *", "The variable controlled by the parameter (var:).");
				Combo variable = new Combo(parameterDetail, SWT.DROP_DOWN);
				variable.setItems(variables().toArray(String[]::new));
				variable.setText(parameter.variable());
				detailLayout(variable, "param:var");
				variable.addModifyListener(e -> {
					if (!loading) update.accept(new ExperimentDraft.Parameter(parameters.get(index).title(),
							variable.getText().trim(), parameters.get(index).facets()));
				});
				// Batch explorations are driven by among/min/max/step: they come first.
				List<String> fields = ExperimentDraft.BATCH.equals(type)
						? List.of("among", "min", "max", "step", "init", "category", "unit")
						: ExperimentFacet.PARAMETER_FIELDS;
				Function<String, Consumer<String>> setter = facet -> value -> {
					var current = parameters.get(index);
					var originalFacets = index < original.parameters().size()
							? original.parameters().get(index).facets() : List.<Facet> of();
					update.accept(new ExperimentDraft.Parameter(current.title(), current.variable(),
							ExperimentDraft.withFacet(current.facets(), originalFacets, facet, value)));
				};
				for (String facet : fields)
					detailFacet(parameterDetail, "param:", facet, false, parameter.facet(facet), setter.apply(facet));
				for (String facet : ExperimentFacet.PARAMETER_FLAGS)
					detailFacet(parameterDetail, "param:", facet, true, parameter.facet(facet), setter.apply(facet));
				others(parameterDetail, "param:other", parameter.facets(), fields, ExperimentFacet.PARAMETER_FLAGS);
			} finally {
				loading = false;
			}
		}
		parameterDetail.getParent().layout(true, true);
	}

	// ---- Monitors

	private Composite createMonitors(final Composite parent) {
		Composite page = WizardWidgets.composite(parent, 2);
		monitorTable = table(page, "Monitors", "Title", "Value", "Facets");
		Composite buttons = WizardWidgets.composite(page, 1);
		buttons.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		Button add = WizardWidgets.button(buttons, "Add", "Add a monitor (in the output section)");
		add.setData(FIELD, "AddMonitor");
		removeMonitor = WizardWidgets.button(buttons, "Remove", "Remove the selected monitor");
		removeMonitor.setData(FIELD, "RemoveMonitor");
		monitorDetail = WizardWidgets.composite(page, 4);
		monitorDetail.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
		monitorDetail.setData(FIELD, "MonitorDetail");
		add.addListener(SWT.Selection, e -> {
			monitors.add(new ExperimentDraft.Monitor("Monitor " + (monitors.size() + 1), "cycle", List.of()));
			fill(monitorTable, monitors.size(), this::monitorRow);
			monitorTable.setSelection(monitors.size() - 1);
			showMonitor();
			changed();
		});
		removeMonitor.addListener(SWT.Selection, e -> {
			int index = monitorTable.getSelectionIndex();
			if (index < 0) return;
			monitors.remove(index);
			fill(monitorTable, monitors.size(), this::monitorRow);
			if (!monitors.isEmpty()) monitorTable.setSelection(Math.min(index, monitors.size() - 1));
			showMonitor();
			changed();
		});
		monitorTable.addListener(SWT.Selection, e -> showMonitor());
		fill(monitorTable, monitors.size(), this::monitorRow);
		if (!monitors.isEmpty()) monitorTable.setSelection(0);
		showMonitor();
		return page;
	}

	private String[] monitorRow(final int index) {
		var monitor = monitors.get(index);
		return new String[] { monitor.title(), monitor.value(), summary(monitor.facets()) };
	}

	private void showMonitor() {
		for (Control child : monitorDetail.getChildren()) child.dispose();
		int index = monitorTable.getSelectionIndex();
		removeMonitor.setEnabled(index >= 0);
		if (index >= 0) {
			loading = true;
			try {
				var monitor = monitors.get(index);
				Consumer<ExperimentDraft.Monitor> update = changed -> {
					monitors.set(index, changed);
					row(monitorTable, index, monitorRow(index));
					changed();
				};
				Text title = detailText(monitorDetail, "Title", "Label shown in the interface (plain text).",
						monitor.title());
				title.addModifyListener(e -> {
					if (!loading) update.accept(new ExperimentDraft.Monitor(title.getText(), monitors.get(index).value(),
							monitors.get(index).facets()));
				});
				Text value = detailText(monitorDetail, "Value *", "The expression monitored (value:).", monitor.value());
				value.setData(FIELD, "monitor:value");
				value.addModifyListener(e -> {
					if (!loading) update.accept(new ExperimentDraft.Monitor(monitors.get(index).title(), value.getText(),
							monitors.get(index).facets()));
				});
				for (String facet : ExperimentFacet.MONITOR_FIELDS) detailFacet(monitorDetail, "monitor:", facet, false,
						monitor.facet(facet), v -> {
							var current = monitors.get(index);
							var originalFacets = index < original.monitors().size()
									? original.monitors().get(index).facets() : List.<Facet> of();
							update.accept(new ExperimentDraft.Monitor(current.title(), current.value(),
									ExperimentDraft.withFacet(current.facets(), originalFacets, facet, v)));
						});
				others(monitorDetail, "monitor:other", monitor.facets(), ExperimentFacet.MONITOR_FIELDS, List.of());
			} finally {
				loading = false;
			}
		}
		monitorDetail.getParent().layout(true, true);
	}

	// ---- Detail helpers

	private Text detailText(final Composite parent, final String label, final String description, final String value) {
		WizardWidgets.label(parent, label, description);
		Text text = new Text(parent, SWT.BORDER | SWT.SINGLE);
		text.setText(value);
		text.setToolTipText(description);
		detailLayout(text, parent == parameterDetail ? "param:title" : "monitor:title");
		return text;
	}

	private void detailLayout(final Control control, final String key) {
		GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		data.widthHint = 160;
		control.setLayoutData(data);
		control.setData(FIELD, key);
		WizardWidgets.accessible(control, key);
	}

	private void detailFacet(final Composite parent, final String prefix, final String facet, final boolean flag,
			final String value, final Consumer<String> setter) {
		if (flag) {
			if ("slider".equals(facet)) {
				literalCheckbox(parent, facet, value, () -> true,
						"Allows a slider when the parameter has suitable numeric bounds.", prefix + facet, setter);
			} else new gaml.compiler.ui.wizards.species.BooleanFacetField(parent, facet, value,
					() -> "slider".equals(facet) ? Boolean.TRUE : null, false,
					"slider".equals(facet) ? "Allows a slider when the parameter has suitable numeric bounds."
							: "Read-only status also depends on the target variable's modifiability.",
					FIELD, prefix + facet, setter);
			return;
		}
		WizardWidgets.label(parent, facet, "GAML expression for " + facet + ":");
		Text text = new Text(parent, SWT.BORDER | SWT.SINGLE);
		text.setText(value == null ? "" : value);
		detailLayout(text, prefix + facet);
		text.addModifyListener(e -> {
			if (!loading) setter.accept(text.getText().isBlank() ? null : text.getText());
		});
	}

	private void others(final Composite parent, final String key, final List<Facet> all, final List<String> fields,
			final List<String> flags) {
		List<Facet> rest = all.stream().filter(f -> !fields.contains(f.name()) && !flags.contains(f.name())).toList();
		if (rest.isEmpty()) return;
		WizardWidgets.label(parent, "Other facets", "Other facets, kept as written (edit them in the source).");
		Text text = new Text(parent, SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY);
		text.setText(summary(rest));
		GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
		text.setLayoutData(data);
		text.setData(FIELD, key);
	}

	private static String summary(final List<Facet> facets) {
		StringBuilder text = new StringBuilder();
		for (var facet : facets) {
			if (!text.isEmpty()) text.append(' ');
			text.append(facet.name()).append(": ").append(facet.value().trim());
		}
		return text.toString();
	}

	private Table table(final Composite parent, final String label, final String... columns) {
		Table table = new Table(parent, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		for (String column : columns) {
			TableColumn tc = new TableColumn(table, SWT.NONE);
			tc.setText(WizardWidgets.labelText(column));
			tc.setWidth(column.equals("Facets") ? 260 : 140);
		}
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = 110;
		table.setLayoutData(data);
		table.setData(FIELD, label);
		WizardWidgets.accessible(table, label);
		return table;
	}

	private static void fill(final Table table, final int size, final Function<Integer, String[]> row) {
		table.removeAll();
		for (int i = 0; i < size; i++) new TableItem(table, SWT.NONE).setText(row.apply(i));
	}

	private static void row(final Table table, final int index, final String[] texts) {
		table.getItem(index).setText(texts);
	}

	// ---- Init code

	/** Creates the init code editor: the embedded GAML editor when the GAML UI runs, otherwise a plain text area. */
	protected BodyEditor createBodyEditor(final Composite parent, final String initial) {
		try {
			EmbeddedActionBody embedded = EmbeddedActionBody.create(parent,
					standalone(ExperimentDraft.empty(ExperimentDraft.GUI)), initial);
			if (embedded != null) return new BodyEditor() {
				@Override
				public Control control() { return embedded.control(); }

				@Override
				public String text() { return embedded.text(); }
			};
		} catch (RuntimeException | LinkageError error) {
			// The GAML UI injector is unavailable (e.g. outside the workbench): fall back to plain text.
		}
		StyledText text = new StyledText(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		text.setText(initial);
		text.addModifyListener(e -> changed());
		return new BodyEditor() {
			@Override
			public Control control() { return text; }

			@Override
			public String text() { return text.getText(); }
		};
	}

	/** Current form values; the initial instance when nothing changed. */
	public ExperimentDraft readDraft() {
		ExperimentDraft draft = new ExperimentDraft(name.getText().trim(), type, facets, attributes.values(),
				init.text(), parameters, monitors);
		return draft.equals(original) ? original : draft;
	}

	private void changed() {
		if (!ready || loading) return;
		ExperimentDraft current = readDraft();
		signature.setText(signature(current));
		signature.getParent().layout(new Control[] { signature });
		String error = current.validationError();
		host.setErrorMessage(error);
		List<String> inactive = current.inactiveFacets();
		host.setMessage(inactive.isEmpty() ? INFO
				: WARNING + " by " + current.type() + " experiments: " + String.join(", ", inactive) + ".");
		host.setComplete(error == null);
	}

	/** The declaration shown above the init code. */
	protected String signature(final ExperimentDraft draft) {
		return draft.header() + " {\n\t...\n\tinit {";
	}

	/** Local checks, contextual compilation, then the adapter validator. */
	public boolean validateNow() {
		ExperimentDraft draft = readDraft();
		String[] error = { draft.validationError() };
		if (error[0] == null && bodyContext != null)
			BusyIndicator.showWhile(name.getDisplay(), () -> error[0] = verify(draft));
		if (error[0] == null) error[0] = validator.apply(draft);
		host.setErrorMessage(error[0]);
		host.setComplete(draft.validationError() == null);
		return error[0] == null;
	}

	private String verify(final ExperimentDraft draft) {
		ActionBodyContext context;
		try {
			context = bodyContext.apply(draft);
		} catch (RuntimeException failure) {
			return "Cannot build the experiment context: " + failure.getMessage();
		}
		if (context == null) return "Cannot build the experiment context.";
		List<ActionVerifier.Problem> problems;
		try {
			problems = compile(context, draft.init());
		} catch (RuntimeException | LinkageError failure) {
			return "Cannot verify the experiment: " + failure.getMessage();
		}
		for (var problem : problems) if (problem.inBody()) return "Init: " + problem;
		Set<String> existing = baseline();
		for (var problem : problems) if (!existing.contains(problem.message())) return problem.toString();
		return null;
	}

	/** Compiles {@code context.text(code)}; overridable to substitute the compiler. */
	protected List<ActionVerifier.Problem> compile(final ActionBodyContext context, final String code) {
		return ActionVerifier.verify(context, code);
	}

	/** Errors outside the init code that already exist for the initial draft are not attributed to this edit. */
	private Set<String> baseline() {
		if (baseline != null) return baseline;
		baseline = new HashSet<>();
		try {
			if (original.validationError() == null) {
				ActionBodyContext context = bodyContext.apply(original);
				if (context != null) for (var problem : compile(context, original.init()))
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
	public ExperimentDraft getResult() { return result; }

}
