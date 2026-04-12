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
        // System.out.println("  Loading data from CSV files...");
        try {
            userService         = new UserService();
            courseService       = new CourseService();
            assignmentService   = new AssignmentService();
            examService         = new ExamService();
            notificationService = new NotificationService();
            // System.out.println("  [OK] All services initialized.\n");
        } catch (Exception e) {
            System.err.println("  [ERROR] Failed to initialize services: " + e.getMessage());
            System.exit(1);
        }
    }

    // ── Login Flow ───────────────────────────────────────────────────────────

   private static void loginAs(String role) {
    System.out.println("\n  Login as " + role + "\t\t");
    System.out.println("  Available " + role + " accounts:");

    for (User u : userService.getUsersByRole(role)) {
        System.out.println("    ID: " + u.getId() + "  |  Name: " + u.getName());
    }

    int id = InputValidator.readInt("  Enter your User ID: ");
    String password = InputValidator.readPassword("  Enter your Password: ");

    User user = userService.login(id, password);

    if (user == null) {
        return;
    }

    if (!user.getRole().equalsIgnoreCase(role)) {
        System.out.println("Invalid id , Doesn't belong to role " + role);
        return;
    }

    System.out.println("\n  Logged in successfully as , " + user.getName() + "!!");

    if (role.equalsIgnoreCase("STUDENT")){ 
        new StudentController((Student) user, courseService, assignmentService, examService, notificationService).handleMenu(); 
    }

    else if (role.equalsIgnoreCase("TEACHER")){ 
        new TeacherController((Teacher) user, courseService, assignmentService, notificationService).handleMenu(); 
    }

    else if (role.equalsIgnoreCase("ADMIN")){
        new AdminController((Admin) user, userService, courseService).handleMenu(); 
    }

    else if (role.equalsIgnoreCase("EXAM_CELL")){
        new ExamCellController((ExamCellStaff) user, examService, userService, notificationService).handleMenu(); 
    }

    else{ 
        System.out.println("Invalid role."); 
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
        // System.out.println("  ------------------------------------");
        System.out.println("             MAIN MENU             ");
        // System.out.println("  ------------------------------------");
        System.out.println();
        System.out.println("    1. Login as Student            ");
        System.out.println("    2. Login as Teacher            ");
        System.out.println("    3. Login as Admin              ");
        System.out.println("    4. Login as Exam Cell Staff    ");
        System.out.println("    5. Exit                        ");
        System.out.println();
        // System.out.println("  ------------------------------------");
    }
}
