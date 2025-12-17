package hpms.ui.panels;

import hpms.model.PaymentMethod;
import hpms.ui.components.SectionHeader;
import hpms.ui.components.Theme;
import hpms.util.DataStore;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class SettingsPanel extends JPanel {
    public SettingsPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        add(SectionHeader.info("Settings", "Configure system options and preferences"), BorderLayout.NORTH);

        // Main settings panel
        JPanel settingsPanel = new JPanel(new BorderLayout());
        settingsPanel.setBackground(Theme.BG);
        settingsPanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Settings form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(245, 247, 250));
        formPanel.setBorder(new LineBorder(new Color(200, 200, 200), 1));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(12, 12, 12, 12);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        // Payment Methods Section
        JLabel paymentLabel = new JLabel("Payment Methods");
        paymentLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2; c.weightx = 1;
        formPanel.add(paymentLabel, c);

        c.gridy = 1; c.gridwidth = 1; c.weightx = 0.1;
        JCheckBox cashCB = new JCheckBox("Cash", DataStore.allowedPaymentMethods.contains(PaymentMethod.CASH));
        formPanel.add(cashCB, c);

        c.gridx = 1; c.weightx = 0.9;
        formPanel.add(new JLabel("Accept payment in cash"), c);

        c.gridx = 0; c.gridy = 2; c.weightx = 0.1;
        JCheckBox cardCB = new JCheckBox("Credit Card", DataStore.allowedPaymentMethods.contains(PaymentMethod.CARD));
        formPanel.add(cardCB, c);
        
        c.gridx = 1; c.weightx = 0.9;
        formPanel.add(new JLabel("Accept credit/debit card payments"), c);
        
        c.gridx = 0; c.gridy = 3; c.weightx = 0.1;
        JCheckBox insuranceCB = new JCheckBox("Insurance", DataStore.allowedPaymentMethods.contains(PaymentMethod.INSURANCE));
        formPanel.add(insuranceCB, c);

        c.gridx = 1; c.weightx = 0.9;
        formPanel.add(new JLabel("Accept insurance payments"), c);

        // Separator
        c.gridx = 0; c.gridy = 6; c.gridwidth = 2; c.insets = new Insets(20, 12, 12, 12);
        JSeparator separator = new JSeparator();
        formPanel.add(separator, c);

        // Appointment Settings
        c.insets = new Insets(12, 12, 12, 12);
        JLabel appointmentLabel = new JLabel("Appointment Settings");
        appointmentLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        c.gridx = 0; c.gridy = 7; c.gridwidth = 2; c.weightx = 1;
        formPanel.add(appointmentLabel, c);

        c.gridy = 8; c.gridwidth = 1; c.weightx = 0.3;
        formPanel.add(new JLabel("Default Appointment Duration (minutes):"), c);

        c.gridx = 1; c.weightx = 0.7; c.fill = GridBagConstraints.HORIZONTAL;
        JSpinner durationSpinner = new JSpinner(new SpinnerNumberModel(30, 15, 120, 5));
        formPanel.add(durationSpinner, c);

        // Enable notifications
        c.gridx = 0; c.gridy = 9; c.gridwidth = 2; c.weightx = 0.1;
        JCheckBox notificationsCB = new JCheckBox("Enable Appointment Reminders", true);
        formPanel.add(notificationsCB, c);

        settingsPanel.add(formPanel, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        buttonsPanel.setBackground(Theme.BG);

        JButton saveBtn = new JButton("Save Settings");
        styleButton(saveBtn, new Color(0, 110, 102));

        JButton resetBtn = new JButton("Reset to Defaults");
        styleButton(resetBtn, new Color(192, 57, 43));

        saveBtn.addActionListener(e -> {
            // Update payment methods
            DataStore.allowedPaymentMethods.clear();
            if (cashCB.isSelected()) DataStore.allowedPaymentMethods.add(PaymentMethod.CASH);
            if (cardCB.isSelected()) DataStore.allowedPaymentMethods.add(PaymentMethod.CARD);
            if (insuranceCB.isSelected()) DataStore.allowedPaymentMethods.add(PaymentMethod.INSURANCE);

            JOptionPane.showMessageDialog(this,
                "Settings saved successfully!\n\n" +
                "Payment Methods: " + DataStore.allowedPaymentMethods.size() + " enabled\n" +
                "Appointment Duration: " + durationSpinner.getValue() + " minutes\n" +
                "Reminders: " + (notificationsCB.isSelected() ? "Enabled" : "Disabled"),
                "Success", JOptionPane.INFORMATION_MESSAGE);
        });

        resetBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Reset all settings to defaults?", "Confirm Reset", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                cashCB.setSelected(true);
                cardCB.setSelected(true);
                insuranceCB.setSelected(true);
                durationSpinner.setValue(30);
                notificationsCB.setSelected(true);
                JOptionPane.showMessageDialog(this, "Settings reset to defaults", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        buttonsPanel.add(resetBtn);
        buttonsPanel.add(saveBtn);

        settingsPanel.add(buttonsPanel, BorderLayout.SOUTH);
        add(settingsPanel, BorderLayout.CENTER);
    }

    private void styleButton(JButton btn, Color bgColor) {
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(6, 12, 6, 12));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}

