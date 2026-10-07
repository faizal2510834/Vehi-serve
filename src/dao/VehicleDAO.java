package dao;

import exception.DatabaseException;
import model.Vehicle;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VehicleDAO {

    private void translateSQLException(SQLException e) throws DatabaseException {
        if (e.getErrorCode() == 1) { // ORA-00001: unique constraint violated
            throw new DatabaseException("A vehicle with this registration number already exists.");
        } else if (e.getErrorCode() == 2291) { // ORA-02291: integrity constraint violated - parent key not found
            throw new DatabaseException("The selected customer does not exist.");
        } else if (e.getErrorCode() == 2292) { // ORA-02292: integrity constraint violated - child record found
            throw new DatabaseException("This vehicle has service records and cannot be deleted.");
        }
        throw new DatabaseException("Database error: " + e.getMessage());
    }

    private Vehicle mapResultSetToVehicle(ResultSet rs) throws SQLException {
        return new Vehicle(
                rs.getInt("vehicle_id"),
                rs.getInt("customer_id"),
                rs.getString("reg_number"),
                rs.getString("vehicle_type"),
                rs.getString("make"),
                rs.getString("model"),
                rs.getInt("manufacture_year"),
                rs.getString("fuel_type")
        );
    }

    public int addVehicle(Vehicle vehicle) throws DatabaseException {
        String sql = "INSERT INTO VEHICLE (customer_id, reg_number, vehicle_type, make, model, manufacture_year, fuel_type) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, new String[]{"VEHICLE_ID"})) {

            pstmt.setInt(1, vehicle.getCustomerId());
            pstmt.setString(2, vehicle.getRegNumber());
            pstmt.setString(3, vehicle.getVehicleType());
            pstmt.setString(4, vehicle.getMake());
            pstmt.setString(5, vehicle.getModel());
            pstmt.setInt(6, vehicle.getManufactureYear());
            pstmt.setString(7, vehicle.getFuelType());

            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            throw new DatabaseException("Failed to retrieve generated vehicle ID.");
        } catch (SQLException e) {
            translateSQLException(e);
            return -1; // Unreachable
        }
    }

    public void updateVehicle(Vehicle vehicle) throws DatabaseException {
        String sql = "UPDATE VEHICLE SET customer_id = ?, reg_number = ?, vehicle_type = ?, " +
                     "make = ?, model = ?, manufacture_year = ?, fuel_type = ? WHERE vehicle_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, vehicle.getCustomerId());
            pstmt.setString(2, vehicle.getRegNumber());
            pstmt.setString(3, vehicle.getVehicleType());
            pstmt.setString(4, vehicle.getMake());
            pstmt.setString(5, vehicle.getModel());
            pstmt.setInt(6, vehicle.getManufactureYear());
            pstmt.setString(7, vehicle.getFuelType());
            pstmt.setInt(8, vehicle.getVehicleId());

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new DatabaseException("Update failed: Vehicle not found.");
            }
        } catch (SQLException e) {
            translateSQLException(e);
        }
    }

    public void deleteVehicle(int vehicleId) throws DatabaseException {
        String sql = "DELETE FROM VEHICLE WHERE vehicle_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, vehicleId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new DatabaseException("Delete failed: Vehicle not found.");
            }
        } catch (SQLException e) {
            translateSQLException(e);
        }
    }

    public List<Vehicle> getAllVehicles() throws DatabaseException {
        List<Vehicle> vehicles = new ArrayList<>();
        String sql = "SELECT * FROM VEHICLE ORDER BY vehicle_id DESC";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                vehicles.add(mapResultSetToVehicle(rs));
            }
        } catch (SQLException e) {
            translateSQLException(e);
        }
        return vehicles;
    }

    public Vehicle findByRegNumber(String regNumber) throws DatabaseException {
        String sql = "SELECT * FROM VEHICLE WHERE reg_number = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, regNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToVehicle(rs);
                }
            }
        } catch (SQLException e) {
            translateSQLException(e);
        }
        return null;
    }

    public List<Vehicle> getVehiclesByCustomer(int customerId) throws DatabaseException {
        List<Vehicle> vehicles = new ArrayList<>();
        String sql = "SELECT * FROM VEHICLE WHERE customer_id = ? ORDER BY vehicle_id DESC";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setInt(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    vehicles.add(mapResultSetToVehicle(rs));
                }
            }
        } catch (SQLException e) {
            translateSQLException(e);
        }
        return vehicles;
    }

    public List<Vehicle> searchVehicles(String keyword) throws DatabaseException {
        List<Vehicle> vehicles = new ArrayList<>();
        // Search by vehicle reg number OR by owner name
        String sql = "SELECT v.* FROM VEHICLE v " +
                     "LEFT JOIN CUSTOMER c ON v.customer_id = c.customer_id " +
                     "WHERE LOWER(v.reg_number) LIKE ? OR LOWER(c.name) LIKE ? " +
                     "ORDER BY v.vehicle_id DESC";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            String searchStr = "%" + keyword.toLowerCase() + "%";
            pstmt.setString(1, searchStr);
            pstmt.setString(2, searchStr);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    vehicles.add(mapResultSetToVehicle(rs));
                }
            }
        } catch (SQLException e) {
            translateSQLException(e);
        }
        return vehicles;
    }
}
