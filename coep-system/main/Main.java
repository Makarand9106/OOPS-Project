package main;

import controllers.*;
import services.*;
import users.*;
import utils.*;

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
    private static QuizService         quizService;
    

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
                    System.out.println("\n System closed successfully \n");
                    running = false;
                    break;
                default:
                    System.out.println("  Enter a valid choice");
            }
        }
        notificationService.shutdown();
    }

    // ── Service Initialization ───────────────────────────────────────────────

    private static void initServices() {
        try {
            userService         = new UserService();
            courseService       = new CourseService();
            assignmentService   = new AssignmentService();
            examService         = new ExamService();
            notificationService = new NotificationService();
            quizService         = new QuizService();
        } catch (Exception e) {
            System.err.println("  [ERROR] Failed to initialize services: " + e.getMessage());
            System.exit(1);
        }
    }

    // ── Login Flow ───────────────────────────────────────────────────────────

    private static void loginAs(String role) {
        System.out.println();
        int id = InputValidator.readInt("  Enter your User ID: ");
        String password = InputValidator.readPassword("  Enter your Password: ");
        System.out.println();
        User user = userService.login(id, password);

        if (user == null) {
            return;
        }

        if (!user.getRole().equalsIgnoreCase(role)) {
            System.out.println("Invalid id , Doesn't belong to role " + role);
            return;
        }

        System.out.println("\n  Logged in successfully as , " + user.getName() + "!!");

        try {
            if (role.equalsIgnoreCase("STUDENT")) {
                new StudentController((Student) user, courseService, assignmentService,
                        examService, notificationService,
                        quizService, userService).handleMenu();
            }
            else if (role.equalsIgnoreCase("TEACHER")) {
                new TeacherController((Teacher) user, courseService, assignmentService,
                        notificationService, quizService, userService).handleMenu();
            }
            else if (role.equalsIgnoreCase("ADMIN")) {
                new AdminController((Admin) user, userService, courseService).handleMenu();
            }
            else if (role.equalsIgnoreCase("EXAM_CELL")) {
                new ExamCellController((ExamCellStaff) user, examService,
                        userService, courseService, notificationService).handleMenu();
            }
            else {
                System.out.println("Invalid role.");
            }
        } catch (RuntimeException e) {
            // PASSWORD_CHANGED signal: user is logged out immediately
            if ("PASSWORD_CHANGED".equals(e.getMessage())) {
                System.out.println("\n  [SECURITY] Password changed. All active sessions invalidated.");
                System.out.println("  Please log in again with your new password.\n");
            } else {
                throw e; // re-throw any other unexpected runtime exceptions
            }
        }
    }

    // ── UI Helpers ───────────────────────────────────────────────────────────

    private static void printBanner() {
        System.out.println();       
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.printf( "║  %-40s║%n", "College of Engineering Pune (COEP)");
        System.out.printf( "║  %-40s║%n", "Student Management System");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.println();
    }

    private static void printMainMenu() {
        System.out.println();
        System.out.println("             MAIN MENU             ");
        System.out.println();
        System.out.println("    1. Login as Student            ");
        System.out.println("    2. Login as Teacher            ");
        System.out.println("    3. Login as Admin              ");
        System.out.println("    4. Login as Exam Cell Staff    ");
        System.out.println("    5. Exit                        ");
        System.out.println();
    }
}
