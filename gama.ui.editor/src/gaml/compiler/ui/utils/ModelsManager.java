/*******************************************************************************************************
 *
 * ModelsManager.java, in gama.ui.editor, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ui.utils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.xtext.ui.editor.IURIEditorOpener;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;

import com.google.inject.Injector;
import com.google.inject.Singleton;

import gama.api.GAMA;
import gama.api.compilation.GamlCompilationError;
import gama.api.compilation.IModelsManager;
import gama.api.constants.GamlFileExtension;
import gama.api.kernel.species.IExperimentSpecies;
import gama.api.kernel.species.IModelSpecies;
import gama.api.utils.files.IFileMetadataProvider;
import gama.api.utils.files.IGamlFileInfo;
import gama.api.utils.tests.TestExperimentSummary;
import gama.core.experiment.TestAgent;
import gama.core.experiment.parameters.ParametersSet;
import gama.dev.DEBUG;
import gama.ui.editor.internal.EditorActivator;
import gama.ui.navigator.view.contents.WrappedGamaFile;
import gama.ui.shared.utils.WorkbenchHelper;
import gaml.compiler.resource.GamlFileInfo;
import gaml.compiler.validation.GamlModelBuilder;

/**
 * The class ModelsManager.
 *
 * @author drogoul
 * @since 19 juin 2016
 *
 */
@Singleton
public class ModelsManager extends AbstractServiceFactory implements IModelsManager {

	/**
	 * Edits the model internal.
	 *
	 * @param eObject
	 *            the e object
	 */
	private void editModelInternal(final Object eObject) {
		if (eObject instanceof URI uri) {
			final Injector injector = EditorActivator.getInstance().getInjector(EditorActivator.GAML_COMPILER_GAML);
			final IURIEditorOpener opener = injector.getInstance(IURIEditorOpener.class);
			opener.open(uri, true);
		} else if (eObject instanceof EObject) {
			editModelInternal(EcoreUtil.getURI((EObject) eObject));
		} else if (eObject instanceof String) {
			final IWorkspace workspace = GAMA.getWorkspaceManager().getWorkspace();
			final IFile file = workspace.getRoot().getFile(new Path((String) eObject));
			editModelInternal(file);
		} else if (eObject instanceof IFile file) {
			if (!file.exists()) {
				DEBUG.LOG("File " + file.getFullPath().toString() + " does not exist in the workspace");
				return;
			}
			try {
				IDE.openEditor(WorkbenchHelper.getPage(), file);
			} catch (final PartInitException e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public void editModel(final Object eObject) {
		WorkbenchHelper.asyncRun(() -> editModelInternal(eObject));
	}

	@SuppressWarnings ("unchecked")
	@Override
	public List<TestExperimentSummary> runHeadlessTests(final Object object) {
		// final StringBuilder sb = new StringBuilder();
		final IModelSpecies model = findModel(object);
		if (model == null) return null;
		final List<String> testExpNames = model.getDescription().getExperimentNames().stream()
				.filter(e -> model.getExperiment(e).isTest()).toList();
		if (testExpNames.isEmpty()) return null;
		final List<TestExperimentSummary> result = new ArrayList<>();
		for (final String expName : testExpNames) {
			final IExperimentSpecies exp = GAMA.addHeadlessExperiment(model, expName, new ParametersSet(), null);
			if (exp != null) {
				exp.setHeadless(true);
				final TestAgent agent = (TestAgent) exp.getAgent();
				// exp.getController().getScheduler().resume();
				agent.step(agent.getScope());
				result.add(agent.getSummary());
				GAMA.closeExperiment(exp);
			}
		}
		return result;
	}

	/**
	 * @param object
	 * @return
	 */
	private IModelSpecies findModel(final Object object) {
		if (object instanceof IModelSpecies) return (IModelSpecies) object;
		if (object instanceof WrappedGamaFile) return findModel(((WrappedGamaFile) object).getResource());
		switch (object) {
			case IFile file -> {
				try {
					if (file.findMaxProblemSeverity(IMarker.PROBLEM, true,
							IResource.DEPTH_ZERO) == IMarker.SEVERITY_ERROR) {
						GAMA.getGui().getDialogFactory()
								.error("Model " + file.getFullPath() + " has errors and cannot be launched");
						return null;
					}
				} catch (final CoreException e) {
					e.printStackTrace();
				}
				final URI uri = URI.createPlatformResourceURI(file.getFullPath().toString(), true);
				return findModel(uri);
			}
			case URI uri -> {
				final List<GamlCompilationError> errors = new ArrayList<>();
				final IModelSpecies model = GamlModelBuilder.getInstance().compile(uri, errors);
				if (model == null) {
					GAMA.getGui().getDialogFactory().error("File " + uri.lastSegment() + " cannot be built because of "
							+ errors.size() + " compilation errors");
				}
				return model;
			}
			case IXtextDocument doc -> {
				IModelSpecies model = null;
				try {
					model = doc.readOnly(state -> GamlModelBuilder.getInstance().compile(state.getURI(), null));
				} catch (final Exception ex) {
					GAMA.getGui().getDialogFactory().error(
							"Experiment cannot be instantiated because of the following error: " + ex.getMessage());
				}
				return model;
			}
			case null, default -> {
			}
		}
		return null;
	}

	@Override
	public void runModel(final Object object, final String exp) {
		final IModelSpecies model = findModel(object);
		if (model == null) return;
		GAMA.runGuiExperiment(exp, model);
	}

	@Override
	public Object create(final Class serviceInterface, final IServiceLocator parentLocator,
			final IServiceLocator locator) {
		return this;
	}

	@Override
	public List<IGamlFileInfo> getAllModels() {
		List<IGamlFileInfo> infos = new ArrayList<>();
		try {
			processContainer(GAMA.getWorkspaceManager().getRoot(), infos);
		} catch (CoreException e) {}
		return infos;
	}

	/**
	 * Process container.
	 *
	 * @param container
	 *            the container
	 * @throws CoreException
	 *             the core exception
	 */
	static void processContainer(final IContainer container, final List<IGamlFileInfo> list) throws CoreException {
		IResource[] members = container.members();
		IFileMetadataProvider provider = GAMA.getMetadataProvider();
		for (IResource member : members) {
			if (member instanceof IContainer) {
				processContainer((IContainer) member, list);
			} else if (member instanceof IFile && GamlFileExtension.isGaml(member.getName())) {
				GamlFileInfo data = (GamlFileInfo) provider.getMetaData(member, true, true);
				// in case the data is not compatible anymore
				if (data.uri == null || data.uri.isEmpty() || data.getName() == null) { provider.refreshAllMetaData(); }
				list.add((GamlFileInfo) provider.getMetaData(member, true, true));
			}
		}
	}

}
