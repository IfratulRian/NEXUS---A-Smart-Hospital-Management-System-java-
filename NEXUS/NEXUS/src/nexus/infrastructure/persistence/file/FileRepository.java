package nexus.infrastructure.persistence.file;

import nexus.domain.common.Identifiable;
import nexus.exception.RepositoryException;
import nexus.infrastructure.persistence.repository.Repository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Template Method: subclasses only describe how one entity maps to a tab-separated line.
 * Loading, caching, flushing and error translation live here once.
 */
public abstract class FileRepository<T extends Identifiable<String>> implements Repository<T, String> {
    private final Path file;
    private final Map<String, T> cache = new LinkedHashMap<>();

    protected FileRepository(Path file) {
        this.file = file;
        load();
    }

    protected abstract String serialize(T entity);
    protected abstract T deserialize(String[] fields);

    protected static String clean(String value) {
        return value == null ? "" : value.replace('\t', ' ').replace('\n', ' ').replace('\r', ' ');
    }

    @Override
    public synchronized void save(T entity) {
        cache.put(entity.getId(), entity);
        flush();
    }

    @Override
    public synchronized void saveAll(Collection<T> entities) {
        entities.forEach(entity -> cache.put(entity.getId(), entity));
        flush();
    }

    @Override public synchronized Optional<T> findById(String id) { return Optional.ofNullable(cache.get(id)); }
    @Override public synchronized List<T> findAll() { return new ArrayList<>(cache.values()); }
    @Override public synchronized boolean existsById(String id) { return cache.containsKey(id); }

    @Override
    public synchronized void deleteById(String id) {
        if (cache.remove(id) != null) flush();
    }

    private void load() {
        try {
            Files.createDirectories(file.getParent());
            if (!Files.exists(file)) return;
            for (String line : Files.readAllLines(file)) {
                if (line.isBlank()) continue;
                T entity = deserialize(line.split("\t", -1));
                cache.put(entity.getId(), entity);
            }
        } catch (IOException | RuntimeException e) {
            throw new RepositoryException("Failed to load " + file + ": " + e.getMessage(), e);
        }
    }

    private void flush() {
        try {
            List<String> lines = cache.values().stream().map(this::serialize).toList();
            Files.write(file, lines);
        } catch (IOException e) {
            throw new RepositoryException("Failed to write " + file, e);
        }
    }
}
