/*******************************************************************************************************
 *
 * Candidate.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.gaml.operators.multi_criteria;

import java.util.Map;

/**
 * Represents a decision candidate in a Multi-Criteria Decision Making (MCDM) context. Each
 * candidate is identified by a 0-based integer index and carries a map associating criterion names
 * to their evaluated numeric values. Instances of this class are consumed by the {@link Electre},
 * {@link Promethee}, and {@link EvidenceTheory} solvers to determine the best alternative among a
 * set of candidates.
 *
 * <p>Example usage:
 * <pre>{@code
 * Map<String, Double> values = new HashMap<>();
 * values.put("cost",     0.3);
 * values.put("distance", 0.7);
 * Candidate c = new Candidate(0, values);
 * }</pre>
 *
 * @see Electre
 * @see Promethee
 * @see EvidenceTheory
 */
public class Candidate {

	/**
	 * The 0-based position of this candidate in the original list of alternatives. It is used as a
	 * stable identifier throughout the MCDM computation, in particular when building the
	 * proposition sets in {@link EvidenceTheory}.
	 */
	private int index;

	/**
	 * A mapping from criterion name to the numeric evaluation of this candidate on that criterion.
	 * All criteria referenced by the chosen MCDM solver must be present as keys in this map,
	 * otherwise a {@link NullPointerException} will be raised during the computation.
	 */
	private Map<String, Double> valCriteria;

	/**
	 * Constructs a new {@code Candidate} with the given index and criterion values.
	 *
	 * @param index
	 *            the 0-based position of this candidate in the list of alternatives
	 * @param valCriteria
	 *            a map from each criterion name to the numeric value of this candidate on that
	 *            criterion; must contain an entry for every criterion used by the solver
	 */
	protected Candidate(final int index, final Map<String, Double> valCriteria) {
		super();
		this.index = index;
		this.valCriteria = valCriteria;
	}

	/**
	 * Returns a human-readable representation of this candidate, consisting of the index followed
	 * by an arrow and the criterion-value map.
	 *
	 * @return a string of the form {@code "<index> -> {criterion=value, ...}"}
	 */
	@Override
	public String toString() {
		return index + " -> " + valCriteria;
	}

	/**
	 * Computes a hash code based on the candidate's index and its criterion values map.
	 *
	 * @return the hash code for this candidate
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + index;
		result = prime * result + (valCriteria == null ? 0 : valCriteria.hashCode());
		return result;
	}

	/**
	 * Checks equality by comparing both the index and the criterion-value map. Two candidates are
	 * equal only if they share the same index and identical criterion evaluations.
	 *
	 * @param obj
	 *            the object to compare with this candidate
	 * @return {@code true} if {@code obj} is a {@code Candidate} with the same index and criterion
	 *         values; {@code false} otherwise
	 */
	@Override
	public boolean equals(final Object obj) {
		if ( this == obj ) { return true; }
		if ( obj == null ) { return false; }
		if ( getClass() != obj.getClass() ) { return false; }
		Candidate other = (Candidate) obj;
		if ( index != other.index ) { return false; }
		if ( valCriteria == null ) {
			if ( other.valCriteria != null ) { return false; }
		} else if ( !valCriteria.equals(other.valCriteria) ) { return false; }
		return true;
	}

	/**
	 * Returns the 0-based index that identifies this candidate in the list of alternatives.
	 *
	 * @return the 0-based index of this candidate
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Sets the 0-based index of this candidate.
	 *
	 * @param index
	 *            the new 0-based position of this candidate
	 */
	public void setIndex(final int index) {
		this.index = index;
	}

	/**
	 * Returns the map of criterion names to their evaluated numeric values for this candidate.
	 *
	 * @return a mutable map from criterion name to criterion value; never {@code null} after
	 *         construction
	 */
	public Map<String, Double> getValCriteria() {
		return valCriteria;
	}

	/**
	 * Replaces the criterion-value map for this candidate.
	 *
	 * @param valCriteria
	 *            the new map from criterion name to numeric value; should contain an entry for
	 *            every criterion used by the MCDM solver
	 */
	public void setValCriteria(final Map<String, Double> valCriteria) {
		this.valCriteria = valCriteria;
	}

}
