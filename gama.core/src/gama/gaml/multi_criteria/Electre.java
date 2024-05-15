/*******************************************************************************************************
 *
 * Electre.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.gaml.multi_criteria;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class Electre.
 */
public class Electre {

	/** The poids. */
	private Map<String, Double> poids = new HashMap<>();
	
	/** The seuil coupe. */
	private double seuilCoupe = 0.7;
	
	/** The preference. */
	private Map<String, Double> preference = new HashMap<>();
	
	/** The indifference. */
	private Map<String, Double> indifference = new HashMap<>();
	
	/** The veto. */
	private Map<String, Double> veto = new HashMap<>();
	
	/** The critere ordonnes. */
	private final List<String> critereOrdonnes;

	/**
	 * Instantiates a new electre.
	 *
	 * @param critereOrdonnes the critere ordonnes
	 */
	public Electre(final List<String> critereOrdonnes) {
		super();
		this.critereOrdonnes = critereOrdonnes;
		for (final String param : critereOrdonnes) {
			poids.put(param, 5d);
			preference.put(param, 0.3);
			indifference.put(param, 0.1);
			veto.put(param, 0d);
		}
	}

	// TODO UCdetector: Remove unused code:
	// public Electre(final Electre electre) {
	// this.seuilCoupe = electre.seuilCoupe;
	// this.seuilCoupe = electre.seuilCoupe;
	// poids = new IMap<String, Double>();
	// for ( String param : electre.poids.keySet() ) {
	// poids.put(param, electre.poids.get(param));
	// preference.put(param, electre.preference.get(param));
	// indifference.put(param, electre.indifference.get(param));
	// veto.put(param, electre.veto.get(param));
	// }
	// critereOrdonnes = electre.critereOrdonnes;
	// }

	/**
	 * Gets the poids.
	 *
	 * @return the poids
	 */
	public Map<String, Double> getPoids() {
		return poids;
	}

	/**
	 * Sets the poids.
	 *
	 * @param poids the poids
	 */
	public void setPoids(final Map<String, Double> poids) {
		this.poids = poids;
		critereOrdonnes.clear();
		critereOrdonnes.addAll(poids.keySet());
		Collections.sort(critereOrdonnes);
	}

	/**
	 * Decision.
	 *
	 * @param locations the locations
	 * @return the candidate
	 */
	public Candidate decision(final List<Candidate> locations) {
		final int relation[][] = new int[locations.size()][locations.size()];

		for (int i = 0; i < locations.size() - 1; i++) {
			final Candidate act1 = locations.get(i);
			for (int j = i + 1; j < locations.size(); j++) {
				final Candidate act2 = locations.get(j);

				relation[i][j] = 0;
				relation[j][i] = 0;

				final String relationPaire = relation(act1, act2);
				if ("A1_P_A2".equals(relationPaire)) {
					relation[i][j] = 1;
					relation[j][i] = -1;
				} else if ("A2_P_A1".equals(relationPaire)) {
					relation[i][j] = -1;
					relation[j][i] = 1;
				}
			}
		}

		int max = -999999;
		Candidate candMax = null;
		for (int i = 0; i < locations.size(); i++) {
			int val = 0;
			for (int j = 0; j < locations.size(); j++) {
				val += relation[i][j];
			}
			if (val > max) {
				max = val;
				candMax = locations.get(i);
			}
		}
		return candMax;
	}

	/**
	 * Concordance.
	 *
	 * @param a1 the a 1
	 * @param a2 the a 2
	 * @param crit the crit
	 * @return the double
	 */
	private double concordance(final double a1, final double a2, final String crit) {
		double concordance = 0;
		final double prefCrit = preference.get(crit).doubleValue();
		final double indifCrit = indifference.get(crit).doubleValue();
		final double diff = a1 - a2;
		if (diff > -indifCrit) {
			concordance = 1;
		} else if (diff > -prefCrit) {
			concordance = (diff + prefCrit) / (prefCrit - indifCrit);
		}
		return concordance;
	}

	/**
	 * Discordance.
	 *
	 * @param a1 the a 1
	 * @param a2 the a 2
	 * @param crit the crit
	 * @return the double
	 */
	private double discordance(final double a1, final double a2, final String crit) {
		final double prefCrit = preference.get(crit).doubleValue();
		final double vetoCrit = veto.get(crit).doubleValue();
		final double diff = a1 - a2;
		double discordance = 0;
		if (diff < -vetoCrit) {
			discordance = 1;
		} else if (diff < -prefCrit) {
			discordance += (diff + prefCrit) / (prefCrit - vetoCrit);
		}
		return discordance;
	}

	/**
	 * Relation.
	 *
	 * @param val1 the val 1
	 * @param val2 the val 2
	 * @return the string
	 */
	private String relation(final Candidate val1, final Candidate val2) {
		// On commence par calculer pour chaque crit�re les concordances et les discordances entre
		// le vecteur courant et le vecteur de ref
		double concordGA1A2 = 0;
		double concordGA2A1 = 0;

		double poidsTot = 0;
		for (final String crit : poids.keySet()) {
			final double poidsCrit = poids.get(crit).doubleValue();
			final double a1 = val1.getValCriteria().get(crit).doubleValue();
			final double a2 = val2.getValCriteria().get(crit).doubleValue();
			poidsTot += poidsCrit;
			concordGA1A2 += poidsCrit * concordance(a1, a2, crit);
			concordGA2A1 += poidsCrit * concordance(a2, a1, crit);

		}
		concordGA1A2 /= poidsTot;
		concordGA2A1 /= poidsTot;

		double TA1A2 = 1;
		double TA2A1 = 1;
		for (final String crit : poids.keySet()) {
			final double a1 = val1.getValCriteria().get(crit).doubleValue();
			final double a2 = val2.getValCriteria().get(crit).doubleValue();

			final double discordanceA1A2 = discordance(a1, a2, crit);
			final double discordanceA2A1 = discordance(a2, a1, crit);

			if (discordanceA1A2 > concordGA1A2) {
				TA1A2 *= (1 - discordanceA1A2) / (1 - concordGA1A2);
			}
			if (discordanceA2A1 > concordGA2A1) {
				TA2A1 *= (1 - discordanceA2A1) / (1 - concordGA2A1);
			}
		}

		final double credibiliteGlobaleA1A2 = concordGA1A2 * TA1A2;
		final double credibiliteGlobaleA2A1 = concordGA2A1 * TA2A1;

		// on d�duit enfin de ces valeurs la relation existante entre les deux vecteurs de valeurs
		if (credibiliteGlobaleA1A2 < seuilCoupe) {
			if (credibiliteGlobaleA2A1 < seuilCoupe) { return "A1_R_A2"; }
			return "A2_P_A1";
		}
		if (credibiliteGlobaleA2A1 < seuilCoupe) { return "A1_P_A2"; }
		return "A1_I_A2";
	}

	@Override
	public String toString() {
		String str = this.seuilCoupe + ",";
		for (final String crit : critereOrdonnes) {
			str += crit + ":" + poids.get(crit) + "," + preference.get(crit) + "," + indifference.get(crit) + ","
					+ veto.get(crit);
		}
		return str;
	}

	/**
	 * Gets the seuil coupe.
	 *
	 * @return the seuil coupe
	 */
	public double getSeuilCoupe() {
		return seuilCoupe;
	}

	/**
	 * Sets the seuil coupe.
	 *
	 * @param seuilCoupe the new seuil coupe
	 */
	public void setSeuilCoupe(final double seuilCoupe) {
		this.seuilCoupe = seuilCoupe;
	}

	/**
	 * Gets the preference.
	 *
	 * @return the preference
	 */
	public Map<String, Double> getPreference() {
		return preference;
	}

	/**
	 * Sets the preference.
	 *
	 * @param preference the preference
	 */
	public void setPreference(final Map<String, Double> preference) {
		this.preference = preference;
	}

	/**
	 * Gets the indifference.
	 *
	 * @return the indifference
	 */
	public Map<String, Double> getIndifference() {
		return indifference;
	}

	/**
	 * Sets the indifference.
	 *
	 * @param indifference the indifference
	 */
	public void setIndifference(final Map<String, Double> indifference) {
		this.indifference = indifference;
	}

	/**
	 * Gets the veto.
	 *
	 * @return the veto
	 */
	public Map<String, Double> getVeto() {
		return veto;
	}

	/**
	 * Sets the veto.
	 *
	 * @param veto the veto
	 */
	public void setVeto(final Map<String, Double> veto) {
		this.veto = veto;
	}

	/**
	 * Gets the critere ordonnes.
	 *
	 * @return the critere ordonnes
	 */
	public List<String> getCritereOrdonnes() {
		return critereOrdonnes;
	}

}
