/*******************************************************************************************************
 *
 * GamaSkillType.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.types;

import gama.annotations.doc;
import gama.annotations.type;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.additions.registries.GamaSkillRegistry;
import gama.api.constants.IKeyword;
import gama.api.data.objects.IMap;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.kernel.skill.ISkill;
import gama.api.runtime.scope.IScope;

/**
 * The type used for representing species objects (since they can be manipulated in a model)
 *
 * Written by drogoul Modified on 1 ao�t 2010
 *
 * @todo Description
 *
 */
@SuppressWarnings ("unchecked")
@type (
		name = IKeyword.SKILL,
		id = IType.SKILL,
		wraps = { ISkill.class },
		kind = ISymbolKind.Variable.REGULAR,
		concept = { IConcept.TYPE, IConcept.SKILL },
		doc = @doc ("Meta-type of the skills present in the GAML language"))
public class GamaSkillType extends GamaType<ISkill> {

	@Override
	@doc ("Tries to convert the parameter to a skill. If it is a skill already, returns it. If it is a string, returns it if it is registered in GAMA. Otherwise return null")
	public ISkill cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		if (obj instanceof ISkill) return (ISkill) obj;
		if (obj instanceof String) return GamaSkillRegistry.INSTANCE.getSkillInstanceFor((String) obj);
		return null;
	}

	@Override
	public ISkill getDefault() { return null; }

	@Override
	public boolean canCastToConst() {
		return true;
	}

	@Override
	public ISkill deserializeFromJson(final IScope scope, final IMap<String, Object> map2) {
		return cast(scope, map2.get("name"), null, false);
	}

}
