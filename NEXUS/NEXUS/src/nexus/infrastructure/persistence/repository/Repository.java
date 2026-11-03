package nexus.infrastructure.persistence.repository;

import nexus.domain.common.Identifiable;
import java.util.List;
import java.util.Optional;

/** Generic repository contract (Dependency Inversion: services depend on this, not on files). */
public interface Repository<T extends Identifiable<ID>, ID> {
    void save(T entity);
    Optional<T> findById(ID id);
    List<T> findAll();
    boolean existsById(ID id);
    void deleteById(ID id);
    default long count() { return findAll().size(); }
}
