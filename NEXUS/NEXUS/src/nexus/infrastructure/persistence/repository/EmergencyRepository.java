package nexus.infrastructure.persistence.repository;

import nexus.domain.emergency.EmergencyCase;
import nexus.domain.emergency.TriageLevel;
import java.util.List;

public interface EmergencyRepository extends Repository<EmergencyCase, String> {
    List<EmergencyCase> findActiveCases();
    List<EmergencyCase> findByTriageLevel(TriageLevel level);
}
