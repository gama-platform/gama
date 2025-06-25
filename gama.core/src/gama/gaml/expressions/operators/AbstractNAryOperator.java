/*******************************************************************************************************
 *
 * AbstractNAryOperator.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.expressions.operators;

import static gama.annotations.precompiler.ITypeProvider.ALL;
import static gama.annotations.precompiler.ITypeProvider.CONTENT_TYPE_AT_INDEX;
import static gama.annotations.precompiler.ITypeProvider.DENOTED_TYPE_AT_INDEX;
import static gama.annotations.precompiler.ITypeProvider.FIRST_CONTENT_TYPE_OR_TYPE;
import static gama.annotations.precompiler.ITypeProvider.FLOAT_IN_CASE_OF_INT;
import static gama.annotations.precompiler.ITypeProvider.INDEXED_TYPES;
import static gama.annotations.precompiler.ITypeProvider.KEY_TYPE_AT_INDEX;
import static gama.annotations.precompiler.ITypeProvider.SECOND_CONTENT_TYPE_OR_TYPE;
import static gama.annotations.precompiler.ITypeProvider.SECOND_DENOTED_TYPE;
import static gama.annotations.precompiler.ITypeProvider.THIRD_CONTENT_TYPE_OR_TYPE;
import static gama.annotations.precompiler.ITypeProvider.TYPE_AT_INDEX;
import static gama.annotations.precompiler.ITypeProvider.WRAPPED;

import java.util.Arrays;
import java.util.function.Predicate;

import gama.annotations.precompiler.GamlProperties;
import gama.annotations.precompiler.ITypeProvider;
import gama.core.common.interfaces.IKeyword;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.ICollector;
import gama.gaml.descriptions.IVarDescriptionUser;
import gama.gaml.descriptions.OperatorProto;
import gama.gaml.descriptions.SpeciesDescription;
import gama.gaml.descriptions.VariableDescription;
import gama.gaml.expressions.AbstractExpression;
import gama.gaml.expressions.IExpression;
import gama.gaml.types.GamaType;
import gama.gaml.types.IContainerType;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

/**
 * AbstractBinaryOperator
 *
 * @author drogoul 23 august 07
 */
@SuppressWarnings ({ "rawtypes" })
public abstract class AbstractNAryOperator extends AbstractExpression implements IOperator {

	/** The exprs. */
	public final IExpression[] exprs;

	/** The prototype. */
	protected OperatorProto prototype;

	/**
	 * Instantiates a new abstract N ary operator.
	 *
	 * @param proto
	 *            the proto
	 * @param expressions
	 *            the expressions
	 * @see #copy()
	 */
	public AbstractNAryOperator(final OperatorProto proto, final IExpression... expressions) {
		if (expressions.length == 0 || expressions[0] == null) {
			exprs = null;
		} else {
			exprs = expressions;
			/**
			 * Copy introduced in order to circumvent issue 1060: still necessary ? It is supposed that the copy should
			 * occur before, for instance in the implementations of {@link #copy()}.
			 */
			// Arrays.copyOf(expressions,expressions.length);
		}
		this.prototype = proto;
		type = computeType();
	}

	@Override
	public void collectMetaInformation(final GamlProperties meta) {
		prototype.collectMetaInformation(meta);
		if (exprs != null) { for (IExpression expr : exprs) { expr.collectMetaInformation(meta); } }
	}

	@Override
	public OperatorProto getPrototype() { return prototype; }

	/**
	 * Compute type.
	 *
	 * @return the i type
	 */
	protected IType computeType() {
		if (prototype == null) return Types.NO_TYPE;
		IType result = computeType(prototype.typeProvider, 0, prototype.returnType, GamaType.TYPE);
		if (result.isContainer()) {
			final IType contentType = computeType(prototype.contentTypeProvider,
					prototype.contentTypeContentTypeProvider, result.getContentType(), GamaType.CONTENT);
			final IType keyType = computeType(prototype.keyTypeProvider, 0, result.getKeyType(), GamaType.KEY);
			result = GamaType.from(result, keyType, contentType);
		}
		return result;
	}

	/**
	 * Compute type.
	 *
	 * @param tp
	 *            the tp
	 * @param contentTypeProvider
	 *            the content type provider
	 * @param defaultType
	 *            the default type
	 * @param kind
	 *            the kind
	 * @return the i type
	 */
	protected IType computeType(final int tp, final int contentTypeProvider, final IType defaultType, final int kind) {
		IType result = defaultType;
		int typeProvider = tp;
		final boolean returnFloatsInsteadOfInts = typeProvider < FLOAT_IN_CASE_OF_INT;
		if (returnFloatsInsteadOfInts) { typeProvider = typeProvider - FLOAT_IN_CASE_OF_INT; }
		if (typeProvider >= 0) {
			result = Types.get(typeProvider);
		} else if (exprs != null) {
			switch (typeProvider) {
				case WRAPPED:
					result = exprs[0].getGamlType().getWrappedType();
					break;
				case ALL:
					result = GamaType.findCommonType(exprs, kind);
					break;
				case FIRST_CONTENT_TYPE_OR_TYPE:
					result = processContentTypeOrType(0);
					break;
				case SECOND_DENOTED_TYPE:
					result = exprs[1].getDenotedType();
					break;
				case SECOND_CONTENT_TYPE_OR_TYPE:
					result = processContentTypeOrType(1);
					break;
				case THIRD_CONTENT_TYPE_OR_TYPE:
					result = processContentTypeOrType(2);
					break;
				default:
					if (typeProvider < INDEXED_TYPES) { result = processIndexedTypeProvider(result, typeProvider); }
			}
		}

		if (contentTypeProvider != ITypeProvider.NONE && result.isContainer()) {
			final IType c =
					computeType(contentTypeProvider, ITypeProvider.NONE, result.getContentType(), GamaType.CONTENT);
			if (c != Types.NO_TYPE) { result = ((IContainerType<?>) result).of(c); }
		}
		if (returnFloatsInsteadOfInts && result == Types.INT) return Types.FLOAT;
		return result;
	}

	/**
	 * Process indexed type provider.
	 *
	 * @param result
	 *            the result
	 * @param typeProvider
	 *            the type provider
	 * @return the i type
	 */
	private IType processIndexedTypeProvider(IType result, final int typeProvider) {
		int index = -1;
		int kindOfIndex = -1;
		if (typeProvider > TYPE_AT_INDEX) {
			index = typeProvider - TYPE_AT_INDEX - 1;
			kindOfIndex = GamaType.TYPE;
		} else if (typeProvider > CONTENT_TYPE_AT_INDEX) {
			index = typeProvider - CONTENT_TYPE_AT_INDEX - 1;
			kindOfIndex = GamaType.CONTENT;
		} else if (typeProvider > DENOTED_TYPE_AT_INDEX) {
			index = typeProvider - DENOTED_TYPE_AT_INDEX - 1;
			kindOfIndex = GamaType.DENOTED;
		} else if (typeProvider > KEY_TYPE_AT_INDEX) {
			index = typeProvider - KEY_TYPE_AT_INDEX - 1;
			kindOfIndex = GamaType.KEY;
		} else if (typeProvider > ITypeProvider.KEY_AND_CONTENT_TYPE_AT_INDEX) {
			index = typeProvider - ITypeProvider.KEY_AND_CONTENT_TYPE_AT_INDEX - 1;
			kindOfIndex = GamaType.PAIR_OF_TYPES;
		}
		if (index > -1 && index < exprs.length) {
			final IExpression expr = exprs[index];
			switch (kindOfIndex) {
				case GamaType.TYPE:
					result = expr.getGamlType();
					break;
				case GamaType.CONTENT:
					result = expr.getGamlType().getContentType();
					break;
				case GamaType.KEY:
					result = expr.getGamlType().getKeyType();
					break;
				case GamaType.DENOTED:
					result = expr.getDenotedType();
					break;
				case GamaType.PAIR_OF_TYPES:
					result = Types.PAIR.of(expr.getGamlType().getKeyType(), expr.getGamlType().getContentType());
			}
		}
		return result;
	}

	/**
	 * Process second content type or type.
	 *
	 * @return the i type
	 */
	private IType processContentTypeOrType(final int index) {
		final IType rightType = exprs[index].getGamlType();
		return rightType.isContainer() ? rightType.getContentType() : rightType;
	}

	/**
	 * Copy. Only called by {@link #resolveAgainst(IScope)}, provides a clean copy of the operator and a copy of its
	 * expressions too
	 *
	 * @return the abstract N ary operator
	 */
	protected abstract AbstractNAryOperator copy();

	@Override
	public IOperator resolveAgainst(final IScope scope) {
		final AbstractNAryOperator copy = copy();
		if (exprs != null) {
			for (int i = 0; i < exprs.length; i++) { copy.exprs[i] = exprs[i].resolveAgainst(scope); }
		}
		return copy;
	}

	@Override
	public boolean isConst() {
		if (!prototype.canBeConst) return false;
		if (exprs != null) { for (final IExpression expr : exprs) { if (!expr.isConst()) return false; } }
		return true;
	}

	@Override
	public String getName() { return prototype.getName(); }

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder().append(literalValue()).append("(");
		if (exprs != null) {
			for (int i = 0; i < exprs.length; i++) {
				final String l = exprs[i] == null ? "null" : exprs[i].toString();
				result.append(l).append(i != exprs.length - 1 ? "," : "");
			}
		}
		return result.append(")").toString();
	}

	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		final StringBuilder sb = new StringBuilder();
		sb.append(literalValue());
		parenthesize(sb, exprs);
		return sb.toString();
	}

	/**
	 * Num arg.
	 *
	 * @return the int
	 */
	public int numArg() {
		return exprs == null ? 0 : exprs.length;
	}

	@Override
	public IExpression arg(final int i) {
		if (exprs == null || i >= exprs.length) return null;
		return exprs[i];
	}

	@Override
	public String getTitle() {
		// Special case for 'as'
		if (IKeyword.AS.equals(getName())) return getTitleForCasting();
		final StringBuilder sb = new StringBuilder(50);
		sb.append("operator ").append(getName()).append(" (");
		if (exprs != null) {
			for (final IExpression expr : exprs) {
				sb.append(expr == null ? "nil" : expr.getGamlType().getName());
				sb.append(',');
			}
			sb.setLength(sb.length() - 1);
		} else if (prototype.signature != null) { sb.append("Argument types: " + prototype.signature.toString()); }
		sb.append(") returns ");
		sb.append(type.getName());
		return sb.toString();
	}

	/**
	 * Gets the title for casting.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the title for casting
	 * @date 3 janv. 2024
	 */
	private String getTitleForCasting() {
		final StringBuilder sb = new StringBuilder(50);
		sb.append("operator for casting ").append(exprs[0].getGamlType().getName()).append(" into ")
				.append(type.getName());
		return sb.toString();
	}

	@Override
	public Doc getDocumentation() { return prototype.getDocumentation(); }

	@Override
	public String getDefiningPlugin() { return prototype.getDefiningPlugin(); }

	@Override
	public void collectUsedVarsOf(final SpeciesDescription species,
			final ICollector<IVarDescriptionUser> alreadyProcessed, final ICollector<VariableDescription> result) {
		if (alreadyProcessed.contains(this)) return;
		alreadyProcessed.add(this);
		prototype.collectUsedVarsOf(species, alreadyProcessed, result);
		if (exprs != null) {
			for (final IExpression e : exprs) {
				if (e != null) { e.collectUsedVarsOf(species, alreadyProcessed, result); }
			}
		}
	}

	@Override
	public boolean isContextIndependant() {
		if (exprs != null) {
			for (final IExpression e : exprs) { if (e != null && !e.isContextIndependant()) return false; }
		}
		return true;
	}

	@Override
	public boolean isAllowedInParameters() {
		if (exprs != null) {
			for (final IExpression e : exprs) { if (e != null && !e.isAllowedInParameters()) return false; }
		}
		return true;
	}

	@Override
	public void visitSuboperators(final IOperatorVisitor visitor) {
		if (exprs != null) {
			for (final IExpression e : exprs) { if (e instanceof IOperator) { visitor.visit((IOperator) e); } }
		}

	}

	@Override
	public Object _value(final IScope scope) throws GamaRuntimeException {
		final Object[] values = new Object[exprs == null ? 0 : exprs.length];
		try {
			for (int i = 0; i < values.length; i++) {
				values[i] = prototype.getLazyness()[i] ? exprs[i] : exprs[i].value(scope);
			}
			return prototype.getHelper().get(scope, values);
		} catch (final GamaRuntimeException e1) {
			e1.addContext("when applying the " + literalValue() + " operator on " + Arrays.toString(values));
			throw e1;
		} catch (final Throwable e) {
			final GamaRuntimeException ee = GamaRuntimeException.create(e, scope);
			ee.addContext("when applying the " + literalValue() + " operator on " + Arrays.toString(values));
			throw ee;
		}
	}

	@Override
	public boolean findAny(final Predicate<IExpression> predicate) {
		if (predicate.test(this)) return true;
		if (exprs != null) { for (final IExpression e : exprs) { if (e.findAny(predicate)) return true; } }
		return false;
	}
}
