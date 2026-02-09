/*******************************************************************************************************
 *
 * StartSoundStatement.java, in gama.extension.sound, is part of the source code of the GAMA modeling and
 * simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.sound;

import java.io.File;

import gama.annotations.doc;
import gama.annotations.facet;
import gama.annotations.facets;
import gama.annotations.inside;
import gama.annotations.symbol;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.annotations.validator;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IDescriptionValidator;
import gama.api.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.statements.AbstractStatementSequence;
import gama.api.gaml.symbols.ISymbol;
import gama.api.gaml.types.IType;
import gama.api.kernel.agent.IAgent;
import gama.api.runtime.scope.IScope;
import gama.api.utils.files.FileUtils;
import gama.extension.sound.StartSoundStatement.StartSoundValidator;

/**
 * The Class StartSoundStatement.
 */
@symbol (
		name = IKeyword.START_SOUND,
		kind = ISymbolKind.SEQUENCE_STATEMENT,
		concept = { IConcept.SOUND },
		with_sequence = true,
		doc = @doc ("Starts playing a music file. The supported formats are aif, au, mp3, wav. One agent"))
@inside (
		kinds = { ISymbolKind.BEHAVIOR, ISymbolKind.SEQUENCE_STATEMENT, ISymbolKind.LAYER, ISymbolKind.OUTPUT })
@facets (
		value = { @facet (
				name = IKeyword.SOURCE,
				type = IType.STRING,
				optional = false,
				doc = @doc ("The path to music file. This path is relative to the path of the model.")),
				@facet (
						name = IKeyword.MODE,
						type = IType.ID,
						values = { IKeyword.OVERWRITE, IKeyword.IGNORE },
						optional = true,
						doc = @doc ("Mode of playing the sound")),
				@facet (
						name = IKeyword.REPEAT,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("Should the sound be repeated or not ?")) })
@validator (StartSoundValidator.class)
@SuppressWarnings ({ "rawtypes" })
@doc ("Allows to start the sound output")
public class StartSoundStatement extends AbstractStatementSequence {

	/**
	 * The Class StartSoundValidator.
	 */
	public static class StartSoundValidator implements IDescriptionValidator {

		/**
		 * Method validate()
		 *
		 * @see gama.api.compilation.descriptions.IDescriptionValidator#validate(gama.api.compilation.descriptions.IDescription)
		 */
		@Override
		public void validate(final IDescription cd) {

		}
	}

	/** The source. */
	private final IExpression source;

	/** The mode. */
	private final IExpression mode;

	/** The repeat. */
	private final IExpression repeat;

	/** The sequence. */
	private AbstractStatementSequence sequence = null;

	/**
	 * Instantiates a new start sound statement.
	 *
	 * @param desc
	 *            the desc
	 */
	public StartSoundStatement(final IDescription desc) {
		super(desc);

		source = getFacet(IKeyword.SOURCE);
		mode = getFacet(IKeyword.MODE);
		repeat = getFacet(IKeyword.REPEAT);
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
		final String soundFilePath = FileUtils.constructAbsoluteFilePath(scope, (String) source.value(scope), false);

		if (soundPlayer != null) {
			soundPlayer.play(scope, new File(soundFilePath),
					mode != null ? (String) mode.value(scope) : GamaSoundPlayer.OVERWRITE_MODE,
					repeat != null ? (Boolean) repeat.value(scope) : false);
		} else {
			// DEBUG.LOG("No more player in pool!");
		}

		if (sequence != null) { scope.execute(sequence, currentAgent, null); }

		return null;
	}
}
