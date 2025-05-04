/*******************************************************************************************************
 *
 * UserOnlyControlArchitecture.java, in gama.core, is part of the source code of the GAMA modeling and simulation
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
 * The Class UserOnlyControlArchitecture.
 */
@skill (
		name = IKeyword.USER_ONLY,
		concept = { IConcept.GUI, IConcept.ARCHITECTURE })
@doc ("A control architecture, based on FSM, where the user is being given complete control of the agents. This skill extends the UserControlArchitecture skill and take all his actions and variables")
// @vars({ @var(name = IKeyword.STATE, type = IType.STRING),
// @var(name = IKeyword.STATES, type = IType.LIST, constant = true) })
public class UserOnlyControlArchitecture extends UserControlArchitecture {

	/**
	 * @param desc
	 */
	public UserOnlyControlArchitecture(final IDescription desc) {
		super(desc);
	}

	@Override
	public Object executeOn(final IScope scope) throws GamaRuntimeException {
		return executeCurrentState(scope);
	}

	@Override
	public boolean init(final IScope scope) throws GamaRuntimeException {
		if (initPanel != null) { scope.execute(initPanel); }
		return true;
	}
}