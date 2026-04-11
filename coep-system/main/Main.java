package main;

import controllers.*;
import services.*;
import users.*;
import utils.InputValidator;

/**
 * Entry point for COEP System.
 * Demonstrates all OOP concepts: Abstraction, Inheritance, Polymorphism, Encapsulation.
 * Uses CSV for persistence, ExecutorService for concurrency (NotificationService).
 */
public class Main {

    private static UserService         userService;
    private static CourseService       courseService;
    private static AssignmentService   assignmentService;
    private static ExamService         examService;
    private static NotificationService notificationService;

    public static void main(String[] args) {
        printBanner();
        initServices();

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = InputValidator.readInt("  Select an option: ");
            switch (choice) {
                case 1: loginAs("STUDENT");   break;
                case 2: loginAs("TEACHER");   break;
                case 3: loginAs("ADMIN");     break;
                case 4: loginAs("EXAM_CELL"); break;
                case 5:
                    System.out.println("\n  Thank you for using COEP System. Goodbye!\n");
                    running = false;
                    break;
                default:
                    System.out.println("  [!] Invalid choice. Please try again.");
            }
        }
        notificationService.shutdown();
    }

    // ── Service Initialization ───────────────────────────────────────────────

    private static void initServices() {
        System.out.println("  Loading data from CSV files...");
        try {
            userService         = new UserService();
            courseService       = new CourseService();
            assignmentService   = new AssignmentService();
            examService         = new ExamService();
            notificationService = new NotificationService();
            System.out.println("  [OK] All services initialized.\n");
        } catch (Exception e) {
            System.err.println("  [ERROR] Failed to initialize services: " + e.getMessage());
            System.exit(1);
        }
    }

    // ── Login Flow ───────────────────────────────────────────────────────────

    private static void loginAs(String role) {
        System.out.println("\n  --- Login as " + role + " ---");
        System.out.println("  Available " + role + " accounts:");
        for (User u : userService.getUsersByRole(role)) {
            System.out.println("    ID: " + u.getId() + "  |  Name: " + u.getName());
        }
        int id = InputValidator.readInt("  Enter your User ID: ");
        User user = userService.findById(id);

        if (user == null) {
            System.out.println("  [!] User ID not found.");
            return;
        }
        if (!user.getRole().equalsIgnoreCase(role)) {
            System.out.println("  [!] This ID does not belong to a " + role + " account.");
            return;
        }
        System.out.println("\n  Welcome, " + user.getName() + "!");

        // Polymorphic dispatch — viewDashboard() and controller routing
        switch (role) {
            case "STUDENT":
                new StudentController((Student) user, courseService,
                        assignmentService, examService, notificationService).handleMenu();
                break;
            case "TEACHER":
                new TeacherController((Teacher) user, courseService,
                        assignmentService, notificationService).handleMenu();
                break;
            case "ADMIN":
                new AdminController((Admin) user, userService, courseService).handleMenu();
                break;
            case "EXAM_CELL":
                new ExamCellController((ExamCellStaff) user, examService,
                        userService, notificationService).handleMenu();
                break;
        }
    }

    // ── UI Helpers ───────────────────────────────────────────────────────────

    private static void printBanner() {
        System.out.println();
        System.out.println("  +==============================================+");
        System.out.println("  |   College of Engineering Pune (COEP)        |");
        System.out.println("  |      Student Management System v1.0         |");
        System.out.println("  |   Built with Core Java | OOP | CSV | Threads|");
        System.out.println("  +==============================================+");
        System.out.println();
    }

    private static void printMainMenu() {
        System.out.println();
        System.out.println("  +----------------------------------+");
        System.out.println("  |           MAIN MENU             |");
        System.out.println("  +----------------------------------+");
        System.out.println("  |  1. Login as Student            |");
        System.out.println("  |  2. Login as Teacher            |");
        System.out.println("  |  3. Login as Admin              |");
        System.out.println("  |  4. Login as Exam Cell Staff    |");
        System.out.println("  |  5. Exit                        |");
        System.out.println("  +----------------------------------+");
    }
}
