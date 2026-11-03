package nexus.domain.hospital;

import nexus.domain.common.Identifiable;
import nexus.exception.ValidationException;
import nexus.utility.validation.Validators;
import java.util.Optional;

/** Encapsulation & state tracking: a bed manages its occupancy state. */
public class Bed implements Identifiable<String> {
    private final String id;
    private final String bedNumber;
    private final BedType type;
    private boolean occupied;
    private String patientId;

    public Bed(String id, String bedNumber, BedType type) {
        this(id, bedNumber, type, false, null);
    }

    public Bed(String id, String bedNumber, BedType type, boolean occupied, String patientId) {
        Validators.requireNonNull(id, "Bed id");
        Validators.NOT_BLANK.validate(bedNumber);
        Validators.requireNonNull(type, "Bed type");
        this.id = id;
        this.bedNumber = bedNumber.trim();
        this.type = type;
        this.occupied = occupied;
        this.patientId = patientId;
    }

    @Override public String getId() { return id; }
    public String getBedNumber() { return bedNumber; }
    public BedType getType() { return type; }
    public boolean isOccupied() { return occupied; }
    public Optional<String> getPatientId() { return Optional.ofNullable(patientId); }

    public void allocate(String patientId) {
        Validators.NOT_BLANK.validate(patientId);
        if (this.occupied) {
            throw new ValidationException("Bed " + bedNumber + " is already occupied by patient " + this.patientId);
        }
        this.occupied = true;
        this.patientId = patientId.trim();
    }

    public void release() {
        this.occupied = false;
        this.patientId = null;
    }

    @Override
    public String toString() {
        return "Bed[" + bedNumber + ", " + type + ", occupied=" + occupied + "]";
    }
}
