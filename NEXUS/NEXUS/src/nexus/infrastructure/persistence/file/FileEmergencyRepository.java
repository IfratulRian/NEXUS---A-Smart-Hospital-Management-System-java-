package nexus.infrastructure.persistence.file;

import nexus.domain.emergency.EmergencyCase;
import nexus.domain.emergency.EmergencyStatus;
import nexus.domain.emergency.TriageLevel;
import nexus.infrastructure.persistence.repository.EmergencyRepository;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

public class FileEmergencyRepository extends FileRepository<EmergencyCase> implements EmergencyRepository {
    public FileEmergencyRepository(Path file) { super(file); }

    @Override
    public List<EmergencyCase> findActiveCases() {
        return findAll().stream().filter(c -> c.getStatus().isActive()).toList();
    }

    @Override
    public List<EmergencyCase> findByTriageLevel(TriageLevel level) {
        return findAll().stream().filter(c -> c.getTriageLevel() == level).toList();
    }

    @Override
    protected String serialize(EmergencyCase c) {
        return String.join("\t",
                c.getId(),
                clean(c.getPatientName()),
                clean(c.getPatientId()),
                c.getTriageLevel().name(),
                c.getStatus().name(),
                clean(c.getChiefComplaint()),
                clean(c.getAssignedDoctorId().orElse("")),
                clean(c.getAmbulanceId().orElse("")),
                c.getArrivalTime().toString(),
                clean(c.getNotes().orElse("")));
    }

    @Override
    protected EmergencyCase deserialize(String[] f) {
        String docId = (f.length > 6 && !f[6].isBlank()) ? f[6] : null;
        String ambId = (f.length > 7 && !f[7].isBlank()) ? f[7] : null;
        Instant arrival = Instant.parse(f[8]);
        String notes = (f.length > 9 && !f[9].isBlank()) ? f[9] : null;
        return new EmergencyCase(f[0], f[1], f[2], TriageLevel.valueOf(f[3]),
                EmergencyStatus.valueOf(f[4]), f[5], docId, ambId, arrival, notes);
    }
}
