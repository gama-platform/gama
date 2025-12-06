package gama.gaml.skills;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.facet;
import gama.annotations.precompiler.GamlAnnotations.facets;
import gama.annotations.precompiler.GamlAnnotations.inside;
import gama.annotations.precompiler.GamlAnnotations.symbol;
import gama.annotations.precompiler.ISymbolKind;
import gama.core.common.interfaces.IKeyword;
import gama.gaml.descriptions.IDescription;
import gama.gaml.descriptions.SkillDescription;
import gama.gaml.types.IType;

@symbol(
		name = "skill",
		kind = ISymbolKind.SKILL,
		with_sequence = true,
		doc = @doc("A user defined skill, i.e a set of actions, reflexes and attributes that can be attached to species to be used by their agents.")
		)
@facets(
	value = {
		@facet (
				name = IKeyword.NAME,
				type = IType.ID,
				optional = false,
				doc = @doc ("the identifier of the skill")),
	}
)
@inside (kinds = { ISymbolKind.MODEL})
public class UserDefinedSkill extends Skill {


	protected SkillDescription description;
	
	public UserDefinedSkill(IDescription desc) {
		super(desc);
	}
	

}
