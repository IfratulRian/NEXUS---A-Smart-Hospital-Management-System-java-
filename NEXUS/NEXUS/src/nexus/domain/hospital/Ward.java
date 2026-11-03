package nexus.domain.hospital;

import nexus.domain.common.DepartmentType;
import nexus.domain.common.Identifiable;
import nexus.utility.validation.Validators;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/** Composition: Ward aggregates rooms belonging to a department. */
public class Ward implements Identifiable<String> {
    private final String id;
    private final String name;
    private final DepartmentType department;
    private final List<Room> rooms = new ArrayList<>();

    public Ward(String id, String name, DepartmentType department) {
        Validators.requireNonNull(id, "Ward id");
        Validators.NOT_BLANK.validate(name);
        Validators.requireNonNull(department, "Department");
        this.id = id;
        this.name = name.trim();
        this.department = department;
    }

    public Ward(String id, String name, DepartmentType department, List<Room> initialRooms) {
        this(id, name, department);
        if (initialRooms != null) this.rooms.addAll(initialRooms);
    }

    @Override public String getId() { return id; }
    public String getName() { return name; }
    public DepartmentType getDepartment() { return department; }
    public List<Room> getRooms() { return Collections.unmodifiableList(rooms); }

    public void addRoom(Room room) {
        Validators.requireNonNull(room, "Room");
        rooms.add(room);
    }

    public Optional<Bed> findAvailableBed() {
        return rooms.stream()
                .map(Room::findFirstAvailableBed)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    public Optional<Bed> findBed(String bedId) {
        return rooms.stream()
                .map(r -> r.findBed(bedId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    public int getTotalBeds() {
        return rooms.stream().mapToInt(Room::getCapacity).sum();
    }

    public long getOccupiedBeds() {
        return rooms.stream().mapToLong(Room::getOccupiedCount).sum();
    }

    @Override
    public String toString() {
        return "Ward[" + name + " (" + department + "), rooms=" + rooms.size() + "]";
    }
}
