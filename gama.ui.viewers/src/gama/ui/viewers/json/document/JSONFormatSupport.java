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
package gama.ui.viewers.json.document;

import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JSONFormatSupport {

    private static final int MAXIMUM_POSITIONS_TO_SCAN = 2000;
    
    private static String STRING_PATTERN_SINGLE_LINE_REDUCE="\\n";
    private static Pattern PATTERN_SINGLE_LINE_REDUCE = Pattern.compile(STRING_PATTERN_SINGLE_LINE_REDUCE);
    private static String STRING_WHITESPACES_BEFORE_QUOTE="[ \\t\\n]+\"";
    private static Pattern PATTERN_SPACES_BEFORE_QUOTE = Pattern.compile(STRING_WHITESPACES_BEFORE_QUOTE);
    private static String STRING_WHITESPACES_AFTER_QUOTE="\"[ \\t\\n]+";
    private static Pattern PATTERN_SPACES_AFTER_QUOTE = Pattern.compile(STRING_WHITESPACES_AFTER_QUOTE);
    
    private static String STRING_COLONS_NO_WHITESPACES_AFTER_QUOTE="\":";
    private static Pattern PATTERN_COLONS_NO_WHITESPACES_AFTER_QUOTE= Pattern.compile(STRING_COLONS_NO_WHITESPACES_AFTER_QUOTE);
    
    private static String STRING_COLONS_NO_WHITESPACES_BEFORE_QUOTE=":\"";
    private static Pattern PATTERN_COLONS_NO_WHITESPACES_BEFORE_QUOTE= Pattern.compile(STRING_COLONS_NO_WHITESPACES_BEFORE_QUOTE);
    
    public class FormatterResult {
        private String origin;
        private String formatted;
        public FormatterResultState state;
        public String message;
        public int line;
        public int column;
        public long offset;

        public String getOrigin() {
            return origin;
        }

        public String getFormatted() {
            return formatted;
        }

        public FormatterResult(String origin, String formatted, FormatterResultState state) {
            this.state = state;
            this.origin = origin;
            this.formatted = formatted;
        }

        public FormatterResult(String origin, String formatted, FormatterResultState state, String message) {
            this(origin, formatted, state);
            this.message = message;
        }

    }

    public enum FormatterResultState {

        KEPT_AS_IS,

        FORMAT_DONE,

        NOT_VALID_JSON_BUT_FALLBACK_DONE,

        NOT_VALID_JSON_NO_FALLBACK_POSSIBLE_SO_KEEP_AS_IS,

        ;

        public boolean hasContentChanged() {
            return this == FORMAT_DONE || this == NOT_VALID_JSON_BUT_FALLBACK_DONE;
        }

    }

    public static final JSONFormatSupport DEFAULT = new JSONFormatSupport();
    private static final ObjectMapper mapper = new ObjectMapper();

    public void setAllowComents(boolean allowComents) {
        if (allowComents) {
            mapper.enable(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_COMMENTS);
        } else {
            mapper.disable(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_COMMENTS);
        }
    }

    public void setAllowUnquotedControlChars(boolean allowNewlines) {
        if (allowNewlines) {
            mapper.enable(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS);
        } else {
            mapper.disable(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS);
        }
    }

    public FormatterResult formatJSONIfNotHavingMinAmountOfNewLines(String str) {
        return formatJSONIfNotHavingMinAmountOfNewLines(str, 3, 1000);
    }

    public JsonNode read(String json) throws JsonMappingException, JsonProcessingException {
        return mapper.readTree(json);
    }

    FormatterResult formatJSONIfNotHavingMinAmountOfNewLines(String str, int minNewLines, int minLengthWithoutAutoChange) {
        if (str == null) {
            return new FormatterResult(str, null, FormatterResultState.NOT_VALID_JSON_NO_FALLBACK_POSSIBLE_SO_KEEP_AS_IS, "empty string is no valid json");
        }
        if (str.length() < minLengthWithoutAutoChange) {
            return new FormatterResult(str, str, FormatterResultState.KEPT_AS_IS);
        }
        int count = 0;
        int pos = 0;
        while (count < minNewLines && pos < MAXIMUM_POSITIONS_TO_SCAN) {
            if (str.length() <= pos) {
                break;
            }
            char c = str.charAt(pos);
            if (c == '\n') {
                count++;
            }
            pos++;
        }
        if (count < minNewLines) {
            return formatJSON(str);
        }
        return new FormatterResult(str, str, FormatterResultState.KEPT_AS_IS);
    }

    public FormatterResult formatJSON(String text) {
        if (text == null) {
            return new FormatterResult(null, null, FormatterResultState.NOT_VALID_JSON_NO_FALLBACK_POSSIBLE_SO_KEEP_AS_IS, "text null not acceptable");
        }
        try {
            String formatted = formatJSONByJacksonMapper(text);
            return new FormatterResult(text, formatted, FormatterResultState.FORMAT_DONE);
        } catch (JsonProcessingException e) {

            JsonProcessingException inspect = null;

            FormatterResult result = formatJSONByFallback(text);
            if (result.state == FormatterResultState.NOT_VALID_JSON_BUT_FALLBACK_DONE) {
                try {
                    formatJSONByJacksonMapper(text);
                } catch (JsonProcessingException e2) {
                    inspect = e2;
                }
            } else {
                inspect = e;
            }
            if (inspect == null) {
                result.message = "illegal state - after fallback format for invalid json - json has become valid?!?!?!";
            } else {
                JsonLocation location = inspect.getLocation();
                result.column = location.getColumnNr();
                result.line = location.getLineNr();
                result.offset = location.getCharOffset();
                result.message = inspect.getMessage();
            }
            return result;
        }

    }

    private String formatJSONByJacksonMapper(String text) throws JsonProcessingException, JsonMappingException {
        Object json = mapper.readValue(text, Object.class);
        String indented = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        return indented;
    }

    private FormatterResult formatJSONByFallback(String text) {
        if (text.indexOf("{\n") == -1) {
            String formatted = text.replaceAll("\\{", "\\{\n");
            return new FormatterResult(text, formatted, FormatterResultState.NOT_VALID_JSON_BUT_FALLBACK_DONE);
        }

        return new FormatterResult(text, text, FormatterResultState.NOT_VALID_JSON_NO_FALLBACK_POSSIBLE_SO_KEEP_AS_IS);
    }

    public void validateJSON(String documentText) throws JsonProcessingException {
        mapper.readTree(documentText);
    }

    public String createJSONAsOneLine(String json) {
        json = PATTERN_SPACES_BEFORE_QUOTE.matcher(json).replaceAll(" \"");
        json = PATTERN_SPACES_AFTER_QUOTE.matcher(json).replaceAll("\" ");
        
        json = PATTERN_COLONS_NO_WHITESPACES_BEFORE_QUOTE.matcher(json).replaceAll(": \"");
        json = PATTERN_COLONS_NO_WHITESPACES_AFTER_QUOTE.matcher(json).replaceAll("\" :");

        json = PATTERN_SINGLE_LINE_REDUCE.matcher(json).replaceAll("");
        
        return json.trim();
    }

}
