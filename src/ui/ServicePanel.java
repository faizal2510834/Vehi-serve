package ui;

import dao.CustomerDAO;
import dao.ServiceRecordDAO;
import dao.VehicleDAO;
import exception.DatabaseException;
import exception.ValidationException;
import model.Customer;
import model.ServiceRecord;
import model.Vehicle;
import util.Validator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ServicePanel extends BasePanel {

    private JTextField searchRegField;
    private JButton findButton;

    private JLabel ownerLabel;
    private JLabel vehicleInfoLabel;

    private JTable historyTable;
    private DefaultTableModel tableModel;

    private JTextField serviceDateField;
    private JTextField odometerField;
    private JTextField serviceTypeField;
    private JTextArea workDoneArea;
    private JTextArea partsReplacedArea;
    private JComboBox<String> conditionCombo;
    private JTextField costField;
    private JTextField nextServiceDateField;
    private JTextField nextServiceKmField;
    private JTextArea remarksArea;

    private JButton saveButton;
    private JButton deleteButton;
    private JButton clearButton;

    private VehicleDAO vehicleDAO;
    private CustomerDAO customerDAO;
    private ServiceRecordDAO serviceDAO;

    private Vehicle currentVehicle = null;
    private int selectedServiceId = -1;

    public ServicePanel() {
        vehicleDAO = new VehicleDAO();
        customerDAO = new CustomerDAO();
        serviceDAO = new ServiceRecordDAO();

        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = createTopPanel();
        JPanel centerPanel = createCenterPanel();
        JPanel formPanel = createFormPanel();

        add(topPanel, BorderLayout.NORTH);
        
        // Use a split pane or just add to center/south
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, centerPanel, formPanel);
        splitPane.setResizeWeight(0.5);
        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Find Vehicle"));

        panel.add(new JLabel("Registration Number:"));
        searchRegField = new JTextField(15);
        panel.add(searchRegField);

        findButton = new JButton("Find");
        findButton.addActionListener(e -> findVehicle());
        panel.add(findButton);

        panel.add(Box.createHorizontalStrut(20));
        ownerLabel = new JLabel("Owner: --");
        panel.add(ownerLabel);
        
        panel.add(Box.createHorizontalStrut(20));
        vehicleInfoLabel = new JLabel("Vehicle: --");
        panel.add(vehicleInfoLabel);

        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Service History"));

        String[] cols = {"ID", "Date", "Odometer", "Type", "Cost", "Condition", "Next Date", "Next KM"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        historyTable = new JTable(tableModel);
        historyTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && historyTable.getSelectedRow() != -1) {
                selectedServiceId = (int) tableModel.getValueAt(historyTable.getSelectedRow(), 0);
                deleteButton.setEnabled(true);
            }
        });

        panel.add(new JScrollPane(historyTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("New Service Entry"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Service Date (yyyy-MM-dd):"), gbc);
        gbc.gridx = 1; gbc.gridy = row;
        serviceDateField = new JTextField(LocalDate.now().toString(), 15);
        panel.add(serviceDateField, gbc);

        gbc.gridx = 2; gbc.gridy = row;
        panel.add(new JLabel("Odometer (km):"), gbc);
        gbc.gridx = 3; gbc.gridy = row;
        odometerField = new JTextField(15);
        panel.add(odometerField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Service Type:"), gbc);
        gbc.gridx = 1; gbc.gridy = row;
        serviceTypeField = new JTextField(15);
        panel.add(serviceTypeField, gbc);

        gbc.gridx = 2; gbc.gridy = row;
        panel.add(new JLabel("Condition:"), gbc);
        gbc.gridx = 3; gbc.gridy = row;
        conditionCombo = new JComboBox<>(new String[]{"Good", "Average", "Poor", "Critical"});
        panel.add(conditionCombo, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Cost:"), gbc);
        gbc.gridx = 1; gbc.gridy = row;
        costField = new JTextField(15);
        panel.add(costField, gbc);
        
        gbc.gridx = 2; gbc.gridy = row;
        panel.add(new JLabel("Next Service Date:"), gbc);
        gbc.gridx = 3; gbc.gridy = row;
        nextServiceDateField = new JTextField(15);
        panel.add(nextServiceDateField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Next Service KM:"), gbc);
        gbc.gridx = 1; gbc.gridy = row;
        nextServiceKmField = new JTextField(15);
        panel.add(nextServiceKmField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Work Done:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        workDoneArea = new JTextArea(2, 40);
        panel.add(new JScrollPane(workDoneArea), gbc);
        gbc.gridwidth = 1;

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Parts Replaced:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        partsReplacedArea = new JTextArea(2, 40);
        panel.add(new JScrollPane(partsReplacedArea), gbc);
        gbc.gridwidth = 1;
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Remarks:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        remarksArea = new JTextArea(2, 40);
        panel.add(new JScrollPane(remarksArea), gbc);
        gbc.gridwidth = 1;

        row++;
        JPanel btnPanel = new JPanel();
        saveButton = new JButton("Save Service");
        deleteButton = new JButton("Delete Record");
        clearButton = new JButton("Clear");

        saveButton.addActionListener(e -> saveService());
        deleteButton.addActionListener(e -> deleteService());
        clearButton.addActionListener(e -> clearForm());

        saveButton.setEnabled(false);
        deleteButton.setEnabled(false);

        btnPanel.add(saveButton);
        btnPanel.add(deleteButton);
        btnPanel.add(clearButton);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 4;
        panel.add(btnPanel, gbc);

        return panel;
    }

    private void findVehicle() {
        try {
            String reg = Validator.validateRegNumber(searchRegField.getText());
            currentVehicle = vehicleDAO.findByRegNumber(reg);
            
            if (currentVehicle == null) {
                showInfo("Vehicle not found.");
                clearPanel();
                return;
            }
            
            Customer owner = customerDAO.getAllCustomers().stream()
                .filter(c -> c.getCustomerId() == currentVehicle.getCustomerId())
                .findFirst().orElse(null);
                
            ownerLabel.setText("Owner: " + (owner != null ? owner.getName() : "Unknown"));
            vehicleInfoLabel.setText("Vehicle: " + currentVehicle.getMake() + " " + currentVehicle.getModel());
            
            loadHistory();
            saveButton.setEnabled(true);
            
        } catch (ValidationException | DatabaseException ex) {
            showError(ex.getMessage());
            clearPanel();
        }
    }

    private void loadHistory() {
        if (currentVehicle == null) return;
        try {
            tableModel.setRowCount(0);
            List<ServiceRecord> records = serviceDAO.getHistoryByVehicle(currentVehicle.getVehicleId());
            for (ServiceRecord r : records) {
                tableModel.addRow(new Object[]{
                    r.getServiceId(),
                    r.getServiceDate(),
                    r.getOdometerKm(),
                    r.getServiceType(),
                    r.getCost(),
                    r.getVehicleCondition(),
                    r.getNextServiceDate(),
                    r.getNextServiceKm()
                });
            }
        } catch (DatabaseException ex) {
            showError("Failed to load history: " + ex.getMessage());
        }
    }

    private void clearPanel() {
        currentVehicle = null;
        ownerLabel.setText("Owner: --");
        vehicleInfoLabel.setText("Vehicle: --");
        tableModel.setRowCount(0);
        clearForm();
        saveButton.setEnabled(false);
    }

    private void clearForm() {
        serviceDateField.setText(LocalDate.now().toString());
        odometerField.setText("");
        serviceTypeField.setText("");
        workDoneArea.setText("");
        partsReplacedArea.setText("");
        conditionCombo.setSelectedIndex(0);
        costField.setText("");
        nextServiceDateField.setText("");
        nextServiceKmField.setText("");
        remarksArea.setText("");
        
        historyTable.clearSelection();
        selectedServiceId = -1;
        deleteButton.setEnabled(false);
    }

    private void saveService() {
        if (currentVehicle == null) return;
        
        try {
            LocalDate serviceDate = Validator.validateDate(serviceDateField.getText(), "Service Date");
            if (serviceDate.isAfter(LocalDate.now())) {
                throw new ValidationException("Service date cannot be in the future.");
            }

            int odometer = Validator.validateOdometer(odometerField.getText());
            int latestOdometer = serviceDAO.getLatestOdometer(currentVehicle.getVehicleId());
            if (odometer < latestOdometer) {
                if (!confirm("Warning: Entered odometer (" + odometer + ") is lower than the previous record (" + latestOdometer + ").\nDo you want to proceed?")) {
                    return;
                }
            }

            String sType = Validator.validateServiceString(serviceTypeField.getText(), "Service Type", 50);
            String cond = (String) conditionCombo.getSelectedItem();
            BigDecimal cost = Validator.validateCost(costField.getText());
            
            LocalDate nextDate = null;
            if (!nextServiceDateField.getText().trim().isEmpty()) {
                nextDate = Validator.validateDate(nextServiceDateField.getText(), "Next Service Date");
                if (nextDate.isBefore(serviceDate)) {
                    throw new ValidationException("Next service date cannot be before the current service date.");
                }
            }
            
            Integer nextKm = null;
            if (!nextServiceKmField.getText().trim().isEmpty()) {
                nextKm = Validator.validateOdometer(nextServiceKmField.getText());
                if (nextKm <= odometer) {
                    throw new ValidationException("Next service KM must be greater than current odometer reading.");
                }
            }

            // Require at least one next service prediction
            if (nextDate == null && nextKm == null) {
                throw new ValidationException("Please provide either Next Service Date or Next Service KM.");
            }

            String work = Validator.validateServiceString(workDoneArea.getText(), "Work Done", 500);
            String parts = Validator.validateServiceString(partsReplacedArea.getText(), "Parts Replaced", 500);
            String rem = Validator.validateServiceString(remarksArea.getText(), "Remarks", 500);

            ServiceRecord record = new ServiceRecord(
                0, currentVehicle.getVehicleId(), serviceDate, odometer, sType,
                work, parts, cond, cost, nextDate, nextKm, rem
            );

            int newId = serviceDAO.addServiceRecord(record);
            showInfo("Service record saved successfully! ID: " + newId);
            clearForm();
            loadHistory();

        } catch (ValidationException | DatabaseException ex) {
            showError(ex.getMessage());
        }
    }

    private void deleteService() {
        if (selectedServiceId == -1) {
            showError("Please select a service record to delete.");
            return;
        }
        if (confirm("Are you sure you want to delete this service record? This action cannot be undone.")) {
            try {
                serviceDAO.deleteServiceRecord(selectedServiceId);
                showInfo("Service record deleted successfully!");
                clearForm();
                loadHistory();
            } catch (DatabaseException ex) {
                showError(ex.getMessage());
            }
        }
    }
}
