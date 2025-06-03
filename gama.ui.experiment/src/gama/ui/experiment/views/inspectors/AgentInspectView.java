/*******************************************************************************************************
 *
 * AgentInspectView.java, in gama.ui.experiment, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.experiment.views.inspectors;

import static gama.ui.shared.resources.GamaColors.getTextColorForBackground;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;

import gama.core.common.interfaces.IGui;
import gama.core.kernel.experiment.IParameter;
import gama.core.kernel.experiment.ITopLevelAgent;
import gama.core.kernel.experiment.ParameterAdapter;
import gama.core.metamodel.agent.IAgent;
import gama.core.outputs.IOutput;
import gama.core.outputs.InspectDisplayOutput;
import gama.core.runtime.IScope;
import gama.gaml.types.Types;
import gama.ui.experiment.menus.AgentsMenu;
import gama.ui.experiment.parameters.AgentAttributesEditorsList;
import gama.ui.shared.controls.FlatButton;
import gama.ui.shared.controls.ParameterExpandBar;
import gama.ui.shared.controls.ParameterExpandItem;
import gama.ui.shared.resources.GamaColors.GamaUIColor;
import gama.ui.shared.resources.IGamaColors;
import gama.ui.shared.views.toolbar.IToolbarDecoratedView;

/**
 * The Class AgentInspectView.
 */
public class AgentInspectView extends AttributesEditorsView<IAgent> implements
	IToolbarDecoratedView.Pausable /* implements GamaSelectionListener */ {

    /** The Constant ID. */
    public static final String ID = IGui.AGENT_VIEW_ID;

    /** The first part name. */
    public String firstPartName = null;

    @Override
    public void addOutput(final IOutput output) {

	if (output == null) {
	    reset();
	    return;
	}
	if (!(output instanceof InspectDisplayOutput out)) {
	    return;
	}
	final IAgent[] agents = out.getLastValue();
	if (agents == null || agents.length == 0) {
	    reset();
	    return;
	}

	final IAgent agent = agents[0];
	if (getParentComposite() == null) {
	    super.addOutput(out);
	} else if (editors == null || !editors.getSections().containsKey(agent)) {
	    super.addOutput(out);
	    addItem(agent);
	}
    }

    @Override
    public void ownCreatePartControl(final Composite parent) {
	// DEBUG.LOG("Inspector creating its own part control");
	parent.setBackground(parent.getBackground());
	if (!outputs.isEmpty()) {
	    final IAgent[] init = getOutput().getLastValue();
	    if (init != null && init.length > 0) {
		for (final IAgent a : init) {
		    addItem(a);
		}
	    }
	}
    }

    @Override
    public InspectDisplayOutput getOutput() {
	return (InspectDisplayOutput) super.getOutput();
    }

    @Override
    public boolean areItemsClosable() {
	return true;
    }

    /**
     * Creates the left label.
     *
     * @param parent
     *            the parent
     * @param title
     *            the title
     * @param tooltip
     *            the tooltip
     * @param isSubParameter
     *            the is sub parameter
     * @return the label
     */
    public Label createLeftLabel(final Composite parent, final String title, final String tooltip,
	    final boolean isSubParameter) {
	final Label label = new Label(parent, SWT.WRAP | SWT.RIGHT);
	// label.setBackground(parent.getBackground());
	label.setForeground(getTextColorForBackground(parent.getBackground()).color());
	final GridData d = new GridData(SWT.END, SWT.CENTER, true, true);
	d.minimumWidth = 70;
	d.horizontalIndent = isSubParameter ? 30 : 0;
	label.setLayoutData(d);
	label.setText(title);
	label.setToolTipText(tooltip);
	return label;
    }

    @Override
    protected Composite createItemContentsFor(final IAgent agent) {
	final Composite attributes = super.createItemContentsFor(agent);
	createLeftLabel(attributes, "Actions", "", false);
	final Composite composite = new Composite(attributes, SWT.NONE);
	composite.setBackground(attributes.getBackground());
	final GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
	data.minimumWidth = 150;
	composite.setLayoutData(data);

	final GridLayout layout = new GridLayout(2, false);
	layout.marginWidth = 5;

	composite.setLayout(layout);
	final FlatButton b = FlatButton.menu(composite, IGamaColors.BLUE, "Select...");
	b.setSelectionListener(e -> {
	    final Menu m = new Menu(b);
	    AgentsMenu.createMenuForAgent(m, agent, agent instanceof ITopLevelAgent, false);
	    m.setVisible(true);
	});

	return attributes;
    }

    @Override
    public boolean addItem(final IAgent agent) {
	if (editors == null) {
	    editors = new AgentAttributesEditorsList();
	}
	updatePartName();
	if (!editors.getSections().containsKey(agent)) {
	    ((AgentAttributesEditorsList) editors).add(getParametersToInspect(agent), agent);
	    final ParameterExpandItem item = createItem(getParentComposite(), agent, true, null);
	    if (item == null) {
		return false;
	    }
	    return true;
	}
	return false;
    }

    @Override
    protected ParameterExpandItem buildConcreteItem(final ParameterExpandBar bar, final IAgent data,
	    final GamaUIColor color) {
	return new ParameterExpandItem(bar, data, SWT.None, 0, color);
    }

    /**
     * Gets the parameters to inspect.
     *
     * @param agent
     *            the agent
     * @return the parameters to inspect
     */
    private List<IParameter> getParametersToInspect(final IAgent agent) {
	final Map<String, String> names = getOutput().getAttributes();
	if (names == null) {
	    return new ArrayList<>(agent.getSpecies().getVars());
	}
	final List<IParameter> params = new ArrayList<>();
	for (final String s : names.keySet()) {
	    if (agent.getSpecies().getVar(s) != null) {
		params.add(agent.getSpecies().getVar(s));
	    } else {
		params.add(buildAttribute(agent, s, names.get(s)));
	    }
	}
	return params;
    }

    /**
     * Builds the attribute.
     *
     * @param agent
     *            the agent
     * @param att
     *            the att
     * @param t
     *            the t
     * @return the i parameter
     */
    private IParameter buildAttribute(final IAgent agent, final String att, final String t) {
	return new ParameterAdapter(att, Types.get(t).id()) {

	    @Override
	    public void setValue(final IScope scope, final Object value) {
		agent.setAttribute(att, value);
	    }

	    @Override
	    public boolean isEditable() {
		return true;
	    }

	    @Override
	    public boolean isDefined() {
		return true;
	    }

	    @Override
	    public Object value() {
		return agent.getAttribute(att);
	    }

	};
    }

    @Override
    public void removeItem(final IAgent a) {
	InspectDisplayOutput found = null;
	for (final IOutput out : outputs) {
	    final InspectDisplayOutput output = (InspectDisplayOutput) out;
	    final IAgent[] agents = output.getLastValue();
	    if (agents != null && agents.length > 0 && agents[0] == a) {
		found = output;
		break;
	    }
	}
	if (found != null) {
	    found.close();
	    removeOutput(found);
	}
	updatePartName();
	super.removeItem(a);
    }

    /**
     * Update part name.
     */
    public void updatePartName() {
	if (firstPartName == null) {
	    final InspectDisplayOutput out = getOutput();
	    firstPartName = out == null ? "Inspect: " : out.getName();
	}
	final Set<String> names = new LinkedHashSet<>();
	for (final IOutput o : outputs) {
	    final InspectDisplayOutput out = (InspectDisplayOutput) o;
	    final IAgent a = out.getLastValue()[0];
	    if (a != null) {
		names.add(a.getName());
	    }
	}
	this.setPartName(firstPartName + " " + (names.isEmpty() ? "" : names.toString()));
    }

    /**
     * Method pauseChanged()
     *
     * @see gama.ui.shared.views.toolbar.IToolbarDecoratedView.Pausable#pauseChanged()
     */
    @Override
    public void pauseChanged() {
    }

    /**
     * Method handleMenu()
     *
     * @see gama.core.common.interfaces.ItemList#handleMenu(java.lang.Object,
     *      int, int)
     */
    @Override
    public Map<String, Runnable> handleMenu(final IAgent data, final int x, final int y) {
	return null;
    }

}
