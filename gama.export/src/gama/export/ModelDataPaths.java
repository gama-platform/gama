package gama.export;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;

import gaml.compiler.gaml.Facet;
import gaml.compiler.gaml.Statement;
import gaml.compiler.gaml.StringLiteral;

/** Distinguishes literal save destinations from data that must exist before execution. */
final class ModelDataPaths {

	private ModelDataPaths() {}

	static Set<Path> outputOnly(final EObject model, final Path directory) {
		final var outputs = new HashSet<Path>();
		final var inputs = new HashSet<Path>();
		if (model == null) return outputs;
		final var contents = model.eAllContents();
		while (contents.hasNext()) {
			if (!(contents.next() instanceof StringLiteral literal) || literal.getOp() == null) continue;
			final Path path;
			try {
				path = directory.resolve(literal.getOp().replace('\\', '/')).toAbsolutePath().normalize();
			} catch (final InvalidPathException e) {
				// Not every string in a model represents a filesystem path.
				continue;
			}
			if (literal.eContainer() instanceof Facet facet && "to:".equals(facet.getKey())
					&& facet.getExpr() == literal && facet.eContainer() instanceof Statement statement
					&& "save".equals(statement.getKey())) {
				outputs.add(path);
			} else {
				inputs.add(path);
			}
		}
		outputs.removeAll(inputs);
		return Set.copyOf(outputs);
	}
}
