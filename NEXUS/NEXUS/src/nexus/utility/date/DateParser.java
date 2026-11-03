package nexus.utility.date;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class DateParser {
    public static final DateTimeFormatter FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private DateParser() { }
    public static LocalDate parse(String text) { return LocalDate.parse(text.trim(), FORMAT); }
}
