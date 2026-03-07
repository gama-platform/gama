/*******************************************************************************************************
 *
 * UserFirstControlArchitecture.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.gaml.architecture.user;

import gama.annotations.doc;
import gama.annotations.skill;
import gama.annotations.constants.IKeyword;
import gama.annotations.support.IConcept;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.runtime.scope.IScope;

// @vars({ @var(name = IKeyword.STATE, type = IType.STRING),
/**
 * The Class UserFirstControlArchitecture.
 */
// @var(name = IKeyword.STATES, type = IType.LIST, constant = true) })
@skill (
		name = IKeyword.USER_FIRST,
		concept = { IConcept.GUI, IConcept.ARCHITECTURE })
@doc("A control architecture, based on FSM, where the user is being given control before states / reflexes of the agent are executed. This skill extends the UserControlArchitecture skill and take all his actions and variables ")
public class UserFirstControlArchitecture extends UserControlArchitecture {

	@Override
	public Object executeOn(final IScope scope) throws GamaRuntimeException {
		executeCurrentState(scope);
		return executeReflexes(scope);
	}

	@Override
	public boolean init(final IScope scope) throws GamaRuntimeException {
		if (initPanel != null) {
			scope.execute(initPanel);
		}
		return super.init(scope);
	}
}