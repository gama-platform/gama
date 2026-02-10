/*******************************************************************************************************
 *
 * SpeciesDescription.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.descriptions;

import static com.google.common.collect.Iterables.transform;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.ecore.EObject;

import com.google.common.collect.Iterables;

import gama.annotations.support.ITypeProvider;
import gama.api.additions.IGamaHelper;
import gama.api.additions.registries.AgentConstructorsRegistry;
import gama.api.additions.registries.GamaSkillRegistry;
import gama.api.compilation.descriptions.IActionDescription;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.ISkillDescription;
import gama.api.compilation.descriptions.ISpeciesDescription;
import gama.api.compilation.descriptions.IStatementDescription;
import gama.api.compilation.descriptions.ITypeDescription;
import gama.api.compilation.descriptions.IVariableDescription;
import gama.api.compilation.documentation.GamlRegularDocumentation;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.compilation.serialization.ISymbolSerializer;
import gama.api.constants.IGamlIssue;
import gama.api.constants.IKeyword;
import gama.api.data.factories.GamaMapFactory;
import gama.api.data.objects.IMap;
import gama.api.gaml.GAML;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IExpressionDescription;
import gama.api.gaml.symbols.Facets;
import gama.api.gaml.types.GamaType;
import gama.api.gaml.types.IType;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IAgentConstructor;
import gama.api.kernel.agent.IMacroAgent;
import gama.api.kernel.skill.ISkill;
import gama.api.utils.GamlProperties;
import gama.api.utils.JavaUtils;
import gama.dev.DEBUG;
import gaml.compiler.gaml.expression.SkillConstantExpression;
import gaml.compiler.gaml.expression.SpeciesConstantExpression;

/**
 * The Class SpeciesDescription.
 */

/**
 * The Class SpeciesDescription.
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class SpeciesDescription extends TypeDescription implements ISpeciesDescription {

	static {
		DEBUG.OFF();
	}

	/** The behaviors. */
	// AD 08/16: Behaviors are now inherited dynamically
	private IMap<String, StatementDescription> behaviors;

	/** The aspects. */
	// AD 08/16: Aspects are now inherited dynamically
	private IMap<String, StatementDescription> aspects;

	/** The micro species. */
	private IMap<String, ISpeciesDescription> microSpecies;

	/** The skills. */
	protected LinkedHashSet<ISkillDescription> skills;

	/** The control. */
	protected ISkillDescription control;

	/** The agent constructor. */
	private IAgentConstructor agentConstructor;

	/** The species expr. */
	private SpeciesConstantExpression speciesExpr;

	/** The java base. */
	protected Class javaBase;

	/**
	 * Instantiates a new species description.
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
	 */
	public SpeciesDescription(final String keyword, final Class clazz, final ISpeciesDescription macroDesc,
			final ISpeciesDescription parent, final Iterable<? extends IDescription> cp, final EObject source,
			final Facets facets) {
		this(keyword, clazz, macroDesc, parent, cp, source, facets, Collections.emptySet());
	}

	/**
	 * Instantiates a new species description.
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
	 * @param skills
	 *            the skills
	 */
	public SpeciesDescription(final String keyword, final Class clazz, final ISpeciesDescription macroDesc,
			final ISpeciesDescription parent, final Iterable<? extends IDescription> cp, final EObject source,
			final Facets facets, final Set<String> skills) {
		super(keyword, clazz, macroDesc, parent, cp, source, facets, null);
		set(Flag.CanUseMinimalAgents);
		setIf(Flag.isMirror, hasFacet(IKeyword.MIRRORS));
		setIf(Flag.isGrid, IKeyword.GRID.equals(getKeyword()));
		setJavaBase(clazz);
		setSkills(getFacet(IKeyword.SKILLS), skills);
	}

	/**
	 * This constructor is only called to build built-in species. The parent is passed directly as there is no
	 * ModelFactory to provide it
	 */
	public SpeciesDescription(final String name, final Class clazz, final ISpeciesDescription superDesc,
			final ISpeciesDescription parent, final IAgentConstructor helper, final Set<String> skills2,
			final Facets ff, final String plugin) {
		super(IKeyword.SPECIES, clazz, superDesc, null, null, null, new Facets(IKeyword.NAME, name), plugin);
		set(Flag.CanUseMinimalAgents);
		setIf(Flag.isMirror, hasFacet(IKeyword.MIRRORS));
		setIf(Flag.isGrid, IKeyword.GRID.equals(getKeyword()));
		setJavaBase(clazz);
		setParent(parent);
		setSkills(ff == null ? null : ff.get(IKeyword.SKILLS), skills2);
		setAgentConstructor(helper);
	}

	/**
	 * Adds the skill.
	 *
	 * @param sk
	 *            the sk
	 */
	protected void addSkill(final ISkillDescription sk) {
		if (sk == null) return;
		if (skills == null) { skills = new LinkedHashSet<>(); }
		skills.add(sk);
	}

	@Override
	public ISymbolSerializer createSerializer() {
		return SPECIES_SERIALIZER;
	}

	@Override
	public void dispose() {
		if (isBuiltIn()) return;
		super.dispose();
		behaviors = null;
		aspects = null;
		skills = null;
		if (control != null) {
			control.dispose();
			control = null;
		}
		microSpecies = null;

	}

	/**
	 * Sets the skills.
	 *
	 * @param userDefinedSkills
	 *            the user defined skills
	 * @param builtInSkills
	 *            the built in skills
	 */
	protected void setSkills(final IExpressionDescription userDefinedSkills, final Set<String> builtInSkills) {
		/* We try to add the control architecture if any is defined */
		final String controlName = getLitteral(IKeyword.CONTROL);
		if (controlName != null) {
			final ISkillDescription sd = GamaSkillRegistry.INSTANCE.get(controlName);
			if (sd == null || !sd.isControl()) {
				warning("This control  does not belong to the list of known agent controls ("
						+ GamaSkillRegistry.INSTANCE.getArchitectureNames() + ")", IGamlIssue.WRONG_CONTEXT,
						IKeyword.CONTROL);
			} else {
				control = sd;
				// We add it explicitly so as to add the variables and actions
				// defined in the control. No need to add it in the other cases
				// addSkill(control);
			}
		}

		/* We add the keyword as a possible skill (used for 'grid' species) */
		final ISkillDescription skill = GamaSkillRegistry.INSTANCE.get(getKeyword());
		addSkill(skill);
		/*
		 * We add the user defined skills (i.e. as in 'species a skills: [s1, s2...]')
		 */
		if (userDefinedSkills != null) {
			final IExpression expr = userDefinedSkills.compile(this);
			if (expr instanceof IExpression.List list) { addSkills(list); }
		}
		/*
		 * We add the skills that are defined in Java, either using @species(value='a', skills= {s1,s2}),
		 * or @skill(value="s1", attach_to="a")
		 */
		for (final String s : builtInSkills) { addSkill(GamaSkillRegistry.INSTANCE.get(s)); }

	}

	/**
	 * Adds the skills.
	 *
	 * @param list
	 *            the list
	 */
	private void addSkills(final IExpression.List list) {
		for (final IExpression exp : list.getElements()) {
			if (exp instanceof SkillConstantExpression) {
				final ISkillDescription sk = ((ISkill) exp.getConstValue()).getDescription();
				final String dep = sk.getDeprecated();
				if (dep != null) {
					warning("Skill " + sk.getName() + " is deprecated: " + dep, IGamlIssue.DEPRECATED, IKeyword.SKILLS);
				}
				addSkill(sk);
			}
		}
	}

	@Override
	public boolean redefinesAttribute(final String theName) {
		if (super.redefinesAttribute(theName)) return true;
		if (skills != null) {
			for (final ISkillDescription skill : skills) { if (skill.hasAttribute(theName)) return true; }
		}
		return false;
	}

	@Override
	public boolean redefinesAction(final String theName) {
		if (super.redefinesAction(theName)) return true;
		if (skills != null) {
			for (final ISkillDescription skill : skills) { if (skill.hasAction(theName, false)) return true; }
		}
		return false;
	}

	/**
	 * Gets the control name.
	 *
	 * @return the control name
	 */
	public String getControlName() {
		String controlName = getLitteral(IKeyword.CONTROL);
		// if the "control" is not explicitly declared then inherit it from the
		// parent species. Takes care of invalid species (see Issue 711)
		if (controlName == null) {
			if (parent != null && parent != this) {
				controlName = getParent().getControlName();
			} else {
				controlName = IKeyword.REFLEX;
			}
		}
		return controlName;
	}

	/**
	 * Gets the parent name.
	 *
	 * @return the parent name
	 */
	@Override
	public String getParentName() { return getLitteral(IKeyword.PARENT); }

	@Override
	public IExpression getVarExpr(final String n, final boolean asField) {
		IExpression result = super.getVarExpr(n, asField);
		if (result == null) {
			StatementDescription desc = getBehavior(n);
			if (desc != null) { result = GAML.getExpressionFactory().getExpressionDenoting(desc); }
			desc = getAspect(n);
			if (desc != null) { result = GAML.getExpressionFactory().getExpressionDenoting(desc); }
		}
		return result;
	}

	/**
	 * Copy java additions.
	 */
	@Override
	public void copyJavaAdditions() {
		final Class clazz = getJavaBase();
		if (clazz == null) {
			error("This species cannot be compiled as its Java base is unknown. ", IGamlIssue.UNKNOWN_SPECIES);
			return;
		}
		Iterable<Class<? extends ISkill>> skillClasses = transform(getSkills(), TO_CLASS);
		final List<Class> classes =
				JavaUtils.collectImplementationClasses(getJavaBase(), skillClasses, GAML.ADDITIONS.keySet());
		for (final Class c : classes) { for (final IDescription v : GAML.ADDITIONS.get(c)) { addJavaChild(v); } }
	}

	/**
	 * Adds the java child.
	 *
	 * @param v
	 *            the v
	 */
	private void addJavaChild(final IDescription v) {

		if (isBuiltIn()) { v.setOriginName("built-in species " + getName()); }
		if (v instanceof VariableDescription) {
			boolean toAdd = false;
			if (this.isBuiltIn() && !hasAttribute(v.getName()) || ((VariableDescription) v).isContextualType()) {
				toAdd = true;
			} else if (parent != null && parent != this) {
				final IVariableDescription existing = parent.getAttribute(v.getName());
				if (existing == null || !existing.getOriginName().equals(v.getOriginName())) { toAdd = true; }
			} else {
				toAdd = true;
			}
			if (toAdd) {
				// Fixes a problem where built-in attributes were not linked with their declaring class
				// Class<?> c = VariableDescription.CLASS_DEFINITIONS.remove(v);
				final VariableDescription var = (VariableDescription) v.copy(this);
				addOwnAttribute(var);
				// var.builtInDoc = ((VariableDescription) v).getBuiltInDoc();
				// VariableDescription.CLASS_DEFINITIONS.put(var, c);
			}

		} else {
			boolean toAdd = false;
			if (parent == null) {
				toAdd = true;
			} else if (parent != this) {
				final IActionDescription existing = parent.getAction(v.getName());
				if (existing != null && existing.getOriginName() == null) {

					DEBUG.OUT("");

				}
				if (existing == null || !existing.getOriginName().equals(v.getOriginName())) { toAdd = true; }
			}
			if (toAdd) {

				// v.setEnclosingDescription(this);
				addAction((ActionDescription) v);
			}
		}
	}

	@Override
	public IDescription addChild(final IDescription child) {
		final IDescription desc = super.addChild(child);
		if (desc == null) return null;
		if (desc instanceof StatementDescription statement) {
			final String kw = desc.getKeyword();
			if (IKeyword.PRIMITIVE.equals(kw) || IKeyword.ACTION.equals(kw)) {
				addAction((ActionDescription) statement);
			} else if (IKeyword.ASPECT.equals(kw)) {
				addAspect(statement);
			} else {
				addBehavior(statement);
			}
		} else if (desc instanceof VariableDescription) {
			addOwnAttribute((VariableDescription) desc);
		} else if (desc instanceof SpeciesDescription) { addMicroSpecies((SpeciesDescription) desc); }
		return desc;
	}

	/**
	 * Adds the micro species.
	 *
	 * @param sd
	 *            the sd
	 */
	protected void addMicroSpecies(final SpeciesDescription sd) {
		if (!isModel() && sd.isGrid()) {
			sd.error("For the moment, grids cannot be defined as micro-species anywhere else than in the model");
		}
		getMicroSpecies().put(sd.getName(), sd);
		// DEBUG.OUT("Adding micro-species " + sd.getName() + " to " + getName());
		invalidateMinimalAgents();
	}

	/**
	 * Invalidate minimal agents.
	 */
	protected void invalidateMinimalAgents() {
		unSet(Flag.CanUseMinimalAgents);
		if (parent != null && parent != this && !parent.isBuiltIn()) { getParent().invalidateMinimalAgents(); }
	}

	/**
	 * Use minimal agents.
	 *
	 * @return true, if successful
	 */
	protected boolean useMinimalAgents() {
		if (!isSet(Flag.CanUseMinimalAgents) || parent != null && parent != this && !getParent().useMinimalAgents())
			return false;
		return !hasFacet("use_regular_agents") || IKeyword.FALSE.equals(getLitteral("use_regular_agents"));
	}

	/**
	 * Gets or creates the behaviors map.
	 *
	 * @return the behaviors map
	 */
	private IMap<String, StatementDescription> getBehaviorsMap() {
		if (behaviors == null) { behaviors = GamaMapFactory.create(); }
		return behaviors;
	}

	/**
	 * Gets or creates the aspects map.
	 *
	 * @return the aspects map
	 */
	private IMap<String, StatementDescription> getAspectsMap() {
		if (aspects == null) { aspects = GamaMapFactory.create(); }
		return aspects;
	}

	/**
	 * Adds the behavior.
	 *
	 * @param r
	 *            the r
	 */
	protected void addBehavior(final StatementDescription r) {
		final String behaviorName = r.getName();
		final IMap<String, StatementDescription> behaviorMap = getBehaviorsMap();
		final StatementDescription existing = getBehavior(behaviorName);
		if (existing != null && existing.getKeyword().equals(r.getKeyword())) { duplicateInfo(r, existing); }
		behaviorMap.put(behaviorName, r);
	}
	
	/**
	 * Adds the aspect.
	 *
	 * @param ce
	 *            the ce
	 */
	private void addAspect(final StatementDescription ce) {
		String aspectName = ce.getName();
		if (aspectName == null) {
			aspectName = IKeyword.DEFAULT;
			ce.setName(aspectName);
		}
		if (!IKeyword.DEFAULT.equals(aspectName) && hasAspect(aspectName)) { duplicateInfo(ce, getAspect(aspectName)); }
		getAspectsMap().put(aspectName, ce);
	}

	/**
	 * Gets the behavior.
	 *
	 * @param aName
	 *            the a name
	 * @return the behavior
	 */
	@Override
	public StatementDescription getBehavior(final String aName) {
		StatementDescription ownBehavior = behaviors == null ? null : behaviors.get(aName);
		if (ownBehavior == null && parent != null && parent != this) { 
			ownBehavior = getParent().getBehavior(aName); 
		}
		return ownBehavior;
	}

	/**
	 * Checks for behavior.
	 *
	 * @param a
	 *            the a
	 * @return true, if successful
	 */
	@Override
	public boolean hasBehavior(final String a) {
		return behaviors != null && behaviors.containsKey(a)
				|| parent != null && parent != this && getParent().hasBehavior(a);
	}

	/**
	 * Gets the aspect.
	 *
	 * @param aName
	 *            the a name
	 * @return the aspect
	 */
	@Override
	public StatementDescription getAspect(final String aName) {
		StatementDescription ownAspect = aspects == null ? null : aspects.get(aName);
		if (ownAspect == null && parent != null && parent != this) { 
			ownAspect = getParent().getAspect(aName); 
		}
		return ownAspect;
	}

	/**
	 * Gets the behavior names.
	 *
	 * @return the behavior names
	 */
	public Collection<String> getBehaviorNames() {
		final Collection<String> ownNames =
				behaviors == null ? new LinkedHashSet<>() : new LinkedHashSet<>(behaviors.keySet());
		if (parent != null && parent != this) { 
			ownNames.addAll(getParent().getBehaviorNames()); 
		}
		return ownNames;
	}

	/**
	 * Gets the aspect names.
	 *
	 * @return the aspect names
	 */
	public Collection<String> getAspectNames() {
		final Collection<String> ownNames = aspects == null ? new LinkedHashSet<>() : new LinkedHashSet<>(aspects.keySet());
		if (parent != null && parent != this) { 
			ownNames.addAll(getParent().getAspectNames()); 
		}
		return ownNames;

	}

	/**
	 * Gets the aspects.
	 *
	 * @return the aspects
	 */
	@Override
	public Iterable<IStatementDescription> getAspects() {
		return getAspectNames().stream().map(this::getAspect).collect(java.util.stream.Collectors.toList());
	}

	/**
	 * Gets the control.
	 *
	 * @return the control
	 */
	@Override
	public ISkillDescription getControl() { return control; }

	/**
	 * Checks for aspect.
	 *
	 * @param a
	 *            the a
	 * @return true, if successful
	 */
	@Override
	public boolean hasAspect(final String a) {
		return aspects != null && aspects.containsKey(a)
				|| parent != null && parent != this && getParent().hasAspect(a);
	}

	@Override
	public ISpeciesDescription getSpeciesContext() { return this; }

	/**
	 * Gets the micro species.
	 *
	 * @param name
	 *            the name
	 * @return the micro species
	 */
	@Override
	public ISpeciesDescription getMicroSpecies(final String name) {
		if (hasMicroSpecies()) {
			final ISpeciesDescription retVal = microSpecies.get(name);
			if (retVal != null) return retVal;
		}
		// Takes care of invalid species (see Issue 711)
		if (parent != null && parent != this) return getParent().getMicroSpecies(name);
		return null;
	}

	@Override
	public String toString() {
		return "Description of " + getName();
	}

	/**
	 * Gets the agent constructor.
	 *
	 * @return the agent constructor
	 */
	@Override
	public IAgentConstructor getAgentConstructor() {
		if (agentConstructor == null && parent != null && parent != this) {
			final Class<?> parentJavaBase = getParent().getJavaBase();
			final Class<?> myJavaBase = getJavaBase();
			if (parentJavaBase == myJavaBase) {
				agentConstructor = getParent().getAgentConstructor();
			} else {
				agentConstructor = AgentConstructorsRegistry.CONSTRUCTORS.get(myJavaBase);
			}
		}
		return agentConstructor;
	}

	/**
	 * Sets the agent constructor.
	 *
	 * @param agentConstructor
	 *            the new agent constructor
	 */
	protected void setAgentConstructor(final IAgentConstructor agentConstructor) {
		this.agentConstructor = agentConstructor;
	}

	/**
	 * Gets the macro species.
	 *
	 * @return the macro species
	 */
	@Override
	public SpeciesDescription getMacroSpecies() {
		final IDescription d = getEnclosingDescription();
		if (d instanceof SpeciesDescription) return (SpeciesDescription) d;
		return null;
	}

	@Override
	public SpeciesDescription getParent() { return (SpeciesDescription) super.getParent(); }

	/**
	 * Verify java base.
	 *
	 * @param sd
	 *            the sd
	 * @return true, if successful
	 */
	private boolean verifyJavaBase(final SpeciesDescription sd) {
		if (sd.getJavaBase() == null) {
			error("Species " + sd.getName() + " Java base class can not be found. No validation is possible.",
					IGamlIssue.GENERAL);
			return false;
		}
		return true;
	}

	@Override
	public void inheritFromParent() {
		final SpeciesDescription parentSpecies = getParent();
		// Takes care of invalid species (see Issue 711)
		// built-in parents are not considered as their actions/variables are
		// normally already copied as java additions
		if (parentSpecies == null || !verifyJavaBase(parentSpecies) || !verifyJavaBase(this)) return;
		tryInheritMicroSpecies(parentSpecies);
		super.inheritFromParent();
	}

	/**
	 * Try inherit micro species.
	 *
	 * @param parentSpecies
	 *            the parent species
	 */
	private void tryInheritMicroSpecies(final SpeciesDescription parentSpecies) {
		// Takes care of invalid species (see Issue 711)
		if (parentSpecies == null || parentSpecies == this) return;
		if (!parentSpecies.isBuiltIn()) {
			if (!parentSpecies.getJavaBase().isAssignableFrom(getJavaBase())) {
				error("Species " + getName() + " Java base class (" + getJavaBase().getSimpleName()
						+ ") is not a subclass of its parent species " + parentSpecies.getName() + " base class ("
						+ parentSpecies.getJavaBase().getSimpleName() + ")", IGamlIssue.GENERAL);
			}
			inheritMicroSpecies(parentSpecies);
		} else if (!isBuiltIn() && isModel()) { inheritMicroSpecies(parentSpecies); }
	}

	/**
	 * Inherit micro species.
	 *
	 * @param parentSpecies
	 *            the parent
	 */
	private void inheritMicroSpecies(final SpeciesDescription parentSpecies) {
		if (parentSpecies.hasMicroSpecies()) {
			parentSpecies.getMicroSpecies().forEachPair((k, v) -> {
				getMicroSpecies().putIfAbsent(k, v);
				return true;
			});
		}
	}

	/**
	 * Checks if is grid.
	 *
	 * @return true, if is grid
	 */
	@Override
	public boolean isGrid() { return isSet(Flag.isGrid); }

	@Override
	public String getTitle() { return StringUtils.capitalize(getKeyword()) + " " + getName(); }

	@Override
	public IGamlDocumentation getDocumentation() {
		final IGamlDocumentation result = new GamlRegularDocumentation();
		documentThis(result);
		result.append("<hr/>").append(getMeta().getDocumentation().toString());
		return result;
	}

	/**
	 * Gets the documentation without meta.
	 *
	 * @return the documentation without meta
	 */
	@Override
	public void documentThis(final IGamlDocumentation sb) {
		final String parentName = getParent() == null ? "nil" : getParent().getName();
		final String hostName = getMacroSpecies() == null ? null : getMacroSpecies().getName();
		sb.append("<b>Subspecies of:</b> ").append(parentName).append("<br>");
		if (hostName != null) { sb.append("<b>Microspecies of:</b> ").append(hostName).append("<br>"); }
		final Iterable<String> skills = getSkillsNames();
		if (!Iterables.isEmpty(skills)) { sb.append("<b>Skills:</b> ").append(skills.toString()).append("<br>"); }
		documentAttributes(sb);
		documentActions(sb);
	}

	/**
	 * Gets the skills names.
	 *
	 * @return the skills names
	 */
	@Override
	public Iterable<String> getSkillsNames() {
		return Iterables.concat(Iterables.transform(skills == null ? Collections.EMPTY_LIST : skills, TO_NAME),
				parent != null && parent != this ? getParent().getSkillsNames() : Collections.EMPTY_LIST);

	}

	/**
	 * Returns the constant expression representing this species
	 */
	@Override
	public IExpression getSpeciesExpr() {
		if (speciesExpr == null) {
			final IType type = GamaType.from(SpeciesDescription.this);
			speciesExpr = (SpeciesConstantExpression) GAML.getExpressionFactory().createSpeciesConstant(type);
		}
		return speciesExpr;
	}

	/**
	 * Visit micro species.
	 *
	 * @param visitor
	 *            the visitor
	 * @return true, if successful
	 */
	@Override
	public boolean visitMicroSpecies(final DescriptionVisitor<ISpeciesDescription> visitor) {
		if (!hasMicroSpecies()) return true;
		return getMicroSpecies().forEachValue(visitor);
	}

	// public boolean visitSortedMicroSpecies(final DescriptionVisitor<SpeciesDescription> visitor) {
	// if (!hasMicroSpecies()) { return true; }
	// final Iterable<SpeciesDescription> all = getSortedMicroSpecies();
	// for (final SpeciesDescription sd : all) {
	// if (!visitor.process(sd)) { return false; }
	// }
	// return true;
	// }

	/**
	 * Sets the parent.
	 *
	 * @param parent
	 *            the new parent
	 */
	@Override
	public void setParent(final ITypeDescription parent) {
		super.setParent(parent);
		if (!isBuiltIn() && !verifyParent()) {
			super.setParent(null);
			return;
		}
		if (parent instanceof SpeciesDescription && parent != this && !isSet(Flag.CanUseMinimalAgents)
				&& !parent.isBuiltIn()) {
			((SpeciesDescription) parent).invalidateMinimalAgents();
		}
	}

	/**
	 * Verifies if the specified species can be a parent of this species.
	 *
	 * A species can be parent of other if the following conditions are hold 1. A parent species is visible to the
	 * sub-species. 2. A species can' be a sub-species of itself. 3. 2 species can't be parent of each other. 5. A
	 * species can't be a sub-species of its direct/in-direct micro-species. 6. A species and its direct/indirect
	 * micro/macro-species can't share one/some direct/indirect parent-species having micro-species. 7. The inheritance
	 * between species from different branches doesn't form a "circular" inheritance.
	 *
	 * @param parentName
	 *            the name of the potential parent
	 * @throws GamlException
	 *             if the species with the specified name can not be a parent of this species.
	 */
	protected boolean verifyParent() {
		if (parent == null) return true;
		if (this == parent) {
			error(getName() + " species can't be a sub-species of itself", IGamlIssue.GENERAL);
			return false;
		}
		if (parentIsAmongTheMicroSpecies()) {
			error(getName() + " species can't be a sub-species of one of its micro-species", IGamlIssue.GENERAL);
			return false;
		}
		if (!parentIsVisible()) {
			error(parent.getName() + " can't be a parent species of " + this.getName() + " species.",
					IGamlIssue.WRONG_PARENT, IKeyword.PARENT);
			return false;
		}
		if (hierarchyContainsSelf()) {
			error(this.getName() + " species and " + parent.getName() + " species can't be sub-species of each other.");
			return false;
		}
		return true;
	}

	/**
	 * Parent is among the micro species.
	 *
	 * @return true, if successful
	 */
	private boolean parentIsAmongTheMicroSpecies() {
		final java.util.concurrent.atomic.AtomicBoolean result = new java.util.concurrent.atomic.AtomicBoolean(false);
		visitMicroSpecies(new DescriptionVisitor<ISpeciesDescription>() {

			@Override
			public boolean process(final ISpeciesDescription desc) {
				if (desc == parent) {
					result.set(true);
					return false;
				}
				desc.visitMicroSpecies(this);
				return true;
			}
		});
		return result.get();
	}

	/**
	 * Hierarchy contains self.
	 *
	 * @return true, if successful
	 */
	private boolean hierarchyContainsSelf() {
		SpeciesDescription currentSpeciesDesc = this;
		while (currentSpeciesDesc != null) {
			final SpeciesDescription p = currentSpeciesDesc.getParent();
			// Takes care of invalid species (see Issue 711)
			if (p == currentSpeciesDesc || p == this) return true;
			currentSpeciesDesc = p;
		}
		return false;
	}

	/**
	 * Parent is visible.
	 *
	 * @return true, if successful
	 */
	protected boolean parentIsVisible() {
		if (getParent().isExperiment()) return false;
		SpeciesDescription host = getMacroSpecies();
		while (host != null) {
			if (host == parent || host.getMicroSpecies(parent.getName()) != null) return true;
			host = host.getMacroSpecies();
		}
		return false;
	}

	/**
	 * Finalizes the species description + Copy the behaviors, attributes from parent; + Creates the control if
	 * necessary. Add a variable representing the population of each micro-species
	 *
	 * @throws GamlException
	 */
	@Override
	public boolean finalizeDescription() {
		if (isMirror()) {
			addChild(GAML.getDescriptionFactory().create(IKeyword.AGENT, this, IKeyword.NAME, IKeyword.TARGET,
					IKeyword.TYPE, String.valueOf(ITypeProvider.MIRROR_TYPE)));
		}

		// Add the control if it is not already added
		finalizeControl();
		final boolean isBuiltIn = this.isBuiltIn();

		final DescriptionVisitor<ISpeciesDescription> visitor = microSpec -> {
			if (!microSpec.finalizeDescription()) return false;
			if (!microSpec.isExperiment() && !isBuiltIn) {
				final String n = microSpec.getName();
				if (hasAttribute(n) && !getAttribute(n).isSyntheticSpeciesContainer()) {
					microSpec.error(
							microSpec.getName() + " is the name of an existing attribute in " + SpeciesDescription.this,
							IGamlIssue.DUPLICATE_NAME, IKeyword.NAME);
					return false;
				}
				final VariableDescription var = (VariableDescription) GAML.getDescriptionFactory().create(IKeyword.LIST,
						SpeciesDescription.this, IKeyword.NAME, n);

				var.setSyntheticSpeciesContainer();
				var.setFacet(IKeyword.OF, GAML.getExpressionFactory()
						.createTypeExpression(getModelDescription().getTypeNamed(microSpec.getName())));
				final IGamaHelper get = (scope1, agent1, skill1, values1) -> ((IMacroAgent) agent1)
						.getMicroPopulation(microSpec.getName());
				final IGamaHelper set = (scope2, agent2, skill2, values2) -> null;
				final IGamaHelper init = (scope3, agent3, skill3, values3) -> {
					((IMacroAgent) agent3).initializeMicroPopulation(scope3, microSpec.getName());
					return ((IMacroAgent) agent3).getMicroPopulation(microSpec.getName());
				};

				var.addHelpers(get, init, set);
				addChild(var);
			}
			return true;
		};

		// recursively finalize the sorted micro-species
		if (!visitMicroSpecies(visitor)) return false;
		// Calling sortAttributes later (in compilation)
		// add the listeners to the variables (if any)
		// addListenersToVariables();
		return true;
	}

	/**
	 *
	 */
	private void finalizeControl() {
		if (isSet(Flag.ControlFinalized)) return;
		set(Flag.ControlFinalized);

		if (control == null && parent != this && parent instanceof SpeciesDescription) {
			((SpeciesDescription) parent).finalizeControl();
			control = ((SpeciesDescription) parent).getControl();
		}
		if (control == null) {
			control = GamaSkillRegistry.INSTANCE.get(IKeyword.REFLEX);
			return;
		}
		Class<? extends ISkill> clazz = control.getJavaBase().getSuperclass();
		while (!Modifier.isAbstract(clazz.getModifiers())) {
			final ISkillDescription sk = GamaSkillRegistry.INSTANCE.get(clazz);
			if (sk != null) { addSkill(sk); }
			clazz = (Class<? extends ISkill>) clazz.getSuperclass();

		}

	}

	@Override
	protected boolean validateChildren() {
		// We try to issue information about the state of the species: at first,
		// abstract.

		for (final IActionDescription a : getActions()) {
			if (a.isAbstract()) {
				this.info("Action '" + a.getName() + "' is defined or inherited as virtual. In consequence, "
						+ getName() + " will be considered as abstract.", IGamlIssue.MISSING_ACTION);
			}
		}

		return super.validateChildren();
	}

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
	 * Checks for micro species.
	 *
	 * @return true, if successful
	 */
	@Override
	public boolean hasMicroSpecies() {
		return microSpecies != null;
	}

	/**
	 * Gets the micro species.
	 *
	 * @return the micro species
	 */
	@Override
	public IMap<String, ISpeciesDescription> getMicroSpecies() {
		if (microSpecies == null) { microSpecies = GamaMapFactory.create(); }
		return microSpecies;
	}

	/**
	 * Checks if is mirror.
	 *
	 * @return true, if is mirror
	 */
	@Override
	public boolean isMirror() { return isSet(Flag.isMirror); }

	/**
	 * Returns whether or not a species implements (directly or indirectly through its parents) a skill named after the
	 * parameter.
	 *
	 * @param skill
	 *            the name of the skill
	 * @return true if this species implements a skill or if its parent does. WARNING: no possibility, right now, to
	 *         know if a skill extends another skill, so this possibility is not considered in this method.
	 */
	@Override
	public Boolean implementsSkill(final String skill) {
		if (skills != null) {
			for (final ISkillDescription sk : skills) { if (sk.getName().equals(skill)) return true; }
		}
		if (parent != null && parent != this) return getParent().implementsSkill(skill);
		return false;
	}

	@Override
	public Class<? extends IAgent> getJavaBase() {
		if (javaBase == null) {
			if (parent != null && parent != this && !IKeyword.AGENT.equals(getParent().getName())) {
				javaBase = getParent().getJavaBase();
				// If the parent is a minimal agent, we need to use the minimal version
			} else {
				javaBase = AgentConstructorsRegistry.getBaseClass(isGrid(), useMinimalAgents());
			}
		}
		if (javaBase == null) {

			DEBUG.OUT("");
			javaBase = AgentConstructorsRegistry.getBaseClass(isGrid(), useMinimalAgents());

		}
		return javaBase;
	}

	/**
	 * Sets the java base.
	 *
	 * @param javaBase
	 *            the new java base
	 */
	protected void setJavaBase(final Class javaBase) { this.javaBase = javaBase; }

	/**
	 * @param found_sd
	 * @return
	 */
	@Override
	public boolean hasMacroSpecies(final ISpeciesDescription found_sd) {
		final ISpeciesDescription sd = getMacroSpecies();
		if (sd == null) return false;
		if (sd.equals(found_sd)) return true;
		return sd.hasMacroSpecies(found_sd);
	}

	/**
	 * @param macro
	 * @return
	 */
	@Override
	public boolean hasParent(final ISpeciesDescription p) {
		final SpeciesDescription sd = getParent();
		// Takes care of invalid species (see Issue 711)
		if (sd == null || sd == this) return false;
		if (sd.equals(p)) return true;
		return sd.hasParent(p);
	}

	@Override
	public boolean visitOwnChildren(final DescriptionVisitor<IDescription> visitor) {
		if (!super.visitOwnChildren(visitor) || microSpecies != null && !microSpecies.forEachValue(visitor))
			return false;
		if (behaviors != null && !behaviors.forEachValue(visitor) || aspects != null && !aspects.forEachValue(visitor))
			return false;
		return true;
	}

	@Override
	public boolean visitOwnChildrenRecursively(final DescriptionVisitor<IDescription> visitor) {
		final DescriptionVisitor<IDescription> recursiveVisitor = each -> {
			if (!visitor.process(each)) return false;
			return each.visitOwnChildrenRecursively(visitor);
		};
		if (!super.visitOwnChildrenRecursively(visitor)
				|| microSpecies != null && !microSpecies.forEachValue(recursiveVisitor))
			return false;
		if (behaviors != null && !behaviors.forEachValue(recursiveVisitor)
				|| aspects != null && !aspects.forEachValue(recursiveVisitor))
			return false;
		return true;
	}

	@Override
	public Iterable<IDescription> getOwnChildren() {
		return Iterables.concat(super.getOwnChildren(),
				microSpecies == null ? Collections.emptyList() : microSpecies.values(),
				behaviors == null ? Collections.emptyList() : behaviors.values(),
				aspects == null ? Collections.emptyList() : aspects.values());
	}

	@Override
	public boolean visitChildren(final DescriptionVisitor<IDescription> visitor) {
		boolean result = super.visitChildren(visitor);
		if (!result) return false;
		if (hasMicroSpecies()) { result &= microSpecies.forEachValue(visitor); }
		if (!result) return false;
		for (final IDescription d : getBehaviors()) {
			result &= visitor.process(d);
			if (!result) return false;
		}
		for (final IDescription d : getAspects()) {
			result &= visitor.process(d);
			if (!result) return false;
		}
		return result;
	}

	/**
	 * @return
	 */
	@Override
	public Iterable<IStatementDescription> getBehaviors() {
		return getBehaviorNames().stream().map(this::getBehavior).collect(java.util.stream.Collectors.toList());
	}

	@Override
	public void collectMetaInformation(final GamlProperties meta) {
		super.collectMetaInformation(meta);
		if (isBuiltIn()) { meta.put(GamlProperties.SPECIES, getName()); }
	}

	/**
	 * Belongs to A micro model.
	 *
	 * @return true, if successful
	 */
	@Override
	public boolean belongsToAMicroModel() {
		return getModelDescription().isMicroModel();
	}

	/**
	 * Gets the skills.
	 *
	 * @return the skills
	 */
	@Override
	public Iterable<ISkillDescription> getSkills() {
		final List<ISkillDescription> base =
				control == null ? Collections.emptyList() : Collections.singletonList(control);
		if (skills == null) return base;
		return Iterables.concat(skills, base);
	}

}
