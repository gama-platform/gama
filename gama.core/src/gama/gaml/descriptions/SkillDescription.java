/*******************************************************************************************************
 *
 * SkillDescription.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.descriptions;

import java.util.Collection;
import java.util.Collections;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.skill;
import gama.core.common.interfaces.IKeyword;
import gama.core.common.interfaces.ISkill;
import gama.gaml.architecture.IArchitecture;
import gama.gaml.skills.Skill;

/**
 * The Class SkillDescription.
 */
public class SkillDescription extends TypeDescription {

	/** The instance. */
	Skill instance;

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
	public Class getJavaBase() { return javaBase; }

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
	public Collection<String> getActionNames() { return actions == null ? Collections.EMPTY_LIST : actions.keySet(); }

	@Override
	public Collection<String> getAttributeNames() {
		return attributes == null ? Collections.EMPTY_LIST : attributes.keySet();
	}

	/**
	 * Creates the instance.
	 *
	 * @return the skill
	 */
	public Skill createInstance() {
		Skill instance = null;
		try {
			instance = (Skill) getJavaBase().newInstance();
			instance.setDescription(this);
		} catch (InstantiationException | IllegalAccessException e) {}
		return instance;
	}

	/**
	 * Gets the single instance of SkillDescription.
	 *
	 * @return single instance of SkillDescription
	 */
	public Skill getInstance() {
		if (instance == null) { instance = createInstance(); }
		return instance;
	}

	/**
	 * Checks if is control.
	 *
	 * @return true, if is control
	 */
	public boolean isControl() { return isSet(Flag.IsControl); }

	@Override
	public String getTitle() { return "skill " + getName(); }

	@Override
	public Doc getDocumentation() {
		final doc d = getDocAnnotation();
		final Doc sb = new RegularDoc();
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
		doc d = null;
		if (javaBase.isAnnotationPresent(skill.class)) {
			skill s = javaBase.getAnnotation(skill.class);
			doc[] docs = s.doc();
			if (docs.length == 0) {
				if (javaBase.isAnnotationPresent(doc.class)) { d = javaBase.getAnnotation(doc.class); }
			} else {
				d = docs[0];
			}
		}
		return d;
	}

	/**
	 * Gets the deprecated.
	 *
	 * @return the deprecated
	 */
	public String getDeprecated() {
		doc d = getDocAnnotation();
		if (d == null) return null;
		String s = d.deprecated();
		if (s == null || s.isEmpty()) return null;
		return s;
	}

}