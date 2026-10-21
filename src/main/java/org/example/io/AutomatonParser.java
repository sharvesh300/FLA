package org.example.io;

import org.example.automata.Dfa;
import org.example.automata.Nfa;
import org.example.automata.Pda;
import org.example.automata.Symbols;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;

// Text format: 'key: values' header lines and 'lhs -> rhs' transition lines; '#' starts a comment.
public final class AutomatonParser {
    private static final String ARROW = "->";
    private static final Set<String> FINITE_AUTOMATON_KEYS = Set.of("states", "alphabet", "start", "accept");
    private static final Set<String> PDA_KEYS =
            Set.of("states", "alphabet", "stack", "start", "initial-stack", "accept", "accept-by");

    private AutomatonParser() {
    }

    public static Dfa readDfa(Path file) throws IOException {
        return parseDfa(Files.readString(file));
    }

    public static Nfa readNfa(Path file) throws IOException {
        return parseNfa(Files.readString(file));
    }

    public static Pda readPda(Path file) throws IOException {
        return parsePda(Files.readString(file));
    }

    public static Dfa parseDfa(String text) {
        Definition definition = Definition.parse(text, FINITE_AUTOMATON_KEYS);
        Map<String, Map<Character, String>> transitions = new LinkedHashMap<>();
        for (TransitionLine line : definition.transitions()) {
            line.requireShape(2, 1, 1, "state symbol -> state");
            String from = line.left().get(0);
            char symbol = line.symbol(line.left().get(1), false);
            String to = line.right().get(0);
            // put() returns the old target if this (state, symbol) was already defined.
            if (transitions.computeIfAbsent(from, key -> new LinkedHashMap<>()).put(symbol, to) != null) {
                throw line.error("a DFA allows only one transition from " + from + " on '" + symbol + "'");
            }
        }
        return build(() -> new Dfa(
                definition.values("states"),
                definition.symbols("alphabet"),
                transitions,
                definition.single("start"),
                definition.optionalValues("accept")));
    }

    public static Nfa parseNfa(String text) {
        Definition definition = Definition.parse(text, FINITE_AUTOMATON_KEYS);
        Map<String, Map<Character, Set<String>>> transitions = new LinkedHashMap<>();
        for (TransitionLine line : definition.transitions()) {
            line.requireShape(2, 1, Integer.MAX_VALUE, "state symbol -> state...");
            char symbol = line.symbol(line.left().get(1), true);
            transitions.computeIfAbsent(line.left().get(0), key -> new LinkedHashMap<>())
                    .computeIfAbsent(symbol, key -> new LinkedHashSet<>())
                    .addAll(line.right());
        }
        return build(() -> new Nfa(
                definition.values("states"),
                definition.symbols("alphabet"),
                transitions,
                definition.single("start"),
                definition.optionalValues("accept")));
    }

    public static Pda parsePda(String text) {
        Definition definition = Definition.parse(text, PDA_KEYS);
        List<Pda.Transition> transitions = new ArrayList<>();
        for (TransitionLine line : definition.transitions()) {
            line.requireShape(3, 2, 2, "state input pop -> state push");
            // Pushing ε means pushing nothing.
            String push = line.right().get(1);
            transitions.add(new Pda.Transition(
                    line.left().get(0),
                    line.symbol(line.left().get(1), true),
                    line.symbol(line.left().get(2), true),
                    line.right().get(0),
                    Symbols.isEpsilonToken(push) ? "" : push));
        }
        return build(() -> new Pda(
                definition.values("states"),
                definition.symbols("alphabet"),
                definition.symbols("stack"),
                transitions,
                definition.single("start"),
                definition.symbol("initial-stack"),
                definition.optionalValues("accept"),
                definition.acceptanceMode()));
    }

    // Report constructor validation errors as format errors.
    private static <T> T build(Supplier<T> constructor) {
        try {
            return constructor.get();
        } catch (AutomatonFormatException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw new AutomatonFormatException("invalid automaton: " + e.getMessage());
        }
    }

    private static List<String> tokens(String text) {
        String stripped = text.strip();
        return stripped.isEmpty() ? List.of() : List.of(stripped.split("\\s+"));
    }

    private static char parseSymbol(int line, String token, boolean allowEpsilon) {
        if (Symbols.isEpsilonToken(token)) {
            if (allowEpsilon) {
                return Symbols.EPSILON;
            }
            throw new AutomatonFormatException(line, "ε is not allowed here");
        }
        if (token.length() != 1) {
            throw new AutomatonFormatException(line, "'" + token + "' must be a single character");
        }
        return token.charAt(0);
    }

    private record Header(int line, List<String> values) {
    }

    private record TransitionLine(int line, List<String> left, List<String> right) {

        static TransitionLine parse(int line, String text) {
            int arrow = text.indexOf(ARROW);
            return new TransitionLine(line, tokens(text.substring(0, arrow)), tokens(text.substring(arrow + ARROW.length())));
        }

        void requireShape(int leftCount, int minRight, int maxRight, String expectedForm) {
            if (left.size() != leftCount || right.size() < minRight || right.size() > maxRight) {
                throw error("expected a transition of the form '" + expectedForm + "'");
            }
        }

        char symbol(String token, boolean allowEpsilon) {
            return parseSymbol(line, token, allowEpsilon);
        }

        AutomatonFormatException error(String message) {
            return new AutomatonFormatException(line, message);
        }
    }

    // The file split into headers and transition lines, before building an automaton.
    private static final class Definition {
        private final Map<String, Header> headers = new HashMap<>();
        private final List<TransitionLine> transitions = new ArrayList<>();

        static Definition parse(String text, Set<String> allowedKeys) {
            Definition definition = new Definition();
            List<String> lines = text.lines().toList();
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i).strip();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                if (line.contains(ARROW)) {
                    definition.transitions.add(TransitionLine.parse(i + 1, line));
                } else {
                    definition.addHeader(i + 1, line, allowedKeys);
                }
            }
            return definition;
        }

        private void addHeader(int line, String text, Set<String> allowedKeys) {
            int colon = text.indexOf(':');
            if (colon < 0) {
                throw new AutomatonFormatException(line, "expected 'key: values' or a transition containing '->'");
            }
            String key = text.substring(0, colon).strip().toLowerCase(Locale.ROOT);
            if (!allowedKeys.contains(key)) {
                throw new AutomatonFormatException(line,
                        "unknown key '" + key + "', expected one of " + new TreeSet<>(allowedKeys));
            }
            if (headers.put(key, new Header(line, tokens(text.substring(colon + 1)))) != null) {
                throw new AutomatonFormatException(line, "'" + key + "' is declared more than once");
            }
        }

        List<TransitionLine> transitions() {
            return transitions;
        }

        Set<String> values(String key) {
            return new LinkedHashSet<>(require(key).values());
        }

        Set<String> optionalValues(String key) {
            Header header = headers.get(key);
            return header == null ? Set.of() : new LinkedHashSet<>(header.values());
        }

        String single(String key) {
            Header header = require(key);
            if (header.values().size() != 1) {
                throw new AutomatonFormatException(header.line(), "'" + key + "' expects exactly one value");
            }
            return header.values().get(0);
        }

        Set<Character> symbols(String key) {
            Header header = require(key);
            Set<Character> symbols = new LinkedHashSet<>();
            for (String token : header.values()) {
                symbols.add(parseSymbol(header.line(), token, false));
            }
            return symbols;
        }

        char symbol(String key) {
            return parseSymbol(require(key).line(), single(key), false);
        }

        Pda.AcceptanceMode acceptanceMode() {
            if (!headers.containsKey("accept-by")) {
                return Pda.AcceptanceMode.FINAL_STATE;
            }
            return switch (single("accept-by")) {
                case "final-state" -> Pda.AcceptanceMode.FINAL_STATE;
                case "empty-stack" -> Pda.AcceptanceMode.EMPTY_STACK;
                default -> throw new AutomatonFormatException(headers.get("accept-by").line(),
                        "'accept-by' must be 'final-state' or 'empty-stack'");
            };
        }

        private Header require(String key) {
            Header header = headers.get(key);
            if (header == null || header.values().isEmpty()) {
                throw new AutomatonFormatException("missing '" + key + ":' declaration");
            }
            return header;
        }
    }
}
