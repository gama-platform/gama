package gama.ui.viewers.json.eclipse.preferences;

/*
 * Copyright 2020 Albert Tregnaghi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 */

/**
 * Constant definitions for plug-in preferences
 */
public enum JsonEditorPreferenceConstants implements PreferenceIdentifiable {

    P_EDITOR_MATCHING_BRACKETS_ENABLED("matchingBrackets"), P_EDITOR_ALLOW_COMMENTS_ENABLED("allowComments"), P_EDITOR_ALLOW_UNQUOTED_CONTROL_CHARS("allowUnquotedControlChars"),
    P_EDITOR_HIGHLIGHT_BRACKET_AT_CARET_LOCATION("highlightBracketAtCaretLocation"), P_EDITOR_ENCLOSING_BRACKETS("enclosingBrackets"), P_EDITOR_MATCHING_BRACKETS_COLOR("matchingBracketsColor"),
    P_EDITOR_AUTO_CREATE_END_BRACKETSY("autoCreateEndBrackets"),

    P_LINK_EDITOR_WITH_OUTLINE("linkEditorWithOutline"), P_CREATE_OUTLINE_FOR_NEW_EDITOR("createOutlineForEditor"), P_CREATE_GROUPED_ARRAYS_TRESHOLD("createGroupedArraysTreshold"),

    P_VALIDATE_ON_SAVE("validateOnSaveEnabled"),

    P_CODE_ASSIST_ADD_KEYWORDS("codeAssistAddsKeyWords"), P_CODE_ASSIST_ADD_SIMPLEWORDS("codeAssistAddsSimpleWords"),

    ;

    private String id;

    private JsonEditorPreferenceConstants(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
