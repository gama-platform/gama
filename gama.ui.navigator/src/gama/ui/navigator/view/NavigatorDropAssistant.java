/*******************************************************************************************************
 *
 * NavigatorDropAssistant.java, in gama.ui.navigator.view, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.ui.navigator.view;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.navigator.CommonDropAdapter;
import org.eclipse.ui.navigator.CommonDropAdapterAssistant;
import org.eclipse.ui.part.ResourceTransfer;

import gama.ui.navigator.view.actions.PasteAction;
import gama.ui.navigator.view.contents.NavigatorRoot;

/**
 * The Class NavigatorDropAssistant.
 */
public class NavigatorDropAssistant extends CommonDropAdapterAssistant {

	/**
	 * Instantiates a new navigator drop assistant.
	 */
	public NavigatorDropAssistant() {}

	@Override
	public IStatus validateDrop(final Object target, final int operation, final TransferData transferType) {
		return target instanceof NavigatorRoot ? Status.OK_STATUS : Status.CANCEL_STATUS;
	}

	@Override
	public IStatus handleDrop(final CommonDropAdapter adapter, final DropTargetEvent event, final Object target) {
		if (FileTransfer.getInstance().isSupportedType(event.currentDataType)) {
			final String[] files = (String[]) event.data;
			if (files != null && files.length > 0) {
				PasteAction.handlePaste(files);
				return Status.OK_STATUS;
			}
		} else if (ResourceTransfer.getInstance().isSupportedType(event.currentDataType)) {

		}
		return Status.CANCEL_STATUS;
	}

	@Override
	public boolean isSupportedType(final TransferData aTransferType) {
		return super.isSupportedType(aTransferType) || FileTransfer.getInstance().isSupportedType(aTransferType);
	}

}
