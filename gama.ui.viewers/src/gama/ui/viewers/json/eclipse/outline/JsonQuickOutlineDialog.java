/*******************************************************************************************************
 *
 * JsonQuickOutlineDialog.java, in gama.ui.viewers, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.viewers.json.eclipse.outline;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import gama.ui.viewers.json.FilterPatternMatcher;
import gama.ui.viewers.json.eclipse.AbstractFilterableTreeQuickDialog;
import gama.ui.viewers.json.eclipse.AbstractTreeViewerFilter;
import gama.ui.viewers.json.eclipse.JsonEditor;
import gama.ui.viewers.json.eclipse.JsonEditorActivator;
import gama.ui.viewers.json.outline.Item;
import gama.ui.viewers.json.outline.ItemTextMatcher;

/**
 * This dialog is inspired by: <a href=
 * "https://github.com/eclipse/eclipse.jdt.ui/blob/master/org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/text/AbstractInformationControl.java">org.eclipse.jdt.internal.ui.text.AbstractInformationControl</a>
 * and <a href=
 * "https://github.com/eclipse/eclipse.jdt.ui/blob/master/org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/text/JavaOutlineInformationControl.java">org.eclipse.jdt.internal.ui.text.JavaOutlineInformationControl</a>
 *
 * @author Albert Tregnaghi
 *
 */
public class JsonQuickOutlineDialog extends AbstractFilterableTreeQuickDialog<Item> {

	/** The Constant MIN_WIDTH. */
	private static final int MIN_WIDTH = 400;

	/** The Constant MIN_HEIGHT. */
	private static final int MIN_HEIGHT = 300;

	/** The editor. */
	private final JsonEditor editor;

	/**
	 * Creates a quick outline dialog
	 *
	 * @param adaptable
	 *            an adapter which should be able to provide a tree content provider and gradle editor. If gradle editor
	 *            is not set a selected item will only close the dialog but do not select editor parts..
	 * @param parent
	 *            shell to use is null the outline will have no content! If the gradle editor is null location setting
	 *            etc. will not work.
	 * @param infoText
	 *            information to show at bottom of dialog
	 */
	public JsonQuickOutlineDialog(final IAdaptable adaptable, final Shell parent, final String infoText) {
		super(adaptable, parent, "Json quick outline", MIN_WIDTH, MIN_HEIGHT, infoText);
		this.editor = adaptable.getAdapter(JsonEditor.class);
	}

	@Override
	protected ITreeContentProvider createTreeContentProvider(final IAdaptable adaptable) {
		return adaptable.getAdapter(ITreeContentProvider.class);
	}

	@Override
	protected void openSelectionImpl(final ISelection selection, final String filterText) {
		if (editor == null) return;
		JsonEditorContentOutlinePage outlinePage = editor.getOutlinePage();
		boolean outlineAvailable = outlinePageVisible(outlinePage);
		if (outlineAvailable) {
			/*
			 * select part in editor - grab focus not necessary, because this will close quick outline dialog as well,
			 * so editor will get focus back
			 */
			editor.openSelectedTreeItemInEditor(selection, false);
		} else {
			outlinePage.setSelection(selection);
		}

	}

	/**
	 * Outline page visible.
	 *
	 * @param outlinePage
	 *            the outline page
	 * @return true, if successful
	 */
	protected boolean outlinePageVisible(final JsonEditorContentOutlinePage outlinePage) {
		Control control = outlinePage.getControl();
		/* when control is not available - means outline view is not visible, */
		boolean controlAvailable = control == null || control.isDisposed() || !control.isVisible();
		return controlAvailable;
	}

	@Override
	protected AbstractUIPlugin getUIPlugin() {
		JsonEditorActivator editorActivator = JsonEditorActivator.getDefault();
		return editorActivator;
	}

	@Override
	protected Item getInitialSelectedItem() {
		if (editor == null) return null;
		Item item = editor.getItemAtCarretPosition();
		return item;
	}

	@Override
	protected FilterPatternMatcher<Item> createItemMatcher() {
		return new ItemTextMatcher();
	}

	@Override
	protected IBaseLabelProvider createLabelProvider() {
		JsonEditorOutlineLabelProvider labelProvider = new JsonEditorOutlineLabelProvider();
		return new DelegatingStyledCellLabelProvider(labelProvider);
	}

	@Override
	protected AbstractTreeViewerFilter<Item> createFilter() {
		return new ItemTextViewerFilter();
	}

}
