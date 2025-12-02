package multitier.trans.model.enums;

// Passenger categories
// Each category has a discount value
// Financial service takes the discount value to calculate the seat price

public enum PassengerCategory {
    ADULT(0.0),
    CHILD(25.0),
    SENIOR(30.0),
    STUDENT(20.0);

    private final double discountPercentage;

    PassengerCategory(double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public double getDiscountPercentage() {
        return discountPercentage;
    }
}
