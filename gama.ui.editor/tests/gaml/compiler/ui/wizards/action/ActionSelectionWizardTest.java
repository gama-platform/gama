package gaml.compiler.ui.wizards.action;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.emf.common.util.URI;
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
import org.eclipse.swt.widgets.Text;

import gaml.compiler.GamlStandaloneSetup;

/** Selection-only option placement, independent body buffers, contextual extraction and transactional cancellation. */
public final class ActionSelectionWizardTest {
	private static int checks;
	private static void check(final boolean condition, final String message) {
		checks++;
		if (!condition) throw new AssertionError(message);
	}
	private static Control field(final Control root, final String key) {
		if (key.equals(root.getData("action.field"))) return root;
		if (root instanceof Composite parent) for (Control child : parent.getChildren()) {
			Control found = field(child, key);
			if (found != null) return found;
		}
		return null;
	}
	private static final class Dialog extends ActionWizard {
		final List<String> compiled = new ArrayList<>();
		String failure;
		Dialog(final Shell shell, final ActionExtractionSource source, final AtomicBoolean stale,
				final AtomicReference<Dialog> current) {
			super(shell, source.draft(), draft -> stale.get() ? "The document changed."
					: source.validationError(draft, current.get().isSelectionIncluded()), List.of("int", "float"),
					draft -> source.bodyContext(draft, current.get().isSelectionIncluded(),
							URI.createURI("synthetic:/selection.action-wizard.gaml")));
			current.set(this);
			setSelectionExtractionAvailable(true);
			setCreationMode(true);
		}
		@Override protected List<ActionVerifier.Problem> compile(final ActionBodyContext context, final String body) {
			compiled.add(context.text(body));
			return failure == null ? List.of() : List.of(new ActionVerifier.Problem(failure, true, 1));
		}
		Control field(final String key) { return ActionSelectionWizardTest.field(getShell(), key); }
		void include(final boolean include) {
			((Button) field("Include selection")).setSelection(include);
			field("Include selection").notifyListeners(SWT.Selection, new Event());
		}
		void click(final int id) { getButton(id).notifyListeners(SWT.Selection, new Event()); }
	}

	public static void main(final String[] args) throws Exception {
		var parser = GamlStandaloneSetup.doSetup().getInstance(org.eclipse.xtext.parser.IParser.class);
		String selected = "write 'selected';\n";
		String source = "model m\nglobal {reflex r {\n" + selected + "write 'outside';\n}}\n";
		var extraction = new ActionExtractionSource(source, source.indexOf(selected), selected.length(), parser);
		Display display = new Display();
		Shell shell = new Shell(display);
		try {
			var stale = new AtomicBoolean();
			var current = new AtomicReference<Dialog>();
			Dialog d = new Dialog(shell, extraction, stale, current);
			d.create();
			Control returns = d.field("Returns"), include = d.field("Include selection"), virtual = d.field("Virtual");
			check(include instanceof Button b && (b.getStyle() & SWT.CHECK) != 0 && b.getSelection(),
					"Selection-only direct checkbox initially checked");
			check(include.getParent() == returns.getParent()
					&& returns.getBounds().x < include.getBounds().x && include.getBounds().x < virtual.getBounds().x
					&& Math.abs(returns.toDisplay(0, 0).y - include.toDisplay(0, 0).y) < 12,
					"Include selection is inline after Returns, before Virtual");
			StyledText body = (StyledText) d.field("Code");
			check(body.getText().equals(selected) && d.isSelectionIncluded() && d.compiled.isEmpty(),
					"Selected source prefilled without compilation");
			check(d.validateNow() && d.getResult() == null, "Validate does not accept");
			check(d.compiled.stream().anyMatch(text -> text.contains("do my_action;") && text.contains(selected)),
					"Validation sees replacement call and prefilled action");
			check(d.accept() && d.getResult() == extraction.draft(), "Untouched prefilled action can be accepted");
			d.compiled.clear();
			body.setText("write 'edited selected';\n");
			d.include(false);
			check(!d.isSelectionIncluded() && body.getText().isEmpty(), "Opt-out starts with a separate empty action body");
			body.setText("write 'independent';");
			d.include(true);
			check(body.getText().equals("write 'edited selected';\n"), "Opt-in restores edited selected-body buffer");
			d.include(false);
			check(body.getText().equals("write 'independent';"), "Opt-out restores edited separate-body buffer");
			check(d.compiled.isEmpty(), "Toggling/editing bodies never compiles on keystrokes");
			((Text) d.field("Name")).setText("independent");
			check(d.accept() && extraction.render(d.getResult(), false).contains(selected), "Opt-out does not remove caller code");
			d.include(true);
			((Text) d.field("Name")).setText("moved");
			check(d.accept() && extraction.render(d.getResult(), true).contains("do moved;"),
					"Chosen name drives replacement call");
			((Combo) d.field("Returns")).setText("int");
			body.setText("return 1;");
			check(d.accept() && extraction.render(d.getResult(), true).contains("do moved;"),
					"Typed action still uses a do call (return value intentionally discarded)");
			d.failure = "Unknown caller variable";
			check(!d.accept() && d.getResult() == null && d.getErrorMessage().contains("Unknown caller variable"),
					"Contextual errors block extraction and clear previous result");
			d.failure = null;
			stale.set(true);
			check(!d.accept() && d.getResult() == null && d.getErrorMessage().contains("document changed"),
					"Editor changes during modal dialog block acceptance");
			stale.set(false);
			d.click(IDialogConstants.CANCEL_ID);
			check(d.getResult() == null && extraction.draft().body().equals(selected), "Cancel never mutates source draft");

			d = new Dialog(shell, extraction, stale, current);
			d.create();
			((Button) d.field("Virtual")).setSelection(true);
			d.field("Virtual").notifyListeners(SWT.Selection, new Event());
			check(!d.accept() && d.getErrorMessage().contains("cannot be virtual"), "Virtual extraction is explicitly rejected");
			d.include(false);
			check(d.accept() && d.getResult().virtual(), "Opt-out permits ordinary virtual action creation");
			d.close();

			var plain = new ActionWizard(shell, ActionDraft.empty(), x -> null, List.of("int"), null);
			plain.create();
			check(field(plain.getShell(), "Include selection") == null && !plain.isSelectionIncluded(),
					"No selection option in ordinary creation/editing/diagram dialogs");
			plain.close();

			var partial = new ActionExtractionSource(source, source.indexOf("write 'selected'"), "write".length(), parser);
			d = new Dialog(shell, partial, stale, current);
			d.create();
			check(!d.accept() && d.getErrorMessage() != null, "Partial selection cannot be extracted");
			d.include(false);
			check(d.accept(), "User can uncheck selection instead of leaving the form to create a normal action");
			d.close();
			System.out.println("ActionSelectionWizardTest: " + checks + " checks passed");
		} finally {
			shell.dispose();
			display.dispose();
		}
	}
}
