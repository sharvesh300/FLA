package org.example;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

public final class Main {
    private static final String USAGE = """
            Usage:
              dfa <file> <input>...        simulate a DFA on each input string
              nfa2dfa <file>               convert an NFA to a DFA (subset construction)
              minimize <file>              minimize a DFA
              intersect <file1> <file2>    build a DFA for L1 ∩ L2 (product construction)
              pda <file> <input>...        simulate a PDA on each input string
            Use ε (or eps) for the empty input string. Run without arguments for an interactive menu.""";

    private Main() {
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            new InteractiveMenu(new Scanner(System.in), System.out).run();
            return;
        }
        try {
            System.out.println(execute(List.of(args)));
        } catch (NoSuchFileException e) {
            exitWithError("file not found: " + e.getFile());
        } catch (IOException e) {
            exitWithError("cannot read file: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            exitWithError(e.getMessage());
        }
    }

    static String execute(List<String> args) throws IOException {
        String command = args.get(0);
        List<String> rest = args.subList(1, args.size());
        return switch (command) {
            case "dfa" -> Tasks.simulateDfa(file(rest, 2, Integer.MAX_VALUE), rest.subList(1, rest.size()));
            case "nfa2dfa" -> Tasks.convertNfa(file(rest, 1, 1));
            case "minimize" -> Tasks.minimizeDfa(file(rest, 1, 1));
            case "intersect" -> Tasks.intersectDfas(file(rest, 2, 2), Path.of(rest.get(1)));
            case "pda" -> Tasks.simulatePda(file(rest, 2, Integer.MAX_VALUE), rest.subList(1, rest.size()));
            case "help", "--help", "-h" -> USAGE;
            default -> throw new IllegalArgumentException("unknown command '" + command + "'\n" + USAGE);
        };
    }

    private static Path file(List<String> args, int min, int max) {
        if (args.size() < min || args.size() > max) {
            throw new IllegalArgumentException("wrong number of arguments\n" + USAGE);
        }
        return Path.of(args.get(0));
    }

    private static void exitWithError(String message) {
        System.err.println("Error: " + message);
        System.exit(1);
    }
}
