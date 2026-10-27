package gaml.compiler.ui.hover;

import java.io.StringReader;
import java.lang.reflect.Proxy;
import java.util.List;

import org.eclipse.xtext.parser.IParser;

import gama.api.additions.registries.ArtefactRegistry;
import gama.api.compilation.artefacts.IArtefact;
import gama.api.compilation.documentation.GamlConstantDocumentation;
import gaml.compiler.GamlStandaloneSetup;
import gaml.compiler.gaml.S_Species;

public final class GamlClassHoverTest {
	private static int checks;

	private static final class Dispatch extends GamlHoverProvider.GamlDispatchingEObjectTextHover {
		org.eclipse.emf.ecore.EObject at(final org.eclipse.xtext.resource.XtextResource resource, final int offset) {
			return getXtextElementAt(resource, offset).getFirst();
		}
	}

	private static void check(final boolean condition, final String message) {
		if (!condition) throw new AssertionError(message);
		checks++;
	}

	public static void main(final String[] args) throws Exception {
		var injector = GamlStandaloneSetup.doSetup();
		IParser parser = injector.getInstance(IParser.class);
		var parsed = parser.parse(new StringReader("model sample\nclass record virtual: true { int n; }\n"));
		var provider = new GamlHoverDocumentationProvider();
		var nodes = parsed.getRootASTElement().eAllContents();
		S_Species declaration = null;
		while (nodes.hasNext()) if (nodes.next() instanceof S_Species value) declaration = value;
		if (declaration == null) throw new AssertionError("No class AST");
		var result = provider.getDoc(declaration);
		check(result != null && result.getTitle().contains("record"), "Class has contextual hover without model compilation");
		check(result.getDocumentation().toString().contains("attributes and actions"), "Class help describes capabilities");
		check(result.getDocumentation().toString().contains("implicit parent is <code>object</code>"), "Class root documented");
		check(result.getDocumentation().toString().contains("Species and grids cannot inherit"), "Inheritance boundary documented");
		check(result.getDocumentation().toString().contains("virtual: true"), "Class facet semantics documented");
		IArtefact.Symbol reference = (IArtefact.Symbol) Proxy.newProxyInstance(IArtefact.Symbol.class.getClassLoader(),
				new Class<?>[] { IArtefact.Symbol.class }, (proxy, method, arguments) -> switch (method.getName()) {
					case "getDocumentation" -> new GamlConstantDocumentation("Registered class reference");
					case "getKind" -> gama.annotations.support.ISymbolKind.CLASS;
					default -> throw new UnsupportedOperationException(method.getName());
				});
		ArtefactRegistry.addArtefact(reference, List.of("class"));
		check(provider.getDoc(declaration).getDocumentation().toString().contains("Registered class reference"),
				"Class hover includes registered declaration documentation");
		var incomplete = parser.parse(new StringReader("model m\nclass partial {"));
		var contents = incomplete.getRootASTElement().eAllContents();
		boolean found = false;
		while (contents.hasNext()) if (contents.next() instanceof S_Species value) {
			found = true;
			check(provider.getDoc(value).getDocumentation().toString().contains("Registered class reference"),
					"Incomplete class still has hover help");
		}
		check(found, "Incomplete class parse exercised");
		String source = "model sample\nclass record virtual: true { int n; }\n";
		var resource = injector.getInstance(org.eclipse.xtext.resource.XtextResource.class);
		resource.setURI(org.eclipse.emf.common.util.URI.createURI("memory:/class-hover.gaml"));
		var resourceParse = parser.parse(new StringReader(source));
		resource.setParseResult(resourceParse);
		resource.getContents().add(resourceParse.getRootASTElement());
		var dispatch = new Dispatch();
		for (String field : List.of("eObjectAtOffsetHelper", "locationInFileProvider")) {
			var member = GamlHoverProvider.GamlDispatchingEObjectTextHover.class.getDeclaredField(field);
			member.setAccessible(true);
			member.set(dispatch, injector.getInstance(member.getType()));
		}
		var target = dispatch.at(resource, source.indexOf("class"));
		check(target instanceof S_Species && provider.getDoc(target).getDocumentation().toString().contains("Registered class reference"),
				"Actual keyword hover dispatch reaches class documentation");
		target = dispatch.at(resource, source.indexOf("record"));
		check(target instanceof S_Species && provider.getDoc(target).getTitle().contains("record"),
				"Class name hover uses the same contextual documentation");
		var ordinary = parser.parse(new StringReader("model m\nspecies wolf {} grid cell width: 2 height: 2 {}\n"));
		var declarations = ordinary.getRootASTElement().eAllContents();
		int unchanged = 0;
		while (declarations.hasNext()) {
			if (declarations.next() instanceof S_Species value) {
				check(provider.caseS_Species(value) == null,
						"Class-specific documentation leaves existing " + value.getKey() + " hover dispatch unchanged");
				unchanged++;
			}
		}
		check(unchanged == 2, "Both species and grid hover preservation exercised");
		System.out.println("Class hover regression: " + checks + " checks passed");
	}
}
