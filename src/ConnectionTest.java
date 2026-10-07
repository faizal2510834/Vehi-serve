import util.DBConnection;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

public class ConnectionTest {
    public static void main(String[] args) {
        System.out.println("Starting Connection Test...");
        try {
            DBConnection dbConnection = DBConnection.getInstance();
            System.out.println("Configuration loaded successfully.");
            
            try (Connection conn = dbConnection.getConnection()) {
                System.out.println("Connection established successfully!");
                DatabaseMetaData metaData = conn.getMetaData();
                System.out.println("Database Product Name: " + metaData.getDatabaseProductName());
                System.out.println("Database Product Version: \n" + metaData.getDatabaseProductVersion());
            }
        } catch (SQLException e) {
            System.err.println("Failed to connect to the database.");
            System.err.println("Error Message: " + e.getMessage());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
        } catch (RuntimeException e) {
            System.err.println("Runtime error: " + e.getMessage());
        }
    }
}
