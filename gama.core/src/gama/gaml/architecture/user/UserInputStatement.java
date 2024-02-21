/*******************************************************************************************************
 *
 * UserInputStatement.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.architecture.user;

import java.util.List;

import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.ISymbolKind;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.facet;
import gama.annotations.precompiler.GamlAnnotations.facets;
import gama.annotations.precompiler.GamlAnnotations.inside;
import gama.annotations.precompiler.GamlAnnotations.symbol;
import gama.annotations.precompiler.GamlAnnotations.usage;
import gama.core.common.interfaces.IKeyword;
import gama.core.kernel.experiment.IParameter;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaColor;
import gama.gaml.descriptions.IDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.operators.Cast;
import gama.gaml.statements.AbstractPlaceHolderStatement;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

/**
 * Written by drogoul Modified on 7 févr. 2010
 *
 * @todo Description
 *
 */
@symbol (
		name = { IKeyword.USER_INPUT },
		kind = ISymbolKind.SINGLE_STATEMENT,
		with_sequence = false,
		concept = { IConcept.GUI })
@inside (
		symbols = IKeyword.USER_COMMAND)
@facets (
		value = { @facet (
				name = IKeyword.NAME,
				type = IType.LABEL,
				optional = true,
				doc = @doc ("the displayed name")),
				@facet (
						name = IKeyword.TYPE,
						type = IType.TYPE_ID,
						optional = true,
						doc = @doc ("the variable type")),
				@facet (
						name = IKeyword.INIT,
						type = IType.NONE,
						optional = false,
						doc = @doc ("the init value")),
				@facet (
						name = IKeyword.MIN,
						type = IType.FLOAT,
						optional = true,
						doc = @doc ("the minimum value")),
				@facet (
						name = "slider",
						type = IType.BOOL,
						optional = true,
						doc = @doc ("Whether to display a slider or not when applicable")),
				@facet (
						name = IKeyword.MAX,
						type = IType.FLOAT,
						optional = true,
						doc = @doc ("the maximum value")),
				@facet (
						name = IKeyword.RETURNS,
						type = IType.NEW_TEMP_ID,
						optional = false,
						doc = @doc ("a new local variable containing the value given by the user")),
				@facet (
						name = IKeyword.AMONG,
						type = IType.LIST,
						of = IType.STRING,
						optional = true,
						doc = @doc ("the set of acceptable values, only for string inputs")) },
		omissible = IKeyword.NAME)
@doc (
		value = "It allows to let the user define the value of a variable.",
		usages = { @usage (
				value = "",
				examples = { @example (
						value = "user_panel \"Advanced Control\" {",
						isExecutable = false),
						@example (
								value = "	user_input \"Location\" returns: loc type: point <- {0,0};",
								isExecutable = false),
						@example (
								value = "	create cells number: 10 with: [location::loc];",
								isExecutable = false),
						@example (
								value = "}",
								isExecutable = false) }) },
		see = { IKeyword.USER_COMMAND, IKeyword.USER_INIT, IKeyword.USER_PANEL })
@SuppressWarnings ({ "rawtypes" })
public class UserInputStatement extends AbstractPlaceHolderStatement implements IParameter {

	// int order;
	/** The is valued. */
	// static int index;
	boolean isValued;

	/** The current value. */
	Object initialValue, currentValue;

	/** The slider. */
	IExpression min, max, among, init, slider;

	/** The temp var. */
	String tempVar;

	/**
	 * Instantiates a new user input statement.
	 *
	 * @param desc
	 *            the desc
	 */
	public UserInputStatement(final IDescription desc) {
		super(desc);
		// order = index++;
		init = getFacet(IKeyword.INIT);
		min = getFacet(IKeyword.MIN);
		max = getFacet(IKeyword.MAX);
		among = getFacet(IKeyword.AMONG);
		slider = getFacet("slider");
		tempVar = getLiteral(IKeyword.RETURNS);
	}

	@Override
	public String getTitle() { return description.getName(); }

	@Override
	public String getCategory() { return null; }

	@Override
	public String getUnitLabel(final IScope scope) {
		return null;
	}

	@Override
	public void setValue(final IScope scope, final Object value) {
		currentValue = value;
	}

	@Override
	public Object value(final IScope scope) throws GamaRuntimeException {
		if (!isValued) {
			if (init != null) { currentValue = initialValue = init.value(scope); }
			isValued = true;
		}
		return currentValue;
	}

	@Override
	public IType getType() {
		final IType type = description.getGamlType();
		if (type != Types.NO_TYPE) return type;
		if (init == null) return Types.NO_TYPE;
		return init.getGamlType();
	}

	@Override
	public Object getInitialValue(final IScope scope) {
		return initialValue;
	}

	@Override
	public Comparable getMinValue(final IScope scope) {
		return min == null ? null : (Comparable) min.value(scope);
	}

	@Override
	public Comparable getMaxValue(final IScope scope) {
		return max == null ? null : (Comparable) max.value(scope);
	}

	@Override
	protected Object privateExecuteIn(final IScope scope) {
		scope.addVarWithValue(tempVar, currentValue);
		return currentValue;
	}

	/**
	 * Gets the temp var name.
	 *
	 * @return the temp var name
	 */
	public String getTempVarName() { return tempVar; }

	@Override
	public List getAmongValue(final IScope scope) {
		return among == null ? null : (List) among.value(scope);
	}

	@Override
	public boolean isEditable() { return true; }

	@Override
	public Comparable getStepValue(final IScope scope) {
		return null;
	}

	/**
	 * Method setUnitLabel()
	 *
	 * @see gama.core.kernel.experiment.IParameter#setUnitLabel(java.lang.String)
	 */
	@Override
	public void setUnitLabel(final String label) {}

	/**
	 * Method isDefined()
	 *
	 * @see gama.core.kernel.experiment.IParameter#isDefined()
	 */
	@Override
	public boolean isDefined() { return true; }

	/**
	 * Method setDefined()
	 *
	 * @see gama.core.kernel.experiment.IParameter#setDefined(boolean)
	 */
	@Override
	public void setDefined(final boolean b) {}

	@Override
	public boolean acceptsSlider(final IScope scope) {
		if (slider == null) return true;
		return Cast.asBool(scope, slider.value(scope));
	}

	@Override
	public List<GamaColor> getColors(final IScope scope) {
		return null;
	}

	@Override
	public GamaColor getColor(final IScope scope) {
		return null;
	}

	@Override
	public boolean isDefinedInExperiment() {
		// False by default ?
		return false;
	}

	@Override
	public void setValueNoCheckNoNotification(final Object value) { currentValue = value; }

}
