/**
 *
 */
package gaml.compiler.gaml.resource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

import gama.api.compilation.descriptions.IDescription;
import gama.api.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.GAML;
import gama.api.runtime.scope.IExecutionContext;
import gama.api.utils.collections.Collector;
import gaml.compiler.gaml.StringEvaluator;
import gaml.compiler.gaml.ast.SyntacticModelElement;

/**
 *
 */
public class GamlSyntheticResourcesServices {

	/**
	 * Parses a string expression into an EObject by creating a temporary GAML resource.
	 */
	public static EObject getEObjectOf(final String string, final IExecutionContext tempContext, final IDescription ctx)
			throws GamaRuntimeException {
		EObject result = null;
		final String s = "dummy <- " + string;
		final GamlResource resource = GamlResourceServices.getTemporaryResource(ctx);
		try {
			final InputStream is = new ByteArrayInputStream(s.getBytes());
			try {
				resource.loadSynthetic(is, tempContext);
			} catch (final Exception e1) {
				e1.printStackTrace();
				return null;
			}
			if (resource.hasErrors()) {
				final Resource.Diagnostic d = resource.getErrors().get(0);
				throw GamaRuntimeException.error(d.getMessage(), tempContext.getScope());
			}
			final EObject e = resource.getContents().get(0);
			if (e instanceof StringEvaluator) { result = ((StringEvaluator) e).getExpr(); }

			return result;
		} finally {
			GamlResourceServices.discardTemporaryResource(resource);
		}
	}

	/**
	 * Compiles a block of GAML statements from a string into a list of description objects. This method parses a string
	 * containing multiple GAML statements or a single complex statement and creates a list of IDescription objects that
	 * can be executed sequentially. It is primarily used for compiling action bodies, conditional blocks, and loop
	 * bodies.
	 *
	 * <p>
	 * This method handles:
	 * </p>
	 * <ul>
	 * <li>Multi-statement block parsing and validation</li>
	 * <li>Individual statement compilation and description creation</li>
	 * <li>Context-sensitive compilation within action or block scopes</li>
	 * <li>Error reporting for syntax and semantic issues</li>
	 * </ul>
	 *
	 * @param string
	 *            the string containing GAML statements to compile (can contain multiple statements)
	 * @param actionContext
	 *            the description context providing scope for the compiled statements
	 * @param tempContext
	 *            the temporary execution context for runtime information and error reporting
	 * @return the list of compiled statement descriptions, or null if compilation fails
	 * @throws gama.api.exceptions.GamaRuntimeException
	 *             if parsing or compilation errors occur
	 */
	public static List<IDescription> compileBlock(final String string, final IDescription actionContext,
			final IExecutionContext tempContext) throws GamaRuntimeException {
		final String s = "__synthetic__ {" + string + "}";
		final GamlResource resource = GamlResourceServices.getTemporaryResource(actionContext);
		try (final Collector.AsList<IDescription> result = Collector.getList()) {
			final InputStream is = new ByteArrayInputStream(s.getBytes());
			try {
				resource.loadSynthetic(is, tempContext);
			} catch (final Exception e1) {
				e1.printStackTrace();
				return null;
			} finally {}
			if (resource.hasErrors()) {
				final Resource.Diagnostic d = resource.getErrors().get(0);
				throw GamaRuntimeException.error(d.getMessage(), tempContext.getScope());
			}
			final SyntacticModelElement elt = (SyntacticModelElement) resource.getSyntacticContents();
			if (!elt.hasChildren() && elt.hasFacet(IKeyword.FUNCTION)) {
				elt.getExpressionAt(IKeyword.FUNCTION).compile(actionContext);
			}
			elt.visitChildren(e -> {
				final IDescription desc = GAML.getDescriptionFactory().create(e, actionContext, null);
				result.add(desc);
			});
			return result.items();
		} finally {
			GamlResourceServices.discardTemporaryResource(resource);
		}
	}

}
