/*******************************************************************************************************
 *
 * GamlResourceDocumenter.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation
 * platform (v.2024-06).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.documentation;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;

import gama.core.common.interfaces.IDocManager;
import gama.dev.DEBUG;
import gama.gaml.descriptions.IDescription;
import gama.gaml.descriptions.ModelDescription;
import gama.gaml.interfaces.IGamlDescription;
import gaml.compiler.gaml.resource.GamlResourceServices;

/**
 * Class GamlResourceDocumenter.
 *
 * @author drogoul
 * @since 13 avr. 2014
 *
 */

@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamlResourceDocumenter implements IDocManager {

	static {
		DEBUG.OFF();
	}

	/** The documentation queue. */
	final Map<URI, GamlResourceDocumentationTask> documentationTasks = new ConcurrentHashMap();

	/**
	 * The references from Eobject to resources and documentation. Key is the URI of the object, value is the
	 * corresponding "documented object"
	 */
	final Map<URI, DocumentedObject> documentedObjects = new ConcurrentHashMap();

	/**
	 * The Record DocumentedObject.
	 *
	 * @param node
	 *            the documentation node corresponding to this object
	 * @param resources
	 *            the resources, resources that are referencing this object
	 */
	record DocumentedObject(DocumentationNode node, Set<URI> resources) {}

	/**
	 * Adds arbitrary documentation task.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param task
	 *            the task
	 * @date 30 déc. 2023
	 */
	public void addDocumentationTask(final URI res, final Runnable run) {
		if (run == null || !isTaskValid(res)) return;
		getTaskFor(res).add(run);
	}

	/**
	 * Sets the gaml documentation.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param object
	 *            the object
	 * @param desc
	 *            the description
	 * @param replace
	 *            the replace
	 * @param force
	 *            the force
	 * @date 29 déc. 2023
	 */
	@Override
	public void setGamlDocumentation(final URI res, final EObject object, final IGamlDescription desc) {
		addDocumentationTask(res,
				() -> internalSetGamlDocumentation(res, getCurrentDocGenerationFor(res), object, desc));
	}

	/**
	 * Internal set gaml documentation.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param openResource
	 *            the open resource
	 * @param object
	 *            the object
	 * @param description
	 *            the description
	 * @date 31 déc. 2023
	 */
	boolean internalSetGamlDocumentation(final URI res, final int generation, final EObject object,
			final IGamlDescription desc) {
		try {
			if (!isTaskValid(res) || !isValidGeneration(res, generation)) return false;
			URI fragment = EcoreUtil.getURI(object);
			DocumentedObject documented = documentedObjects.get(fragment);
			if (documented == null) {
				documented = new DocumentedObject(new DocumentationNode(desc), new HashSet());
				documentedObjects.put(fragment, documented);
			}
			documented.resources.add(res);
			getTaskFor(res).objects.add(fragment);
			return true;
		} catch (final RuntimeException e) {
			DEBUG.ERR("Error in documenting " + res.lastSegment(), e);
			return false;
		}
	}

	// To be called once the validation has been done
	@Override
	public void doDocument(final URI res, final ModelDescription desc,
			final Map<EObject, IGamlDescription> additionalExpressions) {
		GamlResourceDocumentationTask task;
		task = getTaskFor(res);
		task.incrementGeneration();
		int generation = getCurrentDocGenerationFor(res);
		task.add(() -> {
			internalDoDocument(res, generation, desc);
			additionalExpressions.forEach((e, d) -> internalSetGamlDocumentation(res, generation, e, d));
		});
	}

	/**
	 * Internal do document.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param resource
	 *            the resource
	 * @param desc
	 *            the desc
	 * @date 31 déc. 2023
	 */
	private boolean internalDoDocument(final URI resource, final int generation, final IDescription desc) {
		if (desc == null) return false;
		final EObject e = desc.getUnderlyingElement();
		if (e == null) return true; // We return true to continue exploring if other descriptions should be documented
		if (!internalSetGamlDocumentation(resource, generation, e, desc)) return false;
		return desc.visitOwnChildren(d -> internalDoDocument(resource, generation, d));
	}

	@Override
	public IGamlDescription getGamlDocumentation(final EObject object) {
		if (object == null) return null;
		DocumentedObject documented = documentedObjects.get(EcoreUtil.getURI(object));
		return documented == null ? null : documented.node;
	}

	@Override
	public void invalidate(final URI uri) {
		if (uri == null) return;
		GamlResourceDocumentationTask task = documentationTasks.remove(uri);
		if (task != null) {
			task.objects.forEach(object -> {
				DocumentedObject documented = documentedObjects.get(object);
				if (documented != null) {
					Set<URI> resources = documented.resources;
					resources.remove(uri);
					if (resources.isEmpty()) { documentedObjects.remove(object); }
				}
			});
		}
	}

	/**
	 * Invalidate all.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 30 déc. 2023
	 */
	public void invalidateAll() {
		getTaskFor(URI.createURI("")).add(() -> {
			documentedObjects.clear();
			documentationTasks.clear();
		});

	}

	/**
	 * Checks if is task valid.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param resource
	 *            the resource
	 * @return true, if is task valid
	 * @date 1 janv. 2024
	 */
	boolean isTaskValid(final URI resource) {
		return resource != null && GamlResourceServices.isEdited(resource);
	}

	/**
	 * Checks if is valid generation.
	 *
	 * @param resource
	 *            the resource
	 * @param generation
	 *            the generation
	 * @return true, if is valid generation
	 */
	boolean isValidGeneration(final URI resource, final int generation) {
		return getCurrentDocGenerationFor(resource) == generation;
	}

	/**
	 * Gets the task for.
	 *
	 * @param resource
	 *            the resource
	 * @return the task for
	 */
	GamlResourceDocumentationTask getTaskFor(final URI resource) {
		return documentationTasks.computeIfAbsent(resource, GamlResourceDocumentationTask::new);
	}

	/**
	 * Gets the current doc generation for.
	 *
	 * @param resource
	 *            the resource
	 * @return the current doc generation or -1 if not available
	 */
	int getCurrentDocGenerationFor(final URI resource) {
		return getTaskFor(resource).currentGeneration;
	}

}
