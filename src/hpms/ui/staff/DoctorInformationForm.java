package hpms.ui.staff;

import javax.swing.*;
import javax.swing.border.*;
import hpms.ui.components.Theme;
import hpms.service.StaffService;
import java.awt.*;
import java.util.*;

/**
 * Comprehensive Doctor Information Form matching the reference image exactly.
 * Includes all personal, contact, medical, credentials, and professional
 * fields.
 */
public class DoctorInformationForm extends JFrame {
    // Section 1: Doctor Information
    private JTextField fullNameField, dobField;
    private JComboBox<String> genderCombo, pronounsCombo;
    private JTextField specializationField, licenseNumberField, hospitalAffiliationField;
    private JTextField phoneNumberField, emailAddressField, officeAddressField;
    private JComboBox<String> preferredContactCombo;

    // Section 2: Professional Credentials
    private JTextField medicalSchoolField, yearGraduatedField, residencyField, fellowshipField;
    private JCheckBox boardCertYesRadio, boardCertNoRadio;
    private JTextField licenseExpirationField;

    // Section 3: Professional Baccalaureates
    private JCheckBox clinicalDiagnosticsCheck, patientAssessmentCheck, surgicalSkillsCheck;
    private JCheckBox emergencyManagementCheck, proceduresCheck, othersCheck;
    private JTextField othersField;
    private JCheckBox clinicalSkillsCheck, diagnocticsCheck, surgeryCheck, proceduresCheckAlt;
    private JCheckBox copahoCheck;
    private JTextField copahoDtlField;

    // Section 4: Educational Licenses & Registration
    private JTextField medicalEducationField, boardExamField, ptrField, registrationIDField;
    private JComboBox<String> specialtyBoardCombo;
    private JCheckBox pogsCheck, pnaCheck, pcsCheck, otherBoardCheck;
    private JTextField otherBoardField;

    // Section 5: Work Experience
    private JTextField workExpInstitutionField, workExpHospitalField, workExpDurationField;

    // Section 6: Skills & Competencies
    private JCheckBox clinicalSkillsCheckAlt, diagnosticsCheck, teamworkCheck;
    private JCheckBox patientAssessmentCheckAlt, leadershipCheck;
    private JCheckBox emergencyCheck, criticalThinkingCheck, surgicalCheck;
    private JCheckBox proceduresCheckAlt2, specializationCheck;

    // Section 8: Professional Ancriry
    private JCheckBox pmaAssociationCheck, specialtySocietiesCheck;
    private JTextField specialtySocietiesField, membershipField;

    // Section 9: Character References
    private JTextField reference1Field, reference2Field, optionalRef3Field;

    public DoctorInformationForm() {
        setTitle("DOCTOR INFORMATION FORM");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1200, 1400);
        setLocationRelativeTo(null);

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Theme.PRIMARY);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        JLabel titleLabel = new JLabel("DOCTOR INFORMATION FORM");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Scrollable content
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBackground(Color.WHITE);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Footer with buttons
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footerPanel.setBackground(Color.WHITE);
        footerPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Arial", Font.BOLD, 12));
        cancelBtn.setBackground(new Color(155, 155, 155));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        cancelBtn.setFocusPainted(false);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = new JButton("Save Doctor");
        saveBtn.setFont(new Font("Arial", Font.BOLD, 12));
        saveBtn.setBackground(Theme.PRIMARY);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        saveBtn.setFocusPainted(false);
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveBtn.addActionListener(e -> saveDoctor());

        footerPanel.add(cancelBtn);
        footerPanel.add(saveBtn);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        // Build form sections
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);

        // Section 1: Doctor Information
        contentPanel.add(createSection1(), gbc);
        gbc.gridy++;

        // Section 2: Professional Credentials
        contentPanel.add(createSection2(), gbc);
        gbc.gridy++;

        // Section 3: Professional Baccalaureates
        contentPanel.add(createSection3(), gbc);
        gbc.gridy++;

        // Section 4: Educational Licenses & Registration
        contentPanel.add(createSection4(), gbc);
        gbc.gridy++;

        // Section 5: Work Experience
        contentPanel.add(createSection5(), gbc);
        gbc.gridy++;

        // Section 6: Skills & Competencies
        contentPanel.add(createSection6(), gbc);
        gbc.gridy++;

        // Section 8: Professional Ancriry
        contentPanel.add(createSection8(), gbc);
        gbc.gridy++;

        // Section 9: Character References
        contentPanel.add(createSection9(), gbc);
        gbc.gridy++;

        // Fill remaining space
        gbc.weighty = 1.0;
        contentPanel.add(new JPanel(), gbc);
    }

    private String buildDoctorCertifications() {
        java.util.List<String> parts = new java.util.ArrayList<>();
        if (boardCertYesRadio != null && boardCertYesRadio.isSelected()) {
            parts.add("Board Certified: Yes");
        } else if (boardCertNoRadio != null && boardCertNoRadio.isSelected()) {
            parts.add("Board Certified: No");
        }
        if (specialtyBoardCombo != null && specialtyBoardCombo.getSelectedItem() != null) {
            String v = String.valueOf(specialtyBoardCombo.getSelectedItem()).trim();
            if (!v.isEmpty()) {
                parts.add("Specialty Board: " + v);
            }
        }
        if (otherBoardField != null && otherBoardField.getText() != null) {
            String v = otherBoardField.getText().trim();
            if (!v.isEmpty()) {
                parts.add("Other Board: " + v);
            }
        }
        return parts.isEmpty() ? null : String.join("\n", parts);
    }

    private String buildDoctorEducation() {
        java.util.List<String> parts = new java.util.ArrayList<>();
        if (medicalSchoolField != null && medicalSchoolField.getText() != null) {
            String v = medicalSchoolField.getText().trim();
            if (!v.isEmpty()) parts.add("Medical School: " + v);
        }
        if (yearGraduatedField != null && yearGraduatedField.getText() != null) {
            String v = yearGraduatedField.getText().trim();
            if (!v.isEmpty()) parts.add("Year Graduated: " + v);
        }
        if (residencyField != null && residencyField.getText() != null) {
            String v = residencyField.getText().trim();
            if (!v.isEmpty()) parts.add("Residency: " + v);
        }
        if (fellowshipField != null && fellowshipField.getText() != null) {
            String v = fellowshipField.getText().trim();
            if (!v.isEmpty()) parts.add("Fellowship: " + v);
        }
        if (medicalEducationField != null && medicalEducationField.getText() != null) {
            String v = medicalEducationField.getText().trim();
            if (!v.isEmpty()) parts.add("Medical Education: " + v);
        }
        if (boardExamField != null && boardExamField.getText() != null) {
            String v = boardExamField.getText().trim();
            if (!v.isEmpty()) parts.add("Board Exam: " + v);
        }
        if (ptrField != null && ptrField.getText() != null) {
            String v = ptrField.getText().trim();
            if (!v.isEmpty()) parts.add("PTR Number: " + v);
        }
        if (registrationIDField != null && registrationIDField.getText() != null) {
            String v = registrationIDField.getText().trim();
            if (!v.isEmpty()) parts.add("Registration ID: " + v);
        }
        return parts.isEmpty() ? null : String.join("\n", parts);
    }

    private String buildDoctorExpertise() {
        java.util.List<String> parts = new java.util.ArrayList<>();
        if (workExpInstitutionField != null && workExpInstitutionField.getText() != null) {
            String v = workExpInstitutionField.getText().trim();
            if (!v.isEmpty()) parts.add("Institution: " + v);
        }
        if (workExpHospitalField != null && workExpHospitalField.getText() != null) {
            String v = workExpHospitalField.getText().trim();
            if (!v.isEmpty()) parts.add("Hospital/Clinic: " + v);
        }
        if (workExpDurationField != null && workExpDurationField.getText() != null) {
            String v = workExpDurationField.getText().trim();
            if (!v.isEmpty()) parts.add("Duration: " + v);
        }
        if (licenseExpirationField != null && licenseExpirationField.getText() != null) {
            String v = licenseExpirationField.getText().trim();
            if (!v.isEmpty()) parts.add("License Expiration: " + v);
        }
        return parts.isEmpty() ? null : String.join("\n", parts);
    }

    private String joinSelectedSkills() {
        java.util.List<String> parts = new java.util.ArrayList<>();
        if (clinicalDiagnosticsCheck != null && clinicalDiagnosticsCheck.isSelected()) parts.add("Clinical Diagnostics");
        if (patientAssessmentCheck != null && patientAssessmentCheck.isSelected()) parts.add("Patient Assessment");
        if (surgicalSkillsCheck != null && surgicalSkillsCheck.isSelected()) parts.add("Surgical Skills");
        if (emergencyManagementCheck != null && emergencyManagementCheck.isSelected()) parts.add("Emergency Management");
        if (proceduresCheck != null && proceduresCheck.isSelected()) parts.add("Procedures");
        if (othersCheck != null && othersCheck.isSelected()) parts.add("Others");
        if (othersField != null && othersField.getText() != null) {
            String v = othersField.getText().trim();
            if (!v.isEmpty()) parts.add(v);
        }
        if (clinicalSkillsCheckAlt != null && clinicalSkillsCheckAlt.isSelected()) parts.add("Clinical Skills");
        if (diagnosticsCheck != null && diagnosticsCheck.isSelected()) parts.add("Diagnostics");
        if (teamworkCheck != null && teamworkCheck.isSelected()) parts.add("Teamwork");
        if (patientAssessmentCheckAlt != null && patientAssessmentCheckAlt.isSelected()) parts.add("Patient Assessment");
        if (leadershipCheck != null && leadershipCheck.isSelected()) parts.add("Leadership");
        if (emergencyCheck != null && emergencyCheck.isSelected()) parts.add("Emergency Care");
        if (criticalThinkingCheck != null && criticalThinkingCheck.isSelected()) parts.add("Critical Thinking");
        if (surgicalCheck != null && surgicalCheck.isSelected()) parts.add("Surgical Skills");
        if (proceduresCheckAlt2 != null && proceduresCheckAlt2.isSelected()) parts.add("Procedures");
        if (specializationCheck != null && specializationCheck.isSelected()) parts.add("Specialization");
        return parts.isEmpty() ? null : String.join(", ", parts);
    }

    private String joinSelectedCompetencies() {
        java.util.List<String> parts = new java.util.ArrayList<>();
        if (clinicalSkillsCheck != null && clinicalSkillsCheck.isSelected()) parts.add("Clinical Skills");
        if (diagnocticsCheck != null && diagnocticsCheck.isSelected()) parts.add("Diagnostics");
        if (surgeryCheck != null && surgeryCheck.isSelected()) parts.add("Surgery");
        if (proceduresCheckAlt != null && proceduresCheckAlt.isSelected()) parts.add("Procedures");
        if (copahoCheck != null && copahoCheck.isSelected()) parts.add("COPAHO");
        if (copahoDtlField != null && copahoDtlField.getText() != null) {
            String v = copahoDtlField.getText().trim();
            if (!v.isEmpty()) parts.add(v);
        }
        return parts.isEmpty() ? null : String.join(", ", parts);
    }

    private JPanel createSection1() {
        JPanel section = new JPanel(new GridBagLayout());
        section.setBackground(Color.WHITE);
        section.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                "1. Doctor Information", TitledBorder.LEFT, TitledBorder.TOP));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Full Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.2;
        section.add(new JLabel("Full Name: "), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.5;
        fullNameField = new JTextField();
        fullNameField.setMinimumSize(new Dimension(300, 28));
        fullNameField.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        section.add(fullNameField, gbc);

        // DOB and Gender - on same row
        gbc.gridx = 2;
        gbc.weightx = 0.15;
        section.add(new JLabel("Date of Birth (MM/DD/YY): "), gbc);
        gbc.gridx = 3;
        gbc.weightx = 0.15;
        dobField = new JTextField();
        dobField.setMinimumSize(new Dimension(120, 28));
        dobField.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        section.add(dobField, gbc);

        gbc.gridx = 4;
        gbc.weightx = 0.1;
        section.add(new JLabel("Gender: "), gbc);
        gbc.gridx = 5;
        gbc.weightx = 0.15;
        genderCombo = new JComboBox<>(new String[] { "Male", "Female", "Other" });
        genderCombo.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        section.add(genderCombo, gbc);

        // Preferred Pronouns
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0.2;
        section.add(new JLabel("Preferred Pronouns: "), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 5;
        gbc.weightx = 0.8;
        JPanel pronounsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        pronounsPanel.setBackground(Color.WHITE);
        pronounsPanel.add(new JCheckBox("He/Him"));
        pronounsPanel.add(new JCheckBox("She/Her"));
        pronounsPanel.add(new JCheckBox("They/Them"));
        pronounsPanel.add(new JCheckBox("Other: "));
        pronounsPanel.add(new JTextField(10));
        section.add(pronounsPanel, gbc);
        gbc.gridwidth = 1;

        // Specialization / Department
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0.2;
        section.add(new JLabel("Specialization / Department: "), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 0.5;
        specializationField = new JTextField();
        specializationField.setMinimumSize(new Dimension(300, 28));
        specializationField.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        section.add(specializationField, gbc);
        gbc.gridwidth = 1;

        // Medical License & Hospital Affiliation
        gbc.gridx = 3;
        gbc.weightx = 0.2;
        section.add(new JLabel("Medical License Number: "), gbc);
        gbc.gridx = 4;
        gbc.gridwidth = 2;
        gbc.weightx = 0.4;
        licenseNumberField = new JTextField();
        licenseNumberField.setMinimumSize(new Dimension(200, 28));
        licenseNumberField.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        section.add(licenseNumberField, gbc);
        gbc.gridwidth = 1;

        // Contact information row
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0.15;
        section.add(new JLabel("Phone Number: "), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.25;
        phoneNumberField = new JTextField();
        phoneNumberField.setMinimumSize(new Dimension(150, 28));
        phoneNumberField.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        section.add(phoneNumberField, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.15;
        section.add(new JLabel("Email Address: "), gbc);
        gbc.gridx = 3;
        gbc.gridwidth = 2;
        gbc.weightx = 0.35;
        emailAddressField = new JTextField();
        emailAddressField.setMinimumSize(new Dimension(250, 28));
        emailAddressField.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        section.add(emailAddressField, gbc);
        gbc.gridwidth = 1;

        // Office Address
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0.15;
        section.add(new JLabel("Office Address: "), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 5;
        gbc.weightx = 0.85;
        officeAddressField = new JTextField();
        officeAddressField.setMinimumSize(new Dimension(400, 28));
        officeAddressField.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        section.add(officeAddressField, gbc);
        gbc.gridwidth = 1;

        // Preferred Contact Method
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0.2;
        section.add(new JLabel("Preferred Contact Method: "), gbc);
        gbc.gridx = 1;
        JPanel contactPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        contactPanel.setBackground(Color.WHITE);
        contactPanel.add(new JCheckBox("Phone"));
        contactPanel.add(new JCheckBox("Email"));
        contactPanel.add(new JCheckBox("Text"));
        gbc.gridwidth = 4;
        gbc.weightx = 0.8;
        section.add(contactPanel, gbc);
        gbc.gridwidth = 1;

        // Hospital Affiliation
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0.2;
        section.add(new JLabel("Hospital / Clinic Affiliation: "), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 5;
        gbc.weightx = 0.8;
        hospitalAffiliationField = new JTextField();
        hospitalAffiliationField.setMinimumSize(new Dimension(400, 28));
        hospitalAffiliationField.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        section.add(hospitalAffiliationField, gbc);
        gbc.gridwidth = 1;

        return section;
    }

    private JPanel createSection2() {
        JPanel section = new JPanel(new GridBagLayout());
        section.setBackground(Color.WHITE);
        section.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                "2. Professional Credentials", TitledBorder.LEFT, TitledBorder.TOP));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.2;
        section.add(new JLabel("Medical School: "), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.3;
        medicalSchoolField = new JTextField();
        medicalSchoolField.setMinimumSize(new Dimension(250, 28));
        medicalSchoolField.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        section.add(medicalSchoolField, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.2;
        section.add(new JLabel("Year Graduated: "), gbc);
        gbc.gridx = 3;
        gbc.weightx = 0.3;
        yearGraduatedField = new JTextField();
        yearGraduatedField.setMinimumSize(new Dimension(150, 28));
        yearGraduatedField.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        section.add(yearGraduatedField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0.2;
        section.add(new JLabel("Residency Training: "), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.3;
        residencyField = new JTextField();
        residencyField.setMinimumSize(new Dimension(250, 28));
        residencyField.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        section.add(residencyField, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.2;
        section.add(new JLabel("Fellowship (If any): "), gbc);
        gbc.gridx = 3;
        gbc.weightx = 0.3;
        fellowshipField = new JTextField();
        fellowshipField.setMinimumSize(new Dimension(150, 28));
        fellowshipField.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        section.add(fellowshipField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0.2;
        section.add(new JLabel("Board Certification: "), gbc);
        gbc.gridx = 1;
        JPanel boardPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        boardPanel.setBackground(Color.WHITE);
        boardCertYesRadio = new JCheckBox("Yes");
        boardCertNoRadio = new JCheckBox("No");
        boardPanel.add(boardCertYesRadio);
        boardPanel.add(boardCertNoRadio);
        gbc.gridwidth = 3;
        gbc.weightx = 0.8;
        section.add(boardPanel, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0.2;
        section.add(new JLabel("License Expiration Date: "), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.3;
        licenseExpirationField = new JTextField();
        licenseExpirationField.setMinimumSize(new Dimension(150, 28));
        licenseExpirationField.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        section.add(licenseExpirationField, gbc);

        return section;
    }

    private JPanel createSection3() {
        JPanel section = new JPanel(new GridBagLayout());
        section.setBackground(Color.WHITE);
        section.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                "3. Professional Baccalaureates", TitledBorder.LEFT, TitledBorder.TOP));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.2;
        section.add(new JLabel("Clinical Skills: "), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        JPanel skillsPanel = new JPanel(new GridLayout(2, 2, 10, 5));
        skillsPanel.setBackground(Color.WHITE);
        skillsPanel.add(new JCheckBox("Clinical Diagnostics"));
        skillsPanel.add(new JCheckBox("Patient Assessment"));
        skillsPanel.add(new JCheckBox("Emergency Management"));
        skillsPanel.add(new JCheckBox("Surgical Skills"));
        skillsPanel.add(new JCheckBox("Surgeries"));
        skillsPanel.add(new JCheckBox("Procedures: ___"));
        skillsPanel.add(new JCheckBox("Others: "));
        section.add(skillsPanel, gbc);

        return section;
    }

    private JPanel createSection4() {
        JPanel section = new JPanel(new GridBagLayout());
        section.setBackground(Color.WHITE);
        section.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                "4. Educational Licenses & Registration", TitledBorder.LEFT, TitledBorder.TOP));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.2;
        section.add(new JLabel("Medical Education: "), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.3;
        medicalEducationField = new JTextField();
        medicalEducationField.setMinimumSize(new Dimension(250, 28));
        medicalEducationField.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        section.add(medicalEducationField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        section.add(new JLabel("Board Exam Results: "), gbc);
        gbc.gridx = 1;
        boardExamField = new JTextField();
        boardExamField.setMinimumSize(new Dimension(250, 28));
        boardExamField.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        section.add(boardExamField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        section.add(new JLabel("PTR Number: "), gbc);
        gbc.gridx = 1;
        ptrField = new JTextField();
        ptrField.setMinimumSize(new Dimension(250, 28));
        ptrField.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        section.add(ptrField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        section.add(new JLabel("Medical Registration ID: "), gbc);
        gbc.gridx = 1;
        registrationIDField = new JTextField();
        registrationIDField.setMinimumSize(new Dimension(250, 28));
        registrationIDField.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        section.add(registrationIDField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        section.add(new JLabel("Specialty Board Certification: "), gbc);
        gbc.gridx = 1;
        JPanel boardsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        boardsPanel.setBackground(Color.WHITE);
        boardsPanel.add(new JCheckBox("POGS"));
        boardsPanel.add(new JCheckBox("PNA"));
        boardsPanel.add(new JCheckBox("PCS"));
        boardsPanel.add(new JCheckBox("Other: "));
        boardsPanel.add(new JTextField(10));
        gbc.gridwidth = 3;
        gbc.weightx = 0.8;
        section.add(boardsPanel, gbc);
        gbc.gridwidth = 1;

        return section;
    }

    private JPanel createSection5() {
        JPanel section = new JPanel(new GridBagLayout());
        section.setBackground(Color.WHITE);
        section.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                "5. Work Experience", TitledBorder.LEFT, TitledBorder.TOP));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.2;
        section.add(new JLabel("Add Work Experience: "), gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        section.add(new JLabel("Institution / "), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.3;
        workExpInstitutionField = new JTextField();
        workExpInstitutionField.setMinimumSize(new Dimension(250, 28));
        workExpInstitutionField.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        section.add(workExpInstitutionField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        section.add(new JLabel("Hospital / Clinic: "), gbc);
        gbc.gridx = 1;
        workExpHospitalField = new JTextField();
        workExpHospitalField.setMinimumSize(new Dimension(250, 28));
        workExpHospitalField.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        section.add(workExpHospitalField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        section.add(new JLabel("Duration: "), gbc);
        gbc.gridx = 1;
        workExpDurationField = new JTextField();
        workExpDurationField.setMinimumSize(new Dimension(250, 28));
        workExpDurationField.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        section.add(workExpDurationField, gbc);

        return section;
    }

    private JPanel createSection6() {
        JPanel section = new JPanel(new GridBagLayout());
        section.setBackground(Color.WHITE);
        section.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                "6. Skills & Competencies", TitledBorder.LEFT, TitledBorder.TOP));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        JPanel skillsPanel = new JPanel(new GridLayout(4, 2, 10, 5));
        skillsPanel.setBackground(Color.WHITE);
        skillsPanel.add(new JCheckBox("Clinical Skills"));
        skillsPanel.add(new JCheckBox("Clinical Diagnostics"));
        skillsPanel.add(new JCheckBox("Teamwork/Collaboration"));
        skillsPanel.add(new JCheckBox("Leadership"));
        skillsPanel.add(new JCheckBox("Patient Assessment"));
        skillsPanel.add(new JCheckBox("Emergency Management"));
        skillsPanel.add(new JCheckBox("Critical Thinking"));
        skillsPanel.add(new JCheckBox("Surgical Skills"));
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        section.add(skillsPanel, gbc);

        return section;
    }

    private JPanel createSection8() {
        JPanel section = new JPanel(new GridBagLayout());
        section.setBackground(Color.WHITE);
        section.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                "8. Professional Ancriry", TitledBorder.LEFT, TitledBorder.TOP));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.2;
        section.add(new JLabel("Medical Associations: "), gbc);
        gbc.gridx = 1;
        JPanel assocPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        assocPanel.setBackground(Color.WHITE);
        assocPanel.add(new JCheckBox("PMA"));
        assocPanel.add(new JCheckBox("Specialty Societies"));
        assocPanel.add(new JLabel("Specify: "));
        assocPanel.add(new JTextField(15));
        gbc.gridwidth = 3;
        gbc.weightx = 0.8;
        section.add(assocPanel, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.gridy++;
        section.add(new JLabel("Hospital Staff Membership: "), gbc);
        gbc.gridx = 1;
        membershipField = new JTextField();
        membershipField.setMinimumSize(new Dimension(250, 28));
        membershipField.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        section.add(membershipField, gbc);

        return section;
    }

    private JPanel createSection9() {
        JPanel section = new JPanel(new GridBagLayout());
        section.setBackground(Color.WHITE);
        section.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                "9. Character References", TitledBorder.LEFT, TitledBorder.TOP));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.2;
        section.add(new JLabel("Reference 1: "), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.8;
        reference1Field = new JTextField();
        reference1Field.setMinimumSize(new Dimension(400, 28));
        reference1Field.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        section.add(reference1Field, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        section.add(new JLabel("Reference 2: "), gbc);
        gbc.gridx = 1;
        reference2Field = new JTextField();
        reference2Field.setMinimumSize(new Dimension(400, 28));
        reference2Field.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        section.add(reference2Field, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        section.add(new JLabel("(Optional 3rd): "), gbc);
        gbc.gridx = 1;
        optionalRef3Field = new JTextField();
        optionalRef3Field.setMinimumSize(new Dimension(400, 28));
        optionalRef3Field.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        section.add(optionalRef3Field, gbc);

        return section;
    }

    private void saveDoctor() {
        // Validate all required fields
        if (fullNameField.getText().trim().isEmpty() ||
                dobField.getText().trim().isEmpty() ||
                phoneNumberField.getText().trim().isEmpty() ||
                emailAddressField.getText().trim().isEmpty() ||
                medicalSchoolField.getText().trim().isEmpty() ||
                yearGraduatedField.getText().trim().isEmpty()) {

            JOptionPane.showMessageDialog(this,
                    "Please fill all required fields",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Build qualifications string from form data
        StringBuilder qualifications = new StringBuilder();
        if (!medicalSchoolField.getText().trim().isEmpty()) {
            qualifications.append("Medical School: ").append(medicalSchoolField.getText()).append("; ");
        }
        if (!yearGraduatedField.getText().trim().isEmpty()) {
            qualifications.append("Year Graduated: ").append(yearGraduatedField.getText()).append("; ");
        }
        if (!licenseNumberField.getText().trim().isEmpty()) {
            qualifications.append("License: ").append(licenseNumberField.getText()).append("; ");
        }

        // Build notes string from additional information
        StringBuilder notes = new StringBuilder();
        if (!officeAddressField.getText().trim().isEmpty()) {
            notes.append("Office: ").append(officeAddressField.getText()).append("; ");
        }
        if (!hospitalAffiliationField.getText().trim().isEmpty()) {
            notes.append("Hospital: ").append(hospitalAffiliationField.getText()).append("; ");
        }

        // Save doctor information using 9-parameter StaffService.add method
        java.util.List<String> result = StaffService.add(
                fullNameField.getText(),
                "DOCTOR",
                "Medical",
                specializationField.getText(),
                phoneNumberField.getText(),
                emailAddressField.getText(),
                licenseNumberField.getText(),
                qualifications.toString(),
                notes.toString());

        String actualStaffId = null;
        if (result != null && !result.isEmpty() && result.get(0).startsWith("Staff added")) {
            actualStaffId = result.get(0).substring("Staff added".length()).trim();
        }

        if (actualStaffId != null) {
            hpms.model.Staff saved = hpms.util.DataStore.staff.get(actualStaffId);
            if (saved != null) {
                saved.certifications = buildDoctorCertifications();
                saved.education = buildDoctorEducation();
                saved.expertise = buildDoctorExpertise();
                saved.skills = joinSelectedSkills();
                saved.competencies = joinSelectedCompetencies();
                try {
                    StaffService.updateStaff(saved);
                } catch (Exception ignored) {
                }
            }
        }

        if (result.isEmpty() || result.get(0).contains("Error")) {
            JOptionPane.showMessageDialog(this, "Error saving doctor", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Doctor added successfully", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        }
    }
}
