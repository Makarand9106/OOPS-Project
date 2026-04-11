package controllers;

import services.*;
import users.*;
import utils.InputValidator;

import java.util.*;

/**
 * Controller for Admin role. Handles user management and course oversight.
 */
public class AdminController {

    private final Admin admin;
    private final UserService userService;
    private final CourseService courseService;

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
                case 1: addUser();               break;
                case 2: removeUser();            break;
                case 3: listAllUsers();          break;
                case 4: manageCourses();         break;
                case 5: departmentSummary();     break;
                case 0: running = false;
                        System.out.println("\n  Logged out. Goodbye, " + admin.getName() + "!");
                        break;
                default: System.out.println("  [!] Invalid option.");
            }
        }
    }

    private void addUser() {
        System.out.println("\n  -- Add New User --");
        String name = InputValidator.readString("  Full Name: ");
        System.out.println("  Roles: STUDENT | TEACHER | ADMIN | EXAM_CELL");
        String role = InputValidator.readString("  Role: ").toUpperCase();
        String dept = InputValidator.readString("  Department: ");
        userService.addUser(name, role, dept);
    }

    private void removeUser() {
        System.out.println("\n  -- Remove User --");
        userService.printAllUsers();
        int id = InputValidator.readInt("  Enter User ID to remove: ");
        String confirm = InputValidator.readString("  Confirm removal of user " + id + "? (yes/no): ");
        if ("yes".equalsIgnoreCase(confirm)) {
            userService.removeUser(id);
        } else {
            System.out.println("  Cancelled.");
        }
    }

    private void listAllUsers() {
        userService.printAllUsers();
    }

    private void manageCourses() {
        System.out.println("\n  -- Manage Courses --");
        System.out.println("  1. View All Courses");
        System.out.println("  2. Remove a Course");
        int choice = InputValidator.readInt("  Choice: ");
        if (choice == 1) {
            courseService.printAllCourses();
        } else if (choice == 2) {
            courseService.printAllCourses();
            String cid = InputValidator.readString("  Enter Course ID to remove: ").toUpperCase();
            boolean ok = courseService.removeCourse(cid);
            System.out.println(ok ? "  [+] Course removed." : "  [!] Course not found.");
        }
    }

    private void departmentSummary() {
        System.out.println("\n  -- Department Summary --");
        List<User> all = userService.getAllUsers();
        Map<String, Integer> deptCount = new LinkedHashMap<>();
        for (User u : all) {
            deptCount.merge(u.getDepartment(), 1, Integer::sum);
        }
        System.out.printf("  %-30s %-10s%n", "Department", "Users");
        System.out.println("  " + "-".repeat(42));
        for (Map.Entry<String, Integer> e : deptCount.entrySet()) {
            System.out.printf("  %-30s %-10d%n", e.getKey(), e.getValue());
        }
        System.out.println("\n  Role breakdown:");
        List<User> students  = userService.getUsersByRole("STUDENT");
        List<User> teachers  = userService.getUsersByRole("TEACHER");
        List<User> admins    = userService.getUsersByRole("ADMIN");
        List<User> examCells = userService.getUsersByRole("EXAM_CELL");
        System.out.println("    Students   : " + students.size());
        System.out.println("    Teachers   : " + teachers.size());
        System.out.println("    Admins     : " + admins.size());
        System.out.println("    Exam Cell  : " + examCells.size());
        System.out.println("    Total Users: " + all.size());
    }
}
