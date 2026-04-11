package users;

/**
 * Represents an Admin user. Inherits from User (Inheritance OOP principle).
 */
public class Admin extends User {

    public Admin(int id, String name, String department) {
        super(id, name, "ADMIN", department);
    }

    @Override
    public void viewDashboard() {
        printHeader("ADMIN DASHBOARD");
        System.out.println("\n  Available Actions:");
        System.out.println("  1. Add New User");
        System.out.println("  2. Remove User");
        System.out.println("  3. List All Users");
        System.out.println("  4. Manage Courses");
        System.out.println("  5. View Department Summary");
        System.out.println("  0. Logout");
    }
}
