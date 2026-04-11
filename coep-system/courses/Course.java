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
}
