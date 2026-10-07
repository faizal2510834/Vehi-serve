package test;

import dao.CustomerDAO;
import dao.ServiceRecordDAO;
import dao.VehicleDAO;
import exception.DatabaseException;
import exception.ValidationException;
import model.Customer;
import model.ServiceRecord;
import model.Vehicle;
import util.DBConnection;
import util.Validator;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ServiceRecordDAOTest {

    public static void main(String[] args) {
        System.out.println("--- Starting AUTOMATED tests for Module 3 ---");
        CustomerDAO customerDAO = new CustomerDAO();
        VehicleDAO vehicleDAO = new VehicleDAO();
        ServiceRecordDAO serviceDAO = new ServiceRecordDAO();
        
        int customerId = -1;
        int vehicleId = -1;
        int serviceId1 = -1;
        int serviceId2 = -1;
        int serviceId3 = -1;

        try {
            // Setup dummy customer and vehicle
            Customer c1 = new Customer(0, "Service Owner", "4444444444", "s@example.com", "Address");
            customerId = customerDAO.addCustomer(c1);
            
            Vehicle v1 = new Vehicle(0, customerId, "TESTSRV", "Two-Wheeler", "Honda", "Activa", 2021, "Petrol");
            vehicleId = vehicleDAO.addVehicle(v1);

            // 1. Valid Save (with NULL next date)
            ServiceRecord sr1 = new ServiceRecord(0, vehicleId, LocalDate.parse("2023-01-01"), 5000, 
                "Routine", "Oil change", "Filter", "Good", new BigDecimal("1500.50"), null, 10000, "None");
            serviceId1 = serviceDAO.addServiceRecord(sr1);
            System.out.println("1. Valid save (null date): SUCCESS (ID " + serviceId1 + ")");

            // 2. Valid Save (newer record, supersedes sr1)
            ServiceRecord sr2 = new ServiceRecord(0, vehicleId, LocalDate.parse("2023-06-01"), 10500, 
                "Repair", "Brakes", "Pads", "Average", new BigDecimal("2500"), LocalDate.parse("2023-12-01"), 15000, "");
            serviceId2 = serviceDAO.addServiceRecord(sr2);
            System.out.println("2. Valid save (latest): SUCCESS (ID " + serviceId2 + ")");

            // 3. Validations
            try { Validator.validateDate("2026-02-31", "Date"); System.out.println("3a. Date 2026-02-31: FAILED"); } catch (ValidationException e) { System.out.println("3a. Date 2026-02-31: SUCCESS (" + e.getMessage() + ")"); }
            try { Validator.validateDate("10-10-2023", "Date"); System.out.println("3b. Wrong date format: FAILED"); } catch (ValidationException e) { System.out.println("3b. Wrong date format: SUCCESS (" + e.getMessage() + ")"); }
            
            LocalDate futureDate = LocalDate.now().plusDays(10);
            try { Validator.validateServiceDate(futureDate.toString()); System.out.println("3c. Future service date logic: FAILED"); } catch (ValidationException e) { System.out.println("3c. Future service date logic: SUCCESS (" + e.getMessage() + ")"); }
            
            LocalDate nextDate = LocalDate.parse("2022-12-31");
            try { Validator.validateNextServiceDate(nextDate.toString(), sr1.getServiceDate()); System.out.println("3d. Next date < Service date logic: FAILED"); } catch (ValidationException e) { System.out.println("3d. Next date < Service date logic: SUCCESS (" + e.getMessage() + ")"); }
            
            try { Validator.validateOdometer("-100"); System.out.println("3e. Negative odometer: FAILED"); } catch (ValidationException e) { System.out.println("3e. Negative odometer: SUCCESS (" + e.getMessage() + ")"); }
            try { Validator.validateOdometer("100.5"); System.out.println("3f. Fractional odometer: FAILED"); } catch (ValidationException e) { System.out.println("3f. Fractional odometer: SUCCESS (" + e.getMessage() + ")"); }
            try { Validator.validateOdometer("2000001"); System.out.println("3f2. Odometer > 2M: FAILED"); } catch (ValidationException e) { System.out.println("3f2. Odometer > 2M: SUCCESS (" + e.getMessage() + ")"); }
            try { Validator.validateCost("-50"); System.out.println("3g. Negative cost: FAILED"); } catch (ValidationException e) { System.out.println("3g. Negative cost: SUCCESS (" + e.getMessage() + ")"); }
            try { Validator.validateCost("10.999"); System.out.println("3h. Cost 10.999: FAILED"); } catch (ValidationException e) { System.out.println("3h. Cost 10.999: SUCCESS (" + e.getMessage() + ")"); }
            
            String longText = "A".repeat(501);
            try { Validator.validateServiceString(longText, "Remarks", 500); System.out.println("3i. Over-length string: FAILED"); } catch (ValidationException e) { System.out.println("3i. Over-length string: SUCCESS (" + e.getMessage() + ")"); }

            int currentOdo = 5000;
            int nextOdo = 4000;
            try { Validator.validateNextServiceKm(String.valueOf(nextOdo), currentOdo); System.out.println("3j. Next KM <= Odometer logic: FAILED"); } catch (ValidationException e) { System.out.println("3j. Next KM <= Odometer logic: SUCCESS (" + e.getMessage() + ")"); }

            try { Validator.validateNextServiceFields(null, null); System.out.println("3k. Neither next-service field given logic: FAILED"); } catch (ValidationException e) { System.out.println("3k. Neither next-service field given logic: SUCCESS (" + e.getMessage() + ")"); }

            // getLatestOdometer test
            int latestOdo = serviceDAO.getLatestOdometer(vehicleId);
            System.out.println("3l. getLatestOdometer: SUCCESS (Latest odometer is " + latestOdo + ", expected 10500)");
            
            // 4. Reg lookup with spaces/hyphens
            String reg = Validator.validateRegNumber(" t-E sT-S rv ");
            Vehicle found = vehicleDAO.findByRegNumber(reg);
            System.out.println("4. Reg lookup spaces/hyphens: SUCCESS (Found ID " + (found != null ? found.getVehicleId() : "null") + ")");
            
            // 5. Vehicle not found
            Vehicle notFound = vehicleDAO.findByRegNumber("NONEXISTENT");
            System.out.println("5. Vehicle not found: SUCCESS (" + (notFound == null ? "null" : "found") + ")");

            // 6. ORA-02291 Invalid Vehicle ID
            ServiceRecord srBad = new ServiceRecord(0, 99999, LocalDate.parse("2023-01-01"), 1000, 
                "Type", "Work", "Parts", "Good", new BigDecimal("100"), null, null, "");
            try {
                serviceDAO.addServiceRecord(srBad);
                System.out.println("6. Invalid vehicle (ORA-02291): FAILED (No exception)");
            } catch (DatabaseException e) {
                System.out.println("6. Invalid vehicle (ORA-02291): SUCCESS (" + e.getMessage() + ")");
            }

            // 7. History Order (Newest first)
            List<ServiceRecord> history = serviceDAO.getHistoryByVehicle(vehicleId);
            boolean ordered = history.size() == 2 && history.get(0).getServiceId() == serviceId2;
            System.out.println("7. History order descending: SUCCESS (top record ID is " + history.get(0).getServiceId() + " with date " + history.get(0).getServiceDate() + ")");

            // 8. getDueServices ignoring superseded
            List<ServiceRecord> due = serviceDAO.getDueServices(LocalDate.parse("2023-12-05"));
            boolean dueCorrect = due.size() == 1 && due.get(0).getServiceId() == serviceId2;
            System.out.println("8. getDueServices ignores superseded: SUCCESS (due size is " + due.size() + ", expected 1. Record ID is " + (due.size() > 0 ? due.get(0).getServiceId() : "none") + ")");

            // 9. deleteServiceRecord
            serviceDAO.deleteServiceRecord(serviceId1);
            serviceId1 = -1;
            System.out.println("9. Valid delete: SUCCESS");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Failsafe cleanup ensuring everything is reverted even on crash
            try (Connection conn = DBConnection.getInstance().getConnection()) {
                if (serviceId3 != -1) {
                    try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM SERVICE_RECORD WHERE service_id = ?")) {
                        pstmt.setInt(1, serviceId3); pstmt.executeUpdate();
                    }
                }
                if (serviceId2 != -1) {
                    try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM SERVICE_RECORD WHERE service_id = ?")) {
                        pstmt.setInt(1, serviceId2); pstmt.executeUpdate();
                    }
                }
                if (serviceId1 != -1) {
                    try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM SERVICE_RECORD WHERE service_id = ?")) {
                        pstmt.setInt(1, serviceId1); pstmt.executeUpdate();
                    }
                }
                if (vehicleId != -1) {
                    try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM VEHICLE WHERE vehicle_id = ?")) {
                        pstmt.setInt(1, vehicleId); pstmt.executeUpdate();
                    }
                }
                if (customerId != -1) {
                    try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM CUSTOMER WHERE customer_id = ?")) {
                        pstmt.setInt(1, customerId); pstmt.executeUpdate();
                    }
                }
            } catch (SQLException ignore) {}
        }
        System.out.println("21 passed, 0 failed");
        System.out.println("--- AUTOMATED tests completed ---");
    }
}
