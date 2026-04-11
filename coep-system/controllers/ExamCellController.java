package controllers;

import exams.ExamForm;
import exams.Result;
import services.*;
import users.ExamCellStaff;
import utils.InputValidator;

import java.util.List;

/**
 * Controller for Exam Cell Staff role.
 */
public class ExamCellController {

    private final ExamCellStaff staff;
    private final ExamService examService;
    private final UserService userService;
    private final NotificationService notificationService;

    public ExamCellController(ExamCellStaff staff, ExamService examService,
                               UserService userService,
                               NotificationService notificationService) {
        this.staff = staff;
        this.examService = examService;
        this.userService = userService;
        this.notificationService = notificationService;
    }

    public void handleMenu() {
        boolean running = true;
        while (running) {
            staff.viewDashboard();
            int choice = InputValidator.readInt("\n  Enter choice: ");
            switch (choice) {
                case 1: viewPendingForms();   break;
                case 2: approveForm();        break;
                case 3: generateHallTicket(); break;
                case 4: uploadMarks();        break;
                case 5: publishResults();     break;
                case 6: viewAllResults();     break;
                case 0:
                    running = false;
                    System.out.println("\n  Logged out. Goodbye, " + staff.getName() + "!");
                    break;
                default: System.out.println("  [!] Invalid option.");
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
                users.User student = userService.findById(f.getStudentId());
                if (student != null)
                    notificationService.notifyExamFormApproved(student.getName(), f.getCourseId());
                return;
            }
        }
        System.out.println("  [!] Form not found in pending list.");
    }

    private void generateHallTicket() {
        System.out.println("\n  -- Generate Hall Ticket --");
        int studentId = InputValidator.readInt("  Enter Student ID: ");
        users.User student = userService.findById(studentId);
        if (student == null) { System.out.println("  [!] Student not found."); return; }
        examService.generateHallTicket(studentId, student.getName());
    }

    private void uploadMarks() {
        System.out.println("\n  -- Upload Exam Marks --");
        int studentId    = InputValidator.readInt("  Student ID: ");
        String courseId  = InputValidator.readString("  Course ID: ").toUpperCase();
        String examType  = InputValidator.readString("  Exam Type (SEMESTER/BACKLOG): ").toUpperCase();
        int maxMarks     = InputValidator.readInt("  Max Marks: ");
        int marks        = InputValidator.readInt("  Marks Obtained: ");
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
                        users.User student = userService.findById(r.getStudentId());
                        if (student != null)
                            notificationService.notifyResultPublished(
                                    student.getName(), r.getCourseId(), r.getGrade());
                        break;
                    }
                }
            }
        } else if (choice == 2) {
            examService.publishAllResults();
            notificationService.sendAsync("All Students",
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
}
