package hpms.ui.staff;

import hpms.auth.AuthService;
import hpms.model.Staff;
import hpms.model.StaffRole;
import hpms.service.StaffService;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDateTime;

/**
 * Unified Registration Form that adapts based on role selection
 * - DOCTOR: Shows detailed doctor form with all fields
 * - NURSE/CASHIER/FRONT_DESK: Shows simplified registration form
 * Merges quick registration, detailed doctor form, and register user into single process
 */
public class UnifiedRegistrationForm extends JDialog {
    private JTextField doctorPhoneField, staffPhoneField, doctorEmailField, staffEmailField, doctorAddressField, staffAddressField;
    private JTextField doctorNameField, staffNameField; // Separate name fields for each form
    private JComboBox<String> roleCombo, deptCombo;
    private JComboBox<String> specCombo, nursingCombo;
    private JTextField licenseField, yearsExperienceField, yearsOfWorkField;
    private JPanel roleSpecificPanel;
    private JPanel detailedDoctorPanel;
    private JPanel simplifiedStaffPanel;

    private JTextField docMedSchoolField;
    private JTextField docYearGradField;
    private JTextField docResidencyField;
    private JTextField docFellowshipField;
    private JRadioButton docBoardYesRadio;
    private JRadioButton docBoardNoRadio;
    private JTextField docLicenseExpField;

    private JTextField docMedicalEducationField;
    private JTextField docBoardExamField;
    private JTextField docPtrField;
    private JTextField docRegistrationIdField;
    private JComboBox<String> docSpecialtyBoardCombo;
    private JTextField docOtherBoardField;

    private java.util.List<JCheckBox> docSkillChecks = new java.util.ArrayList<>();
    private java.util.List<JCheckBox> docCompetencyChecks = new java.util.ArrayList<>();
    
    // Clinic schedule components (for doctors)
    private JCheckBox[] dayCheckboxes = new JCheckBox[7];
    private JTextField[] startTimeFields = new JTextField[7];
    private JTextField[] endTimeFields = new JTextField[7];
    private String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

    public UnifiedRegistrationForm(Window owner) {
        super(owner, "Unified Staff Registration", ModalityType.APPLICATION_MODAL);
        setSize(900, 750); // Increased size to accommodate scrollable content
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Main panel with border layout
        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(245, 250, 255));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 220, 240), 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JLabel titleLabel = new JLabel("Staff Registration");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(31, 41, 55));
        
        JLabel subtitleLabel = new JLabel("Select a role to see the appropriate registration form");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(107, 114, 128));
        
        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Form content area
        JPanel formPanel = new JPanel(new CardLayout());
        formPanel.setBackground(Color.WHITE);
        
        // Create role selection panel
        JPanel roleSelectionPanel = createRoleSelectionPanel();
        formPanel.add(roleSelectionPanel, "role_selection");
        
        // Create detailed doctor panel
        detailedDoctorPanel = createDetailedDoctorPanel();
        formPanel.add(detailedDoctorPanel, "doctor_form");
        
        // Create simplified staff panel
        simplifiedStaffPanel = createSimplifiedStaffPanel();
        formPanel.add(simplifiedStaffPanel, "staff_form");

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Footer with navigation buttons
        JPanel footerPanel = createFooterPanel(formPanel);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }
    
    private JPanel createRoleSelectionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        // Role selection
        gbc.gridy = 0;
        gbc.gridx = 0;
        panel.add(createLabel("Select Role *"), gbc);
        
        gbc.gridy = 1;
        gbc.gridx = 0;
        roleCombo = new JComboBox<>(new String[]{"DOCTOR", "NURSE", "CASHIER", "FRONT_DESK"});
        roleCombo.setBackground(Color.WHITE);
        roleCombo.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        roleCombo.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(roleCombo, gbc);
        
        // Role description
        gbc.gridy = 2;
        gbc.gridx = 0;
        JTextArea roleDescription = new JTextArea(
            "• DOCTOR: Full medical registration with license, specialization, and clinic schedule\n" +
            "• NURSE: Medical staff registration with nursing field assignment\n" +
            "• CASHIER: Billing and administrative staff registration\n" +
            "• FRONT_DESK: Reception and patient services staff registration"
        );
        roleDescription.setEditable(false);
        roleDescription.setOpaque(false);
        roleDescription.setFont(new Font("Arial", Font.PLAIN, 11));
        roleDescription.setForeground(new Color(75, 85, 99));
        roleDescription.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        panel.add(roleDescription, gbc);
        
        return panel;
    }
    
    private JPanel createDetailedDoctorPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        
        // Create the content panel with all the fields
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.5;
        
        int row = 0;
        
        // Section 1: Doctor Information
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JLabel section1Label = new JLabel("Section 1: Doctor Information");
        section1Label.setFont(new Font("Arial", Font.BOLD, 14));
        section1Label.setForeground(new Color(31, 41, 55));
        contentPanel.add(section1Label, gbc);
        row++;
        gbc.gridwidth = 1;
        
        // Full Name
        gbc.gridy = row;
        gbc.gridx = 0;
        contentPanel.add(createLabel("Full Name *"), gbc);
        gbc.gridx = 1;
        doctorNameField = new JTextField(20);
        doctorNameField.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        contentPanel.add(doctorNameField, gbc);
        row++;
        
        // Date of Birth
        gbc.gridy = row;
        gbc.gridx = 0;
        contentPanel.add(createLabel("Date of Birth *"), gbc);
        gbc.gridx = 1;
        JTextField dobField = new JTextField(20);
        dobField.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        contentPanel.add(dobField, gbc);
        row++;
        
        // Gender
        gbc.gridy = row;
        gbc.gridx = 0;
        contentPanel.add(createLabel("Gender *"), gbc);
        gbc.gridx = 1;
        JComboBox<String> genderCombo = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        genderCombo.setBackground(Color.WHITE);
        genderCombo.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        contentPanel.add(genderCombo, gbc);
        row++;
        
        // Pronouns
        gbc.gridy = row;
        gbc.gridx = 0;
        contentPanel.add(createLabel("Pronouns"), gbc);
        gbc.gridx = 1;
        JComboBox<String> pronounsCombo = new JComboBox<>(new String[]{"He/Him", "She/Her", "They/Them"});
        pronounsCombo.setBackground(Color.WHITE);
        pronounsCombo.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        contentPanel.add(pronounsCombo, gbc);
        row++;
        
        // Specialization
        gbc.gridy = row;
        gbc.gridx = 0;
        contentPanel.add(createLabel("Specialization *"), gbc);
        gbc.gridx = 1;
        specCombo = new JComboBox<>(new String[]{"Cardiology", "Neurology", "Orthopedics", "Pediatrics", "Surgery", "Oncology", "ER"});
        specCombo.setBackground(Color.WHITE);
        specCombo.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        contentPanel.add(specCombo, gbc);
        row++;
        
        // License Number
        gbc.gridy = row;
        gbc.gridx = 0;
        contentPanel.add(createLabel("License Number *"), gbc);
        gbc.gridx = 1;
        licenseField = new JTextField(20);
        licenseField.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        contentPanel.add(licenseField, gbc);
        row++;
        
        // Hospital Affiliation
        gbc.gridy = row;
        gbc.gridx = 0;
        contentPanel.add(createLabel("Hospital Affiliation"), gbc);
        gbc.gridx = 1;
        JTextField hospitalField = new JTextField(20);
        hospitalField.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        contentPanel.add(hospitalField, gbc);
        row++;
        
        // Phone Number
        gbc.gridy = row;
        gbc.gridx = 0;
        contentPanel.add(createLabel("Phone Number *"), gbc);
        gbc.gridx = 1;
        doctorPhoneField = new JTextField(20);
        doctorPhoneField.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        contentPanel.add(doctorPhoneField, gbc);
        row++;
        
        // Email Address
        gbc.gridy = row;
        gbc.gridx = 0;
        contentPanel.add(createLabel("Email Address *"), gbc);
        gbc.gridx = 1;
        doctorEmailField = new JTextField(20);
        doctorEmailField.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        contentPanel.add(doctorEmailField, gbc);
        row++;
        
        // Office Address
        gbc.gridy = row;
        gbc.gridx = 0;
        contentPanel.add(createLabel("Office Address"), gbc);
        gbc.gridx = 1;
        doctorAddressField = new JTextField(20);
        doctorAddressField.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        contentPanel.add(doctorAddressField, gbc);
        row++;
        
        // Preferred Contact Method
        gbc.gridy = row;
        gbc.gridx = 0;
        contentPanel.add(createLabel("Preferred Contact"), gbc);
        gbc.gridx = 1;
        JComboBox<String> contactCombo = new JComboBox<>(new String[]{"Phone", "Email", "Both"});
        contactCombo.setBackground(Color.WHITE);
        contactCombo.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        contentPanel.add(contactCombo, gbc);
        row++;
        
        // Section 2: Professional Credentials
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JLabel section2Label = new JLabel("Section 2: Professional Credentials");
        section2Label.setFont(new Font("Arial", Font.BOLD, 14));
        section2Label.setForeground(new Color(31, 41, 55));
        contentPanel.add(section2Label, gbc);
        row++;
        gbc.gridwidth = 1;
        
        // Medical School
        gbc.gridy = row;
        gbc.gridx = 0;
        contentPanel.add(createLabel("Medical School *"), gbc);
        gbc.gridx = 1;
        docMedSchoolField = new JTextField(20);
        docMedSchoolField.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        contentPanel.add(docMedSchoolField, gbc);
        row++;
        
        // Year Graduated
        gbc.gridy = row;
        gbc.gridx = 0;
        contentPanel.add(createLabel("Year Graduated *"), gbc);
        gbc.gridx = 1;
        docYearGradField = new JTextField(20);
        docYearGradField.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        contentPanel.add(docYearGradField, gbc);
        row++;
        
        // Residency
        gbc.gridy = row;
        gbc.gridx = 0;
        contentPanel.add(createLabel("Residency"), gbc);
        gbc.gridx = 1;
        docResidencyField = new JTextField(20);
        docResidencyField.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        contentPanel.add(docResidencyField, gbc);
        row++;
        
        // Fellowship
        gbc.gridy = row;
        gbc.gridx = 0;
        contentPanel.add(createLabel("Fellowship"), gbc);
        gbc.gridx = 1;
        docFellowshipField = new JTextField(20);
        docFellowshipField.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        contentPanel.add(docFellowshipField, gbc);
        row++;
        
        // Board Certification
        gbc.gridy = row;
        gbc.gridx = 0;
        contentPanel.add(createLabel("Board Certified"), gbc);
        gbc.gridx = 1;
        JPanel certPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        certPanel.setOpaque(false);
        docBoardYesRadio = new JRadioButton("Yes");
        docBoardNoRadio = new JRadioButton("No");
        ButtonGroup certGroup = new ButtonGroup();
        certGroup.add(docBoardYesRadio);
        certGroup.add(docBoardNoRadio);
        certPanel.add(docBoardYesRadio);
        certPanel.add(docBoardNoRadio);
        contentPanel.add(certPanel, gbc);
        row++;
        
        // License Expiration
        gbc.gridy = row;
        gbc.gridx = 0;
        contentPanel.add(createLabel("License Expiration"), gbc);
        gbc.gridx = 1;
        docLicenseExpField = new JTextField(20);
        docLicenseExpField.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        contentPanel.add(docLicenseExpField, gbc);
        row++;
        
        // Section 3: Professional Skills
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JLabel section3Label = new JLabel("Section 3: Professional Skills");
        section3Label.setFont(new Font("Arial", Font.BOLD, 14));
        section3Label.setForeground(new Color(31, 41, 55));
        contentPanel.add(section3Label, gbc);
        row++;
        gbc.gridwidth = 1;
        
        // Skills Checkboxes
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JPanel skillsPanel = new JPanel(new GridLayout(0, 2, 10, 5));
        skillsPanel.setOpaque(false);
        
        String[] skills = {
            "Clinical Diagnostics", "Patient Assessment", "Surgical Skills",
            "Emergency Management", "Procedures", "Leadership",
            "Teamwork", "Critical Thinking"
        };
        
        for (String skill : skills) {
            JCheckBox skillCheck = new JCheckBox(skill);
            skillCheck.setBackground(Color.WHITE);
            skillsPanel.add(skillCheck);
            docSkillChecks.add(skillCheck);
        }
        
        contentPanel.add(skillsPanel, gbc);
        row++;
        
        // Section 4: Work Experience
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JLabel section4Label = new JLabel("Section 4: Work Experience");
        section4Label.setFont(new Font("Arial", Font.BOLD, 14));
        section4Label.setForeground(new Color(31, 41, 55));
        contentPanel.add(section4Label, gbc);
        row++;
        gbc.gridwidth = 1;
        
        // Years Experience
        gbc.gridy = row;
        gbc.gridx = 0;
        contentPanel.add(createLabel("Years Experience"), gbc);
        gbc.gridx = 1;
        yearsExperienceField = new JTextField(20);
        yearsExperienceField.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        contentPanel.add(yearsExperienceField, gbc);
        row++;
        
        // Years at Hospital
        gbc.gridy = row;
        gbc.gridx = 0;
        contentPanel.add(createLabel("Years at Hospital"), gbc);
        gbc.gridx = 1;
        yearsOfWorkField = new JTextField(20);
        yearsOfWorkField.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        contentPanel.add(yearsOfWorkField, gbc);
        row++;
        
        // Section 5: References
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JLabel section5Label = new JLabel("Section 5: Character References");
        section5Label.setFont(new Font("Arial", Font.BOLD, 14));
        section5Label.setForeground(new Color(31, 41, 55));
        contentPanel.add(section5Label, gbc);
        row++;
        gbc.gridwidth = 1;
        
        // Reference 1
        gbc.gridy = row;
        gbc.gridx = 0;
        contentPanel.add(createLabel("Reference 1"), gbc);
        gbc.gridx = 1;
        JTextField ref1Field = new JTextField(20);
        ref1Field.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        contentPanel.add(ref1Field, gbc);
        row++;
        
        // Reference 2
        gbc.gridy = row;
        gbc.gridx = 0;
        contentPanel.add(createLabel("Reference 2"), gbc);
        gbc.gridx = 1;
        JTextField ref2Field = new JTextField(20);
        ref2Field.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        contentPanel.add(ref2Field, gbc);
        row++;
        
        // Optional Reference 3
        gbc.gridy = row;
        gbc.gridx = 0;
        contentPanel.add(createLabel("Reference 3 (Optional)"), gbc);
        gbc.gridx = 1;
        JTextField ref3Field = new JTextField(20);
        ref3Field.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        contentPanel.add(ref3Field, gbc);
        row++;
        
        // Section 4: Educational Licenses & Registration
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JLabel eduLicensesLabel = new JLabel("Section 4: Educational Licenses & Registration");
        eduLicensesLabel.setFont(new Font("Arial", Font.BOLD, 14));
        eduLicensesLabel.setForeground(new Color(31, 41, 55));
        contentPanel.add(eduLicensesLabel, gbc);
        row++;
        gbc.gridwidth = 1;
        
        // Medical Education
        gbc.gridy = row;
        gbc.gridx = 0;
        contentPanel.add(createLabel("Medical Education"), gbc);
        gbc.gridx = 1;
        docMedicalEducationField = new JTextField(20);
        docMedicalEducationField.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        contentPanel.add(docMedicalEducationField, gbc);
        row++;
        
        // Board Exam
        gbc.gridy = row;
        gbc.gridx = 0;
        contentPanel.add(createLabel("Board Exam"), gbc);
        gbc.gridx = 1;
        docBoardExamField = new JTextField(20);
        docBoardExamField.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        contentPanel.add(docBoardExamField, gbc);
        row++;
        
        // PTR Number
        gbc.gridy = row;
        gbc.gridx = 0;
        contentPanel.add(createLabel("PTR Number"), gbc);
        gbc.gridx = 1;
        docPtrField = new JTextField(20);
        docPtrField.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        contentPanel.add(docPtrField, gbc);
        row++;
        
        // Registration ID
        gbc.gridy = row;
        gbc.gridx = 0;
        contentPanel.add(createLabel("Registration ID"), gbc);
        gbc.gridx = 1;
        docRegistrationIdField = new JTextField(20);
        docRegistrationIdField.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        contentPanel.add(docRegistrationIdField, gbc);
        row++;
        
        // Specialty Board
        gbc.gridy = row;
        gbc.gridx = 0;
        contentPanel.add(createLabel("Specialty Board"), gbc);
        gbc.gridx = 1;
        docSpecialtyBoardCombo = new JComboBox<>(new String[]{"None", "Internal Medicine", "Surgery", "Pediatrics", "OB-GYN", "Psychiatry"});
        docSpecialtyBoardCombo.setBackground(Color.WHITE);
        docSpecialtyBoardCombo.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        contentPanel.add(docSpecialtyBoardCombo, gbc);
        row++;
        
        // Professional Associations
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JPanel associationsPanel = new JPanel(new GridLayout(0, 2, 10, 5));
        associationsPanel.setOpaque(false);
        
        String[] associations = {"PMA", "PNA", "PCS", "Other"};
        for (String assoc : associations) {
            JCheckBox assocCheck = new JCheckBox(assoc);
            assocCheck.setBackground(Color.WHITE);
            associationsPanel.add(assocCheck);
        }
        
        contentPanel.add(associationsPanel, gbc);
        row++;
        gbc.gridwidth = 1;
        
        // Other Board
        gbc.gridy = row;
        gbc.gridx = 0;
        contentPanel.add(createLabel("Other Board"), gbc);
        gbc.gridx = 1;
        docOtherBoardField = new JTextField(20);
        docOtherBoardField.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        contentPanel.add(docOtherBoardField, gbc);
        row++;
        
        // Section 6: Skills & Competencies
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JLabel section6Label = new JLabel("Section 6: Skills & Competencies");
        section6Label.setFont(new Font("Arial", Font.BOLD, 14));
        section6Label.setForeground(new Color(31, 41, 55));
        contentPanel.add(section6Label, gbc);
        row++;
        gbc.gridwidth = 1;
        
        // Extended Skills Checkboxes
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JPanel extendedSkillsPanel = new JPanel(new GridLayout(0, 3, 8, 5));
        extendedSkillsPanel.setOpaque(false);
        
        String[] extendedSkills = {
            "Clinical Skills", "Diagnostics", "Teamwork",
            "Patient Assessment", "Leadership", "Emergency Care",
            "Critical Thinking", "Surgical Skills", "Procedures",
            "Specialization", "Research", "Teaching"
        };
        
        for (String skill : extendedSkills) {
            JCheckBox skillCheck = new JCheckBox(skill);
            skillCheck.setBackground(Color.WHITE);
            extendedSkillsPanel.add(skillCheck);
            docCompetencyChecks.add(skillCheck);
        }
        
        contentPanel.add(extendedSkillsPanel, gbc);
        row++;
        
        // Section 8: Professional Ancillary
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JLabel section8Label = new JLabel("Section 8: Professional Ancillary");
        section8Label.setFont(new Font("Arial", Font.BOLD, 14));
        section8Label.setForeground(new Color(31, 41, 55));
        contentPanel.add(section8Label, gbc);
        row++;
        gbc.gridwidth = 1;
        
        // PMA Association
        gbc.gridy = row;
        gbc.gridx = 0;
        contentPanel.add(createLabel("PMA Member"), gbc);
        gbc.gridx = 1;
        JCheckBox pmaCheck = new JCheckBox();
        pmaCheck.setBackground(Color.WHITE);
        contentPanel.add(pmaCheck, gbc);
        row++;
        
        // Specialty Societies
        gbc.gridy = row;
        gbc.gridx = 0;
        contentPanel.add(createLabel("Specialty Societies"), gbc);
        gbc.gridx = 1;
        JCheckBox specialtySocietiesCheck = new JCheckBox();
        specialtySocietiesCheck.setBackground(Color.WHITE);
        contentPanel.add(specialtySocietiesCheck, gbc);
        row++;
        
        // Specialty Societies Details
        gbc.gridy = row;
        gbc.gridx = 0;
        contentPanel.add(createLabel("Societies Details"), gbc);
        gbc.gridx = 1;
        JTextField societiesField = new JTextField(20);
        societiesField.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        contentPanel.add(societiesField, gbc);
        row++;
        
        // Membership
        gbc.gridy = row;
        gbc.gridx = 0;
        contentPanel.add(createLabel("Other Memberships"), gbc);
        gbc.gridx = 1;
        JTextField membershipField = new JTextField(20);
        membershipField.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        contentPanel.add(membershipField, gbc);
        row++;
        
        // Clinic Schedule Section
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JLabel scheduleLabel = new JLabel("Clinic Schedule");
        scheduleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        scheduleLabel.setForeground(new Color(31, 41, 55));
        contentPanel.add(scheduleLabel, gbc);
        row++;
        gbc.gridwidth = 1;
        
        // Initialize clinic schedule fields
        for (int i = 0; i < 7; i++) {
            dayCheckboxes[i] = new JCheckBox(daysOfWeek[i]);
            dayCheckboxes[i].setBackground(Color.WHITE);
            startTimeFields[i] = new JTextField(8);
            startTimeFields[i].setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
            endTimeFields[i] = new JTextField(8);
            endTimeFields[i].setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
            
            // Add to layout
            gbc.gridy = row;
            gbc.gridx = 0;
            contentPanel.add(dayCheckboxes[i], gbc);
            gbc.gridx = 1;
            JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            timePanel.setOpaque(false);
            timePanel.add(new JLabel("From:"));
            timePanel.add(startTimeFields[i]);
            timePanel.add(new JLabel("To:"));
            timePanel.add(endTimeFields[i]);
            contentPanel.add(timePanel, gbc);
            row++;
        }
        
        // Add content panel to scroll pane
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        return mainPanel;
    }
    
    private JPanel createSimplifiedStaffPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        
        // Create the content panel with all the fields
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.5;
        
        int row = 0;
        
        // Basic Information
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JLabel basicInfoLabel = new JLabel("Staff Information");
        basicInfoLabel.setFont(new Font("Arial", Font.BOLD, 14));
        basicInfoLabel.setForeground(new Color(31, 41, 55));
        contentPanel.add(basicInfoLabel, gbc);
        row++;
        gbc.gridwidth = 1;
        
        // Name
        gbc.gridy = row;
        gbc.gridx = 0;
        contentPanel.add(createLabel("Full Name *"), gbc);
        gbc.gridx = 1;
        staffNameField = new JTextField(20);
        staffNameField.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        contentPanel.add(staffNameField, gbc);
        row++;
        
        // Phone
        gbc.gridy = row;
        gbc.gridx = 0;
        contentPanel.add(createLabel("Phone Number *"), gbc);
        gbc.gridx = 1;
        staffPhoneField = new JTextField(20);
        staffPhoneField.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        contentPanel.add(staffPhoneField, gbc);
        row++;
        
        // Email
        gbc.gridy = row;
        gbc.gridx = 0;
        contentPanel.add(createLabel("Email Address *"), gbc);
        gbc.gridx = 1;
        staffEmailField = new JTextField(20);
        staffEmailField.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        contentPanel.add(staffEmailField, gbc);
        row++;
        
        // Address
        gbc.gridy = row;
        gbc.gridx = 0;
        contentPanel.add(createLabel("Address"), gbc);
        gbc.gridx = 1;
        staffAddressField = new JTextField(20);
        staffAddressField.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        contentPanel.add(staffAddressField, gbc);
        row++;
        
        // Department
        gbc.gridy = row;
        gbc.gridx = 0;
        contentPanel.add(createLabel("Department *"), gbc);
        gbc.gridx = 1;
        deptCombo = new JComboBox<>();
        deptCombo.setBackground(Color.WHITE);
        deptCombo.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        contentPanel.add(deptCombo, gbc);
        row++;
        
        // Role-specific fields
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        roleSpecificPanel = new JPanel(new GridBagLayout());
        roleSpecificPanel.setBackground(Color.WHITE);
        roleSpecificPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 230, 245), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        GridBagConstraints rpGbc = new GridBagConstraints();
        rpGbc.insets = new Insets(8, 8, 8, 8);
        rpGbc.fill = GridBagConstraints.HORIZONTAL;
        rpGbc.weightx = 0.5;
        
        // Nursing field
        rpGbc.gridy = 0;
        rpGbc.gridx = 0;
        JLabel nurseLabel = new JLabel("Nursing Field");
        nurseLabel.setFont(new Font("Arial", Font.BOLD, 11));
        nurseLabel.setForeground(new Color(60, 80, 120));
        roleSpecificPanel.add(nurseLabel, rpGbc);
        
        rpGbc.gridx = 1;
        nursingCombo = new JComboBox<>(new String[]{"ER", "ICU", "Ward", "Pediatric", "OB Ward"});
        nursingCombo.setBackground(Color.WHITE);
        nursingCombo.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        roleSpecificPanel.add(nursingCombo, rpGbc);
        
        // Initially hidden
        roleSpecificPanel.setVisible(false);
        
        contentPanel.add(roleSpecificPanel, gbc);
        
        // Add content panel to scroll pane
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        return mainPanel;
    }
    
    private JPanel createFooterPanel(JPanel formPanel) {
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(Color.WHITE);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Navigation buttons
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        navPanel.setOpaque(false);
        
        JButton backBtn = new JButton("← Back");
        backBtn.setFont(new Font("Arial", Font.PLAIN, 11));
        backBtn.setBackground(new Color(200, 200, 210));
        backBtn.setForeground(new Color(40, 40, 40));
        backBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.setVisible(false); // Initially hidden
        backBtn.addActionListener(e -> {
            CardLayout cl = (CardLayout) formPanel.getLayout();
            cl.show(formPanel, "role_selection");
            backBtn.setVisible(false);
        });
        
        JButton continueBtn = new JButton("Continue →");
        continueBtn.setFont(new Font("Arial", Font.BOLD, 11));
        continueBtn.setBackground(new Color(47, 111, 237));
        continueBtn.setForeground(Color.WHITE);
        continueBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        continueBtn.setFocusPainted(false);
        continueBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        continueBtn.addActionListener(e -> {
            String selectedRole = (String) roleCombo.getSelectedItem();
            if (selectedRole == null) {
                JOptionPane.showMessageDialog(this, "Please select a role", "Role Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            CardLayout cl = (CardLayout) formPanel.getLayout();
            
            if ("DOCTOR".equals(selectedRole)) {
                // Load doctor-specific departments
                deptCombo.setModel(new DefaultComboBoxModel<>(new String[]{"Cardiology", "Neurology", "Orthopedics", "Pediatrics", "Oncology", "ER"}));
                cl.show(formPanel, "doctor_form");
            } else {
                // Load role-specific departments
                String[] depts;
                if ("NURSE".equals(selectedRole)) {
                    depts = new String[]{"Nursing", "ER", "Pediatrics", "Oncology", "Admin"};
                    roleSpecificPanel.setVisible(true);
                    // Show nursing fields
                    for (Component comp : roleSpecificPanel.getComponents()) {
                        comp.setVisible(true);
                    }
                } else if ("CASHIER".equals(selectedRole)) {
                    depts = new String[]{"Billing", "Admin"};
                    roleSpecificPanel.setVisible(false);
                } else if ("FRONT_DESK".equals(selectedRole)) {
                    depts = new String[]{"Reception", "Admissions", "Patient Services", "Admin"};
                    roleSpecificPanel.setVisible(false);
                } else {
                    depts = new String[]{"Admin"};
                    roleSpecificPanel.setVisible(false);
                }
                deptCombo.setModel(new DefaultComboBoxModel<>(depts));
                cl.show(formPanel, "staff_form");
            }
            
            backBtn.setVisible(true);
        });
        
        navPanel.add(backBtn);
        navPanel.add(continueBtn);
        
        // Action buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setOpaque(false);
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Arial", Font.BOLD, 11));
        cancelBtn.setBackground(new Color(200, 200, 210));
        cancelBtn.setForeground(new Color(40, 40, 40));
        cancelBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        cancelBtn.setFocusPainted(false);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> dispose());
        
        JButton registerBtn = new JButton("Register Staff");
        registerBtn.setFont(new Font("Arial", Font.BOLD, 11));
        registerBtn.setBackground(new Color(34, 197, 94));
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        registerBtn.setFocusPainted(false);
        registerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerBtn.addActionListener(e -> registerStaff());
        
        actionPanel.add(cancelBtn);
        actionPanel.add(registerBtn);
        
        footerPanel.add(navPanel, BorderLayout.WEST);
        footerPanel.add(actionPanel, BorderLayout.EAST);
        
        return footerPanel;
    }
    
        
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 11));
        label.setForeground(new Color(60, 80, 120));
        return label;
    }
    
    private void registerStaff() {
        // Validate required fields
        // Get role first
        String role = (String) roleCombo.getSelectedItem();
        
        // Get correct name field based on role
        JTextField activeNameField = "DOCTOR".equals(role) ? doctorNameField : staffNameField;
        
        // Get correct phone field based on role
        JTextField activePhoneField = "DOCTOR".equals(role) ? doctorPhoneField : staffPhoneField;
        
        // Get correct email field based on role
        JTextField activeEmailField = "DOCTOR".equals(role) ? doctorEmailField : staffEmailField;
        
        // Get correct address field based on role
        JTextField activeAddressField = "DOCTOR".equals(role) ? doctorAddressField : staffAddressField;
        
        String name = activeNameField.getText().trim();
        String phone = activePhoneField.getText().trim();
        String email = activeEmailField.getText().trim();
        String license = licenseField != null ? licenseField.getText().trim() : "";
        String dept = (String) deptCombo.getSelectedItem();
        String address = activeAddressField != null ? activeAddressField.getText().trim() : "";

        // Debug: Print field values to diagnose the issue
        System.out.println("DEBUG - Role: " + role);
        System.out.println("DEBUG - Name field value: '" + name + "'");
        System.out.println("DEBUG - Name field is null: " + (activeNameField == null));
        System.out.println("DEBUG - Name field text: " + (activeNameField != null ? "'" + activeNameField.getText() + "'" : "N/A"));
        System.out.println("DEBUG - Name field text length: " + (activeNameField != null ? activeNameField.getText().length() : "N/A"));

        // Enhanced validation for name field
        String rawName = activeNameField.getText();
        if (rawName == null) {
            JOptionPane.showMessageDialog(this, "Name is required", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        name = rawName.trim();
        
        // Check if name is empty after trimming
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name is required", "Validation Error", JOptionPane.WARNING_MESSAGE);
            activeNameField.requestFocus();
            return;
        }
        
        // Additional validation: ensure name contains at least one letter
        if (!name.matches(".*[a-zA-Z]+.*")) {
            JOptionPane.showMessageDialog(this, "Name must contain at least one letter", "Validation Error", JOptionPane.WARNING_MESSAGE);
            activeNameField.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Phone is required", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validate phone number (10 digits only)
        if (!hpms.util.Validators.isValidPhoneNumber(phone)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid 10-digit phone number", "Phone Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Email is required", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!hpms.util.Validators.isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address", "Email Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // License validation for doctors
        if ("DOCTOR".equals(role) && license.isEmpty()) {
            JOptionPane.showMessageDialog(this, "License Number is required for doctors", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Create Staff object
        Staff staff = new Staff(null, name, StaffRole.valueOf(role), dept, LocalDateTime.now());
        staff.phone = phone;
        staff.email = email;
        staff.address = address.isEmpty() ? null : address;
        staff.licenseNumber = license.isEmpty() ? null : license;
        staff.isAvailable = true;

        // Set years of experience if provided
        if (yearsExperienceField != null) {
            String yearsExpText = yearsExperienceField.getText().trim();
            if (!yearsExpText.isEmpty()) {
                try {
                    staff.yearsExperience = Integer.parseInt(yearsExpText);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Years Experience must be a number", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
        }

        // Set years at hospital if provided
        if (yearsOfWorkField != null) {
            String yearsWorkText = yearsOfWorkField.getText().trim();
            if (!yearsWorkText.isEmpty()) {
                try {
                    staff.yearsOfWork = Integer.parseInt(yearsWorkText);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Years at Hospital must be a number", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
        }

        // Set specialization for doctors/nurses
        if ("DOCTOR".equals(role) && specCombo != null && specCombo.getSelectedItem() != null) {
            staff.specialty = specCombo.getSelectedItem().toString();
        } else if ("NURSE".equals(role) && nursingCombo != null && nursingCombo.getSelectedItem() != null) {
            staff.specialty = nursingCombo.getSelectedItem().toString();
        }

        // Set clinic schedule for doctors
        if ("DOCTOR".equals(role) && dayCheckboxes != null) {
            for (int i = 0; i < 7; i++) {
                String startTime = startTimeFields[i].getText().trim();
                String endTime = endTimeFields[i].getText().trim();
                boolean isActive = dayCheckboxes[i].isSelected();
                
                // Validate time format (HH:mm)
                if (isActive && (!startTime.matches("^([0-1][0-9]|2[0-3]):[0-5][0-9]$") || 
                    !endTime.matches("^([0-1][0-9]|2[0-3]):[0-5][0-9]$"))) {
                    JOptionPane.showMessageDialog(this, "Invalid time format. Use HH:mm (e.g., 08:00)", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                staff.clinicSchedule.put(daysOfWeek[i], new Staff.ScheduleEntry(isActive, startTime, endTime));
            }
        }

        // Add to DataStore using StaffService
        java.util.List<String> staffResult = StaffService.add(
            staff.name,
            staff.role.toString(),
            staff.department,
            staff.specialty,
            staff.phone,
            staff.email,
            staff.licenseNumber,
            staff.qualifications,
            "Added via Unified Registration"
        );
        
        String actualStaffId = null;
        if (!staffResult.isEmpty() && staffResult.get(0).startsWith("Staff added")) {
            actualStaffId = staffResult.get(0).substring("Staff added ".length()).trim();
        }

        if (actualStaffId != null) {
            if ("DOCTOR".equals(role)) {
                Staff saved = hpms.util.DataStore.staff.get(actualStaffId);
                if (saved != null) {
                    saved.certifications = buildDoctorCertifications();
                    saved.education = buildDoctorEducation();
                    saved.expertise = buildDoctorExpertise();
                    saved.skills = joinSelected(docSkillChecks);
                    saved.competencies = joinSelected(docCompetencyChecks);
                    try {
                        StaffService.updateStaff(saved);
                    } catch (Exception ignored) {
                    }
                }
            }
            // Create user account
            String code = AuthService.generateRandomPasswordForUI();
            java.util.List<String> result = AuthService.register(actualStaffId, code, role);
            if (result != null && !result.isEmpty() && result.get(0).startsWith("User registered")) {
                AuthService.changePasswordNoOld(actualStaffId, code);
                JOptionPane.showMessageDialog(this,
                        "Staff registered successfully!\n\n" +
                                "Staff ID: " + actualStaffId + "\n" +
                                "Name: " + staff.name + "\n" +
                                "Role: " + role + "\n" +
                                "Department: " + dept + "\n" +
                                "6-Digit Login Code: " + code + "\n\n" +
                                "Please save this information securely.",
                        "Registration Success",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Staff registered but account creation failed.\n" +
                                "Staff ID: " + actualStaffId,
                        "Partial Success",
                        JOptionPane.WARNING_MESSAGE);
                dispose();
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to register staff. Please try again.",
                    "Registration Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private String joinSelected(java.util.List<JCheckBox> checks) {
        if (checks == null || checks.isEmpty()) return null;
        java.util.List<String> out = new java.util.ArrayList<>();
        for (JCheckBox c : checks) {
            if (c != null && c.isSelected() && c.getText() != null && !c.getText().trim().isEmpty()) {
                out.add(c.getText().trim());
            }
        }
        return out.isEmpty() ? null : String.join(", ", out);
    }

    private String buildDoctorCertifications() {
        java.util.List<String> parts = new java.util.ArrayList<>();
        if (docBoardYesRadio != null && docBoardYesRadio.isSelected()) {
            parts.add("Board Certified: Yes");
        } else if (docBoardNoRadio != null && docBoardNoRadio.isSelected()) {
            parts.add("Board Certified: No");
        }
        if (docSpecialtyBoardCombo != null && docSpecialtyBoardCombo.getSelectedItem() != null) {
            String v = String.valueOf(docSpecialtyBoardCombo.getSelectedItem()).trim();
            if (!v.isEmpty() && !"None".equalsIgnoreCase(v)) {
                parts.add("Specialty Board: " + v);
            }
        }
        if (docOtherBoardField != null && docOtherBoardField.getText() != null) {
            String v = docOtherBoardField.getText().trim();
            if (!v.isEmpty()) {
                parts.add("Other Board: " + v);
            }
        }
        return parts.isEmpty() ? null : String.join("\n", parts);
    }

    private String buildDoctorEducation() {
        java.util.List<String> parts = new java.util.ArrayList<>();
        if (docMedSchoolField != null && docMedSchoolField.getText() != null) {
            String v = docMedSchoolField.getText().trim();
            if (!v.isEmpty()) parts.add("Medical School: " + v);
        }
        if (docYearGradField != null && docYearGradField.getText() != null) {
            String v = docYearGradField.getText().trim();
            if (!v.isEmpty()) parts.add("Year Graduated: " + v);
        }
        if (docResidencyField != null && docResidencyField.getText() != null) {
            String v = docResidencyField.getText().trim();
            if (!v.isEmpty()) parts.add("Residency: " + v);
        }
        if (docFellowshipField != null && docFellowshipField.getText() != null) {
            String v = docFellowshipField.getText().trim();
            if (!v.isEmpty()) parts.add("Fellowship: " + v);
        }
        if (docMedicalEducationField != null && docMedicalEducationField.getText() != null) {
            String v = docMedicalEducationField.getText().trim();
            if (!v.isEmpty()) parts.add("Medical Education: " + v);
        }
        if (docBoardExamField != null && docBoardExamField.getText() != null) {
            String v = docBoardExamField.getText().trim();
            if (!v.isEmpty()) parts.add("Board Exam: " + v);
        }
        if (docPtrField != null && docPtrField.getText() != null) {
            String v = docPtrField.getText().trim();
            if (!v.isEmpty()) parts.add("PTR Number: " + v);
        }
        if (docRegistrationIdField != null && docRegistrationIdField.getText() != null) {
            String v = docRegistrationIdField.getText().trim();
            if (!v.isEmpty()) parts.add("Registration ID: " + v);
        }
        return parts.isEmpty() ? null : String.join("\n", parts);
    }

    private String buildDoctorExpertise() {
        java.util.List<String> parts = new java.util.ArrayList<>();
        if (yearsExperienceField != null && yearsExperienceField.getText() != null) {
            String v = yearsExperienceField.getText().trim();
            if (!v.isEmpty()) parts.add("Years Experience: " + v);
        }
        if (yearsOfWorkField != null && yearsOfWorkField.getText() != null) {
            String v = yearsOfWorkField.getText().trim();
            if (!v.isEmpty()) parts.add("Years at Hospital: " + v);
        }
        if (docLicenseExpField != null && docLicenseExpField.getText() != null) {
            String v = docLicenseExpField.getText().trim();
            if (!v.isEmpty()) parts.add("License Expiration: " + v);
        }
        return parts.isEmpty() ? null : String.join("\n", parts);
    }
}
