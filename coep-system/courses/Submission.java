package courses;

/**
 * Represents a student's assignment submission.
 */
public class Submission {

    private String submissionId;
    private String assignmentId;
    private int studentId;
    private String submissionDate;
    private int marksObtained;
    private boolean graded;

    public Submission(String submissionId, String assignmentId, int studentId,
                      String submissionDate, int marksObtained, boolean graded) {
        this.submissionId = submissionId;
        this.assignmentId = assignmentId;
        this.studentId = studentId;
        this.submissionDate = submissionDate;
        this.marksObtained = marksObtained;
        this.graded = graded;
    }

    public String getSubmissionId()  { return submissionId; }
    public String getAssignmentId()  { return assignmentId; }
    public int getStudentId()        { return studentId; }
    public String getSubmissionDate(){ return submissionDate; }
    public int getMarksObtained()    { return marksObtained; }
    public boolean isGraded()        { return graded; }
    public void setGraded(boolean g) { this.graded = g; }
    public void setMarksObtained(int m) { this.marksObtained = m; }

    @Override
    public String toString() {
        return String.format("[%s] Assignment: %s | Student: %d | Marks: %d | Graded: %s",
                submissionId, assignmentId, studentId, marksObtained, graded ? "Yes" : "No");
    }
}
