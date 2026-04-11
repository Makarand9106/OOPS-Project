package users;

/**
 * Represents an Exam Cell Staff user. Inherits from User.
 */
public class ExamCellStaff extends User {

    public ExamCellStaff(int id, String name, String department) {
        super(id, name, "EXAM_CELL", department);
    }

    @Override
    public void viewDashboard() {
        printHeader("EXAM CELL DASHBOARD");
        System.out.println("\n  Available Actions:");
        System.out.println("  1. View Pending Exam Forms");
        System.out.println("  2. Approve Exam Form");
        System.out.println("  3. Generate Hall Ticket (Simulation)");
        System.out.println("  4. Upload Marks / Enter Results");
        System.out.println("  5. Publish Results");
        System.out.println("  6. View All Results");
        System.out.println("  0. Logout");
    }
}
