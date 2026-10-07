package ui;

import dao.CustomerDAO;
import dao.VehicleDAO;
import exception.DatabaseException;
import exception.ValidationException;
import model.Customer;
import model.Vehicle;
import util.Validator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class VehiclePanel extends BasePanel {

    private JComboBox<CustomerItem> customerCombo;
    private JTextField regNumberField;
    private JComboBox<String> vehicleTypeCombo;
    private JTextField makeField;
    private JTextField modelField;
    private JTextField yearField;
    private JComboBox<String> fuelTypeCombo;
    
    private JTable vehicleTable;
    private DefaultTableModel tableModel;

    private JButton saveButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton clearButton;

    private VehicleDAO vehicleDAO;
    private CustomerDAO customerDAO;
    private int selectedVehicleId = -1;

    public VehiclePanel() {
        vehicleDAO = new VehicleDAO();
        customerDAO = new CustomerDAO();
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = createFormPanel();
        JPanel tablePanel = createTablePanel();

        add(formPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);

        loadVehicles();
        loadCustomers();
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Vehicle Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 1
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Owner:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        customerCombo = new JComboBox<>();
        formPanel.add(customerCombo, gbc);

        gbc.gridx = 2; gbc.gridy = 0;
        formPanel.add(new JLabel("Reg Number:"), gbc);
        gbc.gridx = 3; gbc.gridy = 0;
        regNumberField = new JTextField(15);
        formPanel.add(regNumberField, gbc);

        // Row 2
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Vehicle Type:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        vehicleTypeCombo = new JComboBox<>(new String[]{"Two-Wheeler", "Four-Wheeler"});
        formPanel.add(vehicleTypeCombo, gbc);

        gbc.gridx = 2; gbc.gridy = 1;
        formPanel.add(new JLabel("Fuel Type:"), gbc);
        gbc.gridx = 3; gbc.gridy = 1;
        fuelTypeCombo = new JComboBox<>(new String[]{"Petrol", "Diesel", "EV", "CNG", "Hybrid"});
        formPanel.add(fuelTypeCombo, gbc);

        // Row 3
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Make:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        makeField = new JTextField(15);
        formPanel.add(makeField, gbc);

        gbc.gridx = 2; gbc.gridy = 2;
        formPanel.add(new JLabel("Model:"), gbc);
        gbc.gridx = 3; gbc.gridy = 2;
        modelField = new JTextField(15);
        formPanel.add(modelField, gbc);

        // Row 4
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Year:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3;
        yearField = new JTextField(15);
        formPanel.add(yearField, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        saveButton = new JButton("Save");
        updateButton = new JButton("Update");
        deleteButton = new JButton("Delete");
        clearButton = new JButton("Clear");

        saveButton.addActionListener(e -> saveVehicle());
        updateButton.addActionListener(e -> updateVehicle());
        deleteButton.addActionListener(e -> deleteVehicle());
        clearButton.addActionListener(e -> clearForm());

        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);

        buttonPanel.add(saveButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 4;
        formPanel.add(buttonPanel, gbc);

        return formPanel;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Registered Vehicles"));

        String[] columns = {"ID", "Owner ID", "Reg Number", "Type", "Make", "Model", "Year", "Fuel"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        vehicleTable = new JTable(tableModel);
        vehicleTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && vehicleTable.getSelectedRow() != -1) {
                populateFormFromSelection();
            }
        });

        tablePanel.add(new JScrollPane(vehicleTable), BorderLayout.CENTER);
        return tablePanel;
    }

    public void loadCustomers() {
        SwingUtilities.invokeLater(() -> {
            try {
                customerCombo.removeAllItems();
                customerCombo.addItem(new CustomerItem(-1, "Select Customer", ""));
                List<Customer> customers = customerDAO.getAllCustomers();
                for (Customer c : customers) {
                    customerCombo.addItem(new CustomerItem(c.getCustomerId(), c.getName(), c.getPhone()));
                }
            } catch (DatabaseException ex) {
                showError("Failed to load customers: " + ex.getMessage());
            }
        });
    }

    private void loadVehicles() {
        SwingUtilities.invokeLater(() -> {
            try {
                tableModel.setRowCount(0);
                List<Vehicle> vehicles = vehicleDAO.getAllVehicles();
                for (Vehicle v : vehicles) {
                    tableModel.addRow(new Object[]{
                            v.getVehicleId(),
                            v.getCustomerId(),
                            v.getRegNumber(),
                            v.getVehicleType(),
                            v.getMake(),
                            v.getModel(),
                            v.getManufactureYear(),
                            v.getFuelType()
                    });
                }
            } catch (DatabaseException ex) {
                showError("Failed to load vehicles: " + ex.getMessage());
            }
        });
    }

    private void populateFormFromSelection() {
        int row = vehicleTable.getSelectedRow();
        if (row != -1) {
            selectedVehicleId = (int) tableModel.getValueAt(row, 0);
            int customerId = (int) tableModel.getValueAt(row, 1);
            
            // Set customer combo
            for (int i = 0; i < customerCombo.getItemCount(); i++) {
                if (customerCombo.getItemAt(i).customerId == customerId) {
                    customerCombo.setSelectedIndex(i);
                    break;
                }
            }

            regNumberField.setText((String) tableModel.getValueAt(row, 2));
            vehicleTypeCombo.setSelectedItem(tableModel.getValueAt(row, 3));
            makeField.setText(tableModel.getValueAt(row, 4) == null ? "" : (String) tableModel.getValueAt(row, 4));
            modelField.setText(tableModel.getValueAt(row, 5) == null ? "" : (String) tableModel.getValueAt(row, 5));
            yearField.setText(String.valueOf(tableModel.getValueAt(row, 6)));
            fuelTypeCombo.setSelectedItem(tableModel.getValueAt(row, 7));

            saveButton.setEnabled(false);
            updateButton.setEnabled(true);
            deleteButton.setEnabled(true);
        }
    }

    private void clearForm() {
        selectedVehicleId = -1;
        customerCombo.setSelectedIndex(0);
        regNumberField.setText("");
        vehicleTypeCombo.setSelectedIndex(0);
        makeField.setText("");
        modelField.setText("");
        yearField.setText("");
        fuelTypeCombo.setSelectedIndex(0);

        vehicleTable.clearSelection();

        saveButton.setEnabled(true);
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }

    private Vehicle getVehicleFromForm() throws ValidationException {
        CustomerItem selectedCustomer = (CustomerItem) customerCombo.getSelectedItem();
        int customerId = selectedCustomer != null ? selectedCustomer.customerId : -1;
        Validator.validateCustomerId(customerId);

        String regNumber = Validator.validateRegNumber(regNumberField.getText());
        
        int year = 0;
        String yearText = yearField.getText().trim();
        if (!yearText.isEmpty()) {
            try {
                year = Integer.parseInt(yearText);
            } catch (NumberFormatException e) {
                throw new ValidationException("Manufacture year must be a valid number.");
            }
        }
        Validator.validateManufactureYear(year);

        String make = Validator.validateMakeModel(makeField.getText(), "Make");
        String model = Validator.validateMakeModel(modelField.getText(), "Model");
        
        String vType = (String) vehicleTypeCombo.getSelectedItem();
        String fType = (String) fuelTypeCombo.getSelectedItem();

        return new Vehicle(selectedVehicleId, customerId, regNumber, vType, make, model, year, fType);
    }

    private void saveVehicle() {
        try {
            Vehicle vehicle = getVehicleFromForm();
            int newId = vehicleDAO.addVehicle(vehicle);
            showInfo("Vehicle saved successfully! ID: " + newId);
            clearForm();
            loadVehicles();
        } catch (ValidationException | DatabaseException ex) {
            showError(ex.getMessage());
        }
    }

    private void updateVehicle() {
        if (selectedVehicleId == -1) {
            showError("Please select a vehicle to update.");
            return;
        }
        try {
            Vehicle vehicle = getVehicleFromForm();
            vehicleDAO.updateVehicle(vehicle);
            showInfo("Vehicle updated successfully!");
            clearForm();
            loadVehicles();
        } catch (ValidationException | DatabaseException ex) {
            showError(ex.getMessage());
        }
    }

    private void deleteVehicle() {
        if (selectedVehicleId == -1) {
            showError("Please select a vehicle to delete.");
            return;
        }
        if (confirm("Are you sure you want to delete this vehicle?")) {
            try {
                vehicleDAO.deleteVehicle(selectedVehicleId);
                showInfo("Vehicle deleted successfully!");
                clearForm();
                loadVehicles();
            } catch (DatabaseException ex) {
                showError(ex.getMessage());
            }
        }
    }

    // Wrapper class for JComboBox items
    private static class CustomerItem {
        int customerId;
        String name;
        String phone;

        public CustomerItem(int customerId, String name, String phone) {
            this.customerId = customerId;
            this.name = name;
            this.phone = phone;
        }

        @Override
        public String toString() {
            if (customerId == -1) return name; // "Select Customer"
            return name + " - " + phone;
        }
    }
}
