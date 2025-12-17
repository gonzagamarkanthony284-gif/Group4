package hpms.test;

import hpms.ui.panels.ServicesPanel;
import javax.swing.*;

public class StandaloneServicesTest {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Standalone Services Panel Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(900, 700);
            frame.setLocationRelativeTo(null);
            
            // Create a simple menu bar to simulate the main app
            JMenuBar menuBar = new JMenuBar();
            JMenu menu = new JMenu("Menu");
            JMenuItem servicesItem = new JMenuItem("View Services");
            
            // Add Services panel when menu item is clicked
            servicesItem.addActionListener(e -> {
                ServicesPanel servicesPanel = new ServicesPanel();
                frame.setContentPane(servicesPanel);
                frame.revalidate();
                frame.repaint();
            });
            
            menu.add(servicesItem);
            menuBar.add(menu);
            frame.setJMenuBar(menuBar);
            
            // Show initial message
            JLabel label = new JLabel("Click Menu -> View Services to see the updated panel", SwingConstants.CENTER);
            frame.add(label);
            
            frame.setVisible(true);
        });
    }
}
