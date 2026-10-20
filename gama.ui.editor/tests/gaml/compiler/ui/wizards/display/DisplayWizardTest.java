package gaml.compiler.ui.wizards.display;

import java.util.List;

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
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Text;
import org.eclipse.xtext.parser.IParser;

import gaml.compiler.GamlStandaloneSetup;
import gaml.compiler.ui.wizards.action.ActionBodyContext;
import gaml.compiler.ui.wizards.action.ActionVerifier;
import gaml.compiler.ui.wizards.species.SpeciesDraft.Facet;

/** Native settings-only dialog: literal-only controls, hidden layer/source retention and explicit validation. */
public final class DisplayWizardTest {
	private static int checks;
	private static IParser parser;
	private static void check(final boolean condition, final String message) {
		checks++;
		if (!condition) throw new AssertionError(message);
	}
	private static Control find(final Control root, final String key) {
		if (key.equals(root.getData(DisplayWizard.FIELD))) return root;
		if (root instanceof Composite c) for (Control child : c.getChildren()) {
			Control found = find(child, key);
			if (found != null) return found;
		}
		return null;
	}
	private static final class Dialog extends DisplayWizard {
		int compiles, validations;
		String selectedColor, failure;
		Dialog(final Shell shell, final DisplayDraft draft) {
			super(shell, draft, d -> null, null, null, null);
		}
		@Override protected IParser parser() { return parser; }
		@Override protected List<ActionVerifier.Problem> compile(final ActionBodyContext context) {
			compiles++;
			return failure == null ? List.of() : List.of(new ActionVerifier.Problem(failure, true, 1));
		}
		@Override protected String chooseColor(final String current) { return selectedColor; }
		Control field(final String key) { return find(getShell(), key); }
		void click(final String key) { field(key).notifyListeners(SWT.Selection, new Event()); }
		void dimension(final boolean is3D) {
			((Button) field(is3D ? "3D" : "2D")).setSelection(true);
			click(is3D ? "3D" : "2D");
		}
		void cancel() { getButton(IDialogConstants.CANCEL_ID).notifyListeners(SWT.Selection, new Event()); }
	}
	private static void sameRow(final Dialog d, final String row, final String... keys) {
		Composite parent = (Composite) d.field(row);
		int y = -1;
		for (String key : keys) {
			Control child = d.field(key);
			check(child.getParent() == parent || child.getParent().getParent() == parent, row + ": " + key);
			int current = child.toDisplay(0, 0).y;
			if (y >= 0) check(Math.abs(current - y) < 12, row + " aligns " + key);
			y = current;
		}
	}
	public static void main(final String[] args) {
		parser = GamlStandaloneSetup.doSetup().getInstance(IParser.class);
		Display display = new Display();
		Shell shell = new Shell(display);
		var axes = gama.api.utils.prefs.GamaPreferences.Displays.CORE_DRAW_ENV;
		var toolbar = gama.api.utils.prefs.GamaPreferences.Displays.CORE_DISPLAY_TOOLBAR;
		boolean savedAxes = axes.getValue(), savedToolbar = toolbar.getValue();
		try {
			axes.setValueNoCheckNoNotification(false);
			toolbar.setValueNoCheckNoNotification(false);
			DisplayDraft fresh = DisplayDraft.empty();
			check(fresh.renderer().equals("java2D") && "true".equals(DisplayDraft.find(fresh.facets(), "axes"))
					&& "true".equals(DisplayDraft.find(fresh.facets(), "toolbar")), "Creation explicitly overrides contrary preferences");
			Dialog d = new Dialog(shell, fresh);
			d.setBlockOnOpen(false); d.open();
			while (display.readAndDispatch()) {}
			check(((TabFolder) d.field("Tabs")).getItemCount() == 2
					&& ((TabFolder) d.field("Tabs")).getItem(0).getText().equals("Settings")
					&& ((TabFolder) d.field("Tabs")).getItem(1).getText().equals("Layers"), "Settings and Layers tabs");
			for (String key : List.of("Layers", "New layer kind", "Add", "Edit", "Duplicate", "Remove", "Move up", "Move down"))
				check(d.field(key) != null, "Layer organization control: " + key);
			check(d.field("Raw source") == null, "Unsupported code has no lossy/raw form");
			for (String key : List.of("parent", "locked", "draw_diffuse_light", "background", "Advanced", "Advanced 3D",
					"virtual:expression", "antialias:expression", "fullscreen:expression", "light:expression", "axes:expression", "toolbar:expression"))
				check(d.field("display:" + key) == null, "Unwanted control absent: " + key);
			sameRow(d, "Settings row 1", "Name", "display:title", "2D", "3D", "display:virtual");
			sameRow(d, "Settings row 2", "display:refresh", "display:autosave", "display:antialias", "display:fullscreen");
			sameRow(d, "Settings row 3", "display:choose:background", "display:toolbar");
			check(d.field("display:antialias").getBounds().x < d.field("display:fullscreen").getBounds().x
					&& ((Composite) d.field("Settings row 2")).getChildren().length == 6,
					"Fullscreen directly follows Antialias, with no secondary controls");
			check(((Composite) d.field("Settings row 3")).getChildren().length == 4,
					"Only Background and Toolbar remain on row3");
			check(((Combo) d.field("display:refresh")).getText().equals("true")
					&& ((Combo) d.field("display:autosave")).getText().equals("false")
					&& ((Combo) d.field("display:toolbar")).getText().equals("true"), "Expression initialization");
			for (String key : List.of("virtual", "antialias", "fullscreen", "light", "axes"))
				check(d.field("display:" + key) instanceof Button b && (b.getStyle() & SWT.CHECK) != 0, "Direct checkbox: " + key);
			check(((Button) d.field("display:fullscreen")).getText().equals("Fullscreen"),
					"Display boolean labels are capitalized");
			check(!d.field("3D settings").getVisible() && ((Button) d.field("2D")).getSelection(), "2D hides 3D fields");
			check(d.readDraft() == fresh && d.accept() && d.compiles == 0,
					"Untouched fallback values preserve no-op: " + d.readDraft() + " versus " + fresh + "; compiles " + d.compiles);
			d.dimension(true);
			d.getShell().layout(true, true);
			while (display.readAndDispatch()) {}
			check(d.field("3D settings").getVisible(), "3D directly exposes fields without disclosure");
			sameRow(d, "Settings row 5", "display:light", "display:axes", "display:keystone");
			sameRow(d, "Settings row 6", "display:camera", "display:z_near", "display:z_far");
			check(((Button) d.field("display:light")).getSelection() && ((Button) d.field("display:axes")).getSelection(), "3D flags start true");
			List<String> choices = List.of(((Combo) d.field("display:camera")).getItems());
			for (String preset : gama.api.ui.layers.ICameraDefinition.PRESETS)
				check(choices.contains("\"" + preset + "\""), "Real camera preset: " + preset);
			check(((Combo) d.field("display:camera")).getText().equals("\"default\"")
					&& ((Text) d.field("display:z_near")).getText().isEmpty()
					&& ((Text) d.field("display:z_far")).getText().isEmpty(), "Camera and clipping defaults");
			d.click("display:choose:background");
			check(DisplayDraft.find(d.readDraft().facets(), "background") == null, "Color cancellation is no-op");
			d.selectedColor = "rgb(10, 20, 30)"; d.click("display:choose:background");
			((Combo) d.field("display:title")).setText("\"Window title\"");
			((Combo) d.field("display:toolbar")).setText("cycle > 1");
			((Text) d.field("display:z_far")).setText("1000.0");
			((Combo) d.field("display:keystone")).setText("[{0,0},{1,0},{0,1},{1,1}]");
			DisplayDraft edited = d.readDraft();
			d.dimension(false);
			check(!d.field("3D settings").getVisible() && d.readDraft().facets().equals(edited.facets()), "2D retains hidden facets");
			d.dimension(true);
			check(d.readDraft().equals(edited), "3D roundtrip restores draft");
			check(d.accept() && d.compiles > 0, "Changed OK compiles explicitly");
			check(!parser.parse(new java.io.StringReader(DisplayWizard.standalone(d.readDraft(), parser).prefix())).hasSyntaxErrors(),
					"Generated settings GAML parses");
			d.close();

			for (String value : List.of("", "true", "false", "fullscreen_condition")) {
				DisplayDraft initial = new DisplayDraft("d", "java2D", value.isEmpty() ? List.of()
						: List.of(new gaml.compiler.ui.wizards.species.SpeciesDraft.Facet("fullscreen", value)),
						List.of(), "");
				d = new Dialog(shell, initial);
				d.create();
				Button fullscreen = (Button) d.field("display:fullscreen");
				check(fullscreen.getSelection() == value.equals("true") && d.readDraft() == initial
						&& d.accept() && d.getResult() == initial, "Fullscreen loaded literal/expression no-op: " + value);
				fullscreen.setSelection(true); d.click("display:fullscreen");
				check("true".equals(DisplayDraft.find(d.readDraft().facets(), "fullscreen")), "Fullscreen explicit true: " + value);
				fullscreen.setSelection(false); d.click("display:fullscreen");
				check(DisplayDraft.find(d.readDraft().facets(), "fullscreen") == null, "Fullscreen false omission default: " + value);
				d.cancel();
				check(d.getResult() == null, "Fullscreen edits discarded on Cancel");
			}

			String body = "\n// ordered\nspecies wolf visible: cycle > 0;\nchart c type: series {data x value: cycle;}\n// tail\n";
			String source = "model m\nspecies wolf {}\nexperiment e type: gui {output {display d type: opengl "
					+ "virtual: virtual_condition light: light_condition axes: axes_condition antialias: aa_condition fullscreen: fullscreen_condition "
					+ "background: color_expression parent: base locked: true draw_diffuse_light: false {" + body + "}}}";
			var adapter = new DisplaySource(source, source.indexOf("display d"), parser, false);
			for (String renderer : List.of("opengl", "'2d'", "\"3d\"", "java2D", "", "plugin_renderer")) {
				var original = adapter.draft();
				var loaded = new DisplayDraft(original.name(), renderer, original.facets(), original.layers(), original.trailingSource());
				d = new Dialog(shell, loaded); d.create();
				check(d.readDraft() == loaded && d.accept() && d.getResult() == loaded, "Loaded no-op retains renderer/source: " + renderer);
				check(d.field("display:virtual") instanceof Button && d.field("display:axes") instanceof Button
						&& d.field("display:background") == null, "Nonliteral values have no expression UI");
				((Combo) d.field("display:title")).setText("\"New title\"");
				check(d.readDraft().layers().equals(loaded.layers()) && d.readDraft().trailingSource().equals(loaded.trailingSource()),
						"Settings edits keep exact layer drafts/order/trailing bytes");
				String rendered = adapter.render(new DisplayDraft(loaded.name(), original.renderer(), d.readDraft().facets(),
						d.readDraft().layers(), d.readDraft().trailingSource()));
				check(rendered.contains("{" + body + "}") && rendered.contains("background: color_expression")
						&& rendered.contains("parent: base locked: true draw_diffuse_light: false"), "Source header edit preserves body and hidden facets");
				for (String key : List.of("virtual", "antialias", "light", "axes", "fullscreen")) {
					((Button) d.field("display:" + key)).setSelection(false); d.click("display:" + key);
					check(java.util.Objects.equals(DisplayDraft.find(d.readDraft().facets(), key),
							key.equals("virtual") ? null : "false"),
							"Explicit checkbox replaces only its source expression: " + key);
				}
				check(d.accept() && d.getResult().layers().equals(loaded.layers()), "Accepted edit retains layers");
				d.close();
			}
			d = new Dialog(shell, adapter.draft()); d.setBlockOnOpen(false); d.open();
			((Text) d.field("Name")).setText("cancelled"); d.cancel();
			check(d.getResult() == null && adapter.render(adapter.draft()) == source, "Cancel retains complete source");
			System.out.println("DisplayWizardTest: " + checks + " checks passed");
		} finally {
			axes.setValueNoCheckNoNotification(savedAxes);
			toolbar.setValueNoCheckNoNotification(savedToolbar);
			shell.dispose(); display.dispose();
		}
	}
}
