/*******************************************************************************************************
 *
 * NavigatorSorter.java, in gama.ui.navigator.view, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.ui.navigator.view;

import java.text.Collator;

import org.eclipse.jface.viewers.ViewerComparator;

import gama.core.common.interfaces.IKeyword;
import gama.ui.navigator.view.contents.Category;
import gama.ui.navigator.view.contents.WrappedExperimentContent;
import gama.ui.navigator.view.contents.WrappedSyntacticContent;

/**
 * The Class NavigatorSorter.
 */
public class NavigatorSorter extends ViewerComparator {

	/**
	 * Instantiates a new navigator sorter.
	 */
	public NavigatorSorter() {
	}

	/**
	 * Instantiates a new navigator sorter.
	 *
	 * @param collator the collator
	 */
	public NavigatorSorter(final Collator collator) {
		super(collator);
	}

	@Override
	public int category(final Object e2) {
		if (e2 instanceof Category) {
			return -1;
		}
		if (e2 instanceof WrappedExperimentContent) {
			return 100;
		}
		if (e2 instanceof WrappedSyntacticContent) {
			final WrappedSyntacticContent w = (WrappedSyntacticContent) e2;
			if (w.element.isSpecies() && w.element.getKeyword().equals(IKeyword.GRID)) {
				return 0;
			}
			if (w.element.isSpecies()) {
				return 1;
			}
			if (!w.element.hasChildren()) {
				return 2;
			}
			if (w.element.hasChildren() && w.element.getKeyword().equals(IKeyword.ACTION)) {
				return 3;
			}
			return 4;
		}
		return 0;
	}

}
