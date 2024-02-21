/*******************************************************************************************************
 *
 * UserInitPanelStatement.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.gaml.architecture.user;

import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.ISymbolKind;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.inside;
import gama.annotations.precompiler.GamlAnnotations.symbol;
import gama.core.common.interfaces.IKeyword;
import gama.gaml.descriptions.IDescription;

/**
 * The Class UserInitPanelStatement.
 */
@symbol(name = IKeyword.USER_INIT, kind = ISymbolKind.BEHAVIOR, with_sequence = true, concept = { IConcept.GUI })
@inside(kinds = { ISymbolKind.SPECIES, ISymbolKind.EXPERIMENT, ISymbolKind.MODEL })
@doc(value = "Used in the user control architecture, user_init is executed only once when the agent is created. It opens a special panel (if it contains user_commands statements). It is the equivalent to the init block in the basic agent architecture.", see = {
		IKeyword.USER_COMMAND, IKeyword.USER_INIT, IKeyword.USER_INPUT })
public class UserInitPanelStatement extends UserPanelStatement {

	/**
	 * Instantiates a new user init panel statement.
	 *
	 * @param desc the desc
	 */
	public UserInitPanelStatement(final IDescription desc) {
		super(desc);
	}

}
