/*******************************************************************************************************
 *
 * ModelSerializer.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.serialization;

import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IModelDescription;
import gama.api.compilation.descriptions.ISpeciesDescription;
import gama.api.utils.StringUtils;

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
	public void serializeKeyword(final IDescription desc, final StringBuilder sb, final boolean includingBuiltIn) {
		sb.append("model ").append(desc.getName().replace(IModelDescription.MODEL_SUFFIX, "")).append(StringUtils.LN)
				.append(StringUtils.LN);
		sb.append("global ");
	}

	@Override
	public void serializeChildren(final IDescription d, final StringBuilder sb, final boolean includingBuiltIn) {
		final ISpeciesDescription desc = (ISpeciesDescription) d;
		sb.append(' ').append('{').append(StringUtils.LN);
		Iterable<? extends IDescription> children = desc.getAttributes();
		sb.append(StringUtils.LN);
		sb.append("// Global attributes of ").append(desc.getName()).append(StringUtils.LN);
		for (final IDescription s : children) { serializeChild(s, sb, includingBuiltIn); }
		children = desc.getActions();
		sb.append(StringUtils.LN);
		sb.append("// Global actions of ").append(desc.getName()).append(StringUtils.LN);
		for (final IDescription s : children) { serializeChild(s, sb, includingBuiltIn); }
		children = desc.getBehaviors();
		sb.append(StringUtils.LN);
		sb.append("// Behaviors of ").append(desc.getName()).append(StringUtils.LN);
		for (final IDescription s : children) { serializeChild(s, sb, includingBuiltIn); }
		children = desc.getAspects();
		sb.append(StringUtils.LN);
		sb.append("// Aspects of ").append(desc.getName()).append(StringUtils.LN);
		for (final IDescription s : children) { serializeChild(s, sb, includingBuiltIn); }
		sb.append('}').append(StringUtils.LN);
		children = desc.getOwnMicroSpecies().values();
		for (final IDescription s : children) {
			sb.append(StringUtils.LN);
			serializeChild(s, sb, includingBuiltIn);
		}

		children = ((IModelDescription) desc).getExperiments();
		for (final IDescription s : children) {
			sb.append(StringUtils.LN);
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
	public String serializeFacetValue(final IDescription s, final String key, final boolean includingBuiltIn) {
		if (NAME.equals(key)) return null;
		return super.serializeFacetValue(s, key, includingBuiltIn);
	}

}