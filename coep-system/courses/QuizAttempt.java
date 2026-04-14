package courses;

/**
 * Represents a student's attempt at a Quiz.
 */
public class QuizAttempt {

    private String attemptId;
    private String quizId;
    private int studentId;
    private int score;
    private int totalQuestions;
    private String attemptDate;
    private boolean completed;

    public QuizAttempt(String attemptId, String quizId, int studentId,
                       int score, int totalQuestions, String attemptDate, boolean completed) {
        this.attemptId = attemptId;
        this.quizId = quizId;
        this.studentId = studentId;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.attemptDate = attemptDate;
        this.completed = completed;
    }

    public String getAttemptId()    { return attemptId; }
    public String getQuizId()       { return quizId; }
    public int getStudentId()       { return studentId; }
    public int getScore()           { return score; }
    public int getTotalQuestions()  { return totalQuestions; }
    public String getAttemptDate()  { return attemptDate; }
    public boolean isCompleted()    { return completed; }

    public void setScore(int score)             { this.score = score; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    @Override
    public String toString() {
        return String.format("[%s] Quiz: %s | Student: %d | Score: %d/%d | Date: %s | Done: %s",
                attemptId, quizId, studentId, score, totalQuestions, attemptDate, completed);
    }
}
