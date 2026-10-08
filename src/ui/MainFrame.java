package ui;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import java.awt.Component;

public class MainFrame extends JFrame {

    private JTabbedPane tabbedPane;

    public MainFrame() {
        setTitle("Vehicle Service Management System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen

        tabbedPane = new JTabbedPane();
        
        // Add tabs
        CustomerPanel customerPanel = new CustomerPanel();
        customerPanel.setMainFrame(this);
        tabbedPane.addTab("Customers", customerPanel);
        
        VehiclePanel vehiclePanel = new VehiclePanel();
        tabbedPane.addTab("Vehicles", vehiclePanel);
        
        ServicePanel servicePanel = new ServicePanel();
        tabbedPane.addTab("Services", servicePanel);
        
        SearchPanel searchPanel = new SearchPanel();
        tabbedPane.addTab("History Search", searchPanel);
        
        tabbedPane.addChangeListener(e -> {
            // Keep listener if we need it later, but remove loadCustomers() since combo box is gone
        });
        
        add(tabbedPane);
    }

    public void switchToVehiclesAndSearch(String phone) {
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            if ("Vehicles".equals(tabbedPane.getTitleAt(i))) {
                tabbedPane.setSelectedIndex(i);
                Component comp = tabbedPane.getComponentAt(i);
                if (comp instanceof VehiclePanel) {
                    ((VehiclePanel) comp).searchByPhone(phone);
                }
                break;
            }
        }
    }
}
