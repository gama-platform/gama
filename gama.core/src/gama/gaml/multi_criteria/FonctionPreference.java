/*******************************************************************************************************
 *
 * FonctionPreference.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.gaml.multi_criteria;

/**
 * The Interface FonctionPreference.
 */
public interface FonctionPreference {

	/**
	 * Valeur.
	 *
	 * @param diff the diff
	 * @return the double
	 */
	public double valeur(double diff);

	/**
	 * Copie.
	 *
	 * @return the fonction preference
	 */
	public FonctionPreference copie();
}
