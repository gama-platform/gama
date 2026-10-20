package gaml.compiler.ui.wizards.display;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;

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
import org.eclipse.xtext.parser.IParser;

import gaml.compiler.GamlStandaloneSetup;
import gaml.compiler.ui.wizards.action.ActionBodyContext;
import gaml.compiler.ui.wizards.action.ActionVerifier;
import gaml.compiler.ui.wizards.action.EmbeddedActionBody;

/** Native kind-specific forms and actual nested modal Create/Apply/Cancel, with compiler substitution. */
public final class LayerWizardTest {
	private static int checks;
	private static IParser parser;
	private static void check(final boolean condition, final String message) {
		checks++;
		if (!condition) throw new AssertionError(message);
	}
	private static Control field(final Control root, final String property, final String key) {
		if (key.equals(root.getData(property))) return root;
		if (root instanceof Composite parent) for (Control child : parent.getChildren()) {
			Control found = field(child, property, key);
			if (found != null) return found;
		}
		return null;
	}
	private static Button button(final Control root, final String text) {
		if (root instanceof Button b && b.getText().equals(text)) return b;
		if (root instanceof Composite parent) for (Control child : parent.getChildren()) {
			Button found = button(child, text);
			if (found != null) return found;
		}
		return null;
	}
	private static void click(final Control control) { control.notifyListeners(SWT.Selection, new Event()); }
	private static final class Dialog extends LayerWizard {
		int compiles;
		String failure;
		Dialog(final Shell shell, final LayerDraft draft, final AtomicInteger validations) {
			super(shell, draft, d -> { validations.incrementAndGet(); return null; },
					() -> List.of("wolf"), () -> List.of("cells"), null, s -> List.of("default", "icon"));
		}
		@Override protected List<ActionVerifier.Problem> compile(final ActionBodyContext context) {
			compiles++;
			return failure == null || !context.text("").contains("circle(2)") ? List.of()
					: List.of(new ActionVerifier.Problem(failure, false, 0));
		}
		@Override protected EmbeddedActionBody createEmbeddedEditor(final Composite parent,
				final ActionBodyContext context, final String initial) { return null; }
		@Override protected String chooseColor(final String current) { return null; }
		Control field(final String key) { return LayerWizardTest.field(getShell(), FIELD, key); }
	}

	private static final class DisplayDialog extends DisplayWizard {
		final List<String> compiled = new ArrayList<>();
		DisplayDialog(final Shell shell, final DisplayDraft draft, final AtomicInteger validations, final AtomicBoolean stale) {
			super(shell, draft, d -> { validations.incrementAndGet(); return stale.get() ? "The document changed." : null; }, () -> List.of("wolf"),
					() -> List.of("cells"), d -> DisplayWizard.standalone(d, parser), s -> List.of("default"));
		}
		@Override protected List<ActionVerifier.Problem> compile(final ActionBodyContext context) {
			compiled.add(context.text(""));
			List<ActionVerifier.Problem> problems = new ArrayList<>();
			problems.add(new ActionVerifier.Problem("Pre-existing outer error", false, 0));
			if (context.text("").contains("mesh [[9.0]]"))
				problems.add(new ActionVerifier.Problem("Invalid mesh", false, 0));
			return problems;
		}
		@Override protected EmbeddedActionBody createEmbeddedEditor(final Composite parent,
				final ActionBodyContext context, final String initial) { return null; }
		Control field(final String key) { return LayerWizardTest.field(getShell(), FIELD, key); }
		void select(final int index) {
			((org.eclipse.swt.widgets.List) field("Layers")).setSelection(index);
			click(field("Layers"));
		}
	}

	/** The callback runs inside the real child modal event loop, not instead of opening a child wizard. */
	private static void child(final Display display, final DisplayDialog parent,
			final java.util.function.Consumer<Shell> action) {
		display.asyncExec(() -> {
			Shell child = display.getActiveShell();
			if (child == null || child == parent.getShell()) {
				for (Shell candidate : display.getShells())
					if (candidate != parent.getShell() && candidate.getParent() == parent.getShell()) child = candidate;
			}
			check(child != null && child != parent.getShell(), "Nested wizard actually opened");
			try { action.accept(child); }
			finally { if (!child.isDisposed()) child.close(); }
		});
	}

	public static void main(final String[] args) {
		parser = GamlStandaloneSetup.doSetup().getInstance(IParser.class);
		Display display = new Display();
		Shell shell = new Shell(display);
		try {
			for (String source : List.of("species wolf aspect: default;", "agents \"chosen\" value: wolf;",
					"grid cells;", "image \"file.png\";", "graphics \"text\" { draw \"Hello\" color: #red; }",
					"mesh [[1.0]];", "graphics scene { draw circle(1); draw square(1); }",
					"overlay \"HUD\" { draw \"Cycle\" at: {0,0}; }",
					"camera \"custom\" location: {0,0,10} target: {0,0,0};",
					"light \"custom\" type: \"point\" location: {0,0,10};", "rotation 30.0 axis: {0,0,1};",
					"species wolf { species wolf; }", "agents \"chosen\" value: wolf { draw circle(1); }")) {
				LayerDraft original = DisplaySource.parseLayer("/*lead*/ " + source + " //tail", "stable", parser);
				AtomicInteger validations = new AtomicInteger();
				Dialog d = new Dialog(shell, original, validations);
				d.create();
				check(d.getShell().getText().equals(original.kind().title() + " Layer Editor"), "Kind-specific title");
				check(d.field("Kind") == null && button(d.getShell(), "Apply") != null, "Kind locked, common edit footer");
				check(d.readDraft() == original && d.compiles == 0 && validations.get() == 0, "Opening is a detached no-op");
				check(d.accept() && d.getResult() == original && d.compiles == 0, "No-op accepts original without compilation");
				click(button(d.getShell(), "Cancel"));
				check(d.getResult() == null && validations.get() == 1, "Cancel discards even a previously accepted draft");
			}
			for (LayerKind kind : LayerKind.values()) if (kind != LayerKind.RAW) {
				Dialog d = new Dialog(shell, LayerDraft.empty(kind), new AtomicInteger());
				d.setCreationMode(true);
				d.create();
				check(button(d.getShell(), "Create") != null, "Creation footer for " + kind);
				if (kind == LayerKind.SPECIES || kind == LayerKind.GRID || kind == LayerKind.MESH)
					check(!button(d.getShell(), "Create").getEnabled(), "Required expression blocks creation: " + kind);
				Combo expression = (Combo) d.field("Expression");
				expression.setText(switch (kind) {
					case SPECIES -> "wolf";
					case GRID -> "cells";
					case MESH -> "[[1.0]]";
					case ROTATION -> "30.0";
					case TEXT -> "\"Cycle: \" + cycle";
					case IMAGE -> "\"Value\"";
					default -> "\"label\"";
				});
				if (kind == LayerKind.AGENTS) ((Combo) d.field("facet:value")).setText("wolf");
				check(d.readDraft().validationError() == null && button(d.getShell(), "Create").getEnabled(),
						"Required fields satisfied: " + kind);
				click(button(d.getShell(), "Validate"));
				check(d.getResult() == null && d.getErrorMessage() == null && d.compiles == 1, "Explicit validation without acceptance");
				check(d.accept() && d.getResult().id().equals(d.readDraft().id()), "Create preserves detached identity");
				d.close();
			}
			var graphics = DisplaySource.parseLayer("\n// drawing\ngraphics g visible: true { draw circle(1); /*keep*/ }\n", "g", parser);
			Dialog d = new Dialog(shell, graphics, new AtomicInteger());
			d.create();
			((StyledText) d.field("Body")).setText(" draw circle(2); // new body\n");
			check(d.readDraft().body().contains("circle(2)") && d.compiles == 0, "Body edit does not compile on keystrokes");
			d.failure = "Invalid drawing";
			check(!d.accept() && d.getResult() == null && d.getErrorMessage().contains("Invalid drawing"), "Validation errors block acceptance");
			d.failure = null;
			check(d.accept() && DisplaySource.renderLayer(d.getResult(), parser).contains("// drawing\ngraphics g visible: true { draw circle(2)"),
					"Body edit preserves header and leading comment");
			((StyledText) d.field("Body")).setText("} image \"injected\";");
			check(!button(d.getShell(), "Apply").getEnabled(), "Body cannot escape layer scope");
			d.close();

			var parsed = DisplaySource.parseBody("\nimage \"old.png\";\nchart c type: series {data t value: cycle;}\n"
					+ "event 'mouse_down' {write 'click';}\ncustom_layer x;\n", parser);
			var initial = new DisplayDraft("d", "opengl", List.of(), parsed.layers(), parsed.trailingSource());
			var validations = new AtomicInteger();
			var stale = new AtomicBoolean();
			DisplayDialog parent = new DisplayDialog(shell, initial, validations, stale);
			parent.create();
			check(parent.readDraft() == initial && parent.compiled.isEmpty(), "Display opening preserves all rows");
			parent.select(0);
			check(!parent.field("Move up").getEnabled() && parent.field("Move down").getEnabled(), "First row reorder bounds");
			for (int index : List.of(1, 2, 3)) {
				parent.select(index);
				check(!parent.field("Edit").getEnabled(), "Chart/event/unknown edit disabled");
				parent.field("Layers").notifyListeners(SWT.DefaultSelection, new Event());
				check(parent.readDraft() == initial, "Double-click unsupported row is no-op");
			}
			check(!parent.field("Move down").getEnabled(), "Last row reorder bound");
			Combo choices = (Combo) parent.field("New layer kind");
			check(!List.of(choices.getItems()).contains("Raw") && choices.getItemCount() == 11, "Only supported layer/definition kinds can be added");
			parent.select(0);
			child(display, parent, nested -> {
				check(button(nested, "Apply") != null, "Edit invokes Apply mode");
				click(button(nested, "Apply"));
			});
			click(parent.field("Edit"));
			check(parent.readDraft() == initial, "Nested no-op Apply preserves display instance");
			child(display, parent, nested -> {
				((Combo) field(nested, LayerWizard.FIELD, "Expression")).setText("\"cancelled.png\"");
				click(button(nested, "Cancel"));
			});
			click(parent.field("Edit"));
			check(parent.readDraft() == initial, "Child Cancel preserves parent draft");
			stale.set(true);
			child(display, parent, nested -> {
				click(button(nested, "Apply"));
				check(!nested.isDisposed(), "Child no-op Apply still runs editor staleness checks");
				click(button(nested, "Cancel"));
			});
			click(parent.field("Edit"));
			stale.set(false);
			check(parent.readDraft() == initial, "Stale child acceptance leaves parent unchanged");
			choices.select(List.of(choices.getItems()).indexOf("Mesh"));
			child(display, parent, nested -> {
				((Combo) field(nested, LayerWizard.FIELD, "Expression")).setText("[[9.0]]");
				click(button(nested, "Create"));
				check(!nested.isDisposed(), "New contextual compiler error blocks child Create");
				click(button(nested, "Cancel"));
			});
			click(parent.field("Add"));
			check(parent.readDraft() == initial, "Failed child Create never inserts a draft");
			child(display, parent, nested -> {
				check(button(nested, "Create") != null && nested.getText().equals("Mesh Layer Editor"), "Add invokes kind-specific Create wizard");
				((Combo) field(nested, LayerWizard.FIELD, "Expression")).setText("[[2.0]]");
				click(button(nested, "Create"));
			});
			click(parent.field("Add"));
			check(parent.readDraft().layers().size() == 5 && parent.readDraft().layers().getLast().kind() == LayerKind.MESH,
					"Child Create appends only after successful acceptance");
			check(parent.compiled.stream().anyMatch(text -> text.contains("mesh [[2.0]]")
					&& text.contains("chart c") && text.contains("event 'mouse_down'")), "Nested validation receives complete display context");
			String meshId = parent.readDraft().layers().getLast().id();
			click(parent.field("Move up"));
			check(parent.readDraft().layers().get(3).id().equals(meshId), "Move preserves layer identity");
			click(parent.field("Duplicate"));
			check(!parent.readDraft().layers().get(4).id().equals(meshId), "Duplicate gives a new identity");
			click(parent.field("Remove"));
			parent.select(3);
			click(parent.field("Remove"));
			check(parent.readDraft() == initial, "Removing newly added layers restores exact original");
			child(display, parent, nested -> click(button(nested, "Cancel")));
			click(parent.field("Add"));
			check(parent.readDraft() == initial, "Add Cancel never inserts an empty layer");
			child(display, parent, nested -> {
				((Combo) field(nested, LayerWizard.FIELD, "Expression")).setText("[[3.0]]");
				click(button(nested, "Create"));
			});
			click(parent.field("Add"));
			click(button(parent.getShell(), "Cancel"));
			check(parent.getResult() == null && initial.layers().size() == 4, "Parent Cancel discards accepted child edits");
			System.out.println("LayerWizardTest: " + checks + " checks passed");
		} finally {
			shell.dispose();
			display.dispose();
		}
	}
}
