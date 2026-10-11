package org.example.io;

public class AutomatonFormatException extends IllegalArgumentException {

    public AutomatonFormatException(String message) {
        super(message);
    }

    public AutomatonFormatException(int line, String message) {
        super("line " + line + ": " + message);
    }
}
