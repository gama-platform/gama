/*******************************************************************************************************
 *
 * ClassDescription.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.descriptions;

import org.eclipse.emf.ecore.EObject;

import gama.core.common.interfaces.IKeyword;
import gama.core.metamodel.agent.IObject;
import gama.gaml.compilation.GAML;
import gama.gaml.compilation.IObjectConstructor;
import gama.gaml.expressions.types.ClassConstantExpression;
import gama.gaml.statements.Facets;
import gama.gaml.types.GamaType;
import gama.gaml.types.IType;

/**
 *
 */
public class ClassDescription extends TypeDescription {

	/** The constructor. */
	private final IObjectConstructor constructor;

	/**
	 * Instantiates a new class description.
	 *
	 * @param keyword
	 *            the keyword
	 * @param clazz
	 *            the clazz
	 * @param macroDesc
	 *            the macro desc
	 * @param parent
	 *            the parent
	 * @param cp
	 *            the cp
	 * @param source
	 *            the source
	 * @param facets
	 *            the facets
	 * @param plugin
	 *            the plugin
	 */
	public ClassDescription(final String keyword, final Class clazz, final ModelDescription macroDesc,
			final TypeDescription parent, final Iterable<? extends IDescription> cp, final EObject source,
			final IObjectConstructor constructor, final Facets facets, final String plugin) {
		super(keyword, clazz, macroDesc, parent, cp, source, facets, plugin);
		this.constructor = constructor == null ? IObjectConstructor.DEFAULT : constructor;
	}

	@Override
	public boolean isBuiltIn() { return false; }

	@Override
	public String getTitle() { return IKeyword.CLASS + " " + getName(); }

	@Override
	public ClassDescription getParent() { return (ClassDescription) super.getParent(); }

	@Override
	public Class<? extends IObject> getJavaBase() { return IObject.class; }

	@Override
	public void documentThis(final Doc sb) {
		final String parentName = getParent() == null ? "nil" : getParent().getName();
		final String hostName = getEnclosingDescription() == null ? null : getEnclosingDescription().getName();
		sb.append("<b>Subclass of:</b> ").append(parentName).append("<br>");
		if (hostName != null) { sb.append("<b>Class defined in:</b> ").append(hostName).append("<br>"); }
		// final Iterable<String> skills = getSkillsNames();
		// if (!Iterables.isEmpty(skills)) { sb.append("<b>Skills:</b> ").append(skills.toString()).append("<br>"); }
		documentAttributes(sb);
		documentActions(sb);
	}

	@Override
	public ClassConstantExpression getConstantExpr() {
		if (constantExpr == null) {
			final IType type = GamaType.from(ClassDescription.this);
			constantExpr = GAML.getExpressionFactory().createSpeciesConstant(type);
		}
		return (ClassConstantExpression) constantExpr;
	}

}
