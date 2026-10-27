package gaml.compiler.ui.wizards.action;

import java.util.List;

import org.eclipse.jface.text.Document;
import org.eclipse.text.undo.DocumentUndoManagerRegistry;
import org.eclipse.xtext.parser.IParser;

import gaml.compiler.GamlStandaloneSetup;
import gaml.compiler.ui.wizards.species.SpeciesDocumentEdit;

/** Lossless action creation/edition in GAML source, single-undo document transactions and stale protection. */
public final class ActionSourceTest {
	private static int checks;

	private static void check(final boolean condition, final String message) {
		if (!condition) throw new AssertionError(message);
		checks++;
	}

	private static ActionDraft with(final ActionDraft d, final String name, final String type, final boolean virtual,
			final List<ActionDraft.Argument> args, final String body) {
		return new ActionDraft(name == null ? d.name() : name, type == null ? d.returnType() : type, virtual,
				args == null ? d.arguments() : args, body == null ? d.body() : body);
	}

	private static void parses(final IParser parser, final String text, final String message) {
		var parsed = parser.parse(new java.io.StringReader(text));
		check(!parsed.hasSyntaxErrors(), message + " (parses)\n" + text);
	}

	public static void main(final String[] args) throws Exception {
		IParser parser = GamlStandaloneSetup.doSetup().getInstance(IParser.class);
		String body = "\n\t\t// keep comment\n\t\tenergy <- energy + n; /* inline */\n\t";
		String source = "model sample\n\nglobal {\n\tint total <- 0;\n}\n"
				+ "species wolf {\n\tfloat energy <- 1.0;\n"
				+ "\taction grow(int n /* count */ <- 1, float rate) priority: 2 {" + body + "}\n"
				+ "\tfloat size() { return energy; }\n"
				+ "\taction abstract_one(int x) virtual: true;\n"
				+ "\taction implicit_one();\n"
				+ "\taction typed() type: int { return 1; }\n"
				+ "}\nclass record { int value; }\nexperiment main type: gui {}\n";
		parses(parser, source, "fixture");

		// ---- reading
		var grow = new ActionSource(source, source.indexOf("grow("), parser, false);
		ActionDraft d = grow.draft();
		check(d.name().equals("grow") && d.returnType().equals("action") && !d.virtual(), "Header read");
		check(d.arguments().equals(List.of(new ActionDraft.Argument("n", "int", "1"),
				new ActionDraft.Argument("rate", "float", ""))), "Arguments read in order with defaults: " + d.arguments());
		check(d.body().equals(body), "Body is exact text between braces");
		check(grow.isActionSelection(source.indexOf("action grow"), 6), "Header selection enables edit");
		check(!grow.isActionSelection(source.indexOf("energy <- energy + n"), 0), "Caret inside code does not edit");
		check(new ActionSource(source, source.indexOf("energy <- energy + n"), parser, false).draft().name().equals("grow"),
				"Caret in code still resolves enclosing action");
		var size = new ActionSource(source, source.indexOf("size()"), parser, false);
		check(size.draft().returnType().equals("float") && size.draft().body().equals(" return energy; "), "Typed method read");
		var abs = new ActionSource(source, source.indexOf("abstract_one"), parser, false);
		check(abs.draft().virtual() && abs.draft().body().isEmpty(), "Explicit virtual read");
		var implicit = new ActionSource(source, source.indexOf("implicit_one"), parser, false);
		check(implicit.draft().virtual(), "Bodyless action is implicitly virtual");
		check(grow.keepsBlockWhenVirtual() && !implicit.keepsBlockWhenVirtual()
				&& !new ActionSource(source, source.indexOf("grow("), parser, true).keepsBlockWhenVirtual(),
				"Virtual keeps the block only for existing actions written with one");
		var typed = new ActionSource(source, source.indexOf("action typed"), parser, false);
		check(typed.draft().returnType().equals("int"), "Legacy type: facet read as return type");
		check(grow.siblingActions().equals(new java.util.TreeSet<>(List.of("abstract_one", "implicit_one", "size", "typed"))),
				"Sibling actions exclude edited one: " + grow.siblingActions());
		check(grow.localTypeNames().containsAll(List.of("wolf", "record")), "Local species/classes offered as types");

		// ---- no-op
		check(grow.render(d) == source, "Unchanged draft returns the exact source instance");
		check(grow.render(new ActionDraft(d.name(), d.returnType(), d.virtual(), d.arguments(), d.body())).equals(source),
				"Equal draft is a no-op");

		// ---- minimal patches
		String renamed = grow.render(with(d, "expand", null, false, null, null));
		check(renamed.equals(source.replace("action grow(", "action expand(")), "Rename touches only the name");
		String retyped = grow.render(with(d, null, "float", false, null, null));
		check(retyped.equals(source.replace("action grow(", "float grow(")), "action -> float replaces keyword only");
		parses(parser, retyped, "retyped");
		String back = size.render(with(size.draft(), null, "action", false, null, null));
		check(back.equals(source.replace("float size()", "action size()")), "float -> action");
		String typedChange = typed.render(with(typed.draft(), null, "float", false, null, null));
		check(typedChange.equals(source.replace("type: int", "type: float")), "type: facet edited in place");
		String typedRemoved = typed.render(with(typed.draft(), null, "action", false, null, null));
		check(!typedRemoved.contains("type: int") && typedRemoved.contains("action typed()")
				&& typedRemoved.contains("{ return 1; }"), "type: facet removed for no return");
		parses(parser, typedRemoved, "typed removed");

		var a = d.arguments();
		String argType = grow.render(with(d, null, null, false,
				List.of(new ActionDraft.Argument("n", "float", "1"), a.get(1)), null));
		check(argType.equals(source.replace("(int n /*", "(float n /*")), "Argument type patched, comment kept");
		String argDefault = grow.render(with(d, null, null, false,
				List.of(new ActionDraft.Argument("n", "int", "2"), new ActionDraft.Argument("rate", "float", "0.5")), null));
		check(argDefault.contains("int n /* count */ <- 2, float rate <- 0.5)"), "Default changed and added in place");
		parses(parser, argDefault, "defaults");
		String argNoDefault = grow.render(with(d, null, null, false,
				List.of(new ActionDraft.Argument("n", "int", ""), a.get(1)), null));
		String header = argNoDefault.substring(argNoDefault.indexOf("grow"), argNoDefault.indexOf("priority"));
		check(header.contains("/* count */") && !header.contains("<-"), "Default removed, comment kept: " + header);
		parses(parser, argNoDefault, "no default");
		String reordered = grow.render(with(d, null, null, false, List.of(a.get(1), a.get(0)), null));
		check(reordered.contains("action grow(float rate, int n <- 1) priority: 2 {" + body + "}"),
				"Reorder regenerates only the argument list");
		String added = grow.render(with(d, null, null, false,
				List.of(a.get(0), a.get(1), new ActionDraft.Argument("p", "list<point>", "[]")), null));
		check(added.contains("(int n <- 1, float rate, list<point> p <- [])"), "Argument added");
		parses(parser, added, "added");

		String newBody = "\n\t\tenergy <- 0.0;\n\t";
		String rebodied = grow.render(with(d, null, null, false, null, newBody));
		check(rebodied.equals(source.replace(body, newBody)), "Only the code region changes");

		// ---- virtual toggles
		String virtualOn = grow.render(with(d, null, null, true, null, null));
		check(virtualOn.contains("action grow(int n /* count */ <- 1, float rate) virtual: true priority: 2 {" + body + "}"),
				"Virtual on keeps code and other facets");
		parses(parser, virtualOn, "virtual on");
		String virtualOff = abs.render(with(abs.draft(), null, null, false, null, "\n\t\tx <- 1;\n\t"));
		String offHeader = virtualOff.substring(virtualOff.indexOf("abstract_one"), virtualOff.indexOf("action implicit_one"));
		check(offHeader.equals("abstract_one(int x) {\n\t\tx <- 1;\n\t}\n\t"),
				"Virtual off adds block: " + offHeader);
		parses(parser, virtualOff, "virtual off");
		String implicitOff = implicit.render(with(implicit.draft(), null, null, false, null, ""));
		check(implicitOff.contains("action implicit_one() {\n\t}"), "Implicit virtual becomes concrete with empty block");
		parses(parser, implicitOff, "implicit off");
		var vOn = new ActionSource(virtualOn, virtualOn.indexOf("grow("), parser, false);
		check(vOn.draft().virtual() && vOn.draft().body().equals(body), "Virtual action keeps inactive code");
		check(vOn.render(with(vOn.draft(), null, null, false, null, null)).equals(source),
				"Virtual round trip restores the declaration");

		// ---- validation
		check(grow.validationError(with(d, "size", null, false, null, null)) != null, "Name clash with sibling rejected");
		check(grow.validationError(with(d, "9x", null, false, null, null)) != null, "Invalid name rejected");
		check(grow.validationError(with(d, null, "list<int", false, null, null)) != null, "Invalid return type rejected");
		check(grow.validationError(with(d, null, null, false,
				List.of(a.get(0), new ActionDraft.Argument("n", "int", "")), null)) != null, "Duplicate argument rejected");
		check(grow.validationError(d) == null, "Unchanged valid");

		// ---- creation
		var inWolf = new ActionSource(source, source.indexOf("float energy"), parser, true);
		check(inWolf.isCreation() && inWolf.draft().equals(ActionDraft.empty()), "Creation draft");
		var created = new ActionDraft("hunt", "bool", false, List.of(new ActionDraft.Argument("prey", "record", "")),
				"\n\t\treturn true;\n\t");
		String withHunt = inWolf.render(created);
		check(withHunt.contains("\taction typed() type: int { return 1; }\n\tbool hunt(record prey) {\n\t\treturn true;\n\t}\n}\nclass record"),
				"Created in caret species before closing brace");
		parses(parser, withHunt, "created");
		check(inWolf.validationError(new ActionDraft("grow", "action", false, List.of(), "")) != null,
				"Creation clash rejected");
		var inGlobal = new ActionSource(source, source.indexOf("model sample"), parser, true);
		String virt = inGlobal.render(new ActionDraft("todo", "int", true, List.of(), "ignored"));
		check(virt.contains("\tint total <- 0;\n\tint todo() virtual: true;\n}"), "Virtual creation in global is bodyless");
		parses(parser, virt, "virtual creation");
		var inExperiment = new ActionSource(source, source.indexOf("main type"), parser, true);
		String inExp = inExperiment.render(ActionDraft.empty());
		check(inExp.contains("experiment main type: gui {\n\taction my_action() {\n\t}\n}"), "Created in one-line experiment block: " + inExp.substring(inExp.indexOf("experiment")));
		parses(parser, inExp, "experiment creation");

		// ---- body context
		var uri = org.eclipse.emf.common.util.URI.createURI("synthetic:/x/.action-wizard.gaml");
		var ctx = grow.bodyContext(with(d, null, "int", false, null, "ignored"), uri);
		check(ctx.prefix().endsWith("int grow(int n /* count */ <- 1, float rate) priority: 2 {")
				&& ctx.suffix().startsWith("}\n\tfloat size()"), "Context uses rendered signature around body");
		check(ctx.text(body).equals(source.replace("action grow(", "int grow(")), "Context + body = rendered document");
		var vctx = abs.bodyContext(with(abs.draft(), null, null, true, null, ""), uri);
		check(!vctx.prefix().substring(vctx.prefix().lastIndexOf("abstract_one")).contains("virtual"),
				"Context of virtual draft is a concrete wrapper");

		// ---- document transactions
		Document document = new Document(source);
		DocumentUndoManagerRegistry.connect(document);
		var undo = DocumentUndoManagerRegistry.getDocumentUndoManager(document);
		undo.connect(document);
		var tx = new SpeciesDocumentEdit(document);
		check(!tx.apply(grow.render(d)), "No-op OK leaves document untouched");
		check(!undo.undoable(), "No undo entry for no-op");
		check(tx.apply(argDefault) && document.get().equals(argDefault), "OK applies");
		undo.undo();
		check(document.get().equals(source), "Single undo restores source");
		undo.redo();
		check(document.get().equals(argDefault), "Redo");
		var stale = new SpeciesDocumentEdit(document);
		document.replace(0, 0, " ");
		boolean refused = false;
		try {
			stale.apply(argDefault + "x");
		} catch (IllegalStateException expected) {
			refused = true;
		}
		check(refused && document.get().equals(" " + argDefault), "Stale document refused, not rebased");

		// ---- errors
		boolean error = false;
		try {
			new ActionSource(source, source.indexOf("int total"), parser, false);
		} catch (IllegalArgumentException expected) {
			error = true;
		}
		check(error, "Edit outside an action rejected");
		error = false;
		try {
			new ActionSource("model x\nglobal { action a( { }", 20, parser, false);
		} catch (IllegalArgumentException expected) {
			error = true;
		}
		check(error, "Syntax errors rejected");
		System.out.println("ActionSourceTest: " + checks + " checks passed");
	}
}
