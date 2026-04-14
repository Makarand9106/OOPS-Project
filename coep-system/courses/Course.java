package courses;

/**
 * Represents a Course in the COEP system.
 */

public class Course {
    private String courseId;
    private String courseName;
    private int teacherId;
    private String department;
    private int credits;

    public Course(String courseId, String courseName, int teacherId, String department, int credits) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.teacherId = teacherId;
        this.department = department;
        this.credits = credits;
    }

    // Getters and Setters
    public String getCourseId()             { return courseId; }
    public void setCourseId(String id)      { this.courseId = id; }

    public String getCourseName()           { return courseName; }
    public void setCourseName(String name)  { this.courseName = name; }

    public int getTeacherId()               { return teacherId; }
    public void setTeacherId(int id)        { this.teacherId = id; }

    public String getDepartment()           { return department; }
    public void setDepartment(String dept)  { this.department = dept; }

    public int getCredits()                 { return credits; }
    public void setCredits(int c)           { this.credits = c; }

    @Override
    public String toString() {
        return String.format("[%s] %s | Dept: %s | Credits: %d | Teacher ID: %d",
                courseId, courseName, department, credits, teacherId);
    }

    public void printDetailedCourse(java.util.List<Assignment> assignments) {

    System.out.println("\n╔══════════════════════════════════════════╗");
    System.out.printf( "║  %-40s║%n", "COURSE DETAILS");
    System.out.println("╠══════════════════════════════════════════╣");
    System.out.printf( "║  Course ID   : %-26s║%n", courseId);
    System.out.printf( "║  Name        : %-26s║%n", courseName);
    System.out.printf( "║  Department  : %-26s║%n", department);
    System.out.printf( "║  Credits     : %-26d║%n", credits);
    System.out.printf( "║  Teacher ID  : %-26d║%n", teacherId);
    System.out.println("╠══════════════════════════════════════════╣");

    System.out.printf( "║  %-40s║%n", "Assignments");

    boolean found = false;

    for (Assignment a : assignments) {
        if (a.getCourseId().equalsIgnoreCase(this.courseId)) {
            found = true;

            System.out.println("║------------------------------------------║");
            System.out.printf("║  ID   : %-32s║%n", a.getAssignmentId());
            System.out.printf("║  Title: %-32s║%n", a.getTitle());
            System.out.printf("║  Due  : %-32s║%n", a.getDueDate());
            System.out.printf("║  Marks: %-32d║%n", a.getMaxMarks());
        }
    }

    if (!found) {
        System.out.printf("║  %-40s║%n", "No assignments available");
    }

    System.out.println("╚══════════════════════════════════════════╝");
}
}
