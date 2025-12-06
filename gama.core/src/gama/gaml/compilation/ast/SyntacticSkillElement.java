package gama.gaml.compilation.ast;

import org.eclipse.emf.ecore.EObject;

import gama.gaml.statements.Facets;

public class SyntacticSkillElement extends SyntacticStructuralElement {

	public SyntacticSkillElement(final String keyword, final Facets facets, final EObject statement) {
		super(keyword, facets, statement);
	}

	@Override
	public boolean isSkill() {
		return true;
	}


}
