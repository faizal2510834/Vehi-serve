package dao;

import exception.DatabaseException;
import model.Customer;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    private void handleSQLException(SQLException e, String action) throws DatabaseException {
        if (e.getErrorCode() == 1) { // ORA-00001
            throw new DatabaseException("A customer with this phone number already exists.");
        } else if (e.getErrorCode() == 2292) { // ORA-02292
            throw new DatabaseException("This customer still has vehicles and cannot be deleted.");
        }
        throw new DatabaseException("Database error during " + action + ": " + e.getMessage(), e);
    }

    public int addCustomer(Customer customer) throws DatabaseException {
        String sql = "INSERT INTO CUSTOMER (name, phone, email, address) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, new String[]{"CUSTOMER_ID"})) {
            
            pstmt.setString(1, customer.getName());
            pstmt.setString(2, customer.getPhone());
            pstmt.setString(3, customer.getEmail());
            pstmt.setString(4, customer.getAddress());
            
            pstmt.executeUpdate();
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            throw new DatabaseException("Failed to retrieve generated customer ID.");
        } catch (SQLException e) {
            handleSQLException(e, "adding customer");
            return -1; // Unreachable
        }
    }

    public void updateCustomer(Customer customer) throws DatabaseException {
        String sql = "UPDATE CUSTOMER SET name = ?, phone = ?, email = ?, address = ? WHERE customer_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, customer.getName());
            pstmt.setString(2, customer.getPhone());
            pstmt.setString(3, customer.getEmail());
            pstmt.setString(4, customer.getAddress());
            pstmt.setInt(5, customer.getCustomerId());
            
            int affected = pstmt.executeUpdate();
            if (affected == 0) {
                throw new DatabaseException("Customer not found for update.");
            }
        } catch (SQLException e) {
            handleSQLException(e, "updating customer");
        }
    }

    public void deleteCustomer(int customerId) throws DatabaseException {
        String sql = "DELETE FROM CUSTOMER WHERE customer_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, customerId);
            int affected = pstmt.executeUpdate();
            if (affected == 0) {
                throw new DatabaseException("Customer not found for deletion.");
            }
        } catch (SQLException e) {
            handleSQLException(e, "deleting customer");
        }
    }

    public List<Customer> getAllCustomers() throws DatabaseException {
        return searchCustomers("");
    }

    public List<Customer> searchCustomers(String keyword) throws DatabaseException {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT customer_id, name, phone, email, address FROM CUSTOMER " +
                     "WHERE LOWER(name) LIKE ? OR phone LIKE ? ORDER BY name";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + keyword.toLowerCase().trim() + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    customers.add(new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("address")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database error during search: " + e.getMessage(), e);
        }
        return customers;
    }

    public Customer findByPhone(String phone) throws DatabaseException {
        String sql = "SELECT customer_id, name, phone, email, address FROM CUSTOMER WHERE phone = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, phone.trim());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("address")
                    );
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database error during find by phone: " + e.getMessage(), e);
        }
        return null;
    }
}
