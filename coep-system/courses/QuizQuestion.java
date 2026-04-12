package courses;

/**
 * Represents a single question belonging to a Quiz.
 */
public class QuizQuestion {

    private String questionId;
    private String quizId;
    private String questionText;
    private String answer;

    public QuizQuestion(String questionId, String quizId,
                        String questionText, String answer) {
        this.questionId = questionId;
        this.quizId = quizId;
        this.questionText = questionText;
        this.answer = answer;
    }

    public String getQuestionId()   { return questionId; }
    public String getQuizId()       { return quizId; }
    public String getQuestionText() { return questionText; }
    public String getAnswer()       { return answer; }

    @Override
    public String toString() {
        return String.format("[%s] (Quiz: %s) %s", questionId, quizId, questionText);
    }
}
