/*******************************************************************************************************
 *
 * CritereFctCroyancesBasique.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.gaml.operators.multi_criteria;

/**
 * A concrete {@link CritereFonctionsCroyances} criterion whose three basic belief assignment (BBA)
 * functions—"for", "against", and "ignorance"—are modelled as piecewise-linear functions of the
 * criterion value. The shape of each function is defined by two anchor thresholds {@code s1} and
 * {@code s2} and the corresponding belief masses at those thresholds.
 *
 * <p>For a given criterion value {@code a}:
 * <ul>
 *   <li>If {@code a ≤ s1}: the belief mass equals the value specified at {@code s1}
 *       ({@code v1Pour} or {@code v1Contre}).</li>
 *   <li>If {@code a ≥ s2}: the belief mass equals the value specified at {@code s2}
 *       ({@code v2Pour} or {@code v2Contre}).</li>
 *   <li>Otherwise: the belief mass is linearly interpolated between the two anchor points.</li>
 * </ul>
 *
 * <p>The ignorance mass is computed as {@code max(0, 1 - massePour(a) - masseContre(a))},
 * ensuring that the three masses always sum to at most 1.
 *
 * <p>Example usage:
 * <pre>{@code
 * // Criterion "cost": belief for being the best rises from 0.8 to 0.1 as value goes from 0 to 1.
 * CritereFctCroyancesBasique c = new CritereFctCroyancesBasique(
 *     "cost",
 *     0.0,  // s1 – lower threshold
 *     0.1,  // v2Pour – "for" belief at s2
 *     0.8,  // v1Pour – "for" belief at s1
 *     0.1,  // v1Contre – "against" belief at s1
 *     0.8,  // v2Contre – "against" belief at s2
 *     1.0   // s2 – upper threshold
 * );
 * }</pre>
 *
 * @author PTaillandier
 * @see CritereFonctionsCroyances
 * @see EvidenceTheory
 */
public class CritereFctCroyancesBasique extends CritereFonctionsCroyances {

	/**
	 * Lower threshold of the piecewise-linear zone. At or below this value the belief masses are
	 * constant and equal to {@code v1Pour} and {@code v1Contre} respectively.
	 */
	private double s1;

	/**
	 * The "for" belief mass at the upper threshold {@code s2}. When the criterion value is at or
	 * above {@code s2} the "for" mass is clamped to this value.
	 */
	private double v2Pour;

	/**
	 * The "for" belief mass at the lower threshold {@code s1}. When the criterion value is at or
	 * below {@code s1} the "for" mass is clamped to this value.
	 */
	private double v1Pour;

	/**
	 * The "against" belief mass at the lower threshold {@code s1}. When the criterion value is at
	 * or below {@code s1} the "against" mass is clamped to this value.
	 */
	private double v1Contre;

	/**
	 * The "against" belief mass at the upper threshold {@code s2}. When the criterion value is at
	 * or above {@code s2} the "against" mass is clamped to this value.
	 */
	private double v2Contre;

	/**
	 * Upper threshold of the piecewise-linear zone. At or above this value the belief masses are
	 * constant and equal to {@code v2Pour} and {@code v2Contre} respectively.
	 */
	private double s2;

	/**
	 * Pre-computed slope of the linear segment for the "for" belief function:
	 * {@code (v1Pour - v2Pour) / (s1 - s2)}.
	 * Set to {@code 0} when {@code s1 == s2} to avoid division by zero.
	 */
	private final double cdPour;

	/**
	 * Pre-computed intercept of the linear segment for the "for" belief function:
	 * {@code v1Pour - cdPour * s1}.
	 */
	private final double rPour;

	/**
	 * Pre-computed slope of the linear segment for the "against" belief function:
	 * {@code (v1Contre - v2Contre) / (s1 - s2)}.
	 * Set to {@code 0} when {@code s1 == s2} to avoid division by zero.
	 */
	private final double cdContre;

	/**
	 * Pre-computed intercept of the linear segment for the "against" belief function:
	 * {@code v1Contre - cdContre * s1}.
	 */
	private final double rContre;

	/**
	 * Constructs a piecewise-linear belief criterion. The slopes and intercepts of the linear
	 * interpolation segments are pre-computed from the supplied threshold and mass values.
	 *
	 * @param nom
	 *            the name of this criterion; must match the corresponding key in each
	 *            {@link Candidate}'s criterion-value map
	 * @param s1
	 *            the lower threshold below which the belief masses are constant
	 * @param v2Pour
	 *            the "for" belief mass when the criterion value is at or above {@code s2}
	 * @param v1Pour
	 *            the "for" belief mass when the criterion value is at or below {@code s1}
	 * @param v1Contre
	 *            the "against" belief mass when the criterion value is at or below {@code s1}
	 * @param v2Contre
	 *            the "against" belief mass when the criterion value is at or above {@code s2}
	 * @param s2
	 *            the upper threshold above which the belief masses are constant; should satisfy
	 *            {@code s2 > s1} for a meaningful linear zone
	 */
	public CritereFctCroyancesBasique(final String nom, final double s1, final double v2Pour, final double v1Pour,
			final double v1Contre, final double v2Contre, final double s2) {
		super(nom);
		this.s1 = s1;
		this.v2Pour = v2Pour;
		this.v1Pour = v1Pour;
		this.v1Contre = v1Contre;
		this.v2Contre = v2Contre;
		this.s2 = s2;
		cdPour = s1 == s2 ? 0 : (v1Pour - v2Pour) / (s1 - s2);
		rPour = v1Pour - cdPour * s1;
		cdContre = s1 == s2 ? 0 : (v1Contre - v2Contre) / (s1 - s2);
		rContre = v1Contre - cdContre * s1;
	}

	/**
	 * Returns the lower threshold {@code s1}.
	 *
	 * @return the lower boundary of the linear interpolation zone
	 */
	public double getS1() {
		return s1;
	}

	/**
	 * Sets the lower threshold {@code s1}. Note that changing this value does not recompute the
	 * pre-calculated slopes and intercepts; use a new instance for updated parameters.
	 *
	 * @param s1
	 *            the new lower boundary of the linear interpolation zone
	 */
	public void setS1(final double s1) {
		this.s1 = s1;
	}

	/**
	 * Returns the "for" belief mass at the upper threshold {@code s2}.
	 *
	 * @return the "for" mass at {@code s2}
	 */
	public double getV2Pour() {
		return v2Pour;
	}

	/**
	 * Sets the "for" belief mass at the upper threshold {@code s2}.
	 *
	 * @param v2Pour
	 *            the new "for" mass at {@code s2}
	 */
	public void setV2Pour(final double v2Pour) {
		this.v2Pour = v2Pour;
	}

	/**
	 * Returns the "for" belief mass at the lower threshold {@code s1}.
	 *
	 * @return the "for" mass at {@code s1}
	 */
	public double getV1Pour() {
		return v1Pour;
	}

	/**
	 * Sets the "for" belief mass at the lower threshold {@code s1}.
	 *
	 * @param v1Pour
	 *            the new "for" mass at {@code s1}
	 */
	public void setV1Pour(final double v1Pour) {
		this.v1Pour = v1Pour;
	}

	/**
	 * Returns the "against" belief mass at the lower threshold {@code s1}.
	 *
	 * @return the "against" mass at {@code s1}
	 */
	public double getV1Contre() {
		return v1Contre;
	}

	/**
	 * Sets the "against" belief mass at the lower threshold {@code s1}.
	 *
	 * @param v1Contre
	 *            the new "against" mass at {@code s1}
	 */
	public void setV1Contre(final double v1Contre) {
		this.v1Contre = v1Contre;
	}

	/**
	 * Returns the "against" belief mass at the upper threshold {@code s2}.
	 *
	 * @return the "against" mass at {@code s2}
	 */
	public double getV2Contre() {
		return v2Contre;
	}

	/**
	 * Sets the "against" belief mass at the upper threshold {@code s2}.
	 *
	 * @param v2Contre
	 *            the new "against" mass at {@code s2}
	 */
	public void setV2Contre(final double v2Contre) {
		this.v2Contre = v2Contre;
	}

	/**
	 * Returns the upper threshold {@code s2}.
	 *
	 * @return the upper boundary of the linear interpolation zone
	 */
	public double getS2() {
		return s2;
	}

	/**
	 * Sets the upper threshold {@code s2}. Note that changing this value does not recompute the
	 * pre-calculated slopes and intercepts; use a new instance for updated parameters.
	 *
	 * @param s2
	 *            the new upper boundary of the linear interpolation zone
	 */
	public void setS2(final double s2) {
		this.s2 = s2;
	}

	/**
	 * Computes the "against" belief mass for a candidate whose criterion value is {@code a}. The
	 * result is piecewise linear:
	 * <ul>
	 *   <li>{@code v1Contre} when {@code a ≤ s1}</li>
	 *   <li>{@code v2Contre} when {@code a ≥ s2}</li>
	 *   <li>{@code a * cdContre + rContre} otherwise (linear interpolation)</li>
	 * </ul>
	 * When {@code s1 == s2} the function degenerates to the constant {@code v1Contre}.
	 *
	 * @param a
	 *            the current numeric value of this criterion for a given candidate
	 * @return the "against" belief mass in {@code [0, 1]}
	 */
	@Override
	public double masseCroyanceContre(final double a) {
		if (s2 - s1 == 0) { return v1Contre; }
		if (a <= s1) { return v1Contre; }
		if (a >= s2) { return v2Contre; }
		return a * cdContre + rContre;
	}

	/**
	 * Computes the ignorance belief mass for a candidate whose criterion value is {@code a}.
	 * Ignorance is the residual uncertainty that can be assigned neither to "for" nor to
	 * "against": {@code max(0, 1 - massePour(a) - masseContre(a))}.
	 *
	 * @param a
	 *            the current numeric value of this criterion for a given candidate
	 * @return the ignorance belief mass in {@code [0, 1]}
	 */
	@Override
	public double masseCroyanceIgnorance(final double a) {
		return Math.max(0, 1 - (masseCroyancePour(a) + masseCroyanceContre(a)));
	}

	/**
	 * Computes the "for" belief mass for a candidate whose criterion value is {@code a}. The
	 * result is piecewise linear:
	 * <ul>
	 *   <li>{@code v1Pour} when {@code a ≤ s1}</li>
	 *   <li>{@code v2Pour} when {@code a ≥ s2}</li>
	 *   <li>{@code a * cdPour + rPour} otherwise (linear interpolation)</li>
	 * </ul>
	 * When {@code s1 == s2} the function degenerates to the constant {@code v1Pour}.
	 *
	 * @param a
	 *            the current numeric value of this criterion for a given candidate
	 * @return the "for" belief mass in {@code [0, 1]}
	 */
	@Override
	public double masseCroyancePour(final double a) {
		if (s2 - s1 == 0) { return v1Pour; }
		if (a <= s1) { return v1Pour; }
		if (a >= s2) { return v2Pour; }
		return a * cdPour + rPour;
	}

}
