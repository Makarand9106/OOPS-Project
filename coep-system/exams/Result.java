package exams;

/**
 * Represents an exam result for a student.
 */
public class Result {

    private String resultId;
    private int studentId;
    private String courseId;
    private String examType;
    private int marksObtained;
    private int maxMarks;
    private String grade;
    private boolean published;

    public Result(String resultId, int studentId, String courseId, String examType,
                  int marksObtained, int maxMarks, String grade, boolean published) {
        this.resultId = resultId;
        this.studentId = studentId;
        this.courseId = courseId;
        this.examType = examType;
        this.marksObtained = marksObtained;
        this.maxMarks = maxMarks;
        this.grade = grade;
        this.published = published;
    }

    public String getResultId()         { return resultId; }
    public int getStudentId()           { return studentId; }
    public String getCourseId()         { return courseId; }
    public String getExamType()         { return examType; }
    public int getMarksObtained()       { return marksObtained; }
    public int getMaxMarks()            { return maxMarks; }
    public String getGrade()            { return grade; }
    public boolean isPublished()        { return published; }
    public void setPublished(boolean p) { this.published = p; }
    public void setGrade(String g)      { this.grade = g; }

    public static String calculateGrade(int marks, int max) {
        double pct = (marks * 100.0) / max;
        if (pct >= 90) return "AA";
        else if (pct >= 80) return "AB";
        else if (pct >= 70) return "BB";
        else if (pct >= 60) return "BC";
        else if (pct >= 50) return "CC";
        else if (pct >= 40) return "CD";
        else if (pct >= 35) return "DD";
        else return "F";
    }

    @Override
    public String toString() {
        return String.format("[%s] Student: %d | Course: %s | Marks: %d/%d | Grade: %s | Published: %s",
                resultId, studentId, courseId, marksObtained, maxMarks, grade, published ? "Yes" : "No");
    }
}
