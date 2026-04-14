package exams;

/**
 * Represents a student's exam application form.
 */
public class ExamForm {

    private String formId;
    private int studentId;
    private String courseId;
    private String examType;
    private String status;   // PENDING, APPROVED, REJECTED
    private String appliedDate;
 
    public ExamForm(String formId, int studentId, String courseId,
                    String examType, String status, String appliedDate) {
        this.formId = formId;
        this.studentId = studentId;
        this.courseId = courseId;
        this.examType = examType;
        this.status = status;
        this.appliedDate = appliedDate;
    }

    public String getFormId()     { return formId; }
    public int getStudentId()     { return studentId; }
    public String getCourseId()   { return courseId; }
    public String getExamType()   { return examType; }
    public String getStatus()     { return status; }
    public void setStatus(String s){ this.status = s; }
    public String getAppliedDate(){ return appliedDate; }

    @Override
    public String toString() {
        return String.format("[%s] Student: %d | Course: %s | Type: %s | Status: %s | Date: %s",
                formId, studentId, courseId, examType, status, appliedDate);
    }
}
