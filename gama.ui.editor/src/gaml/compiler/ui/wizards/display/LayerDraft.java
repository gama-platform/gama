package gaml.compiler.ui.wizards.display;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import gaml.compiler.ui.wizards.species.SpeciesDraft.Facet;

/**
 * Detached layer. The opaque id is retained across edits/moves, never reused for a duplicate. Source includes leading
 * whitespace/comments and same-line trailing comments, retained even on structured rows for token-only edits.
 * RAW rows are opaque, not editable in the form. Expressions and facet values are raw GAML;
 * TEXT uses graphics facets and separate draw facets. Body is block contents only, never the whole declaration.
 */
public record LayerDraft(String id, LayerKind kind, String expression, List<Facet> facets, List<Facet> textFacets,
		String source, String body) {

	/** Compatibility constructor for leaf layers and existing adapters. Body is block contents only. */
	public LayerDraft(final String id, final LayerKind kind, final String expression, final List<Facet> facets,
			final List<Facet> textFacets, final String source) {
		this(id, kind, expression, facets, textFacets, source, "");
	}

	public LayerDraft {
		Objects.requireNonNull(id);
		Objects.requireNonNull(kind);
		expression = expression == null ? "" : expression;
		facets = facets == null ? List.of() : List.copyOf(facets);
		textFacets = textFacets == null ? List.of() : List.copyOf(textFacets);
		source = source == null ? "" : source;
		body = body == null ? "" : body;
	}

	public static LayerDraft empty(final LayerKind kind) {
		return new LayerDraft(UUID.randomUUID().toString(), kind, kind == LayerKind.TEXT ? "\"Text\"" : "",
				List.of(), List.of(), "");
	}

	public LayerDraft duplicate() {
		return new LayerDraft(UUID.randomUUID().toString(), kind, expression, facets, textFacets, source, body);
	}

	public String validationError() {
		if (id.isBlank()) return "A layer needs an identity.";
		if (kind == LayerKind.RAW) return source.isBlank() ? "Enter the layer's GAML source." : null;
		if (expression.isBlank() && (kind == LayerKind.SPECIES || kind == LayerKind.GRID
				|| kind == LayerKind.TEXT || kind == LayerKind.MESH || kind == LayerKind.CAMERA
				|| kind == LayerKind.LIGHT || kind == LayerKind.ROTATION))
			return "Enter the " + (kind == LayerKind.TEXT ? "text expression"
					: kind == LayerKind.MESH ? "mesh source" : kind == LayerKind.ROTATION ? "rotation angle"
					: kind == LayerKind.CAMERA || kind == LayerKind.LIGHT ? "name" : "species") + ".";
		if (!kind.hasBody() && !body.isEmpty()) return "This layer kind does not accept a body.";
		if (kind != LayerKind.TEXT && !textFacets.isEmpty()) return "Draw facets belong to text layers only.";
		String error = DisplayDraft.facetError(facets);
		if (error != null) return error;
		if (kind.expressionFacet() != null && DisplayDraft.find(facets, kind.expressionFacet()) != null)
			return "Use the expression field for " + kind.expressionFacet() + ".";
		error = DisplayDraft.facetError(textFacets);
		if (error != null) return error;
		if (DisplayDraft.find(textFacets, "geometry") != null) return "Use the text expression field.";
		for (var facet : DisplayFacet.of(kind.compilerKeyword()))
			if (facet.required() && !facet.name().equals(kind.expressionFacet())
					&& DisplayDraft.find(facets, facet.name()) == null)
				return "Enter a value for " + facet.name() + ".";
		return null;
	}
}
