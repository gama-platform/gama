/*******************************************************************************************************
 *
 * JsonEditorPreferencePage.java, in gama.ui.viewers, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.viewers.json.eclipse.preferences;
/*
 * Copyright 2020 Albert Tregnaghi
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 */

import static gama.ui.viewers.json.eclipse.JsonEditorUtil.getPreferences;
import static gama.ui.viewers.json.eclipse.preferences.JsonEditorPreferenceConstants.P_CODE_ASSIST_ADD_KEYWORDS;
import static gama.ui.viewers.json.eclipse.preferences.JsonEditorPreferenceConstants.P_CODE_ASSIST_ADD_SIMPLEWORDS;
import static gama.ui.viewers.json.eclipse.preferences.JsonEditorPreferenceConstants.P_CREATE_GROUPED_ARRAYS_TRESHOLD;
import static gama.ui.viewers.json.eclipse.preferences.JsonEditorPreferenceConstants.P_CREATE_OUTLINE_FOR_NEW_EDITOR;
import static gama.ui.viewers.json.eclipse.preferences.JsonEditorPreferenceConstants.P_EDITOR_ALLOW_COMMENTS_ENABLED;
import static gama.ui.viewers.json.eclipse.preferences.JsonEditorPreferenceConstants.P_EDITOR_ALLOW_UNQUOTED_CONTROL_CHARS;
import static gama.ui.viewers.json.eclipse.preferences.JsonEditorPreferenceConstants.P_EDITOR_AUTO_CREATE_END_BRACKETSY;
import static gama.ui.viewers.json.eclipse.preferences.JsonEditorPreferenceConstants.P_EDITOR_ENCLOSING_BRACKETS;
import static gama.ui.viewers.json.eclipse.preferences.JsonEditorPreferenceConstants.P_EDITOR_HIGHLIGHT_BRACKET_AT_CARET_LOCATION;
import static gama.ui.viewers.json.eclipse.preferences.JsonEditorPreferenceConstants.P_EDITOR_MATCHING_BRACKETS_COLOR;
import static gama.ui.viewers.json.eclipse.preferences.JsonEditorPreferenceConstants.P_EDITOR_MATCHING_BRACKETS_ENABLED;
import static gama.ui.viewers.json.eclipse.preferences.JsonEditorPreferenceConstants.P_LINK_EDITOR_WITH_OUTLINE;
import static gama.ui.viewers.json.eclipse.preferences.JsonEditorPreferenceConstants.P_VALIDATE_ON_SAVE;

import java.util.ArrayList;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import gama.ui.viewers.json.eclipse.JsonEditorUtil;

/**
 * Parts are inspired by <a href=
 * "https://github.com/eclipse/eclipse.jdt.ui/blob/master/org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/preferences/JavaEditorAppearanceConfigurationBlock.java">org.eclipse.jdt.internal.ui.preferences.JavaEditorAppearanceConfigurationBlock
 * </a>
 *
 * @author Albert Tregnaghi
 *
 */
public class JsonEditorPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	/** The Constant INDENT. */
	protected static final int INDENT = 20;

	/**
	 * Indent.
	 *
	 * @param control
	 *            the control
	 */
	protected static void indent(final Control control) {
		((GridData) control.getLayoutData()).horizontalIndent += INDENT;
	}

	/** The bracket highlighting checkbox. */
	private Button bracketHighlightingCheckbox;

	/** The enclosing brackets radio button. */
	private Button enclosingBracketsRadioButton;

	/** The matching bracket and caret location radio button. */
	private Button matchingBracketAndCaretLocationRadioButton;

	/** The matching bracket radio button. */
	private Button matchingBracketRadioButton;

	/** The matching brackets color. */
	private ColorFieldEditor matchingBracketsColor;

	/** The link editor with outline. */
	private BooleanFieldEditor linkEditorWithOutline;

	/** The create outline. */
	private BooleanFieldEditor createOutline;

	/** The master slave listeners. */
	private final ArrayList<MasterButtonSlaveSelectionListener> masterSlaveListeners = new ArrayList<>();

	/** The enclosing brackets. */
	private boolean enclosingBrackets;

	/** The highlight bracket at caret location. */
	private boolean highlightBracketAtCaretLocation;

	/** The matching brackets. */
	private boolean matchingBrackets;

	/** The allow comments. */
	private BooleanFieldEditor allowComments;

	/** The allow unquoted control chars. */
	private BooleanFieldEditor allowUnquotedControlChars;

	/** The validate on save. */
	private BooleanFieldEditor validateOnSave;

	/** The auto create end brackets. */
	private BooleanFieldEditor autoCreateEndBrackets;

	/** The code assist with highspeed JSON keywords. */
	private BooleanFieldEditor codeAssistWithJsonKeywords;

	/** The code assist with simple words. */
	private BooleanFieldEditor codeAssistWithSimpleWords;

	/** The array grouping. */
	private IntegerFieldEditor arrayGrouping;

	/**
	 * Instantiates a new highspeed JSON editor preference page.
	 */
	public JsonEditorPreferencePage() {
		super(GRID);
		setPreferenceStore(getPreferences().getPreferenceStore());
	}

	@Override
	public void init(final IWorkbench workbench) {

	}

	@Override
	public void performDefaults() {
		super.performDefaults();
		reloadBracketHighlightingPreferenceDefaults();
	}

	@Override
	public boolean performOk() {
		boolean ok = super.performOk();
		if (ok) {
			setBoolean(P_EDITOR_MATCHING_BRACKETS_ENABLED, matchingBrackets);
			setBoolean(P_EDITOR_HIGHLIGHT_BRACKET_AT_CARET_LOCATION, highlightBracketAtCaretLocation);
			setBoolean(P_EDITOR_ENCLOSING_BRACKETS, enclosingBrackets);

			JsonEditorUtil.refreshParserSettings();
		}
		return ok;
	}

	/**
	 * Creates the dependency.
	 *
	 * @param master
	 *            the master
	 * @param slave
	 *            the slave
	 */
	protected void createDependency(final Button master, final Control slave) {
		Assert.isNotNull(slave);
		indent(slave);
		MasterButtonSlaveSelectionListener listener = new MasterButtonSlaveSelectionListener(master, slave);
		master.addSelectionListener(listener);
		this.masterSlaveListeners.add(listener);
	}

	@Override
	protected void createFieldEditors() {
		Composite appearanceComposite = new Composite(getFieldEditorParent(), SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		appearanceComposite.setLayout(layout);

		/* OTHER */
		Composite otherComposite = new Composite(appearanceComposite, SWT.NONE);
		GridLayout otherLayout = new GridLayout();
		otherLayout.marginWidth = 0;
		otherLayout.marginHeight = 0;
		otherComposite.setLayout(otherLayout);

		/* outline */
		linkEditorWithOutline = new BooleanFieldEditor(P_LINK_EDITOR_WITH_OUTLINE.getId(),
				"New opened editors are linked with outline", otherComposite);
		linkEditorWithOutline.getDescriptionControl(otherComposite)
				.setToolTipText("Via this setting the default behaviour for new opened outlines is set");
		addField(linkEditorWithOutline);
		/* linking with outline */
		createOutline = new BooleanFieldEditor(P_CREATE_OUTLINE_FOR_NEW_EDITOR.getId(),
				"New opened editors will create an outline", otherComposite);
		createOutline.getDescriptionControl(otherComposite).setToolTipText(
				"When enabled, every new opened editor will automatically create a n outline (which is time consuming and increases memory consumtion");
		addField(createOutline);

		/* array grouping */
		arrayGrouping = new IntegerFieldEditor(P_CREATE_GROUPED_ARRAYS_TRESHOLD.getId(),
				"Treshold for array grouping in outline", otherComposite);
		addField(arrayGrouping);

		/* parsing, allow comments */
		allowComments =
				new BooleanFieldEditor(P_EDITOR_ALLOW_COMMENTS_ENABLED.getId(), "Allow comments", otherComposite);
		allowComments.getDescriptionControl(otherComposite).setToolTipText("When enabled comments are allowed");
		addField(allowComments);

		/* parsing, allow newlines in strings */
		allowUnquotedControlChars = new BooleanFieldEditor(P_EDITOR_ALLOW_UNQUOTED_CONTROL_CHARS.getId(),
				"Allow unquoted control characters", otherComposite);
		allowUnquotedControlChars.getDescriptionControl(otherComposite).setToolTipText(
				"When enabled, unquoted control characters (newlines, tabs, etc.) in strings are allowed");
		addField(allowUnquotedControlChars);

		/* validate */
		validateOnSave = new BooleanFieldEditor(P_VALIDATE_ON_SAVE.getId(), "Validate on save", otherComposite);
		validateOnSave.getDescriptionControl(otherComposite)
				.setToolTipText("When enabled each save of document will validate automatically and show errors");
		addField(validateOnSave);

		Label spacer = new Label(appearanceComposite, SWT.LEFT);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 2;
		gd.heightHint = convertHeightInCharsToPixels(1) / 2;
		spacer.setLayoutData(gd);

		/* BRACKETS */
		/*
		 * Why so ugly implemented and not using field editors ? Because SourceViewerDecorationSupport needs 3 different
		 * preference keys to do its job, so this preference doing must be same as on Java editor preferences.
		 */
		GridData bracketsGroupLayoutData = new GridData();
		bracketsGroupLayoutData.horizontalSpan = 2;
		bracketsGroupLayoutData.widthHint = 400;

		Group bracketsGroup = new Group(appearanceComposite, SWT.NONE);
		bracketsGroup.setText("Brackets");
		bracketsGroup.setLayout(new GridLayout());
		bracketsGroup.setLayoutData(bracketsGroupLayoutData);

		autoCreateEndBrackets = new BooleanFieldEditor(P_EDITOR_AUTO_CREATE_END_BRACKETSY.getId(),
				"Auto create ending brackets", bracketsGroup);
		addField(autoCreateEndBrackets);

		String label = "Bracket highlighting";

		bracketHighlightingCheckbox = addButton(bracketsGroup, SWT.CHECK, label, 0, new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				matchingBrackets = bracketHighlightingCheckbox.getSelection();
			}
		});

		Composite radioComposite = new Composite(bracketsGroup, SWT.NONE);
		GridLayout radioLayout = new GridLayout();
		radioLayout.marginWidth = 0;
		radioLayout.marginHeight = 0;
		radioComposite.setLayout(radioLayout);

		label = "highlight matching bracket";
		matchingBracketRadioButton = addButton(radioComposite, SWT.RADIO, label, 0, new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (matchingBracketRadioButton.getSelection()) { highlightBracketAtCaretLocation = false; }
			}
		});
		createDependency(bracketHighlightingCheckbox, matchingBracketRadioButton);

		label = "highlight matching bracket and caret location";
		matchingBracketAndCaretLocationRadioButton =
				addButton(radioComposite, SWT.RADIO, label, 0, new SelectionAdapter() {
					@Override
					public void widgetSelected(final SelectionEvent e) {
						if (matchingBracketAndCaretLocationRadioButton.getSelection()) {
							highlightBracketAtCaretLocation = true;
						}
					}
				});
		createDependency(bracketHighlightingCheckbox, matchingBracketAndCaretLocationRadioButton);

		label = "highlight enclosing brackets";
		enclosingBracketsRadioButton = addButton(radioComposite, SWT.RADIO, label, 0, new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				boolean selection = enclosingBracketsRadioButton.getSelection();
				enclosingBrackets = selection;
				if (selection) { highlightBracketAtCaretLocation = true; }
			}
		});
		createDependency(bracketHighlightingCheckbox, enclosingBracketsRadioButton);

		matchingBracketsColor = new ColorFieldEditor(P_EDITOR_MATCHING_BRACKETS_COLOR.getId(),
				"Matching brackets color", radioComposite);
		addField(matchingBracketsColor);
		createDependency(bracketHighlightingCheckbox, matchingBracketsColor.getLabelControl(radioComposite));
		createDependency(bracketHighlightingCheckbox, matchingBracketsColor.getColorSelector().getButton());

		/* --------------------- */
		/* -- Code assistance -- */
		/* --------------------- */

		GridData codeAssistGroupLayoutData = new GridData();
		codeAssistGroupLayoutData.horizontalSpan = 2;
		codeAssistGroupLayoutData.widthHint = 400;

		Group codeAssistGroup = new Group(appearanceComposite, SWT.NONE);
		codeAssistGroup.setText("Code assistence");
		codeAssistGroup.setLayout(new GridLayout());
		codeAssistGroup.setLayoutData(codeAssistGroupLayoutData);

		codeAssistWithJsonKeywords = new BooleanFieldEditor(P_CODE_ASSIST_ADD_KEYWORDS.getId(),
				"Json keywords and external commands", codeAssistGroup);
		codeAssistWithJsonKeywords.getDescriptionControl(codeAssistGroup).setToolTipText(
				"When enabled the standard keywords supported by json editor are always automatically available as code proposals");
		addField(codeAssistWithJsonKeywords);

		codeAssistWithSimpleWords =
				new BooleanFieldEditor(P_CODE_ASSIST_ADD_SIMPLEWORDS.getId(), "Existing words", codeAssistGroup);
		codeAssistWithSimpleWords.getDescriptionControl(codeAssistGroup).setToolTipText(
				"When enabled the current source will be scanned for words. The existing words will be available as code proposals");
		addField(codeAssistWithSimpleWords);

	}

	@Override
	protected void initialize() {
		initializeBracketHighlightingPreferences();
		super.initialize();
		updateSlaveComponents();
	}

	/**
	 * Adds the button.
	 *
	 * @param parent
	 *            the parent
	 * @param style
	 *            the style
	 * @param label
	 *            the label
	 * @param indentation
	 *            the indentation
	 * @param listener
	 *            the listener
	 * @return the button
	 */
	private Button addButton(final Composite parent, final int style, final String label, final int indentation, final SelectionListener listener) {
		Button button = new Button(parent, style);
		button.setText(label);

		GridData gd = new GridData(32);
		gd.horizontalIndent = indentation;
		gd.horizontalSpan = 2;
		button.setLayoutData(gd);
		button.addSelectionListener(listener);

		return button;
	}

	/**
	 * Sets the boolean.
	 *
	 * @param id
	 *            the id
	 * @param value
	 *            the value
	 */
	private void setBoolean(final JsonEditorPreferenceConstants id, final boolean value) {
		getPreferences().setBooleanPreference(id, value);
	}

	/**
	 * Gets the boolean.
	 *
	 * @param id
	 *            the id
	 * @return the boolean
	 */
	private boolean getBoolean(final JsonEditorPreferenceConstants id) {
		return getPreferences().getBooleanPreference(id);
	}

	/**
	 * Gets the default boolean.
	 *
	 * @param id
	 *            the id
	 * @return the default boolean
	 */
	private boolean getDefaultBoolean(final JsonEditorPreferenceConstants id) {
		return getPreferences().getDefaultBooleanPreference(id);
	}

	/**
	 * Initialize bracket highlighting preferences.
	 */
	private void initializeBracketHighlightingPreferences() {
		matchingBrackets = getBoolean(P_EDITOR_MATCHING_BRACKETS_ENABLED);
		highlightBracketAtCaretLocation = getBoolean(P_EDITOR_HIGHLIGHT_BRACKET_AT_CARET_LOCATION);
		enclosingBrackets = getBoolean(P_EDITOR_ENCLOSING_BRACKETS);

		updateBracketUI();
	}

	/**
	 * Reload bracket highlighting preference defaults.
	 */
	private void reloadBracketHighlightingPreferenceDefaults() {
		matchingBrackets = getDefaultBoolean(P_EDITOR_MATCHING_BRACKETS_ENABLED);
		highlightBracketAtCaretLocation = getDefaultBoolean(P_EDITOR_HIGHLIGHT_BRACKET_AT_CARET_LOCATION);
		enclosingBrackets = getDefaultBoolean(P_EDITOR_ENCLOSING_BRACKETS);

		updateBracketUI();
	}

	/**
	 * Update bracket UI.
	 */
	private void updateBracketUI() {
		this.bracketHighlightingCheckbox.setSelection(matchingBrackets);

		this.enclosingBracketsRadioButton.setSelection(enclosingBrackets);
		if (!enclosingBrackets) {
			this.matchingBracketRadioButton.setSelection(!highlightBracketAtCaretLocation);
			this.matchingBracketAndCaretLocationRadioButton.setSelection(highlightBracketAtCaretLocation);
		}
		updateSlaveComponents();
	}

	/**
	 * Update slave components.
	 */
	private void updateSlaveComponents() {
		for (MasterButtonSlaveSelectionListener listener : masterSlaveListeners) { listener.updateSlaveComponent(); }
	}

	/**
	 * The listener interface for receiving masterButtonSlaveSelection events. The class that is interested in
	 * processing a masterButtonSlaveSelection event implements this interface, and the object created with that class
	 * is registered with a component using the component's <code>addMasterButtonSlaveSelectionListener</code> method.
	 * When the masterButtonSlaveSelection event occurs, that object's appropriate method is invoked.
	 *
	 * @see MasterButtonSlaveSelectionEvent
	 */
	private static class MasterButtonSlaveSelectionListener implements SelectionListener {

		/** The master. */
		private final Button master;

		/** The slave. */
		private final Control slave;

		/**
		 * Instantiates a new master button slave selection listener.
		 *
		 * @param master
		 *            the master
		 * @param slave
		 *            the slave
		 */
		public MasterButtonSlaveSelectionListener(final Button master, final Control slave) {
			this.master = master;
			this.slave = slave;
		}

		@Override
		public void widgetDefaultSelected(final SelectionEvent e) {

		}

		@Override
		public void widgetSelected(final SelectionEvent e) {
			updateSlaveComponent();
		}

		/**
		 * Update slave component.
		 */
		private void updateSlaveComponent() {
			boolean state = master.getSelection();
			slave.setEnabled(state);
		}

	}

}
