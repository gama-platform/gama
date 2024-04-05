/*******************************************************************************************************
 *
 * Norm.java, in gama.extension.bdi, is part of the source code of the GAMA modeling and simulation
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
 * The Class Norm.
 */
@vars ({ @variable (
		name = "name",
		type = IType.STRING,
		doc = @doc ("The name of this norm")),
		@variable (
				name = NormStatement.INTENTION,
				type = PredicateType.id,
				doc = @doc ("A string representing the current intention of this Norm")),
		@variable (
				name = NormStatement.OBLIGATION,
				type = PredicateType.id,
				doc = @doc ("A string representing the current obligation of this Norm")),
		@variable (
				name = SimpleBdiArchitecture.FINISHEDWHEN,
				type = IType.STRING,
				doc = @doc ("represents the condition when a norm is finished")),
		@variable (
				name = SimpleBdiArchitecture.INSTANTANEOUS,
				type = IType.BOOL,
				doc = @doc ("indicates if the norm is instantaneous")),
		@variable (
				name = NormStatement.LIFETIME,
				type = IType.INT,
				doc = @doc ("the lifetim during which the norm is considered violated when it has been violated")) })

// Classe qui permet de définir les normes comme type, contenant le norm statement, sur l'exemple des plans
public class Norm implements IValue {

	@Override
	public JsonValue serializeToJson(final Json json) {
		return json.typedObject(getGamlType(), "name", getName());
	}

	/** The norm statement. */
	private NormStatement normStatement;

	/** The is violated. */
	private Boolean isViolated;

	/** The lifetime violation. */
	private Integer lifetimeViolation;

	/** The no lifetime. */
	private Boolean noLifetime;

	/** The is applied. */
	private Boolean isApplied;

	/** The is sanctioned. */
	private Boolean isSanctioned;

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	@getter ("name")
	public String getName() { return this.normStatement.getName(); }

	/**
	 * Gets the lifetime.
	 *
	 * @param scope
	 *            the scope
	 * @return the lifetime
	 */
	@getter (NormStatement.LIFETIME)
	public Integer getLifetime(final IScope scope) {
		return this.lifetimeViolation;
	}

	/**
	 * Gets the when.
	 *
	 * @return the when
	 */
	@getter ("when")
	public String getWhen() { return this.normStatement._when.serializeToGaml(true); }

	/**
	 * Gets the intention.
	 *
	 * @param scope
	 *            the scope
	 * @return the intention
	 */
	@getter (NormStatement.INTENTION)
	public Predicate getIntention(final IScope scope) {
		if (this.normStatement != null && this.normStatement._intention != null)
			return (Predicate) this.normStatement._intention.value(scope);
		return null;
	}

	/**
	 * Gets the obligation.
	 *
	 * @param scope
	 *            the scope
	 * @return the obligation
	 */
	@getter (NormStatement.OBLIGATION)
	public Predicate getObligation(final IScope scope) {
		if (this.normStatement != null && this.normStatement._obligation != null)
			return (Predicate) this.normStatement._obligation.value(scope);
		return null;
	}

	/**
	 * Gets the finished when.
	 *
	 * @return the finished when
	 */
	@getter (SimpleBdiArchitecture.FINISHEDWHEN)
	public String getFinishedWhen() {
		if (this.normStatement != null && this.normStatement._executedwhen != null)
			return this.normStatement._executedwhen.serializeToGaml(true);
		return null;
	}

	/**
	 * Gets the instantaneous.
	 *
	 * @return the instantaneous
	 */
	@getter (SimpleBdiArchitecture.INSTANTANEOUS)
	public boolean getInstantaneous(final IScope scope) {
		return normStatement._instantaneous == null ? false : gama.gaml.operators.Cast.asBool(scope, normStatement._instantaneous.value(scope)); 
	}

	/**
	 * Gets the norm statement.
	 *
	 * @return the norm statement
	 */
	public NormStatement getNormStatement() { return this.normStatement; }

	/**
	 * Gets the violated.
	 *
	 * @return the violated
	 */
	public Boolean getViolated() { return this.isViolated; }

	/**
	 * Gets the applied.
	 *
	 * @return the applied
	 */
	public Boolean getApplied() { return this.isApplied; }

	/**
	 * Gets the sanctioned.
	 *
	 * @return the sanctioned
	 */
	public Boolean getSanctioned() { return this.isSanctioned; }


	/**
	 * Instantiates a new norm.
	 *
	 * @param statement
	 *            the statement
	 */
	public Norm(final NormStatement statement) {
		this.normStatement = statement;
		this.lifetimeViolation = -1;
		this.isViolated = false;
		this.noLifetime = true;
		this.isApplied = false;
		this.isSanctioned = false;
	}

	/**
	 * Instantiates a new norm.
	 *
	 * @param statement
	 *            the statement
	 * @param scope
	 *            the scope
	 */
	public Norm(final NormStatement statement, final IScope scope) {
		this.normStatement = statement;
		this.isViolated = false;
		this.isApplied = false;
		this.isSanctioned = false;
		if (statement._lifetime != null) {
			this.lifetimeViolation = (Integer) statement._lifetime.value(scope);
			this.noLifetime = false;
		} else {
			this.lifetimeViolation = -1;
			this.noLifetime = true;
		}
	}

	/**
	 * Sets the violation.
	 *
	 * @param violation
	 *            the new violation
	 */
	public void setViolation(final Boolean violation) {
		this.isViolated = violation;
		this.isApplied = !violation;
	}

	/**
	 * Sets the sanctioned.
	 *
	 * @param sanction
	 *            the new sanctioned
	 */
	public void setSanctioned(final Boolean sanction) { this.isSanctioned = sanction; }

	/**
	 * Sanctioned.
	 */
	public void sanctioned() {
		this.isSanctioned = true;
	}

	/**
	 * Violated.
	 *
	 * @param scope
	 *            the scope
	 */
	public void violated(final IScope scope) {
		this.isViolated = true;
		this.isApplied = false;
		if (this.normStatement._lifetime != null) {
			this.lifetimeViolation = (Integer) this.normStatement._lifetime.value(scope);
		} else {
			this.lifetimeViolation = 1;
		}
		this.noLifetime = false;
	}

	/**
	 * Applied.
	 *
	 * @param scope
	 *            the scope
	 */
	public void applied(final IScope scope) {
		this.isApplied = true;
		this.isViolated = false;
		this.lifetimeViolation = -1;
		this.noLifetime = false;
	}

	/**
	 * Update lifeime.
	 */
	public void updateLifeime() {
		if (!noLifetime && isViolated) { this.lifetimeViolation--; }
		if (this.lifetimeViolation < 0 && !noLifetime) {
			isViolated = false;
			isSanctioned = false;
			noLifetime = true;
		}
	}

	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		return "Norm(" + normStatement + ")";
	}

	@Override
	public int hashCode() {
		return Objects.hash(isApplied, isSanctioned, isViolated, lifetimeViolation, noLifetime, normStatement);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		Norm other = (Norm) obj;
		return Objects.equals(normStatement, other.normStatement);
	}

	@Override
	public IType<?> getGamlType() {
		return Types.get(NormType.id);
		// return null;
	}

	@Override
	public String stringValue(final IScope scope) throws GamaRuntimeException {
		return "Norm(" + normStatement + ")";
	}

	@Override
	public IValue copy(final IScope scope) throws GamaRuntimeException {
		return new Norm(normStatement);
	}

}
