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

public interface CodePosSupport {

    /**
     * Moves to new position
     * 
     * @param newPos
     */
    void moveToPos(int newPos);

    /**
     * Get initial start position inside code fragment
     * 
     * @return start position
     */
    int getInitialStartPos();

    /**
     * Gives back character at wanted position, or <code>null</code>
     * 
     * @param pos
     * @return character at wanted position, or <code>null</code>
     */
    Character getCharacterAtPosOrNull(int pos);

}
