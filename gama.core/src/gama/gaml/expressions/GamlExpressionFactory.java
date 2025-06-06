
/*******************************************************************************************************
 *
 * GamlExpressionFactory.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.expressions;

import static com.google.common.collect.Iterables.any;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;

import gama.core.common.interfaces.IKeyword;
import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.IExecutionContext;
import gama.core.util.IMap;
import gama.dev.DEBUG;
import gama.gaml.compilation.GAML;
import gama.gaml.descriptions.ActionDescription;
import gama.gaml.descriptions.IDescription;
import gama.gaml.descriptions.IExpressionDescription;
import gama.gaml.descriptions.OperatorProto;
import gama.gaml.descriptions.SpeciesDescription;
import gama.gaml.descriptions.StringBasedExpressionDescription;
import gama.gaml.expressions.data.ListExpression;
import gama.gaml.expressions.data.MapExpression;
import gama.gaml.expressions.operators.PrimitiveOperator;
import gama.gaml.expressions.types.SkillConstantExpression;
import gama.gaml.expressions.types.SpeciesConstantExpression;
import gama.gaml.expressions.units.UnitConstantExpression;
import gama.gaml.expressions.variables.AgentVariableExpression;
import gama.gaml.expressions.variables.EachExpression;
import gama.gaml.expressions.variables.GlobalVariableExpression;
import gama.gaml.expressions.variables.MyselfExpression;
import gama.gaml.expressions.variables.SelfExpression;
import gama.gaml.expressions.variables.SuperExpression;
import gama.gaml.expressions.variables.TempVariableExpression;
import gama.gaml.factories.DescriptionFactory;
import gama.gaml.interfaces.IGamlIssue;
import gama.gaml.statements.ActionStatement;
import gama.gaml.statements.Arguments;
import gama.gaml.types.IType;
import gama.gaml.types.Signature;
import gama.gaml.types.Types;

/**
 * The static class ExpressionFactory.
 *
 * @author drogoul
 */

/**
 * A factory for creating GamlExpression objects.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 28 déc. 2023
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamlExpressionFactory implements IExpressionFactory {

	static {
		DEBUG.OFF();
	}

	/**
	 * The Interface ParserProvider.
	 */
	public interface ParserProvider {

		/**
		 * Gets the.
		 *
		 * @return the i expression compiler
		 */
		IExpressionCompiler get();
	}

	/** The parser. */
	static ThreadLocal<IExpressionCompiler> parser;

	/**
	 * Register parser provider.
	 *
	 * @param f
	 *            the f
	 */
	public static void registerParserProvider(final ParserProvider f) {
		parser = new ThreadLocal() {
			@Override
			protected IExpressionCompiler initialValue() {
				return f.get();
			}
		};
	}

	/**
	 * Gets the parser.
	 *
	 * @return the parser
	 */
	// @Override
	private IExpressionCompiler getParser() { return parser.get(); }

	@Override
	public void resetParser() {
		parser.get().dispose();
		parser.remove();
	}

	/**
	 * Method createUnit()
	 *
	 * @see gama.gaml.expressions.IExpressionFactory#createUnit(java.lang.Object, gama.gaml.types.IType,
	 *      java.lang.String)
	 */
	@Override
	public UnitConstantExpression createUnit(final Object value, final IType t, final String name, final String doc,
			final String deprecated, final boolean isTime, final String[] names) {
		final UnitConstantExpression exp = UnitConstantExpression.create(value, t, name, doc, isTime, names);
		if (deprecated != null && !deprecated.isEmpty()) { exp.setDeprecated(deprecated); }
		return exp;

	}

	@Override
	public ConstantExpression createConst(final Object val, final IType type) {
		return createConst(val, type, null);
	}

	@Override
	public SpeciesConstantExpression createSpeciesConstant(final IType type) {
		if (type.getGamlType() != Types.SPECIES) return null;
		final SpeciesDescription sd = type.getContentType().getSpecies();
		if (sd == null) return null;
		return new SpeciesConstantExpression(sd.getName(), type, sd);
	}

	@Override
	public ConstantExpression createConst(final Object val, final IType type, final String name) {
		if (type.getGamlType() == Types.SPECIES) return createSpeciesConstant(type);
		if (type == Types.SKILL) return new SkillConstantExpression((String) val, type);
		if (val == null) return NIL_EXPR;
		if (val instanceof Boolean) return (Boolean) val ? TRUE_EXPR : FALSE_EXPR;
		return new ConstantExpression(val, type, name);
	}

	@Override
	public UnitConstantExpression getUnitExpr(final String unit) {
		return GAML.UNITS.get(unit);
	}

	@Override
	public IExpression createExpr(final IExpressionDescription ied, final IDescription context) {
		return getParser().compile(ied, context);
	}

	@Override
	public IExpression createExpr(final String s, final IDescription context) {
		if (s == null || s.isEmpty()) return null;
		return getParser().compile(StringBasedExpressionDescription.create(s), context);
	}

	@Override
	public IExpression createExpr(final String s, final IDescription context,
			final IExecutionContext additionalContext) {
		if (s == null || s.isEmpty()) return null;
		return getParser().compile(s, context, additionalContext);
	}

	@Override
	public Arguments createArgumentMap(final ActionDescription action, final IExpressionDescription args,
			final IDescription context) {
		if (args == null) return null;
		return getParser().parseArguments(action, args.getTarget(), context, false);
	}

	@Override
	public IExpression createVar(final String name, final IType type, final boolean isConst, final int scope,
			final IDescription definitionDescription) {
		return switch (scope) {
			case IVarExpression.GLOBAL -> GlobalVariableExpression.create(name, type, isConst,
					definitionDescription.getModelDescription());
			case IVarExpression.AGENT -> new AgentVariableExpression(name, type, isConst, definitionDescription);
			case IVarExpression.TEMP -> new TempVariableExpression(name, type, definitionDescription);
			case IVarExpression.EACH -> new EachExpression(name, type);
			case IVarExpression.SELF -> new SelfExpression(type);
			case IVarExpression.SUPER -> new SuperExpression(type);
			case IVarExpression.MYSELF -> new MyselfExpression(type, definitionDescription);
			default -> null;
		};
	}

	@Override
	public IExpression createList(final Iterable<? extends IExpression> elements) {
		return ListExpression.create(elements);
	}

	/**
	 * Creates a new GamlExpression object.
	 *
	 * @param elements
	 *            the elements
	 * @return the i expression
	 */
	public IExpression createList(final IExpression[] elements) {
		return ListExpression.create(elements);
	}

	@Override
	public IExpression createMap(final Iterable<? extends IExpression> elements) {
		return MapExpression.create(elements);
	}

	@Override
	public boolean hasExactOperator(final String op, final IExpression arg) {
		// If arguments are invalid, we have no match
		// If the operator is not known, we have no match
		IMap<Signature, OperatorProto> variants = GAML.OPERATORS.get(op);
		if (arg == null || variants == null) return false;
		return variants.containsKey(new Signature(arg).simplified());
		// Signature sig = new Signature(arg).simplified();
		// return any(variants.keySet(), si -> sig.equals(si));
	}

	/**
	 * Checks for operator.
	 *
	 * @param op
	 *            the op
	 * @param sig
	 *            the sig
	 * @return true, if successful
	 */
	@Override
	public boolean hasOperator(final String op, final Signature s) {
		// If arguments are invalid, we have no match
		// If the operator is not known, we have no match
		if (s == null || s.size() == 0 || !GAML.OPERATORS.containsKey(op)) return false;
		final IMap<Signature, OperatorProto> ops = GAML.OPERATORS.get(op);
		Signature sig = s.simplified();
		// Does any known operator signature match with the signatue of the expressions ?
		boolean matches = any(ops.keySet(), si -> sig.matchesDesiredSignature(si));
		if (!matches) {
			// Check if a varArg is not a possibility
			matches = any(ops.keySet(), si -> Signature.varArgFrom(sig).matchesDesiredSignature(si));
		}
		return matches;
	}

	@Override
	public IExpression createOperator(final String op, final IDescription context, final EObject eObject,
			final IExpression... args) {
		if (args == null || args.length == 0 || !GAML.OPERATORS.containsKey(op))
			return emitError(op, context, eObject, args == null ? new IExpression[0] : args);
		for (final IExpression exp : args) { if (exp == null) return emitError(op, context, eObject, args); }
		// if (!hasOperator(op, userSignature)) return emitError(op, context, eObject, args);
		// We get the possible sets of types registered in OPERATORS
		final IMap<Signature, OperatorProto> ops = GAML.OPERATORS.get(op);
		// We create the signature corresponding to the arguments
		// 19/02/14 Only the simplified signature is used now
		Signature userSignature = Signature.createSimplified(args);
		// If the signature is not present in the registry
		if (!ops.containsKey(userSignature)) {
			final Signature originalUserSignature = userSignature;
			int distance = Integer.MAX_VALUE;
			// We browse all the entries of the operators with this name
			for (Map.Entry<Signature, OperatorProto> entry : ops.entrySet()) {
				Signature formalParametersSignature = entry.getKey();

				if (originalUserSignature.matchesDesiredSignature(formalParametersSignature)) {
					final int dist = Signature.distanceBetween(formalParametersSignature, originalUserSignature);
					if (dist == 0) {
						distance = 0;
						userSignature = formalParametersSignature;
						break;
					}
					if (dist < distance) {
						distance = dist;
						userSignature = formalParametersSignature;
					}
				}
			}

			if (distance == Integer.MAX_VALUE) { // Not found - try varArg
				Signature varArg = Signature.varArgFrom(originalUserSignature);
				for (Map.Entry<Signature, OperatorProto> entry : ops.entrySet()) {
					Signature s = entry.getKey();
					if (varArg.matchesDesiredSignature(s))
						return createOperator(op, context, eObject, createList(args));
				}
				return emitError(op, context, eObject, args);
			}

			// We coerce the types if necessary, by wrapping the original
			// expressions in a casting expression

			for (int i = 0; i < args.length; i++) {
				IType originalType = originalUserSignature.get(i);
				IType newType = userSignature.get(i);
				IType coercingType = findCoercingType(context, eObject, originalType, newType, args[i]);
				if (coercingType != null) { args[i] = createAs(context, args[i], createTypeExpression(coercingType)); }
			}
		}

		final OperatorProto proto = ops.get(userSignature);
		// We finally make an instance of the operator and init it with the arguments
		final IExpression operator = proto.create(context, eObject, args);
		if (operator != null) {
			// We verify that it is not deprecated
			final String ged = proto.getDeprecated();
			if (ged != null) {
				context.warning(proto.getName() + " is deprecated: " + ged, IGamlIssue.DEPRECATED, eObject);
			}
		}
		return operator;
	}

	/**
	 * Find coercing type.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param originalType
	 *            the original type
	 * @param newType
	 *            the new type
	 * @param argument
	 *            the argument
	 * @return the i type
	 * @date 9 janv. 2024
	 */
	private IType findCoercingType(final IDescription context, final EObject eObject, final IType originalType,
			final IType newType, final IExpression argument) {
		if (originalType == Types.INT && newType == Types.FLOAT) return Types.FLOAT;
		if (originalType == Types.FLOAT && newType == Types.INT) {
			// Emits an info when a float is truncated. See Issue 735.
			context.info("'" + argument.serializeToGaml(false) + "' will be  truncated to int.",
					IGamlIssue.UNMATCHED_OPERANDS, eObject);
			return Types.INT;
		}
		return null;
	}

	/**
	 * Emit error.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param op
	 *            the op
	 * @param context
	 *            the context
	 * @param eObject
	 *            the e object
	 * @param args
	 *            the args
	 * @return the i expression
	 * @date 14 nov. 2023
	 */
	private IExpression emitError(final String op, final IDescription context, final EObject eObject,
			final IExpression... args) {
		final IMap<Signature, OperatorProto> ops = GAML.OPERATORS.get(op);
		final Signature userSignature = new Signature(args).simplified();
		StringBuilder msg =
				new StringBuilder("No operator found for applying '").append(op).append("' to ").append(userSignature);
		if (ops != null) {
			msg.append(" (operators available for ").append(Arrays.toString(ops.keySet().toArray())).append(")");
		}
		context.error(msg.toString(), IGamlIssue.UNMATCHED_OPERANDS, eObject);
		return null;
	}

	@Override
	public IExpression createAs(final IDescription context, final IExpression toCast, final IExpression type) {
		return OperatorProto.AS.create(context, null, toCast, type);
	}

	@Override
	public IExpression createAction(final String op, final IDescription callerContext, final ActionDescription action,
			final IExpression call, final Arguments arguments) {
		if (action.verifyArgs(callerContext, arguments))
			return new PrimitiveOperator(callerContext, action, call, arguments, call instanceof SuperExpression);
		return null;
	}

	/**
	 * Method createCastingExpression()
	 *
	 * @see gama.gaml.expressions.IExpressionFactory#createCastingExpression(gama.gaml.types.IType)
	 */
	@Override
	public IExpression createTypeExpression(final IType type) {
		return type.getExpression();
	}

	@Override
	public IExpression createTemporaryActionForAgent(final IAgent agent, final String action,
			final IExecutionContext tempContext) {
		final SpeciesDescription context = agent.getSpecies().getDescription();
		final ActionDescription desc = (ActionDescription) DescriptionFactory.create(IKeyword.ACTION, context,
				Collections.EMPTY_LIST, IKeyword.TYPE, IKeyword.UNKNOWN, IKeyword.NAME, TEMPORARY_ACTION_NAME);
		final List<IDescription> children = getParser().compileBlock(action, context, tempContext);
		for (final IDescription child : children) { desc.addChild(child); }
		desc.validate();
		context.addChild(desc);
		final ActionStatement a = (ActionStatement) desc.compile();
		agent.getSpecies().addTemporaryAction(a);
		return getParser().compile(TEMPORARY_ACTION_NAME + "()", context, null);
	}

}