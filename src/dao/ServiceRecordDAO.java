package dao;

import exception.DatabaseException;
import model.ServiceRecord;
import util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ServiceRecordDAO {

    private void translateSQLException(SQLException e) throws DatabaseException {
        if (e.getErrorCode() == 2291) {
            throw new DatabaseException("The selected vehicle does not exist.");
        }
        throw new DatabaseException("Database error: " + e.getMessage());
    }

    private ServiceRecord mapResultSetToServiceRecord(ResultSet rs) throws SQLException {
        Date serviceDate = rs.getDate("service_date");
        Date nextDate = rs.getDate("next_service_date");
        
        return new ServiceRecord(
                rs.getInt("service_id"),
                rs.getInt("vehicle_id"),
                serviceDate != null ? serviceDate.toLocalDate() : null,
                rs.getInt("odometer_km"),
                rs.getString("service_type"),
                rs.getString("work_done"),
                rs.getString("parts_replaced"),
                rs.getString("vehicle_condition"),
                rs.getBigDecimal("cost"),
                nextDate != null ? nextDate.toLocalDate() : null,
                rs.getObject("next_service_km") != null ? rs.getInt("next_service_km") : null,
                rs.getString("remarks")
        );
    }

    public int addServiceRecord(ServiceRecord record) throws DatabaseException {
        String sql = "INSERT INTO SERVICE_RECORD (vehicle_id, service_date, odometer_km, service_type, " +
                     "work_done, parts_replaced, vehicle_condition, cost, next_service_date, next_service_km, remarks) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, new String[]{"SERVICE_ID"})) {

            pstmt.setInt(1, record.getVehicleId());
            pstmt.setDate(2, record.getServiceDate() != null ? Date.valueOf(record.getServiceDate()) : null);
            pstmt.setInt(3, record.getOdometerKm());
            pstmt.setString(4, record.getServiceType());
            pstmt.setString(5, record.getWorkDone());
            pstmt.setString(6, record.getPartsReplaced());
            pstmt.setString(7, record.getVehicleCondition());
            pstmt.setBigDecimal(8, record.getCost());
            
            if (record.getNextServiceDate() != null) {
                pstmt.setDate(9, Date.valueOf(record.getNextServiceDate()));
            } else {
                pstmt.setNull(9, Types.DATE);
            }
            
            if (record.getNextServiceKm() != null) {
                pstmt.setInt(10, record.getNextServiceKm());
            } else {
                pstmt.setNull(10, Types.NUMERIC);
            }
            
            pstmt.setString(11, record.getRemarks());

            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            throw new DatabaseException("Failed to retrieve generated service ID.");
        } catch (SQLException e) {
            translateSQLException(e);
            return -1;
        }
    }

    public List<ServiceRecord> getHistoryByVehicle(int vehicleId) throws DatabaseException {
        List<ServiceRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM SERVICE_RECORD WHERE vehicle_id = ? ORDER BY service_date DESC, service_id DESC";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setInt(1, vehicleId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    records.add(mapResultSetToServiceRecord(rs));
                }
            }
        } catch (SQLException e) {
            translateSQLException(e);
        }
        return records;
    }

    public int getLatestOdometer(int vehicleId) throws DatabaseException {
        String sql = "SELECT MAX(odometer_km) FROM SERVICE_RECORD WHERE vehicle_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setInt(1, vehicleId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1); // Returns 0 if no records
                }
            }
        } catch (SQLException e) {
            translateSQLException(e);
        }
        return 0;
    }

    public void deleteServiceRecord(int serviceId) throws DatabaseException {
        String sql = "DELETE FROM SERVICE_RECORD WHERE service_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, serviceId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new DatabaseException("Delete failed: Service record not found.");
            }
        } catch (SQLException e) {
            translateSQLException(e);
        }
    }

    public List<ServiceRecord> getDueServices(LocalDate upToDate) throws DatabaseException {
        List<ServiceRecord> records = new ArrayList<>();
        // Get only the LATEST service record per vehicle where next_service_date is <= upToDate
        String sql = "SELECT sr.* FROM SERVICE_RECORD sr " +
                     "INNER JOIN ( " +
                     "   SELECT vehicle_id, MAX(service_date) as max_date, MAX(service_id) as max_id " +
                     "   FROM SERVICE_RECORD " +
                     "   GROUP BY vehicle_id " +
                     ") latest ON sr.vehicle_id = latest.vehicle_id AND sr.service_id = latest.max_id " +
                     "WHERE sr.next_service_date IS NOT NULL AND sr.next_service_date <= ? " +
                     "ORDER BY sr.next_service_date ASC";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setDate(1, Date.valueOf(upToDate));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    records.add(mapResultSetToServiceRecord(rs));
                }
            }
        } catch (SQLException e) {
            translateSQLException(e);
        }
        return records;
    }

    public List<model.ServiceHistoryRow> searchFullHistory(String keyword) throws DatabaseException {
        List<model.ServiceHistoryRow> results = new ArrayList<>();
        String sql = "SELECT s.service_date, v.reg_number, v.make || ' ' || v.model AS make_model, " +
                     "c.name, c.phone, s.service_type, s.odometer_km, s.cost, s.next_service_date, s.next_service_km, " +
                     "s.work_done, s.parts_replaced, s.remarks " +
                     "FROM SERVICE_RECORD s " +
                     "INNER JOIN VEHICLE v ON s.vehicle_id = v.vehicle_id " +
                     "INNER JOIN CUSTOMER c ON v.customer_id = c.customer_id ";
                     
        boolean hasFilter = keyword != null && !keyword.trim().isEmpty();
        if (hasFilter) {
            sql += "WHERE UPPER(c.name) LIKE ? ESCAPE '\\' " +
                   "OR UPPER(c.phone) LIKE ? ESCAPE '\\' " +
                   "OR REPLACE(REPLACE(UPPER(v.reg_number), '-', ''), ' ', '') LIKE ? ESCAPE '\\' " +
                   "OR UPPER(s.service_type) LIKE ? ESCAPE '\\' ";
        }
        
        sql += "ORDER BY s.service_date DESC, s.service_id DESC " +
               "FETCH FIRST 500 ROWS ONLY";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            if (hasFilter) {
                String escapedKw = keyword.replace("\\", "\\\\")
                                          .replace("%", "\\%")
                                          .replace("_", "\\_");
                String likeKw = "%" + escapedKw.toUpperCase() + "%";
                String strippedRegLike = "%" + escapedKw.replace(" ", "").replace("-", "").toUpperCase() + "%";
                
                pstmt.setString(1, likeKw);
                pstmt.setString(2, likeKw);
                pstmt.setString(3, strippedRegLike);
                pstmt.setString(4, likeKw);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    java.sql.Date nextSqlDate = rs.getDate("next_service_date");
                    LocalDate nextDate = nextSqlDate != null ? nextSqlDate.toLocalDate() : null;
                    
                    Integer nextKm = null;
                    int nKm = rs.getInt("next_service_km");
                    if (!rs.wasNull()) {
                        nextKm = nKm;
                    }
                    
                    results.add(new model.ServiceHistoryRow(
                        rs.getDate("service_date").toLocalDate(),
                        rs.getString("reg_number"),
                        rs.getString("make_model"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("service_type"),
                        rs.getInt("odometer_km"),
                        rs.getBigDecimal("cost"),
                        nextDate,
                        nextKm,
                        rs.getString("work_done"),
                        rs.getString("parts_replaced"),
                        rs.getString("remarks")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to search history: " + e.getMessage());
        }
        return results;
    }
}
