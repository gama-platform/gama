/*******************************************************************************************************
 *
 * ModelSerializer.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.descriptions;

import gama.gaml.operators.Strings;

/**
 * The Class ModelSerializer.
 */
public class ModelSerializer extends SpeciesSerializer {

	/**
	 * Serialize keyword.
	 *
	 * @param desc
	 *            the desc
	 * @param sb
	 *            the sb
	 * @param includingBuiltIn
	 *            the including built in
	 */
	@Override
	protected void serializeKeyword(final IDescription desc, final StringBuilder sb, final boolean includingBuiltIn) {
		sb.append("model ").append(desc.getName().replace(ModelDescription.MODEL_SUFFIX, "")).append(Strings.LN)
				.append(Strings.LN);
		sb.append("global ");
	}

	@Override
	protected void serializeChildren(final IDescription d, final StringBuilder sb, final boolean includingBuiltIn) {
		final SpeciesDescription desc = (SpeciesDescription) d;
		sb.append(' ').append('{').append(Strings.LN);
		Iterable<? extends IDescription> children = desc.getAttributes();
		sb.append(Strings.LN);
		sb.append("// Global attributes of ").append(desc.getName()).append(Strings.LN);
		for (final IDescription s : children) { serializeChild(s, sb, includingBuiltIn); }
		children = desc.getActions();
		sb.append(Strings.LN);
		sb.append("// Global actions of ").append(desc.getName()).append(Strings.LN);
		for (final IDescription s : children) { serializeChild(s, sb, includingBuiltIn); }
		children = desc.getBehaviors();
		sb.append(Strings.LN);
		sb.append("// Behaviors of ").append(desc.getName()).append(Strings.LN);
		for (final IDescription s : children) { serializeChild(s, sb, includingBuiltIn); }
		children = desc.getAspects();
		sb.append(Strings.LN);
		sb.append("// Aspects of ").append(desc.getName()).append(Strings.LN);
		for (final IDescription s : children) { serializeChild(s, sb, includingBuiltIn); }
		sb.append('}').append(Strings.LN);
		if (desc.hasMicroSpecies()) {
			children = desc.getMicroSpecies().values();
			for (final IDescription s : children) {
				sb.append(Strings.LN);
				serializeChild(s, sb, includingBuiltIn);
			}
		}

		children = ((ModelDescription) desc).getExperiments();
		for (final IDescription s : children) {
			sb.append(Strings.LN);
			serializeChild(s, sb, includingBuiltIn);
		}
	}

	/**
	 * Serialize facet value.
	 *
	 * @param s
	 *            the s
	 * @param key
	 *            the key
	 * @param includingBuiltIn
	 *            the including built in
	 * @return the string
	 */
	@Override
	protected String serializeFacetValue(final IDescription s, final String key, final boolean includingBuiltIn) {
		if (NAME.equals(key)) return null;
		return super.serializeFacetValue(s, key, includingBuiltIn);
	}

}