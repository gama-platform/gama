package gaml.compiler.ui.wizards.species;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

import gaml.compiler.ui.wizards.species.SpeciesWizardContext.InheritedAttribute;

/** Native SWT regression using production controls and deterministic metadata. */
public final class SpeciesWizardTest {
	private static int checks;

	private static final class Dialog extends SpeciesWizard {
		Dialog(final Shell shell, final SpeciesDraft draft, final Function<SpeciesDraft, String> validator,
				final boolean editBody, final SpeciesWizardContext context) {
			this(shell, draft, validator, editBody, context, Kind.SPECIES);
		}

		Dialog(final Shell shell, final SpeciesDraft draft, final Function<SpeciesDraft, String> validator,
				final boolean editBody, final SpeciesWizardContext context, final Kind kind) {
			super(shell, draft, validator, editBody,
					kind == Kind.CLASS ? List.of("object", "base", "other") : List.of("agent", "animal", "wolf", "prey"),
					context, kind);
		}

		@Override
		protected List<String> possibleSkillNames() { return List.of("moving", "bouncing", InheritedSkillFixture.SKILL); }

		@Override
		protected List<String> possibleControlNames() { return List.of("reflex", "fsm"); }

		@Override
		protected List<String> possibleTypeNames() {
			return List.of("agent", "bool", "float", "int", "point", "string").stream().sorted().toList();
		}

		void click(final int id) { getButton(id).notifyListeners(SWT.Selection, new Event()); }
		boolean acceptsInput() { return getButton(IDialogConstants.OK_ID).getEnabled(); }
	}

	private static Control field(final Composite parent, final String key, final Object value) {
		for (Control control : parent.getChildren()) {
			if (value.equals(control.getData(key))) return control;
			if (control instanceof Composite composite) {
				Control found = field(composite, key, value);
				if (found != null) return found;
			}
		}
		return null;
	}

	private static Control header(final Dialog dialog, final String name) {
		return field(dialog.getShell(), "species.field", name);
	}

	private static Button button(final Composite parent, final String text) {
		for (Control control : parent.getChildren()) {
			if (control instanceof Button button
					&& (text.equals(button.getText()) || text.equals(button.getToolTipText()))) return button;
			if (control instanceof Composite composite) {
				Button found = button(composite, text);
				if (found != null) return found;
			}
		}
		return null;
	}

	private static Table attributes(final Dialog dialog) {
		return (Table) field(dialog.getShell(), "species.attributes", true);
	}

	private static ExpandableComposite advanced(final Dialog dialog) {
		return (ExpandableComposite) header(dialog, "Advanced");
	}

	private static void expand(final Dialog dialog, final boolean expanded) {
		advanced(dialog).setExpanded(expanded, true);
	}

	private static void select(final Table table, final String name) {
		for (int i = 0; i < table.getItemCount(); i++) {
			if (name.equals(table.getItem(i).getText(0))) {
				table.setSelection(i);
				table.notifyListeners(SWT.Selection, new Event());
				return;
			}
		}
		throw new AssertionError("Missing attribute " + name);
	}

	private static boolean contains(final Table table, final String name) {
		for (var row : table.getItems()) if (name.equals(row.getText(0))) return true;
		return false;
	}

	private static void choose(final Combo combo, final String value) {
		combo.select(combo.indexOf(value));
		combo.notifyListeners(SWT.Selection, new Event());
	}

	private static void check(final boolean condition, final String message) {
		checks++;
		if (!condition) throw new AssertionError(message);
	}

	private static void checkHelp(final Composite parent) {
		for (Control control : parent.getChildren()) {
			if (control instanceof Label label && label.getText().endsWith(":")) {
				check(label.getToolTipText() != null && !label.getToolTipText().isBlank(),
						"Hover explanation for " + label.getText());
			}
			if (control instanceof Composite composite) checkHelp(composite);
		}
	}

	private static InheritedAttribute inherited(final String name, final String type, final String origin,
			final boolean redefinable) {
		return new InheritedAttribute(new SpeciesDraft.Variable(name, type, "", "", "", "", ""), origin, redefinable);
	}

	private static void screenshot(final Shell shell, final String path) {
		shell.open();
		while (shell.getDisplay().readAndDispatch()) {}
		shell.update();
		Image image = new Image(shell.getDisplay(), shell.getClientArea().width, shell.getClientArea().height);
		GC gc = new GC(shell);
		try {
			gc.copyArea(image, 0, 0);
			ImageLoader loader = new ImageLoader();
			loader.data = new org.eclipse.swt.graphics.ImageData[] { image.getImageData() };
			loader.save(path, SWT.IMAGE_PNG);
		} finally {
			gc.dispose();
			image.dispose();
		}
	}

	private static final class ModelDialog extends SpeciesWizard {
		ModelDialog(final Shell shell, final SpeciesDraft draft, final SpeciesWizardContext context,
				final ModelLocation location, final boolean editable, final ModelDocumentation documentation) {
			super(shell, draft, d -> null, !editable, context, location, editable, documentation);
		}

		@Override
		protected List<String> possibleSkillNames() { return List.of("moving", "bouncing"); }

		@Override
		protected List<String> possibleControlNames() { return List.of("reflex", "fsm"); }

		@Override
		protected List<String> possibleTypeNames() { return List.of("float", "int", "string"); }

		@Override
		protected String locationError(final ModelLocation proposed) {
			return proposed.fileName().equals("taken.gaml") ? "A file with the same name already exists." : null;
		}

		Control field(final String name) { return SpeciesWizardTest.field(getShell(), "species.field", name); }
		void click(final int id) { getButton(id).notifyListeners(SWT.Selection, new Event()); }
		boolean acceptsInput() { return getButton(IDialogConstants.OK_ID).getEnabled(); }
	}

	private static void checkModel(final Shell shell, final SpeciesWizardContext context) {
		var docs = new ModelDocumentation("Ann", "ecology", "Line 1\nLine 2");
		var creation = new ModelDialog(shell, SpeciesDraft.empty(), context, new ModelLocation("/p/models", "New Model.gaml"),
				true, docs);
		creation.create();
		check(creation.getKind() == SpeciesWizard.Kind.MODEL, "Model kind");
		check(((Text) creation.field("Container")).getEditable() && ((Text) creation.field("File name")).getEditable(),
				"Creation location editable");
		check(button(creation.getShell(), "Browse...").getVisible(), "Creation offers Browse");
		check(((Text) creation.field("Model name")).getText().equals("NewModel"), "Model name derived from file name");
		((Text) creation.field("File name")).setText("2 preys.gaml");
		check(((Text) creation.field("Model name")).getText().equals("_2preys"), "File name keeps syncing model name");
		check(creation.field("Parent") == null && creation.field("Parallel") == null && creation.field("Mirrors") == null,
				"Model has no Parent, Mirrors or Parallel");
		check(creation.field("Skills") != null && creation.field("Skills").getParent() == creation.field("Control").getParent()
				&& ((ExpandableComposite) creation.field("Advanced")).getClient() == creation.field("Skills").getParent(),
				"Model skills in Advanced, on the Control row");
		var advancedRow = creation.field("Skills").getParent().getChildren();
		check(java.util.Arrays.asList(advancedRow).indexOf(creation.field("Skills"))
				< java.util.Arrays.asList(advancedRow).indexOf(creation.field("Control")), "Skills before Control");
		check(!((ExpandableComposite) creation.field("Advanced")).isExpanded(), "Model Advanced collapsed");
		check(creation.field("Control") != null && creation.field("Schedules") != null
				&& creation.field("Frequency") != null, "Model Advanced has control and scheduling");
		check(((ExpandableComposite) creation.field("Attributes")).isExpanded(), "Model attributes open");
		var docSection = (ExpandableComposite) creation.field("Documentation");
		check(!docSection.isExpanded(), "Documentation collapsed");
		check(((Text) SpeciesWizardTest.field((Composite) docSection.getClient(), "species.field", "Author")).getText()
				.equals("Ann"), "Author seeded");
		((Text) creation.field("Model name")).setText("prey_model");
		((Text) creation.field("File name")).setText("other.gaml");
		check(((Text) creation.field("Model name")).getText().equals("prey_model"), "Edited model name no longer synced");
		((Text) creation.field("File name")).setText("taken.gaml");
		check(!creation.acceptsInput(), "Existing file disables Finish/OK");
		((Text) creation.field("File name")).setText("a/b.gaml");
		check(!creation.acceptsInput(), "Path separators rejected in file name");
		((Text) creation.field("File name")).setText("ok.gaml");
		((Text) creation.field("Container")).setText("");
		check(!creation.acceptsInput(), "Empty container rejected");
		((Text) creation.field("Container")).setText("/p/models");
		((Text) creation.field("Model name")).setText("1bad");
		check(!creation.acceptsInput(), "Invalid model name rejected");
		((Text) creation.field("Model name")).setText("prey_model");
		check(creation.acceptsInput(), "Valid model accepted");
		Text tags = (Text) SpeciesWizardTest.field((Composite) docSection.getClient(), "species.field", "Tags");
		tags.setText("ecology, prey");
		creation.click(IDialogConstants.OK_ID);
		check(creation.getResult().name().equals("prey_model"), "Model name result");
		check(creation.getLocation().equals(new ModelLocation("/p/models", "ok.gaml")), "Location result");
		check(creation.getDocumentation().equals(new ModelDocumentation("Ann", "ecology, prey", "Line 1\nLine 2")),
				"Documentation result");

		var existing = new SpeciesDraft("predators", List.of(), List.of(new SpeciesDraft.Facet("control", "fsm")),
				List.of(), "", List.of());
		var edit = new ModelDialog(shell, existing, context, new ModelLocation("/p/models", "predators.gaml"), false,
				docs);
		edit.create();
		check(!((Text) edit.field("Container")).getEditable() && !((Text) edit.field("File name")).getEditable(),
				"Editing location read-only");
		check(!button(edit.getShell(), "Browse...").getVisible(), "Editing hides Browse");
		check(((Text) edit.field("Model name")).getText().equals("predators"), "Existing model name shown");
		edit.click(IDialogConstants.OK_ID);
		check(edit.getResult().equals(existing) && edit.getDocumentation().equals(docs), "Unchanged edit is a no-op");
		var cancelled = new ModelDialog(shell, existing, context, null, false, docs);
		cancelled.create();
		((Text) cancelled.field("Model name")).setText("changed");
		cancelled.click(IDialogConstants.CANCEL_ID);
		check(cancelled.getResult() == null && cancelled.getLocation() == null, "Model Cancel has no result");
	}

	private static void checkBuiltInParents(final Shell shell) throws Exception {
		try (var fixture = new BuiltInParentFixture()) {
			check(SpeciesWizardContext.builtInParentNames().contains(BuiltInParentFixture.PARENT),
					"Registered built-in parent names are discovered");
			for (String excluded : List.of(BuiltInParentFixture.MODEL, BuiltInParentFixture.EXPERIMENT,
					gama.api.kernel.GamaMetaModel.INTERNAL_GLOBAL_SCHEDULER_SPECIES)) {
				check(SpeciesWizardContext.builtInParent(excluded) == null
						&& !SpeciesWizardContext.builtInParentNames().contains(excluded),
						"Inapplicable built-in excluded: " + excluded);
			}

			check(SpeciesWizardContext.builtInParent("wizard_missing") == null,
					"Unknown built-in parent is not recognized");
			for (var kind : List.of(SpeciesWizard.Kind.SPECIES, SpeciesWizard.Kind.GRID)) {
				var dialog = new Dialog(shell, SpeciesDraft.empty(), draft -> null, true,
						SpeciesWizardContext.builtIns(), kind);
				dialog.create();
				Combo parent = (Combo) header(dialog, "Parent");
				check(parent.indexOf(BuiltInParentFixture.PARENT) >= 0, kind + " offers registered built-in parent");
				choose(parent, BuiltInParentFixture.PARENT);
				check(contains(attributes(dialog), BuiltInParentFixture.ATTRIBUTE),
						kind + " resolves registered inherited attributes");
				select(attributes(dialog), BuiltInParentFixture.ATTRIBUTE);
				check(!button(dialog.getShell(), "Redefine").getEnabled(),
						kind + " preserves protected built-in attribute metadata");
				dialog.click(IDialogConstants.OK_ID);
				SpeciesDraft accepted = dialog.getResult();
				check(accepted.parent().equals(BuiltInParentFixture.PARENT),
						kind + " accepts built-in parent facet");
				var unchanged = new Dialog(shell, accepted, draft -> null, true, SpeciesWizardContext.builtIns(), kind);
				unchanged.create();
				unchanged.click(IDialogConstants.OK_ID);
				check(accepted.equals(unchanged.getResult()), kind + " built-in parent no-op retains the draft");
				var external = new SpeciesDraft("existing", List.of(),
						List.of(new SpeciesDraft.Facet("parent", "imported_parent")), List.of(), "", List.of());
				var retained = new Dialog(shell, external, draft -> null, true, SpeciesWizardContext.builtIns(), kind);
				retained.create();
				retained.click(IDialogConstants.OK_ID);
				var expectedFacets = new java.util.ArrayList<>(external.facets());
				if (kind == SpeciesWizard.Kind.GRID)
					expectedFacets.add(new SpeciesDraft.Facet("use_individual_shapes", "false"));
				check(new SpeciesDraft(external.name(), external.skills(), expectedFacets, external.variables(),
						external.init(), external.reflexOrder()).equals(retained.getResult()),
						kind + " retains existing external parent (grid explicitly saves absent shapes as false)");
			}
			SpeciesWizardContext filtered = new SpeciesWizardContext() {
				@Override
				public List<InheritedAttribute> attributes(final SpeciesDraft selection) { return List.of(); }

				@Override
				public List<String> parentNames(final List<String> candidates) {
					return candidates.stream().filter(name -> !BuiltInParentFixture.PARENT.equals(name)).toList();
				}
			};
			var filteredDialog = new Dialog(shell, SpeciesDraft.empty(), draft -> null, true,
					filtered.and(draft -> List.of()));
			filteredDialog.create();
			check(((Combo) header(filteredDialog, "Parent")).indexOf(BuiltInParentFixture.PARENT) < 0,
					"Composed editor filter runs after built-in candidates are merged");
			check(((Combo) header(filteredDialog, "Mirrors")).indexOf("wolf") >= 0,
					"Parent filtering does not remove mirror candidates");
			filteredDialog.click(IDialogConstants.CANCEL_ID);
		}
	}

	private static void checkInheritedSkills(final Shell shell) throws Exception {
		try (var fixture = new InheritedSkillFixture()) {
			var registry = gama.api.additions.registries.GamaSkillRegistry.INSTANCE;
			var own = new ArrayList<String>();
			registry.getVariablesForSkill(InheritedSkillFixture.SKILL).forEach(variable -> own.add(variable.getName()));
			check(own.equals(List.of("direct_attribute")), "Own-attributes registry API remains unchanged");
			var effective = registry.getEffectiveVariablesForSkill(InheritedSkillFixture.SKILL);
			check(effective.stream().map(variable -> variable.getName()).sorted().toList()
					.equals(List.of("acceleration", "direct_attribute", "speed")),
					"Registry resolves effective inherited skill attributes");
			check(registry.getEffectiveVariablesForSkill("wizard_unknown_skill").isEmpty(),
					"Unknown effective skill lookup is empty");
			boolean immutable = false;
			try { effective.clear(); } catch (UnsupportedOperationException expected) { immutable = true; }
			check(immutable, "Effective attribute list is an immutable snapshot");
			var metadata = SpeciesWizardContext.skillAttributes(InheritedSkillFixture.SKILL, "skill: driving");
			check(metadata.stream().map(attribute -> attribute.variable().name()).sorted().toList()
					.equals(List.of("acceleration", "direct_attribute", "speed")),
					"Driving class hierarchy attributes supplement directly registered attributes");
			check(metadata.stream().filter(attribute -> attribute.variable().name().equals("speed"))
					.findFirst().orElseThrow().redefinable(), "Driving override wins over moving superclass");
			for (var kind : List.of(SpeciesWizard.Kind.SPECIES, SpeciesWizard.Kind.GRID, SpeciesWizard.Kind.MODEL)) {
				var dialog = new Dialog(shell, SpeciesDraft.empty(), draft -> null, true,
						SpeciesWizardContext.builtIns(), kind);
				dialog.create();
				if (kind == SpeciesWizard.Kind.MODEL) ((Text) header(dialog, "Model name")).setText("driving_model");
				Table table = attributes(dialog);
				check(!contains(table, "acceleration"), kind + " Driving initially absent");
				var skill = java.util.Arrays.stream(header(dialog, "Skills").getMenu().getItems())
						.filter(item -> item.getText().equals(InheritedSkillFixture.SKILL)).findFirst().orElseThrow();
				skill.setSelection(true);
				skill.notifyListeners(SWT.Selection, new Event());
				check(contains(table, "acceleration") && contains(table, "speed") && contains(table, "direct_attribute"),
						kind + " Selecting driving immediately displays inherited attributes");
				select(table, "acceleration");
				check(table.getSelection()[0].getText(2).equals("skill: " + InheritedSkillFixture.SKILL),
						kind + " Inherited attribute origin is selected skill");
				skill.setSelection(false);
				skill.notifyListeners(SWT.Selection, new Event());
				check(!contains(table, "acceleration"), kind + " Deselecting driving removes inherited attributes");
				skill.setSelection(true);
				skill.notifyListeners(SWT.Selection, new Event());
				dialog.click(IDialogConstants.OK_ID);
				check(dialog.getResult().variables().isEmpty()
						&& dialog.getResult().skills().contains(InheritedSkillFixture.SKILL),
						kind + " Inherited attributes are not materialized as local declarations");
			}
		}
	}

	private static void checkClasses(final Shell shell) {
		AtomicInteger calls = new AtomicInteger();
		Function<SpeciesDraft, String> validator = draft -> { calls.incrementAndGet(); return null; };
		SpeciesWizardContext context = draft -> "base".equals(draft.parent("object"))
				? List.of(inherited("inherited", "int", "parent: base", true)) : List.of();
		var dialog = new Dialog(shell, SpeciesDraft.empty(), validator, true, context, SpeciesWizard.Kind.CLASS);
		dialog.create();
		check(dialog.getShell().getText().equals("Class"), "Class title");
		check(((Text) header(dialog, "Name")).getText().equals("my_class"), "Class creation name");
		Combo parent = (Combo) header(dialog, "Parent");
		check(parent.getText().equals("object") && !List.of(parent.getItems()).contains("agent"),
				"Class defaults to object, without agent parent");
		for (String name : List.of("Skills", "Advanced", "Control", "Mirrors", "Parallel", "Schedules", "Frequency",
				"Documentation", "Container", "File name"))
			check(header(dialog, name) == null, "Class omits " + name);
		checkHelp(dialog.getShell());
		choose(parent, "base");
		check(contains(attributes(dialog), "inherited") && !contains(attributes(dialog), "location"),
				"Class parent refresh exposes class attributes only");
		select(attributes(dialog), "inherited");
		Button redefine = button(dialog.getShell(), "Redefine");
		redefine.setSelection(true);
		redefine.notifyListeners(SWT.Selection, new Event());
		((Text) field(dialog.getShell(), "species.attribute.field", 2)).setText("42");
		Button virtual = (Button) header(dialog, "Virtual");
		virtual.setSelection(true);
		virtual.notifyListeners(SWT.Selection, new Event());
		check(calls.get() == 0, "Class metadata/field edits do not invoke adapter compilation");
		Table classAttributes = attributes(dialog);
		classAttributes.getColumn(0).notifyListeners(SWT.Selection, new Event());
		check(classAttributes.getSortDirection() == SWT.UP, "Class attributes support sorting");
		button(dialog.getShell(), "Add attribute").notifyListeners(SWT.Selection, new Event());
		check(classAttributes.getSelection()[0].getText(0).equals("my_attribute"),
				"Class Add selects a new attribute under sorting");
		button(dialog.getShell(), "Remove selected attribute").notifyListeners(SWT.Selection, new Event());
		check(!contains(classAttributes, "my_attribute") && contains(classAttributes, "inherited"),
				"Class Remove deletes only the selected local attribute");
		dialog.click(IDialogConstants.OK_ID);
		var accepted = dialog.getResult();
		check(calls.get() == 1 && accepted != null, "Class validated only on OK");
		check(accepted.parent("object").equals("base") && SpeciesWizardContext.facet(accepted, "virtual", "").equals("true"),
				"Class parent and virtual accepted");
		check(accepted.variables().size() == 1 && accepted.variables().getFirst().init().equals("42"),
				"Class inherited attribute redefinition is local");
		var unchanged = new Dialog(shell, accepted, validator, true, context, SpeciesWizard.Kind.CLASS);
		unchanged.create();
		unchanged.click(IDialogConstants.OK_ID);
		check(accepted.equals(unchanged.getResult()), "Class no-op preserves draft exactly");
		var cancelled = new Dialog(shell, accepted, validator, true, context, SpeciesWizard.Kind.CLASS);
		cancelled.create();
		((Text) header(cancelled, "Name")).setText("cancelled");
		int before = calls.get();
		cancelled.click(IDialogConstants.CANCEL_ID);
		check(cancelled.getResult() == null && calls.get() == before, "Class cancel returns no draft and never compiles");
		var implicit = new SpeciesDraft("plain", List.of(), List.of(), List.of(), "", List.of());
		var defaults = new Dialog(shell, implicit, validator, true, context, SpeciesWizard.Kind.CLASS);
		defaults.create();
		defaults.click(IDialogConstants.OK_ID);
		check(implicit.equals(defaults.getResult()), "Class implicit object/false defaults do not materialize facets");
		var text = new Dialog(shell, accepted, validator, false, context, SpeciesWizard.Kind.CLASS);
		text.create();
		check(!button(text.getShell(), "Add attribute").getEnabled(), "Source class attributes visibly read-only");
		text.click(IDialogConstants.CANCEL_ID);
		var invalid = new Dialog(shell, new SpeciesDraft("bad", List.of("moving"), List.of(), List.of(), "", List.of()),
				validator, true, context, SpeciesWizard.Kind.CLASS);
		invalid.create();
		check(!invalid.acceptsInput(), "Class rejects hidden agent settings rather than silently preserving them");
		invalid.click(IDialogConstants.CANCEL_ID);
	}

	public static void main(final String[] args) throws Exception {
		Display display = new Display();
		Shell shell = new Shell(display);
		try {
			check(WizardWidgets.labelText("use_individual_shapes").equals("Use individual shapes")
					&& WizardWidgets.labelText("z_near").equals("Z near"),
					"Shared wizard labels capitalize and humanize facet labels without changing facet keys");
			checkBuiltInParents(shell);
			checkInheritedSkills(shell);
			checkClasses(shell);
			AtomicInteger contextCalls = new AtomicInteger();
			SpeciesWizardContext context = draft -> {
				contextCalls.incrementAndGet();
				var result = new ArrayList<InheritedAttribute>();
				result.add(inherited("location", "point", "agent", true));
				result.add(inherited("index", "int", "agent", false));
				if ("animal".equals(draft.parent())) result.add(inherited("energy", "float", "parent: animal", true));
				if (draft.skills().contains("moving")) result.add(inherited("speed", "float", "skill: moving", true));
				if ("fsm".equals(SpeciesWizardContext.facet(draft, "control", "reflex")))
					result.add(inherited("state", "string", "control: fsm", true));
				String mirror = SpeciesWizardContext.facet(draft, "mirrors", "");
				if (!mirror.isEmpty()) result.add(inherited("target", mirror, "mirror: " + mirror, false));
				return result;
			};
			AtomicInteger validationCalls = new AtomicInteger();
			Function<SpeciesDraft, String> validator = draft -> { validationCalls.incrementAndGet(); return null; };
			var dialog = new Dialog(shell, SpeciesDraft.empty(), validator, true, context);
			dialog.create();
			Text source = (Text) header(dialog, "Frequency");
			source.setText("  energy + unknown( /* incomplete */ ");
			source.notifyListeners(SWT.FocusOut, new Event());
			source.notifyListeners(SWT.DefaultSelection, new Event());
			check(source.getText().equals("  energy + unknown( /* incomplete */ "), "Raw expressions preserved");
			source.setText("");
			check(source.getText().isEmpty(), "Empty optional expression retained");
			check(((Text) header(dialog, "Name")).getText().equals("my_species"), "Creation default name");
			check(!((Button) header(dialog, "Virtual")).getSelection(), "Virtual unchecked by default");
			check(((Combo) header(dialog, "Parallel")).getText().isEmpty(), "Parallel defaults to platform preference");
			check(((Combo) header(dialog, "Parent")).getText().equals("agent"), "Default parent agent");
			check(((Combo) header(dialog, "Control")).getText().equals("reflex"), "Default control reflex");
			check(((Combo) header(dialog, "Mirrors")).getText().isEmpty(), "Default mirror empty");
			check(((Text) header(dialog, "Schedules")).getText().isEmpty()
					&& ((Text) header(dialog, "Frequency")).getText().isEmpty(), "Scheduling fields empty by default");
			check(!advanced(dialog).isExpanded(), "Advanced collapsed initially");
			check(!header(dialog, "Control").isVisible(), "Advanced controls hidden initially");
			check(!header(dialog, "Parallel").isVisible(), "Parallel is hidden inside Advanced");
			check(field(dialog.getShell(), "species.attributes", true) != null,
					"Attributes appear directly in the structure form");
			check(((ExpandableComposite) header(dialog, "Attributes")).isExpanded(),
					"Attributes is an expanded disclosure section");
			check(field(dialog.getShell(), "species.attribute.field", 4) == null,
					"Function field is not exposed");
			check(field(dialog.getShell(), "species.attribute.field", 1).getClass() == Combo.class,
					"Attribute type uses a searchable type list");
			check(header(dialog, "Name").getClass() == Text.class
					&& header(dialog, "Virtual").getClass() == Button.class
					&& header(dialog, "Parent").getClass() == Combo.class
					&& field(dialog.getShell(), "species.attribute.field", 1).getClass() == Combo.class,
					"Production form uses standard native controls");
			check((((Combo) header(dialog, "Parent")).getStyle() & SWT.READ_ONLY) != 0,
					"Parent choices are read-only");
			check(dialog.getShell().getSize().x <= 720 && dialog.getShell().getSize().y <= 720,
					"Collapsed form fits compact wizard dimensions");
			check(Math.abs(header(dialog, "Name").toDisplay(0, 0).y
							- header(dialog, "Virtual").toDisplay(0, 0).y) < 8,
					"Name and Virtual share the first row");
			check(Math.abs(header(dialog, "Parent").toDisplay(0, 0).y
					- (header(dialog, "Skills").toDisplay(0, 0).y + header(dialog, "Skills").getSize().y / 2)
					+ header(dialog, "Parent").getSize().y / 2) < 8,
					"Parent and Skills share the second row");
			check(contains(attributes(dialog), "location") && contains(attributes(dialog), "index"), "Base built-in attributes visible");
			check(!field(dialog.getShell(), "species.attribute.field", 5).getVisible()
					&& !field(dialog.getShell(), "species.attribute.field", 6).getVisible(),
					"Minimum and maximum are hidden for non-ordinal point type");
			check(validationCalls.get() == 0, "Context compilation is not performed on keystrokes");
			select(attributes(dialog), "location");
			Button redefine = button(dialog.getShell(), "Redefine");
			check(redefine.getVisible() && !redefine.getSelection(), "Inherited attribute initially not redefined");
			check(!field(dialog.getShell(), "species.attribute.field", 2).getEnabled(), "Inherited values disabled until Redefine");
			redefine.setSelection(true);
			redefine.notifyListeners(SWT.Selection, new Event());
			check(field(dialog.getShell(), "species.attribute.field", 2).getEnabled(), "Redefine enables facet editing");
			check(!field(dialog.getShell(), "species.attribute.field", 0).getEnabled(), "Inherited name remains fixed");
			((Text) field(dialog.getShell(), "species.attribute.field", 2)).setText("{1,2,0}");
			select(attributes(dialog), "index");
			check(!redefine.getEnabled(), "Protected built-in attribute cannot be redefined");
			choose((Combo) header(dialog, "Parent"), "animal");
			check(contains(attributes(dialog), "energy"), "Parent change refreshes inherited attributes");
			select(attributes(dialog), "energy");
			check(field(dialog.getShell(), "species.attribute.field", 5).getVisible()
					&& field(dialog.getShell(), "species.attribute.field", 6).getVisible(),
					"Minimum and maximum appear for float attributes");
			Button skills = (Button) header(dialog, "Skills");
			MenuItem moving = skills.getMenu().getItem(0);
			check((moving.getStyle() & SWT.CHECK) != 0, "Skills menu uses independent checkmarks");
			moving.setSelection(true);
			moving.notifyListeners(SWT.Selection, new Event());
			check(contains(attributes(dialog), "speed"), "Skill selection adds its attributes");
			check(moving.getSelection(), "Selected skill remains checked");
			MenuItem bouncing = skills.getMenu().getItem(1);
			bouncing.setSelection(true);
			bouncing.notifyListeners(SWT.Selection, new Event());
			check(moving.getSelection() && bouncing.getSelection() && skills.getText().equals("moving, bouncing"),
					"Multiple skills can be selected simultaneously");
			moving.setSelection(false);
			moving.notifyListeners(SWT.Selection, new Event());
			check(!contains(attributes(dialog), "speed"), "Skill deselection removes inherited attributes");
			check(!moving.getSelection() && bouncing.getSelection() && skills.getText().equals("bouncing"),
					"Deselecting one skill leaves other choices selected");
			expand(dialog, true);
			check(header(dialog, "Parallel").getParent().getParent() == advanced(dialog).getClient(),
					"Parallel belongs to the Advanced section");
			choose((Combo) header(dialog, "Control"), "fsm");
			check(contains(attributes(dialog), "state"), "Architecture selection refreshes attributes");
			choose((Combo) header(dialog, "Mirrors"), "prey");
			select(attributes(dialog), "target");
			check(attributes(dialog).getSelection()[0].getText(1).equals("prey"), "Mirror adds only typed target");
			check(!contains(attributes(dialog), "prey_attribute"), "Mirror does not copy target-species attributes");
			((Text) header(dialog, "Frequency")).setText("2");
			((Text) header(dialog, "Schedules")).setText("shuffle(my_species)");
			Button virtual = (Button) header(dialog, "Virtual");
			virtual.setSelection(true);
			virtual.notifyListeners(SWT.Selection, new Event());
			choose((Combo) header(dialog, "Parallel"), "true");
			expand(dialog, false);
			check(!header(dialog, "Parallel").isVisible(), "Collapsing Advanced hides Parallel");
			expand(dialog, true);
			check(((Combo) header(dialog, "Parallel")).getText().equals("true"),
					"Expanding Advanced preserves entered values");
			dialog.click(IDialogConstants.OK_ID);
			check(dialog.getReturnCode() == Window.OK, "OK accepted");
			check(dialog.getResult().variables().size() == 1
					&& dialog.getResult().variables().getFirst().name().equals("location"), "Only explicit override emitted");
			check(dialog.getResult().variables().getFirst().init().equals("{1,2,0}"), "Redefined attribute facets stored");
			check(dialog.getResult().skills().equals(List.of("bouncing")), "Checked skills are stored in the draft");
			check(SpeciesWizardContext.facet(dialog.getResult(), "virtual", "").equals("true"), "Virtual stored");
			check(SpeciesWizardContext.facet(dialog.getResult(), "parallel", "").equals("true"), "Parallel stored");
			check(SpeciesWizardContext.facet(dialog.getResult(), "control", "").equals("fsm"), "Control stored");
			check(SpeciesWizardContext.facet(dialog.getResult(), "frequency", "").equals("2"), "Frequency stored");
			check(validationCalls.get() == 1, "Adapter validates once on OK");
			check(contextCalls.get() >= 6, "Metadata refreshes on selection changes");

			var unchanged = new SpeciesDraft("wolf", List.of("unknown_existing_skill"),
					List.of(new SpeciesDraft.Facet("frequency", "  2 "), new SpeciesDraft.Facet("parallel", " 20 "),
							new SpeciesDraft.Facet("parent", "animal"),
							new SpeciesDraft.Facet("unsupported_facet", "true"), new SpeciesDraft.Facet("control", "reflex")),
					List.of(new SpeciesDraft.Variable("energy", "float", "3", "", "age + 1", "", "")),
					"write energy;", List.of("step"));
			var noOp = new Dialog(shell, unchanged, validator, true, context);
			noOp.create();
			check(java.util.Arrays.stream(header(noOp, "Skills").getMenu().getItems()).anyMatch(item ->
					item.getText().equals("unknown_existing_skill") && item.getSelection()),
					"Existing unregistered skills remain available and checked");
			noOp.click(IDialogConstants.OK_ID);
			check(unchanged.equals(noOp.getResult()), "No-op preserves source values/order, unknown facets and skills");
			check(noOp.getResult().variables().getFirst().function().equals("age + 1"),
					"Hidden Function value is retained");
			var functionPreservation = new Dialog(shell, unchanged, validator, true, context);
			functionPreservation.create();
			select(attributes(functionPreservation), "energy");
			((Text) field(functionPreservation.getShell(), "species.attribute.field", 3)).setText("energy + 2");
			functionPreservation.click(IDialogConstants.OK_ID);
			check(functionPreservation.getResult().variables().getFirst().function().equals("age + 1"),
					"Editing other facets retains the hidden Function value");
			var parallelEdit = new Dialog(shell, unchanged, validator, true, context);
			parallelEdit.create();
			((Combo) header(parallelEdit, "Parallel")).setText("40");
			parallelEdit.click(IDialogConstants.OK_ID);
			check(SpeciesWizardContext.facet(parallelEdit.getResult(), "parallel", "").equals("40"),
					"Parallel accepts integer thresholds");
			var parallelClear = new Dialog(shell, unchanged, validator, true, context);
			parallelClear.create();
			((Combo) header(parallelClear, "Parallel")).setText("");
			parallelClear.click(IDialogConstants.OK_ID);
			check(parallelClear.getResult().facets().stream().noneMatch(facet -> facet.name().equals("parallel")),
					"Clearing Parallel restores platform preference");

			var undoRedefine = new Dialog(shell, unchanged, validator, true, context);
			undoRedefine.create();
			select(attributes(undoRedefine), "energy");
			Button undoCheck = button(undoRedefine.getShell(), "Redefine");
			check(undoCheck.getSelection(), "Existing inherited-name declaration is marked Redefined");
			undoCheck.setSelection(false);
			undoCheck.notifyListeners(SWT.Selection, new Event());
			undoRedefine.click(IDialogConstants.OK_ID);
			check(undoRedefine.getResult().variables().isEmpty(), "Unchecking Redefine removes only local declaration");

			var cancelled = new Dialog(shell, unchanged, validator, true, context);
			cancelled.create();
			Event insert = new Event();
			insert.keyCode = SWT.INSERT;
			attributes(cancelled).notifyListeners(SWT.KeyDown, insert);
			check(contains(attributes(cancelled), "my_attribute"), "Insert shortcut creates local attribute");
			select(attributes(cancelled), "my_attribute");
			Event delete = new Event();
			delete.keyCode = SWT.DEL;
			attributes(cancelled).notifyListeners(SWT.KeyDown, delete);
			check(!contains(attributes(cancelled), "my_attribute"), "Delete shortcut removes local attribute");
			cancelled.click(IDialogConstants.CANCEL_ID);
			check(cancelled.getResult() == null, "Cancel discards all edits");

			var headerOnly = new Dialog(shell, unchanged, validator, false, context);
			headerOnly.create();
			select(attributes(headerOnly), "energy");
			check(!button(headerOnly.getShell(), "Redefine").getEnabled(), "Text header-only inherited attributes read-only");
			check(!button(headerOnly.getShell(), "Add attribute").getEnabled(), "Header-only cannot create attributes");
			headerOnly.click(IDialogConstants.OK_ID);
			check(headerOnly.getResult().equals(unchanged), "Header-only acceptance preserves body draft");

			var defaults = new Dialog(shell, SpeciesDraft.empty(), validator, true, context);
			defaults.create();
			defaults.click(IDialogConstants.OK_ID);
			check(defaults.getResult().facets().isEmpty() && defaults.getResult().variables().isEmpty(),
					"Implicit defaults and unmodified inherited attributes do not emit declarations");

			var complexVirtual = new SpeciesDraft("wolf", List.of(),
					List.of(new SpeciesDraft.Facet("virtual", "some_condition")), List.of(), "", List.of());
			var complexNoOp = new Dialog(shell, complexVirtual, validator, true, context);
			complexNoOp.create();
			check(header(complexNoOp, "Virtual") instanceof Button
					&& header(complexNoOp, "Virtual:expression") == null, "Species Virtual is checkbox-only for nonliteral source");
			complexNoOp.click(IDialogConstants.OK_ID);
			check(complexVirtual.equals(complexNoOp.getResult()), "Untouched complex existing virtual facet is preserved");
			for (SpeciesWizard.Kind virtualKind : List.of(SpeciesWizard.Kind.SPECIES, SpeciesWizard.Kind.GRID, SpeciesWizard.Kind.CLASS)) {
				var virtualDialog = new Dialog(shell, complexVirtual, validator, true, context, virtualKind);
				virtualDialog.create();
				check(header(virtualDialog, "Virtual") instanceof Button b && (b.getStyle() & SWT.CHECK) != 0
						&& header(virtualDialog, "Virtual:expression") == null, virtualKind + " Virtual is direct checkbox");
				Button virtualControl = (Button) header(virtualDialog, "Virtual");
				virtualControl.setSelection(true);
				virtualControl.notifyListeners(SWT.Selection, new Event());
				virtualControl.setSelection(false);
				virtualControl.notifyListeners(SWT.Selection, new Event());
				virtualDialog.click(IDialogConstants.OK_ID);
				check(virtualDialog.getResult() != null && SpeciesWizardContext.facet(virtualDialog.getResult(), "virtual", "").isEmpty(),
						virtualKind + " edited Virtual expression replaced with omitted false default");
			}

			var duplicateEdits = new Dialog(shell, SpeciesDraft.empty(), validator, true, context);
			duplicateEdits.create();
			var typeSearch = new Dialog(shell, SpeciesDraft.empty(), validator, true, context);
			typeSearch.create();
			button(typeSearch.getShell(), "Add attribute").notifyListeners(SWT.Selection, new Event());
			Combo searchableType = (Combo) field(typeSearch.getShell(), "species.attribute.field", 1);
			check((searchableType.getStyle() & SWT.READ_ONLY) == 0,
					"Type choice permits typing a prefix");
			searchableType.setText("fl");
			check(java.util.Arrays.stream(searchableType.getItems()).allMatch(type -> type.startsWith("fl"))
					&& java.util.Arrays.asList(searchableType.getItems()).contains("float"),
					"Typing a type prefix filters choices");
			searchableType.setText("float");
			check(field(typeSearch.getShell(), "species.attribute.field", 5).getVisible(),
					"Float type reveals min/max");
			searchableType.setText("string");
			check(!field(typeSearch.getShell(), "species.attribute.field", 5).getVisible(),
					"String type hides min/max");
			typeSearch.click(IDialogConstants.CANCEL_ID);
			button(duplicateEdits.getShell(), "Add attribute").notifyListeners(SWT.Selection, new Event());
			button(duplicateEdits.getShell(), "Add attribute").notifyListeners(SWT.Selection, new Event());
			Text attrName = (Text) field(duplicateEdits.getShell(), "species.attribute.field", 0);
			attrName.setText("my_attribute");
			check(attributes(duplicateEdits).getSelection()[0].getText(0).equals("my_attribute")
					&& duplicateEdits.acceptsInput(), "Existing name jumps to its row instead of creating a duplicate");
			((Text) field(duplicateEdits.getShell(), "species.attribute.field", 2)).setText("7");
			check(attributes(duplicateEdits).getSelection()[0].getText(2).equals("my_species"),
					"Local origin displays actual species name");
			((Text) header(duplicateEdits, "Name")).setText("renamed_species");
			check(attributes(duplicateEdits).getSelection()[0].getText(2).equals("renamed_species"),
					"Local origin updates when species name changes");
			attrName.setText("location");
			check(attributes(duplicateEdits).getSelection()[0].getText(0).equals("location")
					&& button(duplicateEdits.getShell(), "Redefine").getEnabled(),
					"Existing redefinable attribute jumps to its override controls");
			select(attributes(duplicateEdits), "my_attribute");
			attrName.setText("index");
			check(attributes(duplicateEdits).getSelection()[0].getText(0).equals("index")
					&& !attrName.getEnabled() && !button(duplicateEdits.getShell(), "Redefine").getEnabled(),
					"Existing protected attribute stops name editing");
			duplicateEdits.click(IDialogConstants.OK_ID);
			check(duplicateEdits.getResult().variables().size() == 2
					&& duplicateEdits.getResult().variables().getFirst().name().equals("my_attribute")
					&& duplicateEdits.getResult().variables().getFirst().init().equals("7"),
					"Jumping to existing attributes preserves both local declarations");

			var staleOverride = new Dialog(shell, unchanged, validator, true, context);
			staleOverride.create();
			choose((Combo) header(staleOverride, "Parent"), "agent");
			check(contains(attributes(staleOverride), "energy"), "Changing parent preserves an explicit local override");
			staleOverride.click(IDialogConstants.OK_ID);
			check(staleOverride.getResult().variables().equals(unchanged.variables()),
					"Former inherited override becomes local without losing facet values");

			var invalid = new Dialog(shell, SpeciesDraft.empty(), draft -> "Model error", true, context);
			invalid.create();
			invalid.click(IDialogConstants.OK_ID);
			check(invalid.getResult() == null && !invalid.getShell().isDisposed(), "Context validation error keeps form open");
			check(invalid.getErrorMessage().equals("Model error"), "Context error displayed");
			((Text) header(invalid, "Name")).setText("not valid");
			check(!invalid.acceptsInput(), "Invalid name disables OK");
			invalid.click(IDialogConstants.CANCEL_ID);
			var preview = new Dialog(shell, SpeciesDraft.empty(), validator, true, context);
			preview.create();
			preview.getShell().open();
			while (display.readAndDispatch()) {}
			if (args.length == 1) screenshot(preview.getShell(), args[0]);
			check(attributes(preview).getSize().x >= 300, "Attributes list is widened");
			ExpandableComposite attributeDisclosure = (ExpandableComposite) header(preview, "Attributes");
			attributeDisclosure.setExpanded(false, true);
			check(!attributes(preview).isVisible(), "Attributes disclosure can collapse");
			attributeDisclosure.setExpanded(true, true);
			check(attributes(preview).isVisible(), "Attributes disclosure can reopen");
			Button addAction = button(preview.getShell(), "Add attribute");
			Button removeAction = button(preview.getShell(), "Remove selected attribute");
			check(addAction.getText().equals("Add...") && removeAction.getText().equals("Remove"),
					"Standard Add and Remove buttons replace icons");
			check(addAction.toDisplay(0, 0).x > attributes(preview).toDisplay(0, 0).x
					+ attributes(preview).getSize().x
					&& addAction.toDisplay(0, 0).y < button(preview.getShell(), "Redefine").toDisplay(0, 0).y,
					"Attribute actions sit right of the list above Redefine");
			checkHelp(preview.getShell());
			Event openSkills = new Event();
			openSkills.keyCode = SWT.ARROW_DOWN;
			header(preview, "Skills").notifyListeners(SWT.KeyDown, openSkills);
			check(header(preview, "Skills").getMenu().getVisible(), "Down arrow opens multi-choice skills menu");
			header(preview, "Skills").getMenu().setVisible(false);
			expand(preview, true);
			Control schedules = header(preview, "Schedules");
			Control frequency = header(preview, "Frequency");
			Control parallel = header(preview, "Parallel");
			preview.getShell().layout(true, true);
			while (display.readAndDispatch()) {}
			if (args.length == 1) screenshot(preview.getShell(), args[0].replace(".png", "-advanced.png"));
			check(Math.abs(schedules.toDisplay(0, 0).y - frequency.toDisplay(0, 0).y) < 8
					&& Math.abs(frequency.toDisplay(0, 0).y - parallel.toDisplay(0, 0).y) < 8
					&& schedules.toDisplay(0, 0).x < frequency.toDisplay(0, 0).x
					&& frequency.toDisplay(0, 0).x < parallel.toDisplay(0, 0).x,
					"Schedules, Frequency and Parallel share one row in order: " + schedules.getBounds()
							+ " / " + frequency.getBounds() + " / " + parallel.getBounds());
			check(preview.getShell().getSize().x <= 720 && preview.getShell().getSize().y <= 760,
					"Expanded form remains compact");
			preview.click(IDialogConstants.CANCEL_ID);
			var grid = new Dialog(shell, SpeciesDraft.empty(), validator, true, context, SpeciesWizard.Kind.GRID);
			grid.create();
			check(header(grid, "Mirrors") == null, "Grid has no Mirrors control");
			check(((Text) header(grid, "Name")).getText().equals("my_grid"), "Grid creation name default");
			check(((Text) header(grid, "Width")).getText().equals("100")
					&& ((Text) header(grid, "Height")).getText().equals("100"), "Grid dimensions have defaults");
			check(java.util.Arrays.asList(((Combo) header(grid, "Neighbors")).getItems()).equals(List.of("4", "6", "8")),
					"Grid offers 4, 6 or 8 neighbors");
			check(!((Button) header(grid, "Use individual shapes")).getSelection()
					&& ((Button) header(grid, "Use neighbors cache")).getSelection()
					&& header(grid, "Use regular agents") == null, "Grid shapes initially false, cache true; regular-agents UI removed");
			check(header(grid, "Use individual shapes:expression") == null
					&& header(grid, "Use neighbors cache:expression") == null, "Grid flags are checkbox-only");
			expand(grid, true);
			((Text) header(grid, "Width")).setText("20");
			((Text) header(grid, "Height")).setText("30");
			choose((Combo) header(grid, "Neighbors"), "6");
			((Text) header(grid, "File")).setText("image_file(\"cells.png\")");
			((Text) header(grid, "Files")).setText("[image_file(\"bands.png\")]");
			for (String option : List.of("Use individual shapes", "Use neighbors cache")) {
				Button checkbox = (Button) header(grid, option);
				checkbox.setSelection(false);
				checkbox.notifyListeners(SWT.Selection, new Event());
			}
			grid.getShell().open();
			while (display.readAndDispatch()) {}
			checkHelp(grid.getShell());
			check(Math.abs(header(grid, "File").toDisplay(0, 0).y - header(grid, "Files").toDisplay(0, 0).y) < 8,
					"Grid File and Files share an Advanced row");
			check(Math.abs(header(grid, "Use individual shapes").toDisplay(0, 0).y
					- header(grid, "Use neighbors cache").toDisplay(0, 0).y) < 8,
					"Grid optimization checkboxes share an Advanced row");
			if (args.length == 1) screenshot(grid.getShell(), args[0].replace(".png", "-grid.png"));
			grid.click(IDialogConstants.OK_ID);
			check(grid.getResult() != null, "Grid accepted: " + grid.getErrorMessage());
			check(SpeciesWizardContext.facet(grid.getResult(), "width", "").equals("20")
					&& SpeciesWizardContext.facet(grid.getResult(), "height", "").equals("30")
					&& SpeciesWizardContext.facet(grid.getResult(), "neighbors", "").equals("6"), "Grid dimensions stored");
			check(SpeciesWizardContext.facet(grid.getResult(), "file", "").equals("image_file(\"cells.png\")")
					&& SpeciesWizardContext.facet(grid.getResult(), "files", "").equals("[image_file(\"bands.png\")]"),
					"Grid file expressions stored without evaluation");
			check(List.of("use_individual_shapes", "use_neighbors_cache").stream()
					.allMatch(key -> SpeciesWizardContext.facet(grid.getResult(), key, "").equals("false")),
					"Grid optimization options stored");
			var gridNoOp = new Dialog(shell, grid.getResult(), validator, true, context, SpeciesWizard.Kind.GRID);
			gridNoOp.create();
			gridNoOp.click(IDialogConstants.OK_ID);
			check(gridNoOp.getResult().equals(grid.getResult()), "Grid no-op retains every draft value");
			for (String current : List.of("", "true", "false", "shape_condition")) {
				var initial = new SpeciesDraft("loaded_grid", List.of(), current.isEmpty() ? List.of()
						: List.of(new SpeciesDraft.Facet("use_individual_shapes", current)), List.of(), "", List.of());
				var shapeDialog = new Dialog(shell, initial, validator, true, context, SpeciesWizard.Kind.GRID);
				shapeDialog.create();
				var shapeCheck = (Button) header(shapeDialog, "Use individual shapes");
				check(shapeCheck.getSelection() == "true".equals(current),
						"Grid shapes false initial policy respects explicit literal: " + current);
				check(shapeCheck.getToolTipText().contains("Wizard initial value: false")
						&& shapeCheck.getToolTipText().contains("Runtime default: true"), "Grid shapes help distinguishes defaults");
				shapeDialog.click(IDialogConstants.OK_ID);
				check(shapeDialog.getResult() != null && SpeciesWizardContext.facet(shapeDialog.getResult(),
						"use_individual_shapes", "").equals(current.isEmpty() ? "false" : current),
						"Accepted absent shapes becomes explicit false; loaded value retained: " + current);
				shapeDialog = new Dialog(shell, initial, validator, true, context, SpeciesWizard.Kind.GRID);
				shapeDialog.create();
				shapeCheck = (Button) header(shapeDialog, "Use individual shapes");
				shapeCheck.setSelection(false);
				shapeCheck.notifyListeners(SWT.Selection, new Event());
				shapeDialog.click(IDialogConstants.OK_ID);
				check(shapeDialog.getResult() != null && SpeciesWizardContext.facet(shapeDialog.getResult(),
						"use_individual_shapes", "").equals("false"), "Explicit false selection serialized: " + current);
				shapeDialog = new Dialog(shell, initial, validator, true, context, SpeciesWizard.Kind.GRID);
				shapeDialog.create();
				shapeCheck = (Button) header(shapeDialog, "Use individual shapes");
				shapeCheck.setSelection(true);
				shapeCheck.notifyListeners(SWT.Selection, new Event());
				shapeDialog.click(IDialogConstants.CANCEL_ID);
				check(shapeDialog.getResult() == null, "Grid shapes Cancel returns no draft: " + current);
			}
			var defaultGrid = new Dialog(shell, SpeciesDraft.empty(), validator, true, context, SpeciesWizard.Kind.GRID);
			defaultGrid.create();
			defaultGrid.click(IDialogConstants.OK_ID);
			check(defaultGrid.getResult() != null && SpeciesWizardContext.facet(defaultGrid.getResult(),
					"use_individual_shapes", "").equals("false"), "New grid saves false without a checkbox edit");
			var expressionGrid = new SpeciesDraft("expression_grid", List.of(),
					List.of(new SpeciesDraft.Facet("use_individual_shapes", "shape_condition"),
							new SpeciesDraft.Facet("use_neighbors_cache", "cache_condition"),
							new SpeciesDraft.Facet("use_regular_agents", "false")), List.of(), "", List.of());
			var expressionGridDialog = new Dialog(shell, expressionGrid, validator, true, context, SpeciesWizard.Kind.GRID);
			expressionGridDialog.create();
			check(header(expressionGridDialog, "Use individual shapes") instanceof Button
					&& header(expressionGridDialog, "Use neighbors cache") instanceof Button
					&& header(expressionGridDialog, "Use regular agents") == null, "Loaded grid expressions have checkbox-only controls");
			expressionGridDialog.click(IDialogConstants.OK_ID);
			check(expressionGrid.equals(expressionGridDialog.getResult()), "Grid no-op preserves nonliteral flags and hidden regular agents");
			expressionGridDialog = new Dialog(shell, expressionGrid, validator, true, context, SpeciesWizard.Kind.GRID);
			expressionGridDialog.create();
			for (String key : List.of("Use individual shapes", "Use neighbors cache")) {
				var checkBox = (Button) header(expressionGridDialog, key);
				checkBox.setSelection(false); checkBox.notifyListeners(SWT.Selection, new Event());
				checkBox.setSelection(true); checkBox.notifyListeners(SWT.Selection, new Event());
			}
			expressionGridDialog.click(IDialogConstants.OK_ID);
			check(expressionGridDialog.getResult() != null
					&& SpeciesWizardContext.facet(expressionGridDialog.getResult(), "use_individual_shapes", "").isEmpty()
					&& SpeciesWizardContext.facet(expressionGridDialog.getResult(), "use_neighbors_cache", "").isEmpty()
					&& SpeciesWizardContext.facet(expressionGridDialog.getResult(), "use_regular_agents", "").equals("false"),
					"Edited true grid defaults omitted without deleting hidden regular-agents facet");
			var fileGridDraft = new SpeciesDraft("file_grid", List.of(),
					List.of(new SpeciesDraft.Facet("file", "raster_file(\"map.asc\")")), List.of(), "write 1;", List.of("tick"));
			var fileGrid = new Dialog(shell, fileGridDraft, validator, true, context, SpeciesWizard.Kind.GRID);
			fileGrid.create();
			fileGrid.click(IDialogConstants.OK_ID);
			check(new SpeciesDraft(fileGridDraft.name(), fileGridDraft.skills(),
					List.of(fileGridDraft.facets().getFirst(), new SpeciesDraft.Facet("use_individual_shapes", "false")),
					fileGridDraft.variables(), fileGridDraft.init(), fileGridDraft.reflexOrder()).equals(fileGrid.getResult()),
					"File-defined grid preserves absent dimensions and hidden bodies while explicitly saving false shapes");
			for (SpeciesWizard.Kind kind : List.of(SpeciesWizard.Kind.SPECIES, SpeciesWizard.Kind.GRID, SpeciesWizard.Kind.MODEL)) {
				var sorting = new Dialog(shell, unchanged, validator, true, context, kind);
				sorting.create();
				Table list = attributes(sorting);
				select(list, "energy");
				list.getColumn(0).notifyListeners(SWT.Selection, new Event());
				check(list.getItem(0).getText(0).equals("energy") && list.getSortDirection() == SWT.UP,
						kind + " Name header sorts ascending");
				list.getColumn(0).notifyListeners(SWT.Selection, new Event());
				check(list.getItem(0).getText(0).equals("location") && list.getSortDirection() == SWT.DOWN,
						kind + " Name header reverses sort");
				check(list.getSelection()[0].getText(0).equals("energy"), kind + " Sorting preserves selection");
				list.getColumn(2).notifyListeners(SWT.Selection, new Event());
				check(list.getSortColumn() == list.getColumn(2) && list.getSortDirection() == SWT.UP,
						kind + " Source header sorts ascending");
				for (int row = 1; row < list.getItemCount(); row++)
					check(list.getItem(row - 1).getText(2).compareToIgnoreCase(list.getItem(row).getText(2)) <= 0,
							kind + " Source rows ordered");
				list.getColumn(2).notifyListeners(SWT.Selection, new Event());
				check(list.getSortDirection() == SWT.DOWN, kind + " Source header reverses sort");
				((Text) field(sorting.getShell(), "species.attribute.field", 2)).setText("99");
				button(sorting.getShell(), "Add attribute").notifyListeners(SWT.Selection, new Event());
				check(list.getSelection()[0].getText(0).equals("my_attribute"),
						kind + " Add selects new attribute under sorting");
				button(sorting.getShell(), "Remove selected attribute").notifyListeners(SWT.Selection, new Event());
				sorting.click(IDialogConstants.OK_ID);
				check(sorting.getResult().variables().size() == 1
						&& sorting.getResult().variables().getFirst().name().equals("energy")
						&& sorting.getResult().variables().getFirst().init().equals("99"),
						kind + " Sorted edits stay attached to original declaration");
			}
			var invalidGrid = new Dialog(shell, SpeciesDraft.empty(), validator, true, context, SpeciesWizard.Kind.GRID);
			invalidGrid.create();
			((Text) header(invalidGrid, "Width")).setText("0");
			check(!invalidGrid.acceptsInput(), "Nonpositive grid size disables OK");
			invalidGrid.click(IDialogConstants.CANCEL_ID);
			check(invalidGrid.getResult() == null, "Grid Cancel has no result");
			checkModel(shell, context);
			System.out.println("Species wizard SWT regression: " + checks + " checks passed");
		} finally {
			shell.dispose();
			display.dispose();
		}
	}
}
