package gaml.compiler.ui.wizards.behavior;

import java.util.List;

import org.eclipse.jface.text.Document;
import org.eclipse.text.undo.DocumentUndoManagerRegistry;
import org.eclipse.xtext.parser.IParser;

import gaml.compiler.GamlStandaloneSetup;
import gaml.compiler.ui.wizards.species.SpeciesDocumentEdit;
import gaml.compiler.ui.wizards.species.SpeciesDraft.Facet;

/** Lossless behaviour creation/edition in GAML source, single-undo document transactions and stale protection. */
public final class BehaviorSourceTest {
	private static int checks;

	private static void check(final boolean condition, final String message) {
		if (!condition) throw new AssertionError(message);
		checks++;
	}

	private static void parses(final IParser parser, final String text, final String message) {
		var parsed = parser.parse(new java.io.StringReader(text));
		check(!parsed.hasSyntaxErrors(), message + " (parses)\n" + text);
	}

	private static BehaviorDraft named(final BehaviorDraft d, final String name) {
		return new BehaviorDraft(d.kind(), name, d.facets(), d.body());
	}

	private static BehaviorDraft body(final BehaviorDraft d, final String body) {
		return new BehaviorDraft(d.kind(), d.name(), d.facets(), body);
	}

	private static boolean refused(final Runnable action) {
		try {
			action.run();
			return false;
		} catch (IllegalArgumentException expected) {
			return true;
		}
	}

	public static void main(final String[] args) throws Exception {
		IParser parser = GamlStandaloneSetup.doSetup().getInstance(IParser.class);
		String code = "\n\t\t// keep comment\n\t\tenergy <- energy - 0.1; /* inline */\n\t";
		String source = "model sample\n\nglobal {\n\tint total <- 0;\n\treflex count {}\n}\n"
				+ "species wolf control: fsm {\n\tfloat energy <- 1.0;\n"
				+ "\treflex move when: /* alive */ energy > 0 {" + code + "}\n"
				+ "\treflex {}\n"
				+ "\tstate idle initial: true {\n\t\twrite 'idle';\n\t}\n"
				+ "\tstate busy final: false;\n"
				+ "\tinit { energy <- 2.0; }\n"
				+ "}\n"
				+ "species worker control: weighted_tasks {\n\ttask work weight: 2.0 { }\n}\n"
				+ "species agent0 skills: [] {\n\tperceive target: wolf in: 10 { }\n"
				+ "\tplan go_home intention: \"home\" { }\n"
				+ "\trule belief: \"rain\" new_desire: \"shelter\";\n}\n"
				+ "experiment main type: gui {\n\treflex tick {}\n}\n";
		parses(parser, source, "fixture");

		// ---- reading
		var move = new BehaviorSource(source, source.indexOf("move when"), parser, false);
		BehaviorDraft d = move.draft();
		check(d.kind() == BehaviorKind.REFLEX && d.name().equals("move"), "Reflex header read");
		check(d.facets().equals(List.of(new Facet("when", "energy > 0"))), "Facet read without comment: " + d.facets());
		check(d.body().equals(code), "Body is exact text between braces");
		check(move.isBehaviorSelection(source.indexOf("reflex move"), 6), "Header selection enables edit");
		check(!move.isBehaviorSelection(source.indexOf("energy <- energy -"), 0), "Caret inside code does not edit");
		var anonymous = new BehaviorSource(source, source.indexOf("reflex {}"), parser, false);
		check(anonymous.draft().name().isEmpty(), "Anonymous reflex read");
		var idle = new BehaviorSource(source, source.indexOf("idle initial"), parser, false);
		check(idle.draft().kind() == BehaviorKind.STATE && idle.draft().name().equals("idle")
				&& idle.draft().facets().equals(List.of(new Facet("initial", "true"))), "State read: " + idle.draft());
		var busy = new BehaviorSource(source, source.indexOf("state busy"), parser, false);
		check(busy.draft().facet("final").equals("false") && busy.draft().body().isEmpty(), "Bodyless state read");
		var work = new BehaviorSource(source, source.indexOf("task work"), parser, false);
		check(work.draft().kind() == BehaviorKind.TASK && work.draft().facet("weight").equals("2.0"), "Task read");
		var perceive = new BehaviorSource(source, source.indexOf("perceive"), parser, false);
		check(perceive.draft().kind() == BehaviorKind.PERCEIVE && perceive.draft().name().isEmpty()
				&& perceive.draft().facet("target").equals("wolf") && perceive.draft().facet("in").equals("10"),
				"Anonymous perceive read: " + perceive.draft());
		var plan = new BehaviorSource(source, source.indexOf("plan go_home"), parser, false);
		check(plan.draft().name().equals("go_home") && plan.draft().facet("intention").equals("\"home\""), "Plan read");
		var rule = new BehaviorSource(source, source.indexOf("rule belief"), parser, false);
		check(rule.draft().kind() == BehaviorKind.RULE && rule.draft().name().isEmpty()
				&& rule.draft().facets().size() == 2, "Rule read: " + rule.draft());
		check(refused(() -> new BehaviorSource(source, source.indexOf("energy <- 2.0"), parser, false)),
				"init blocks are not behaviours");
		check(refused(() -> new BehaviorSource(source, source.indexOf("int total"), parser, false)),
				"Caret outside behaviours refused");
		check(move.siblingNames(BehaviorKind.REFLEX).isEmpty() && idle.siblingNames(BehaviorKind.STATE).equals(
				new java.util.TreeSet<>(List.of("busy"))), "Siblings of the same kind exclude edited one");

		// ---- no-op
		check(move.render(d) == source, "Unchanged draft returns the exact source instance");
		check(move.render(new BehaviorDraft(d.kind(), d.name(), d.facets(), d.body())).equals(source),
				"Equal draft is a no-op");

		// ---- minimal patches
		String renamed = move.render(named(d, "walk"));
		check(renamed.equals(source.replace("reflex move", "reflex walk")), "Rename touches only the name");
		String unnamed = move.render(named(d, ""));
		check(unnamed.equals(source.replace("reflex move when", "reflex when")), "Name removed: " + unnamed);
		parses(parser, unnamed, "anonymous");
		String named = anonymous.render(named(anonymous.draft(), "think"));
		check(named.equals(source.replace("\treflex {}", "\treflex think {}")), "Anonymous gets a name");
		String when = move.render(d.withFacet("when", "energy > 1"));
		check(when.equals(source.replace("energy > 0", "energy > 1"))
				&& when.contains("when: /* alive */ energy > 1"), "Facet value replaced, comment kept");
		String noWhen = move.render(d.withFacet("when", null));
		check(noWhen.equals(source.replace(" when: /* alive */ energy > 0", " /* alive */")), "Facet removed, comment kept: " + noWhen);
		parses(parser, noWhen, "facet removed");
		String added = idle.render(idle.draft().withFacet("final", "true"));
		check(added.equals(source.replace("idle initial: true {", "idle initial: true final: true {")), "Facet appended");
		String bodyless = busy.render(busy.draft().withFacet("initial", "false"));
		check(bodyless.equals(source.replace("busy final: false;", "busy final: false initial: false;")),
				"Facet appended before ';'");
		String state = idle.render(named(idle.draft(), "waiting"));
		check(state.equals(source.replace("state idle", "state waiting")), "State rename");
		String newCode = move.render(body(d, "\n\t\tdo die;\n\t"));
		check(newCode.equals(source.replace(code, "\n\t\tdo die;\n\t")), "Body replaced exactly");
		String block = busy.render(body(busy.draft(), "write 'x';"));
		check(block.equals(source.replace("busy final: false;", "busy final: false {\nwrite 'x';\n\t}")),
				"';' replaced by a block: " + block);
		parses(parser, block, "block added");
		String ruleEdit = rule.render(new BehaviorDraft(BehaviorKind.RULE, "", rule.draft().facets(), "ignored"));
		check(ruleEdit == source || ruleEdit.equals(source), "Rule code is never rendered");
		String rule2 = rule.render(rule.draft().withFacet("priority", "2"));
		check(rule2.equals(source.replace("new_desire: \"shelter\";", "new_desire: \"shelter\" priority: 2;")), "Rule facet appended");
		String perceiveNamed = perceive.render(named(perceive.draft(), "see"));
		check(perceiveNamed.equals(source.replace("perceive target", "perceive see target")), "Perceive named");
		parses(parser, perceiveNamed, "perceive named");
		String perceiveBack = new BehaviorSource(perceiveNamed, perceiveNamed.indexOf("perceive see"), parser, false)
				.render(perceive.draft());
		check(perceiveBack.equals(source), "Perceive name removed again");

		// ---- creation
		var create = new BehaviorSource(source, source.indexOf("float energy"), parser, true);
		check(create.isCreation() && create.draft().equals(BehaviorDraft.empty(BehaviorKind.REFLEX)), "Creation draft");
		String created = create.render(new BehaviorDraft(BehaviorKind.REFLEX, "grow", List.of(new Facet("when", "true")),
				"energy <- energy + 1;"));
		check(created.contains("\tinit { energy <- 2.0; }\n\treflex grow when: true {\nenergy <- energy + 1;\n\t}\n}\nspecies worker"),
				"Reflex created at the end of the owner: " + created);
		parses(parser, created, "created reflex");
		String createdRule = new BehaviorSource(source, source.indexOf("perceive"), parser, true).render(
				new BehaviorDraft(BehaviorKind.RULE, "", List.of(new Facet("belief", "\"sun\"")), "dropped"));
		check(createdRule.contains("\trule belief: \"sun\";\n}\nexperiment"), "Rule created without code");
		var global = new BehaviorSource(source, 0, parser, true);
		check(global.render(BehaviorDraft.empty(BehaviorKind.REFLEX)).contains("\treflex count {}\n\treflex my_reflex {\n\t}\n}"),
				"Global is the default owner");
		String experiment = new BehaviorSource(source, source.indexOf("reflex tick"), parser, true)
				.render(BehaviorDraft.empty(BehaviorKind.REFLEX));
		check(experiment.contains("reflex tick {}\n\treflex my_reflex {\n\t}\n}"), "Experiment owner");
		String crlf = source.replace("\n", "\r\n");
		String createdCrlf = new BehaviorSource(crlf, crlf.indexOf("float energy"), parser, true)
				.render(BehaviorDraft.empty(BehaviorKind.STATE));
		check(createdCrlf.contains("\tstate my_state {\r\n\t}\r\n}") && !createdCrlf.replace("\r\n", "").contains("\n"),
				"CRLF kept");

		// ---- validation
		check(move.validationError(d) == null, "Valid draft");
		check(idle.validationError(named(idle.draft(), "busy")).contains("already exists"), "Duplicate state name");
		check(move.validationError(named(d, "idle")) == null, "Same name of another kind allowed");
		check(idle.validationError(named(idle.draft(), "")) != null, "State name required");
		check(work.validationError(work.draft().withFacet("weight", null)) != null, "Task weight required");
		check(move.validationError(named(d, "2bad")) != null, "Identifier required");

		// ---- contextual body
		var context = move.bodyContext(d, org.eclipse.emf.common.util.URI.createURI("synthetic:/x/.behavior-wizard.gaml"));
		check(context.text(d.body()).equals(source), "Context reproduces the source around the code");
		check(context.prefix().endsWith("reflex move when: /* alive */ energy > 0 {"), "Prefix ends with the header");
		var ruleContext = rule.bodyContext(rule.draft(), org.eclipse.emf.common.util.URI.createURI("synthetic:/x/.b.gaml"));
		check(ruleContext.prefix().equals(source) && ruleContext.suffix().isEmpty(), "Codeless context is the document");

		// ---- document transactions
		Document document = new Document(source);
		DocumentUndoManagerRegistry.connect(document);
		var undo = DocumentUndoManagerRegistry.getDocumentUndoManager(document);
		undo.connect(document);
		var tx = new SpeciesDocumentEdit(document);
		check(!tx.apply(move.render(d)), "No-op OK leaves document untouched");
		check(!undo.undoable(), "No undo entry for no-op");
		check(tx.apply(when) && document.get().equals(when), "OK applies");
		undo.undo();
		check(document.get().equals(source), "Single undo restores source");
		var stale = new SpeciesDocumentEdit(document);
		document.replace(0, 0, " ");
		boolean refusedStale = false;
		try {
			stale.apply(when);
		} catch (IllegalStateException expected) {
			refusedStale = true;
		}
		check(refusedStale && document.get().equals(" " + source), "Stale document refused, not rebased");
		check(refused(() -> new BehaviorSource(source + "species {", 0, parser, true)), "Syntax errors rejected");
		System.out.println("BehaviorSourceTest: " + checks + " checks passed");
	}
}
