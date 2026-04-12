package controllers;

import courses.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import services.*;
import users.Teacher;
import utils.InputValidator;

/**
 * Controller for Teacher role. Handles all teacher menu interactions.
 */
public class TeacherController {

    private final Teacher teacher;
    private final CourseService courseService;
    private final AssignmentService assignmentService;
    private final NotificationService notificationService;
    private final QuizService quizService;
    private final UserService userService;

    public TeacherController(Teacher teacher, CourseService courseService,
                             AssignmentService assignmentService,
                             NotificationService notificationService,
                             QuizService quizService,
                             UserService userService) {
        this.teacher = teacher;
        this.courseService = courseService;
        this.assignmentService = assignmentService;
        this.notificationService = notificationService;
        this.quizService = quizService;
        this.userService = userService;
    }

    public void handleMenu() {
        boolean running = true;
        while (running) {
            teacher.viewDashboard();
            int choice = InputValidator.readInt("\n  Enter choice: ");
            switch (choice) {
                case 1: createCourse();      break;
                case 2: uploadMaterial();    break;
                case 3: createAssignment();  break;
                case 4: gradeSubmissions();  break;
                case 5: createQuiz();        break;
                case 6: viewMyCourses();     break;
                case 7: changePassword();    break;
                case 0: running = false;
                        System.out.println("\n  Logged out successfully " + teacher.getName() + "!");
                        break;
                default: System.out.println("  Invalid option.");
            }
        }
    }

    /**
     * Creates a course strictly within the teacher's own department.
     * Department is auto-assigned — no manual input allowed.
     * Duplicate course ID is blocked by CourseService.
     */
    private void createCourse() {
        System.out.println("\n  -- Create New Course --");
        String courseId = InputValidator.readString("  Course ID (e.g. C201): ").toUpperCase();
        String name     = InputValidator.readString("  Course Name: ");
        int credits     = InputValidator.readInt("  Credits (1-6): ");

        // Enforce: department comes ONLY from the teacher's profile
        String dept = teacher.getDepartment();
        System.out.println("  Department (auto-set from your profile): " + dept);

        boolean ok = courseService.addCourse(courseId, name, teacher.getId(), dept, credits);
        if (ok) {
            System.out.println("  [✓] Course '" + name + "' created under " + dept);
        }
    }

    /**
     * Simulates material upload. The SIM tag is removed from the notification text.
     */
    private void uploadMaterial() {
        System.out.println("\n  -- Upload Study Material --");
        String courseId = InputValidator.readString("  Course ID: ").toUpperCase();
        String title    = InputValidator.readString("  Material Title: ");
        String fileType = InputValidator.readString("  File Type (PDF/PPT/DOC): ").toUpperCase();
        System.out.println("  Material '" + title + "' (" + fileType + ") uploaded to course " + courseId);
        System.out.println("  Notifying enrolled students...");
        notificationService.sendAsync("All Students in " + courseId,
                "New material '" + title + "' has been uploaded by " + teacher.getName());
    }

    /**
     * Creates an assignment for one of this teacher's courses only.
     * Due date: mandatory, must be strictly in the future, month validated 1-12.
     */
    private void createAssignment() {
        System.out.println("\n  -- Create Assignment --");

        List<Course> myCourses = courseService.getCoursesByTeacher(teacher.getId());
        if (myCourses.isEmpty()) {
            System.out.println("  You have no courses yet. Create a course first.");
            return;
        }
        System.out.println("  Your courses:");
        for (Course c : myCourses)
            System.out.println("    [" + c.getCourseId() + "] " + c.getCourseName());

        String courseId = InputValidator.readString("  Course ID: ").toUpperCase();
        boolean owns = myCourses.stream().anyMatch(c -> c.getCourseId().equals(courseId));
        if (!owns) {
            System.out.println("  [!] Course " + courseId + " is not assigned to you.");
            return;
        }

        String title = InputValidator.readString("  Assignment Title: ");

        // Due date: non-empty, valid format, valid month (1-12), must be future date
        String dueDate = "";
        while (true) {
            dueDate = InputValidator.readString("  Due Date (YYYY-MM-DD): ").trim();
            if (dueDate.isEmpty()) {
                System.out.println("  [!] Due date cannot be empty.");
                continue;
            }
            // Validate format and ranges (month 1-12, day 1-31) via LocalDate parsing
            try {
                LocalDate due = LocalDate.parse(dueDate);
                // Extra explicit month check
                int month = due.getMonthValue();
                if (month < 1 || month > 12) {
                    System.out.println("  [!] Invalid month (" + month
                            + "). Month must be between 1 and 12.");
                    continue;
                }
                if (!due.isAfter(LocalDate.now())) {
                    System.out.println("  [!] Due date must be after today ("
                            + LocalDate.now() + ").");
                    continue;
                }
                break; // valid
            } catch (DateTimeParseException e) {
                System.out.println("  [!] Invalid date. Use YYYY-MM-DD (e.g. 2026-06-30).");
                System.out.println("      Month must be 01-12, Day must be 01-31.");
            }
        }

        int maxMarks = InputValidator.readInt("  Max Marks: ");
        assignmentService.createAssignment(title, courseId, teacher.getId(), dueDate, maxMarks);
        notificationService.sendAsync("Students in " + courseId,
                "New assignment '" + title + "' posted. Due: " + dueDate);
    }

    private void gradeSubmissions() {
        System.out.println("\n  -- Grade Submissions --");
        List<Submission> ungraded = assignmentService.getUngradedByTeacher(teacher.getId());
        if (ungraded.isEmpty()) {
            System.out.println("  No ungraded submissions found.");
            return;
        }
        System.out.printf("  %-10s %-10s %-10s %-12s%n", "Sub ID", "Assign ID", "Student", "Submitted");
        System.out.println("  " + "-".repeat(45));
        for (Submission s : ungraded) {
            System.out.printf("  %-10s %-10s %-10d %-12s%n",
                    s.getSubmissionId(), s.getAssignmentId(),
                    s.getStudentId(), s.getSubmissionDate());
        }
        String subId = InputValidator.readString("\n  Enter Submission ID to grade: ").toUpperCase();
        Submission target = null;
        for (Submission s : ungraded) {
            if (s.getSubmissionId().equals(subId)) { target = s; break; }
        }
        if (target == null) { System.out.println("  [!] Submission not found."); return; }

        int maxMarks = assignmentService.findAssignmentById(target.getAssignmentId())
                .map(a -> a.getMaxMarks()).orElse(100);
        int marks = InputValidator.readInt("  Enter marks (0-" + maxMarks + "): ");
        if (marks < 0 || marks > maxMarks) {
            System.out.println("  [!] Invalid marks.");
            return;
        }
        assignmentService.gradeSubmission(subId, marks);
        notificationService.notifyGraded("Student " + target.getStudentId(),
                target.getAssignmentId(), marks);
    }

    /**
     * Creates a quiz for one of this teacher's own courses ONLY.
     * Questions are strictly True/False type.
     */
    private void createQuiz() {
        System.out.println("\n  -- Create Quiz --");

        // Only teacher's own courses may be selected
        List<Course> myCourses = courseService.getCoursesByTeacher(teacher.getId());
        if (myCourses.isEmpty()) {
            System.out.println("  You have no courses assigned. Create a course first.");
            return;
        }

        System.out.println("  Your courses (quiz can only be created for these):");
        for (Course c : myCourses) {
            System.out.println("    [" + c.getCourseId() + "] " + c.getCourseName());
        }

        String courseId = InputValidator.readString("  Select Course ID: ").toUpperCase();

        // Strict ownership check
        boolean owns = myCourses.stream().anyMatch(c -> c.getCourseId().equals(courseId));
        if (!owns) {
            System.out.println("  [!] Access DENIED. Course '" + courseId
                    + "' is not assigned to you. You can only create quizzes for your own courses.");
            return;
        }

        String quizTitle  = InputValidator.readString("  Quiz Title: ");
        int timeLimitMins = InputValidator.readInt("  Time Limit (minutes): ");
        int maxAttempts   = InputValidator.readInt("  Max Attempts per Student: ");
        int numQ          = InputValidator.readInt("  Number of Questions: ");

        Quiz quiz = quizService.createQuiz(quizTitle, courseId, teacher.getId(),
                timeLimitMins, maxAttempts);

        System.out.println("  NOTE: All answers must be True or False (case-insensitive).");
        for (int i = 1; i <= numQ; i++) {
            String q = InputValidator.readString("  Q" + i + " (statement): ");
            // Strictly validate True/False answer
            String ans;
            while (true) {
                ans = InputValidator.readString("  Answer (True/False): ").trim();
                if (ans.equalsIgnoreCase("True") || ans.equalsIgnoreCase("False")) {
                    // Normalize to Title Case
                    ans = ans.substring(0, 1).toUpperCase() + ans.substring(1).toLowerCase();
                    break;
                }
                System.out.println("  [!] Answer must be 'True' or 'False' only.");
            }
            quizService.addQuestion(quiz.getQuizId(), q, ans);
        }

        System.out.println("  [✓] Quiz '" + quizTitle + "' saved with " + numQ
                + " True/False question(s) for course " + courseId);
        System.out.println("  [✓] Time limit: " + timeLimitMins
                + " min | Max attempts: " + maxAttempts);
    }

    private void viewMyCourses() {
        System.out.println("\n  -- My Courses --");
        List<Course> myCourses = courseService.getCoursesByTeacher(teacher.getId());
        if (myCourses.isEmpty()) { System.out.println("  No courses assigned yet."); return; }
        System.out.printf("  %-8s %-35s %-8s%n", "ID", "Course Name", "Credits");
        System.out.println("  " + "─".repeat(55));
        for (Course c : myCourses)
            System.out.printf("  %-8s %-35s %-8d%n",
                    c.getCourseId(), c.getCourseName(), c.getCredits());
    }

    /**
     * Changes teacher's own password. Forces logout on success.
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
        boolean ok = userService.changePassword(teacher.getId(), current, newPass);
        if (ok) {
            System.out.println("  Session will now end for security.");
            throw new RuntimeException("PASSWORD_CHANGED");
        }
    }
}
