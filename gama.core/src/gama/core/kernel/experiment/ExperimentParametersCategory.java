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
package gama.core.kernel.experiment;

import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.ISymbolKind;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.facet;
import gama.annotations.precompiler.GamlAnnotations.facets;
import gama.annotations.precompiler.GamlAnnotations.inside;
import gama.annotations.precompiler.GamlAnnotations.symbol;
import gama.core.common.interfaces.IKeyword;
import gama.core.common.preferences.GamaPreferences;
import gama.core.runtime.IScope;
import gama.core.util.GamaColor;
import gama.gaml.compilation.ISymbol;
import gama.gaml.compilation.Symbol;
import gama.gaml.descriptions.IDescription;
import gama.gaml.types.IType;

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
	public GamaColor getColor(final IScope scope) {
		return getFacetValue(scope, IKeyword.COLOR, null);
	}

}
