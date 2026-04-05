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
package gama.ui.viewers.json.eclipse;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.DefaultCharacterPairMatcher;
import org.eclipse.jface.text.source.ICharacterPairMatcher;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;

public final class JsonBracketsSupport extends DefaultCharacterPairMatcher {
    protected final static char[] BRACKETS = { '{', '}', '[', ']' };
    private List<IRegion> previousSelections;

    public JsonBracketsSupport() {
        super(BRACKETS, IDocumentExtension3.DEFAULT_PARTITIONING, true);
    }

    /**
     * Goto matching bracket
     * 
     * @param adaptable must support {@link StatusMessageSupport} and
     *                  {@link ISourceViewer} otherwise method will do nothing
     */
    public void gotoMatchingBracket(IAdaptable adaptable) {
        ISourceViewer sourceViewer = adaptable.getAdapter(ISourceViewer.class);
        StatusMessageSupport statusMessageSupport = adaptable.getAdapter(StatusMessageSupport.class);
        if (sourceViewer == null) {
            return;
        }
        if (statusMessageSupport == null) {
            return;
        }
        IDocument document = sourceViewer.getDocument();
        if (document == null)
            return;

        IRegion selection = getSignedSelection(sourceViewer);
        if (previousSelections == null) {
            initializePreviousSelectionList();
        }

        IRegion region = match(document, selection.getOffset(), selection.getLength());
        if (region == null) {
            region = findEnclosingPeerCharacters(document, selection.getOffset(), selection.getLength());
            initializePreviousSelectionList();
            previousSelections.add(selection);
        } else {
            if (previousSelections.size() == 2) {
                if (!selection.equals(previousSelections.get(1))) {
                    initializePreviousSelectionList();
                }
            } else if (previousSelections.size() == 3) {
                if (selection.equals(previousSelections.get(2)) && !selection.equals(previousSelections.get(0))) {
                    IRegion originalSelection = previousSelections.get(0);
                    sourceViewer.setSelectedRange(originalSelection.getOffset(), originalSelection.getLength());
                    sourceViewer.revealRange(originalSelection.getOffset(), originalSelection.getLength());
                    initializePreviousSelectionList();
                    return;
                }
                initializePreviousSelectionList();
            }
        }

        if (region == null) {
            statusMessageSupport.setErrorMessage("Can't go to matching bracket");
            sourceViewer.getTextWidget().getDisplay().beep();
            return;
        }

        int offset = region.getOffset();
        int length = region.getLength();

        if (length < 1)
            return;

        int anchor = getAnchor();
        int targetOffset = (ICharacterPairMatcher.RIGHT == anchor) ? offset + 1 : offset + length - 1;

        boolean visible = false;
        if (sourceViewer instanceof ITextViewerExtension5) {
            ITextViewerExtension5 extension = (ITextViewerExtension5) sourceViewer;
            visible = (extension.modelOffset2WidgetOffset(targetOffset) > -1);
        } else {
            IRegion visibleRegion = sourceViewer.getVisibleRegion();
            // http://dev.eclipse.org/bugs/show_bug.cgi?id=34195
            visible = (targetOffset >= visibleRegion.getOffset() && targetOffset <= visibleRegion.getOffset() + visibleRegion.getLength());
        }

        if (!visible) {
            statusMessageSupport.setErrorMessage("Matching bracket outside selected type");
            sourceViewer.getTextWidget().getDisplay().beep();
            return;
        }

        int adjustment = getOffsetAdjustment(document, selection.getOffset() + selection.getLength(), selection.getLength());
        targetOffset += adjustment;
        int direction = (selection.getLength() == 0) ? 0 : ((selection.getLength() > 0) ? 1 : -1);
        if (previousSelections.size() == 1 && direction < 0) {
            targetOffset++;
        }

        if (previousSelections.size() > 0) {
            previousSelections.add(new Region(targetOffset, direction));
        }
        sourceViewer.setSelectedRange(targetOffset, direction);
        sourceViewer.revealRange(targetOffset, direction);
    }

    /*
     * Copy of org.eclipse.jface.text.source.DefaultCharacterPairMatcher.
     * getOffsetAdjustment(IDocument, int, int)
     */
    protected int getOffsetAdjustment(IDocument document, int offset, int length) {
        if (length == 0 || Math.abs(length) > 1)
            return 0;
        try {
            if (length < 0) {
                if (isOpeningBracket(document.getChar(offset))) {
                    return 1;
                }
            } else {
                if (isClosingBracket(document.getChar(offset - 1))) {
                    return -1;
                }
            }
        } catch (BadLocationException e) {
            // do nothing
        }
        return 0;
    }

    /*
     * Copy of
     * org.eclipse.jface.text.source.MatchingCharacterPainter.getSignedSelection
     * (ISourceViewer)
     */
    protected final IRegion getSignedSelection(ISourceViewer sourceViewer) {
        Point viewerSelection = sourceViewer.getSelectedRange();

        StyledText text = sourceViewer.getTextWidget();
        Point selection = text.getSelectionRange();
        if (text.getCaretOffset() == selection.x) {
            viewerSelection.x = viewerSelection.x + viewerSelection.y;
            viewerSelection.y = -viewerSelection.y;
        }

        return new Region(viewerSelection.x, viewerSelection.y);
    }

    protected boolean isClosingBracket(char character) {
        for (int i = 1; i < BRACKETS.length; i += 2) {
            if (character == BRACKETS[i])
                return true;
        }
        return false;
    }

    protected boolean isOpeningBracket(char character) {
        for (int i = 0; i < BRACKETS.length; i += 2) {
            if (character == BRACKETS[i])
                return true;
        }
        return false;
    }

    private void initializePreviousSelectionList() {
        previousSelections = new ArrayList<>(3);
    }

}