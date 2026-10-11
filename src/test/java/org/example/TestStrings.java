package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class TestStrings {
    private TestStrings() {
    }

    public static List<String> allUpTo(Set<Character> alphabet, int maxLength) {
        List<String> all = new ArrayList<>(List.of(""));
        List<String> previous = List.of("");
        for (int length = 1; length <= maxLength; length++) {
            List<String> current = new ArrayList<>();
            for (String prefix : previous) {
                for (char symbol : alphabet) {
                    current.add(prefix + symbol);
                }
            }
            all.addAll(current);
            previous = current;
        }
        return all;
    }
}
