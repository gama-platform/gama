/*******************************************************************************************************
 *
 * ExportProjectAsSimulation.java, in gama.ui.navigator.commands, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.ui.navigator.commands;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.stream.StreamSupport;
import java.util.stream.Collectors;
import java.awt.Desktop;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import org.eclipse.emf.common.util.URI;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.resources.IContainer;
import org.eclipse.jface.dialogs.IDialogConstants;

import gaml.compiler.validation.GamlModelBuilder;
import gaml.compiler.resource.GamlFileInfo;
import gama.api.kernel.species.IModelSpecies;
import gama.api.utils.GamlProperties;
import gama.api.constants.GamlFileExtension;
import gama.api.utils.files.IFileMetadataProvider;
import gama.api.GAMA;
import gama.export.ui.ExportModelDialog;
import gama.export.GamaZipBuilder;
import gama.export.ExportHelper;

/**
 * Exports a whole project as a simulation launcher. 
 */
public class ExportProjectAsSimulation extends AbstractHandler {

    private static final String contextualSeparator = "    from model    ";

	private boolean exportIsDone = false;

	private synchronized boolean getExportIsDone() {
		return exportIsDone;
	}

	private synchronized void setExportIsDoneToTrue() {
		exportIsDone = true;
	}

	/**
	 * Process container.
	 *
	 * @param container
	 *            the container
	 */
	public static void getModelsFromProject(final IContainer container, final List<IFile> list) throws CoreException {
		IResource[] members = container.members();
		IFileMetadataProvider provider = GAMA.getMetadataProvider();
		for (IResource member : members) {
			if (member instanceof IContainer) {
				getModelsFromProject((IContainer) member, list);
			} else if (member instanceof IFile && GamlFileExtension.isGaml(member.getName())) {
				list.add((IFile) member);
			}
		}
	}

	/**
	 * Orchestrates all the export logic and ui
	 * from the set of exported model files
	 * 
	 * @param modeilFiles
	 *            List<IFile> of the models to be exported
	 */
	public void export(List<IFile> modelFiles)
    {
		final Set<String> plugins = new HashSet<String>(); 

		final List<String> experimentNames = new ArrayList<String>(); 

		final Map<IProject,Set<String>> dataFiles = new HashMap<IProject,Set<String>>();

		final Set<IModelSpecies> alreadyProcessedModels = new HashSet<IModelSpecies>();

		final List<IProject> projects = new LinkedList<IProject>();

        final boolean exportFromProject =  modelFiles.size() != 1;

		String relativeModelPath = "";

		if (! exportFromProject)
			relativeModelPath = modelFiles.get(0).getFullPath().toOSString();

        try {
			boolean isModelFromTargetProject = true;
			while (! modelFiles.isEmpty())
			{
				List<IFile> modelFilesToProcess = new LinkedList<IFile>();

				for (IFile modelFile : modelFiles)
				{
					final GamlProperties metaProperties = new GamlProperties();

					final IModelSpecies model = GamlModelBuilder
						.getInstance()
							.compile(URI.createPlatformResourceURI(
								modelFile.getProject().getName() + "/" 
								+ modelFile.getProjectRelativePath().toString(),true)
							,null);

					if (alreadyProcessedModels.contains(model))
						continue;
						
					alreadyProcessedModels.add(model);
					
					if (! projects.contains(modelFile.getProject()))
						projects.add(modelFile.getProject());

					model.getDescription().collectMetaInformation(metaProperties);
					
					plugins.addAll(metaProperties.get(GamlProperties.PLUGINS));

					if(isModelFromTargetProject)
						experimentNames.addAll(
							StreamSupport.stream(
								model.getExperiments().spliterator(),false
							)
							.map(exportFromProject ? 
								experiment -> experiment.getDescription().getName() 
											  + contextualSeparator 
											  + modelFile.getFullPath().toOSString()
								: experiment -> experiment.getDescription().getName())
							.toList()
						);

					// Gather the data files imported by this model and resolve them
					// to absolute paths so the builder can embed the external ones.
					final GamlFileInfo fileInfo = new GamlFileInfo(modelFile);
					final Path modelFileParent = Path.of(modelFile.getLocation().toOSString()).getParent();

					final Set<String> thisProjectDataFiles = dataFiles.getOrDefault(modelFile.getProject(),new HashSet<String>());
					
					for (final String use : fileInfo.getUses()) {
						thisProjectDataFiles.add(modelFileParent.resolve(use).normalize().toString());
					}

					dataFiles.put(modelFile.getProject(),thisProjectDataFiles);
					
					for (final String importedModelUriStr : fileInfo.getImports())
					{
						IPath importedModelPath;

						if(importedModelUriStr.startsWith("/resource"))
							importedModelPath = org.eclipse.core.runtime.Path.fromOSString(
								URI.createPlatformResourceURI(importedModelUriStr.substring(9),false)
									.toPlatformString(true)
								);
						else
							importedModelPath = modelFile.getFullPath()
								.removeLastSegments(1)
								.append(URI.decode(importedModelUriStr));
					
						IFile importedModelFile = ResourcesPlugin.getWorkspace().getRoot()
												.getFile(importedModelPath);

						modelFilesToProcess.add(importedModelFile);
					}
				}

				modelFiles = modelFilesToProcess;
				isModelFromTargetProject = false;
			}
			
			final ExportModelDialog dialog = new ExportModelDialog(experimentNames.toArray(String[]::new));
			final int result = dialog.open();
        
			if (result != IDialogConstants.OK_ID)
				return;

			final Path outputPath = Path.of(dialog.getOutputPath(),dialog.getOutputFileName());

			final boolean zipWithJdk = dialog.getIncludeJdk();
			final boolean oneFile = dialog.getOneFile();
	
			String[] formattedtargetExperiments = dialog.getSelectedExperiments();
			
			if (exportFromProject)
			{
				if(formattedtargetExperiments.length == 1)
				{
					// fallback to single model export
					int lastIndex = formattedtargetExperiments[0].lastIndexOf(contextualSeparator);
					relativeModelPath = 
						formattedtargetExperiments[0]
							.substring(lastIndex + contextualSeparator.length());

					formattedtargetExperiments[0] = formattedtargetExperiments[0].substring(0,lastIndex);
				} else
					// "prey_predator from model testmodel" becomes "prey_predator@testmodel"
					formattedtargetExperiments = Arrays.stream(dialog.getSelectedExperiments()).map(label -> {
						int lastIndex = label.lastIndexOf(contextualSeparator);

						if ((lastIndex) == -1)
							return "";
			
						return label.substring(0,lastIndex) + "@" + label.substring(lastIndex + contextualSeparator.length());
					}).toArray(String[]::new);
			}
			// else
			// 	formattedtargetExperiments = dialog.getSelectedExperiments();


			// adding experiments separators
			final String targetExperiments = String.join("#",formattedtargetExperiments);

			// handling the progressbar
			Display display = Display.getDefault();

			if (display == null || display.isDisposed())
				return;

			Shell shell = new Shell(display, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
			shell.setText("information");
			shell.setSize(350, 120);
			shell.setLayout(new GridLayout(1, false));

			Label statusLabel = new Label(shell, SWT.NONE);
			statusLabel.setText("The export is being created.");

			ProgressIndicator progressIndicator = new ProgressIndicator(shell, SWT.NONE);
			progressIndicator.setLayoutData(new org.eclipse.swt.layout.GridData(SWT.FILL, SWT.CENTER, true, false));

			Shell workbenchShell = null;
			if (PlatformUI.isWorkbenchRunning()) {
				IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if (activeWindow != null) {
					workbenchShell = activeWindow.getShell();
				}
			}

			// position the progress indicator relative to the main window
			if (workbenchShell != null && !workbenchShell.isDisposed()) {
				int x = workbenchShell.getLocation().x + (workbenchShell.getSize().x - shell.getSize().x) / 2;
				int y = workbenchShell.getLocation().y + (workbenchShell.getSize().y - shell.getSize().y) / 2;
				shell.setLocation(x, y);
			}


			int totalWork = 1526;

			if (zipWithJdk)
				totalWork += 582;

			progressIndicator.beginTask(totalWork);
			shell.open();

			final GamaZipBuilder ziper = new GamaZipBuilder(
				plugins,
				projects,
				relativeModelPath,
				targetExperiments,
				dataFiles,
				zipWithJdk,
				oneFile);

			new Thread(() -> {
				try { 
					ziper.zip(outputPath.toString());
					setExportIsDoneToTrue();
					System.out.println("Model exported successfully");
					if(
						Desktop.isDesktopSupported()
					    &&
						GAMA.getGui()
							.getDialogFactory()
								.question("Model export successful","Do you want to show the target directory ?")
					)
					{
						Desktop desktop = Desktop.getDesktop();
						try {
							desktop.open(outputPath.getParent().toFile());
						} catch (IOException ioe) {
							ioe.printStackTrace();
						}
					}

				} catch (Exception exception) {
					setExportIsDoneToTrue();
					System.err.println("Exception raised while cloning GAMA :\n" + exception);
					exception.printStackTrace();
					String message = "An error occured while exporting the ";
					message = message + (exportFromProject ? "project." : "model.");
					GAMA.getGui().getDialogFactory().error(message);
				}
			}).start();

			int workDone = 0;
			while(!getExportIsDone())
			{
				Thread.sleep(100);
				int progress = ziper.getProgress();
				if (progress > 0)
				{
					progressIndicator.worked(progress);
					workDone += progress;
					System.out.println("copied files : " + workDone + "/" + totalWork);
				}
				while (display.readAndDispatch()) {
						// Fixes the frozen window by clearing out pending render events
				}
			}
			shell.close();

		} catch (Throwable t) {
            t.printStackTrace();
        }
    }

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {

		IProject project = ExportHelper.getProjectFromEvent(event);

		if (project == null)
			return null;

		List<IFile> modelFiles = new LinkedList<IFile>();
		try {
			getModelsFromProject(project,modelFiles);
		} catch (Throwable t) {
			t.printStackTrace();
			GAMA.getGui()
				.getDialogFactory()
					.error("An error occured while resolving the models of the target project.");
		}
		
		export(modelFiles);

		return null;
	}
}