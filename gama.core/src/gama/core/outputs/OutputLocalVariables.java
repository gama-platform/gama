/*******************************************************************************************************
 *
 * OutputLocalVariables.java, in gama.core, is part of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import gama.annotations.constants.IKeyword;
import gama.api.compilation.descriptions.IDescription;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IVarExpression;
import gama.api.kernel.simulation.ITopLevelAgent;
import gama.api.runtime.scope.IScope;
import gama.api.runtime.scope.PersistentLocalVariables;
import gama.gaml.statements.LetStatement;

/** Declaration storage and one-time initialization for output managers. */
final class OutputLocalVariables {

	private final List<LetStatement> declarations = new ArrayList<>();
	private ITopLevelAgent owner;
	private PersistentLocalVariables variables;

	void add(final LetStatement declaration) { declarations.add(declaration); }

	List<LetStatement> getDeclarations() { return declarations; }

	boolean isEmpty() { return declarations.isEmpty(); }

	synchronized void initialize(final IScope scope) {
		if (isEmpty()) return;
		if (owner == scope.getRoot() && variables != null) {
			PersistentLocalVariables.install(scope, variables);
			return;
		}
		final Set<IDescription> owners = new HashSet<>();
		for (final LetStatement declaration : declarations) {
			if (!(declaration.getFacet(IKeyword.NAME) instanceof IVarExpression.Persistent variable))
				throw GamaRuntimeException.error("An output declaration must have persistent lexical storage.", scope);
			final IDescription description = variable.getDefinitionDescription();
			if (owners.add(description)) { PersistentLocalVariables.push(scope, description); }
			if (!PersistentLocalVariables.isInitialized(scope, description, variable.getName())
					&& !scope.execute(declaration).passed())
				throw GamaRuntimeException.error("Could not initialize output local '" + variable.getName() + "'.", scope);
		}
		owner = scope.getRoot();
		variables = PersistentLocalVariables.current(scope);
	}

	void dispose() {
		declarations.forEach(LetStatement::dispose);
		declarations.clear();
		owner = null;
		variables = null;
	}
}
