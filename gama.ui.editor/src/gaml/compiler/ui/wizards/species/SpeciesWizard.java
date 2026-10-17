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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

import gama.api.additions.registries.GamaSkillRegistry;
import gama.api.gaml.types.Types;
import gaml.compiler.ui.wizards.species.SpeciesWizardContext.InheritedAttribute;

/** Detached species settings. Editor adapters validate on OK and own the undoable apply. */
public class SpeciesWizard extends TitleAreaDialog {

	public enum Kind { SPECIES, GRID, MODEL }

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
	private final List<String> selectedSkills = new ArrayList<>();
	private final List<String> skillChoices = new ArrayList<>();
	private SpeciesDraft result;
	private Attributes attributes;
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
		getShell().setText(kind == Kind.GRID ? "Grid" : kind == Kind.MODEL ? "Model" : "Species");
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
		String noun = kind == Kind.GRID ? "grid" : kind == Kind.MODEL ? "model" : "species";
		host.setTitle(original.name().isBlank() ? "New " + noun
				: kind == Kind.GRID ? "Grid settings" : kind == Kind.MODEL ? "Model settings" : "Species settings");
		host.setMessage(editBody ? "Define the " + noun + " structure and its attributes."
				: kind == Kind.MODEL ? "Define the model header. Global declarations remain unchanged in the source."
				: "Define the species header. Attribute declarations remain unchanged in the source.");
		Composite form = composite(parent, 1);
		form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		if (kind == Kind.MODEL) createModelHeader(form);
		else {
			Composite basic = composite(form, 4);
			basic.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			field(basic, "Name", original.name().isBlank() ? kind == Kind.GRID ? "my_grid" : "my_species" : original.name(), null);
			field(basic, "Virtual", Boolean.valueOf(facet("virtual", "false").trim()), null);
			field(basic, "Parent", original.parent(), speciesChoices(original.parent(), false));
			createSkills(basic);
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
			gridOption(options, "Use regular agents", "use_regular_agents");
		}
		advanced.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(final ExpansionEvent event) {
				form.layout(true, true);
				host.relayout();
			}
		});

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
		attributes = new Attributes(attributeSection);
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
		field(parent, label, Boolean.valueOf(facet(facet, "true").trim()), null);
		header.put(facet, header.remove(label.toLowerCase()));
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
			button.setText(label);
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
		label.setText(text + ":");
		label.setToolTipText(description);
		return label;
	}

	private String explanation(final String name) {
		return switch (name) {
			case "Name" -> "Name of the species, used to refer to it in GAML.";
			case "Model name" -> "Name of the model, declared by the model statement. Derived from the file name until edited.";
			case "Container" -> "Workspace folder containing the model file.";
			case "File name" -> "Name of the model file; the .gaml extension is added when omitted.";
			case "Author" -> "Author of the model, kept in the model header comment.";
			case "Tags" -> "Comma-separated keywords describing the model, kept in the model header comment.";
			case "Documentation" -> "Description of the model, kept in the model header comment.";
			case "Virtual" -> "A virtual species defines a common structure but cannot be instantiated directly.";
			case "Parent" -> "Species whose attributes and behaviors are inherited. The default is agent.";
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
		else result.add("agent");
		List<String> candidates = new ArrayList<>();
		if (!empty) candidates.addAll(SpeciesWizardContext.builtInParentNames());
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
		button.setText(selectedSkills.isEmpty() ? "Select skills..." : names);
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
		button.setText(text);
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
			changes.put("parallel", value("parallel"));
		}
		changes.put("control", value("control"));
		if (kind == Kind.SPECIES) changes.put("mirrors", value("mirrors"));
		changes.put("schedules", value("schedules"));
		changes.put("frequency", value("frequency"));
		Map<String, String> defaults = Map.of("parent", "agent", "virtual", "false", "parallel", "", "control", "reflex",
				"mirrors", "", "schedules", "", "frequency", "");
		if (kind == Kind.GRID) {
			for (String key : List.of("width", "height", "neighbors", "file", "files",
					"use_individual_shapes", "use_neighbors_cache", "use_regular_agents")) changes.put(key, value(key));
			defaults = new LinkedHashMap<>(defaults);
			defaults.putAll(Map.of("width", "", "height", "", "neighbors", "4", "file", "", "files", "",
					"use_individual_shapes", "true", "use_neighbors_cache", "true", "use_regular_agents", "true"));
		}
		List<SpeciesDraft.Facet> facets = new ArrayList<>();
		for (var facet : original.facets()) {
			String change = changes.remove(facet.name());
			if (change == null || change.equals(initialHeader.get(facet.name()))
					|| change.equals(facet.value()) || change.equals(facet.value().trim()))
				facets.add(facet);
			else if (!change.isBlank()) facets.add(new SpeciesDraft.Facet(facet.name(), change));
		}
		for (var change : changes.entrySet()) {
			if (!change.getValue().isBlank() && !change.getValue().equals(defaults.get(change.getKey())))
				facets.add(new SpeciesDraft.Facet(change.getKey(), change.getValue()));
		}
		return new SpeciesDraft(value("name").trim(), selectedSkills, facets,
				attributes == null ? original.variables() : attributes.localValues(), original.init(), original.reflexOrder());
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
				for (var variable : GamaSkillRegistry.INSTANCE.getVariablesForSkill("grid"))
					metadata.add(SpeciesWizardContext.attribute(variable, "grid"));
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
		if (error == null && kind != Kind.MODEL && draft.name().equals(draft.parent()))
			error = "A species cannot inherit from itself.";
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

	private final class Attributes {
		private final List<SpeciesDraft.Variable> locals = new ArrayList<>(original.variables());
		private final Map<String, InheritedAttribute> inherited = new LinkedHashMap<>();
		private final List<InheritedAttribute> shown = new ArrayList<>();
		private final List<Integer> localIndices = new ArrayList<>();
		private final Table table;
		private final Button add;
		private final Button remove;
		private final Button redefine;
		private final Map<Integer, Control> fields = new LinkedHashMap<>();
		private final Map<Integer, Label> fieldLabels = new LinkedHashMap<>();
		private final List<String> types = possibleTypeNames();
		private boolean loading;
		private boolean filteringTypes;
		private int sortColumn = -1;
		private boolean ascending = true;

		Attributes(final Composite parent) {
			Composite body = composite(parent, 2);
			((GridLayout) body.getLayout()).makeColumnsEqualWidth = false;
			body.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			Composite list = composite(body, 1);
			((GridLayout) list.getLayout()).marginWidth = 0;
			list.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
			table = new Table(list, SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL);
			table.setData("species.attributes", true);
			table.setHeaderVisible(true);
			table.setToolTipText("Attributes: Name identifies the attribute, Type defines its values, and From identifies its origin.");
			accessible(table, "Attributes");
			GridData tableData = new GridData(SWT.FILL, SWT.FILL, false, true);
			tableData.widthHint = 300;
			tableData.heightHint = 200;
			table.setLayoutData(tableData);
			for (String name : List.of("Name", "Type", "From")) {
				TableColumn column = new TableColumn(table, SWT.NONE);
				column.setText(name);
				column.setToolTipText(switch (name) {
					case "Name" -> "Attribute identifier.";
					case "Type" -> "Type of values stored in the attribute.";
					default -> "Species or skill providing the attribute; Redefined indicates a local override.";
				});
				column.setWidth("Name".equals(name) ? 125 : "Type".equals(name) ? 75 : 100);
				int index = table.getColumnCount() - 1;
				if (index == 0 || index == 2) {
					column.setToolTipText(column.getToolTipText() + " Click to sort; click again to reverse.");
					column.addListener(SWT.Selection, event -> {
						ascending = sortColumn != index || !ascending;
						sortColumn = index;
						table.setSortColumn(column);
						table.setSortDirection(ascending ? SWT.UP : SWT.DOWN);
						renderRows(selected() == null ? "" : selected().variable().name());
					});
				}
			}
			Composite detail = composite(body, 2);
			detail.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			Composite actions = composite(detail, 2);
			((GridLayout) actions.getLayout()).marginWidth = 0;
			actions.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			add = action(actions, "Add...", "Add attribute");
			remove = action(actions, "Remove", "Remove selected attribute");
			redefine = new Button(detail, SWT.CHECK);
			redefine.setText("Redefine");
			redefine.setToolTipText("Declare a local override of this inherited attribute. Uncheck to use its inherited values.");
			redefine.setData("species.redefine", true);
			redefine.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
			List<String> labels = List.of("Name", "Type", "Initial value", "Update", "Function", "Minimum", "Maximum");
			List<String> descriptions = List.of(
					"Attribute name. Enter an existing name to select that attribute instead of creating a duplicate.",
					"Attribute type. Type its first letters to filter the available types.",
					"GAML expression providing the initial attribute value.",
					"GAML expression recomputing the attribute value on each simulation cycle.",
					"",
					"Lower bound expression for this int or float attribute.",
					"Upper bound expression for this int or float attribute.");
			for (int i = 0; i < labels.size(); i++) {
				int index = i;
				if (index == 4) continue;
				Label label = label(detail, labels.get(i), descriptions.get(i));
				label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
				fieldLabels.put(index, label);
				Control control;
				if (index == 1) {
					Combo type = new Combo(detail, SWT.DROP_DOWN | SWT.V_SCROLL);
					type.setItems(types.toArray(String[]::new));
					type.addModifyListener(event -> {
						if (loading || filteringTypes) return;
						filterTypes(type);
						edit(index, type.getText());
					});
					type.addListener(SWT.Selection, event -> edit(index, type.getText()));
					control = type;
				} else {
					Text text = new Text(detail, SWT.BORDER | SWT.SINGLE);
					text.addModifyListener(event -> edit(index, text.getText()));
					control = text;
				}
				control.setData("species.attribute.field", i);
				control.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				accessible(control, labels.get(i));
				control.setToolTipText(descriptions.get(i));
				fields.put(index, control);
			}
			table.addListener(SWT.Selection, event -> select());
			redefine.addListener(SWT.Selection, event -> {
				InheritedAttribute item = selected();
				if (item == null) return;
				int index = localIndex(item.variable().name());
				if (redefine.getSelection() && index < 0) locals.add(item.variable());
				else if (!redefine.getSelection() && index >= 0) locals.remove(index);
				refresh(List.copyOf(inherited.values()));
				validateDraft();
			});
			add.setEnabled(editBody);
			add.addListener(SWT.Selection, event -> {
				String name = "my_attribute";
				int suffix = 2;
				while (localIndex(name) >= 0 || inherited.containsKey(name)) name = "my_attribute_" + suffix++;
				locals.add(new SpeciesDraft.Variable(name, "float", "", "", "", "", ""));
				refresh(List.copyOf(inherited.values()));
				for (int row = 0; row < localIndices.size(); row++)
					if (localIndices.get(row) == locals.size() - 1) table.setSelection(row);
				select();
				fields.get(0).setFocus();
				validateDraft();
			});
			remove.addListener(SWT.Selection, event -> {
				InheritedAttribute item = selected();
				if (item == null) return;
				int index = localIndices.get(table.getSelectionIndex());
				if (index >= 0) locals.remove(index);
				refresh(List.copyOf(inherited.values()));
				validateDraft();
			});
			table.addListener(SWT.KeyDown, event -> {
				if (event.keyCode == SWT.INSERT && add.getEnabled()) add.notifyListeners(SWT.Selection, null);
				if (event.keyCode == SWT.DEL && remove.getEnabled()) remove.notifyListeners(SWT.Selection, null);
			});
		}

		private void filterTypes(final Combo type) {
			String input = type.getText();
			String prefix = input.toLowerCase(java.util.Locale.ROOT);
			List<String> matches = types.stream().filter(candidate ->
					candidate.toLowerCase(java.util.Locale.ROOT).startsWith(prefix)).toList();
			filteringTypes = true;
			try {
				type.setItems(matches.toArray(String[]::new));
				type.setText(input);
				type.setSelection(new org.eclipse.swt.graphics.Point(input.length(), input.length()));
				if (!input.isBlank() && !matches.isEmpty()) type.setListVisible(true);
			} finally {
				filteringTypes = false;
			}
		}

		private void updateOrdinalFields(final String type) {
			boolean ordinal = "int".equalsIgnoreCase(type) || "float".equalsIgnoreCase(type);
			for (int index = 5; index <= 6; index++) {
				Label label = fieldLabels.get(index);
				Control control = fields.get(index);
				label.setVisible(ordinal);
				((GridData) label.getLayoutData()).exclude = !ordinal;
				control.setVisible(ordinal);
				((GridData) control.getLayoutData()).exclude = !ordinal;
			}
			fields.get(0).getParent().layout(true, true);
		}

		private int localIndex(final String name) {
			for (int i = 0; i < locals.size(); i++) if (locals.get(i).name().equals(name)) return i;
			return -1;
		}

		private InheritedAttribute selected() {
			int index = table.getSelectionIndex();
			return index < 0 ? null : shown.get(index);
		}

		void refresh(final List<InheritedAttribute> metadata) {
			InheritedAttribute previous = selected();
			String name = previous == null ? "" : previous.variable().name();
			inherited.clear();
			for (var item : metadata) inherited.put(item.variable().name(), item);
			shown.clear();
			localIndices.clear();
			shown.addAll(inherited.values());
			for (var item : shown) localIndices.add(localIndex(item.variable().name()));
			for (int i = 0; i < locals.size(); i++) {
				var local = locals.get(i);
				if (!inherited.containsKey(local.name())) {
					shown.add(new InheritedAttribute(local, value("name").trim(), true));
					localIndices.add(i);
				}
			}
			renderRows(name);
			select();
		}

		private String source(final int row) {
			var item = shown.get(row);
			return localIndices.get(row) >= 0 && inherited.containsKey(item.variable().name())
					? "Redefined" : item.origin();
		}

		private void renderRows(final String name) {
			if (sortColumn >= 0) {
				List<Integer> order = new ArrayList<>();
				for (int row = 0; row < shown.size(); row++) order.add(row);
				java.util.Comparator<Integer> comparator = java.util.Comparator.comparing(
						row -> sortColumn == 0 ? shown.get(row).variable().name() : source(row),
						String.CASE_INSENSITIVE_ORDER);
				comparator = comparator.thenComparing(row -> shown.get(row).variable().name());
				order.sort(ascending ? comparator : comparator.reversed());
				var sorted = order.stream().map(shown::get).toList();
				var indices = order.stream().map(localIndices::get).toList();
				shown.clear();
				shown.addAll(sorted);
				localIndices.clear();
				localIndices.addAll(indices);
			}
			table.removeAll();
			int selected = 0;
			for (int i = 0; i < shown.size(); i++) {
				var item = shown.get(i);
				int index = localIndices.get(i);
				var variable = index < 0 ? item.variable() : locals.get(index);
				TableItem row = new TableItem(table, SWT.NONE);
				row.setText(new String[] { variable.name(), variable.type(), source(i) });
				if (name.equals(variable.name())) selected = i;
			}
			if (!shown.isEmpty()) table.setSelection(selected);
		}

		private void select() {
			loading = true;
			try {
				var item = selected();
				boolean builtIn = item != null && inherited.containsKey(item.variable().name());
				int index = item == null ? -1 : localIndices.get(table.getSelectionIndex());
				boolean editable = editBody && index >= 0;
				redefine.setVisible(builtIn);
				((GridData) redefine.getLayoutData()).exclude = !builtIn;
				redefine.setEnabled(editBody && builtIn && item.redefinable());
				redefine.setSelection(builtIn && index >= 0);
				remove.setEnabled(editable);
				SpeciesDraft.Variable variable = item == null
						? new SpeciesDraft.Variable("", "", "", "", "", "", "")
						: index >= 0 ? locals.get(index) : item.variable();
				String[] values = { variable.name(), variable.type(), variable.init(), variable.update(), variable.function(),
						variable.min(), variable.max() };
				Combo type = (Combo) fields.get(1);
				List<String> choices = withCurrent(types, values[1]);
				type.setItems(choices.toArray(String[]::new));
				for (int i : fields.keySet()) {
					Control field = fields.get(i);
					if (field instanceof Text text) text.setText(values[i]);
					else if (field instanceof Combo combo) combo.setText(values[i]);
					field.setEnabled(editable && (!builtIn || i >= 2));
				}
				updateOrdinalFields(values[1]);
				redefine.getParent().layout(true, true);
			} finally { loading = false; }
		}

		private void edit(final int field, final String value) {
			if (loading) return;
			var item = selected();
			if (item == null) return;
			int index = localIndices.get(table.getSelectionIndex());
			if (index < 0) return;
			if (field == 0) {
				for (int row = 0; row < shown.size(); row++) {
					if (row != table.getSelectionIndex() && table.getItem(row).getText(0).equals(value)) {
						table.setSelection(row);
						table.showSelection();
						select();
						table.setFocus();
						validateDraft();
						return;
					}
				}
			}
			var v = locals.get(index);
			String[] values = { v.name(), v.type(), v.init(), v.update(), v.function(), v.min(), v.max() };
			values[field] = value;
			SpeciesDraft.Variable updated = new SpeciesDraft.Variable(values[0], values[1], values[2], values[3],
					values[4], values[5], values[6]);
			locals.set(index, updated);
			shown.set(table.getSelectionIndex(), new InheritedAttribute(updated, item.origin(), item.redefinable()));
			table.getSelection()[0].setText(0, updated.name());
			table.getSelection()[0].setText(1, updated.type());
			if (field == 0 && sortColumn >= 0) renderRows(updated.name());
			if (field == 1) updateOrdinalFields(value);
			validateDraft();
		}

		List<SpeciesDraft.Variable> localValues() { return List.copyOf(locals); }
	}
}
