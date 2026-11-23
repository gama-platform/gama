package gaml.compiler.transition;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.XtextPackage;
import org.eclipse.xtext.nodemodel.ILeafNode;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.parser.IParser;
import org.eclipse.xtext.resource.impl.BinaryGrammarResourceFactoryImpl;

import gaml.compiler.GamlStandaloneSetupGenerated;
import gaml.compiler.gaml.Facet;
import gaml.compiler.gaml.FacetsAndBlock;
import gaml.compiler.gaml.GamlPackage;

/**
 * Updates deprecated selector facets and the former layer content coordinate facet in parseable GAML source.
 */
final class FixLegacyFacets implements IFileTransformer {

	private static class Parser {
		private static final IParser INSTANCE = create();

		private static IParser create() {
			// Load the binary grammar without replacing the IDE's GAML resource service provider.
			XtextPackage.eINSTANCE.eClass();
			GamlPackage.eINSTANCE.eClass();
			Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap()
					.putIfAbsent("xtextbin", new BinaryGrammarResourceFactoryImpl());
			return new GamlStandaloneSetupGenerated().createInjector().getInstance(IParser.class);
		}
	}

	private record Edit(int start, int end, String replacement) {}

	@Override
	public String transform(final String content) {
		if (!content.contains("type") && !content.contains("content_coordinates")) return content;
		final var parsed = Parser.INSTANCE.parse(new StringReader(content));
		final var root = parsed.getRootASTElement();
		// Recovery nodes are not reliable source ranges for in-place migration.
		if (root == null || parsed.hasSyntaxErrors()) return content;

		final List<Edit> edits = new ArrayList<>();
		if (root instanceof FacetsAndBlock statement) collectEdits(content, statement, edits);
		for (final var children = root.eAllContents(); children.hasNext();) {
			if (children.next() instanceof FacetsAndBlock statement) collectEdits(content, statement, edits);
		}
		if (edits.isEmpty()) return content;
		edits.sort(Comparator.comparingInt(Edit::start).reversed());
		final var result = new StringBuilder(content);
		for (final Edit edit : edits) {
			result.replace(edit.start(), edit.end(), edit.replacement());
		}
		return result.toString();
	}

	private static void collectEdits(final String content, final FacetsAndBlock statement, final List<Edit> edits) {
		final String keyword = statement.getKey();
		if (keyword == null) return;
		switch (keyword) {
			case "experiment" -> renameFacet(statement, "type:", "mode:", edits);
			case "display" -> renameFacet(statement, "type:", "renderer:", edits);
			case "chart", "light" -> renameFacet(statement, "type:", "kind:", edits);
			case "event" -> renameFacet(statement, "type:", "device:", edits);
			case "inspect", "browse" -> migrateInspection(content, statement, edits);
			case "graphics", "overlay" -> renameFacet(statement, "content_coordinates:", "referential:", edits);
		}
	}

	private static Facet uniqueFacet(final FacetsAndBlock statement, final String key) {
		Facet found = null;
		for (final Facet facet : statement.getFacets()) {
			if (hasKey(facet, key)) {
				if (found != null) return null;
				found = facet;
			}
		}
		return found;
	}

	private static INode keyNode(final Facet facet) {
		return NodeModelUtils.findNodesForFeature(facet, GamlPackage.Literals.FACET__KEY).getFirst();
	}

	private static boolean hasKey(final Facet facet, final String key) {
		if (key.equals(facet.getKey())) return true;
		// Xtext data type rules can include the hidden text between the identifier and colon.
		if (!facet.getKey().endsWith(":")) return false;
		final var spelling = new StringBuilder();
		for (final ILeafNode leaf : keyNode(facet).getLeafNodes()) {
			if (!leaf.isHidden()) spelling.append(leaf.getText());
		}
		return key.contentEquals(spelling);
	}

	private static void renameFacet(final FacetsAndBlock statement, final String oldKey, final String newKey,
			final List<Edit> edits) {
		final Facet legacy = uniqueFacet(statement, oldKey);
		if (legacy == null || statement.getFacets().stream().anyMatch(f -> hasKey(f, newKey))) return;
		// Edit just the identifier: whitespace or comments before the colon can remain as written.
		for (final ILeafNode leaf : keyNode(legacy).getLeafNodes()) {
			if (!leaf.isHidden()) {
				final boolean includesColon = leaf.getText().endsWith(":");
				edits.add(new Edit(leaf.getOffset(), leaf.getEndOffset(),
						includesColon ? newKey : newKey.substring(0, newKey.length() - 1)));
				break;
			}
		}
	}

	private static void migrateInspection(final String content, final FacetsAndBlock statement,
			final List<Edit> edits) {
		final Facet type = uniqueFacet(statement, "type:");
		if (type == null || type.getExpr() == null) return;
		final String replacement = switch (NodeModelUtils.getTokenText(NodeModelUtils.getNode(type.getExpr()))) {
			case "agent", "'agent'", "\"agent\"" -> "inspect";
			case "table", "'table'", "\"table\"" -> "browse";
			default -> null;
		};
		if (replacement == null) return;
		if (!replacement.equals(statement.getKey())) {
			final INode key =
					NodeModelUtils.findNodesForFeature(statement, GamlPackage.Literals.FACETS_AND_BLOCK__KEY).getFirst();
			edits.add(new Edit(key.getOffset(), key.getEndOffset(), replacement));
		}

		final INode facet = NodeModelUtils.getNode(type);
		final List<ILeafNode> tokens = new ArrayList<>();
		boolean hasComment = false;
		for (final ILeafNode leaf : facet.getLeafNodes()) {
			if (!leaf.isHidden()) {
				tokens.add(leaf);
			} else if (leaf.getText().startsWith("/")) {
				hasComment = true;
			}
		}
		int start = tokens.getFirst().getOffset();
		final int end = tokens.getLast().getEndOffset();
		if (hasComment) {
			for (final ILeafNode token : tokens) {
				edits.add(new Edit(token.getOffset(), token.getEndOffset(), ""));
			}
		} else {
			while (start > 0 && (content.charAt(start - 1) == ' ' || content.charAt(start - 1) == '\t')) {
				start--;
			}
			edits.add(new Edit(start, end, ""));
		}
	}
}
