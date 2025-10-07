/*******************************************************************************************************
 *
 * ModelRunner.java, in gama.ui.editor, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ui.utils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
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

import gama.core.kernel.experiment.IExperimentPlan;
import gama.core.kernel.experiment.ParametersSet;
import gama.core.kernel.experiment.TestAgent;
import gama.core.kernel.model.IModel;
import gama.core.runtime.GAMA;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.dev.DEBUG;
import gama.gaml.compilation.GamlCompilationError;
import gama.gaml.statements.test.TestExperimentSummary;
import gama.ui.editor.internal.EditorActivator;
import gama.ui.navigator.view.contents.WrappedGamaFile;
import gama.ui.shared.interfaces.IModelRunner;
import gama.ui.shared.utils.WorkbenchHelper;
import gaml.compiler.gaml.validation.GamlModelBuilder;

/**
 * The class ModelRunner.
 *
 * @author drogoul
 * @since 19 juin 2016
 *
 */
@Singleton
public class ModelRunner extends AbstractServiceFactory implements IModelRunner {

	/**
	 * Edits the model internal.
	 *
	 * @param eObject
	 *            the e object
	 */
	private void editModelInternal(final Object eObject) {

		switch (eObject) {
			case null -> {
				DEBUG.LOG("Cannot edit a null object");
				return;
			}
			case URI uri -> {
				final Injector injector = EditorActivator.getInstance().getInjector(EditorActivator.GAML_COMPILER_GAML);
				final IURIEditorOpener opener = injector.getInstance(IURIEditorOpener.class);
				opener.open(uri, true);
			}
			case EObject eObj -> {
				if (eObj.eResource() == null) {
					DEBUG.LOG("Cannot edit an EObject that is not contained in a resource: " + eObj);
					return;
				}
				editModelInternal(EcoreUtil.getURI(eObj));
			}
			case String str -> {
				if (str.isEmpty()) {
					DEBUG.LOG("Cannot edit an empty string");
					return;
				}
				final IWorkspace workspace = ResourcesPlugin.getWorkspace();
				final IFile file = workspace.getRoot().getFile(new Path(str));
				editModelInternal(file);
			}
			case IFile file -> {
				if (!file.exists()) {
					DEBUG.LOG("Cannot edit a file that does not exist: " + file.getFullPath());
					return;
				}
				try {
					IDE.openEditor(WorkbenchHelper.getPage(), file);
				} catch (final PartInitException e) {
					e.printStackTrace();
				}
			}

			default -> DEBUG.LOG("Cannot edit nsupported object type: " + eObject.getClass().getName());
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
		final IModel model = findModel(object);
		if (model == null) return null;
		final List<String> testExpNames = model.getDescription().getExperimentNames().stream()
				.filter(e -> model.getExperiment(e).isTest()).toList();
		if (testExpNames.isEmpty()) return null;
		final List<TestExperimentSummary> result = new ArrayList<>();
		for (final String expName : testExpNames) {
			final IExperimentPlan exp = GAMA.addHeadlessExperiment(model, expName, new ParametersSet(), null);
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
	private IModel findModel(final Object object) {
		switch (object) {
			case null -> {
				DEBUG.LOG("Cannot find a model from a null object");
				return null;
			}
			case IModel model -> {
				return model;
			}
			case WrappedGamaFile wrapped -> {
				return findModel(wrapped.getResource());
			}
			case IFile file -> {
				try {
					if (file.findMaxProblemSeverity(IMarker.PROBLEM, true,
							IResource.DEPTH_ZERO) == IMarker.SEVERITY_ERROR) {
						GAMA.getGui().error("Model " + file.getFullPath() + " has errors and cannot be launched");
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
				final IModel model = GamlModelBuilder.getDefaultInstance().compile(uri, errors);
				if (model == null) {
					GAMA.getGui().error("File " + uri.lastSegment() + " cannot be built because of " + errors.size()
							+ " compilation errors");
				}
				return model;
			}
			case IXtextDocument doc -> {
				IModel model = null;
				try {
					model = doc.readOnly(state -> GamlModelBuilder.getDefaultInstance().compile(state.getURI(), null));
				} catch (final GamaRuntimeException ex) {
					GAMA.getGui().error(
							"Experiment cannot be instantiated because of the following error: " + ex.getMessage());
				}
				return model;
			}
			default -> {
				DEBUG.LOG("Cannot find a model from an object of type: " + object.getClass().getName());
				return null;
			}
		}

	}

	@Override
	public void runModel(final Object object, final String exp) {
		final IModel model = findModel(object);
		if (model == null) return;
		GAMA.runGuiExperiment(exp, model);
	}

	@Override
	public Object create(final Class serviceInterface, final IServiceLocator parentLocator,
			final IServiceLocator locator) {
		return this;
	}

}
