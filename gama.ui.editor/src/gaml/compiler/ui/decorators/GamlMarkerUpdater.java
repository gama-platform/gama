/*******************************************************************************************************
 *
 * GamlMarkerUpdater.java, in gama.ui.shared.modeling, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ui.decorators;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.builder.builderState.MarkerUpdaterImpl;
import org.eclipse.xtext.resource.IResourceDescription.Delta;
import org.eclipse.xtext.ui.markers.IMarkerContributor;
import org.eclipse.xtext.ui.resource.IStorage2UriMapper;
import org.eclipse.xtext.ui.validation.IResourceUIValidatorExtension;
import org.eclipse.xtext.ui.validation.MarkerEraser;
import org.eclipse.xtext.util.Pair;
import org.eclipse.xtext.validation.CheckMode;

import com.google.inject.Inject;

import gama.dev.DEBUG;

/**
 * Manages the lifecycle of error and warning markers in GAML resource files.
 * 
 * <p>This class is responsible for synchronizing Eclipse markers with validation results
 * from GAML resources. It updates or removes markers based on resource changes detected
 * in the build process.
 * 
 * <p>Key responsibilities include:
 * <ul>
 * <li>Creating or updating markers when resources are added or modified</li>
 * <li>Removing markers when resources are deleted</li>
 * <li>Delegating to validation extensions for custom marker management</li>
 * <li>Respecting cancellation requests during long-running operations</li>
 * </ul>
 * 
 * <p>The updater works in conjunction with:
 * <ul>
 * <li>{@link IResourceUIValidatorExtension} - for validation-related markers</li>
 * <li>{@link IMarkerContributor} - for custom marker contributions</li>
 * <li>{@link MarkerEraser} - for marker cleanup when extensions are not available</li>
 * </ul>
 *
 * @author drogoul
 * @since 4 sept. 2016
 * @see MarkerUpdaterImpl
 * @see IResourceUIValidatorExtension
 * @see IMarkerContributor
 */
public class GamlMarkerUpdater extends MarkerUpdaterImpl {

	/** 
	 * Maps storage objects (like IFile) to their corresponding EMF URIs.
	 * Injected by the dependency injection framework.
	 */
	@Inject IStorage2UriMapper mapper;

	/** 
	 * Provides fallback marker deletion functionality when no custom validator extension is available.
	 * Injected by the dependency injection framework.
	 */
	@Inject MarkerEraser eraser;

	/**
	 * Updates or removes markers on workspace files based on resource changes.
	 * 
	 * <p>This method is called by the Xtext builder infrastructure when a resource change is detected.
	 * It determines whether markers should be updated (for new/modified resources) or deleted 
	 * (for removed resources) and delegates to the appropriate handler.
	 * 
	 * <p>The method processes all storage locations (typically IFile instances) associated with
	 * the resource URI. For each file, it either:
	 * <ul>
	 * <li>Updates markers if the resource is new or modified (delta.getNew() != null)</li>
	 * <li>Deletes markers if the resource has been removed (delta.getNew() == null)</li>
	 * </ul>
	 * 
	 * <p>The operation respects cancellation requests via the progress monitor to allow
	 * users to interrupt long-running marker updates.
	 * 
	 * @param delta describes the resource change, including the URI and whether the resource
	 *              is new, modified, or deleted
	 * @param resourceSet the resource set containing the resources to validate, may be null
	 *                    for deleted resources
	 * @param monitor progress monitor to report progress and check for cancellation
	 */
	@Override
	public void updateMarkers(final Delta delta, final ResourceSet resourceSet, final IProgressMonitor monitor) {
		if (monitor.isCanceled()) return;

		final URI uri = delta.getUri();
		final IResourceUIValidatorExtension validatorExtension = getResourceUIValidatorExtension(uri);
		final IMarkerContributor markerContributor = getMarkerContributor(uri);
		final boolean isNewResource = delta.getNew() != null;

		for (final Pair<IStorage, IProject> pair : mapper.getStorages(uri)) {
			if (monitor.isCanceled()) return;
			
			if (!(pair.getFirst() instanceof IFile file)) continue;

			if (isNewResource) {
				updateMarkersForNewResource(file, uri, resourceSet, validatorExtension, markerContributor, monitor);
			} else {
				deleteMarkersForRemovedResource(file, validatorExtension, markerContributor, monitor);
			}
		}
	}

	/**
	 * Updates markers for a new or modified resource.
	 * 
	 * <p>This method loads the resource from the resource set and delegates marker creation
	 * to the appropriate extensions:
	 * <ul>
	 * <li>The validator extension updates validation markers (errors, warnings, infos)</li>
	 * <li>The marker contributor adds custom markers specific to GAML</li>
	 * </ul>
	 * 
	 * <p>If the resource set is null or the resource cannot be loaded, the method returns
	 * early without updating markers.
	 * 
	 * @param file the workspace file to update markers on
	 * @param uri the EMF URI of the resource
	 * @param resourceSet the resource set containing the resource, may be null
	 * @param validatorExtension the validator extension for validation markers, may be null
	 * @param markerContributor the contributor for custom markers, may be null
	 * @param monitor progress monitor to check for cancellation
	 */
	private void updateMarkersForNewResource(final IFile file, final URI uri, final ResourceSet resourceSet,
			final IResourceUIValidatorExtension validatorExtension, final IMarkerContributor markerContributor,
			final IProgressMonitor monitor) {
		if (resourceSet == null) return;
		
		final Resource resource = resourceSet.getResource(uri, true);
		if (resource == null) return;

		if (validatorExtension != null) {
			validatorExtension.updateValidationMarkers(file, resource, CheckMode.NORMAL_AND_FAST, monitor);
		}
		if (markerContributor != null) {
			markerContributor.updateMarkers(file, resource, monitor);
		}
	}

	/**
	 * Deletes markers for a removed resource.
	 * 
	 * <p>This method removes all markers associated with a file that has been deleted or
	 * removed from the workspace. It delegates marker deletion to the appropriate extensions:
	 * <ul>
	 * <li>The validator extension deletes validation markers if available</li>
	 * <li>If no validator extension is available, the {@link MarkerEraser} performs the deletion</li>
	 * <li>The marker contributor deletes custom markers if available</li>
	 * <li>If no marker contributor is available, directly deletes markers of type {@link IMarkerContributor#MARKER_TYPE}</li>
	 * </ul>
	 * 
	 * <p>This two-tiered approach (extension or fallback) ensures markers are properly cleaned up
	 * even when custom extensions are not configured.
	 * 
	 * @param file the workspace file to delete markers from
	 * @param validatorExtension the validator extension for validation markers, may be null
	 * @param markerContributor the contributor for custom markers, may be null
	 * @param monitor progress monitor to check for cancellation
	 */
	private void deleteMarkersForRemovedResource(final IFile file,
			final IResourceUIValidatorExtension validatorExtension, final IMarkerContributor markerContributor,
			final IProgressMonitor monitor) {
		if (validatorExtension != null) {
			validatorExtension.deleteValidationMarkers(file, CheckMode.NORMAL_AND_FAST, monitor);
		} else {
			eraser.deleteValidationMarkers(file, CheckMode.NORMAL_AND_FAST, monitor);
		}

		if (markerContributor != null) {
			markerContributor.deleteMarkers(file, monitor);
		} else {
			try {
				file.deleteMarkers(IMarkerContributor.MARKER_TYPE, true, IResource.DEPTH_ZERO);
			} catch (final CoreException e) {
				DEBUG.ERR(e.getMessage());
			}
		}
	}

}
