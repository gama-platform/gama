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
			check(d.readDraft() == state, "Unchecking keeps the explicit false");
			((Text) field(d, "Name")).setText("");
			check(!d.ok() && d.error().contains("name is required"), "State name required");
			d.close();

			// ---- creation: kind selector, facets reorganised, required facets, codeless rule
			BehaviorDraft empty = BehaviorDraft.empty(BehaviorKind.REFLEX);
			d = new Dialog(shell, empty, EnumSet.allOf(BehaviorKind.class), x -> null);
			d.create();
			Combo kind = (Combo) field(d, "Kind");
			check(kind.getEnabled() && kind.getItemCount() == 6, "Six kinds offered on creation");
			check(field(d, "facet:when") instanceof Button, "Reflex 'when' as check box when absent");
			toggle(d, "when", true);
			select(d, BehaviorKind.TASK);
			check(field(d, "facet:weight") instanceof Text && !d.ok() && d.error().contains("weight"),
					"Task requires weight");
			check(field(d, "facet:when") instanceof Text t && t.getText().equals("true"),
					"Facet of the previous kind kept as other facet");
			((Text) field(d, "facet:weight")).setText("2.0");
			check(d.ok() && d.signature().equals("task my_reflex when: true weight: 2.0 {"), "Task signature");
			((Text) field(d, "facet:when")).setText("");
			select(d, BehaviorKind.RULE);
			code = (StyledText) field(d, "Code");
			check(!code.getEditable() && !d.closed() && d.signature().endsWith(";"), "Rule has no code nor braces");
			check(d.signature().equals("rule my_reflex weight: 2.0;"), "Rule signature: " + d.signature());
			select(d, BehaviorKind.PERCEIVE);
			check(field(d, "facet:target") instanceof Text && !d.ok(), "Perceive requires target");
			((Text) field(d, "facet:target")).setText("wolf");
			check(d.ok() && code.getEditable() && d.closed(), "Perceive has code");
			check(d.accept() && d.getResult().kind() == BehaviorKind.PERCEIVE, "Creation accepted");
			d.close();

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
