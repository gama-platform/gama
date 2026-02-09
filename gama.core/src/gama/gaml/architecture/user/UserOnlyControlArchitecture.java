/*******************************************************************************************************
 *
 * UserOnlyControlArchitecture.java, in gama.core, is part of the source code of the
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
import gama.annotations.support.IConcept;
import gama.api.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.runtime.scope.IScope;

/**
 * The Class UserOnlyControlArchitecture.
 */
@skill (
		name = IKeyword.USER_ONLY,
		concept = { IConcept.GUI, IConcept.ARCHITECTURE })
@doc ("A control architecture, based on FSM, where the user is being given complete control of the agents. This skill extends the UserControlArchitecture skill and take all his actions and variables")
// @vars({ @var(name = IKeyword.STATE, type = IType.STRING),
// @var(name = IKeyword.STATES, type = IType.LIST, constant = true) })
public class UserOnlyControlArchitecture extends UserControlArchitecture {

	@Override
	public Object executeOn(final IScope scope) throws GamaRuntimeException {
		return executeCurrentState(scope);
	}

	@Override
	public boolean init(final IScope scope) throws GamaRuntimeException {
		if (initPanel != null) {
			scope.execute(initPanel);
		}
		return true;
	}
}