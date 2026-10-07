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
        regNumber = regNumber.trim().toUpperCase();
        if (regNumber.length() > 20) {
            throw new ValidationException("Registration number cannot exceed 20 characters.");
        }
        if (!regNumber.matches("^[A-Z0-9]+$")) {
            throw new ValidationException("Registration number can only contain letters and digits.");
        }
        return regNumber;
    }

    public static void validateManufactureYear(int year) throws ValidationException {
        if (year < 1980 || year > 2100) {
            throw new ValidationException("Manufacture year must be between 1980 and 2100.");
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
}
