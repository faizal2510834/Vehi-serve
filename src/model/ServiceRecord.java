package model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ServiceRecord {
    private int serviceId;
    private int vehicleId;
    private LocalDate serviceDate;
    private int odometerKm;
    private String serviceType;
    private String workDone;
    private String partsReplaced;
    private String vehicleCondition;
    private BigDecimal cost;
    private LocalDate nextServiceDate;
    private Integer nextServiceKm; // Integer to allow null
    private String remarks;

    public ServiceRecord() {}

    public ServiceRecord(int serviceId, int vehicleId, LocalDate serviceDate, int odometerKm, String serviceType, String workDone, String partsReplaced, String vehicleCondition, BigDecimal cost, LocalDate nextServiceDate, Integer nextServiceKm, String remarks) {
        this.serviceId = serviceId;
        this.vehicleId = vehicleId;
        this.serviceDate = serviceDate;
        this.odometerKm = odometerKm;
        this.serviceType = serviceType;
        this.workDone = workDone;
        this.partsReplaced = partsReplaced;
        this.vehicleCondition = vehicleCondition;
        this.cost = cost;
        this.nextServiceDate = nextServiceDate;
        this.nextServiceKm = nextServiceKm;
        this.remarks = remarks;
    }

    public int getServiceId() { return serviceId; }
    public void setServiceId(int serviceId) { this.serviceId = serviceId; }

    public int getVehicleId() { return vehicleId; }
    public void setVehicleId(int vehicleId) { this.vehicleId = vehicleId; }

    public LocalDate getServiceDate() { return serviceDate; }
    public void setServiceDate(LocalDate serviceDate) { this.serviceDate = serviceDate; }

    public int getOdometerKm() { return odometerKm; }
    public void setOdometerKm(int odometerKm) { this.odometerKm = odometerKm; }

    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }

    public String getWorkDone() { return workDone; }
    public void setWorkDone(String workDone) { this.workDone = workDone; }

    public String getPartsReplaced() { return partsReplaced; }
    public void setPartsReplaced(String partsReplaced) { this.partsReplaced = partsReplaced; }

    public String getVehicleCondition() { return vehicleCondition; }
    public void setVehicleCondition(String vehicleCondition) { this.vehicleCondition = vehicleCondition; }

    public BigDecimal getCost() { return cost; }
    public void setCost(BigDecimal cost) { this.cost = cost; }

    public LocalDate getNextServiceDate() { return nextServiceDate; }
    public void setNextServiceDate(LocalDate nextServiceDate) { this.nextServiceDate = nextServiceDate; }

    public Integer getNextServiceKm() { return nextServiceKm; }
    public void setNextServiceKm(Integer nextServiceKm) { this.nextServiceKm = nextServiceKm; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
