/*******************************************************************************************************
 *
 * GamlSkill.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.skills;

import gama.annotations.precompiler.GamlAnnotations.inside;
import gama.annotations.precompiler.GamlAnnotations.symbol;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.ISymbolKind;
import gama.core.common.interfaces.IKeyword;
import gama.gaml.descriptions.IDescription;

/**
 * The class GamlSkill. A skill specified in GAML
 */
@symbol (
		name = { IKeyword.SKILL },
		kind = ISymbolKind.SPECIES,
		with_sequence = true,
		concept = { IConcept.SKILL })
@inside (
		kinds = { ISymbolKind.MODEL })
public class GamlSkill extends Skill {

	/**
	 * @param desc
	 */
	public GamlSkill(final IDescription desc) {
		super(desc);
	}

}
