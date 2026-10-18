package gaml.compiler.ui.wizards.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.diagnostics.AbstractDiagnostic;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.util.LazyStringInputStream;

import gaml.compiler.indexer.GamlResourceIndexer;
import gaml.compiler.resource.GamlResource;
import gaml.compiler.resource.GamlResourceServices;

/**
 * Compiles {@code prefix + body + suffix} in a fresh, private resource set under the context's isolated URI, then
 * discards it (validation context, import edges and resources). Never touches editor resources or markers.
 */
public final class ActionVerifier {

	/** A compilation error; {@code bodyLine} is 1-based within the body, or 0 when outside the body. */
	public record Problem(String message, boolean inBody, int bodyLine) {
		@Override
		public String toString() {
			return inBody ? "Code line " + bodyLine + ": " + message : message;
		}
	}

	private ActionVerifier() {}

	public static List<Problem> verify(final ActionBodyContext context, final String body) {
		String text = context.text(body);
		List<Problem> problems = new ArrayList<>();
		XtextResourceSet set = new XtextResourceSet();
		Resource created = set.createResource(context.uri());
		if (!(created instanceof GamlResource resource))
			throw new IllegalStateException("The GAML compiler is not available to verify the code.");
		try {
			resource.load(new LazyStringInputStream(text, resource.getEncoding()), Map.of());
			EcoreUtil.resolveAll(resource);
			for (Resource.Diagnostic diagnostic : resource.getErrors()) problems.add(problem(context, text,
					diagnostic.getMessage(), diagnostic instanceof AbstractDiagnostic d ? d.getOffset() : -1,
					diagnostic.getLine()));
			if (problems.isEmpty()) {
				try {
					resource.validate();
				} catch (RuntimeException error) {
					problems.add(new Problem(String.valueOf(error.getMessage()), false, 0));
				}
				for (var error : resource.getValidationContext().getInternalErrors()) {
					if (!error.isError()) continue;
					var node = error.source() == null ? null : NodeModelUtils.findActualNodeFor(error.source());
					problems.add(problem(context, text, error.message(), node == null ? -1 : node.getOffset(),
							node == null ? -1 : node.getStartLine()));
				}
			}
		} catch (java.io.IOException error) {
			problems.add(new Problem("Cannot read the code: " + error.getMessage(), false, 0));
		} finally {
			dispose(resource);
		}
		return List.copyOf(problems);
	}

	private static Problem problem(final ActionBodyContext context, final String text, final String message,
			final int offset, final int line) {
		int start = context.prefix().length();
		int end = text.length() - context.suffix().length();
		int prefixLine = (int) context.prefix().chars().filter(c -> c == '\n').count() + 1;
		if (offset >= start && offset <= end) {
			int bodyLine = (int) text.substring(start, offset).chars().filter(c -> c == '\n').count() + 1;
			return new Problem(message, true, bodyLine);
		}
		if (offset < 0 && line >= prefixLine) {
			int bodyLines = (int) text.substring(start, end).chars().filter(c -> c == '\n').count();
			if (line <= prefixLine + bodyLines) return new Problem(message, true, line - prefixLine + 1);
		}
		return new Problem(message, false, 0);
	}

	private static void dispose(final GamlResource resource) {
		try {
			GamlResourceServices.discardValidationContext(resource);
			// Removes the import edges this isolated URI added to the shared import graph.
			resource.reparse("model action_wizard_disposed");
			GamlResourceIndexer.updateImports(resource);
			GamlResourceServices.discardValidationContext(resource);
		} catch (Exception ignored) {
			// best effort cleanup
		} finally {
			var set = resource.getResourceSet();
			for (Resource r : List.copyOf(set.getResources())) r.unload();
			set.getResources().clear();
		}
	}
}
