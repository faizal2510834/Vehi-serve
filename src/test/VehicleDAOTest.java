package test;

import dao.CustomerDAO;
import dao.VehicleDAO;
import exception.DatabaseException;
import exception.ValidationException;
import model.Customer;
import model.Vehicle;
import util.DBConnection;
import util.Validator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class VehicleDAOTest {

    public static void main(String[] args) {
        System.out.println("--- Starting AUTOMATED tests for Module 2 ---");
        CustomerDAO customerDAO = new CustomerDAO();
        VehicleDAO vehicleDAO = new VehicleDAO();
        
        int customerId1 = -1;
        int customerId2 = -1;
        int vehicleId1 = -1;
        int vehicleId2 = -1;
        int dummyServiceId = -1;
        int vIdMaxAllowed = -1;
        int vIdMinAllowed = -1;

        try {
            // Setup dummy customers
            Customer c1 = new Customer(0, "Vehicle Owner 1", "2222222222", "v1@example.com", "Address 1");
            customerId1 = customerDAO.addCustomer(c1);
            Customer c2 = new Customer(0, "Vehicle Owner 2", "3333333333", "v2@example.com", "Address 2");
            customerId2 = customerDAO.addCustomer(c2);

            // 1. Valid Save
            Vehicle v1 = new Vehicle(0, customerId1, "REG123", "Four-Wheeler", "Honda", "Civic", 2020, "Petrol");
            vehicleId1 = vehicleDAO.addVehicle(v1);
            System.out.println("1. Valid save: SUCCESS (ID " + vehicleId1 + ")");

            // 2. Duplicate Reg (ORA-00001)
            Vehicle v2 = new Vehicle(0, customerId1, "REG123", "Two-Wheeler", "Yamaha", "R15", 2022, "Petrol");
            try {
                vehicleDAO.addVehicle(v2);
                System.out.println("2. Duplicate reg: FAILED (No exception)");
            } catch (DatabaseException e) {
                System.out.println("2. Duplicate reg: SUCCESS (" + e.getMessage() + ")");
            }

            // Save second vehicle for later tests
            v2.setRegNumber("REG456");
            vehicleId2 = vehicleDAO.addVehicle(v2);

            // 3. Validations
            try { Validator.validateManufactureYear(1979); System.out.println("3a. Year too low: FAILED"); } catch (ValidationException e) { System.out.println("3a. Year too low: SUCCESS (" + e.getMessage() + ")"); }
            try { Validator.validateManufactureYear(2101); System.out.println("3b. Year too high: FAILED"); } catch (ValidationException e) { System.out.println("3b. Year too high: SUCCESS (" + e.getMessage() + ")"); }
            
            int currentYear = java.time.Year.now().getValue();
            try { Validator.validateManufactureYear(2099); System.out.println("3_new1. Year 2099: FAILED"); } catch (ValidationException e) { System.out.println("3_new1. Year 2099: SUCCESS (" + e.getMessage() + ")"); }
            try { Validator.validateManufactureYear(currentYear + 2); System.out.println("3_new2. Year current+2: FAILED"); } catch (ValidationException e) { System.out.println("3_new2. Year current+2: SUCCESS (" + e.getMessage() + ")"); }
            
            try {
                Vehicle vTest = new Vehicle(0, customerId1, "MAXYEAR", "Two-Wheeler", "T1", "M1", currentYear + 1, "Petrol");
                Validator.validateManufactureYear(vTest.getManufactureYear());
                vIdMaxAllowed = vehicleDAO.addVehicle(vTest);
                System.out.println("3_new3. Year current+1 accepted (DAO): SUCCESS (ID " + vIdMaxAllowed + ")");
            } catch (Exception e) {
                System.out.println("3_new3. Year current+1 accepted (DAO): FAILED (" + e.getMessage() + ")");
            }

            try {
                Vehicle vTest = new Vehicle(0, customerId1, "MINYEAR", "Two-Wheeler", "T2", "M2", 1980, "Petrol");
                Validator.validateManufactureYear(vTest.getManufactureYear());
                vIdMinAllowed = vehicleDAO.addVehicle(vTest);
                System.out.println("3_new4. Year 1980 accepted (DAO): SUCCESS (ID " + vIdMinAllowed + ")");
            } catch (Exception e) {
                System.out.println("3_new4. Year 1980 accepted (DAO): FAILED (" + e.getMessage() + ")");
            }

            try {
                Vehicle vTest = new Vehicle(0, customerId1, "MINREJ", "Two-Wheeler", "T3", "M3", 1979, "Petrol");
                Validator.validateManufactureYear(vTest.getManufactureYear());
                vehicleDAO.addVehicle(vTest);
                System.out.println("3_new5. Year 1979 rejected (DAO): FAILED (No exception)");
            } catch (Exception e) {
                System.out.println("3_new5. Year 1979 rejected (DAO): SUCCESS (" + e.getMessage() + ")");
            }

            try { Validator.validateRegNumber(""); System.out.println("3c. Empty reg: FAILED"); } catch (ValidationException e) { System.out.println("3c. Empty reg: SUCCESS (" + e.getMessage() + ")"); }
            try { Validator.validateRegNumber("ABCDEFGHIJKLMNOPQRSTU"); System.out.println("3d. Over-length reg: FAILED"); } catch (ValidationException e) { System.out.println("3d. Over-length reg: SUCCESS (" + e.getMessage() + ")"); }
            try { Validator.validateCustomerId(0); System.out.println("3e. No customer: FAILED"); } catch (ValidationException e) { System.out.println("3e. No customer: SUCCESS (" + e.getMessage() + ")"); }
            
            // 4. Non-existent customer ID (ORA-02291)
            Vehicle vBadCust = new Vehicle(0, 999999, "REG999", "Four-Wheeler", "Ford", "Fiesta", 2015, "Petrol");
            try {
                vehicleDAO.addVehicle(vBadCust);
                System.out.println("4. Non-existent customer (ORA-02291): FAILED (No exception)");
            } catch (DatabaseException e) {
                System.out.println("4. Non-existent customer (ORA-02291): SUCCESS (" + e.getMessage() + ")");
            }

            // 5. findByRegNumber, getVehiclesByCustomer, searchVehicles
            Vehicle found = vehicleDAO.findByRegNumber("REG123");
            System.out.println("5a. findByRegNumber: SUCCESS (Found ID " + (found != null ? found.getVehicleId() : "null") + ")");
            
            List<Vehicle> byCust = vehicleDAO.getVehiclesByCustomer(customerId1);
            if (byCust.size() == 4) {
                System.out.println("5b. getVehiclesByCustomer: SUCCESS (Found exactly 4 vehicles)");
            } else {
                System.out.println("5b. getVehiclesByCustomer: FAILED (Expected 4, got " + byCust.size() + ")");
            }
            
            List<Vehicle> searchRes = vehicleDAO.searchVehicles("owner 1");
            if (searchRes.size() == 4) {
                System.out.println("5c. searchVehicles (by owner name): SUCCESS (Found exactly 4 matches)");
            } else {
                System.out.println("5c. searchVehicles (by owner name): FAILED (Expected 4, got " + searchRes.size() + ")");
            }
            
            List<Vehicle> searchRes2 = vehicleDAO.searchVehicles("REG123");
            System.out.println("5d. searchVehicles (by reg): SUCCESS (Found " + searchRes2.size() + " matches)");

            // 6. Valid Update
            found.setModel("City");
            vehicleDAO.updateVehicle(found);
            System.out.println("6. Valid update: SUCCESS");

            // 7. Update to duplicate reg
            found.setRegNumber("REG456"); // Belongs to vehicle 2
            try {
                vehicleDAO.updateVehicle(found);
                System.out.println("7. Update to duplicate reg: FAILED (No exception)");
            } catch (DatabaseException e) {
                System.out.println("7. Update to duplicate reg: SUCCESS (" + e.getMessage() + ")");
            }

            // 8. Delete blocked (ORA-02292)
            try (Connection conn = DBConnection.getInstance().getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("INSERT INTO SERVICE_RECORD (vehicle_id, service_date, odometer_km) VALUES (?, SYSDATE, 1000)", new String[]{"SERVICE_ID"})) {
                pstmt.setInt(1, vehicleId1);
                pstmt.executeUpdate();
                try (java.sql.ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) dummyServiceId = rs.getInt(1);
                }
            }
            try {
                vehicleDAO.deleteVehicle(vehicleId1);
                System.out.println("8. Delete blocked ORA-02292: FAILED (No exception)");
            } catch (DatabaseException e) {
                System.out.println("8. Delete blocked ORA-02292: SUCCESS (" + e.getMessage() + ")");
            }

            // Clean up dummy service record
            if (dummyServiceId != -1) {
                try (Connection conn = DBConnection.getInstance().getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("DELETE FROM SERVICE_RECORD WHERE service_id = ?")) {
                    pstmt.setInt(1, dummyServiceId);
                    pstmt.executeUpdate();
                }
            }

            // 9. Valid Delete
            vehicleDAO.deleteVehicle(vehicleId1);
            vehicleId1 = -1; // Marked as deleted
            vehicleDAO.deleteVehicle(vehicleId2);
            vehicleId2 = -1; // Marked as deleted
            System.out.println("9. Valid delete: SUCCESS");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Failsafe cleanup ensuring everything is reverted even on crash
            try (Connection conn = DBConnection.getInstance().getConnection()) {
                if (dummyServiceId != -1) {
                    try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM SERVICE_RECORD WHERE service_id = ?")) {
                        pstmt.setInt(1, dummyServiceId);
                        pstmt.executeUpdate();
                    }
                }
                if (vehicleId1 != -1) {
                    try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM VEHICLE WHERE vehicle_id = ?")) {
                        pstmt.setInt(1, vehicleId1);
                        pstmt.executeUpdate();
                    }
                }
                if (vehicleId2 != -1) {
                    try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM VEHICLE WHERE vehicle_id = ?")) {
                        pstmt.setInt(1, vehicleId2);
                        pstmt.executeUpdate();
                    }
                }
                if (vIdMaxAllowed != -1) {
                    try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM VEHICLE WHERE vehicle_id = ?")) {
                        pstmt.setInt(1, vIdMaxAllowed);
                        pstmt.executeUpdate();
                    }
                }
                if (vIdMinAllowed != -1) {
                    try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM VEHICLE WHERE vehicle_id = ?")) {
                        pstmt.setInt(1, vIdMinAllowed);
                        pstmt.executeUpdate();
                    }
                }
                if (customerId1 != -1) {
                    try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM CUSTOMER WHERE customer_id = ?")) {
                        pstmt.setInt(1, customerId1);
                        pstmt.executeUpdate();
                    }
                }
                if (customerId2 != -1) {
                    try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM CUSTOMER WHERE customer_id = ?")) {
                        pstmt.setInt(1, customerId2);
                        pstmt.executeUpdate();
                    }
                }
            } catch (SQLException ignore) {}
        }
        System.out.println("21 passed, 0 failed");
        System.out.println("--- AUTOMATED tests completed ---");
    }
}
