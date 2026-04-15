package gui;

import courses.*;
import exams.Result;
import services.*;
import users.Student;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;
import java.util.List;

import static gui.LoginPanel.*;

/**
 * StudentDashboard — full Swing dashboard for the Student role.
 *
 * GUI ↔ Logic connection:
 *   Every button action directly calls the appropriate Service method
 *   (courseService, assignmentService, examService, quizService, userService).
 *   No Scanner or System.out.println used here — results are rendered in
 *   JTable / JOptionPane / status labels.
 */
public class StudentDashboard extends JPanel {

    // ── Services (Model) ─────────────────────────────────────────────────────
    private final MainFrame         frame;
    private final Student           student;
    private final CourseService     courseService;
    private final AssignmentService assignmentService;
    private final ExamService       examService;
    private final NotificationService notificationService;
    private final QuizService       quizService;
    private final UserService       userService;

    // Content cards
    private JPanel     contentPanel;
    private CardLayout contentLayout;

    // Sidebar buttons for visual state tracking
    private JButton activeBtn = null;

    // ─────────────────────────────────────────────────────────────────────────

    public StudentDashboard(MainFrame frame, Student student,
                            CourseService courseService,
                            AssignmentService assignmentService,
                            ExamService examService,
                            NotificationService notificationService,
                            QuizService quizService,
                            UserService userService) {
        this.frame               = frame;
        this.student             = student;
        this.courseService       = courseService;
        this.assignmentService   = assignmentService;
        this.examService         = examService;
        this.notificationService = notificationService;
        this.quizService         = quizService;
        this.userService         = userService;

        setLayout(new BorderLayout());
        buildUI();
    }

    // ── UI Construction ──────────────────────────────────────────────────────

    private void buildUI() {
        add(buildSidebar(), BorderLayout.WEST);
        add(buildHeader(),  BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
    }

    // ── Header bar ───────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, LIGHT_GRAY),
                new EmptyBorder(10, 20, 10, 20)));

        JLabel title = makeLabel("Student Dashboard",
                new Font("Segoe UI", Font.BOLD, 20), NAVY);
        JLabel info  = makeLabel("ID: " + student.getId() + "  |  Dept: " + student.getDepartment(),
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
                    "Logged out successfully.",
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

        // User greeting
        JLabel greet = makeLabel("  👤 " + student.getName(),
                new Font("Segoe UI", Font.BOLD, 13), GOLD);
        greet.setAlignmentX(Component.LEFT_ALIGNMENT);
        greet.setBorder(new EmptyBorder(8, 12, 16, 12));
        sidebar.add(greet);

        // Nav items: label → card name
        String[][] items = {
            {"📚  Enroll in Course",    "ENROLL"},
            {"📝  Submit Assignment",   "SUBMIT"},
            {"📋  Fill Exam Form",      "EXAM_FORM"},
            {"📊  My Results",          "RESULTS"},
            {"🎓  My Enrollments",      "ENROLLMENTS"},
            {"📁  Submission History",  "HISTORY"},
            {"🧠  Quiz Center",         "QUIZ"},
            {"🔑  Change Password",     "CHANGE_PASS"},
        };

        for (String[] item : items) {
            JButton btn = makeSidebarButton(item[0]);
            btn.addActionListener(e -> {
                setActiveButton(btn);
                contentLayout.show(contentPanel, item[1]);
            });
            sidebar.add(btn);
            if (item[1].equals("ENROLL")) {
                activeBtn = btn;
                btn.setBackground(new Color(0x003F7D));
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
        if (activeBtn != null) { activeBtn.setBackground(NAVY); }
        activeBtn = btn;
        btn.setBackground(COEP_BLUE);
    }

    // ── Content area ─────────────────────────────────────────────────────────

    private JPanel buildContent() {
        contentLayout = new CardLayout();
        contentPanel  = new JPanel(contentLayout);
        contentPanel.setBackground(OFF_WHITE);

        contentPanel.add(buildEnrollPanel(),    "ENROLL");
        contentPanel.add(buildSubmitPanel(),    "SUBMIT");
        contentPanel.add(buildExamFormPanel(),  "EXAM_FORM");
        contentPanel.add(buildResultsPanel(),   "RESULTS");
        contentPanel.add(buildEnrollmentsPanel(),"ENROLLMENTS");
        contentPanel.add(buildHistoryPanel(),   "HISTORY");
        contentPanel.add(buildQuizPanel(),      "QUIZ");
        contentPanel.add(buildChangePassPanel(),"CHANGE_PASS");

        contentLayout.show(contentPanel, "ENROLL");
        return contentPanel;
    }

    // ── ENROLL IN COURSE ─────────────────────────────────────────────────────

    private JPanel buildEnrollPanel() {
        JPanel p = sectionPanel("📚  Enroll in Course");

        JLabel info = makeLabel("Showing courses for your department: " + student.getDepartment(),
                new Font("Segoe UI", Font.ITALIC, 12), new Color(0x556677));
        p.add(info);
        p.add(Box.createVerticalStrut(10));

        // Table of dept courses
        String[] cols = {"Course ID", "Course Name", "Credits"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = styledTable(model);
        JScrollPane sp = new JScrollPane(table);
        sp.setPreferredSize(new Dimension(600, 220));
        sp.setBorder(BorderFactory.createLineBorder(LIGHT_GRAY));

        // Populate table
        List<Course> deptCourses = courseService.getCoursesByDepartment(student.getDepartment());
        for (Course c : deptCourses) {
            model.addRow(new Object[]{c.getCourseId(), c.getCourseName(), c.getCredits()});
        }

        JButton refreshBtn = makeButton("Refresh", COEP_BLUE, Color.WHITE);
        refreshBtn.setPreferredSize(new Dimension(100, 32));
        refreshBtn.addActionListener(e -> {
            model.setRowCount(0);
            List<Course> refreshed = courseService.getCoursesByDepartment(student.getDepartment());
            for (Course c : refreshed) {
                model.addRow(new Object[]{c.getCourseId(), c.getCourseName(), c.getCredits()});
            }
            if (refreshed.isEmpty()) {
                JOptionPane.showMessageDialog(frame,
                        "No courses available for your department.",
                        "No Courses", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JPanel topRow = rowPanel();
        topRow.add(refreshBtn);
        p.add(topRow);
        p.add(Box.createVerticalStrut(8));

        if (deptCourses.isEmpty()) {
            JLabel empty = makeLabel("No courses available for your department.",
                    new Font("Segoe UI", Font.ITALIC, 13), new Color(0x778899));
            p.add(empty);
        }
        p.add(sp);
        p.add(Box.createVerticalStrut(12));

        // Row + input
        JPanel row = rowPanel();
        JTextField cidField = new JTextField(10);
        styleTextField(cidField, "Course ID");
        JButton enrollBtn = makeButton("Enroll", COEP_BLUE, Color.WHITE);
        enrollBtn.setPreferredSize(new Dimension(100, 34));

        // Click on table row → fills field
        table.getSelectionModel().addListSelectionListener(e -> {
            int sel = table.getSelectedRow();
            if (sel >= 0) cidField.setText((String) model.getValueAt(sel, 0));
        });

        enrollBtn.addActionListener(e -> {
            String cid = cidField.getText().trim().toUpperCase();
            if (cid.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please enter or select a Course ID.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // Calls existing CourseService.enrollStudent with dept validation
            boolean ok = courseService.enrollStudent(student.getId(), cid, student.getDepartment());
            if (ok) {
                notificationService.notifyEnrollment(student.getName(), cid);
                JOptionPane.showMessageDialog(frame,
                        "Successfully enrolled in course: " + cid,
                        "Enrolled ✓", JOptionPane.INFORMATION_MESSAGE);
                cidField.setText("");
            } else {
                JOptionPane.showMessageDialog(frame,
                        "Enrollment failed. You may already be enrolled, or this course does not belong to your department.",
                        "Enrollment Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        row.add(makeLabel("Course ID:", new Font("Segoe UI", Font.BOLD, 13), NAVY));
        row.add(Box.createHorizontalStrut(8));
        row.add(cidField);
        row.add(Box.createHorizontalStrut(10));
        row.add(enrollBtn);
        p.add(row);
        return p;
    }

    // ── SUBMIT ASSIGNMENT ─────────────────────────────────────────────────────

    private JPanel buildSubmitPanel() {
        JPanel p = sectionPanel("📝  Submit Assignment");

        JLabel info = makeLabel("Select your course, then choose an assignment to submit.",
                new Font("Segoe UI", Font.ITALIC, 12), new Color(0x556677));
        p.add(info);
        p.add(Box.createVerticalStrut(10));

        // Course combo (enrolled courses)
        JPanel row1 = rowPanel();

        // Assignment table
        String[] cols = {"Assignment ID", "Title", "Due Date", "Max Marks"};
        DefaultTableModel assignModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable assignTable = styledTable(assignModel);

        List<String[]> enrollments = courseService.getEnrollmentsByStudent(student.getId());
        String[] courseIds = enrollments.stream().map(e -> e[2]).toArray(String[]::new);
        JComboBox<String> courseCombo = new JComboBox<>(courseIds);
        styleCombo(courseCombo);
        courseCombo.setPreferredSize(new Dimension(160, 34));

        JButton refreshBtn = makeButton("Refresh", COEP_BLUE, Color.WHITE);
        refreshBtn.setPreferredSize(new Dimension(100, 32));
        refreshBtn.addActionListener(e -> {
            refreshEnrolledCourseCombo(courseCombo);
            if (courseCombo.getItemCount() == 0) {
                JOptionPane.showMessageDialog(frame,
                        "You are not enrolled in any courses yet.",
                        "No Enrollments", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JButton loadBtn = makeButton("Load Assignments", COEP_BLUE, Color.WHITE);
        loadBtn.setPreferredSize(new Dimension(160, 34));

        // Load assignments for selected course
        loadBtn.addActionListener(e -> {
            String sel = (String) courseCombo.getSelectedItem();
            if (sel == null) return;
            assignModel.setRowCount(0);
            for (Assignment a : assignmentService.getAssignmentsByCourse(sel)) {
                assignModel.addRow(new Object[]{
                        a.getAssignmentId(), a.getTitle(), a.getDueDate(), a.getMaxMarks()});
            }
            if (assignModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(frame,
                        "No assignments for course " + sel + ".",
                        "No Assignments", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        row1.add(makeLabel("Course:", new Font("Segoe UI", Font.BOLD, 13), NAVY));
        row1.add(Box.createHorizontalStrut(8));
        row1.add(courseCombo);
        row1.add(Box.createHorizontalStrut(10));
        row1.add(loadBtn);
        row1.add(Box.createHorizontalStrut(10));
        row1.add(refreshBtn);
        p.add(row1);
        p.add(Box.createVerticalStrut(10));

        JScrollPane sp = new JScrollPane(assignTable);
        sp.setPreferredSize(new Dimension(620, 200));
        sp.setBorder(BorderFactory.createLineBorder(LIGHT_GRAY));
        p.add(sp);
        p.add(Box.createVerticalStrut(10));

        JPanel row2 = rowPanel();
        JTextField aidField = new JTextField(10);
        styleTextField(aidField, "Assignment ID");
        assignTable.getSelectionModel().addListSelectionListener(e -> {
            int sel = assignTable.getSelectedRow();
            if (sel >= 0) aidField.setText((String) assignModel.getValueAt(sel, 0));
        });
        JButton submitBtn = makeButton("Submit", new Color(0x1A7A3F), Color.WHITE);
        submitBtn.setPreferredSize(new Dimension(100, 34));
        submitBtn.addActionListener(e -> {
            String aid = aidField.getText().trim().toUpperCase();
            if (aid.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please select or enter an Assignment ID.",
                        "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // Calls existing AssignmentService.submitAssignment
            boolean ok = assignmentService.submitAssignment(aid, student.getId());
            if (ok) {
                JOptionPane.showMessageDialog(frame,
                        "Assignment submitted successfully!",
                        "Submitted ✓", JOptionPane.INFORMATION_MESSAGE);
                aidField.setText("");
            } else {
                JOptionPane.showMessageDialog(frame,
                        "Submission failed. You may have already submitted, or the assignment ID is invalid.",
                        "Submission Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        row2.add(makeLabel("Assignment ID:", new Font("Segoe UI", Font.BOLD, 13), NAVY));
        row2.add(Box.createHorizontalStrut(8));
        row2.add(aidField);
        row2.add(Box.createHorizontalStrut(10));
        row2.add(submitBtn);
        p.add(row2);
        return p;
    }

    // ── FILL EXAM FORM ────────────────────────────────────────────────────────

    private JPanel buildExamFormPanel() {
        JPanel p = sectionPanel("📋  Fill Exam Form");

        List<String[]> enrollments = courseService.getEnrollmentsByStudent(student.getId());
        String[] courseIds = enrollments.stream().map(e -> e[2]).toArray(String[]::new);
        JComboBox<String> courseCombo = new JComboBox<>(courseIds);
        styleCombo(courseCombo);
        courseCombo.setPreferredSize(new Dimension(200, 34));

        JButton refreshBtn = makeButton("Refresh", COEP_BLUE, Color.WHITE);
        refreshBtn.setPreferredSize(new Dimension(100, 32));
        refreshBtn.addActionListener(e -> {
            refreshEnrolledCourseCombo(courseCombo);
            if (courseCombo.getItemCount() == 0) {
                JOptionPane.showMessageDialog(frame,
                        "You are not enrolled in any courses yet.",
                        "No Enrollments", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JPanel topRow = rowPanel();
        topRow.add(refreshBtn);
        p.add(topRow);
        p.add(Box.createVerticalStrut(8));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LIGHT_GRAY, 1, true),
                new EmptyBorder(20, 30, 20, 30)));
        form.setMaximumSize(new Dimension(500, 200));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 8, 8, 8);
        gc.anchor = GridBagConstraints.WEST;

        gc.gridx = 0; gc.gridy = 0;
        form.add(makeLabel("Course:", new Font("Segoe UI", Font.BOLD, 13), NAVY), gc);
        gc.gridx = 1;
        form.add(courseCombo, gc);

        gc.gridx = 0; gc.gridy = 1;
        form.add(makeLabel("Exam Type:", new Font("Segoe UI", Font.BOLD, 13), NAVY), gc);
        gc.gridx = 1;
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"SEMESTER", "BACKLOG"});
        styleCombo(typeCombo);
        typeCombo.setPreferredSize(new Dimension(200, 34));
        form.add(typeCombo, gc);

        gc.gridx = 1; gc.gridy = 2;
        JButton submitBtn = makeButton("Submit Form", COEP_BLUE, Color.WHITE);
        submitBtn.setPreferredSize(new Dimension(140, 36));
        submitBtn.addActionListener(e -> {
            String cid = (String) courseCombo.getSelectedItem();
            String examType = (String) typeCombo.getSelectedItem();
            if (cid == null || cid.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "No enrolled courses found.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Calls existing ExamService.fillExamForm
            boolean ok = examService.fillExamForm(student.getId(), cid, examType);
            if (ok) {
                JOptionPane.showMessageDialog(frame,
                        "Exam form submitted successfully for " + cid + " (" + examType + ").",
                        "Form Submitted ✓", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame,
                        "Form already submitted for this course and exam type.",
                        "Already Submitted", JOptionPane.WARNING_MESSAGE);
            }
        });
        form.add(submitBtn, gc);

        p.add(form);
        return p;
    }

    // ── VIEW RESULTS ─────────────────────────────────────────────────────────

    private JPanel buildResultsPanel() {
        JPanel p = sectionPanel("📊  My Results");

        String[] cols = {"Result ID", "Course", "Exam Type", "Marks", "Max Marks", "Grade"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = styledTable(model);

        // Load button to refresh
        JButton refreshBtn = makeButton("Refresh", COEP_BLUE, Color.WHITE);
        refreshBtn.setPreferredSize(new Dimension(100, 32));
        refreshBtn.addActionListener(e -> loadResults(model));

        JPanel topRow = rowPanel();
        topRow.add(refreshBtn);
        p.add(topRow);
        p.add(Box.createVerticalStrut(8));

        JScrollPane sp = new JScrollPane(table);
        sp.setPreferredSize(new Dimension(680, 300));
        sp.setBorder(BorderFactory.createLineBorder(LIGHT_GRAY));
        p.add(sp);

        // Initial load
        loadResults(model);
        return p;
    }

    private void loadResults(DefaultTableModel model) {
        model.setRowCount(0);
        // Calls existing ExamService.getResultsByStudent
        List<Result> results = examService.getResultsByStudent(student.getId());
        for (Result r : results) {
            model.addRow(new Object[]{
                    r.getResultId(), r.getCourseId(), r.getExamType(),
                    r.getMarksObtained(), r.getMaxMarks(), r.getGrade()});
        }
        if (results.isEmpty()) {
            // empty state handled by empty table row count
        }
    }

    // ── MY ENROLLMENTS ────────────────────────────────────────────────────────

    private JPanel buildEnrollmentsPanel() {
        JPanel p = sectionPanel("🎓  My Enrollments");

        String[] cols = {"Enrollment ID", "Course ID", "Enrollment Date"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = styledTable(model);

        // Load button
        JButton refreshBtn = makeButton("Refresh", COEP_BLUE, Color.WHITE);
        refreshBtn.setPreferredSize(new Dimension(100, 32));
        refreshBtn.addActionListener(e -> loadEnrollments(model));

        JPanel topRow = rowPanel();
        topRow.add(refreshBtn);
        p.add(topRow);
        p.add(Box.createVerticalStrut(8));

        JScrollPane sp = new JScrollPane(table);
        sp.setPreferredSize(new Dimension(560, 300));
        sp.setBorder(BorderFactory.createLineBorder(LIGHT_GRAY));
        p.add(sp);

        loadEnrollments(model);
        return p;
    }

    private void loadEnrollments(DefaultTableModel model) {
        model.setRowCount(0);
        List<String[]> enr = courseService.getEnrollmentsByStudent(student.getId());
        for (String[] e : enr) {
            model.addRow(new Object[]{e[0], e[2], e[3]});
        }
    }

    private void refreshEnrolledCourseCombo(JComboBox<String> combo) {
        combo.removeAllItems();
        List<String[]> enrollments = courseService.getEnrollmentsByStudent(student.getId());
        for (String[] e : enrollments) {
            combo.addItem(e[2]);
        }
    }

    // ── SUBMISSION HISTORY ────────────────────────────────────────────────────

    private JPanel buildHistoryPanel() {
        JPanel p = sectionPanel("📁  Submission History");

        String[] cols = {"Sub ID", "Assignment ID", "Title", "Course", "Due Date", "Marks", "Graded"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = styledTable(model);

        JButton refreshBtn = makeButton("Refresh", COEP_BLUE, Color.WHITE);
        refreshBtn.setPreferredSize(new Dimension(100, 32));
        refreshBtn.addActionListener(e -> loadHistory(model));

        JPanel topRow = rowPanel();
        topRow.add(refreshBtn);
        p.add(topRow);
        p.add(Box.createVerticalStrut(8));

        JScrollPane sp = new JScrollPane(table);
        sp.setPreferredSize(new Dimension(740, 300));
        sp.setBorder(BorderFactory.createLineBorder(LIGHT_GRAY));
        p.add(sp);

        loadHistory(model);
        return p;
    }

    private void loadHistory(DefaultTableModel model) {
        model.setRowCount(0);
        List<Submission> subs = assignmentService.getSubmissionsByStudent(student.getId());
        for (Submission s : subs) {
            Optional<Assignment> aOpt = assignmentService.findAssignmentById(s.getAssignmentId());
            String title   = aOpt.map(Assignment::getTitle).orElse("Unknown");
            String cid     = aOpt.map(Assignment::getCourseId).orElse("?");
            String due     = aOpt.map(Assignment::getDueDate).orElse("?");
            String marks   = s.isGraded()
                    ? s.getMarksObtained() + "/" + aOpt.map(Assignment::getMaxMarks).orElse(0)
                    : "Pending";
            model.addRow(new Object[]{
                    s.getSubmissionId(), s.getAssignmentId(), title, cid, due, marks,
                    s.isGraded() ? "✓" : "✗"});
        }
    }

    // ── QUIZ CENTER ───────────────────────────────────────────────────────────

    private JPanel buildQuizPanel() {
        JPanel p = sectionPanel("🧠  Quiz Center");

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        tabs.addTab("Available Quizzes", buildQuizListTab());
        tabs.addTab("Attempt a Quiz",    buildAttemptTab());
        tabs.addTab("My Quiz Results",   buildQuizResultsTab());

        p.add(tabs);
        return p;
    }

    private JPanel buildQuizListTab() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(12, 12, 12, 12));
        p.setBackground(OFF_WHITE);

        String[] cols = {"Quiz ID", "Title", "Course", "Time (min)", "Max Attempts", "Used"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = styledTable(model);
        JButton refreshBtn = makeButton("Refresh", COEP_BLUE, Color.WHITE);
        refreshBtn.setPreferredSize(new Dimension(100, 32));
        refreshBtn.addActionListener(e -> {
            model.setRowCount(0);
            List<String[]> enr = courseService.getEnrollmentsByStudent(student.getId());
            List<String> cids = new ArrayList<>();
            for (String[] en : enr) cids.add(en[2]);
            for (Quiz q : quizService.getQuizzesForStudent(cids)) {
                int used = quizService.getAttemptCount(q.getQuizId(), student.getId());
                model.addRow(new Object[]{
                        q.getQuizId(), q.getTitle(), q.getCourseId(),
                        q.getTimeLimitMinutes(), q.getMaxAttempts(), used});
            }
        });
        JPanel row = rowPanel();
        row.add(refreshBtn);
        p.add(row);
        p.add(Box.createVerticalStrut(8));
        JScrollPane sp = new JScrollPane(table);
        sp.setPreferredSize(new Dimension(600, 260));
        sp.setBorder(BorderFactory.createLineBorder(LIGHT_GRAY));
        p.add(sp);
        return p;
    }

    private JPanel buildAttemptTab() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(16, 20, 16, 20));
        p.setBackground(OFF_WHITE);

        JPanel row = rowPanel();
        JTextField qidField = new JTextField(10);
        styleTextField(qidField, "Quiz ID (e.g. Q001)");
        JButton startBtn = makeButton("Start Quiz", COEP_BLUE, Color.WHITE);
        startBtn.setPreferredSize(new Dimension(110, 34));
        row.add(makeLabel("Quiz ID:", new Font("Segoe UI", Font.BOLD, 13), NAVY));
        row.add(Box.createHorizontalStrut(8));
        row.add(qidField);
        row.add(Box.createHorizontalStrut(10));
        row.add(startBtn);
        p.add(row);
        p.add(Box.createVerticalStrut(16));

        JTextArea results = new JTextArea(8, 50);
        results.setEditable(false);
        results.setFont(new Font("Monospaced", Font.PLAIN, 13));
        results.setBackground(new Color(0xF7F9FC));
        results.setBorder(new EmptyBorder(10, 10, 10, 10));
        JScrollPane sp = new JScrollPane(results);
        sp.setBorder(BorderFactory.createLineBorder(LIGHT_GRAY));
        p.add(sp);

        startBtn.addActionListener(e -> {
            String qid = qidField.getText().trim().toUpperCase();
            if (qid.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Enter a Quiz ID first.",
                        "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Optional<Quiz> qOpt = quizService.findQuizById(qid);
            if (qOpt.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Quiz not found: " + qid,
                        "Not Found", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Quiz quiz = qOpt.get();

            // Enrollment check
            List<String[]> enr = courseService.getEnrollmentsByStudent(student.getId());
            boolean enrolled = enr.stream().anyMatch(en -> en[2].equalsIgnoreCase(quiz.getCourseId()));
            if (!enrolled) {
                JOptionPane.showMessageDialog(frame,
                        "You are not enrolled in the course for this quiz: " + quiz.getCourseId(),
                        "Access Denied", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Max attempts check
            int attempts = quizService.getAttemptCount(qid, student.getId());
            if (attempts >= quiz.getMaxAttempts()) {
                JOptionPane.showMessageDialog(frame,
                        "Maximum attempt limit reached (" + quiz.getMaxAttempts() + ").",
                        "Limit Reached", JOptionPane.WARNING_MESSAGE);
                return;
            }

            List<QuizQuestion> questions = quizService.getQuestionsForQuiz(qid);
            if (questions.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "This quiz has no questions yet.",
                        "Empty Quiz", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Interactive True/False question dialogs
            String[] answers = new String[questions.size()];
            for (int i = 0; i < questions.size(); i++) {
                QuizQuestion qq = questions.get(i);
                int choice = JOptionPane.showOptionDialog(frame,
                        "Q" + (i+1) + ": " + qq.getQuestionText(),
                        "Quiz — Attempt " + (attempts+1) + " | " + quiz.getTitle(),
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        new String[]{"True", "False"},
                        "True");
                if (choice == JOptionPane.CLOSED_OPTION) {
                    JOptionPane.showMessageDialog(frame,
                            "Quiz cancelled. Progress not saved.",
                            "Quiz Cancelled", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                answers[i] = (choice == 0) ? "True" : "False";
            }

            // Record attempt via QuizService
            QuizAttempt attempt = quizService.recordAttempt(qid, student.getId(), answers);
            int pct = attempt.getTotalQuestions() > 0
                    ? (attempt.getScore() * 100 / attempt.getTotalQuestions()) : 0;

            results.setText(
                    "===== QUIZ RESULT =====\n" +
                    "Quiz    : " + quiz.getTitle() + "\n" +
                    "Attempt : " + (attempts+1) + " of " + quiz.getMaxAttempts() + "\n" +
                    "Score   : " + attempt.getScore() + " / " + attempt.getTotalQuestions() + "\n" +
                    "Percent : " + pct + "%\n" +
                    "AttemptID: " + attempt.getAttemptId() + "\n" +
                    "======================\n"
            );
        });
        return p;
    }

    private JPanel buildQuizResultsTab() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(12, 12, 12, 12));
        p.setBackground(OFF_WHITE);

        String[] cols = {"Attempt ID", "Quiz ID", "Title", "Score", "Total", "Date"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = styledTable(model);
        JButton refreshBtn = makeButton("Refresh", COEP_BLUE, Color.WHITE);
        refreshBtn.setPreferredSize(new Dimension(100, 32));
        refreshBtn.addActionListener(e -> {
            model.setRowCount(0);
            for (QuizAttempt a : quizService.getAttemptsByStudent(student.getId())) {
                Optional<Quiz> q = quizService.findQuizById(a.getQuizId());
                String title = q.map(Quiz::getTitle).orElse("Unknown");
                model.addRow(new Object[]{a.getAttemptId(), a.getQuizId(), title,
                        a.getScore(), a.getTotalQuestions(), a.getAttemptDate()});
            }
        });
        JPanel row = rowPanel();
        row.add(refreshBtn);
        p.add(row);
        p.add(Box.createVerticalStrut(8));
        JScrollPane sp = new JScrollPane(table);
        sp.setPreferredSize(new Dimension(600, 260));
        sp.setBorder(BorderFactory.createLineBorder(LIGHT_GRAY));
        p.add(sp);
        return p;
    }

    // ── CHANGE PASSWORD ───────────────────────────────────────────────────────

    private JPanel buildChangePassPanel() {
        JPanel p = sectionPanel("🔑  Change Password");

        // Use a centred wrapper so the form card sits nicely
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        wrapper.setOpaque(false);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LIGHT_GRAY, 1, true),
                new EmptyBorder(28, 36, 28, 36)));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets  = new Insets(10, 10, 10, 10);
        gc.anchor  = GridBagConstraints.WEST;
        gc.fill    = GridBagConstraints.HORIZONTAL; // ← makes fields expand horizontally

        // Explicit sizing + font so Nimbus renders them correctly
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
            // Calls UserService.changePassword (existing logic)
            boolean ok = userService.changePassword(student.getId(), current, newPass);
            if (ok) {
                JOptionPane.showMessageDialog(frame,
                        "Password changed successfully! You will be logged out.",
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

    /** Creates a properly-sized JPasswordField for the Nimbus L&F. */
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

    /** Creates a titled, scrollable content section panel. */
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

    /** Creates a horizontal flow panel aligned left. */
    private JPanel rowPanel() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        return row;
    }

    /** Creates a styled JTable with alternating row colours. */
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

    private void addFormRow(JPanel form, GridBagConstraints gc,
                            int row, String label, JComponent field) {
        gc.gridx = 0; gc.gridy = row;
        form.add(makeLabel(label, new Font("Segoe UI", Font.BOLD, 13), NAVY), gc);
        gc.gridx = 1;
        form.add(field, gc);
    }
}
