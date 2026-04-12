package users;

/**
 * Abstract base class for all users in the COEP system.
 * Demonstrates Abstraction and Encapsulation (OOP principles).
 */
public abstract class User {

    private int id;
    private String name;
    private String role;
    private String department;
    private String password;

    public User(int id, String name, String role, String department, String password) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.department = department;
        this.password = password;
    }

    public int getId()               { return id; }
    public void setId(int id)        { this.id = id; }

    public String getName()          { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole()          { return role; }
    public void setRole(String role) { this.role = role; }

    public String getDepartment()              { return department; }
    public void setDepartment(String dep)      { this.department = dep; }

    public String getPassword()              { return password; }
    public void setPassword(String password)      { this.password = password; }


    public abstract void viewDashboard();


    protected void printHeader(String title) {
        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.printf( "║  %-40s║%n", title);
        System.out.println("╠══════════════════════════════════════════╣");
        System.out.printf( "║  User : %-32s ║%n", name);
        System.out.printf( "║  Role : %-32s ║%n", role);
        System.out.printf( "║  Dept : %-32s ║%n", department);
        System.out.println("╚══════════════════════════════════════════╝");
    }

    @Override
    public String toString() {
        return String.format("User[id=%d, name=%s, role=%s, dept=%s]", id, name, role, department);
    }
}
