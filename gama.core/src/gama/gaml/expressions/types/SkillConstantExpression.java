/*******************************************************************************************************
 *
 * SkillConstantExpression.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.expressions.types;

import gama.annotations.precompiler.GamlProperties;
import gama.gaml.architecture.IArchitecture;
import gama.gaml.compilation.kernel.GamaSkillRegistry;
import gama.gaml.skills.ISkill;
import gama.gaml.types.IType;

/**
 * The Class SkillConstantExpression.
 */
public class SkillConstantExpression extends TypeConstantExpression {

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
	 * @see gama.gaml.expressions.IExpression#getDocumentation()
	 */
	@Override
	public Doc getDocumentation() { return ((ISkill) value).getDocumentation(); }

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
