package gama.gaml.factories;

import org.eclipse.emf.ecore.EObject;

import gama.gaml.descriptions.IDescription;
import gama.gaml.descriptions.SkillDescription;
import gama.gaml.descriptions.SymbolProto;
import gama.gaml.skills.UserDefinedSkill;
import gama.gaml.statements.Facets;

public class SkillFactory extends SymbolFactory {

	@Override
	protected IDescription buildDescription(String keyword, Facets facets, EObject element,
			Iterable<IDescription> children, IDescription enclosing, SymbolProto proto) {
		return new SkillDescription(facets.get("name").toString(), UserDefinedSkill.class, children, null );
	}
	
}
