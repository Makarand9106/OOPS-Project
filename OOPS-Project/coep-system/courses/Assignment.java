package courses;

/**
 * Represents an Assignment created by a Teacher.
 */
public class Assignment {

    private String assignmentId;
    private String title;
    private String courseId;
    private int teacherId;
    private String dueDate;
    private int maxMarks;

    public Assignment(String assignmentId, String title, String courseId,
                      int teacherId, String dueDate, int maxMarks) {
        this.assignmentId = assignmentId;
        this.title = title;
        this.courseId = courseId;
        this.teacherId = teacherId;
        this.dueDate = dueDate;
        this.maxMarks = maxMarks;
    }

    public String getAssignmentId()            { return assignmentId; }
    public String getTitle()                   { return title; }
    public String getCourseId()                { return courseId; }
    public int getTeacherId()                  { return teacherId; }
    public String getDueDate()                 { return dueDate; }
    public int getMaxMarks()                   { return maxMarks; }

    @Override
    public String toString() {
        return String.format("[%s] %s | Course: %s | Due: %s | Max Marks: %d",
                assignmentId, title, courseId, dueDate, maxMarks);
    }
}
