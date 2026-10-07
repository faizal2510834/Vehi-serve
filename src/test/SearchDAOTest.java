package test;

import dao.CustomerDAO;
import dao.ServiceRecordDAO;
import dao.VehicleDAO;
import model.Customer;
import model.ServiceHistoryRow;
import model.ServiceRecord;
import model.Vehicle;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class SearchDAOTest {
    public static void main(String[] args) {
        System.out.println("--- Starting SearchDAOTest ---");
        CustomerDAO customerDAO = new CustomerDAO();
        VehicleDAO vehicleDAO = new VehicleDAO();
        ServiceRecordDAO serviceDAO = new ServiceRecordDAO();

        int cId = -1;
        int vId = -1;
        int sId1 = -1;
        int sId2 = -1;
        int cIdOther = -1;
        int vIdOther = -1;
        int sIdOther = -1;
        
        int[] bulkSIds = new int[501];
        boolean bulkInserted = false;

        try {
            // Setup dummy data
            Customer c = new Customer(0, "Schwarzenegger", "9998887776", "arnie@test.com", "Address");
            cId = customerDAO.addCustomer(c);

            Vehicle v = new Vehicle(0, cId, "KA-10-XY-9999", "Four-Wheeler", "Toyota", "Corolla", 2020, "Petrol");
            vId = vehicleDAO.addVehicle(v);

            ServiceRecord s1 = new ServiceRecord(0, vId, LocalDate.parse("2023-01-01"), 10000, "General Service % Test",
                "Work", "Parts", "Good", new BigDecimal("100.00"), LocalDate.parse("2023-06-01"), 15000, "Remarks");
            sId1 = serviceDAO.addServiceRecord(s1);

            ServiceRecord s2 = new ServiceRecord(0, vId, LocalDate.parse("2023-02-01"), 11000, "Oil Change _ Test",
                "Work2", "Parts2", "Good", new BigDecimal("200.00"), LocalDate.parse("2023-07-01"), 16000, "Remarks2");
            sId2 = serviceDAO.addServiceRecord(s2);
            
            Customer cOther = new Customer(0, "Stallone", "1112223334", "sly@test.com", "Address");
            cIdOther = customerDAO.addCustomer(cOther);
            Vehicle vOther = new Vehicle(0, cIdOther, "MH-12-AB-1234", "Two-Wheeler", "Honda", "Activa", 2019, "Petrol");
            vIdOther = vehicleDAO.addVehicle(vOther);
            ServiceRecord sOther = new ServiceRecord(0, vIdOther, LocalDate.parse("2023-03-01"), 5000, "Brake pad change",
                "Work3", "Parts3", "Average", new BigDecimal("50.00"), LocalDate.parse("2023-08-01"), 10000, "Remarks3");
            sIdOther = serviceDAO.addServiceRecord(sOther);

            System.out.println("Setup complete.");

            // 1. Partial phone
            List<ServiceHistoryRow> resPhone = serviceDAO.searchFullHistory("888777");
            if (!resPhone.isEmpty() && resPhone.get(0).getCustomerPhone().contains("888777")) {
                System.out.println("1. Partial phone search ('888777'): SUCCESS (Found " + resPhone.size() + ")");
            } else {
                System.out.println("1. Partial phone search: FAILED");
            }
            
            // 2. Service type in different case
            List<ServiceHistoryRow> resType = serviceDAO.searchFullHistory("gEnEral sERVICE");
            if (!resType.isEmpty()) {
                System.out.println("2. Service type different case ('gEnEral sERVICE'): SUCCESS (Found " + resType.size() + ")");
            } else {
                System.out.println("2. Service type different case: FAILED");
            }
            
            // 3. No match
            List<ServiceHistoryRow> resNone = serviceDAO.searchFullHistory("zxcvbnm");
            if (resNone.isEmpty()) {
                System.out.println("3. No match search ('zxcvbnm'): SUCCESS (Empty list)");
            } else {
                System.out.println("3. No match search: FAILED (Found " + resNone.size() + ")");
            }
            
            // 4. Empty keyword
            List<ServiceHistoryRow> resEmpty = serviceDAO.searchFullHistory("");
            if (!resEmpty.isEmpty() && resEmpty.size() >= 3) {
                System.out.println("4. Empty keyword search: SUCCESS (Found " + resEmpty.size() + ")");
            } else {
                System.out.println("4. Empty keyword search: FAILED");
            }
            
            // 5. Reg searched as "ka-10 x" and "KA 10 X"
            List<ServiceHistoryRow> resReg1 = serviceDAO.searchFullHistory("ka-10 x");
            List<ServiceHistoryRow> resReg2 = serviceDAO.searchFullHistory("KA 10 X");
            if (!resReg1.isEmpty() && !resReg2.isEmpty() && resReg1.size() == resReg2.size()) {
                System.out.println("5. Reg searched with spaces/hyphens ('ka-10 x' and 'KA 10 X'): SUCCESS (Found " + resReg1.size() + ")");
            } else {
                System.out.println("5. Reg searched with spaces/hyphens: FAILED");
            }
            
            // 6. Another customer's records not in results
            List<ServiceHistoryRow> resArnold = serviceDAO.searchFullHistory("schwarze");
            boolean stalloneFound = resArnold.stream().anyMatch(r -> r.getCustomerName().equals("Stallone"));
            if (!stalloneFound) {
                System.out.println("6. Another customer's records not in results: SUCCESS");
            } else {
                System.out.println("6. Another customer's records not in results: FAILED (Found Stallone)");
            }
            
            // 7. 500-row cap
            System.out.println("7. Inserting 501 rows for cap test...");
            for(int i=0; i<501; i++) {
                 ServiceRecord bulkS = new ServiceRecord(0, vIdOther, LocalDate.parse("2023-01-01"), 1000 + i, "Bulk " + i,
                    "Work", "Parts", "Good", new BigDecimal("10.00"), null, null, "Remarks");
                 bulkSIds[i] = serviceDAO.addServiceRecord(bulkS);
            }
            bulkInserted = true;
            
            List<ServiceHistoryRow> resCap = serviceDAO.searchFullHistory("Stallone");
            if (resCap.size() == 500) {
                System.out.println("7. 500-row cap test: SUCCESS (Returned exactly 500)");
            } else {
                System.out.println("7. 500-row cap test: FAILED (Returned " + resCap.size() + ")");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("Cleaning up...");
            if (bulkInserted) {
                for(int i=0; i<501; i++) {
                    try { if (bulkSIds[i] > 0) serviceDAO.deleteServiceRecord(bulkSIds[i]); } catch (Exception ignore) {}
                }
            }
            try { if (sIdOther != -1) serviceDAO.deleteServiceRecord(sIdOther); } catch (Exception ignore) {}
            try { if (vIdOther != -1) vehicleDAO.deleteVehicle(vIdOther); } catch (Exception ignore) {}
            try { if (cIdOther != -1) customerDAO.deleteCustomer(cIdOther); } catch (Exception ignore) {}

            try { if (sId2 != -1) serviceDAO.deleteServiceRecord(sId2); } catch (Exception ignore) {}
            try { if (sId1 != -1) serviceDAO.deleteServiceRecord(sId1); } catch (Exception ignore) {}
            try { if (vId != -1) vehicleDAO.deleteVehicle(vId); } catch (Exception ignore) {}
            try { if (cId != -1) customerDAO.deleteCustomer(cId); } catch (Exception ignore) {}
            System.out.println("Cleanup complete.");
        }
    }
}
