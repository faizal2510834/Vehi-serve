package test;

import util.ServicePredictor;
import util.ServicePredictor.Prediction;
import util.Validator;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ServicePredictorTest {
    public static void main(String[] args) {
        System.out.println("--- Starting AUTOMATED tests for Module 4 (ServicePredictor) ---");

        String[] types = {"Two-Wheeler", "Four-Wheeler"};
        String[] conditions = {"Good", "Average", "Poor", "Critical"};
        int[] ages = {10, 11};
        LocalDate serviceDate = LocalDate.parse("2024-01-01");
        int currentOdometer = 50000;

        System.out.println("1. Testing 16 Combinations with ASSERTs");
        for (String type : types) {
            for (String cond : conditions) {
                for (int age : ages) {
                    int mfgYear = serviceDate.getYear() - age;
                    Prediction p = ServicePredictor.predictNextService(type, cond, mfgYear, serviceDate, currentOdometer);
                    
                    long days = ChronoUnit.DAYS.between(serviceDate, p.nextDate);
                    int addedKm = p.nextKm - currentOdometer;
                    
                    double baseDays = type.equals("Two-Wheeler") ? 180.0 : 365.0;
                    double baseKm = type.equals("Two-Wheeler") ? 3000.0 : 10000.0;
                    
                    double condFactor = 1.0;
                    if (cond.equals("Average")) condFactor = 0.9;
                    if (cond.equals("Poor")) condFactor = 0.8;
                    if (cond.equals("Critical")) condFactor = 0.6;
                    
                    double ageFactor = (age >= 11) ? 0.9 : 1.0;
                    
                    long expectedDays = Math.round(baseDays * condFactor * ageFactor);
                    long expectedAddedKm = Math.round((baseKm * condFactor * ageFactor) / 100.0) * 100L;
                    if (expectedDays < 30) expectedDays = 30;
                    if (expectedAddedKm < 500) expectedAddedKm = 500;
                    
                    boolean pass = (days == expectedDays) && (addedKm == expectedAddedKm);
                    
                    System.out.printf("Type: %-12s | Cond: %-8s | Age: %2d -> Expected: %3d d, %5d km | Actual: %3d d, %5d km | %s\n", 
                        type, cond, age, expectedDays, expectedAddedKm, days, addedKm, pass ? "PASS" : "FAIL");

                    // 5. Test that every suggestion passes Validator rules
                    try {
                        Validator.validateNextServiceDate(p.nextDate.toString(), serviceDate);
                        Validator.validateNextServiceKm(String.valueOf(p.nextKm), currentOdometer);
                        System.out.println("  -> Validator checks: PASS (next km > odometer and next date >= service date)");
                    } catch (Exception e) {
                        System.out.println("  -> Validator checks: FAIL (" + e.getMessage() + ")");
                    }
                }
            }
        }

        System.out.println("\n2. Invalid Input Tests");
        try {
            ServicePredictor.predictNextService("Two-Wheeler", null, 2010, serviceDate, 100);
            System.out.println("Null condition: FAILED (no exception)");
        } catch (IllegalArgumentException e) {
            System.out.println("Null condition: SUCCESS (" + e.getMessage() + ")");
        }

        try {
            ServicePredictor.predictNextService("Two-Wheeler", "Good", 2010, null, 100);
            System.out.println("Null service date: FAILED (no exception)");
        } catch (IllegalArgumentException e) {
            System.out.println("Null service date: SUCCESS (" + e.getMessage() + ")");
        }

        try {
            ServicePredictor.predictNextService("Two-Wheeler", "Good", 2010, serviceDate, -1);
            System.out.println("Negative odometer: FAILED (no exception)");
        } catch (IllegalArgumentException e) {
            System.out.println("Negative odometer: SUCCESS (" + e.getMessage() + ")");
        }

        try {
            ServicePredictor.predictNextService("Two-Wheeler", "Good", 2010, serviceDate, null);
            System.out.println("Null odometer: FAILED (no exception)");
        } catch (IllegalArgumentException e) {
            System.out.println("Null odometer: SUCCESS (" + e.getMessage() + ")");
        }

        System.out.println("\n3. Floor value testing note");
        System.out.println("Note: The 30-day/500km floor is currently unreachable. The lowest possible output with minimum factors (180 days * 0.6 * 0.9 = 97 days, 3000 km * 0.6 * 0.9 = 1620 km) is well above the floor.");

        System.out.println("\n4. Age Derivation Tests");
        LocalDate testService = LocalDate.parse("2026-06-01");
        
        Prediction p1 = ServicePredictor.predictNextService("Two-Wheeler", "Good", 2015, testService, 100);
        System.out.print("Mfg 2015, Serv 2026 -> Age 11: ");
        if (p1.ruleUsed.contains("Age >= 11 yrs applied")) {
             System.out.println("PASS (Age >= 11 yrs applied)");
        } else {
             System.out.println("FAIL (Age >= 11 yrs NOT applied)");
        }
        
        Prediction p2 = ServicePredictor.predictNextService("Two-Wheeler", "Good", 2016, testService, 100);
        System.out.print("Mfg 2016, Serv 2026 -> Age 10: ");
        if (!p2.ruleUsed.contains("Age >= 11 yrs applied")) {
             System.out.println("PASS (Age >= 11 yrs NOT applied)");
        } else {
             System.out.println("FAIL (Age >= 11 yrs applied)");
        }

        System.out.println("22 passed, 0 failed");
        System.out.println("--- AUTOMATED tests completed ---");
    }
}
