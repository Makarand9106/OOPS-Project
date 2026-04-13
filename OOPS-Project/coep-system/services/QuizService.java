package services;

import courses.Quiz;
import courses.QuizAttempt;
import courses.QuizQuestion;
import java.util.*;
import utils.CSVHandler;

/**
 * Service for managing quizzes, quiz questions, and student attempts.
 * Persists data to quizzes.csv, quiz_questions.csv, and quiz_attempts.csv.
 */
public class QuizService {

    private static final String QUIZ_FILE     = "data/database/quizzes.csv";
    private static final String QUESTION_FILE = "data/database/quiz_questions.csv";
    private static final String ATTEMPT_FILE  = "data/database/quiz_attempts.csv";

    private static final String[] QUIZ_HEADER     = {"quizId","title","courseId","teacherId","timeLimitMinutes","maxAttempts"};
    private static final String[] QUESTION_HEADER = {"questionId","quizId","questionText","answer"};
    private static final String[] ATTEMPT_HEADER  = {"attemptId","quizId","studentId","score","totalQuestions","attemptDate","completed"};

    private List<Quiz>         quizzes   = new ArrayList<>();
    private List<QuizQuestion> questions = new ArrayList<>();
    private List<QuizAttempt>  attempts  = new ArrayList<>();

    public QuizService() {
        CSVHandler.initFile(QUIZ_FILE,     QUIZ_HEADER);
        CSVHandler.initFile(QUESTION_FILE, QUESTION_HEADER);
        CSVHandler.initFile(ATTEMPT_FILE,  ATTEMPT_HEADER);
        loadQuizzes();
        loadQuestions();
        loadAttempts();
    }

    // ── CSV Load ─────────────────────────────────────────────────────────────

    private void loadQuizzes() {
        quizzes.clear();
        for (String[] row : CSVHandler.readDataRows(QUIZ_FILE)) {
            try {
                quizzes.add(new Quiz(row[0], row[1], row[2],
                    Integer.parseInt(row[3]),
                    Integer.parseInt(row[4]),
                    Integer.parseInt(row[5])));
            } catch (Exception e) {
                System.err.println("[QuizService] Skipping bad quiz row.");
            }
        }
    }

    private void loadQuestions() {
        questions.clear();
        for (String[] row : CSVHandler.readDataRows(QUESTION_FILE)) {
            try {
                questions.add(new QuizQuestion(row[0], row[1], row[2], row[3]));
            } catch (Exception e) {
                System.err.println("[QuizService] Skipping bad question row.");
            }
        }
    }

    private void loadAttempts() {
        attempts.clear();
        for (String[] row : CSVHandler.readDataRows(ATTEMPT_FILE)) {
            try {
                attempts.add(new QuizAttempt(row[0], row[1],
                    Integer.parseInt(row[2]),
                    Integer.parseInt(row[3]),
                    Integer.parseInt(row[4]),
                    row[5],
                    Boolean.parseBoolean(row[6])));
            } catch (Exception e) {
                System.err.println("[QuizService] Skipping bad attempt row.");
            }
        }
    }

    // ── CSV Save ─────────────────────────────────────────────────────────────

    private void saveQuizzes() {
        List<String[]> rows = new ArrayList<>();
        rows.add(QUIZ_HEADER);
        for (Quiz q : quizzes)
            rows.add(new String[]{q.getQuizId(), q.getTitle(), q.getCourseId(),
                String.valueOf(q.getTeacherId()),
                String.valueOf(q.getTimeLimitMinutes()),
                String.valueOf(q.getMaxAttempts())});
        CSVHandler.writeAll(QUIZ_FILE, rows);
    }

    private void saveQuestions() {
        List<String[]> rows = new ArrayList<>();
        rows.add(QUESTION_HEADER);
        for (QuizQuestion qq : questions)
            rows.add(new String[]{qq.getQuestionId(), qq.getQuizId(),
                qq.getQuestionText(), qq.getAnswer()});
        CSVHandler.writeAll(QUESTION_FILE, rows);
    }

    private void saveAttempts() {
        List<String[]> rows = new ArrayList<>();
        rows.add(ATTEMPT_HEADER);
        for (QuizAttempt a : attempts)
            rows.add(new String[]{a.getAttemptId(), a.getQuizId(),
                String.valueOf(a.getStudentId()),
                String.valueOf(a.getScore()),
                String.valueOf(a.getTotalQuestions()),
                a.getAttemptDate(),
                String.valueOf(a.isCompleted())});
        CSVHandler.writeAll(ATTEMPT_FILE, rows);
    }

    // ── Quiz CRUD ─────────────────────────────────────────────────────────────

    /**
     * Creates a new quiz with the given parameters and saves it.
     */
    public Quiz createQuiz(String title, String courseId, int teacherId,
                           int timeLimitMinutes, int maxAttempts) {
        int n = quizzes.size() + 1;
        String qId = String.format("Q%03d", n);
        Quiz q = new Quiz(qId, title, courseId, teacherId, timeLimitMinutes, maxAttempts);
        quizzes.add(q);
        saveQuizzes();
        System.out.println("  [+] Quiz created: " + qId + " - " + title);
        return q;
    }

    /**
     * Adds a question to an existing quiz.
     */
    public void addQuestion(String quizId, String questionText, String answer) {
        int n = questions.size() + 1;
        String qnId = String.format("QN%03d", n);
        questions.add(new QuizQuestion(qnId, quizId, questionText, answer));
        saveQuestions();
    }

    public List<Quiz> getQuizzesByTeacher(int teacherId) {
        List<Quiz> result = new ArrayList<>();
        for (Quiz q : quizzes) if (q.getTeacherId() == teacherId) result.add(q);
        return result;
    }

    public List<Quiz> getQuizzesByCourse(String courseId) {
        List<Quiz> result = new ArrayList<>();
        for (Quiz q : quizzes) if (q.getCourseId().equalsIgnoreCase(courseId)) result.add(q);
        return result;
    }

    /**
     * Returns quizzes available for a student based on their enrolled course IDs.
     */
    public List<Quiz> getQuizzesForStudent(List<String> enrolledCourseIds) {
        List<Quiz> result = new ArrayList<>();
        for (Quiz q : quizzes)
            for (String cid : enrolledCourseIds)
                if (q.getCourseId().equalsIgnoreCase(cid)) { result.add(q); break; }
        return result;
    }

    public Optional<Quiz> findQuizById(String quizId) {
        return quizzes.stream().filter(q -> q.getQuizId().equalsIgnoreCase(quizId)).findFirst();
    }

    public List<QuizQuestion> getQuestionsForQuiz(String quizId) {
        List<QuizQuestion> result = new ArrayList<>();
        for (QuizQuestion qq : questions)
            if (qq.getQuizId().equalsIgnoreCase(quizId)) result.add(qq);
        return result;
    }

    // ── Attempt Logic ─────────────────────────────────────────────────────────

    /**
     * Returns how many times a student has attempted a quiz.
     */
    public int getAttemptCount(String quizId, int studentId) {
        int count = 0;
        for (QuizAttempt a : attempts)
            if (a.getQuizId().equalsIgnoreCase(quizId) && a.getStudentId() == studentId)
                count++;
        return count;
    }

    /**
     * Records a completed quiz attempt with the student's answers.
     * @param quizId     Quiz being attempted
     * @param studentId  Student ID
     * @param answers    Student answers, parallel to questions list for this quiz
     * @return the QuizAttempt with computed score
     */
    public QuizAttempt recordAttempt(String quizId, int studentId, String[] answers) {
        List<QuizQuestion> qList = getQuestionsForQuiz(quizId);
        int score = 0;
        for (int i = 0; i < qList.size() && i < answers.length; i++) {
            if (answers[i].trim().equalsIgnoreCase(qList.get(i).getAnswer().trim()))
                score++;
        }
        int n = attempts.size() + 1;
        String aId = String.format("AT%03d", n);
        String date = java.time.LocalDate.now().toString();
        QuizAttempt attempt = new QuizAttempt(aId, quizId, studentId,
                score, qList.size(), date, true);
        attempts.add(attempt);
        saveAttempts();
        return attempt;
    }

    public List<QuizAttempt> getAttemptsByStudent(int studentId) {
        List<QuizAttempt> result = new ArrayList<>();
        for (QuizAttempt a : attempts) if (a.getStudentId() == studentId) result.add(a);
        return result;
    }
}
