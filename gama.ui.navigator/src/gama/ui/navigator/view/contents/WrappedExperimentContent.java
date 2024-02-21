/*******************************************************************************************************
 *
 * WrappedExperimentContent.java, in gama.ui.navigator.view, is part of the source code of the GAMA modeling and
 * simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.navigator.view.contents;

import org.eclipse.jface.resource.ImageDescriptor;

import gama.core.common.interfaces.IGamlLabelProvider;
import gama.core.runtime.GAMA;
import gama.core.util.file.GamlFileInfo;
import gama.gaml.compilation.ast.ISyntacticElement;
import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.utils.WorkbenchHelper;

/**
 * The Class WrappedExperimentContent.
 */
public class WrappedExperimentContent extends WrappedSyntacticContent {

	/** Not used if an EObject is passed to the Wrapper */
	String icon, expName;

	/**
	 * Instantiates a new wrapped experiment content.
	 *
	 * @param file
	 *            the file
	 * @param e
	 *            the e
	 */
	public WrappedExperimentContent(final WrappedGamaFile file, final ISyntacticElement e) {
		super(file, e, WorkbenchHelper.getService(IGamlLabelProvider.class).getText(e));
		expName = e.getName();
	}

	/**
	 * Instantiates a new wrapped experiment content.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param file
	 *            the file
	 * @param e
	 *            the e
	 * @date 2 janv. 2024
	 */
	public WrappedExperimentContent(final WrappedGamaFile file, final String e) {
		super(file, null, "Experiment " + e.replace(GamlFileInfo.BATCH_PREFIX, ""));
		boolean isBatch = e.startsWith(GamlFileInfo.BATCH_PREFIX);
		icon = isBatch ? "gaml/_batch" : "gaml/_gui";
		expName = e.replace(GamlFileInfo.BATCH_PREFIX, "");
	}

	@Override
	public WrappedGamaFile getFile() { return (WrappedGamaFile) getParent(); }

	@Override
	public boolean handleDoubleClick() {
		GAMA.getGui().runModel(getParent(), expName);
		return true;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return element == null ? GamaIcon.named(icon).descriptor()
				: (ImageDescriptor) WorkbenchHelper.getService(IGamlLabelProvider.class).getImageDescriptor(element);
	}

	@Override
	public void getSuffix(final StringBuilder sb) {
		sb.append("Double-click to run");
	}

}