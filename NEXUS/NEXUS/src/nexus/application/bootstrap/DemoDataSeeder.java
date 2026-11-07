package nexus.application.bootstrap;

import nexus.domain.appointment.Appointment;
import nexus.domain.appointment.AppointmentPriority;
import nexus.domain.billing.Invoice;
import nexus.domain.billing.charges.ConsultationCharge;
import nexus.domain.billing.payment.CashPayment;
import nexus.domain.common.BloodGroup;
import nexus.domain.common.DepartmentType;
import nexus.domain.common.Gender;
import nexus.domain.emergency.EmergencyCase;
import nexus.domain.emergency.TriageLevel;
import nexus.domain.hospital.Admission;
import nexus.domain.hospital.Bed;
import nexus.domain.hospital.Department;
import nexus.domain.people.doctor.Doctor;
import nexus.domain.people.nurse.Nurse;
import nexus.domain.people.nurse.Shift;
import nexus.domain.people.patient.EmergencyContact;
import nexus.domain.people.patient.Patient;
import nexus.domain.people.patient.PatientStatus;
import nexus.domain.pharmacy.Capsule;
import nexus.domain.pharmacy.Injection;
import nexus.domain.pharmacy.Medicine;
import nexus.domain.pharmacy.Ointment;
import nexus.domain.pharmacy.Syrup;
import nexus.domain.pharmacy.Tablet;
import nexus.infrastructure.persistence.repository.AdmissionRepository;
import nexus.infrastructure.persistence.repository.AppointmentRepository;
import nexus.infrastructure.persistence.repository.DepartmentRepository;
import nexus.infrastructure.persistence.repository.DoctorRepository;
import nexus.infrastructure.persistence.repository.EmergencyRepository;
import nexus.infrastructure.persistence.repository.InvoiceRepository;
import nexus.infrastructure.persistence.repository.MedicineRepository;
import nexus.infrastructure.persistence.repository.NurseRepository;
import nexus.infrastructure.persistence.repository.PatientRepository;
import nexus.utility.id.IdGenerator;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Creates an editable, Bangladesh-context sample hospital dataset on first launch. */
final class DemoDataSeeder {
    private static final String[] GIVEN_NAMES = {
            "Karim", "Siam", "Afrin", "Sayma", "Abu Bakkar", "Abir", "Rian", "Nadia",
            "Sadia", "Ayesha", "Tanvir", "Farhana", "Imran", "Nusrat", "Mahmud", "Jannat",
            "Sakib", "Tasnim", "Rafi", "Maliha", "Shakil", "Sumaiya", "Arif", "Tanjila",
            "Fahim", "Sabrina", "Hasan", "Mim", "Rakib", "Samira", "Mehedi", "Faria"
    };
    private static final String[] FAMILY_NAMES = {
            "Rahman", "Ahmed", "Hossain", "Akter", "Islam", "Chowdhury", "Khan", "Begum",
            "Miah", "Sarker", "Uddin", "Karim", "Hasan", "Jahan", "Alam", "Sultana"
    };
    private static final String[] CONDITIONS = {
            "Type 2 diabetes", "Hypertension", "Asthma", "Seasonal allergy", "Gastritis",
            "Migraine", "No known chronic condition"
    };
    private static final String[] SPECIALIZATIONS = {
            "Cardiology", "Neurology", "Orthopedics", "Pediatrics", "Dermatology",
            "General Medicine", "Emergency Medicine", "General Surgery"
    };

    private DemoDataSeeder() { }

    static void seedCatalogs(PatientRepository patients, DoctorRepository doctors, NurseRepository nurses,
                             AppointmentRepository appointments, MedicineRepository medicines,
                             EmergencyRepository emergencies, InvoiceRepository invoices) {
        if (patients.count() == 0) patients.saveAll(createPatients());
        if (doctors.count() == 0) doctors.saveAll(createDoctors());
        if (nurses.count() == 0) nurses.saveAll(createNurses());
        if (appointments.count() == 0) appointments.saveAll(createAppointments(patients, doctors));
        if (medicines.count() == 0) medicines.saveAll(createMedicines());
        if (emergencies.count() == 0) emergencies.saveAll(createEmergencyCases(patients));
        if (invoices.count() == 0) invoices.saveAll(createInvoices(patients, doctors));
    }

    static void seedHospital(DepartmentRepository departments, AdmissionRepository admissions,
                             PatientRepository patients, IdGenerator admissionIds,
                             DoctorRepository doctors, NurseRepository nurses) {
        List<Department> hospitalDepartments = departments.findAll();
        List<Doctor> doctorList = doctors.findAll();
        List<Nurse> nurseList = nurses.findAll();
        for (int i = 0; i < hospitalDepartments.size(); i++) {
            Department department = hospitalDepartments.get(i);
            if (department.getDoctorIds().isEmpty() && !doctorList.isEmpty()) {
                for (int n = i; n < doctorList.size(); n += hospitalDepartments.size()) {
                    department.assignDoctor(doctorList.get(n).getId());
                }
            }
            if (department.getNurseIds().isEmpty() && !nurseList.isEmpty()) {
                for (int n = i; n < nurseList.size(); n += hospitalDepartments.size()) {
                    department.assignNurse(nurseList.get(n).getId());
                }
            }
        }
        if (hospitalDepartments.stream().anyMatch(d -> d.getDoctorIds().isEmpty() || d.getNurseIds().isEmpty())) {
            departments.saveAll(hospitalDepartments);
        }

        if (admissions.count() != 0) return;

        List<Patient> patientList = patients.findAll();
        List<Patient> admittedPatients = new ArrayList<>();
        List<Admission> newAdmissions = new ArrayList<>();
        int patientIndex = 0;
        for (Department department : hospitalDepartments) {
            while (patientIndex < patientList.size() && department.getOccupiedBeds() < department.getTotalBeds()) {
                Patient patient = patientList.get(patientIndex++);
                var bed = department.allocateBed(patient.getId());
                if (bed.isEmpty()) break;
                patient.transitionTo(PatientStatus.ADMITTED);
                admittedPatients.add(patient);
                newAdmissions.add(new Admission(admissionIds.next(), patient.getId(), bed.get().getId(),
                        department.getType(), "Inpatient care"));
                if (newAdmissions.size() >= 16) break;
            }
            if (newAdmissions.size() >= 16) break;
        }
        if (!newAdmissions.isEmpty()) {
            patients.saveAll(admittedPatients);
            departments.saveAll(hospitalDepartments);
            admissions.saveAll(newAdmissions);
        }
    }

    private static List<Patient> createPatients() {
        List<Patient> result = new ArrayList<>(75);
        for (int i = 0; i < 75; i++) {
            String name = GIVEN_NAMES[i % GIVEN_NAMES.length] + " "
                    + FAMILY_NAMES[(i / GIVEN_NAMES.length) % FAMILY_NAMES.length];
            PatientStatus status = i >= 70 ? PatientStatus.DISCHARGED : PatientStatus.REGISTERED;
            result.add(Patient.builder()
                    .id(id("PAT", i + 1))
                    .name(name)
                    .dateOfBirth(LocalDate.now().minusYears(1 + i % 80).minusDays(i % 365))
                    .gender(i % 3 == 0 ? Gender.FEMALE : Gender.MALE)
                    .phone(String.format("+88017%08d", 10000000 + i))
                    .email("patient" + (i + 1) + "@example.com")
                    .bloodGroup(BloodGroup.values()[i % BloodGroup.values().length])
                    .emergencyContact(new EmergencyContact(
                            GIVEN_NAMES[(i + 3) % GIVEN_NAMES.length] + " " + FAMILY_NAMES[i % FAMILY_NAMES.length],
                            String.format("+88018%08d", 20000000 + i), "Family"))
                    .conditions(i % 7 == 0 ? List.of() : List.of(CONDITIONS[i % CONDITIONS.length]))
                    .allergies(i % 11 == 0 ? List.of("Penicillin") : List.of())
                    .status(status)
                    .registeredAt(Instant.now().minusSeconds((long) i * 3600))
                    .build());
        }
        return result;
    }

    private static List<Doctor> createDoctors() {
        List<Doctor> result = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            result.add(new Doctor(id("DOC", i + 1),
                    GIVEN_NAMES[(i + 8) % GIVEN_NAMES.length] + " " + FAMILY_NAMES[(i + 4) % FAMILY_NAMES.length],
                    LocalDate.now().minusYears(34 + i % 18), i % 2 == 0 ? Gender.MALE : Gender.FEMALE,
                    String.format("+88019%08d", 30000000 + i), "doctor" + (i + 1) + "@nexus.example",
                    DepartmentType.values()[i % DepartmentType.values().length],
                    LocalDate.now().minusYears(4 + i % 15), SPECIALIZATIONS[i % SPECIALIZATIONS.length],
                    "MBBS, FCPS", 5 + i % 21, 800 + (i % 5) * 200));
        }
        return result;
    }

    private static List<Nurse> createNurses() {
        List<Nurse> result = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            result.add(new Nurse(id("NUR", i + 1),
                    GIVEN_NAMES[(i + 12) % GIVEN_NAMES.length] + " " + FAMILY_NAMES[(i + 7) % FAMILY_NAMES.length],
                    LocalDate.now().minusYears(23 + i % 15), i % 2 == 0 ? Gender.FEMALE : Gender.MALE,
                    String.format("+88016%08d", 40000000 + i), "nurse" + (i + 1) + "@nexus.example",
                    DepartmentType.values()[i % DepartmentType.values().length],
                    LocalDate.now().minusYears(1 + i % 10), Shift.values()[i % Shift.values().length],
                    DepartmentType.values()[i % DepartmentType.values().length] + " Ward"));
        }
        return result;
    }

    private static List<Appointment> createAppointments(PatientRepository patients, DoctorRepository doctors) {
        List<String> patientIds = patients.findAll().stream().map(Patient::getId).toList();
        List<String> doctorIds = doctors.findAll().stream().map(Doctor::getId).toList();
        List<Appointment> result = new ArrayList<>();
        int count = Math.min(40, Math.min(patientIds.size(), doctorIds.size()) * 4);
        for (int i = 0; i < count; i++) {
            result.add(new Appointment(id("APP", i + 1), patientIds.get(i % patientIds.size()),
                    doctorIds.get(i % doctorIds.size()),
                    LocalDate.now().atTime(8, 0).plusMinutes(15L * i), 30,
                    i % 8 == 0 ? AppointmentPriority.URGENT : AppointmentPriority.ROUTINE,
                    "Outpatient consultation"));
        }
        return result;
    }

    private static List<Medicine> createMedicines() {
        String[] names = {"Napa", "Seclo", "Amodis", "Monas", "Ceevit", "Ace", "Orsaline",
                "Losectil", "Avolac", "Napa Extend", "Cef-3", "Rupa", "Fexo", "Calbo-D",
                "Azithro", "Pantonix", "Neuro-B", "Histacin", "Paracetamol", "Salbutamol"};
        String[] generics = {"Paracetamol", "Omeprazole", "Metronidazole", "Montelukast", "Vitamin C",
                "Paracetamol", "Oral rehydration salts", "Omeprazole", "Lactulose", "Paracetamol",
                "Cefixime", "Rupatadine", "Fexofenadine", "Calcium carbonate", "Azithromycin",
                "Pantoprazole", "Vitamin B complex", "Chlorpheniramine", "Paracetamol", "Salbutamol"};
        List<Medicine> result = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            int stock = i < 3 ? 5 + i : 80 + i * 3;
            int minimum = i < 3 ? 10 : 20;
            LocalDate expiry = LocalDate.now().plusMonths(8 + i % 24);
            String id = id("MED", i + 1);
            String batch = "BD-" + (2026 + i % 2) + "-" + String.format("%04d", i + 1);
            double price = 3.0 + i * 2.5;
            result.add(switch (i % 5) {
                case 0 -> new Tablet(id, names[i], generics[i], "Square Pharmaceuticals", batch, expiry, price, stock, minimum, 10);
                case 1 -> new Capsule(id, names[i], generics[i], "Beximco Pharma", batch, expiry, price, stock, minimum, 20);
                case 2 -> new Syrup(id, names[i], generics[i], "Incepta Pharmaceuticals", batch, expiry, price, stock, minimum, 100);
                case 3 -> new Injection(id, names[i], generics[i], "Renata Limited", batch, expiry, price, stock, minimum, 500, false);
                default -> new Ointment(id, names[i], generics[i], "Healthcare Pharma", batch, expiry, price, stock, minimum, 20);
            });
        }
        return result;
    }

    private static List<EmergencyCase> createEmergencyCases(PatientRepository patients) {
        List<Patient> patientList = patients.findAll();
        List<EmergencyCase> result = new ArrayList<>();
        String[] complaints = {"Road traffic injury", "High fever", "Chest discomfort", "Breathing difficulty",
                "Fall injury", "Severe abdominal pain", "Dehydration"};
        for (int i = 0; i < 7 && i < patientList.size(); i++) {
            Patient patient = patientList.get(i + 30);
            result.add(new EmergencyCase(id("EMG", i + 1), patient.getName(), patient.getId(),
                    TriageLevel.values()[i % TriageLevel.values().length], complaints[i],
                    "AMB-" + String.format("%03d", i + 1)));
        }
        return result;
    }

    private static List<Invoice> createInvoices(PatientRepository patients, DoctorRepository doctors) {
        List<Patient> patientList = patients.findAll();
        List<Doctor> doctorList = doctors.findAll();
        List<Invoice> result = new ArrayList<>();
        int count = Math.min(80, patientList.size());
        for (int i = 0; i < count; i++) {
            Doctor doctor = doctorList.get(i % doctorList.size());
            String invoiceId = id("INV", i + 1);
            double total = doctor.getConsultationFee() + 1200 + (i % 6) * 250;
            double paid = i % 10 == 0 ? 0 : (i % 7 == 0 ? total / 2 : total);
            Invoice.Builder invoice = Invoice.builder().id(invoiceId)
                    .patientId(patientList.get(i).getId()).issueDate(LocalDate.now().minusDays(i % 45))
                    .charge(new ConsultationCharge(doctor.getId(), doctor.getName(), total))
                    .paid(paid);
            if (paid > 0) {
                invoice.payment(new CashPayment(id("PAY", i + 1), invoiceId, paid,
                        Instant.now().minusSeconds((long) (i % 45) * 86400), paid, 0));
            }
            result.add(invoice.build());
        }
        return result;
    }

    private static String id(String prefix, int sequence) {
        return String.format("%s-%05d", prefix, sequence);
    }
}
