/**
 * Created by drogoul, 28 mars 2026
 *
 */
package gaml.compiler.ui.editor;

import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.xtext.ui.editor.DirtyStateEditorSupport;
import org.eclipse.xtext.ui.editor.SchedulingRuleFactory;

/**
 * The class GamlDirtyStateSupport.
 *
 * @author drogoul
 * @since 28 mars 2026
 *
 */
public class GamlDirtyStateSupport extends DirtyStateEditorSupport {

	/** The Constant SCHEDULING_RULE. */
	private static final ISchedulingRule SCHEDULING_RULE = SchedulingRuleFactory.INSTANCE.newSequence();

	@Override
	public void markEditorClean(final IDirtyStateEditorSupportClient client) {
		if (!isDirty()) return;
		super.markEditorClean(client);
	}

	@Override
	protected UpdateEditorStateJob createUpdateEditorJob() {
		return new UpdateEditorStateJob(SCHEDULING_RULE) {

			@Override
			protected int getDelay() {

				return GamlEditor.SCHEDULE_DELAY;

			}

		};
	}

}
