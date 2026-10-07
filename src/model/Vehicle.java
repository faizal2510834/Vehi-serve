package model;

public class Vehicle {
    private int vehicleId;
    private int customerId;
    private String regNumber;
    private String vehicleType;
    private String make;
    private String model;
    private int manufactureYear;
    private String fuelType;

    public Vehicle() {}

    public Vehicle(int vehicleId, int customerId, String regNumber, String vehicleType, String make, String model, int manufactureYear, String fuelType) {
        this.vehicleId = vehicleId;
        this.customerId = customerId;
        this.regNumber = regNumber;
        this.vehicleType = vehicleType;
        this.make = make;
        this.model = model;
        this.manufactureYear = manufactureYear;
        this.fuelType = fuelType;
    }

    public int getVehicleId() { return vehicleId; }
    public void setVehicleId(int vehicleId) { this.vehicleId = vehicleId; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getRegNumber() { return regNumber; }
    public void setRegNumber(String regNumber) { this.regNumber = regNumber; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public String getMake() { return make; }
    public void setMake(String make) { this.make = make; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public int getManufactureYear() { return manufactureYear; }
    public void setManufactureYear(int manufactureYear) { this.manufactureYear = manufactureYear; }

    public String getFuelType() { return fuelType; }
    public void setFuelType(String fuelType) { this.fuelType = fuelType; }

    @Override
    public String toString() {
        return regNumber + " (" + make + " " + model + ")";
    }
}
