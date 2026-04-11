package services;

import courses.Assignment;
import courses.Submission;
import utils.CSVHandler;

import java.util.*;

/**
 * Service for assignments and submissions. Generics: List<Assignment>, List<Submission>.
 */
public class AssignmentService {

    private static final String ASSIGN_FILE = "data/database/assignments.csv";
    private static final String SUBMIT_FILE = "data/database/submissions.csv";

    private static final String[] ASSIGN_HEADER = {"assignmentId","title","courseId","teacherId","dueDate","maxMarks"};
    private static final String[] SUBMIT_HEADER = {"submissionId","assignmentId","studentId","submissionDate","marksObtained","graded"};

    private List<Assignment> assignments = new ArrayList<>();
    private List<Submission> submissions = new ArrayList<>();

    public AssignmentService() {
        CSVHandler.initFile(ASSIGN_FILE, ASSIGN_HEADER);
        CSVHandler.initFile(SUBMIT_FILE, SUBMIT_HEADER);
        loadAssignments();
        loadSubmissions();
    }

    // ── CSV Load ─────────────────────────────────────────────────────────────

    private void loadAssignments() {
        assignments.clear();
        for (String[] row : CSVHandler.readDataRows(ASSIGN_FILE)) {
            try {
                assignments.add(new Assignment(row[0], row[1], row[2],
                    Integer.parseInt(row[3]), row[4], Integer.parseInt(row[5])));
            } catch (Exception e) {
                System.err.println("[AssignmentService] Skipping bad assignment row.");
            }
        }
    }

    private void loadSubmissions() {
        submissions.clear();
        for (String[] row : CSVHandler.readDataRows(SUBMIT_FILE)) {
            try {
                submissions.add(new Submission(row[0], row[1],
                    Integer.parseInt(row[2]), row[3],
                    row[4].isEmpty() ? 0 : Integer.parseInt(row[4]),
                    Boolean.parseBoolean(row[5])));
            } catch (Exception e) {
                System.err.println("[AssignmentService] Skipping bad submission row.");
            }
        }
    }

    // ── CSV Save ─────────────────────────────────────────────────────────────

    private void saveAssignments() {
        List<String[]> rows = new ArrayList<>();
        rows.add(ASSIGN_HEADER);
        for (Assignment a : assignments) {
            rows.add(new String[]{a.getAssignmentId(), a.getTitle(), a.getCourseId(),
                String.valueOf(a.getTeacherId()), a.getDueDate(), String.valueOf(a.getMaxMarks())});
        }
        CSVHandler.writeAll(ASSIGN_FILE, rows);
    }

    private void saveSubmissions() {
        List<String[]> rows = new ArrayList<>();
        rows.add(SUBMIT_HEADER);
        for (Submission s : submissions) {
            rows.add(new String[]{s.getSubmissionId(), s.getAssignmentId(),
                String.valueOf(s.getStudentId()), s.getSubmissionDate(),
                String.valueOf(s.getMarksObtained()), String.valueOf(s.isGraded())});
        }
        CSVHandler.writeAll(SUBMIT_FILE, rows);
    }

    // ── Assignment CRUD ──────────────────────────────────────────────────────

    public boolean createAssignment(String title, String courseId, int teacherId, String dueDate, int maxMarks) {
        int n = assignments.size() + 1;
        String id = String.format("A%03d", n);
        assignments.add(new Assignment(id, title, courseId, teacherId, dueDate, maxMarks));
        saveAssignments();
        System.out.println("  [+] Assignment created: " + id + " - " + title);
        return true;
    }

    public List<Assignment> getAssignmentsByCourse(String courseId) {
        List<Assignment> result = new ArrayList<>();
        for (Assignment a : assignments) if (a.getCourseId().equals(courseId)) result.add(a);
        return result;
    }

    public List<Assignment> getAssignmentsByTeacher(int teacherId) {
        List<Assignment> result = new ArrayList<>();
        for (Assignment a : assignments) if (a.getTeacherId() == teacherId) result.add(a);
        return result;
    }

    public Optional<Assignment> findAssignmentById(String id) {
        return assignments.stream().filter(a -> a.getAssignmentId().equals(id)).findFirst();
    }

    // ── Submission CRUD ──────────────────────────────────────────────────────

    public boolean submitAssignment(String assignmentId, int studentId) {
        // Prevent duplicate submissions
        for (Submission s : submissions) {
            if (s.getAssignmentId().equals(assignmentId) && s.getStudentId() == studentId) {
                System.out.println("  [!] Already submitted for this assignment.");
                return false;
            }
        }
        if (findAssignmentById(assignmentId).isEmpty()) {
            System.out.println("  [!] Assignment not found: " + assignmentId);
            return false;
        }
        int n = submissions.size() + 1;
        String subId = String.format("S%03d", n);
        String date = java.time.LocalDate.now().toString();
        submissions.add(new Submission(subId, assignmentId, studentId, date, 0, false));
        saveSubmissions();
        System.out.println("  [+] Assignment submitted! Submission ID: " + subId);
        return true;
    }

    public List<Submission> getUngradedByTeacher(int teacherId) {
        List<String> tAssignIds = new ArrayList<>();
        for (Assignment a : assignments) if (a.getTeacherId() == teacherId) tAssignIds.add(a.getAssignmentId());
        List<Submission> result = new ArrayList<>();
        for (Submission s : submissions)
            if (!s.isGraded() && tAssignIds.contains(s.getAssignmentId())) result.add(s);
        return result;
    }

    public boolean gradeSubmission(String submissionId, int marks) {
        for (Submission s : submissions) {
            if (s.getSubmissionId().equals(submissionId)) {
                s.setMarksObtained(marks);
                s.setGraded(true);
                saveSubmissions();
                System.out.println("  [+] Graded submission " + submissionId + " with " + marks + " marks.");
                return true;
            }
        }
        System.out.println("  [!] Submission not found: " + submissionId);
        return false;
    }

    public List<Submission> getSubmissionsByStudent(int studentId) {
        List<Submission> result = new ArrayList<>();
        for (Submission s : submissions) if (s.getStudentId() == studentId) result.add(s);
        return result;
    }

    public void printAssignments(List<Assignment> list) {
        if (list.isEmpty()) { System.out.println("  No assignments found."); return; }
        System.out.printf("  %-8s %-30s %-8s %-12s %-6s%n", "ID", "Title", "Course", "Due Date", "Marks");
        System.out.println("  " + "─".repeat(68));
        for (Assignment a : list)
            System.out.printf("  %-8s %-30s %-8s %-12s %-6d%n",
                a.getAssignmentId(), a.getTitle(), a.getCourseId(), a.getDueDate(), a.getMaxMarks());
    }
}
