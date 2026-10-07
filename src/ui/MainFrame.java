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
        
        add(tabbedPane);
    }
}
