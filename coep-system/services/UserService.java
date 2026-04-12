package services;

import java.util.*;
import users.*;
import utils.CSVHandler;

/**
 * Service for managing users. Uses Generics (List<User>, Map<Integer,User>).
 * Reads/writes from users.csv for persistence.
 */

public class UserService {

    private static final String FILE = "data/database/users.csv";
    private static final String[] HEADER = {"id", "name", "role", "department", "password"};

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
                    String password = row.length > 4 ? row[4] : "";

                    User user = createUser(id, name, role, dept, password);

                    if (user != null) userMap.put(id, user);

                } catch (Exception e) {
                    System.err.println("[UserService] Skipping bad row: " + Arrays.toString(row));
                }
            }
        }

        private void saveToCSV() {
        List<String[]> rows = new ArrayList<>();
        rows.add(HEADER);

        for (User u : userMap.values()) {
            rows.add(new String[]{
                String.valueOf(u.getId()),
                u.getName(),
                u.getRole(),
                u.getDepartment(),
                u.getPassword()
            });
        }

        CSVHandler.writeAll(FILE, rows);
    }

    private User createUser(int id, String name, String role, String dept, String password) {
        switch (role.toUpperCase()) {
            case "STUDENT":   return new Student(id, name, dept, password);
            case "TEACHER":   return new Teacher(id, name, dept, password);
            case "ADMIN":     return new Admin(id, name, dept, password);
            case "EXAM_CELL": return new ExamCellStaff(id, name, dept, password);
            default:
                System.err.println("[UserService] Unknown role: " + role);
                return null;
        }
    }

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

    public boolean addUser(String name, String role, String department, String password) {
        int newId = 1;
            for (int id : userMap.keySet()) {
                if (id >= newId) newId = id + 1;
        }

        User user = createUser(newId, name, role, department, password);

        if (user == null) return false;
        userMap.put(newId, user);

        saveToCSV();

        System.out.println("  User added with ID: " + newId);
        return true;
    }

    public boolean removeUser(int id) {
        if (!userMap.containsKey(id)) {
            System.out.println("  User ID " + id + " not found.");
            return false;
        }
        userMap.remove(id);
        saveToCSV();
        System.out.println("  User ID " + id + " removed.");
        return true;
    }

    public User login(int id, String password) {
        User user = userMap.get(id);

        if (user == null) {
            System.out.println("  User not found.");
            return null;
        }

        if (!user.getPassword().equals(password)) {
            System.out.println("  Incorrect password.");
            return null;
        }

        return user;
    }

    /**
     * Changes the password for a user after verifying the current password.
     * Persists the change to CSV immediately.
     *
     * @param userId          ID of the user
     * @param currentPassword Current password to verify identity
     * @param newPassword     New password to set
     * @return true if changed successfully, false otherwise
     */
    public boolean changePassword(int userId, String currentPassword, String newPassword) {
        User user = userMap.get(userId);
        if (user == null) {
            System.out.println("  [!] User not found.");
            return false;
        }
        if (!user.getPassword().equals(currentPassword)) {
            System.out.println("  [!] Incorrect current password.");
            return false;
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            System.out.println("  [!] New password cannot be empty.");
            return false;
        }
        if (newPassword.equals(currentPassword)) {
            System.out.println("  [!] New password must be different from the current password.");
            return false;
        }
        user.setPassword(newPassword);
        saveToCSV();
        System.out.println("  [✓] Password changed successfully. You will be logged out.");
        return true;
    }

    public void printAllUsers() {
        System.out.println("\n  All Users \n");

        System.out.printf("  %-5s %-25s %-12s %-25s%n", "ID", "Name", "Role", "Department");
        System.out.println("--------------------------------------------------------------------");    
            
        for (User u : userMap.values()) {
            System.out.printf("  %-5d %-25s %-12s %-25s%n",
                u.getId(), u.getName(), u.getRole(), u.getDepartment());
        }
    }

    /**
     * Returns all unique departments present in user data, sorted lexicographically.
     */
    public List<String> getAllDepartments() {
        Set<String> depts = new TreeSet<>();
        for (User u : userMap.values()) depts.add(u.getDepartment());
        return new ArrayList<>(depts);
    }
}
