/*******************************************************************************************************
 *
 * ExperimentStateProvider.java, in gama.ui.shared.experiment, is part of the source code of the GAMA modeling and
 * simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.experiment.commands;

import static java.util.Map.of;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;
import org.eclipse.ui.services.IServiceLocator;

import gama.core.kernel.experiment.IExperimentSpecies;
import gama.core.runtime.GAMA;
import gama.core.runtime.IExperimentStateListener;
import gama.ui.shared.utils.WorkbenchHelper;

/**
 * The Class SimulationStateProvider.
 */
public class ExperimentStateProvider extends AbstractSourceProvider implements IExperimentStateListener {

    /** The Constant SOURCE_NAMES. */
    final static String[] SOURCE_NAMES = { EXPERIMENT_RUNNING_STATE, EXPERIMENT_TYPE, EXPERIMENT_STEPBACK };

    /** The Constant map. */
    private final Map<String, String> states = new HashMap<>(of(EXPERIMENT_RUNNING_STATE, State.NONE.name(),
	    EXPERIMENT_TYPE, Type.NONE.name(), EXPERIMENT_STEPBACK, CANNOT_STEP_BACK));

    @Override
    public void initialize(final IServiceLocator locator) {
	super.initialize(locator);
	GAMA.addExperimentStateListener(this);
    }

    @Override
    public void dispose() {
	GAMA.removeExperimentStateListener(this);
    }

    @Override
    public String[] getProvidedSourceNames() {
	return SOURCE_NAMES;
    }

    /**
     * FALSE: should target the experiment ?
     */
    @Override
    public Map<String, String> getCurrentState() {
	return states;
    }

    /**
     * Change the UI state based on the state of the experiment (see
     * IExperimentStateListener.State)
     */
    @Override
    public void updateStateTo(final IExperimentSpecies exp, final State newState) {
	String state = newState.name();
	if (!Objects.equals(states.get(EXPERIMENT_RUNNING_STATE), state)) {
	    states.put(EXPERIMENT_RUNNING_STATE, state);
	    WorkbenchHelper.run(() -> fireSourceChanged(ISources.WORKBENCH, EXPERIMENT_RUNNING_STATE, state));
	}
	String simulationType = exp == null ? Type.NONE.name()
		: exp.isTest() ? Type.TEST.name()
			: exp.isBatch() ? Type.BATCH.name()
				: exp.isMemorize() ? Type.RECORD.name() : Type.REGULAR.name();
	if (!Objects.equals(states.get(EXPERIMENT_TYPE), simulationType)) {
	    states.put(EXPERIMENT_TYPE, simulationType);
	    WorkbenchHelper.run(() -> fireSourceChanged(ISources.WORKBENCH, EXPERIMENT_TYPE, simulationType));
	}
	String canStepBack = exp != null && exp.getAgent() != null && exp.getAgent().canStepBack() ? CAN_STEP_BACK
		: CANNOT_STEP_BACK;
	if (!Objects.equals(states.get(EXPERIMENT_STEPBACK), canStepBack)) {
	    states.put(EXPERIMENT_STEPBACK, canStepBack);
	    WorkbenchHelper.run(() -> fireSourceChanged(ISources.WORKBENCH, EXPERIMENT_STEPBACK, canStepBack));
	}
    }

}