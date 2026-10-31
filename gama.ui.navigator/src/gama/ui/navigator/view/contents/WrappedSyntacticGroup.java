/*******************************************************************************************************
 *
 * WrappedSyntacticGroup.java, in gama.ui.navigator, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.navigator.view.contents;

import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;

import gama.api.compilation.ast.IGamlLabelProvider.OutlineGroup;
import gama.api.compilation.ast.ISyntacticElement;
import gama.ui.shared.resources.GamaIcon;

/**
 * A folder grouping the syntactic children of a species (or of the model) in the navigator, like in the GAML editor
 * outline (Attributes, Actions, Behaviors, Aspects, Tests...).
 */
public class WrappedSyntacticGroup extends VirtualContent<WrappedSyntacticContent> {

	/** The group. */
	public final OutlineGroup group;

	/** The elements. */
	final List<ISyntacticElement> elements;

	/**
	 * Instantiates a new wrapped syntactic group.
	 *
	 * @param parent
	 *            the parent
	 * @param group
	 *            the group
	 * @param elements
	 *            the elements
	 */
	public WrappedSyntacticGroup(final WrappedSyntacticContent parent, final OutlineGroup group,
			final List<ISyntacticElement> elements) {
		super(parent, group.label);
		this.group = group;
		this.elements = elements;
	}

	@Override
	public boolean hasChildren() {
		return !elements.isEmpty();
	}

	@Override
	public Object[] getNavigatorChildren() {
		return elements.stream().map(e -> new WrappedSyntacticContent(this, e)).toArray();
	}

	@Override
	public ImageDescriptor getImageDescriptor() { return GamaIcon.named("gaml/" + group.icon).descriptor(); }

	@Override
	public void getSuffix(final StringBuilder sb) {}

	@Override
	public int findMaxProblemSeverity() {
		int severity = -1;
		for (final Object o : getNavigatorChildren()) {
			severity = Math.max(severity, ((WrappedSyntacticContent) o).findMaxProblemSeverity());
		}
		return severity;
	}

	@Override
	public ImageDescriptor getOverlay() {
		final int severity = findMaxProblemSeverity();
		if (severity != -1) return DESCRIPTORS.get(severity);
		return null;
	}

	@Override
	public VirtualContentType getType() { return VirtualContentType.GAML_ELEMENT; }

	@Override
	public String getStatusMessage() { return getName(); }

}
