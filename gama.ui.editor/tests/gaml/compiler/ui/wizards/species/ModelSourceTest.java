package gaml.compiler.ui.wizards.species;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.Document;
import org.eclipse.text.undo.DocumentUndoManagerRegistry;
import org.eclipse.xtext.parser.IParser;

import gaml.compiler.GamlStandaloneSetup;

public final class ModelSourceTest {
	private static int checks;

	private static void check(final boolean condition, final String message) {
		if (!condition) throw new AssertionError(message);
		checks++;
	}

	private static SpeciesDraft with(final SpeciesDraft draft, final String name, final List<SpeciesDraft.Facet> facets) {
		return new SpeciesDraft(name, draft.skills(), facets, draft.variables(), draft.init(), draft.reflexOrder());
	}

	private static List<SpeciesDraft.Facet> put(final List<SpeciesDraft.Facet> facets, final String name,
			final String value) {
		var result = new ArrayList<SpeciesDraft.Facet>();
		for (var facet : facets) if (!facet.name().equals(name)) result.add(facet);
		if (value != null) result.add(new SpeciesDraft.Facet(name, value));
		return result;
	}

	public static void main(final String[] args) throws Exception {
		IParser parser = GamlStandaloneSetup.doSetup().getInstance(IParser.class);
		String nl = "\r\n";
		String header = "/**" + nl + "* Name: predators" + nl + "* Hunting * / model" + nl + "* Author: Ann" + nl
				+ "* Tags: ecology, agents" + nl + "*/" + nl;
		String body = "\tint n <- 3; // count" + nl + "\tinit { create wolf number: n; }" + nl;
		String source = header + nl + "model predators" + nl + nl + "global /* g */ control: fsm {" + nl + body + "}"
				+ nl + nl + "species wolf {}" + nl + "experiment main type: gui {}" + nl;

		var doc = ModelDocumentation.read(source);
		check(doc.author().equals("Ann") && doc.tags().equals("ecology, agents"), "Author and tags read from header");
		check(doc.documentation().equals("Hunting */ model"), "Documentation lines read");
		check(ModelDocumentation.read("model m\nglobal {}").equals(ModelDocumentation.EMPTY), "No header gives EMPTY");
		check(ModelDocumentation.parse("/** plain javadoc */") == null, "Unrecognized comment not treated as header");
		var escaped = new ModelDocumentation("A */ B", "x", "line1" + nl + "end */ here");
		String comment = escaped.comment("m", nl);
		check(comment.indexOf("*/") == comment.length() - 2, "Comment terminator escaped inside values");
		check(ModelDocumentation.parse(comment).author().equals("A */ B"), "Escaped author parsed back");

		var adapter = new ModelSource(source, source.indexOf("global"), parser);
		check(adapter.isGlobalSelection(source.indexOf("global"), 0), "Global keyword enables Edit");
		check(!adapter.isGlobalSelection(source.indexOf("int n"), 0), "Attribute caret does not enable global Edit");
		check(!adapter.isGlobalSelection(source.indexOf("wolf"), 0), "Species does not enable global Edit");
		var draft = adapter.draft();
		check(draft.name().equals("predators"), "Draft name is model name");
		check(adapter.documentation().equals(doc), "Adapter reads documentation");
		check(adapter.render(draft, adapter.documentation()).equals(source), "No-op preserves every byte");
		check(adapter.validationError(draft) == null, "Existing global valid");

		var renamed = with(draft, "prey", draft.facets());
		String renamedSource = adapter.render(renamed, adapter.documentation());
		check(renamedSource.equals(source.replace("Name: predators", "Name: prey").replace("model predators",
				"model prey")), "Rename patches only model name and header Name line");
		check(adapter.validationError(renamed) == null, "Rename valid");

		var facets = put(put(draft.facets(), "control", null), "frequency", "2");
		String facetSource = adapter.render(with(draft, draft.name(), facets), adapter.documentation());
		check(facetSource.contains("/* g */") && facetSource.contains(body), "Global comment and body preserved");
		check(!facetSource.contains("control: fsm") && facetSource.contains("frequency: 2"), "Facets edited");
		check(facetSource.startsWith(header), "Facet edit keeps header bytes");

		var newDoc = new ModelDocumentation("Bob", "tag", "First\nSecond");
		String docSource = adapter.render(draft, newDoc);
		check(ModelDocumentation.read(docSource).equals(newDoc), "Edited documentation retained when editing again");
		check(docSource.substring(docSource.indexOf("model predators")).equals(
				source.substring(source.indexOf("model predators"))), "Documentation edit does not touch GAML");
		check(!docSource.contains("\n") || !docSource.replace(nl, "").contains("\n"), "CRLF preserved in comment");

		String bare = "model bare\n\nglobal {\n}\n";
		var bareAdapter = new ModelSource(bare, bare.indexOf("global"), parser);
		check(bareAdapter.render(bareAdapter.draft(), ModelDocumentation.EMPTY).equals(bare), "Empty doc inserts nothing");
		String inserted = bareAdapter.render(bareAdapter.draft(), newDoc);
		check(inserted.endsWith(bare) && ModelDocumentation.read(inserted).equals(newDoc), "Header inserted before model");

		var variableEdit = new SpeciesDraft(draft.name(), draft.skills(), draft.facets(), List.of(), draft.init(),
				draft.reflexOrder());
		check(adapter.validationError(variableEdit) != null, "Header-only text edit rejects attribute changes");
		check(adapter.validationError(with(draft, "1bad", draft.facets())) != null, "Invalid model name rejected");

		var creation = ModelSource.creation(parser);
		var created = with(creation.draft(), "my_model", List.of(new SpeciesDraft.Facet("frequency", "1")));
		String file = ModelSource.newModel(created, newDoc, "\n");
		check(file.contains("model my_model") && file.contains("global frequency: 1"), "New model contents");
		check(ModelDocumentation.read(file).equals(new ModelDocumentation("Bob", "tag", "First\nSecond")),
				"New model header readable");
		var reopened = new ModelSource(file, file.indexOf("global"), parser);
		check(reopened.draft().name().equals("my_model") && reopened.validationError(reopened.draft()) == null,
				"New model parses and validates");

		Document document = new Document(source);
		Object owner = new Object();
		DocumentUndoManagerRegistry.connect(document);
		var undo = DocumentUndoManagerRegistry.getDocumentUndoManager(document);
		undo.connect(owner);
		try {
			check(!new SpeciesDocumentEdit(document).apply(source), "No-op leaves no undo entry");
			check(!undo.undoable(), "No-op has no undo");
			new SpeciesDocumentEdit(document).apply(docSource);
			check(document.get().equals(docSource), "Documentation edit applied");
			undo.undo();
			check(document.get().equals(source), "Single undo restores model");
			undo.redo();
			check(document.get().equals(docSource), "Redo restores documentation");
		} finally {
			undo.disconnect(owner);
			DocumentUndoManagerRegistry.disconnect(document);
		}
		System.out.println("Model source/document regression: " + checks + " checks passed");
	}
}
