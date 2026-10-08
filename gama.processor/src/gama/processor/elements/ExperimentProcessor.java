/*******************************************************************************************************
 *
 * ExperimentProcessor.java, in gama.processor, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.processor.elements;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import gama.annotations.experiment;
import gama.processor.Constants;

/**
 * The ExperimentProcessor is responsible for processing {@code @experiment} annotations during the annotation processing phase.
 * 
 * <p>Experiments in GAMA define the execution context and configuration for running simulations.
 * They specify how models should be executed, what parameters can be varied, and how results
 * should be collected and analyzed. The experiment processor handles the registration of these
 * experiment types with the GAMA runtime system.
 * 
 * <p>This processor handles:
 * <ul>
 * <li><strong>Experiment Registration:</strong> Registering experiment types with the GAMA runtime</li>
 * <li><strong>Factory Generation:</strong> Creating factory methods for experiment instantiation</li>
 * <li><strong>Validation:</strong> Ensuring experiment classes extend the required base classes</li>
 * <li><strong>Documentation Validation:</strong> Verifying proper documentation for experiments</li>
 * </ul>
 * 
 * <h3>Experiment Structure:</h3>
 * <p>An experiment definition includes:
 * <ul>
 * <li>A unique name for the experiment type</li>
 * <li>The implementing Java class that handles experiment execution</li>
 * <li>Constructor logic for creating experiment instances</li>
 * <li>Proper inheritance from IExperimentAgent</li>
 * </ul>
 * 
 * <h3>Example usage:</h3>
 * <pre>{@code
 * @experiment("batch")
 * public class BatchExperiment extends AbstractExperimentAgent {
 *     
 *     public BatchExperiment(IPopulation<? extends IAgent> s, int index) {
 *         super(s, index);
 *     }
 *     
 *     // Experiment implementation
 * }
 * }</pre>
 * 
 * @author GAMA Development Team
 * @since 1.0
 * @see experiment
 * @see ElementProcessor
 */
public class ExperimentProcessor extends ElementProcessor<experiment> {

	/**
	 * Returns the annotation class that this processor handles.
	 * 
	 * @return the {@link experiment} annotation class
	 */
	@Override
	protected Class<experiment> getAnnotationClass() { return experiment.class; }

	/**
	 * Creates the element code for an experiment annotation.
	 * 
	 * <p>This method generates the runtime registration code for experiment types.
	 * The generated code enables GAMA to recognize and instantiate experiments
	 * of the defined type.
	 * 
	 * @param sb the StringBuilder to append the generated registration code to
	 * @param e the class element annotated with @experiment
	 * @param exp the experiment annotation containing experiment metadata
	 */
	@Override
	public void createElement(final StringBuilder sb, final Element e, final experiment exp) {
		verifyDoc(e, "experiment " + exp.value(), exp);
		String clazz = e.asType().toString();
		sb.append(in).append("_experiment(").append(toJavaString(exp.value())).append(",(p, i)->new ")
				.append(rawNameOf(e.asType())).append("(p, i),").append(clazz).append(".class);");
	}

	/**
	 * Validates that an experiment element meets the requirements for experiment processing.
	 * 
	 * <p>This method ensures that the experiment class properly extends the IExperimentAgent
	 * interface, which is required for all experiment implementations in GAMA.
	 * 
	 * @param e the element to validate (should be a class annotated with @experiment)
	 * @return {@code true} if the element extends IExperimentAgent, {@code false} otherwise
	 */
	@Override
	protected boolean validateElement(final Element e) {
		boolean result =
				assertClassExtends(true, (TypeElement) e, context.getType(Constants.IExperimentAgentClassName));
		return result;
	}
}
