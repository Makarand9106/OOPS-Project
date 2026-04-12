package controllers;

import courses.Submission;
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

    public TeacherController(Teacher teacher, CourseService courseService,
                             AssignmentService assignmentService,
                             NotificationService notificationService) {
        this.teacher = teacher;
        this.courseService = courseService;
        this.assignmentService = assignmentService;
        this.notificationService = notificationService;
    }

    public void handleMenu() {
        boolean running = true;
        while (running) {
            teacher.viewDashboard();
            int choice = InputValidator.readInt("\n  Enter choice: ");
            switch (choice) {
                case 1: createCourse();          break;
                case 2: uploadMaterial();        break;
                case 3: createAssignment();      break;
                case 4: gradeSubmissions();      break;
                case 5: createQuiz();            break;
                case 6: viewMyCourses();         break;
                case 0: running = false;
                        System.out.println("\n  Logged out successfully " + teacher.getName() + "!");
                        break;
                default: System.out.println("  Invalid option.");
            }
        }
    }

    private void createCourse() {
        System.out.println("\n  -- Create New Course --");
        String courseId = InputValidator.readString("  Course ID (e.g. C201): ").toUpperCase();
        String name     = InputValidator.readString("  Course Name: ");
        String dept     = InputValidator.readString("  Department: ");
        int credits     = InputValidator.readInt("  Credits: ");
        courseService.addCourse(courseId, name, teacher.getId(), dept, credits);
    }

    private void uploadMaterial() {
        System.out.println("\n  -- Upload Study Material (Simulation) --");
        String courseId  = InputValidator.readString("  Course ID: ").toUpperCase();
        String title     = InputValidator.readString("  Material Title: ");
        String fileType  = InputValidator.readString("  File Type (PDF/PPT/DOC): ").toUpperCase();
        System.out.println("  [SIM] Material '" + title + "' (" + fileType + ") uploaded to course " + courseId);
        System.out.println("  [SIM] Notifying enrolled students...");
        notificationService.sendAsync("All Students in " + courseId,
                "New material '" + title + "' has been uploaded by " + teacher.getName());
    }

    private void createAssignment() {
        System.out.println("\n  -- Create Assignment --");
        String courseId  = InputValidator.readString("  Course ID: ").toUpperCase();
        String title     = InputValidator.readString("  Assignment Title: ");
        String dueDate   = InputValidator.readString("  Due Date (YYYY-MM-DD): ");
        int maxMarks     = InputValidator.readInt("  Max Marks: ");
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

        // Find max marks for this assignment
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

    private void createQuiz() {
        System.out.println("\n  -- Create Quiz (In-Memory) --");
        String courseId = InputValidator.readString("  Course ID: ").toUpperCase();
        String quizTitle = InputValidator.readString("  Quiz Title: ");
        int numQ = InputValidator.readInt("  Number of questions: ");
        List<String[]> quiz = new ArrayList<>();
        for (int i = 1; i <= numQ; i++) {
            String q    = InputValidator.readString("  Q" + i + ": ");
            String ans  = InputValidator.readString("  Answer: ");
            quiz.add(new String[]{q, ans});
        }
        System.out.println("\n  [+] Quiz '" + quizTitle + "' created with " + numQ + " question(s) for course " + courseId);
        System.out.println("  [SIM] Quiz stored in-memory (not persisted in this version).");
        System.out.println("  Questions:");
        for (int i = 0; i < quiz.size(); i++) {
            System.out.println("    Q" + (i+1) + ": " + quiz.get(i)[0]);
        }
    }

    private void viewMyCourses() {
        System.out.println("\n  -- My Courses --");
        List<courses.Course> myCourses = courseService.getCoursesByTeacher(teacher.getId());
        if (myCourses.isEmpty()) { System.out.println("  No courses assigned yet."); return; }
        for (courses.Course c : myCourses) System.out.println("  " + c);
    }
}
