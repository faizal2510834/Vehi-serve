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

public class CustomerDAOTest {

    public static void main(String[] args) {
        System.out.println("--- Starting AUTOMATED tests for Module 1 ---");
        CustomerDAO dao = new CustomerDAO();
        int customerId1 = -1;
        int customerId2 = -1;
        int dummyVehicleId = -1;

        try {
            // 1. Valid Save
            System.out.println("1. Testing valid save...");
            Customer c1 = new Customer(0, "Test User 1", "1234567890", "test1@example.com", "Address 1");
            customerId1 = dao.addCustomer(c1);
            System.out.println("   SUCCESS. Generated ID: " + customerId1);

            // 2. Duplicate Phone (ORA-00001)
            System.out.println("2. Testing duplicate phone...");
            Customer c2 = new Customer(0, "Test User 2", "1234567890", "test2@example.com", "Address 2");
            try {
                dao.addCustomer(c2);
                System.out.println("   FAILED. Expected DatabaseException for duplicate phone.");
            } catch (DatabaseException e) {
                if (e.getMessage().contains("already exists")) {
                    System.out.println("   SUCCESS. Caught expected exception: " + e.getMessage());
                } else {
                    System.out.println("   FAILED. Unexpected message: " + e.getMessage());
                }
            }

            // Save second customer for later tests
            c2.setPhone("0987654321");
            customerId2 = dao.addCustomer(c2);

            // 3. Validation Tests
            System.out.println("3. Testing validations...");
            try { Validator.validateName(""); System.out.println("   FAILED. Expected empty name error."); } catch (ValidationException e) { System.out.println("   SUCCESS. Empty name caught: " + e.getMessage()); }
            try { Validator.validatePhone("12345"); System.out.println("   FAILED. Expected short phone error."); } catch (ValidationException e) { System.out.println("   SUCCESS. Short phone caught: " + e.getMessage()); }
            try { Validator.validateEmail("bademail"); System.out.println("   FAILED. Expected bad email error."); } catch (ValidationException e) { System.out.println("   SUCCESS. Bad email caught: " + e.getMessage()); }

            // 4. Valid Update
            System.out.println("4. Testing valid update...");
            Customer cToUpdate = dao.findByPhone("1234567890");
            cToUpdate.setAddress("Updated Address 1");
            dao.updateCustomer(cToUpdate);
            System.out.println("   SUCCESS. Update executed without error.");

            // 5. Update changing phone to duplicate
            System.out.println("5. Testing update changing to duplicate phone...");
            cToUpdate.setPhone("0987654321");
            try {
                dao.updateCustomer(cToUpdate);
                System.out.println("   FAILED. Expected duplicate phone error on update.");
            } catch (DatabaseException e) {
                System.out.println("   SUCCESS. Caught expected exception: " + e.getMessage());
            }

            // 6. Delete blocked (ORA-02292)
            System.out.println("6. Testing delete blocked by vehicle...");
            // Manually insert dummy vehicle
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
                System.out.println("   FAILED. Expected delete to be blocked.");
            } catch (DatabaseException e) {
                if (e.getMessage().contains("still has vehicles")) {
                    System.out.println("   SUCCESS. Caught expected exception: " + e.getMessage());
                } else {
                    System.out.println("   FAILED. Unexpected message: " + e.getMessage());
                }
            }

            // Clean up dummy vehicle
            if (dummyVehicleId != -1) {
                try (Connection conn = DBConnection.getInstance().getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("DELETE FROM VEHICLE WHERE vehicle_id = ?")) {
                    pstmt.setInt(1, dummyVehicleId);
                    pstmt.executeUpdate();
                }
            }

            // 7. Valid Delete
            System.out.println("7. Testing valid delete...");
            dao.deleteCustomer(customerId1);
            dao.deleteCustomer(customerId2);
            System.out.println("   SUCCESS. Customers deleted.");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Failsafe cleanup
            try (Connection conn = DBConnection.getInstance().getConnection()) {
                if (dummyVehicleId != -1) {
                    PreparedStatement pstmt1 = conn.prepareStatement("DELETE FROM VEHICLE WHERE vehicle_id = ?");
                    pstmt1.setInt(1, dummyVehicleId);
                    pstmt1.executeUpdate();
                }
                if (customerId1 != -1) {
                    PreparedStatement pstmt2 = conn.prepareStatement("DELETE FROM CUSTOMER WHERE customer_id = ?");
                    pstmt2.setInt(1, customerId1);
                    pstmt2.executeUpdate();
                }
                if (customerId2 != -1) {
                    PreparedStatement pstmt3 = conn.prepareStatement("DELETE FROM CUSTOMER WHERE customer_id = ?");
                    pstmt3.setInt(1, customerId2);
                    pstmt3.executeUpdate();
                }
            } catch (SQLException ignore) {}
        }
        System.out.println("--- AUTOMATED tests completed ---");
    }
}
