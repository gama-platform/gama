package gama.export;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;

import gama.api.gaml.GAML;
import gaml.compiler.gaml.Access;
import gaml.compiler.gaml.BinaryOperator;
import gaml.compiler.gaml.Facet;
import gaml.compiler.gaml.If;
import gaml.compiler.gaml.Import;
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
			if (isSaveTarget(literal)) {
				outputs.add(path);
			} else {
				inputs.add(path);
			}
		}
		outputs.removeAll(inputs);
		return Set.copyOf(outputs);
	}

	/** Paths of data files referenced by a literal that is not part of a larger expression, hence statically known. */
	static Set<Path> staticInputs(final EObject model, final Path directory) {
		final var inputs = new HashSet<Path>();
		if (model == null) return inputs;
		final var contents = model.eAllContents();
		while (contents.hasNext()) {
			if (!(contents.next() instanceof StringLiteral literal) || literal.getOp() == null || isSaveTarget(literal)
					|| isComputed(literal))
				continue;
			try {
				inputs.add(directory.resolve(literal.getOp().replace('\\', '/')).toAbsolutePath().normalize());
			} catch (final InvalidPathException e) {
				// Not every string in a model represents a filesystem path.
			}
		}
		return inputs;
	}

	static int sourceLine(final EObject model, final Path directory, final Path target) {
		final Iterator<EObject> contents = model.eAllContents();
		while (contents.hasNext()) {
			if (!(contents.next() instanceof StringLiteral literal) || literal.getOp() == null || isSaveTarget(literal)
					|| isComputed(literal))
				continue;
			try {
				final Path path = directory.resolve(literal.getOp().replace('\\', '/')).toAbsolutePath().normalize();
				if (path.equals(target)) {
					return GAML.getSourceLocation(literal).line();
				}
			} catch (final InvalidPathException e) {
				// Not every string in a model represents a filesystem path.
			}
		}
		return -1;
	}

	static int importLine(final EObject model, final String imported) {
		final String importPath = org.eclipse.emf.common.util.URI.decode(imported).replace('\\', '/');
		final Iterator<EObject> contents = model.eAllContents();
		while (contents.hasNext()) {
			if (!(contents.next() instanceof Import modelImport) || modelImport.getImportURI() == null) continue;
			final String sourcePath = modelImport.getImportURI().replace('\\', '/');
			if (!sourcePath.equals(importPath) && !sourcePath.endsWith("/" + importPath)
					&& !importPath.endsWith("/" + sourcePath)) continue;
			return GAML.getSourceLocation(modelImport).line();
		}
		return -1;
	}

	/** Whether the literal is only an operand (e.g. of a concatenation, a conditional or an access) of a computed path. */
	private static boolean isComputed(final StringLiteral literal) {
		final EObject container = literal.eContainer();
		return container instanceof BinaryOperator operator && !"::".equals(operator.getOp())
				|| container instanceof If || container instanceof Access;
	}

	private static boolean isSaveTarget(final StringLiteral literal) {
		return literal.eContainer() instanceof Facet facet && "to:".equals(facet.getKey())
				&& facet.getExpr() == literal && facet.eContainer() instanceof Statement statement
				&& "save".equals(statement.getKey());
	}
}
