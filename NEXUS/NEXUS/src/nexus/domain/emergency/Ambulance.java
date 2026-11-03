package nexus.domain.emergency;

import nexus.domain.common.Identifiable;
import nexus.exception.ValidationException;
import nexus.utility.validation.Validators;

public class Ambulance implements Identifiable<String> {
    private final String id;
    private final String vehicleNumber;
    private final String driverName;
    private final String paramedicName;
    private boolean available;
    private String currentLocation;

    public Ambulance(String id, String vehicleNumber, String driverName, String paramedicName) {
        this(id, vehicleNumber, driverName, paramedicName, true, "Hospital Base");
    }

    public Ambulance(String id, String vehicleNumber, String driverName, String paramedicName,
                     boolean available, String currentLocation) {
        Validators.requireNonNull(id, "Ambulance id");
        Validators.NOT_BLANK.validate(vehicleNumber);
        Validators.NOT_BLANK.validate(driverName);
        this.id = id;
        this.vehicleNumber = vehicleNumber.trim();
        this.driverName = driverName.trim();
        this.paramedicName = paramedicName == null ? "" : paramedicName.trim();
        this.available = available;
        this.currentLocation = currentLocation == null ? "Hospital Base" : currentLocation.trim();
    }

    @Override public String getId() { return id; }
    public String getVehicleNumber() { return vehicleNumber; }
    public String getDriverName() { return driverName; }
    public String getParamedicName() { return paramedicName; }
    public boolean isAvailable() { return available; }
    public String getCurrentLocation() { return currentLocation; }

    public void dispatch(String destination) {
        if (!available) {
            throw new ValidationException("Ambulance " + vehicleNumber + " is currently deployed at " + currentLocation);
        }
        this.available = false;
        this.currentLocation = destination.trim();
    }

    public void completeMission() {
        this.available = true;
        this.currentLocation = "Hospital Base";
    }

    @Override
    public String toString() {
        return "Ambulance[" + vehicleNumber + ", driver=" + driverName + ", available=" + available + ", loc=" + currentLocation + "]";
    }
}
