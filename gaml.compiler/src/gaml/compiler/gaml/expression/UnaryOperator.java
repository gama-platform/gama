/*******************************************************************************************************
 *
 * UnaryOperator.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.expression;

import static gama.annotations.support.ITypeProvider.CONTENT_TYPE_AT_INDEX;
import static gama.annotations.support.ITypeProvider.DENOTED_TYPE_AT_INDEX;
import static gama.annotations.support.ITypeProvider.FIRST_CONTENT_TYPE_OR_TYPE;
import static gama.annotations.support.ITypeProvider.FIRST_ELEMENT_CONTENT_TYPE;
import static gama.annotations.support.ITypeProvider.FLOAT_IN_CASE_OF_INT;
import static gama.annotations.support.ITypeProvider.KEY_AND_CONTENT_TYPE_AT_INDEX;
import static gama.annotations.support.ITypeProvider.KEY_TYPE_AT_INDEX;
import static gama.annotations.support.ITypeProvider.TYPE_AT_INDEX;
import static gama.annotations.support.ITypeProvider.WRAPPED;

import java.util.function.Predicate;

import gama.api.additions.registries.ArtefactRegistry;
import gama.api.compilation.artefacts.IArtefact;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.ITypeDescription;
import gama.api.compilation.descriptions.IVarDescriptionUser;
import gama.api.compilation.descriptions.IVariableDescription;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IOperator;
import gama.api.gaml.types.GamaType;
import gama.api.gaml.types.IContainerType;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.utils.GamlProperties;
import gama.api.utils.collections.ICollector;

/**
 * The Class UnaryOperator.
 */
@SuppressWarnings ({ "rawtypes" })
public class UnaryOperator extends AbstractExpression implements IOperator {

	/** The child. */
	final protected IExpression child;

	/** The prototype. */
	protected final IArtefact.Operator prototype;

	/**
	 * Creates the.
	 *
	 * @param artefact
	 *            the artefact
	 * @param context
	 *            the context
	 * @param child
	 *            the child
	 * @return the i expression
	 */
	public static IExpression create(final IArtefact.Operator proto, final IDescription context,
			final IExpression child) {
		return new UnaryOperator(proto, context, child).optimized();
	}

	@Override
	public boolean isConst() { return prototype.canBeConst() && child.isConst(); }

	@Override
	public String getDefiningPlugin() { return prototype.getDefiningPlugin(); }

	/**
	 * Instantiates a new unary operator.
	 *
	 * @param artefact
	 *            the artefact
	 * @param context
	 *            the context
	 * @param child
	 *            the child
	 */
	public UnaryOperator(final IArtefact.Operator proto, final IDescription context, final IExpression... child) {
		this.child = child[0];
		this.prototype = proto;
		if (proto != null) {
			type = proto.getReturnType();
			computeType();
			proto.verifyExpectedTypes(context, child[0].getGamlType().getContentType());
		}
	}

	@Override
	public Object _value(final IScope scope) throws GamaRuntimeException {
		final Object childValue = prototype.getLazyness()[0] ? child : child.value(scope);
		try {
			return prototype.getHelper().get(scope, childValue);
		} catch (final GamaRuntimeException e1) {
			e1.addContext("when applying the " + literalValue() + " operator on " + childValue);
			throw e1;
		} catch (final Throwable e) {
			final GamaRuntimeException ee = GamaRuntimeException.create(e, scope);
			ee.addContext("when applying the " + literalValue() + " operator on " + childValue);
			throw ee;
		}
	}

	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		final String s = literalValue();
		final StringBuilder sb = new StringBuilder(s);
		if (ArtefactRegistry.ARTEFACTS_WITHOUT_PARENTHESES.contains(s)) {
			parenthesize(sb, child);
		} else {
			sb.append("(").append(child.serializeToGaml(includingBuiltIn)).append(")");
		}
		return sb.toString();
	}

	@Override
	public boolean shouldBeParenthesized() {
		return false;
	}

	@Override
	public String toString() {
		return literalValue() + "(" + child + ")";
	}

	@Override
	public String getTitle() {
		final StringBuilder sb = new StringBuilder(50);
		sb.append("operator ").append(getName()).append(" (");
		sb.append(child == null ? prototype.getSignature() : child.getGamlType().getName());
		sb.append(") returns ").append(getGamlType().getName());
		return sb.toString();
	}

	@Override
	public IGamlDocumentation getDocumentation() { return prototype.getDocumentation(); }

	/**
	 * Compute type.
	 *
	 * @param theType
	 *            the the type
	 * @param def
	 *            the def
	 * @return the i type
	 */
	private IType computeType(final int theType, final IType def) {
		int t = theType;
		final boolean returnFloatsInsteadOfInts = t < FLOAT_IN_CASE_OF_INT;
		if (returnFloatsInsteadOfInts) { t = t - FLOAT_IN_CASE_OF_INT; }
		IType result = def;
		switch (t) {
			case WRAPPED:
				result = child.getGamlType().getWrappedType();
				break;
			case FIRST_ELEMENT_CONTENT_TYPE:
				if (child instanceof ListExpression) {
					final IExpression[] array = ((ListExpression) child).getElements();
					if (array.length == 0) {
						result = Types.NO_TYPE;
					} else {
						result = array[0].getGamlType().getContentType();
					}
				} else if (child instanceof MapExpression) {
					final IExpression[] array = ((MapExpression) child).getValues();
					if (array.length == 0) {
						result = Types.NO_TYPE;
					} else {
						result = array[0].getGamlType().getContentType();
					}
				} else {
					final IType tt = child.getGamlType().getContentType().getContentType();
					if (tt != Types.NO_TYPE) { result = tt; }
				}
				break;
			case FIRST_CONTENT_TYPE_OR_TYPE:
				final IType firstType = child.getGamlType();
				final IType t2 = firstType.getContentType();
				if (t2 == Types.NO_TYPE) {
					result = firstType;
				} else {
					result = t2;
				}
				break;
			default:
				result = t == TYPE_AT_INDEX + 1 ? child.getGamlType()
						: t == CONTENT_TYPE_AT_INDEX + 1 ? child.getGamlType().getContentType()
						: t == KEY_TYPE_AT_INDEX + 1 ? child.getGamlType().getKeyType() : t >= 0 ? Types.get(t)
						: t == DENOTED_TYPE_AT_INDEX + 1 ? child.getDenotedType()
						: t == KEY_AND_CONTENT_TYPE_AT_INDEX + 1
								? Types.PAIR.of(child.getGamlType().getKeyType(), child.getGamlType().getContentType())
						: def;
				break;
		}
		if (returnFloatsInsteadOfInts && result == Types.INT) return Types.FLOAT;
		return result;
	}

	/**
	 * Compute type.
	 */
	protected void computeType() {
		type = computeType(prototype.getTypeProvider(), type);
		if (type.isContainer()) {
			IType<?> contentType = computeType(prototype.getContentTypeProvider(), type.getContentType());
			if (contentType.isParametricFormOf(Types.PAIR) && type == Types.LIST) {
				type = Types.LIST.of(contentType);
			} else if (contentType.isContainer()) {
				// WARNING Special case for pairs of map. See if it works for other
				// fields as well
				if (contentType.getKeyType() == Types.NO_TYPE && contentType.getContentType() == Types.NO_TYPE) {
					contentType = GamaType.from(contentType, child.getGamlType().getKeyType(),
							child.getGamlType().getContentType());
				}
				final IType contentContentType =
						computeType(prototype.getContentTypeContentTypeProvider(), contentType.getContentType());
				contentType = ((IContainerType<?>) contentType).of(contentContentType);

			}
			if (!type.isParametricFormOf(Types.LIST)) {
				final IType keyType = computeType(prototype.getKeyTypeProvider(), type.getKeyType());
				type = GamaType.from(type, keyType, contentType);
			}
		}
	}

	@Override
	public IOperator resolveAgainst(final IScope scope) {
		return new UnaryOperator(prototype, null, child.resolveAgainst(scope));
	}

	@Override
	public String getName() { return prototype.getName(); }

	@Override
	public IExpression arg(final int i) {
		return i == 0 ? child : null;
	}

	@Override
	public void collectMetaInformation(final GamlProperties meta) {
		prototype.collectMetaInformation(meta);
		child.collectMetaInformation(meta);
	}

	@Override
	public void collectUsedVarsOf(final ITypeDescription species,
			final ICollector<IVarDescriptionUser> alreadyProcessed, final ICollector<IVariableDescription> result) {
		if (alreadyProcessed.contains(this)) return;
		alreadyProcessed.add(this);
		prototype.collectUsedVarsOf(species, alreadyProcessed, result);
		child.collectUsedVarsOf(species, alreadyProcessed, result);
	}

	@Override
	public boolean isContextIndependant() { return child.isContextIndependant(); }

	@Override
	public boolean isAllowedInParameters() { return child.isAllowedInParameters(); }

	@Override
	public IArtefact getPrototype() { return prototype; }

	@Override
	public void visitSuboperators(final IOperatorVisitor visitor) {
		if (child instanceof IOperator) { visitor.visit((IOperator) child); }

	}

	@Override
	public boolean findAny(final Predicate<IExpression> predicate) {
		if (predicate.test(this)) return true;
		return child != null && child.findAny(predicate);
	}

}
