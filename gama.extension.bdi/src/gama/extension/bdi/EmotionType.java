/*******************************************************************************************************
 *
 * EmotionType.java, in gama.extension.bdi, is part of the source code of the GAMA modeling and simulation
 * platform .
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
 * The Class EmotionType.
 */
@SuppressWarnings ("unchecked")
@type (
		name = "emotion",
		id = EmotionType.EMOTIONTYPE_ID,
		wraps = { Emotion.class },
		concept = { IConcept.TYPE, IConcept.BDI })
@doc ("represents the type emotion")
public class EmotionType extends GamaType<Emotion> {

	/** The Constant id. */
	public final static int EMOTIONTYPE_ID = IType.AVAILABLE_TYPES + 546656;

	@Override
	public boolean canCastToConst() {
		return true;
	}

	@Override
	@doc ("cast an object instance of emotion as an emotion")
	public Emotion cast(final IScope scope, final Object obj, final Object val, final boolean copy)
			throws GamaRuntimeException {
		if (obj instanceof Emotion) return (Emotion) obj;
		return null;
	}

	@Override
	public Emotion getDefault() {

		return null;
	}

	@Override
	public Emotion deserializeFromJson(final IScope scope, final IMap<String, Object> map2) {
		// See later how to deal with BDI objects
		return null;
	}

}
