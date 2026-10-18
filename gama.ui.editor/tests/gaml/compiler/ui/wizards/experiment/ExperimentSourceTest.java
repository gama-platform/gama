package gaml.compiler.ui.wizards.experiment;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.text.Document;
import org.eclipse.text.undo.DocumentUndoManagerRegistry;
import org.eclipse.xtext.parser.IParser;

import gaml.compiler.GamlStandaloneSetup;
import gaml.compiler.ui.wizards.species.SpeciesDocumentEdit;
import gaml.compiler.ui.wizards.species.SpeciesDraft;
import gaml.compiler.ui.wizards.species.SpeciesDraft.Facet;

/** Lossless experiment creation/edition in GAML source, single-undo document transactions and stale protection. */
public final class ExperimentSourceTest {
	private static int checks;
	private static IParser parser;

	private static void check(final boolean condition, final String message) {
		if (!condition) throw new AssertionError(message);
		checks++;
	}

	private static void parses(final String text, final String message) {
		var parsed = parser.parse(new java.io.StringReader(text));
		check(!parsed.hasSyntaxErrors(), message + " (parses)\n" + text);
	}

	private static boolean refused(final Runnable action) {
		try {
			action.run();
			return false;
		} catch (IllegalArgumentException expected) {
			return true;
		}
	}

	private static ExperimentDraft with(final ExperimentDraft d, final String name, final String type,
			final List<Facet> facets) {
		return new ExperimentDraft(name, type, facets, d.variables(), d.init(), d.parameters(), d.monitors());
	}

	private static ExperimentDraft variables(final ExperimentDraft d, final List<SpeciesDraft.Variable> variables) {
		return new ExperimentDraft(d.name(), d.type(), d.facets(), variables, d.init(), d.parameters(), d.monitors());
	}

	private static ExperimentDraft parameters(final ExperimentDraft d, final List<ExperimentDraft.Parameter> list) {
		return new ExperimentDraft(d.name(), d.type(), d.facets(), d.variables(), d.init(), list, d.monitors());
	}

	private static ExperimentDraft monitors(final ExperimentDraft d, final List<ExperimentDraft.Monitor> list) {
		return new ExperimentDraft(d.name(), d.type(), d.facets(), d.variables(), d.init(), d.parameters(), list);
	}

	private static ExperimentDraft init(final ExperimentDraft d, final String init) {
		return new ExperimentDraft(d.name(), d.type(), d.facets(), d.variables(), init, d.parameters(), d.monitors());
	}

	private static final List<String> KEPT = List.of("// attributes", "method exhaustive maximize: n;",
			"test t1 { assert n > 0; }", "permanent { display d { } }", "display main { }", "// keep", "/* reps */",
			"reflex tick {}");

	private static String edited(final ExperimentSource source, final ExperimentDraft draft, final String message) {
		String result = source.render(draft);
		parses(result, message);
		for (String kept : KEPT) check(result.contains(kept), message + ": keeps " + kept + "\n" + result);
		return result;
	}

	public static void main(final String[] args) throws Exception {
		parser = GamlStandaloneSetup.doSetup().getInstance(IParser.class);
		String init = "\n\t\twrite \"start\"; // keep\n\t";
		String source = "model sample\n\nglobal {\n\tint n <- 10;\n\tfloat rate <- 0.5;\n}\n\n"
				+ "experiment \"My exp\" type: batch keep_seed: true repeat: 2 /* reps */ until: cycle > 100 {\n"
				+ "\t// attributes\n"
				+ "\tint k <- 1 min: 0 const: true;\n"
				+ "\tfloat w <- 0.1;\n"
				+ "\tparameter \"Count\" var: n min: 1 category: \"C\";\n"
				+ "\tparameter rate_par var: rate slider: false;\n"
				+ "\tmethod exhaustive maximize: n;\n"
				+ "\tinit {" + init + "}\n"
				+ "\ttest t1 { assert n > 0; }\n"
				+ "\tpermanent { display d { } }\n"
				+ "\toutput {\n"
				+ "\t\tmonitor \"Value\" value: n refresh: every(2 #cycle);\n"
				+ "\t\tdisplay main { }\n"
				+ "\t}\n"
				+ "}\n\n"
				+ "experiment simple {\n\treflex tick {}\n}\n";
		parses(source, "fixture");

		// ---- reading
		int header = source.indexOf("experiment \"My exp\"");
		var exp = new ExperimentSource(source, header + 3, parser, false, ExperimentDraft.GUI);
		ExperimentDraft d = exp.draft();
		check(d.name().equals("My exp") && d.type().equals("batch"), "Name and type read: " + d);
		check(d.facets().equals(List.of(new Facet("keep_seed", "true"), new Facet("repeat", "2"),
				new Facet("until", "cycle > 100"))), "Header facets read: " + d.facets());
		check(d.variables().size() == 2 && d.variables().get(0).equals(new SpeciesDraft.Variable("k", "int", "1", "", "",
				"0", "")), "Attributes read: " + d.variables());
		check(d.parameters().equals(List.of(
				new ExperimentDraft.Parameter("Count", "n", List.of(new Facet("min", "1"), new Facet("category", "\"C\""))),
				new ExperimentDraft.Parameter("rate_par", "rate", List.of(new Facet("slider", "false"))))),
				"Parameters read (both forms): " + d.parameters());
		check(d.monitors().equals(List.of(new ExperimentDraft.Monitor("Value", "n",
				List.of(new Facet("refresh", "every(2 #cycle)"))))), "Monitors read: " + d.monitors());
		check(d.init().equals(init), "Init is the exact text between braces");
		check(d.validationError() == null, "Batch monitors and read draft are valid");
		check(exp.globalNames().equals(List.of("n", "rate")), "Global attributes offered: " + exp.globalNames());
		check(exp.isExperimentSelection(header, 10), "Header selection enables edit");
		check(!exp.isExperimentSelection(source.indexOf("write \"start\""), 0), "Caret in body does not edit");
		check(refused(() -> new ExperimentSource(source, source.indexOf("global"), parser, false, "gui")),
				"Outside experiments refused");
		var simple = new ExperimentSource(source, source.indexOf("experiment simple") + 2, parser, false, "gui");
		check(simple.draft().type().equals("gui") && simple.draft().facets().isEmpty()
				&& simple.draft().parameters().isEmpty() && simple.draft().init().isEmpty(), "Implicit gui read");

		// ---- no-op
		check(exp.render(d) == source, "No-op returns the source itself");
		check(exp.render(new ExperimentDraft(d.name(), d.type(), d.facets(), d.variables(), d.init(), d.parameters(),
				d.monitors())) == source, "Equal draft is a no-op");

		// ---- header
		String headerEdit = edited(exp, with(d, d.name(), d.type(), List.of(new Facet("repeat", "3"),
				new Facet("until", "cycle > 100"), new Facet("keep_simulations", "false"))), "Header edit");
		check(headerEdit.contains(
				"experiment \"My exp\" type: batch repeat: 3 /* reps */ until: cycle > 100 keep_simulations: false {"),
				"Facet replaced, removed and appended in place:\n" + headerEdit);
		String gui = edited(exp, with(d, d.name(), "gui", d.facets()), "Type switch");
		check(gui.equals(source.replace("type: batch", "type: gui")), "Type switch keeps every other setting");
		check(with(d, d.name(), "gui", d.facets()).inactiveFacets().equals(List.of("keep_seed", "repeat", "until")),
				"Kept batch facets reported as inactive for gui");
		check(with(d, d.name(), "batch", d.facets()).inactiveFacets().isEmpty(), "No warning for batch");
		String testType = simple.render(with(simple.draft(), "simple", "test", List.of()));
		parses(testType, "Type added");
		check(testType.contains("experiment simple type: test {"), "Type facet appended when implicit");
		String renamed = edited(exp, with(d, "Other exp", d.type(), d.facets()), "Rename quoted");
		check(renamed.equals(source.replace("\"My exp\"", "\"Other exp\"")), "Quoted name stays quoted");
		check(simple.render(with(simple.draft(), "basic", "gui", List.of())).contains("experiment basic {"),
				"Identifier name stays an identifier");
		check(simple.render(with(simple.draft(), "a b", "gui", List.of())).contains("experiment \"a b\" {"),
				"Non-identifier name quoted");
		check(simple.validationError(with(simple.draft(), "My exp", "gui", List.of())) != null,
				"Duplicate experiment name refused");
		check(exp.validationError(d) == null, "Own name is not a duplicate");

		// ---- attributes
		var k = d.variables().get(0);
		var w = d.variables().get(1);
		var k5 = new SpeciesDraft.Variable("kk", "int", "5", "", "", "", "10");
		String attr = edited(exp, variables(d, List.of(k5, w)), "Attribute edit");
		check(attr.contains("\tint kk <- 5 const: true max: 10;\n"),
				"Renamed, init replaced, min removed, max appended, unknown facet kept:\n" + attr);
		var label = new SpeciesDraft.Variable("label", "string", "\"x\"", "", "", "", "");
		String added = edited(exp, variables(d, List.of(k, w, label)), "Attribute added");
		check(added.contains("\tfloat w <- 0.1;\n\tstring label <- \"x\";\n"), "Added after the last attribute:\n" + added);
		String removed = edited(exp, variables(d, List.of(k)), "Attribute removed");
		check(removed.equals(source.replace("\tfloat w <- 0.1;\n", "")), "Removal deletes the whole line");
		String moved = edited(exp, variables(d, List.of(label, k)), "Removal and addition");
		check(!moved.contains("float w") && moved.contains("const: true;\n\tstring label <- \"x\";\n"),
				"Addition anchored on a kept attribute:\n" + moved);
		String none = edited(exp, variables(d, List.of()), "All attributes removed");
		check(!none.contains("int k") && !none.contains("float w"), "All attributes removed");

		// ---- parameters
		var count = d.parameters().get(0);
		var rate = d.parameters().get(1);
		var count2 = new ExperimentDraft.Parameter("Count \"2\"", "n",
				List.of(new Facet("min", "2"), new Facet("category", "\"C\""), new Facet("max", "5")));
		String param = edited(exp, parameters(d, List.of(count2, rate)), "Parameter edit");
		check(param.contains("\tparameter \"Count \\\"2\\\"\" var: n min: 2 category: \"C\" max: 5;\n"),
				"Title quoted/escaped, facet replaced and appended:\n" + param);
		var rateTitled = new ExperimentDraft.Parameter("Rate", "n", List.of());
		String rateEdit = edited(exp, parameters(d, List.of(count, rateTitled)), "Identifier parameter edit");
		check(rateEdit.contains("\tparameter \"Rate\" var: n;\n"), "Identifier title, var and flag patched:\n" + rateEdit);
		var extra = new ExperimentDraft.Parameter("Extra", "w", List.of(new Facet("among", "[0.1, 0.2]")));
		String paramAdded = edited(exp, parameters(d, List.of(count, rate, extra)), "Parameter added");
		check(paramAdded.contains("slider: false;\n\tparameter \"Extra\" var: w among: [0.1, 0.2];\n"),
				"Parameter added after the last one:\n" + paramAdded);
		String paramRemoved = edited(exp, parameters(d, List.of(rate)), "Parameter removed");
		check(paramRemoved.equals(source.replace("\tparameter \"Count\" var: n min: 1 category: \"C\";\n", "")),
				"Parameter line removed");
		String firstParam = simple.render(parameters(simple.draft(), List.of(extra)));
		parses(firstParam, "First parameter");
		check(firstParam.contains("experiment simple {\n\tparameter \"Extra\" var: w among: [0.1, 0.2];\n\treflex tick {}"),
				"First parameter at the start of the block:\n" + firstParam);

		// ---- monitors
		var value = d.monitors().get(0);
		var value2 = new ExperimentDraft.Monitor("Value", "n * 2", List.of(new Facet("color", "#red")));
		String mon = edited(exp, monitors(d, List.of(value2)), "Monitor edit");
		check(mon.contains("\t\tmonitor \"Value\" value: n * 2 color: #red;\n"), "Value replaced, facets patched:\n" + mon);
		var cycles = new ExperimentDraft.Monitor("Cycles", "cycle", List.of());
		String monAdded = edited(exp, monitors(d, List.of(value, cycles)), "Monitor added");
		check(monAdded.contains("every(2 #cycle);\n\t\tmonitor \"Cycles\" value: cycle;\n\t\tdisplay main"),
				"Monitor added after the last one:\n" + monAdded);
		String monRemoved = edited(exp, monitors(d, List.of()), "Monitor removed");
		check(monRemoved.equals(source.replace("\t\tmonitor \"Value\" value: n refresh: every(2 #cycle);\n", "")),
				"Monitor line removed, output kept");
		String monReplaced = edited(exp, monitors(d, List.of()), "Monitor replaced");
		check(monReplaced.contains("output {\n\t\tdisplay main"), "Output and display kept");
		String output = simple.render(monitors(simple.draft(), List.of(cycles)));
		parses(output, "Output created");
		check(output.contains("\treflex tick {}\n\toutput {\n\t\tmonitor \"Cycles\" value: cycle;\n\t}\n}"),
				"Output block created before the closing brace:\n" + output);

		// ---- init
		String code = "\n\t\twrite \"go\";\n\t";
		String initEdit = exp.render(init(d, code));
		parses(initEdit, "Init edit");
		check(initEdit.equals(source.replace("init {" + init + "}", "init {" + code + "}")), "Only the init text changes");
		String initAdded = simple.render(init(simple.draft(), "write 1;"));
		parses(initAdded, "Init added");
		check(initAdded.contains("experiment simple {\n\tinit {\nwrite 1;\n\t}\n\treflex tick {}"),
				"Init block inserted:\n" + initAdded);
		check(simple.render(init(simple.draft(), "  ")) == source, "Blank init without block is a no-op");

		// ---- combined edit keeps order
		var combined = new ExperimentDraft("Combined", "gui", List.of(new Facet("autorun", "true")), List.of(k, label),
				code, List.of(extra), List.of(cycles));
		String all = simple.render(combined);
		parses(all, "Combined edit");
		check(all.contains("experiment Combined autorun: true {\n\tint k <- 1 min: 0;\n\tstring label <- \"x\";\n"
				+ "\tparameter \"Extra\" var: w among: [0.1, 0.2];\n\tinit {" + code + "}\n\treflex tick {}\n\toutput {"),
				"Members added in order:\n" + all);

		// ---- body context
		URI uri = URI.createURI("synthetic:/x/.experiment-wizard.gaml");
		var context = exp.bodyContext(d, uri);
		check(context.text(d.init()).equals(source), "Context reproduces the source around the init code");
		check(context.prefix().endsWith("init {"), "Prefix ends at the init brace");
		var simpleContext = simple.bodyContext(simple.draft(), uri);
		parses(simpleContext.text("write 1;"), "Context adds an init block when absent");

		// ---- creation
		var creation = new ExperimentSource(source, -1, parser, true, ExperimentDraft.GUI);
		check(creation.isCreation() && creation.draft().equals(ExperimentDraft.empty("gui")), "Creation draft");
		var created = new ExperimentDraft("Test all", "test", List.of(new Facet("autorun", "false")),
				List.of(label), "", List.of(), List.of());
		String createdText = creation.render(created);
		parses(createdText, "Creation");
		check(createdText.startsWith(source.stripTrailing()) && createdText.endsWith(
				"\n\nexperiment \"Test all\" type: test autorun: false {\n\tstring label <- \"x\";\n}\n"),
				"Experiment appended:\n" + createdText);
		check(creation.validationError(with(created, "simple", "test", List.of())) != null, "Creation duplicate refused");

		// ---- CRLF
		String crlf = source.replace("\n", "\r\n");
		var crlfSource = new ExperimentSource(crlf, crlf.indexOf("experiment \"My") + 2, parser, false, "gui");
		String crlfEdit = crlfSource.render(monitors(variables(crlfSource.draft(), List.of(k, w, label)),
				List.of(crlfSource.draft().monitors().get(0), cycles)));
		parses(crlfEdit, "CRLF edit");
		check(!crlfEdit.replace("\r\n", "").contains("\n"), "CRLF line endings kept");
		check(crlfSource.render(variables(crlfSource.draft(), List.of(k))).equals(crlf.replace("\tfloat w <- 0.1;\r\n", "")),
				"CRLF removal deletes the whole line");

		// ---- refusals
		check(refused(() -> new ExperimentSource(source + "species {", header + 3, parser, false, "gui")),
				"Syntax errors rejected");
		String weird = "model m\nexperiment e type: foo {}\n";
		check(refused(() -> new ExperimentSource(weird, weird.indexOf("experiment") + 2, parser, false, "gui")),
				"Unknown type edited in source only");
		var bad = monitors(d, List.of(new ExperimentDraft.Monitor("M", " ", List.of())));
		check(exp.validationError(bad) != null, "Monitor value required");
		check(parameters(d, List.of(new ExperimentDraft.Parameter("P", "", List.of()))).validationError() != null,
				"Parameter var required");

		// ---- alignment
		check(java.util.Arrays.equals(ExperimentSource.align(List.of("a", "b", "c"), List.of("a", "x", "c")),
				new int[] { 0, 1, 2 }), "Modified entry paired in place");
		check(java.util.Arrays.equals(ExperimentSource.align(List.of("a", "b", "c"), List.of("a", "c")),
				new int[] { 0, 2 }), "Removed entry unpaired");
		check(java.util.Arrays.equals(ExperimentSource.align(List.of("a", "b"), List.of("a", "b", "z")),
				new int[] { 0, 1, -1 }), "Added entry unpaired");

		// ---- document transactions
		String edit = exp.render(init(d, code));
		Document document = new Document(source);
		DocumentUndoManagerRegistry.connect(document);
		var undo = DocumentUndoManagerRegistry.getDocumentUndoManager(document);
		undo.connect(document);
		var tx = new SpeciesDocumentEdit(document);
		check(!tx.apply(exp.render(d)), "No-op OK leaves document untouched");
		check(!undo.undoable(), "No undo entry for no-op");
		String big = exp.render(new ExperimentDraft("Big", "gui", List.of(), List.of(k5, label), code,
				List.of(count2, extra), List.of(value2, cycles)));
		parses(big, "Multi-part edit");
		check(tx.apply(big) && document.get().equals(big), "OK applies a multi-part edit");
		undo.undo();
		check(document.get().equals(source), "Single undo restores the source");
		check(!undo.undoable(), "Only one undo step");
		var stale = new SpeciesDocumentEdit(document);
		document.replace(0, 0, " ");
		boolean refusedStale = false;
		try {
			stale.apply(edit);
		} catch (IllegalStateException expected) {
			refusedStale = true;
		}
		check(refusedStale && document.get().equals(" " + source), "Stale document refused, not rebased");
		// ---- escaped labels: decoded from both quote styles, re-encoded on change
		String escaped = "model m\nglobal { int n; }\nexperiment e {\n\tparameter \"Say \\\"hi\\\" \\\\ x\" var: n;\n"
				+ "\tparameter 'it\\'s' var: n;\n\toutput { monitor \"A\\\\B\" value: n; }\n}\n";
		ExperimentSource esc = new ExperimentSource(escaped, escaped.indexOf("experiment e") + 2, parser, false, "gui");
		ExperimentDraft escDraft = esc.draft();
		check(escDraft.parameters().get(0).title().equals("Say \"hi\" \\ x"), "Double-quoted escapes decoded");
		check(escDraft.parameters().get(1).title().equals("it's"), "Single-quoted escapes decoded");
		check(escDraft.monitors().get(0).title().equals("A\\B"), "Escaped backslash decoded");
		check(esc.render(escDraft) == escaped, "Escaped labels: no-op");
		ExperimentDraft escChanged = new ExperimentDraft("e", "gui", List.of(), List.of(), "",
				List.of(new ExperimentDraft.Parameter("Say \"bye\" \\ y", "n", List.of()), escDraft.parameters().get(1)),
				escDraft.monitors());
		String escOut = esc.render(escChanged);
		check(escOut.contains("parameter \"Say \\\"bye\\\" \\\\ y\" var: n;") && escOut.contains("'it\\'s'"),
				"Changed label re-encoded, untouched label kept as written");
		check(new ExperimentSource(escOut, escOut.indexOf("experiment e") + 2, parser, false, "gui").draft()
				.equals(escChanged), "Escaped label round trip");
		System.out.println("ExperimentSourceTest: " + checks + " checks passed");
	}
}
