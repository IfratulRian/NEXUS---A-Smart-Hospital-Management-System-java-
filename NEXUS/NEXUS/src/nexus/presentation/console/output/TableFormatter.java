package nexus.presentation.console.output;

import java.util.Arrays;
import java.util.List;

public final class TableFormatter {
    private TableFormatter() { }

    public static String render(List<String> headers, List<List<String>> rows) {
        int[] widths = new int[headers.size()];
        for (int i = 0; i < widths.length; i++) widths[i] = headers.get(i).length();
        for (List<String> row : rows)
            for (int i = 0; i < widths.length; i++) widths[i] = Math.max(widths[i], row.get(i).length());

        StringBuilder sb = new StringBuilder();
        appendRow(sb, headers, widths);
        sb.append("  ").append("-".repeat(Arrays.stream(widths).sum() + 3 * (widths.length - 1))).append('\n');
        rows.forEach(r -> appendRow(sb, r, widths));
        return sb.toString();
    }

    private static void appendRow(StringBuilder sb, List<String> cells, int[] widths) {
        sb.append("  ");
        for (int i = 0; i < widths.length; i++) {
            sb.append(String.format("%-" + widths[i] + "s", cells.get(i)));
            if (i < widths.length - 1) sb.append(" | ");
        }
        sb.append('\n');
    }
}
