package gaml.compiler.ui.wizards.species;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.nodemodel.ILeafNode;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.parser.IParser;

import gaml.compiler.gaml.Facet;
import gaml.compiler.gaml.Model;
import gaml.compiler.gaml.S_Global;

/**
 * Lossless source adapter for the model name, the global header and the model documentation comment. Global bodies,
 * other declarations and unrelated comments are never serialized; only changed tokens are replaced.
 */
public final class ModelSource {

	private final String source;
	private final IParser parser;
	private final Model model;
	private final S_Global global;
	private final SpeciesDraft original;
	private final ModelDocumentation documentation;

	private ModelSource(final IParser parser) {
		source = null;
		this.parser = parser;
		model = null;
		global = null;
		original = SpeciesDraft.empty();
		documentation = ModelDocumentation.EMPTY;
	}

	/** Selects the global declaration enclosing {@code offset}. */
	public ModelSource(final String source, final int offset, final IParser parser) {
		this.source = Objects.requireNonNull(source);
		this.parser = parser;
		var parsed = parser.parse(new StringReader(source));
		if (parsed.hasSyntaxErrors()) throw new IllegalArgumentException("Fix the document's syntax errors first.");
		if (!(parsed.getRootASTElement() instanceof Model root))
			throw new IllegalArgumentException("Model settings require a GAML model, not an experiment file.");
		model = root;
		S_Global selected = null;
		if (model.getBlock() != null) for (EObject statement : model.getBlock().getStatements()) {
			if (statement instanceof S_Global candidate) {
				INode node = NodeModelUtils.findActualNodeFor(candidate);
				if (offset >= node.getOffset() && offset < node.getEndOffset()) selected = candidate;
			}
		}
		if (selected == null) throw new IllegalArgumentException("Select the global declaration.");
		if (selected.getBlock() == null)
			throw new IllegalArgumentException("This global has no editable block. Edit its declaration in the source.");
		global = selected;
		original = SpeciesSource.readHeader(global, model.getName());
		documentation = ModelDocumentation.read(source);
	}

	/** Adapter for a model file that does not exist yet; see {@link #newModel(SpeciesDraft, ModelDocumentation, String)}. */
	public static ModelSource creation(final IParser parser) {
		return new ModelSource(parser);
	}

	public SpeciesDraft draft() { return original; }

	public ModelDocumentation documentation() { return documentation; }

	/** True for a selection in the global header (before its block) or covering the whole global declaration. */
	public boolean isGlobalSelection(final int offset, final int length) {
		if (global == null || offset < 0 || length < 0) return false;
		INode node = NodeModelUtils.findActualNodeFor(global);
		INode block = NodeModelUtils.findActualNodeFor(global.getBlock());
		long end = (long) offset + length;
		return offset >= node.getOffset() && end <= block.getOffset()
				|| offset <= node.getOffset() && end >= node.getEndOffset();
	}

	public SpeciesWizardContext context() { return SpeciesWizardContext.builtIns(); }

	public String validationError(final SpeciesDraft draft) {
		String error = draft.validationError();
		if (error != null) return error.startsWith("Species name") ? "Model name must be a GAML identifier." : error;
		for (var facet : draft.facets()) {
			if (!isExpression(facet.value())) return "Enter a single valid GAML expression for facet " + facet.name() + ".";
		}
		for (var variable : draft.variables()) {
			String[] values = { variable.init(), variable.update(), variable.function(), variable.min(), variable.max() };
			String[] labels = { "init", "update", "function", "min", "max" };
			for (int i = 0; i < values.length; i++) {
				if (!values[i].isBlank() && !isExpression(values[i]))
					return "Enter a single valid GAML expression for " + variable.name() + " " + labels[i] + ".";
			}
		}
		if (global != null && (!draft.variables().equals(original.variables()) || !draft.init().equals(original.init())
				|| !draft.reflexOrder().equals(original.reflexOrder())))
			return "Edit existing global declarations in the source editor.";
		if (global == null && !draft.reflexOrder().isEmpty()) return "A new model has no reflexes to reorder.";
		String candidate = global == null ? newModel(draft, ModelDocumentation.EMPTY, "\n") : render(draft, documentation);
		var parsed = parser.parse(new StringReader(candidate));
		for (INode node : parsed.getSyntaxErrors())
			return "GAML syntax error at line " + node.getStartLine() + ": " + node.getSyntaxErrorMessage().getMessage();
		return null;
	}

	private boolean isExpression(final String value) {
		var parsed = parser.parse(new StringReader("model __model_wizard\nglobal frequency: " + value + " {}"));
		if (parsed.hasSyntaxErrors() || !(parsed.getRootASTElement() instanceof Model root)
				|| root.getBlock().getStatements().size() != 1) return false;
		return root.getBlock().getStatements().getFirst() instanceof S_Global target && target.getFacets().size() == 1
				&& target.getBlock() != null && target.getBlock().getStatements().isEmpty();
	}

	/** Complete snapshot of the edited source. Unchanged draft and documentation return the source itself. */
	public String render(final SpeciesDraft draft, final ModelDocumentation edited) {
		if (global == null) throw new IllegalStateException("Use newModel for a model that does not exist yet.");
		List<SpeciesSource.Patch> patches = new ArrayList<>();
		if (!draft.name().equals(original.name())) {
			INode node = NodeModelUtils.findNodesForFeature(model, model.eClass().getEStructuralFeature("name")).getFirst();
			patches.add(new SpeciesSource.Patch(node.getOffset(), node.getLength(), draft.name()));
		}
		Map<String, String> before = SpeciesSource.header(original);
		Map<String, String> after = SpeciesSource.header(draft);
		var existing = new HashSet<String>();
		for (Facet facet : global.getFacets()) {
			String key = SpeciesSource.key(facet);
			existing.add(key);
			if ("skills".equals(key) && original.skills().equals(draft.skills())) continue;
			if (Objects.equals(before.get(key), after.get(key))) continue;
			if (after.containsKey(key)) SpeciesSource.replaceTokens(patches, NodeModelUtils.findActualNodeFor(facet.getExpr()), after.get(key));
			else SpeciesSource.replaceTokens(patches, NodeModelUtils.findActualNodeFor(facet), "");
		}
		StringBuilder additions = new StringBuilder();
		after.forEach((key, value) -> {
			if (!existing.contains(key)) additions.append(key).append(": ").append(value).append(' ');
		});
		if (!additions.isEmpty()) {
			INode block = NodeModelUtils.findActualNodeFor(global.getBlock());
			patches.add(new SpeciesSource.Patch(block.getOffset(), 0, additions.toString()));
		}
		documentationPatch(draft.name(), Objects.requireNonNull(edited), patches);
		patches.sort(Comparator.comparingInt(SpeciesSource.Patch::offset).reversed());
		StringBuilder result = new StringBuilder(source);
		for (var patch : patches) result.replace(patch.offset(), patch.offset() + patch.length(), patch.text());
		return result.toString();
	}

	private void documentationPatch(final String name, final ModelDocumentation edited,
			final List<SpeciesSource.Patch> patches) {
		int[] range = ModelDocumentation.headerRange(source);
		String newline = newline();
		if (range == null) {
			if (edited.isEmpty()) return;
			int first = firstToken();
			patches.add(new SpeciesSource.Patch(first, 0, edited.comment(name, newline) + newline + newline));
		} else if (!edited.equals(documentation)) {
			patches.add(new SpeciesSource.Patch(range[0], range[1] - range[0], edited.comment(name, newline)));
		} else if (!name.equals(original.name())) {
			// Only the Name line follows a rename; the rest of the comment is untouched.
			var matcher = java.util.regex.Pattern.compile("(?im)^([\\s*]*Name:[ \\t]*)([^\\r\\n]*)")
					.matcher(source).region(range[0], range[1]);
			if (matcher.find()) patches.add(new SpeciesSource.Patch(matcher.start(2), matcher.end(2) - matcher.start(2), name));
		}
	}

	private int firstToken() {
		for (ILeafNode leaf : NodeModelUtils.getNode(model).getLeafNodes()) if (!leaf.isHidden()) return leaf.getOffset();
		return 0;
	}

	private String newline() { return source.contains("\r\n") ? "\r\n" : "\n"; }

	/** Complete content of a new model file: header comment, model, global and a GUI experiment. */
	public static String newModel(final SpeciesDraft draft, final ModelDocumentation documentation, final String newline) {
		return documentation.comment(draft.name(), newline) + newline + newline + "model " + draft.name() + newline
				+ newline + SpeciesSource.declaration(draft, newline, SpeciesWizard.Kind.MODEL) + newline + newline
				+ "experiment " + draft.name() + " type: gui {" + newline + "\toutput {" + newline + "\t}" + newline + "}"
				+ newline;
	}
}
