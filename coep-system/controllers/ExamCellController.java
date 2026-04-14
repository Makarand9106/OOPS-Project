package controllers;

import exams.ExamForm;
import exams.Result;
import java.util.*;
import services.*;
import users.ExamCellStaff;
import users.User;
import utils.InputValidator;

/**
 * Controller for Exam Cell Staff role.
 */
public class ExamCellController {

    private final ExamCellStaff staff;
    private final ExamService examService;
    private final UserService userService;
    private final CourseService courseService;
    private final NotificationService notificationService;

    public ExamCellController(ExamCellStaff staff, ExamService examService,
                               UserService userService, CourseService courseService,
                               NotificationService notificationService) {
        this.staff = staff;
        this.examService = examService;
        this.userService = userService;
        this.courseService = courseService;
        this.notificationService = notificationService;
    }

    public void handleMenu() {
        boolean running = true;
        while (running) {
            staff.viewDashboard();
            int choice = InputValidator.readInt("\n  Enter choice: ");
            switch (choice) {
                case 1: viewPendingForms(); break;
                case 2: approveForm();      break;
                case 3: uploadMarks();      break;
                case 4: publishResults();   break;
                case 5: viewAllResults();   break;
                case 6: changePassword();   break;
                case 0:
                    running = false;
                    System.out.println("\n  Logged out successfully " + staff.getName() + "!");
                    break;
                default: System.out.println("  Invalid option.");
            }
        }
    }

    private void viewPendingForms() {
        System.out.println("\n  -- Pending Exam Forms --");
        List<ExamForm> pending = examService.getPendingForms();
        if (pending.isEmpty()) { System.out.println("  No pending forms."); return; }
        System.out.printf("  %-8s %-10s %-10s %-14s %-12s%n",
                "Form ID", "Student", "Course", "Exam Type", "Date");
        System.out.println("  " + "-".repeat(57));
        for (ExamForm f : pending)
            System.out.printf("  %-8s %-10d %-10s %-14s %-12s%n",
                    f.getFormId(), f.getStudentId(), f.getCourseId(),
                    f.getExamType(), f.getAppliedDate());
    }

    private void approveForm() {
        viewPendingForms();
        String formId = InputValidator.readString("\n  Enter Form ID to approve: ").toUpperCase();
        List<ExamForm> pending = examService.getPendingForms();
        for (ExamForm f : pending) {
            if (f.getFormId().equals(formId)) {
                examService.approveForm(formId);
                User student = userService.findById(f.getStudentId());
                if (student != null)
                    notificationService.notifyExamFormApproved(student.getName(), f.getCourseId());
                return;
            }
        }
        System.out.println("  [!] Form not found in pending list.");
    }

    /**
     * Upload exam marks with two validations:
     * 1. Student must exist.
     * 2. Student must be enrolled in the specified course — marks rejected otherwise.
     * Students are shown sorted by Branch (A→Z) then Student ID before input.
     */
    private void uploadMarks() {
        System.out.println("\n  -- Upload Exam Marks --");

        // Display all students sorted by Branch then Student ID
        List<User> students = userService.getUsersByRole("STUDENT");
        students.sort(Comparator.comparing(User::getDepartment)
                                .thenComparingInt(User::getId));

        System.out.println("\n  Student Reference List (sorted by Branch, then ID):");
        System.out.printf("  %-5s %-25s %-30s%n", "ID", "Name", "Branch");
        System.out.println("  " + "─".repeat(63));

        String lastDept = "";
        for (User u : students) {
            if (!u.getDepartment().equals(lastDept)) {
                lastDept = u.getDepartment();
                System.out.println("  -- " + lastDept + " --");
            }
            System.out.printf("  %-5d %-25s %-30s%n",
                    u.getId(), u.getName(), u.getDepartment());
        }
        System.out.println();

        int studentId = InputValidator.readInt("  Student ID: ");

        // Validate student exists
        User student = userService.findById(studentId);
        if (student == null || !student.getRole().equalsIgnoreCase("STUDENT")) {
            System.out.println("  [!] Student with ID " + studentId + " not found.");
            return;
        }
        System.out.println("  Student : " + student.getName()
                + " | Branch: " + student.getDepartment());

        String courseId = InputValidator.readString("  Course ID: ").toUpperCase();

        // ── ENROLLMENT CHECK ─────────────────────────────────────────────────
        List<String[]> enrollments = courseService.getEnrollmentsByStudent(studentId);
        boolean enrolled = enrollments.stream()
                .anyMatch(e -> e[2].equalsIgnoreCase(courseId));
        if (!enrolled) {
            System.out.println("  [!] REJECTED: Student '" + student.getName()
                    + "' (ID: " + studentId + ") is NOT enrolled in course " + courseId + ".");
            System.out.println("      Marks upload blocked. Enroll the student first.");
            return;
        }
        // ─────────────────────────────────────────────────────────────────────

        String examType = InputValidator.readString("  Exam Type (SEMESTER/BACKLOG): ").toUpperCase();
        int maxMarks    = InputValidator.readInt("  Max Marks: ");
        int marks       = InputValidator.readInt("  Marks Obtained: ");
        if (marks < 0 || marks > maxMarks) {
            System.out.println("  [!] Invalid marks. Must be between 0 and " + maxMarks);
            return;
        }
        examService.uploadResult(studentId, courseId, examType, marks, maxMarks);
    }

    private void publishResults() {
        System.out.println("\n  -- Publish Results --");
        System.out.println("  1. Publish a specific result");
        System.out.println("  2. Publish ALL unpublished results");
        int choice = InputValidator.readInt("  Choice: ");
        if (choice == 1) {
            viewAllResults();
            String rId = InputValidator.readString("  Enter Result ID to publish: ").toUpperCase();
            boolean ok = examService.publishResult(rId);
            if (ok) {
                List<Result> all = examService.getAllResults();
                for (Result r : all) {
                    if (r.getResultId().equals(rId)) {
                        User student = userService.findById(r.getStudentId());
                        if (student != null)
                            notificationService.notifyResultPublished(
                                    student.getName(), r.getCourseId(), r.getGrade());
                        break;
                    }
                }
            }
        } else if (choice == 2) {
            examService.publishAllResults();
            notificationService.sendInThread("All Students",
                    "Exam results have been published. Please check your portal.");
        }
    }

    private void viewAllResults() {
        System.out.println("\n  -- All Results --");
        List<Result> all = examService.getAllResults();
        if (all.isEmpty()) { System.out.println("  No results found."); return; }
        System.out.printf("  %-8s %-10s %-10s %-12s %-12s %-6s %-9s%n",
                "ID", "Student", "Course", "Exam Type", "Marks", "Grade", "Published");
        System.out.println("  " + "-".repeat(72));
        for (Result r : all)
            System.out.printf("  %-8s %-10d %-10s %-12s %d/%-9d %-6s %-9s%n",
                    r.getResultId(), r.getStudentId(), r.getCourseId(),
                    r.getExamType(), r.getMarksObtained(), r.getMaxMarks(),
                    r.getGrade(), r.isPublished() ? "Yes" : "No");
    }

    /**
     * Changes the Exam Cell staff member's own password. Forces logout on success.
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
        boolean ok = userService.changePassword(staff.getId(), current, newPass);
        if (ok) {
            System.out.println("  Session will now end for security.");
            throw new RuntimeException("PASSWORD_CHANGED");
        }
    }
}
