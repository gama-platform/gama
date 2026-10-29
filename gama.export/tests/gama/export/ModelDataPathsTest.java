package gama.export;

import java.nio.file.Path;
import java.util.Set;

import gaml.compiler.gaml.GamlFactory;
import gaml.compiler.gaml.Statement;

public final class ModelDataPathsTest {

	private static Statement statement(final String keyword, final String facetName, final String value) {
		final var factory = GamlFactory.eINSTANCE;
		final var statement = factory.createStatement();
		statement.setKey(keyword);
		final var facet = factory.createFacet();
		facet.setKey(facetName);
		final var literal = factory.createStringLiteral();
		literal.setOp(value);
		facet.setExpr(literal);
		statement.getFacets().add(facet);
		return statement;
	}

	public static void main(final String[] args) {
		final Path directory = Path.of("Example/models").toAbsolutePath();
		final var root = GamlFactory.eINSTANCE.createBlock();
		root.getStatements().add(statement("save", "to:", "../result/file.simulation"));
		root.getStatements().add(statement("save", "to:", "../data/shared.csv"));
		root.getStatements().add(statement("write", "message:", "../data/./shared.csv"));
		root.getStatements().add(statement("save", "item:", "../data/input.csv"));
		root.getStatements().add(statement("other", "to:", "../data/other.csv"));
		final Set<Path> outputs = ModelDataPaths.outputOnly(root, directory);
		if (!outputs.equals(Set.of(directory.resolve("../result/file.simulation").normalize())))
			throw new AssertionError("Exclude only direct save destinations never used as inputs: " + outputs);
		if (!ModelDataPaths.outputOnly(null, directory).isEmpty()) throw new AssertionError("No guessed outputs without syntax");
		System.out.println("Save destination versus required model data checks passed");
	}
}
