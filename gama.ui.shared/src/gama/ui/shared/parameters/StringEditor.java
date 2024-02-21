/*******************************************************************************************************
 *
 * StringEditor.java, in gama.ui.shared.shared, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.parameters;

import gama.core.kernel.experiment.IParameter;
import gama.core.metamodel.agent.IAgent;
import gama.gaml.types.IType;
import gama.gaml.types.Types;
import gama.ui.shared.interfaces.EditorListener;

/**
 * The Class StringEditor.
 */
public class StringEditor extends ExpressionBasedEditor<String> {

	/**
	 * Instantiates a new string editor.
	 *
	 * @param scope
	 *            the scope
	 * @param agent
	 *            the agent
	 * @param param
	 *            the param
	 * @param l
	 *            the l
	 */
	StringEditor(final IAgent agent, final IParameter param, final EditorListener<String> l) {
		super(agent, param, l);
	}

	@Override
	public IType<String> getExpectedType() { return Types.STRING; }

}
