package gaml.compiler.ui.wizards.species;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import gaml.compiler.ui.wizards.action.ActionBodyContext;
import gaml.compiler.ui.wizards.action.ActionDraft;
import gaml.compiler.ui.wizards.action.ActionVerifier;
import gaml.compiler.ui.wizards.action.ActionWizard;
import gaml.compiler.ui.wizards.behavior.BehaviorDraft;
import gaml.compiler.ui.wizards.behavior.BehaviorKind;
import gaml.compiler.ui.wizards.behavior.BehaviorWizard;
import gaml.compiler.ui.wizards.display.DisplayDraft;
import gaml.compiler.ui.wizards.display.DisplayWizard;
import gaml.compiler.ui.wizards.experiment.ExperimentDraft;
import gaml.compiler.ui.wizards.experiment.ExperimentWizard;

/** Cross-family native contract: arrangement, action order, explicit mode and detached validation/acceptance. */
public final class DeclarationWizardTest {
	private static int checks;

	private static void check(final boolean value, final String message) {
		checks++;
		if (!value) throw new AssertionError(message);
	}

	private static List<Button> buttons(final Control root) {
		List<Button> result = new ArrayList<>();
		if (root instanceof Button button) result.add(button);
		if (root instanceof Composite composite)
			for (Control child : composite.getChildren()) result.addAll(buttons(child));
		return result;
	}

	private static Control field(final Control root, final String property, final String key) {
		if (key.equals(root.getData(property))) return root;
		if (root instanceof Composite composite) for (Control child : composite.getChildren()) {
			Control found = field(child, property, key);
			if (found != null) return found;
		}
		return null;
	}

	private static DeclarationWizard<?> wizard(final Shell shell, final String family, final AtomicInteger calls,
			final AtomicBoolean stale) {
		Function<Object, String> validate = d -> {
			calls.incrementAndGet();
			return stale.get() ? "The document changed." : null;
		};
		return switch (family) {
			case "Action" -> new ActionWizard(shell, new ActionDraft("a", "action", false, List.of(), ""),
					validate::apply, List.of("int"), null) {
				@Override protected List<ActionVerifier.Problem> compile(final ActionBodyContext context, final String code) {
					return List.of();
				}
			};
			case "Reflex" -> new BehaviorWizard(shell, BehaviorDraft.empty(BehaviorKind.REFLEX), validate::apply, null) {
				@Override protected List<ActionVerifier.Problem> compile(final ActionBodyContext context, final String code) {
					return List.of();
				}
			};
			case "Experiment" -> new ExperimentWizard(shell, ExperimentDraft.empty("gui"), validate::apply,
					List.of("int"), List::of, null, () -> List.of("base")) {
				@Override protected List<String> possibleControlNames() { return List.of("reflex", "fsm"); }
			};
			case "Display" -> new DisplayWizard(shell, DisplayDraft.empty(), validate::apply, List::of, List::of, null) {
				@Override protected List<ActionVerifier.Problem> compile(final ActionBodyContext context) { return List.of(); }
			};
			default -> new SpeciesWizard(shell,
					new SpeciesDraft("declaration", List.of(), List.of(), List.of(), "", List.of()),
					validate::apply, true, List.of("agent", "object"), d -> List.of(), SpeciesWizard.Kind.valueOf(family)) {
				@Override protected List<String> possibleControlNames() { return List.of("reflex", "fsm"); }
				@Override protected List<String> possibleTypeNames() { return List.of("int"); }
				@Override protected List<String> possibleSkillNames() { return List.of(); }
			};
		};
	}

	public static void main(final String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		try {
			for (String family : List.of("SPECIES", "GRID", "CLASS", "MODEL", "Action", "Reflex", "Experiment", "Display")) {
				for (boolean create : List.of(false, true)) {
					var calls = new AtomicInteger();
					var stale = new AtomicBoolean();
					DeclarationWizard<?> wizard = wizard(shell, family, calls, stale);
					check(!wizard.isCreationMode(), "Existing callers default to edit mode: " + family);
					wizard.setCreationMode(create);
					wizard.create();
					boolean modeLocked = false;
					try {
						wizard.setCreationMode(!create);
					} catch (IllegalStateException expected) {
						modeLocked = true;
					}
					check(modeLocked && wizard.isCreationMode() == create, "Mode is fixed after creation: " + family);
					check(wizard.isCreationMode() == create, "Explicit mode: " + family);
					String primaryText = create ? "Create" : "Apply";
					Button primary = buttons(wizard.getShell()).stream().filter(b -> b.getText().equals(primaryText)).findFirst().orElseThrow();
					List<Button> footer = List.of(((Composite) primary.getParent()).getChildren()).stream()
							.filter(Button.class::isInstance).map(Button.class::cast).toList();
					check(footer.stream().map(Button::getText).toList().equals(List.of("Validate", "Cancel", primaryText)),
							"Same footer order: " + family + create);
					check(wizard.getShell().getDefaultButton() == primary && primary.getEnabled(),
							"Create/Apply is enabled default action: " + family);
					check(calls.get() == 0 && wizard.getResult() == null, "Opening never validates or accepts: " + family);
					footer.getFirst().notifyListeners(SWT.Selection, new Event());
					check(calls.get() == 1 && wizard.getResult() == null && wizard.getErrorMessage() == null,
							"Validate checks without accepting: " + family);
					stale.set(true);
					footer.getFirst().notifyListeners(SWT.Selection, new Event());
					check(calls.get() == 2 && wizard.getErrorMessage().equals("The document changed.")
							&& wizard.getResult() == null, "Same validation error feedback: " + family);
					primary.notifyListeners(SWT.Selection, new Event());
					check(!wizard.getShell().isDisposed() && wizard.getResult() == null, "Failed acceptance stays open: " + family);
					stale.set(false);
					check(wizard.accept() && wizard.getResult() != null, "Acceptance returns detached draft: " + family);
					stale.set(true);
					check(!wizard.accept() && wizard.getResult() == null, "Failed retry clears previous result: " + family);
					stale.set(false);
					check(wizard.accept(), "Fresh acceptance after failed retry: " + family);
					footer.get(1).notifyListeners(SWT.Selection, new Event());
					check(wizard.getResult() == null, "Cancel clears accepted draft: " + family);

					wizard = wizard(shell, family, new AtomicInteger(), new AtomicBoolean());
					wizard.setCreationMode(create);
					wizard.create();
					primary = buttons(wizard.getShell()).stream().filter(b -> b.getText().equals(primaryText)).findFirst().orElseThrow();
					primary.notifyListeners(SWT.Selection, new Event());
					check(wizard.getShell() == null && wizard.getResult() != null
							&& wizard.getReturnCode() == IDialogConstants.OK_ID, "Primary retains OK result ID: " + family);
				}
			}

			for (String family : List.of("SPECIES", "GRID", "CLASS", "MODEL", "Action", "Reflex", "Experiment", "Display")) {
				var wizard = wizard(shell, family, new AtomicInteger(), new AtomicBoolean());
				wizard.create();
				Composite identity = (Composite) field(wizard.getShell(), "wizard.section", "Identity");
				check(identity != null && identity.getChildren()[1] instanceof Text,
						"Name is the first field in every identity row: " + family);
				if (List.of("SPECIES", "GRID", "CLASS", "Experiment").contains(family)) {
					String property = family.equals("Experiment") ? "experiment.field" : "species.field";
					Control parent = field(wizard.getShell(), property, family.equals("Experiment") ? "facet:parent" : "Parent");
					Control virtual = field(wizard.getShell(), property, family.equals("Experiment") ? "facet:virtual" : "Virtual");
					check(parent.getParent() == identity && virtual.getParent() == identity,
							"Name, Parent and Virtual share the identity row: " + family);
					Control name = identity.getChildren()[1];
					check(name.getBounds().x < parent.getBounds().x && parent.getBounds().x < virtual.getBounds().x
							&& Math.abs(name.toDisplay(0, 0).y - parent.toDisplay(0, 0).y) < 12
							&& Math.abs(name.toDisplay(0, 0).y - virtual.toDisplay(0, 0).y) < 12,
							"Name, Parent and Virtual order/alignment: " + family);
				}
				if (List.of("SPECIES", "GRID", "MODEL").contains(family)) {
					Control skills = field(wizard.getShell(), "species.field", "Skills");
					Control control = field(wizard.getShell(), "species.field", "Control");
					check(skills.getParent() == control.getParent()
							&& skills.getParent().getData("wizard.section").equals("Capabilities"),
							"Skills and Control share the capabilities row: " + family);
					check(skills.getBounds().x < control.getBounds().x
							&& Math.abs(skills.toDisplay(0, 0).y - control.toDisplay(0, 0).y) < 12,
							"Skills and Control order/alignment: " + family);
				}
				((Text) identity.getChildren()[1]).setText("");
				Button apply = buttons(wizard.getShell()).stream().filter(b -> b.getText().equals("Apply")).findFirst().orElseThrow();
				boolean nameRequired = !family.equals("Reflex");
				check(apply.getEnabled() != nameRequired, "Local validity controls acceptance: " + family);
				Button validate = buttons(wizard.getShell()).stream().filter(b -> b.getText().equals("Validate")).findFirst().orElseThrow();
				check(validate.getEnabled() == apply.getEnabled(), "Validate and primary share validity state: " + family);
				wizard.close();
			}
			System.out.println("DeclarationWizardTest: " + checks + " checks passed");
		} finally {
			shell.dispose();
			display.dispose();
		}
	}
}
