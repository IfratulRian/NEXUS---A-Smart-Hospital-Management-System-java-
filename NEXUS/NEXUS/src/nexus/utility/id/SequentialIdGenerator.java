package nexus.utility.id;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

/** Produces ids like PAT-00001. Thread-safe; can be seeded from existing ids. */
public class SequentialIdGenerator implements IdGenerator {
    private final String prefix;
    private final AtomicInteger counter;

    public SequentialIdGenerator(String prefix, int lastUsed) {
        this.prefix = prefix;
        this.counter = new AtomicInteger(lastUsed);
    }

    public static SequentialIdGenerator seededFrom(String prefix, Collection<String> existingIds) {
        int max = existingIds.stream()
                .filter(id -> id.startsWith(prefix + "-"))
                .mapToInt(id -> parseSuffix(id, prefix))
                .max().orElse(0);
        return new SequentialIdGenerator(prefix, max);
    }

    private static int parseSuffix(String id, String prefix) {
        try { return Integer.parseInt(id.substring(prefix.length() + 1)); }
        catch (NumberFormatException e) { return 0; }
    }

    @Override
    public String next() { return String.format("%s-%05d", prefix, counter.incrementAndGet()); }
}
