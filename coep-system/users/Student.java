package users;

/**
 * Represents a Student user. Inherits from User (Inheritance OOP principle).
 */
public class Student extends User {

    public Student(int id, String name, String department, String password) {
        super(id, name, "STUDENT", department, password);
    }

    /**
     * Polymorphic override of viewDashboard().
     */
    @Override
    public void viewDashboard() {
        printHeader("STUDENT DASHBOARD");
        System.out.println("\n  Available Actions:");
        System.out.println("  1. Enroll in Course (Dept-restricted)");
        System.out.println("  2. Submit Assignment");
        System.out.println("  3. Fill Exam Form");
        System.out.println("  4. View My Results");
        System.out.println("  5. View My Enrollments");
        System.out.println("  6. Submission History");
        System.out.println("  7. Quiz Center");
        System.out.println("  8. Change Password");
        System.out.println("  0. Logout");
    }
}
