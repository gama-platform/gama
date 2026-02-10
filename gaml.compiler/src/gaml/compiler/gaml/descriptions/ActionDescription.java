/*******************************************************************************************************
 *
 * ActionDescription.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.descriptions;

import static gama.api.constants.IKeyword.DEFAULT;
import static gama.api.constants.IKeyword.FALSE;
import static gama.api.constants.IKeyword.OPTIONAL;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.eclipse.emf.ecore.EObject;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import gama.api.compilation.descriptions.IActionDescription;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.documentation.GamlConstantDocumentation;
import gama.api.compilation.documentation.GamlRegularDocumentation;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.constants.IGamlIssue;
import gama.api.constants.IKeyword;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IExpressionDescription;
import gama.api.gaml.symbols.Arguments;
import gama.api.gaml.symbols.Facets;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;

/**
 * The Class ActionDescription.
 */
public class ActionDescription extends StatementWithChildrenDescription implements IActionDescription {

	/** The Constant NULL_ARGS. */
	public static final Arguments NULL_ARGS = new Arguments();

	/** 
	 * Cached list of argument names to avoid repeated stream operations.
	 * <p><strong>Optimization:</strong> Computed once on first access and reused thereafter.</p>
	 */
	private List<String> cachedArgNames;

	/**
	 * Instantiates a new action description.
	 *
	 * @param keyword
	 *            the keyword
	 * @param superDesc
	 *            the super desc
	 * @param cp
	 *            the cp
	 * @param source
	 *            the source
	 * @param facets
	 *            the facets
	 */
	public ActionDescription(final String keyword, final IDescription superDesc, final Iterable<IDescription> cp,
			final EObject source, final Facets facets) {
		super(keyword, superDesc, cp, true, source, facets, null);
		setIf(Flag.Abstract, IKeyword.TRUE.equals(getLitteral(IKeyword.VIRTUAL)));
		if (getName() != null && getName().startsWith(IKeyword.SYNTHETIC)) {
			set(Flag.Synthetic);
			unSet(Flag.BuiltIn);
		}
		removeFacets(IKeyword.VIRTUAL);
	}

	@Override
	public ActionDescription copy(final IDescription into) {
		final ActionDescription desc = new ActionDescription(getKeyword(), into, children, element, getFacetsCopy());
		desc.setOriginName(getOriginName());
		// Note: cachedArgNames is intentionally not copied - it will be lazily recomputed if needed
		return desc;
	}

	/**
	 * Checks if is abstract.
	 *
	 * @return true, if is abstract
	 */
	@Override
	public boolean isAbstract() { return isSet(Flag.Abstract); }

	@Override
	protected boolean isSynthetic() { return isSet(Flag.Synthetic); }

	/**
	 * Gets the list of argument names for this action.
	 * 
	 * <p><strong>Optimization:</strong> The result is cached on first call to avoid repeated
	 * stream operations. This is safe because formal arguments don't change after construction.</p>
	 *
	 * @return the list of argument names
	 */
	@Override
	public List<String> getArgNames() {
		if (cachedArgNames == null) {
			cachedArgNames = StreamSupport.stream(getFormalArgs().spliterator(), false)
					.map(TO_NAME)
					.collect(Collectors.toList());
		}
		return cachedArgNames;
	}

	/**
	 * Verify args.
	 *
	 * @param caller
	 *            the caller
	 * @param args
	 *            the args
	 * @return true, if successful
	 */
	@Override
	@SuppressWarnings ("rawtypes")
	public boolean verifyArgs(final IDescription caller, final Arguments args) {
		final Arguments names = args == null ? NULL_ARGS : args;
		final Iterable<IDescription> formalArgs = getFormalArgs();
		final boolean noArgs = names.isEmpty();
		if (noArgs) {
			// Phase 2 Optimization: Replace stream with direct iteration
			final List<IDescription> formalArgsWithoutDefault = new ArrayList<>();
			for (final IDescription each : formalArgs) {
				if (!each.hasFacet(DEFAULT)) {
					formalArgsWithoutDefault.add(each);
				}
			}
			if (formalArgsWithoutDefault.isEmpty()) return true;
		}

		final List<String> allArgs = getArgNames();
		// If the names were not known at the time of the creation of the
		// caller, only the order
		if (caller.isInvocation() && names.containsKey("0")) { replaceNumberedArgs(names, allArgs); }

		// We compute the list of mandatory args
		if (formalArgs != null) {
			for (final IDescription c : formalArgs) {
				final String n = c.getName();
				if (c.hasFacet(DEFAULT)) {
					// AD: we compile the default (which is, otherwise, not
					// computed before validation
					c.getFacet(DEFAULT).compile(this);
					continue;
				}
				if (names.containsKey(n)) { continue; }
				if (c.hasFacet(OPTIONAL) && c.getFacet(OPTIONAL).equalsString(FALSE) || !c.hasFacet(OPTIONAL)) {
					caller.error(
							"Missing argument " + n + " in call to " + getName() + ". Arguments passed are : " + names,
							IGamlIssue.MISSING_ARGUMENT, caller.getUnderlyingElement(), n);
					return false;
				}
			}
		}

		return names.forEachFacet((s, e) -> {
			// A null value indicates a previous compilation error in the
			// arguments
			if (e != null) {
				if (!allArgs.contains(s)) {
					caller.error("Unknown argument " + s + " in call to " + getName(), IGamlIssue.UNKNOWN_ARGUMENT,
							e.getTarget(), s);
					return false;
				}
				if (e.getExpression() != null) {
					final IDescription formalArg = Iterables.find(formalArgs, input -> input.getName().equals(s));
					if (formalArg.isID()) return true;
					final IType<?> formalType = formalArg.getGamlType();
					final IType<?> callerType = e.getExpression().getGamlType();
					if (!Types.intFloatCase(formalType, callerType)) {
						boolean accepted = formalType == Types.NO_TYPE || callerType.isTranslatableInto(formalType);
						accepted = accepted || callerType == Types.NO_TYPE && formalType.getDefault() == null;
						if (!accepted) {
							caller.error("The type of argument " + s + " should be " + formalType,
									IGamlIssue.WRONG_TYPE, e.getTarget());
							return false;
						}
						return true;
					}
					caller.warning(
							"The argument " + s + " (of type " + callerType + ") will be casted to " + formalType,
							IGamlIssue.WRONG_TYPE, e.getTarget());
				}
				return true;
			}
			return false;
		});
	}

	/**
	 * Replace numbered args.
	 *
	 * @param names
	 *            the names
	 * @param allArgs
	 *            the all args
	 */
	private void replaceNumberedArgs(final Arguments names, final List<String> allArgs) {
		int index = 0;
		for (final String the_name : allArgs) {
			final String key = String.valueOf(index++);
			final IExpressionDescription old = names.get(key);
			if (old != null) {
				names.put(the_name, old);
				names.remove(key);
			}
		}
	}

	/**
	 * Contains arg.
	 *
	 * @param s
	 *            the s
	 * @return true, if successful
	 */
	@Override
	public boolean containsArg(final String s) {
		final IDescription formalArg = Iterables.find(getFormalArgs(), input -> input.getName().equals(s));
		return formalArg != null;
	}

	@Override
	public Arguments createCompiledArgs() {
		final Arguments ca = new Arguments();
		for (final IDescription sd : getFormalArgs()) {
			final String the_name = sd.getName();
			IExpression e = null;
			final IDescription superDesc = getEnclosingDescription();
			final IExpressionDescription ed = sd.getFacet(IKeyword.VALUE, DEFAULT);
			if (ed != null) { e = ed.compile(superDesc); }
			ca.putExpression(the_name, e);
		}
		return ca;

	}

	@Override
	public IGamlDocumentation getDocumentation() {
		IGamlDocumentation documentation = getShortDocumentation(false);

		if (getArgNames().size() > 0) {
			getFormalArgs().forEach(arg -> {
				final StringBuilder sb1 = new StringBuilder(100);
				sb1.append(arg.getGamlType());
				if (arg.hasFacet(DEFAULT) && arg.getFacetExpr(DEFAULT) != null) {
					sb1.append(" <i>(default: ").append(arg.getFacetExpr(DEFAULT).serializeToGaml(false))
							.append(")</i>");
				}
				documentation.set("Arguments accepted: ", arg.getName(), new GamlConstantDocumentation(sb1.toString()));
			});
		}
		return documentation;

	}

	/**
	 * Gets the short documentation.
	 *
	 * @return the short documentation
	 */
	@Override
	public IGamlDocumentation getShortDocumentation(final boolean withArgs) {
		IGamlDocumentation result = new GamlRegularDocumentation();
		Iterable<IDescription> args = getFormalArgs();
		if (withArgs && Iterables.size(args) > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append("(");
			for (final IDescription desc : args) {
				sb.append(desc.getGamlType()).append(" ").append(desc.getName()).append(", ");
			}
			if (sb.length() > 0) { sb.setLength(sb.length() - 2); }
			sb.append(")");
			result.append(sb.toString());
		} else {
			result.append("no arguments");
		}
		final String returns = getGamlType().equals(Types.NO_TYPE) ? ", no value returned"
				: ", returns a result of type " + getGamlType().getName();
		result.append(returns);
		final String doc = getBuiltInDoc();
		if (doc != null && !doc.isBlank()) { result.append(". ").append(doc); }
		result.append("<br/>");
		return result;
	}

	/**
	 * Gets the built in doc.
	 *
	 * @return the built in doc
	 */
	protected String getBuiltInDoc() { return null; }

}
