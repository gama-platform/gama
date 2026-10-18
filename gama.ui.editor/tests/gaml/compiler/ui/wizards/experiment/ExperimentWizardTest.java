package gaml.compiler.ui.wizards.experiment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import gaml.compiler.ui.wizards.action.ActionBodyContext;
import gaml.compiler.ui.wizards.action.ActionVerifier;
import gaml.compiler.ui.wizards.species.SpeciesDraft.Facet;
import gaml.compiler.ui.wizards.species.SpeciesDraft.Variable;

/** Native SWT regression of the experiment dialog; the compiler is substituted. */
public final class ExperimentWizardTest {
	private static int checks;

	private static class Dialog extends ExperimentWizard {
		final List<String> compiled = new ArrayList<>();
		Function<String, List<ActionVerifier.Problem>> byCode = c -> List.of();

		Dialog(final Shell shell, final ExperimentDraft draft, final Function<ExperimentDraft, String> validator) {
			super(shell, draft, validator, List.of("int", "float", "string"), () -> List.of("n", "rate"),
					d -> new ActionBodyContext(
							org.eclipse.emf.common.util.URI.createURI("synthetic:/t.experiment-wizard.gaml"), "prefix{",
							"}"));
		}

		@Override
		protected List<ActionVerifier.Problem> compile(final ActionBodyContext context, final String code) {
			compiled.add(code);
			return byCode.apply(code);
		}

		boolean ok() { return getButton(IDialogConstants.OK_ID).getEnabled(); }

		String error() { return getErrorMessage(); }

		String message() { return getMessage(); }

		int messageType() {
			// TitleAreaDialog does not expose the type; the warning prefix is the contract.
			return message() != null && message().startsWith("Kept but not used") ? IMessageProvider.WARNING
					: IMessageProvider.NONE;
		}

		void cancel() { getButton(IDialogConstants.CANCEL_ID).notifyListeners(SWT.Selection, new Event()); }
	}

	private static void check(final boolean condition, final String message) {
		checks++;
		if (!condition) throw new AssertionError(message);
		System.out.println("ok " + checks + " " + message);
	}

	private static Control find(final Control root, final Object value) {
		if (value.equals(root.getData(ExperimentWizard.FIELD)) && !root.isDisposed()) return root;
		if (root instanceof Composite c) for (Control child : c.getChildren()) {
			Control found = find(child, value);
			if (found != null) return found;
		}
		return null;
	}

	private static Control field(final Dialog d, final String key) { return find(d.getShell(), key); }

	private static void select(final Dialog d, final String type) {
		Combo combo = (Combo) field(d, "Type");
		combo.select(Arrays.asList(combo.getItems()).indexOf(type));
		combo.notifyListeners(SWT.Selection, new Event());
	}

	private static void press(final Dialog d, final String key) {
		((Button) field(d, key)).notifyListeners(SWT.Selection, new Event());
	}

	private static void toggle(final Dialog d, final String key, final boolean on) {
		Button b = (Button) field(d, key);
		b.setSelection(on);
		b.notifyListeners(SWT.Selection, new Event());
	}

	private static List<String> tabs(final Dialog d) {
		return Arrays.stream(((TabFolder) field(d, "Tabs")).getItems()).map(TabItem::getText).toList();
	}

	private static boolean inside(final Control control, final Control area) {
		for (Control c = control; c != null; c = c.getParent()) if (c == area) return true;
		return false;
	}

	private static void selectRow(final Dialog d, final String table, final int index) {
		Table t = (Table) field(d, table);
		t.setSelection(index);
		t.notifyListeners(SWT.Selection, new Event());
	}

	public static void main(final String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		try {
			AtomicInteger validations = new AtomicInteger();
			ExperimentDraft gui = new ExperimentDraft("My exp", "gui",
					List.of(new Facet("autorun", "true"), new Facet("parallel", "nb > 2"), new Facet("custom", "3")),
					List.of(new Variable("k", "int", "1", "", "", "0", "")), "\n\t\twrite k;\n\t",
					List.of(new ExperimentDraft.Parameter("Count", "n",
							List.of(new Facet("min", "1"), new Facet("category", "\"C\""), new Facet("on_change", "foo")))),
					List.of(new ExperimentDraft.Monitor("Value", "n", List.of(new Facet("refresh", "every(2 #cycle)")))));
			Dialog d = new Dialog(shell, gui, x -> {
				validations.incrementAndGet();
				return null;
			});
			d.create();

			// ---- loading
			check(((Text) field(d, "Name")).getText().equals("My exp"), "Name loaded");
			check(((Combo) field(d, "Type")).getText().equals("gui"), "Type loaded");
			check(field(d, "facet:autorun") instanceof Button b && b.getSelection()
					&& inside(b, field(d, "Common")), "Literal flag as checked box in the common section");
			check(field(d, "facet:parallel") instanceof Text t && t.getText().equals("nb > 2"),
					"Expression-valued flag shown as expression, not a check box");
			check(field(d, "facet:benchmark") instanceof Button b && !b.getSelection(), "Absent flag unchecked");
			check(field(d, "facet:keep_seed") == null, "Batch flag hidden for gui");
			check(field(d, "facet:record") != null && inside(field(d, "facet:record"), field(d, "TypeSection")),
					"GUI section holds record");
			check(field(d, "facet:repeat") == null, "Batch facets hidden for gui");
			check(field(d, "facet:custom") instanceof Text t && t.getText().equals("3") && inside(t, field(d, "Other")),
					"Undeclared facet kept in the other area");
			check(tabs(d).equals(List.of("Attributes", "Parameters", "Monitors", "Init")), "GUI tab order");
			check(((Table) field(d, "Parameters")).getItemCount() == 1 && ((Table) field(d, "Monitors")).getItemCount() == 1,
					"Parameter and monitor rows");
			check(((Text) field(d, "param:title")).getText().equals("Count")
					&& ((Combo) field(d, "param:var")).getText().equals("n")
					&& ((Text) field(d, "param:min")).getText().equals("1"), "Parameter detail loaded");
			check(Arrays.asList(((Combo) field(d, "param:var")).getItems()).containsAll(List.of("n", "rate", "k")),
					"Variables offered: model's and experiment's attributes");
			check(((Text) field(d, "param:other")).getText().equals("on_change: foo")
					&& !((Text) field(d, "param:other")).getEditable(), "Unexposed parameter facets shown read-only");
			check(field(d, "param:slider") instanceof Button, "Parameter flag as check box");
			StyledText init = (StyledText) field(d, "Init");
			check(init.getText().equals(gui.init()), "Init box holds only the raw body");
			String sig = ((Label) field(d, "Signature")).getText();
			check(sig.startsWith("experiment \"My exp\" autorun: true parallel: nb > 2 custom: 3 {")
					&& sig.endsWith("init {") && !init.getText().contains("experiment"),
					"Signature outside the body: " + sig);
			check(field(d, "Closing") instanceof Label, "Closing brace outside the body");

			// ---- unchanged / no-op
			check(d.readDraft() == gui, "Unchanged form returns the initial instance");
			check(d.accept() && d.getResult() == gui && d.compiled.isEmpty() && validations.get() == 1,
					"Unchanged OK: no compilation, adapter validator, same instance");

			// ---- live signature
			((Text) field(d, "Name")).setText("Other");
			check(((Label) field(d, "Signature")).getText().startsWith("experiment Other autorun"),
					"Signature follows the name (identifier unquoted)");
			((Text) field(d, "Name")).setText("bad\"name");
			check(!d.ok() && d.error() != null, "Quote in the name blocks OK");
			((Text) field(d, "Name")).setText("My exp");
			check(d.readDraft() == gui, "Restored name: identical draft");

			// ---- check boxes
			toggle(d, "facet:autorun", false);
			check(d.readDraft().facet("autorun") == null, "Unchecking an implicit flag removes it");
			toggle(d, "facet:autorun", true);
			check(d.readDraft() == gui, "Rechecking restores the identical draft");
			toggle(d, "facet:benchmark", true);
			check("true".equals(d.readDraft().facet("benchmark")), "Checking sets true");
			toggle(d, "facet:benchmark", false);
			check(d.readDraft() == gui, "Unchecking a new flag removes it");

			// ---- type switch keeps everything, reorganizes, warns
			((Text) field(d, "facet:custom")).setText("4");
			select(d, "batch");
			check(tabs(d).equals(List.of("Parameters (explored)", "Attributes", "Init", "Monitors")), "Batch tab order");
			check(((Group) field(d, "TypeSection")).getText().equals("Batch exploration")
					&& inside(field(d, "facet:repeat"), field(d, "TypeSection"))
					&& field(d, "facet:keep_simulations") instanceof Button, "Batch section reorganized");
			check(field(d, "facet:record") == null, "GUI section gone");
			check(field(d, "Init") == init && !init.isDisposed() && field(d, "Attributes") != null
					&& field(d, "Monitors") != null, "Pages survive the reorganization of the tabs");
			check(((Text) field(d, "facet:custom")).getText().equals("4"), "Edited other facet survives the switch");
			check(d.ok() && d.messageType() == IMessageProvider.NONE, "No inactive facet yet");
			ExperimentDraft batch = d.readDraft();
			check(batch.type().equals("batch") && batch.parameters().equals(gui.parameters())
					&& batch.monitors().equals(gui.monitors()) && batch.variables().equals(gui.variables())
					&& batch.init().equals(gui.init()), "Switch keeps attributes, parameters, monitors and init");
			check(((Label) field(d, "Signature")).getText().startsWith("experiment \"My exp\" type: batch"),
					"Signature shows the type");
			((Text) field(d, "facet:repeat")).setText("3");
			check(d.readDraft().facet("repeat").equals("3"), "Batch facet set");
			// Parameter detail puts the exploration facets first in batch.
			Control among = field(d, "param:among");
			Control category = field(d, "param:category");
			check(among != null && among.toDisplay(0, 0).y <= category.toDisplay(0, 0).y
					&& (among.toDisplay(0, 0).y < category.toDisplay(0, 0).y
							|| among.toDisplay(0, 0).x < category.toDisplay(0, 0).x),
					"Batch parameter detail: among before category");
			select(d, "test");
			check(tabs(d).equals(List.of("Attributes", "Init", "Parameters", "Monitors")), "Test tab order");
			check(d.ok() && d.messageType() == IMessageProvider.WARNING && d.message().contains("repeat"),
					"Batch facet kept with a warning in test: " + d.message());
			check(field(d, "facet:repeat") instanceof Text t && t.getText().equals("3")
					&& inside(t, field(d, "Other")), "Inactive facet editable in the other area");
			((Text) field(d, "facet:repeat")).setText("");
			check(d.readDraft().facet("repeat") == null && d.messageType() == IMessageProvider.NONE,
					"Clearing it removes the facet and the warning");
			select(d, "gui");
			((Text) field(d, "facet:custom")).setText("3");
			check(d.readDraft() == gui, "Back to gui with original values: identical draft");

			// ---- parameters
			press(d, "AddParameter");
			check(((Table) field(d, "Parameters")).getItemCount() == 2 && d.readDraft().parameters().size() == 2,
					"Parameter added");
			ExperimentDraft.Parameter added = d.readDraft().parameters().get(1);
			check(added.variable().equals("n") && !added.title().isBlank(), "New parameter defaults");
			((Text) field(d, "param:title")).setText("Rate");
			((Combo) field(d, "param:var")).setText("rate");
			((Text) field(d, "param:max")).setText("1.0");
			toggle(d, "param:slider", true);
			added = d.readDraft().parameters().get(1);
			check(added.title().equals("Rate") && added.variable().equals("rate") && "1.0".equals(added.facet("max"))
					&& "true".equals(added.facet("slider")), "Parameter edited through the detail panel");
			check(((Table) field(d, "Parameters")).getItem(1).getText(2).contains("max: 1.0"), "Row refreshed");
			((Text) field(d, "param:title")).setText("");
			check(!d.ok() && d.error() != null, "Parameter title required");
			((Text) field(d, "param:title")).setText("Rate");
			((Combo) field(d, "param:var")).setText("");
			check(!d.ok(), "Parameter variable required");
			((Combo) field(d, "param:var")).setText("rate");
			selectRow(d, "Parameters", 0);
			check(((Text) field(d, "param:title")).getText().equals("Count"), "Selecting a row shows its detail");
			((Text) field(d, "param:min")).setText("");
			check(d.readDraft().parameters().get(0).facet("min") == null
					&& "foo".equals(d.readDraft().parameters().get(0).facet("on_change")),
					"Clearing a facet removes it, unexposed facets kept");
			((Text) field(d, "param:min")).setText("1");
			check(d.readDraft().parameters().get(0).equals(gui.parameters().get(0)),
					"Restoring the facet restores the original parameter");
			selectRow(d, "Parameters", 1);
			press(d, "RemoveParameter");
			check(d.readDraft() == gui, "Removing the added parameter: identical draft");

			// ---- monitors
			press(d, "AddMonitor");
			((Text) field(d, "monitor:value")).setText("");
			check(!d.ok() && d.error() != null, "Monitor value required");
			((Text) field(d, "monitor:value")).setText("rate * 2");
			((Text) field(d, "monitor:color")).setText("#red");
			ExperimentDraft.Monitor monitor = d.readDraft().monitors().get(1);
			check(monitor.value().equals("rate * 2") && "#red".equals(monitor.facet("color")), "Monitor edited");
			check(d.ok(), "Valid monitor");
			press(d, "RemoveMonitor");
			check(d.readDraft() == gui, "Removing the added monitor: identical draft");

			// ---- attributes (shared table) feed the parameter variables
			// (the AttributeTable has its own suite; here only the cross-field link)

			// ---- init code and contextual validation
			init.setText("\n\t\tdo die;\n\t");
			d.byCode = c -> c.contains("die") ? List.of(new ActionVerifier.Problem("unknown die", true, 2)) : List.of();
			check(!d.accept() && d.error().equals("Init: line 2: unknown die") || !d.accept() && d.error().contains("unknown die"),
					"In-code error reported: " + d.error());
			check(d.getResult() == null, "Error: no result");
			init.setText("\n\t\twrite 2;\n\t");
			check(d.accept() && d.getResult().init().equals("\n\t\twrite 2;\n\t") && d.compiled.contains("\n\t\twrite 2;\n\t"),
					"Changed init compiled on OK");
			d.close();

			// ---- baseline: pre-existing errors outside the code are not attributed to the edit
			d = new Dialog(shell, gui, x -> null);
			d.byCode = c -> c.equals(gui.init()) ? List.of(new ActionVerifier.Problem("old error", false, 0))
					: List.of(new ActionVerifier.Problem("old error", false, 0),
							new ActionVerifier.Problem("new error", false, 0));
			d.create();
			((StyledText) field(d, "Init")).setText("x");
			check(!d.accept() && d.error().contains("new error"), "New out-of-code error reported");
			d.byCode = c -> List.of(new ActionVerifier.Problem("old error", false, 0));
			check(d.accept(), "Pre-existing out-of-code error ignored");
			d.close();

			// ---- explicit false kept
			ExperimentDraft keep = new ExperimentDraft("k", "batch", List.of(new Facet("keep_seed", "false")), List.of(),
					"", List.of(), List.of());
			d = new Dialog(shell, keep, x -> null);
			d.create();
			check(field(d, "facet:keep_seed") instanceof Button b && !b.getSelection(), "Explicit false unchecked");
			toggle(d, "facet:keep_seed", true);
			toggle(d, "facet:keep_seed", false);
			check(d.readDraft() == keep, "Unchecking keeps the explicit false");
			check(tabs(d).get(0).equals("Parameters (explored)"), "Batch draft opens on explored parameters");
			d.close();

			// ---- creation from the empty draft
			ExperimentDraft empty = ExperimentDraft.empty(ExperimentDraft.TEST);
			d = new Dialog(shell, empty, x -> null);
			d.create();
			check(((Combo) field(d, "Type")).getText().equals("test") && ((StyledText) field(d, "Init")).getText().isEmpty(),
					"Empty test draft: empty init box");
			check(((Label) field(d, "Signature")).getText().startsWith("experiment my_experiment type: test {"),
					"Creation signature");
			check(d.accept() && d.getResult() == empty, "Unchanged creation accepted as is");
			d.close();

			// ---- cancel and adapter errors
			d = new Dialog(shell, gui, x -> null);
			d.setBlockOnOpen(false);
			d.open();
			((Text) field(d, "Name")).setText("changed");
			d.cancel();
			check(d.getResult() == null, "Cancel returns no result");
			d = new Dialog(shell, gui, x -> "stale");
			d.create();
			check(!d.accept() && "stale".equals(d.error()) && d.getResult() == null,
					"Adapter error (e.g. stale document) blocks unchanged OK");
			d.close();
			ActionBodyContext standalone = ExperimentWizard.standalone(gui);
			check(standalone.text("X").contains("init {X}") && standalone.text("X").startsWith("model "),
					"Standalone context");
			System.out.println("ExperimentWizardTest: " + checks + " checks passed");
		} finally {
			display.dispose();
		}
	}
}
