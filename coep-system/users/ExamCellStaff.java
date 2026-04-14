package users;

/**
 * Represents an Exam Cell Staff user. Inherits from User.
 */
public class ExamCellStaff extends User {

    public ExamCellStaff(int id, String name, String department, String password) {
        super(id, name, "EXAM_CELL", department, password);
    }

    @Override
    public void viewDashboard() {
        printHeader("EXAM CELL DASHBOARD");
        System.out.println("\n  Available Actions:");
        System.out.println("  1. View Pending Exam Forms");
        System.out.println("  2. Approve Exam Form");
        System.out.println("  3. Upload Marks / Enter Results");
        System.out.println("  4. Publish Results");
        System.out.println("  5. View All Results");
        System.out.println("  6. Change Password");
        System.out.println("  0. Logout");
    }
}
