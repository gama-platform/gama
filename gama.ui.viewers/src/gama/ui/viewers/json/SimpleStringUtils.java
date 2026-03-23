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
package gama.ui.viewers.json;

public class SimpleStringUtils {
    private static final String EMPTY = "";

    public static boolean equals(String text1, String text2) {
        if (text1 == null) {
            if (text2 == null) {
                return true;
            }
            return false;
        }
        if (text2 == null) {
            return false;
        }
        return text2.equals(text1);
    }

    public static String shortString(String string, int max) {
        if (max == 0) {
            return EMPTY;
        }
        if (string == null) {
            return EMPTY;
        }
        if (string.length() <= max) {
            return string;
        }
        /* length > max */
        if (max == 1) {
            return ".";
        }
        if (max == 2) {
            return "..";
        }
        if (max == 3) {
            return "...";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(string.substring(0, max - 3));
        sb.append("...");
        return sb.toString();
    }
}
