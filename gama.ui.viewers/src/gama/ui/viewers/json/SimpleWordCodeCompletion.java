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

import static java.util.Collections.*;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class SimpleWordCodeCompletion {

    private Set<String> additionalWordsCache = new HashSet<>();

    private SortedSet<String> allWordsCache = new TreeSet<>();

    private WordListBuilder wordListBuilder;

    /**
     * Adds an additional word - will be removed on all of {@link #reset()}
     * 
     * @param word
     */
    public void add(String word) {
        if (word == null) {
            return;
        }
        if (!allWordsCache.isEmpty()) {
            allWordsCache.clear(); // reset the all words cache so rebuild will
                                   // be triggered
        }
        additionalWordsCache.add(word.trim());
    }

    /**
     * Calculates the resulting proposals for given offset.
     * 
     * @param source
     * @param offset
     * @return proposals, never <code>null</code>
     */
    public Set<String> calculate(String source, int offset) {
        rebuildCacheIfNecessary(source);
        if (offset == 0) {
            return unmodifiableSet(allWordsCache);
        }
        String wanted = getTextbefore(source, offset);
        return filter(allWordsCache, wanted);
    }

    /**
     * Resolves text before given offset
     * 
     * @param source
     * @param offset
     * @return text, never <code>null</code>
     */
    public String getTextbefore(String source, int offset) {
        if (source == null || source.isEmpty()) {
            return "";
        }
        if (offset <= 0) {
            return "";
        }
        int sourceLength = source.length();
        if (offset > sourceLength) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int current = offset - 1; // -1 because we want the char before
        boolean ongoing = false;
        do {
            if (current < 0) {
                break;
            }
            char c = source.charAt(current--);
            ongoing = !Character.isWhitespace(c);
            if (ongoing) {
                sb.insert(0, c);
            }
        } while (ongoing);

        return sb.toString();
    }

    /**
     * Reset allWordsCache
     * 
     * @return completion
     */
    public SimpleWordCodeCompletion reset() {
        allWordsCache.clear();
        additionalWordsCache.clear();
        return this;
    }

    Set<String> filter(SortedSet<String> allWords, String wanted) {
        if (wanted == null || wanted.isEmpty()) {
            return allWords;
        }
        LinkedHashSet<String> filtered = new LinkedHashSet<>();
        LinkedHashSet<String> addAfterEnd = new LinkedHashSet<>();
        String wantedLowerCase = wanted.toLowerCase();

        for (String word : allWords) {
            String wordLowerCase = word.toLowerCase();
            if (wordLowerCase.startsWith(wantedLowerCase)) {
                filtered.add(word);
            } else if (wordLowerCase.indexOf(wantedLowerCase) != -1) {
                addAfterEnd.add(word);
            }
        }
        filtered.addAll(addAfterEnd);
        /* remove wanted itself */
        filtered.remove(wanted);
        return filtered;
    }

    private void rebuildCacheIfNecessary(String source) {
        if (allWordsCache.isEmpty()) {
            allWordsCache.addAll(additionalWordsCache);
            allWordsCache.addAll(getWordListBuilder().build(source));
            // we do not want the empty String
            allWordsCache.remove("");
        }
    }

    public WordListBuilder getWordListBuilder() {
        if (wordListBuilder == null) {
            wordListBuilder = new SimpleWordListBuilder();
        }
        return wordListBuilder;
    }

    public void setWordListBuilder(WordListBuilder wordListBuilder) {
        this.wordListBuilder = wordListBuilder;
    }
}
