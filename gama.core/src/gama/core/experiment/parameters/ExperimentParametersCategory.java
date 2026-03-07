/*******************************************************************************************************
 *
 * ExperimentParametersCategory.java, in gama.core, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.experiment.parameters;

import gama.annotations.doc;
import gama.annotations.facet;
import gama.annotations.facets;
import gama.annotations.inside;
import gama.annotations.symbol;
import gama.annotations.constants.IKeyword;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.compilation.descriptions.IDescription;
import gama.api.gaml.symbols.ISymbol;
import gama.api.gaml.symbols.Symbol;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;
import gama.api.types.color.IColor;
import gama.api.utils.interfaces.ICategory;
import gama.api.utils.prefs.GamaPreferences;

/**
 * The Class CategoryStatement.
 */

/**
 * The Class ExperimentParametersCategory.
 */
@facets (
		value = { @facet (
				name = IKeyword.NAME,
				type = IType.LABEL,
				optional = false,
				doc = @doc ("The title of the category displayed in the UI")),
				@facet (
						name = "expanded",
						optional = true,
						type = IType.BOOL,
						doc = @doc ("Whether the category is initially expanded or not")),
				@facet (
						name = IKeyword.COLOR,
						type = IType.COLOR,
						optional = true,
						doc = @doc ("The background color of the category in the UI")) },
		omissible = IKeyword.NAME)

@symbol (
		name = { IKeyword.CATEGORY },
		kind = ISymbolKind.SINGLE_STATEMENT,
		with_sequence = false,

		concept = { IConcept.EXPERIMENT, IConcept.PARAMETER })
@inside (
		kinds = { ISymbolKind.EXPERIMENT })
@doc ("Allows to define a category of parameters that will serve to group parameters in the UI. The category can be declared as initially expanded or closed (overriding the corresponding preference) and with a background color")
public class ExperimentParametersCategory extends Symbol implements ICategory {

	/**
	 * Instantiates a new category statement.
	 *
	 * @param desc
	 *            the desc
	 */
	public ExperimentParametersCategory(final IDescription desc) {
		super(desc);
		setName(getLiteral(IKeyword.NAME));
	}

	@Override
	public void setChildren(final Iterable<? extends ISymbol> children) {}

	/**
	 * Checks if is expanded.
	 *
	 * @param scope
	 *            the scope
	 * @return true, if is expanded
	 */
	@Override
	public boolean isExpanded(final IScope scope) {
		return getFacetValue(scope, "expanded", GamaPreferences.Runtime.CORE_EXPAND_PARAMS.getValue());
	}

	@Override
	public String getTitle() { return getName(); }

	@Override
	public String getUnitLabel(final IScope scope) {
		return null;
	}

	@Override
	public boolean isDefinedInExperiment() { return true; }

	@Override
	public IColor getColor(final IScope scope) {
		return getFacetValue(scope, IKeyword.COLOR, null);
	}

}
