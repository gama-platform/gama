/*******************************************************************************************************
 *
 * WrappedContainer.java, in gama.ui.navigator.view, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.navigator.view.contents;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import gama.api.utils.files.IFileMetadataProvider;
import gama.dev.DEBUG;
import gama.workspace.metadata.FileMetaDataProvider;
import one.util.streamex.StreamEx;

/**
 * The Class WrappedContainer.
 *
 * @param <C>
 *            the generic type
 */
public abstract class WrappedContainer<C extends IContainer> extends WrappedResource<VirtualContent<?>, C> {

	/** The models count. */
	int modelsCount = NOT_COMPUTED;

	/** The suffix. */
	String suffix = null;

	/** The children. */
	Object[] children;

	/**
	 * Instantiates a new wrapped container.
	 *
	 * @param root
	 *            the root
	 * @param wrapped
	 *            the wrapped
	 */
	public WrappedContainer(final VirtualContent<?> root, final C wrapped) {
		super(root, wrapped);
	}

	/**
	 * Initialize children.
	 */
	public void initializeChildren() {
		invalidateModelsCount();
		invalidateSeverity();
		if (children != null) { loadChildren(); }
	}

	private void loadChildren() {
		if (!isOpen()) {
			children = EMPTY;
			return;
		}
		try {
			final IResource[] cc = getResource().members();
			final int size = cc.length;
			if (size == 0) {
				children = EMPTY;
			} else {
				children = StreamEx.of(cc).map(r -> getManager().wrap(this, r)).toArray(WrappedResource.class);
			}
		} catch (final CoreException e) {
			DEBUG.ERR("Unable to load navigator children of " + getResource().getFullPath(), e);
			children = EMPTY;
		}
	}

	@Override
	public boolean hasChildren() {
		return isOpen() && getNavigatorChildren().length > 0;
	}

	@Override
	public Object[] getNavigatorChildren() {
		if (children == null) { loadChildren(); }
		return children;
	}

	@Override
	public int countModels() {
		if (modelsCount == NOT_COMPUTED) {
			modelsCount = 0;
			suffix = null;
			if (isOpen()) {
				try {
					getResource().accept(r -> {
						if (r instanceof IFile file && isModel(file)) { modelsCount++; }
						return true;
					});
				} catch (final CoreException e) {
					modelsCount = NOT_COMPUTED;
					throw new IllegalStateException("Unable to count navigator models in " + getResource().getFullPath(), e);
				}
				if (modelsCount > 0) { suffix = "" + modelsCount + " model" + (modelsCount > 1 ? "s" : ""); }
			}
		}
		return modelsCount;
	}

	private static boolean isModel(final IFile file) {
		return IFileMetadataProvider.GAML_CT_ID.equals(FileMetaDataProvider.getContentTypeId(file));
	}

	@Override
	public int findMaxProblemSeverity() {
		if (!isOpen()) return CLOSED;
		if (severity == NOT_COMPUTED) {
			int result = NO_PROBLEM;
			try {
				for (final IMarker marker : getResource().findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE)) {
					if (marker.getResource() instanceof IFile file && isModel(file)) {
						result = Math.max(result, marker.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO));
					}
				}
				severity = result;
			} catch (final CoreException e) {
				throw new IllegalStateException("Unable to read navigator markers in " + getResource().getFullPath(), e);
			}
		}
		return severity;
	}

	/**
	 * Invalidate models count.
	 */
	public void invalidateModelsCount() {
		modelsCount = NOT_COMPUTED;
		final Object p = getParent();
		if (p instanceof WrappedContainer) { ((WrappedContainer<?>) p).invalidateModelsCount(); }
	}

	@Override
	public void getSuffix(final StringBuilder sb) {
		countModels();
		if (suffix != null) { sb.append(suffix); }
	}

}
