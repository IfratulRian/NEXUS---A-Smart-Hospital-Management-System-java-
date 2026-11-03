package nexus.domain.hospital;

import nexus.domain.common.DepartmentType;
import nexus.domain.common.Identifiable;
import nexus.utility.validation.Validators;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/** Aggregate Root & Information Expert: Coordinates hospital-wide departments, beds, and admissions. */
public class Hospital implements Identifiable<String> {
    private final String id;
    private final String name;
    private final List<Department> departments = new ArrayList<>();

    public Hospital(String id, String name) {
        Validators.requireNonNull(id, "Hospital id");
        Validators.NOT_BLANK.validate(name);
        this.id = id;
        this.name = name.trim();
    }

    public Hospital(String id, String name, List<Department> initialDepartments) {
        this(id, name);
        if (initialDepartments != null) this.departments.addAll(initialDepartments);
    }

    @Override public String getId() { return id; }
    public String getName() { return name; }
    public List<Department> getDepartments() { return Collections.unmodifiableList(departments); }

    public void addDepartment(Department department) {
        Validators.requireNonNull(department, "Department");
        departments.add(department);
    }

    public Optional<Department> findDepartment(DepartmentType type) {
        return departments.stream().filter(d -> d.getType() == type).findFirst();
    }

    public Optional<Department> findDepartmentById(String deptId) {
        return departments.stream().filter(d -> d.getId().equalsIgnoreCase(deptId)).findFirst();
    }

    public Optional<Bed> allocateBed(DepartmentType deptType, String patientId) {
        return findDepartment(deptType).flatMap(d -> d.allocateBed(patientId));
    }

    public Optional<Bed> findBed(String bedId) {
        return departments.stream()
                .map(d -> d.findBed(bedId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    public int getTotalBeds() {
        return departments.stream().mapToInt(Department::getTotalBeds).sum();
    }

    public long getOccupiedBeds() {
        return departments.stream().mapToLong(Department::getOccupiedBeds).sum();
    }

    public double getOccupancyRate() {
        int total = getTotalBeds();
        return total == 0 ? 0.0 : (double) getOccupiedBeds() / total * 100.0;
    }

    @Override
    public String toString() {
        return "Hospital[" + name + ", departments=" + departments.size() + ", beds=" + getOccupiedBeds() + "/" + getTotalBeds() + "]";
    }
}
