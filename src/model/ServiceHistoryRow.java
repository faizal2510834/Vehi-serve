package model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ServiceHistoryRow {
    private final LocalDate serviceDate;
    private final String regNumber;
    private final String makeModel;
    private final String customerName;
    private final String customerPhone;
    private final String serviceType;
    private final int odometer;
    private final BigDecimal cost;
    private final LocalDate nextDate;
    private final Integer nextKm;
    
    private final String workDone;
    private final String partsReplaced;
    private final String remarks;

    public ServiceHistoryRow(LocalDate serviceDate, String regNumber, String makeModel, String customerName,
                             String customerPhone, String serviceType, int odometer, BigDecimal cost,
                             LocalDate nextDate, Integer nextKm, String workDone, String partsReplaced, String remarks) {
        this.serviceDate = serviceDate;
        this.regNumber = regNumber;
        this.makeModel = makeModel;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.serviceType = serviceType;
        this.odometer = odometer;
        this.cost = cost;
        this.nextDate = nextDate;
        this.nextKm = nextKm;
        this.workDone = workDone;
        this.partsReplaced = partsReplaced;
        this.remarks = remarks;
    }

    public LocalDate getServiceDate() { return serviceDate; }
    public String getRegNumber() { return regNumber; }
    public String getMakeModel() { return makeModel; }
    public String getCustomerName() { return customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public String getServiceType() { return serviceType; }
    public int getOdometer() { return odometer; }
    public BigDecimal getCost() { return cost; }
    public LocalDate getNextDate() { return nextDate; }
    public Integer getNextKm() { return nextKm; }
    public String getWorkDone() { return workDone; }
    public String getPartsReplaced() { return partsReplaced; }
    public String getRemarks() { return remarks; }
}
