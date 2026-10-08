/*******************************************************************************************************
 *
 * CritereFonctionsCroyances.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.gaml.operators.multi_criteria;

/**
 * Abstract base class for a criterion used in the Evidence Theory (Dempster–Shafer) multi-criteria
 * decision making method. Each concrete subclass encodes three <em>basic belief assignment</em>
 * (BBA) functions for a single criterion:
 * <ul>
 *   <li><b>pour</b> ("for"): the degree of belief that the candidate <em>is</em> the best choice
 *       according to this criterion.</li>
 *   <li><b>contre</b> ("against"): the degree of belief that the candidate is <em>not</em> the
 *       best choice according to this criterion.</li>
 *   <li><b>ignorance</b>: the degree of uncertainty, i.e. the residual mass that is neither
 *       committed to "for" nor to "against".</li>
 * </ul>
 *
 * <p>All three mass values must satisfy: {@code pour + contre + ignorance ≤ 1}.
 *
 * <p>Subclasses are used by {@link EvidenceTheory} to fuse evidence across multiple criteria and
 * derive a ranking of {@link Candidate} objects.
 *
 * @author PTaillandier
 * @see EvidenceTheory
 * @see CritereFctCroyancesBasique
 */
public abstract class CritereFonctionsCroyances {

	/**
	 * The name of this criterion. It must match the key used in the {@link Candidate#getValCriteria()}
	 * map so that the correct numeric value can be looked up during belief mass computation.
	 */
	private String nom;

	/**
	 * Constructs a new criterion with the given name.
	 *
	 * @param nom
	 *            the unique name of this criterion; must correspond to a key present in every
	 *            {@link Candidate}'s criterion-value map
	 */
	protected CritereFonctionsCroyances(final String nom) {
		this.nom = nom;
	}

	/**
	 * Returns the name of this criterion.
	 *
	 * @return the criterion name; never {@code null} after construction
	 */
	public String getNom() {
		return nom;
	}

	/**
	 * Sets the name of this criterion.
	 *
	 * @param nom
	 *            the new criterion name; must remain consistent with the keys in each
	 *            {@link Candidate}'s criterion-value map
	 */
	public void setNom(final String nom) {
		this.nom = nom;
	}

	/**
	 * Returns the name of this criterion as its string representation.
	 *
	 * @return the criterion name
	 */
	@Override
	public String toString() {
		return nom;
	}

	/**
	 * Computes a hash code based solely on the criterion name.
	 *
	 * @return the hash code for this criterion
	 */
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + (nom == null ? 0 : nom.hashCode());
		return result;
	}

	/**
	 * Checks equality based on the criterion name. Two criteria are considered equal when they
	 * share the same name, regardless of their internal belief function parameters.
	 *
	 * @param obj
	 *            the object to compare with this criterion
	 * @return {@code true} if {@code obj} is a {@code CritereFonctionsCroyances} with the same
	 *         name; {@code false} otherwise
	 */
	@Override
	public boolean equals(final Object obj) {
		if ( this == obj ) { return true; }
		if ( obj == null ) { return false; }
		if ( getClass() != obj.getClass() ) { return false; }
		final CritereFonctionsCroyances other = (CritereFonctionsCroyances) obj;
		if ( nom == null ) {
			if ( other.nom != null ) { return false; }
		} else if ( !nom.equals(other.nom) ) { return false; }
		return true;
	}

	/**
	 * Computes the "for" belief mass for this criterion at the given criterion value. This mass
	 * represents the degree of evidence, according to this single criterion, that the candidate
	 * whose value is {@code a} <em>is</em> the best alternative.
	 *
	 * @param a
	 *            the current numeric value of this criterion for a given candidate
	 * @return a value in {@code [0, 1]} representing how strongly this criterion supports the
	 *         hypothesis that the candidate is the best choice
	 */
	public abstract double masseCroyancePour(double a);

	/**
	 * Computes the "against" belief mass for this criterion at the given criterion value. This mass
	 * represents the degree of evidence, according to this single criterion, that the candidate
	 * whose value is {@code a} is <em>not</em> the best alternative.
	 *
	 * @param a
	 *            the current numeric value of this criterion for a given candidate
	 * @return a value in {@code [0, 1]} representing how strongly this criterion opposes the
	 *         hypothesis that the candidate is the best choice
	 */
	public abstract double masseCroyanceContre(double a);

	/**
	 * Computes the ignorance belief mass for this criterion at the given criterion value. The
	 * ignorance mass is the residual uncertainty that cannot be assigned to either "for" or
	 * "against", and is typically computed as {@code max(0, 1 - pour - contre)}.
	 *
	 * @param a
	 *            the current numeric value of this criterion for a given candidate
	 * @return a value in {@code [0, 1]} representing the degree of uncertainty about whether this
	 *         candidate is the best choice
	 */
	public abstract double masseCroyanceIgnorance(double a);

}
