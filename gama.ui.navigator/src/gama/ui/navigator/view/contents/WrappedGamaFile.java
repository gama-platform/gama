/*******************************************************************************************************
 *
 * WrappedGamaFile.java, in gama.ui.navigator.view, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.navigator.view.contents;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.URI;

import gama.core.common.GamlFileExtension;
import gama.core.common.interfaces.IKeyword;
import gama.core.runtime.GAMA;
import gama.core.util.GamaMapFactory;
import gama.core.util.IMap;
import gama.core.util.file.GamlFileInfo;
import gama.core.util.file.IGamaFileMetaData;
import gama.gaml.compilation.GAML;
import gama.gaml.compilation.ast.ISyntacticElement;
import gama.gaml.descriptions.IExpressionDescription;
import gama.ui.navigator.view.NavigatorContentProvider;
import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.resources.IGamaIcons;
import gama.ui.shared.utils.PreferencesHelper;
import one.util.streamex.StreamEx;

/**
 * The Class WrappedGamaFile.
 */
public class WrappedGamaFile extends WrappedFile {

	/** The is experiment. */
	boolean isExperiment;

	/** The uri problems. */
	IMap<String, Integer> uriProblems;

	/**
	 * Instantiates a new wrapped gama file.
	 *
	 * @param root
	 *            the root
	 * @param wrapped
	 *            the wrapped
	 */
	public WrappedGamaFile(final WrappedContainer<?> root, final IFile wrapped) {
		super(root, wrapped);
		computeURIProblems();
	}

	/**
	 * Compute URI problems.
	 */
	public void computeURIProblems() {
		try {
			uriProblems = null;
			final IMarker[] markers = getResource().findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_ZERO);
			for (final IMarker marker : markers) {
				final String s = marker.getAttribute("URI_KEY", "UNKNOWN");
				final int severity = marker.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
				if (uriProblems == null) { uriProblems = GamaMapFactory.createUnordered(); }
				uriProblems.put(s, severity);
			}
		} catch (final CoreException ce) {}

	}

	@Override
	public boolean canBeDecorated() {
		return true;
	}

	@Override
	public boolean hasChildren() {
		return true;
	}

	@Override
	public Object[] getNavigatorChildren() {
		if (NavigatorContentProvider.FILE_CHILDREN_ENABLED) return getFileChildren();
		return EMPTY;
	}

	@Override
	public boolean isGamaFile() { return true; }

	@Override
	public int countModels() {
		return 1;
	}

	@Override
	protected void computeFileImage() {
		// final IFile f = getResource();
		if (isExperiment) {
			image = GamaIcon.named(IGamaIcons.FILE_EXPERIMENT).descriptor();
		} else {
			image = GamaIcon.named(IGamaIcons.FILE_MODEL).descriptor();
		}

	}

	@Override
	protected void computeFileType() {
		final IFile f = getResource();
		isExperiment = GamlFileExtension.isExperiment(f.getName());
	}

	/**
	 * Checks for tag.
	 *
	 * @param tag
	 *            the tag
	 * @return true, if successful
	 */
	public boolean hasTag(final String tag) {
		final IGamaFileMetaData metaData = GAMA.getGui().getMetaDataProvider().getMetaData(getResource(), false, false);
		// DEBUG.LOG("Tags of " + getName() + ": " + ((GamlFileInfo) metaData).getTags());
		if (metaData instanceof GamlFileInfo) {
			for (final String t : ((GamlFileInfo) metaData).getTags()) { if (t.contains(tag)) return true; }
		}
		return false;
	}

	@Override
	public Object[] getFileChildren() {
		final IGamaFileMetaData metaData = GAMA.getGui().getMetaDataProvider().getMetaData(getResource(), false, false);
		if (metaData instanceof GamlFileInfo info) {
			final List<VirtualContent<?>> l = new ArrayList<>();
			if (PreferencesHelper.NAVIGATOR_OUTLINE.getValue()) {
				final String path = getResource().getFullPath().toOSString();
				final ISyntacticElement element = GAML.getContents(URI.createPlatformResourceURI(path, true));
				if (element != null) {
					if (!GamlFileExtension.isExperiment(path)) { l.add(new WrappedModelContent(this, element)); }
					element.visitExperiments(exp -> {
						final IExpressionDescription d = exp.getExpressionAt(IKeyword.VIRTUAL);
						if (d == null || !d.equalsString("true")) { l.add(new WrappedExperimentContent(this, exp)); }
					});
				}
			} else {
				for (String exp : info.getExperiments()) { l.add(new WrappedExperimentContent(this, exp)); }
			}
			if (!info.getImports().isEmpty()) {
				final Category wf = new Category(this, info.getImports(), "Imports");
				if (wf.getNavigatorChildren().length > 0) { l.add(wf); }
			}
			if (!info.getUses().isEmpty()) {
				final Category wf = new Category(this, info.getUses(), "Uses");
				if (wf.getNavigatorChildren().length > 0) { l.add(wf); }
			}
			if (!info.getTags().isEmpty()) {
				final Tags wf =
						new Tags(this, StreamEx.of(info.getTags()).toMap(s -> "Double-click to search"), "Tags", true);
				if (wf.getNavigatorChildren().length > 0) { l.add(wf); }
			}
			return l.toArray();
		}
		return VirtualContent.EMPTY;
	}

	/**
	 * Gets the URI problem.
	 *
	 * @param uri
	 *            the uri
	 * @return the URI problem
	 */
	public int getURIProblem(final URI uri) {

		if (uri == null || uriProblems == null) return -1;
		final String fragment = uri.toString();
		final int[] severity = { -1 };
		uriProblems.forEachPair((s, arg1) -> {
			if (s.startsWith(fragment)) {
				severity[0] = arg1;
				return false;
			}
			return true;
		});
		return severity[0];

	}

}
