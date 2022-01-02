/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.filoghost.holographicdisplays.plugin.placeholder.parsing;

import me.filoghost.holographicdisplays.plugin.placeholder.PlaceholderOccurrence;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

class Parser {

    private static final char PLACEHOLDER_START_CHAR = '{';
    private static final char PLACEHOLDER_END_CHAR = '}';
    private static final char ESCAPE_CHAR = '\\';

    /**
     * Returns null if the string doesn't contain any placeholder (optimization).
     */
    static @Nullable List<Part> parse(@NotNull String string) {
        List<Part> parts = null;
        int placeholderStartIndex = -1;
        int lastAppendIndex = 0;
        boolean escapeNext = false;

        for (int currentIndex = 0; currentIndex < string.length(); currentIndex++) {
            char currentChar = string.charAt(currentIndex);

            if (placeholderStartIndex >= 0) {
                if (escapeNext) {
                    escapeNext = false;
                } else if (currentChar == ESCAPE_CHAR) {
                    escapeNext = true;
                } else if (currentChar == PLACEHOLDER_END_CHAR) {
                    // Inside placeholder
                    int endIndex = currentIndex + 1;

                    // The unparsed string includes the opening and closing tags (e.g.: "{online: lobby}")
                    String unparsedString = removeEscapes(string.substring(placeholderStartIndex, endIndex));

                    // The content string does NOT include the opening and closing tags (e.g.: "online: lobby")
                    String contentString = unparsedString.substring(1, unparsedString.length() - 1);
                    PlaceholderOccurrence content = PlaceholderOccurrence.parse(contentString);

                    if (parts == null) {
                        parts = new ArrayList<>();
                    }

                    // Append leading literal part (if any)
                    if (placeholderStartIndex != lastAppendIndex) {
                        parts.add(new StringPart(string.substring(lastAppendIndex, placeholderStartIndex)));
                    }

                    // Append placeholder part
                    parts.add(new PlaceholderPart(content, unparsedString));
                    lastAppendIndex = endIndex;
                    placeholderStartIndex = -1;

                } else if (currentChar == PLACEHOLDER_START_CHAR) {
                    // Nested placeholder, ignore outer placeholder and update start index
                    placeholderStartIndex = currentIndex;
                }
            } else {
                // Outside placeholders, just look for the start of a placeholder
                if (currentChar == PLACEHOLDER_START_CHAR) {
                    placeholderStartIndex = currentIndex;
                }
            }
        }

        // Append trailing literal part (if any)
        if (lastAppendIndex != string.length() && parts != null) {
            parts.add(new StringPart(string.substring(lastAppendIndex)));
        }

        return parts;
    }

    private static String removeEscapes(String string) {
        StringBuilder output = new StringBuilder(string.length());
        boolean escape = false;

        for (int currentIndex = 0; currentIndex < string.length(); currentIndex++) {
            char currentChar = string.charAt(currentIndex);

            if (escape) {
                escape = false;
            } else if (currentChar == ESCAPE_CHAR) {
                escape = true;
                continue; // Do not append the escape character
            }

            output.append(currentChar);
        }

        return output.toString();
    }

}
