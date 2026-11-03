package nexus.presentation.console.input;

import nexus.exception.ValidationException;
import nexus.utility.date.DateParser;
import nexus.utility.validation.Validator;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/** One controlled input mechanism: re-prompts on bad input instead of crashing. */
public class ConsoleInput {
    private final BufferedReader reader;
    private final PrintStream out;

    public ConsoleInput(InputStream in, PrintStream out) {
        this.reader = new BufferedReader(new InputStreamReader(in));
        this.out = out;
    }

    public String readLine(String prompt) {
        out.print("  " + prompt + ": ");
        out.flush();
        try {
            String line = reader.readLine();
            if (line == null) throw new InputClosedException();
            return line.trim();
        } catch (IOException e) {
            throw new InputClosedException();
        }
    }

    public String readValidated(String prompt, Validator<String> validator) {
        while (true) {
            String value = readLine(prompt);
            try {
                validator.validate(value);
                return value;
            } catch (ValidationException e) {
                out.println("  [!] " + e.getMessage());
            }
        }
    }

    public int readInt(String prompt, int min, int max) {
        while (true) {
            String text = readLine(prompt);
            try {
                int value = Integer.parseInt(text);
                if (value >= min && value <= max) return value;
                out.println("  [!] Enter a number between " + min + " and " + max);
            } catch (NumberFormatException e) {
                out.println("  [!] '" + text + "' is not a valid whole number");
            }
        }
    }

    public double readNonNegativeDouble(String prompt) {
        while (true) {
            String text = readLine(prompt);
            try {
                double value = Double.parseDouble(text);
                if (value >= 0) return value;
                out.println("  [!] Value cannot be negative");
            } catch (NumberFormatException e) {
                out.println("  [!] '" + text + "' is not a valid number");
            }
        }
    }

    public LocalDate readDate(String prompt) {
        while (true) {
            String text = readLine(prompt + " (yyyy-MM-dd)");
            try {
                return DateParser.parse(text);
            } catch (DateTimeParseException e) {
                out.println("  [!] Invalid date, use format yyyy-MM-dd");
            }
        }
    }

    public <E extends Enum<E>> E readEnum(String prompt, Class<E> type) {
        E[] options = type.getEnumConstants();
        out.println("  " + prompt + ":");
        for (int i = 0; i < options.length; i++) out.println("    " + (i + 1) + ") " + options[i]);
        return options[readInt("Choose", 1, options.length) - 1];
    }

    public boolean confirm(String prompt) {
        while (true) {
            String answer = readLine(prompt + " (y/n)").toLowerCase();
            if (answer.equals("y") || answer.equals("yes")) return true;
            if (answer.equals("n") || answer.equals("no")) return false;
        }
    }
}
