/*******************************************************************************************************
 *
 * PopulationEditor.java, in gama.ui.shared.shared, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.parameters;

import java.util.Collection;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import gama.core.kernel.experiment.IParameter;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.population.IPopulation;
import gama.core.outputs.ValuedDisplayOutputFactory;
import gama.core.util.IContainer;
import gama.gaml.species.ISpecies;
import gama.ui.shared.interfaces.EditorListener;

/**
 * The Class PopulationEditor.
 */
@SuppressWarnings ({ "rawtypes", "unchecked" })
public class PopulationEditor extends AbstractEditor<IContainer> {

	/** The population displayer. */
	Text populationDisplayer;

	/**
	 * Instantiates a new population editor.
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
	PopulationEditor(final IAgent agent, final IParameter param, final EditorListener l) {
		super(agent, param, l);
	}

	@Override
	public Control createCustomParameterControl(final Composite compo) {
		populationDisplayer = new Text(compo, SWT.READ_ONLY);
		populationDisplayer.setEnabled(false);
		final GridData data = new GridData(GridData.FILL, GridData.CENTER, true, false);
		populationDisplayer.setLayoutData(data);
		return populationDisplayer;
	}

	@Override
	protected void displayParameterValue() {
		internalModification = true;
		final String s = currentValue instanceof IPopulation ? ((IPopulation) currentValue).getName()
				: currentValue == null ? "nil"
				: currentValue instanceof ISpecies ? currentValue.getGamlType().toString()
				: currentValue.serializeToGaml(true);
		populationDisplayer.setText(s);
		populationDisplayer.setToolTipText(s);
		internalModification = false;
	}

	@Override
	protected void applyBrowse() {
		if (currentValue instanceof Collection) { ValuedDisplayOutputFactory.browse((Collection) currentValue); }
	}

	@Override
	protected int[] getToolItems() { return new int[] { BROWSE }; }

}
