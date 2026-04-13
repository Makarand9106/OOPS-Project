package gui;

import exams.ExamForm;
import exams.Result;
import services.*;
import users.ExamCellStaff;
import users.User;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;
import java.util.List;

import static gui.LoginPanel.*;

/**
 * ExamCellDashboard — full Swing dashboard for the Exam Cell Staff role.
 *
 * GUI ↔ Logic connection:
 *   Calls ExamService, UserService, CourseService, NotificationService directly.
 *   Covers: pending forms, approve form, upload marks, publish results,
 *           view all results, change password.
 */
public class ExamCellDashboard extends JPanel {

    private final MainFrame           frame;
    private final ExamCellStaff       staff;
    private final ExamService         examService;
    private final UserService         userService;
    private final CourseService       courseService;
    private final NotificationService notificationService;

    private JPanel     contentPanel;
    private CardLayout contentLayout;
    private JButton    activeBtn = null;

    // ─────────────────────────────────────────────────────────────────────────

    public ExamCellDashboard(MainFrame frame, ExamCellStaff staff,
                             ExamService examService, UserService userService,
                             CourseService courseService,
                             NotificationService notificationService) {
        this.frame               = frame;
        this.staff               = staff;
        this.examService         = examService;
        this.userService         = userService;
        this.courseService       = courseService;
        this.notificationService = notificationService;

        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        add(buildHeader(),  BorderLayout.NORTH);
        add(buildSidebar(), BorderLayout.WEST);
        add(buildContent(), BorderLayout.CENTER);
    }

    // ── Header ───────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, LIGHT_GRAY),
                new EmptyBorder(10, 20, 10, 20)));

        JLabel title = makeLabel("Exam Cell Dashboard",
                new Font("Segoe UI", Font.BOLD, 20), NAVY);
        JLabel info  = makeLabel("ID: " + staff.getId() + "  |  Dept: " + staff.getDepartment(),
                new Font("Segoe UI", Font.PLAIN, 13), new Color(0x778899));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        left.add(title);
        left.add(Box.createHorizontalStrut(16));
        left.add(info);
        header.add(left, BorderLayout.WEST);

        JButton logoutBtn = makeButton("Logout", new Color(0xCC3333), Color.WHITE);
        logoutBtn.setPreferredSize(new Dimension(90, 32));
        logoutBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame,
                    "Logged out successfully. Goodbye, " + staff.getName() + "!",
                    "Logged Out", JOptionPane.INFORMATION_MESSAGE);
            frame.showLogin();
        });
        header.add(logoutBtn, BorderLayout.EAST);
        return header;
    }

    // ── Sidebar ──────────────────────────────────────────────────────────────

    private JScrollPane buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(NAVY);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(210, 0));
        sidebar.setBorder(new EmptyBorder(12, 0, 12, 0));

        JLabel greet = makeLabel("  📋 " + staff.getName(),
                new Font("Segoe UI", Font.BOLD, 13), GOLD);
        greet.setAlignmentX(Component.LEFT_ALIGNMENT);
        greet.setBorder(new EmptyBorder(8, 12, 16, 12));
        sidebar.add(greet);

        String[][] items = {
            {"📩  Pending Exam Forms",  "PENDING"},
            {"✅  Approve Form",        "APPROVE"},
            {"📤  Upload Marks",        "UPLOAD_MARKS"},
            {"🚀  Publish Results",     "PUBLISH"},
            {"📊  All Results",         "ALL_RESULTS"},
            {"🔑  Change Password",     "CHANGE_PASS"},
        };

        for (String[] item : items) {
            JButton btn = makeSidebarButton(item[0]);
            btn.addActionListener(e -> {
                setActiveButton(btn);
                contentLayout.show(contentPanel, item[1]);
            });
            sidebar.add(btn);
            if (item[1].equals("PENDING")) {
                activeBtn = btn;
                btn.setBackground(COEP_BLUE);
            }
        }
        sidebar.add(Box.createVerticalGlue());

        JScrollPane sp = new JScrollPane(sidebar,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setBorder(null);
        return sp;
    }

    private JButton makeSidebarButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(new Color(0xCCDDEE));
        btn.setBackground(NAVY);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setBorder(new EmptyBorder(10, 18, 10, 10));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (btn != activeBtn) btn.setBackground(new Color(0x223350));
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                if (btn != activeBtn) btn.setBackground(NAVY);
            }
        });
        return btn;
    }

    private void setActiveButton(JButton btn) {
        if (activeBtn != null) activeBtn.setBackground(NAVY);
        activeBtn = btn;
        btn.setBackground(COEP_BLUE);
    }

    // ── Content ───────────────────────────────────────────────────────────────

    private JPanel buildContent() {
        contentLayout = new CardLayout();
        contentPanel  = new JPanel(contentLayout);
        contentPanel.setBackground(OFF_WHITE);

        contentPanel.add(buildPendingFormsPanel(),  "PENDING");
        contentPanel.add(buildApprovePanel(),       "APPROVE");
        contentPanel.add(buildUploadMarksPanel(),   "UPLOAD_MARKS");
        contentPanel.add(buildPublishPanel(),       "PUBLISH");
        contentPanel.add(buildAllResultsPanel(),    "ALL_RESULTS");
        contentPanel.add(buildChangePassPanel(),    "CHANGE_PASS");

        contentLayout.show(contentPanel, "PENDING");
        return contentPanel;
    }

    // ── PENDING EXAM FORMS ────────────────────────────────────────────────────

    private JPanel buildPendingFormsPanel() {
        JPanel p = sectionPanel("📩  Pending Exam Forms");

        String[] cols = {"Form ID", "Student ID", "Course ID", "Exam Type", "Applied Date"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = styledTable(model);

        JButton refreshBtn = makeButton("Refresh", COEP_BLUE, Color.WHITE);
        refreshBtn.setPreferredSize(new Dimension(100, 32));
        refreshBtn.addActionListener(e -> loadPendingForms(model));

        JPanel topRow = rowPanel();
        topRow.add(refreshBtn);
        p.add(topRow);
        p.add(Box.createVerticalStrut(8));

        JScrollPane sp = new JScrollPane(table);
        sp.setPreferredSize(new Dimension(680, 320));
        sp.setBorder(BorderFactory.createLineBorder(LIGHT_GRAY));
        p.add(sp);

        loadPendingForms(model);
        return p;
    }

    private void loadPendingForms(DefaultTableModel model) {
        model.setRowCount(0);
        for (ExamForm f : examService.getPendingForms()) {
            model.addRow(new Object[]{
                    f.getFormId(), f.getStudentId(), f.getCourseId(),
                    f.getExamType(), f.getAppliedDate()});
        }
    }

    // ── APPROVE FORM ─────────────────────────────────────────────────────────

    private JPanel buildApprovePanel() {
        JPanel p = sectionPanel("✅  Approve Exam Form");

        // Pending forms table (same as above but with an approve action)
        String[] cols = {"Form ID", "Student ID", "Course ID", "Exam Type", "Applied Date"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = styledTable(model);

        JButton refreshBtn = makeButton("Refresh", COEP_BLUE, Color.WHITE);
        refreshBtn.setPreferredSize(new Dimension(100, 32));
        refreshBtn.addActionListener(e -> loadPendingForms(model));

        JPanel topRow = rowPanel();
        topRow.add(refreshBtn);
        p.add(topRow);
        p.add(Box.createVerticalStrut(8));

        JScrollPane sp = new JScrollPane(table);
        sp.setPreferredSize(new Dimension(680, 220));
        sp.setBorder(BorderFactory.createLineBorder(LIGHT_GRAY));
        p.add(sp);
        p.add(Box.createVerticalStrut(10));

        JPanel approveRow = rowPanel();
        JTextField fidField = new JTextField(10); styleTextField(fidField, "Form ID");
        // Auto-fill from table selection
        table.getSelectionModel().addListSelectionListener(e -> {
            int sel = table.getSelectedRow();
            if (sel >= 0) fidField.setText((String) model.getValueAt(sel, 0));
        });

        JButton approveBtn = makeButton("Approve", new Color(0x1A7A3F), Color.WHITE);
        approveBtn.setPreferredSize(new Dimension(100, 34));
        approveBtn.addActionListener(e -> {
            String fid = fidField.getText().trim().toUpperCase();
            if (fid.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Select or enter a Form ID.",
                        "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // Check form exists in pending list
            List<ExamForm> pending = examService.getPendingForms();
            ExamForm target = null;
            for (ExamForm f : pending) {
                if (f.getFormId().equals(fid)) { target = f; break; }
            }
            if (target == null) {
                JOptionPane.showMessageDialog(frame,
                        "Form " + fid + " not found in pending list.",
                        "Not Found", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Calls ExamService.approveForm (existing logic)
            boolean ok = examService.approveForm(fid);
            if (ok) {
                User student = userService.findById(target.getStudentId());
                if (student != null) {
                    notificationService.notifyExamFormApproved(
                            student.getName(), target.getCourseId());
                }
                JOptionPane.showMessageDialog(frame,
                        "Form " + fid + " approved successfully.",
                        "Approved ✓", JOptionPane.INFORMATION_MESSAGE);
                loadPendingForms(model);
                fidField.setText("");
            } else {
                JOptionPane.showMessageDialog(frame, "Approval failed.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        approveRow.add(makeLabel("Form ID:", new Font("Segoe UI", Font.BOLD, 13), NAVY));
        approveRow.add(Box.createHorizontalStrut(8));
        approveRow.add(fidField);
        approveRow.add(Box.createHorizontalStrut(10));
        approveRow.add(approveBtn);
        p.add(approveRow);

        loadPendingForms(model);
        return p;
    }

    // ── UPLOAD MARKS ─────────────────────────────────────────────────────────

    private JPanel buildUploadMarksPanel() {
        JPanel p = sectionPanel("📤  Upload Exam Marks");

        // Student reference list
        JLabel refLabel = makeLabel("Student Reference (sorted by Branch → ID):",
                new Font("Segoe UI", Font.BOLD, 13), NAVY);
        refLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(refLabel);
        p.add(Box.createVerticalStrut(6));

        String[] refCols = {"ID", "Name", "Branch"};
        DefaultTableModel refModel = new DefaultTableModel(refCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable refTable = styledTable(refModel);

        List<User> students = userService.getUsersByRole("STUDENT");
        students.sort(Comparator.comparing(User::getDepartment).thenComparingInt(User::getId));
        for (User u : students) {
            refModel.addRow(new Object[]{u.getId(), u.getName(), u.getDepartment()});
        }

        JScrollPane refSp = new JScrollPane(refTable);
        refSp.setPreferredSize(new Dimension(500, 140));
        refSp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        refSp.setBorder(BorderFactory.createLineBorder(LIGHT_GRAY));
        refSp.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(refSp);
        p.add(Box.createVerticalStrut(14));

        // Input form
        JPanel form = cardForm();
        GridBagConstraints gc = formGc();

        JTextField sidField   = new JTextField(12); styleTextField(sidField, "Student ID");
        JTextField sidName    = new JTextField(18); sidName.setEditable(false);
        sidName.setBackground(new Color(0xF4F6FB));
        styleTextField(sidName, "Auto-filled after Validate");

        JTextField courseField = new JTextField(12); styleTextField(courseField, "Course ID");
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"SEMESTER", "BACKLOG"});
        styleCombo(typeCombo);
        typeCombo.setPreferredSize(new Dimension(180, 34));
        JSpinner maxMarks = new JSpinner(new SpinnerNumberModel(100, 1, 500, 10));
        JSpinner obtained = new JSpinner(new SpinnerNumberModel(0, 0, 500, 1));

        // Auto-fill student name when pressing Tab or clicking validate
        JButton validateBtn = makeButton("Validate", new Color(0x336699), Color.WHITE);
        validateBtn.setPreferredSize(new Dimension(90, 30));
        validateBtn.addActionListener(e -> {
            String sidText = sidField.getText().trim();
            try {
                int sid = Integer.parseInt(sidText);
                User u = userService.findById(sid);
                if (u == null || !u.getRole().equalsIgnoreCase("STUDENT")) {
                    sidName.setText("Not found");
                } else {
                    sidName.setText(u.getName() + " | " + u.getDepartment());
                }
            } catch (NumberFormatException ex) {
                sidName.setText("Invalid ID");
            }
        });

        // Auto-select from reference table
        refTable.getSelectionModel().addListSelectionListener(e -> {
            int sel = refTable.getSelectedRow();
            if (sel >= 0) {
                sidField.setText(String.valueOf(refModel.getValueAt(sel, 0)));
                sidName.setText(refModel.getValueAt(sel, 1) + " | " + refModel.getValueAt(sel, 2));
            }
        });

        gc.gridx = 0; gc.gridy = 0;
        form.add(makeLabel("Student ID:", new Font("Segoe UI", Font.BOLD, 13), NAVY), gc);
        gc.gridx = 1;
        JPanel sidRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        sidRow.setOpaque(false);
        sidRow.add(sidField); sidRow.add(validateBtn);
        form.add(sidRow, gc);

        gc.gridx = 0; gc.gridy = 1;
        form.add(makeLabel("Student Name:", new Font("Segoe UI", Font.BOLD, 13), NAVY), gc);
        gc.gridx = 1; form.add(sidName, gc);

        addFormRow(form, gc, 2, "Course ID:", courseField);
        addFormRow(form, gc, 3, "Exam Type:", typeCombo);
        addFormRow(form, gc, 4, "Max Marks:", maxMarks);
        addFormRow(form, gc, 5, "Marks Obtained:", obtained);

        gc.gridx = 1; gc.gridy = 6;
        JButton uploadBtn = makeButton("Upload Marks", COEP_BLUE, Color.WHITE);
        uploadBtn.setPreferredSize(new Dimension(140, 36));
        uploadBtn.addActionListener(e -> {
            String sidText = sidField.getText().trim();
            String cid     = courseField.getText().trim().toUpperCase();
            String type    = (String) typeCombo.getSelectedItem();
            int max  = (int) maxMarks.getValue();
            int got  = (int) obtained.getValue();

            if (sidText.isEmpty() || cid.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Student ID and Course ID are required.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int sid;
            try { sid = Integer.parseInt(sidText); }
            catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Student ID must be a number.",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            User student = userService.findById(sid);
            if (student == null || !student.getRole().equalsIgnoreCase("STUDENT")) {
                JOptionPane.showMessageDialog(frame, "Student with ID " + sid + " not found.",
                        "Not Found", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Enrollment check — same as CLI ExamCellController
            List<String[]> enrollments = courseService.getEnrollmentsByStudent(sid);
            boolean enrolled = enrollments.stream().anyMatch(en -> en[2].equalsIgnoreCase(cid));
            if (!enrolled) {
                JOptionPane.showMessageDialog(frame,
                        "REJECTED: Student '" + student.getName() + "' (ID: " + sid + ")\n" +
                        "is NOT enrolled in course " + cid + ".\n" +
                        "Marks upload blocked. Enroll the student first.",
                        "Enrollment Check Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (got < 0 || got > max) {
                JOptionPane.showMessageDialog(frame,
                        "Marks must be between 0 and " + max + ".",
                        "Invalid Marks", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Calls ExamService.uploadResult (existing logic)
            boolean ok = examService.uploadResult(sid, cid, type, got, max);
            if (ok) {
                JOptionPane.showMessageDialog(frame,
                        "Marks uploaded for student " + student.getName() + " in " + cid + ".",
                        "Uploaded ✓", JOptionPane.INFORMATION_MESSAGE);
                sidField.setText(""); sidName.setText(""); courseField.setText("");
            } else {
                JOptionPane.showMessageDialog(frame,
                        "Upload failed. A result may already exist for this student/course/type.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        form.add(uploadBtn, gc);
        p.add(form);
        return p;
    }

    // ── PUBLISH RESULTS ───────────────────────────────────────────────────────

    private JPanel buildPublishPanel() {
        JPanel p = sectionPanel("🚀  Publish Results");

        // All results table
        String[] cols = {"Result ID", "Student", "Course", "Exam Type", "Marks", "Max", "Grade", "Published"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = styledTable(model);
        // Colour-code published vs not
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    Object published = model.getValueAt(row, 7);
                    c.setBackground("Yes".equals(published)
                            ? new Color(0xD4EDDA) : new Color(0xFFF3CD));
                }
                return c;
            }
        });

        JButton refreshBtn = makeButton("Refresh", COEP_BLUE, Color.WHITE);
        refreshBtn.setPreferredSize(new Dimension(100, 32));
        refreshBtn.addActionListener(e -> loadAllResults(model));

        JButton pubAllBtn = makeButton("Publish ALL", new Color(0x1A7A3F), Color.WHITE);
        pubAllBtn.setPreferredSize(new Dimension(130, 32));
        pubAllBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(frame,
                    "Publish ALL unpublished results?\nThis will notify all students.",
                    "Confirm Publish All", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                examService.publishAllResults();
                notificationService.sendInThread("All Students",
                        "Exam results have been published. Please check your portal.");
                JOptionPane.showMessageDialog(frame, "All results published!",
                        "Published ✓", JOptionPane.INFORMATION_MESSAGE);
                loadAllResults(model);
            }
        });

        JPanel topRow = rowPanel();
        topRow.add(refreshBtn);
        topRow.add(Box.createHorizontalStrut(12));
        topRow.add(pubAllBtn);
        p.add(topRow);
        p.add(Box.createVerticalStrut(8));

        JScrollPane sp = new JScrollPane(table);
        sp.setPreferredSize(new Dimension(720, 220));
        sp.setBorder(BorderFactory.createLineBorder(LIGHT_GRAY));
        p.add(sp);
        p.add(Box.createVerticalStrut(10));

        // Publish specific result row
        JPanel pubRow = rowPanel();
        JTextField ridField = new JTextField(10); styleTextField(ridField, "Result ID");
        table.getSelectionModel().addListSelectionListener(e -> {
            int sel = table.getSelectedRow();
            if (sel >= 0) ridField.setText((String) model.getValueAt(sel, 0));
        });

        JButton pubBtn = makeButton("Publish Selected", new Color(0x336699), Color.WHITE);
        pubBtn.setPreferredSize(new Dimension(160, 34));
        pubBtn.addActionListener(e -> {
            String rid = ridField.getText().trim().toUpperCase();
            if (rid.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Select or enter a Result ID.",
                        "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // Calls ExamService.publishResult (existing logic)
            boolean ok = examService.publishResult(rid);
            if (ok) {
                // Notify the student
                List<Result> all = examService.getAllResults();
                for (Result r : all) {
                    if (r.getResultId().equals(rid)) {
                        User student = userService.findById(r.getStudentId());
                        if (student != null) {
                            notificationService.notifyResultPublished(
                                    student.getName(), r.getCourseId(), r.getGrade());
                        }
                        break;
                    }
                }
                JOptionPane.showMessageDialog(frame,
                        "Result " + rid + " published successfully.",
                        "Published ✓", JOptionPane.INFORMATION_MESSAGE);
                loadAllResults(model);
                ridField.setText("");
            } else {
                JOptionPane.showMessageDialog(frame,
                        "Publish failed. Result may already be published or not found.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        pubRow.add(makeLabel("Result ID:", new Font("Segoe UI", Font.BOLD, 13), NAVY));
        pubRow.add(Box.createHorizontalStrut(8));
        pubRow.add(ridField);
        pubRow.add(Box.createHorizontalStrut(10));
        pubRow.add(pubBtn);
        p.add(pubRow);

        loadAllResults(model);
        return p;
    }

    // ── ALL RESULTS ───────────────────────────────────────────────────────────

    private JPanel buildAllResultsPanel() {
        JPanel p = sectionPanel("📊  All Exam Results");

        String[] cols = {"Result ID", "Student ID", "Course", "Exam Type", "Obtained", "Max", "Grade", "Published"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = styledTable(model);

        JButton refreshBtn = makeButton("Refresh", COEP_BLUE, Color.WHITE);
        refreshBtn.setPreferredSize(new Dimension(100, 32));
        refreshBtn.addActionListener(e -> loadAllResults(model));

        JPanel topRow = rowPanel();
        topRow.add(refreshBtn);
        p.add(topRow);
        p.add(Box.createVerticalStrut(8));

        JScrollPane sp = new JScrollPane(table);
        sp.setPreferredSize(new Dimension(720, 340));
        sp.setBorder(BorderFactory.createLineBorder(LIGHT_GRAY));
        p.add(sp);

        loadAllResults(model);
        return p;
    }

    private void loadAllResults(DefaultTableModel model) {
        model.setRowCount(0);
        for (Result r : examService.getAllResults()) {
            model.addRow(new Object[]{
                    r.getResultId(), r.getStudentId(), r.getCourseId(),
                    r.getExamType(), r.getMarksObtained(), r.getMaxMarks(),
                    r.getGrade(), r.isPublished() ? "Yes" : "No"});
        }
    }

    // ── CHANGE PASSWORD ───────────────────────────────────────────────────────

    private JPanel buildChangePassPanel() {
        JPanel p = sectionPanel("🔑  Change Password");

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        wrapper.setOpaque(false);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel form = cardForm();
        GridBagConstraints gc = formGc();
        gc.fill = GridBagConstraints.HORIZONTAL; // key fix: fields render at correct size

        JPasswordField curr = styledPasswordField();
        JPasswordField newP = styledPasswordField();
        JPasswordField conf = styledPasswordField();

        addFormRow(form, gc, 0, "Current Password:", curr);
        addFormRow(form, gc, 1, "New Password:", newP);
        addFormRow(form, gc, 2, "Confirm New Password:", conf);

        gc.gridx = 1; gc.gridy = 3;
        gc.fill  = GridBagConstraints.NONE;
        JButton changeBtn = makeButton("Change Password", COEP_BLUE, Color.WHITE);
        changeBtn.setPreferredSize(new Dimension(180, 38));
        changeBtn.addActionListener(e -> {
            String current = new String(curr.getPassword());
            String newPass = new String(newP.getPassword());
            String confirm = new String(conf.getPassword());
            if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "All fields are required.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!newPass.equals(confirm)) {
                JOptionPane.showMessageDialog(frame, "New passwords do not match.",
                        "Mismatch", JOptionPane.ERROR_MESSAGE);
                return;
            }
            boolean ok = userService.changePassword(staff.getId(), current, newPass);
            if (ok) {
                JOptionPane.showMessageDialog(frame,
                        "Password changed. You will be logged out.",
                        "Success ✓", JOptionPane.INFORMATION_MESSAGE);
                frame.showLogin();
            } else {
                JOptionPane.showMessageDialog(frame,
                        "Password change failed. Check your current password.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        form.add(changeBtn, gc);
        wrapper.add(form);
        p.add(wrapper);
        return p;
    }

    /** Creates a properly-sized JPasswordField for Nimbus L&F. */
    private static JPasswordField styledPasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setPreferredSize(new Dimension(240, 36));
        pf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xCCD3DC), 1, true),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        return pf;
    }

    // ── Utility helpers ───────────────────────────────────────────────────────

    private JPanel sectionPanel(String title) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(OFF_WHITE);
        p.setBorder(new EmptyBorder(20, 24, 20, 24));
        JLabel t = makeLabel(title, new Font("Segoe UI", Font.BOLD, 18), NAVY);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(t);
        p.add(Box.createVerticalStrut(4));
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(LIGHT_GRAY);
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(sep);
        p.add(Box.createVerticalStrut(14));
        return p;
    }

    private JPanel rowPanel() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        return row;
    }

    private JPanel cardForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LIGHT_GRAY, 1, true),
                new EmptyBorder(20, 30, 20, 30)));
        form.setAlignmentX(Component.LEFT_ALIGNMENT);
        return form;
    }

    private GridBagConstraints formGc() {
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets  = new Insets(8, 8, 8, 8);
        gc.anchor  = GridBagConstraints.WEST;
        return gc;
    }

    private void addFormRow(JPanel form, GridBagConstraints gc,
                            int row, String label, JComponent field) {
        gc.gridx = 0; gc.gridy = row;
        form.add(makeLabel(label, new Font("Segoe UI", Font.BOLD, 13), NAVY), gc);
        gc.gridx = 1;
        form.add(field, gc);
    }

    private JTable styledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(26);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(NAVY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(0xD0E4F0));
        table.setGridColor(new Color(0xE8EDF4));
        table.setIntercellSpacing(new Dimension(8, 4));
        return table;
    }
}
