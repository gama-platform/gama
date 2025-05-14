/*******************************************************************************************************
 *
 * GamlResourceServices.java, in gaml.compiler.gaml, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
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
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.resource.SynchronizedXtextResourceSet;
import org.eclipse.xtext.resource.XtextResourceSet;

import com.google.common.collect.Iterables;

import gama.core.common.interfaces.IKeyword;
import gama.core.runtime.GAMA;
import gama.core.util.GamaMapFactory;
import gama.core.util.IMap;
import gama.dev.DEBUG;
import gama.gaml.compilation.ast.ISyntacticElement;
import gama.gaml.descriptions.IDescription;
import gama.gaml.descriptions.ModelDescription;
import gama.gaml.descriptions.ValidationContext;
import gaml.compiler.gaml.documentation.GamlResourceDocumenter;
import gaml.compiler.gaml.indexer.GamlResourceIndexer;
import gaml.compiler.gaml.parsing.GamlSyntacticConverter;
import gaml.compiler.gaml.validation.IGamlBuilderListener;

/**
 * The Class GamlResourceServices.
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamlResourceServices {

	static {
		DEBUG.OFF();
	}

	/** The resource count. */
	private static int resourceCount = 0;

	/** The documenter. */
	private static GamlResourceDocumenter documenter = new GamlResourceDocumenter();

	/** The converter. */
	private static GamlSyntacticConverter converter = new GamlSyntacticConverter();

	/** The Constant resourceListeners. */
	private static final Map<URI, IGamlBuilderListener> resourceListeners = GamaMapFactory.createUnordered();

	/** The Constant resourceErrors. */
	private static final Map<URI, ValidationContext> resourceErrors = GamaMapFactory.createUnordered();

	/** The Constant cachedResourceContents. */
	private static final Map<URI, ISyntacticElement> cachedResourceContents = GamaMapFactory.concurrentMap();

	/** The pool set. */
	private static volatile XtextResourceSet poolSet;

	/** The resource set. */
	private static volatile XtextResourceSet resourceSet;

	/**
	 * Properly encodes and partially verifies the uri passed in parameter. In the case of an URI that does not use the
	 * "platform:" scheme, it is first converted into a file URI so that headless operations that do not use a workspace
	 * can still perform correctly
	 *
	 * @param uri
	 * @return null if the parameter is null or does not represent neither a file or a resource, otherwise a properly
	 *         encoded version of the parameter.
	 */
	public static URI properlyEncodedURI(final URI uri) {
		if (uri == null) return null;
		URI uriToReturn = uri;
		if (GAMA.isInHeadLessMode() && !uri.isPlatformResource()) {
			final String filePath = uri.toFileString();
			if (filePath != null) {
				try {
					uriToReturn = URI.createFileURI(new File(filePath).getCanonicalPath());
				} catch (IOException e) {
					e.printStackTrace();
					return uri;
				}
			}
		}
		// return uriToReturn;
		return URI.createURI(uriToReturn.toString(), true);
	}

	/**
	 * Checks if is edited.
	 *
	 * @param uri
	 *            the uri
	 * @return true, if is edited
	 */
	public static boolean isEdited(final URI uri) {
		return resourceListeners.containsKey(uri);
	}

	/**
	 * Checks if is edited.
	 *
	 * @param r
	 *            the r
	 * @return true, if is edited
	 */
	public static boolean isEdited(final Resource r) {
		return isEdited(r.getURI());
	}

	/**
	 * Update state.
	 *
	 * @param uri
	 *            the uri
	 * @param model
	 *            the model
	 * @param newState
	 *            the new state
	 * @param status
	 *            the status
	 */
	public static void updateState(final URI uri, final ModelDescription model, final boolean newState,
			final ValidationContext status) {
		// DEBUG.LOG("Beginning updating the state of editor in
		// ResourceServices for " + uri.lastSegment());
		final URI newURI = uri;

		final IGamlBuilderListener listener = resourceListeners.get(newURI);
		if (listener == null) return;
		// DEBUG.LOG("Finishing updating the state of editor for " +
		// uri.lastSegment());
		final Iterable exps = model == null ? newState ? Collections.EMPTY_SET : null
				: Iterables.filter(model.getExperiments(), each -> !each.isAbstract());
		listener.validationEnded(model, exps, status);
	}

	/**
	 * Adds the resource listener.
	 *
	 * @param uri
	 *            the uri
	 * @param listener
	 *            the listener
	 */
	public static void addResourceListener(final URI uri, final IGamlBuilderListener listener) {
		final URI newURI = uri; // properlyEncodedURI(uri);
		resourceListeners.put(newURI, listener);
	}

	/**
	 * Removes the resource listener (happens when a file ceases being edited). Invalidates both the documentation
	 * already collected and the collection of this documentation by the validation context if it exists
	 *
	 * @param listener
	 *            the listener
	 */
	public static void removeResourceListener(final IGamlBuilderListener listener) {
		final Iterator<Map.Entry<URI, IGamlBuilderListener>> it = resourceListeners.entrySet().iterator();
		while (it.hasNext()) {
			final Map.Entry<URI, IGamlBuilderListener> entry = it.next();
			if (entry.getValue() == listener) {
				getResourceDocumenter().invalidate(entry.getKey());
				ValidationContext vc = GamlResourceServices.getValidationContext(entry.getKey());
				if (vc != null) { vc.shouldDocument(false); }
				it.remove();

				return;
			}
		}
	}

	/**
	 * Gets the validation context or creates it for the specified resource
	 *
	 * @param r
	 *            the r
	 * @return the validation context
	 */
	public static ValidationContext getOrCreateValidationContext(final GamlResource r) {
		final URI newURI = r.getURI();
		if (!resourceErrors.containsKey(newURI)) {
			resourceErrors.put(newURI, new ValidationContext(newURI, r.hasErrors(), getResourceDocumenter()));
		}
		final ValidationContext result = resourceErrors.get(newURI);
		return result;
	}

	/**
	 * Gets the validation context of an URI or null if not already created
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param newURI
	 *            the new URI
	 * @return the validation context
	 * @date 30 déc. 2023
	 */
	public static ValidationContext getValidationContext(final URI newURI) {
		return resourceErrors.get(newURI);
	}

	/**
	 * Discard validation context.
	 *
	 * @param r
	 *            the r
	 */
	public static void discardValidationContext(final GamlResource r) {
		resourceErrors.remove(r.getURI());
	}

	/**
	 * Returns the path from the root of the workspace
	 *
	 * @return an IPath. Never null.
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
			e.printStackTrace();
		}
		return path;

	}

	/**
	 * Gets the model path of.
	 *
	 * @param r
	 *            the r
	 * @return the model path of
	 */
	public static String getModelPathOf(final Resource r) {
		// Likely in a headless scenario (w/o workspace)
		if (r.getURI().isFile()) return new Path(r.getURI().toFileString()).toOSString();
		final IPath path = getPathOf(r);
		final IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
		final IPath fullPath = file.getLocation();
		return fullPath == null ? "" : fullPath.toOSString();
	}

	/**
	 * Checks if is project.
	 *
	 * @param f
	 *            the f
	 * @return true, if is project
	 */
	private static boolean isProject(final File f) {
		final String[] files = f.list();
		if (files != null) { for (final String s : files) { if (".project".equals(s)) return true; } }
		return false;
	}

	/**
	 * Gets the project path of.
	 *
	 * @param r
	 *            the r
	 * @return the project path of
	 */
	public static String getProjectPathOf(final Resource r) {
		if (r == null) return "";
		final URI uri = r.getURI();
		if (uri == null) return "";
		// Cf. #2983 -- we are likely in a headless scenario
		if (!uri.isFile()) {
			final IPath path = getPathOf(r);
			final IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
			final IPath fullPath = file.getProject().getLocation();
			return fullPath == null ? "" : fullPath.toOSString();
		}
		File project = new File(uri.toFileString());
		while (project != null && !isProject(project)) { project = project.getParentFile(); }
		return project == null ? "" : project.getAbsolutePath();
	}

	// AD The removal of synchronized solves an issue where threads at startup would end up waiting for
	/**
	 * Gets the temporary resource.
	 *
	 * @param existing
	 *            the existing
	 * @return the temporary resource
	 */
	// GamlResourceServices to become free
	public/* synchronized */ static GamlResource getTemporaryResource(final IDescription existing) {
		ResourceSet rs = null;
		GamlResource r = null;
		if (existing != null) {
			final ModelDescription desc = existing.getModelDescription();
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
		// TODO Modifier le cache de la resource ici ?
		final GamlResource result = (GamlResource) rs.createResource(uri);
		final IMap<URI, String> imports = GamaMapFactory.create();
		imports.put(uri, null);
		if (r != null) {
			imports.put(r.getURI(), null);
			final Map<URI, String> uris = GamlResourceIndexer.allImportsOf(r);
			imports.putAll(uris);
		}
		// result.setImports(imports);
		result.getCache().getOrCreate(result).set(GamlResourceIndexer.IMPORTED_URIS, imports);
		return result;
	}

	/**
	 * Discard temporary resource.
	 *
	 * @param temp
	 *            the temp
	 */
	public static void discardTemporaryResource(final GamlResource temp) {
		try {
			temp.delete(null);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the resource documenter.
	 *
	 * @return the resource documenter
	 */
	public static GamlResourceDocumenter getResourceDocumenter() { return documenter; }

	/**
	 * Builds the syntactic contents.
	 *
	 * @param r
	 *            the r
	 * @return the i syntactic element
	 */
	public static ISyntacticElement buildSyntacticContents(final GamlResource r) {
		// DEBUG.OUT("Building contents for " + r.getURI().lastSegment());
		return converter.buildSyntacticContents(r.getParseResult().getRootASTElement(), null);
	}

	/**
	 * Gets the pool set.
	 *
	 * @return the pool set
	 */
	private static XtextResourceSet getPoolSet() {
		if (poolSet == null) {
			// AD Synchronized necessary for Gama Server messages that necessitate the compilation of expressions
			poolSet = new SynchronizedXtextResourceSet() {
				{
					setClasspathURIContext(GamlResourceServices.class);
				}
			};
		}
		return poolSet;
	}

	/**
	 * Gets the resource set.
	 *
	 * @return the resource set
	 */
	static XtextResourceSet getResourceSet() {
		// AD synchronized as a measure of precaution against parallel compilation
		if (resourceSet == null) { resourceSet = new SynchronizedXtextResourceSet(); }
		return resourceSet;
	}

	/**
	 * Equals.
	 *
	 * @param uri1
	 *            the uri 1
	 * @param uri2
	 *            the uri 2
	 * @return true, if successful
	 */
	public static boolean equals(final URI uri1, final URI uri2) {
		if (uri1 == null) return uri2 == null;
		if (uri2 == null) return false;
		return properlyEncodedURI(uri1).equals(properlyEncodedURI(uri2));
	}

	/**
	 * Gets the or create syntactic contents.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param uri
	 *            the uri
	 * @return the or create syntactic contents
	 * @date 1 janv. 2024
	 */
	public static ISyntacticElement getOrCreateSyntacticContents(final URI uri) {
		// ISyntacticElement existing = cachedResourceContents.get(uri);
		// if (existing != null) return existing;
		try {
			final GamlResource r = (GamlResource) getResourceSet().getResource(uri, true);
			ISyntacticElement result = buildSyntacticContents(r);
			// cachedResourceContents.put(uri, result);
			return result;
		} finally {
			clearResourceSet(getResourceSet());
		}
	}

	/**
	 * Invalidate syntactic contents.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param uri
	 *            the uri
	 * @date 2 janv. 2024
	 */
	public static void invalidateSyntacticContents(final URI uri) {
		ISyntacticElement existing = cachedResourceContents.remove(uri);
		if (existing != null) { existing.dispose(); }
	}

	/**
	 * Clear resource set.
	 *
	 * @param resourceSet
	 *            the resource set
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
