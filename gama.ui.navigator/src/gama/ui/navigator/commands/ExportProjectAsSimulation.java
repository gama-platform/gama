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
import java.nio.file.Path;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.StreamSupport;
import java.util.stream.Collectors;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.ui.handlers.HandlerUtil;

import gaml.compiler.validation.GamlModelBuilder;
import gama.api.kernel.species.IModelSpecies;
import gama.api.utils.GamlProperties;
import gama.api.constants.GamlFileExtension;
import gama.api.utils.files.IFileMetadataProvider;
import gama.api.GAMA;
import gama.export.ui.ExportModelDialog;
import gama.export.GamaZipBuilder;

/**
 * Exports a whole project as a simulation launcher. 
 */
public class ExportProjectAsSimulation extends AbstractHandler {

	private final static String contextualSeparator = "    from model    ";


	public static IProject getProjectFromEvent(final ExecutionEvent event) {
        ISelection selection = HandlerUtil.getCurrentSelection(event);
		IProject project = null;
        
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection) selection;
            Object firstElement = structuredSelection.getFirstElement();
            
            if (firstElement != null) {
                
                if (firstElement instanceof IProject) {
                    project = (IProject) firstElement;
                } 
                else if (firstElement instanceof IAdaptable) {
                    IResource resource = ((IAdaptable) firstElement).getAdapter(IResource.class);
                    if (resource != null) {
                        project = resource.getProject();
                    }
                }
            }
        }
		return project;
	} 


	/**
	 * Process container.
	 *
	 * @param container
	 *            the container
	 * @throws CoreException
	 *             the core exception
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

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {

		IProject project = getProjectFromEvent(event);

		if (project == null)
			return null;

		final Set<String> plugins = new HashSet<String>(); 
		
		final List<String> experimentNames = new ArrayList<String>(); 

		try {
			final List<IFile> modelFiles = new ArrayList<>();
			getModelsFromProject(project,modelFiles);

			for (IFile modelFile : modelFiles)
			{
				final GamlProperties metaProperties = new GamlProperties();

				final IModelSpecies model = GamlModelBuilder.getInstance().compile(modelFile.getLocation().toFile(),null,metaProperties);

				plugins.addAll(metaProperties.get(GamlProperties.PLUGINS));

				experimentNames.addAll(
					StreamSupport.stream(
						model.getExperiments().spliterator(),false
					)
					.map(experiment -> experiment.getDescription().getName() + contextualSeparator + modelFile.getFullPath().toOSString())
					.toList()
				);

			}
			
			final ExportModelDialog dialog = new ExportModelDialog(experimentNames.toArray(String[]::new));

			final int result = dialog.open();
        
			if (result != IDialogConstants.OK_ID)
				return null;

			final Path outputPath = Path.of(dialog.getOutputPath(),dialog.getOutputFileName());

			// "prey_predator from model testmodel" becomes "prey_predator@testmodel"
			final String[] formattedtargetExperiments = Arrays.stream(dialog.getSelectedExperiments()).map(label -> {
				int lastIndex = label.lastIndexOf(contextualSeparator);

				if ((lastIndex) == -1)
					return "";
	
				return label.substring(0,lastIndex) + "@" + label.substring(lastIndex + contextualSeparator.length());
			}).toArray(String[]::new);

			// adding experiments separators
			final String targetExperiments = String.join("#",formattedtargetExperiments);

			final GamaZipBuilder ziper = new GamaZipBuilder(
				plugins,
				project.getLocation().toOSString(),
				"",
				targetExperiments);

			try { 
				ziper.zip(outputPath.toString());
				System.out.println("Model exported successfully");
			} catch (Exception exception) {
				System.err.println("Exception raised while cloning GAMA :\n" + exception);
				exception.printStackTrace();
			}

		} catch (Throwable t) {
            t.printStackTrace();
        }

		return null;
	}
}