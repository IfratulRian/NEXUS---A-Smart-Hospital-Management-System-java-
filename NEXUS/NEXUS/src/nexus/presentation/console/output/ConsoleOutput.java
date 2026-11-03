package nexus.presentation.console.output;

import java.io.PrintStream;
import java.util.List;

/** The only class that writes to the terminal - swap it to redirect output (tests, GUI adapter). */
public class ConsoleOutput {
    private final PrintStream out;
    private final String hospitalName;

    public ConsoleOutput(PrintStream out, String hospitalName) {
        this.out = out;
        this.hospitalName = hospitalName;
    }

    public void println(String text) { out.println(text); }
    public void header(String title) { out.println(ConsoleFormatter.header(hospitalName, title)); }
    public void footer() { out.println(ConsoleFormatter.footer()); }
    public void field(String key, Object value) { out.println(ConsoleFormatter.keyValue(key, value)); }
    public void table(List<String> headers, List<List<String>> rows) { out.print(TableFormatter.render(headers, rows)); }
    public void success(String message) { out.println("  [OK] " + message); }
    public void error(String message) { out.println("  [ERROR] " + message); }
    public void info(String message) { out.println("  " + message); }
}
