package users;

/**
 * Represents a Teacher user. Inherits from User (Inheritance OOP principle).
 */
public class Teacher extends User {

    public Teacher(int id, String name, String department) {
        super(id, name, "TEACHER", department);
    }

    @Override
    public void viewDashboard() {
        printHeader("TEACHER DASHBOARD");
        System.out.println("\n  Available Actions:");
        System.out.println("  1. Create Course");
        System.out.println("  2. Upload Study Material (Simulation)");
        System.out.println("  3. Create Assignment");
        System.out.println("  4. Grade Assignment Submissions");
        System.out.println("  5. Create Quiz (In-Memory)");
        System.out.println("  6. View My Courses");
        System.out.println("  0. Logout");
    }
}
