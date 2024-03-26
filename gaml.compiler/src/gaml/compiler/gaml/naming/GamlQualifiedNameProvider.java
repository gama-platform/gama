/*******************************************************************************************************
 *
 * GamlQualifiedNameProvider.java, in gaml.compiler.gaml, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gaml.compiler.gaml.naming;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.eclipse.xtext.naming.QualifiedName;

import gama.core.common.interfaces.IKeyword;
import gama.gaml.descriptions.ModelDescription;
import gaml.compiler.gaml.ArgumentPair;
import gaml.compiler.gaml.GamlDefinition;
import gaml.compiler.gaml.Model;
import gaml.compiler.gaml.S_Reflex;
import gaml.compiler.gaml.speciesOrGridDisplayStatement;
import gaml.compiler.gaml.util.GamlSwitch;

/**
 * GAML Qualified Name provider.
 *
 */
public class GamlQualifiedNameProvider extends IQualifiedNameProvider.AbstractImpl {

	/** The Constant NULL. */
	private final static String NULL = "";
	
	/** The Constant SWITCH. */
	private final static GamlSwitch<String> SWITCH = new GamlSwitch<String>() {

		@Override
		public String caseS_Reflex(final S_Reflex s) {
			if (IKeyword.ASPECT.equals(s.getKey())) { return s.getName(); }
			return NULL;
		}

		@Override
		public String casespeciesOrGridDisplayStatement(final speciesOrGridDisplayStatement s) {
			return NULL;
		}

		@Override
		public String caseModel(final Model o) {
			return o.getName() + ModelDescription.MODEL_SUFFIX;
		}

		@Override
		public String defaultCase(final EObject e) {
			return NULL;
		}

		@Override
		public String caseGamlDefinition(final GamlDefinition object) {
			return object.getName();
		}

		@Override
		public String caseArgumentPair(final ArgumentPair object) {
			return object.getOp();
		}

	};

	@Override
	public QualifiedName getFullyQualifiedName(final EObject input) {
		final String string = SWITCH.doSwitch(input);
		if (string == null || string.equals(NULL)) { return null; }
		return QualifiedName.create(string);
	}

}