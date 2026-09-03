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
import java.awt.Desktop;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;

import gama.export.ExportHelper;
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

		final SaveSimulationsArtifactsDialog dialog = new SaveSimulationsArtifactsDialog();
		final int result = dialog.open();

		if (result != IDialogConstants.OK_ID)
			return null;	

		new Thread(() -> {
			try
			{
				if(StartupModelHelper.getInstance().areThereAnyArtifactsToSave())
				{	
					final Path outputParentDirectory = Path.of(dialog.getOutputPath());
					final String outputDirectoryName = dialog.getOutputDirectoryName();
					final boolean openFileExplorer = dialog.getOpenFileExplorer(); 

					final Path targetSavePath = outputParentDirectory.resolve(outputDirectoryName);

					StartupModelHelper.getInstance().saveSimulationArtifacts(targetSavePath);
					System.out.println("Simulation artifacts saved successfully.");
					
					if(Desktop.isDesktopSupported() && openFileExplorer)
					{
						Desktop desktop = Desktop.getDesktop();
						try {
							desktop.open(targetSavePath.toFile());
						} catch (IOException ioe) {
							ioe.printStackTrace();
						}
					}
				} else {
					
					System.out.println("No new artifacts worth saving have been found.");
					
					if(Desktop.isDesktopSupported())
						GAMA.getGui()
							.getDialogFactory()
								.inform("No new artifacts worth saving have been found.");
				}

			} catch (IOException e) {
				System.out.println("An error occured while saving simulation artifacts : ");
				e.printStackTrace();
				GAMA.getGui().getDialogFactory().error("An error occured while saving simulation artifacts.");
			}
		}).start();
		

		return null;
	}

	@Override
	public void updateElement(final UIElement element, final Map parameters) {
	}

}

