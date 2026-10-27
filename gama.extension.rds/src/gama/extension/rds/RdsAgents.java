/*******************************************************************************************************
 *
 * RdsAgents.java, in gama.extension.rds, is part of the source code of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.rds;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.renjin.sexp.ListVector;
import org.renjin.sexp.SEXP;

import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.Types;
import gama.api.kernel.agent.IAgent;
import gama.api.runtime.scope.IScope;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.types.misc.IContainer;
import gama.gaml.statements.SaveStatement;

/** Agent collections are exported as dataframes, not restorable agents. */
final class RdsAgents {

	private RdsAgents() {}

	static boolean isAgentCollection(final Object value) {
		if (!(value instanceof IContainer<?, ?> container)) return false;
		if (container.getGamlType().getContentType().isAgentType()) return true;
		return value instanceof IList<?> list && list.stream().anyMatch(IAgent.class::isInstance);
	}

	static SEXP snapshot(final IScope scope, final IContainer<?, ?> container, final Object attributes) {
		final List<IAgent> agents = new ArrayList<>();
		for (final Object value : container.iterable(scope)) {
			if (!(value instanceof IAgent agent)) throw RdsConverter.error(scope, "Agent collections cannot contain nil or non-agents");
			if (!agents.isEmpty() && agent.getSpecies() != agents.get(0).getSpecies())
				throw RdsConverter.error(scope, "Agent collections must contain a single species");
			agents.add(agent);
		}
		final var species = agents.isEmpty() ? container.getGamlType().getContentType().getSpecies()
				: agents.get(0).getSpecies().getDescription();
		final Map<String, IExpression> selected = new LinkedHashMap<>();
		if (attributes instanceof IExpression.Map expressions) {
			for (final var entry : expressions.getElements().entrySet()) {
				if (!(entry.getKey().value(scope) instanceof String name) || name.isEmpty())
					throw RdsConverter.error(scope, "Attribute column names must be non-empty strings");
				if (selected.put(name, entry.getValue()) != null)
					throw RdsConverter.error(scope, "Duplicate attribute column: " + name);
			}
		} else if (attributes instanceof IExpression expression) {
			final Object evaluated = expression.value(scope);
			if (!(evaluated instanceof IList<?> names)) throw RdsConverter.error(scope, "attributes must be a list of names or a literal map");
			for (final Object name : names) {
				if (!(name instanceof String text) || text.isEmpty() || selected.containsKey(text))
					throw RdsConverter.error(scope, "Attribute names must be unique, non-empty strings");
				selected.put(text, null);
			}
		} else if (attributes != null) {
			throw RdsConverter.error(scope, "Unsupported attributes selection");
		} else {
			if (species != null) {
				for (final String name : species.getAttributeNames()) {
					if (!SaveStatement.NON_SAVEABLE_ATTRIBUTE_NAMES.contains(name)
							&& !List.of("name", "x", "y", "z").contains(name)) selected.put(name, null);
				}
			}
		}
		final ListVector.NamedBuilder frame = new ListVector.NamedBuilder();
		final IList<Object> names = GamaListFactory.create(Types.STRING);
		final IList<Object> x = GamaListFactory.create(Types.FLOAT);
		final IList<Object> y = GamaListFactory.create(Types.FLOAT);
		final IList<Object> z = GamaListFactory.create(Types.FLOAT);
		for (final IAgent agent : agents) {
			names.add(agent.getName());
			x.add(agent.getLocation().getX());
			y.add(agent.getLocation().getY());
			z.add(agent.getLocation().getZ());
		}
		frame.add("name", RdsConverter.toR(scope, names));
		frame.add("x", RdsConverter.toR(scope, x));
		frame.add("y", RdsConverter.toR(scope, y));
		frame.add("z", RdsConverter.toR(scope, z));
		for (final var entry : selected.entrySet()) {
			if (List.of("name", "x", "y", "z").contains(entry.getKey()))
				throw RdsConverter.error(scope, "Attribute column conflicts with a snapshot column: " + entry.getKey());
			final var attribute = species == null ? null : species.getAttribute(entry.getKey());
			if (entry.getValue() == null && attribute == null)
				throw RdsConverter.error(scope, "Unknown agent attribute: " + entry.getKey());
			final IList<Object> values = GamaListFactory.create(entry.getValue() == null
					? attribute.getGamlType() : entry.getValue().getGamlType());
			for (final IAgent agent : agents) {
				values.add(entry.getValue() == null ? agent.getDirectVarValue(scope, entry.getKey())
						: scope.evaluate(entry.getValue(), agent).getValue());
			}
			frame.add(entry.getKey(), RdsConverter.toR(scope, values));
		}
		return RdsConverter.withRows(frame.build(), agents.size());
	}
}
