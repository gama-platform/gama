/*******************************************************************************************************
 *
 * SkillConstantExpression.java, in gama.core, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.expressions;

import gama.api.additions.registries.GamaSkillRegistry;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.IType;
import gama.api.kernel.skill.IArchitecture;
import gama.api.kernel.skill.ISkill;
import gama.api.utils.GamlProperties;

/**
 * The Class SkillConstantExpression.
 */
public class SkillConstantExpression extends ConstantExpression {

	/**
	 * Instantiates a new skill constant expression.
	 *
	 * @param val
	 *            the val
	 * @param t
	 *            the t
	 */
	public SkillConstantExpression(final String val, final IType<ISkill> t) {
		super(GamaSkillRegistry.INSTANCE.getSkillInstanceFor(val), t);
	}

	/**
	 * @see gama.api.gaml.expressions.IExpression#getDocumentation()
	 */
	@Override
	public IGamlDocumentation getDocumentation() { return ((ISkill) value).getDocumentation(); }

	@Override
	public String getTitle() { return ((ISkill) value).getTitle(); }

	@Override
	public String literalValue() {
		return ((ISkill) value).getName();
	}

	@Override
	public void collectMetaInformation(final GamlProperties meta) {
		final ISkill skill = (ISkill) value;
		meta.put(GamlProperties.PLUGINS, skill.getDefiningPlugin());
		meta.put(skill instanceof IArchitecture ? GamlProperties.ARCHITECTURES : GamlProperties.SKILLS,
				skill.getName());
	}

}
