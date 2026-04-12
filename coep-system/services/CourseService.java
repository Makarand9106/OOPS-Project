package services;

import courses.Course;
import java.util.*;
import utils.CSVHandler;

/**
 * Service for managing courses. Demonstrates Generics with List<Course>.
 * Persists data to courses.csv.
 */
public class CourseService {

    private static final String FILE   = "data/database/courses.csv";
    private static final String ENROLL = "data/database/enrollments.csv";
    private static final String[] HEADER        = {"courseId","courseName","teacherId","department","credits"};
    private static final String[] ENROLL_HEADER = {"enrollmentId","studentId","courseId","enrollmentDate"};

    private List<Course> courses = new ArrayList<>();

    public CourseService() {
        CSVHandler.initFile(FILE, HEADER);
        CSVHandler.initFile(ENROLL, ENROLL_HEADER);
        loadFromCSV();
    }

    // ── CSV I/O ─────────────────────────────────────────────────────────────

    private void loadFromCSV() {
        courses.clear();
        List<String[]> rows = CSVHandler.readDataRows(FILE);
        for (String[] row : rows) {
            try {
                courses.add(new Course(row[0], row[1],
                    Integer.parseInt(row[2]), row[3], Integer.parseInt(row[4])));
            } catch (Exception e) {
                System.err.println("[CourseService] Skipping bad row: " + Arrays.toString(row));
            }
        }
    }

    private void saveToCSV() {
        List<String[]> rows = new ArrayList<>();
        rows.add(HEADER);
        for (Course c : courses) {
            rows.add(new String[]{c.getCourseId(), c.getCourseName(),
                String.valueOf(c.getTeacherId()), c.getDepartment(), String.valueOf(c.getCredits())});
        }
        CSVHandler.writeAll(FILE, rows);
    }

    // ── Public API ───────────────────────────────────────────────────────────

    public List<Course> getAllCourses() { return new ArrayList<>(courses); }

    public Optional<Course> findById(String courseId) {
        return courses.stream().filter(c -> c.getCourseId().equals(courseId)).findFirst();
    }

    public List<Course> getCoursesByTeacher(int teacherId) {
        List<Course> result = new ArrayList<>();
        for (Course c : courses) if (c.getTeacherId() == teacherId) result.add(c);
        return result;
    }

    public boolean addCourse(String courseId, String name, int teacherId, String dept, int credits) {
        for (Course c : courses) {
            if (c.getCourseId().equalsIgnoreCase(courseId)) {
                System.out.println("  Course ID already exists.");
                return false;
            }
        }
        courses.add(new Course(courseId, name, teacherId, dept, credits));
        saveToCSV();
        System.out.println("  Course created: " + courseId);
        return true;
    }

    public boolean removeCourse(String courseId) {
        boolean removed = courses.removeIf(c -> c.getCourseId().equalsIgnoreCase(courseId));
        if (removed) saveToCSV();
        return removed;
    }

    // ── Enrollments ──────────────────────────────────────────────────────────

    public boolean enrollStudent(int studentId, String courseId) {
        // Check if already enrolled
        List<String[]> enrollments = CSVHandler.readDataRows(ENROLL);
        for (String[] row : enrollments) {
            if (row[1].equals(String.valueOf(studentId)) && row[2].equals(courseId)) {
                System.out.println("  [!] Already enrolled in " + courseId);
                return false;
            }
        }
        // Check course exists
        if (findById(courseId).isEmpty()) {
            System.out.println("  [!] Course not found: " + courseId);
            return false;
        }
        int eId = enrollments.size() + 1;
        String enrollId = String.format("E%03d", eId);
        String date = java.time.LocalDate.now().toString();
        CSVHandler.appendRow(ENROLL, new String[]{enrollId, String.valueOf(studentId), courseId, date});
        System.out.println("  [+] Enrolled in course: " + courseId);
        return true;
    }

    public List<String[]> getEnrollmentsByStudent(int studentId) {
        List<String[]> result = new ArrayList<>();
        for (String[] row : CSVHandler.readDataRows(ENROLL)) {
            if (row[1].equals(String.valueOf(studentId))) result.add(row);
        }
        return result;
    }

    public void printAllCourses() {
        System.out.println("\n  ─────────────────────────── All Courses ───────────────────────────");
        if (courses.isEmpty()) { System.out.println("  No courses found."); return; }
        System.out.println("  " + "─".repeat(68));
        System.out.printf("  %-8s %-35s %-12s %-8s%n", "ID", "Course Name", "Department", "Credits");
        System.out.println("  " + "─".repeat(68));
        for (Course c : courses) {
            System.out.printf("  %-8s %-35s %-12s %-8d%n",
                c.getCourseId(), c.getCourseName(), c.getDepartment(), c.getCredits());
        }
    }

}
