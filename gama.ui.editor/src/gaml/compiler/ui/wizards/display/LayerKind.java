package gaml.compiler.ui.wizards.display;

/** Structured display layers. TEXT is a single text draw in a graphics layer, not a GAML text-layer keyword. */
public enum LayerKind {
	SPECIES("species", "species_layer", "species"),
	AGENTS("agents", "agents", "name"),
	GRID("grid", "display_grid", "species"),
	IMAGE("image", "picture", "name"),
	TEXT("graphics", "graphics", null),
	RAW("", "", null);

	private final String keyword;
	private final String compilerKeyword;
	private final String expressionFacet;

	LayerKind(final String keyword, final String compilerKeyword, final String expressionFacet) {
		this.keyword = keyword;
		this.compilerKeyword = compilerKeyword;
		this.expressionFacet = expressionFacet;
	}

	public String keyword() { return keyword; }
	public String compilerKeyword() { return compilerKeyword; }
	public String expressionFacet() { return expressionFacet; }
}
