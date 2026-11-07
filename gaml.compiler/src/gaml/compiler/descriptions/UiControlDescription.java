/*******************************************************************************************************
 *
 * UiControlDescription.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.descriptions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;

import gama.api.compilation.descriptions.IDescription;
import gama.api.gaml.expressions.IVarExpression;
import gama.api.gaml.symbols.Facets;
import gama.api.gaml.types.Types;
import gaml.compiler.expressions.TempVariableExpression;

/** Callback values are local to the control's handler, never to its enclosing overlay. */
public class UiControlDescription extends StatementWithChildrenDescription {

	public UiControlDescription(final String keyword, final IDescription enclosing,
			final Iterable<IDescription> children, final EObject source, final Facets facets) {
		super(keyword, enclosing, children, false, source, facets, null);
		if (!"ui_button".equals(keyword) || isToggle()) {
			addTemp(this, null, "ui_value", "ui_slider".equals(keyword) ? Types.FLOAT : Types.BOOL);
		}
	}

	public boolean isToggle() {
		final var mode = getFacetExpr("toggle");
		return "ui_button".equals(getKeyword()) && mode != null && Boolean.TRUE.equals(mode.getConstValue());
	}

	@Override
	public IDescription validate() {
		final IDescription result = super.validate();
		final var binding = getFacetExpr("var");
		if (binding instanceof TempVariableExpression
				|| binding instanceof IVarExpression variable
						&& variable.getOwner() instanceof TempVariableExpression) {
			error("var: cannot bind a temporary variable; use a persistent model or experiment variable.");
		}
		visitFacets((name, expression) -> {
			if ("action".equals(name) || "on_change".equals(name)) return true;
			final var compiled = expression.getExpression();
			if (compiled != null && compiled.findAny(e -> e instanceof TempVariableExpression temp
					&& "ui_value".equals(temp.getName()))) {
				error("ui_value is only available in the change handler, not in drawing facets.");
			}
			return true;
		});
		return result;
	}

	@Override
	public UiControlDescription copy(final IDescription into) {
		final List<IDescription> children = new ArrayList<>();
		for (final IDescription child : getOwnChildren()) children.add(child.copy(into));
		return new UiControlDescription(getKeyword(), into, children, getUnderlyingElement(), getFacetsCopy());
	}
}
