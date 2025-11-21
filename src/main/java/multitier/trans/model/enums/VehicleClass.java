package multitier.trans.model.enums;

// Vehicle models
// A vehicle model is assigned per route
// A seat capacity is assigned per vehicle
// When creating a route, seat capacity is taken from this enum

public enum VehicleClass {
    STANDARD(50),
    COACH(60),
    MINI_BUS(20),
    DOUBLE_DECKER(80);

    private final int seatCapacity;

    VehicleClass(int seatCapacity) {
        this.seatCapacity = seatCapacity;
    }

    public int getSeatCapacity() {
        return seatCapacity;
    }
}