package gaml.compiler.ui.wizards.display;

import java.util.HashSet;
import java.util.List;

import gaml.compiler.ui.wizards.experiment.ExperimentDraft;
import gaml.compiler.ui.wizards.species.SpeciesDraft;
import gaml.compiler.ui.wizards.species.SpeciesDraft.Facet;

/**
 * Immutable display settings and ordered layers. Name is decoded plain text; renderer and facets are raw GAML
 * (empty renderer means the default, not an explicit type). Body bytes after the last layer belong to trailingSource.
 */
public record DisplayDraft(String name, String renderer, List<Facet> facets, List<LayerDraft> layers,
		String trailingSource) {
	public DisplayDraft {
		name = name == null ? "" : name;
		renderer = renderer == null ? "" : renderer;
		facets = facets == null ? List.of() : List.copyOf(facets);
		layers = layers == null ? List.of() : List.copyOf(layers);
		trailingSource = trailingSource == null ? "" : trailingSource;
	}

	/** New displays start in 2D; user-facing true defaults must override contrary live preferences explicitly. */
	public static DisplayDraft empty() {
		List<Facet> defaults = new java.util.ArrayList<>();
		if (!gama.api.utils.prefs.GamaPreferences.Displays.CORE_DRAW_ENV.getValue())
			defaults.add(new Facet("axes", "true"));
		if (!gama.api.utils.prefs.GamaPreferences.Displays.CORE_DISPLAY_TOOLBAR.getValue())
			defaults.add(new Facet("toolbar", "true"));
		return new DisplayDraft("my_display", "java2D", defaults, List.of(), "\n");
	}

	public String validationError() {
		if (name.isBlank()) return "Display name is required.";
		if (name.contains("\n") || name.contains("\r")) return "Display name must be on one line.";
		String error = facetError(facets);
		if (error != null) return error;
		if (find(facets, "name") != null || find(facets, "type") != null) return "Use the Name and Renderer fields.";
		var ids = new HashSet<String>();
		for (var layer : layers) {
			if (!ids.add(layer.id())) return "Duplicate layer identity. Use Duplicate to copy a layer.";
			error = layer.validationError();
			if (error != null) return "Layer " + (layers.indexOf(layer) + 1) + ": " + error;
		}
		return null;
	}

	public String header() {
		StringBuilder text = new StringBuilder("display ").append(ExperimentDraft.quoteName(name));
		if (!renderer.isBlank()) text.append(" type: ").append(renderer);
		appendFacets(text, facets);
		return text.toString();
	}

	static void appendFacets(final StringBuilder text, final List<Facet> facets) {
		for (var facet : facets) text.append(' ').append(facet.name()).append(": ").append(facet.value());
	}

	public static String find(final List<Facet> facets, final String name) {
		for (var facet : facets) if (name.equals(facet.name())) return facet.value();
		return null;
	}

	static String facetError(final List<Facet> facets) {
		var names = new HashSet<String>();
		for (var facet : facets) {
			if (!SpeciesDraft.isIdentifier(facet.name())) return "Invalid facet name: " + facet.name();
			if (!names.add(facet.name())) return "Duplicate facet: " + facet.name();
			if (facet.value().isBlank()) return "Enter a value for " + facet.name() + ".";
		}
		return null;
	}
}
