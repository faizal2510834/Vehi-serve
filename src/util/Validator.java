package util;

import exception.ValidationException;

public class Validator {
    
    public static String validateName(String name) throws ValidationException {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Name cannot be empty.");
        }
        name = name.trim();
        if (name.length() > 100) {
            throw new ValidationException("Name cannot exceed 100 characters.");
        }
        return name;
    }

    public static String validatePhone(String phone) throws ValidationException {
        if (phone == null || phone.trim().isEmpty()) {
            throw new ValidationException("Phone number cannot be empty.");
        }
        phone = phone.trim();
        if (!phone.matches("\\d{10}")) {
            throw new ValidationException("Phone number must be exactly 10 digits.");
        }
        return phone;
    }

    public static String validateEmail(String email) throws ValidationException {
        if (email == null || email.trim().isEmpty()) {
            return null; // Email is optional
        }
        email = email.trim();
        if (email.length() > 100) {
            throw new ValidationException("Email cannot exceed 100 characters.");
        }
        // Basic email validation
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new ValidationException("Invalid email format.");
        }
        return email;
    }

    public static String validateAddress(String address) throws ValidationException {
        if (address == null || address.trim().isEmpty()) {
            return null; // Address is optional
        }
        address = address.trim();
        if (address.length() > 255) {
            throw new ValidationException("Address cannot exceed 255 characters.");
        }
        return address;
    }

    public static void validateCustomerId(int customerId) throws ValidationException {
        if (customerId <= 0) {
            throw new ValidationException("No customer selected.");
        }
    }

    public static String validateRegNumber(String regNumber) throws ValidationException {
        if (regNumber == null || regNumber.trim().isEmpty()) {
            throw new ValidationException("Registration number cannot be empty.");
        }
        regNumber = regNumber.replace(" ", "").replace("-", "").toUpperCase();
        if (regNumber.length() > 20) {
            throw new ValidationException("Registration number cannot exceed 20 characters.");
        }
        if (!regNumber.matches("^[A-Z0-9]+$")) {
            throw new ValidationException("Registration number can only contain letters and digits.");
        }
        return regNumber;
    }

    public static void validateManufactureYear(int year) throws ValidationException {
        int maxYear = java.time.Year.now().getValue() + 1;
        if (year < 1980 || year > maxYear) {
            throw new ValidationException("Manufacture year must be between 1980 and " + maxYear + ".");
        }
    }

    public static String validateMakeModel(String value, String fieldName) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        value = value.trim();
        if (value.length() > 50) {
            throw new ValidationException(fieldName + " cannot exceed 50 characters.");
        }
        return value;
    }

    public static java.time.LocalDate validateDate(String dateStr, String fieldName) throws ValidationException {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            throw new ValidationException(fieldName + " cannot be empty.");
        }
        try {
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("uuuu-MM-dd")
                    .withResolverStyle(java.time.format.ResolverStyle.STRICT);
            return java.time.LocalDate.parse(dateStr.trim(), formatter);
        } catch (java.time.format.DateTimeParseException e) {
            throw new ValidationException("Invalid " + fieldName + " format. Use yyyy-MM-dd and ensure the date is real.");
        }
    }

    public static java.time.LocalDate validateServiceDate(String input) throws ValidationException {
        java.time.LocalDate date = validateDate(input, "Service Date");
        if (date.isAfter(java.time.LocalDate.now())) {
            throw new ValidationException("Service date cannot be in the future.");
        }
        return date;
    }

    public static java.time.LocalDate validateNextServiceDate(String input, java.time.LocalDate serviceDate) throws ValidationException {
        if (input == null || input.trim().isEmpty()) return null;
        java.time.LocalDate nextDate = validateDate(input, "Next Service Date");
        if (nextDate.isBefore(serviceDate)) {
            throw new ValidationException("Next service date cannot be before the current service date.");
        }
        return nextDate;
    }

    public static Integer validateNextServiceKm(String input, int currentOdometer) throws ValidationException {
        if (input == null || input.trim().isEmpty()) return null;
        int nextKm = validateOdometer(input);
        if (nextKm <= currentOdometer) {
            throw new ValidationException("Next service KM must be greater than current odometer reading.");
        }
        return nextKm;
    }

    public static void validateNextServiceFields(java.time.LocalDate nextDate, Integer nextKm) throws ValidationException {
        if (nextDate == null && nextKm == null) {
            throw new ValidationException("Please provide either Next Service Date or Next Service KM.");
        }
    }

    public static int validateOdometer(String odometerStr) throws ValidationException {
        if (odometerStr == null || odometerStr.trim().isEmpty()) {
            throw new ValidationException("Odometer reading cannot be empty.");
        }
        try {
            int odometer = Integer.parseInt(odometerStr.trim());
            if (odometer < 0) {
                throw new ValidationException("Odometer reading cannot be negative.");
            }
            if (odometer > 2000000) {
                throw new ValidationException("Odometer reading cannot exceed 2,000,000.");
            }
            return odometer;
        } catch (NumberFormatException e) {
            throw new ValidationException("Odometer reading must be a whole number.");
        }
    }

    public static java.math.BigDecimal validateCost(String costStr) throws ValidationException {
        if (costStr == null || costStr.trim().isEmpty()) {
            return null;
        }
        try {
            java.math.BigDecimal cost = new java.math.BigDecimal(costStr.trim());
            if (cost.compareTo(java.math.BigDecimal.ZERO) < 0) {
                throw new ValidationException("Cost cannot be negative.");
            }
            if (cost.scale() > 2) {
                throw new ValidationException("Cost cannot have more than 2 decimal places.");
            }
            if (cost.precision() - cost.scale() > 10) {
                throw new ValidationException("Cost value is too large.");
            }
            return cost;
        } catch (NumberFormatException e) {
            throw new ValidationException("Cost must be a valid number.");
        }
    }

    public static String validateServiceString(String val, String fieldName, int maxLength) throws ValidationException {
        if (val == null || val.trim().isEmpty()) {
            return null;
        }
        val = val.trim();
        if (val.length() > maxLength) {
            throw new ValidationException(fieldName + " cannot exceed " + maxLength + " characters.");
        }
        return val;
    }
}
