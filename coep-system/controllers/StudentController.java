package controllers;

import courses.*;
import exams.Result;
import java.util.*;
import services.*;
import users.Student;
import utils.InputValidator;

/**
 * Controller for Student role. Handles all student menu interactions.
 */

public class StudentController {

    private final Student student;
    private final CourseService courseService;
    private final AssignmentService assignmentService;
    private final ExamService examService;
    private final NotificationService notificationService;
    private final QuizService quizService;
    private final UserService userService;

    public StudentController(Student student, CourseService courseService,
                             AssignmentService assignmentService, ExamService examService,
                             NotificationService notificationService,
                             QuizService quizService, UserService userService) {
        this.student = student;
        this.courseService = courseService;
        this.assignmentService = assignmentService;
        this.examService = examService;
        this.notificationService = notificationService;
        this.quizService = quizService;
        this.userService = userService;
    }

    public void handleMenu() {
        boolean running = true;
        while (running) {
            student.viewDashboard();
            int choice = InputValidator.readInt("\n  Enter choice: ");
            switch (choice) {
                case 1: enrollCourse();           break;
                case 2: submitAssignment();       break;
                case 3: fillExamForm();           break;
                case 4: viewResults();            break;
                case 5: viewEnrollments();        break;
                case 6: viewSubmissionHistory();  break;
                case 7: quizMenu();               break;
                case 8: changePassword();         break;
                case 0:
                    running = false;
                    System.out.println("\n  Logged out successfully " + student.getName() + "!");
                    break;
                default: System.out.println("  Invalid option.");
            }
        }
    }

    /**
     * Enrolls student in a course. Only courses from the student's own department are shown.
     * Branch enforcement is applied at the service level as well.
     */
    private void enrollCourse() {
        System.out.println("\n  -- Enroll in Course --");
        System.out.println("  Your Department: " + student.getDepartment());
        System.out.println("  (Only courses from your department are available)\n");

        List<Course> deptCourses = courseService.getCoursesByDepartment(student.getDepartment());
        if (deptCourses.isEmpty()) {
            System.out.println("  No courses available for your department.");
            return;
        }

        System.out.printf("  %-8s %-35s %-8s%n", "ID", "Course Name", "Credits");
        System.out.println("  " + "─".repeat(55));
        for (Course c : deptCourses) {
            System.out.printf("  %-8s %-35s %-8d%n",
                    c.getCourseId(), c.getCourseName(), c.getCredits());
        }

        String courseId = InputValidator.readString("\n  Enter Course ID to enroll: ").toUpperCase();
        boolean ok = courseService.enrollStudent(student.getId(), courseId, student.getDepartment());
        if (ok) notificationService.notifyEnrollment(student.getName(), courseId);
    }

    private void submitAssignment() {
        System.out.println("\n  -- Submit Assignment --");
        List<String[]> enrollments = courseService.getEnrollmentsByStudent(student.getId());
        if (enrollments.isEmpty()) {
            System.out.println("  You are not enrolled in any course.");
            return;
        }
        System.out.println("  Your enrolled courses:");
        for (String[] e : enrollments) System.out.println("    - " + e[2]);

        String courseId = InputValidator.readString("  Enter Course ID: ").toUpperCase();
        List<Assignment> assignments = assignmentService.getAssignmentsByCourse(courseId);
        if (assignments.isEmpty()) {
            System.out.println("  No assignments available for this course.");
            return;
        }
        System.out.println("\n  Available Assignments:");
        assignmentService.printAssignments(assignments);
        String assignId = InputValidator.readString("  Enter Assignment ID to submit: ").toUpperCase();
        assignmentService.submitAssignment(assignId, student.getId());
    }

    private void fillExamForm() {
        System.out.println("\n  -- Fill Exam Form --");
        List<String[]> enrollments = courseService.getEnrollmentsByStudent(student.getId());
        if (enrollments.isEmpty()) {
            System.out.println("  You are not enrolled in any course.");
            return;
        }
        System.out.println("  Your enrolled courses:");
        for (String[] e : enrollments) System.out.println("    - " + e[2]);
        String courseId = InputValidator.readString("  Enter Course ID: ").toUpperCase();
        String examType = InputValidator.readString("  Exam Type (SEMESTER/BACKLOG): ").toUpperCase();
        examService.fillExamForm(student.getId(), courseId, examType);
    }

    private void viewResults() {
        System.out.println("\n  -- My Results --");
        List<Result> results = examService.getResultsByStudent(student.getId());
        if (results.isEmpty()) {
            System.out.println("  No published results found.");
            return;
        }
        System.out.printf("  %-8s %-10s %-12s %-14s %-6s%n",
                "ID", "Course", "Exam Type", "Marks", "Grade");
        System.out.println("  " + "-".repeat(54));
        for (Result r : results) {
            System.out.printf("  %-8s %-10s %-12s %d/%-11d %-6s%n",
                    r.getResultId(), r.getCourseId(), r.getExamType(),
                    r.getMarksObtained(), r.getMaxMarks(), r.getGrade());
        }
    }

    private void viewEnrollments() {
        System.out.println("\n  -- My Enrollments --");
        List<String[]> enr = courseService.getEnrollmentsByStudent(student.getId());
        if (enr.isEmpty()) { System.out.println("  No enrollments found."); return; }
        System.out.printf("  %-10s %-10s %-12s%n", "Enroll ID", "Course ID", "Date");
        System.out.println("  " + "-".repeat(35));
        for (String[] e : enr)
            System.out.printf("  %-10s %-10s %-12s%n", e[0], e[2], e[3]);
    }

    /**
     * Shows all assignments this student has submitted, with marks if graded.
     */
    private void viewSubmissionHistory() {
        System.out.println("\n  -- My Submission History --");
        List<Submission> subs = assignmentService.getSubmissionsByStudent(student.getId());
        if (subs.isEmpty()) {
            System.out.println("  No submissions found.");
            return;
        }
        System.out.printf("  %-8s %-8s %-30s %-10s %-8s %-8s %-7s%n",
                "Sub ID", "Assgn", "Title", "Course", "Due Date", "Marks", "Graded");
        System.out.println("  " + "─".repeat(82));
        for (Submission s : subs) {
            Optional<Assignment> aOpt = assignmentService.findAssignmentById(s.getAssignmentId());
            String title   = aOpt.map(Assignment::getTitle).orElse("Unknown");
            String courseId = aOpt.map(Assignment::getCourseId).orElse("?");
            String dueDate  = aOpt.map(Assignment::getDueDate).orElse("?");
            String marks   = s.isGraded()
                    ? (s.getMarksObtained() + "/" + aOpt.map(Assignment::getMaxMarks).orElse(0))
                    : "Pending";
            System.out.printf("  %-8s %-8s %-30s %-10s %-8s %-8s %-7s%n",
                    s.getSubmissionId(), s.getAssignmentId(),
                    title.length() > 29 ? title.substring(0, 28) + "…" : title,
                    courseId, dueDate, marks,
                    s.isGraded() ? "Yes" : "No");
        }
    }

    // ── Quiz Features ─────────────────────────────────────────────────────────

    /**
     * Sub-menu for all quiz-related actions.
     */
    private void quizMenu() {
        System.out.println("\n  -- Quiz Center --");
        System.out.println("  1. View Available Quizzes");
        System.out.println("  2. Attempt a Quiz");
        System.out.println("  3. View My Quiz Results");
        int choice = InputValidator.readInt("  Choice: ");
        switch (choice) {
            case 1: viewQuizzes();    break;
            case 2: attemptQuiz();   break;
            case 3: viewQuizResults(); break;
            default: System.out.println("  Invalid option.");
        }
    }

    /**
     * Shows quizzes available for the student's enrolled courses.
     */
    private void viewQuizzes() {
        System.out.println("\n  -- Available Quizzes --");
        List<String[]> enrollments = courseService.getEnrollmentsByStudent(student.getId());
        if (enrollments.isEmpty()) {
            System.out.println("  You are not enrolled in any course.");
            return;
        }

        List<String> enrolledCourseIds = new ArrayList<>();
        for (String[] e : enrollments) enrolledCourseIds.add(e[2]);

        List<Quiz> available = quizService.getQuizzesForStudent(enrolledCourseIds);
        if (available.isEmpty()) {
            System.out.println("  No quizzes available for your courses yet.");
            return;
        }

        System.out.printf("  %-8s %-30s %-10s %-9s %-12s%n",
                "Quiz ID", "Title", "Course", "Time(min)", "Max Attempts");
        System.out.println("  " + "─".repeat(72));
        for (Quiz q : available) {
            int attempts = quizService.getAttemptCount(q.getQuizId(), student.getId());
            System.out.printf("  %-8s %-30s %-10s %-9d %-4d  (used: %d)%n",
                    q.getQuizId(), q.getTitle(), q.getCourseId(),
                    q.getTimeLimitMinutes(), q.getMaxAttempts(), attempts);
        }
    }

    /**
     * Allows student to attempt a quiz with constraint checks (max attempts).
     * Questions are True/False type.
     */
    private void attemptQuiz() {
        System.out.println("\n  -- Attempt Quiz --");
        viewQuizzes();

        String quizId = InputValidator.readString("\n  Enter Quiz ID to attempt: ").toUpperCase();

        Optional<Quiz> quizOpt = quizService.findQuizById(quizId);
        if (quizOpt.isEmpty()) {
            System.out.println("  [!] Quiz not found.");
            return;
        }
        Quiz quiz = quizOpt.get();

        // Validate student is enrolled in the quiz's course
        List<String[]> enrollments = courseService.getEnrollmentsByStudent(student.getId());
        boolean enrolled = enrollments.stream()
                .anyMatch(e -> e[2].equalsIgnoreCase(quiz.getCourseId()));
        if (!enrolled) {
            System.out.println("  [!] You are not enrolled in the course for this quiz.");
            return;
        }

        // Check attempt limit
        int attempts = quizService.getAttemptCount(quizId, student.getId());
        if (attempts >= quiz.getMaxAttempts()) {
            System.out.println("  [!] You have reached the maximum attempt limit ("
                    + quiz.getMaxAttempts() + ") for this quiz.");
            return;
        }

        List<QuizQuestion> questions = quizService.getQuestionsForQuiz(quizId);
        if (questions.isEmpty()) {
            System.out.println("  [!] This quiz has no questions yet.");
            return;
        }

        System.out.println("\n  ════════════════════════════════════════════");
        System.out.println("  Quiz: " + quiz.getTitle());
        System.out.println("  Course: " + quiz.getCourseId()
                + "  |  Time limit: " + quiz.getTimeLimitMinutes() + " min");
        System.out.println("  Attempt " + (attempts + 1) + " of " + quiz.getMaxAttempts());
        System.out.println("  All questions are True/False type.");
        System.out.println("  ════════════════════════════════════════════");

        String[] studentAnswers = new String[questions.size()];
        for (int i = 0; i < questions.size(); i++) {
            System.out.println("\n  Q" + (i + 1) + ": " + questions.get(i).getQuestionText());
            String ans;
            while (true) {
                ans = InputValidator.readString("  Your Answer (True/False): ").trim();
                if (ans.equalsIgnoreCase("True") || ans.equalsIgnoreCase("False")) {
                    break;
                }
                System.out.println("  [!] Please enter 'True' or 'False' only.");
            }
            studentAnswers[i] = ans;
        }

        QuizAttempt result = quizService.recordAttempt(quizId, student.getId(), studentAnswers);

        System.out.println("\n  ════════════ QUIZ RESULT ════════════");
        System.out.printf("  Score: %d / %d%n", result.getScore(), result.getTotalQuestions());
        int pct = result.getTotalQuestions() > 0
                ? (result.getScore() * 100 / result.getTotalQuestions()) : 0;
        System.out.println("  Percentage: " + pct + "%");
        System.out.println("  Attempt ID: " + result.getAttemptId());
        System.out.println("  ═════════════════════════════════════");
    }

    /**
     * Shows all past quiz attempt results for this student.
     */
    private void viewQuizResults() {
        System.out.println("\n  -- My Quiz Results --");
        List<QuizAttempt> myAttempts = quizService.getAttemptsByStudent(student.getId());
        if (myAttempts.isEmpty()) {
            System.out.println("  No quiz attempts yet.");
            return;
        }
        System.out.printf("  %-8s %-8s %-8s %-20s %-12s%n",
                "Attempt", "Quiz", "Score", "Title", "Date");
        System.out.println("  " + "─".repeat(60));
        for (QuizAttempt a : myAttempts) {
            Optional<Quiz> q = quizService.findQuizById(a.getQuizId());
            String title = q.map(Quiz::getTitle).orElse("Unknown");
            System.out.printf("  %-8s %-8s %d/%-6d %-20s %-12s%n",
                    a.getAttemptId(), a.getQuizId(),
                    a.getScore(), a.getTotalQuestions(),
                    title, a.getAttemptDate());
        }
    }

    /**
     * Changes the student's own password. Forces logout on success.
     */
    private void changePassword() {
        System.out.println("\n  -- Change Password --");
        String current = InputValidator.readPassword("  Current Password: ");
        String newPass = InputValidator.readPassword("  New Password: ");
        String confirm = InputValidator.readPassword("  Confirm New Password: ");
        if (!newPass.equals(confirm)) {
            System.out.println("  [!] Passwords do not match. Cancelled.");
            return;
        }
        boolean ok = userService.changePassword(student.getId(), current, newPass);
        if (ok) {
            System.out.println("  Session will now end for security.");
            throw new RuntimeException("PASSWORD_CHANGED");
        }
    }
}
