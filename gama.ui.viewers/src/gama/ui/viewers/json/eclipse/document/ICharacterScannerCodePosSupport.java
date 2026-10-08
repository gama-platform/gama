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
package gama.ui.viewers.json.eclipse.document;

import org.eclipse.jface.text.rules.ICharacterScanner;

public class ICharacterScannerCodePosSupport implements CodePosSupport {

    private ICharacterScanner scanner;
    private int startPos;
    private Counter counter;
    private int lastReadValue = ICharacterScanner.EOF;
    private int pos;

    public ICharacterScannerCodePosSupport(ICharacterScanner scanner) {
        this.scanner = scanner;
        this.counter = new Counter();
        // startPos start will always be 0. this is okay, here, because no tokens will
        // be created so the information can start from 0 and
        // is okay. also an ICharacterScanner does not support real startPos handling
        // but only forward and backward
        this.startPos = 0;
        this.pos = -1;
    }

    @Override
    public void moveToPos(int newPos) {
        if (newPos == pos) {
            return;
        }

        while (newPos < pos) {
            moveBack();
        }

        while (newPos > pos) {
            moveForward();
        }

    }

    private void moveForward() {
        pos++;
        lastReadValue = scanner.read();
        counter.count++;

    }

    private void moveBack() {
        pos--;

        scanner.unread();
        scanner.unread();
        lastReadValue = scanner.read();
        counter.count--;
    }

    @Override
    public int getInitialStartPos() {
        return startPos;
    }

    @Override
    public Character getCharacterAtPosOrNull(int pos) {
        moveToPos(pos);
        if (lastReadValue == ICharacterScanner.EOF) {
            return null;
        }
        char lastCharacter = (char) lastReadValue;
        return Character.valueOf(lastCharacter);
    }

    /**
     * Reset cursor movements
     */
    public void resetToStartPos() {
        counter.cleanupAndReturn(scanner, false);
    }

}
