# COEP System — College of Engineering Pune
## Student Management System · Core Java CLI Project

---

## Project Overview

A fully terminal-based Java application simulating the academic operations of COEP Technological University. The system supports four user roles — **Student**, **Teacher**, **Admin**, and **Exam Cell Staff** — each with their own dashboard and menu-driven workflows.

All data is persisted to CSV files under `data/database/`. No external libraries or databases are required.

---

## Project Structure

```
coep-system/
├── main/
│   └── Main.java                    # Entry point, main menu, login dispatcher
├── users/
│   ├── User.java                    # Abstract base class
│   ├── Student.java                 # Extends User
│   ├── Teacher.java                 # Extends User
│   ├── Admin.java                   # Extends User
│   └── ExamCellStaff.java           # Extends User
├── courses/
│   ├── Course.java                  # Course model
│   ├── Assignment.java              # Assignment model
│   ├── Submission.java              # Submission model
│   ├── Quiz.java                    # Quiz model
│   ├── QuizQuestion.java            # Question model
│   └── QuizAttempt.java             # Attempt/score model
├── exams/
│   ├── ExamForm.java                # Exam form model
│   └── Result.java                  # Exam result model
├── services/
│   ├── UserService.java             # User CRUD + CSV persistence
│   ├── CourseService.java           # Course/enrollment CRUD + CSV
│   ├── AssignmentService.java       # Assignment/submission CRUD + CSV
│   ├── ExamService.java             # Exam forms + results CRUD + CSV
│   ├── QuizService.java             # Quiz/question/attempt CRUD + CSV
│   └── NotificationService.java     # Async notifications via Threads
├── controllers/
│   ├── StudentController.java       # Student menu handling
│   ├── TeacherController.java       # Teacher menu handling
│   ├── AdminController.java         # Admin menu handling
│   └── ExamCellController.java      # Exam Cell menu handling
├── utils/
│   ├── CSVHandler.java              # readAll / writeAll / appendRow
│   └── InputValidator.java          # Safe terminal input helpers
├── data/
│   └── database/
│       ├── users.csv
│       ├── courses.csv
│       ├── enrollments.csv
│       ├── assignments.csv
│       ├── submissions.csv
│       ├── exam_forms.csv
│       ├── results.csv
│       ├── quizzes.csv
│       ├── quiz_questions.csv
│       └── quiz_attempts.csv
├── out/                             # Compiled .class files (auto-generated)
├── run.sh                           # One-click build & run (Linux/macOS)
└── README.md
```

---

## Java Concepts — How & Where They Are Used

### 1. Encapsulation

**What it is:** Bundling data (fields) and methods that operate on that data inside a single class, and restricting direct access to fields using `private` with controlled `public` getters and setters.

**Where it is used:**

Every model class in the project fully encapsulates its data.

`User.java` — All five fields (`id`, `name`, `role`, `department`, `password`) are declared `private`. External classes can only read or modify them through the provided getters and setters:
```java
private int id;
private String name;
private String role;
private String department;
private String password;

public int getId()               { return id; }
public void setId(int id)        { this.id = id; }
public String getPassword()      { return password; }
public void setPassword(String password) { this.password = password; }
```

`Course.java`, `Assignment.java`, `Quiz.java`, `QuizQuestion.java`, `QuizAttempt.java`, `ExamForm.java`, `Result.java` — All follow the same pattern: `private` fields with public getters/setters only.

`NotificationService.java` — Internal state (`notificationThreads`, `threadLock`) is `private final`, preventing external code from interfering with thread management.

---

### 2. Inheritance

**What it is:** A child class acquiring the properties and behaviours of a parent class using the `extends` keyword.

**Where it is used:**

`User.java` is the abstract parent class. All four role-specific classes extend it:

```
User  (abstract)
 ├── Student       extends User
 ├── Teacher       extends User
 ├── Admin         extends User
 └── ExamCellStaff extends User
```

Each subclass calls `super(...)` to initialise the common fields and hardcodes its own role string:
```java
// Student.java
public Student(int id, String name, String department, String password) {
    super(id, name, "STUDENT", department, password);
}

// Teacher.java
public Teacher(int id, String name, String department, String password) {
    super(id, name, "TEACHER", department, password);
}
```

This means `Student`, `Teacher`, `Admin`, and `ExamCellStaff` all automatically have `getId()`, `getName()`, `getDepartment()`, `getPassword()`, `setPassword()`, and `printHeader()` without any code duplication.

`Main.java` uses the inherited `getName()` method directly on any role after login:
```java
System.out.println("  Logged in successfully as , " + user.getName() + "!!");
```

---

### 3. Polymorphism

**What it is:** The ability of a single method call to behave differently depending on the actual object type at runtime (runtime/dynamic polymorphism via method overriding).

**Where it is used:**

`User.java` declares `viewDashboard()` as `abstract` — forcing every subclass to provide its own implementation:
```java
// User.java
public abstract void viewDashboard();
```

Each subclass overrides it with its own role-specific menu:
```java
// Student.java
@Override
public void viewDashboard() {
    printHeader("STUDENT DASHBOARD");
    System.out.println("  1. Enroll in Course (Dept-restricted)");
    System.out.println("  2. Submit Assignment");
    // ...
}

// Teacher.java
@Override
public void viewDashboard() {
    printHeader("TEACHER DASHBOARD");
    System.out.println("  1. Create Course (Dept auto-set)");
    System.out.println("  2. Upload Study Material (Simulation)");
    // ...
}
```

In `UserService.java`, the `createUser()` factory method returns the parent type `User`, but the actual runtime object is the appropriate subclass. The caller does not need to know the concrete type:
```java
private User createUser(int id, String name, String role, String dept, String password) {
    switch (role.toUpperCase()) {
        case "STUDENT":   return new Student(id, name, dept, password);
        case "TEACHER":   return new Teacher(id, name, dept, password);
        case "ADMIN":     return new Admin(id, name, dept, password);
        case "EXAM_CELL": return new ExamCellStaff(id, name, dept, password);
    }
}
```

In `Main.java`, the `User` reference returned from `login()` is cast to the correct type and dispatched polymorphically:
```java
User user = userService.login(id, password);
// ...
new StudentController((Student) user, ...).handleMenu();
```

---

### 4. Abstraction

**What it is:** Hiding internal implementation details and exposing only what is necessary. In Java this is achieved using `abstract` classes and interfaces.

**Where it is used:**

`User.java` is declared `abstract`. It cannot be instantiated directly — you can never do `new User(...)`. It defines the contract (what all users must support) without dictating how:
```java
public abstract class User {
    // Common data shared by all roles
    private int id;
    private String name;
    // ...

    // Contract: every role MUST implement this
    public abstract void viewDashboard();

    // Shared behaviour available to all subclasses
    protected void printHeader(String title) { ... }
}
```

`CSVHandler.java` provides an abstracted persistence layer. All services (`UserService`, `CourseService`, `QuizService`, etc.) call clean, high-level methods like `readDataRows()`, `writeAll()`, and `appendRow()` without ever knowing about `BufferedReader`, file streams, or CSV quoting rules. The complexity is hidden inside `CSVHandler`.

`NotificationService.java` abstracts thread creation. Controllers simply call `notifyGraded(studentName, title, marks)` — they have no awareness that a `Thread` is being created and started underneath.

---

### 5. Generics

**What it is:** Writing classes and methods that work with any type using type parameters (`<T>`), providing type safety at compile time without casting.

**Where it is used:**

`UserService.java`:
```java
private Map<Integer, User> userMap = new HashMap<>();   // Maps ID → User
public List<User> getAllUsers() { ... }
public List<User> getUsersByRole(String role) { ... }
```

`CourseService.java`:
```java
private List<Course> courses = new ArrayList<>();
public List<Course> getAllCourses() { ... }
public List<Course> getCoursesByDepartment(String department) { ... }
public Optional<Course> findById(String courseId) { ... }  // Optional<T> generic
```

`AssignmentService.java`:
```java
private List<Assignment> assignments = new ArrayList<>();
private List<Submission> submissions = new ArrayList<>();
```

`QuizService.java`:
```java
private List<Quiz>         quizzes   = new ArrayList<>();
private List<QuizQuestion> questions = new ArrayList<>();
private List<QuizAttempt>  attempts  = new ArrayList<>();

public List<Quiz> getQuizzesForStudent(List<String> enrolledCourseIds) { ... }
public Optional<Quiz> findQuizById(String quizId) { ... }
```

`CSVHandler.java`:
```java
public static List<String[]> readAll(String filePath) { ... }
public static void writeAll(String filePath, List<String[]> data) { ... }
```

`NotificationService.java`:
```java
private final List<Thread> notificationThreads = new ArrayList<>();
```

Using generics means the compiler catches type mismatches at compile time — for example, accidentally adding a `Course` into a `List<User>` would be a compile error, not a runtime crash.

---

### 6. Exception Handling (try-catch)

**What it is:** A structured mechanism to detect, catch, and recover from runtime errors using `try`, `catch`, and `finally` blocks, preventing the application from crashing unexpectedly.

**Where it is used:**

`InputValidator.java` — Prevents crashes from bad terminal input. If a user types `"abc"` when an integer is expected, `NumberFormatException` is caught and the user is re-prompted:
```java
public static int readInt(String prompt) {
    while (true) {
        System.out.print(prompt);
        try {
            String line = scanner.nextLine().trim();
            return Integer.parseInt(line);  // throws NumberFormatException if not a number
        } catch (NumberFormatException e) {
            System.out.println("  [!] Invalid input. Please enter a valid integer.");
        }
    }
}
```

`CSVHandler.java` — All file I/O is wrapped in try-with-resources, which also auto-closes streams:
```java
try (BufferedReader br = new BufferedReader(new FileReader(file))) {
    String line;
    while ((line = br.readLine()) != null) { ... }
} catch (IOException e) {
    System.err.println("[CSVHandler] Error reading file: " + filePath + " -> " + e.getMessage());
}
```

`UserService.java` / `CourseService.java` / `AssignmentService.java` / `QuizService.java` — Every CSV row parse is wrapped in try-catch so a single malformed row is skipped without crashing the entire data load:
```java
for (String[] row : CSVHandler.readDataRows(FILE)) {
    try {
        int id = Integer.parseInt(row[0]);
        // ...
        userMap.put(id, user);
    } catch (Exception e) {
        System.err.println("[UserService] Skipping bad row: " + Arrays.toString(row));
    }
}
```

`NotificationService.java` — `InterruptedException` is caught inside notification threads to handle thread interruption gracefully:
```java
try {
    Thread.sleep(300);
    System.out.println("  [NOTIFICATION -> " + recipientName + "] " + message);
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();  // restore the interrupted status
}
```

`Main.java` — A `RuntimeException` with message `"PASSWORD_CHANGED"` is used as a signal to forcibly log out a user after a password change:
```java
try {
    new StudentController(...).handleMenu();
} catch (RuntimeException e) {
    if ("PASSWORD_CHANGED".equals(e.getMessage())) {
        System.out.println("  [SECURITY] Password changed. Please log in again.");
    } else {
        throw e;  // re-throw unexpected exceptions
    }
}
```

---

### 7. Packages

**What it is:** A namespace mechanism to group related classes, avoid name conflicts, and control access between different parts of the application.

**Where it is used:**

The project is divided into 7 packages, each with a clear responsibility:

| Package | Classes | Responsibility |
|---|---|---|
| `main` | `Main.java` | Application entry point and login routing |
| `users` | `User`, `Student`, `Teacher`, `Admin`, `ExamCellStaff` | User domain model and role hierarchy |
| `courses` | `Course`, `Assignment`, `Submission`, `Quiz`, `QuizQuestion`, `QuizAttempt` | Academic content models |
| `exams` | `ExamForm`, `Result` | Examination-related models |
| `services` | `UserService`, `CourseService`, `AssignmentService`, `ExamService`, `QuizService`, `NotificationService` | Business logic and CSV persistence |
| `controllers` | `StudentController`, `TeacherController`, `AdminController`, `ExamCellController` | User interaction and menu handling |
| `utils` | `CSVHandler`, `InputValidator` | Reusable utility helpers |

Each file declares its package at the top:
```java
package users;      // in User.java, Student.java, Teacher.java etc.
package services;   // in UserService.java, CourseService.java etc.
package utils;      // in CSVHandler.java, InputValidator.java
```

`Main.java` imports all packages it depends on using wildcard imports:
```java
import controllers.*;
import services.*;
import users.*;
import utils.*;
```

Services import from both `users` and `courses` packages, showing cross-package dependency:
```java
// UserService.java
import users.*;
import utils.CSVHandler;

// AssignmentService.java
import courses.Assignment;
import courses.Submission;
import utils.CSVHandler;
```

This separation ensures that changing the CSV format in `CSVHandler` only affects `services`, not `users` or `courses`.

---

### 8. Concurrent Programming & Multithreading

**What it is:** Running multiple threads simultaneously so that independent tasks (like sending notifications) do not block the main program flow.

**Where it is used:**

`NotificationService.java` is the dedicated concurrent component. Instead of making the user wait while a notification is "sent", each notification is dispatched on its own background thread.

**Thread creation and naming:**
```java
public void sendInThread(String recipientName, String message) {
    Thread notificationThread = new Thread(() -> {
        try {
            Thread.sleep(300);  // simulates network/email delay
            System.out.println("  [NOTIFICATION -> " + recipientName + "] " + message);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    });

    notificationThread.setName("NotificationThread-" + recipientName);  // named for debugging
    // ...
    notificationThread.start();  // starts concurrently — caller is NOT blocked
}
```

**Thread-safe list with `synchronized`:**
The list of active threads is accessed from multiple threads (the main thread adds to it; `shutdown()` iterates it), so access is protected with a `synchronized` block using an explicit lock object:
```java
private final List<Thread> notificationThreads = new ArrayList<>();
private final Object threadLock = new Object();

// Safe write from main thread
synchronized (threadLock) {
    notificationThreads.add(notificationThread);
}

// Safe read during shutdown
synchronized (threadLock) {
    for (Thread thread : notificationThreads) {
        if (thread.isAlive()) {
            thread.join();  // wait for each thread to finish
        }
    }
}
```

**Parallel broadcast:**
`broadcastInThreads()` sends to multiple recipients at the same time — each gets its own thread, so all notifications are dispatched in parallel rather than sequentially:
```java
public void broadcastInThreads(String[] recipients, String message) {
    for (String recipient : recipients) {
        sendInThread(recipient, message);  // each call creates and starts a new thread
    }
}
```

**Graceful shutdown with `thread.join()`:**
When the application exits, `Main.java` calls `notificationService.shutdown()`. This iterates all tracked threads and calls `join()` on each — guaranteeing that every pending notification is delivered before the JVM exits:
```java
notificationService.shutdown();  // called in Main.java before exit
```

**Convenience methods wiring it all together:**
Controllers call these high-level methods without any thread-related code in the controller itself:
```java
notificationService.notifyGraded(studentName, assignmentTitle, marks);
notificationService.notifyResultPublished(studentName, courseId, grade);
notificationService.notifyExamFormApproved(studentName, courseId);
notificationService.notifyEnrollment(studentName, courseId);
```

---

## Prerequisites

- **Java JDK 11 or higher** (must have `javac` and `java` on your PATH)

### Install JDK

**Ubuntu / Debian:**
```bash
sudo apt-get update && sudo apt-get install default-jdk
```

**macOS (Homebrew):**
```bash
brew install openjdk
```

**Windows:**
Download from https://adoptium.net and add to PATH.

---

## How to Compile & Run

### Option A — One-click script (Linux / macOS)
```bash
chmod +x run.sh
./run.sh
```
The script auto-detects `javac`, compiles all source files into `out/`, and launches the application.

### Option B — Manual (all platforms)

**Step 1 — Compile** (from inside the `coep-system/` directory):
```bash
javac -d out \
  utils/*.java \
  users/*.java \
  courses/*.java \
  exams/*.java \
  services/*.java \
  controllers/*.java \
  main/*.java
```

**Step 2 — Run:**
```bash
java -cp out main.Main
```

### Windows CMD equivalent:
```cmd
mkdir out
javac -d out utils\*.java users\*.java courses\*.java exams\*.java services\*.java controllers\*.java main\*.java
java -cp out main.Main
```

---

## Login Credentials

The system uses **numeric User ID + Password** for authentication.

### Students (IDs 1–20)

| ID | Name | Department | Password |
|---|---|---|---|
| 1 | Jaivardhan Malhotra | Computer Engineering | `Jai@123` |
| 2–20 | (various students) | Mixed departments | `123345` |

### Teachers (IDs 21–35)

| ID | Name | Department | Password |
|---|---|---|---|
| 21 | Dr. Sunita Kulkarni | Computer Engineering | `Teacher@coep` |
| 22 | Dr. Rajesh Patil | Computer Engineering | `Teacher@coep` |
| 23–35 | (various teachers) | Mixed departments | `Teacher@coep` |

### Admin

| ID | Name | Password |
|---|---|---|
| 36 | Admin User | `Admin@coeptech` |

### Exam Cell Staff

| ID | Name | Password |
|---|---|---|
| 37 | Exam Officer 1 | `123456789` |
| 38 | Exam Officer 2 | `123456789` |

> **Default passwords when Admin creates a new user:**
> - Student → `student@coep`
> - Teacher → `Teacher@coep`
> - Admin → `Admin@coeptech`
> - Exam Cell → `examcell@coep`
>
> All users can change their own password after login. Changing a password immediately invalidates the current session and requires re-login.

---

## Role Dashboards & Features

### Student
1. Enroll in Course *(department-restricted — only courses from your department are shown)*
2. Submit Assignment
3. Fill Exam Form
4. View My Results
5. View My Enrollments
6. View Submission History
7. Quiz Menu *(attempt quizzes, view past attempts)*
8. Change Password

### Teacher
1. Create Course *(auto-assigned to teacher's own department)*
2. Upload Course Material *(simulated)*
3. Create Assignment
4. Grade Submissions
5. Create Quiz *(add questions, set time limit and max attempts)*
6. View My Courses
7. Change Password

### Admin
1. Add User *(default password auto-assigned based on role)*
2. Remove User
3. List All Users
4. Manage Courses *(view/delete)*
5. Department Summary
6. Change Password

### Exam Cell Staff
1. View Pending Exam Forms
2. Approve Exam Form
3. Upload Marks
4. Publish Results
5. View All Results
6. Change Password

---

## CSV Database Schema

### users.csv
```
id, name, role, department, password
```

### courses.csv
```
courseId, courseName, teacherId, department, credits
```

### enrollments.csv
```
enrollmentId, studentId, courseId, enrollmentDate
```

### assignments.csv
```
assignmentId, title, courseId, teacherId, dueDate, maxMarks
```

### submissions.csv
```
submissionId, assignmentId, studentId, submissionDate, marksObtained, graded
```

### exam_forms.csv
```
formId, studentId, courseId, examType, status, appliedDate
```

### results.csv
```
resultId, studentId, courseId, examType, marksObtained, maxMarks, grade, published
```

### quizzes.csv
```
quizId, title, courseId, teacherId, timeLimitMinutes, maxAttempts
```

### quiz_questions.csv
```
questionId, quizId, questionText, answer
```

### quiz_attempts.csv
```
attemptId, quizId, studentId, score, totalQuestions, attemptDate, completed
```

---

## Grading Scale

| Percentage | Grade |
|---|---|
| >= 90 | O (Outstanding) |
| >= 75 | A |
| >= 60 | B |
| >= 50 | C |
| >= 40 | D |
| < 40  | F |

---

## Sample Session

```
╔══════════════════════════════════════════╗
║  College of Engineering Pune (COEP)      ║
║  Student Management System               ║
╚══════════════════════════════════════════╝

             MAIN MENU

    1. Login as Student
    2. Login as Teacher
    3. Login as Admin
    4. Login as Exam Cell Staff
    5. Exit

  Select an option: 1
  Enter your User ID: 1
  Enter your Password: ********

  Logged in successfully as, Jaivardhan Malhotra!!

STUDENT DASHBOARD
  1. Enroll in Course
  2. Submit Assignment
  3. Fill Exam Form
  4. View My Results
  5. View My Enrollments
  6. View Submission History
  7. Quiz Menu
  8. Change Password
  0. Logout
```