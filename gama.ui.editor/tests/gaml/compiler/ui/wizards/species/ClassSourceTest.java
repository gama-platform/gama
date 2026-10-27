package gaml.compiler.ui.wizards.species;

import java.util.List;

import org.eclipse.jface.text.Document;
import org.eclipse.text.undo.DocumentUndoManagerRegistry;
import org.eclipse.xtext.parser.IParser;

import gaml.compiler.GamlStandaloneSetup;

/** Class creation/editing uses the same lossless, stale-guarded document transactions as species. */
public final class ClassSourceTest {
	private static int checks;

	private static void check(final boolean condition, final String message) {
		if (!condition) throw new AssertionError(message);
		checks++;
	}

	private static SpeciesDraft parent(final SpeciesDraft draft, final String name) {
		return new SpeciesDraft(draft.name(), draft.skills(), List.of(new SpeciesDraft.Facet("parent", name)),
				draft.variables(), draft.init(), draft.reflexOrder());
	}

	public static void main(final String[] args) throws Exception {
		IParser parser = GamlStandaloneSetup.doSetup().getInstance(IParser.class);
		String body = "\tfloat energy <- 2; // keep attribute\n\taction grow() { energy <- energy + 1; }\n"
				+ "\tfloat magnitude() { return energy; }\n";
		String source = "model sample\n\nglobal { int n <- 1; }\n"
				+ "class base virtual: true { int inherited <- 3; action helper() {} }\n"
				+ "class child /* keep header */ parent: base {\n" + body + "}\n"
				+ "class descendant parent: child {}\n"
				+ "species wolf { int value <- 3; }\n"
				+ "grid cells width: 2 height: 2 {}\nexperiment main type: gui {}\n";
		var parsed = parser.parse(new java.io.StringReader(source));
		for (var error : parsed.getSyntaxErrors())
			throw new AssertionError(error.getSyntaxErrorMessage().getMessage() + ": " + error.getText());
		var adapter = new SpeciesSource(source, source.indexOf("class child"), parser);
		check(adapter.kind() == SpeciesWizard.Kind.CLASS, "Class keyword recognized through editor selection adapter");
		check(adapter.isSpeciesSelection(source.indexOf("class child"), "class".length()), "Edit enabled on class keyword");
		check(!adapter.isSpeciesSelection(source.indexOf("action grow"), 0), "Nested action does not select class header");
		check(adapter.draft().name().equals("child") && adapter.draft().parent("object").equals("base"), "Class header read");
		check(adapter.draft().variables().size() == 1, "Class attribute displayed");
		check(adapter.render(adapter.draft()).equals(source), "Unchanged class preserves complete source");
		check(adapter.validationError(adapter.draft()) == null, "Unchanged class valid");
		check(adapter.parentNames().containsAll(List.of("object", "base")), "Class parents include object and other classes");
		check(!adapter.parentNames().contains("child") && !adapter.parentNames().contains("descendant"),
				"Self and descendant class parents filtered");
		check(!adapter.parentNames().contains("wolf") && !adapter.parentNames().contains("cells")
				&& !adapter.parentNames().contains("agent"), "Class parents exclude species/grid/agent");
		check(adapter.context().attributes(adapter.draft()).stream()
				.anyMatch(attribute -> attribute.variable().name().equals("inherited") && attribute.origin().equals("parent: base")),
				"Parent class attributes resolved without agent metadata");
		check(adapter.context().attributes(adapter.draft()).stream().noneMatch(attribute -> attribute.origin().equals("agent")),
				"Classes do not inherit agent attributes");
		check(adapter.context().attributes(adapter.draft()).size() == 1, "Parent actions are not mistaken for inherited attributes");
		check(adapter.validationError(parent(adapter.draft(), "descendant")) != null, "Class inheritance cycle rejected");
		check(adapter.validationError(parent(adapter.draft(), "wolf")) != null, "Species cannot parent a class");
		check(adapter.validationError(parent(adapter.draft(), "cells")) != null, "Grid cannot parent a class");
		check(adapter.validationError(parent(adapter.draft(), "missing")) != null, "Unknown new class parent rejected");
		var renamed = new SpeciesDraft("renamed", adapter.draft().skills(), adapter.draft().facets(),
				adapter.draft().variables(), "", List.of());
		check(adapter.render(renamed).equals(source.replace("class child", "class renamed")),
				"Rename modifies only declaration name; no body reconstruction");
		check(adapter.validationError(renamed) == null, "Class rename valid");
		check(adapter.render(parent(adapter.draft(), "object")).contains(body), "Parent edit preserves body and actions");
		check(adapter.render(parent(adapter.draft(), "object")).contains("/* keep header */"), "Parent edit preserves comments");
		var virtual = new SpeciesDraft(adapter.draft().name(), List.of(),
				List.of(new SpeciesDraft.Facet("parent", "base"), new SpeciesDraft.Facet("virtual", "true")),
				adapter.draft().variables(), "", List.of());
		check(adapter.validationError(virtual) == null && adapter.render(virtual).contains("virtual: true")
				&& adapter.render(virtual).contains(body), "Virtual facet edit preserves all actions and declarations");
		var species = new SpeciesSource(source, source.indexOf("species wolf"), parser);
		check(!species.parentNames().contains("base") && !species.parentNames().contains("child"), "Classes excluded from species parents");
		check(species.validationError(parent(species.draft(), "base")) != null, "Class cannot parent species");
		check(species.validationError(parent(species.draft(), "object")) != null, "Root object class cannot parent species");
		var grid = new SpeciesSource(source, source.indexOf("grid cells"), parser);
		check(grid.validationError(parent(grid.draft(), "base")) != null, "Class cannot parent grid");
		var creation = new SpeciesSource(source, -1, parser, SpeciesWizard.Kind.CLASS);
		var created = new SpeciesDraft("record", List.of(), List.of(),
				List.of(new SpeciesDraft.Variable("count", "int", "1", "", "", "0", "10")), "", List.of());
		check(creation.validationError(created) == null, "Class creation with implicit object parent valid");
		String insertion = creation.render(created);
		check(insertion.startsWith(source) && insertion.contains("class record {\n\tint count init: 1 min: 0 max: 10;"),
				"Class declaration inserted at world level with attributes");
		check(parser.parse(new java.io.StringReader(insertion)).hasSyntaxErrors() == false, "Inserted model parses");
		check(creation.validationError(parent(created, "wolf")) != null, "Creation enforces class-only parent");
		check(creation.validationError(parent(created, "base")) == null, "Creation accepts a local class parent");
		var computed = new SpeciesDraft("computed", List.of(), List.of(),
				List.of(new SpeciesDraft.Variable("amount", "float", "1.5", "amount + 1", "", "0", "100"),
						new SpeciesDraft.Variable("double_amount", "float", "", "", "amount * 2", "", "")), "", List.of());
		check(creation.validationError(computed) == null, "Class attributes retain update/function expressions");
		String computedSource = creation.render(computed);
		var computedAdapter = new SpeciesSource(computedSource, computedSource.indexOf("class computed"), parser);
		check(computedAdapter.draft().variables().stream().map(variable -> new SpeciesDraft.Variable(
				variable.name(), variable.type(), variable.init().trim(), variable.update().trim(),
				variable.function().trim(), variable.min().trim(), variable.max().trim())).toList().equals(computed.variables()),
				"Class attribute type/init/update/function/min/max survive source creation and reread");
		check(computedAdapter.render(computedAdapter.draft()).equals(computedSource),
				"Computed class attributes remain lossless on unchanged Edit");
		String abstractSource = "model m\nclass base virtual: true {\n"
				+ "\tfloat explicit() virtual: true;\n\tfloat implicit();\n"
				+ "\taction procedure() virtual: true;\n}\n";
		var abstractClass = new SpeciesSource(abstractSource, abstractSource.indexOf("class"), parser);
		check(abstractClass.draft().variables().isEmpty() && abstractClass.validationError(abstractClass.draft()) == null,
				"Explicit and implicit abstract methods are not mistaken for attributes");
		var abstractRename = new SpeciesDraft("renamed", List.of(), abstractClass.draft().facets(), List.of(), "", List.of());
		check(abstractClass.render(abstractRename).equals(abstractSource.replace("class base", "class renamed")),
				"Editing a virtual class preserves typed/void abstract method signatures and absent bodies");
		var duplicate = new SpeciesDraft("wolf", List.of(), List.of(), List.of(), "", List.of());
		check(creation.validationError(duplicate) != null, "Class name cannot collide with sibling species");
		var invalid = new SpeciesDraft("invalid", List.of("moving"), List.of(), List.of(), "", List.of());
		check(creation.validationError(invalid) != null, "Class rejects skills");
		var nestedText = "model m\nspecies container { class nested {} }\n";
		var nested = new SpeciesSource(nestedText, nestedText.indexOf("class"), parser);
		check(nested.validationError(nested.draft()) != null, "Classes cannot be nested in a species");
		String globalClasses = "model m\nglobal { class base {} class child parent: base {} }\nclass sibling parent: child {}\n";
		var globalClass = new SpeciesSource(globalClasses, globalClasses.indexOf("class child"), parser);
		check(globalClass.validationError(globalClass.draft()) == null
				&& globalClass.render(globalClass.draft()).equals(globalClasses), "Global-contained classes belong to the world");
		check(globalClass.parentNames().contains("base") && !globalClass.parentNames().contains("sibling"),
				"World class visibility/cycle checks cross the global wrapper");
		var globalCreation = new SpeciesSource(globalClasses, -1, parser, SpeciesWizard.Kind.CLASS);
		check(globalCreation.parentNames().contains("child"), "New top-level class sees classes inside global");
		check(globalCreation.validationError(new SpeciesDraft("base", List.of(), List.of(), List.of(), "", List.of())) != null,
				"Class duplicates detected across global/model-level declarations");
		try (var fixture = new BuiltInParentFixture()) {
			String invalidParent = "model m\nclass invalid parent: " + BuiltInParentFixture.PARENT + " {}\n";
			var invalidClass = new SpeciesSource(invalidParent, invalidParent.indexOf("class"), parser);
			check(invalidClass.validationError(invalidClass.draft()) != null,
					"Unchanged invalid built-in species parent cannot bypass class validation");
			check(invalidClass.parentNames().stream().noneMatch(name -> name.equals(BuiltInParentFixture.PARENT)),
					"Built-in species registry cannot leak into class parent choices");
		}
		check(SpeciesDraft.empty().parent().equals("agent") && SpeciesDraft.empty().parent("object").equals("object"),
				"Default-parent API preserves existing species behavior");
		Document document = new Document(source);
		Object owner = new Object();
		DocumentUndoManagerRegistry.connect(document);
		var undo = DocumentUndoManagerRegistry.getDocumentUndoManager(document);
		undo.connect(owner);
		try {
			check(!new SpeciesDocumentEdit(document).apply(source) && !undo.undoable(), "No-op class acceptance has no undo entry");
			new SpeciesDocumentEdit(document).apply(insertion);
			undo.undo();
			check(document.get().equals(source), "Class insertion undone in one operation");
			undo.redo();
			check(document.get().equals(insertion), "Class insertion redo exact");
			document.set(source);
			undo.reset();
			new SpeciesDocumentEdit(document).apply(adapter.render(renamed));
			undo.undo();
			check(document.get().equals(source), "Class header edit undo restores exact source");
			var stale = new SpeciesDocumentEdit(document);
			document.replace(0, 0, "// user edit\n");
			check(!stale.isCurrent(), "Concurrent source changes invalidate class transaction");
			boolean rejected = false;
			try { stale.apply(adapter.render(renamed)); } catch (IllegalStateException expected) { rejected = true; }
			check(rejected, "Stale class transaction refuses to overwrite newer edits");
		} finally {
			undo.disconnect(owner);
			DocumentUndoManagerRegistry.disconnect(document);
		}
		try (var examples = java.nio.file.Files.list(java.nio.file.Path.of("gama.library/models/Recipes/Define Classes"))) {
			for (var file : examples.filter(path -> path.toString().endsWith(".gaml")).sorted().toList()) {
				String example = java.nio.file.Files.readString(file);
				var model = parser.parse(new java.io.StringReader(example));
				check(!model.hasSyntaxErrors(), "Class library example parses: " + file.getFileName());
				var contents = model.getRootASTElement().eAllContents();
				while (contents.hasNext()) {
					if (!(contents.next() instanceof gaml.compiler.gaml.S_Species declaration)
							|| !"class".equals(declaration.getKey())) continue;
					int offset = org.eclipse.xtext.nodemodel.util.NodeModelUtils.findActualNodeFor(declaration).getOffset();
					var existing = new SpeciesSource(example, offset, parser);
					check(existing.render(existing.draft()).equals(example),
							"Library class no-op preserves full source: " + declaration.getName());
					check(existing.validationError(existing.draft()) == null,
							"Library class unchanged acceptance valid: " + declaration.getName());
					existing.context().attributes(existing.draft());
				}
			}
		}
		System.out.println("Class source/document regression: " + checks + " checks passed");
	}
}
