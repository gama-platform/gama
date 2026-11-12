/*******************************************************************************************************
 *
 * GamlScopeProvider.java, in gaml.compiler.gaml, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gaml.compiler.scoping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.nodemodel.ILeafNode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.resource.EObjectDescription;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.ISelectable;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.scoping.Scopes;
import org.eclipse.xtext.scoping.impl.SimpleScope;

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;

import gama.annotations.constants.IKeyword;
import gaml.compiler.EGaml;
import gaml.compiler.gaml.Access;
import gaml.compiler.gaml.Block;
import gaml.compiler.gaml.Expression;
import gaml.compiler.gaml.Facet;
import gaml.compiler.gaml.Function;
import gaml.compiler.gaml.GamlPackage;
import gaml.compiler.gaml.Model;
import gaml.compiler.gaml.ReservedLiteral;
import gaml.compiler.gaml.S_Callable;
import gaml.compiler.gaml.S_Definition;
import gaml.compiler.gaml.S_Do;
import gaml.compiler.gaml.S_Experiment;
import gaml.compiler.gaml.S_Global;
import gaml.compiler.gaml.S_Loop;
import gaml.compiler.gaml.S_Reflex;
import gaml.compiler.gaml.S_Species;
import gaml.compiler.gaml.SkillRef;
import gaml.compiler.gaml.StandaloneExperiment;
import gaml.compiler.gaml.Statement;
import gaml.compiler.gaml.TypeDefinition;
import gaml.compiler.gaml.TypeRef;
import gaml.compiler.gaml.VarDefinition;
import gaml.compiler.gaml.VariableRef;

/**
 * This class contains custom scoping description.
 *
 * see : http://www.eclipse.org/Xtext/documentation/latest/xtext.html#scoping on how and when to use it
 *
 */
// @Singleton
public class GamlScopeProvider extends org.eclipse.xtext.scoping.impl.SimpleLocalScopeProvider {

	@Inject private BuiltinGlobalScopeProvider builtinScopeProvider;

	/**
	 * The Class GamlMultimapBasedSelectable.
	 */
	private class GamlMultimapBasedSelectable implements ISelectable {
		
		/** The descriptions. */
		List<IEObjectDescription> descriptions;
		
		/** The name to objects. */
		private Multimap<QualifiedName, IEObjectDescription> nameToObjects;

		@Override
		public boolean isEmpty() {
			return descriptions == null;
		}

		@Override
		public Iterable<IEObjectDescription> getExportedObjectsByType(final EClass type) {
			if (descriptions == null) { return Collections.emptyList(); }
			return Iterables.filter(descriptions, input -> EcoreUtil2.isAssignableFrom(type, input.getEClass()));
		}

		@Override
		public Iterable<IEObjectDescription> getExportedObjectsByObject(final EObject object) {
			if (descriptions == null) { return Collections.emptyList(); }
			final URI uri = EcoreUtil2.getPlatformResourceOrNormalizedURI(object);
			return Iterables.filter(descriptions, input -> 
				input.getEObjectOrProxy() == object || uri.equals(input.getEObjectURI())
			);
		}

		@Override
		public Iterable<IEObjectDescription> getExportedObjects(final EClass type, final QualifiedName name,
				final boolean ignoreCase) {
			if (nameToObjects == null) { return Collections.emptyList(); }
			if (!nameToObjects.containsKey(name)) { return Collections.emptyList(); }

			final List<IEObjectDescription> result = new ArrayList<>();
			for (final IEObjectDescription desc : nameToObjects.get(name)) {
				if (EcoreUtil2.isAssignableFrom(type, desc.getEClass())) { result.add(desc); }
			}
			return result;
		}

		@Override
		public Iterable<IEObjectDescription> getExportedObjects() {
			return descriptions == null ? Collections.emptyList() : descriptions;
		}

		/**
		 * Adds the.
		 *
		 * @param name the name
		 * @param e the e
		 */
		private void add(final QualifiedName name, final EObjectDescription e) {
			if (descriptions == null) {
				descriptions = new ArrayList<>(32);
				nameToObjects = LinkedHashMultimap.create(32, 2);
			}
			descriptions.add(e);
			nameToObjects.put(name, e);
		}

	}

	@Override
	public IScope getScope(final EObject context, final EReference reference) {
		IScope scope = super.getScope(context, reference);
		if (context == null || !isLocallyScoped(reference)) return scope;

		final Access memberAccess = enclosingMemberAccess(context);
		if (memberAccess != null) {
			final IScope memberScope = scopeForReceiver(memberAccess.getLeft(), reference);
			if (memberScope != null) return memberScope;
		}

		if (!isInheritanceFacet(context)) {
			final List<S_Species> inheritedSpecies = new ArrayList<>();
			final Set<S_Species> visitedSpecies = Collections.newSetFromMap(new IdentityHashMap<>());
			final S_Species species = enclosingSpecies(context);
			if (species != null) { collectInheritedSpecies(species, inheritedSpecies, visitedSpecies); }
			for (final S_Species inherited : inheritedSpecies) {
				if (inherited.getBlock() != null) {
					scope = scopeForBlock(inherited.getBlock(), reference, scope);
				}
			}
		}

		final List<Block> enclosingBlocks = new ArrayList<>();
		for (EObject current = context; current != null; current = current.eContainer()) {
			if (current instanceof Block block) { enclosingBlocks.add(block); }
		}

		for (int i = enclosingBlocks.size() - 1; i >= 0; i--) {
			scope = scopeForBlock(enclosingBlocks.get(i), reference, scope);
		}

		if (reference == GamlPackage.Literals.VARIABLE_REF__REF && isAspectFacet(context)) {
			scope = scopeForSpeciesAspects(context, scope);
		}
		return scope;
	}

	private Access enclosingMemberAccess(final EObject context) {
		for (EObject current = context; current != null; current = current.eContainer()) {
			if (current instanceof Access access && ".".equals(access.getOp())
					&& isContainedIn(context, access.getRight())) {
				return access;
			}
		}
		return null;
	}

	private boolean isContainedIn(final EObject object, final EObject ancestor) {
		for (EObject current = object; current != null; current = current.eContainer()) {
			if (current == ancestor) return true;
		}
		return false;
	}

	private IScope scopeForReceiver(final Expression receiver, final EReference reference) {
		if (receiver instanceof ReservedLiteral literal) {
			final String keyword = literal.getOp();
			final S_Species species = enclosingSpecies(receiver);
			if ("self".equals(keyword)) return scopeForSelfMembers(receiver, reference);
			if ("super".equals(keyword) && species != null) {
				return scopeForParentMembers(species, reference);
			}
		}

		final S_Species sourceSpecies = sourceSpeciesOf(receiver);
		if (sourceSpecies != null) { return scopeForSpeciesMembers(sourceSpecies, reference); }

		final String speciesName = speciesNameOf(receiver);
		return speciesName == null ? null : scopeForBuiltInSpecies(speciesName, reference, IScope.NULLSCOPE);
	}

	/** Resolves receiver members from source declarations and platform metadata, even in an incomplete model. */
	public IScope getMemberCompletionScope(final EObject context, final Expression receiver, final String receiverName,
			final EReference reference, final int offset) {
		if ("self".equals(receiverName)) {
			final IScope scope = scopeForSelfMembers(context, reference);
			return scope == null ? IScope.NULLSCOPE : scope;
		}
		if ("super".equals(receiverName)) {
			final S_Species species = enclosingSpecies(context);
			return species == null ? IScope.NULLSCOPE : scopeForParentMembers(species, reference);
		}
		if (receiver != null) {
			final IScope scope = scopeForReceiver(receiver, reference);
			if (scope != null) return scope;
			if (!(receiver instanceof VariableRef)) return IScope.NULLSCOPE;
		}
		if (receiverName == null) return IScope.NULLSCOPE;
		final IEObjectDescription candidate = getCompletionScope(context, GamlPackage.Literals.VARIABLE_REF__REF, offset)
				.getSingleElement(QualifiedName.create(receiverName));
		if (candidate != null && candidate.getEObjectOrProxy() instanceof S_Definition definition) {
			final S_Species species = sourceSpeciesOf(definition.getTkey());
			if (species != null) return scopeForSpeciesMembers(species, reference);
			final String type = speciesNameOf(definition.getTkey());
			if (type != null) return scopeForBuiltInSpecies(type, reference, IScope.NULLSCOPE);
		}
		return IScope.NULLSCOPE;
	}

	/** Keeps completion lexically scoped; local declarations are visible only after their declaration ends. */
	public IScope getCompletionScope(final EObject context, final EReference reference, final int offset) {
		if (context == null) return IScope.NULLSCOPE;
		if (isInheritanceFacet(context) || isAspectFacet(context)) return getScope(context, reference);
		final boolean actionName = isActionNameContext(context, offset);
		if (actionName && reference == GamlPackage.Literals.VARIABLE_REF__REF) {
			return getCompletionScope(context, GamlPackage.Literals.ACTION_REF__REF, offset);
		}
		IScope scope = actionName ? IScope.NULLSCOPE
				: builtinScopeProvider.getExpressionScope(reference == GamlPackage.Literals.ACTION_REF__REF);
		final List<EObject> globals = new ArrayList<>();
		for (final IEObjectDescription description : super.getScope(context, reference).getAllElements()) {
			final EObject definition = EcoreUtil.resolve(description.getEObjectOrProxy(), context);
			if (definition.eContainer() instanceof Block block && block.eContainer() instanceof S_Global
					&& isCompletionDeclaration(definition, reference)) {
				globals.add(definition);
			} else if (definition instanceof S_Species && enclosingSpecies(definition.eContainer()) == null
					&& reference == GamlPackage.Literals.VARIABLE_REF__REF) {
				globals.add(definition);
			}
		}
		scope = Scopes.scopeFor(globals, scope);
		final IScope selfMembers = scopeForSelfMembers(context, reference);
		if (selfMembers != null) {
			final List<IEObjectDescription> members = new ArrayList<>();
			selfMembers.getAllElements().forEach(members::add);
			scope = new SimpleScope(scope, members);
		}
		final List<EObject> containers = new ArrayList<>();
		for (EObject current = context; current != null; current = current.eContainer()) { containers.add(current); }
		for (int i = containers.size() - 1; i >= 0; i--) {
			final EObject current = containers.get(i);
			if (reference == GamlPackage.Literals.VARIABLE_REF__REF && current instanceof S_Callable callable
					&& !declarationEndsBefore(callable, offset)) {
				scope = Scopes.scopeFor(callable.getArgs(), scope);
			}
			if (reference == GamlPackage.Literals.VARIABLE_REF__REF && current instanceof S_Loop loop
					&& loop.getName() != null && !declarationEndsBefore(loop, offset)) {
				scope = Scopes.scopeFor(List.of(loop), scope);
			}
			if (current instanceof Block block && blockOpenAt(block, offset)
					&& !(block.eContainer() instanceof S_Species)
					&& !(block.eContainer() instanceof S_Global) && !(block.eContainer() instanceof Model)) {
				final List<EObject> locals = new ArrayList<>();
				for (final Statement statement : block.getStatements()) {
					if (isCompletionDeclaration(statement, reference) && declarationEndsBefore(statement, offset)) {
						locals.add(statement);
					}
				}
				scope = Scopes.scopeFor(locals, scope);
			}
		}
		return scope;
	}

	private boolean declarationEndsBefore(final Statement statement, final int offset) {
		final var node = NodeModelUtils.findActualNodeFor(statement);
		if (node == null) return false;
		ILeafNode last = null;
		for (final var leaf : node.getLeafNodes()) {
			if (!leaf.isHidden()) last = leaf;
		}
		if (last == null || last.getEndOffset() > offset) return false;
		if (statement.getBlock() != null) {
			return "}".equals(last.getText())
					&& last.getParent() == NodeModelUtils.findActualNodeFor(statement.getBlock());
		}
		return ";".equals(last.getText());
	}

	private boolean blockOpenAt(final Block block, final int offset) {
		final var node = NodeModelUtils.findActualNodeFor(block);
		if (node == null || node.getOffset() > offset) return false;
		for (final var leaf : node.getLeafNodes()) {
			if (!leaf.isHidden() && "}".equals(leaf.getText()) && leaf.getOffset() < offset) {
				// Only the block's own closing brace ends its scope, not those of nested statements.
				if (leaf.getParent() == node) return false;
			}
		}
		return true;
	}

	private boolean isActionNameContext(final EObject context, final int offset) {
		for (EObject current = context; current != null; current = current.eContainer()) {
			if (current instanceof Facet facet) {
				final String key = EGaml.getInstance().getKeyOf(facet);
				return IKeyword.ACTION.equals(key) || IKeyword.ON_CHANGE.equals(key);
			}
			if (current instanceof S_Do statement) {
				if (declarationEndsBefore(statement, offset)) return false;
				final Expression expression = statement.getExpr();
				return expression == null || expression instanceof VariableRef && isContainedIn(context, expression)
						|| expression instanceof Function function && isContainedIn(context, function.getLeft());
			}
		}
		return false;
	}

	private boolean isCompletionDeclaration(final EObject definition, final EReference reference) {
		if (reference == GamlPackage.Literals.ACTION_REF__REF) {
			return definition instanceof S_Callable callable && callable.getName() != null;
		}
		return definition instanceof S_Definition variable && !(variable instanceof S_Callable)
				&& variable.getName() != null
				|| definition instanceof S_Species species && species.getName() != null;
	}

	private IScope scopeForParentMembers(final S_Species species, final EReference reference) {
		for (final Facet facet : species.getFacets()) {
			if (!IKeyword.PARENT.equals(EGaml.getInstance().getKeyOf(facet)) || facet.getExpr() == null) continue;
			final List<VariableRef> references = new ArrayList<>();
			if (facet.getExpr() instanceof VariableRef variableRef) { references.add(variableRef); }
			final Iterator<EObject> contents = EcoreUtil2.getAllContents(facet.getExpr(), true);
			while (contents.hasNext()) {
				if (contents.next() instanceof VariableRef variableRef) { references.add(variableRef); }
			}
			for (final VariableRef variableRef : references) {
				if (variableRef.getRef() instanceof S_Species parent) {
					return scopeForSpeciesMembers(parent, reference);
				}
				final String parentName = variableRef.getRef() == null ? null : variableRef.getRef().getName();
				if (parentName != null) { return scopeForBuiltInSpecies(parentName, reference, IScope.NULLSCOPE); }
			}
		}
		return scopeForBuiltInSpecies("agent", reference, IScope.NULLSCOPE);
	}

	private S_Species sourceSpeciesOf(final Expression expression) {
		if (expression instanceof VariableRef reference) {
			if (reference.getRef() instanceof S_Species species) return species;
			if (reference.getRef() instanceof S_Definition definition) {
				final S_Species species = sourceSpeciesOf(definition.getTkey());
				if (species != null) return species;
			}
		}
		if (expression instanceof TypeRef reference && reference.getRef() instanceof S_Species species) {
			return species;
		}
		if (expression instanceof Access access) { return sourceSpeciesOf(access.getRight()); }
		return null;
	}

	private String speciesNameOf(final Expression expression) {
		if (expression instanceof VariableRef reference) {
			if (reference.getRef() instanceof S_Definition definition) {
				final String typeName = speciesNameOf(definition.getTkey());
				if (typeName != null) return typeName;
			}
			return reference.getRef() == null ? null : reference.getRef().getName();
		}
		if (expression instanceof TypeRef reference) {
			final TypeDefinition type = reference.getRef();
			return type == null ? null : type.getName();
		}
		if (expression instanceof Access access) { return speciesNameOf(access.getRight()); }
		return null;
	}

	private IScope scopeForSpeciesMembers(final S_Species species, final EReference reference) {
		final List<S_Species> hierarchy = new ArrayList<>();
		final Set<S_Species> visited = Collections.newSetFromMap(new IdentityHashMap<>());
		collectInheritedSpecies(species, hierarchy, visited);
		hierarchy.add(species);

		IScope scope = IScope.NULLSCOPE;
		for (final S_Species type : hierarchy) {
			scope = scopeForBuiltInParent(type, reference, scope);
			scope = scopeForSkills(type, reference, scope);
			scope = scopeForDeclaredMembers(type.getBlock(), reference, scope);
		}
		return scope;
	}

	private IScope scopeForSelfMembers(final EObject context, final EReference reference) {
		for (EObject current = context; current != null; current = current.eContainer()) {
			if (current instanceof S_Species species) return scopeForSpeciesMembers(species, reference);
			if (current instanceof StandaloneExperiment experiment) {
				return scopeForDeclaredMembers(experiment.getBlock(), reference,
						scopeForBuiltInSpecies("experiment", reference, IScope.NULLSCOPE));
			}
			if (current instanceof S_Global || current instanceof S_Experiment) {
				final Statement section = (Statement) current;
				IScope scope = scopeForBuiltInSpecies(current instanceof S_Global ? "model" : "experiment",
						reference, IScope.NULLSCOPE);
				if (current instanceof S_Global && current.eContainer() instanceof Block block) {
					scope = scopeForDeclaredMembers(block, reference, scope);
				}
				return scopeForDeclaredMembers(section.getBlock(), reference, scope);
			}
		}
		return null;
	}

	private IScope scopeForDeclaredMembers(final Block block, final EReference reference, final IScope parent) {
		if (block == null) return parent;
		final List<EObject> members = new ArrayList<>();
		for (final Statement statement : block.getStatements()) {
			if (isCompletionDeclaration(statement, reference)) { members.add(statement); }
		}
		return Scopes.scopeFor(members, parent);
	}

	private IScope scopeForBuiltInParent(final S_Species species, final EReference reference, final IScope parent) {
		boolean hasParent = false;
		for (final Facet facet : species.getFacets()) {
			if (!IKeyword.PARENT.equals(EGaml.getInstance().getKeyOf(facet)) || facet.getExpr() == null) continue;
			hasParent = true;
			final List<VariableRef> references = new ArrayList<>();
			if (facet.getExpr() instanceof VariableRef variableRef) { references.add(variableRef); }
			final Iterator<EObject> contents = EcoreUtil2.getAllContents(facet.getExpr(), true);
			while (contents.hasNext()) {
				if (contents.next() instanceof VariableRef variableRef) { references.add(variableRef); }
			}
			for (final VariableRef variableRef : references) {
				if (variableRef.getRef() instanceof S_Species) continue;
				final String name = variableRef.getRef() == null ? null : variableRef.getRef().getName();
				if (name != null) { return scopeForBuiltInSpecies(name, reference, parent); }
			}
		}
		return hasParent ? parent : scopeForBuiltInSpecies("agent", reference, parent);
	}

	private IScope scopeForSkills(final S_Species species, final EReference reference, final IScope parent) {
		IScope scope = parent;
		for (final Facet facet : species.getFacets()) {
			if (!IKeyword.SKILLS.equals(EGaml.getInstance().getKeyOf(facet)) || facet.getExpr() == null) continue;
			final List<EObject> skillReferences = new ArrayList<>();
			if (facet.getExpr() instanceof SkillRef || facet.getExpr() instanceof VariableRef) {
				skillReferences.add(facet.getExpr());
			}
			final Iterator<EObject> contents = EcoreUtil2.getAllContents(facet.getExpr(), true);
			while (contents.hasNext()) {
				final EObject object = contents.next();
				if (object instanceof SkillRef || object instanceof VariableRef) { skillReferences.add(object); }
			}
			for (final EObject skillReference : skillReferences) {
				final String name = EGaml.getInstance().getNameOfRef(skillReference);
				if (name != null && !name.isBlank()) { scope = scopeForBuiltInSkill(name, reference, scope); }
			}
		}
		return scope;
	}

	private IScope scopeForBuiltInSpecies(final String speciesName, final EReference reference, final IScope parent) {
		return builtinScopeProvider.getSpeciesMemberScope(speciesName,
				reference == GamlPackage.Literals.ACTION_REF__REF, parent);
	}

	private IScope scopeForBuiltInSkill(final String skillName, final EReference reference, final IScope parent) {
		return builtinScopeProvider.getSkillMemberScope(skillName,
				reference == GamlPackage.Literals.ACTION_REF__REF, parent);
	}

	private boolean isLocallyScoped(final EReference reference) {
		return reference == GamlPackage.Literals.VARIABLE_REF__REF
				|| reference == GamlPackage.Literals.ACTION_REF__REF;
	}

	private IScope scopeForBlock(final Block block, final EReference reference, final IScope parent) {
		final List<EObject> declarations = new ArrayList<>();
		for (final Statement statement : block.getStatements()) {
			if (reference == GamlPackage.Literals.ACTION_REF__REF) {
				if (statement instanceof S_Callable callable && callable.getName() != null) {
					declarations.add(callable);
				}
			} else if (statement instanceof VarDefinition definition && definition.getName() != null) {
				declarations.add(definition);
			}
		}
		return declarations.isEmpty() ? parent : Scopes.scopeFor(declarations, parent);
	}

	private S_Species enclosingSpecies(final EObject context) {
		for (EObject current = context; current != null; current = current.eContainer()) {
			if (current instanceof S_Species species) return species;
		}
		return null;
	}

	private boolean isInheritanceFacet(final EObject context) {
		final Facet facet = enclosingFacet(context);
		final String key = facet == null ? null : EGaml.getInstance().getKeyOf(facet);
		return IKeyword.PARENT.equals(key) || IKeyword.SKILLS.equals(key);
	}

	private boolean isAspectFacet(final EObject context) {
		final Facet facet = enclosingFacet(context);
		return facet != null && IKeyword.ASPECT.equals(EGaml.getInstance().getKeyOf(facet));
	}

	private Facet enclosingFacet(final EObject context) {
		for (EObject current = context; current != null; current = current.eContainer()) {
			if (current instanceof Facet facet) return facet;
		}
		return null;
	}

	private void collectInheritedSpecies(final S_Species species, final List<S_Species> result,
			final Set<S_Species> visited) {
		for (final Facet facet : species.getFacets()) {
			if (IKeyword.PARENT.equals(EGaml.getInstance().getKeyOf(facet))) {
				collectSpeciesReferences(facet, result, visited);
			}
		}
	}

	private void collectSpeciesReferences(final Facet facet, final List<S_Species> result,
			final Set<S_Species> visited) {
		if (facet.getExpr() == null) return;
		if (facet.getExpr() instanceof VariableRef reference) {
			collectSpeciesReference(reference, result, visited);
		}
		final Iterator<EObject> contents = EcoreUtil2.getAllContents(facet.getExpr(), true);
		while (contents.hasNext()) {
			if (contents.next() instanceof VariableRef reference) {
				collectSpeciesReference(reference, result, visited);
			}
		}
	}

	private void collectSpeciesReference(final VariableRef reference, final List<S_Species> result,
			final Set<S_Species> visited) {
		if (reference.getRef() instanceof S_Species species && visited.add(species)) {
			collectInheritedSpecies(species, result, visited);
			result.add(species);
		}
	}

	private IScope scopeForSpeciesAspects(final EObject context, final IScope parent) {
		final S_Species layer = enclosingSpecies(context);
		if (layer == null || layer.getName() == null || layer.eResource() == null) return parent;

		final List<S_Species> matchingSpecies = new ArrayList<>();
		final Iterator<EObject> contents = EcoreUtil2.getAllContents(layer.eResource(), true);
		while (contents.hasNext()) {
			final EObject object = contents.next();
			if (object instanceof S_Species species && species != layer
					&& layer.getName().equals(species.getName())) {
				matchingSpecies.add(species);
			}
		}

		IScope result = parent;
		final Set<S_Reflex> addedAspects = Collections.newSetFromMap(new IdentityHashMap<>());
		for (final S_Species species : matchingSpecies) {
			final List<S_Species> hierarchy = new ArrayList<>();
			final Set<S_Species> visited = Collections.newSetFromMap(new IdentityHashMap<>());
			collectInheritedSpecies(species, hierarchy, visited);
			hierarchy.add(species);
			for (final S_Species type : hierarchy) {
				if (type.getBlock() == null) continue;
				final List<EObject> aspects = new ArrayList<>();
				for (final Statement statement : type.getBlock().getStatements()) {
					if (statement instanceof S_Reflex reflex && IKeyword.ASPECT.equals(reflex.getKey())
							&& reflex.getName() != null && addedAspects.add(reflex)) {
						aspects.add(reflex);
					}
				}
				if (!aspects.isEmpty()) { result = Scopes.scopeFor(aspects, result); }
			}
		}
		return result;
	}

	@Override
	protected ISelectable getAllDescriptions(final Resource resource) {
		final GamlMultimapBasedSelectable result = new GamlMultimapBasedSelectable();
		final IQualifiedNameProvider provider = getNameProvider();
		final Iterator<EObject> iterator = EcoreUtil2.getAllContents(resource, true);
		while (iterator.hasNext()) {
			final EObject from = iterator.next();
			final QualifiedName qualifiedName = provider.apply(from);
			if (qualifiedName != null) {
				result.add(qualifiedName, new EObjectDescription(qualifiedName, from, null));
			}
		}
		return result;
	}

}