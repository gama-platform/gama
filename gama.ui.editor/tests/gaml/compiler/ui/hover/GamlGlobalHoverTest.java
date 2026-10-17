package gaml.compiler.ui.hover;

import java.io.StringReader;
import java.lang.reflect.Proxy;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.parser.IParser;

import gama.api.additions.registries.ArtefactRegistry;
import gama.api.compilation.artefacts.IArtefact;
import gama.api.compilation.descriptions.IGamlDescription;
import gama.api.compilation.documentation.GamlConstantDocumentation;
import gaml.compiler.GamlStandaloneSetup;
import gaml.compiler.gaml.Model;
import gaml.compiler.gaml.S_Global;

/** Standalone regression for global documentation with and without compiled model context. */
public final class GamlGlobalHoverTest {

	private static int checks;

	private static void check(final boolean condition, final String message) {
		checks++;
		if (!condition) throw new AssertionError(message);
	}

	public static void main(final String[] args) {
		IParser parser = GamlStandaloneSetup.doSetup().getInstance(IParser.class);
		var parsed = parser.parse(new StringReader("model sample\nglobal { init { write 1; } }\n"));
		S_Global global = null;
		var contents = parsed.getRootASTElement().eAllContents();
		while (contents.hasNext()) if (contents.next() instanceof S_Global value) global = value;
		if (global == null) throw new AssertionError("Parser did not produce global");

		var provider = new GamlHoverDocumentationProvider();
		var uncompiled = provider.getDoc(global);
		check(uncompiled.getTitle().contains("sample"), "Uncompiled model keeps a contextual title");
		check(uncompiled.getDocumentation().toString().contains("world (simulation) species"),
				"Global documentation is useful before reference registration");

		IArtefact.Symbol modelReference = (IArtefact.Symbol) Proxy.newProxyInstance(
				IArtefact.Symbol.class.getClassLoader(), new Class<?>[] { IArtefact.Symbol.class },
				(proxy, method, arguments) -> switch (method.getName()) {
					case "getDocumentation" -> new GamlConstantDocumentation("World reference: torus, skills, schedules.");
					case "getKind" -> gama.annotations.support.ISymbolKind.MODEL;
					default -> throw new UnsupportedOperationException(method.getName());
				});
		ArtefactRegistry.addArtefact(modelReference, List.of("model"));
		var reference = provider.getDoc(global);
		check(reference.getDocumentation().toString().contains("World reference: torus, skills, schedules."),
				"Global includes registered model reference documentation");
		check(reference.getDocumentation().toString().contains("do not apply here"),
				"Global distinguishes grid-only settings");

		var compiledProvider = new GamlHoverDocumentationProvider() {
			@Override
			IGamlDescription getDoc(final EObject object) {
				return object instanceof Model ? new Result("Model sample (compiled)", "") : super.getDoc(object);
			}
		};
		var compiled = compiledProvider.getDoc(global);
		check(compiled.getTitle().contains("Model sample (compiled)"), "Compiled model title is preserved");
		check(compiled.getDocumentation().toString().contains("World reference"),
				"Compiled global retains reference documentation");

		var incomplete = parser.parse(new StringReader("model incomplete\nglobal { init {"));
		var nodes = incomplete.getRootASTElement().eAllContents();
		boolean foundIncomplete = false;
		while (nodes.hasNext()) if (nodes.next() instanceof S_Global value) {
			foundIncomplete = true;
			check(provider.getDoc(value).getDocumentation().toString().contains("World reference"),
					"Incomplete global still has reference help");
		}
		check(foundIncomplete, "Incomplete parse exercises the global hover");
		System.out.println("Global hover regression: " + checks + " checks passed");
	}
}
