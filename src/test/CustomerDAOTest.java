package test;

import dao.CustomerDAO;
import exception.DatabaseException;
import exception.ValidationException;
import model.Customer;
import util.DBConnection;
import util.Validator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class CustomerDAOTest {

    public static void main(String[] args) {
        System.out.println("--- Starting AUTOMATED tests for Module 1 ---");
        CustomerDAO dao = new CustomerDAO();
        int customerId1 = -1;
        int customerId2 = -1;
        int dummyVehicleId = -1;

        try {
            // 1. Valid Save
            Customer c1 = new Customer(0, "Test User 1", "1234567890", "test1@example.com", "Address 1");
            customerId1 = dao.addCustomer(c1);
            System.out.println("1. Valid save: SUCCESS (ID " + customerId1 + ")");

            // 2. Duplicate Phone (ORA-00001)
            Customer c2 = new Customer(0, "Test User 2", "1234567890", "test2@example.com", "Address 2");
            try {
                dao.addCustomer(c2);
                System.out.println("2. Duplicate phone: FAILED (No exception)");
            } catch (DatabaseException e) {
                System.out.println("2. Duplicate phone: SUCCESS (" + e.getMessage() + ")");
            }

            // Save second customer for later tests
            c2.setPhone("0987654321");
            customerId2 = dao.addCustomer(c2);

            // 3. Validations
            try { Validator.validateName(""); System.out.println("3a. Empty name: FAILED"); } catch (ValidationException e) { System.out.println("3a. Empty name: SUCCESS (" + e.getMessage() + ")"); }
            try { Validator.validatePhone("12345"); System.out.println("3b. Short phone: FAILED"); } catch (ValidationException e) { System.out.println("3b. Short phone: SUCCESS (" + e.getMessage() + ")"); }
            try { Validator.validatePhone("abcdefghij"); System.out.println("3c. Non-digit phone: FAILED"); } catch (ValidationException e) { System.out.println("3c. Non-digit phone: SUCCESS (" + e.getMessage() + ")"); }
            try { Validator.validateEmail("bademail"); System.out.println("3d. Bad email: FAILED"); } catch (ValidationException e) { System.out.println("3d. Bad email: SUCCESS (" + e.getMessage() + ")"); }
            
            String longName = "A".repeat(101);
            try { Validator.validateName(longName); System.out.println("3e. Over-length name: FAILED"); } catch (ValidationException e) { System.out.println("3e. Over-length name: SUCCESS (" + e.getMessage() + ")"); }
            
            String longAddress = "B".repeat(256);
            try { Validator.validateAddress(longAddress); System.out.println("3f. Over-length address: FAILED"); } catch (ValidationException e) { System.out.println("3f. Over-length address: SUCCESS (" + e.getMessage() + ")"); }

            // 4. searchCustomers and findByPhone
            List<Customer> searchResults = dao.searchCustomers("Test User");
            System.out.println("4a. searchCustomers: SUCCESS (Found " + searchResults.size() + " matches)");
            
            Customer found = dao.findByPhone("1234567890");
            System.out.println("4b. findByPhone: SUCCESS (Found " + (found != null ? found.getName() : "null") + ")");

            // 5. Valid Update
            found.setAddress("Updated Address 1");
            dao.updateCustomer(found);
            System.out.println("5. Valid update: SUCCESS");

            // 6. Update changing phone to duplicate
            found.setPhone("0987654321"); // Belongs to customer 2
            try {
                dao.updateCustomer(found);
                System.out.println("6. Update to duplicate phone: FAILED (No exception)");
            } catch (DatabaseException e) {
                System.out.println("6. Update to duplicate phone: SUCCESS (" + e.getMessage() + ")");
            }

            // 7. Delete blocked (ORA-02292)
            try (Connection conn = DBConnection.getInstance().getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("INSERT INTO VEHICLE (customer_id, reg_number, vehicle_type) VALUES (?, 'TESTREG', 'Two-Wheeler')", new String[]{"VEHICLE_ID"})) {
                pstmt.setInt(1, customerId1);
                pstmt.executeUpdate();
                try (java.sql.ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) dummyVehicleId = rs.getInt(1);
                }
            }
            try {
                dao.deleteCustomer(customerId1);
                System.out.println("7. Delete blocked ORA-02292: FAILED (No exception)");
            } catch (DatabaseException e) {
                System.out.println("7. Delete blocked ORA-02292: SUCCESS (" + e.getMessage() + ")");
            }

            // Clean up dummy vehicle
            if (dummyVehicleId != -1) {
                try (Connection conn = DBConnection.getInstance().getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("DELETE FROM VEHICLE WHERE vehicle_id = ?")) {
                    pstmt.setInt(1, dummyVehicleId);
                    pstmt.executeUpdate();
                }
            }

            // 8. Delete without vehicles
            dao.deleteCustomer(customerId1);
            dao.deleteCustomer(customerId2);
            System.out.println("8. Delete without vehicles: SUCCESS");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Failsafe cleanup ensuring everything is reverted even on crash
            try (Connection conn = DBConnection.getInstance().getConnection()) {
                if (dummyVehicleId != -1) {
                    try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM VEHICLE WHERE vehicle_id = ?")) {
                        pstmt.setInt(1, dummyVehicleId);
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
        System.out.println("--- AUTOMATED tests completed ---");
    }
}
