package gui;

import courses.Course;
import services.CourseService;
import services.UserService;
import users.Admin;
import users.User;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;
import java.util.List;

import static gui.LoginPanel.*;

/**
 * AdminDashboard — full Swing dashboard for the Admin role.
 *
 * GUI ↔ Logic connection:
 *   Calls UserService and CourseService directly on button events.
 *   Default passwords are auto-assigned per role (same logic as AdminController).
 */
public class AdminDashboard extends JPanel {

    // Default passwords — mirrors AdminController constants exactly
    private static final String DEFAULT_PASS_STUDENT   = "student@coep";
    private static final String DEFAULT_PASS_TEACHER   = "Teacher@coep";
    private static final String DEFAULT_PASS_ADMIN     = "Admin@coeptech";
    private static final String DEFAULT_PASS_EXAM_CELL = "examcell@coep";

    private final MainFrame     frame;
    private final Admin         admin;
    private final UserService   userService;
    private final CourseService courseService;

    private JPanel     contentPanel;
    private CardLayout contentLayout;
    private JButton    activeBtn = null;

    // ─────────────────────────────────────────────────────────────────────────

    public AdminDashboard(MainFrame frame, Admin admin,
                          UserService userService, CourseService courseService) {
        this.frame         = frame;
        this.admin         = admin;
        this.userService   = userService;
        this.courseService = courseService;

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

        JLabel title = makeLabel("Admin Dashboard",
                new Font("Segoe UI", Font.BOLD, 20), NAVY);
        JLabel info  = makeLabel("ID: " + admin.getId() + "  |  Dept: " + admin.getDepartment(),
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
                    "Logged out successfully. Goodbye, " + admin.getName() + "!",
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

        JLabel greet = makeLabel("  🛡️ " + admin.getName(),
                new Font("Segoe UI", Font.BOLD, 13), GOLD);
        greet.setAlignmentX(Component.LEFT_ALIGNMENT);
        greet.setBorder(new EmptyBorder(8, 12, 16, 12));
        sidebar.add(greet);

        String[][] items = {
            {"➕  Add User",          "ADD_USER"},
            {"🗑️  Remove User",        "REMOVE_USER"},
            {"👥  All Users",          "ALL_USERS"},
            {"📚  Manage Courses",     "MANAGE_COURSES"},
            {"📊  Dept Summary",       "DEPT_SUMMARY"},
            {"🔑  Change Password",    "CHANGE_PASS"},
        };

        for (String[] item : items) {
            JButton btn = makeSidebarButton(item[0]);
            btn.addActionListener(e -> {
                setActiveButton(btn);
                contentLayout.show(contentPanel, item[1]);
            });
            sidebar.add(btn);
            if (item[1].equals("ADD_USER")) {
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

        contentPanel.add(buildAddUserPanel(),       "ADD_USER");
        contentPanel.add(buildRemoveUserPanel(),    "REMOVE_USER");
        contentPanel.add(buildAllUsersPanel(),      "ALL_USERS");
        contentPanel.add(buildManageCoursesPanel(), "MANAGE_COURSES");
        contentPanel.add(buildDeptSummaryPanel(),   "DEPT_SUMMARY");
        contentPanel.add(buildChangePassPanel(),    "CHANGE_PASS");

        contentLayout.show(contentPanel, "ADD_USER");
        return contentPanel;
    }

    // ── ADD USER ─────────────────────────────────────────────────────────────

    private JPanel buildAddUserPanel() {
        JPanel p = sectionPanel("➕  Add New User");

        JPanel info = new JPanel(new FlowLayout(FlowLayout.LEFT));
        info.setOpaque(false);
        info.add(makeLabel(
                "Default passwords are auto-assigned per role. Share them with the new user.",
                new Font("Segoe UI", Font.ITALIC, 12), new Color(0x556677)));
        info.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(info);
        p.add(Box.createVerticalStrut(10));

        JPanel form = cardForm();
        GridBagConstraints gc = formGc();

        JTextField nameField = new JTextField(18); styleTextField(nameField, "Full Name");
        JComboBox<String> roleCombo = new JComboBox<>(
                new String[]{"STUDENT", "TEACHER", "ADMIN", "EXAM_CELL"});
        styleCombo(roleCombo);
        roleCombo.setPreferredSize(new Dimension(200, 34));
        JTextField deptField = new JTextField(18); styleTextField(deptField, "Department");

        // Live password preview
        JLabel passPreview = makeLabel("  default: " + DEFAULT_PASS_STUDENT,
                new Font("Segoe UI", Font.ITALIC, 12), new Color(0x556677));
        roleCombo.addActionListener(e -> {
            String r = (String) roleCombo.getSelectedItem();
            String dp = getDefaultPassword(r);
            passPreview.setText("  default: " + (dp != null ? dp : "N/A"));
        });

        addFormRow(form, gc, 0, "Name:", nameField);
        addFormRow(form, gc, 1, "Role:", roleCombo);
        gc.gridx = 0; gc.gridy = 2;
        form.add(makeLabel("Default Password:", new Font("Segoe UI", Font.BOLD, 13), NAVY), gc);
        gc.gridx = 1;
        form.add(passPreview, gc);
        addFormRow(form, gc, 3, "Department:", deptField);

        gc.gridx = 1; gc.gridy = 4;
        JButton addBtn = makeButton("Add User", COEP_BLUE, Color.WHITE);
        addBtn.setPreferredSize(new Dimension(120, 36));
        addBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String role = (String) roleCombo.getSelectedItem();
            String dept = deptField.getText().trim();

            if (name.isEmpty() || dept.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Name and Department are required.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String defaultPass = getDefaultPassword(role);
            if (defaultPass == null) {
                JOptionPane.showMessageDialog(frame, "Unknown role: " + role,
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Calls UserService.addUser (existing logic)
            boolean ok = userService.addUser(name, role, dept, defaultPass);
            if (ok) {
                JOptionPane.showMessageDialog(frame,
                        "User '" + name + "' added as " + role + ".\n" +
                        "Default password: " + defaultPass + "\n" +
                        "Please share the password with the user.",
                        "User Added ✓", JOptionPane.INFORMATION_MESSAGE);
                nameField.setText(""); deptField.setText("");
            } else {
                JOptionPane.showMessageDialog(frame,
                        "Failed to add user. Check role or try again.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        form.add(addBtn, gc);
        p.add(form);
        return p;
    }

    // ── REMOVE USER ───────────────────────────────────────────────────────────

    private JPanel buildRemoveUserPanel() {
        JPanel p = sectionPanel("🗑️  Remove User");

        String[] cols = {"ID", "Name", "Role", "Department"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = styledTable(model);

        JButton refreshBtn = makeButton("Refresh List", COEP_BLUE, Color.WHITE);
        refreshBtn.setPreferredSize(new Dimension(120, 32));
        refreshBtn.addActionListener(e -> loadUsersToTable(model));

        JPanel topRow = rowPanel();
        topRow.add(refreshBtn);
        p.add(topRow);
        p.add(Box.createVerticalStrut(8));

        JScrollPane sp = new JScrollPane(table);
        sp.setPreferredSize(new Dimension(640, 240));
        sp.setBorder(BorderFactory.createLineBorder(LIGHT_GRAY));
        p.add(sp);
        p.add(Box.createVerticalStrut(10));

        JPanel removeRow = rowPanel();
        JTextField idField = new JTextField(8); styleTextField(idField, "User ID");
        // Fill ID from selected row
        table.getSelectionModel().addListSelectionListener(e -> {
            int sel = table.getSelectedRow();
            if (sel >= 0) idField.setText(String.valueOf(model.getValueAt(sel, 0)));
        });

        JButton removeBtn = makeButton("Remove", new Color(0xCC3333), Color.WHITE);
        removeBtn.setPreferredSize(new Dimension(100, 34));
        removeBtn.addActionListener(e -> {
            String idText = idField.getText().trim();
            if (idText.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Select or enter a User ID.",
                        "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int uid;
            try { uid = Integer.parseInt(idText); }
            catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "User ID must be a number.",
                        "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(frame,
                    "Are you sure you want to REMOVE user with ID " + uid + "?\nThis action cannot be undone.",
                    "Confirm Removal", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                // Calls UserService.removeUser (existing logic)
                boolean ok = userService.removeUser(uid);
                if (ok) {
                    JOptionPane.showMessageDialog(frame, "User " + uid + " removed successfully.",
                            "Removed ✓", JOptionPane.INFORMATION_MESSAGE);
                    loadUsersToTable(model);
                    idField.setText("");
                } else {
                    JOptionPane.showMessageDialog(frame, "User ID " + uid + " not found.",
                            "Not Found", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        removeRow.add(makeLabel("User ID:", new Font("Segoe UI", Font.BOLD, 13), NAVY));
        removeRow.add(Box.createHorizontalStrut(8));
        removeRow.add(idField);
        removeRow.add(Box.createHorizontalStrut(10));
        removeRow.add(removeBtn);
        p.add(removeRow);

        loadUsersToTable(model);
        return p;
    }

    // ── ALL USERS ─────────────────────────────────────────────────────────────

    private JPanel buildAllUsersPanel() {
        JPanel p = sectionPanel("👥  All Users");

        String[] cols = {"ID", "Name", "Role", "Department"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = styledTable(model);

        // Colour-code rows by role
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    String role = (String) model.getValueAt(row, 2);
                    switch (role) {
                        case "ADMIN":     c.setBackground(new Color(0xFFF3CD)); break;
                        case "TEACHER":   c.setBackground(new Color(0xD4EDDA)); break;
                        case "EXAM_CELL": c.setBackground(new Color(0xD1ECF1)); break;
                        default:          c.setBackground(Color.WHITE); break;
                    }
                }
                return c;
            }
        });

        JButton refreshBtn = makeButton("Refresh", COEP_BLUE, Color.WHITE);
        refreshBtn.setPreferredSize(new Dimension(100, 32));
        refreshBtn.addActionListener(e -> loadAllUsers(model));

        // Role filter
        JComboBox<String> filterCombo = new JComboBox<>(
                new String[]{"ALL", "STUDENT", "TEACHER", "ADMIN", "EXAM_CELL"});
        styleCombo(filterCombo);
        filterCombo.setPreferredSize(new Dimension(140, 32));
        JButton filterBtn = makeButton("Filter", COEP_BLUE, Color.WHITE);
        filterBtn.setPreferredSize(new Dimension(80, 32));
        filterBtn.addActionListener(e -> {
            String sel = (String) filterCombo.getSelectedItem();
            model.setRowCount(0);
            List<User> users = "ALL".equals(sel)
                    ? userService.getAllUsers()
                    : userService.getUsersByRole(sel);
            users.sort(Comparator.comparingInt(User::getId));
            for (User u : users) {
                model.addRow(new Object[]{u.getId(), u.getName(), u.getRole(), u.getDepartment()});
            }
        });

        JPanel topRow = rowPanel();
        topRow.add(refreshBtn);
        topRow.add(Box.createHorizontalStrut(16));
        topRow.add(makeLabel("Filter:", new Font("Segoe UI", Font.BOLD, 13), NAVY));
        topRow.add(Box.createHorizontalStrut(6));
        topRow.add(filterCombo);
        topRow.add(Box.createHorizontalStrut(6));
        topRow.add(filterBtn);
        p.add(topRow);
        p.add(Box.createVerticalStrut(8));

        JScrollPane sp = new JScrollPane(table);
        sp.setPreferredSize(new Dimension(640, 320));
        sp.setBorder(BorderFactory.createLineBorder(LIGHT_GRAY));
        p.add(sp);

        loadAllUsers(model);
        return p;
    }

    private void loadAllUsers(DefaultTableModel model) {
        model.setRowCount(0);
        List<User> users = userService.getAllUsers();
        // Sort: role hierarchy (Admin → Exam Cell → Teacher → Student), then by ID
        users.sort(Comparator.comparingInt((User u) -> roleOrder(u.getRole()))
                             .thenComparingInt(User::getId));
        for (User u : users) {
            model.addRow(new Object[]{u.getId(), u.getName(), u.getRole(), u.getDepartment()});
        }
    }

    private void loadUsersToTable(DefaultTableModel model) {
        model.setRowCount(0);
        List<User> users = userService.getAllUsers();
        users.sort(Comparator.comparingInt(User::getId));
        for (User u : users) {
            model.addRow(new Object[]{u.getId(), u.getName(), u.getRole(), u.getDepartment()});
        }
    }

    // ── MANAGE COURSES ────────────────────────────────────────────────────────

    private JPanel buildManageCoursesPanel() {
        JPanel p = sectionPanel("📚  Manage Courses");

        String[] cols = {"Course ID", "Course Name", "Teacher ID", "Department", "Credits"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = styledTable(model);

        JButton refreshBtn = makeButton("Refresh", COEP_BLUE, Color.WHITE);
        refreshBtn.setPreferredSize(new Dimension(100, 32));
        refreshBtn.addActionListener(e -> loadCourses(model));

        JPanel topRow = rowPanel();
        topRow.add(refreshBtn);
        p.add(topRow);
        p.add(Box.createVerticalStrut(8));

        JScrollPane sp = new JScrollPane(table);
        sp.setPreferredSize(new Dimension(680, 240));
        sp.setBorder(BorderFactory.createLineBorder(LIGHT_GRAY));
        p.add(sp);
        p.add(Box.createVerticalStrut(10));

        // Remove row
        JPanel removeRow = rowPanel();
        JTextField cidField = new JTextField(10); styleTextField(cidField, "Course ID");
        table.getSelectionModel().addListSelectionListener(e -> {
            int sel = table.getSelectedRow();
            if (sel >= 0) cidField.setText((String) model.getValueAt(sel, 0));
        });

        JButton removeBtn = makeButton("Remove Course", new Color(0xCC3333), Color.WHITE);
        removeBtn.setPreferredSize(new Dimension(150, 34));
        removeBtn.addActionListener(e -> {
            String cid = cidField.getText().trim().toUpperCase();
            if (cid.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Select or enter a Course ID.",
                        "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(frame,
                    "Remove course '" + cid + "'? This cannot be undone.",
                    "Confirm Removal", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                boolean ok = courseService.removeCourse(cid);
                if (ok) {
                    JOptionPane.showMessageDialog(frame, "Course " + cid + " removed.",
                            "Removed ✓", JOptionPane.INFORMATION_MESSAGE);
                    loadCourses(model);
                    cidField.setText("");
                } else {
                    JOptionPane.showMessageDialog(frame, "Course not found: " + cid,
                            "Not Found", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        removeRow.add(makeLabel("Course ID:", new Font("Segoe UI", Font.BOLD, 13), NAVY));
        removeRow.add(Box.createHorizontalStrut(8));
        removeRow.add(cidField);
        removeRow.add(Box.createHorizontalStrut(10));
        removeRow.add(removeBtn);
        p.add(removeRow);

        loadCourses(model);
        return p;
    }

    private void loadCourses(DefaultTableModel model) {
        model.setRowCount(0);
        List<Course> courses = courseService.getAllCourses();
        courses.sort(Comparator.comparing(Course::getDepartment));
        for (Course c : courses) {
            model.addRow(new Object[]{
                    c.getCourseId(), c.getCourseName(), c.getTeacherId(),
                    c.getDepartment(), c.getCredits()});
        }
    }

    // ── DEPARTMENT SUMMARY ────────────────────────────────────────────────────

    private JPanel buildDeptSummaryPanel() {
        JPanel p = sectionPanel("📊  Department Summary");

        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        area.setBackground(new Color(0xF7F9FC));
        area.setBorder(new EmptyBorder(10, 14, 10, 14));

        JButton refreshBtn = makeButton("Refresh", COEP_BLUE, Color.WHITE);
        refreshBtn.setPreferredSize(new Dimension(100, 32));
        refreshBtn.addActionListener(e -> buildSummaryText(area));

        JPanel topRow = rowPanel();
        topRow.add(refreshBtn);
        p.add(topRow);
        p.add(Box.createVerticalStrut(8));

        JScrollPane sp = new JScrollPane(area);
        sp.setPreferredSize(new Dimension(640, 360));
        sp.setBorder(BorderFactory.createLineBorder(LIGHT_GRAY));
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(sp);

        buildSummaryText(area);
        return p;
    }

    private void buildSummaryText(JTextArea area) {
        List<User> all = userService.getAllUsers();
        Map<String, List<User>> byDept = new TreeMap<>();
        for (User u : all) {
            byDept.computeIfAbsent(u.getDepartment(), k -> new ArrayList<>()).add(u);
        }

        StringBuilder sb = new StringBuilder();
        int totalS = 0, totalT = 0, totalA = 0, totalE = 0;

        for (Map.Entry<String, List<User>> entry : byDept.entrySet()) {
            List<User> users = entry.getValue();
            int s = 0, t = 0, a = 0, e = 0;
            for (User u : users) {
                switch (u.getRole()) {
                    case "STUDENT":   s++; totalS++; break;
                    case "TEACHER":   t++; totalT++; break;
                    case "ADMIN":     a++; totalA++; break;
                    case "EXAM_CELL": e++; totalE++; break;
                }
            }
            sb.append(String.format("┌─ Department: %s  [Total: %d]\n",
                    entry.getKey(), users.size()));
            if (a > 0) sb.append("│  Admins    : ").append(a).append("\n");
            if (e > 0) sb.append("│  Exam Cell : ").append(e).append("\n");
            if (t > 0) sb.append("│  Teachers  : ").append(t).append("\n");
            if (s > 0) sb.append("│  Students  : ").append(s).append("\n");
            sb.append("└").append("─".repeat(55)).append("\n\n");
        }

        sb.append("── Overall Breakdown ────────────────────────────\n");
        sb.append(String.format("  Admins     : %d\n", totalA));
        sb.append(String.format("  Exam Cell  : %d\n", totalE));
        sb.append(String.format("  Teachers   : %d\n", totalT));
        sb.append(String.format("  Students   : %d\n", totalS));
        sb.append(String.format("  Total      : %d\n", all.size()));

        area.setText(sb.toString());
        area.setCaretPosition(0);
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
            boolean ok = userService.changePassword(admin.getId(), current, newPass);
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

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String getDefaultPassword(String role) {
        switch (role) {
            case "STUDENT":   return DEFAULT_PASS_STUDENT;
            case "TEACHER":   return DEFAULT_PASS_TEACHER;
            case "ADMIN":     return DEFAULT_PASS_ADMIN;
            case "EXAM_CELL": return DEFAULT_PASS_EXAM_CELL;
            default:          return null;
        }
    }

    /** Role hierarchy order: Admin(1) > Exam_Cell(2) > Teacher(3) > Student(4) */
    private static int roleOrder(String role) {
        switch (role) {
            case "ADMIN":     return 1;
            case "EXAM_CELL": return 2;
            case "TEACHER":   return 3;
            case "STUDENT":   return 4;
            default:          return 5;
        }
    }

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
