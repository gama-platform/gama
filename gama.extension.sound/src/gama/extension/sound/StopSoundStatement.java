/*******************************************************************************************************
 *
 * StopSoundStatement.java, in gama.extension.sound, is part of the source code of the
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
import gama.annotations.constants.IKeyword;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.annotations.validator;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IDescriptionValidator;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.statements.AbstractStatementSequence;
import gama.api.gaml.symbols.ISymbol;
import gama.api.kernel.agent.IAgent;
import gama.api.runtime.scope.IScope;
import gama.extension.sound.StopSoundStatement.StopSoundValidator;

/**
 * The Class StopSoundStatement.
 */
@symbol (
		name = IKeyword.STOP_SOUND,
		kind = ISymbolKind.SEQUENCE_STATEMENT,
		with_sequence = true,
		concept = { IConcept.SOUND })
@inside (
		kinds = { ISymbolKind.BEHAVIOR, ISymbolKind.SEQUENCE_STATEMENT, ISymbolKind.LAYER, ISymbolKind.OUTPUT })
@validator (StopSoundValidator.class)
@doc ("Allows to stop the sound output")
public class StopSoundStatement extends AbstractStatementSequence {

	/**
	 * The Class StopSoundValidator.
	 */
	public static class StopSoundValidator implements IDescriptionValidator<IDescription> {

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
	 * Instantiates a new stop sound statement.
	 *
	 * @param desc the desc
	 */
	public StopSoundStatement(final IDescription desc) {
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
		soundPlayer.stop(scope, false);

		if (sequence != null) { scope.execute(sequence, currentAgent, null); }

		return null;
	}
}
