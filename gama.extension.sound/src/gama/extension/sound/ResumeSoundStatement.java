/*******************************************************************************************************
 *
 * ResumeSoundStatement.java, in gama.extension.sound, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.extension.sound;

import gama.annotations.doc;
import gama.annotations.inside;
import gama.annotations.symbol;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.annotations.validator;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IDescriptionValidator;
import gama.api.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.statements.AbstractStatementSequence;
import gama.api.gaml.symbols.ISymbol;
import gama.api.kernel.agent.IAgent;
import gama.api.runtime.scope.IScope;
import gama.extension.sound.ResumeSoundStatement.ResumeSoundValidator;

/**
 * The Class ResumeSoundStatement.
 */
@symbol (
		name = IKeyword.RESUME_SOUND,
		kind = ISymbolKind.SEQUENCE_STATEMENT,
		with_sequence = true,
		concept = { IConcept.SOUND })
@inside (
		kinds = { ISymbolKind.BEHAVIOR, ISymbolKind.SEQUENCE_STATEMENT, ISymbolKind.LAYER, ISymbolKind.OUTPUT })
@validator (ResumeSoundValidator.class)
@doc ("Allows to resume the sound output")
public class ResumeSoundStatement extends AbstractStatementSequence {

	/**
	 * The Class ResumeSoundValidator.
	 */
	public static class ResumeSoundValidator implements IDescriptionValidator<IDescription> {

		/**
		 * Method validate()
		 *
		 * @see gama.api.compilation.descriptions.IDescriptionValidator#validate(gama.api.compilation.descriptions.IDescription)
		 */
		@Override
		public void validate(final IDescription cd) {

			// what to validate?
		}
	}

	/** The sequence. */
	private AbstractStatementSequence sequence = null;

	/**
	 * Instantiates a new resume sound statement.
	 *
	 * @param desc the desc
	 */
	public ResumeSoundStatement(final IDescription desc) {
		super(desc);
	}

	@Override
	public void setChildren(final Iterable<? extends ISymbol> com) {
		sequence = new AbstractStatementSequence(description);
		sequence.setName("commands of " + getName());
		sequence.setChildren(com);
	}

	@Override
	public Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		final IAgent currentAgent = scope.getAgent();

		final GamaSoundPlayer soundPlayer = SoundPlayerBroker.getInstance().getSoundPlayer(currentAgent);
		soundPlayer.resume(scope);

		if (sequence != null) { scope.execute(sequence, currentAgent, null); }

		return null;
	}
}
