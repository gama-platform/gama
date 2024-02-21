/*******************************************************************************************************
 *
 * BDIPlan.java, in gama.extension.bdi, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.bdi;

import java.util.Objects;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.getter;
import gama.annotations.precompiler.GamlAnnotations.variable;
import gama.annotations.precompiler.GamlAnnotations.vars;
import gama.core.common.interfaces.IValue;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.file.json.Json;
import gama.core.util.file.json.JsonValue;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

/**
 * The Class BDIPlan.
 */
@vars ({ @variable (
		name = "name",
		type = IType.STRING,
		doc = @doc ("The name of this BDI plan")),
		@variable (
				name = "todo",
				type = IType.STRING,
				doc = @doc ("represent the when facet of a plan")),
		@variable (
				name = SimpleBdiPlanStatement.INTENTION,
				type = MentalStateType.id,
				doc = @doc ("A string representing the current intention of this BDI plan")),
		@variable (
				name = SimpleBdiArchitecture.FINISHEDWHEN,
				type = IType.STRING,
				doc = @doc ("a string representing the finished condition of this plan")),
		@variable (
				name = SimpleBdiArchitecture.INSTANTANEAOUS,
				type = IType.STRING,
				doc = @doc ("indicates if the plan is instantaneous")) })
public class BDIPlan implements IValue {

	@Override
	public JsonValue serializeToJson(final Json json) {
		return json.typedObject(getGamlType(), "name", getName());
	}

	/** The planstatement. */
	private SimpleBdiPlanStatement planstatement;

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	@getter ("name")
	public String getName() { return this.planstatement.getName(); }

	/**
	 * Gets the when.
	 *
	 * @return the when
	 */
	@getter ("todo")
	public String getWhen() { return this.planstatement._when.serializeToGaml(true); }

	/**
	 * Gets the finished when.
	 *
	 * @return the finished when
	 */
	@getter (SimpleBdiArchitecture.FINISHEDWHEN)
	public String getFinishedWhen() { return this.planstatement._executedwhen.serializeToGaml(true); }

	/**
	 * Gets the intention.
	 *
	 * @param scope
	 *            the scope
	 * @return the intention
	 */
	@getter (SimpleBdiPlanStatement.INTENTION)
	public Predicate getIntention(final IScope scope) {
		return (Predicate) this.planstatement._intention.value(scope);
	}

	/**
	 * Gets the instantaneous.
	 *
	 * @return the instantaneous
	 */
	@getter (SimpleBdiArchitecture.INSTANTANEAOUS)
	public String getInstantaneous() { return this.planstatement._instantaneous.serializeToGaml(true); }

	/**
	 * Gets the plan statement.
	 *
	 * @return the plan statement
	 */
	public SimpleBdiPlanStatement getPlanStatement() { return this.planstatement; }

	/**
	 * Instantiates a new BDI plan.
	 */
	public BDIPlan() {}

	/**
	 * Instantiates a new BDI plan.
	 *
	 * @param statement
	 *            the statement
	 */
	public BDIPlan(final SimpleBdiPlanStatement statement) {
		this.planstatement = statement;
	}

	/**
	 * Sets the simple bdi plan statement.
	 *
	 * @param statement
	 *            the new simple bdi plan statement
	 */
	public void setSimpleBdiPlanStatement(final SimpleBdiPlanStatement statement) {
		this.planstatement = statement;

	}

	@Override
	public String toString() {
		return serializeToGaml(true);
	}

	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		return "BDIPlan(" + planstatement.getName()
		// +(values == null ? "" : "," + values) +
				+ ")";
	}

	@Override
	public String stringValue(final IScope scope) throws GamaRuntimeException {
		return "BDIPlan(" + planstatement.getName() + ")"
		// +(values == null ? "" : "," + values)
		;
	}

	@Override
	public IValue copy(final IScope scope) throws GamaRuntimeException {
		return new BDIPlan(planstatement);
	}

	/**
	 * Checks if is similar name.
	 *
	 * @param other
	 *            the other
	 * @return true, if is similar name
	 */
	public boolean isSimilarName(final BDIPlan other) {
		if (this == other) return true;
		if (other == null || !Objects.equals(planstatement, other.planstatement)) return false;
		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hash(planstatement);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		final BDIPlan other = (BDIPlan) obj;
		if (!Objects.equals(planstatement, other.planstatement)) return false;
		return true;
	}

	/**
	 * Method getType()
	 *
	 * @see gama.core.common.interfaces.ITyped#getGamlType()
	 */
	@Override
	public IType<?> getGamlType() { return Types.get(IType.TYPE_ID); }

}
