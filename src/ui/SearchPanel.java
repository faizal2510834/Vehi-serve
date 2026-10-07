package ui;

import dao.ServiceRecordDAO;
import exception.DatabaseException;
import model.ServiceHistoryRow;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

public class SearchPanel extends BasePanel {

    private JTextField searchField;
    private JButton searchButton;
    private JLabel statusLabel;
    
    private JTable resultTable;
    private DefaultTableModel tableModel;
    
    private JTextArea detailsArea;
    private ServiceRecordDAO serviceDAO;
    
    private List<ServiceHistoryRow> currentResults;

    public SearchPanel() {
        serviceDAO = new ServiceRecordDAO();
        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = createTopPanel();
        JPanel centerPanel = createCenterPanel();
        JPanel bottomPanel = createBottomPanel();

        add(topPanel, BorderLayout.NORTH);
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, centerPanel, bottomPanel);
        splitPane.setResizeWeight(0.7);
        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Search Full History"));

        panel.add(new JLabel("Keyword (Name, Phone, Reg, Type):"));
        searchField = new JTextField(20);
        
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performSearch();
                }
            }
        });
        
        panel.add(searchField);

        searchButton = new JButton("Search");
        searchButton.addActionListener(e -> performSearch());
        panel.add(searchButton);
        
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.BLUE);
        panel.add(statusLabel);

        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        String[] cols = {"Date", "Reg Number", "Vehicle", "Customer", "Phone", "Type", "Odometer", "Cost", "Next Date", "Next KM"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        resultTable = new JTable(tableModel);
        resultTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showDetails();
            }
        });

        panel.add(new JScrollPane(resultTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Service Details (Read-Only)"));
        
        detailsArea = new JTextArea(6, 50);
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        panel.add(new JScrollPane(detailsArea), BorderLayout.CENTER);
        return panel;
    }

    private void performSearch() {
        String keyword = searchField.getText().trim();
        try {
            currentResults = serviceDAO.searchFullHistory(keyword);
            
            tableModel.setRowCount(0);
            for (ServiceHistoryRow row : currentResults) {
                tableModel.addRow(new Object[]{
                    row.getServiceDate(),
                    row.getRegNumber(),
                    row.getMakeModel(),
                    row.getCustomerName(),
                    row.getCustomerPhone(),
                    row.getServiceType(),
                    row.getOdometer(),
                    row.getCost(),
                    row.getNextDate(),
                    row.getNextKm()
                });
            }
            
            if (currentResults.size() == 500) {
                statusLabel.setText("Showing first 500 rows (cap reached).");
            } else {
                statusLabel.setText("Found " + currentResults.size() + " records.");
            }
            
            detailsArea.setText("");
            
        } catch (DatabaseException ex) {
            showError("Search failed: " + ex.getMessage());
        }
    }

    private void showDetails() {
        int selectedIndex = resultTable.getSelectedRow();
        if (selectedIndex >= 0 && selectedIndex < currentResults.size()) {
            ServiceHistoryRow row = currentResults.get(selectedIndex);
            StringBuilder sb = new StringBuilder();
            sb.append("WORK DONE:\n").append(row.getWorkDone() != null ? row.getWorkDone() : "N/A").append("\n\n");
            sb.append("PARTS REPLACED:\n").append(row.getPartsReplaced() != null ? row.getPartsReplaced() : "N/A").append("\n\n");
            sb.append("REMARKS:\n").append(row.getRemarks() != null ? row.getRemarks() : "N/A").append("\n");
            detailsArea.setText(sb.toString());
            detailsArea.setCaretPosition(0);
        } else {
            detailsArea.setText("");
        }
    }
}
