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

            System.out.println("Setup complete.");

            // 1. Case-insensitive search
            List<ServiceHistoryRow> res1 = serviceDAO.searchFullHistory("schwarze");
            if (!res1.isEmpty()) {
                System.out.println("1. Case-insensitive search ('schwarze'): SUCCESS (Found " + res1.size() + ")");
            } else {
                System.out.println("1. Case-insensitive search: FAILED (No results)");
            }

            // 2. Partial matches (Reg Number stripping)
            List<ServiceHistoryRow> res2 = serviceDAO.searchFullHistory("ka10x");
            if (!res2.isEmpty()) {
                System.out.println("2. Partial match without spaces/hyphens ('ka10x'): SUCCESS (Found " + res2.size() + ")");
            } else {
                System.out.println("2. Partial match without spaces/hyphens: FAILED (No results)");
            }

            // 3. Handling of special chars (%)
            List<ServiceHistoryRow> res3 = serviceDAO.searchFullHistory("% Test");
            if (res3.size() == 1) {
                System.out.println("3. Handling of special char '%' ('% Test'): SUCCESS (Found " + res3.size() + ", Service Type: " + res3.get(0).getServiceType() + ")");
            } else {
                System.out.println("3. Handling of special char '%': FAILED (Found " + res3.size() + ")");
            }

            // 4. Handling of special chars (_)
            List<ServiceHistoryRow> res4 = serviceDAO.searchFullHistory("_ Test");
            if (res4.size() == 1) {
                System.out.println("4. Handling of special char '_' ('_ Test'): SUCCESS (Found " + res4.size() + ", Service Type: " + res4.get(0).getServiceType() + ")");
            } else {
                System.out.println("4. Handling of special char '_': FAILED (Found " + res4.size() + ")");
            }

            // 5. Injection resistance test
            List<ServiceHistoryRow> res5 = serviceDAO.searchFullHistory("' OR 1=1 --");
            if (res5.isEmpty()) {
                System.out.println("5. Injection resistance test: SUCCESS (0 results)");
            } else {
                System.out.println("5. Injection resistance test: FAILED (Found " + res5.size() + " results)");
            }

            // 6. Newest-first order validation
            List<ServiceHistoryRow> res6 = serviceDAO.searchFullHistory("KA-10-XY-9999");
            if (res6.size() >= 2 && res6.get(0).getServiceDate().isAfter(res6.get(1).getServiceDate())) {
                System.out.println("6. Newest-first order validation: SUCCESS");
            } else {
                System.out.println("6. Newest-first order validation: FAILED");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("Cleaning up...");
            try { if (sId2 != -1) serviceDAO.deleteServiceRecord(sId2); } catch (Exception ignore) {}
            try { if (sId1 != -1) serviceDAO.deleteServiceRecord(sId1); } catch (Exception ignore) {}
            try { if (vId != -1) vehicleDAO.deleteVehicle(vId); } catch (Exception ignore) {}
            try { if (cId != -1) customerDAO.deleteCustomer(cId); } catch (Exception ignore) {}
            System.out.println("Cleanup complete.");
        }
    }
}
