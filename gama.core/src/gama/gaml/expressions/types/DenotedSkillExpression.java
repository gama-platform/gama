package gama.gaml.expressions.types;

import org.apache.commons.lang3.NotImplementedException;

import gama.core.runtime.IScope;
import gama.gaml.descriptions.SkillDescription;
import gama.gaml.expressions.variables.VariableExpression;
import gama.gaml.types.Types;

//Not sure about inheritance, but that's the closest match for now
public class DenotedSkillExpression extends VariableExpression{

	
	public DenotedSkillExpression(final SkillDescription skill) {
		super(skill.getName(), Types.SKILL, true, skill);
	}

	@Override
	public void setVal(IScope scope, Object v, boolean create) {
		throw new NotImplementedException("I don't know if it should be possible or not yet");
		
	}

	@Override
	protected Object _value(IScope scope) {
		//TODO: I have no idea what I'm doing here
		return getDefinitionDescription();
	}

}
