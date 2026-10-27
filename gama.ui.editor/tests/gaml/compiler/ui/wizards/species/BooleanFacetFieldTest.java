package gaml.compiler.ui.wizards.species;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import gama.api.utils.prefs.GamaPreferences;
import gaml.compiler.ui.wizards.display.DisplayFacet;
import gaml.compiler.ui.wizards.experiment.ExperimentFacet;
import gaml.compiler.ui.wizards.species.SpeciesDraft.Facet;

/** Native checkbox/expression transitions, default normalization and live context without evaluating GAML. */
public final class BooleanFacetFieldTest {
	private static int checks;
	private static void check(final boolean condition, final String message) {
		checks++;
		if (!condition) throw new AssertionError(message);
	}
	private static Control field(final Composite parent, final String key) {
		for (Control child : parent.getChildren()) if (key.equals(child.getData("test"))) return child;
		throw new AssertionError("Missing " + key);
	}
	private static void toggle(final Composite parent, final boolean selection) {
		Button check = (Button) field(parent, "value");
		check.setSelection(selection);
		check.notifyListeners(SWT.Selection, new Event());
	}
	private static void mode(final Composite parent) {
		field(parent, "value:expression").notifyListeners(SWT.Selection, new Event());
	}
	private static DisplayFacet metadata(final String keyword, final String name) {
		return DisplayFacet.of(keyword).stream().filter(f -> name.equals(f.name())).findFirst().orElseThrow();
	}
	public static void main(final String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		try {
			for (boolean defaultValue : List.of(false, true)) {
				AtomicReference<String> edited = new AtomicReference<>("untouched");
				var control = new BooleanFacetField(shell, "Flag", null, () -> defaultValue, false, "Description",
						"test", "value", edited::set);
				check(((Button) field(control, "value")).getSelection() == defaultValue, "Uses actual default");
				check(edited.get().equals("untouched") && control.value() == null, "Creation does not edit");
				check(field(control, "value").getToolTipText().contains("Default: " + defaultValue), "Default documented");
				check(control.getChildren().length == 2, "One checkbox and expression action, no presence control");
				toggle(control, !defaultValue);
				check(edited.get().equals(Boolean.toString(!defaultValue)), "Override explicitly written");
				toggle(control, defaultValue);
				check(edited.get() == null, "Returning to default removes facet");
				mode(control);
				check(field(control, "value") instanceof Text && edited.get() == null, "Expression path does not edit");
				((Text) field(control, "value")).setText(" cycle > 0 ");
				check(edited.get().equals(" cycle > 0 "), "Expression kept exactly");
				mode(control);
				check(edited.get() == null && ((Button) field(control, "value")).getSelection() == defaultValue,
						"Explicitly replacing expression uses default");
				control.dispose();

				control = new BooleanFacetField(shell, "Flag", " " + defaultValue + " ", () -> defaultValue,
						false, "", "test", "value", edited::set);
				check(control.value().equals(" " + defaultValue + " ") && !control.edited(), "Explicit default untouched");
				toggle(control, !defaultValue);
				toggle(control, defaultValue);
				check(control.value() == null, "Edited explicit default removed");
				control.dispose();

				control = new BooleanFacetField(shell, "Required", null, () -> defaultValue, true, "",
						"test", "value", edited::set);
				check(control.value() == null, "Missing required declaration not silently manufactured");
				toggle(control, defaultValue);
				check(edited.get().equals(Boolean.toString(defaultValue)), "Required default remains explicit");
				control.dispose();
			}
			var context = new AtomicReference<Boolean>(true);
			var control = new BooleanFacetField(shell, "Context", null, context::get, false, "", "test", "value", v -> {});
			context.set(false);
			BooleanFacetField.refreshDefaults(shell);
			check(!((Button) field(control, "value")).getSelection() && !control.edited(), "Live default refresh no edit");
			context.set(null);
			BooleanFacetField.refreshDefaults(shell);
			check(field(control, "value") instanceof Text && control.getChildren().length == 2,
					"Unresolved context is one labelled expression field");
			((Text) field(control, "value")).setText("true");
			check(control.value().equals("true"), "Unknown default never causes omission");
			context.set(true);
			BooleanFacetField.refreshDefaults(shell);
			check(((Button) field(control, "value")).getSelection() && control.value().equals("true"),
					"Resolving context does not silently normalize existing literal");
			control.dispose();
			check(metadata("species_layer", "refresh").defaultValue("species_layer", List.of()), "Species refresh true");
			check(!metadata("picture", "refresh").defaultValue("picture", List.of()), "Image refresh false");
			check(metadata("species_layer", "visible").defaultValue("species_layer", List.of()), "Visible true");
			check(!metadata("display_grid", "smooth").defaultValue("display_grid", List.of()), "Grid smooth false");
			check(metadata("draw", "perspective").defaultValue("draw", List.of()), "Text perspective true");
			check(metadata("draw", "lighted").defaultValue("draw", List.of()), "Draw lighted true");
			check(!metadata("draw", "wireframe").defaultValue("draw", List.of()), "Draw wireframe false");
			check(metadata("display", "refresh").defaultValue("display", List.of(new Facet("parent", "base"))) == null,
					"Parent prevents unsafe default inference");
			check(metadata("display", "axes").defaultValue("display", List.of(new Facet("draw_env", "cycle > 0"))) == null,
					"Legacy alias prevents unsafe axes default inference");
			check(!metadata("display", "virtual").defaultValue("display", List.of(new Facet("parent", "base"))),
					"Display virtual is deliberately not inherited by compiler");
			var preference = GamaPreferences.Displays.CORE_ANTIALIAS;
			boolean saved = preference.getValue();
			try {
				preference.setValueNoCheckNoNotification(!saved);
				check(metadata("display", "antialias").defaultValue("display", List.of()) == !saved,
						"Antialias reads changed live preference");
				preference.setValueNoCheckNoNotification(saved);
				check(metadata("display", "antialias").defaultValue("display", List.of()) == saved,
						"Preference value is not frozen in metadata");
			} finally { preference.setValueNoCheckNoNotification(saved); }
			var axes = GamaPreferences.Displays.CORE_DRAW_ENV;
			boolean savedAxes = axes.getValue();
			try {
				axes.setValueNoCheckNoNotification(!savedAxes);
				check(metadata("display", "axes").defaultValue("display", List.of()) == !savedAxes,
						"Axes follows live environment-drawing preference");
			} finally { axes.setValueNoCheckNoNotification(savedAxes); }
			var autorun = GamaPreferences.Runtime.CORE_AUTO_RUN;
			boolean savedAutorun = autorun.getValue();
			var autorunFacet = ExperimentFacet.DECLARED.stream().filter(f -> f.name().equals("autorun")).findFirst().orElseThrow();
			try {
				autorun.setValueNoCheckNoNotification(!savedAutorun);
				check(autorunFacet.defaultValue(List.of()) == !savedAutorun, "Autorun follows live runtime preference");
				check(autorunFacet.defaultValue(List.of(new Facet("parent", "base"))) == null,
						"Inherited autorun does not assume preference");
			} finally { autorun.setValueNoCheckNoNotification(savedAutorun); }
			System.out.println("BooleanFacetFieldTest: " + checks + " checks passed");
		} finally { display.dispose(); }
	}
}
