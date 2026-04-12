package courses;

/**
 * Represents a Quiz created by a Teacher for a specific course.
 */
public class Quiz {

    private String quizId;
    private String title;
    private String courseId;
    private int teacherId;
    private int timeLimitMinutes;
    private int maxAttempts;

    public Quiz(String quizId, String title, String courseId,
                int teacherId, int timeLimitMinutes, int maxAttempts) {
        this.quizId = quizId;
        this.title = title;
        this.courseId = courseId;
        this.teacherId = teacherId;
        this.timeLimitMinutes = timeLimitMinutes;
        this.maxAttempts = maxAttempts;
    }

    public String getQuizId()           { return quizId; }
    public String getTitle()            { return title; }
    public String getCourseId()         { return courseId; }
    public int getTeacherId()           { return teacherId; }
    public int getTimeLimitMinutes()    { return timeLimitMinutes; }
    public int getMaxAttempts()         { return maxAttempts; }

    @Override
    public String toString() {
        return String.format("[%s] %s | Course: %s | Time: %d min | Max Attempts: %d",
                quizId, title, courseId, timeLimitMinutes, maxAttempts);
    }
}
