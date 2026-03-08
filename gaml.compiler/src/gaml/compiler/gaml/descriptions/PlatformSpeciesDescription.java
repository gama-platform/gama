/*******************************************************************************************************
 *
 * PlatformSpeciesDescription.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.descriptions;

import static gama.api.compilation.descriptions.IVariableDescription.PREF_DEFINITIONS;

import java.util.Set;

import org.eclipse.emf.ecore.EObject;

import gama.annotations.constants.IKeyword;
import gama.api.GAMA;
import gama.api.additions.IGamaHelper;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.ISpeciesDescription;
import gama.api.compilation.descriptions.IVarDescriptionProvider;
import gama.api.compilation.validation.IValidationContext;
import gama.api.gaml.GAML;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.symbols.Facets;
import gama.api.kernel.agent.IAgentConstructor;
import gama.api.kernel.simulation.ITopLevelAgent;
import gama.api.utils.prefs.Pref;
import gaml.compiler.gaml.validation.ValidationContext;

/**
 * The Class PlatformSpeciesDescription.
 */
public class PlatformSpeciesDescription extends SpeciesDescription implements ISpeciesDescription.Platform {

	/** The alternate var provider. */
	IVarDescriptionProvider alternateVarProvider;

	/**
	 * Instantiates a new platform species description.
	 *
	 * @param keyword
	 *            the keyword
	 * @param clazz
	 *            the clazz
	 * @param macroDesc
	 *            the macro desc
	 * @param parent
	 *            the parent
	 * @param cp
	 *            the cp
	 * @param source
	 *            the source
	 * @param facets
	 *            the facets
	 */
	public PlatformSpeciesDescription(final String keyword, final Class<?> clazz, final ISpeciesDescription macroDesc,
			final ISpeciesDescription parent, final Iterable<? extends IDescription> cp, final EObject source,
			final Facets facets) {
		super(keyword, clazz, macroDesc, parent, cp, source, facets);
	}

	/**
	 * Instantiates a new platform species description.
	 *
	 * @param name
	 *            the name
	 * @param clazz
	 *            the clazz
	 * @param superDesc
	 *            the super desc
	 * @param parent
	 *            the parent
	 * @param helper
	 *            the helper
	 * @param skills2
	 *            the skills 2
	 * @param ff
	 *            the ff
	 * @param plugin
	 *            the plugin
	 */
	public PlatformSpeciesDescription(final String name, final Class<?> clazz, final ISpeciesDescription superDesc,
			final ISpeciesDescription parent, final IAgentConstructor helper, final Set<String> skills2,
			final Facets ff, final String plugin) {
		super(name, clazz, superDesc, parent, helper, skills2, ff, plugin);
	}

	@Override
	public void attachAlternateVarDescriptionProvider(final IVarDescriptionProvider vp) {
		alternateVarProvider = vp;
	}

	@Override
	public void copyJavaAdditions() {
		super.copyJavaAdditions();
		for (final Pref<?> pref : GAMA.getPreferenceStore().getPreferences()) { addPrefAsVariable(pref); }
	}

	/**
	 * Adds the pref.
	 *
	 * @param key
	 *            the key
	 * @param entry
	 *            the entry
	 */
	@Override
	public void addPrefAsVariable(final Pref<?> entry) {
		String key = entry.getKey();
		final VariableDescription var = (VariableDescription) GAML.getDescriptionFactory()
				.create(entry.getType().toString(), this, IKeyword.NAME, key);
		PREF_DEFINITIONS.put(key, entry.getTitle());
		final IGamaHelper<?> get = (scope, agent, skill, values) -> entry.getValue();
		final IGamaHelper<?> set = (scope, agent, skill, val) -> {
			if (agent instanceof ITopLevelAgent.Platform gama) {
				// Should be in any case
				gama.savePrefToRestore(key, entry.getValue());
			}
			GAMA.getPreferenceStore().get(key).setValue(scope, val);
			return this;
		};
		final IGamaHelper<?> init = (scope, agent, skill, values) -> entry.getValue();
		var.addHelpers(get, init, set);
		addChild(var);
	}

	@Override
	public IValidationContext getValidationContext() { return ValidationContext.NULL; }

	@Override
	public IVarDescriptionProvider getDescriptionDeclaringVar(final String name) {
		IVarDescriptionProvider provider = super.getDescriptionDeclaringVar(name);
		if (provider == null && alternateVarProvider != null && alternateVarProvider.hasAttribute(name)) {
			provider = alternateVarProvider;
		}
		return provider;
	}

	/**
	 * Gets the fake pref expression.
	 *
	 * @param key
	 *            the key
	 * @return the fake pref expression
	 */
	public IExpression getFakePrefExpression(final String key) {
		final VariableDescription var = (VariableDescription) GAML.getDescriptionFactory().create(IKeyword.UNKNOWN,
				PlatformSpeciesDescription.this, IKeyword.NAME, key);
		PREF_DEFINITIONS.put(key, "This preference is not available in the current configuration of GAMA");
		return var.getVarExpr(true);
	}

	@Override
	protected boolean verifyAttributeCycles() {
		return true;
	}

}
