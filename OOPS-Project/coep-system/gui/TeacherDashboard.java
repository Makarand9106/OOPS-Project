package gui;

import courses.*;
import services.*;
import users.Teacher;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;

import static gui.LoginPanel.*;

/**
 * TeacherDashboard — full Swing dashboard for the Teacher role.
 *
 * GUI ↔ Logic connection:
 *   Calls CourseService, AssignmentService, QuizService, NotificationService, UserService
 *   directly on button events. Department is auto-set from teacher profile (same enforcement as CLI).
 */
public class TeacherDashboard extends JPanel {

    private final MainFrame           frame;
    private final Teacher             teacher;
    private final CourseService       courseService;
    private final AssignmentService   assignmentService;
    private final NotificationService notificationService;
    private final QuizService         quizService;
    private final UserService         userService;

    private JPanel     contentPanel;
    private CardLayout contentLayout;
    private JButton    activeBtn = null;

    // ─────────────────────────────────────────────────────────────────────────

    public TeacherDashboard(MainFrame frame, Teacher teacher,
                            CourseService courseService,
                            AssignmentService assignmentService,
                            NotificationService notificationService,
                            QuizService quizService,
                            UserService userService) {
        this.frame               = frame;
        this.teacher             = teacher;
        this.courseService       = courseService;
        this.assignmentService   = assignmentService;
        this.notificationService = notificationService;
        this.quizService         = quizService;
        this.userService         = userService;

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

        JLabel title = makeLabel("Teacher Dashboard",
                new Font("Segoe UI", Font.BOLD, 20), NAVY);
        JLabel info  = makeLabel("ID: " + teacher.getId() + "  |  Dept: " + teacher.getDepartment(),
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
                    "Logged out successfully. Goodbye, " + teacher.getName() + "!",
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

        JLabel greet = makeLabel("  🏫 " + teacher.getName(),
                new Font("Segoe UI", Font.BOLD, 13), GOLD);
        greet.setAlignmentX(Component.LEFT_ALIGNMENT);
        greet.setBorder(new EmptyBorder(8, 12, 16, 12));
        sidebar.add(greet);

        String[][] items = {
            {"📖  Create Course",       "CREATE_COURSE"},
            {"📤  Upload Material",     "UPLOAD"},
            {"📝  Create Assignment",   "CREATE_ASSIGN"},
            {"✅  Grade Submissions",   "GRADE"},
            {"🧠  Create Quiz",         "CREATE_QUIZ"},
            {"📋  My Courses",          "MY_COURSES"},
            {"🔑  Change Password",     "CHANGE_PASS"},
        };

        for (String[] item : items) {
            JButton btn = makeSidebarButton(item[0]);
            btn.addActionListener(e -> {
                setActiveButton(btn);
                contentLayout.show(contentPanel, item[1]);
            });
            sidebar.add(btn);
            if (item[1].equals("CREATE_COURSE")) {
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

        contentPanel.add(buildCreateCoursePanel(), "CREATE_COURSE");
        contentPanel.add(buildUploadPanel(),       "UPLOAD");
        contentPanel.add(buildCreateAssignPanel(), "CREATE_ASSIGN");
        contentPanel.add(buildGradePanel(),        "GRADE");
        contentPanel.add(buildCreateQuizPanel(),   "CREATE_QUIZ");
        contentPanel.add(buildMyCoursesPanel(),    "MY_COURSES");
        contentPanel.add(buildChangePassPanel(),   "CHANGE_PASS");

        contentLayout.show(contentPanel, "CREATE_COURSE");
        return contentPanel;
    }

    // ── CREATE COURSE ─────────────────────────────────────────────────────────

    private JPanel buildCreateCoursePanel() {
        JPanel p = sectionPanel("📖  Create New Course");

        JPanel form = cardForm();

        JTextField cidField  = new JTextField(16); styleTextField(cidField, "e.g. C201");
        JTextField nameField = new JTextField(16); styleTextField(nameField, "Course Name");
        JSpinner   credits   = new JSpinner(new SpinnerNumberModel(3, 1, 6, 1));
        credits.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JLabel deptLabel = makeLabel(teacher.getDepartment(),
                new Font("Segoe UI", Font.BOLD, 13), COEP_BLUE);

        GridBagConstraints gc = formGc();
        addFormRow(form, gc, 0, "Course ID:", cidField);
        addFormRow(form, gc, 1, "Course Name:", nameField);
        addFormRow(form, gc, 2, "Credits (1-6):", credits);
        gc.gridx = 0; gc.gridy = 3;
        form.add(makeLabel("Department (auto):", new Font("Segoe UI", Font.BOLD, 13), NAVY), gc);
        gc.gridx = 1;
        form.add(deptLabel, gc);

        gc.gridx = 1; gc.gridy = 4;
        JButton createBtn = makeButton("Create Course", COEP_BLUE, Color.WHITE);
        createBtn.setPreferredSize(new Dimension(150, 36));
        createBtn.addActionListener(e -> {
            String cid  = cidField.getText().trim().toUpperCase();
            String name = nameField.getText().trim();
            int    cred = (int) credits.getValue();
            if (cid.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Course ID and Name are required.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // Department auto-set from teacher profile — same as CLI enforcement
            String dept = teacher.getDepartment();
            boolean ok = courseService.addCourse(cid, name, teacher.getId(), dept, cred);
            if (ok) {
                JOptionPane.showMessageDialog(frame,
                        "Course '" + name + "' created successfully under " + dept + "!",
                        "Course Created ✓", JOptionPane.INFORMATION_MESSAGE);
                cidField.setText(""); nameField.setText("");
            } else {
                JOptionPane.showMessageDialog(frame,
                        "Course creation failed. ID or Name may already exist.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        form.add(createBtn, gc);
        p.add(form);
        return p;
    }

    // ── UPLOAD MATERIAL ───────────────────────────────────────────────────────

    private JPanel buildUploadPanel() {
        JPanel p = sectionPanel("📤  Upload Study Material");
        JPanel form = cardForm();
        GridBagConstraints gc = formGc();

        JTextField cidField   = new JTextField(16); styleTextField(cidField, "Course ID");
        JTextField titleField = new JTextField(16); styleTextField(titleField, "Material Title");
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"PDF", "PPT", "DOC"});
        styleCombo(typeCombo);
        typeCombo.setPreferredSize(new Dimension(180, 34));

        addFormRow(form, gc, 0, "Course ID:", cidField);
        addFormRow(form, gc, 1, "Material Title:", titleField);
        addFormRow(form, gc, 2, "File Type:", typeCombo);

        gc.gridx = 1; gc.gridy = 3;
        JButton uploadBtn = makeButton("Upload", COEP_BLUE, Color.WHITE);
        uploadBtn.setPreferredSize(new Dimension(120, 36));
        uploadBtn.addActionListener(e -> {
            String cid   = cidField.getText().trim().toUpperCase();
            String title = titleField.getText().trim();
            String type  = (String) typeCombo.getSelectedItem();
            if (cid.isEmpty() || title.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Course ID and Title are required.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // Simulated upload + notification (same as CLI)
            notificationService.sendInThread("All Students in " + cid,
                    "New material '" + title + "' has been uploaded by " + teacher.getName());
            JOptionPane.showMessageDialog(frame,
                    "Material '" + title + "' (" + type + ") uploaded to course " + cid + ".\nStudents notified.",
                    "Uploaded ✓", JOptionPane.INFORMATION_MESSAGE);
            cidField.setText(""); titleField.setText("");
        });
        form.add(uploadBtn, gc);
        p.add(form);
        return p;
    }

    // ── CREATE ASSIGNMENT ─────────────────────────────────────────────────────

    private JPanel buildCreateAssignPanel() {
        JPanel p = sectionPanel("📝  Create Assignment");

        // My courses combo
        List<Course> myCourses = courseService.getCoursesByTeacher(teacher.getId());
        String[] cids = myCourses.stream().map(Course::getCourseId).toArray(String[]::new);
        JComboBox<String> courseCombo = new JComboBox<>(cids);
        styleCombo(courseCombo);
        courseCombo.setPreferredSize(new Dimension(180, 34));

        JPanel form = cardForm();
        GridBagConstraints gc = formGc();

        JTextField titleField = new JTextField(16); styleTextField(titleField, "Assignment Title");
        JTextField dueField   = new JTextField(16); styleTextField(dueField, "YYYY-MM-DD");
        JSpinner   marksSpinner = new JSpinner(new SpinnerNumberModel(100, 1, 500, 5));
        marksSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        gc.gridx = 0; gc.gridy = 0;
        form.add(makeLabel("Your Course:", new Font("Segoe UI", Font.BOLD, 13), NAVY), gc);
        gc.gridx = 1;
        if (cids.length == 0) {
            form.add(makeLabel("No courses — create a course first.",
                    new Font("Segoe UI", Font.ITALIC, 13), new Color(0x778899)), gc);
        } else {
            form.add(courseCombo, gc);
        }

        addFormRow(form, gc, 1, "Assignment Title:", titleField);
        addFormRow(form, gc, 2, "Due Date (YYYY-MM-DD):", dueField);
        addFormRow(form, gc, 3, "Max Marks:", marksSpinner);

        gc.gridx = 1; gc.gridy = 4;
        JButton createBtn = makeButton("Create Assignment", COEP_BLUE, Color.WHITE);
        createBtn.setPreferredSize(new Dimension(170, 36));
        createBtn.addActionListener(e -> {
            if (cids.length == 0) {
                JOptionPane.showMessageDialog(frame, "You have no courses. Create a course first.",
                        "No Courses", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String  cid   = (String) courseCombo.getSelectedItem();
            String  title = titleField.getText().trim();
            String  due   = dueField.getText().trim();
            int     marks = (int) marksSpinner.getValue();

            if (title.isEmpty() || due.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "All fields are required.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // Date validation — same rules as CLI
            try {
                LocalDate dueDate = LocalDate.parse(due);
                if (!dueDate.isAfter(LocalDate.now())) {
                    JOptionPane.showMessageDialog(frame,
                            "Due date must be after today (" + LocalDate.now() + ").",
                            "Invalid Date", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(frame,
                        "Invalid date format. Use YYYY-MM-DD (e.g. 2026-06-30).",
                        "Invalid Date", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Ownership check (same as CLI TeacherController)
            boolean owns = myCourses.stream().anyMatch(c -> c.getCourseId().equals(cid));
            if (!owns) {
                JOptionPane.showMessageDialog(frame,
                        "Course " + cid + " is not assigned to you.",
                        "Access Denied", JOptionPane.ERROR_MESSAGE);
                return;
            }

            assignmentService.createAssignment(title, cid, teacher.getId(), due, marks);
            notificationService.sendInThread("Students in " + cid,
                    "New assignment '" + title + "' posted. Due: " + due);
            JOptionPane.showMessageDialog(frame,
                    "Assignment '" + title + "' created for course " + cid + ".",
                    "Created ✓", JOptionPane.INFORMATION_MESSAGE);
            titleField.setText(""); dueField.setText("");
        });
        form.add(createBtn, gc);
        p.add(form);
        return p;
    }

    // ── GRADE SUBMISSIONS ─────────────────────────────────────────────────────

    private JPanel buildGradePanel() {
        JPanel p = sectionPanel("✅  Grade Submissions");

        String[] cols = {"Sub ID", "Assignment ID", "Student ID", "Submitted"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = styledTable(model);

        JButton refreshBtn = makeButton("Refresh", COEP_BLUE, Color.WHITE);
        refreshBtn.setPreferredSize(new Dimension(100, 32));
        refreshBtn.addActionListener(e -> loadUngraded(model));

        JPanel topRow = rowPanel();
        topRow.add(refreshBtn);
        p.add(topRow);
        p.add(Box.createVerticalStrut(8));

        JScrollPane sp = new JScrollPane(table);
        sp.setPreferredSize(new Dimension(640, 220));
        sp.setBorder(BorderFactory.createLineBorder(LIGHT_GRAY));
        p.add(sp);
        p.add(Box.createVerticalStrut(10));

        // Grade row
        JPanel gradeRow = rowPanel();
        JTextField subIdField = new JTextField(10); styleTextField(subIdField, "Submission ID");
        JSpinner   marksSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 500, 1));
        marksSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        marksSpinner.setPreferredSize(new Dimension(80, 34));

        // Fill sub ID on row click
        table.getSelectionModel().addListSelectionListener(e -> {
            int sel = table.getSelectedRow();
            if (sel >= 0) {
                subIdField.setText((String) model.getValueAt(sel, 0));
            }
        });

        JButton gradeBtn = makeButton("Grade", new Color(0x1A7A3F), Color.WHITE);
        gradeBtn.setPreferredSize(new Dimension(90, 34));
        gradeBtn.addActionListener(e -> {
            String subId = subIdField.getText().trim().toUpperCase();
            int    marks = (int) marksSpinner.getValue();
            if (subId.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Select or enter a Submission ID.",
                        "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // Find max marks for this submission's assignment
            List<Submission> ungraded = assignmentService.getUngradedByTeacher(teacher.getId());
            Submission target = null;
            for (Submission s : ungraded) {
                if (s.getSubmissionId().equals(subId)) { target = s; break; }
            }
            if (target == null) {
                JOptionPane.showMessageDialog(frame,
                        "Submission not found among ungraded: " + subId,
                        "Not Found", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int maxMarks = assignmentService.findAssignmentById(target.getAssignmentId())
                    .map(Assignment::getMaxMarks).orElse(100);
            if (marks < 0 || marks > maxMarks) {
                JOptionPane.showMessageDialog(frame,
                        "Marks must be between 0 and " + maxMarks + ".",
                        "Invalid Marks", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Calls AssignmentService.gradeSubmission (existing logic)
            boolean ok = assignmentService.gradeSubmission(subId, marks);
            if (ok) {
                notificationService.notifyGraded(
                        "Student " + target.getStudentId(), target.getAssignmentId(), marks);
                JOptionPane.showMessageDialog(frame,
                        "Submission " + subId + " graded with " + marks + " marks.",
                        "Graded ✓", JOptionPane.INFORMATION_MESSAGE);
                loadUngraded(model);
                subIdField.setText("");
            } else {
                JOptionPane.showMessageDialog(frame,
                        "Grading failed.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        gradeRow.add(makeLabel("Sub ID:", new Font("Segoe UI", Font.BOLD, 13), NAVY));
        gradeRow.add(Box.createHorizontalStrut(8));
        gradeRow.add(subIdField);
        gradeRow.add(Box.createHorizontalStrut(12));
        gradeRow.add(makeLabel("Marks:", new Font("Segoe UI", Font.BOLD, 13), NAVY));
        gradeRow.add(Box.createHorizontalStrut(6));
        gradeRow.add(marksSpinner);
        gradeRow.add(Box.createHorizontalStrut(12));
        gradeRow.add(gradeBtn);
        p.add(gradeRow);

        loadUngraded(model);
        return p;
    }

    private void loadUngraded(DefaultTableModel model) {
        model.setRowCount(0);
        for (Submission s : assignmentService.getUngradedByTeacher(teacher.getId())) {
            model.addRow(new Object[]{
                    s.getSubmissionId(), s.getAssignmentId(),
                    s.getStudentId(), s.getSubmissionDate()});
        }
    }

    // ── CREATE QUIZ ───────────────────────────────────────────────────────────

    private JPanel buildCreateQuizPanel() {
        JPanel p = sectionPanel("🧠  Create Quiz");

        List<Course> myCourses = courseService.getCoursesByTeacher(teacher.getId());
        String[] cids = myCourses.stream().map(Course::getCourseId).toArray(String[]::new);

        JPanel    form        = cardForm();
        GridBagConstraints gc = formGc();

        JComboBox<String> courseCombo = new JComboBox<>(cids);
        styleCombo(courseCombo);
        courseCombo.setPreferredSize(new Dimension(180, 34));

        JTextField titleField = new JTextField(16); styleTextField(titleField, "Quiz Title");
        JSpinner   timeLimit  = new JSpinner(new SpinnerNumberModel(30, 5, 180, 5));
        JSpinner   maxAttempts = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));
        JSpinner   numQ       = new JSpinner(new SpinnerNumberModel(5, 1, 20, 1));

        gc.gridx = 0; gc.gridy = 0;
        form.add(makeLabel("Your Course:", new Font("Segoe UI", Font.BOLD, 13), NAVY), gc);
        gc.gridx = 1;
        form.add(cids.length == 0
                ? makeLabel("No courses yet.", new Font("Segoe UI", Font.ITALIC, 13), new Color(0x778899))
                : courseCombo, gc);

        addFormRow(form, gc, 1, "Quiz Title:", titleField);
        addFormRow(form, gc, 2, "Time Limit (min):", timeLimit);
        addFormRow(form, gc, 3, "Max Attempts:", maxAttempts);
        addFormRow(form, gc, 4, "Number of Questions:", numQ);

        gc.gridx = 1; gc.gridy = 5;
        JButton createBtn = makeButton("Create Quiz + Add Questions", COEP_BLUE, Color.WHITE);
        createBtn.setPreferredSize(new Dimension(240, 36));
        createBtn.addActionListener(e -> {
            if (cids.length == 0) {
                JOptionPane.showMessageDialog(frame, "No courses assigned.", "No Courses", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String cid        = (String) courseCombo.getSelectedItem();
            String title      = titleField.getText().trim();
            int    tLimit     = (int) timeLimit.getValue();
            int    maxAtt     = (int) maxAttempts.getValue();
            int    questionCount = (int) numQ.getValue();

            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Quiz Title is required.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // Ownership check
            boolean owns = myCourses.stream().anyMatch(c -> c.getCourseId().equals(cid));
            if (!owns) {
                JOptionPane.showMessageDialog(frame,
                        "Course " + cid + " is not assigned to you.",
                        "Access Denied", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Create quiz in QuizService (existing logic)
            Quiz quiz = quizService.createQuiz(title, cid, teacher.getId(), tLimit, maxAtt);

            // Prompt for each T/F question via dialog
            for (int i = 1; i <= questionCount; i++) {
                JTextField qText = new JTextField(30);
                styleTextField(qText, "Question statement...");
                JComboBox<String> ansCombo = new JComboBox<>(new String[]{"True", "False"});
                styleCombo(ansCombo);

                JPanel qPanel = new JPanel(new GridBagLayout());
                GridBagConstraints qgc = new GridBagConstraints();
                qgc.insets = new Insets(6, 6, 6, 6); qgc.anchor = GridBagConstraints.WEST;
                qgc.gridx = 0; qgc.gridy = 0;
                qPanel.add(makeLabel("Q" + i + " Statement:", new Font("Segoe UI", Font.BOLD, 13), NAVY), qgc);
                qgc.gridx = 1; qPanel.add(qText, qgc);
                qgc.gridx = 0; qgc.gridy = 1;
                qPanel.add(makeLabel("Correct Answer:", new Font("Segoe UI", Font.BOLD, 13), NAVY), qgc);
                qgc.gridx = 1; qPanel.add(ansCombo, qgc);

                int result = JOptionPane.showConfirmDialog(frame, qPanel,
                        "Question " + i + " of " + questionCount + " — " + title,
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (result != JOptionPane.OK_OPTION) {
                    JOptionPane.showMessageDialog(frame,
                            "Quiz creation cancelled at question " + i + ". Already added questions are saved.",
                            "Cancelled", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                String qt  = qText.getText().trim();
                String ans = (String) ansCombo.getSelectedItem();
                if (qt.isEmpty()) { i--; continue; } // re-prompt if empty
                // Calls QuizService.addQuestion (existing logic)
                quizService.addQuestion(quiz.getQuizId(), qt, ans);
            }

            JOptionPane.showMessageDialog(frame,
                    "Quiz '" + title + "' created with " + questionCount + " questions for course " + cid + ".",
                    "Quiz Created ✓", JOptionPane.INFORMATION_MESSAGE);
            titleField.setText("");
        });
        form.add(createBtn, gc);
        p.add(form);
        return p;
    }

    // ── MY COURSES ────────────────────────────────────────────────────────────

    private JPanel buildMyCoursesPanel() {
        JPanel p = sectionPanel("📋  My Courses");

        String[] cols = {"Course ID", "Course Name", "Department", "Credits"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = styledTable(model);

        JButton refreshBtn = makeButton("Refresh", COEP_BLUE, Color.WHITE);
        refreshBtn.setPreferredSize(new Dimension(100, 32));
        refreshBtn.addActionListener(e -> loadMyCourses(model));

        JPanel topRow = rowPanel();
        topRow.add(refreshBtn);
        p.add(topRow);
        p.add(Box.createVerticalStrut(8));

        JScrollPane sp = new JScrollPane(table);
        sp.setPreferredSize(new Dimension(640, 300));
        sp.setBorder(BorderFactory.createLineBorder(LIGHT_GRAY));
        p.add(sp);

        loadMyCourses(model);
        return p;
    }

    private void loadMyCourses(DefaultTableModel model) {
        model.setRowCount(0);
        for (Course c : courseService.getCoursesByTeacher(teacher.getId())) {
            model.addRow(new Object[]{
                    c.getCourseId(), c.getCourseName(), c.getDepartment(), c.getCredits()});
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
        gc.fill = GridBagConstraints.HORIZONTAL; // ← key fix: fields expand & get focus

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
            boolean ok = userService.changePassword(teacher.getId(), current, newPass);
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

    /** Returns a white card form panel with standard border. */
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
