/*******************************************************************************************************
 *
 * SelectExperiment.java, in gama.ui.shared.experiment, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.ui.experiment.commands;

import java.util.Map;
import java.nio.file.Path;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.awt.Desktop;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.swt.widgets.Display;

import gama.export.ui.SaveSimulationsArtifactsDialog;
import gama.ui.application.workbench.StartupModelHelper;
import gama.api.GAMA;

/**
 * The Class SaveSimulationsArtifacts is meant to save the embedded workspace data in a	directory specified
 * by the user. This is especially needed if the simulation has been started from a single executable file. 
 */
public class SaveSimulationsArtifacts extends AbstractHandler implements IElementUpdater {

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		boolean areThereAnyArtifactsToSave;

		// safely check if there are artifacts to save
		try {
			areThereAnyArtifactsToSave = StartupModelHelper.getInstance().areThereAnyArtifactsToSave();
		} catch (IOException e){
			System.out.println("An error occured while checking simulation artifacts to save : ");
			e.printStackTrace();
			GAMA.getGui().getDialogFactory().error("An error occured while checking simulation artifacts to save.");
			return Boolean.FALSE;
		}

		// if no artifacts to save, exit with info popup
		if (! areThereAnyArtifactsToSave)
		{
			System.out.println("No new artifacts worth saving have been found.");
			return Boolean.TRUE;
		}

		// there are artifacts to save
		final SaveSimulationsArtifactsDialog dialog = new SaveSimulationsArtifactsDialog();
		final int result = dialog.open();

		if (result != IDialogConstants.OK_ID) return Boolean.FALSE;

		final Path targetSavePath;
		try {
			targetSavePath = Path.of(dialog.getOutputPath()).resolve(dialog.getOutputDirectoryName());
		} catch (RuntimeException e) {
			GAMA.getGui().getDialogFactory().error("The simulation artifacts destination is invalid.");
			return Boolean.FALSE;
		}
		try {
			new ProgressMonitorDialog(Display.getDefault().getActiveShell()).run(true, true,
					monitor -> {
						try {
							StartupModelHelper.getInstance().saveSimulationArtifacts(targetSavePath, monitor);
						} catch (IOException e) {
							throw new InvocationTargetException(e);
						}
					});
		} catch (InterruptedException e) {
			return Boolean.FALSE;
		} catch (InvocationTargetException e) {
			if (e.getCause() instanceof OperationCanceledException) return Boolean.FALSE;
			System.out.println("An error occured while saving simulation artifacts : ");
			if (e.getCause() != null) e.getCause().printStackTrace();
			GAMA.getGui().getDialogFactory().error("An error occured while saving simulation artifacts.");
			return Boolean.FALSE;
		} catch (RuntimeException e) {
			System.out.println("An error occured while saving simulation artifacts : ");
			e.printStackTrace();
			GAMA.getGui().getDialogFactory().error("An error occured while saving simulation artifacts.");
			return Boolean.FALSE;
		}

		System.out.println("Simulation artifacts saved successfully.");
		if (Desktop.isDesktopSupported() && dialog.getOpenFileExplorer()) {
			try {
				Desktop.getDesktop().open(targetSavePath.toFile());
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
		return Boolean.TRUE;
	}

	@Override
	public void updateElement(final UIElement element, final Map parameters) {
	}

}
