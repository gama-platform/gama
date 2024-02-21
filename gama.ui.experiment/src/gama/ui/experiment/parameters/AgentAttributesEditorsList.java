/*******************************************************************************************************
 *
 * AgentAttributesEditorsList.java, in gama.ui.shared.experiment, is part of the source code of the GAMA modeling and
 * simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.experiment.parameters;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import gama.core.common.interfaces.IKeyword;
import gama.core.common.interfaces.ItemList;
import gama.core.kernel.experiment.IExperimentDisplayable;
import gama.core.kernel.experiment.IParameter;
import gama.core.metamodel.agent.IAgent;
import gama.core.util.GamaColor;
import gama.ui.shared.interfaces.IParameterEditor;
import gama.ui.shared.parameters.EditorFactory;

/**
 * The Class AgentAttributesEditorsList.
 */
public class AgentAttributesEditorsList extends EditorsList<IAgent> {

	/** The Constant DEAD_MARKER. */
	private static final String DEAD_MARKER = " dead at step ";

	/** The Constant AGENT_MARKER. */
	private static final String AGENT_MARKER = "Agent" + ItemList.SEPARATION_CODE;

	/** The Constant HIDDEN. */
	private static final Set<String> HIDDEN =
			new HashSet<>(Arrays.asList(IKeyword.PEERS, IKeyword.MEMBERS, IKeyword.AGENTS));

	@Override
	public String getItemDisplayName(final IAgent ag, final String name) {
		if (name == null) return AGENT_MARKER + ag.getName();
		if (ag.dead() && !name.contains(DEAD_MARKER)) {
			final long cycle = ag.getScope().getClock().getCycle();
			return AGENT_MARKER + ItemList.ERROR_CODE + name.substring(name.indexOf(ItemList.SEPARATION_CODE) + 1)
					+ DEAD_MARKER + cycle;
		}
		return name;
	}

	@Override
	public GamaColor getItemDisplayColor(final IAgent o) {
		return null;
	}

	/**
	 * Adds the.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param params
	 *            the params
	 * @param agent
	 *            the agent
	 * @date 13 août 2023
	 */
	public void add(final Collection<? extends IExperimentDisplayable> params, final IAgent agent) {
		if (addItem(agent) && !agent.dead()) {
			for (final IExperimentDisplayable var : params) {
				if (var instanceof IParameter && !HIDDEN.contains(var.getName())) {
					final IParameterEditor<?> gp = EditorFactory.getInstance().create(agent, (IParameter) var, null);
					sections.get(agent).put(gp.getParam().getName(), gp);
				}
			}
		}
	}

	@Override
	public boolean addItem(final IAgent agent) {
		if (!sections.containsKey(agent)) {
			sections.put(agent, new HashMap<>());
			return true;
		}
		return false;
	}

	@Override
	public void updateItemValues(final boolean synchronously) {
		for (final Map.Entry<IAgent, Map<String, IParameterEditor<?>>> entry : sections.entrySet()) {
			if (!entry.getKey().dead()) {
				for (final IParameterEditor<?> gp : entry.getValue().values()) {
					gp.updateWithValueOfParameter(synchronously, false);
				}

			}
		}
	}

	/**
	 * Method handleMenu()
	 *
	 * @see gama.core.common.interfaces.ItemList#handleMenu(java.lang.Object, int, int)
	 */
	@Override
	public Map<String, Runnable> handleMenu(final IAgent data, final int x, final int y) {
		return null;
	}

}
