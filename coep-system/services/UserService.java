package services;

import users.*;
import utils.CSVHandler;

import java.util.*;

/**
 * Service for managing users. Uses Generics (List<User>, Map<Integer,User>).
 * Reads/writes from users.csv for persistence.
 */
public class UserService {

    private static final String FILE = "data/database/users.csv";
    private static final String[] HEADER = {"id", "name", "role", "department"};

    private Map<Integer, User> userMap = new HashMap<>();

    public UserService() {
        CSVHandler.initFile(FILE, HEADER);
        loadFromCSV();
    }

    // ── CSV I/O ─────────────────────────────────────────────────────────────

    private void loadFromCSV() {
        userMap.clear();
        List<String[]> rows = CSVHandler.readDataRows(FILE);
        for (String[] row : rows) {
            try {
                int id         = Integer.parseInt(row[0]);
                String name    = row[1];
                String role    = row[2];
                String dept    = row.length > 3 ? row[3] : "General";
                User user = createUser(id, name, role, dept);
                if (user != null) userMap.put(id, user);
            } catch (NumberFormatException e) {
                System.err.println("[UserService] Skipping bad row: " + Arrays.toString(row));
            }
        }
    }

    private void saveToCSV() {
        List<String[]> rows = new ArrayList<>();
        rows.add(HEADER);
        for (User u : userMap.values()) {
            rows.add(new String[]{
                String.valueOf(u.getId()), u.getName(), u.getRole(), u.getDepartment()
            });
        }
        CSVHandler.writeAll(FILE, rows);
    }

    private User createUser(int id, String name, String role, String dept) {
        switch (role.toUpperCase()) {
            case "STUDENT":   return new Student(id, name, dept);
            case "TEACHER":   return new Teacher(id, name, dept);
            case "ADMIN":     return new Admin(id, name, dept);
            case "EXAM_CELL": return new ExamCellStaff(id, name, dept);
            default:
                System.err.println("[UserService] Unknown role: " + role);
                return null;
        }
    }

    // ── Public API ───────────────────────────────────────────────────────────

    public User findById(int id) {
        return userMap.get(id);
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(userMap.values());
    }

    public List<User> getUsersByRole(String role) {
        List<User> result = new ArrayList<>();
        for (User u : userMap.values()) {
            if (u.getRole().equalsIgnoreCase(role)) result.add(u);
        }
        return result;
    }

    public boolean addUser(String name, String role, String department) {
        int newId = userMap.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
        User user = createUser(newId, name, role, department);
        if (user == null) return false;
        userMap.put(newId, user);
        saveToCSV();
        System.out.println("  [+] User added with ID: " + newId);
        return true;
    }

    public boolean removeUser(int id) {
        if (!userMap.containsKey(id)) {
            System.out.println("  [!] User ID " + id + " not found.");
            return false;
        }
        userMap.remove(id);
        saveToCSV();
        System.out.println("  [+] User ID " + id + " removed.");
        return true;
    }

    public void printAllUsers() {
        System.out.println("\n  ── All Users ──────────────────────────────────");
        if (userMap.isEmpty()) { System.out.println("  No users found."); return; }
        System.out.printf("  %-5s %-25s %-12s %-25s%n", "ID", "Name", "Role", "Department");
        System.out.println("  " + "─".repeat(70));
        for (User u : userMap.values()) {
            System.out.printf("  %-5d %-25s %-12s %-25s%n",
                u.getId(), u.getName(), u.getRole(), u.getDepartment());
        }
    }
}
