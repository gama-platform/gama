/*******************************************************************************************************
 *
 * TestStubGenerator.java, in gama.ui.devtools, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.devtools.quickfix;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import gama.ui.devtools.templates.CodeTemplates;
import gama.ui.devtools.util.GamaProjects;
import gama.ui.devtools.util.ResourceUtils;

/**
 * Generates, for each GAMA plugin, a GAML test model ({@code models/Tests/Untested Operators.gaml}) containing one test
 * stub per operator reported as "not tested" by the annotation processor. Existing stubs are kept: only the missing
 * ones are appended.
 */
public final class TestStubGenerator {

	/** The project-relative path of the generated model. */
	public static final String PATH = "models/Tests/Untested Operators.gaml";

	private TestStubGenerator() {}

	/** The untested operators of a plugin: GAML name -> Java signatures. */
	private static Map<String, List<String>> untested(final IProject project, final List<IMarker> only,
			final IProgressMonitor monitor) throws CoreException {
		Map<IFile, List<IMarker>> byFile = new LinkedHashMap<>();
		List<IMarker> markers = only != null ? only
				: List.of(project.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE));
		for (IMarker m : markers) {
			ProcessorWarning w = ProcessorWarning.of(m);
			if (w != null && w.kind() == ProcessorWarning.Kind.UNTESTED && m.getResource().getProject().equals(project)) {
				byFile.computeIfAbsent((IFile) m.getResource(), k -> new ArrayList<>()).add(m);
			}
		}
		Map<String, List<String>> result = new TreeMap<>();
		for (Map.Entry<IFile, List<IMarker>> e : byFile.entrySet()) {
			ICompilationUnit cu = JavaCore.createCompilationUnitFrom(e.getKey());
			CompilationUnit root = cu == null ? null : JavaSourceFixer.parse(cu, monitor);
			for (IMarker m : e.getValue()) {
				String op = ProcessorWarning.of(m).name();
				BodyDeclaration decl = root == null ? null : JavaSourceFixer.locate(root, m);
				result.computeIfAbsent(op, k -> new ArrayList<>())
						.add(decl instanceof MethodDeclaration md ? signature(md) : e.getKey().getName());
			}
		}
		return result;
	}

	/** A readable Java signature, without the IScope parameter: {@code Class.method(int, double) -> double}. */
	private static String signature(final MethodDeclaration m) {
		StringBuilder sb = new StringBuilder();
		ASTNode parent = m.getParent();
		if (parent instanceof AbstractTypeDeclaration t) { sb.append(t.getName().getIdentifier()).append('.'); }
		sb.append(m.getName().getIdentifier()).append('(');
		List<String> params = new ArrayList<>();
		for (Object o : m.parameters()) {
			String type = ((SingleVariableDeclaration) o).getType().toString();
			if (!type.endsWith("IScope")) { params.add(type); }
		}
		sb.append(String.join(", ", params)).append(')');
		if (m.getReturnType2() != null) { sb.append(" -> ").append(m.getReturnType2()); }
		return sb.toString();
	}

	/**
	 * Generates (or completes) the test stubs of the given plugins.
	 *
	 * @param projects
	 *            the plugins
	 * @param only
	 *            if not null, restricts the generation to the operators designated by these markers
	 * @return the files that have been created or modified
	 */
	public static List<IFile> generate(final List<IProject> projects, final List<IMarker> only,
			final IProgressMonitor monitor) throws CoreException {
		SubMonitor sub = SubMonitor.convert(monitor, "Generating GAML test stubs", projects.size());
		List<IFile> files = new ArrayList<>();
		for (IProject p : projects) {
			Map<String, List<String>> ops = untested(p, only, sub.split(1));
			if (ops.isEmpty()) { continue; }
			String content = ResourceUtils.read(p, PATH);
			StringBuilder tests = new StringBuilder();
			for (Map.Entry<String, List<String>> e : ops.entrySet()) {
				if (content != null && content.contains(testName(e.getKey()))) { continue; }
				tests.append(stub(e.getKey(), e.getValue()));
			}
			if (tests.isEmpty()) { continue; }
			if (content == null) {
				content = header(p);
			}
			int end = content.lastIndexOf('}');
			content = end < 0 ? content + tests : content.substring(0, end) + tests + content.substring(end);
			files.add(ResourceUtils.write(p, PATH, content, monitor));
		}
		return files;
	}

	private static String testName(final String op) {
		return "test \"" + op.replace("\"", "'") + "\"";
	}

	private static String stub(final String op, final List<String> signatures) {
		StringBuilder sb = new StringBuilder("\n\t").append(testName(op)).append(" {\n");
		for (String s : signatures) { sb.append("\t\t// Java: ").append(s).append('\n'); }
		sb.append("\t\t// TODO: assert ").append(op).append("(...) = ...;\n\t}\n");
		return sb.toString();
	}

	private static String header(final IProject p) {
		String model = CodeTemplates.gamlIdentifier(GamaProjects.shortcut(p.getName()) + "_untested_operators");
		return """
				/**
				* Name: %1$s
				* Test stubs generated by the GAMA Developer Tools for the operators of %2$s that are not tested.
				* Complete each test with real assertions. Then, to remove the "not tested" warning of the annotation
				* processor, annotate the Java method with @no_test(Reason.ALREADY_TESTED) (or move the assertions
				* to @test annotations on the method itself).
				* Tags: test
				*/

				model %1$s

				experiment "Untested operators of %2$s" type: test {
				}
				""".formatted(model, p.getName());
	}

}
