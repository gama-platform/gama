/*******************************************************************************************************
 *
 * ListEditor.java, in gama.ui.shared.shared, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.parameters;

import org.eclipse.jface.dialogs.IDialogConstants;

import gama.core.kernel.experiment.IParameter;
import gama.core.metamodel.agent.IAgent;
import gama.core.util.IList;
import gama.gaml.types.IType;
import gama.gaml.types.Types;
import gama.ui.shared.interfaces.EditorListener;
import gama.ui.shared.utils.WorkbenchHelper;

/**
 * The Class ListEditor.
 */
public class ListEditor extends ExpressionBasedEditor<java.util.List<?>> {

	/**
	 * Instantiates a new list editor.
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
	ListEditor(final IAgent agent, final IParameter param, final EditorListener<java.util.List<?>> l) {
		super(agent, param, l);
	}

	@SuppressWarnings ("rawtypes")
	@Override
	public void applyEdit() {
		if (currentValue instanceof IList) {
			final ListEditorDialog d =
					new ListEditorDialog(WorkbenchHelper.getShell(), (IList) currentValue, param.getName());
			if (d.open() == IDialogConstants.OK_ID) { modifyAndDisplayValue(d.getList(ListEditor.this)); }
		}
	}

	@Override
	protected void updateToolbar() {
		super.updateToolbar();
		editorToolbar.enable(EDIT, currentValue instanceof IList);
	}

	@SuppressWarnings ({ "unchecked", "rawtypes" })
	@Override
	public IType getExpectedType() { return Types.LIST; }

	@Override
	protected int[] getToolItems() { return new int[] { EDIT, REVERT }; }

}
