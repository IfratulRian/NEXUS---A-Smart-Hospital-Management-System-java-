package nexus.infrastructure.persistence.repository;

import nexus.domain.pharmacy.Medicine;
import java.time.LocalDate;
import java.util.List;

public interface MedicineRepository extends Repository<Medicine, String> {
    List<Medicine> findByNameContaining(String fragment);
    List<Medicine> findLowStock();
    List<Medicine> findExpiringBefore(LocalDate date);
}
