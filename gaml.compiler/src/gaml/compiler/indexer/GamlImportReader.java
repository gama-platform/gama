package gaml.compiler.indexer;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.parser.IParser;
import org.eclipse.xtext.resource.IResourceServiceProvider;

import gaml.compiler.gaml.Model;
import gaml.compiler.gaml.StandaloneExperiment;
import gaml.compiler.resource.GamlFileInfo;
import gaml.compiler.resource.GamlResourceServices;

/** Reads saved imports and optional literal resource uses, without linking or semantic compilation. */
public final class GamlImportReader {

	public record ImportReference(URI uri, String alias, int line) {}

	public record Result(List<ImportReference> imports, List<String> diagnostics, List<String> uses) {
		public Result {
			imports = List.copyOf(imports);
			diagnostics = List.copyOf(diagnostics);
			uses = List.copyOf(uses);
		}
	}

	private GamlImportReader() {}

	public static URI resolveImport(final URI base, final String target) {
		return GamlResourceServices.properlyEncodedURI(URI.createURI(target, true).resolve(base));
	}

	public static Result read(final URI uri, final Reader source) throws IOException {
		return read(uri, source, false);
	}

	public static Result read(final URI uri, final Reader source, final boolean includeResources) throws IOException {
		final var provider = IResourceServiceProvider.Registry.INSTANCE.getResourceServiceProvider(uri);
		if (provider == null) throw new IOException("No GAML parser is registered for " + uri);
		final IParser parser = provider.get(IParser.class);
		if (parser == null) throw new IOException("No GAML parser is available for " + uri);
		final var parsed = parser.parse(source);
		final var imports = new ArrayList<ImportReference>();
		final var diagnostics = new ArrayList<String>();
		for (final var error : parsed.getSyntaxErrors()) {
			diagnostics.add("Line " + error.getStartLine() + ": " + error.getSyntaxErrorMessage().getMessage());
		}
		if (parsed.getRootASTElement() instanceof Model model) {
			for (final var declaration : model.getImports()) {
				final var node = NodeModelUtils.getNode(declaration);
				add(imports, diagnostics, uri, declaration.getImportURI(), declaration.getName(),
						node == null ? 0 : node.getStartLine());
			}
		} else if (parsed.getRootASTElement() instanceof StandaloneExperiment experiment) {
			add(imports, diagnostics, uri, experiment.getImportURI(), null, 1);
		} else {
			diagnostics.add("No model or standalone experiment could be parsed");
		}
		return new Result(imports, diagnostics,
				includeResources ? new ArrayList<>(GamlFileInfo.extractUses(parsed.getRootNode().getText())) : List.of());
	}

	private static void add(final List<ImportReference> imports, final List<String> diagnostics, final URI base,
			final String target, final String alias, final int line) {
		if (target == null || target.isBlank()) {
			diagnostics.add("Line " + line + ": missing import path");
			return;
		}
		try {
			imports.add(new ImportReference(resolveImport(base, target), alias, line));
		} catch (final IllegalArgumentException e) {
			diagnostics.add("Line " + line + ": invalid import '" + target + "': " + e.getMessage());
		}
	}
}
