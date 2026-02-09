/*******************************************************************************************************
 *
 * GamaWizardPage.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.parameters;

import java.util.List;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import gama.api.data.factories.GamaMapFactory;
import gama.api.data.objects.IMap;
import gama.api.gaml.symbols.IParameter;
import gama.api.runtime.scope.IScope;
import gama.ui.shared.interfaces.EditorListener;

/**
 * The Class GamaWizardPage.
 */
public class GamaWizardPage extends WizardPage {

	/** The values. */
	private final IMap<String, Object> values = GamaMapFactory.createUnordered();

	/** The parameters. */
	private final List<IParameter> parameters;

	/** The scope. */
	private final IScope scope;

	/** The wizard. */
	private GamaWizard wizard = null;

	/**
	 * Instantiates a new gama wizard page.
	 *
	 * @param scope
	 *            the scope
	 * @param parameters
	 *            the parameters
	 * @param title
	 *            the title
	 * @param description
	 *            the description
	 * @param font
	 *            the font
	 */
	public GamaWizardPage(final IScope scope, final List<IParameter> parameters, final String title,
			final String description) {
		super(title);
		setTitle(title);
		setDescription(description);
		this.scope = scope;
		this.parameters = parameters;
		parameters.forEach(p -> { values.put(p.getName(), p.getInitialValue(scope)); });

	}

	@Override
	public GamaWizard getWizard() { return wizard; }

	/**
	 * Sets the wizard.
	 *
	 * @param wizard
	 *            the new wizard
	 */
	public void setWizard(final GamaWizard wizard) { this.wizard = wizard; }

	@Override
	public void createControl(final Composite parent) {
		EditorsGroup composite = new EditorsGroup(parent, SWT.NONE);
		// AD The application of font cannot work at this level. It must be passed down to the editor controls.
		// Font f = null;
		// if (font != null) {
		// f = new Font(WorkbenchHelper.getDisplay(), font.getFontName(), font.getSize(), font.getStyle());
		// }
		parameters.forEach(param -> {
			final EditorListener<?> listener = newValue -> {
				param.setValue(scope, newValue);
				values.put(param.getName(), newValue);
				if (wizard != null) {
					boolean canFinish = wizard.canFinish();
					setPageComplete(canFinish);
				}

			};
			EditorFactory.create(scope, composite, param, listener, false);
		});
		composite.layout();
		setControl(composite);

		// if (font != null) {
		// composite.setFont(
		// new Font(WorkbenchHelper.getDisplay(), font.getFontName(), font.getSize(), font.getStyle()));
		// }

	}

	/**
	 * Gets the values.
	 *
	 * @return the values
	 */
	public IMap<String, Object> getValues() { return values; }

	@Override
	public boolean isPageComplete() { return true; }

}
