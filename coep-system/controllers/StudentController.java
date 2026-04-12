package controllers;

import courses.*;
import exams.Result;
import java.util.List;
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

    public StudentController(Student student, CourseService courseService,
                             AssignmentService assignmentService, ExamService examService,
                             NotificationService notificationService) {
        this.student = student;
        this.courseService = courseService;
        this.assignmentService = assignmentService;
        this.examService = examService;
        this.notificationService = notificationService;
    }

    public void handleMenu() {
        boolean running = true;
        while (running) {
            student.viewDashboard();
            int choice = InputValidator.readInt("\n  Enter choice: ");
            switch (choice) {
                case 1: enrollCourse();     break;
                case 2: submitAssignment(); break;
                case 3: fillExamForm();     break;
                case 4: viewResults();      break;
                case 5: viewEnrollments();  break;
                case 0:
                    running = false;
                    System.out.println("\n  Logged out successfully " + student.getName() + "!");
                    break;
                default: System.out.println("  Invalid option.");
            }
        }
    }

    private void enrollCourse() {
        System.out.println("\n  -- Enroll in Course --");
        courseService.printAllCourses();
        String courseId = InputValidator.readString("\n  Enter Course ID to enroll: ").toUpperCase();
        boolean ok = courseService.enrollStudent(student.getId(), courseId);
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
}
