package controllers;

import java.util.*;
import services.*;
import users.*;
import utils.InputValidator;

/**
 * Controller for Admin role. Handles user management and course oversight.
 */

public class AdminController {

    private final Admin admin;
    private final UserService userService;
    private final CourseService courseService;

    // ── Default passwords per role ───────────────────────────────────────────
    private static final String DEFAULT_PASS_STUDENT   = "student@coep";
    private static final String DEFAULT_PASS_TEACHER   = "Teacher@coep";
    private static final String DEFAULT_PASS_ADMIN     = "Admin@coeptech";
    private static final String DEFAULT_PASS_EXAM_CELL = "examcell@coep";

    public AdminController(Admin admin, UserService userService, CourseService courseService) {
        this.admin = admin;
        this.userService = userService;
        this.courseService = courseService;
    }

    public void handleMenu() {
        boolean running = true;
        while (running) {
            admin.viewDashboard();
            int choice = InputValidator.readInt("\n  Enter choice: ");
            switch (choice) {
                case 1: addUser();           break;
                case 2: removeUser();        break;
                case 3: listAllUsers();      break;
                case 4: manageCourses();     break;
                case 5: departmentSummary(); break;
                case 6: changePassword();    break;
                case 0: running = false;
                        System.out.println("\n  Logged out successfully " + admin.getName() + "!");
                        break;
                default: System.out.println("  Invalid option.");
            }
        }
    }

    /**
     * Adds a user WITHOUT asking for a password.
     * A default password is auto-assigned based on the role.
     */
    private void addUser() {
        System.out.println("\n  -- Add New User --");
        String name = InputValidator.readString("  Name: ");
        System.out.println("  Roles: STUDENT | TEACHER | ADMIN | EXAM_CELL");
        String role = InputValidator.readString("  Role: ").toUpperCase();
        String dept = InputValidator.readString("  Department: ");

        // Auto-assign default password — no prompt
        String defaultPassword = getDefaultPassword(role);
        if (defaultPassword == null) {
            System.out.println("  [!] Unknown role '" + role + "'. Valid: STUDENT, TEACHER, ADMIN, EXAM_CELL");
            return;
        }

        boolean ok = userService.addUser(name, role, dept, defaultPassword);
        if (ok) {
            System.out.println("  [✓] Default password assigned for role " + role + ": " + defaultPassword);
            System.out.println("      Please share this password with the user.");
        }
    }

    private String getDefaultPassword(String role) {
        switch (role) {
            case "STUDENT":   return DEFAULT_PASS_STUDENT;
            case "TEACHER":   return DEFAULT_PASS_TEACHER;
            case "ADMIN":     return DEFAULT_PASS_ADMIN;
            case "EXAM_CELL": return DEFAULT_PASS_EXAM_CELL;
            default:          return null;
        }
    }

    private void removeUser() {
        System.out.println("\n  -- Remove User --");
        listAllUsers();
        System.out.println();
        int id = InputValidator.readInt("  Enter User ID to remove: ");
        String confirm = InputValidator.readString("  Confirm removal of user " + id + "? (yes/no): ");
        if ("yes".equalsIgnoreCase(confirm)) {
            userService.removeUser(id);
        } else {
            System.out.println("  Cancelled.");
        }
    }

    /**
     * Lists all users in role hierarchy order:
     * ADMIN → EXAM_CELL → TEACHER → STUDENT
     * Within each role, sorted by user ID ascending.
     */
    private void listAllUsers() {
        System.out.println("\n  ══════════════════════════════════════════════════════");
        System.out.println("               ALL USERS  (by Role Hierarchy)          ");
        System.out.println("  ══════════════════════════════════════════════════════");

        List<User> all = userService.getAllUsers();
        if (all.isEmpty()) { System.out.println("  No users found."); return; }

        // Sort: role hierarchy first, then by ID within each role
        all.sort(Comparator.comparingInt((User u) -> roleOrder(u.getRole()))
                           .thenComparingInt(User::getId));

        String currentRole = "";
        for (User u : all) {
            if (!u.getRole().equals(currentRole)) {
                currentRole = u.getRole();
                System.out.println("\n  ┌─ " + roleLabel(currentRole) + " ──────────────────────────────────────────");
                System.out.printf("  │  %-5s %-25s %-30s%n", "ID", "Name", "Department");
                System.out.println("  │  " + "─".repeat(63));
            }
            System.out.printf("  │  %-5d %-25s %-30s%n",
                    u.getId(), u.getName(), u.getDepartment());
        }
        System.out.println("  └" + "─".repeat(67));
        System.out.println("  Total: " + all.size() + " user(s)");
    }

    private void manageCourses() {
        System.out.println("\n  -- Manage Courses --");
        System.out.println("  1. View All Courses (grouped by department)");
        System.out.println("  2. Remove a Course");
        int choice = InputValidator.readInt("  Choice: ");
        if (choice == 1) {
            courseService.printCoursesByDepartment();
        } else if (choice == 2) {
            courseService.printCoursesByDepartment();
            String cid = InputValidator.readString("  Enter Course ID to remove: ").toUpperCase();
            boolean ok = courseService.removeCourse(cid);
            System.out.println(ok ? "  Course removed." : "  [!] Course not found.");
        }
    }

    /**
     * Department summary with role-hierarchy ordering (Admin > Exam Cell > Teacher > Student).
     */
    private void departmentSummary() {
        System.out.println("\n  -- Department Summary --");

        List<User> all = userService.getAllUsers();

        // Group by department, lexicographic
        Map<String, List<User>> byDept = new TreeMap<>();
        for (User u : all) {
            byDept.computeIfAbsent(u.getDepartment(), k -> new ArrayList<>()).add(u);
        }

        int totalStudents = 0, totalTeachers = 0, totalAdmins = 0, totalExamCell = 0;

        for (Map.Entry<String, List<User>> entry : byDept.entrySet()) {
            List<User> users = entry.getValue();

            // Sort within dept: role hierarchy, then by ID
            users.sort(Comparator.comparingInt((User u) -> roleOrder(u.getRole()))
                                 .thenComparingInt(User::getId));

            int s = 0, t = 0, a = 0, e = 0;
            for (User u : users) {
                switch (u.getRole()) {
                    case "STUDENT":   s++; totalStudents++; break;
                    case "TEACHER":   t++; totalTeachers++; break;
                    case "ADMIN":     a++; totalAdmins++;   break;
                    case "EXAM_CELL": e++; totalExamCell++; break;
                }
            }

            System.out.println("\n  ┌─ " + entry.getKey() + "  [Total: " + users.size() + "]");
            if (a > 0) System.out.println("  │  Admins     : " + a);
            if (e > 0) System.out.println("  │  Exam Cell  : " + e);
            if (t > 0) System.out.println("  │  Teachers   : " + t);
            if (s > 0) System.out.println("  │  Students   : " + s);
            System.out.println("  └" + "─".repeat(50));
        }

        System.out.println("\n  ── Overall Role Breakdown ──────────────────────────");
        System.out.println("    Admins     : " + totalAdmins);
        System.out.println("    Exam Cell  : " + totalExamCell);
        System.out.println("    Teachers   : " + totalTeachers);
        System.out.println("    Students   : " + totalStudents);
        System.out.println("    Total      : " + all.size());
    }

    /**
     * Changes admin's own password. Forces logout on success.
     */
    private void changePassword() {
        System.out.println("\n  -- Change Password --");
        String current = InputValidator.readPassword("  Current Password: ");
        String newPass = InputValidator.readPassword("  New Password: ");
        String confirm = InputValidator.readPassword("  Confirm New Password: ");
        if (!newPass.equals(confirm)) {
            System.out.println("  [!] Passwords do not match. Cancelled.");
            return;
        }
        boolean ok = userService.changePassword(admin.getId(), current, newPass);
        if (ok) {
            System.out.println("  Session will now end for security.");
            throw new RuntimeException("PASSWORD_CHANGED");
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /** Role hierarchy: Admin(1) > Exam_Cell(2) > Teacher(3) > Student(4) */
    private static int roleOrder(String role) {
        switch (role) {
            case "ADMIN":     return 1;
            case "EXAM_CELL": return 2;
            case "TEACHER":   return 3;
            case "STUDENT":   return 4;
            default:          return 5;
        }
    }

    private static String roleLabel(String role) {
        switch (role) {
            case "ADMIN":     return "ADMINS";
            case "EXAM_CELL": return "EXAM CELL OFFICERS";
            case "TEACHER":   return "TEACHERS";
            case "STUDENT":   return "STUDENTS";
            default:          return role;
        }
    }
}
