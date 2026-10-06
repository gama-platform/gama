package gaml.compiler.ui.editor.toolbar;

import java.nio.file.Path;
import java.nio.file.Files;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.StreamSupport;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.awt.Desktop;
import java.io.IOException;

import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.emf.common.util.URI;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

import gama.api.GAMA;
import gaml.compiler.validation.GamlModelBuilder;
import gama.api.kernel.species.IModelSpecies;
import gama.api.types.file.GenericFile;
import gama.api.kernel.species.IExperimentSpecies;
import gama.api.utils.prefs.GamaPreferences;
import gama.api.utils.GamlProperties;
import gama.ui.shared.utils.WorkbenchHelper;
import gama.ui.shared.views.toolbar.Selector;
import gaml.compiler.ui.editor.GamlEditor;
import gaml.compiler.ui.editor.GamlEditorState;
import gama.export.GamaZipBuilder;
import gama.export.ui.ExportModelDialog;
import gaml.compiler.resource.GamlFileInfo;

public class ExportExperimentSelectionListener implements Selector {

	/** The editor. */
	GamlEditor editor;

	/** The state. */
	GamlEditorState state;

	private boolean exportIsDone = false;

	/**
	 *
	 */
	public ExportExperimentSelectionListener(final GamlEditor editor, final GamlEditorState state) {
		this.editor = editor;
		this.state = state;
	}

	private synchronized boolean getExportIsDone() {
		return exportIsDone;
	}

	private synchronized void setExportIsDoneToTrue() {
		exportIsDone = true;
	}

	/**
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
	public void widgetSelected(final SelectionEvent e) {

		// final IGui gui = GAMA.getRegularGui();
		// We refuse to run if there is no XtextGui available.
		editor.doSave(null);
		if (GamaPreferences.Modeling.EDITOR_SAVE.getValue()) {
			WorkbenchHelper.getPage().saveAllEditors(GamaPreferences.Modeling.EDITOR_SAVE_ASK.getValue());
		}

		final IFile file = ((IFileEditorInput) editor.getEditorInput()).getFile();

		final String relativeModelPathStr = file.getFullPath().toOSString();

		final GamlProperties metaProperties = new GamlProperties();

		final Set<IModelSpecies> alreadyProcessedModels = new HashSet<IModelSpecies>(); 

		List<IFile> modelFiles = new LinkedList<IFile>();
		modelFiles.add(file);

		String[] experimentNames = null;

		final Map<IProject,Set<String>> dataFiles = new HashMap<IProject,Set<String>>();

		final List<IProject> projects = new LinkedList<IProject>();

		Set<String> plugins = new HashSet<String>();

		try {
			boolean isTargetModel = true;

			while (! modelFiles.isEmpty())
			{
				List<IFile> modelFilesToProcess = new LinkedList<IFile>();

				for (IFile modelFile : modelFiles)
				{
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

					GamlFileInfo fileInfo = new GamlFileInfo(modelFile);
					final Path modelFileParent = Path.of(modelFile.getLocation().toOSString()).getParent();
					
					final Set<String> thisProjectDataFiles = dataFiles.getOrDefault(modelFile.getProject(),new HashSet<String>());

					for (final String use : fileInfo.getUses()) {

						thisProjectDataFiles.add(modelFileParent.resolve(use).normalize().toString());
					}

					dataFiles.put(modelFile.getProject(),thisProjectDataFiles);

					plugins.addAll(metaProperties.get(GamlProperties.PLUGINS));
					
					if(isTargetModel)
						experimentNames = StreamSupport.stream(model.getExperiments().spliterator(),false)
							.map(experiment -> experiment.getDescription().getName())
							.toArray(String[]::new);

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
				isTargetModel = false;
			}

				
			final ExportModelDialog dialog = new ExportModelDialog(experimentNames);

			final int result = dialog.open();
        
			if (result != IDialogConstants.OK_ID)
				return;
				
			// Récupération des données via les getters de la classe
			
			final String targetExperiments = String.join("#",dialog.getSelectedExperiments());
			final boolean zipWithJdk = dialog.getIncludeJdk();
			final boolean oneFile = dialog.getOneFile();

			// final String formattedTimestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd.HHmmss"));
			// final String modelName = Path.of(relativeModelPathStr).getFileName().toString().replace(".gaml","");
			// final String outputFileName = modelName  + "-" + targetExperiments + "-" + formattedTimestamp + ".zip";

			final Path outputPath = Path.of(dialog.getOutputPath(),dialog.getOutputFileName());

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
				relativeModelPathStr,
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
					GAMA.getGui().getDialogFactory().error("An error occured while exporting the model.");
				}
			}).start();

			while(!getExportIsDone())
			{
				Thread.sleep(100);
				int progress = ziper.getProgress();
				System.out.println("progress : " + progress + ", done : " + exportIsDone);
				progressIndicator.worked(progress);
				while (display.readAndDispatch()) {
						// Fixes the frozen window by clearing out pending render events
				}
			}
			shell.close();

		} catch (Throwable t) {
            t.printStackTrace();
        }


	}

}
