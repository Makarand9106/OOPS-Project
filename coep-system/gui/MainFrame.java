package gui;

import services.*;
import users.User;

import javax.swing.*;
import java.awt.*;

/**
 * MainFrame — root JFrame of the COEP ERP GUI.
 *
 * Uses CardLayout to switch between LoginPanel and the four role dashboards.
 * Initialises all service singletons here so every dashboard can share them.
 *
 * GUI ↔ Logic connection:
 *   Services (UserService, CourseService, …) are instantiated once and passed
 *   down to each dashboard panel, which call their methods directly on button clicks.
 */
public class MainFrame extends JFrame {

    // ── Shared services (Model layer) ────────────────────────────────────────
    private UserService         userService;
    private CourseService       courseService;
    private AssignmentService   assignmentService;
    private ExamService         examService;
    private NotificationService notificationService;
    private QuizService         quizService;

    // ── Card layout navigation ────────────────────────────────────────────────
    private JPanel     cardPanel;
    private CardLayout cardLayout;

    // Card names
    public static final String CARD_LOGIN      = "LOGIN";
    public static final String CARD_STUDENT    = "STUDENT";
    public static final String CARD_TEACHER    = "TEACHER";
    public static final String CARD_ADMIN      = "ADMIN";
    public static final String CARD_EXAM_CELL  = "EXAM_CELL";

    // Dashboard references (created once, re-used)
    private StudentDashboard    studentDashboard;
    private TeacherDashboard    teacherDashboard;
    private AdminDashboard      adminDashboard;
    private ExamCellDashboard   examCellDashboard;

    // ─────────────────────────────────────────────────────────────────────────

    public MainFrame() {
        super("COEP Academic Portal — Student Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1150, 720);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);                 // centre on screen

        applyLookAndFeel();
        initServices();
        buildUI();

        setVisible(true);
    }

    // ── Look & Feel ──────────────────────────────────────────────────────────

    private void applyLookAndFeel() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");

            // Global font tweak for Nimbus
            UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 13));
            UIManager.put("Table.alternateRowColor", new Color(0xF0F4FA));
            UIManager.put("Table[Enabled+Selected].textForeground", Color.WHITE);

        } catch (Exception e) {
            // Fall back to system default silently
        }
    }

    // ── Service Initialisation ───────────────────────────────────────────────

    private void initServices() {
        try {
            userService         = new UserService();
            courseService       = new CourseService();
            assignmentService   = new AssignmentService();
            examService         = new ExamService();
            notificationService = new NotificationService();
            quizService         = new QuizService();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                "Failed to initialise services:\n" + e.getMessage(),
                "Startup Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    // ── UI Construction ──────────────────────────────────────────────────────

    private void buildUI() {
        cardLayout = new CardLayout();
        cardPanel  = new JPanel(cardLayout);

        // LoginPanel — always present
        LoginPanel loginPanel = new LoginPanel(this, userService);
        cardPanel.add(loginPanel, CARD_LOGIN);

        // Dashboard placeholders — built lazily when needed (added via showDashboard)
        add(cardPanel);
        cardLayout.show(cardPanel, CARD_LOGIN);
    }

    // ── Navigation helpers (called by LoginPanel / dashboards) ───────────────

    /**
     * Called by LoginPanel after a successful login.
     * Builds or refreshes the appropriate dashboard and switches to it.
     *
     * @param user The authenticated User object
     */
    public void showDashboard(User user) {
        String role = user.getRole().toUpperCase();

        switch (role) {
            case "STUDENT": {
                // Remove stale card if it exists, build fresh panel for new session
                if (studentDashboard != null) cardPanel.remove(studentDashboard);
                studentDashboard = new StudentDashboard(this, (users.Student) user,
                        courseService, assignmentService, examService,
                        notificationService, quizService, userService);
                cardPanel.add(studentDashboard, CARD_STUDENT);
                cardLayout.show(cardPanel, CARD_STUDENT);
                break;
            }
            case "TEACHER": {
                if (teacherDashboard != null) cardPanel.remove(teacherDashboard);
                teacherDashboard = new TeacherDashboard(this, (users.Teacher) user,
                        courseService, assignmentService, notificationService,
                        quizService, userService);
                cardPanel.add(teacherDashboard, CARD_TEACHER);
                cardLayout.show(cardPanel, CARD_TEACHER);
                break;
            }
            case "ADMIN": {
                if (adminDashboard != null) cardPanel.remove(adminDashboard);
                adminDashboard = new AdminDashboard(this, (users.Admin) user,
                        userService, courseService);
                cardPanel.add(adminDashboard, CARD_ADMIN);
                cardLayout.show(cardPanel, CARD_ADMIN);
                break;
            }
            case "EXAM_CELL": {
                if (examCellDashboard != null) cardPanel.remove(examCellDashboard);
                examCellDashboard = new ExamCellDashboard(this, (users.ExamCellStaff) user,
                        examService, userService, courseService, notificationService);
                cardPanel.add(examCellDashboard, CARD_EXAM_CELL);
                cardLayout.show(cardPanel, CARD_EXAM_CELL);
                break;
            }
            default:
                JOptionPane.showMessageDialog(this, "Unknown role: " + role,
                        "Error", JOptionPane.ERROR_MESSAGE);
        }

        cardPanel.revalidate();
        cardPanel.repaint();
    }

    /**
     * Navigates back to the Login screen (called by any dashboard on logout).
     */
    public void showLogin() {
        cardLayout.show(cardPanel, CARD_LOGIN);
        cardPanel.revalidate();
        cardPanel.repaint();
    }

    // ── Entry Point ──────────────────────────────────────────────────────────

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainFrame::new);
    }
}
