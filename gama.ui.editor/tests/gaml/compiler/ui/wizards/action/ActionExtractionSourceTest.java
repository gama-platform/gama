package gaml.compiler.ui.wizards.action;

import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.text.Document;
import org.eclipse.text.undo.DocumentUndoManagerRegistry;
import org.eclipse.xtext.parser.IParser;

import gaml.compiler.GamlStandaloneSetup;
import gaml.compiler.ui.wizards.species.SpeciesDocumentEdit;

/** AST statement boundaries, snapshot extraction, caller validation context and a single document transaction. */
public final class ActionExtractionSourceTest {
	private static int checks;

	private static void check(final boolean condition, final String message) {
		checks++;
		if (!condition) throw new AssertionError(message);
	}

	private static ActionDraft named(final ActionDraft draft, final String name) {
		return new ActionDraft(name, draft.returnType(), draft.virtual(), draft.arguments(), draft.body());
	}

	private static void parses(final IParser parser, final String source) {
		check(!parser.parse(new java.io.StringReader(source)).hasSyntaxErrors(), "Generated source parses:\n" + source);
	}

	private static ActionExtractionSource selection(final String source, final String selected, final IParser parser) {
		return new ActionExtractionSource(source, source.indexOf(selected), selected.length(), parser);
	}

	public static void main(final String[] args) throws Exception {
		IParser parser = GamlStandaloneSetup.doSetup().getInstance(IParser.class);
		for (String owner : List.of("global", "species wolf", "grid cells width: 2 height: 2", "class sample", "experiment e type: gui")) {
			for (String nl : List.of("\n", "\r\n")) {
				String selected = "\t\t// moved 'quoted }' comment" + nl
						+ "\t\twrite \"braces { and }\"; /* end */" + nl
						+ "\t\twrite 'single } quoted';" + nl;
				String before = "model m" + nl + owner + " {" + nl + "\treflex r {" + nl;
				String after = "\t\twrite 'keep caller';" + nl + "\t}" + nl + "}" + nl;
				String source = before + selected + after;
				parses(parser, source);
				var extraction = selection(source, selected, parser);
				check(extraction.draft().body().equals(selected), "Body prefilled byte for byte for " + owner);
				ActionDraft draft = named(extraction.draft(), "extracted");
				check(extraction.validationError(draft, true) == null, "Complete statements extract for " + owner);
				String rendered = extraction.render(draft, true);
				check(rendered.startsWith(before + "\t\tdo extracted;" + nl + after.substring(0, after.indexOf("}"))),
						"Caller replaced without duplicate indentation/newlines");
				check(rendered.contains("action extracted()") && rendered.contains(selected), "Selected code moved into action");
				check(rendered.indexOf("write 'single } quoted'") == rendered.lastIndexOf("write 'single } quoted'"),
						"Selected statement occurs exactly once");
				check(rendered.contains("write 'keep caller';"), "Unselected caller preserved");
				parses(parser, rendered);
				var context = extraction.bodyContext(draft, true, URI.createURI("synthetic:/extraction.gaml"));
				check(context.text(draft.body()).equals(rendered), "Context includes generated action and replaced caller");
				check(context.prefix().contains("do extracted;") && context.suffix().contains("}"), "Validation sees call site");
				String separate = extraction.render(new ActionDraft("separate", "action", false, List.of(), "write 'other';"), false);
				check(separate.startsWith(before + selected + after.substring(0, after.indexOf("}"))),
						"Selection stays byte-identical when excluded");
				check(separate.contains("write 'other';"), "Excluded mode creates independent body");
				parses(parser, separate);
			}
		}
		String source = "model m\nglobal {\n int total;\n action my_action() {}\n reflex r {\n"
				+ "  int n <- 2;\n  total <- total + n;\n  write n;\n }\n}\n";
		var extraction = selection(source, "total <- total + n;", parser);
		check(extraction.draft().name().equals("my_action_2"), "Initial action name avoids sibling collision");
		ActionDraft draft = new ActionDraft("increase", "action", false,
				List.of(new ActionDraft.Argument("n", "int", "")), extraction.draft().body());
		String rendered = extraction.render(draft, true);
		check(rendered.contains("do increase (n: n);") && rendered.contains("action increase(int n)"),
				"Required argument passed from matching caller local");
		parses(parser, rendered);
		draft = new ActionDraft("increase", "action", false,
				List.of(new ActionDraft.Argument("n", "int", "1")), extraction.draft().body());
		check(extraction.render(draft, true).contains("do increase;"), "Defaulted argument omitted at call site");
		for (String body : List.of("", "   ", "\n\t", "/* only a comment */", "write 'changed';")) {
			draft = new ActionDraft("increase", "action", false, List.of(), body);
			for (boolean include : List.of(true, false)) {
				var context = extraction.bodyContext(draft, include, URI.createURI("synthetic:/extraction.gaml"));
				parses(parser, context.text(body));
				check(context.prefix().contains(include ? "do increase;" : "total <- total + n;"),
						"Edited/blank body validation retains the appropriate caller");
				if (!body.isBlank())
					check(context.text(body).equals(extraction.render(draft, include)), "Edited body context matches final source");
			}
		}
		String otherOwner = "model m\nglobal {action shared() {}}\nspecies s {reflex r {write 'x';}}\n";
		var sameName = selection(otherOwner, "write 'x';", parser);
		var sameDraft = named(sameName.draft(), "shared");
		check(sameName.bodyContext(sameDraft, true, URI.createURI("synthetic:/extraction.gaml")).text(sameDraft.body())
				.equals(sameName.render(sameDraft, true)), "Context targets the new action, not a same-name action in another owner");
		check(extraction.validationError(named(extraction.draft(), "my_action"), true).contains("already exists"),
				"Duplicate action name rejected");
		check(extraction.validationError(new ActionDraft("v", "action", true, List.of(), ""), true).contains("virtual"),
				"Virtual extraction blocked");
		check(extraction.validationError(new ActionDraft("v", "action", true, List.of(), ""), false) == null,
				"Virtual separate creation allowed");
		for (String selected : List.of("total + n", "total <- total", "n <- 2;", "write n", "int total;")) {
			var invalid = selection(source, selected, parser);
			check(invalid.validationError(invalid.draft(), true) != null, "Unsafe/partial selection rejected: " + selected);
			check(invalid.validationError(invalid.draft(), false) == null, "Can opt out of invalid selection");
		}
		for (String code : List.of("return 1;", "loop while: true { break; }", "loop i from: 0 to: 1 { continue; }")) {
			String control = "model m\nglobal { int f() { " + code + " } }\n";
			parses(parser, control);
			var invalid = selection(control, code, parser);
			check(invalid.validationError(invalid.draft(), true).contains("cannot be moved safely"),
					"Control-transfer semantics protected: " + code);
		}
		String drawing = "model m\nspecies s { aspect default { draw circle(1); } }\n";
		var invalid = selection(drawing, "draw circle(1);", parser);
		check(invalid.validationError(invalid.draft(), true).contains("Drawing/output"), "Drawing code cannot become an action");
		String nested = "model m\nglobal {reflex r {loop times: 2 {write 'x'; write 'y';}}}\n";
		var valid = selection(nested, "write 'x'; write 'y';", parser);
		check(valid.validationError(valid.draft(), true) == null, "Complete nested sibling statements allowed");
		parses(parser, valid.render(valid.draft(), true));
		String commented = "model m\nglobal {reflex r {write 'x'; /*partial comment*/ write 'y';}}\n";
		invalid = selection(commented, "write 'x'; /*partial", parser);
		check(invalid.validationError(invalid.draft(), true).contains("comment"), "Cannot split a comment token");

		var document = new Document(source);
		DocumentUndoManagerRegistry.connect(document);
		var undo = DocumentUndoManagerRegistry.getDocumentUndoManager(document);
		undo.connect(document);
		try {
			SpeciesDocumentEdit tx = new SpeciesDocumentEdit(document);
			check(document.get().equals(source) && !undo.undoable(), "Opening/cancelling extraction has no live edit");
			String generated = extraction.render(named(extraction.draft(), "increase"), true);
			check(tx.apply(generated), "Combined removal/call/insertion applied once");
			undo.undo();
			check(document.get().equals(source) && !undo.undoable(), "One undo restores source byte for byte");
			undo.redo();
			check(document.get().equals(generated), "One redo restores entire extraction");
			tx = new SpeciesDocumentEdit(document);
			document.replace(0, 0, "// external change\n");
			try {
				tx.apply(source);
				throw new AssertionError("Stale extraction accepted");
			} catch (IllegalStateException expected) {
				check(document.get().startsWith("// external change"), "Stale snapshot refuses to overwrite current document");
			}
		} finally {
			undo.disconnect(document);
			DocumentUndoManagerRegistry.disconnect(document);
		}
		System.out.println("ActionExtractionSourceTest: " + checks + " checks passed");
	}
}
