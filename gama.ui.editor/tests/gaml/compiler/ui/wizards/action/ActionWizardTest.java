package gaml.compiler.ui.wizards.action;

import java.util.ArrayList;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

/** Native SWT regression of the action dialog; the compiler is substituted, the code area is plain text. */
public final class ActionWizardTest {
	private static int checks;

	private static class Dialog extends ActionWizard {
		final List<String> compiled = new ArrayList<>();
		List<ActionVerifier.Problem> problems = List.of();

		Dialog(final Shell shell, final ActionDraft draft, final Function<ActionDraft, String> validator) {
			super(shell, draft, validator, List.of("int", "float", "string", "list<int>"),
					d -> new ActionBodyContext(org.eclipse.emf.common.util.URI.createURI("synthetic:/t.action-wizard.gaml"), "prefix{", "}"));
		}

		@Override
		protected List<ActionVerifier.Problem> compile(final ActionBodyContext context, final String body) {
			compiled.add(body);
			return problems;
		}

		void click(final int id) { getButton(id).notifyListeners(SWT.Selection, new Event()); }

		boolean ok() { return getButton(IDialogConstants.OK_ID).getEnabled(); }

		String error() { return getErrorMessage(); }

		String signature() { return ((org.eclipse.swt.widgets.Label) field(this, "Signature")).getText(); }

		boolean closed() {
			Control c = field(this, "Closing");
			return c.getVisible() && "}".equals(((org.eclipse.swt.widgets.Label) c).getText());
		}
	}

	private static void check(final boolean condition, final String message) {
		checks++;
		if (!condition) throw new AssertionError(message);
		System.out.println("ok " + checks + " " + message);
	}

	private static Control find(final Control root, final String key, final Object value) {
		if (value.equals(root.getData(key))) return root;
		if (root instanceof Composite c) for (Control child : c.getChildren()) {
			Control found = find(child, key, value);
			if (found != null) return found;
		}
		return null;
	}

	private static Control field(final Dialog d, final String label) { return find(d.getShell(), "action.field", label); }

	private static Button button(final Control root, final String text) {
		if (root instanceof Button b && text.equals(b.getText())) return b;
		if (root instanceof Composite c) for (Control child : c.getChildren()) {
			Button found = button(child, text);
			if (found != null) return found;
		}
		return null;
	}

	private static void press(final Dialog d, final String text) {
		button(d.getShell(), text).notifyListeners(SWT.Selection, new Event());
	}

	private static Table table(final Dialog d) { return (Table) find(d.getShell(), "action.arguments", true); }

	private static ActionDraft sample() {
		return new ActionDraft("grow", "float", false, List.of(new ActionDraft.Argument("n", "int", "1"),
				new ActionDraft.Argument("rate", "float", "")), "\n\treturn n * rate;\n");
	}

	public static void main(final String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		try {
			AtomicInteger validations = new AtomicInteger();
			ActionDraft initial = sample();
			Dialog d = new Dialog(shell, initial, x -> { validations.incrementAndGet(); return null; });
			d.create();
			check(((Text) field(d, "Name")).getText().equals("grow"), "Name loaded");
			Combo returns = (Combo) field(d, "Returns");
			check(returns.getItem(0).equals("action") && returns.getText().equals("float"),
					"Return choices start with action; current type selected");
			check(table(d).getItemCount() == 2 && table(d).getItem(0).getText(2).equals("1"),
					"Arguments table shows name/type/default");
			StyledText code = (StyledText) field(d, "Code");
			check(code.getText().equals(initial.body()) && code.getEditable(), "Code editable for non-virtual");
			check(d.readDraft() == initial, "Unchanged form returns the initial instance");
			check(d.signature().equals("float grow(int n <- 1, float rate) {") && d.closed(),
					"Signature with opening brace shown above code, closing brace below");
			check(!code.getText().contains("grow(") && !code.getText().contains("{") && !code.getText().contains("}"),
					"Code box holds only the body");
			Control sig = field(d, "Signature");
			check(sig.toDisplay(0, 0).y < code.toDisplay(0, 0).y
					&& field(d, "Closing").toDisplay(0, 0).y > code.toDisplay(0, 0).y, "Signature above, brace below code");
			check(d.accept() && d.getResult() == initial && d.compiled.isEmpty() && validations.get() == 1,
					"Unchanged OK: no recompilation, adapter validator run, same instance");

			Button virtual = (Button) field(d, "Virtual");
			check((virtual.getStyle() & SWT.CHECK) != 0 && field(d, "Virtual:expression") == null,
					"Action Virtual is checkbox-only");
			virtual.setSelection(true);
			virtual.notifyListeners(SWT.Selection, new Event());
			check(!code.getEditable() && code.getText().equals(initial.body()), "Virtual disables code but keeps text");
			check(d.signature().equals("float grow(int n <- 1, float rate) virtual: true;") && !d.closed(),
					"Virtual signature is bodyless, without braces");
			check(d.readDraft().virtual() && d.readDraft().body().equals(initial.body()), "Draft keeps inactive code");
			d.compiled.clear();
			check(d.accept() && d.compiled.isEmpty(), "Inactive virtual code is not compiled");
			virtual.setSelection(false);
			virtual.notifyListeners(SWT.Selection, new Event());
			check(code.getEditable() && d.readDraft() == initial, "Toggle back restores identical draft");
			check(d.signature().endsWith(") {") && d.closed(), "Unchecking virtual restores braces");

			table(d).setSelection(0);
			table(d).notifyListeners(SWT.Selection, new Event());
			press(d, "Down");
			check(d.readDraft().arguments().get(0).name().equals("rate")
					&& d.readDraft().arguments().get(1).name().equals("n"), "Down reorders arguments");
			check(d.signature().equals("float grow(float rate, int n <- 1) {"), "Signature follows argument order");
			press(d, "Up");
			check(d.readDraft() == initial, "Up restores order");
			press(d, "Add");
			check(d.readDraft().arguments().size() == 3 && d.readDraft().arguments().get(2).name().equals("arg3"),
					"Add appends a fresh argument");
			check(d.signature().equals("float grow(int n <- 1, float rate, int arg3) {"), "Signature shows added argument");
			((Text) field(d, "Argument name")).setText("n");
			check(!d.ok() && d.error() != null, "Duplicate argument name blocks OK");
			((Text) field(d, "Argument name")).setText("k");
			((Text) field(d, "Argument default")).setText("2");
			check(d.ok() && d.readDraft().arguments().get(2).equals(new ActionDraft.Argument("k", "int", "2")),
					"Argument edits update draft");
			((Combo) field(d, "Argument type")).setText("list<int>");
			check(d.signature().equals("float grow(int n <- 1, float rate, list<int> k <- 2) {"),
					"Signature follows argument name/type/default edits");
			press(d, "Remove");
			check(d.readDraft() == initial, "Remove restores original");
			check(d.signature().equals("float grow(int n <- 1, float rate) {"), "Signature drops removed argument");

			((Text) field(d, "Name")).setText("1bad");
			check(!d.ok() && d.error() != null, "Invalid name blocks OK");
			((Text) field(d, "Name")).setText("grow2");
			check(d.ok(), "Valid name re-enables OK");
			returns.setText("action");
			check(!d.readDraft().returnsValue(), "action means no return value");
			check(d.signature().equals("action grow2(int n <- 1, float rate) {"), "Signature follows name and return type");
			returns.setText("float");

			d.problems = List.of(new ActionVerifier.Problem("unknown variable foo", true, 1));
			d.compiled.clear();
			check(!d.accept() && d.error().contains("foo"), "Code error blocks OK: " + d.error());
			check(d.compiled.contains(initial.body()), "Changed draft compiles its own code");
			d.problems = List.of(new ActionVerifier.Problem("outside", false, 0));
			check(d.validateNow(), "Pre-existing context problem ignored via baseline");
			d.problems = List.of();
			Dialog adapter = d;
			check(adapter.accept() && adapter.getResult().name().equals("grow2"), "Valid change accepted");
			d.close();

			Dialog kept = new Dialog(shell, initial, x -> null) {
				@Override
				protected String signature(final ActionDraft draft) {
					return draft.virtual() ? draft.header() + " virtual: true {" : super.signature(draft);
				}
			};
			kept.create();
			Button keptVirtual = (Button) field(kept, "Virtual");
			keptVirtual.setSelection(true);
			keptVirtual.notifyListeners(SWT.Selection, new Event());
			check(kept.signature().endsWith("virtual: true {") && kept.closed(),
					"Adapter keeping the block shows virtual signature with braces");
			kept.close();

			Dialog rejected = new Dialog(shell, initial, x -> "stale document");
			rejected.create();
			((Text) field(rejected, "Name")).setText("other");
			check(!rejected.accept() && "stale document".equals(rejected.error()), "Adapter validator error blocks OK");
			rejected.click(IDialogConstants.CANCEL_ID);
			check(rejected.getResult() == null, "Cancel returns null");

			Dialog created = new Dialog(shell, ActionDraft.empty(), x -> null);
			created.create();
			check(created.readDraft() == ActionDraft.empty() || created.readDraft().equals(ActionDraft.empty()),
					"Creation starts from empty draft");
			created.close();
			System.out.println("ActionWizardTest: " + checks + " checks passed");
		} finally {
			shell.dispose();
			display.dispose();
		}
	}
}
