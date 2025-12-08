// NEW ADD PATIENT DIALOG - Matches reference image structure exactly
// This is the replacement for the addPatientDialog() method in PatientsPanel.java

public void addPatientDialog() {
    JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Patient", true);
    dialog.setLayout(new BorderLayout(10, 10));
    dialog.setSize(1000, 850);
    dialog.setLocationRelativeTo(this);

    // Main panel with form sections
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
    mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
    mainPanel.setBackground(Color.WHITE);

    // ==================== PATIENT INFORMATION SECTION ====================
    JPanel patientInfoSection = new JPanel(new GridBagLayout());
    patientInfoSection.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.BLACK, 2),
            "Patient Information",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)));
    patientInfoSection.setBackground(Color.WHITE);

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;

    int row = 0;

    // Name and DOB row
    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.weightx = 0.3;
    patientInfoSection.add(new JLabel("Name"), gbc);
    JTextField nameField = new JTextField(25);
    gbc.gridx = 1;
    gbc.weightx = 0.7;
    patientInfoSection.add(nameField, gbc);

    gbc.gridx = 2;
    gbc.weightx = 0.3;
    patientInfoSection.add(new JLabel("DOB (MM/DD/YYYY):"), gbc);
    JTextField dobField = new JTextField(15);
    gbc.gridx = 3;
    gbc.weightx = 0.7;
    patientInfoSection.add(dobField, gbc);
    row++;

    // Gender checkboxes
    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.weightx = 0.3;
    patientInfoSection.add(new JLabel("Gender:"), gbc);

    JPanel genderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
    genderPanel.setBackground(Color.WHITE);
    JCheckBox genderMale = new JCheckBox("Male");
    JCheckBox genderFemale = new JCheckBox("Female");
    JCheckBox genderOther = new JCheckBox("Other");
    JTextField genderOtherText = new JTextField(10);
    genderOtherText.setEnabled(false);
    genderPanel.add(genderMale);
    genderPanel.add(genderFemale);
    genderPanel.add(genderOther);
    genderPanel.add(genderOtherText);

    gbc.gridx = 1;
    gbc.gridy = row;
    gbc.gridwidth = 3;
    gbc.weightx = 1.0;
    patientInfoSection.add(genderPanel, gbc);
    gbc.gridwidth = 1;

    // Gender checkbox mutual exclusion
    genderMale.addActionListener(e -> {
        if (genderMale.isSelected()) {
            genderFemale.setSelected(false);
            genderOther.setSelected(false);
            genderOtherText.setEnabled(false);
        }
    });
    genderFemale.addActionListener(e -> {
        if (genderFemale.isSelected()) {
            genderMale.setSelected(false);
            genderOther.setSelected(false);
            genderOtherText.setEnabled(false);
        }
    });
    genderOther.addActionListener(e -> {
        if (genderOther.isSelected()) {
            genderMale.setSelected(false);
            genderFemale.setSelected(false);
            genderOtherText.setEnabled(true);
        } else {
            genderOtherText.setEnabled(false);
        }
    });
    row++;

    // Preferred Pronouns
    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.weightx = 0.3;
    patientInfoSection.add(new JLabel("Preferred Pronouns:"), gbc);

    JPanel pronounPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
    pronounPanel.setBackground(Color.WHITE);
    JCheckBox pronounHeHim = new JCheckBox("He/Him");
    JCheckBox pronounSheHer = new JCheckBox("She/Her");
    JCheckBox pronounTheyThem = new JCheckBox("They/Them");
    JCheckBox pronounOther = new JCheckBox("Other");
    JTextField pronounOtherText = new JTextField(10);
    pronounOtherText.setEnabled(false);
    pronounPanel.add(pronounHeHim);
    pronounPanel.add(pronounSheHer);
    pronounPanel.add(pronounTheyThem);
    pronounPanel.add(pronounOther);
    pronounPanel.add(pronounOtherText);

    gbc.gridx = 1;
    gbc.gridy = row;
    gbc.gridwidth = 3;
    gbc.weightx = 1.0;
    patientInfoSection.add(pronounPanel, gbc);
    gbc.gridwidth = 1;

    pronounOther.addActionListener(e -> pronounOtherText.setEnabled(pronounOther.isSelected()));
    row++;

    // Address and City
    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.weightx = 0.3;
    patientInfoSection.add(new JLabel("Address:"), gbc);
    JTextField addressField = new JTextField(30);
    gbc.gridx = 1;
    gbc.weightx = 0.7;
    patientInfoSection.add(addressField, gbc);

    gbc.gridx = 2;
    gbc.weightx = 0.3;
    patientInfoSection.add(new JLabel("City"), gbc);
    JTextField cityField = new JTextField(15);
    gbc.gridx = 3;
    gbc.weightx = 0.7;
    patientInfoSection.add(cityField, gbc);
    row++;

    // State, Zip, Phone
    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.weightx = 0.15;
    patientInfoSection.add(new JLabel("State:"), gbc);
    JTextField stateField = new JTextField(5);
    gbc.gridx = 1;
    gbc.weightx = 0.35;
    patientInfoSection.add(stateField, gbc);

    gbc.gridx = 2;
    gbc.weightx = 0.15;
    patientInfoSection.add(new JLabel("Zip:"), gbc);
    JTextField zipField = new JTextField(10);
    gbc.gridx = 3;
    gbc.weightx = 0.35;
    patientInfoSection.add(zipField, gbc);
    row++;

    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.weightx = 0.15;
    patientInfoSection.add(new JLabel("Phone:"), gbc);
    JTextField phoneField = new JTextField(15);
    gbc.gridx = 1;
    gbc.weightx = 0.35;
    patientInfoSection.add(phoneField, gbc);
    row++;

    // Email and Preferred Contact Method
    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.weightx = 0.3;
    patientInfoSection.add(new JLabel("Email:"), gbc);
    JTextField emailField = new JTextField(30);
    gbc.gridx = 1;
    gbc.weightx = 0.7;
    patientInfoSection.add(emailField, gbc);

    gbc.gridx = 2;
    gbc.weightx = 0.3;
    patientInfoSection.add(new JLabel("Preferred Contact Method:"), gbc);

    JPanel contactMethodPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
    contactMethodPanel.setBackground(Color.WHITE);
    JCheckBox contactPhone = new JCheckBox("Phone");
    JCheckBox contactEmail = new JCheckBox("Email");
    JCheckBox contactText = new JCheckBox("Text");
    contactMethodPanel.add(contactPhone);
    contactMethodPanel.add(contactEmail);
    contactMethodPanel.add(contactText);

    gbc.gridx = 3;
    gbc.weightx = 0.7;
    patientInfoSection.add(contactMethodPanel, gbc);
    row++;

    // Primary language and Interpreter needed
    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.weightx = 0.3;
    patientInfoSection.add(new JLabel("Primary language:"), gbc);
    JTextField primaryLanguageField = new JTextField(20);
    gbc.gridx = 1;
    gbc.weightx = 0.7;
    patientInfoSection.add(primaryLanguageField, gbc);

    gbc.gridx = 2;
    gbc.weightx = 0.3;
    patientInfoSection.add(new JLabel("Interpreter needed:"), gbc);

    JPanel interpreterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
    interpreterPanel.setBackground(Color.WHITE);
    JRadioButton interpreterYes = new JRadioButton("Yes");
    JRadioButton interpreterNo = new JRadioButton("No");
    ButtonGroup interpreterGroup = new ButtonGroup();
    interpreterGroup.add(interpreterYes);
    interpreterGroup.add(interpreterNo);
    interpreterNo.setSelected(true);
    interpreterPanel.add(interpreterYes);
    interpreterPanel.add(interpreterNo);

    gbc.gridx = 3;
    gbc.weightx = 0.7;
    patientInfoSection.add(interpreterPanel, gbc);
    row++;

    // Emergency Contact Name and Phone
    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.weightx = 0.3;
    patientInfoSection.add(new JLabel("Emergency Contact Name:"), gbc);
    JTextField emergencyContactName = new JTextField(25);
    gbc.gridx = 1;
    gbc.weightx = 0.7;
    patientInfoSection.add(emergencyContactName, gbc);

    gbc.gridx = 2;
    gbc.weightx = 0.3;
    patientInfoSection.add(new JLabel("Phone:"), gbc);
    JTextField emergencyContactPhone = new JTextField(15);
    gbc.gridx = 3;
    gbc.weightx = 0.7;
    patientInfoSection.add(emergencyContactPhone, gbc);
    row++;

    // Relationship to Patient
    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.weightx = 0.3;
    patientInfoSection.add(new JLabel("Relationship to Patient"), gbc);
    JTextField emergencyRelationship = new JTextField(20);
    gbc.gridx = 1;
    gbc.gridwidth = 3;
    gbc.weightx = 1.0;
    patientInfoSection.add(emergencyRelationship, gbc);
    gbc.gridwidth = 1;

    mainPanel.add(patientInfoSection);
    mainPanel.add(Box.createVerticalStrut(15));

    // ==================== INSURANCE INFORMATION SECTION ====================
    JPanel insuranceSection = new JPanel(new GridBagLayout());
    insuranceSection.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.BLACK, 2),
            "Insurance Information (if applicable)",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)));
    insuranceSection.setBackground(Color.WHITE);

    row = 0;

    // Provider and Policy number
    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.weightx = 0.3;
    insuranceSection.add(new JLabel("Provider:"), gbc);
    JTextField insuranceProvider = new JTextField(25);
    gbc.gridx = 1;
    gbc.weightx = 0.7;
    insuranceSection.add(insuranceProvider, gbc);

    gbc.gridx = 2;
    gbc.weightx = 0.3;
    insuranceSection.add(new JLabel("Policy number:"), gbc);
    JTextField policyNumber = new JTextField(20);
    gbc.gridx = 3;
    gbc.weightx = 0.7;
    insuranceSection.add(policyNumber, gbc);
    row++;

    // Group Number and Policyholder Name
    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.weightx = 0.3;
    insuranceSection.add(new JLabel("Group Number:"), gbc);
    JTextField groupNumber = new JTextField(20);
    gbc.gridx = 1;
    gbc.weightx = 0.7;
    insuranceSection.add(groupNumber, gbc);

    gbc.gridx = 2;
    gbc.weightx = 0.3;
    insuranceSection.add(new JLabel("Policyholder Name"), gbc);
    JTextField policyholderName = new JTextField(25);
    gbc.gridx = 3;
    gbc.weightx = 0.7;
    insuranceSection.add(policyholderName, gbc);
    row++;

    // Relationship to Patient checkboxes
    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.weightx = 0.3;
    insuranceSection.add(new JLabel("Relationship to Patient:"), gbc);

    JPanel relPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
    relPanel.setBackground(Color.WHITE);
    JCheckBox relSelf = new JCheckBox("Self");
    JCheckBox relSpouse = new JCheckBox("Spouse");
    JCheckBox relParent = new JCheckBox("Parent");
    JCheckBox relOther = new JCheckBox("Other");
    JTextField relOtherText = new JTextField(10);
    relOtherText.setEnabled(false);
    relPanel.add(relSelf);
    relPanel.add(relSpouse);
    relPanel.add(relParent);
    relPanel.add(relOther);
    relPanel.add(relOtherText);

    gbc.gridx = 1;
    gbc.gridwidth = 3;
    gbc.weightx = 1.0;
    insuranceSection.add(relPanel, gbc);
    gbc.gridwidth = 1;

    // Relationship mutual exclusion
    relSelf.addActionListener(e -> {
        if (relSelf.isSelected()) {
            relSpouse.setSelected(false);
            relParent.setSelected(false);
            relOther.setSelected(false);
            relOtherText.setEnabled(false);
            policyholderName.setText(nameField.getText());
        }
    });
    relSpouse.addActionListener(e -> {
        if (relSpouse.isSelected()) {
            relSelf.setSelected(false);
            relParent.setSelected(false);
            relOther.setSelected(false);
            relOtherText.setEnabled(false);
        }
    });
    relParent.addActionListener(e -> {
        if (relParent.isSelected()) {
            relSelf.setSelected(false);
            relSpouse.setSelected(false);
            relOther.setSelected(false);
            relOtherText.setEnabled(false);
        }
    });
    relOther.addActionListener(e -> {
        if (relOther.isSelected()) {
            relSelf.setSelected(false);
            relSpouse.setSelected(false);
            relParent.setSelected(false);
            relOtherText.setEnabled(true);
        } else {
            relOtherText.setEnabled(false);
        }
    });

    mainPanel.add(insuranceSection);
    mainPanel.add(Box.createVerticalStrut(15));

    // ==================== PHARMACY INFORMATION SECTION ====================
    JPanel pharmacySection = new JPanel(new GridBagLayout());
    pharmacySection.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.BLACK, 2),
            "Pharmacy Information",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)));
    pharmacySection.setBackground(Color.WHITE);

    row = 0;

    // Preferred Pharmacy Name and Phone Number
    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.weightx = 0.3;
    pharmacySection.add(new JLabel("Preferred Pharmacy Name:"), gbc);
    JTextField pharmacyName = new JTextField(30);
    gbc.gridx = 1;
    gbc.weightx = 0.7;
    pharmacySection.add(pharmacyName, gbc);

    gbc.gridx = 2;
    gbc.weightx = 0.3;
    pharmacySection.add(new JLabel("Phone Number:"), gbc);
    JTextField pharmacyPhone = new JTextField(15);
    gbc.gridx = 3;
    gbc.weightx = 0.7;
    pharmacySection.add(pharmacyPhone, gbc);
    row++;

    // Address
    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.weightx = 0.3;
    pharmacySection.add(new JLabel("Address:"), gbc);
    JTextField pharmacyAddress = new JTextField(40);
    gbc.gridx = 1;
    gbc.gridwidth = 3;
    gbc.weightx = 1.0;
    pharmacySection.add(pharmacyAddress, gbc);
    gbc.gridwidth = 1;

    mainPanel.add(pharmacySection);
    mainPanel.add(Box.createVerticalStrut(15));

    // ==================== CONSENT & SIGNATURE SECTION ====================
    JPanel consentSection = new JPanel(new GridBagLayout());
    consentSection.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.BLACK, 2),
            "Consent & Signature",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)));
    consentSection.setBackground(Color.WHITE);

    row = 0;

    // Consent statement
    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.gridwidth = 4;
    gbc.weightx = 1.0;
    JLabel consentLabel = new JLabel(
            "I confirm that the information provided is accurate to the best of my knowledge.");
    consentLabel.setFont(new Font("Arial", Font.ITALIC, 12));
    consentSection.add(consentLabel, gbc);
    gbc.gridwidth = 1;
    row++;

    // Signature and Date
    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.weightx = 0.2;
    consentSection.add(new JLabel("Signature:"), gbc);
    JTextField signatureField = new JTextField(30);
    gbc.gridx = 1;
    gbc.weightx = 0.6;
    consentSection.add(signatureField, gbc);

    gbc.gridx = 2;
    gbc.weightx = 0.2;
    consentSection.add(new JLabel("Date:"), gbc);
    JTextField dateField = new JTextField(15);
    dateField.setText(LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy")));
    gbc.gridx = 3;
    gbc.weightx = 0.2;
    consentSection.add(dateField, gbc);

    mainPanel.add(consentSection);

    // Scroll pane for main panel
    JScrollPane scrollPane = new JScrollPane(mainPanel);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    scrollPane.getVerticalScrollBar().setUnitIncrement(16);

    dialog.add(scrollPane, BorderLayout.CENTER);

    // ==================== BUTTONS ====================
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
    buttonPanel.setBackground(Color.WHITE);

    JButton saveButton = new JButton("Save Patient");
    saveButton.setFont(new Font("Arial", Font.BOLD, 12));
    saveButton.setBackground(new Color(0, 102, 102));
    saveButton.setForeground(Color.WHITE);
    saveButton.setFocusPainted(false);

    JButton cancelButton = new JButton("Cancel");
    cancelButton.setFont(new Font("Arial", Font.PLAIN, 12));
    cancelButton.setFocusPainted(false);

    buttonPanel.add(saveButton);
    buttonPanel.add(cancelButton);

    dialog.add(buttonPanel, BorderLayout.SOUTH);

    // ==================== SAVE BUTTON ACTION ====================
    saveButton.addActionListener(e -> {
        // Validate required fields
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "Name is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (dobField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "Date of Birth is required", "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Parse DOB from MM/DD/YYYY to YYYY-MM-DD and calculate age
        String dobStr = dobField.getText().trim();
        String birthday_yyyymmdd;
        int calculatedAge;
        try {
            java.time.format.DateTimeFormatter inputFormatter = java.time.format.DateTimeFormatter
                    .ofPattern("MM/dd/yyyy");
            LocalDate dob = LocalDate.parse(dobStr, inputFormatter);
            birthday_yyyymmdd = dob.toString(); // YYYY-MM-DD format
            calculatedAge = java.time.Period.between(dob, LocalDate.now()).getYears();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(dialog, "Invalid Date of Birth format. Use MM/DD/YYYY", "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate gender
        String genderValue;
        if (genderMale.isSelected()) {
            genderValue = "Male";
        } else if (genderFemale.isSelected()) {
            genderValue = "Female";
        } else if (genderOther.isSelected()) {
            genderValue = genderOtherText.getText().trim().isEmpty() ? "Other" : genderOtherText.getText().trim();
        } else {
            JOptionPane.showMessageDialog(dialog, "Gender is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (phoneField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "Phone is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (addressField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "Address is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (signatureField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "Signature is required", "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Build contact info with preference
        StringBuilder contactInfo = new StringBuilder(phoneField.getText().trim());
        if (!emailField.getText().trim().isEmpty()) {
            contactInfo.append(" | ").append(emailField.getText().trim());
        }

        // Build full address
        StringBuilder fullAddress = new StringBuilder(addressField.getText().trim());
        if (!cityField.getText().trim().isEmpty()) {
            fullAddress.append(", ").append(cityField.getText().trim());
        }
        if (!stateField.getText().trim().isEmpty()) {
            fullAddress.append(", ").append(stateField.getText().trim());
        }
        if (!zipField.getText().trim().isEmpty()) {
            fullAddress.append(" ").append(zipField.getText().trim());
        }

        // Insurance relationship
        String insuranceRel = "";
        if (relSelf.isSelected())
            insuranceRel = "Self";
        else if (relSpouse.isSelected())
            insuranceRel = "Spouse";
        else if (relParent.isSelected())
            insuranceRel = "Parent";
        else if (relOther.isSelected())
            insuranceRel = relOtherText.getText().trim().isEmpty() ? "Other" : relOtherText.getText().trim();

        // Determine patient type (default to OUTPATIENT for intake form)
        String patientType = "OUTPATIENT";

        // Call service with backward-compatible parameters
        List<String> result = PatientService.add(
                nameField.getText().trim(),
                String.valueOf(calculatedAge),
                birthday_yyyymmdd,
                genderValue,
                contactInfo.toString(),
                fullAddress.toString(),
                patientType,
                "Walk-in Patient", // registration type
                "", // incident time
                "", // brought by
                "", // initial BP
                "", // initial HR
                "", // initial SpO2
                "", // chief complaint
                "", // allergies (empty for now)
                "", // medications
                "", // past medical history
                "", // smoking status
                "", // alcohol use
                "", // occupation
                insuranceProvider.getText().trim(),
                policyNumber.getText().trim(),
                policyholderName.getText().trim(),
                "", // policy holder DOB
                insuranceRel);

        if (!result.isEmpty() && result.get(0).startsWith("Patient created")) {
            String patientId = result.get(0).substring("Patient created ".length()).trim();

            // Store additional new fields in patient record
            Patient newPatient = DataStore.patients.get(patientId);
            if (newPatient != null) {
                // Store emergency contact info
                if (!emergencyContactName.getText().trim().isEmpty()) {
                    newPatient.progressNotes.add("Emergency Contact: " + emergencyContactName.getText().trim() +
                            " | Phone: " + emergencyContactPhone.getText().trim() +
                            " | Relationship: " + emergencyRelationship.getText().trim());
                }

                // Store pharmacy info
                if (!pharmacyName.getText().trim().isEmpty()) {
                    newPatient.progressNotes.add("Preferred Pharmacy: " + pharmacyName.getText().trim() +
                            " | Phone: " + pharmacyPhone.getText().trim() +
                            " | Address: " + pharmacyAddress.getText().trim());
                }

                // Store consent signature
                newPatient.progressNotes.add("Consent signed by: " + signatureField.getText().trim() +
                        " on " + dateField.getText().trim());

                // Store preferred pronouns
                StringBuilder pronouns = new StringBuilder();
                if (pronounHeHim.isSelected())
                    pronouns.append("He/Him ");
                if (pronounSheHer.isSelected())
                    pronouns.append("She/Her ");
                if (pronounTheyThem.isSelected())
                    pronouns.append("They/Them ");
                if (pronounOther.isSelected())
                    pronouns.append(pronounOtherText.getText().trim());
                if (pronouns.length() > 0) {
                    newPatient.progressNotes.add("Preferred Pronouns: " + pronouns.toString().trim());
                }

                // Store primary language and interpreter
                if (!primaryLanguageField.getText().trim().isEmpty()) {
                    newPatient.progressNotes.add("Primary Language: " + primaryLanguageField.getText().trim() +
                            " | Interpreter needed: " + (interpreterYes.isSelected() ? "Yes" : "No"));
                }

                // Store preferred contact method
                StringBuilder contactMethods = new StringBuilder();
                if (contactPhone.isSelected())
                    contactMethods.append("Phone ");
                if (contactEmail.isSelected())
                    contactMethods.append("Email ");
                if (contactText.isSelected())
                    contactMethods.append("Text ");
                if (contactMethods.length() > 0) {
                    newPatient.progressNotes.add("Preferred Contact: " + contactMethods.toString().trim());
                }

                // Store insurance group number if provided
                if (!groupNumber.getText().trim().isEmpty()) {
                    newPatient.insuranceGroup = groupNumber.getText().trim();
                }

                try {
                    BackupUtil.saveToDefault();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            // Generate patient portal credentials
            String defaultPassword = generateDefaultPassword(patientId);
            showPatientCredentialsDialog(patientId, defaultPassword);

            JOptionPane.showMessageDialog(dialog, "Patient created successfully!\nPatient ID: " + patientId,
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
            refresh();
        } else {
            JOptionPane.showMessageDialog(dialog, result.get(0), "Error", JOptionPane.ERROR_MESSAGE);
        }
    });

    cancelButton.addActionListener(e -> dialog.dispose());

    dialog.setVisible(true);
}
