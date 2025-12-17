package hpms.test;

import hpms.ui.panels.ServicesPanel;
import javax.swing.*;

public class ServicesPanelTest {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Enhanced Services Panel Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(900, 700);
            frame.setLocationRelativeTo(null);
            
            ServicesPanel panel = new ServicesPanel();
            frame.add(panel);
            
            frame.setVisible(true);
        });
    }
}
