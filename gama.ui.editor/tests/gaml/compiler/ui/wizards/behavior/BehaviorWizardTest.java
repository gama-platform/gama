package gaml.compiler.ui.wizards.behavior;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import gaml.compiler.ui.wizards.action.ActionBodyContext;
import gaml.compiler.ui.wizards.action.ActionVerifier;
import gaml.compiler.ui.wizards.species.SpeciesDraft.Facet;

/** Native SWT regression of the behaviour dialog; the compiler is substituted, the code area is plain text. */
public final class BehaviorWizardTest {
	private static int checks;

	private static class Dialog extends BehaviorWizard {
		final List<String> compiled = new ArrayList<>();
		List<ActionVerifier.Problem> problems = List.of();
		Function<String, List<ActionVerifier.Problem>> byCode;

		Dialog(final Shell shell, final BehaviorDraft draft) {
			super(shell, draft, x -> null, null);
		}

		Dialog(final Shell shell, final BehaviorDraft draft, final EnumSet<BehaviorKind> kinds,
				final Function<BehaviorDraft, String> validator) {
			super(shell, draft, kinds, validator, d -> new ActionBodyContext(
					org.eclipse.emf.common.util.URI.createURI("synthetic:/t.behavior-wizard.gaml"), "prefix{", "}"));
		}

		@Override
		protected List<ActionVerifier.Problem> compile(final ActionBodyContext context, final String code) {
			compiled.add(code);
			return byCode == null ? problems : byCode.apply(code);
		}

		boolean ok() { return getButton(IDialogConstants.OK_ID).getEnabled(); }

		void cancel() { getButton(IDialogConstants.CANCEL_ID).notifyListeners(SWT.Selection, new Event()); }

		String error() { return getErrorMessage(); }

		String signature() { return ((Label) field(this, "Signature")).getText(); }

		boolean closed() { return field(this, "Closing").getVisible(); }
	}

	private static void check(final boolean condition, final String message) {
		checks++;
		if (!condition) throw new AssertionError(message);
		System.out.println("ok " + checks + " " + message);
	}

	private static Control find(final Control root, final Object value) {
		if (value.equals(root.getData(BehaviorWizard.FIELD))) return root;
		if (root instanceof Composite c) for (Control child : c.getChildren()) {
			Control found = find(child, value);
			if (found != null) return found;
		}
		return null;
	}

	private static Control field(final Dialog d, final String label) { return find(d.getShell(), label); }

	private static void toggle(final Dialog d, final String facet, final boolean on) {
		Button b = (Button) field(d, "facet:" + facet);
		b.setSelection(on);
		b.notifyListeners(SWT.Selection, new Event());
	}

	private static void select(final Dialog d, final BehaviorKind kind) {
		Combo combo = (Combo) field(d, "Kind");
		combo.select(List.of(combo.getItems()).indexOf(kind.keyword()));
		combo.notifyListeners(SWT.Selection, new Event());
	}

	public static void main(final String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		try {
			AtomicInteger validations = new AtomicInteger();
			// ---- reflex with an expression-valued boolean facet and an undeclared facet
			BehaviorDraft reflex = new BehaviorDraft(BehaviorKind.REFLEX, "move",
					List.of(new Facet("when", "energy > 0"), new Facet("custom", "3")), "\n\tdo wander;\n");
			Dialog d = new Dialog(shell, reflex, EnumSet.of(BehaviorKind.REFLEX), x -> {
				validations.incrementAndGet();
				return null;
			});
			d.create();
			check(((Text) field(d, "Name")).getText().equals("move"), "Name loaded");
			check(!((Combo) field(d, "Kind")).getEnabled(), "Kind fixed when editing");
			check(field(d, "facet:when") instanceof Text t && t.getText().equals("energy > 0"),
					"Expression-valued boolean facet shown as expression, not a check box");
			check(field(d, "facet:custom") instanceof Text t && t.getText().equals("3"), "Undeclared facet kept");
			StyledText code = (StyledText) field(d, "Code");
			check(code.getText().equals(reflex.body()) && code.getEditable(), "Code box holds only the body");
			check(d.signature().equals("reflex move when: energy > 0 custom: 3 {") && d.closed(),
					"Live signature with brace above, closing brace below");
			check(field(d, "Signature").toDisplay(0, 0).y < code.toDisplay(0, 0).y
					&& field(d, "Closing").toDisplay(0, 0).y > code.toDisplay(0, 0).y, "Signature above, brace below");
			check(d.readDraft() == reflex, "Unchanged form returns the initial instance");
			check(d.accept() && d.getResult() == reflex && d.compiled.isEmpty() && validations.get() == 1,
					"Unchanged OK: no recompilation, adapter validator run, same instance");

			((Text) field(d, "Name")).setText("walk");
			check(d.signature().startsWith("reflex walk when"), "Signature follows the name");
			((Text) field(d, "Name")).setText("");
			check(d.ok() && d.signature().equals("reflex when: energy > 0 custom: 3 {"), "Anonymous reflex allowed");
			((Text) field(d, "Name")).setText("2x");
			check(!d.ok() && d.error() != null, "Invalid name blocks OK");
			((Text) field(d, "Name")).setText("move");
			((Text) field(d, "facet:custom")).setText("");
			check(d.readDraft().facet("custom") == null && d.signature().equals("reflex move when: energy > 0 {"),
					"Clearing an undeclared facet removes it");
			((Text) field(d, "facet:custom")).setText("3");
			check(d.readDraft() == reflex, "Restoring it restores the identical draft (original position)");
			code.setText("\n\tdo die;\n");
			d.problems = List.of(new ActionVerifier.Problem("unknown die", true, 2));
			check(!d.accept() && d.error().equals("Code line 2: unknown die") && d.getResult() == null,
					"In-code compilation error reported and blocks OK");
			d.problems = List.of();
			check(d.accept() && d.getResult().body().equals("\n\tdo die;\n") && d.compiled.contains("\n\tdo die;\n"),
					"Changed code compiled on OK");
			d.close();

			// ---- baseline: pre-existing errors outside the code are not attributed to the edit
			d = new Dialog(shell, reflex, EnumSet.of(BehaviorKind.REFLEX), x -> null);
			d.byCode = c -> c.equals(reflex.body()) ? List.of(new ActionVerifier.Problem("old error", false, 0))
					: List.of(new ActionVerifier.Problem("old error", false, 0), new ActionVerifier.Problem("new error", false, 0));
			d.create();
			((Text) field(d, "facet:when")).setText("true");
			check(d.accept(), "Pre-existing out-of-code error ignored");
			code = (StyledText) field(d, "Code");
			code.setText("x");
			check(!d.accept() && d.error().equals("new error"), "New out-of-code error reported");
			d.close();

			// ---- state: literal booleans as check boxes, explicit false preserved
			BehaviorDraft state = new BehaviorDraft(BehaviorKind.STATE, "idle",
					List.of(new Facet("initial", "true"), new Facet("final", "false")), "");
			d = new Dialog(shell, state, EnumSet.of(BehaviorKind.STATE), x -> null);
			d.create();
			check(field(d, "facet:initial") instanceof Button b && b.getSelection(), "initial: true as checked box");
			check(field(d, "facet:final") instanceof Button b && !b.getSelection(), "final: false as unchecked box");
			toggle(d, "initial", false);
			check(d.readDraft().facet("initial") == null && d.signature().equals("state idle final: false {"),
					"Unchecking removes an implicit facet");
			toggle(d, "initial", true);
			check(d.readDraft() == state, "Rechecking restores the identical draft");
			toggle(d, "final", true);
			check(d.readDraft().facet("final").equals("true"), "Checking sets true");
			toggle(d, "final", false);
			check(d.readDraft().facet("final") == null, "Edited default false is omitted");
			((Text) field(d, "Name")).setText("");
			check(!d.ok() && d.error().contains("name is required"), "State name required");
			d.close();

			// ---- generic creation stays lifecycle-only, even if a caller requests every kind
			BehaviorDraft empty = BehaviorDraft.empty(BehaviorKind.REFLEX);
			d = new Dialog(shell, empty, EnumSet.allOf(BehaviorKind.class), x -> null);
			d.create();
			Combo kind = (Combo) field(d, "Kind");
			check(kind.getEnabled() && List.of(kind.getItems()).equals(List.of("reflex", "init", "abort")),
					"Generic creation offers only reflex lifecycle kinds");
			check(d.getShell().getText().equals("Reflex Editor"), "Lifecycle dialog title");
			check(field(d, "facet:when") instanceof Text, "Reflex 'when' always inline expression");
			Text when = (Text) field(d, "facet:when");
			check(when.getText().equals("true") && d.readDraft() == empty, "Absent condition displays true without a facet");
			check(when.getParent() == kind.getParent() && when.getParent() == field(d, "Name").getParent()
					&& ((org.eclipse.swt.layout.GridLayout) when.getParent().getLayout()).numColumns == 6,
					"Kind, Name and When share one six-column row");
			check(field(d, "Name").getBounds().x < kind.getBounds().x
					&& kind.getBounds().x < when.getBounds().x
					&& kind.getBounds().y + kind.getBounds().height / 2 == when.getBounds().y + when.getBounds().height / 2,
					"Header controls ordered Name, Kind, When and vertically aligned");
			check(find(d.getShell(), "facet:when:expression") == null
					&& when.getParent().getChildren().length == 6, "No When checkbox, defined control or expression button");
			when.setText("false");
			select(d, BehaviorKind.INIT);
			check(((Text) field(d, "facet:when")).getText().equals("false"), "Init retains edited guard");
			select(d, BehaviorKind.ABORT);
			check(((Text) field(d, "facet:when")).getText().equals("false")
					&& "false".equals(d.readDraft().facet("when")), "Lifecycle switch preserves explicit guard override");
			when.setText("true");
			check(d.readDraft().facet("when") == null, "Abort true default omitted");
			when.setText("energy > 2");
			check(d.accept() && d.getResult().facet("when").equals("energy > 2"), "Inline condition edit validated on accept");
			d.close();

			for (String condition : List.of("true", "false", "energy > 0")) {
				BehaviorDraft explicit = empty.withFacet("when", condition);
				d = new Dialog(shell, explicit);
				d.create();
				when = (Text) field(d, "facet:when");
				check(when.getText().equals(condition) && d.accept() && d.getResult() == explicit,
						"Untouched explicit condition preserved: " + condition);
				when.setText("changed_condition");
				when.setText(condition);
				check("true".equals(condition) ? d.readDraft().facet("when") == null : d.readDraft() == explicit,
						"Edited default omitted; restored nondefault condition is no-op: " + condition);
				when.setText("");
				check(d.readDraft().facet("when") == null, "Clearing condition omits optional facet");
				d.cancel();
				check(d.getResult() == null, "Condition edits discarded on Cancel");
			}

			// ---- diagram-specific editors never offer a cross-kind conversion
			for (BehaviorKind fixed : BehaviorKind.values()) {
				if (BehaviorKind.LIFECYCLE.contains(fixed)) continue;
				BehaviorDraft initial = BehaviorDraft.empty(fixed).withFacet("custom", "42");
				if (fixed == BehaviorKind.TASK) initial = initial.withFacet("weight", "2.0");
				if (fixed == BehaviorKind.PERCEIVE) initial = initial.withFacet("target", "wolf");
				d = new Dialog(shell, initial, EnumSet.allOf(BehaviorKind.class), x -> null);
				d.create();
				kind = (Combo) field(d, "Kind");
				String title = Character.toUpperCase(fixed.keyword().charAt(0)) + fixed.keyword().substring(1) + " Editor";
				check(d.getShell().getText().equals(title), "Dedicated title: " + title);
				check(!kind.getEnabled() && List.of(kind.getItems()).equals(List.of(fixed.keyword())),
						"Kind locked for " + fixed);
				check(d.accept() && d.getResult() == initial && d.compiled.isEmpty(),
						"Dedicated editor no-op preserves draft for " + fixed);
				code = (StyledText) field(d, "Code");
				check(code.getEditable() == fixed.hasBody() && d.closed() == fixed.hasBody(),
						"Dedicated code semantics for " + fixed);
				if (fixed == BehaviorKind.PLAN) {
					check(field(d, "facet:finished_when") instanceof Text,
							"Completion absence semantics remain an expression");
					check(field(d, "facet:instantaneous") instanceof Button b && !b.getSelection(),
							"Instantaneous defaults false");
				}
				if (fixed == BehaviorKind.TASK || fixed == BehaviorKind.PERCEIVE) {
					String required = fixed == BehaviorKind.TASK ? "weight" : "target";
					Text expression = (Text) field(d, "facet:" + required);
					String value = expression.getText();
					expression.setText("");
					check(!d.ok() && !d.accept(), "Required facet validation for " + fixed);
					expression.setText(value);
				}
				((Text) field(d, "facet:custom")).setText("43");
				check(d.accept() && d.getResult().equals(initial.withFacet("custom", "43")) && !d.compiled.isEmpty(),
						"Dedicated editor validates and preserves other data for " + fixed);
				d.cancel();
				check(d.getResult() == null, "Dedicated editor Cancel returns no result for " + fixed);
			}

			// ---- lifecycle conversion (reflex / init / abort): everything kept, unchanged round trip is a no-op
			d = new Dialog(shell, reflex, EnumSet.copyOf(reflex.kind().convertibleKinds()), x -> null);
			d.create();
			kind = (Combo) field(d, "Kind");
			check(kind.getEnabled() && List.of(kind.getItems()).equals(List.of("reflex", "init", "abort")),
					"Lifecycle kinds offered when editing a reflex: " + List.of(kind.getItems()));
			select(d, BehaviorKind.INIT);
			check(d.ok() && d.signature().startsWith("init move when: ") && d.closed(), "Init signature: " + d.signature());
			select(d, BehaviorKind.ABORT);
			check(d.signature().startsWith("abort move when: "), "Abort signature: " + d.signature());
			((Text) field(d, "Name")).setText("");
			check(d.ok() && d.signature().startsWith("abort when: "), "Anonymous abort allowed");
			check(d.accept() && d.getResult().equals(new BehaviorDraft(BehaviorKind.ABORT, "", reflex.facets(), reflex.body())),
					"Abort keeps facets and code: " + d.getResult());
			d.close();
			d = new Dialog(shell, reflex, EnumSet.copyOf(BehaviorKind.LIFECYCLE), x -> null);
			d.create();
			select(d, BehaviorKind.ABORT);
			select(d, BehaviorKind.REFLEX);
			check(d.accept() && d.getResult() == reflex, "Switching back is a no-op");
			d.close();

			for (BehaviorKind lifecycle : BehaviorKind.LIFECYCLE) {
				BehaviorDraft initial = BehaviorDraft.empty(lifecycle);
				d = new Dialog(shell, initial);
				d.create();
				kind = (Combo) field(d, "Kind");
				check(kind.getEnabled() && List.of(kind.getItems()).equals(List.of("reflex", "init", "abort")),
						"Default constructor allows only lifecycle conversion for " + lifecycle);
				check(d.getShell().getText().equals("Reflex Editor"), "Shared lifecycle title for " + lifecycle);
				check(d.accept() && d.getResult() == initial, "Default constructor lifecycle no-op for " + lifecycle);
				d.close();
			}

			// ---- cancel
			d = new Dialog(shell, reflex, EnumSet.of(BehaviorKind.REFLEX), x -> null);
			d.setBlockOnOpen(false);
			d.open();
			((Text) field(d, "Name")).setText("changed");
			d.cancel();
			check(d.getResult() == null, "Cancel returns no result");

			// ---- adapter validator errors block OK
			d = new Dialog(shell, reflex, EnumSet.of(BehaviorKind.REFLEX), x -> "stale");
			d.create();
			check(!d.accept() && "stale".equals(d.error()), "Adapter error (e.g. stale document) blocks unchanged OK");
			d.close();
			check(BehaviorWizard.standalone(state).text("").contains("state idle initial: true final: false {"),
					"Standalone context");
			System.out.println("BehaviorWizardTest: " + checks + " checks passed");
		} finally {
			display.dispose();
		}
	}
}
