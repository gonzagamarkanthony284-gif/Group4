package hpms.ui.panels;

import hpms.model.*;
import hpms.service.*;
import hpms.util.*;
import hpms.ui.components.CardPanel;
import hpms.ui.PatientDetailsDialog;
import hpms.ui.PatientDetailsDialogNew;
import hpms.ui.components.IconButton;
import hpms.ui.components.SectionHeader;
import hpms.ui.components.Theme;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ItemEvent;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class PatientsPanel extends JPanel {
    // Show room, status and patient type (registration type) instead of
    // contact/address so staff sees important info at a glance
    private final DefaultTableModel patientsModel = new DefaultTableModel(
            new String[] { "ID", "Name", "Age", "Gender", "Room", "Status", "Type" }, 0) {
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private JLabel nameLabel, ageGenderLabel, contactLabel, roomLabel, doctorLabel, lastVisitLabel;
    private JLabel allergiesLabel, registrationLabel, vitalsLabel;
    private JLabel statusValueLabel;
    private JTextArea notesArea;
    private JTextField searchField, minAgeField, maxAgeField;
    private JComboBox<String> genderFilter;
    private JCheckBox assignedCheck, notAssignedCheck, recentCheck, showInactiveCheck;
    private IconButton transferBtn; // Store for enabling/disabling based on patient status
    private JComboBox<String> statusCombo;
    // Date range filter components
    private JComboBox<String> dateRangeFilter;
    private JTextField customStartDate, customEndDate;
    private JPanel customDatePanel;
    private JTable table;
    // pagination
    private int pageSize = 20;
    private int currentPage = 1;
    private JButton prevPage, nextPage;
    private JLabel pageInfo;
    // Category filtering (NEW)
    private PatientStatus selectedCategory = null; // null means show all, otherwise filter by category

    public PatientsPanel() {
        setLayout(new BorderLayout(12, 12));
        setBackground(Theme.BG);
        add(SectionHeader.info("Patient Management", "Add, search, and update patient records with quick actions."),
                BorderLayout.NORTH);

        // top area with two columns: left = patient info card, right = status/notes
        // card
        JPanel topWrap = new JPanel(new BorderLayout(12, 12));
        topWrap.setOpaque(false);
        JPanel topRow = new JPanel(new GridLayout(1, 2, 12, 12));
        topRow.setOpaque(false);

        CardPanel infoCard = new CardPanel();
        infoCard.setLayout(new BorderLayout());
        JPanel infoInner = new JPanel(new GridLayout(0, 2, 8, 8));
        infoInner.setOpaque(false);
        nameLabel = labelField("Name");
        ageGenderLabel = labelField("Age/Gender");
        contactLabel = labelField("Contact");
        roomLabel = labelField("Room");
        doctorLabel = labelField("Doctor");
        lastVisitLabel = labelField("Last Visit");
        allergiesLabel = labelField("Allergies");
        registrationLabel = labelField("Registration");
        vitalsLabel = labelField("Vitals");
        infoInner.add(nameLabel);
        infoInner.add(ageGenderLabel);
        infoInner.add(contactLabel);
        infoInner.add(roomLabel);
        infoInner.add(doctorLabel);
        infoInner.add(lastVisitLabel);
        infoInner.add(allergiesLabel);
        infoInner.add(registrationLabel);
        infoInner.add(vitalsLabel);
        infoCard.add(infoInner, BorderLayout.CENTER);
        topRow.add(infoCard);

        CardPanel statusCard = new CardPanel();
        statusCard.setLayout(new BorderLayout());
        JPanel statusTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusTop.setOpaque(false);
        statusValueLabel = new JLabel();
        styleBadge(statusValueLabel, "OUTPATIENT");
        statusCombo = new JComboBox<>(new String[] { "OUTPATIENT", "INPATIENT", "EMERGENCY", "DISCHARGED" });
        JButton statusApply = new JButton("Update");
        JButton statusHistoryBtn = new JButton("History");
        statusTop.add(statusValueLabel);
        statusTop.add(statusCombo);
        statusTop.add(statusApply);
        statusTop.add(statusHistoryBtn);
        statusCard.add(statusTop, BorderLayout.NORTH);
        notesArea = new JTextArea(4, 30);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setEditable(false);
        notesArea.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        JPanel notesWrap = new JPanel(new BorderLayout());
        notesWrap.setOpaque(false);
        JButton toggleNotes = new JButton("Show/Hide Notes");
        notesWrap.add(toggleNotes, BorderLayout.NORTH);
        notesWrap.add(new JScrollPane(notesArea), BorderLayout.CENTER);
        statusCard.add(notesWrap, BorderLayout.CENTER);
        topRow.add(statusCard);

        topWrap.add(topRow, BorderLayout.CENTER);

        // actions row
        JPanel actionsRow = new JPanel(new BorderLayout());
        actionsRow.setOpaque(false);
        JPanel actionButtons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        actionButtons.setOpaque(false);
        IconButton viewInfo = new IconButton("View Info", UIManager.getIcon("FileView.fileIcon"));
        transferBtn = new IconButton("Transfer", UIManager.getIcon("FileView.directoryIcon"));
        IconButton consult = new IconButton("Consult", UIManager.getIcon("FileView.fileIcon"));
        IconButton addAlert = new IconButton("Add Note", UIManager.getIcon("OptionPane.warningIcon"));
        IconButton print = new IconButton("Print", UIManager.getIcon("FileView.hardDriveIcon"));
        actionButtons.add(viewInfo);
        actionButtons.add(transferBtn);
        actionButtons.add(consult);
        actionButtons.add(addAlert);
        actionButtons.add(print);
        JPanel clinicianButtons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        clinicianButtons.setOpaque(false);
        IconButton clinicalBtn = new IconButton("Clinical", UIManager.getIcon("FileView.floppyDriveIcon"));
        if (hpms.auth.AuthService.current != null && (hpms.auth.AuthService.current.role == UserRole.DOCTOR
                || hpms.auth.AuthService.current.role == UserRole.NURSE
                || hpms.auth.AuthService.current.role == UserRole.ADMIN)) {
            clinicianButtons.add(clinicalBtn);
        }
        // wrap both groups in a centered container so icon+label buttons align in the
        // middle
        JPanel actionsWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 6));
        actionsWrap.setOpaque(false);
        actionsWrap.add(actionButtons);
        actionsWrap.add(clinicianButtons);
        actionsRow.add(actionsWrap, BorderLayout.CENTER);
        topWrap.add(actionsRow, BorderLayout.SOUTH);

        add(topWrap, BorderLayout.NORTH);

        // center: filters (collapsible) and table area
        JSplitPane centerSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        centerSplit.setResizeWeight(0.0);
        centerSplit.setOpaque(false);
        // filters as collapsible panel
        CardPanel filtersCard = new CardPanel();
        filtersCard.setLayout(new BorderLayout());
        JPanel filtersInner = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtersInner.setOpaque(false);
        filtersInner.add(new JLabel("Search:"));
        searchField = new JTextField(18);
        filtersInner.add(searchField);
        JButton searchBtn = new JButton("Search");
        filtersInner.add(searchBtn);
        genderFilter = new JComboBox<>(new String[] { "All", "Male", "Female", "Other" });
        filtersInner.add(new JLabel("Gender"));
        filtersInner.add(genderFilter);
        minAgeField = new JTextField(3);
        maxAgeField = new JTextField(3);
        filtersInner.add(new JLabel("Age"));
        filtersInner.add(minAgeField);
        filtersInner.add(new JLabel("-"));
        filtersInner.add(maxAgeField);
        assignedCheck = new JCheckBox("Assigned Room");
        notAssignedCheck = new JCheckBox("Not Assigned Room");
        recentCheck = new JCheckBox("Recent Visits (30d)");
        showInactiveCheck = new JCheckBox("Show Inactive");
        showInactiveCheck.setForeground(new Color(200, 50, 50)); // Red color to indicate special filter
        filtersInner.add(assignedCheck);
        filtersInner.add(notAssignedCheck);
        filtersInner.add(recentCheck);
        filtersInner.add(showInactiveCheck);

        // Date range filter
        filtersInner.add(new JLabel("Registration Date:"));
        dateRangeFilter = new JComboBox<>(new String[] { "All Time", "Last 3 Weeks", "Last Month", "Custom Range" });
        filtersInner.add(dateRangeFilter);

        // Custom date range fields (initially hidden)
        customDatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
        customDatePanel.setOpaque(false);
        customDatePanel.add(new JLabel("From:"));
        customStartDate = new JTextField(10);
        customStartDate.setToolTipText("YYYY-MM-DD");
        customDatePanel.add(customStartDate);
        customDatePanel.add(new JLabel("To:"));
        customEndDate = new JTextField(10);
        customEndDate.setToolTipText("YYYY-MM-DD");
        customDatePanel.add(customEndDate);
        customDatePanel.setVisible(false);
        filtersInner.add(customDatePanel);

        JButton applyFilters = new JButton("Apply Filters");
        filtersInner.add(applyFilters);
        filtersCard.add(filtersInner, BorderLayout.CENTER);
        centerSplit.setTopComponent(filtersCard);

        // Category filter buttons (NEW) - separate section for patient categories
        JPanel categoryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        categoryPanel.setBackground(Theme.BG);
        categoryPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 1, 0, Theme.BORDER),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));

        JLabel categoryLabel = new JLabel("Patient Category:");
        categoryLabel.setFont(new Font("Arial", Font.BOLD, 12));
        categoryPanel.add(categoryLabel);

        // Create category buttons
        JButton allBtn = new JButton("All Patients");
        JButton outpatientBtn = new JButton("Outpatient");
        JButton inpatientBtn = new JButton("Inpatient");
        JButton emergencyBtn = new JButton("Emergency");
        JButton dischargedBtn = new JButton("Discharged");

        // Style selected button
        allBtn.setBackground(new Color(0, 102, 102));
        allBtn.setForeground(Color.WHITE);
        allBtn.setFocusPainted(false);

        // Action listeners for category buttons
        allBtn.addActionListener(e -> {
            selectedCategory = null;
            currentPage = 1;
            applyPatientFilter();
            // Update button styles
            allBtn.setBackground(new Color(0, 102, 102));
            allBtn.setForeground(Color.WHITE);
            outpatientBtn.setBackground(UIManager.getColor("Button.background"));
            outpatientBtn.setForeground(UIManager.getColor("Button.foreground"));
            inpatientBtn.setBackground(UIManager.getColor("Button.background"));
            inpatientBtn.setForeground(UIManager.getColor("Button.foreground"));
            emergencyBtn.setBackground(UIManager.getColor("Button.background"));
            emergencyBtn.setForeground(UIManager.getColor("Button.foreground"));
            dischargedBtn.setBackground(UIManager.getColor("Button.background"));
            dischargedBtn.setForeground(UIManager.getColor("Button.foreground"));
        });

        outpatientBtn.addActionListener(e -> {
            selectedCategory = PatientStatus.OUTPATIENT;
            currentPage = 1;
            applyPatientFilter();
            allBtn.setBackground(UIManager.getColor("Button.background"));
            allBtn.setForeground(UIManager.getColor("Button.foreground"));
            outpatientBtn.setBackground(new Color(0, 102, 102));
            outpatientBtn.setForeground(Color.WHITE);
            inpatientBtn.setBackground(UIManager.getColor("Button.background"));
            inpatientBtn.setForeground(UIManager.getColor("Button.foreground"));
            emergencyBtn.setBackground(UIManager.getColor("Button.background"));
            emergencyBtn.setForeground(UIManager.getColor("Button.foreground"));
            dischargedBtn.setBackground(UIManager.getColor("Button.background"));
            dischargedBtn.setForeground(UIManager.getColor("Button.foreground"));
        });

        inpatientBtn.addActionListener(e -> {
            selectedCategory = PatientStatus.INPATIENT;
            currentPage = 1;
            applyPatientFilter();
            allBtn.setBackground(UIManager.getColor("Button.background"));
            allBtn.setForeground(UIManager.getColor("Button.foreground"));
            outpatientBtn.setBackground(UIManager.getColor("Button.background"));
            outpatientBtn.setForeground(UIManager.getColor("Button.foreground"));
            inpatientBtn.setBackground(new Color(0, 102, 102));
            inpatientBtn.setForeground(Color.WHITE);
            emergencyBtn.setBackground(UIManager.getColor("Button.background"));
            emergencyBtn.setForeground(UIManager.getColor("Button.foreground"));
            dischargedBtn.setBackground(UIManager.getColor("Button.background"));
            dischargedBtn.setForeground(UIManager.getColor("Button.foreground"));
        });

        emergencyBtn.addActionListener(e -> {
            selectedCategory = PatientStatus.EMERGENCY;
            currentPage = 1;
            applyPatientFilter();
            allBtn.setBackground(UIManager.getColor("Button.background"));
            allBtn.setForeground(UIManager.getColor("Button.foreground"));
            outpatientBtn.setBackground(UIManager.getColor("Button.background"));
            outpatientBtn.setForeground(UIManager.getColor("Button.foreground"));
            inpatientBtn.setBackground(UIManager.getColor("Button.background"));
            inpatientBtn.setForeground(UIManager.getColor("Button.foreground"));
            emergencyBtn.setBackground(new Color(0, 102, 102));
            emergencyBtn.setForeground(Color.WHITE);
            dischargedBtn.setBackground(UIManager.getColor("Button.background"));
            dischargedBtn.setForeground(UIManager.getColor("Button.foreground"));
        });

        dischargedBtn.addActionListener(e -> {
            selectedCategory = PatientStatus.DISCHARGED;
            currentPage = 1;
            applyPatientFilter();
            allBtn.setBackground(UIManager.getColor("Button.background"));
            allBtn.setForeground(UIManager.getColor("Button.foreground"));
            outpatientBtn.setBackground(UIManager.getColor("Button.background"));
            outpatientBtn.setForeground(UIManager.getColor("Button.foreground"));
            inpatientBtn.setBackground(UIManager.getColor("Button.background"));
            inpatientBtn.setForeground(UIManager.getColor("Button.foreground"));
            emergencyBtn.setBackground(UIManager.getColor("Button.background"));
            emergencyBtn.setForeground(UIManager.getColor("Button.foreground"));
            dischargedBtn.setBackground(new Color(0, 102, 102));
            dischargedBtn.setForeground(Color.WHITE);
        });

        categoryPanel.add(allBtn);
        categoryPanel.add(outpatientBtn);
        categoryPanel.add(inpatientBtn);
        categoryPanel.add(emergencyBtn);
        categoryPanel.add(dischargedBtn);

        JSplitPane centerSplit2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        centerSplit2.setResizeWeight(0.0);
        centerSplit2.setOpaque(false);
        centerSplit2.setTopComponent(categoryPanel);

        // table area
        table = new JTable(patientsModel);
        // zebra striping and consistent foreground handling so text never disappears
        table.setDefaultRenderer(Object.class, new TableCellRenderer() {
            private final DefaultTableCellRenderer DEFAULT = new DefaultTableCellRenderer();

            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = DEFAULT.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                // Ensure cell renderer is opaque so background shows
                if (c instanceof JComponent)
                    ((JComponent) c).setOpaque(true);
                if (isSelected) {
                    c.setBackground(Theme.SELECTED_BACKGROUND);
                    c.setForeground(Theme.SELECTED_FOREGROUND);
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 246, 248));
                    // always ensure non-selected rows use readable app text color
                    c.setForeground(Theme.FOREGROUND);
                }
                return c;
            }
        });
        table.setRowSorter(new TableRowSorter<>(patientsModel));
        // Make selection visuals explicit in case UI themes toggle when switching views
        table.setSelectionBackground(Theme.SELECTED_BACKGROUND);
        table.setSelectionForeground(Theme.SELECTED_FOREGROUND);
        table.setGridColor(Theme.BORDER);
        // Style header about the same time
        Theme.styleTableHeader(table.getTableHeader());
        JScrollPane tableScroll = new JScrollPane(table);

        // table top controls (search + Add/Edit/Delete top-right)
        JPanel tableTop = new JPanel(new BorderLayout());
        tableTop.setOpaque(false);
        // table-level search control â€” use its own JTextField (keeps layout stable).
        JPanel tableTopLeft = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tableTopLeft.setOpaque(false);
        JPanel tableTopRight = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        tableTopRight.setOpaque(false);
        JButton add = new JButton("Add");
        JButton edit = new JButton("Edit");
        JButton del = new JButton("Delete");
        tableTopRight.add(add);
        tableTopRight.add(edit);
        tableTopRight.add(del);
        tableTop.add(tableTopLeft, BorderLayout.WEST);
        tableTop.add(tableTopRight, BorderLayout.EAST);

        JPanel tableArea = new JPanel(new BorderLayout());
        tableArea.setOpaque(false);
        tableArea.add(tableTop, BorderLayout.NORTH);
        tableArea.add(tableScroll, BorderLayout.CENTER);

        // pagination controls
        JPanel pager = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pager.setOpaque(false);
        prevPage = new JButton("Prev");
        nextPage = new JButton("Next");
        pageInfo = new JLabel("Page 1");
        pager.add(prevPage);
        pager.add(pageInfo);
        pager.add(nextPage);
        tableArea.add(pager, BorderLayout.SOUTH);

        // Complete the split pane hierarchy: Filters â†’ Category â†’ Table
        centerSplit.setBottomComponent(categoryPanel);
        centerSplit2.setBottomComponent(tableArea);

        JSplitPane outerSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        outerSplit.setResizeWeight(0.0);
        outerSplit.setOpaque(false);
        outerSplit.setTopComponent(centerSplit);
        outerSplit.setBottomComponent(centerSplit2);

        add(outerSplit, BorderLayout.CENTER);

        // If this panel becomes visible again (user navigates away and back), ensure
        // the data is refreshed.
        this.addHierarchyListener(evt -> {
            // HIERARCHY_CHANGED with SHOWING_CHANGED bit implies visibility changed
            if ((evt.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0) {
                if (this.isShowing()) {
                    // run later on EDT to allow layout to stabilize
                    SwingUtilities.invokeLater(() -> {
                        // Defensive: if DataStore has items but the table is empty, refresh
                        if (!DataStore.patients.isEmpty() && patientsModel.getRowCount() == 0) {
                            applyPatientFilter();
                        } else {
                            // always attempt a refresh to keep state consistent
                            applyPatientFilter();
                        }
                    });
                }
            }
        });

        // wire actions (reuse many existing behaviors)
        add.addActionListener(e -> addPatientDialog());
        edit.addActionListener(e -> editPatient(table));
        del.addActionListener(e -> deletePatient(table));
        searchBtn.addActionListener(e -> {
            currentPage = 1;
            applyPatientFilter();
        });
        // Keep both search boxes synchronized (filters area + table top).
        // applyPatientFilter reads `searchField`.
        searchField.addActionListener(e -> {
            currentPage = 1;
            applyPatientFilter();
        });

        // NOTE: We intentionally keep *two separate* search text fields here:
        // - `searchField` is part of the top filter area (collapsible) and is used by
        // the global filtering logic.
        // - `tableSearchField` is a separate search field placed with the table
        // controls.
        //
        // We cannot reuse the same JTextField instance in both containers because
        // re-parenting
        // a component in Swing removes it from its previous container. Re-using a
        // single
        // instance would cause it to be removed from one area when added to the other,
        // which leads to blank UIs and hard-to-debug visuals when switching views.
        //
        // Instead we create two text fields and keep them synchronized (text + events).
        // applyPatientFilter() reads the `searchField` value as the primary source.
        // `tableSearchField` updates `searchField` on user input (and vice-versa),
        // giving
        // the user a consistent experience without risking component reparenting.
        // Live-sync both search boxes (keep them identical and filter as user types)
        javax.swing.event.DocumentListener syncSearch = new javax.swing.event.DocumentListener() {
            private void doSync(javax.swing.event.DocumentEvent e) {
                try {
                    currentPage = 1;
                    applyPatientFilter();
                } catch (Exception ex) {
                }
            }

            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                doSync(e);
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                doSync(e);
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                doSync(e);
            }
        };
        searchField.getDocument().addDocumentListener(syncSearch);
        searchField.getDocument().addDocumentListener(syncSearch);
        applyFilters.addActionListener(e -> {
            currentPage = 1;
            applyPatientFilter();
        });
        genderFilter.addActionListener(e -> {
            currentPage = 1;
            applyPatientFilter();
        });
        recentCheck.addActionListener(e -> {
            currentPage = 1;
            applyPatientFilter();
        });
        assignedCheck.addActionListener(e -> {
            currentPage = 1;
            applyPatientFilter();
        });
        notAssignedCheck.addActionListener(e -> {
            currentPage = 1;
            applyPatientFilter();
        });
        showInactiveCheck.addActionListener(e -> {
            currentPage = 1;
            applyPatientFilter();
        });
        dateRangeFilter.addActionListener(e -> {
            String selected = String.valueOf(dateRangeFilter.getSelectedItem());
            customDatePanel.setVisible("Custom Range".equals(selected));
            currentPage = 1;
            applyPatientFilter();
        });
        // make age filters apply immediately as the user types
        javax.swing.event.DocumentListener ageListener = new javax.swing.event.DocumentListener() {
            private void d() {
                currentPage = 1;
                applyPatientFilter();
            }

            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                d();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                d();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                d();
            }
        };
        minAgeField.getDocument().addDocumentListener(ageListener);
        maxAgeField.getDocument().addDocumentListener(ageListener);

        statusApply.addActionListener(e -> {
            int i = table.getSelectedRow();
            if (i < 0)
                return;
            String id = String.valueOf(table.getValueAt(i, 0));
            List<String> out = PatientStatusService.setStatus(id, String.valueOf(statusCombo.getSelectedItem()),
                    hpms.auth.AuthService.current == null ? "" : hpms.auth.AuthService.current.username, "");
            showOut(out);
            if (String.valueOf(statusCombo.getSelectedItem()).equals("EMERGENCY")) {
                CommunicationService.addAlert(id, "Status set to EMERGENCY");
            }
            updateOverviewFromSelection(table);
        });
        statusHistoryBtn.addActionListener(e -> {
            int i = table.getSelectedRow();
            if (i < 0)
                return;
            String id = String.valueOf(table.getValueAt(i, 0));
            DefaultTableModel m = new DefaultTableModel(new String[] { "Time", "Status", "By", "Note" }, 0) {
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };
            for (StatusHistoryEntry h : PatientStatusService.history(id))
                m.addRow(new Object[] { h.at, h.status, h.byStaffId, h.note });
            JTable ht = new JTable(m);
            JOptionPane.showMessageDialog(this, new JScrollPane(ht));
        });

        viewInfo.addActionListener(e -> {
            int i = table.getSelectedRow();
            if (i < 0) {
                JOptionPane.showMessageDialog(this, "Select a patient first to view details.", "No patient selected",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            String id = String.valueOf(table.getValueAt(i, 0));
            showPatientDetailsDialog(id);
        });

        transferBtn.addActionListener(e -> {
            int i = table.getSelectedRow();
            if (i < 0) {
                JOptionPane.showMessageDialog(this, "Select a patient first to transfer.", "No patient selected",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            String id = String.valueOf(table.getValueAt(i, 0));
            // Check if patient is inpatient
            PatientStatus status = PatientStatusService.getStatus(id);
            if (status != PatientStatus.INPATIENT) {
                JOptionPane.showMessageDialog(this,
                        "Only inpatients can be assigned to rooms.\nCurrent status: " + status, "Invalid Status",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            java.util.List<String> opts = new java.util.ArrayList<>();
            for (Room x : DataStore.rooms.values())
                if (x.status == RoomStatus.VACANT)
                    opts.add(x.id);
            JComboBox<String> rid = new JComboBox<>(opts.toArray(new String[0]));
            int r = JOptionPane.showConfirmDialog(this, new Object[] { "Room", rid }, "Transfer Patient",
                    JOptionPane.OK_CANCEL_OPTION);
            if (r == JOptionPane.OK_OPTION) {
                showOut(RoomService.assign(String.valueOf(rid.getSelectedItem()), id));
                updateOverviewFromSelection(table);
            }
        });

        consult.addActionListener(e -> {
            int i = table.getSelectedRow();
            if (i < 0) {
                JOptionPane.showMessageDialog(this, "Select a patient first to request consultation.",
                        "No patient selected", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String id = String.valueOf(table.getValueAt(i, 0));
            JComboBox<String> sid = new JComboBox<>(DataStore.staff.values().stream()
                    .filter(s -> s.role == StaffRole.DOCTOR).map(s -> s.id).toArray(String[]::new));
            JTextField date = new JTextField(java.time.LocalDate.now().toString());
            JTextField time = new JTextField(java.time.LocalTime.now().withSecond(0).withNano(0).toString());
            JComboBox<String> dept = new JComboBox<>(DataStore.departments.toArray(new String[0]));
            int r = JOptionPane.showConfirmDialog(this,
                    new Object[] { "Doctor", sid, "Date", date, "Time", time, "Department", dept },
                    "Request Consultation", JOptionPane.OK_CANCEL_OPTION);
            if (r == JOptionPane.OK_OPTION) {
                java.util.List<String> out = AppointmentService.schedule(id, String.valueOf(sid.getSelectedItem()),
                        date.getText(), time.getText(), String.valueOf(dept.getSelectedItem()));
                showOut(out);
                if (!out.isEmpty() && out.get(0).startsWith("Appointment created ")) {
                    String apptId = out.get(0).substring("Appointment created ".length()).trim();
                    hpms.model.Appointment a = DataStore.appointments.get(apptId);
                    if (a != null)
                        a.notes = "Pending acceptance";
                }
            }
        });

        addAlert.addActionListener(e -> {
            int i = table.getSelectedRow();
            if (i < 0) {
                JOptionPane.showMessageDialog(this, "Select a patient first to add a critical note.",
                        "No patient selected", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String id = String.valueOf(table.getValueAt(i, 0));
            JTextArea ta = new JTextArea(5, 40);
            int r = JOptionPane.showConfirmDialog(this, new Object[] { "Critical Note", new JScrollPane(ta) },
                    "Add Critical Note", JOptionPane.OK_CANCEL_OPTION);
            if (r == JOptionPane.OK_OPTION) {
                showOut(CommunicationService.addAlert(id, ta.getText()));
                updateOverviewFromSelection(table);
            }
        });

        clinicalBtn.addActionListener(e -> {
            int i = table.getSelectedRow();
            if (i < 0) {
                JOptionPane.showMessageDialog(this, "Select a patient first");
                return;
            }
            String id = String.valueOf(table.getValueAt(i, 0));
            showClinicalDialogFor(id);
            updateOverviewFromSelection(table);
        });

        print.addActionListener(e -> {
            int i = table.getSelectedRow();
            if (i < 0) {
                JOptionPane.showMessageDialog(this, "Select a patient first to print a summary.", "No patient selected",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            String id = String.valueOf(table.getValueAt(i, 0));
            Patient p = DataStore.patients.get(id);
            if (p == null)
                return;
            StringBuilder sb = new StringBuilder();
            sb.append("ID: ").append(p.id).append('\n');
            sb.append("Name: ").append(p.name).append('\n');
            sb.append("Age/Gender: ").append(p.age).append(" / ").append(p.gender).append('\n');
            sb.append("Contact: ").append(p.contact).append('\n');
            Room r = findRoomForPatient(p.id);
            sb.append("Room: ").append(r == null ? "Not Assigned" : (r.id + " (" + r.status + ")")).append('\n');
            PatientStatus st = PatientStatusService.getStatus(p.id);
            sb.append("Status: ").append(st).append('\n');
            java.util.List<String> alerts = CommunicationService.alerts(p.id);
            sb.append("Critical Notes:\n");
            if (alerts.isEmpty())
                sb.append("None\n");
            else
                for (String a : alerts)
                    sb.append("- ").append(a).append('\n');
            JTextArea ta = new JTextArea(sb.toString(), 15, 50);
            ta.setEditable(false);
            JOptionPane.showMessageDialog(this, new JScrollPane(ta), "Patient Summary",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        table.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if (!e.getValueIsAdjusting())
                updateOverviewFromSelection(table);
        });

        // popup menu
        JPopupMenu popup = new JPopupMenu();
        JMenuItem viewInfoIt = new JMenuItem("View Info");
        JMenuItem viewIt = new JMenuItem("View / Edit");
        JMenuItem clinIt = new JMenuItem("Add Clinical Info");
        JMenuItem attachIt = new JMenuItem("Attach Files");
        JMenuItem medicalFolderIt = new JMenuItem("ðŸ“ Medical Document Folder");
        JMenuItem newVisitIt = new JMenuItem("Record New Arrival/Visit");
        popup.add(viewInfoIt);
        popup.add(viewIt);
        popup.add(clinIt);
        popup.add(medicalFolderIt);
        popup.add(attachIt);
        popup.add(newVisitIt);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent e) {
                maybeShow(e);
            }

            public void mouseReleased(java.awt.event.MouseEvent e) {
                maybeShow(e);
            }

            public void mouseClicked(java.awt.event.MouseEvent e) {
                // on double-click, open detailed patient view
                if (e.getClickCount() == 2 && !e.isConsumed()) {
                    int row = table.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        table.setRowSelectionInterval(row, row);
                        String id = String.valueOf(table.getValueAt(row, 0));
                        showPatientDetailsDialog(id);
                    }
                }
            }

            private void maybeShow(java.awt.event.MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int row = table.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        table.setRowSelectionInterval(row, row);
                        popup.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });
        viewIt.addActionListener(e -> {
            int i = table.getSelectedRow();
            if (i < 0)
                return;
            editPatient(table);
        });
        viewInfoIt.addActionListener(e -> {
            int i = table.getSelectedRow();
            if (i < 0)
                return;
            String id = String.valueOf(table.getValueAt(i, 0));
            showPatientDetailsDialog(id);
        });
        clinIt.addActionListener(e -> {
            int i = table.getSelectedRow();
            if (i < 0)
                return;
            String id = String.valueOf(table.getValueAt(i, 0));
            showClinicalDialogFor(id);
            updateOverviewFromSelection(table);
        });
        medicalFolderIt.addActionListener(e -> {
            int i = table.getSelectedRow();
            if (i < 0)
                return;
            String id = String.valueOf(table.getValueAt(i, 0));
            Patient p = DataStore.patients.get(id);
            if (p == null)
                return;
            showMedicalDocumentFolderDialog(id, p);
        });
        attachIt.addActionListener(e -> {
            int i = table.getSelectedRow();
            if (i < 0)
                return;
            String id = String.valueOf(table.getValueAt(i, 0));
            Patient p = DataStore.patients.get(id);
            if (p == null)
                return;
            javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
            fc.setMultiSelectionEnabled(true);
            if (fc.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
                java.io.File[] files = fc.getSelectedFiles();
                for (java.io.File f : files)
                    p.attachmentPaths.add(f.getAbsolutePath());
                LogManager.log("attach_files " + id);
                JOptionPane.showMessageDialog(this, "Files attached: " + files.length);
                updateOverviewFromSelection(table);
            }
        });
        newVisitIt.addActionListener(e -> {
            int i = table.getSelectedRow();
            if (i < 0)
                return;
            String id = String.valueOf(table.getValueAt(i, 0));
            Patient p = DataStore.patients.get(id);
            if (p == null)
                return;
            showNewVisitDialog(id, p);
        });

        // pagination handlers
        prevPage.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                applyPatientFilter();
            }
        });
        nextPage.addActionListener(e -> {
            currentPage++;
            applyPatientFilter();
        });

        // toggle notes
        toggleNotes.addActionListener(e -> notesArea.setVisible(!notesArea.isVisible()));

        refresh();
    }

    private JLabel labelField(String title) {
        JLabel l = new JLabel(title + ": ");
        l.setFont(Theme.APP_FONT.deriveFont(13f));
        return l;
    }

    private void styleBadge(JLabel l, String text) {
        l.setText(text);
        l.setOpaque(true);
        l.setBackground(new Color(220, 235, 255));
        l.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        l.setForeground(Theme.FOREGROUND);
    }

    public void refresh() {
        applyPatientFilter();
    }

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
                JOptionPane.showMessageDialog(dialog, "Name is required", "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
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
                JOptionPane.showMessageDialog(dialog, "Invalid Date of Birth format. Use MM/DD/YYYY",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
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
                JOptionPane.showMessageDialog(dialog, "Gender is required", "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (phoneField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Phone is required", "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (addressField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Address is required", "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
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

    public void editPatient(JTable t) {
        int i = t.getSelectedRow();
        if (i < 0)
            return;
        String id = String.valueOf(t.getValueAt(i, 0));
        Patient p = DataStore.patients.get(id);

        // Create all form fields with existing patient data
        JTextField name = new JTextField(p.name);
        JTextField age = new JTextField(String.valueOf(p.age));
        JTextField birthday = new JTextField(p.birthday == null ? "" : p.birthday);
        JComboBox<String> gender = new JComboBox<>(new String[] { "Male", "Female", "LGBTQ+", "Other" });
        String gsel = "Other";
        if (p.gender == Gender.Male)
            gsel = "Male";
        else if (p.gender == Gender.Female)
            gsel = "Female";
        else if (p.gender == Gender.LGBTQ_PLUS)
            gsel = "LGBTQ+";
        gender.setSelectedItem(gsel);
        JTextField contact = new JTextField(p.contact);
        JTextField address = new JTextField(p.address);
        JComboBox<String> patientTypeCombo = new JComboBox<>(new String[] { "INPATIENT", "OUTPATIENT" });
        patientTypeCombo.setSelectedItem(
                p.patientType == null || p.patientType.trim().isEmpty() ? "OUTPATIENT" : p.patientType);

        // CRITICAL RULE: If patient is marked as permanent OUTPATIENT, disable type
        // editing
        if (p.isOutpatientPermanent) {
            patientTypeCombo.setEnabled(false);
            // Add tooltip to explain why it's disabled
            patientTypeCombo.setToolTipText(
                    "Patient type is locked as OUTPATIENT (permanent). Cannot be changed to INPATIENT.");
        }

        JTextArea allergies = new JTextArea(p.allergies, 3, 40);
        allergies.setLineWrap(true);
        allergies.setWrapStyleWord(true);
        JTextArea meds = new JTextArea(p.medications, 3, 40);
        meds.setLineWrap(true);
        meds.setWrapStyleWord(true);
        JTextArea history = new JTextArea(p.pastMedicalHistory, 3, 40);
        history.setLineWrap(true);
        history.setWrapStyleWord(true);

        JComboBox<String> smoking = new JComboBox<>(new String[] { "Current", "Former", "Never", "Prefer not to say" });
        smoking.setSelectedItem(p.smokingStatus == null ? "" : p.smokingStatus);
        JComboBox<String> alcohol = new JComboBox<>(
                new String[] { "Never", "Monthly or less", "2-4 times/month", "2-3 times/week", "4+ times/week" });
        alcohol.setSelectedItem(p.alcoholUse == null ? "" : p.alcoholUse);

        JComboBox<String> occupation = new JComboBox<>(new String[] { "Unemployed", "Student", "Employed" });
        occupation.setEditable(false);
        JTextField occupationDetail = new JTextField();
        occupationDetail.setVisible(false);
        occupationDetail.setColumns(20);
        if (p.occupation != null) {
            String pv = p.occupation.trim();
            if (pv.equalsIgnoreCase("Unemployed") || pv.equalsIgnoreCase("Student")) {
                occupation.setSelectedItem(pv.equalsIgnoreCase("Unemployed") ? "Unemployed" : "Student");
                occupationDetail.setText("");
            } else if (pv.equalsIgnoreCase("Employed")) {
                occupation.setSelectedItem("Employed");
            } else if (!pv.isEmpty()) {
                occupation.setSelectedItem("Employed");
                occupationDetail.setText(pv);
            }
        }

        JComboBox<String> insProvider = new JComboBox<>(new String[] { "None", "PhilHealth", "Other" });
        insProvider.setEditable(false);
        JTextField insProviderOther = new JTextField();
        insProviderOther.setColumns(20);
        String curProv = p.insuranceProvider == null ? "" : p.insuranceProvider;
        boolean provFound = false;
        for (int ii = 0; ii < insProvider.getItemCount(); ii++)
            if (insProvider.getItemAt(ii).equalsIgnoreCase(curProv)) {
                provFound = true;
                insProvider.setSelectedItem(insProvider.getItemAt(ii));
                break;
            }
        if (!provFound && !curProv.isEmpty()) {
            insProvider.setSelectedItem("Other");
            insProviderOther.setText(curProv);
            insProviderOther.setVisible(true);
        }

        JTextField insId = new JTextField(p.insuranceId);
        JTextField policyHolder = new JTextField(p.policyHolderName);
        JTextField policyDob = new JTextField(p.policyHolderDob);
        JComboBox<String> policyRel = new JComboBox<>(
                new String[] { "Self", "Parent", "Spouse/Partner", "Employer", "Other" });
        JTextField policyRelOther = new JTextField();
        policyRelOther.setVisible(false);
        policyRelOther.setColumns(20);
        String curRel = p.policyRelationship == null ? "" : p.policyRelationship;
        boolean relFound = false;
        for (int ii = 0; ii < policyRel.getItemCount(); ii++)
            if (policyRel.getItemAt(ii).equalsIgnoreCase(curRel)) {
                relFound = true;
                policyRel.setSelectedItem(policyRel.getItemAt(ii));
                break;
            }
        if (!relFound && !curRel.isEmpty()) {
            policyRel.setSelectedItem("Other");
            policyRelOther.setText(curRel);
            policyRelOther.setVisible(true);
        }

        // Registration / mode of arrival
        String[] regOptions = new String[] {
                "Walk-in Patient",
                "Emergency Patient",
                "Accident / Trauma Case",
                "Referral Patient",
                "OB / Maternity Case",
                "Surgical Admission",
                "Pediatric Patient",
                "Geriatric Patient",
                "Telemedicine / Virtual Consult"
        };
        JComboBox<String> regCombo = new JComboBox<>(regOptions);
        if (p.registrationType != null && !p.registrationType.isEmpty())
            regCombo.setSelectedItem(p.registrationType);

        // Make registration type non-editable in edit dialog - it cannot be changed
        // after creation
        regCombo.setEnabled(false);
        regCombo.setToolTipText("Type of Arrival cannot be changed after patient creation");

        JPanel incidentPanel = new JPanel(new GridBagLayout());
        GridBagConstraints ipc = new GridBagConstraints();
        ipc.insets = new Insets(4, 4, 4, 4);
        ipc.anchor = GridBagConstraints.WEST;
        ipc.fill = GridBagConstraints.HORIZONTAL;
        ipc.weightx = 1.0;
        int ipy = 0;
        JLabel timeOfIncidentLbl = new JLabel("Time of Incident (HH:mm)");
        timeOfIncidentLbl.setFont(Theme.APP_FONT.deriveFont(Font.PLAIN, 10f));
        JTextField timeOfIncident = new JTextField(p.incidentTime == null ? "" : p.incidentTime);
        timeOfIncident.setColumns(8);
        ipc.gridy = ipy;
        ipc.gridx = 0;
        incidentPanel.add(timeOfIncidentLbl, ipc);
        ipc.gridx = 1;
        incidentPanel.add(timeOfIncident, ipc);
        ipy++;
        JLabel broughtLbl = new JLabel("Brought by:");
        broughtLbl.setFont(Theme.APP_FONT.deriveFont(Font.PLAIN, 10f));
        JCheckBox cbAmb = new JCheckBox("Ambulance");
        JCheckBox cbFam = new JCheckBox("Family");
        JCheckBox cbBy = new JCheckBox("Bystander");
        JCheckBox cbPol = new JCheckBox("Police");
        // pre-select broughtBy checkboxes from patient value
        if (p.broughtBy != null && !p.broughtBy.isEmpty()) {
            String[] parts = p.broughtBy.split(";");
            for (String s : parts) {
                String part = s.trim();
                if (part.equalsIgnoreCase("Ambulance"))
                    cbAmb.setSelected(true);
                if (part.equalsIgnoreCase("Family"))
                    cbFam.setSelected(true);
                if (part.equalsIgnoreCase("Bystander"))
                    cbBy.setSelected(true);
                if (part.equalsIgnoreCase("Police"))
                    cbPol.setSelected(true);
            }
        }
        ipc.gridy = ipy++;
        ipc.gridx = 0;
        incidentPanel.add(broughtLbl, ipc);
        JPanel cbPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        cbPanel.setOpaque(false);
        cbPanel.add(cbAmb);
        cbPanel.add(cbFam);
        cbPanel.add(cbBy);
        cbPanel.add(cbPol);
        ipc.gridx = 1;
        incidentPanel.add(cbPanel, ipc);
        JLabel vitalsLabel = new JLabel("Initial Vitals: BP (systolic/diastolic) | HR | SpO2");
        vitalsLabel.setFont(Theme.APP_FONT.deriveFont(Font.PLAIN, 10f));
        JTextField bpSys = new JTextField();
        bpSys.setColumns(4);
        JTextField bpDia = new JTextField();
        bpDia.setColumns(4);
        JTextField hrField = new JTextField();
        hrField.setColumns(4);
        JTextField spo2Field = new JTextField();
        spo2Field.setColumns(4);
        if (p.initialBp != null && p.initialBp.contains("/")) {
            String[] bp = p.initialBp.split("/");
            if (bp.length >= 2) {
                bpSys.setText(bp[0]);
                bpDia.setText(bp[1]);
            }
        }
        hrField.setText(p.initialHr == null ? "" : p.initialHr);
        spo2Field.setText(p.initialSpo2 == null ? "" : p.initialSpo2);
        JPanel vpanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        vpanel.setOpaque(false);
        vpanel.add(new JLabel("BP"));
        vpanel.add(bpSys);
        vpanel.add(new JLabel("/"));
        vpanel.add(bpDia);
        vpanel.add(new JLabel("HR"));
        vpanel.add(hrField);
        vpanel.add(new JLabel("SpOâ‚‚%"));
        vpanel.add(spo2Field);
        ipc.gridy = ipy++;
        ipc.gridx = 0;
        incidentPanel.add(vitalsLabel, ipc);
        ipc.gridx = 1;
        incidentPanel.add(vpanel, ipc);
        JLabel chiefLbl = new JLabel("Chief Complaint");
        chiefLbl.setFont(Theme.APP_FONT.deriveFont(Font.PLAIN, 10f));
        JTextArea chiefArea = new JTextArea(p.chiefComplaint == null ? "" : p.chiefComplaint, 2, 40);
        chiefArea.setLineWrap(true);
        chiefArea.setWrapStyleWord(true);
        ipc.gridy = ipy++;
        ipc.gridx = 0;
        incidentPanel.add(chiefLbl, ipc);
        ipc.gridx = 1;
        incidentPanel.add(new JScrollPane(chiefArea), ipc);
        boolean needIncident = p.registrationType != null && (p.registrationType.equals("Emergency Patient")
                || p.registrationType.equals("Accident / Trauma Case"));
        incidentPanel.setVisible(needIncident);

        // Set uniform column widths
        name.setColumns(25);
        age.setColumns(8);
        contact.setColumns(25);
        address.setColumns(25);
        insId.setColumns(20);
        policyHolder.setColumns(20);
        policyDob.setColumns(18);

        // Main outer panel with vertical layout: Identity â†’ (Lifestyle + Insurance)
        // â†’
        // Medical
        JPanel outerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints oc = new GridBagConstraints();
        oc.anchor = GridBagConstraints.NORTHWEST;
        oc.fill = GridBagConstraints.BOTH;
        oc.insets = new Insets(0, 0, 0, 0);
        oc.weightx = 1.0;
        int oRow = 0;

        // ====== IDENTITY (full width at top) ======
        JPanel identityPanel = new JPanel(new GridBagLayout());
        GridBagConstraints ic = new GridBagConstraints();
        ic.insets = new Insets(10, 12, 10, 12);
        ic.anchor = GridBagConstraints.WEST;
        ic.fill = GridBagConstraints.HORIZONTAL;
        ic.weightx = 1.0;
        int iy = 0;

        JLabel identSectionLbl = new JLabel("ðŸ‘¤ Identity Information");
        identSectionLbl.setFont(Theme.APP_FONT.deriveFont(Font.BOLD, 12f));
        identSectionLbl.setForeground(new Color(0, 102, 102));
        ic.gridy = iy++;
        ic.gridx = 0;
        ic.gridwidth = 4;
        ic.insets = new Insets(15, 12, 5, 12);
        identityPanel.add(identSectionLbl, ic);
        ic.gridwidth = 1;
        ic.insets = new Insets(10, 12, 10, 12);
        ic.gridy = iy++;
        ic.gridx = 0;
        ic.gridwidth = 4;
        identityPanel.add(new JSeparator(), ic);
        ic.gridwidth = 1;

        JLabel nameLbl = new JLabel("Name *");
        nameLbl.setDisplayedMnemonic('N');
        nameLbl.setLabelFor(name);
        nameLbl.setFont(Theme.APP_FONT.deriveFont(Font.BOLD, 10f));
        name.getAccessibleContext().setAccessibleName("Name");
        ic.gridy = iy;
        ic.gridx = 0;
        ic.weightx = 0.25;
        identityPanel.add(nameLbl, ic);
        ic.gridx = 1;
        ic.weightx = 0.25;
        identityPanel.add(name, ic);
        JLabel ageLbl = new JLabel("Age *");
        ageLbl.setDisplayedMnemonic('A');
        ageLbl.setLabelFor(age);
        ageLbl.setFont(Theme.APP_FONT.deriveFont(Font.BOLD, 10f));
        age.getAccessibleContext().setAccessibleName("Age");
        ic.gridx = 2;
        ic.weightx = 0.25;
        identityPanel.add(ageLbl, ic);
        ic.gridx = 3;
        ic.weightx = 0.25;
        identityPanel.add(age, ic);
        iy++;

        JLabel genderLbl = new JLabel("Gender *");
        genderLbl.setDisplayedMnemonic('G');
        genderLbl.setLabelFor(gender);
        genderLbl.setFont(Theme.APP_FONT.deriveFont(Font.BOLD, 10f));
        gender.getAccessibleContext().setAccessibleName("Gender");
        ic.gridy = iy;
        ic.gridx = 0;
        ic.weightx = 0.25;
        identityPanel.add(genderLbl, ic);
        ic.gridx = 1;
        ic.weightx = 0.25;
        identityPanel.add(gender, ic);
        JLabel contactLbl = new JLabel("Contact *");
        contactLbl.setDisplayedMnemonic('C');
        contactLbl.setLabelFor(contact);
        contactLbl.setFont(Theme.APP_FONT.deriveFont(Font.BOLD, 10f));
        contact.getAccessibleContext().setAccessibleName("Contact");
        ic.gridx = 2;
        ic.weightx = 0.25;
        identityPanel.add(contactLbl, ic);
        ic.gridx = 3;
        ic.weightx = 0.25;
        identityPanel.add(contact, ic);
        iy++;

        JLabel addressLbl = new JLabel("Address");
        addressLbl.setFont(Theme.APP_FONT.deriveFont(Font.BOLD, 10f));
        ic.gridy = iy++;
        ic.gridx = 0;
        ic.gridwidth = 4;
        ic.weightx = 1.0;
        identityPanel.add(addressLbl, ic);
        ic.gridwidth = 1;
        ic.gridy = iy++;
        ic.gridx = 0;
        ic.gridwidth = 4;
        ic.weightx = 1.0;
        identityPanel.add(address, ic);
        // Add registration type and incident details (if needed)
        JLabel regLbl = new JLabel("Type of Registration / Mode of Arrival (Locked)");
        regLbl.setFont(Theme.APP_FONT.deriveFont(Font.BOLD, 10f));
        ic.gridy = iy;
        ic.gridx = 0;
        ic.weightx = 0.5;
        identityPanel.add(regLbl, ic);
        ic.gridx = 1;
        ic.weightx = 0.5;
        identityPanel.add(regCombo, ic);
        iy++;

        // Add first diagnosis display (read-only)
        JLabel firstDiagnosisLbl = new JLabel("First Diagnosis (Locked)");
        firstDiagnosisLbl.setFont(Theme.APP_FONT.deriveFont(Font.BOLD, 10f));
        String firstDiagnosisText = (p.diagnoses != null && !p.diagnoses.isEmpty())
                ? p.diagnoses.get(0)
                : "(No diagnosis recorded)";
        JLabel firstDiagnosisValue = new JLabel(firstDiagnosisText);
        firstDiagnosisValue.setFont(Theme.APP_FONT.deriveFont(Font.PLAIN, 10f));
        firstDiagnosisValue.setForeground(new Color(64, 64, 64));
        firstDiagnosisValue.setToolTipText("First diagnosis cannot be changed after creation");
        ic.gridy = iy;
        ic.gridx = 0;
        ic.weightx = 0.5;
        identityPanel.add(firstDiagnosisLbl, ic);
        ic.gridx = 1;
        ic.weightx = 0.5;
        identityPanel.add(firstDiagnosisValue, ic);
        iy++;

        ic.gridy = iy++;
        ic.gridx = 0;
        ic.gridwidth = 4;
        identityPanel.add(incidentPanel, ic);
        ic.gridwidth = 1;
        identityPanel.setBorder(new javax.swing.border.EmptyBorder(10, 10, 10, 10));
        identityPanel.setBackground(new Color(250, 250, 250));

        oc.gridy = oRow++;
        oc.gridx = 0;
        oc.gridwidth = 2;
        oc.weightx = 1.0;
        oc.weighty = 0;
        oc.fill = GridBagConstraints.HORIZONTAL;
        outerPanel.add(identityPanel, oc);
        oc.gridwidth = 1;

        // ====== LIFESTYLE (Left Column) + INSURANCE (Right Column) ======
        JPanel lifestyleCard = new JPanel(new GridBagLayout());
        GridBagConstraints lc = new GridBagConstraints();
        lc.insets = new Insets(10, 12, 10, 12);
        lc.anchor = GridBagConstraints.WEST;
        lc.fill = GridBagConstraints.HORIZONTAL;
        lc.weightx = 1.0;
        int ly = 0;

        JLabel lifestyleSectionLbl = new JLabel("ðŸ’ª Lifestyle Information");
        lifestyleSectionLbl.setFont(Theme.APP_FONT.deriveFont(Font.BOLD, 11f));
        lifestyleSectionLbl.setForeground(new Color(0, 102, 102));
        lc.gridy = ly++;
        lc.gridx = 0;
        lc.gridwidth = 2;
        lc.insets = new Insets(12, 12, 5, 12);
        lifestyleCard.add(lifestyleSectionLbl, lc);
        lc.gridwidth = 1;
        lc.insets = new Insets(10, 12, 10, 12);
        lc.gridy = ly++;
        lc.gridx = 0;
        lc.gridwidth = 2;
        lifestyleCard.add(new JSeparator(), lc);
        lc.gridwidth = 1;

        JLabel smokingLbl = new JLabel("Smoking Status");
        smokingLbl.setFont(Theme.APP_FONT.deriveFont(Font.BOLD, 10f));
        lc.gridy = ly;
        lc.gridx = 0;
        lc.weightx = 0.4;
        lifestyleCard.add(smokingLbl, lc);
        lc.gridx = 1;
        lc.weightx = 0.6;
        lifestyleCard.add(smoking, lc);
        ly++;

        JLabel alcoholLbl = new JLabel("Alcohol Use");
        alcoholLbl.setFont(Theme.APP_FONT.deriveFont(Font.BOLD, 10f));
        lc.gridy = ly;
        lc.gridx = 0;
        lc.weightx = 0.4;
        lifestyleCard.add(alcoholLbl, lc);
        lc.gridx = 1;
        lc.weightx = 0.6;
        lifestyleCard.add(alcohol, lc);
        ly++;

        JLabel occupationLbl = new JLabel("Occupation");
        occupationLbl.setFont(Theme.APP_FONT.deriveFont(Font.BOLD, 10f));
        lc.gridy = ly;
        lc.gridx = 0;
        lc.weightx = 0.4;
        lifestyleCard.add(occupationLbl, lc);
        lc.gridx = 1;
        lc.weightx = 0.6;
        lifestyleCard.add(occupation, lc);
        ly++;

        JLabel occDetailLbl = new JLabel("If Employed, specify");
        occDetailLbl.setFont(Theme.APP_FONT.deriveFont(Font.ITALIC, 9f));
        occDetailLbl.setVisible(!occupationDetail.getText().trim().isEmpty()
                || "Employed".equals(String.valueOf(occupation.getSelectedItem())));
        lc.gridy = ly;
        lc.gridx = 0;
        lc.weightx = 0.4;
        lifestyleCard.add(occDetailLbl, lc);
        lc.gridx = 1;
        lc.weightx = 0.6;
        lifestyleCard.add(occupationDetail, lc);
        occupationDetail.setVisible(occDetailLbl.isVisible());
        ly++;
        lifestyleCard.setBorder(new javax.swing.border.LineBorder(new Color(200, 200, 200), 1, true));
        lifestyleCard.setBackground(new Color(255, 255, 255));

        // Insurance Card
        JPanel insuranceCard = new JPanel(new GridBagLayout());
        GridBagConstraints inc = new GridBagConstraints();
        inc.insets = new Insets(10, 12, 10, 12);
        inc.anchor = GridBagConstraints.WEST;
        inc.fill = GridBagConstraints.HORIZONTAL;
        inc.weightx = 1.0;
        int iny = 0;

        JLabel insuranceSectionLbl = new JLabel("ðŸ’³ Insurance Information");
        insuranceSectionLbl.setFont(Theme.APP_FONT.deriveFont(Font.BOLD, 11f));
        insuranceSectionLbl.setForeground(new Color(0, 102, 102));
        inc.gridy = iny++;
        inc.gridx = 0;
        inc.gridwidth = 2;
        inc.insets = new Insets(12, 12, 5, 12);
        insuranceCard.add(insuranceSectionLbl, inc);
        inc.gridwidth = 1;
        inc.insets = new Insets(10, 12, 10, 12);
        inc.gridy = iny++;
        inc.gridx = 0;
        inc.gridwidth = 2;
        insuranceCard.add(new JSeparator(), inc);
        inc.gridwidth = 1;

        JLabel insProviderLbl = new JLabel("Provider");
        insProviderLbl.setFont(Theme.APP_FONT.deriveFont(Font.BOLD, 10f));
        inc.gridy = iny;
        inc.gridx = 0;
        inc.weightx = 0.4;
        insuranceCard.add(insProviderLbl, inc);
        inc.gridx = 1;
        inc.weightx = 0.6;
        insuranceCard.add(insProvider, inc);
        iny++;

        JLabel insOtherLbl = new JLabel("If Other, specify");
        insOtherLbl.setFont(Theme.APP_FONT.deriveFont(Font.ITALIC, 9f));
        insOtherLbl.setVisible(insProviderOther.isVisible());
        inc.gridy = iny;
        inc.gridx = 0;
        inc.weightx = 0.4;
        insuranceCard.add(insOtherLbl, inc);
        inc.gridx = 1;
        inc.weightx = 0.6;
        insuranceCard.add(insProviderOther, inc);
        iny++;

        boolean hasInsurance = !"None".equals(String.valueOf(insProvider.getSelectedItem()));
        JLabel insIdLbl = new JLabel("Insurance ID");
        insIdLbl.setFont(Theme.APP_FONT.deriveFont(Font.BOLD, 10f));
        insIdLbl.setVisible(hasInsurance);
        inc.gridy = iny;
        inc.gridx = 0;
        inc.weightx = 0.4;
        insuranceCard.add(insIdLbl, inc);
        inc.gridx = 1;
        inc.weightx = 0.6;
        insuranceCard.add(insId, inc);
        iny++;
        insId.setVisible(hasInsurance);

        JLabel holderLbl = new JLabel("Policy Holder");
        holderLbl.setFont(Theme.APP_FONT.deriveFont(Font.BOLD, 10f));
        holderLbl.setLabelFor(policyHolder);
        holderLbl.setVisible(hasInsurance);
        inc.gridy = iny;
        inc.gridx = 0;
        inc.weightx = 0.4;
        insuranceCard.add(holderLbl, inc);
        inc.gridx = 1;
        inc.weightx = 0.6;
        insuranceCard.add(policyHolder, inc);
        iny++;
        policyHolder.setVisible(hasInsurance);

        JLabel dobLbl = new JLabel("Holder DOB (YYYY-MM-DD)");
        dobLbl.setDisplayedMnemonic('D');
        dobLbl.setLabelFor(policyDob);
        dobLbl.setFont(Theme.APP_FONT.deriveFont(Font.BOLD, 10f));
        dobLbl.setVisible(hasInsurance);
        inc.gridy = iny;
        inc.gridx = 0;
        inc.weightx = 0.4;
        insuranceCard.add(dobLbl, inc);
        inc.gridx = 1;
        inc.weightx = 0.6;
        insuranceCard.add(policyDob, inc);
        iny++;
        policyDob.setVisible(hasInsurance);

        JLabel relLbl = new JLabel("Relationship");
        relLbl.setDisplayedMnemonic('R');
        relLbl.setLabelFor(policyRel);
        relLbl.setFont(Theme.APP_FONT.deriveFont(Font.BOLD, 10f));
        relLbl.setVisible(hasInsurance);
        inc.gridy = iny;
        inc.gridx = 0;
        inc.weightx = 0.4;
        insuranceCard.add(relLbl, inc);
        inc.gridx = 1;
        inc.weightx = 0.6;
        insuranceCard.add(policyRel, inc);
        iny++;
        policyRel.setVisible(hasInsurance);

        JLabel relOtherLbl = new JLabel("If Other, specify");
        relOtherLbl.setFont(Theme.APP_FONT.deriveFont(Font.ITALIC, 9f));
        relOtherLbl.setVisible(policyRelOther.isVisible());
        inc.gridy = iny;
        inc.gridx = 0;
        inc.weightx = 0.4;
        insuranceCard.add(relOtherLbl, inc);
        inc.gridx = 1;
        inc.weightx = 0.6;
        insuranceCard.add(policyRelOther, inc);
        iny++;
        insuranceCard.setBorder(new javax.swing.border.LineBorder(new Color(200, 200, 200), 1, true));
        insuranceCard.setBackground(new Color(255, 255, 255));

        oc.gridy = oRow;
        oc.gridx = 0;
        oc.gridwidth = 1;
        oc.weightx = 0.5;
        oc.weighty = 0;
        oc.insets = new Insets(10, 10, 10, 5);
        oc.fill = GridBagConstraints.BOTH;
        outerPanel.add(lifestyleCard, oc);
        oc.gridx = 1;
        oc.insets = new Insets(10, 5, 10, 10);
        outerPanel.add(insuranceCard, oc);
        oRow++;

        // ====== MEDICAL (full width at bottom) ======
        JPanel medicalPanel = new JPanel(new GridBagLayout());
        GridBagConstraints mc = new GridBagConstraints();
        mc.insets = new Insets(10, 12, 10, 12);
        mc.anchor = GridBagConstraints.WEST;
        mc.fill = GridBagConstraints.HORIZONTAL;
        mc.weightx = 1.0;
        int my = 0;

        JLabel medSectionLbl = new JLabel("ðŸ“‹ Medical Information");
        medSectionLbl.setFont(Theme.APP_FONT.deriveFont(Font.BOLD, 12f));
        medSectionLbl.setForeground(new Color(0, 102, 102));
        mc.gridy = my++;
        mc.gridx = 0;
        mc.gridwidth = 4;
        mc.insets = new Insets(15, 12, 5, 12);
        medicalPanel.add(medSectionLbl, mc);
        mc.gridwidth = 1;
        mc.insets = new Insets(10, 12, 10, 12);
        mc.gridy = my++;
        mc.gridx = 0;
        mc.gridwidth = 4;
        medicalPanel.add(new JSeparator(), mc);
        mc.gridwidth = 1;

        JLabel allergiesLbl = new JLabel("Allergies");
        allergiesLbl.setFont(Theme.APP_FONT.deriveFont(Font.BOLD, 10f));
        mc.gridy = my;
        mc.gridx = 0;
        mc.gridwidth = 2;
        mc.weightx = 0.5;
        mc.anchor = GridBagConstraints.NORTHWEST;
        medicalPanel.add(allergiesLbl, mc);
        mc.gridwidth = 1;
        JLabel medsLbl = new JLabel("Current Medications");
        medsLbl.setFont(Theme.APP_FONT.deriveFont(Font.BOLD, 10f));
        mc.gridx = 2;
        mc.gridwidth = 2;
        mc.weightx = 0.5;
        mc.anchor = GridBagConstraints.NORTHWEST;
        medicalPanel.add(medsLbl, mc);
        mc.gridwidth = 1;
        my++;

        mc.gridy = my;
        mc.gridx = 0;
        mc.gridwidth = 2;
        mc.weightx = 0.5;
        mc.anchor = GridBagConstraints.WEST;
        mc.fill = GridBagConstraints.BOTH;
        mc.weighty = 1.0;
        medicalPanel.add(new JScrollPane(allergies), mc);
        mc.gridwidth = 1;
        mc.weighty = 0;
        mc.gridx = 2;
        mc.gridwidth = 2;
        mc.weightx = 0.5;
        mc.anchor = GridBagConstraints.WEST;
        mc.fill = GridBagConstraints.BOTH;
        mc.weighty = 1.0;
        medicalPanel.add(new JScrollPane(meds), mc);
        mc.gridwidth = 1;
        mc.weighty = 0;
        my++;

        JLabel historyLbl = new JLabel("Past Medical History");
        historyLbl.setFont(Theme.APP_FONT.deriveFont(Font.BOLD, 10f));
        mc.gridy = my++;
        mc.gridx = 0;
        mc.gridwidth = 4;
        mc.weightx = 1.0;
        mc.anchor = GridBagConstraints.NORTHWEST;
        medicalPanel.add(historyLbl, mc);
        mc.gridwidth = 1;
        mc.gridy = my++;
        mc.gridx = 0;
        mc.gridwidth = 4;
        mc.weightx = 1.0;
        mc.anchor = GridBagConstraints.WEST;
        mc.fill = GridBagConstraints.BOTH;
        mc.weighty = 1.0;
        medicalPanel.add(new JScrollPane(history), mc);
        mc.gridwidth = 1;
        mc.weighty = 0;
        medicalPanel.setBorder(new javax.swing.border.EmptyBorder(10, 10, 10, 10));
        medicalPanel.setBackground(new Color(250, 250, 250));

        oc.gridy = oRow++;
        oc.gridx = 0;
        oc.gridwidth = 2;
        oc.weightx = 1.0;
        oc.weighty = 1.0;
        oc.fill = GridBagConstraints.BOTH;
        outerPanel.add(medicalPanel, oc);
        oc.gridwidth = 1;

        JScrollPane scrollPane = new JScrollPane(outerPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(15);

        // listeners to toggle 'Other' inputs and occupation detail
        insProvider.addItemListener(evt -> {
            if (evt.getStateChange() == ItemEvent.SELECTED) {
                String sel = String.valueOf(evt.getItem());
                boolean hasIns = !"None".equals(sel);
                boolean isOther = "Other".equals(sel);
                insOtherLbl.setVisible(isOther);
                insProviderOther.setVisible(isOther);
                insIdLbl.setVisible(hasIns);
                insId.setVisible(hasIns);
                holderLbl.setVisible(hasIns);
                policyHolder.setVisible(hasIns);
                dobLbl.setVisible(hasIns);
                policyDob.setVisible(hasIns);
                relLbl.setVisible(hasIns);
                policyRel.setVisible(hasIns);
                scrollPane.revalidate();
                scrollPane.repaint();
                if (isOther) {
                    insProviderOther.requestFocusInWindow();
                } else {
                    insProviderOther.setText("");
                }
            }
        });
        policyRel.addItemListener(evt -> {
            if (evt.getStateChange() == ItemEvent.SELECTED) {
                String sel = String.valueOf(evt.getItem());
                boolean isOther = "Other".equals(sel);
                relOtherLbl.setVisible(isOther);
                policyRelOther.setVisible(isOther);
                scrollPane.revalidate();
                scrollPane.repaint();
                if (isOther) {
                    policyRelOther.requestFocusInWindow();
                } else {
                    policyRelOther.setText("");
                }
                if ("Self".equals(sel)) {
                    policyHolder.setText(name.getText());
                }
            }
        });
        occupation.addItemListener(evt -> {
            if (evt.getStateChange() == ItemEvent.SELECTED) {
                boolean isEmployed = "Employed".equals(String.valueOf(evt.getItem()));
                occDetailLbl.setVisible(isEmployed);
                occupationDetail.setVisible(isEmployed);
                scrollPane.revalidate();
                scrollPane.repaint();
                if (isEmployed)
                    occupationDetail.requestFocusInWindow();
                if (!isEmployed)
                    occupationDetail.setText("");
            }
        });

        // Note: height/weight inputs are removed from the edit dialog â€” registration
        // /
        // incident data are handled instead

        // Replace JOptionPane with a proper resizable dialog so there's a clear Save
        // button.
        java.awt.Window owner = javax.swing.SwingUtilities.getWindowAncestor(this);
        final JDialog editDialog = new JDialog((java.awt.Frame) owner, "Edit Patient " + id, true);
        editDialog.setLayout(new BorderLayout());
        editDialog.add(scrollPane, BorderLayout.CENTER);

        JPanel editActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton newVisitBtn = new JButton("ðŸ“‹ Record New Visit");
        newVisitBtn.setToolTipText("Record a new arrival/visit for this patient");
        JButton saveBtn = new JButton("Save");
        JButton cancelBtn2 = new JButton("Cancel");
        editActions.add(cancelBtn2);
        editActions.add(newVisitBtn);
        editActions.add(saveBtn);
        editDialog.add(editActions, BorderLayout.SOUTH);

        saveBtn.addActionListener(ev -> {
            // validation
            if (name.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(editDialog, "Name is required");
                return;
            }
            Integer ageVal = parseIntSafe(age.getText());
            if (ageVal == null || ageVal <= 0) {
                JOptionPane.showMessageDialog(editDialog, "Enter a valid age");
                return;
            }
            if (contact.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(editDialog, "Contact is required");
                return;
            }
            String providerVal = insOtherLbl.isVisible() && !insProviderOther.getText().trim().isEmpty()
                    ? insProviderOther.getText().trim()
                    : String.valueOf(insProvider.getSelectedItem());
            String relationVal = relOtherLbl.isVisible() && !policyRelOther.getText().trim().isEmpty()
                    ? policyRelOther.getText().trim()
                    : (relLbl.isVisible() ? String.valueOf(policyRel.getSelectedItem()) : "");
            String occupationVal = String.valueOf(occupation.getSelectedItem());
            if ("Employed".equals(occupationVal)) {
                if (occupationDetail.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please specify the type of work for employed patients");
                    return;
                } else
                    occupationVal = occupationDetail.getText().trim();
            }
            String dobText = policyDob.getText().trim();
            if (!dobText.isEmpty()) {
                try {
                    LocalDate.parse(dobText);
                } catch (DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(this, "Policy Holder DOB must be YYYY-MM-DD");
                    return;
                }
            }
            // gather registration/arrival details
            String genderVal = String.valueOf(gender.getSelectedItem());
            String regTypeVal = String.valueOf(regCombo.getSelectedItem());
            String incidentTVal = timeOfIncident.getText().trim();
            java.util.List<String> broughtList = new java.util.ArrayList<>();
            if (cbAmb.isSelected())
                broughtList.add("Ambulance");
            if (cbFam.isSelected())
                broughtList.add("Family");
            if (cbBy.isSelected())
                broughtList.add("Bystander");
            if (cbPol.isSelected())
                broughtList.add("Police");
            String broughtByVal = String.join(";", broughtList);
            String initialBpVal = (bpSys.getText().trim().isEmpty() || bpDia.getText().trim().isEmpty()) ? ""
                    : (bpSys.getText().trim() + "/" + bpDia.getText().trim());
            String initialHrVal = hrField.getText().trim();
            String initialSpo2Val = spo2Field.getText().trim();
            String chiefVal = chiefArea.getText().trim();

            // If this is an Emergency or Accident case we strongly encourage incident
            // details.
            boolean emergencyCase = "Emergency Patient".equals(regTypeVal)
                    || "Accident / Trauma Case".equals(regTypeVal);
            boolean incidentProvided = !incidentTVal.isEmpty() || !broughtByVal.isEmpty() || !initialBpVal.isEmpty()
                    || !initialHrVal.isEmpty() || !initialSpo2Val.isEmpty() || !chiefVal.isEmpty();
            if (emergencyCase && !incidentProvided) {
                int proceed = JOptionPane.showConfirmDialog(this,
                        "You selected '" + regTypeVal + "' but did not provide incident/emergency details.\n" +
                                "It is strongly recommended to provide at least a chief complaint or initial vitals for emergency/trauma cases.\n\n"
                                +
                                "Do you want to continue saving the patient without incident details?",
                        "Missing Incident Details", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (proceed != JOptionPane.YES_OPTION) {
                    // User chose to edit/cancel â€” do not update the patient.
                    return;
                }
            }
            // perform save and close
            showOut(PatientService.editExtended(id, name.getText(), age.getText(), birthday.getText(), genderVal,
                    contact.getText(),
                    address.getText(), String.valueOf(patientTypeCombo.getSelectedItem()),
                    regTypeVal, incidentTVal, broughtByVal, initialBpVal, initialHrVal, initialSpo2Val, chiefVal,
                    allergies.getText(), meds.getText(), history.getText(), String.valueOf(smoking.getSelectedItem()),
                    String.valueOf(alcohol.getSelectedItem()), occupationVal, providerVal, insId.getText(),
                    policyHolder.getText(), policyDob.getText(), relationVal));
            // refresh table and UI
            refresh();
            editDialog.dispose();
        });

        cancelBtn2.addActionListener(ev -> editDialog.dispose());

        newVisitBtn.addActionListener(ev -> {
            // Open the new visit dialog for this patient
            editDialog.dispose(); // Close the edit dialog first
            showNewVisitDialog(id, p);
        });

        // pack and ensure default size is generous so Save and other UI are visible
        editDialog.pack();
        if (editDialog.getWidth() < 900)
            editDialog.setSize(980, 720);
        editDialog.setLocationRelativeTo(this);
        editDialog.setVisible(true);
        // NOTE: removed automatic attach prompt after Edit â€” attachment/imm
        // operations
        // should be performed explicitly via the popup actions.
    }

    private void deletePatient(JTable t) {
        int i = t.getSelectedRow();
        if (i < 0)
            return;
        String id = String.valueOf(t.getValueAt(i, 0));
        Patient p = DataStore.patients.get(id);
        if (p == null) {
            JOptionPane.showMessageDialog(this, "Patient not found", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (p.isActive) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Deactivate patient " + p.name + " (" + id + ")?\n\n" +
                            "Note: Patient record will be preserved in the database\n" +
                            "but hidden from active lists. You can reactivate later.",
                    "Confirm Deactivation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                showOut(PatientService.deactivate(id));
                refresh();
            }
        } else {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Reactivate patient " + p.name + " (" + id + ")?",
                    "Confirm Reactivation",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                showOut(PatientService.reactivate(id));
                refresh();
            }
        }
    }

    private void showNewVisitDialog(String patientId, Patient p) {
        JDialog visitDialog = new JDialog((java.awt.Frame) SwingUtilities.getWindowAncestor(this),
                "Record New Arrival/Visit - " + p.name, true);
        visitDialog.setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 5, 5, 5);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // Info header with permanent status warning if applicable
        String headerText = "<html><b>Recording a new visit for patient: " + p.name + " (" + patientId + ")</b><br>" +
                "This will create a NEW arrival record without modifying the original one.";
        if (p.isOutpatientPermanent) {
            headerText += "<br><span style='color: red; font-weight: bold;'>âš  Patient has PERMANENT OUTPATIENT status - cannot be changed to INPATIENT.</span>";
        }
        headerText += "</html>";
        JLabel headerLbl = new JLabel(headerText);
        headerLbl.setFont(Theme.APP_FONT.deriveFont(Font.PLAIN, 12f));
        gc.gridx = 0;
        gc.gridy = row++;
        gc.gridwidth = 2;
        mainPanel.add(headerLbl, gc);
        gc.gridwidth = 1;

        // Registration Type
        JLabel regLbl = new JLabel("Type of Arrival:");
        regLbl.setFont(Theme.APP_FONT.deriveFont(Font.BOLD, 11f));
        String[] regOptions = new String[] {
                "Walk-in Patient",
                "Emergency Patient",
                "Accident / Trauma Case",
                "Referral Patient",
                "OB / Maternity Case",
                "Surgical Admission",
                "Pediatric Patient",
                "Geriatric Patient",
                "Telemedicine / Virtual Consult"
        };
        JComboBox<String> regCombo = new JComboBox<>(regOptions);
        gc.gridx = 0;
        gc.gridy = row;
        mainPanel.add(regLbl, gc);
        gc.gridx = 1;
        mainPanel.add(regCombo, gc);
        row++;

        // Incident Time
        JLabel timeLbl = new JLabel("Time of Incident (HH:mm):");
        JTextField timeField = new JTextField(10);
        gc.gridx = 0;
        gc.gridy = row;
        mainPanel.add(timeLbl, gc);
        gc.gridx = 1;
        mainPanel.add(timeField, gc);
        row++;

        // Brought By
        JLabel broughtLbl = new JLabel("Brought By:");
        JCheckBox cbAmb = new JCheckBox("Ambulance");
        JCheckBox cbFam = new JCheckBox("Family");
        JCheckBox cbBy = new JCheckBox("Bystander");
        JCheckBox cbPol = new JCheckBox("Police");
        JPanel broughtPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        broughtPanel.add(cbAmb);
        broughtPanel.add(cbFam);
        broughtPanel.add(cbBy);
        broughtPanel.add(cbPol);
        gc.gridx = 0;
        gc.gridy = row;
        mainPanel.add(broughtLbl, gc);
        gc.gridx = 1;
        mainPanel.add(broughtPanel, gc);
        row++;

        // Vitals
        JLabel bpLbl = new JLabel("Initial BP (Sys/Dia):");
        JTextField bpSys = new JTextField(5);
        JTextField bpDia = new JTextField(5);
        JPanel bpPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bpPanel.add(bpSys);
        bpPanel.add(new JLabel("/"));
        bpPanel.add(bpDia);
        gc.gridx = 0;
        gc.gridy = row;
        mainPanel.add(bpLbl, gc);
        gc.gridx = 1;
        mainPanel.add(bpPanel, gc);
        row++;

        JLabel hrLbl = new JLabel("Heart Rate:");
        JTextField hrField = new JTextField(10);
        gc.gridx = 0;
        gc.gridy = row;
        mainPanel.add(hrLbl, gc);
        gc.gridx = 1;
        mainPanel.add(hrField, gc);
        row++;

        JLabel spo2Lbl = new JLabel("SpO2 (%):");
        JTextField spo2Field = new JTextField(10);
        gc.gridx = 0;
        gc.gridy = row;
        mainPanel.add(spo2Lbl, gc);
        gc.gridx = 1;
        mainPanel.add(spo2Field, gc);
        row++;

        // Chief Complaint
        JLabel chiefLbl = new JLabel("Chief Complaint:");
        JTextArea chiefArea = new JTextArea(4, 30);
        chiefArea.setLineWrap(true);
        chiefArea.setWrapStyleWord(true);
        JScrollPane chiefScroll = new JScrollPane(chiefArea);
        gc.gridx = 0;
        gc.gridy = row;
        gc.anchor = GridBagConstraints.NORTHWEST;
        mainPanel.add(chiefLbl, gc);
        gc.gridx = 1;
        gc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(chiefScroll, gc);
        row++;

        // Attending Doctor
        JLabel docLbl = new JLabel("Attending Doctor:");
        JComboBox<String> docCombo = new JComboBox<>();
        docCombo.addItem("(None)");
        for (Staff s : DataStore.staff.values()) {
            if (s.role == hpms.model.StaffRole.DOCTOR) {
                docCombo.addItem(s.id + " - " + s.name);
            }
        }
        gc.gridx = 0;
        gc.gridy = row;
        gc.anchor = GridBagConstraints.WEST;
        mainPanel.add(docLbl, gc);
        gc.gridx = 1;
        mainPanel.add(docCombo, gc);
        row++;

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("Record Visit");
        JButton cancelBtn = new JButton("Cancel");
        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);

        visitDialog.add(new JScrollPane(mainPanel), BorderLayout.CENTER);
        visitDialog.add(btnPanel, BorderLayout.SOUTH);

        saveBtn.addActionListener(ev -> {
            String regType = String.valueOf(regCombo.getSelectedItem());
            String incidentTime = timeField.getText().trim();

            java.util.List<String> broughtList = new java.util.ArrayList<>();
            if (cbAmb.isSelected())
                broughtList.add("Ambulance");
            if (cbFam.isSelected())
                broughtList.add("Family");
            if (cbBy.isSelected())
                broughtList.add("Bystander");
            if (cbPol.isSelected())
                broughtList.add("Police");
            String broughtBy = String.join(";", broughtList);

            String initialBp = (bpSys.getText().trim().isEmpty() || bpDia.getText().trim().isEmpty()) ? ""
                    : (bpSys.getText().trim() + "/" + bpDia.getText().trim());
            String initialHr = hrField.getText().trim();
            String initialSpo2 = spo2Field.getText().trim();
            String chiefComplaint = chiefArea.getText().trim();

            String doctorStr = String.valueOf(docCombo.getSelectedItem());
            String attendingDoctor = null;
            if (!doctorStr.equals("(None)") && doctorStr.contains(" - ")) {
                attendingDoctor = doctorStr.split(" - ")[0];
            }

            // Create the new visit record
            List<String> result = hpms.service.VisitService.createVisit(patientId, regType,
                    incidentTime, broughtBy, initialBp, initialHr, initialSpo2,
                    chiefComplaint, attendingDoctor);

            showOut(result);
            visitDialog.dispose();
            refresh();
        });

        cancelBtn.addActionListener(ev -> visitDialog.dispose());

        visitDialog.pack();
        visitDialog.setLocationRelativeTo(this);
        visitDialog.setVisible(true);
    }

    private void showMedicalDocumentFolderDialog(String patientId, Patient p) {
        JDialog folderDialog = new JDialog((java.awt.Frame) SwingUtilities.getWindowAncestor(this),
                "Medical Document Folder - " + p.name + " (" + patientId + ")", true);
        folderDialog.setLayout(new BorderLayout(10, 10));
        folderDialog.setSize(1000, 650);

        // Create the medical document folder panel
        hpms.ui.components.MedicalDocumentFolderPanel folderPanel = new hpms.ui.components.MedicalDocumentFolderPanel(
                patientId);

        // Wire up button actions
        folderPanel.getUploadButton().addActionListener(ae -> {
            javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
            fc.setMultiSelectionEnabled(false);
            fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "Medical Documents (PDF, Images, DICOM)", "pdf", "jpg", "jpeg", "png", "dicom", "dcm",
                    "docx", "xlsx", "txt"));

            if (fc.showOpenDialog(folderDialog) == javax.swing.JFileChooser.APPROVE_OPTION) {
                java.io.File selectedFile = fc.getSelectedFile();

                // Show dialog to get file type and category
                JDialog uploadDialog = new JDialog(folderDialog, "Upload File Details", true);
                uploadDialog.setLayout(new GridBagLayout());
                GridBagConstraints gc = new GridBagConstraints();
                gc.insets = new Insets(5, 5, 5, 5);
                gc.anchor = GridBagConstraints.WEST;
                gc.fill = GridBagConstraints.HORIZONTAL;

                int row = 0;

                JLabel fileNameLbl = new JLabel("File: " + selectedFile.getName());
                fileNameLbl.setFont(Theme.APP_FONT.deriveFont(Font.BOLD, 10f));
                gc.gridx = 0;
                gc.gridy = row++;
                gc.gridwidth = 2;
                uploadDialog.add(fileNameLbl, gc);
                gc.gridwidth = 1;

                JLabel categoryLbl = new JLabel("Category:");
                categoryLbl.setFont(Theme.APP_FONT.deriveFont(9f));
                gc.gridx = 0;
                gc.gridy = row;
                uploadDialog.add(categoryLbl, gc);

                JComboBox<String> categoryCombo = new JComboBox<>(
                        hpms.service.AttachmentService.getAvailableCategories().toArray(new String[0]));
                categoryCombo.setSelectedIndex(0);
                gc.gridx = 1;
                uploadDialog.add(categoryCombo, gc);
                row++;

                JLabel typeLabel = new JLabel("File Type:");
                typeLabel.setFont(Theme.APP_FONT.deriveFont(9f));
                gc.gridx = 0;
                gc.gridy = row;
                uploadDialog.add(typeLabel, gc);

                JComboBox<String> typeCombo = new JComboBox<>();
                gc.gridx = 1;
                uploadDialog.add(typeCombo, gc);
                row++;

                JLabel descLbl = new JLabel("Description:");
                descLbl.setFont(Theme.APP_FONT.deriveFont(9f));
                gc.gridx = 0;
                gc.gridy = row;
                gc.anchor = GridBagConstraints.NORTHWEST;
                uploadDialog.add(descLbl, gc);

                JTextArea descArea = new JTextArea(3, 40);
                descArea.setLineWrap(true);
                descArea.setWrapStyleWord(true);
                gc.gridx = 1;
                gc.anchor = GridBagConstraints.CENTER;
                uploadDialog.add(new JScrollPane(descArea), gc);
                row++;

                // Update type combo based on category selection
                categoryCombo.addItemListener(ev -> {
                    String selectedCategory = String.valueOf(categoryCombo.getSelectedItem());
                    List<String> types = hpms.service.AttachmentService.getFileTypesForCategory(selectedCategory);
                    typeCombo.removeAllItems();
                    for (String type : types) {
                        typeCombo.addItem(type);
                    }
                });
                // Initialize type combo
                categoryCombo.setSelectedIndex(0);

                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                JButton uploadBtn = new JButton("Upload");
                JButton cancelBtn = new JButton("Cancel");
                buttonPanel.add(cancelBtn);
                buttonPanel.add(uploadBtn);

                gc.gridx = 0;
                gc.gridy = row;
                gc.gridwidth = 2;
                gc.anchor = GridBagConstraints.EAST;
                uploadDialog.add(buttonPanel, gc);

                uploadBtn.addActionListener(uploadAct -> {
                    String category = String.valueOf(categoryCombo.getSelectedItem());
                    String fileType = String.valueOf(typeCombo.getSelectedItem());
                    String description = descArea.getText().trim();
                    String uploadedBy = (hpms.auth.AuthService.current != null) ? hpms.auth.AuthService.current.username
                            : "Unknown";

                    List<String> result = hpms.service.AttachmentService.uploadAttachment(
                            patientId, selectedFile.getName(), selectedFile.getAbsolutePath(),
                            fileType, category, description, uploadedBy);

                    showOut(result);
                    folderPanel.refreshAttachments();
                    uploadDialog.dispose();
                });

                cancelBtn.addActionListener(cancelAct -> uploadDialog.dispose());

                uploadDialog.pack();
                uploadDialog.setLocationRelativeTo(folderDialog);
                uploadDialog.setVisible(true);
            }
        });

        folderPanel.getViewButton().addActionListener(ae -> {
            FileAttachment att = folderPanel.getSelectedAttachment();
            if (att == null) {
                JOptionPane.showMessageDialog(folderDialog, "Please select a file to preview");
                return;
            }

            java.io.File file = new java.io.File(att.filePath);
            if (!file.exists()) {
                JOptionPane.showMessageDialog(folderDialog, "File not found: " + att.filePath,
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Try to open with default application
            try {
                if (java.awt.Desktop.isDesktopSupported()) {
                    java.awt.Desktop.getDesktop().open(file);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(folderDialog, "Cannot open file: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        folderPanel.getDownloadButton().addActionListener(ae -> {
            FileAttachment att = folderPanel.getSelectedAttachment();
            if (att == null) {
                JOptionPane.showMessageDialog(folderDialog, "Please select a file to download");
                return;
            }

            javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
            fc.setSelectedFile(new java.io.File(att.fileName));
            if (fc.showSaveDialog(folderDialog) == javax.swing.JFileChooser.APPROVE_OPTION) {
                java.io.File destFile = fc.getSelectedFile();
                try {
                    java.nio.file.Files.copy(
                            new java.io.File(att.filePath).toPath(),
                            destFile.toPath(),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    JOptionPane.showMessageDialog(folderDialog,
                            "File downloaded successfully to:\n" + destFile.getAbsolutePath());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(folderDialog, "Error downloading file: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        folderPanel.getDeleteButton().addActionListener(ae -> {
            FileAttachment att = folderPanel.getSelectedAttachment();
            if (att == null) {
                JOptionPane.showMessageDialog(folderDialog, "Please select a file to delete");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(folderDialog,
                    "Delete file: " + att.fileName + "?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                List<String> result = hpms.service.AttachmentService.deleteAttachment(att.id);
                showOut(result);
                folderPanel.refreshAttachments();
            }
        });

        folderDialog.add(folderPanel, BorderLayout.CENTER);

        // Close button
        JPanel closePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(ae -> folderDialog.dispose());
        closePanel.add(closeBtn);
        folderDialog.add(closePanel, BorderLayout.SOUTH);

        folderDialog.setLocationRelativeTo(this);
        folderDialog.setVisible(true);
    }

    private void showOut(List<String> out) {
        JOptionPane.showMessageDialog(this, String.join("\n", out));
    }

    private void updateOverviewFromSelection(JTable t) {
        int i = t.getSelectedRow();
        if (i < 0)
            return;
        String id = String.valueOf(t.getValueAt(i, 0));
        Patient p = DataStore.patients.get(id);
        if (p == null)
            return;
        nameLabel.setText("Name: " + p.name);
        ageGenderLabel.setText("Age/Gender: " + p.age + " / " + p.gender);
        contactLabel.setText("Contact: " + p.contact);
        Room assigned = findRoomForPatient(p.id);
        roomLabel
                .setText("Room: " + (assigned == null ? "Not Assigned" : (assigned.id + " (" + assigned.status + ")")));

        // Get patient status and update UI controls based on status
        PatientStatus status = PatientStatusService.getStatus(p.id);

        // Disable transfer button for outpatients - enforce outpatient rules in UI
        if (status == PatientStatus.OUTPATIENT) {
            transferBtn.setEnabled(false);
            transferBtn.setToolTipText("Room transfer not available for outpatients");
        } else {
            transferBtn.setEnabled(true);
            transferBtn.setToolTipText("Transfer patient to another room");
        }

        Appointment latest = latestAppointmentForPatient(p.id);
        if (latest != null) {
            Staff s = DataStore.staff.get(latest.staffId);
            String sName = s == null ? latest.staffId : s.name;
            String dep = s == null ? latest.department : s.department;
            doctorLabel.setText("Doctor: " + sName + " (" + dep + ")");
            lastVisitLabel.setText("Last Visit: " + latest.dateTime.toLocalDate());
        } else {
            doctorLabel.setText("Doctor: ");
            lastVisitLabel.setText("Last Visit: ");
        }
        PatientStatus st = PatientStatusService.getStatus(p.id);
        statusValueLabel.setText("Status: " + st);
        if (statusCombo != null)
            statusCombo.setSelectedItem(st.name());
        java.util.List<String> alerts = CommunicationService.alerts(p.id);
        notesArea.setText(alerts.isEmpty() ? "None" : String.join("\n", alerts));
        // allergies and vitals
        if (p.allergies != null && !p.allergies.trim().isEmpty()) {
            allergiesLabel.setText("Allergies: " + p.allergies);
            allergiesLabel.setForeground(new Color(150, 0, 0));
        } else {
            allergiesLabel.setText("Allergies: None");
            allergiesLabel.setForeground(Theme.TEXT);
        }
        StringBuilder vit = new StringBuilder();
        // set separate Height / Weight info in the overview card
        String regText = (p.registrationType == null || p.registrationType.isEmpty() ? "N/A" : p.registrationType);
        // If this is an emergency/trauma type but missing incident details, show a
        // visible warning
        boolean emergencyType = "Emergency Patient".equals(p.registrationType)
                || "Accident / Trauma Case".equals(p.registrationType);
        boolean hasIncidentInfo = (p.incidentTime != null && !p.incidentTime.trim().isEmpty())
                || (p.broughtBy != null && !p.broughtBy.trim().isEmpty())
                || (p.initialBp != null && !p.initialBp.trim().isEmpty())
                || (p.initialHr != null && !p.initialHr.trim().isEmpty())
                || (p.initialSpo2 != null && !p.initialSpo2.trim().isEmpty())
                || (p.chiefComplaint != null && !p.chiefComplaint.trim().isEmpty());
        if (emergencyType && !hasIncidentInfo) {
            registrationLabel.setText("Registration: " + regText + " âš  Incident details incomplete");
            registrationLabel.setForeground(new Color(200, 40, 40));
            String tooltip = "Emergency/trauma registration detected â€” required incident details are missing (time, brought-by, initial vitals, or chief complaint).";
            registrationLabel.setToolTipText(tooltip);
        } else {
            registrationLabel.setText("Registration: " + regText);
            registrationLabel.setForeground(Theme.TEXT);
            registrationLabel.setToolTipText(null);
        }
        Double bmiValue = p.getBmi();
        if (bmiValue != null) {
            String category;
            String colorHex;
            if (bmiValue < 18.5) {
                category = "Underweight";
                colorHex = "#2E86C1";
            } // blue
            else if (bmiValue < 25.0) {
                category = "Normal";
                colorHex = "#27AE60";
            } // green
            else if (bmiValue < 30.0) {
                category = "Overweight";
                colorHex = "#F39C12";
            } // orange
            else {
                category = "Obese";
                colorHex = "#E74C3C";
            } // red
              // Use small HTML snippet for inline color
            vit.append(String.format(Locale.US,
                    "<span style='font-weight:bold;'>BMI:%.2f</span> <span style='color:%s;'>(%s)</span>", bmiValue,
                    colorHex, category));
        }
        if (p.bloodPressure != null && !p.bloodPressure.isEmpty()) {
            if (vit.length() > 0)
                vit.append(" <span style='color:#666;'>BP:" + p.bloodPressure + "</span>");
            else
                vit.append("BP:" + p.bloodPressure);
        }
        if (vit.length() == 0) {
            vitalsLabel.setText("Vitals: N/A");
        } else {
            // wrap as HTML so color spans render
            vitalsLabel.setText("<html>Vitals: " + vit.toString() + "</html>");
        }
    }

    // helpers used by popup menu to reuse the clinical dialogs
    private void showClinicalDialogFor(String id) {
        if (id == null)
            return;
        Patient p = DataStore.patients.get(id);
        if (p == null)
            return;
        JTextField height = new JTextField(p.heightCm == null ? "" : String.valueOf(p.heightCm));
        JTextField weight = new JTextField(p.weightKg == null ? "" : String.valueOf(p.weightKg));
        JTextField bp = new JTextField(p.bloodPressure == null ? "" : p.bloodPressure);
        JTextArea note = new JTextArea(4, 40);

        // --- X-RAY upload / summary ---
        JPanel xrayPanel = new JPanel(new GridBagLayout());
        xrayPanel.setOpaque(false);
        GridBagConstraints xc = new GridBagConstraints();
        xc.insets = new Insets(4, 4, 4, 4);
        xc.anchor = GridBagConstraints.WEST;
        xc.fill = GridBagConstraints.HORIZONTAL;
        xc.weightx = 1.0;
        JTextField xrayPath = new JTextField(p.xrayFilePath == null ? "" : p.xrayFilePath);
        xrayPath.setEditable(false);
        xrayPath.setPreferredSize(new Dimension(420, 24));
        JTextArea xNotes = new JTextArea(p.xraySummary == null ? "" : p.xraySummary, 3, 60);
        xNotes.setLineWrap(true);
        xNotes.setWrapStyleWord(true);
        JButton xrayBrowse = new JButton("Browse...");
        xrayBrowse.addActionListener(ae -> {
            javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
            fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Images/DICOM", "jpg", "jpeg", "png",
                    "dcm", "dicom"));
            if (fc.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
                xrayPath.setText(fc.getSelectedFile().getAbsolutePath());
                String summary = hpms.util.TestSummaryExtractor.extractSummary(xrayPath.getText(), "xray");
                if (summary != null && xNotes.getText().trim().isEmpty())
                    xNotes.setText(summary);
            }
        });
        JComboBox<String> xrayStatus = new JComboBox<>(
                new String[] { p.xrayStatus == null ? "Not Uploaded" : p.xrayStatus, "Not Uploaded", "Uploaded",
                        "Reviewed", "Critical" });
        // xray findings checklist
        JPanel xFind = new JPanel(new FlowLayout(FlowLayout.LEFT));
        xFind.setOpaque(false);
        JCheckBox fxBroken = new JCheckBox("Broken Bone");
        JCheckBox fxPneum = new JCheckBox("Pneumonia");
        JCheckBox fxNormal = new JCheckBox("Normal");
        JCheckBox fxFract = new JCheckBox("Fracture");
        JCheckBox fxTumor = new JCheckBox("Tumor/Mass");
        xFind.add(fxBroken);
        xFind.add(fxPneum);
        xFind.add(fxNormal);
        xFind.add(fxFract);
        xFind.add(fxTumor);
        JPanel xLoc = new JPanel(new FlowLayout(FlowLayout.LEFT));
        xLoc.setOpaque(false);
        JCheckBox xlLeftArm = new JCheckBox("Left Arm");
        JCheckBox xlRightLeg = new JCheckBox("Right Leg");
        JCheckBox xlRibs = new JCheckBox("Ribs");
        JCheckBox xlSkull = new JCheckBox("Skull");
        JCheckBox xlSpine = new JCheckBox("Spine");
        JCheckBox xlChest = new JCheckBox("Chest");
        xLoc.add(xlLeftArm);
        xLoc.add(xlRightLeg);
        xLoc.add(xlRibs);
        xLoc.add(xlSkull);
        xLoc.add(xlSpine);
        xLoc.add(xlChest);
        JPanel xSev = new JPanel(new FlowLayout(FlowLayout.LEFT));
        xSev.setOpaque(false);
        JCheckBox xsMild = new JCheckBox("Mild");
        JCheckBox xsMod = new JCheckBox("Moderate");
        JCheckBox xsSev = new JCheckBox("Severe");
        xSev.add(xsMild);
        xSev.add(xsMod);
        xSev.add(xsSev);

        xc.gridy = 0;
        xc.gridx = 0;
        xrayPanel.add(new JLabel("X-ray Upload (Accepts: .jpg, .png, .dicom)"), xc);
        xc.gridy = 1;
        xc.gridx = 0;
        xrayPanel.add(xrayPath, xc);
        xc.gridy = 1;
        xc.gridx = 1;
        xrayPanel.add(xrayBrowse, xc);
        xc.gridy = 2;
        xc.gridx = 0;
        xrayPanel.add(new JLabel("Status:"), xc);
        xc.gridx = 1;
        xrayPanel.add(xrayStatus, xc);
        xc.gridy = 3;
        xc.gridx = 0;
        xrayPanel.add(new JLabel("Findings:"), xc);
        xc.gridx = 1;
        xrayPanel.add(xFind, xc);
        xc.gridy = 4;
        xc.gridx = 0;
        xrayPanel.add(new JLabel("Location:"), xc);
        xc.gridx = 1;
        xrayPanel.add(xLoc, xc);
        xc.gridy = 5;
        xc.gridx = 0;
        xrayPanel.add(new JLabel("Severity:"), xc);
        xc.gridx = 1;
        xrayPanel.add(xSev, xc);
        xc.gridy = 6;
        xc.gridx = 0;
        xrayPanel.add(new JLabel("Notes (editable):"), xc);
        xc.gridx = 1;
        xrayPanel.add(new JScrollPane(xNotes), xc);

        // --- Stool upload / summary ---
        JPanel stoolPanel = new JPanel(new GridBagLayout());
        stoolPanel.setOpaque(false);
        GridBagConstraints sc = new GridBagConstraints();
        sc.insets = new Insets(4, 4, 4, 4);
        sc.anchor = GridBagConstraints.WEST;
        sc.fill = GridBagConstraints.HORIZONTAL;
        sc.weightx = 1.0;
        JTextField stoolPath = new JTextField(p.stoolFilePath == null ? "" : p.stoolFilePath);
        stoolPath.setEditable(false);
        stoolPath.setPreferredSize(new Dimension(420, 24));
        JTextArea stoolNotes = new JTextArea(p.stoolSummary == null ? "" : p.stoolSummary, 3, 60);
        stoolNotes.setLineWrap(true);
        stoolNotes.setWrapStyleWord(true);
        JButton stoolBrowse = new JButton("Browse...");
        stoolBrowse.addActionListener(ae -> {
            javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
            fc.setFileFilter(
                    new javax.swing.filechooser.FileNameExtensionFilter("PDF/Images", "pdf", "jpg", "jpeg", "png"));
            if (fc.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
                stoolPath.setText(fc.getSelectedFile().getAbsolutePath());
                String summary = hpms.util.TestSummaryExtractor.extractSummary(stoolPath.getText(), "stool");
                if (summary != null && stoolNotes.getText().trim().isEmpty())
                    stoolNotes.setText(summary);
            }
        });
        JComboBox<String> stoolStatus = new JComboBox<>(
                new String[] { p.stoolStatus == null ? "Not Uploaded" : p.stoolStatus, "Not Uploaded", "Uploaded",
                        "Reviewed", "Critical" });
        JPanel stoolFind = new JPanel(new FlowLayout(FlowLayout.LEFT));
        stoolFind.setOpaque(false);
        JCheckBox spNone = new JCheckBox("None Detected");
        JCheckBox spPresent = new JCheckBox("Parasites Present");
        JCheckBox sbNormal = new JCheckBox("Normal");
        JCheckBox sbAbnormal = new JCheckBox("Abnormal (E. coli)");
        JCheckBox soOccult = new JCheckBox("Occult Blood Positive");
        stoolFind.add(spNone);
        stoolFind.add(spPresent);
        stoolFind.add(sbNormal);
        stoolFind.add(sbAbnormal);
        stoolFind.add(soOccult);

        sc.gridy = 0;
        sc.gridx = 0;
        stoolPanel.add(new JLabel("Stool Exam Upload (Accepts: .pdf, .jpg, .png)"), sc);
        sc.gridy = 1;
        sc.gridx = 0;
        stoolPanel.add(stoolPath, sc);
        sc.gridy = 1;
        sc.gridx = 1;
        stoolPanel.add(stoolBrowse, sc);
        sc.gridy = 2;
        sc.gridx = 0;
        stoolPanel.add(new JLabel("Status:"), sc);
        sc.gridx = 1;
        stoolPanel.add(stoolStatus, sc);
        sc.gridy = 3;
        sc.gridx = 0;
        stoolPanel.add(new JLabel("Summary / Findings:"), sc);
        sc.gridx = 1;
        stoolPanel.add(stoolFind, sc);
        sc.gridy = 4;
        sc.gridx = 0;
        stoolPanel.add(new JLabel("Notes (editable):"), sc);
        sc.gridx = 1;
        stoolPanel.add(new JScrollPane(stoolNotes), sc);

        // --- Urinalysis upload / summary ---
        JPanel urinePanel = new JPanel(new GridBagLayout());
        urinePanel.setOpaque(false);
        GridBagConstraints uc = new GridBagConstraints();
        uc.insets = new Insets(4, 4, 4, 4);
        uc.anchor = GridBagConstraints.WEST;
        uc.fill = GridBagConstraints.HORIZONTAL;
        uc.weightx = 1.0;
        JTextField urinePath = new JTextField(p.urineFilePath == null ? "" : p.urineFilePath);
        urinePath.setEditable(false);
        urinePath.setPreferredSize(new Dimension(420, 24));
        JTextArea urineNotes = new JTextArea(p.urineSummary == null ? "" : p.urineSummary, 3, 60);
        urineNotes.setLineWrap(true);
        urineNotes.setWrapStyleWord(true);
        JButton urineBrowse = new JButton("Browse...");
        urineBrowse.addActionListener(ae -> {
            javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
            fc.setFileFilter(
                    new javax.swing.filechooser.FileNameExtensionFilter("PDF/Images", "pdf", "jpg", "jpeg", "png"));
            if (fc.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
                urinePath.setText(fc.getSelectedFile().getAbsolutePath());
                String summary = hpms.util.TestSummaryExtractor.extractSummary(urinePath.getText(), "urine");
                if (summary != null && urineNotes.getText().trim().isEmpty())
                    urineNotes.setText(summary);
            }
        });
        JComboBox<String> urineStatus = new JComboBox<>(
                new String[] { p.urineStatus == null ? "Not Uploaded" : p.urineStatus, "Not Uploaded", "Uploaded",
                        "Reviewed", "Critical" });
        JPanel urineFind = new JPanel(new FlowLayout(FlowLayout.LEFT));
        urineFind.setOpaque(false);
        JCheckBox uClear = new JCheckBox("Clear");
        JCheckBox uCloud = new JCheckBox("Cloudy");
        JCheckBox uBlood = new JCheckBox("Blood-tinged");
        JCheckBox uGlucoseNeg = new JCheckBox("Glucose Negative");
        JCheckBox uGlucosePos = new JCheckBox("Glucose Positive");
        JCheckBox uProteinNeg = new JCheckBox("Protein Negative");
        JCheckBox uProteinPos = new JCheckBox("Protein Positive (+1)");
        JCheckBox uWBCHigh = new JCheckBox("WBC High (15-20)");
        urineFind.add(uClear);
        urineFind.add(uCloud);
        urineFind.add(uBlood);
        urineFind.add(uGlucoseNeg);
        urineFind.add(uGlucosePos);
        urineFind.add(uProteinNeg);
        urineFind.add(uProteinPos);
        urineFind.add(uWBCHigh);

        uc.gridy = 0;
        uc.gridx = 0;
        urinePanel.add(new JLabel("Urinalysis Upload (Accepts: .pdf, .jpg, .png)"), uc);
        uc.gridy = 1;
        uc.gridx = 0;
        urinePanel.add(urinePath, uc);
        uc.gridy = 1;
        uc.gridx = 1;
        urinePanel.add(urineBrowse, uc);
        uc.gridy = 2;
        uc.gridx = 0;
        urinePanel.add(new JLabel("Status:"), uc);
        uc.gridx = 1;
        urinePanel.add(urineStatus, uc);
        uc.gridy = 3;
        uc.gridx = 0;
        urinePanel.add(new JLabel("Findings:"), uc);
        uc.gridx = 1;
        urinePanel.add(urineFind, uc);
        uc.gridy = 4;
        uc.gridx = 0;
        urinePanel.add(new JLabel("Notes (editable):"), uc);
        uc.gridx = 1;
        urinePanel.add(new JScrollPane(urineNotes), uc);

        // --- Blood test upload / summary ---
        JPanel bloodPanel = new JPanel(new GridBagLayout());
        bloodPanel.setOpaque(false);
        GridBagConstraints bc = new GridBagConstraints();
        bc.insets = new Insets(4, 4, 4, 4);
        bc.anchor = GridBagConstraints.WEST;
        bc.fill = GridBagConstraints.HORIZONTAL;
        bc.weightx = 1.0;
        JTextField bloodPath = new JTextField(p.bloodFilePath == null ? "" : p.bloodFilePath);
        bloodPath.setEditable(false);
        bloodPath.setPreferredSize(new Dimension(420, 24));
        JTextArea bloodNotes = new JTextArea(p.bloodSummary == null ? "" : p.bloodSummary, 3, 60);
        bloodNotes.setLineWrap(true);
        bloodNotes.setWrapStyleWord(true);
        JButton bloodBrowse = new JButton("Browse...");
        bloodBrowse.addActionListener(ae -> {
            javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
            fc.setFileFilter(
                    new javax.swing.filechooser.FileNameExtensionFilter("PDF/Images", "pdf", "jpg", "jpeg", "png"));
            if (fc.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
                bloodPath.setText(fc.getSelectedFile().getAbsolutePath());
                String summary = hpms.util.TestSummaryExtractor.extractSummary(bloodPath.getText(), "blood");
                if (summary != null && bloodNotes.getText().trim().isEmpty())
                    bloodNotes.setText(summary);
            }
        });
        JComboBox<String> bloodStatus = new JComboBox<>(
                new String[] { p.bloodStatus == null ? "Not Uploaded" : p.bloodStatus, "Not Uploaded", "Uploaded",
                        "Reviewed", "Critical" });
        JPanel bloodFlags = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bloodFlags.setOpaque(false);
        JCheckBox bWBC = new JCheckBox("WBC Abnormal");
        JCheckBox bHGB = new JCheckBox("HGB Abnormal");
        JCheckBox bPLT = new JCheckBox("PLT Abnormal");
        JCheckBox bCRP = new JCheckBox("CRP/ESR Elevated");
        JCheckBox bCulture = new JCheckBox("Blood Culture Positive");
        bloodFlags.add(bWBC);
        bloodFlags.add(bHGB);
        bloodFlags.add(bPLT);
        bloodFlags.add(bCRP);
        bloodFlags.add(bCulture);

        bc.gridy = 0;
        bc.gridx = 0;
        bloodPanel.add(new JLabel("Blood Lab Upload (Accepts: .pdf, .jpg, .png)"), bc);
        bc.gridy = 1;
        bc.gridx = 0;
        bloodPanel.add(bloodPath, bc);
        bc.gridy = 1;
        bc.gridx = 1;
        bloodPanel.add(bloodBrowse, bc);
        bc.gridy = 2;
        bc.gridx = 0;
        bloodPanel.add(new JLabel("Status:"), bc);
        bc.gridx = 1;
        bloodPanel.add(bloodStatus, bc);
        bc.gridy = 3;
        bc.gridx = 0;
        bloodPanel.add(new JLabel("Findings:"), bc);
        bc.gridx = 1;
        bloodPanel.add(bloodFlags, bc);
        bc.gridy = 4;
        bc.gridx = 0;
        bloodPanel.add(new JLabel("Notes (editable):"), bc);
        bc.gridx = 1;
        bloodPanel.add(new JScrollPane(bloodNotes), bc);

        // Assemble main clinical panel (improved layout to avoid horizontal overflow)
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(new Color(250, 250, 250));
        // layout three compact columns (label above single-line field) so fields stay
        // short
        JPanel topVitals = new JPanel(new GridLayout(1, 3, 12, 0));
        topVitals.setOpaque(false);
        Dimension smallField = new Dimension(160, 26);
        height.setPreferredSize(smallField);
        height.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        weight.setPreferredSize(smallField);
        weight.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        bp.setPreferredSize(new Dimension(220, 26));
        bp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        JPanel col1 = new JPanel();
        col1.setOpaque(false);
        col1.setLayout(new BorderLayout(2, 2));
        col1.add(new JLabel("Height (cm)"), BorderLayout.NORTH);
        col1.add(height, BorderLayout.CENTER);
        JPanel col2 = new JPanel();
        col2.setOpaque(false);
        col2.setLayout(new BorderLayout(2, 2));
        col2.add(new JLabel("Weight (kg)"), BorderLayout.NORTH);
        col2.add(weight, BorderLayout.CENTER);
        JPanel col3 = new JPanel();
        col3.setOpaque(false);
        col3.setLayout(new BorderLayout(2, 2));
        col3.add(new JLabel("Blood Pressure"), BorderLayout.NORTH);
        col3.add(bp, BorderLayout.CENTER);
        topVitals.add(col1);
        topVitals.add(col2);
        topVitals.add(col3);
        topVitals.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        main.add(topVitals);
        main.add(Box.createVerticalStrut(6));
        main.add(new JLabel("Progress Note"));
        JScrollPane noteSP = new JScrollPane(note);
        noteSP.setPreferredSize(new Dimension(860, 160));
        noteSP.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));
        main.add(noteSP);
        main.add(Box.createVerticalStrut(10));

        main.add(new JSeparator());
        main.add(Box.createVerticalStrut(6));
        JLabel testsLbl = new JLabel("Medical Test Upload & Summary");
        testsLbl.setFont(Theme.APP_FONT.deriveFont(Font.BOLD, 12f));
        main.add(testsLbl);
        main.add(Box.createVerticalStrut(8));
        // limit widths so the panels wrap and don't force horizontal scrolling
        xrayPanel.setMaximumSize(new Dimension(760, Integer.MAX_VALUE));
        xrayPanel.setBorder(BorderFactory.createTitledBorder("X-ray"));
        xrayPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        stoolPanel.setMaximumSize(new Dimension(760, Integer.MAX_VALUE));
        stoolPanel.setBorder(BorderFactory.createTitledBorder("Stool Exam"));
        stoolPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        urinePanel.setMaximumSize(new Dimension(760, Integer.MAX_VALUE));
        urinePanel.setBorder(BorderFactory.createTitledBorder("Urinalysis"));
        urinePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        bloodPanel.setMaximumSize(new Dimension(760, Integer.MAX_VALUE));
        bloodPanel.setBorder(BorderFactory.createTitledBorder("Blood Lab"));
        bloodPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        main.setBorder(new javax.swing.border.EmptyBorder(10, 10, 10, 10));

        // preview labels (small thumbnails / text previews) for the uploaded files
        JLabel xrayPreview = new JLabel();
        xrayPreview.setPreferredSize(new Dimension(160, 100));
        xrayPreview.setBorder(new javax.swing.border.LineBorder(Color.GRAY));
        JLabel stoolPreview = new JLabel();
        stoolPreview.setPreferredSize(new Dimension(160, 100));
        stoolPreview.setBorder(new javax.swing.border.LineBorder(Color.GRAY));
        JLabel urinePreview = new JLabel();
        urinePreview.setPreferredSize(new Dimension(160, 100));
        urinePreview.setBorder(new javax.swing.border.LineBorder(Color.GRAY));
        JLabel bloodPreview = new JLabel();
        bloodPreview.setPreferredSize(new Dimension(160, 100));
        bloodPreview.setBorder(new javax.swing.border.LineBorder(Color.GRAY));

        // insert previews into each panel next to Browse control (if layout space
        // available)
        GridBagConstraints addPrev = new GridBagConstraints();
        addPrev.insets = new Insets(4, 4, 4, 4);
        addPrev.fill = GridBagConstraints.NONE;
        addPrev.anchor = GridBagConstraints.EAST;
        addPrev.gridy = 1;
        addPrev.gridx = 2;
        xrayPanel.add(xrayPreview, addPrev);
        addPrev.gridy = 1;
        addPrev.gridx = 2;
        stoolPanel.add(stoolPreview, addPrev);
        addPrev.gridy = 1;
        addPrev.gridx = 2;
        urinePanel.add(urinePreview, addPrev);
        addPrev.gridy = 1;
        addPrev.gridx = 2;
        bloodPanel.add(bloodPreview, addPrev);

        // helper for setting preview image or text
        java.util.function.BiConsumer<JLabel, String> setPreview = (lbl, path) -> {
            if (path == null || path.trim().isEmpty()) {
                lbl.setIcon(null);
                lbl.setText("No Preview");
                return;
            }
            try {
                java.io.File f = new java.io.File(path);
                if (!f.exists()) {
                    lbl.setIcon(null);
                    lbl.setText("No Preview");
                    return;
                }
                if (path.toLowerCase(Locale.ROOT).endsWith(".jpg") || path.toLowerCase(Locale.ROOT).endsWith(".jpeg")
                        || path.toLowerCase(Locale.ROOT).endsWith(".png")) {
                    ImageIcon ico = new ImageIcon(path);
                    Image scaled = ico.getImage().getScaledInstance(lbl.getPreferredSize().width,
                            lbl.getPreferredSize().height, Image.SCALE_SMOOTH);
                    lbl.setText("");
                    lbl.setIcon(new ImageIcon(scaled));
                } else {
                    lbl.setIcon(null);
                    lbl.setText("Preview not available");
                }
            } catch (Exception ex) {
                lbl.setIcon(null);
                lbl.setText("Preview error");
            }
        };

        // update previews when file picks occur
        xrayBrowse.addActionListener(ae -> {
            javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
            fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Images/DICOM", "jpg", "jpeg", "png",
                    "dcm", "dicom"));
            if (fc.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
                xrayPath.setText(fc.getSelectedFile().getAbsolutePath());
                String summary = hpms.util.TestSummaryExtractor.extractSummary(xrayPath.getText(), "xray");
                if (summary != null && xNotes.getText().trim().isEmpty())
                    xNotes.setText(summary);
                setPreview.accept(xrayPreview, xrayPath.getText());
            }
        });
        stoolBrowse.addActionListener(ae -> {
            javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
            fc.setFileFilter(
                    new javax.swing.filechooser.FileNameExtensionFilter("PDF/Images", "pdf", "jpg", "jpeg", "png"));
            if (fc.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
                stoolPath.setText(fc.getSelectedFile().getAbsolutePath());
                String summary = hpms.util.TestSummaryExtractor.extractSummary(stoolPath.getText(), "stool");
                if (summary != null && stoolNotes.getText().trim().isEmpty())
                    stoolNotes.setText(summary);
                setPreview.accept(stoolPreview, stoolPath.getText());
            }
        });
        urineBrowse.addActionListener(ae -> {
            javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
            fc.setFileFilter(
                    new javax.swing.filechooser.FileNameExtensionFilter("PDF/Images", "pdf", "jpg", "jpeg", "png"));
            if (fc.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
                urinePath.setText(fc.getSelectedFile().getAbsolutePath());
                String summary = hpms.util.TestSummaryExtractor.extractSummary(urinePath.getText(), "urine");
                if (summary != null && urineNotes.getText().trim().isEmpty())
                    urineNotes.setText(summary);
                setPreview.accept(urinePreview, urinePath.getText());
            }
        });
        bloodBrowse.addActionListener(ae -> {
            javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
            fc.setFileFilter(
                    new javax.swing.filechooser.FileNameExtensionFilter("PDF/Images", "pdf", "jpg", "jpeg", "png"));
            if (fc.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
                bloodPath.setText(fc.getSelectedFile().getAbsolutePath());
                String summary = hpms.util.TestSummaryExtractor.extractSummary(bloodPath.getText(), "blood");
                if (summary != null && bloodNotes.getText().trim().isEmpty())
                    bloodNotes.setText(summary);
                setPreview.accept(bloodPreview, bloodPath.getText());
            }
        });

        // Create a proper resizable JDialog instead of JOptionPane so layout is
        // predictable
        java.awt.Window owner = javax.swing.SwingUtilities.getWindowAncestor(this);
        final JDialog dlg = new JDialog((java.awt.Frame) owner, "Add Clinical Info to " + id, true);
        dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dlg.setLayout(new BorderLayout());
        JScrollPane sp = new JScrollPane(main);
        sp.setBorder(null);
        sp.setPreferredSize(new Dimension(920, 740));
        dlg.add(sp, BorderLayout.CENTER);

        // --- Add a tabbed view for each major test so they're easy to navigate ---
        JTabbedPane testsTabs = new JTabbedPane(JTabbedPane.TOP);
        xrayPanel.setBorder(null);
        stoolPanel.setBorder(null);
        urinePanel.setBorder(null);
        bloodPanel.setBorder(null);
        testsTabs.addTab("X-ray", xrayPanel);
        testsTabs.addTab("Stool Exam", stoolPanel);
        testsTabs.addTab("Urinalysis", urinePanel);
        testsTabs.addTab("Blood Test", bloodPanel);

        // Other attachments list (small panel)
        JPanel otherAttach = new JPanel(new BorderLayout(6, 6));
        otherAttach.setOpaque(false);
        otherAttach.setBorder(BorderFactory.createTitledBorder("Other Attachments"));
        DefaultListModel<String> attachModel = new DefaultListModel<>();
        JList<String> attachList = new JList<>(attachModel);
        attachList.setVisibleRowCount(4);
        JPanel attachControls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addAttach = new JButton("Add File");
        JButton remAttach = new JButton("Remove");
        attachControls.add(addAttach);
        attachControls.add(remAttach);
        otherAttach.add(attachControls, BorderLayout.NORTH);
        JScrollPane attachSP = new JScrollPane(attachList);
        attachSP.setPreferredSize(new Dimension(820, 96));
        attachSP.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        attachList.setFixedCellWidth(780);
        otherAttach.add(attachSP, BorderLayout.CENTER);

        // Add both tabs and Other attachments beneath the tests label in the main panel
        JPanel tabWrap = new JPanel(new BorderLayout(6, 6));
        tabWrap.setOpaque(false);
        tabWrap.add(testsTabs, BorderLayout.CENTER);
        tabWrap.add(otherAttach, BorderLayout.SOUTH);
        tabWrap.setPreferredSize(new Dimension(820, 360));
        tabWrap.setMinimumSize(new Dimension(760, 320));

        main.add(tabWrap);

        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionBar.setOpaque(false);
        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        actionBar.add(cancelBtn);
        actionBar.add(saveBtn);
        dlg.add(actionBar, BorderLayout.SOUTH);
        // Give the dialog a generous starting size and minimum so users won't need to
        // resize
        dlg.setPreferredSize(new Dimension(980, 820));
        dlg.setMinimumSize(new Dimension(860, 720));
        dlg.setResizable(true);

        // pack helpers (initial previews if files already exist)
        setPreview.accept(xrayPreview, xrayPath.getText());
        setPreview.accept(stoolPreview, stoolPath.getText());
        setPreview.accept(urinePreview, urinePath.getText());
        setPreview.accept(bloodPreview, bloodPath.getText());

        // attachment action wiring
        addAttach.addActionListener(ae -> {
            javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
            if (fc.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION)
                attachModel.addElement(fc.getSelectedFile().getAbsolutePath());
        });
        remAttach.addActionListener(ae -> {
            int i = attachList.getSelectedIndex();
            if (i >= 0)
                attachModel.remove(i);
        });

        // Save handler - replicate previous OK logic
        saveBtn.addActionListener(ae -> {
            // Build summaries from the selections if user didn't type manual notes
            String xSummary = xNotes.getText().trim();
            if (xSummary.isEmpty()) {
                StringBuilder s = new StringBuilder();
                if (fxBroken.isSelected())
                    s.append("Broken Bone; ");
                if (fxPneum.isSelected())
                    s.append("Pneumonia; ");
                if (fxFract.isSelected())
                    s.append("Fracture; ");
                if (fxTumor.isSelected())
                    s.append("Tumor/Mass; ");
                if (fxNormal.isSelected())
                    s.append("Normal; ");
                if (xlLeftArm.isSelected())
                    s.append("Location:Left Arm; ");
                if (xlRightLeg.isSelected())
                    s.append("Location:Right Leg; ");
                if (xlRibs.isSelected())
                    s.append("Location:Ribs; ");
                if (xlSkull.isSelected())
                    s.append("Location:Skull; ");
                if (xlSpine.isSelected())
                    s.append("Location:Spine; ");
                if (xlChest.isSelected())
                    s.append("Location:Chest; ");
                if (xsMild.isSelected())
                    s.append("Severity:Mild; ");
                if (xsMod.isSelected())
                    s.append("Severity:Moderate; ");
                if (xsSev.isSelected())
                    s.append("Severity:Severe; ");
                xSummary = s.toString().trim();
            }
            String stoolSummaryStr = stoolNotes.getText().trim();
            if (stoolSummaryStr.isEmpty()) {
                StringBuilder s = new StringBuilder();
                if (spNone.isSelected())
                    s.append("Parasites: None; ");
                if (spPresent.isSelected())
                    s.append("Parasites: Present; ");
                if (sbNormal.isSelected())
                    s.append("Bacteria: Normal; ");
                if (sbAbnormal.isSelected())
                    s.append("Bacteria: Abnormal; ");
                if (soOccult.isSelected())
                    s.append("Occult Blood: Positive; ");
                stoolSummaryStr = s.toString().trim();
            }
            String urineSummaryStr = urineNotes.getText().trim();
            if (urineSummaryStr.isEmpty()) {
                StringBuilder s = new StringBuilder();
                if (uClear.isSelected())
                    s.append("Clear; ");
                if (uCloud.isSelected())
                    s.append("Cloudy; ");
                if (uBlood.isSelected())
                    s.append("Blood-tinged; ");
                if (uGlucoseNeg.isSelected())
                    s.append("Glucose:Negative; ");
                if (uGlucosePos.isSelected())
                    s.append("Glucose:Positive; ");
                if (uProteinNeg.isSelected())
                    s.append("Protein:Negative; ");
                if (uProteinPos.isSelected())
                    s.append("Protein:Positive; ");
                if (uWBCHigh.isSelected())
                    s.append("WBC:High; ");
                urineSummaryStr = s.toString().trim();
            }
            String bloodSummaryStr = bloodNotes.getText().trim();
            if (bloodSummaryStr.isEmpty()) {
                StringBuilder s = new StringBuilder();
                if (bWBC.isSelected())
                    s.append("WBC Abnormal; ");
                if (bHGB.isSelected())
                    s.append("HGB Abnormal; ");
                if (bPLT.isSelected())
                    s.append("PLT Abnormal; ");
                if (bCRP.isSelected())
                    s.append("CRP/ESR Elevated; ");
                if (bCulture.isSelected())
                    s.append("Blood Culture Positive; ");
                bloodSummaryStr = s.toString().trim();
            }

            // collect other attachments into a list
            java.util.List<String> otherAttachList = new java.util.ArrayList<>();
            for (int ai = 0; ai < attachModel.size(); ai++)
                otherAttachList.add(attachModel.get(ai));
            List<String> out = PatientService.addClinicalInfo(id, height.getText(), weight.getText(), bp.getText(),
                    note.getText(), hpms.auth.AuthService.current == null ? "" : hpms.auth.AuthService.current.username,
                    xrayPath.getText().trim().isEmpty() ? null : xrayPath.getText().trim(),
                    String.valueOf(xrayStatus.getSelectedItem()), xSummary.isEmpty() ? null : xSummary,
                    stoolPath.getText().trim().isEmpty() ? null : stoolPath.getText().trim(),
                    String.valueOf(stoolStatus.getSelectedItem()), stoolSummaryStr.isEmpty() ? null : stoolSummaryStr,
                    urinePath.getText().trim().isEmpty() ? null : urinePath.getText().trim(),
                    String.valueOf(urineStatus.getSelectedItem()), urineSummaryStr.isEmpty() ? null : urineSummaryStr,
                    bloodPath.getText().trim().isEmpty() ? null : bloodPath.getText().trim(),
                    String.valueOf(bloodStatus.getSelectedItem()), bloodSummaryStr.isEmpty() ? null : bloodSummaryStr,
                    otherAttachList.isEmpty() ? null : otherAttachList);
            showOut(out);
            dlg.dispose();
        });

        cancelBtn.addActionListener(ae -> dlg.dispose());
        dlg.pack();
        // enforce a larger fallback size so on most screens the dialog shows everything
        dlg.setSize(Math.max(dlg.getWidth(), 980), Math.max(dlg.getHeight(), 820));
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    // Immunization UI removed â€” immunization storage removed from Patient model
    // the ad-hoc dialog and toolbar/menu entry were removed per UX request.

    // Open a modal detailed patient view
    private void showPatientDetailsDialog(String id) {
        if (id == null)
            return;
        Patient p = DataStore.patients.get(id);
        if (p == null)
            return;
        java.awt.Window owner = javax.swing.SwingUtilities.getWindowAncestor(this);
        new PatientDetailsDialogNew(owner, p);
    }

    // Find patients that are likely duplicates based on name or contact
    private java.util.List<Patient> findSimilarPatients(String name, String contact) {
        java.util.List<Patient> matches = new java.util.ArrayList<>();
        if ((name == null || name.trim().isEmpty()) && (contact == null || contact.trim().isEmpty()))
            return matches;
        String n = name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
        String c = contact == null ? "" : contact.trim();
        for (Patient p : DataStore.patients.values()) {
            boolean nameMatch = !n.isEmpty() && p.name != null && p.name.trim().toLowerCase(Locale.ROOT).equals(n);
            boolean contactMatch = !c.isEmpty() && p.contact != null && p.contact.trim().equals(c);
            // prefer exact matches first (both name+contact)
            if (nameMatch || contactMatch)
                matches.add(p);
        }
        return matches;
    }

    private Room findRoomForPatient(String pid) {
        for (Room r : DataStore.rooms.values())
            if (pid.equals(r.occupantPatientId))
                return r;
        return null;
    }

    private Appointment latestAppointmentForPatient(String pid) {
        return DataStore.appointments.values().stream().filter(a -> a.patientId.equals(pid))
                .max(Comparator.comparing(a -> a.dateTime)).orElse(null);
    }

    private void applyPatientFilter() {
        String q = searchField == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        String gSel = genderFilter == null ? "All" : String.valueOf(genderFilter.getSelectedItem());
        Integer minAge = parseIntSafe(minAgeField == null ? null : minAgeField.getText());
        Integer maxAge = parseIntSafe(maxAgeField == null ? null : maxAgeField.getText());
        boolean needAssigned = assignedCheck != null && assignedCheck.isSelected();
        boolean needNotAssigned = notAssignedCheck != null && notAssignedCheck.isSelected();
        boolean recentOnly = recentCheck != null && recentCheck.isSelected();
        boolean showInactive = showInactiveCheck != null && showInactiveCheck.isSelected();

        // Date range filter
        String dateRangeSel = dateRangeFilter == null ? "All Time" : String.valueOf(dateRangeFilter.getSelectedItem());
        LocalDateTime dateStart = null;
        LocalDateTime dateEnd = null;

        if ("Last 3 Weeks".equals(dateRangeSel)) {
            dateStart = LocalDateTime.now().minusWeeks(3);
            dateEnd = LocalDateTime.now();
        } else if ("Last Month".equals(dateRangeSel)) {
            dateStart = LocalDateTime.now().minusMonths(1);
            dateEnd = LocalDateTime.now();
        } else if ("Custom Range".equals(dateRangeSel)) {
            try {
                if (customStartDate != null && !customStartDate.getText().trim().isEmpty()) {
                    dateStart = LocalDate.parse(customStartDate.getText().trim()).atStartOfDay();
                }
                if (customEndDate != null && !customEndDate.getText().trim().isEmpty()) {
                    dateEnd = LocalDate.parse(customEndDate.getText().trim()).atTime(23, 59, 59);
                }
            } catch (DateTimeParseException ex) {
                // Invalid date format - ignore and show all
                dateStart = null;
                dateEnd = null;
            }
        }

        java.util.List<Patient> filtered = new java.util.ArrayList<>();
        for (Patient p : DataStore.patients.values()) {
            // Skip inactive patients unless showInactive is checked
            if (!p.isActive && !showInactive)
                continue;

            // Skip incomplete patients (missing required fields)
            // Required: name, age > 0, birthday, gender, contact, address, patientType
            p.validateCompleteness(); // Update completeness status
            if (!p.isComplete) {
                // Patient has incomplete information - skip from main list
                continue;
            }

            // Date range filter
            if (dateStart != null && p.createdAt != null && p.createdAt.isBefore(dateStart))
                continue;
            if (dateEnd != null && p.createdAt != null && p.createdAt.isAfter(dateEnd))
                continue;

            Room r = findRoomForPatient(p.id);
            hpms.model.PatientStatus st = PatientStatusService.getStatus(p.id);

            // Category filter (NEW) - filter by selected patient category
            if (selectedCategory != null && st != selectedCategory) {
                continue;
            }

            if (!q.isEmpty()) {
                String blob = (p.id + " " + p.name + " " + (r == null ? "" : r.id) + " " + (st == null ? "" : st.name())
                        + " " + (p.registrationType == null ? "" : p.registrationType)).toLowerCase(Locale.ROOT);
                if (!blob.contains(q))
                    continue;
            }
            if (!"All".equalsIgnoreCase(gSel)) {
                String sel = gSel.trim().toLowerCase(Locale.ROOT);
                String gname = p.gender == null ? "" : p.gender.name();
                if ("other".equalsIgnoreCase(sel)) {
                    // treat anything that's not Male or Female as Other (includes LGBTQ_PLUS)
                    if ("Male".equalsIgnoreCase(gname) || "Female".equalsIgnoreCase(gname))
                        continue;
                } else if (!gname.equalsIgnoreCase(sel)) {
                    continue;
                }
            }
            if (minAge != null && p.age < minAge)
                continue;
            if (maxAge != null && p.age > maxAge)
                continue;

            if (needAssigned && r == null)
                continue;
            if (needNotAssigned && r != null)
                continue;
            if (recentOnly) {
                Appointment latest = latestAppointmentForPatient(p.id);
                if (latest == null)
                    continue;
                if (latest.dateTime.isBefore(LocalDateTime.now().minusDays(30)))
                    continue;
            }
            filtered.add(p);
        }
        // pagination
        int total = filtered.size();
        int totalPages = Math.max(1, (int) Math.ceil(total / (double) pageSize));
        if (currentPage > totalPages)
            currentPage = totalPages;
        if (currentPage < 1)
            currentPage = 1;
        int start = (currentPage - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        patientsModel.setRowCount(0);
        for (int i = start; i < end; i++) {
            Patient p = filtered.get(i);
            Room r = findRoomForPatient(p.id);
            hpms.model.PatientStatus st = PatientStatusService.getStatus(p.id);
            String reg = (p.registrationType == null || p.registrationType.trim().isEmpty()) ? "Walk-in Patient"
                    : p.registrationType;
            patientsModel.addRow(new Object[] { p.id, p.name, p.age, p.gender, (r == null ? "Not Assigned" : r.id),
                    (st == null ? "" : st.name()), reg });
        }
        if (pageInfo != null)
            pageInfo.setText("Page " + currentPage + " of " + totalPages + " (" + total + " items)");
        if (prevPage != null)
            prevPage.setEnabled(currentPage > 1);
        if (nextPage != null)
            nextPage.setEnabled(currentPage < totalPages);
    }

    private Integer parseIntSafe(String s) {
        try {
            if (s == null || s.trim().isEmpty())
                return null;
            return Integer.parseInt(s.trim());
        } catch (Exception ex) {
            return null;
        }
    }

    private String generateDefaultPassword(String patientId) {
        // Generate simple, memorable password: PatientID + random 3-digit number
        java.util.Random rand = new java.util.Random();
        int randomNum = 100 + rand.nextInt(900); // 100-999
        return patientId + randomNum;
    }

    private void showPatientCredentialsDialog(String patientId, String defaultPassword) {
        // Create a custom dialog to display patient credentials
        JDialog dialog = new JDialog((java.awt.Frame) javax.swing.SwingUtilities.getWindowAncestor(this),
                "Patient Account Created", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(450, 250);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new java.awt.GridBagLayout());
        mainPanel.setBackground(new Color(240, 248, 255)); // Light blue background
        mainPanel.setBorder(new javax.swing.border.EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Header
        JLabel headerLbl = new JLabel("âœ“ Patient Account Successfully Created!");
        headerLbl.setFont(Theme.APP_FONT.deriveFont(Font.BOLD, 14f));
        headerLbl.setForeground(new Color(0, 102, 102));
        gbc.gridy = 0;
        gbc.gridx = 0;
        mainPanel.add(headerLbl, gbc);

        // Separator
        gbc.gridy = 1;
        gbc.gridx = 0;
        mainPanel.add(new JSeparator(), gbc);

        // Patient ID Section
        JLabel idLabelHeader = new JLabel("Patient ID:");
        idLabelHeader.setFont(Theme.APP_FONT.deriveFont(Font.BOLD, 11f));
        gbc.gridy = 2;
        gbc.gridx = 0;
        mainPanel.add(idLabelHeader, gbc);

        JTextArea idDisplay = new JTextArea(patientId);
        idDisplay.setFont(Theme.APP_FONT.deriveFont(Font.BOLD, 16f));
        idDisplay.setForeground(new Color(0, 102, 102));
        idDisplay.setEditable(false);
        idDisplay.setLineWrap(false);
        idDisplay.setBackground(new Color(255, 255, 255));
        idDisplay.setBorder(new javax.swing.border.LineBorder(new Color(0, 102, 102), 2));
        idDisplay.setCaretPosition(0);
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.ipady = 15;
        mainPanel.add(idDisplay, gbc);
        gbc.ipady = 0;

        // Default Password Section
        JLabel pwdLabelHeader = new JLabel("Default Password:");
        pwdLabelHeader.setFont(Theme.APP_FONT.deriveFont(Font.BOLD, 11f));
        gbc.gridy = 4;
        gbc.gridx = 0;
        mainPanel.add(pwdLabelHeader, gbc);

        JTextArea pwdDisplay = new JTextArea(defaultPassword);
        pwdDisplay.setFont(Theme.APP_FONT.deriveFont(Font.BOLD, 16f));
        pwdDisplay.setForeground(new Color(220, 20, 60));
        pwdDisplay.setEditable(false);
        pwdDisplay.setLineWrap(false);
        pwdDisplay.setBackground(new Color(255, 255, 255));
        pwdDisplay.setBorder(new javax.swing.border.LineBorder(new Color(220, 20, 60), 2));
        pwdDisplay.setCaretPosition(0);
        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.ipady = 15;
        mainPanel.add(pwdDisplay, gbc);
        gbc.ipady = 0;

        // Note
        JLabel noteLabel = new JLabel(
                "âš  Please save these credentials. The patient can use them to access their account.");
        noteLabel.setFont(Theme.APP_FONT.deriveFont(Font.ITALIC, 9f));
        noteLabel.setForeground(new Color(100, 100, 100));
        gbc.gridy = 6;
        gbc.gridx = 0;
        mainPanel.add(noteLabel, gbc);

        // Button Panel
        JPanel buttonPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);

        JButton copyBtn = new JButton("Copy Credentials");
        copyBtn.setFont(Theme.APP_FONT);
        copyBtn.setBackground(new Color(0, 102, 102));
        copyBtn.setForeground(Color.WHITE);
        copyBtn.addActionListener(e -> {
            String credentials = "Patient ID: " + patientId + "\nPassword: " + defaultPassword;
            java.awt.datatransfer.StringSelection selection = new java.awt.datatransfer.StringSelection(credentials);
            java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
            JOptionPane.showMessageDialog(dialog, "Credentials copied to clipboard!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        });
        buttonPanel.add(copyBtn);

        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(Theme.APP_FONT);
        closeBtn.setBackground(new Color(200, 200, 200));
        closeBtn.addActionListener(e -> dialog.dispose());
        buttonPanel.add(closeBtn);

        gbc.gridy = 7;
        gbc.gridx = 0;
        gbc.insets = new Insets(20, 10, 10, 10);
        mainPanel.add(buttonPanel, gbc);

        dialog.add(mainPanel);
        // Persist patient portal credential in secure hashed storage (AuthService)
        java.util.List<String> created = hpms.auth.AuthService.createPatientAccount(patientId, defaultPassword);
        if (!created.isEmpty() && created.get(0).startsWith("Patient account created")) {
            LogManager.log("patient_portal_created " + patientId);
        } else {
            // Could not create user (username exists) â€” warn admin
            JOptionPane.showMessageDialog(this,
                    "Warning: patient portal account not created: " + String.join(";", created), "Portal account",
                    JOptionPane.WARNING_MESSAGE);
        }

        dialog.setVisible(true);
    }
}
