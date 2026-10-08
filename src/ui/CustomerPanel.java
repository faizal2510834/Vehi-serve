package ui;

import dao.CustomerDAO;
import exception.DatabaseException;
import exception.ValidationException;
import model.Customer;
import util.Validator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CustomerPanel extends BasePanel {

    private JTextField txtId;
    private JTextField txtName;
    private JTextField txtPhone;
    private JTextField txtEmail;
    private JTextField txtAddress;
    
    private JButton btnSave;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnClear;
    
    private JTable customerTable;
    private DefaultTableModel tableModel;
    
    private CustomerDAO customerDAO;
    private MainFrame mainFrame;

    public void setMainFrame(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    public CustomerPanel() {
        customerDAO = new CustomerDAO();
        setLayout(new BorderLayout());
        
        initForm();
        initTable();
        loadTableData();
    }

    private void initForm() {
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        formPanel.add(new JLabel("Customer ID (Auto):"));
        txtId = new JTextField();
        txtId.setEditable(false);
        formPanel.add(txtId);

        formPanel.add(new JLabel("Name:"));
        txtName = new JTextField();
        formPanel.add(txtName);

        formPanel.add(new JLabel("Phone (10 digits):"));
        txtPhone = new JTextField();
        formPanel.add(txtPhone);

        formPanel.add(new JLabel("Email:"));
        txtEmail = new JTextField();
        formPanel.add(txtEmail);

        formPanel.add(new JLabel("Address:"));
        txtAddress = new JTextField();
        formPanel.add(txtAddress);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        btnSave = new JButton("Save");
        btnUpdate = new JButton("Update");
        btnDelete = new JButton("Delete");
        btnClear = new JButton("Clear");

        buttonPanel.add(btnSave);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);
        
        formPanel.add(new JLabel("")); // Empty cell
        formPanel.add(buttonPanel);

        add(formPanel, BorderLayout.NORTH);

        // Actions
        btnSave.addActionListener(e -> saveCustomer());
        btnUpdate.addActionListener(e -> updateCustomer());
        btnDelete.addActionListener(e -> deleteCustomer());
        btnClear.addActionListener(e -> clearForm());
    }

    private void initTable() {
        String[] columns = {"ID", "Name", "Phone", "Email", "Address"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        customerTable = new JTable(tableModel);
        customerTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && customerTable.getSelectedRow() != -1) {
                int row = customerTable.getSelectedRow();
                txtId.setText(tableModel.getValueAt(row, 0).toString());
                txtName.setText(tableModel.getValueAt(row, 1).toString());
                txtPhone.setText(tableModel.getValueAt(row, 2).toString());
                Object email = tableModel.getValueAt(row, 3);
                txtEmail.setText(email != null ? email.toString() : "");
                Object address = tableModel.getValueAt(row, 4);
                txtAddress.setText(address != null ? address.toString() : "");
            }
        });

        JScrollPane scrollPane = new JScrollPane(customerTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadTableData() {
        try {
            tableModel.setRowCount(0);
            List<Customer> customers = customerDAO.getAllCustomers();
            for (Customer c : customers) {
                tableModel.addRow(new Object[]{
                    c.getCustomerId(), c.getName(), c.getPhone(), c.getEmail(), c.getAddress()
                });
            }
        } catch (DatabaseException e) {
            showError("Failed to load customers: " + e.getMessage());
        }
    }

    private void saveCustomer() {
        try {
            if (!txtId.getText().isEmpty()) {
                showError("Please clear the form to save a new customer.");
                return;
            }
            
            Customer c = new Customer();
            c.setName(Validator.validateName(txtName.getText()));
            String phone = Validator.validatePhone(txtPhone.getText());
            c.setPhone(phone);
            c.setEmail(Validator.validateEmail(txtEmail.getText()));
            c.setAddress(Validator.validateAddress(txtAddress.getText()));
            
            int newId = customerDAO.addCustomer(c);
            showInfo("Customer saved successfully! Generated ID: " + newId);
            clearForm();
            loadTableData();

            if (mainFrame != null) {
                SwingUtilities.invokeLater(() -> {
                    try {
                        mainFrame.switchToVehiclesAndSearch(phone);
                    } catch (Exception ex) {
                        // Ignore and stay on Customers tab if hand-off fails
                    }
                });
            }
        } catch (ValidationException | DatabaseException e) {
            showError(e.getMessage());
        }
    }

    private void updateCustomer() {
        try {
            if (txtId.getText().isEmpty()) {
                showError("Please select a customer from the table to update.");
                return;
            }
            
            Customer c = new Customer();
            c.setCustomerId(Integer.parseInt(txtId.getText()));
            c.setName(Validator.validateName(txtName.getText()));
            c.setPhone(Validator.validatePhone(txtPhone.getText()));
            c.setEmail(Validator.validateEmail(txtEmail.getText()));
            c.setAddress(Validator.validateAddress(txtAddress.getText()));
            
            customerDAO.updateCustomer(c);
            showInfo("Customer updated successfully!");
            clearForm();
            loadTableData();
        } catch (ValidationException | DatabaseException e) {
            showError(e.getMessage());
        }
    }

    private void deleteCustomer() {
        try {
            if (txtId.getText().isEmpty()) {
                showError("Please select a customer from the table to delete.");
                return;
            }
            
            if (confirm("Are you sure you want to delete this customer?")) {
                int id = Integer.parseInt(txtId.getText());
                customerDAO.deleteCustomer(id);
                showInfo("Customer deleted successfully!");
                clearForm();
                loadTableData();
            }
        } catch (DatabaseException e) {
            showError(e.getMessage());
        }
    }

    private void clearForm() {
        txtId.setText("");
        txtName.setText("");
        txtPhone.setText("");
        txtEmail.setText("");
        txtAddress.setText("");
        customerTable.clearSelection();
    }
}
