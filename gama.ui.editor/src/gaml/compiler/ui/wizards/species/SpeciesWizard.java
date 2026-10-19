package gaml.compiler.ui.wizards.species;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

import gama.api.additions.registries.GamaSkillRegistry;
import gama.api.gaml.types.Types;

/** Detached species settings. Editor adapters validate on OK and own the undoable apply. */
public class SpeciesWizard extends TitleAreaDialog {

	public enum Kind { SPECIES, GRID, MODEL, CLASS }

	/** Receives form feedback when the form is embedded outside this dialog, e.g. in a workbench wizard page. */
	public interface FormHost {
		void setTitle(String title);

		void setMessage(String message);

		void setErrorMessage(String error);

		void setComplete(boolean complete);

		void relayout();
	}

	private final SpeciesDraft original;
	private final ModelLocation location;
	private final boolean locationEditable;
	private final ModelDocumentation documentation;
	private ModelDocumentation documentationResult;
	private ModelLocation locationResult;
	private boolean modelNameEdited;
	private boolean syncingModelName;
	private FormHost host;
	private final Function<SpeciesDraft, String> validator;
	private final boolean editBody;
	private final List<String> parentNames;
	private final SpeciesWizardContext context;
	private final Kind kind;
	private final Map<String, Control> header = new LinkedHashMap<>();
	private final Map<String, String> initialHeader = new LinkedHashMap<>();
	private final java.util.Set<String> editedBooleans = new java.util.HashSet<>();
	private final List<String> selectedSkills = new ArrayList<>();
	private final List<String> skillChoices = new ArrayList<>();
	private SpeciesDraft result;
	private AttributeTable attributes;
	private String contextError;
	private boolean ready;

	public SpeciesWizard(final Shell shell, final SpeciesDraft draft, final Function<SpeciesDraft, String> validator) {
		this(shell, draft, validator, true);
	}

	public SpeciesWizard(final Shell shell, final SpeciesDraft draft, final Function<SpeciesDraft, String> validator,
			final boolean editBody) {
		this(shell, draft, validator, editBody, List.of("agent"));
	}

	public SpeciesWizard(final Shell shell, final SpeciesDraft draft, final Function<SpeciesDraft, String> validator,
			final boolean editBody, final List<String> parentNames) {
		this(shell, draft, validator, editBody, parentNames, SpeciesWizardContext.builtIns());
	}

	public SpeciesWizard(final Shell shell, final SpeciesDraft draft, final Function<SpeciesDraft, String> validator,
			final boolean editBody, final List<String> parentNames, final SpeciesWizardContext context) {
		this(shell, draft, validator, editBody, parentNames, context, Kind.SPECIES);
	}

	public SpeciesWizard(final Shell shell, final SpeciesDraft draft, final Function<SpeciesDraft, String> validator,
			final boolean editBody, final List<String> parentNames, final SpeciesWizardContext context, final Kind kind) {
		this(shell, draft, validator, editBody, parentNames, context, kind, null, false, ModelDocumentation.EMPTY);
	}

	/** Model/global form. A null location hides Container/File name; documentation is returned by getDocumentation. */
	public SpeciesWizard(final Shell shell, final SpeciesDraft draft, final Function<SpeciesDraft, String> validator,
			final boolean editBody, final SpeciesWizardContext context, final ModelLocation location,
			final boolean locationEditable, final ModelDocumentation documentation) {
		this(shell, draft, validator, editBody, List.of(), context, Kind.MODEL, location, locationEditable, documentation);
	}

	private SpeciesWizard(final Shell shell, final SpeciesDraft draft, final Function<SpeciesDraft, String> validator,
			final boolean editBody, final List<String> parentNames, final SpeciesWizardContext context, final Kind kind,
			final ModelLocation location, final boolean locationEditable, final ModelDocumentation documentation) {
		super(shell);
		this.location = location;
		this.locationEditable = locationEditable;
		this.documentation = Objects.requireNonNull(documentation);
		original = Objects.requireNonNull(draft);
		this.validator = Objects.requireNonNull(validator);
		this.context = Objects.requireNonNull(context);
		this.kind = Objects.requireNonNull(kind);
		this.editBody = editBody;
		this.parentNames = List.copyOf(parentNames);
		selectedSkills.addAll(draft.skills());
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		getShell().setText(kind == Kind.GRID ? "Grid" : kind == Kind.MODEL ? "Model" : kind == Kind.CLASS ? "Class" : "Species");
		Composite form = createForm(area, new FormHost() {
			@Override
			public void setTitle(final String title) { SpeciesWizard.this.setTitle(title); }

			@Override
			public void setMessage(final String message) { SpeciesWizard.this.setMessage(message); }

			@Override
			public void setErrorMessage(final String error) { SpeciesWizard.this.setErrorMessage(error); }

			@Override
			public void setComplete(final boolean complete) {
				Button ok = getButton(IDialogConstants.OK_ID);
				if (ok != null) ok.setEnabled(complete);
			}

			@Override
			public void relayout() { getShell().pack(); }
		});
		form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		return area;
	}

	/**
	 * Builds this form inside another container (for instance a workbench wizard page). Use {@link #accept()} instead
	 * of OK; feedback goes to {@code formHost}.
	 */
	public Composite createForm(final Composite parent, final FormHost formHost) {
		host = Objects.requireNonNull(formHost);
		String noun = kind == Kind.GRID ? "grid" : kind == Kind.MODEL ? "model" : kind == Kind.CLASS ? "class" : "species";
		host.setTitle(original.name().isBlank() ? "New " + noun
				: kind == Kind.GRID ? "Grid settings" : kind == Kind.MODEL ? "Model settings"
				: kind == Kind.CLASS ? "Class settings" : "Species settings");
		host.setMessage(editBody ? "Define the " + noun + " structure and its attributes."
				: kind == Kind.MODEL ? "Define the model header. Global declarations remain unchanged in the source."
				: "Define the " + noun + " header. Attribute declarations remain unchanged in the source.");
		Composite form = composite(parent, 1);
		form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		if (kind == Kind.MODEL) createModelHeader(form);
		else {
			Composite basic = composite(form, 4);
			basic.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			field(basic, "Name", original.name().isBlank() ? kind == Kind.GRID ? "my_grid"
					: kind == Kind.CLASS ? "my_class" : "my_species" : original.name(), null);
			booleanField(basic, "Virtual", "virtual", false);
			String parentName = original.parent(kind == Kind.CLASS ? "object" : "agent");
			field(basic, "Parent", parentName, speciesChoices(parentName, false));
			if (kind == Kind.CLASS) ((GridData) header.get("parent").getLayoutData()).horizontalSpan = 3;
			else createSkills(basic);
		}
		if (kind == Kind.GRID) {
			Composite dimensions = composite(form, 6);
			dimensions.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			expressionHeader(dimensions, "Width", facet("width", original.name().isBlank() ? "100" : ""));
			((GridData) header.get("width").getLayoutData()).widthHint = 90;
			expressionHeader(dimensions, "Height", facet("height", original.name().isBlank() ? "100" : ""));
			((GridData) header.get("height").getLayoutData()).widthHint = 90;
			field(dimensions, "Neighbors", facet("neighbors", "4").trim(),
					withCurrent(List.of("4", "6", "8"), facet("neighbors", "4").trim()));
			((GridData) header.get("neighbors").getLayoutData()).widthHint = 65;
		}

		if (kind != Kind.CLASS) {
			ExpandableComposite advanced = new ExpandableComposite(form, SWT.NONE,
					ExpandableComposite.TWISTIE | ExpandableComposite.CLIENT_INDENT);
			advanced.setText("Advanced");
			advanced.setToolTipText(kind == Kind.MODEL ? "Expand to configure skills, the control architecture and scheduling."
					: "Expand to configure control, mirroring, scheduling and concurrency.");
			advanced.setData("species.field", "Advanced");
			advanced.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			accessible(advanced, "Advanced");
			Composite advancedSettings = composite(advanced, 4);
			advanced.setClient(advancedSettings);
			advanced.setExpanded(false);
			if (kind == Kind.MODEL) createSkills(advancedSettings);
			field(advancedSettings, "Control", facet("control", "reflex").trim(),
					withCurrent(possibleControlNames(), facet("control", "reflex").trim()));
			if (kind == Kind.SPECIES) {
				field(advancedSettings, "Mirrors", facet("mirrors", "").trim(),
						speciesChoices(facet("mirrors", "").trim(), true));
			} else if (kind == Kind.GRID) {
				((GridData) header.get("control").getLayoutData()).horizontalSpan = 3;
			}
			Composite scheduling = composite(advancedSettings, kind == Kind.MODEL ? 4 : 6);
			((GridLayout) scheduling.getLayout()).marginWidth = 0;
			((GridLayout) scheduling.getLayout()).marginHeight = 0;
			scheduling.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
			expressionHeader(scheduling, "Schedules", facet("schedules", ""));
			((GridData) header.get("schedules").getLayoutData()).widthHint = 125;
			expressionHeader(scheduling, "Frequency", facet("frequency", ""));
			((GridData) header.get("frequency").getLayoutData()).widthHint = 65;
			if (kind != Kind.MODEL) createParallel(scheduling);
			if (kind == Kind.GRID) {
				expressionHeader(advancedSettings, "File", facet("file", ""));
				expressionHeader(advancedSettings, "Files", facet("files", ""));
				Composite options = composite(advancedSettings, 6);
				options.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
				gridOption(options, "Use individual shapes", "use_individual_shapes");
				gridOption(options, "Use neighbors cache", "use_neighbors_cache");
			}
			advanced.addExpansionListener(new ExpansionAdapter() {
				@Override
				public void expansionStateChanged(final ExpansionEvent event) {
					form.layout(true, true);
					host.relayout();
				}
			});
		}

		ExpandableComposite attributeDisclosure = new ExpandableComposite(form, SWT.NONE,
				ExpandableComposite.TWISTIE | ExpandableComposite.CLIENT_INDENT | ExpandableComposite.EXPANDED);
		attributeDisclosure.setText("Attributes");
		attributeDisclosure.setToolTipText("Local and inherited attributes. Select an inherited attribute and check Redefine to override it.");
		attributeDisclosure.setData("species.field", "Attributes");
		attributeDisclosure.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		accessible(attributeDisclosure, "Attributes");
		Composite attributeSection = composite(attributeDisclosure, 1);
		attributeDisclosure.setClient(attributeSection);
		attributeDisclosure.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(final ExpansionEvent event) {
				form.layout(true, true);
				host.relayout();
			}
		});
		attributes = new AttributeTable(attributeSection, original.variables(), this::possibleTypeNames, editBody,
				() -> value("name"), this::validateDraft);
		if (kind == Kind.MODEL) createDocumentation(form);
		header.keySet().forEach(key -> initialHeader.put(key, value(key)));
		ready = true;
		refreshContext();
		return form;
	}

	private void createParallel(final Composite scheduling) {
		label(scheduling, "Parallel", explanation("Parallel"));
		Combo parallel = new Combo(scheduling, SWT.DROP_DOWN);
		parallel.setItems("", "true", "false");
		parallel.setText(facet("parallel", ""));
		GridData parallelData = new GridData(SWT.FILL, SWT.CENTER, false, false);
		parallelData.widthHint = 65;
		parallel.setLayoutData(parallelData);
		parallel.setToolTipText(explanation("Parallel"));
		parallel.setData("species.field", "Parallel");
		accessible(parallel, "Parallel");
		header.put("parallel", parallel);
		parallel.addModifyListener(event -> changed());
	}

	private void createModelHeader(final Composite form) {
		if (location != null) {
			Composite place = composite(form, 5);
			place.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			field(place, "Container", location.container(), null);
			Text container = (Text) header.get("container");
			container.setEditable(locationEditable);
			Button browse = action(place, "Browse...", "Select the workspace folder that will contain the model file.");
			browse.setVisible(locationEditable);
			((GridData) browse.getLayoutData()).exclude = !locationEditable;
			if (!locationEditable) ((GridData) container.getLayoutData()).horizontalSpan = 2;
			browse.addListener(SWT.Selection, event -> {
				String selected = browseContainer(container.getText());
				if (selected != null) container.setText(selected);
			});
			field(place, "File name", location.fileName(), null);
			Text file = (Text) header.get("file name");
			file.setEditable(locationEditable);
			if (locationEditable) file.addModifyListener(event -> {
				if (modelNameEdited) return;
				syncingModelName = true;
				try {
					((Text) header.get("name")).setText(modelNameFor(file.getText()));
				} finally { syncingModelName = false; }
			});
		}
		Composite basic = composite(form, 4);
		basic.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		field(basic, "Model name", original.name().isBlank() && location != null ? modelNameFor(location.fileName())
				: original.name(), null);
		header.put("name", header.remove("model name"));
		((Text) header.get("name")).addModifyListener(event -> { if (!syncingModelName) modelNameEdited = true; });
		((GridData) header.get("name").getLayoutData()).horizontalSpan = 3;
	}

	/** Model identifier derived from a file name, as the former New Model File wizard did. */
	static String modelNameFor(final String fileName) {
		String base = fileName.endsWith(".gaml") ? fileName.substring(0, fileName.length() - 5) : fileName;
		String name = base.replaceAll("[^A-Za-z0-9_]", "");
		return !name.isEmpty() && Character.isDigit(name.charAt(0)) ? "_" + name : name;
	}

	private void createDocumentation(final Composite form) {
		ExpandableComposite disclosure = new ExpandableComposite(form, SWT.NONE,
				ExpandableComposite.TWISTIE | ExpandableComposite.CLIENT_INDENT);
		disclosure.setText("Documentation");
		disclosure.setToolTipText("Expand to describe the model. Author, tags and documentation are kept in the model header comment.");
		disclosure.setData("species.field", "Documentation");
		disclosure.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		accessible(disclosure, "Documentation");
		Composite settings = composite(disclosure, 4);
		disclosure.setClient(settings);
		disclosure.setExpanded(false);
		field(settings, "Author", documentation.author(), null);
		field(settings, "Tags", documentation.tags(), null);
		Label label = label(settings, "Documentation", explanation("Documentation"));
		label.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		Text text = new Text(settings, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		text.setText(documentation.documentation());
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1);
		data.heightHint = 80;
		data.widthHint = 360;
		text.setLayoutData(data);
		text.setToolTipText(explanation("Documentation"));
		text.setData("species.field", "Documentation");
		accessible(text, "Documentation");
		header.put("documentation", text);
		text.addModifyListener(event -> changed());
		disclosure.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(final ExpansionEvent event) {
				form.layout(true, true);
				host.relayout();
			}
		});
	}

	/** Returns a container path chosen by the user, or null. Workspace-aware adapters override this. */
	protected String browseContainer(final String current) { return null; }

	/** Returns an actionable location error, or null. Called only for editable locations. */
	protected String locationError(final ModelLocation proposed) { return null; }

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

	private void gridOption(final Composite parent, final String label, final String facet) {
		booleanField(parent, label, facet, !"use_individual_shapes".equals(facet));
		if ("use_individual_shapes".equals(facet))
			header.get(facet).setToolTipText(explanation(label)
					+ " Wizard initial value: false. Runtime default: true (or inherited)."
					+ " An absent value is saved explicitly as false on OK; Cancel leaves it unchanged."
					+ " Existing source expressions are kept until this checkbox is edited.");
	}

	private void booleanField(final Composite parent, final String label, final String facet, final boolean defaultValue) {
		String current = facet(facet, Boolean.toString(defaultValue)).trim();
		field(parent, label, "true".equals(current) || (!"false".equals(current) && defaultValue), null);
		Control field = header.remove(label.toLowerCase());
		header.put(facet, field);
		field.setToolTipText(explanation(label) + " Default: " + defaultValue
				+ ". Existing source expressions are kept until this checkbox is edited; grid defaults may be inherited.");
		field.addListener(SWT.Selection, e -> { editedBooleans.add(facet); changed(); });
	}

	private void field(final Composite parent, final String label, final Object value, final List<String> choices) {
		Control control = createField(parent, label, value, choices);
		control.setData("species.field", label);
		header.put(label.toLowerCase(), control);
		if (control instanceof Text text) text.addModifyListener(event -> changed());
		else control.addListener(SWT.Selection, event -> changed());
	}

	private Control createField(final Composite parent, final String label, final Object value,
			final List<String> choices) {
		Control control;
		if (value instanceof Boolean bool) {
			Button button = new Button(parent, SWT.CHECK);
			button.setText(WizardWidgets.labelText(label));
			button.setSelection(bool);
			button.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
			control = button;
		} else {
			label(parent, label, explanation(label));
			if (choices != null) {
				Combo combo = new Combo(parent, SWT.READ_ONLY | SWT.DROP_DOWN);
				combo.setItems(choices.toArray(String[]::new));
				combo.select(choices.indexOf(value));
				control = combo;
			} else {
				Text text = new Text(parent, SWT.BORDER | SWT.SINGLE);
				text.setText(value.toString());
				control = text;
			}
			GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
			data.widthHint = 180;
			data.minimumWidth = 80;
			control.setLayoutData(data);
		}
		accessible(control, label);
		control.setToolTipText(explanation(label));
		return control;
	}

	private Label label(final Composite parent, final String text, final String description) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(WizardWidgets.labelText(text) + ":");
		label.setToolTipText(description);
		return label;
	}

	private String explanation(final String name) {
		return switch (name) {
			case "Name" -> "Name of the " + (kind == Kind.CLASS ? "class" : "species") + ", used to refer to it in GAML.";
			case "Model name" -> "Name of the model, declared by the model statement. Derived from the file name until edited.";
			case "Container" -> "Workspace folder containing the model file.";
			case "File name" -> "Name of the model file; the .gaml extension is added when omitted.";
			case "Author" -> "Author of the model, kept in the model header comment.";
			case "Tags" -> "Comma-separated keywords describing the model, kept in the model header comment.";
			case "Documentation" -> "Description of the model, kept in the model header comment.";
			case "Virtual" -> "A virtual " + (kind == Kind.CLASS ? "class" : "species")
					+ " defines a common structure but cannot be instantiated directly.";
			case "Parent" -> kind == Kind.CLASS ? "Class whose attributes and actions are inherited. The default is object."
					: "Species whose attributes and behaviors are inherited. The default is agent.";
			case "Skills" -> "Select or deselect skills to add their attributes and capabilities to the "
					+ (kind == Kind.MODEL ? "world." : "species.");
			case "Control" -> "Control architecture used by " + (kind == Kind.MODEL ? "the world" : "agents of this species")
					+ ". The default is reflex.";
			case "Mirrors" -> "Species to mirror. Adds a target attribute typed as that species.";
			case "Schedules" -> "Expression selecting the agents to schedule and their execution order. Empty uses the default schedule.";
			case "Frequency" -> "Expression setting how often agents execute, in simulation cycles. Empty uses the default frequency.";
			case "Parallel" -> "true or false enables or disables concurrency; an integer sets its threshold. Empty uses the GAMA preference.";
			case "Width" -> "Number of grid cells along the horizontal axis, as a GAML integer expression.";
			case "Height" -> "Number of grid cells along the vertical axis, as a GAML integer expression.";
			case "Neighbors" -> "Cell neighborhood: 4 orthogonal neighbors, 6 hexagonal neighbors, or 8 surrounding neighbors.";
			case "File" -> "GAML file expression used to initialize grid cell values from a bitmap or grid file.";
			case "Files" -> "GAML list of files used to initialize the grid's bands.";
			case "Use individual shapes" -> "Give each grid cell its own geometry. Disable to use simpler proxy geometries.";
			case "Use neighbors cache" -> "Cache cell neighborhoods to speed repeated neighbor access, using more memory.";
			case "Use regular agents" -> "Use full agents for cells. Minimal agents use less memory but cannot host subpopulations.";
			default -> throw new IllegalArgumentException("Missing field explanation: " + name);
		};
	}

	protected List<String> possibleSkillNames() {
		return GamaSkillRegistry.INSTANCE.getSkillNames().stream().sorted().toList();
	}

	protected List<String> possibleControlNames() {
		return GamaSkillRegistry.INSTANCE.getArchitectureNames().stream().sorted().toList();
	}

	protected List<String> possibleTypeNames() {
		List<String> names = new ArrayList<>();
		Types.getTypeNames().forEach(names::add);
		return names.stream().distinct().sorted(String.CASE_INSENSITIVE_ORDER).toList();
	}

	private List<String> withCurrent(final List<String> choices, final String current) {
		var result = new ArrayList<>(choices);
		if (!result.contains(current)) result.add(current);
		return List.copyOf(result);
	}

	private List<String> speciesChoices(final String current, final boolean empty) {
		List<String> result = new ArrayList<>();
		if (empty) result.add("");
		else result.add(kind == Kind.CLASS ? "object" : "agent");
		List<String> candidates = new ArrayList<>();
		if (!empty && kind != Kind.CLASS) candidates.addAll(SpeciesWizardContext.builtInParentNames());
		candidates.addAll(parentNames);
		(empty ? context.mirrorNames(parentNames) : context.parentNames(candidates)).stream()
				.filter(name -> !name.equals(original.name())).forEach(result::add);
		result.add(current);
		return result.stream().distinct().toList();
	}

	private void createSkills(final Composite parent) {
		label(parent, "Skills", explanation("Skills"));
		skillChoices.addAll(withCurrentSkills(possibleSkillNames()));
		Button skillsControl = createSkillsSelector(parent);
		skillsControl.setData("species.field", "Skills");
		GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		data.widthHint = 180;
		skillsControl.setLayoutData(data);
		accessible(skillsControl, "Skills");
	}

	private List<String> withCurrentSkills(final List<String> registered) {
		var choices = new ArrayList<>(registered);
		for (String skill : selectedSkills) if (!choices.contains(skill)) choices.add(skill);
		return choices;
	}

	private Button createSkillsSelector(final Composite parent) {
		Button button = new Button(parent, SWT.PUSH);
		updateSkillsLabel(button);
		Menu menu = new Menu(button);
		button.setMenu(menu);
		for (String skill : skillChoices) {
			MenuItem item = new MenuItem(menu, SWT.CHECK);
			item.setText(skill);
			item.setSelection(selectedSkills.contains(skill));
			item.addListener(SWT.Selection, event -> {
				setSkillSelected(skill, item.getSelection());
				updateSkillsLabel(button);
			});
		}
		Runnable open = () -> {
			menu.setLocation(button.toDisplay(0, button.getSize().y));
			menu.setVisible(true);
		};
		button.addListener(SWT.Selection, event -> open.run());
		button.addListener(SWT.KeyDown, event -> {
			if (event.keyCode == SWT.ARROW_DOWN) {
				open.run();
				event.doit = false;
			}
		});
		return button;
	}

	private void updateSkillsLabel(final Button button) {
		String names = String.join(", ", selectedSkills);
		button.setText(selectedSkills.isEmpty() ? WizardWidgets.labelText("Select skills...") : names);
		button.setToolTipText(explanation("Skills")
				+ (names.isEmpty() ? "" : ": " + names));
	}

	private void setSkillSelected(final String skill, final boolean selected) {
		if (selected && !selectedSkills.contains(skill)) selectedSkills.add(skill);
		if (!selected) selectedSkills.remove(skill);
		changed();
	}

	private void expressionHeader(final Composite parent, final String label, final String value) {
		Text text = expression(parent, label, value);
		header.put(label.toLowerCase(), text);
		text.setData("species.field", label);
		text.addModifyListener(event -> changed());
	}

	private Text expression(final Composite parent, final String label, final String value) {
		return (Text) createField(parent, label, value, null);
	}

	private Button action(final Composite parent, final String text, final String description) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(WizardWidgets.labelText(text));
		GridData data = new GridData(SWT.FILL, SWT.CENTER, false, false);
		org.eclipse.swt.graphics.GC gc = new org.eclipse.swt.graphics.GC(button);
		gc.setFont(button.getFont());
		int width = org.eclipse.jface.dialogs.Dialog.convertHorizontalDLUsToPixels(gc.getFontMetrics(),
				IDialogConstants.BUTTON_WIDTH);
		gc.dispose();
		data.widthHint = Math.max(width, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		button.setLayoutData(data);
		button.setToolTipText(description);
		accessible(button, description);
		return button;
	}

	private void accessible(final Control control, final String label) {
		control.getAccessible().addAccessibleListener(new AccessibleAdapter() {
			@Override
			public void getName(final AccessibleEvent event) { event.result = label; }
		});
	}

	private String value(final String key) {
		return switch (header.get(key)) {
			case Text text -> text.getText();
			case Combo combo -> combo.getText();
			case Button button -> Boolean.toString(button.getSelection());
			default -> throw new IllegalStateException("Unsupported species control for " + key);
		};
	}

	private String facet(final String name, final String defaultValue) {
		return SpeciesWizardContext.facet(original, name, defaultValue);
	}

	private SpeciesDraft readDraft() {
		Map<String, String> changes = new LinkedHashMap<>();
		if (kind != Kind.MODEL) {
			changes.put("parent", value("parent"));
			changes.put("virtual", value("virtual"));
			if (editedBooleans.contains("virtual") && "false".equals(value("virtual"))) changes.put("virtual", "");
			if (kind != Kind.CLASS) changes.put("parallel", value("parallel"));
		}
		if (kind != Kind.CLASS) {
			changes.put("control", value("control"));
			if (kind == Kind.SPECIES) changes.put("mirrors", value("mirrors"));
			changes.put("schedules", value("schedules"));
			changes.put("frequency", value("frequency"));
		}
		Map<String, String> defaults = Map.of("parent", kind == Kind.CLASS ? "object" : "agent", "virtual", "false", "parallel", "", "control", "reflex",
				"mirrors", "", "schedules", "", "frequency", "");
		if (kind == Kind.GRID) {
			for (String key : List.of("width", "height", "neighbors", "file", "files",
					"use_individual_shapes", "use_neighbors_cache")) changes.put(key, value(key));
			for (String key : List.of("use_individual_shapes", "use_neighbors_cache"))
				if (editedBooleans.contains(key) && "true".equals(value(key)) && "agent".equals(value("parent"))) changes.put(key, "");
			defaults = new LinkedHashMap<>(defaults);
			defaults.putAll(Map.of("width", "", "height", "", "neighbors", "4", "file", "", "files", "",
					"use_individual_shapes", "true", "use_neighbors_cache", "true", "use_regular_agents", "true"));
		}
		List<SpeciesDraft.Facet> facets = new ArrayList<>();
		for (var facet : original.facets()) {
			String change = changes.remove(facet.name());
			if (change == null || !editedBooleans.contains(facet.name()) && change.equals(initialHeader.get(facet.name()))
					|| change.equals(facet.value()) || change.equals(facet.value().trim()))
				facets.add(facet);
			else if (!change.isBlank()) facets.add(new SpeciesDraft.Facet(facet.name(), change));
		}
		for (var change : changes.entrySet()) {
			if (!change.getValue().isBlank() && (editedBooleans.contains(change.getKey())
					|| !change.getValue().equals(defaults.get(change.getKey()))))
				facets.add(new SpeciesDraft.Facet(change.getKey(), change.getValue()));
		}
		return new SpeciesDraft(value("name").trim(), selectedSkills, facets,
				attributes == null ? original.variables() : attributes.values(), original.init(), original.reflexOrder());
	}

	private void changed() {
		if (!ready) return;
		refreshContext();
	}

	private void refreshContext() {
		contextError = null;
		try {
			var metadata = new ArrayList<>(context.attributes(readDraft()));
			if (kind == Kind.GRID) {
				metadata.addAll(SpeciesWizardContext.skillAttributes("grid", "grid"));
			}
			if (kind == Kind.MODEL) {
				var world = gama.api.kernel.GamaMetaModel.getSpeciesDescription(gama.annotations.constants.IKeyword.MODEL);
				var names = new java.util.HashSet<String>();
				metadata.forEach(item -> names.add(item.variable().name()));
				if (world != null) for (var variable : world.getAttributes())
					if (names.add(variable.getName())) metadata.add(SpeciesWizardContext.attribute(variable, "model"));
			}
			attributes.refresh(metadata);
		} catch (IllegalArgumentException | IllegalStateException error) {
			contextError = "Cannot load inherited attributes: " + error.getMessage();
		}
		validateDraft();
	}

	@Override
	protected void createButtonsForButtonBar(final Composite parent) {
		super.createButtonsForButtonBar(parent);
		validateDraft();
	}

	private boolean validateDraft() {
		if (!ready) return false;
		SpeciesDraft draft = readDraft();
		String error = contextError == null ? draft.validationError() : contextError;
		if (kind == Kind.MODEL && error != null && error.startsWith("Species name"))
			error = "Model name must be a GAML identifier.";
		if (kind == Kind.CLASS && error != null && error.startsWith("Species name"))
			error = "Class name must be a GAML identifier.";
		if (error == null && kind == Kind.CLASS && (!draft.skills().isEmpty()
				|| draft.facets().stream().anyMatch(facet -> !List.of("parent", "virtual").contains(facet.name()))
				|| !draft.init().isBlank() || !draft.reflexOrder().isEmpty()))
			error = "Classes support name, parent, virtual, attributes and actions, not agent-specific settings.";
		if (error == null && kind == Kind.MODEL && location != null && locationEditable) {
			ModelLocation proposed = readLocation();
			if (proposed.container().isBlank()) error = "Select the folder that will contain the model.";
			else if (proposed.fileName().isBlank()) error = "Enter a file name.";
			else if (proposed.fileName().contains("/") || proposed.fileName().contains("\\"))
				error = "The file name cannot contain folder separators.";
			else if (!proposed.fileName().endsWith(".gaml")) error = "The file extension must be .gaml.";
			else error = locationError(proposed);
		}
		if (error == null && kind == Kind.GRID) {
			String mirrors = SpeciesWizardContext.facet(draft, "mirrors", "");
			if (!mirrors.isBlank()) error = "A grid cannot mirror another species. Remove its mirrors facet in the source.";
			for (String dimension : List.of("width", "height")) {
				String value = SpeciesWizardContext.facet(draft, dimension, "").trim();
				if (value.matches("-?\\d+") && new java.math.BigInteger(value).signum() <= 0)
					error = "Grid " + dimension + " must be positive.";
			}
		}
		if (error == null && kind != Kind.MODEL && draft.name().equals(draft.parent(kind == Kind.CLASS ? "object" : "agent")))
			error = "A " + (kind == Kind.CLASS ? "class" : "species") + " cannot inherit from itself.";
		if (host != null) {
			host.setErrorMessage(error);
			host.setComplete(error == null);
		}
		return error == null;
	}

	private ModelLocation readLocation() {
		if (location == null) return null;
		String file = value("file name").trim();
		if (!file.isEmpty() && !file.contains(".")) file += ".gaml";
		return new ModelLocation(value("container").trim(), file);
	}

	private ModelDocumentation readDocumentation() {
		if (kind != Kind.MODEL) return ModelDocumentation.EMPTY;
		// Unchanged fields keep their exact original text.
		String author = value("author");
		String tags = value("tags");
		String doc = value("documentation");
		return new ModelDocumentation(author.equals(initialHeader.get("author")) ? documentation.author() : author,
				tags.equals(initialHeader.get("tags")) ? documentation.tags() : tags,
				doc.equals(initialHeader.get("documentation")) ? documentation.documentation()
						: doc.replace("\r\n", "\n").replace('\r', '\n'));
	}

	/** Validates locally and with the adapter validator; on success stores the results. Used for OK and wizard Finish. */
	public boolean accept() {
		if (!validateDraft()) return false;
		SpeciesDraft draft = readDraft();
		String error = validator.apply(draft);
		if (error != null) {
			host.setErrorMessage(error);
			return false;
		}
		result = draft;
		documentationResult = readDocumentation();
		locationResult = readLocation();
		return true;
	}

	@Override
	protected void okPressed() {
		if (accept()) super.okPressed();
	}

	@Override
	protected void cancelPressed() {
		result = null;
		documentationResult = null;
		locationResult = null;
		super.cancelPressed();
	}

	public SpeciesDraft getResult() { return result; }

	/** Documentation accepted with the result (EMPTY for species and grids), or null before a successful OK. */
	public ModelDocumentation getDocumentation() { return result == null ? null : documentationResult; }

	/** Location accepted with the result, or null when hidden or before a successful OK. */
	public ModelLocation getLocation() { return result == null ? null : locationResult; }

	public Kind getKind() { return kind; }
}
