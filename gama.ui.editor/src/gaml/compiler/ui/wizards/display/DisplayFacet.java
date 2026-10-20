package gaml.compiler.ui.wizards.display;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import gama.annotations.facets;
import gama.api.additions.registries.ArtefactRegistry;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.utils.prefs.GamaPreferences;
import gama.core.outputs.LayeredDisplayOutput;
import gama.core.outputs.layers.AgentLayerStatement;
import gama.core.outputs.layers.GraphicLayerStatement;
import gama.core.outputs.layers.GridLayerStatement;
import gama.core.outputs.layers.ImageLayerStatement;
import gama.core.outputs.layers.SpeciesLayerStatement;
import gama.core.outputs.layers.MeshLayerStatement;
import gama.core.outputs.layers.OverlayStatement;
import gama.core.outputs.layers.properties.CameraStatement;
import gama.core.outputs.layers.properties.LightStatement;
import gama.core.outputs.layers.properties.RotationStatement;
import gama.gaml.statements.draw.DrawStatement;
import gaml.compiler.ui.wizards.species.SpeciesDraft.Facet;

/** Real compiler metadata, including multi-type facets; annotations are available even without an initialized registry. */
public record DisplayFacet(String name, String type, boolean flag, boolean required, String documentation) {
	/**
	 * Defaults from LayerData, GridLayerData, ImageLayerStatement, DrawingData, AbstractOutput and
	 * LayeredDisplayData. Inherited display facets and camera/deprecated settings cannot be resolved from a draft.
	 * Preferences are deliberately read on each call, not captured as factory defaults.
	 */
	public Boolean defaultValue(final String keyword, final List<Facet> values) {
		if (!DECLARATIONS.containsKey(keyword)) return null;
		if ("display".equals(keyword)) {
			if ((!"virtual".equals(name) && DisplayDraft.find(values, "parent") != null)
					|| "axes".equals(name) && DisplayDraft.find(values, "draw_env") != null) return null;
			return switch (name) {
				case "virtual" -> false;
				case "fullscreen", "autosave" -> false;
				case "refresh", "light" -> true;
				case "toolbar" -> GamaPreferences.Displays.CORE_DISPLAY_TOOLBAR.getValue();
				case "antialias" -> GamaPreferences.Displays.CORE_ANTIALIAS.getValue();
				case "axes", "draw_env" -> GamaPreferences.Displays.CORE_DRAW_ENV.getValue();
				default -> null;
			};
		}
		if ("draw".equals(keyword)) return switch (name) {
			case "lighted", "perspective" -> true;
			case "wireframe" -> false;
			default -> null;
		};
		return switch (name) {
			case "refresh" -> !"picture".equals(keyword);
			case "visible", "selectable" -> true;
			case "fading" -> false;
			case "wireframe", "triangulation", "smooth", "grayscale", "text" ->
				"display_grid".equals(keyword) ? false : null;
			default -> null;
		};
	}

	private static final Map<String, Class<?>> DECLARATIONS = Map.ofEntries(
			Map.entry("display", LayeredDisplayOutput.class), Map.entry("species_layer", SpeciesLayerStatement.class),
			Map.entry("agents", AgentLayerStatement.class), Map.entry("display_grid", GridLayerStatement.class),
			Map.entry("picture", ImageLayerStatement.class), Map.entry("graphics", GraphicLayerStatement.class),
			Map.entry("draw", DrawStatement.class), Map.entry("mesh", MeshLayerStatement.class),
			Map.entry("overlay", OverlayStatement.class), Map.entry("camera", CameraStatement.class),
			Map.entry("light", LightStatement.class), Map.entry("rotation", RotationStatement.class));

	public static List<DisplayFacet> of(final String keyword) {
		var registered = ArtefactRegistry.getStatementArtefact(keyword);
		List<DisplayFacet> result = new ArrayList<>();
		if (registered != null) {
			registered.getPossibleFacets().forEach((name, facet) -> {
				if (!facet.isInternal()) {
					var types = facet.getTypes();
					result.add(new DisplayFacet(name, String.join(" or ",
							Arrays.stream(types).map(t -> t.getName()).toList()),
							types.length == 1 && types[0].id() == IType.BOOL, !facet.isOptional(),
							facet.getDocumentation() == null ? "" : facet.getDocumentation().getContents()));
				}
			});
		} else {
			Class<?> declaration = DECLARATIONS.get(keyword);
			if (declaration == null) return List.of();
			for (var facet : declaration.getAnnotation(facets.class).value()) if (!facet.internal())
				result.add(new DisplayFacet(facet.name(),
						String.join(" or ", Arrays.stream(facet.type()).mapToObj(DisplayFacet::typeName).toList()),
						facet.type().length == 1 && facet.type()[0] == IType.BOOL, !facet.optional(),
						facet.doc().length == 0 ? "" : facet.doc()[0].value()));
		}
		return List.copyOf(result);
	}

	private static String typeName(final int id) {
		return switch (id) {
			case IType.ID -> "id";
			case IType.LABEL -> "label";
			case IType.NONE -> "expression";
			case IType.INT -> "int";
			case IType.FLOAT -> "float";
			case IType.BOOL -> "bool";
			case IType.STRING -> "string";
			case IType.LIST -> "list";
			case IType.COLOR -> "rgb";
			case IType.POINT -> "point";
			case IType.MATRIX -> "matrix";
			case IType.PAIR -> "pair";
			case IType.CONTAINER -> "container";
			case IType.AGENT -> "agent";
			case IType.SPECIES -> "species";
			case IType.FILE -> "file";
			case IType.FONT -> "font";
			default -> {
				var type = Types.get(id);
				yield type == null ? "expression" : type.getName();
			}
		};
	}
}
