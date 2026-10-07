package ui;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

public class MainFrame extends JFrame {

    private JTabbedPane tabbedPane;

    public MainFrame() {
        setTitle("Vehicle Service Management System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen

        tabbedPane = new JTabbedPane();
        
        // Add tabs
        tabbedPane.addTab("Customers", new CustomerPanel());
        
        VehiclePanel vehiclePanel = new VehiclePanel();
        tabbedPane.addTab("Vehicles", vehiclePanel);
        
        ServicePanel servicePanel = new ServicePanel();
        tabbedPane.addTab("Services", servicePanel);
        
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (selectedIndex != -1) {
                String tabName = tabbedPane.getTitleAt(selectedIndex);
                if ("Vehicles".equals(tabName)) {
                    vehiclePanel.loadCustomers();
                }
            }
        });
        
        add(tabbedPane);
    }
}
