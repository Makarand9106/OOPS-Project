package services;

import exams.ExamForm;
import exams.Result;
import utils.CSVHandler;

import java.util.*;

/**
 * Service for exam forms and results.
 * Demonstrates Generics: List<ExamForm>, List<Result>.
 */
public class ExamService {

    private static final String FORM_FILE   = "data/database/exam_forms.csv";
    private static final String RESULT_FILE = "data/database/results.csv";

    private static final String[] FORM_HEADER   = {"formId","studentId","courseId","examType","status","appliedDate"};
    private static final String[] RESULT_HEADER = {"resultId","studentId","courseId","examType","marksObtained","maxMarks","grade","published"};

    private List<ExamForm> forms   = new ArrayList<>();
    private List<Result>   results = new ArrayList<>();

    public ExamService() {
        CSVHandler.initFile(FORM_FILE, FORM_HEADER);
        CSVHandler.initFile(RESULT_FILE, RESULT_HEADER);
        loadForms();
        loadResults();
    }

    // ── Load ──────────────────────────────────────────────────────────────────

    private void loadForms() {
        forms.clear();
        for (String[] row : CSVHandler.readDataRows(FORM_FILE)) {
            try {
                forms.add(new ExamForm(row[0], Integer.parseInt(row[1]),
                    row[2], row[3], row[4], row[5]));
            } catch (Exception e) {
                System.err.println("[ExamService] Skipping bad form row: " + e.getMessage());
            }
        }
    }

    private void loadResults() {
        results.clear();
        for (String[] row : CSVHandler.readDataRows(RESULT_FILE)) {
            try {
                results.add(new Result(row[0], Integer.parseInt(row[1]), row[2], row[3],
                    Integer.parseInt(row[4]), Integer.parseInt(row[5]), row[6],
                    Boolean.parseBoolean(row[7])));
            } catch (Exception e) {
                System.err.println("[ExamService] Skipping bad result row: " + e.getMessage());
            }
        }
    }

    // ── Save ──────────────────────────────────────────────────────────────────

    private void saveForms() {
        List<String[]> rows = new ArrayList<>();
        rows.add(FORM_HEADER);
        for (ExamForm f : forms)
            rows.add(new String[]{f.getFormId(), String.valueOf(f.getStudentId()),
                f.getCourseId(), f.getExamType(), f.getStatus(), f.getAppliedDate()});
        CSVHandler.writeAll(FORM_FILE, rows);
    }

    private void saveResults() {
        List<String[]> rows = new ArrayList<>();
        rows.add(RESULT_HEADER);
        for (Result r : results)
            rows.add(new String[]{r.getResultId(), String.valueOf(r.getStudentId()),
                r.getCourseId(), r.getExamType(), String.valueOf(r.getMarksObtained()),
                String.valueOf(r.getMaxMarks()), r.getGrade(), String.valueOf(r.isPublished())});
        CSVHandler.writeAll(RESULT_FILE, rows);
    }

    // ── Exam Forms ────────────────────────────────────────────────────────────

    public boolean fillExamForm(int studentId, String courseId, String examType) {
        for (ExamForm f : forms) {
            if (f.getStudentId() == studentId && f.getCourseId().equals(courseId)
                    && f.getExamType().equals(examType)) {
                System.out.println("  [!] Exam form already submitted for " + courseId);
                return false;
            }
        }
        int n = forms.size() + 1;
        String fId = String.format("F%03d", n);
        String date = java.time.LocalDate.now().toString();
        forms.add(new ExamForm(fId, studentId, courseId, examType, "PENDING", date));
        saveForms();
        System.out.println("  [+] Exam form submitted! Form ID: " + fId);
        return true;
    }

    public List<ExamForm> getPendingForms() {
        List<ExamForm> result = new ArrayList<>();
        for (ExamForm f : forms)
            if ("PENDING".equals(f.getStatus())) result.add(f);
        return result;
    }

    public boolean approveForm(String formId) {
        for (ExamForm f : forms) {
            if (f.getFormId().equals(formId)) {
                f.setStatus("APPROVED");
                saveForms();
                System.out.println("  [+] Form " + formId + " approved.");
                return true;
            }
        }
        System.out.println("  [!] Form not found: " + formId);
        return false;
    }

    public List<ExamForm> getFormsByStudent(int studentId) {
        List<ExamForm> result = new ArrayList<>();
        for (ExamForm f : forms)
            if (f.getStudentId() == studentId) result.add(f);
        return result;
    }

    // ── Results ───────────────────────────────────────────────────────────────

    public boolean uploadResult(int studentId, String courseId, String examType,
                                int marksObtained, int maxMarks) {
        for (Result r : results) {
            if (r.getStudentId() == studentId && r.getCourseId().equals(courseId)
                    && r.getExamType().equals(examType)) {
                System.out.println("  [!] Result already exists for student "
                        + studentId + " in " + courseId);
                return false;
            }
        }
        int n = results.size() + 1;
        String rId = String.format("R%03d", n);
        String grade = Result.calculateGrade(marksObtained, maxMarks);
        results.add(new Result(rId, studentId, courseId, examType,
                marksObtained, maxMarks, grade, false));
        saveResults();
        System.out.println("  [+] Result uploaded: " + rId + " | Grade: " + grade);
        return true;
    }

    public boolean publishResult(String resultId) {
        for (Result r : results) {
            if (r.getResultId().equals(resultId)) {
                // Prevent duplicate publication
                if (r.isPublished()) {
                    System.out.println("  [!] Result " + resultId
                            + " is ALREADY published. Cannot publish again.");
                    return false;
                }
                r.setPublished(true);
                saveResults();
                System.out.println("  [+] Result " + resultId + " published.");
                return true;
            }
        }
        System.out.println("  [!] Result not found: " + resultId);
        return false;
    }

    public void publishAllResults() {
        int count = 0;
        for (Result r : results) {
            if (!r.isPublished()) { r.setPublished(true); count++; }
        }
        saveResults();
        System.out.println("  [+] Published " + count + " result(s).");
    }

    public List<Result> getResultsByStudent(int studentId) {
        List<Result> result = new ArrayList<>();
        for (Result r : results)
            if (r.getStudentId() == studentId && r.isPublished()) result.add(r);
        return result;
    }

    public List<Result> getAllResults() {
        return new ArrayList<>(results);
    }

    // ── Hall Ticket (simulation) ──────────────────────────────────────────────

    public void generateHallTicket(int studentId, String studentName) {
        List<ExamForm> approved = new ArrayList<>();
        for (ExamForm f : forms)
            if (f.getStudentId() == studentId && "APPROVED".equals(f.getStatus()))
                approved.add(f);

        if (approved.isEmpty()) {
            System.out.println("  [!] No approved exam forms found for student " + studentId);
            return;
        }
        System.out.println("\n  +----------------------------------------------+");
        System.out.println("  |      COEP HALL TICKET (SIMULATION)           |");
        System.out.println("  +----------------------------------------------+");
        System.out.printf( "  |  Student ID : %-30d|%n", studentId);
        System.out.printf( "  |  Name       : %-30s|%n", studentName);
        System.out.println("  +----------------------------------------------+");
        System.out.println("  |  Approved Courses:                           |");
        for (ExamForm f : approved)
            System.out.printf("  |    * %-40s|%n",
                    f.getCourseId() + " (" + f.getExamType() + ")");
        System.out.println("  +----------------------------------------------+");
    }
}
