/*******************************************************************************************************
 *
 * UserLastControlArchitecture.java, in gama.core, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.architecture.user;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.skill;
import gama.annotations.precompiler.IConcept;
import gama.core.common.interfaces.IKeyword;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.descriptions.IDescription;

/**
 * The Class UserLastControlArchitecture.
 */
@skill (
		name = IKeyword.USER_LAST,
		concept = { IConcept.GUI, IConcept.ARCHITECTURE })
@doc ("A control architecture, based on FSM, where the user is being given control after states / reflexes of the agent are executed. This skill extends the UserControlArchitecture skill and take all his actions and variables ")
// @vars({ @var(name = IKeyword.STATE, type = IType.STRING),
// @var(name = IKeyword.STATES, type = IType.LIST, constant = true) })
public class UserLastControlArchitecture extends UserControlArchitecture {

	/**
	 * @param desc
	 */
	public UserLastControlArchitecture(final IDescription desc) {
		super(desc);
	}

	@Override
	public Object executeOn(final IScope scope) throws GamaRuntimeException {
		executeReflexes(scope);
		return executeCurrentState(scope);
	}

	@Override
	public boolean init(final IScope scope) throws GamaRuntimeException {
		if (!super.init(scope)) return false;
		if (initPanel != null) { scope.execute(initPanel); }
		return true;
	}
}