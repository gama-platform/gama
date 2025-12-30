/*
 * Copyright 2021 Albert Tregnaghi
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
package gama.ui.viewers.json;

import java.util.List;

public interface WordListBuilder {

    /**
     * Build words from source
     * 
     * @param source
     * @return words from given source. Separators are ,;.!?= If there are single or
     *         double quotes at end or start those are removed. Never
     *         <code>null</code>
     */
    List<String> build(String source);

}