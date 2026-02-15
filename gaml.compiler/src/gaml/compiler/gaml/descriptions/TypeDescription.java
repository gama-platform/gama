/*******************************************************************************************************
 *
 * TypeDescription.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.descriptions;

import static gama.api.compilation.descriptions.IVariableDescription.FUNCTION_DEPENDENCIES_FACETS;
import static gama.api.compilation.descriptions.IVariableDescription.INIT_DEPENDENCIES_FACETS;
import static gama.api.constants.IKeyword.FUNCTION;
import static gama.api.constants.IKeyword.INIT;
import static gama.api.constants.IKeyword.NAME;
import static gama.api.constants.IKeyword.SHAPE;
import static gama.api.constants.IKeyword.TRUE;
import static gama.api.constants.IKeyword.VIRTUAL;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Objects;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.jgrapht.graph.DirectedAcyclicGraph;

import com.google.common.collect.Iterables;

import gama.api.compilation.descriptions.IActionDescription;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.ITypeDescription;
import gama.api.compilation.descriptions.IVarDescriptionProvider;
import gama.api.compilation.descriptions.IVariableDescription;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.constants.IGamlIssue;
import gama.api.constants.IKeyword;
import gama.api.data.factories.GamaMapFactory;
import gama.api.data.objects.IMap;
import gama.api.gaml.GAML;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.symbols.Facets;
import gama.api.gaml.types.IType;
import gama.dev.DEBUG;

/**
 * A class that represents skills and species (either built-in or introduced by users) The class TypeDescription.
 *
 * @author drogoul
 * @since 23 fevr. 2013
 *
 */

@SuppressWarnings ({ "unchecked", "rawtypes" })
public abstract class TypeDescription extends SymbolDescription implements ITypeDescription {

	static {
		DEBUG.OFF();
	}

	/**
	 * The Constant TO_CLASS.
	 */

	// AD 08/16 : actions and attributes are now inherited dynamically and built
	/** The actions. */
	// lazily
	protected IMap<String, IActionDescription> actions;

	/** The attributes. */
	protected IMap<String, IVariableDescription> attributes;

	/** The parent. */
	protected ITypeDescription parent;

	/** The is abstract. */
	// protected final boolean isAbstract;

	/**
	 * Instantiates a new type description.
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
	public TypeDescription(final String keyword, final Class clazz, final IDescription macroDesc,
			final ITypeDescription parent, final Iterable<? extends IDescription> cp, final EObject source,
			final Facets facets, final String plugin) {
		super(keyword, macroDesc, source, /* cp, */ facets);
		setIf(Flag.Abstract, TRUE.equals(getLitteral(VIRTUAL)));
		addChildren(cp);
		for (IActionDescription ad : getActions())
			if (ad.isAbstract()) {
				setIf(Flag.Abstract, true);
				break;
			}
		// parent can be null
		if (parent != null) { setParent(parent); }
		if (plugin != null && isBuiltIn()) {
			this.originName = plugin;
			// DEBUG.LOG("Origin name " + getOriginName() + " and plugin "
			// + plugin + " of " + this);
		}

	}

	/**
	 * Documents attributes.
	 *
	 * @param result
	 *            the result
	 */
	@Override
	public void documentAttributes(final IGamlDocumentation result) {
		for (final IVariableDescription f : getAttributes()) {
			result.set("Attributes:", f.getName(), f.getShortDocumentation());
		}
	}

	/**
	 * Documents actions.
	 *
	 * @param result
	 *            the result
	 */
	public void documentActions(final IGamlDocumentation result) {
		for (final IActionDescription f : getActions()) {
			result.set("Actions:", f.getName(), f.getShortDocumentation(true));
		}
	}

	@Override
	public String getDefiningPlugin() {
		if (isBuiltIn()) return originName;
		return null;
	}

	/**
	 * Gets the java base.
	 *
	 * @return the java base
	 */
	@Override
	public abstract Class getJavaBase();

	/**
	 * ==================================== MANAGEMENT OF ATTRIBUTES
	 */

	@Override
	public Iterable<IVariableDescription> getAttributes() {
		return Iterables.transform(getAttributeNames(), this::getAttribute);
	}

	/**
	 * Gets the own attributes.
	 *
	 * @return the own attributes
	 */
	@Override
	public IMap<String, IVariableDescription> getOwnAttributes() {
		return attributes == null ? GamaMapFactory.EMPTY : attributes;
	}

	/**
	 * Gets the attributes map.
	 *
	 * @return the attributes map
	 */
	public IMap<String, IVariableDescription> getAttributesMap() {
		if (attributes == null) { attributes = GamaMapFactory.create(); }
		return attributes;
	}

	/**
	 * Gets the attribute names.
	 *
	 * @return the attribute names
	 */
	@Override
	public Collection<String> getAttributeNames() {
		final Collection<String> accumulator =
				parent != null && parent != this ? parent.getAttributeNames() : new LinkedHashSet<>();
		getOwnAttributes().forEachKey(s -> {
			if (accumulator.contains(s)) { accumulator.remove(s); }
			accumulator.add(s);
			return true;
		});
		return accumulator;
	}

	/**
	 * Gets the attribute.
	 *
	 * @param vn
	 *            the vn
	 * @return the attribute
	 */
	@Override
	public IVariableDescription getAttribute(final String vn) {
		final IVariableDescription attribute = getOwnAttributes().get(vn);
		if (attribute == null && parent != null && parent != this) return parent.getAttribute(vn);
		return attribute;
	}

	/**
	 * Redefines attribute.
	 *
	 * @param vn
	 *            the vn
	 * @return true, if successful
	 */
	public boolean redefinesAttribute(final String vn) {
		return getOwnAttributes().containsKey(vn) && parent != null && parent != this && parent.hasAttribute(vn);
	}

	@Override
	public boolean hasAttribute(final String a) {
		return getOwnAttributes().containsKey(a) || parent != null && parent != this && parent.hasAttribute(a);
	}

	@Override
	public IVarDescriptionProvider getDescriptionDeclaringVar(final String aName) {
		IDescription enc = getEnclosingDescription();
		return hasAttribute(aName) ? this : enc == null ? null : enc.getDescriptionDeclaringVar(aName);
	}

	@Override
	public IExpression getVarExpr(final String n, final boolean asField) {
		final IVariableDescription vd = getAttribute(n);
		if (vd == null) {
			final IActionDescription desc = getAction(n);
			if (desc != null) return GAML.getExpressionFactory().getExpressionDenoting(desc);
			return null;
		}
		return vd.getVarExpr(asField);
	}

	/**
	 * Assert attributes are compatible.
	 *
	 * @param existingVar
	 *            the existing var
	 * @param newVar
	 *            the new var
	 * @return true, if successful
	 */
	public boolean assertAttributesAreCompatible(final IVariableDescription existingVar,
			final IVariableDescription newVar) {
		if (newVar.isBuiltIn() && existingVar.isBuiltIn()) return true;
		final IType existingType = existingVar.getGamlType();
		final IType newType = newVar.getGamlType();
		if (!newType.isTranslatableInto(existingType)) {
			markTypeDifference(existingVar, newVar, existingType, newType, true);
		} else if (!newType.equals(existingType) && !newType.isParametricFormOf(existingType)) {
			markTypeDifference(existingVar, newVar, existingType, newType, false);
		}
		return true;
	}

	/**
	 * Mark type difference.
	 *
	 * @param existingVar
	 *            the existing var
	 * @param newVar
	 *            the new var
	 * @param existingType
	 *            the existing type
	 * @param newType
	 *            the new type
	 * @param error
	 *            the error
	 */
	private void markTypeDifference(final IVariableDescription existingVar, final IVariableDescription newVar,
			final IType existingType, final IType newType, final boolean error) {
		final String msg = "Type (" + newType + ") differs from that (" + existingType + ") of the implementation of  "
				+ newVar.getName() + " in " + existingVar.getOriginName();
		if (existingVar.isBuiltIn()) {
			if (error) {
				newVar.error(msg, IGamlIssue.WRONG_REDEFINITION, NAME);
			} else {
				newVar.warning(msg, IGamlIssue.WRONG_REDEFINITION, NAME);
			}
		} else {
			final EObject newObject = newVar.getUnderlyingElement();
			final Resource newResource = newObject == null ? null : newObject.eResource();
			final EObject existingObject = existingVar.getUnderlyingElement();
			final Resource existingResource = existingObject == null ? null : existingObject.eResource();
			final boolean same = newResource == null ? existingResource == null : newResource.equals(existingResource);
			if (same) {
				if (error) {
					newVar.error(msg, IGamlIssue.WRONG_REDEFINITION, NAME);
				} else {
					newVar.info(msg, IGamlIssue.WRONG_REDEFINITION, NAME);
				}
			} else if (existingResource != null) {
				if (error) {
					newVar.error(msg + " in  imported file " + existingResource.getURI().lastSegment(),
							IGamlIssue.WRONG_REDEFINITION, NAME);
				} else {
					newVar.info(msg + " in  imported file " + existingResource.getURI().lastSegment(),
							IGamlIssue.WRONG_REDEFINITION, NAME);
				}
			}
		}

	}

	/**
	 * Mark attribute redefinition.
	 *
	 * @param existingVar
	 *            the existing var
	 * @param newVar
	 *            the new var
	 */
	public void markAttributeRedefinition(final IVariableDescription existingVar, final IVariableDescription newVar) {
		if (newVar.isBuiltIn() && existingVar.isBuiltIn()) return;
		if (newVar.getOriginName().equals(existingVar.getOriginName())) {
			// TODO must be review carefully the inheritance in comodel
			/// temporay fix for co-model, variable in micro-model can be
			// defined multi time
			if (!"".equals(newVar.getModelDescription().getAlias())) return;

			existingVar.error("Attribute " + newVar.getName() + " is defined twice", IGamlIssue.DUPLICATE_DEFINITION,
					NAME);
			newVar.error("Attribute " + newVar.getName() + " is defined twice", IGamlIssue.DUPLICATE_DEFINITION, NAME);
			return;
		}
		if (existingVar.isBuiltIn()) {
			newVar.info(
					"This definition of " + newVar.getName() + " supersedes the one in " + existingVar.getOriginName(),
					IGamlIssue.REDEFINES, NAME);
		} else {
			// Possibily different resources
			final Resource newResource =
					newVar.getUnderlyingElement() == null ? null : newVar.getUnderlyingElement().eResource();
			final Resource existingResource = existingVar.getUnderlyingElement().eResource();
			if (Objects.equals(newResource, existingResource)) {
				newVar.info("This definition of " + newVar.getName() + " supersedes the one in "
						+ existingVar.getOriginName(), IGamlIssue.REDEFINES, NAME);
			} else {
				newVar.info("This definition of " + newVar.getName() + " supersedes the one in imported file "
						+ existingResource.getURI().lastSegment(), IGamlIssue.REDEFINES, NAME);
			}
		}
	}

	/**
	 * Inherit attributes from.
	 *
	 * @param parent2
	 *            the p
	 */
	protected void inheritAttributesFrom(final ITypeDescription parent2) {
		for (final IVariableDescription v : parent2.getAttributes()) { addInheritedAttribute(v); }
	}

	/**
	 * Adds the own attribute.
	 *
	 * @param vd
	 *            the vd
	 */
	public void addOwnAttribute(final IVariableDescription vd) {
		final String newVarName = vd.getName();
		final IVariableDescription existing = getAttribute(newVarName);

		if (existing != null) {
			// A previous definition has been found
			// We assert whether their types are compatible or not
			if (!assertAttributesAreCompatible(existing, vd)) return;
			markAttributeRedefinition(existing, vd);
			vd.copyFrom(existing);
		}

		getAttributesMap().put(vd.getName(), vd);
	}

	/**
	 * Adds the inherited attribute.
	 *
	 * @param vd
	 *            the vd
	 */
	public void addInheritedAttribute(final IVariableDescription vd) {
		// We dont inherit from previously added variables, as a child and its
		// parent should share the same javaBase

		final String inheritedVarName = vd.getName();

		final IVariableDescription existing = getOwnAttributes().get(inheritedVarName);
		if (existing != null && assertAttributesAreCompatible(vd, existing)) {
			if (!existing.isBuiltIn()) { markAttributeRedefinition(vd, existing); }
			existing.copyFrom(vd);
		}
	}

	/**
	 * Tries to create a new edge of dependency between two variables in the passed graph. If this raises an exception
	 * (meaning a cycle is being introduced by the addition of a variable), then emits an error on the first
	 * non-built-in variable involved in the cycle
	 *
	 * @return true if the addition has been done, false otherwise
	 */
	private boolean add(final DirectedAcyclicGraph<IVariableDescription, Object> graph,
			final IVariableDescription source, final IVariableDescription target, final String type) {
		graph.addVertex(source);
		graph.addVertex(target);
		try {
			graph.addEdge(source, target);
		} catch (IllegalArgumentException e) {
			// Thrown if a cycle is introduced
			IVariableDescription errored;
			if (source.isBuiltIn() || source.isSyntheticSpeciesContainer()) {
				if (target.isBuiltIn() || target.isSyntheticSpeciesContainer()) return false;
				errored = target;
			} else {
				errored = source;
			}
			String vName = errored.getName();
			String oName = (errored == source ? target : source).getName();
			errored.error(
					"Cycle detected in the " + type + " of " + vName + " (through the " + type + " of " + oName + ")");
			return false;
		}
		return true;
	}

	/**
	 * Verification is done through the construction of a directed acyclic graph that gathers all the variables and
	 * their dependencies. As soon as a cycle is introduced, an error is raised and this method also returns false
	 *
	 * @return true if the verification is correct, false otherwise
	 */
	protected boolean verifyAttributeCycles() {
		if (getOwnAttributes().size() <= 1) return true;
		final IVariableDescription shape = getOwnAttributes().get(SHAPE);
		final DirectedAcyclicGraph<IVariableDescription, Object> graph = new DirectedAcyclicGraph<>(Object.class);
		final Collection<IVariableDescription> shapeDeps =
				shape == null ? Collections.EMPTY_SET : shape.getDependencies(INIT_DEPENDENCIES_FACETS, false, true);
		Collection<IVariableDescription> varSet = getOwnAttributes().values();
		for (IVariableDescription var : varSet) {
			if (shape != null && var.isSyntheticSpeciesContainer() && !add(graph, shape, var, INIT)) return false;
			final Collection<IVariableDescription> varDeps =
					var == shape ? shapeDeps : var.getDependencies(INIT_DEPENDENCIES_FACETS, false, true);
			for (final IVariableDescription newVar : varDeps) {
				if (varSet.contains(newVar) && !add(graph, newVar, var, INIT)) return false;
			}
			if (var.hasFacet(FUNCTION)) {
				graph.removeAllVertices(varSet);
				for (final IVariableDescription newVar : var.getDependencies(FUNCTION_DEPENDENCIES_FACETS, true,
						false)) {
					if (varSet.contains(newVar) && !add(graph, newVar, var, FUNCTION)) return false;
				}
			}
		}

		return true;
	}

	/**
	 * Sets the parent.
	 *
	 * @param parent
	 *            the new parent
	 */
	@Override
	public void setParent(final ITypeDescription parent) { this.parent = parent; }

	/**
	 * Duplicate info.
	 *
	 * @param one
	 *            the one
	 * @param two
	 *            the two
	 */
	protected void duplicateInfo(final IDescription one, final IDescription two) {
		final String aName = one.getName();
		final String key = one.getKeyword();
		if (!one.getOriginName().equals(two.getOriginName())) {
			if (IKeyword.REFLEX.equals(key)) {
				one.info(
						"The order in which reflex " + aName + " will be executed in " + one.getOriginName()
								+ " can differ from the order defined in " + two.getOriginName(),
						IGamlIssue.GENERAL, NAME, aName);
			}
			one.info("This definition of " + key + " " + aName + " supersedes the one existing in "
					+ two.getOriginName(), IGamlIssue.DUPLICATE_DEFINITION, NAME, aName);
		} else {
			one.info("This definition of " + key + " " + aName + " supersedes the previous one(s) in the same species",
					IGamlIssue.DUPLICATE_DEFINITION, NAME, aName);

		}
	}

	/**
	 * Adds the action.
	 *
	 * @param newAction
	 *            the new action
	 */
	protected void addAction(final ActionDescription newAction) {
		final String actionName = newAction.getName();
		final IActionDescription existing = getOwnActions().get(actionName);
		if (existing != null) {
			assertActionsAreCompatible(newAction, existing, existing.getOriginName());
			duplicateInfo(newAction, existing);
		}
		getActionsMap().put(actionName, newAction);
	}

	/**
	 * Redefines action.
	 *
	 * @param theName
	 *            the the name
	 * @return true, if successful
	 */
	public boolean redefinesAction(final String theName) {
		return getOwnActions().containsKey(theName)
				|| parent != null && parent == this && parent.hasAction(theName, false);
	}

	@Override
	public IActionDescription getAction(final String aName) {
		IActionDescription ownAction = getOwnActions().get(aName);
		if (ownAction == null && parent != null && parent != this) { ownAction = parent.getAction(aName); }
		return ownAction;
	}

	/**
	 * Gets the own actions.
	 *
	 * @return the own actions
	 */
	@Override
	public IMap<String, IActionDescription> getOwnActions() { return actions == null ? GamaMapFactory.EMPTY : actions; }

	/**
	 * Gets the actions map.
	 *
	 * @return the actions map
	 */
	public IMap<String, IActionDescription> getActionsMap() {
		if (actions == null) { actions = GamaMapFactory.create(); }
		return actions;
	}

	/**
	 * Removes the action.
	 *
	 * @param temp
	 *            the temp
	 */
	public void removeAction(final String temp) {
		getOwnActions().remove(temp);

	}

	/**
	 * Gets the action names.
	 *
	 * @return the action names
	 */
	@Override
	public Collection<String> getActionNames() {
		final Collection<String> allNames = new LinkedHashSet(getOwnActions().keySet());
		if (parent != null && parent != this) { allNames.addAll(parent.getActionNames()); }
		return allNames;
	}

	/**
	 * Gets the actions.
	 *
	 * @return the actions
	 */
	@Override
	public Iterable<IActionDescription> getActions() { return Iterables.transform(getActionNames(), this::getAction); }

	@Override
	public boolean hasAction(final String a, final boolean superInvocation) {
		return superInvocation ? parent != null && parent != this && parent.hasAction(a, false)
				: getOwnActions().containsKey(a)
						|| parent != null && parent != this && parent.hasAction(a, superInvocation);
	}

	@Override
	public ITypeDescription getDescriptionDeclaringAction(final String vn, final boolean superInvocation) {
		if (superInvocation) {
			if (parent == null) return null;
			return parent.getDescriptionDeclaringAction(vn, false);
		}
		return hasAction(vn, false) ? this : null;
	}

	/**
	 * Checks if is abstract.
	 *
	 * @return true, if is abstract
	 */
	@Override
	public final boolean isAbstract() { return isSet(Flag.Abstract); }

	@Override
	protected IType computeType(final boolean doTypeInference) {
		return getTypeNamed(getName());
	}

	/**
	 * Checks if is arg of.
	 *
	 * @param op
	 *            the op
	 * @param arg
	 *            the arg
	 * @return true, if is arg of
	 */
	public boolean isArgOf(final String op, final String arg) {
		final IActionDescription action = getAction(op);
		if (action != null) return action.containsArg(arg);
		return false;
	}

	/**
	 * Returns the parent species.
	 *
	 * @return a TypeDescription or null
	 */
	@Override
	public ITypeDescription getParent() { return parent; }

	@Override
	public void dispose() {
		super.dispose();
		if (isBuiltIn()) return;
		actions = null;
		attributes = null;
		parent = null;
	}

	/**
	 * Inherit from parent.
	 */
	protected void inheritFromParent() {
		// Takes care of invalid species (see Issue 711)
		if (parent != null && parent != this) {
			inheritActionsFrom(parent);
			inheritAttributesFrom(parent);
		}
	}

	/**
	 * Inherit actions from.
	 *
	 * @param p
	 *            the p
	 */
	protected void inheritActionsFrom(final ITypeDescription p) {
		if (p == null || p == this) return;
		for (final IActionDescription inheritedAction : p.getActions()) {
			final String actionName = inheritedAction.getName();
			final IActionDescription userDeclared = getOwnActions().get(actionName);
			if (userDeclared != null) {
				if (!inheritedAction.isBuiltIn() || !userDeclared.isBuiltIn()) {
					TypeDescription.assertActionsAreCompatible(userDeclared, inheritedAction,
							inheritedAction.getOriginName());
					if (inheritedAction.isBuiltIn()) {
						if ("die".equals(actionName)) {
							userDeclared.warning(
									"Redefining the built-in primitive 'die' is not advised as it can lead to potential troubles in the disposal of simulations. If it was not your intention, consider renaming this action.",
									IGamlIssue.GENERAL);
						} else {
							userDeclared.info(
									"Action '" + actionName + "' replaces a primitive of the same name defined in "
											+ userDeclared.getOriginName()
											+ ". If it was not your intention, consider renaming it.",
									IGamlIssue.GENERAL);
						}
					} else {
						userDeclared.info("Action '" + actionName + "' supersedes the one defined in  "
								+ inheritedAction.getOriginName(), IGamlIssue.REDEFINES, IKeyword.NAME);
					}
				}
			} else if (inheritedAction.isAbstract()) {
				this.error(
						"Abstract action '" + actionName + "', inherited from "
								+ inheritedAction.getEnclosingDescription().getName() + ", should be redefined.",
						IGamlIssue.MISSING_ACTION, NAME);
				return;

			}
		}

	}

	/**
	 * Assert actions are compatible.
	 *
	 * @param myAction
	 *            the my action
	 * @param parentAction
	 *            the parent action
	 * @param parentName
	 *            the parent name
	 */
	public static void assertActionsAreCompatible(final IActionDescription myAction,
			final IActionDescription parentAction, final String parentName) {
		// We do no try to point the problems that may exist between two primitives
		// like "send" in messaging and FIPA skills, or the type of heading in moving and moving3D skills...
		if (myAction.isBuiltIn() && parentAction.isBuiltIn()) return;
		final String actionName = parentAction.getName();
		final IType myType = myAction.getGamlType();
		final IType parentType = parentAction.getGamlType();
		if (!parentType.isAssignableFrom(myType)) {
			myAction.error("Return type (" + myType + ") differs from that (" + parentType
					+ ") of the implementation of  " + actionName + " in " + parentName);
			return;
		}
		final Iterable<IDescription> myArgs = myAction.getFormalArgs();
		final Iterable<IDescription> parentArgs = parentAction.getFormalArgs();
		final Iterator<IDescription> myIt = myArgs.iterator();
		final Iterator<IDescription> parentIt = parentArgs.iterator();
		String added = null;
		boolean differentName = false;
		String differentType = null;
		for (IDescription myArg : myArgs) {
			if (!parentIt.hasNext()) {
				added = myArg.getName();
				break;
			}
			final IDescription parentArg = parentIt.next();
			final String myName = myArg.getName();
			final String pName = parentArg.getName();
			if (!myName.equals(pName)) {
				differentName = true;
				break;
			}
			if (!parentArg.getGamlType().isAssignableFrom(myArg.getGamlType())) {
				differentType = myName;
				break;
			}
		}
		if (!myIt.hasNext() && parentIt.hasNext()) {
			final String error = "Missing argument: " + parentIt.next().getName();
			myAction.error(error, IGamlIssue.DIFFERENT_ARGUMENTS, myAction.getUnderlyingElement());
			return;
		}
		if (added != null) {
			final String error =
					"Argument " + added + " does not belong to the definition of " + actionName + " in " + parentName;
			myAction.error(error, IGamlIssue.DIFFERENT_ARGUMENTS, myAction.getUnderlyingElement());
			return;
		}
		if (differentName) {
			final String error = "The  names of arguments should be identical to those of the definition of "
					+ actionName + " in " + parentName;
			myAction.error(error, IGamlIssue.DIFFERENT_ARGUMENTS, myAction.getUnderlyingElement());
			return;
		}
		if (differentType != null) {
			final String error = "The  type of argument  " + differentType
					+ " is not compatible with that in the definition of " + actionName + " in " + parentName;
			myAction.error(error, IGamlIssue.DIFFERENT_ARGUMENTS, myAction.getUnderlyingElement());
		}
	}

	@Override
	public boolean visitChildren(final DescriptionVisitor<IDescription> visitor) {
		for (final IDescription d : getAttributes()) { if (!visitor.process(d)) return false; }
		for (final IDescription d : getActions()) { if (!visitor.process(d)) return false; }
		return true;
	}

	@Override
	public boolean visitOwnChildren(final DescriptionVisitor<IDescription> visitor) {
		if (!visitOwnAttributes(visitor)) return false;
		return visitOwnActions(visitor);
	}

	@Override
	public boolean visitOwnChildrenRecursively(final DescriptionVisitor<IDescription> visitor) {
		if (!visitOwnAttributes(visitor)) return false;
		return visitOwnActionsRecursively(visitor);
	}

	/**
	 * Visit all attributes.
	 *
	 * @param visitor
	 *            the visitor
	 * @return true, if successful
	 */
	@Override
	public boolean visitAllAttributes(final DescriptionVisitor<IDescription> visitor) {
		if (parent != null && parent != this && !parent.visitAllAttributes(visitor)) return false;
		return visitOwnAttributes(visitor);
	}

	/**
	 * Visit own attributes.
	 *
	 * @param visitor
	 *            the visitor
	 * @return true, if successful
	 */
	@Override
	public boolean visitOwnAttributes(final DescriptionVisitor<IDescription> visitor) {
		return getOwnAttributes().forEachValue(visitor);
	}

	/**
	 * Visit own actions.
	 *
	 * @param visitor
	 *            the visitor
	 * @return true, if successful
	 */
	@Override
	public boolean visitOwnActions(final DescriptionVisitor<IDescription> visitor) {
		return getOwnActions().forEachValue(visitor);
	}

	/**
	 * Visit own actions recursively.
	 *
	 * @param visitor
	 *            the visitor
	 * @return true, if successful
	 */
	public boolean visitOwnActionsRecursively(final DescriptionVisitor<IDescription> visitor) {
		return getOwnActions().forEachValue(each -> {
			if (!visitor.process(each)) return false;
			return each.visitOwnChildrenRecursively(visitor);
		});
	}

	@Override
	public Iterable<IDescription> getOwnChildren() {
		return Iterables.concat(getOwnActions().values(), getOwnAttributes().values());
	}

	@Override
	public IDescription validate() {
		if (isSet(Flag.Validated)) return this;
		final IDescription result = super.validate();
		if (result != null && !verifyAttributeCycles()) return null;
		return result;
	}

	/**
	 * Gets the own attribute.
	 *
	 * @param kw
	 *            the kw
	 * @return the own attribute
	 */
	public IVariableDescription getOwnAttribute(final String kw) {
		return getOwnAttributes().get(kw);
	}

	/**
	 * Gets the own action.
	 *
	 * @param kw
	 *            the kw
	 * @return the own action
	 */
	public IActionDescription getOwnAction(final String kw) {
		return getOwnActions().get(kw);
	}

}