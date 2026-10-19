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
		var init = new BehaviorSource(source, source.indexOf("init {"), parser, false);
		check(init.draft().equals(new BehaviorDraft(BehaviorKind.INIT, "", List.of(), " energy <- 2.0; ")),
				"Anonymous init read: " + init.draft());
		check(init.isBehaviorSelection(source.indexOf("init {"), 4), "Init header selection enables edit");
		for (var adapter : List.of(move, anonymous, init))
			check(BehaviorWizardHandler.canEdit(adapter,
					source.indexOf(adapter == move ? "reflex move" : adapter == anonymous ? "reflex {}" : "init {"), 0),
					"Text editor offers lifecycle header: " + adapter.draft().kind());
		for (var adapter : List.of(idle, busy, work, perceive, plan, rule))
			check(!BehaviorWizardHandler.canEdit(adapter, 0, source.length()),
					"Non-lifecycle full declaration selection cannot open generic text editor: " + adapter.draft().kind());
		for (String header : List.of("state idle", "state busy", "task work", "perceive target", "plan go_home",
				"rule belief")) {
			int offset = source.indexOf(header);
			var adapter = new BehaviorSource(source, offset, parser, false);
			check(adapter.isBehaviorSelection(offset, 0) && !BehaviorWizardHandler.canEdit(adapter, offset, 0),
					"Non-lifecycle header is supported by source adapter but not generic text UI: " + header);
		}
		check(!BehaviorWizardHandler.canEdit(move, source.indexOf("energy <- energy -"), 0),
				"Text editor lifecycle filter still excludes body selections");
		check(!init.isBehaviorSelection(source.indexOf("energy <- 2.0"), 0),
				"Caret inside init code is left to the species/experiment form");
		check(refused(() -> new BehaviorSource(source, source.indexOf("int total"), parser, false)),
				"Caret outside behaviours refused");
		check(move.siblingNames(BehaviorKind.REFLEX).isEmpty() && idle.siblingNames(BehaviorKind.STATE).equals(
				new java.util.TreeSet<>(List.of("busy"))), "Siblings of the same kind exclude edited one");

		// ---- no-op
		check(move.render(d) == source, "Unchanged draft returns the exact source instance");
		check(move.render(new BehaviorDraft(d.kind(), d.name(), d.facets(), d.body())).equals(source),
				"Equal draft is a no-op");
		for (BehaviorKind lifecycle : BehaviorKind.LIFECYCLE) {
			for (String condition : List.of("", " when: true", " when: false", " when: cycle > 0")) {
				String fixture = "model m\nglobal {\n\t" + lifecycle.keyword() + condition
						+ " { /* retained body */ }\n}\n";
				var adapter = new BehaviorSource(fixture, fixture.indexOf(lifecycle.keyword()), parser, false);
				check(adapter.render(adapter.draft()) == fixture, "Inline condition no-op source: " + lifecycle + condition);
				String edited = adapter.render(adapter.draft().withFacet("when", "cycle > 1"));
				parses(parser, edited, "Inline condition edit: " + lifecycle + condition);
				check(edited.contains("when: cycle > 1 { /* retained body */ }"), "Condition edit keeps body");
				String omitted = adapter.render(adapter.draft().withFacet("when", null));
				check(omitted.equals("model m\nglobal {\n\t" + lifecycle.keyword() + " { /* retained body */ }\n}\n"),
						"Optional default omission only removes condition");
			}
		}

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

		// ---- lifecycle kinds (reflex / init / abort share one declaration)
		check(BehaviorKind.LIFECYCLE.equals(java.util.EnumSet.of(BehaviorKind.REFLEX, BehaviorKind.INIT, BehaviorKind.ABORT))
				&& BehaviorKind.INIT.convertibleKinds().equals(BehaviorKind.LIFECYCLE)
				&& BehaviorKind.STATE.convertibleKinds().equals(java.util.Set.of(BehaviorKind.STATE))
				&& BehaviorKind.of("abort") == BehaviorKind.ABORT && BehaviorKind.ABORT.ordinal() == 7,
				"Lifecycle contract");
		check(BehaviorDraft.empty(BehaviorKind.INIT).name().isEmpty() && BehaviorDraft.empty(BehaviorKind.ABORT).name().isEmpty()
				&& BehaviorFacet.of(BehaviorKind.ABORT).stream().map(BehaviorFacet::name).toList().equals(List.of("when")),
				"Init/abort anonymous by default, only when");
		String toAbort = move.render(new BehaviorDraft(BehaviorKind.ABORT, d.name(), d.facets(), d.body()));
		check(toAbort.equals(source.replace("reflex move when", "abort move when")), "Reflex -> abort keeps all: " + toAbort);
		parses(parser, toAbort, "abort");
		var abort = new BehaviorSource(toAbort, toAbort.indexOf("abort move"), parser, false);
		check(abort.draft().equals(new BehaviorDraft(BehaviorKind.ABORT, "move", d.facets(), d.body())), "Abort read back");
		check(abort.render(abort.draft()) == toAbort, "Abort no-op");
		check(abort.render(d).equals(source), "Abort -> reflex restores the source");
		String toReflex = init.render(new BehaviorDraft(BehaviorKind.REFLEX, "", List.of(), init.draft().body()));
		check(toReflex.equals(source.replace("\tinit { energy", "\treflex { energy")), "Init -> reflex: " + toReflex);
		String namedInit = init.render(new BehaviorDraft(BehaviorKind.INIT, "setup", List.of(new Facet("when", "true")),
				init.draft().body()));
		check(namedInit.equals(source.replace("\tinit {", "\tinit setup when: true {")), "Named init with when: " + namedInit);
		parses(parser, namedInit, "named init");
		check(init.validationError(BehaviorDraft.empty(BehaviorKind.INIT)) == null, "Several anonymous inits allowed");
		String createdAbort = new BehaviorSource(source, source.indexOf("float energy"), parser, true)
				.render(new BehaviorDraft(BehaviorKind.ABORT, "", List.of(), "\n\t\twrite 'bye';\n\t"));
		check(createdAbort.contains("\tinit { energy <- 2.0; }\n\tabort {\n\t\twrite 'bye';\n\t}\n}"), "Abort created: " + createdAbort);
		parses(parser, createdAbort, "created abort");
		var setup = new BehaviorSource(namedInit, namedInit.indexOf("init setup"), parser, false);
		check(setup.validationError(named(setup.draft(), "setup")) == null, "Own name not a duplicate");
		String twoInits = namedInit.replace("\tinit setup", "\tinit setup {}\n\tinit setup");
		var second = new BehaviorSource(twoInits, twoInits.lastIndexOf("init setup"), parser, false);
		check(second.validationError(second.draft()).contains("already exists"), "Duplicate named init refused");
		check(second.validationError(new BehaviorDraft(BehaviorKind.ABORT, "setup", List.of(), "")).contains("already exists"),
				"Same name across lifecycle kinds refused");
		check(second.validationError(new BehaviorDraft(BehaviorKind.ABORT, "init", List.of(), "")) == null,
				"An init named 'init' is legal next to anonymous ones");

		// ---- validation
		check(move.validationError(d) == null, "Valid draft");
		check(idle.validationError(named(idle.draft(), "busy")).contains("already exists"), "Duplicate state name");
		check(move.validationError(named(d, "idle")).contains("already exists"),
				"Same name of another kind refused (the compiler keeps one behaviour per name)");
		check(move.siblingNames().equals(java.util.Set.of("idle", "busy")) && move.siblingNames(BehaviorKind.STATE)
				.equals(java.util.Set.of("idle", "busy")) && move.siblingNames(BehaviorKind.REFLEX).isEmpty(), "Sibling names");
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
		Document lifecycle = new Document(source);
		DocumentUndoManagerRegistry.connect(lifecycle);
		var lifecycleUndo = DocumentUndoManagerRegistry.getDocumentUndoManager(lifecycle);
		lifecycleUndo.connect(lifecycle);
		var lifecycleTx = new SpeciesDocumentEdit(lifecycle);
		check(!lifecycleTx.apply(init.render(init.draft())) && !lifecycleUndo.undoable(), "Init no-op");
		check(lifecycleTx.apply(toReflex) && lifecycle.get().equals(toReflex), "Init conversion applied");
		lifecycleUndo.undo();
		check(lifecycle.get().equals(source), "Single undo restores the init");
		System.out.println("BehaviorSourceTest: " + checks + " checks passed");
	}
}
