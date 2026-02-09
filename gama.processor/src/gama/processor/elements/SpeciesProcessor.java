/*******************************************************************************************************
 *
 * SpeciesProcessor.java, in gama.processor, is part of the source code of the GAMA modeling and simulation platform
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

import gama.annotations.species;

/**
 * The SpeciesProcessor is responsible for processing {@code @species} annotations during the annotation processing phase.
 * 
 * <p>Species in GAMA represent agent types - the fundamental building blocks of agent-based models.
 * Each species defines a template for creating agents with specific behaviors, attributes, and skills.
 * The species processor handles the registration of these agent types with the GAMA runtime system.
 * 
 * <p>This processor handles:
 * <ul>
 * <li><strong>Species Registration:</strong> Registering species with the GAMA type system</li>
 * <li><strong>Constructor Generation:</strong> Creating factory methods for agent instantiation</li>
 * <li><strong>Skill Integration:</strong> Processing the skills that the species requires</li>
 * <li><strong>Inheritance Validation:</strong> Ensuring species classes extend the proper agent interface</li>
 * <li><strong>Documentation Validation:</strong> Verifying that species have proper documentation</li>
 * </ul>
 * 
 * <h3>Species Definition Structure:</h3>
 * <p>A typical species definition includes:
 * <ul>
 * <li>A unique name for the species</li>
 * <li>The implementing Java class</li>
 * <li>A list of required skills</li>
 * <li>Documentation describing the species purpose and behavior</li>
 * </ul>
 * 
 * <h3>Example usage:</h3>
 * <pre>{@code
 * @species(
 *     name = "predator",
 *     skills = {"moving", "perception"},
 *     doc = @doc("A predator species that hunts prey in the environment")
 * )
 * public class PredatorSpecies extends AbstractAgent {
 *     public PredatorSpecies(IPopulation<? extends IAgent> s, int index) {
 *         super(s, index);
 *     }
 *     // Species implementation
 * }
 * }</pre>
 * 
 * @author GAMA Development Team
 * @since 1.0
 * @see species
 * @see ElementProcessor
 */
public class SpeciesProcessor extends ElementProcessor<species> {

	/**
	 * Returns the annotation class that this processor handles.
	 * 
	 * @return the {@link species} annotation class
	 */
	@Override
	protected Class<species> getAnnotationClass() { return species.class; }

	/**
	 * Creates the element code for a species annotation.
	 * 
	 * <p>This method generates the runtime registration code for a GAMA species. The process involves:
	 * <ol>
	 * <li>Extracting the species name from the annotation</li>
	 * <li>Generating a factory function for agent creation</li>
	 * <li>Processing the list of required skills</li>
	 * <li>Validating and including documentation</li>
	 * </ol>
	 * 
	 * <p>The generated code enables GAMA to:
	 * <ul>
	 * <li>Recognize the species type in GAML models</li>
	 * <li>Instantiate agents of this species</li>
	 * <li>Apply the required skills to agents</li>
	 * <li>Provide proper type checking and validation</li>
	 * </ul>
	 * 
	 * @param sb the StringBuilder to append the generated registration code to
	 * @param e the class element annotated with @species
	 * @param spec the species annotation containing the species metadata
	 */
	@Override
	public void createElement(final StringBuilder sb, final Element e, final species spec) {
		final String clazz = rawNameOf(e.asType());
		verifyDoc(e, "species " + spec.name(), spec);
		sb.append(in).append("_species(").append(toJavaString(spec.name())).append(",").append(toClassObject(clazz))
				.append(",(p, i)->").append("new ").append(clazz).append("(p, i),");
		toArrayOfStrings(spec.skills(), sb).append(");");
	}

	/**
	 * Validates that a species element meets the requirements for species processing.
	 * 
	 * <p>This method ensures that the species class properly extends the IAgent interface,
	 * which is required for all agent types in GAMA. The IAgent interface provides the
	 * fundamental contract that all agents must implement, including:
	 * <ul>
	 * <li>Basic agent lifecycle management</li>
	 * <li>Attribute access and modification</li>
	 * <li>Scope and context handling</li>
	 * <li>Integration with the simulation runtime</li>
	 * </ul>
	 * 
	 * @param e the element to validate (should be a class annotated with @species)
	 * @return {@code true} if the element extends IAgent, {@code false} otherwise
	 */
	@Override
	protected boolean validateElement(final Element e) {
		boolean result = assertClassExtends(true, (TypeElement) e, context.getIAgent());
		return result;
	}

}
