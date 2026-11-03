package nexus.presentation.console.output;

public final class ConsoleFormatter {
    private static final int WIDTH = 60;
    private ConsoleFormatter() { }

    public static String header(String hospital, String title) {
        String bar = "=".repeat(WIDTH);
        return "\n" + bar + "\n" + center(hospital) + "\n" + center(title) + "\n" + bar;
    }

    public static String footer() { return "=".repeat(WIDTH); }

    public static String keyValue(String key, Object value) {
        return String.format("  %-16s: %s", key, value);
    }

    private static String center(String text) {
        int pad = Math.max(0, (WIDTH - text.length()) / 2);
        return " ".repeat(pad) + text;
    }
}
