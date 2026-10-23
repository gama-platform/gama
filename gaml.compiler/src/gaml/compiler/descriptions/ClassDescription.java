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
package gaml.compiler.descriptions;

import java.util.List;

import org.eclipse.emf.ecore.EObject;

import gama.annotations.constants.IKeyword;
import gama.api.compilation.descriptions.IActionDescription;
import gama.api.compilation.descriptions.IClassDescription;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IVariableDescription;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.constants.IGamlIssue;
import gama.api.gaml.GAML;
import gama.api.gaml.symbols.Facets;
import gama.api.gaml.symbols.ISymbol;
import gama.api.kernel.GamaMetaModel;
import gama.api.kernel.object.IClass;
import gama.api.kernel.object.IObject;
import gama.api.kernel.object.IObjectConstructor;
import gama.api.utils.JavaUtils;

/**
 *
 */
public class ClassDescription extends TypeDescription implements IClassDescription {

	private final Class<? extends IObject> javaBase;

	private IObjectConstructor objectConstructor;

	/**
	 * Instantiates a new class description. ONLY USED FOR THE BUILT-IN OBJECT DESCRIPTION
	 *
	 * @param plugin
	 *            the plugin
	 */
	public ClassDescription(final String name, final String plugin) {
		this(name, IObject.class, null, null, plugin);
	}

	public ClassDescription(final String name, final Class<? extends IObject> clazz,
			final IClassDescription parent, final IObjectConstructor constructor, final String plugin) {
		this(IKeyword.CLASS, clazz, GamaMetaModel.getSpeciesDescription(IKeyword.MODEL), parent, null, null,
				new Facets(IKeyword.NAME, name), plugin);
		objectConstructor = constructor;
		set(Flag.IsBuiltIn);
		originName = plugin;
		setIf(Flag.IsAbstract, constructor == null);
	}

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
	public ClassDescription(final String keyword, final Class<? extends IObject> clazz, final IDescription macroDesc,
			final IClassDescription parent, final Iterable<? extends IDescription> cp, final EObject source,
			final Facets facets, final String plugin) {
		super(keyword, clazz, macroDesc, parent, cp, source, facets, plugin);
		javaBase = clazz;
		if (plugin == null) { unSet(Flag.IsBuiltIn); }
	}

	@Override
	public String getTitle() { return IKeyword.CLASS + " " + getName(); }

	@Override
	public ClassDescription getParent() { return (ClassDescription) super.getParent(); }

	/**
	 * Compile as built in.
	 *
	 * @return the i species
	 */
	@Override
	public IClass compileAsBuiltIn() {
		return (IClass) super.compile();
	}

	@Override
	public ISymbol compile() {
		return isBuiltIn() ? GamaMetaModel.getClass(getName()) : super.compile();
	}

	@Override
	public Class<? extends IObject> getJavaBase() {
		if (javaBase != null) return javaBase;
		return getParent() == null ? IObject.class : getParent().getJavaBase();
	}

	@Override
	public IObjectConstructor getConstructor() {
		if (isBuiltIn()) return objectConstructor;
		return getParent() == null ? null : getParent().getConstructor();
	}

	@Override
	public boolean isAbstract() {
		return super.isAbstract() || getJavaBase() != IObject.class && getConstructor() == null;
	}

	/**
	 * Copies annotated members from the implementation hierarchy, from base to derived classes.
	 */
	@Override
	public void copyJavaAdditions() {
		for (final Class<?> clazz : JavaUtils.collectImplementationClasses(getJavaBase(), List.of(),
				GAML.getAdditionClasses())) {
			for (final IDescription member : GAML.getAdditions(clazz)) {
				final IDescription copy = member.copy(this);
				copy.setOriginName("built-in class " + clazz.getName());
				addChild(copy);
			}
		}
	}

	@Override
	public void addChild(final IDescription child) {
		if (child instanceof IActionDescription ad) {
			addAction(ad);
		} else if (child instanceof IVariableDescription vd) {
			addOwnAttribute(vd);
		} else if (child != null) {
			child.error(child.getKeyword() + " cannot be defined in " + getKeyword(), IGamlIssue.WRONG_CONTEXT);
		}
	}

	/**
	 * Document this.
	 *
	 * @param sb
	 *            the sb
	 */
	@Override
	public void documentThis(final IGamlDocumentation sb) {
		final String parentName = getParent() == null ? "nil" : getParent().getName();
		final String hostName = getEnclosingDescription() == null ? null : getEnclosingDescription().getName();
		sb.append("<b>Subclass of:</b> ").append(parentName).append("<br>");
		if (hostName != null) { sb.append("<b>Class defined in:</b> ").append(hostName).append("<br>"); }
		documentAttributes(sb);
		documentActions(sb);
	}

	/**
	 * Checks if is class.
	 *
	 * @return true, if is class
	 */
	@Override
	public boolean isClass() { return true; }

	/**
	 * Checks if is species.
	 *
	 * @return true, if is species
	 */
	@Override
	public boolean isSpecies() { return false; }

	/**
	 * Checks if is experiment.
	 *
	 * @return true, if is experiment
	 */
	@Override
	public boolean isExperiment() { return false; }

	/**
	 * Checks if is model.
	 *
	 * @return true, if is model
	 */
	@Override
	public boolean isModel() { return false; }

	/**
	 * Checks if is skill.
	 *
	 * @return true, if is skill
	 */
	@Override
	public boolean isSkill() { return false; }

}