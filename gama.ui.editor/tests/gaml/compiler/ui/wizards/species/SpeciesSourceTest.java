package gaml.compiler.ui.wizards.species;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.Document;
import org.eclipse.text.undo.DocumentUndoManagerRegistry;
import org.eclipse.xtext.parser.IParser;

import gaml.compiler.GamlStandaloneSetup;

/** Standalone regression harness; run with the editor/compiler classes and Eclipse dependencies. */
public final class SpeciesSourceTest {

	private static int checks;

	private static void check(final boolean condition, final String message) {
		checks++;
		if (!condition) throw new AssertionError(message);
	}

	private static SpeciesDraft header(final SpeciesDraft original, final String name, final List<String> skills,
			final List<SpeciesDraft.Facet> facets) {
		return new SpeciesDraft(name, skills, facets, original.variables(), original.init(), original.reflexOrder());
	}

	private static void rejects(final Runnable action, final String message) {
		try {
			action.run();
		} catch (IllegalArgumentException | IllegalStateException expected) {
			check(true, message);
			return;
		}
		throw new AssertionError(message);
	}

	private static void checkBuiltInParents(final IParser parser) throws Exception {
		try (var fixture = new BuiltInParentFixture()) {
			for (var kind : List.of(SpeciesWizard.Kind.SPECIES, SpeciesWizard.Kind.GRID)) {
				String keyword = kind == SpeciesWizard.Kind.GRID ? "grid" : "species";
				String source = "model sample\n" + keyword + " cells parent: "
						+ BuiltInParentFixture.PARENT + " { int retained <- 1; }\n";
				var adapter = new SpeciesSource(source, source.indexOf("cells"), parser);
				check(adapter.parentNames().contains(BuiltInParentFixture.PARENT),
						kind + " source offers registered built-in parents");
				check(adapter.validationError(adapter.draft()) == null
						&& adapter.render(adapter.draft()).equals(source),
						kind + " built-in parent no-op is valid and lossless");
				check(adapter.context().attributes(adapter.draft()).stream()
						.anyMatch(attribute -> BuiltInParentFixture.ATTRIBUTE.equals(attribute.variable().name())
								&& !attribute.redefinable()),
						kind + " source resolves built-in inherited attribute metadata");
				var changed = header(adapter.draft(), "renamed", List.of(), adapter.draft().facets());
				check(adapter.validationError(changed) == null
						&& adapter.render(changed).equals(source.replace("cells", "renamed")),
						kind + " rename retains built-in parent and body");
				var creation = new SpeciesSource("model sample\n", -1, parser, kind);
				var created = new SpeciesDraft("cells", List.of(),
						List.of(new SpeciesDraft.Facet("parent", BuiltInParentFixture.PARENT)), List.of(), "", List.of());
				check(creation.validationError(created) == null
						&& creation.render(created).contains(keyword + " cells parent: " + BuiltInParentFixture.PARENT),
						kind + " creation accepts a registered built-in parent");
			}
			String shadowed = "model sample\nspecies wolf {}\nspecies " + BuiltInParentFixture.PARENT
					+ " parent: wolf {}\n";
			var adapter = new SpeciesSource(shadowed, shadowed.indexOf("wolf"), parser);
			check(!adapter.parentNames().contains(BuiltInParentFixture.PARENT),
					"Local descendant shadowing a built-in is not a parent choice");
			check(!adapter.context().parentNames(SpeciesWizardContext.builtInParentNames())
					.contains(BuiltInParentFixture.PARENT), "Combined parent candidates cannot bypass local cycles");
			check(adapter.context().mirrorNames(List.of()).contains(BuiltInParentFixture.PARENT),
					"Shadowing local descendant remains a mirror candidate");
			String localAncestry = "model sample\nspecies animal parent: " + BuiltInParentFixture.PARENT
					+ " {}\nspecies wolf parent: animal {}";
			var descendant = new SpeciesSource(localAncestry, localAncestry.indexOf("wolf"), parser);
			check(descendant.context().attributes(descendant.draft()).stream()
					.anyMatch(attribute -> BuiltInParentFixture.ATTRIBUTE.equals(attribute.variable().name())),
					"Built-in attributes resolve through local ancestry");
		}
	}

	public static void main(final String[] args) throws Exception {
		IParser parser = GamlStandaloneSetup.doSetup().getInstance(IParser.class);
		checkBuiltInParents(parser);
		String body = "{\n\t// Keep all declarations and strings untouched\n"
				+ "\tint n <- 1;\n\tinit { write \"{not a block}\"; }\n"
				+ "\treflex step when: true { write n; }\n\taspect base { draw circle(1); }\n}";
		String source = "model sample\n// species fake {}\nspecies wolf /* name comment */ "
				+ "skills: [moving /* skill comment */] parent: /* parent comment */ agent " + body
				+ "\nexperiment main type: gui {}\n";
		var adapter = new SpeciesSource(source, source.indexOf("int n"), parser);
		var draft = adapter.draft();
		check("wolf".equals(draft.name()), "AST species selection");
		check(draft.skills().equals(List.of("moving")), "Skills read without comment tokens");
		check(adapter.render(draft).equals(source), "No-op preserves every byte");
		var headerSelection = new SpeciesSource(source, source.indexOf("wolf"), parser);
		check(headerSelection.isSpeciesSelection(source.indexOf("wolf"), 4), "Species name selection enables Edit");
		check(headerSelection.isSpeciesSelection(source.indexOf("species wolf"), 0), "Species keyword caret enables Edit");
		check(!adapter.isSpeciesSelection(source.indexOf("int n"), 0), "Attribute caret does not enable species Edit");
		check(!adapter.isSpeciesSelection(source.indexOf("reflex step"), "reflex step".length()),
				"Nested statement selection does not enable species Edit");
		int declarationStart = source.indexOf("species wolf");
		check(headerSelection.isSpeciesSelection(declarationStart, source.indexOf("\nexperiment") - declarationStart),
				"Complete species declaration selection enables Edit");
		var renamed = header(draft, "fox", draft.skills(), draft.facets());
		check(adapter.validationError(renamed) == null, "Valid rename accepted");
		check(adapter.render(renamed).equals(source.replace("species wolf", "species fox")), "Only name token changed");
		var changedSkills = header(draft, draft.name(), List.of("moving", "bouncing"), draft.facets());
		String skillOutput = adapter.render(changedSkills);
		check(skillOutput.contains("/* skill comment */"), "Comment inside changed skills retained");
		check(skillOutput.contains(body), "Entire body preserved");
		check(adapter.validationError(changedSkills) == null, "Changed skills remain syntactically valid");
		var noFacets = header(draft, draft.name(), List.of(), List.of());
		String removed = adapter.render(noFacets);
		check(removed.contains("/* skill comment */") && removed.contains("/* parent comment */"),
				"Removed facets do not delete comments");
		check(removed.contains(body), "Removed facets do not affect body");
		check(adapter.validationError(noFacets) == null, "Removed facets valid");
		var facets = new ArrayList<>(draft.facets());
		facets.add(new SpeciesDraft.Facet("control", "fsm"));
		var added = header(draft, draft.name(), draft.skills(), facets);
		check(adapter.validationError(added) == null, "Added facet valid");
		check(adapter.render(added).contains("control: fsm " + body), "New facet inserted before block");

		String emptySkills = "model sample\nspecies wolf skills: [] {}\n";
		var emptyAdapter = new SpeciesSource(emptySkills, emptySkills.indexOf("wolf"), parser);
		var emptyDraft = emptyAdapter.draft();
		var withSkill = header(emptyDraft, "wolf", List.of("moving"), List.of());
		String nonemptySkills = emptyAdapter.render(withSkill);
		check(nonemptySkills.indexOf("skills:") == nonemptySkills.lastIndexOf("skills:"), "Empty skills not duplicated");
		check(emptyAdapter.validationError(withSkill) == null, "Empty-to-nonempty skills valid");
		check(emptyAdapter.render(header(emptyDraft, "fox", List.of(), List.of())).contains("skills: []"),
				"Unchanged empty skills retained during rename");

		String commented = "model sample\nspecies wolf frequency: 1 /* keep */ + 2 {}\n";
		var commentedAdapter = new SpeciesSource(commented, commented.indexOf("wolf"), parser);
		check(!commentedAdapter.draft().facets().getFirst().value().contains("keep"), "Comment not duplicated into input");
		var newFrequency = header(commentedAdapter.draft(), "wolf", List.of(),
				List.of(new SpeciesDraft.Facet("frequency", "3")));
		String frequencyOutput = commentedAdapter.render(newFrequency);
		check(frequencyOutput.indexOf("/* keep */") == frequencyOutput.lastIndexOf("/* keep */"), "Comment preserved once");
		check(commentedAdapter.validationError(newFrequency) == null, "Comment retention preserves syntax");

		String nested = "model sample\nspecies outer { species inner {} }\ngrid cells width: 2 height: 2 {}\n";
		check(new SpeciesSource(nested, nested.indexOf("inner"), parser).draft().name().equals("inner"),
				"Innermost species selected");
		var gridSource = new SpeciesSource(nested, nested.indexOf("cells"), parser);
		check(gridSource.kind() == SpeciesWizard.Kind.GRID
				&& gridSource.isSpeciesSelection(nested.indexOf("cells"), 0), "Grid declaration selection enables Edit");
		check(gridSource.render(gridSource.draft()).equals(nested), "Grid no-op preserves source");
		var gridRenamed = header(gridSource.draft(), "terrain", List.of(), gridSource.draft().facets());
		check(gridSource.render(gridRenamed).equals(nested.replace("grid cells", "grid terrain")),
				"Grid rename preserves declaration kind and existing facets");
		var shapesFalse = new SpeciesDraft.Facet("use_individual_shapes", "false");
		var shapeFacets = new java.util.ArrayList<>(gridSource.draft().facets());
		shapeFacets.add(shapesFalse);
		var shapesDraft = header(gridSource.draft(), "cells", List.of(), shapeFacets);
		check(gridSource.render(shapesDraft).equals(nested.replace("height: 2 {}", "height: 2 use_individual_shapes: false {}"))
				&& gridSource.validationError(shapesDraft) == null,
				"Absent shapes saved as explicit false without touching other grid source");
		for (String literal : List.of("true", "false")) {
			String fixture = "model m\ngrid cells use_individual_shapes: " + literal + " { /* keep */ }\n";
			var shapeAdapter = new SpeciesSource(fixture, fixture.indexOf("cells"), parser);
			check(shapeAdapter.render(shapeAdapter.draft()) == fixture, "Explicit shapes literal source no-op: " + literal);
			check(shapeAdapter.render(header(shapeAdapter.draft(), "cells", List.of(), List.of(shapesFalse)))
					.equals(fixture.replace("shapes: " + literal, "shapes: false")), "False checkbox only patches shapes literal");
		}
		String retainedGrid = "model m\ngrid cells virtual: virtual_condition "
				+ "use_individual_shapes: shape_condition use_neighbors_cache: cache_condition "
				+ "use_regular_agents: false { /* untouched */ int value <- 1; }\n";
		var retainedGridAdapter = new SpeciesSource(retainedGrid, retainedGrid.indexOf("cells"), parser);
		check(retainedGridAdapter.render(retainedGridAdapter.draft()).equals(retainedGrid),
				"Grid no-op keeps hidden regular-agents and nonliteral checkbox facets byte for byte");
		check(retainedGridAdapter.render(header(retainedGridAdapter.draft(), "terrain", List.of(),
				retainedGridAdapter.draft().facets())).equals(retainedGrid.replace("grid cells", "grid terrain")),
				"Unrelated grid edit preserves checkbox expressions and hidden facets");
		var literalGridFacets = gaml.compiler.ui.wizards.experiment.ExperimentDraft.withFacet(
				retainedGridAdapter.draft().facets(), retainedGridAdapter.draft().facets(), "use_individual_shapes", "false");
		check(retainedGridAdapter.render(header(retainedGridAdapter.draft(), "cells", List.of(), literalGridFacets))
				.equals(retainedGrid.replace("use_individual_shapes: shape_condition", "use_individual_shapes: false")),
				"Explicit grid checkbox edit patches only its expression token");
		var newGrid = new SpeciesSource("model sample\n", -1, parser, SpeciesWizard.Kind.GRID);
		var gridDraft = new SpeciesDraft("cells", List.of(),
				List.of(new SpeciesDraft.Facet("width", "10"), new SpeciesDraft.Facet("height", "20"),
						new SpeciesDraft.Facet("neighbors", "6"), new SpeciesDraft.Facet("use_neighbors_cache", "false"), shapesFalse),
				List.of(new SpeciesDraft.Variable("level", "float", "2", "", "", "", "")), "", List.of());
		check(newGrid.validationError(gridDraft) == null && newGrid.render(gridDraft).contains("grid cells width: 10"),
				"Grid creation renders and parses grid-specific facets and attributes");
		check(newGrid.render(gridDraft).contains("use_individual_shapes: false"), "New grid explicit false reaches generated GAML");
		check(newGrid.validationError(header(gridDraft, "cells", List.of(),
				List.of(new SpeciesDraft.Facet("mirrors", "outer")))) != null, "Grid mirroring rejected");
		rejects(() -> new SpeciesSource("model sample\nglobal {}", 20, parser), "Global excluded");
		rejects(() -> new SpeciesSource("model sample\nspecies wolf {", -1, parser), "Malformed source rejected");
		var duplicate = new SpeciesSource("model sample\nspecies wolf {}\nspecies fox {}", 25, parser);
		check(duplicate.validationError(header(duplicate.draft(), "fox", List.of(), List.of())) != null,
				"Duplicate sibling rejected");
		String inheritance = "model sample\nspecies animal {}\nspecies wolf parent: animal {}\nspecies cub parent: wolf {}\n";
		var inherited = new SpeciesSource(inheritance, inheritance.indexOf("wolf"), parser);
		check(inherited.draft().parent().equals("animal"), "Parent accessor reads explicit parent");
		check(inherited.parentNames().contains("animal") && !inherited.parentNames().contains("wolf")
				&& !inherited.parentNames().contains("cub"), "Parent choices exclude self and inheritance descendants");
		check(inherited.context().mirrorNames(List.of()).contains("cub"),
				"Mirror choices are independent of parent-cycle exclusions");
		SpeciesWizardContext localMirrors = new SpeciesWizardContext() {
			@Override
			public List<InheritedAttribute> attributes(final SpeciesDraft selection) { return List.of(); }

			@Override
			public List<String> mirrorNames(final List<String> fallback) { return List.of("cub"); }
		};
		check(SpeciesWizardContext.builtIns().and(localMirrors).mirrorNames(List.of("animal")).equals(List.of("cub")),
				"Context composition preserves the additional mirror candidate provider");
		check(localMirrors.and(selection -> List.of()).mirrorNames(List.of("animal")).equals(List.of("cub")),
				"A default context does not discard earlier mirror candidates");
		var parentChanged = header(inherited.draft(), "wolf", List.of(), List.of(new SpeciesDraft.Facet("parent", "agent")));
		check(inherited.validationError(parentChanged) == null
				&& inherited.render(parentChanged).equals(inheritance.replace("wolf parent: animal", "wolf parent: agent")),
				"Parent selection patches only the existing parent value");
		check(inherited.validationError(header(inherited.draft(), "wolf", List.of(),
				List.of(new SpeciesDraft.Facet("parent", "cub")))) != null, "Cyclic parent rejected");
		check(SpeciesDraft.empty().parent().equals("agent"), "Absent parent defaults to agent");
		String contextualSource = "model sample\nspecies animal { float energy <- 5; }\n"
				+ "species predator parent: animal { int age <- 0; }\nspecies wolf {}\nspecies prey { float weight <- 2; }\n";
		var contextual = new SpeciesSource(contextualSource, contextualSource.indexOf("wolf"), parser);
		var contextualDraft = new SpeciesDraft("wolf", List.of(), List.of(new SpeciesDraft.Facet("parent", "predator"),
				new SpeciesDraft.Facet("mirrors", "prey")), List.of(), "", List.of());
		var metadata = contextual.context().attributes(contextualDraft);
		check(metadata.stream().anyMatch(attr -> "energy".equals(attr.variable().name())
				&& "5".equals(attr.variable().init().trim())), "Parent ancestor attributes include source initialization");
		check(metadata.stream().anyMatch(attr -> "age".equals(attr.variable().name())), "Direct parent attributes visible");
		check(metadata.stream().anyMatch(attr -> "target".equals(attr.variable().name())
				&& "prey".equals(attr.variable().type())), "Mirror exposes target typed as selected species");
		check(metadata.stream().noneMatch(attr -> "weight".equals(attr.variable().name())),
				"Mirror does not inherit the mirrored species' attributes");
		var allFacets = new SpeciesDraft("new_species", List.of(), List.of(new SpeciesDraft.Facet("virtual", "true"),
				new SpeciesDraft.Facet("control", "fsm"), new SpeciesDraft.Facet("mirrors", "prey"),
				new SpeciesDraft.Facet("frequency", "2"), new SpeciesDraft.Facet("schedules", "new_species")),
				List.of(new SpeciesDraft.Variable("location", "point", "{1,2,0}", "", "", "", "")), "", List.of());
		var creationContext = new SpeciesSource(contextualSource, -1, parser);
		check(creationContext.validationError(allFacets) == null, "Fixed advanced fields and explicit override parse as GAML");
		check(adapter.validationError(header(draft, "if", draft.skills(), draft.facets())) != null,
				"Reserved keyword rejected by GAML parser");
		check(adapter.validationError(header(draft, "wolf", draft.skills(),
				List.of(new SpeciesDraft.Facet("frequency", "(")))) != null, "Invalid facet syntax rejected");
		check(adapter.validationError(header(draft, "wolf", draft.skills(),
				List.of(new SpeciesDraft.Facet("frequency", "1 control: fsm")))) != null,
				"Facet value cannot introduce extra facets");
		check(adapter.validationError(header(draft, "wolf", draft.skills(),
				List.of(new SpeciesDraft.Facet("frequency", "1 {} species injected")))) != null,
				"Facet value cannot introduce extra declarations");

		var create = new SpeciesSource("model sample\r\n// trailing comment", -1, parser);
		var newDraft = new SpeciesDraft("wolf", List.of("moving"), List.of(new SpeciesDraft.Facet("frequency", "2")),
				List.of(new SpeciesDraft.Variable("energy", "float", "1.0", "energy - 1", "", "0", "10")),
				"\t\twrite energy;", List.of());
		check(create.validationError(newDraft) == null, "Creation with variables/init accepted");
		String created = create.render(newDraft);
		check(created.startsWith(createSource()) && created.contains("\r\nspecies wolf"), "Creation preserves source/CRLF");
		check(created.contains("float energy init: 1.0 update: energy - 1 min: 0 max: 10;"), "All variable facets emitted");
		check(created.contains("init {\r\n\t\twrite energy;"), "Init is body only");
		var existingReflex = new SpeciesDraft("wolf", List.of(), List.of(), List.of(), "", List.of("step"));
		check(create.validationError(existingReflex) != null, "Creation cannot fabricate reflex bodies");

		Document document = new Document(source);
		Object owner = new Object();
		DocumentUndoManagerRegistry.connect(document);
		var undo = DocumentUndoManagerRegistry.getDocumentUndoManager(document);
		undo.connect(owner);
		try {
			var transaction = new SpeciesDocumentEdit(document);
			check(!transaction.apply(source), "No-op causes no document edit");
			check(!undo.undoable(), "No-op has no undo entry");
			check(transaction.apply(adapter.render(renamed)), "Header transaction applied");
			check(document.get().contains("species fox"), "Header edit visible");
			undo.undo();
			check(document.get().equals(source), "One undo restores exact document");
			undo.redo();
			check(document.get().equals(adapter.render(renamed)), "Redo restores header change");
			document.set(source);
			undo.reset();
			document.replace(document.getLength(), 0, "// preceding user edit\n");
			String preceding = document.get();
			new SpeciesDocumentEdit(document).apply(preceding.replace("species wolf", "species fox"));
			undo.undo();
			check(document.get().equals(preceding), "Wizard undo does not swallow preceding user typing");
			undo.reset();
			document.set(createSource());
			undo.reset();
			new SpeciesDocumentEdit(document).apply(created);
			undo.undo();
			check(document.get().equals(createSource()), "Creation is undoable");
			undo.redo();
			check(document.get().equals(created), "Creation is redoable");
			String gridCreated = newGrid.render(gridDraft);
			document.set("model sample\n");
			undo.reset();
			new SpeciesDocumentEdit(document).apply(gridCreated);
			undo.undo();
			check(document.get().equals("model sample\n"), "Grid insertion is undone in one operation");
			undo.redo();
			check(document.get().equals(gridCreated), "Grid insertion is redoable");
			var gridEdit = new SpeciesSource(gridCreated, gridCreated.indexOf("cells"), parser);
			var editedGridDraft = header(gridEdit.draft(), "terrain", List.of(), gridEdit.draft().facets());
			new SpeciesDocumentEdit(document).apply(gridEdit.render(editedGridDraft));
			undo.undo();
			check(document.get().equals(gridCreated), "Grid header edit undo preserves attribute body");
			undo.redo();
			check(document.get().equals(gridCreated.replace("grid cells", "grid terrain")), "Grid header edit redo exact");
			var stale = new SpeciesDocumentEdit(document);
			document.replace(0, 0, " ");
			rejects(() -> {
				try { stale.apply(source); } catch (org.eclipse.jface.text.BadLocationException e) { throw new AssertionError(e); }
			}, "Stale transaction rejected");
			var reverted = new SpeciesDocumentEdit(document);
			document.replace(0, 0, " ");
			document.replace(0, 1, "");
			check(!reverted.isCurrent(), "Changed-then-restored document still invalidates positions");
			String beforeCancel = document.get();
			new SpeciesDocumentEdit(document);
			check(document.get().equals(beforeCancel), "Detached editing/cancel does not mutate document");
		} finally {
			undo.disconnect(owner);
			DocumentUndoManagerRegistry.disconnect(document);
		}
		var mutableSkills = new ArrayList<>(List.of("moving"));
		var immutable = new SpeciesDraft("wolf", mutableSkills, List.of(), List.of(), "", List.of());
		mutableSkills.clear();
		check(immutable.skills().equals(List.of("moving")), "Draft lists are defensive copies");
		check(new SpeciesDraft("wolf", List.of("moving", "moving"), List.of(), List.of(), "", List.of())
				.validationError() != null, "Duplicate skill validation");
		System.out.println("Species source/document regression: " + checks + " checks passed");
	}

	private static String createSource() { return "model sample\r\n// trailing comment"; }
}
