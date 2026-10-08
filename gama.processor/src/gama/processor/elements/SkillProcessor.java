/*******************************************************************************************************
 *
 * SkillProcessor.java, in gama.processor, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.processor.elements;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import gama.annotations.skill;

/**
 * The SkillProcessor is responsible for processing {@code @skill} annotations during the annotation processing phase.
 * 
 * <p>Skills in GAMA represent reusable behavioral components that can be attached to different species
 * to provide them with specific capabilities. Skills promote code reuse and modular design by allowing
 * common behaviors to be defined once and shared across multiple agent types. They encapsulate
 * actions, variables, and other functionality that can be dynamically added to agents.
 * 
 * <p>This processor handles:
 * <ul>
 * <li><strong>Skill Registration:</strong> Registering skill implementations with the GAMA runtime</li>
 * <li><strong>Attachment Rules:</strong> Processing which species the skill can be attached to</li>
 * <li><strong>Interface Validation:</strong> Ensuring skills implement the required ISkill interface</li>
 * <li><strong>Documentation Validation:</strong> Verifying proper documentation for skills</li>
 * </ul>
 * 
 * <h3>Skill Structure:</h3>
 * <p>A skill definition includes:
 * <ul>
 * <li>A unique name for the skill</li>
 * <li>The implementing Java class providing the skill behavior</li>
 * <li>List of species that can attach this skill</li>
 * <li>Actions and variables provided by the skill</li>
 * </ul>
 * 
 * <h3>Example usage:</h3>
 * <pre>{@code
 * @skill(
 *     name = "moving",
 *     attach_to = {"agent", "species"},
 *     doc = @doc("Provides movement capabilities to agents")
 * )
 * public class MovingSkill implements ISkill {
 *     
 *     @action(name = "move")
 *     public void move(IScope scope) {
 *         // Movement implementation
 *     }
 * }
 * }</pre>
 * 
 * @author GAMA Development Team
 * @since 1.0
 * @see skill
 * @see ElementProcessor
 */
public class SkillProcessor extends ElementProcessor<skill> {

	@Override
	protected Class<skill> getAnnotationClass() { return skill.class; }

	/**
	 * Creates the element.
	 *
	 * @param sb
	 *            the sb
	 * @param context
	 *            the context
	 * @param e
	 *            the e
	 * @param skill
	 *            the skill
	 */
	@Override
	public void createElement(final StringBuilder sb, final Element e, final skill skill) {
		verifyDoc(e, "skill " + skill.name(), skill);
		sb.append(in).append("_skill(").append(toJavaString(skill.name())).append(',')
				.append(toClassObject(rawNameOf(e.asType()))).append(',');
		toArrayOfStrings(skill.attach_to(), sb).append(");");
	}

	@Override
	protected boolean validateElement(final Element e) {
		boolean result = assertClassExtends(true, (TypeElement) e, context.getISkill());
		return result;
	}

}
