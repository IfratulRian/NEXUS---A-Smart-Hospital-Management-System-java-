package nexus.security.role;

import static nexus.security.role.Permission.*;
import java.util.EnumSet;
import java.util.Set;

/** Role-based access: each role owns its permission set; services never check roles directly. */
public enum Role {
    ADMIN(EnumSet.allOf(Permission.class)),
    DOCTOR(EnumSet.of(PATIENT_VIEW, DOCTOR_VIEW, NURSE_VIEW, HOSPITAL_VIEW, APPOINTMENT_MANAGE,
            MEDICAL_RECORD_VIEW, MEDICAL_RECORD_WRITE, PRESCRIPTION_CREATE, EMERGENCY_MANAGE, REPORT_VIEW)),
    NURSE(EnumSet.of(PATIENT_VIEW, PATIENT_MANAGE, DOCTOR_VIEW, NURSE_VIEW, HOSPITAL_VIEW,
            HOSPITAL_MANAGE, MEDICAL_RECORD_VIEW, EMERGENCY_MANAGE)),
    PHARMACIST(EnumSet.of(PRESCRIPTION_VIEW, MEDICINE_DISPENSE, INVENTORY_MANAGE, REPORT_VIEW)),
    RECEPTIONIST(EnumSet.of(PATIENT_VIEW, PATIENT_REGISTER, PATIENT_MANAGE, DOCTOR_VIEW, NURSE_VIEW,
            HOSPITAL_VIEW, APPOINTMENT_MANAGE, BILLING_MANAGE, INSURANCE_MANAGE));

    private final Set<Permission> permissions;
    Role(Set<Permission> permissions) { this.permissions = permissions; }
    public boolean grants(Permission permission) { return permissions.contains(permission); }
}
