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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This builder build words from a given source.
 * 
 * @author albert
 *
 */
public class SimpleWordListBuilder implements WordListBuilder {

    /*
     * (non-Javadoc)
     * 
     * @see gama.ui.viewers.json.WordListBuilder#build(java.lang.String)
     */
    @Override
    public List<String> build(String source) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyList();
        }
        String[] allWords = source.split("[\\s,;:.!()\\?=]");
        List<String> list = new ArrayList<>();
        for (String word : allWords) {
            String transformed = transformIfNecessary(word);
            if (transformed != null && !transformed.isEmpty()) {
                list.add(transformed);
            }
        }
        return list;
    }

    private String transformIfNecessary(String word) {
        if (word == null) {
            return null;
        }
        if (word.isEmpty()) {
            return null;
        }

        String transformed = word;
        /* start */
        if (transformed.startsWith("#")) {
            transformed = dropFirstChar(transformed);
        }
        if (transformed.startsWith("'")) {
            transformed = dropFirstChar(transformed);
        }
        if (transformed.startsWith("\"")) {
            transformed = dropFirstChar(transformed);
        }

        /* end */
        if (transformed.endsWith("'")) {
            transformed = dropLastChar(transformed);
        }
        if (transformed.endsWith("\"")) {
            transformed = dropLastChar(transformed);
        }
        return transformed;
    }

    private String dropLastChar(String transformed) {
        return transformed.substring(0, transformed.length() - 1);
    }

    private String dropFirstChar(String transformed) {
        transformed = transformed.substring(1);
        return transformed;
    }
}
