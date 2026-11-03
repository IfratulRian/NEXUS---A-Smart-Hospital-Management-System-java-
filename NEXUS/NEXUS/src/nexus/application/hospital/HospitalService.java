package nexus.application.hospital;

import nexus.application.patient.PatientService;
import nexus.domain.common.DepartmentType;
import nexus.domain.hospital.Admission;
import nexus.domain.hospital.AdmissionStatus;
import nexus.domain.hospital.Bed;
import nexus.domain.hospital.BedType;
import nexus.domain.hospital.Department;
import nexus.domain.hospital.Hospital;
import nexus.domain.hospital.Room;
import nexus.domain.hospital.RoomType;
import nexus.domain.hospital.Ward;
import nexus.domain.people.patient.Patient;
import nexus.domain.people.patient.PatientStatus;
import nexus.exception.EntityNotFoundException;
import nexus.exception.ValidationException;
import nexus.infrastructure.logging.AuditAction;
import nexus.infrastructure.logging.AuditEntry;
import nexus.infrastructure.logging.AuditLogger;
import nexus.infrastructure.persistence.repository.AdmissionRepository;
import nexus.infrastructure.persistence.repository.DepartmentRepository;
import nexus.security.authentication.SessionContext;
import nexus.utility.id.IdGenerator;
import java.util.List;
import java.util.Optional;

/** Service orchestration for hospital structure, ward/room/bed allocation, and inpatient admissions. */
public class HospitalService {
    private final Hospital hospital;
    private final DepartmentRepository departmentRepository;
    private final AdmissionRepository admissionRepository;
    private final PatientService patientService;
    private final IdGenerator admissionIdGenerator;
    private final AuditLogger audit;
    private final SessionContext session;

    public HospitalService(Hospital hospital, DepartmentRepository departmentRepository,
                           AdmissionRepository admissionRepository, PatientService patientService,
                           IdGenerator admissionIdGenerator, AuditLogger audit, SessionContext session) {
        this.hospital = hospital;
        this.departmentRepository = departmentRepository;
        this.admissionRepository = admissionRepository;
        this.patientService = patientService;
        this.admissionIdGenerator = admissionIdGenerator;
        this.audit = audit;
        this.session = session;
        initDefaultStructureIfEmpty();
    }

    public Hospital getHospital() { return hospital; }

    public List<Department> listDepartments() {
        return hospital.getDepartments();
    }

    public Department getDepartment(DepartmentType type) {
        return hospital.findDepartment(type)
                .orElseThrow(() -> new EntityNotFoundException("Department " + type + " not found"));
    }

    public Admission admitPatient(String patientId, DepartmentType departmentType, String reason) {
        Patient patient = patientService.getPatient(patientId);
        if (admissionRepository.findActiveByPatientId(patient.getId()).isPresent()) {
            throw new ValidationException("Patient " + patientId + " is already admitted to an active bed");
        }
        Department dept = getDepartment(departmentType);
        Bed allocatedBed = dept.allocateBed(patient.getId())
                .orElseThrow(() -> new ValidationException("No available beds in " + departmentType + " department"));

        // Persist department bed changes
        departmentRepository.save(dept);

        // Update patient status to ADMITTED
        if (patient.getStatus() != PatientStatus.ADMITTED) {
            patientService.changeStatus(patient.getId(), PatientStatus.ADMITTED);
        }

        Admission admission = new Admission(admissionIdGenerator.next(), patient.getId(), allocatedBed.getId(),
                departmentType, reason);
        admissionRepository.save(admission);

        audit.log(AuditEntry.of(session.currentUsername(), AuditAction.PATIENT_STATUS_CHANGED, "Admission",
                admission.getId(), "SUCCESS"));
        return admission;
    }

    public Admission dischargePatient(String patientId, String summary) {
        Admission admission = admissionRepository.findActiveByPatientId(patientId)
                .orElseThrow(() -> new EntityNotFoundException("No active admission found for patient " + patientId));

        admission.discharge(summary);
        admissionRepository.save(admission);

        // Release the bed
        hospital.findBed(admission.getBedId()).ifPresent(Bed::release);
        hospital.findDepartment(admission.getDepartment()).ifPresent(departmentRepository::save);

        // Transition patient to DISCHARGED
        patientService.changeStatus(patientId, PatientStatus.DISCHARGED);

        audit.log(AuditEntry.of(session.currentUsername(), AuditAction.PATIENT_DISCHARGED, "Admission",
                admission.getId(), "SUCCESS"));
        return admission;
    }

    public List<Admission> getAdmissionsForPatient(String patientId) {
        return admissionRepository.findByPatientId(patientId);
    }

    public List<Admission> listActiveAdmissions() {
        return admissionRepository.findAll().stream()
                .filter(a -> a.getStatus() == AdmissionStatus.ADMITTED)
                .toList();
    }

    private void initDefaultStructureIfEmpty() {
        if (departmentRepository.count() == 0) {
            for (DepartmentType type : DepartmentType.values()) {
                Department dept = new Department("DEP-" + type.name(), type, type.toString());
                Ward ward = new Ward("WRD-" + type.name() + "-01", type + " Ward A", type);
                Room r1 = new Room("RM-" + type.name() + "-101", "101", RoomType.GENERAL_WARD);
                r1.addBed(new Bed("BED-" + type.name() + "-01", "B-01", BedType.GENERAL));
                r1.addBed(new Bed("BED-" + type.name() + "-02", "B-02", BedType.GENERAL));
                Room r2 = new Room("RM-" + type.name() + "-102", "102", RoomType.ICU_ROOM);
                r2.addBed(new Bed("BED-" + type.name() + "-03", "B-03", BedType.ICU));
                ward.addRoom(r1);
                ward.addRoom(r2);
                dept.addWard(ward);
                departmentRepository.save(dept);
                hospital.addDepartment(dept);
            }
        } else {
            departmentRepository.findAll().forEach(hospital::addDepartment);
        }
    }
}
