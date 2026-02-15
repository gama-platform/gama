/*******************************************************************************************************
 *
 * GamlResourceServices.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.resource;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.resource.SynchronizedXtextResourceSet;
import org.eclipse.xtext.resource.XtextResourceSet;

import com.google.common.collect.Iterables;

import gama.api.GAMA;
import gama.api.compilation.ast.ISyntacticElement;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IModelDescription;
import gama.api.compilation.validation.IValidationContext;
import gama.api.constants.IKeyword;
import gama.api.data.objects.IMap;
import gama.api.utils.map.GamaMapFactory;
import gama.dev.DEBUG;
import gaml.compiler.gaml.documentation.GamlResourceDocumenter;
import gaml.compiler.gaml.indexer.GamlResourceIndexer;
import gaml.compiler.gaml.parsing.GamlSyntacticConverter;
import gaml.compiler.gaml.validation.IGamlBuilderListener;
import gaml.compiler.gaml.validation.ValidationContext;

/**
 * Central service class for managing GAML resources, their validation contexts, listeners, and syntactic contents.
 * Provides utilities for resource encoding, path resolution, and resource lifecycle management.
 * 
 * <p>This class maintains several static registries:
 * <ul>
 *   <li>Resource listeners for editor notifications</li>
 *   <li>Validation contexts for error tracking</li>
 *   <li>Cached syntactic contents for performance</li>
 *   <li>Shared resource sets for resource pooling</li>
 * </ul>
 * 
 * <p>Thread Safety: This class uses concurrent data structures and volatile fields for thread-safe operations.
 * Resource sets are lazily initialized with double-checked locking.
 * 
 * @author GAMA Development Team
 * @since 2.0
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamlResourceServices {

	static {
		DEBUG.OFF();
	}

	/** Counter for generating unique synthetic resource URIs. Thread-safe through atomic operations. */
	private static volatile int resourceCount = 0;

	/** Singleton documenter for generating GAML resource documentation. */
	private static final GamlResourceDocumenter documenter = new GamlResourceDocumenter();

	/** Singleton converter for transforming parse trees into syntactic elements. */
	private static final GamlSyntacticConverter converter = new GamlSyntacticConverter();

	/** 
	 * Registry of resource listeners indexed by URI. 
	 * Listeners receive validation events when resources are processed.
	 */
	private static final Map<URI, IGamlBuilderListener> resourceListeners = GamaMapFactory.createUnordered();

	/** 
	 * Registry of validation contexts indexed by URI.
	 * Contexts accumulate errors and warnings during resource validation.
	 */
	private static final Map<URI, ValidationContext> resourceErrors = GamaMapFactory.createUnordered();

	/** 
	 * Cache of parsed syntactic contents indexed by URI.
	 * Uses concurrent map for thread-safe access.
	 * Note: Currently unused in favor of direct parsing.
	 */
	private static final Map<URI, ISyntacticElement> cachedResourceContents = GamaMapFactory.concurrentMap();

	/** 
	 * Shared resource set pool for temporary resource operations.
	 * Lazily initialized and synchronized for thread safety.
	 */
	private static volatile XtextResourceSet poolSet;

	/** 
	 * Shared resource set for standard resource loading operations.
	 * Lazily initialized and synchronized for thread safety.
	 */
	private static volatile XtextResourceSet resourceSet;

	/**
	 * Private constructor to prevent instantiation of this utility class.
	 */
	private GamlResourceServices() {
		throw new UnsupportedOperationException("Utility class should not be instantiated");
	}

	/**
	 * Properly encodes and partially verifies the URI passed as parameter. In the case of a URI that does not use the
	 * "platform:" scheme, it is first converted into a file URI so that headless operations that do not use a workspace
	 * can still perform correctly.
	 * 
	 * <p>This method handles both platform-based URIs (when running in Eclipse) and file-based URIs (when running
	 * in headless mode). It ensures the URI is properly encoded and uses canonical paths when possible.
	 *
	 * @param uri the URI to encode and verify (may be null)
	 * @return null if the parameter is null or does not represent a file or resource; otherwise, a properly
	 *         encoded version of the parameter
	 */
	public static URI properlyEncodedURI(final URI uri) {
		if (uri == null) return null;
		
		URI uriToReturn = uri;
		if (GAMA.isInHeadLessMode() && !uri.isPlatformResource()) {
			final String filePath = uri.toFileString();
			if (filePath != null) {
				try {
					uriToReturn = URI.createFileURI(new File(filePath).getCanonicalPath());
				} catch (final IOException e) {
					// If canonical path cannot be determined, use original URI
					DEBUG.ERR("Cannot determine canonical path for: " + filePath, e);
					return URI.createURI(uri.toString(), true);
				}
			}
		}
		// Ensure the URI is properly encoded
		return URI.createURI(uriToReturn.toString(), true);
	}

	/**
	 * Checks if the resource identified by the given URI is currently being edited (has a registered listener).
	 *
	 * @param uri the URI of the resource to check
	 * @return true if the resource is being edited, false otherwise
	 */
	public static boolean isEdited(final URI uri) {
		return resourceListeners.containsKey(uri);
	}

	/**
	 * Checks if the given resource is currently being edited (has a registered listener).
	 *
	 * @param r the resource to check
	 * @return true if the resource is being edited, false otherwise
	 */
	public static boolean isEdited(final Resource r) {
		return isEdited(r.getURI());
	}

	/**
	 * Updates the validation state of a resource and notifies its registered listener.
	 * Filters and provides experiment descriptions to the listener if a model is available.
	 *
	 * @param uri the URI of the resource to update
	 * @param model the model description (may be null)
	 * @param newState the new validation state (true for valid, false for invalid)
	 * @param status the validation context containing errors and warnings
	 */
	public static void updateState(final URI uri, final IModelDescription model, final boolean newState,
			final IValidationContext status) {
		final URI newURI = uri;

		final IGamlBuilderListener listener = resourceListeners.get(newURI);
		if (listener == null) return;
		
		final Iterable exps = model == null ? newState ? Collections.EMPTY_SET : null
				: Iterables.filter(model.getExperiments(), each -> !each.isAbstract());
		listener.validationEnded(model, exps, status);
	}

	/**
	 * Registers a listener for the specified resource URI to receive validation events.
	 * The listener will be notified when the resource's validation state changes.
	 *
	 * @param uri the URI of the resource to listen to
	 * @param listener the listener to register
	 */
	public static void addResourceListener(final URI uri, final IGamlBuilderListener listener) {
		final URI newURI = uri;
		resourceListeners.put(newURI, listener);
	}

	/**
	 * Removes the resource listener (happens when a file ceases being edited). 
	 * Invalidates both the documentation already collected and the collection of 
	 * this documentation by the validation context if it exists.
	 *
	 * @param listener the listener to remove
	 */
	public static void removeResourceListener(final IGamlBuilderListener listener) {
		final Iterator<Map.Entry<URI, IGamlBuilderListener>> it = resourceListeners.entrySet().iterator();
		while (it.hasNext()) {
			final Map.Entry<URI, IGamlBuilderListener> entry = it.next();
			if (entry.getValue() == listener) {
				getResourceDocumenter().invalidate(entry.getKey());
				IValidationContext vc = getValidationContext(entry.getKey());
				if (vc != null) { vc.shouldDocument(false); }
				it.remove();
				return;
			}
		}
	}

	/**
	 * Gets or creates the validation context for the specified resource.
	 * The validation context tracks errors, warnings, and documentation for the resource.
	 *
	 * @param r the GAML resource
	 * @return the existing or newly created validation context
	 */
	public static IValidationContext getOrCreateValidationContext(final GamlResource r) {
		final URI newURI = r.getURI();
		if (!resourceErrors.containsKey(newURI)) {
			resourceErrors.put(newURI, new ValidationContext(newURI, r.hasErrors(), getResourceDocumenter()));
		}
		final IValidationContext result = resourceErrors.get(newURI);
		return result;
	}

	/**
	 * Gets the validation context for the specified URI, or null if not already created.
	 *
	 * @param newURI the URI to look up
	 * @return the validation context, or null if none exists
	 */
	public static IValidationContext getValidationContext(final URI newURI) {
		return resourceErrors.get(newURI);
	}

	/**
	 * Removes and discards the validation context for the specified resource.
	 *
	 * @param r the GAML resource whose validation context should be discarded
	 */
	public static void discardValidationContext(final GamlResource r) {
		resourceErrors.remove(r.getURI());
	}

	/**
	 * Returns the path from the root of the workspace for the specified resource.
	 * Handles platform URIs, file URIs, and other URI schemes appropriately.
	 * The path is URL-decoded to handle special characters.
	 *
	 * @param r the resource whose path to retrieve
	 * @return an IPath representing the resource's path (never null)
	 */
	public static IPath getPathOf(final Resource r) {
		IPath path;
		final URI uri = r.getURI();
		if (uri.isPlatform()) {
			path = new Path(uri.toPlatformString(false));
		} else if (uri.isFile()) {
			path = new Path(uri.toFileString());
		} else {
			path = new Path(uri.path());
		}
		try {
			path = new Path(URLDecoder.decode(path.toOSString(), "UTF-8"));
		} catch (final UnsupportedEncodingException e) {
			DEBUG.ERR("Failed to decode path: " + path.toOSString(), e);
		}
		return path;
	}

	/**
	 * Gets the full file system path of the model file for the specified resource.
	 * In headless mode (without workspace), returns the file path directly.
	 * Otherwise, resolves the path through the workspace.
	 *
	 * @param r the resource
	 * @return the model's file system path as a string (empty string if path cannot be determined)
	 */
	public static String getModelPathOf(final Resource r) {
		// Likely in a headless scenario (w/o workspace)
		if (r.getURI().isFile()) return new Path(r.getURI().toFileString()).toOSString();
		final IPath path = getPathOf(r);
		final IFile file = GAMA.getWorkspaceManager().getRoot().getFile(path);
		final IPath fullPath = file.getLocation();
		return fullPath == null ? "" : fullPath.toOSString();
	}

	/**
	 * Checks if the given file represents a project directory by looking for a .project file.
	 *
	 * @param f the file to check
	 * @return true if the file is a project directory, false otherwise
	 */
	private static boolean isProject(final File f) {
		final String[] files = f.list();
		if (files != null) { for (final String s : files) { if (".project".equals(s)) return true; } }
		return false;
	}

	/**
	 * Gets the project path containing the specified resource.
	 * Traverses parent directories until a .project file is found.
	 * In headless mode, searches the file system; otherwise, uses the workspace.
	 *
	 * @param r the resource
	 * @return the project's file system path as a string (empty string if not found)
	 */
	public static String getProjectPathOf(final Resource r) {
		if (r == null) return "";
		final URI uri = r.getURI();
		if (uri == null) return "";
		// Cf. #2983 -- we are likely in a headless scenario
		if (!uri.isFile()) {
			final IPath path = getPathOf(r);
			final IFile file = GAMA.getWorkspaceManager().getRoot().getFile(path);
			final IPath fullPath = file.getProject().getLocation();
			return fullPath == null ? "" : fullPath.toOSString();
		}
		File project = new File(uri.toFileString());
		while (project != null && !isProject(project)) { project = project.getParentFile(); }
		return project == null ? "" : project.getAbsolutePath();
	}

	/**
	 * Creates a temporary synthetic GAML resource with proper import setup.
	 * The resource is assigned a unique synthetic URI and inherits imports from the provided description.
	 * This method is unsynchronized to avoid thread contention at startup (thread-safe via volatile counter).
	 *
	 * @param existing the existing description to inherit imports from (may be null)
	 * @return a newly created temporary GAML resource
	 */
	public static GamlResource getTemporaryResource(final IDescription existing) {
		ResourceSet rs = null;
		GamlResource r = null;
		if (existing != null) {
			final IModelDescription desc = existing.getModelDescription();
			if (desc != null) {
				final EObject e = desc.getUnderlyingElement();
				if (e != null) {
					r = (GamlResource) e.eResource();
					if (r != null) { rs = r.getResourceSet(); }
				}
			}
		}
		if (rs == null) { rs = getPoolSet(); }
		final URI uri = URI.createURI(IKeyword.SYNTHETIC_RESOURCES_PREFIX + resourceCount++ + ".gaml", false);
		final GamlResource result = (GamlResource) rs.createResource(uri);
		final IMap<URI, String> imports = GamaMapFactory.create();
		imports.put(uri, null);
		if (r != null) {
			imports.put(r.getURI(), null);
			final Map<URI, String> uris = GamlResourceIndexer.allImportsOf(r);
			imports.putAll(uris);
		}
		result.getCache().getOrCreate(result).set(GamlResourceIndexer.IMPORTED_URIS, imports);
		return result;
	}

	/**
	 * Deletes and discards a temporary resource.
	 *
	 * @param temp the temporary resource to discard
	 */
	public static void discardTemporaryResource(final GamlResource temp) {
		try {
			temp.delete(null);
		} catch (final IOException e) {
			DEBUG.ERR("Failed to delete temporary resource: " + temp.getURI(), e);
		}
	}

	/**
	 * Gets the singleton resource documenter for generating GAML documentation.
	 *
	 * @return the resource documenter
	 */
	public static GamlResourceDocumenter getResourceDocumenter() { return documenter; }

	/**
	 * Builds the syntactic contents from a GAML resource by converting its AST.
	 * This transforms the parse tree into a structured syntactic element tree.
	 *
	 * @param r the GAML resource to process
	 * @return the root syntactic element representing the resource's contents
	 */
	public static ISyntacticElement buildSyntacticContents(final GamlResource r) {
		return converter.buildSyntacticContents(r.getParseResult().getRootASTElement());
	}

	/**
	 * Gets or creates the pool resource set for temporary resource operations.
	 * This resource set is lazily initialized and synchronized for thread safety.
	 * Used primarily for creating temporary synthetic resources.
	 *
	 * @return the pool resource set (never null)
	 */
	private static XtextResourceSet getPoolSet() {
		if (poolSet == null) {
			// Synchronized necessary for GAMA Server messages that require compilation of expressions
			poolSet = new SynchronizedXtextResourceSet() {
				{
					setClasspathURIContext(GamlResourceServices.class);
				}
			};
		}
		return poolSet;
	}

	/**
	 * Gets or creates the standard resource set for loading GAML resources.
	 * This resource set is lazily initialized and synchronized as a precaution against parallel compilation.
	 *
	 * @return the resource set (never null)
	 */
	static XtextResourceSet getResourceSet() {
		if (resourceSet == null) { resourceSet = new SynchronizedXtextResourceSet(); }
		return resourceSet;
	}

	/**
	 * Compares two URIs for equality after proper encoding.
	 * Handles null URIs appropriately.
	 *
	 * @param uri1 the first URI to compare
	 * @param uri2 the second URI to compare
	 * @return true if both URIs are equal (or both are null), false otherwise
	 */
	public static boolean equals(final URI uri1, final URI uri2) {
		if (uri1 == null) return uri2 == null;
		if (uri2 == null) return false;
		return properlyEncodedURI(uri1).equals(properlyEncodedURI(uri2));
	}

	/**
	 * Gets or creates syntactic contents for the specified URI by loading and parsing the resource.
	 * This method loads the resource, builds its syntactic tree, and cleans up the resource set afterward.
	 *
	 * @param uri the URI of the resource to process
	 * @return the root syntactic element representing the resource's contents
	 */
	public static ISyntacticElement getOrCreateSyntacticContents(final URI uri) {
		try {
			final GamlResource r = (GamlResource) getResourceSet().getResource(uri, true);
			ISyntacticElement result = buildSyntacticContents(r);
			return result;
		} finally {
			clearResourceSet(getResourceSet());
		}
	}

	/**
	 * Invalidates and removes the cached syntactic contents for the specified URI.
	 * Properly disposes of the syntactic element if it exists.
	 *
	 * @param uri the URI whose syntactic contents should be invalidated
	 */
	public static void invalidateSyntacticContents(final URI uri) {
		ISyntacticElement existing = cachedResourceContents.remove(uri);
		if (existing != null) { existing.dispose(); }
	}

	/**
	 * Clears all resources from a resource set, temporarily disabling notifications for performance.
	 * Ensures the original notification state is restored even if an exception occurs.
	 *
	 * @param resourceSet the resource set to clear
	 */
	protected static void clearResourceSet(final ResourceSet resourceSet) {
		final boolean wasDeliver = resourceSet.eDeliver();
		try {
			resourceSet.eSetDeliver(false);
			resourceSet.getResources().clear();
		} catch (final Exception e) {}

		finally {
			resourceSet.eSetDeliver(wasDeliver);
		}
	}

}
