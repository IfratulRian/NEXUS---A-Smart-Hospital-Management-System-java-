package nexus.infrastructure.persistence.file;

import nexus.domain.common.BloodGroup;
import nexus.domain.common.Gender;
import nexus.domain.people.patient.EmergencyContact;
import nexus.domain.people.patient.Patient;
import nexus.domain.people.patient.PatientStatus;
import nexus.infrastructure.persistence.repository.PatientRepository;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class FilePatientRepository extends FileRepository<Patient> implements PatientRepository {
    public FilePatientRepository(Path file) { super(file); }

    @Override
    public List<Patient> findByNameContaining(String fragment) {
        String needle = fragment.toLowerCase();
        return findAll().stream().filter(p -> p.getName().toLowerCase().contains(needle)).toList();
    }

    @Override
    protected String serialize(Patient p) {
        EmergencyContact ec = p.getEmergencyContact();
        return String.join("\t", p.getId(), clean(p.getName()), p.getDateOfBirth().toString(), p.getGender().name(),
                p.getPhone(), clean(p.getEmail()), p.getBloodGroup().name(),
                clean(ec.name()), ec.phone(), clean(ec.relation()),
                joinList(p.getAllergies()), joinList(p.getConditions()),
                p.getStatus().name(), p.getRegisteredAt().toString());
    }

    @Override
    protected Patient deserialize(String[] f) {
        return Patient.builder().id(f[0]).name(f[1]).dateOfBirth(LocalDate.parse(f[2])).gender(Gender.valueOf(f[3]))
                .phone(f[4]).email(f[5]).bloodGroup(BloodGroup.valueOf(f[6]))
                .emergencyContact(new EmergencyContact(f[7], f[8], f[9]))
                .allergies(splitList(f[10])).conditions(splitList(f[11]))
                .status(PatientStatus.valueOf(f[12])).registeredAt(Instant.parse(f[13])).build();
    }

    private static String joinList(List<String> items) {
        return clean(String.join(";", items.stream().map(s -> s.replace(';', ',')).toList()));
    }

    private static List<String> splitList(String text) {
        return text.isBlank() ? List.of() : Arrays.asList(text.split(";"));
    }
}
