/*******************************************************************************************************
 *
 * SocialLink.java, in gama.extension.bdi, is part of the source code of the GAMA modeling and simulation
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
import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.file.json.Json;
import gama.core.util.file.json.JsonValue;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

/**
 * The Class SocialLink.
 */
@vars ({ @variable (
		name = "agent",
		type = IType.AGENT,
		doc = @doc ("the agent with who there is a social link")),
		@variable (
				name = "liking",
				type = IType.FLOAT,
				doc = @doc ("the liking value of the link")),
		@variable (
				name = "dominance",
				type = IType.FLOAT,
				doc = @doc ("the dominance value of the link")),
		@variable (
				name = "solidarity",
				type = IType.FLOAT,
				doc = @doc ("the solidarity value of the link")),
		@variable (
				name = "familiarity",
				type = IType.FLOAT,
				doc = @doc ("the familiarity value of the link")),
		@variable (
				name = "trust",
				type = IType.FLOAT,
				doc = @doc ("the trust value of the link")) })

public class SocialLink implements IValue {

	/** The agent. */
	IAgent agent;

	/** The liking. */
	double liking = 0.0;

	/** The dominance. */
	double dominance = 0.0;

	/** The solidarity. */
	double solidarity = 0.0;

	/** The familiarity. */
	double familiarity = 0.0;

	/** The trust. */
	double trust = 0.0;

	/** The no liking. */
	private boolean hasLiking = false;

	/** The no dominance. */
	private boolean hasDominance = false;

	/** The no solidarity. */
	private boolean hasSolidarity = false;

	/** The no familiarity. */
	private boolean hasFamiliarity = false;

	/** The no trust. */
	private boolean hasTrust = false;

	@Override
	public JsonValue serializeToJson(final Json json) {
		return json.typedObject(getGamlType(), "agent", agent, "liking", liking, "dominance", dominance, "solidarity",
				solidarity).add("trust", trust).add("familiarity", familiarity);
	}

	/**
	 * Gets the agent.
	 *
	 * @return the agent
	 */
	@getter ("agent")
	public IAgent getAgent() { return agent; }

	/**
	 * Gets the liking.
	 *
	 * @return the liking
	 */
	@getter ("liking")
	public double getLiking() { return liking; }

	/**
	 * Gets the dominance.
	 *
	 * @return the dominance
	 */
	@getter ("dominance")
	public double getDominance() { return dominance; }

	/**
	 * Gets the solidarity.
	 *
	 * @return the solidarity
	 */
	@getter ("solidarity")
	public double getSolidarity() { return solidarity; }

	/**
	 * Gets the familiarity.
	 *
	 * @return the familiarity
	 */
	@getter ("familiarity")
	public double getFamiliarity() { return familiarity; }

	/**
	 * Gets the trust.
	 *
	 * @return the trust
	 */
	@getter ("trust")
	public double getTrust() { return trust; }

	/**
	 * Gets the no liking.
	 *
	 * @return the no liking
	 */
	public boolean getHasLiking() { return hasLiking; }

	/**
	 * Gets the no dominance.
	 *
	 * @return the no dominance
	 */
	public boolean getHasDominance() { return hasDominance; }

	/**
	 * Gets the no solidarity.
	 *
	 * @return the no solidarity
	 */
	public boolean getHasSolidarity() { return hasSolidarity; }

	/**
	 * Gets the no familiarity.
	 *
	 * @return the no familiarity
	 */
	public boolean getHasFamiliarity() { return hasFamiliarity; }

	/**
	 * Gets the no trust.
	 *
	 * @return the no trust
	 */
	public boolean getHasTrust() { return hasTrust; }

	/**
	 * Sets the agent.
	 *
	 * @param ag
	 *            the new agent
	 */
	public void setAgent(final IAgent ag) { this.agent = ag; }

	/**
	 * Sets the liking.
	 *
	 * @param appre
	 *            the new liking
	 */
	public void setLiking(final double appre) {
		this.liking = appre;
		this.hasLiking = true;
	}

	/**
	 * Sets the dominance.
	 *
	 * @param domi
	 *            the new dominance
	 */
	public void setDominance(final double domi) {
		this.dominance = domi;
		this.hasDominance = true;
	}

	/**
	 * Sets the solidarity.
	 *
	 * @param solid
	 *            the new solidarity
	 */
	public void setSolidarity(final double solid) {
		this.solidarity = solid;
		this.hasSolidarity = true;
	}

	/**
	 * Sets the familiarity.
	 *
	 * @param fami
	 *            the new familiarity
	 */
	public void setFamiliarity(final double fami) {
		this.familiarity = fami;
		this.hasFamiliarity = true;
	}

	/**
	 * Sets the trust.
	 *
	 * @param tru
	 *            the new trust
	 */
	public void setTrust(final double tru) {
		this.trust = tru;
		this.hasTrust = true;
	}

	/**
	 * Instantiates a new social link.
	 */
	public SocialLink() {
		this.agent = null;
	}

	/**
	 * Instantiates a new social link.
	 *
	 * @param ag
	 *            the ag
	 */
	public SocialLink(final IAgent ag) {
		this.agent = ag;
	}

	/**
	 * Instantiates a new social link.
	 *
	 * @param ag
	 *            the ag
	 * @param appre
	 *            the appre
	 * @param domi
	 *            the domi
	 * @param solid
	 *            the solid
	 * @param fami
	 *            the fami
	 */
	public SocialLink(final IAgent ag, final double appre, final double domi, final double solid, final double fami) {
		this.agent = ag;
		this.liking = appre;
		this.hasLiking = true;
		this.dominance = domi;
		this.hasDominance = true;
		this.solidarity = solid;
		this.hasSolidarity = true;
		this.familiarity = fami;
		this.hasFamiliarity = true;
	}

	/**
	 * Instantiates a new social link.
	 *
	 * @param ag
	 *            the ag
	 * @param appre
	 *            the appre
	 * @param domi
	 *            the domi
	 * @param solid
	 *            the solid
	 * @param fami
	 *            the fami
	 * @param tru
	 *            the tru
	 */
	public SocialLink(final IAgent ag, final double appre, final double domi, final double solid, final double fami,
			final double tru) {
		this.agent = ag;
		this.liking = appre;
		this.hasLiking = true;
		this.dominance = domi;
		this.hasDominance = true;
		this.solidarity = solid;
		this.hasSolidarity = true;
		this.familiarity = fami;
		this.hasFamiliarity = true;
		this.trust = tru;
		this.hasTrust = true;
	}

	@Override
	public String toString() {
		return serializeToGaml(true);
	}

	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		return "(" + agent + "," + liking + "," + dominance + "," + solidarity + "," + familiarity + "," + trust + ")";
	}

	@Override
	public IType<?> getGamlType() { return Types.get(SocialLinkType.id); }

	@Override
	public String stringValue(final IScope scope) throws GamaRuntimeException {
		return "(" + agent + "," + liking + "," + dominance + "," + solidarity + "," + familiarity + ")";
	}

	@Override
	public IValue copy(final IScope scope) throws GamaRuntimeException {
		return new SocialLink(agent, liking, dominance, solidarity, familiarity);
	}

	@Override
	public int hashCode() {
		return Objects.hash(agent, liking, dominance, solidarity, familiarity, trust, hasLiking, hasDominance,
				hasSolidarity, hasFamiliarity, hasTrust);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		final SocialLink other = (SocialLink) obj;
		if (agent != null && (other.getAgent() == null || !agent.equals(other.getAgent()))) return false;
		if (hasLiking && (!other.getHasLiking() || liking != other.getLiking())) return false;
		if (hasDominance && (!other.getHasDominance() || dominance != other.getDominance())) return false;
		if (hasSolidarity && (!other.getHasSolidarity() || solidarity != other.getSolidarity())) return false;
		if (hasFamiliarity && (!other.getHasFamiliarity() || familiarity != other.getFamiliarity())) return false;
		if (hasTrust && (!other.getHasTrust() || trust != other.getTrust())) return false;
		return true;
	}

	/**
	 * Equals in agent.
	 *
	 * @param obj
	 *            the obj
	 * @return true, if successful
	 */
	public boolean equalsInAgent(final Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		final SocialLink other = (SocialLink) obj;
		if (agent != null) {
			return agent.equals(other.getAgent());
		}
		else {
			// Here we know that agent is null
			// so equalsInAgent will return true only if other.getAgent() is null too
			return agent == other.getAgent(); 			
		}
	}

}
