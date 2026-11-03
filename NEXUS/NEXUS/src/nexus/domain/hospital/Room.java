package nexus.domain.hospital;

import nexus.domain.common.Identifiable;
import nexus.utility.validation.Validators;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/** Composition: Room owns Bed instances and delegates bed lookup/allocation. */
public class Room implements Identifiable<String> {
    private final String id;
    private final String roomNumber;
    private final RoomType type;
    private final List<Bed> beds = new ArrayList<>();

    public Room(String id, String roomNumber, RoomType type) {
        Validators.requireNonNull(id, "Room id");
        Validators.NOT_BLANK.validate(roomNumber);
        Validators.requireNonNull(type, "Room type");
        this.id = id;
        this.roomNumber = roomNumber.trim();
        this.type = type;
    }

    public Room(String id, String roomNumber, RoomType type, List<Bed> initialBeds) {
        this(id, roomNumber, type);
        if (initialBeds != null) this.beds.addAll(initialBeds);
    }

    @Override public String getId() { return id; }
    public String getRoomNumber() { return roomNumber; }
    public RoomType getType() { return type; }
    public List<Bed> getBeds() { return Collections.unmodifiableList(beds); }

    public void addBed(Bed bed) {
        Validators.requireNonNull(bed, "Bed");
        beds.add(bed);
    }

    public Optional<Bed> findFirstAvailableBed() {
        return beds.stream().filter(b -> !b.isOccupied()).findFirst();
    }

    public Optional<Bed> findBed(String bedId) {
        return beds.stream().filter(b -> b.getId().equalsIgnoreCase(bedId)).findFirst();
    }

    public int getCapacity() { return beds.size(); }
    public long getOccupiedCount() { return beds.stream().filter(Bed::isOccupied).count(); }
    public boolean hasAvailableBed() { return beds.stream().anyMatch(b -> !b.isOccupied()); }

    @Override
    public String toString() {
        return "Room[" + roomNumber + ", " + type + ", beds=" + beds.size() + "]";
    }
}
