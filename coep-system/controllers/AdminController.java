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
                        System.out.println("\n  Logged out successfully " + admin.getName() + "!");
                        break;
                default: System.out.println("  Invalid option.");
            }
        }
    }

    private void addUser() {
        System.out.println("\n  -- Add New User --");

        String name = InputValidator.readString("  Name: ");

        System.out.println("  Roles: STUDENT | TEACHER | ADMIN | EXAM_CELL");

        String role = InputValidator.readString("  Role: ").toUpperCase();
        String dept = InputValidator.readString("  Department: ");
        String password = InputValidator.readString("  Set Password: ");
        userService.addUser(name, role, dept, password);
    }

    private void removeUser(){
        System.out.println("\n  -- Remove User --");
        userService.printAllUsers();
        System.out.println();

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
            System.out.println(ok ? "  Course removed." : "  [!] Course not found.");
        }
    }

    private void departmentSummary() {
    System.out.println("\n  -- Department Summary --");

    List<User> all = userService.getAllUsers();
    Map<String, Integer> deptCount = new HashMap<>();

    for (User u : all) {
        String dept = u.getDepartment();
        if (deptCount.containsKey(dept)) {
            deptCount.put(dept, deptCount.get(dept) + 1);
        } else {
            deptCount.put(dept, 1);
        }
    }

    System.out.printf("  %-30s %-10s%n", "Department", "Users");
    System.out.println("  ------------------------------------------");

    for (String dept : deptCount.keySet()) {
        System.out.printf("  %-30s %-10d%n", dept, deptCount.get(dept));
    }

    // Role count
    int students = 0, teachers = 0, admins = 0, examCells = 0;

    for (User u : all) {
        String role = u.getRole();
        if (role.equalsIgnoreCase("STUDENT")) students++;
        else if (role.equalsIgnoreCase("TEACHER")) teachers++;
        else if (role.equalsIgnoreCase("ADMIN")) admins++;
        else if (role.equalsIgnoreCase("EXAM_CELL")) examCells++;
    }

    System.out.println("\n  Role breakdown:");
    System.out.println("    Students   : " + students);
    System.out.println("    Teachers   : " + teachers);
    System.out.println("    Admins     : " + admins);
    System.out.println("    Exam Cell  : " + examCells);
    System.out.println("    Total Users: " + all.size());
}
    
}
