package nexus.domain.hospital;

import nexus.domain.common.DepartmentType;
import nexus.domain.common.Identifiable;
import nexus.exception.ValidationException;
import nexus.utility.validation.Validators;
import java.time.Instant;
import java.util.Optional;

/** Encapsulation & Entity: models an inpatient admission episode. */
public class Admission implements Identifiable<String> {
    private final String id;
    private final String patientId;
    private final String bedId;
    private final DepartmentType department;
    private final Instant admitTime;
    private final String reason;
    private Instant dischargeTime;
    private String dischargeSummary;
    private AdmissionStatus status;

    public Admission(String id, String patientId, String bedId, DepartmentType department, String reason) {
        this(id, patientId, bedId, department, Instant.now(), reason, null, null, AdmissionStatus.ADMITTED);
    }

    public Admission(String id, String patientId, String bedId, DepartmentType department,
                     Instant admitTime, String reason, Instant dischargeTime,
                     String dischargeSummary, AdmissionStatus status) {
        Validators.requireNonNull(id, "Admission id");
        Validators.NOT_BLANK.validate(patientId);
        Validators.NOT_BLANK.validate(bedId);
        Validators.requireNonNull(department, "Department");
        Validators.requireNonNull(admitTime, "Admit time");
        Validators.requireNonNull(status, "Admission status");
        this.id = id;
        this.patientId = patientId.trim();
        this.bedId = bedId.trim();
        this.department = department;
        this.admitTime = admitTime;
        this.reason = reason == null ? "" : reason.trim();
        this.dischargeTime = dischargeTime;
        this.dischargeSummary = dischargeSummary;
        this.status = status;
    }

    @Override public String getId() { return id; }
    public String getPatientId() { return patientId; }
    public String getBedId() { return bedId; }
    public DepartmentType getDepartment() { return department; }
    public Instant getAdmitTime() { return admitTime; }
    public String getReason() { return reason; }
    public Optional<Instant> getDischargeTime() { return Optional.ofNullable(dischargeTime); }
    public Optional<String> getDischargeSummary() { return Optional.ofNullable(dischargeSummary); }
    public AdmissionStatus getStatus() { return status; }

    public void discharge(String summary) {
        if (this.status == AdmissionStatus.DISCHARGED) {
            throw new ValidationException("Admission " + id + " is already discharged");
        }
        this.status = AdmissionStatus.DISCHARGED;
        this.dischargeTime = Instant.now();
        this.dischargeSummary = summary == null ? "Discharged" : summary.trim();
    }

    @Override
    public String toString() {
        return "Admission[" + id + ", patient=" + patientId + ", bed=" + bedId + ", status=" + status + "]";
    }
}
