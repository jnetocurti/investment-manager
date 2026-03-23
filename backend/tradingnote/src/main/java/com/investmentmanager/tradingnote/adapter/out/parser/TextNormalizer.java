package com.investmentmanager.tradingnote.adapter.out.parser;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class TextNormalizer {

    static String normalize(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return "";
        }
        String text = rawText;
        text = removeDuplicatedChars(text);
        text = text.replaceAll("[ \\t]+", " ");
        text = text.replaceAll("\\r\\n?", "\n");
        text = text.replaceAll("\\n{3,}", "\n\n");
        return text.trim();
    }

    private static String removeDuplicatedChars(String text) {
        StringBuilder result = new StringBuilder();
        String[] lines = text.split("\\n");
        for (String line : lines) {
            if (isDuplicatedLine(line.trim())) {
                result.append(dedup(line.trim()));
            } else {
                result.append(line);
            }
            result.append("\n");
        }
        return result.toString();
    }

    private static boolean isDuplicatedLine(String line) {
        if (line.length() < 4) return false;
        int checkLen = Math.min(line.length(), 20);
        int matches = 0;
        for (int i = 0; i < checkLen - 1; i += 2) {
            if (line.charAt(i) == line.charAt(i + 1)) {
                matches++;
            }
        }
        return matches >= (checkLen / 2) * 0.7;
    }

    private static String dedup(String line) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < line.length()) {
            sb.append(line.charAt(i));
            if (i + 1 < line.length() && line.charAt(i) == line.charAt(i + 1)) {
                i += 2;
            } else {
                i++;
            }
        }
        return sb.toString();
    }
}
