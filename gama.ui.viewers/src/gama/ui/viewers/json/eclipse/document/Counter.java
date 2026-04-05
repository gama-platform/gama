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

class Counter {
    int count;

    boolean cleanupAndReturn(ICharacterScanner scanner, boolean result) {
        if (result) {
            return true; // do not clean up - pos is as wanted
        }
        if (count == 0) {
            return result;
        }
        if (count > 0) {
            while (count != 0) {
                scanner.unread();
                count--;
            }
        } else {
            while (count != 0) {
                scanner.read();
                count++;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "count:" + count;
    }

}