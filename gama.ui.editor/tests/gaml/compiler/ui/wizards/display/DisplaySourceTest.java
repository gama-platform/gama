package gaml.compiler.ui.wizards.display;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.Document;
import org.eclipse.text.undo.DocumentUndoManagerRegistry;
import org.eclipse.xtext.parser.IParser;

import gaml.compiler.GamlStandaloneSetup;
import gaml.compiler.ui.wizards.species.SpeciesDocumentEdit;
import gaml.compiler.ui.wizards.species.SpeciesDraft.Facet;

/** Source-backed ordered layers, partial edits, raw preservation and document transactions. */
public final class DisplaySourceTest {
	private static int checks;
	private static void check(final boolean value, final String message) {
		checks++;
		if (!value) throw new AssertionError(message);
	}
	private static void refuses(final Runnable action) {
		try { action.run(); } catch (IllegalArgumentException expected) { checks++; return; }
		throw new AssertionError("Expected an explicit parse/structure error.");
	}
	private static DisplayDraft layers(final DisplayDraft d, final List<LayerDraft> layers) {
		return new DisplayDraft(d.name(), d.renderer(), d.facets(), layers, d.trailingSource());
	}
	private static LayerDraft expression(final LayerDraft d, final String value) {
		return new LayerDraft(d.id(), d.kind(), value, d.facets(), d.textFacets(), d.source());
	}
	public static void main(final String[] args) throws Exception {
		IParser parser = GamlStandaloneSetup.doSetup().getInstance(IParser.class);
		String body = "\n\t// wolves\n\tspecies wolf aspect: default visible: cycle > 0;\n"
				+ "\tagents \"chosen\" value: wolf where (each.age > 1) trace: 3;\n"
				+ "\tgrid cells elevation: true;\n\timage \"pic.png\" refresh: false;\n"
				+ "\tgraphics \"caption\" visible: true { draw \"Hi \\\"there\\\"\" at: {0,0} color: #red; }\n"
				+ "\t// keep chart\n\tchart \"series\" type: series { data \"time\" value: cycle; }\n"
				+ "\tgraphics complicated { draw circle(1); draw \"More\"; }\n"
				+ "\tevent 'mouse_down' { write 'click'; }\n\t// trailing\n";
		String source = "model m\nspecies wolf { int age; aspect default { draw circle(1); } }\n"
				+ "species other parent: wolf {}\ngrid cells width: 2 height: 2 {}\n"
				+ "experiment e type: gui { output {\n"
				+ "display 'My display' type: /* renderer */ opengl background: /* color */ #black {" + body + "}\n"
				+ "monitor \"c\" value: cycle;\n} }\n";
		var adapter = new DisplaySource(source, source.indexOf("display 'My"), parser, false);
		var draft = adapter.draft();
		check(draft.name().equals("My display") && draft.renderer().equals("opengl"), "Header decoded");
		check(draft.layers().stream().map(LayerDraft::kind).toList().equals(List.of(LayerKind.SPECIES,
				LayerKind.AGENTS, LayerKind.GRID, LayerKind.IMAGE, LayerKind.TEXT, LayerKind.RAW, LayerKind.RAW, LayerKind.RAW)),
				"Kinds: " + draft.layers());
		check(draft.validationError() == null, "Valid detached draft: " + draft.validationError());
		check(adapter.render(draft) == source, "No-op same instance");
		check(DisplaySource.renderBody(draft.layers(), draft.trailingSource(), parser).equals(body), "Body exact round trip");
		var parsed = DisplaySource.parseBody(body, parser);
		check(DisplaySource.renderBody(parsed.layers(), parsed.trailingSource(), parser).equals(body), "Detached body round trip");
		check(adapter.speciesNames().equals(List.of("cells", "other", "wolf")) && adapter.gridNames().equals(List.of("cells")), "Suggestions");
		check(adapter.isDisplaySelection(source.lastIndexOf("circle(1)"), 0), "Edit anywhere inside display");
		check(draft.layers().get(0).source().startsWith("\n\t// wolves"), "Leading comments attached");
		check(draft.trailingSource().equals("\n\t// trailing\n"), "Trailing source exact");
		String changedHeader = adapter.render(new DisplayDraft("Renamed", "java2D", List.of(new Facet("background", "#white")),
				draft.layers(), draft.trailingSource()));
		check(changedHeader.equals(source.replace("'My display'", "Renamed").replace("opengl", "java2D").replace("#black", "#white")),
				"Header edit does not change layers or comments:\n" + changedHeader);
		var wolf = draft.layers().getFirst();
		var edited = expression(wolf, "other");
		String changedLayer = DisplaySource.renderLayer(edited, parser);
		check(changedLayer.equals(wolf.source().replace("species wolf", "species other")), "Single layer token edit");
		List<LayerDraft> reordered = new ArrayList<>(draft.layers());
		reordered.removeFirst();
		reordered.add(4, edited);
		String moved = adapter.render(layers(draft, reordered));
		check(moved.contains(edited.source().substring(0, edited.source().indexOf("species")) + "species other"), "Move carries comment");
		check(!parser.parse(new StringReader(moved)).hasSyntaxErrors(), "Moved document parses");
		check(reordered.get(4).id().equals(wolf.id()), "Move retains identity");
		var copy = wolf.duplicate();
		check(!copy.id().equals(wolf.id()) && copy.source().equals(wolf.source()), "Duplicate gets identity and retains formatting");
		var text = draft.layers().get(4);
		check(text.expression().equals("\"Hi \\\"there\\\"\"") && DisplayDraft.find(text.facets(), "name").equals("\"caption\""),
				"Text expression is raw GAML");
		check(DisplaySource.renderLayer(expression(text, "\"Changed\""), parser)
				.equals(text.source().replace("\"Hi \\\"there\\\"\"", "\"Changed\"")), "Text edit keeps outer settings");
		for (var kind : LayerKind.values()) if (kind != LayerKind.RAW) {
			var empty = LayerDraft.empty(kind);
			String generated = DisplaySource.renderLayer(empty, parser);
			if (kind == LayerKind.AGENTS) empty = new LayerDraft(empty.id(), kind, "", List.of(new Facet("value", "wolf")), List.of(), "");
			else if (kind == LayerKind.SPECIES || kind == LayerKind.GRID) empty = expression(empty, kind == LayerKind.GRID ? "cells" : "wolf");
			else if (kind == LayerKind.IMAGE) empty = expression(empty, "\"pic.png\"");
			generated = DisplaySource.renderLayer(empty, parser);
			check(!DisplaySource.parseBody("\n" + generated + "\n", parser).layers().isEmpty(), "New " + kind + " parses");
			check(empty.validationError() == null, "New " + kind + " valid: " + empty.validationError());
		}
		for (String fragment : List.of("/*comment*/ species species: wolf; //tail",
				"species wolf { species child; }", "chart c type: series { data x value: cycle; }", "graphics g {draw circle(1);}")) {
			var layer = DisplaySource.parseLayer(fragment, "stable", parser);
			check(DisplaySource.renderLayer(layer, parser).equals(fragment), "Fragment exact: " + fragment);
		}
		refuses(() -> DisplaySource.parseBody("species ; invalid ???", parser));
		refuses(() -> DisplaySource.parseLayer("species wolf; grid cells;", "x", parser));
		refuses(() -> new DisplaySource(source, 0, parser, true));
		refuses(() -> DisplaySource.parseBody("} } } experiment injected type: gui { output { display hacked {", parser));
		check(DisplaySource.parseBody(" // only comment", parser).layers().isEmpty(), "Comment-only body");
		check(DisplaySource.parseBody("", parser).trailingSource().isEmpty(), "Empty body");
		var terminal = DisplaySource.parseBody("species wolf; // tail", parser);
		check(terminal.layers().getFirst().source().equals("species wolf; // tail") && terminal.trailingSource().isEmpty(),
				"Synthetic wrapper newline never leaks into a terminal comment row");
		var inline = DisplaySource.parseBody("species wolf; // wolves\nspecies other;\n", parser);
		check(inline.layers().getFirst().source().contains("// wolves") && !inline.layers().getLast().source().contains("// wolves"),
				"Inline comment is attached to its own row: " + inline.layers());
		var inlineMoved = new ArrayList<>(inline.layers());
		java.util.Collections.reverse(inlineMoved);
		String inlineBody = DisplaySource.renderBody(inlineMoved, inline.trailingSource(), parser);
		check(inlineBody.contains("species wolf; // wolves") && DisplaySource.parseBody(inlineBody, parser).layers().size() == 2,
				"Inline comment moves with its row");
		check(DisplaySource.renderBody(inline.layers(), inline.trailingSource(), parser).equals("species wolf; // wolves\nspecies other;\n"),
				"Inline comments round trip exactly");
		for (String first : List.of("species wolf; // trailing comment", "graphics {draw 'x';} // trailing comment")) {
			var rows = List.of(DisplaySource.parseLayer(first, "first", parser),
					DisplaySource.parseLayer("species wolf;", "second", parser));
			String safe = DisplaySource.renderBody(rows, "", parser);
			check(DisplaySource.parseBody(safe, parser).layers().size() == 2, "A // comment must not swallow row 2");
			check(safe.contains("// trailing comment\nspecies wolf;"), "Only the necessary separator is added: " + safe);
			String finalComment = DisplaySource.renderBody(rows.subList(0, 1), "", parser);
			check(finalComment.endsWith("\n") && DisplaySource.parseBody(finalComment, parser).layers().size() == 1,
					"Closing brace cannot be swallowed");
		}
		for (String safe : List.of("species wolf; species wolf;", "image \"http://host/image\";species wolf;",
				"species wolf; /* // not a line comment */ species wolf;",
				"species wolf; // comment\n species wolf;")) {
			var safeBody = DisplaySource.parseBody(safe, parser);
			check(DisplaySource.renderBody(safeBody.layers(), safeBody.trailingSource(), parser).equals(safe),
					"Safe boundaries preserved byte for byte: " + safe);
		}
		String windows = body.replace("\n", "\r\n");
		var windowsBody = DisplaySource.parseBody(windows, parser);
		var windowsRows = new ArrayList<>(windowsBody.layers());
		windowsRows.add(expression(LayerDraft.empty(LayerKind.SPECIES), "wolf"));
		String addedWindows = DisplaySource.renderBody(windowsRows, windowsBody.trailingSource(), parser);
		check(!addedWindows.replace("\r\n", "").contains("\n"), "New row uses surrounding CRLF");
		String aspects = source.replace("int age;", "int age; aspect drawing {draw circle(1);}");
		check(new DisplaySource(aspects, aspects.indexOf("display 'My"), parser, false).aspectNames("wolf")
				.equals(List.of("default", "drawing")), "Source-visible aspect suggestions");
		String escaped = source.replace("'My display'", "\"People's \\\"view\\\"\"");
		check(new DisplaySource(escaped, escaped.indexOf("display "), parser, false).draft().name().equals("People's \"view\""),
				"Display labels decode quotes and escapes");
		String created = new DisplaySource(source, source.indexOf("monitor"), parser, true).render(DisplayDraft.empty());
		check(created.contains("display my_display") && created.contains("monitor \"c\" value: cycle;"), "Create in existing output");
		check(created.indexOf("output {") == created.lastIndexOf("output {"), "Existing output is reused, not duplicated");
		check(!parser.parse(new StringReader(created)).hasSyntaxErrors(), "Created model parses");
		check(created.contains("type: java2D"), "Source creation explicitly selects 2D rather than renderer preference");
		String settingsSource = source.replace("opengl background:", "'3d' camera: /* keep camera */ \"From above\" "
				+ "z_near: 0.1 z_far: 1000.0 axes: false fullscreen: 1 background:");
		var settingsAdapter = new DisplaySource(settingsSource, settingsSource.indexOf("display 'My"), parser, false);
		var settingsDraft = settingsAdapter.draft();
		check(settingsAdapter.render(settingsDraft) == settingsSource, "Loaded camera/clipping/monitor values are exact no-op");
		var settingsFacets = new ArrayList<>(settingsDraft.facets());
		settingsFacets.replaceAll(f -> f.name().equals("z_near") ? new Facet("z_near", "0.25") : f);
		String settingsEdit = settingsAdapter.render(new DisplayDraft(settingsDraft.name(), "java2D", settingsFacets,
				settingsDraft.layers(), settingsDraft.trailingSource()));
		check(settingsEdit.equals(settingsSource.replace("'3d'", "java2D").replace("z_near: 0.1", "z_near: 0.25")),
				"Switch to 2D/clipping edit preserves hidden camera, expressions, comments and every layer");
		check(!parser.parse(new StringReader(settingsEdit)).hasSyntaxErrors(), "Reorganized settings output parses");
		String noOutput = "model n\nexperiment e type: gui { int v; }\n";
		created = new DisplaySource(noOutput, noOutput.indexOf("int v"), parser, true).render(DisplayDraft.empty());
		check(!parser.parse(new StringReader(created)).hasSyntaxErrors() && created.contains("output {"), "Creates output block");
		String permanent = "model n\nexperiment e type: batch { output {} permanent { monitor 'm' value: cycle; } }\n";
		String inPermanent = new DisplaySource(permanent, permanent.indexOf("monitor"), parser, true).render(DisplayDraft.empty());
		check(inPermanent.contains("output {} permanent") && inPermanent.indexOf("display my_display") > inPermanent.indexOf("permanent"),
				"Selected permanent output receives the display");
		String global = "model n\nglobal {output {}}\nexperiment e type: gui {}\n";
		refuses(() -> new DisplaySource(global, global.indexOf("output"), parser, true));
		try {
			new DisplaySource(global, global.indexOf("global"), parser, true);
			throw new AssertionError("Global display creation accepted");
		} catch (IllegalArgumentException expected) {
			check(!expected.getMessage().contains("global") && expected.getMessage().contains("inside an experiment")
					&& expected.getMessage().contains("output section will be created"),
					"Creation error explains experiment/output placement without suggesting global");
		}
		String locations = "model locations\nglobal { int value; }\nspecies s { reflex r {} }\n"
				+ "experiment first type: gui { init {} output { monitor 'm' value: cycle; } }\n"
				+ "experiment second type: gui { int other; }\n";
		var root = parser.parse(new StringReader(locations)).getRootASTElement();
		for (String outside : List.of("model locations", "global", "int value", "species s", "reflex r")) {
			int offset = locations.indexOf(outside);
			check(!DisplaySource.canCreate(root, offset), "Add display unavailable outside experiment: " + outside);
			refuses(() -> new DisplaySource(locations, offset, parser, true));
		}
		check(!DisplaySource.canCreate(null, 0) && !DisplaySource.canCreate(root, -1)
				&& !DisplaySource.canCreate(root, locations.length()),
				"Absent/outside caret never enables Add display");
		for (String inside : List.of("experiment first", "init {}", "monitor 'm'", "experiment second", "int other")) {
			int offset = locations.indexOf(inside);
			check(DisplaySource.canCreate(root, offset), "Add display available in experiment: " + inside);
			String generated = new DisplaySource(locations, offset, parser, true).render(DisplayDraft.empty());
			check(!parser.parse(new StringReader(generated)).hasSyntaxErrors(), "Creation at experiment location parses");
			check(generated.indexOf("display my_display") > generated.indexOf(
					inside.contains("second") || inside.equals("int other") ? "experiment second" : "experiment first"),
					"Display created in selected experiment");
			if (!inside.contains("second") && !inside.equals("int other"))
				check(generated.indexOf("display my_display") < generated.indexOf("experiment second"),
						"Creation in first experiment never modifies a different experiment");
		}
		check(created.contains("int v;") && created.contains("output {"), "Missing output created without losing experiment content");
		Document creationDocument = new Document(noOutput);
		DocumentUndoManagerRegistry.connect(creationDocument);
		var creationUndo = DocumentUndoManagerRegistry.getDocumentUndoManager(creationDocument);
		creationUndo.connect(creationDocument);
		check(new SpeciesDocumentEdit(creationDocument).apply(created), "Output and display inserted as one transaction");
		creationUndo.undo();
		check(creationDocument.get().equals(noOutput), "One undo removes generated output and display together");
		creationUndo.redo();
		check(creationDocument.get().equals(created), "Redo restores output and display");
		creationUndo.disconnect(creationDocument);
		DocumentUndoManagerRegistry.disconnect(creationDocument);
		check(DisplayFacet.of("display").stream().anyMatch(f -> f.name().equals("fullscreen") && !f.flag()), "Multi-type not checkbox");
		check(DisplayFacet.of("display_grid").stream().noneMatch(f -> f.name().equals("hexagonal")), "Internal facets hidden");
		check(LayerDraft.empty(LayerKind.AGENTS).validationError().contains("value"), "Agents require value facet");
		Document document = new Document(source);
		DocumentUndoManagerRegistry.connect(document);
		var undo = DocumentUndoManagerRegistry.getDocumentUndoManager(document);
		undo.connect(document);
		var tx = new SpeciesDocumentEdit(document);
		check(!tx.apply(adapter.render(draft)) && !undo.undoable(), "No-op creates no undo");
		var boolLayers = new ArrayList<>(draft.layers());
		var image = boolLayers.get(3);
		boolLayers.set(3, new LayerDraft(image.id(), image.kind(), image.expression(), List.of(), image.textFacets(), image.source()));
		var caption = boolLayers.get(4);
		boolLayers.set(4, new LayerDraft(caption.id(), caption.kind(), caption.expression(), List.of(),
				List.of(new Facet("at", "{0,0}"), new Facet("color", "#red"), new Facet("perspective", "false")), caption.source()));
		String booleans = adapter.render(layers(draft, boolLayers));
		check(!booleans.contains("refresh: false") && !booleans.contains("visible: true"),
				"Default literal removal affects only intended source facets:\n" + booleans);
		check(!parser.parse(new StringReader(booleans)).hasSyntaxErrors(), "Normalized boolean source parses");
		check(booleans.contains("visible: cycle > 0") && booleans.contains("perspective: false")
				&& booleans.contains("// keep chart"), "Expressions, false override and comments retained");
		check(tx.apply(booleans), "Apply normalized booleans transactionally");
		undo.undo();
		check(document.get().equals(source), "Boolean edit undo restores explicit defaults byte for byte");
		undo.redo();
		check(document.get().equals(booleans), "Boolean edit redo");
		undo.undo();
		tx = new SpeciesDocumentEdit(document);
		check(tx.apply(moved), "Apply order/edit");
		undo.undo();
		check(document.get().equals(source), "One undo restores all");
		undo.redo();
		check(document.get().equals(moved), "Redo");
		var stale = new SpeciesDocumentEdit(document);
		document.replace(0, 0, " ");
		try { stale.apply(source); throw new AssertionError("Stale accepted"); }
		catch (IllegalStateException expected) { checks++; }
		if (args.length > 0) {
			var directory = java.nio.file.Path.of(args[0]);
			java.nio.file.Files.createDirectories(directory);
			for (var entry : java.util.Map.of("original", source, "settings", changedHeader, "reordered", moved,
					"created-output", created, "invalid-species", source.replace("species wolf aspect:", "species missing_species aspect:"),
					"coerced-value", source.replace("value: wolf where (each.age > 1)", "value: 42"),
					"coerced-background", source.replace("#black", "42"),
					"invalid-facet", source.replace("trace: 3", "unknown_facet: 3")).entrySet())
				java.nio.file.Files.writeString(directory.resolve(entry.getKey() + ".gaml"), entry.getValue());
			java.nio.file.Files.writeString(directory.resolve("permanent.gaml"),
					inPermanent + "\nexperiment check_gui type: gui {}\n");
			List<LayerDraft> fresh = new ArrayList<>();
			for (var kind : LayerKind.values()) {
				var layer = LayerDraft.empty(kind);
				fresh.add(switch (kind) {
					case SPECIES -> expression(layer, "wolf");
					case GRID -> expression(layer, "cells");
					case IMAGE -> expression(layer, "\"pic.png\"");
					case AGENTS -> new LayerDraft(layer.id(), kind, "\"selected\"", List.of(new Facet("value", "wolf")), List.of(), "");
					case TEXT -> new LayerDraft(layer.id(), kind, "\"Population\"", List.of(),
							List.of(new Facet("color", "rgb(10, 20, 30)"), new Facet("at", "{0,0}")), "");
					case RAW -> DisplaySource.parseLayer("\nchart 'Raw' type: series {data 'time' value: cycle;}", layer.id(), parser);
				});
			}
			for (String renderer : List.of("java2D", "opengl", "\"2d\"", "\"3d\"")) {
				var generated = new DisplayDraft("New \"view\"", renderer, List.of(new Facet("background", "rgb(10, 20, 30)")),
						fresh, "\n");
				String model = new DisplaySource(source, source.indexOf("experiment e"), parser, true).render(generated);
				check(!parser.parse(new StringReader(model)).hasSyntaxErrors(), "All newly generated kinds/renderer parse: " + renderer);
				java.nio.file.Files.writeString(directory.resolve("new-" + renderer.replace("\"", "") + ".gaml"), model);
			}
		}
		System.out.println("DisplaySourceTest: " + checks + " checks passed");
	}
}
