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
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;
import java.io.IOException;
import java.awt.Desktop;
import java.lang.Iterable;

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

		Path projectPath = Path.of(StartupModelHelper.getInstance().getModel().getProjectPath());
		final SaveSimulationsArtifactsDialog dialog = new SaveSimulationsArtifactsDialog();
		final int result = dialog.open();
		
		if (result != IDialogConstants.OK_ID)
			return null;	
	
		Path outputParentDirectory = Path.of(dialog.getOutputPath());
		String outputDirectoryName = dialog.getOutputDirectoryName();

		Path targetSavePath = outputParentDirectory.resolve(outputDirectoryName);

		new Thread(() -> {

			try(Stream<Path> stream = Files.walk(projectPath))
			{
				Files.createDirectories(targetSavePath);
				
				for (Path path : (Iterable<Path>) stream::iterator) {
						Path targetPath = targetSavePath.resolve(projectPath.relativize(path));

						if (targetPath.getParent() != null)
							Files.createDirectories(targetPath.getParent());

						if(! Files.isDirectory(targetPath))
							Files.copy(path,targetPath,StandardCopyOption.REPLACE_EXISTING);
				}

			
				System.out.println("Simulation artifacts saved successfully.");
				
				if(
					Desktop.isDesktopSupported()
					&&
					GAMA.getGui()
						.getDialogFactory()
							.question("Simulation artifacts save successful","Do you want to show the target directory ?")
				)
				{
					Desktop desktop = Desktop.getDesktop();
					try {
						desktop.open(targetSavePath.toFile());
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}

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

