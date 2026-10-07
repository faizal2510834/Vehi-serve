package util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ServicePredictor {

    public static class Prediction {
        public final LocalDate nextDate;
        public final int nextKm;
        public final String ruleUsed;

        public Prediction(LocalDate nextDate, int nextKm, String ruleUsed) {
            this.nextDate = nextDate;
            this.nextKm = nextKm;
            this.ruleUsed = ruleUsed;
        }
    }

    public static Prediction predictNextService(String vehicleType, String condition, int manufactureYear, LocalDate serviceDate, int currentOdometer) {
        if (vehicleType == null || condition == null || serviceDate == null || currentOdometer < 0) {
            throw new IllegalArgumentException("Invalid input for prediction");
        }

        double baseDays;
        double baseKm;

        if ("Two-Wheeler".equals(vehicleType)) {
            baseDays = 180.0;
            baseKm = 3000.0;
        } else if ("Four-Wheeler".equals(vehicleType)) {
            baseDays = 365.0;
            baseKm = 10000.0;
        } else {
            throw new IllegalArgumentException("Unknown vehicle type: " + vehicleType);
        }

        double conditionFactor;
        switch (condition) {
            case "Good": conditionFactor = 1.0; break;
            case "Average": conditionFactor = 0.9; break;
            case "Poor": conditionFactor = 0.8; break;
            case "Critical": conditionFactor = 0.6; break;
            default: throw new IllegalArgumentException("Unknown condition: " + condition);
        }

        int age = serviceDate.getYear() - manufactureYear;
        double ageFactor = (age >= 11) ? 0.9 : 1.0;

        double combinedFactor = conditionFactor * ageFactor;

        long finalDays = Math.round(baseDays * combinedFactor);
        long finalKmToAdd = Math.round((baseKm * combinedFactor) / 100.0) * 100L;

        if (finalDays < 30) finalDays = 30;
        if (finalKmToAdd < 500) finalKmToAdd = 500;

        LocalDate nextDate = serviceDate.plusDays(finalDays);
        int nextKm = currentOdometer + (int) finalKmToAdd;
        
        String ruleLabel = String.format("Calculated based on %s, %s condition", vehicleType, condition);
        if (ageFactor < 1.0) {
            ruleLabel += " (Age >= 11 yrs applied)";
        }

        return new Prediction(nextDate, nextKm, ruleLabel);
    }
}
