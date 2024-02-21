/*******************************************************************************************************
 *
 * NumberEditor.java, in gama.ui.shared.shared, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.parameters;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import gama.core.kernel.experiment.IParameter;
import gama.core.kernel.experiment.InputParameter;
import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.ui.shared.interfaces.EditorListener;
import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.resources.IGamaIcons;

/**
 * The Class NumberEditor.
 *
 * @param <T>
 *            the generic type
 */
public abstract class NumberEditor<T extends Comparable> extends ExpressionBasedEditor<T> {

	/** The Constant UNDEFINED_LABEL. */
	static final String UNDEFINED_LABEL = "-- Undefined --";

	/**
	 * Instantiates a new number editor.
	 *
	 * @param scope
	 *            the scope
	 * @param supportParameter
	 *            the support parameter
	 * @param whenModified
	 *            the when modified
	 * @param canBeNull
	 *            the can be null
	 */
	public NumberEditor(final IScope scope, final InputParameter supportParameter, final EditorListener<T> whenModified,
			final boolean canBeNull) {
		this(scope.getAgent(), supportParameter, whenModified, canBeNull);
	}

	/**
	 * Instantiates a new number editor.
	 *
	 * @param scope
	 *            the scope
	 * @param a
	 *            the a
	 * @param p
	 *            the p
	 * @param l
	 *            the l
	 * @param canBeNull
	 *            the can be null
	 */
	public NumberEditor(final IAgent a, final IParameter p, final EditorListener<T> l, final boolean canBeNull) {
		super(a, p, l);
		acceptNull = canBeNull;
	}

	@Override
	public Control createCustomParameterControl(final Composite composite) throws GamaRuntimeException {
		normalizeValues();
		return super.createCustomParameterControl(composite);
	}

	/**
	 * Normalize values.
	 *
	 * @return the number
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	protected abstract Number normalizeValues() throws GamaRuntimeException;

	@Override
	protected void updateToolbar() {
		super.updateToolbar();
		final Label t = editorToolbar.getItem(DEFINE);
		if (t == null || t.isDisposed()) return;
		if (param.isDefined()) {
			t.setToolTipText("Set the parameter to undefined");
			t.setImage(GamaIcon.named(IGamaIcons.SMALL_UNDEFINE).image());
			editorControl.setActive(true);
		} else {
			t.setToolTipText("Define the parameter (currently undefined)");
			t.setImage(GamaIcon.named(IGamaIcons.SMALL_DEFINE).image());
			editorControl.setActive(false);
		}
	}

	@Override
	protected void applyDefine() {
		if (param.isDefined()) {
			param.setDefined(false);
			internalModification = true;
			editorControl.setText(UNDEFINED_LABEL);
			internalModification = false;
			modifyValue(null);
		} else {
			param.setDefined(true);
			internalModification = true;
			expression.modifyValue();
			internalModification = false;
		}
		updateToolbar();
	}

	@Override
	protected int[] getToolItems() {
		if (acceptNull) return new int[] { DEFINE, PLUS, MINUS, REVERT };
		return new int[] { PLUS, MINUS, REVERT };
	}
}
