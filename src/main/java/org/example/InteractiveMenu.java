package org.example;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

final class InteractiveMenu {
    private static final String MENU = """

            1) DFA simulation
            2) NFA to DFA conversion
            3) DFA minimization
            4) Intersection of two DFAs
            5) PDA simulation
            0) Exit""";

    private final Scanner scanner;
    private final PrintStream out;

    InteractiveMenu(Scanner scanner, PrintStream out) {
        this.scanner = scanner;
        this.out = out;
    }

    void run() {
        try {
            while (true) {
                out.println(MENU);
                String choice = prompt("Choose a task: ");
                if (choice.equals("0")) {
                    return;
                }
                try {
                    out.println(perform(choice));
                } catch (NoSuchFileException e) {
                    out.println("Error: file not found: " + e.getFile());
                } catch (IOException e) {
                    out.println("Error: cannot read file: " + e.getMessage());
                } catch (IllegalArgumentException e) {
                    out.println("Error: " + e.getMessage());
                }
            }
        } catch (NoSuchElementException endOfInput) {
            // End of input (e.g. Ctrl-D): leave quietly.
            out.println();
        }
    }

    private String perform(String choice) throws IOException {
        return switch (choice) {
            case "1" -> Tasks.simulateDfa(askPath("DFA file: "), askInputs());
            case "2" -> Tasks.convertNfa(askPath("NFA file: "));
            case "3" -> Tasks.minimizeDfa(askPath("DFA file: "));
            case "4" -> Tasks.intersectDfas(askPath("First DFA file: "), askPath("Second DFA file: "));
            case "5" -> Tasks.simulatePda(askPath("PDA file: "), askInputs());
            default -> "Unknown option '" + choice + "'";
        };
    }

    private Path askPath(String label) {
        return Path.of(prompt(label));
    }

    private List<String> askInputs() {
        String line = prompt("Input strings, separated by spaces (ε for the empty string): ");
        return line.isEmpty() ? List.of() : List.of(line.split("\\s+"));
    }

    private String prompt(String label) {
        out.print(label);
        out.flush();
        return scanner.nextLine().strip();
    }
}
