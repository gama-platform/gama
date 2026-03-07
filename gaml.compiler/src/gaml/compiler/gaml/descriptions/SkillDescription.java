/*******************************************************************************************************
 *
 * SkillDescription.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.descriptions;

import java.lang.reflect.Constructor;
import java.util.Collection;

import gama.annotations.doc;
import gama.annotations.skill;
import gama.annotations.constants.IKeyword;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.ISkillDescription;
import gama.api.compilation.documentation.GamlRegularDocumentation;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.kernel.skill.IArchitecture;
import gama.api.kernel.skill.ISkill;

/**
 * The Class SkillDescription.
 */
public class SkillDescription extends TypeDescription implements ISkillDescription {

	/** The INSTANCE. */
	ISkill instance;

	/** The is control. */
	// final boolean isControl;

	/** The java base. */
	final Class<? extends ISkill> javaBase;

	/**
	 * Instantiates a new skill description.
	 *
	 * @param name
	 *            the name
	 * @param support
	 *            the support
	 * @param children
	 *            the children
	 * @param plugin
	 *            the plugin
	 */
	public SkillDescription(final String name, final Class<? extends ISkill> support,
			final Iterable<IDescription> children, final String plugin) {
		super(IKeyword.SKILL, support, null, null, children, null, null, plugin);
		this.name = name;
		this.javaBase = support;
		setIf(Flag.IsControl, IArchitecture.class.isAssignableFrom(support));

	}

	@Override
	public Class<? extends ISkill> getJavaBase() { return javaBase; }

	@Override
	public String getName() { return name; }

	@Override
	public IDescription addChild(final IDescription child) {
		child.setEnclosingDescription(this);
		switch (child) {
			case ActionDescription ad:
				addAction(ad);
				break;
			case VariableDescription vd:
				addOwnAttribute(vd);
				break;
			default:
				break;
		}
		return child;
	}

	@Override
	public boolean visitChildren(final DescriptionVisitor<IDescription> visitor) {
		return visitOwnChildren(visitor);
	}

	@Override
	public Collection<String> getActionNames() { return getOwnActions().keySet(); }

	@Override
	public Collection<String> getAttributeNames() { return getOwnAttributes().keySet(); }

	/**
	 * Creates the INSTANCE.
	 *
	 * @return the skill
	 */
	public ISkill createInstance() {
		ISkill instance1 = null;
		try {
			Constructor<? extends ISkill> c = getJavaBase().getConstructor();
			instance1 = c.newInstance();
		} catch (Exception e) {
			return null;
		}
		instance1.setDescription(this);
		return instance1;
	}

	@Override
	public IArchitecture createArchitectureInstance() {
		if (!isControl()) return null;
		IArchitecture instance1 = null;
		try {
			Constructor<? extends ISkill> c = getJavaBase().getConstructor();
			instance1 = (IArchitecture) c.newInstance();
			instance1.setDescription(this);
		} catch (Exception e) {
			return null;
		}
		return instance1;
	}

	/**
	 * Gets the single INSTANCE of SkillDescription.
	 *
	 * @return single INSTANCE of SkillDescription
	 */
	@Override
	public ISkill getInstance() {
		if (instance == null) { instance = createInstance(); }
		return instance;
	}

	/**
	 * Checks if is control.
	 *
	 * @return true, if is control
	 */
	@Override
	public boolean isControl() { return isSet(Flag.IsControl); }

	@Override
	public String getTitle() { return "skill " + getName(); }

	@Override
	public IGamlDocumentation getDocumentation() {
		final doc d = getDocAnnotation();
		final IGamlDocumentation sb = new GamlRegularDocumentation();
		if (d != null) {
			String s = d.value();
			if (s != null && !s.isEmpty()) {
				sb.append(s);
				sb.append("<br/>");
			}
			String deprecated = d.deprecated();
			if (deprecated != null && !deprecated.isEmpty()) {
				sb.append("<b>Deprecated</b>: ").append("<i>").append(deprecated).append("</i><br/>");
			}
		}
		documentAttributes(sb);
		documentActions(sb);

		return sb;

	}

	/**
	 * Gets the doc annotation.
	 *
	 * @return the doc annotation
	 */
	public doc getDocAnnotation() {
		skill s = javaBase.getAnnotation(skill.class);
		doc[] docs = s.doc();
		doc d = null;
		if (docs.length == 0) {
			if (javaBase.isAnnotationPresent(doc.class)) { d = javaBase.getAnnotation(doc.class); }
		} else {
			d = docs[0];
		}
		return d;
	}

	/**
	 * Gets the deprecated.
	 *
	 * @return the deprecated
	 */
	@Override
	public String getDeprecated() {
		doc d = getDocAnnotation();
		if (d == null) return null;
		String s = d.deprecated();
		if (s == null || s.isEmpty()) return null;
		return s;
	}

}