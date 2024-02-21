/*******************************************************************************************************
 *
 * SocialLinkType.java, in gama.extension.bdi, is part of the source code of the GAMA modeling and
 * simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.bdi;

import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.type;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.IMap;
import gama.gaml.types.GamaType;
import gama.gaml.types.IType;

/**
 * The Class SocialLinkType.
 */
@SuppressWarnings ("unchecked")
@type (
		name = "social_link",
		id = SocialLinkType.id,
		wraps = { SocialLink.class },
		concept = { IConcept.TYPE, IConcept.BDI })
@doc ("represents a social link")
public class SocialLinkType extends GamaType<SocialLink> {

	/** The Constant id. */
	public final static int id = IType.AVAILABLE_TYPES + 546657;

	@Override
	public boolean canCastToConst() {
		return true;
	}

	@Override
	@doc ("cast an object into a social link, if it is an instance of a social link")
	public SocialLink cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		if (obj instanceof SocialLink) return (SocialLink) obj;
		return null;
	}

	@Override
	public SocialLink getDefault() { return null; }

	@Override
	public SocialLink deserializeFromJson(final IScope scope, final IMap<String, Object> map2) {
		// See later how to deal with BDI objects
		return null;
	}

}
