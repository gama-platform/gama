/*******************************************************************************************************
 *
 * ActionStatement.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.statements;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.facet;
import gama.annotations.facets;
import gama.annotations.inside;
import gama.annotations.symbol;
import gama.annotations.usage;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.annotations.serializer;
import gama.api.annotations.validator;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.serialization.StatementSerializer;
import gama.api.compilation.validation.ActionValidator;
import gama.api.constants.IKeyword;
import gama.api.gaml.expressions.IExpressionDescription;
import gama.api.gaml.statements.ActionStatement.ActionSerializer;
import gama.api.gaml.symbols.Arguments;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;

/**
 * The Class ActionCommand.
 *
 * @author drogoul
 */
@symbol (
		name = IKeyword.ACTION,
		kind = ISymbolKind.ACTION,
		with_sequence = true,
		with_args = true,
		unique_name = true,
		concept = { IConcept.SPECIES, IConcept.ACTION })
@inside (
		kinds = { ISymbolKind.SPECIES, ISymbolKind.EXPERIMENT, ISymbolKind.MODEL })
@facets (
		value = { @facet (
				name = IKeyword.NAME,
				type = IType.ID,
				optional = false,
				doc = @doc ("identifier of the action")),
				@facet (
						name = IKeyword.TYPE,
						type = IType.TYPE_ID,
						optional = true,
						doc = @doc ("the action returned type"),
						internal = true),
				@facet (
						name = IKeyword.OF,
						type = IType.TYPE_ID,
						optional = true,
						doc = @doc ("if the action returns a container, the type of its elements"),
						internal = true),
				@facet (
						name = IKeyword.INDEX,
						type = IType.TYPE_ID,
						optional = true,
						doc = @doc ("if the action returns a map, the type of its keys"),
						internal = true),
				@facet (
						name = IKeyword.VIRTUAL,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("whether the action is virtual (defined without a set of instructions) (false by default)")), },
		omissible = IKeyword.NAME)
@doc (
		value = "Allows to define in a species, model or experiment a new action that can be called elsewhere.",
		usages = { @usage (
				value = "The simplest syntax to define an action that does not take any parameter and does not return anything is:",
				examples = { @example (
						value = "action simple_action {",
						isExecutable = false),
						@example (
								value = "   // [set of statements]",
								isExecutable = false),
						@example (
								value = "}",
								isExecutable = false) }),
				@usage (
						value = "If the action needs some parameters, they can be specified betwee, braquets after the identifier of the action:",
						examples = { @example (
								value = "action action_parameters(int i, string s){",
								isExecutable = false),
								@example (
										value = "   // [set of statements using i and s]",
										isExecutable = false),
								@example (
										value = "}",
										isExecutable = false) }),
				@usage (
						value = "If the action returns any value, the returned type should be used instead of the \"action\" keyword. A return statement inside the body of the action statement is mandatory.",
						examples = { @example (
								value = "int action_return_val(int i, string s){",
								isExecutable = false),
								@example (
										value = "   // [set of statements using i and s]",
										isExecutable = false),
								@example (
										value = "   return i + i;",
										isExecutable = false),
								@example (
										value = "}",
										isExecutable = false) }),
				@usage (
						value = "If virtual: is true, then the action is abstract, which means that the action is defined without body. A species containing at least one abstract action is abstract. Agents of this species cannot be created. The common use of an abstract action is to define an action that can be used by all its sub-species, which should redefine all abstract actions and implements its body.",
						examples = { @example (
								value = "species parent_species {",
								isExecutable = false),
								@example (
										value = "   int virtual_action(int i, string s);",
										isExecutable = false),
								@example (
										value = "}",
										isExecutable = false),
								@example (
										value = "",
										isExecutable = false),
								@example (
										value = "species children parent: parent_species {",
										isExecutable = false),
								@example (
										value = "   int virtual_action(int i, string s) {",
										isExecutable = false),
								@example (
										value = "      return i + i;",
										isExecutable = false),
								@example (
										value = "   }",
										isExecutable = false),
								@example (
										value = "}",
										isExecutable = false) }) },
		see = { "do" })
@validator (ActionValidator.class)
@serializer (ActionSerializer.class)
@SuppressWarnings ({ "rawtypes" })
public class ActionStatement extends AbstractStatementSequenceWithArgs implements IStatement.Action {

	/**
	 * The Class ActionSerializer.
	 */
	public static class ActionSerializer extends StatementSerializer {

		@Override
		public String serializeFacetValue(final IDescription s, final String key, final boolean includingBuiltIn) {
			if (TYPE.equals(key)) return null;
			return super.serializeFacetValue(s, key, includingBuiltIn);
		}

		@Override
		protected void serializeArg(final IDescription desc, final IDescription arg, final StringBuilder sb,
				final boolean includingBuiltIn) {
			final String name = arg.getLitteral(NAME);
			final IExpressionDescription type = arg.getFacet(TYPE);
			final IExpressionDescription def = arg.getFacet(DEFAULT);

			sb.append(type == null ? "unknown" : type.serializeToGaml(includingBuiltIn)).append(" ").append(name);
			if (def != null) { sb.append(" <- ").append(def.serializeToGaml(includingBuiltIn)); }
		}

		@Override
		public void serializeKeyword(final IDescription desc, final StringBuilder sb, final boolean includingBuiltIn) {
			String type = desc.getGamlType().serializeToGaml(includingBuiltIn);
			if (UNKNOWN.equals(type)) { type = ACTION; }
			sb.append(type).append(" ");
		}

	}

	/** The formal args. */
	Arguments formalArgs = new Arguments();

	/**
	 * The Constructor.
	 *
	 * @param actionDesc
	 *            the action desc
	 * @param sim
	 *            the sim
	 */
	public ActionStatement(final IDescription desc) {
		super(desc);
		if (hasFacet(IKeyword.NAME)) { name = getLiteral(IKeyword.NAME); }

	}

	@Override
	public void leaveScope(final IScope scope) {
		// Clears any _action_halted status present
		scope.getAndClearReturnStatus();
		super.leaveScope(scope);
	}

	@Override
	public void setRuntimeArgs(final IScope scope, final Arguments args) {
		super.setRuntimeArgs(scope, args);
		actualArgs.get().complementWith(formalArgs);
	}

	/**
	 * Sets the formal args.
	 *
	 * @param args
	 *            the new formal args
	 */
	@Override
	public void setFormalArgs(final Arguments args) {
		formalArgs.putAll(args);
	}

	@Override
	public void dispose() {
		formalArgs.dispose();
		super.dispose();
	}
}
